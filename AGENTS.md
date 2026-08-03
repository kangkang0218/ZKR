# Agents Instructions

## 版本号规则

每次构建镜像前，必须先读取当前运行中的容器镜像版本号，新版本 = 当前版本号 + 1：

```bash
# 查看当前运行版本
docker inspect zkr-erp-backend --format '{{.Config.Image}}'
docker inspect zkr-lab-erp-demo --format '{{.Config.Image}}'
```

| 规则 | 说明 |
|------|------|
| 版本号递增 | 后端从 `v1.XX` → `v1.XX+1`，前端从 `v1.XX` → `v1.XX+1` |
| 禁止时间戳 | 禁止使用 `date +%Y%m%d%H%M` 作为版本号 |
| 构建前确认 | 构建前必须确认当前运行中的实际版本号 |

## 部署流程

1. 读取当前版本号
2. 新版本 = 当前版本 + 1
3. 构建前后端镜像
4. `docker push` 两个版本
5. 更新 `docker-compose.yml` 中的 image tag
6. `docker compose up -d erp-backend lab-erp-demo`
7. 验证容器状态
8. git commit 版本号变更

---

## 已知 Bug 记录

### 2026-08-03 17:05 — 修复负责人（如 lihaotian，角色 DATA）看不到「项目文件」入口

**原因：** 上一版把前端入口可见性绑在 `userStore.isManager`（role ∈ ADMIN/MANAGER/BUSINESS）上，但负责人是**数据身份**（`sys_project.manager_id` 或 BD/BUSINESS 成员），角色为 DATA/DEV 等的负责人（如 lihaotian，DATA 角色、负责「亦庄_EBOM」）按钮不显示，路由 `allowedRoles` 同样拦截。

**改动位置：**
- `lab-erp-demo/src/stores/userStore.js` — 新增 `managedProjectCount`（null=未加载）、`hasManagedProjects`（count>0）、`refreshManagedProjects()`（复用 `GET /api/projects/managed`，非管理员返回 `findManagedProjects`）、`ensureManagedProjectsLoaded()`（幂等加载）；`logoutErp` 时重置为 null
- `lab-erp-demo/src/App.vue:221,370-375` — 按钮条件改为 `isErpLoggedIn && (白名单 || userStore.hasManagedProjects)`（移除 isManager）；`watch(isErpLoggedIn)` 登录时自动刷新
- `lab-erp-demo/src/router/index.js:150-159,224-282` — `/admin/project-files` meta 由 `allowedRoles` 改为 `requiresProjectFileManager`；守卫改为 async，先 `await ensureManagedProjectsLoaded()` 再判 `!isProvisionAdmin && !hasManagedProjects` 则跳转；allowedRoles 分支恢复原语义
- 部署：frontend `v1.178`（后端 v1.161 无改动，后端门禁本身数据驱动本就正确）

**验证（真实 JWT 实测）：** lihaotian → `/api/projects/managed` 200 返回「亦庄_EBOM」、文件管理器 `/projects` 200 仅此项目；前端按钮条件与后端门禁完全对齐（白名单 || hasManagedProjects）。

**效果：** 「项目文件」入口改为数据驱动判定，任何有负责项目的用户（无论角色）都能看到入口并只操作自己负责的项目。

### 2026-08-03 16:22 — 项目文件管理器对负责人开放（仅可见/操作自己负责的项目）

**原因：** 用户需求：左上角「项目文件」按钮原为 Provision Admin 专用（Zhangqi/guojianwen/jiaomiao），要求改为负责人也可见；非白名单负责人只能看到并操作自己负责的项目的项目文件夹（全部操作权限），白名单维持现状。要求复用既有项目可见性机制（不新写权限体系）。

**改动位置：**
- `erp-backend/.../projectfile/ProjectFileManagerController.java` — 原 `requireProvisionAdmin()` 门禁拆分为：`requireProjectFileAccess()`（白名单放行，否则须为负责人——复用 `SysProjectRepository.findManagedProjects`）、`requireProjectFileAccess(projectId)`（单项目归属校验，非白名单校验 projectId ∈ 其负责项目集合）、`requireMappingAccess(mappingId)`/`requireMappingAccess(ids)`（mapping/目录解析出 projectId 后校验，含批量端点）；`POST /scan` 保持仅白名单
- `erp-backend/.../projectfile/ProjectFileManagerService.java` — `listProjects()` 改为 `listProjects(boolean admin, String userId)`：白名单返回全部，负责人返回 `findManagedProjects(userId)`；新增 `getMappingProjectId()`/`getFolderProjectId()` 归属解析助手
- `lab-erp-demo/src/App.vue:221` — 按钮可见性：`isErpLoggedIn && (isManager || canAccessProvisioning(username))`（复用 `userStore.isManager` = role ∈ ADMIN/MANAGER/BUSINESS）
- `lab-erp-demo/src/router/index.js:154-158,257` — `/admin/project-files` meta 由 `requiresProvisionAdmin` 改为 `allowedRoles: ['ADMIN','MANAGER','BUSINESS']`（复用既有 allowedRoles 守卫分支）；守卫分支 ⑤ 放行条件加 `&& !isProvisionAdmin`，保证白名单用户（如 Zhangqi 角色 DATA_ENGINEER）不受 allowedRoles 拦截
- 部署：backend `v1.161`、frontend `v1.177`

**验证（真实 JWT 实测）：** 负责人 neibu → `/projects` 仅返回自己 4 个项目、自己项目 tree 200、他人项目 tree 403「仅负责人可操作」；Zhangqi（白名单）→ 全部 35 个项目 + `/scan` 200；BUSINESS 但无负责项目、普通成员 → 403。

**效果：** 项目文件管理器从「仅白名单」开放为「白名单（全量）+ 负责人（仅自己负责的项目，全部操作权限）」；ProjectDetail 只读文件树面板保持仅白名单可见。

### 2026-08-03 16:30 — 修复全局业务检索无反应 + 一句话报表 month 筛选报错

**原因：** ① 运行中的 `finance-rag-api` 容器由旧项目副本（`/home/a/zhangqi/5090/workspace/ZKR`）启动，环境变量是 ollama 默认值（qwen3:8b / qwen3-embedding:4b，请求不可达的 host.docker.internal:11434），且 Qdrant 索引从未建成，导致全局业务检索无数据可用；同时探测确认 **OpenCode AI 网关（https://opencode.ai/zen/go/v1）只提供 /chat/completions，不提供 /embeddings**，向量检索无法使用 text-embedding-3-small。② 一句话报表偶发「筛选 month 缺少过滤值」：大模型输出的 filter 缺少 values，校验器直接抛错终止生成。

**改动位置：**
- `rag-service/app.py` — 新增关键词检索模式（`SEARCH_MODE=keyword`，默认启用）：`tokenize()` 中英文双字分词，`index_blocks_keyword()`/`search_keyword()` 基于 Redis 存储上下文块做关键词打分，答案仍由 LLM（deepseek-v4-pro）生成；向量模式代码保留（未来接入 embedding 服务可切回）
- `docker-compose.yml:106` — rag 服务新增 `SEARCH_MODE: ${FINANCE_RAG_SEARCH_MODE:-keyword}` 环境变量
- `erp-backend/.../finance/service/ReportSpecParserService.java` — system prompt 强化：filter 必须含 field/op/values 且 values≥1 个具体值，无法确定时省略该 filter
- `erp-backend/.../finance/service/FinanceReportService.java` — 新增 `normalizeFilters()`：解析后自动丢弃 values 为空的 filter（记录日志），保证报表仍能生成
- 运维：从当前目录重建 rag 三件套接管旧容器；旧副本 `5090/workspace/ZKR` 移至 `5090_backup/ZKR_20260803`

**效果：** 全局业务检索恢复可用（关键词检索 + DeepSeek V4 Pro 生成「结论/依据/建议」）；一句话报表不再因空 filter 报错。当前实际使用模型：LLM=`deepseek-v4-pro`（OpenCode AI），检索=关键词模式（embedding 服务暂无可用供应商）。

### 2026-08-03 15:30 — 财务「一句话生成可视化报表」功能

**原因：** 用户需求：财务系统支持用一句话自然语言生成可视化报表，图表必须由 LLM 实时渲染，可选的报表描述存库仅用于一键复跑。

**方案：** LLM 生成受限 JSON 报表规格（ReportSpec），Java 白名单校验后查询聚合，ECharts 渲染；不缓存图表结果，每次复跑都由大模型实时生成。

**改动位置：**
- `erp-backend/.../finance/enums/ReportSource.java` — 数据源白名单（expense_submission/cost_entry/bank_balance），声明可用维度/度量字段并做统一字段抽取
- `erp-backend/.../finance/service/ReportSpecParserService.java` — 复用 `LlmClient`（OpenCode AI deepseek-v4-pro）将自然语言解析为 ReportSpec JSON，相对时间自动换算为具体月份过滤值
- `erp-backend/.../finance/service/ReportDataService.java` — 白名单校验（图表/字段/聚合/筛选）+ 内存过滤聚合，行数上限 500
- `erp-backend/.../finance/service/FinanceReportService.java` + `finance/controller/FinanceReportController.java` — `POST /api/finance/report/generate`、`GET/DELETE /api/finance/report/prompts`（Finance 域）
- `erp-backend/.../finance/entity/FinanceReportPrompt.java` + `repository/FinanceReportPromptRepository.java` + `db/migration/V20260803_001__create_finance_report_prompt.sql` — 只存 prompt 的历史表
- `lab-erp-demo/src/api/finance/report.js` — 报表 API 封装
- `lab-erp-demo/src/views/finance/FinanceReportView.vue` — 一句话输入 + 示例 chips + 保存开关 + 历史记录（复跑/删除）+ echarts@6 渲染（柱状/折线/饼图/数字/表格）+ CSV 导出
- `lab-erp-demo/src/views/finance/FinanceAiHub.vue:11-21` — 新增「一句话报表」Tab

**效果：** 财务用户可在 AI 业务中心输入如"近三个月各项目费用柱状图"实时生成图表；保存的报表描述可一键复跑获取最新数据，图表始终由 LLM 实时渲染。

### 2026-07-31 11:00 — 项目文件管理器三连修复：光标/上传/下载异常

**原因：** 用户反馈三个问题：① 鼠标悬停表格行显示 I-beam 文本光标，② 上传文件报"系统内部错误"，③ 下载文件报"下载失败"。

**根因分析：**
1. **光标**：`.name-cell` / `.item-name` 未设置 `cursor: pointer`，浏览器默认对文本显示 `cursor: text`
2. **上传 Content-Type**：前端手动设置 `Content-Type: multipart/form-data` 缺少 boundary，服务端无法解析
3. **上传 source_id 列过短**：DB 中 `project_file_mapping.source_id` 为 `varchar(64)`，但上传路径含 UUID+中文文件名，超过 64 字符
4. **上传 source_type check constraint 缺失**：DB check constraint 未包含 `UPLOADED_FILE`，插入被拒绝
5. **后端异常吞没**：`GlobalExceptionHandler` 缺少 `IllegalArgumentException` 和 `RuntimeException` handler，全部落入泛化 `Exception` handler 返回硬编码"系统内部错误"
6. **前端 blob 错误提取**：下载/预览使用 `responseType: 'blob'`，错误时 `e.response.data` 为 Blob，未从中解析 JSON 错误消息

**改动位置：**
- `lab-erp-demo/src/views/ProjectFileManagerView.vue:548-551,758-772` — 光标修复：`.name-cell` / `.item-name` 新增 `cursor: pointer; user-select: none`；上传移除错误 `Content-Type` header；新增 `extractBlobError()` 从 blob 错误响应解析 message
- `erp-backend/.../GlobalExceptionHandler.java:40-50` — 新增 `IllegalArgumentException`（400，透传消息）和 `RuntimeException`（500，透传消息）handler
- DB `project_file_mapping` — `source_id` `varchar(64)` → `varchar(512)`；check constraint 新增 `'UPLOADED_FILE'`

**效果：** 光标恢复正常 pointer；上传不再因 header/列宽/constraint 三重问题失败；下载/预览错误消息正确显示。

### 2026-07-20 09:55 — 部署 PR #7 到 v1.156/v1.174

**原因：** 合并并部署 PR #7 到本地容器。

**改动位置：**
- `docker-compose.yml:21,123` — 后端镜像更新为 `v1.156`，前端镜像更新为 `v1.174`。

**效果：** 容器 `zkr-erp-backend` / `zkr-lab-erp-demo` 已更新运行，PR #7 所有改动已生效。

### 2026-07-20 10:00 — 合并 PR #7（工单修复 + 员工详情弹窗 + 费用审批重构）

**原因：** 合并同事 aurura12 提交的 PR #7，包含工单 4/5/6/7/12 项修复及多项前端功能增强。

**改动位置：**
- `erp-backend/.../AdminUserController.java` — 新增 `GET /api/admin/users/{userId}/profile` 员工详情接口。
- `erp-backend/.../SystemRole.java` — 新增 `CI`（群体智能队长）、`BUSINESS`（商务队长）角色。
- `erp-backend/.../AuthService.java` — JWT 角色集合扩展 CI/BUSINESS。
- `erp-backend/.../NaturalLanguageParserService.java` — LLM 角色映射扩展。
- `erp-backend/.../FinanceDividendSheetRepository.java` / `ProjectExpenseRepository.java` — 新增/修正查询方法。
- `lab-erp-demo/.../WageManagementView.vue` — 新增员工详情弹窗（个人信息、合同文档、考勤日历、报销记录、参与项目、队长身份、分成 7 个 Tab）。
- `lab-erp-demo/.../ExpenseReviewView.vue` — 重构为月份分组合并显示，新增筛选栏（月份/状态/人员），合并待审批和历史记录，新增反审批功能。
- `lab-erp-demo/.../LeaderManagementView.vue` / `LeaderDashboardView.vue` / `ManagerDashboard.vue` — 新增 CI（群体智能）、BUSINESS（商务）队长角色 Tab 与入口。
- `lab-erp-demo/.../ProjectDetail.vue` / `AdminCreateUserView.vue` — `formatRole`/`formatMemberIdentityTag` 新增 CI 角色映射。
- `lab-erp-demo/.../userStore.js` — 角色信息扩展。

**效果：** 14 个文件，+788/-105 行，工单问题修复，员工工资管理新增多维度详情弹窗，费用审批页面支持月度分组与多条件筛选，队长管理支持群体智能和商务两类新队长角色。

### 2026-07-17 11:20 — 修复「调增人力成本」保存时报系统内部错误

**原因：** 新增 `ProjectCostAdjustmentType.LABOR` 后，后端代码已能解析并保存，但数据库 `project_cost_adjustment` 表的 check constraint `project_cost_adjustment_adjustment_type_check` 只允许 `HARDWARE`、`SERVER_COMPUTE`、`EXTERNAL_SERVICE`、`REIMBURSEMENT`，未包含 `LABOR`，导致 INSERT 被 PostgreSQL 拒绝。

**改动位置：**
- 数据库 `project_cost_adjustment` — 通过 `ALTER TABLE ... DROP CONSTRAINT ... ADD CONSTRAINT ...` 把 `LABOR` 加入 check constraint 允许列表。
- 后端镜像更新为 `127.0.0.1:5555/zhangqi_backend:v1.155`。
- `docker-compose.yml:21` — 后端镜像更新为 `v1.155`。

**效果：** 管理员点击「调增人力成本」并提交后，记录可正常写入 `project_cost_adjustment` 表；跑批后金额会计入 `FinanceCostSummary.totalLaborCost`。

### 2026-07-17 10:25 — 合并并部署 PR #6（队长管理、项目文件管理器、工单）

**原因：** 合并同事 aurura12 提交的 PR #6，包含队长管理、项目文件管理器增强、产品流工单等功能；同时关闭已被覆盖的旧 PR #4。

**改动位置：**
- 后端新增 `WorkOrder` / `WorkOrderStatus` / `WorkOrderRepository` / `WorkOrderService` / `WorkOrderController`，支持产品流工单创建、接单、完成、关闭/取消及站内通知。
- 后端新增 `V20260715_001__create_work_order.sql` Flyway 迁移，创建 `work_order` 表。
- `LeaderDashboardController` / `LeaderDashboardService` / `SysProjectMemberRepository` 完善队长工作台数据查询与 `GET /api/leader/current-leader` 接口。
- `ProjectFileManagerController` / `ProjectFileManagerService` 增强：表格排序、勾选批量删除、递归删目录、批量下载/移动、单文件直接下载、zip 命名等。
- 前端 `LeaderDashboardView.vue` / `LeaderManagementView.vue` / `ProjectDetail.vue` / `ProjectFileManagerView.vue` / `App.vue` 同步队长与文件管理器改造。
- 新增 `lab-erp-demo/src/api/workOrders.js` 与 `lab-erp-demo/src/api/leader.js`。
- `docker-compose.yml:21,123` — 后端镜像更新为 `v1.154`，前端镜像更新为 `v1.173`。

**效果：** PR #6 已合并到 `main` 并部署到 `zhangqi_backend:v1.154` / `zhangqi_frontend:v1.173`；PR #4 已关闭并说明原因。

### 2026-07-17 09:55 — 项目成本调整支持「调增人力成本」并部署 v1.153/v1.172

**原因：** 需要在项目详情「调整项目成本」区域增加一个直接调增人力成本的入口，使人工成本可以通过调整单补充。

**改动位置：**
- `erp-backend/src/main/java/com/smartlab/erp/enums/ProjectCostAdjustmentType.java` — 新增 `LABOR` 枚举值。
- `erp-backend/src/main/java/com/smartlab/erp/finance/service/FinanceCostBatchService.java:645-655` — `project_cost_adjustment` 类型为 `LABOR` 时生成的跑批条目 `laborCost` 等于金额；其他类型保持为 0。
- `erp-backend/src/main/java/com/smartlab/erp/service/ProjectFinancialMetricsService.java:327-329` 与 `erp-backend/src/main/java/com/smartlab/erp/service/ProjectService.java:2427-2429` — 成本分解的 `adjustmentCost` 跳过 `LABOR` 类型，避免与人力成本重复统计。
- `lab-erp-demo/src/views/ProjectDetail.vue:52,1409,2970-2981` — 新增 `👤 调增人力成本` 按钮，成本类型下拉增加「人力成本」，打开弹窗时自动填充默认值。
- `docker-compose.yml:21,123` — 后端镜像更新为 `v1.153`，前端镜像更新为 `v1.172`。

**效果：** 管理员在项目详情页可直接调增人力成本；调增金额会在下次成本跑批后计入 `FinanceCostSummary.totalLaborCost`，并在 ManagerDashboard/项目详情成本分解中显示为「人力成本」，不再重复计入「成本调整」。

### 2026-07-17 09:50 — 修复 ManagerDashboard 「人力成本」等于「总成本」的问题并部署 v1.152

**原因：** 上一步虽然把非人工条目的 `laborCost` 改为 0，但 `ProjectService.getManagedProjectsSummary()` 里 `totalHumanCost` 直接复用了 `totalCost`（总结算成本），导致 ManagerDashboard 的「人力成本」KPI 仍与「总成本」相同。

**改动位置：**
- `erp-backend/src/main/java/com/smartlab/erp/service/ProjectService.java:1028-1033,1105` — 新增 `totalHumanCost` 统计：对每个项目取 `ProjectFinancialSnapshot.costBreakdown().humanCost()`（即 `FinanceCostSummary.totalLaborCost`）求和，不再复用 `totalCost`。
- `docker-compose.yml:21` — 后端镜像更新为 `127.0.0.1:5555/zhangqi_backend:v1.152`

**效果：** ManagerDashboard 的「人力成本」现在仅包含考勤/人工分摊，「总成本」仍包含项目费用、成本调整、公司报销等全部非人工成本，两者不再相同。

### 2026-07-17 09:30 — 部署 v1.151/v1.171 到本地 5555 仓库

**原因：** 重新构建并发布前后端镜像，使运行中的容器使用最新版本。

**改动位置：**
- `docker-compose.yml:21` — 后端镜像更新为 `127.0.0.1:5555/zhangqi_backend:v1.151`
- `docker-compose.yml:123` — 前端镜像更新为 `127.0.0.1:5555/zhangqi_frontend:v1.171`

**效果：** 本地 5555 仓库已推送 `zhangqi_backend:v1.151` / `zhangqi_frontend:v1.171`，容器 `zkr-erp-backend` 与 `zkr-lab-erp-demo` 已重新创建并运行。

### 2026-07-17 09:10 — 修复非人工成本条目混入人力成本汇总

**原因：** `FinanceCostBatchService.buildNonLaborEntries()` 在生成 project_expense、project_cost_adjustment、company_expense 三类非人工条目时，把金额同时写入 `laborCost`，导致成本分解/人力成本汇总把非人工金额也计算进去。

**改动位置：**
- `erp-backend/src/main/java/com/smartlab/erp/finance/service/FinanceCostBatchService.java:634,652,670` — 三类非人工条目的 `laborCost` 改为 `BigDecimal.ZERO`，`finalSettlementCost` 保持原金额不变。
- `erp-backend/src/test/java/com/smartlab/erp/finance/service/FinanceCostBatchServiceTest.java` — 新增 `nonLaborEntriesShouldNotContributeToLaborCost` 单测，验证非人工条目人力成本为 0。
- `erp-backend/src/test/java/com/smartlab/erp/finance/FinanceReportingServiceTest.java:80,86` 和 `erp-backend/src/test/java/com/smartlab/erp/finance/service/FinanceReportingServiceTest.java:80,83` — 补充 `CompanyExpenseRepository` 构造参数，修复编译错误。

**效果：** 成本分解中人力成本仅包含考勤/人工分摊，不再把项目费用、成本调整、公司报销等非人工金额重复计入。

### 2026-07-14 16:50 — 个人采购申请弹窗化，归属国科九天公司，含OCR台账

**原因：** 替换旧的整页表单，改为弹窗+三tab（合同/采购/报销），复用项目详情 dialog 和完整审批→OCR 流程，归属主体从项目变为国科九天公司。

**改动位置：**
- `erp-backend/.../config/CompanyProjectInitializer.java` — **新建**，启动时创建 projectId=COMPANY 的公司项目
- `erp-backend/.../controller/ProjectController.java:310-325` — 新增 `POST /api/projects/expenses/company` 端点
- `lab-erp-demo/src/components/CompanyExpenseDialog.vue` — **新建**，三tab弹窗
- `lab-erp-demo/src/App.vue:65,531` — 按钮改为弹窗
- `lab-erp-demo/src/views/UserProfile.vue:51` — 同上
- `lab-erp-demo/src/router/index.js` — 删除旧路由

**效果：** ZIP 附件自动拆包生成 InvoiceLedger，审批通过后自动触发 PaddleOCR 识别形成台账

### 2026-07-10 16:14 — 修复成本跑批 0607 以来为 0 的根因

**原因：** 考勤表 `attendance_record.user_name` 与系统用户表 `sys_user.name` 存在格式差异导致全量匹配失败：
- 系统用户名含 `_实习` 后缀（如 `刘浩洋_实习`），考勤名为纯名（如 `刘浩洋`）→ 39 人无法匹配
- 考勤名偶有 `主机位` 后缀（如 `刘忠益主机位`）和 `MM-DD-` 日期前缀（如 `02-22-赵翌池`）

**改动位置：**
- `erp-backend/.../finance/service/FinanceCostBatchService.java:518-525,534,793-803` — 新增 `normalizeSysUserName()`（去 `_实习`）和 `normalizeAttendUserName()`（去 `主机位`、`\d{2}-\d{2}-`）方法；name→userId 映射与考勤查找两处均做归一化

**效果：** 归一化后匹配率从 ~40% 升至 94%（缺 `郭健雯`/`孙鑫` 两名无系统记录用户），跑批恢复正常产出

### 2026-07-10 16:01 — 侧边栏瘦身：跑批日志弹窗化 + AI/审计合并

**原因：** Finance 侧边栏 14 个条目过于琐碎，跑批日志仅 70 行却独占一个路由。

**改动位置：**
- `lab-erp-demo/src/views/finance/BatchControlView.vue` — 新增「查看日志」按钮 + el-dialog 弹窗，内嵌跑批执行记录卡片列表
- `lab-erp-demo/src/views/finance/FinanceAiHub.vue` — **新建**，双 tab 包装器：全局检索 | 智能助手
- `lab-erp-demo/src/views/finance/FinanceAuditHub.vue` — **新建**，双 tab 包装器：手工调账 | 成本调整日志
- `lab-erp-demo/src/router/financeRoutes.js` — 删除 5 个 navItem（调账、全局业务检索、全局业务助手、成本调整日志、跑批日志），新增 2 个（AI 业务、审计）

**效果：** 侧边栏 14→11 条目；跑批日志在跑批控制页内一键查看；AI 和审计各聚合为一个入口

### 2026-07-10 15:50 — 项目文件管理器全面修复和功能增强

**原因：** 文件系统只能浏览目录，无法选中文件，下载按钮名存实亡，缺少上传/预览/删除功能。

**改动位置：**
- `erp-backend/.../projectfile/ProjectFileSourceType.java` — 新增 `UPLOADED_FILE` 枚举值
- `erp-backend/.../projectfile/ProjectFileManagerService.java:161-241` — 新增 `uploadFile`、`deleteFile`、`downloadUploadedFile`、`getMimeType` 方法
- `erp-backend/.../projectfile/ProjectFileManagerController.java:75-127` — 修复下载 Content-Disposition 双编码 bug；新增 `previewFile`（inline + 正确 MIME 类型）、`uploadFile`（multipart）、`deleteFile` 端点
- `lab-erp-demo/src/views/ProjectFileManagerView.vue` — 修复文件节点不可点击（替换未定义 CSS 变量 `--science-blue-soft`）；新增预览/上传/删除按钮和对应逻辑；修复 `link.download` 空文件名
- `lab-erp-demo/src/components/ProjectFileTree.vue` — 文件单点改为预览打开新标签页，hover/active 状态修复

**效果：** 文件节点可点击选中并高亮显示；双击预览（新标签页 inline 展示）；工具栏支持下载/预览/移动/删除/上传完整操作；上传文件保存到 `uploads/project-files/{projectId}/` 目录。

**原因：** `App.vue` 通过 ref 调用子组件 submit 方法，但三个子组件均使用 `<script setup>` 默认闭包，未 `defineExpose`，父组件调用时 `confirmCreate`/`submit` 为 `undefined`，静默跳过无效果。

**改动位置：**
- `lab-erp-demo/src/App.vue:164-171,238-250` — 添加模板 ref + `submitLaunchForm` 委托逻辑
- `lab-erp-demo/src/views/CreateProject.vue:136` — `defineExpose({ confirmCreate })`
- `lab-erp-demo/src/views/CreateDeliveryProjectView.vue:197` — `defineExpose({ submit })`
- `lab-erp-demo/src/views/CreateResearchView.vue:146` — `defineExpose({ submit })`

**效果：** 发起弹窗点击确认后正确调用对应 API，提交成功后弹窗自动关闭。

### 2026-07-08 15:45 — 修复发起弹窗确认按钮无响应（第一版）

### 2026-07-08 10:37 — 统一发起按钮 + 会议自动定时强提醒

**原因：** 三个独立发起按钮（产品/项目/科研）分散在不同位置，入口不统一；会议系统缺少参会提醒。

**改动位置：**
- `erp-backend/.../db/migration/V20260707_001__add_meeting_reminder.sql` — **新建** meeting_record 加 last_reminded_at
- `erp-backend/.../meeting/entity/MeetingRecord.java:62-63` — 新增 lastRemindedAt 字段
- `erp-backend/.../meeting/repository/MeetingRecordRepository.java:27-29` — 新增查询：SCHEDULED + 15分钟内 + 未提醒
- `erp-backend/.../meeting/scheduler/MeetingReminderScheduler.java` — **新建** @Scheduled(cron="0 * * * * *") 每分钟扫描即将开始的会议，通过 InternalMessageService 向所有参会人发送 MEETING_REMINDER 强提醒
- `lab-erp-demo/.../App.vue:43-44,162-180,188-220,290-306,635-654` — 替换「发起产品」为统一样式「发起」按钮（圆角矩形，#0066cc）；新增发起弹窗 + el-tabs（产品/项目/科研三个 tab 按权限显示）；新增会议提醒 toast
- `lab-erp-demo/.../CreateProject.vue` — 新增 embedded prop + submitted emit，嵌入式表单跳过外层遮罩
- `lab-erp-demo/.../CreateDeliveryProjectView.vue` — 同上
- `lab-erp-demo/.../CreateResearchView.vue` — 同上
- `lab-erp-demo/.../ManagerDashboard.vue:38-59` — 移除独立的「发起项目」「发起科研」按钮

**效果：**
- 导航栏统一「发起」按钮代替分散的三个按钮，点击弹窗按权限显示 tab（产品→所有人，项目→BUSINESS，科研→RESEARCH/白名单）
- 三个 tab 直接嵌入创建表单，提交后自动关闭弹窗
- 会议开始前 15 分钟自动向所有参会人发送站内消息（MEETING_REMINDER），前端 toast 提示「⏰ 会议即将开始」
- ManagerDashboard 按钮精简为会议中心/队长管理/队长工作台

**原因：** Windows 创建的 ZIP 文件名使用 GBK 编码，`ZipInputStream` 默认 UTF-8 解码失败抛 `MalformedInputException` → 全局异常处理器返回「系统内部错误」，用户无法定位问题。

**改动位置：**
- `erp-backend/.../service/ReimbursementZipService.java:95-135` — `process()` 新增 `extractZipWithFallback()`：UTF-8 解压失败自动回退 GBK 编码重试；GBK 也失败时抛「请使用 UTF-8 格式压缩文件（推荐 7-Zip 重新打包）」的明确提示
- `erp-backend/.../service/ProjectService.java:2093-2099` — ZIP 处理异常捕获从 `BusinessException` 扩展为 `Exception`，确保所有错误都包装为中文提示

**效果：** UTF-8/GBK 双编码自动兼容；编码问题有明确修复指引而非「系统内部错误」。

**原因：** 报销 Excel 模板下载入口隐藏在 `.zip-hint` 提示区，不够醒目。将模板下载按钮提升到底部操作栏，永远可见。

**改动位置：**
- `lab-erp-demo/.../ExpenseSubmissionForm.vue:100-107` — 模板下载按钮从 `.zip-hint` 内移到 `.footer-row` 操作栏最左侧（与提交按钮、提示文案同行）

**效果：** 报销表单底部始终显示「下载报销模板」按钮，无论是否选择发票文件均可见。

### 2026-07-06 10:24 — 阶段3：合同台账建表与合同 OCR 识别入账

**原因：** 合同（`ProjectExpense` 类型 `EXTERNAL_SERVICE`）审批通过后需 OCR 扫描合同文件（PDF/DOCX/图片），提取 23 个结构化字段写入合同台账，并与发票台账通过 `contract_no` 关联。

**改动位置：**
- `erp-backend/.../db/migration/V20260703_002__create_contract_ledger.sql` — **新建** Flyway 迁移，创建 `contract_ledger` 表（23 业务列 + OCR 审计列 + 3 索引）
- `erp-backend/.../entity/ContractLedger.java` — **新建** JPA 实体
- `erp-backend/.../repository/ContractLedgerRepository.java` — **新建** JPA 仓储
- `erp-backend/.../ocr/ContractOcrService.java` — **新建** `@Async @Transactional`：读取合同文件 → 调 `OcrClient.recognizeContract()` → 回填 23 字段 → 写 `contract_ledger`
- `erp-backend/.../ocr/ContractOcrResult.java` — **新建** 合同 OCR 结果 DTO（23 业务字段 + 置信度 + 错误信息）
- `erp-backend/.../ocr/OcrClient.java:88-149` — 新增 `recognizeContract()` 方法，`POST /ocr/contract`；重构 `buildRestTemplate()` 公共方法
- `erp-backend/.../service/ProjectService.java:133,2187-2192` — 注入 `ContractOcrService`；`reviewExpense()` 中 APPROVED 时按类型分流：`EXTERNAL_SERVICE` → 合同 OCR，其他 → 发票 OCR
- `erp-backend/.../controller/ProjectController.java:300` — `submitProjectExpense()` 新增 `counterparty` 可选参数
- `erp-backend/.../dto/SubmitProjectExpenseRequest.java` — 新增 `counterparty` 字段
- `paddle-ocr/Dockerfile` — 新增 `poppler-utils` 系统包 + `pdf2image python-docx PyPDF2` Python 包
- `paddle-ocr/requirements.txt` — 新增 `pdf2image python-docx PyPDF2`
- `paddle-ocr/app.py:220-360` — 新增 `POST /ocr/contract` 端点；文件类型自动分流（图片→PaddleOCR，PDF→PyPDF2/pdf2image，DOCX→python-docx）；`_parse_contract_fields()` 正则提取 23 字段
- `lab-erp-demo/.../ProjectDetail.vue:1442-1444,1562,3002,3011` — 合同对话框新增「合同对方名称」字段；`expenseForm` 新增 `counterparty`；提交 FormData 携带 `counterparty`

**效果：**
- 审批通过时按 `expenseType` 自动分流：合同（`EXTERNAL_SERVICE`）触发 `ContractOcrService` 写入 `contract_ledger`，其他类型触发 `InvoiceOcrService` 写入 `invoice_ledger`
- 合同 OCR 支持 PDF（含扫描件 OCR 回退）、DOCX（文本直提）、图片格式
- 23 字段由正则提取 + PaddleOCR 文字识别自动填入
- 发票台账通过 `contract_no` 与合同台账关联（1:N）
- 合同对话框新增「合同对方名称」字段供用户手动补充

### 2026-07-03 16:59 — 阶段2：PaddleOCR 引擎集成与异步发票识别入账

**原因：** 审批通过的报销需自动 OCR 扫描发票图片，提取金额/发票号/日期/往来单位等字段回填发票台账，并进行 Excel 与 OCR 的金额交叉校验。

**改动位置：**
- `paddle-ocr/Dockerfile` — **新建** 基于 `paddlepaddle/paddleocr:2.8.1`，安装 FastAPI + uvicorn，内部端口 18952（不暴露宿主机）
- `paddle-ocr/requirements.txt` — **新建** Python 依赖
- `paddle-ocr/app.py` — **新建** FastAPI OCR 服务：`GET /health` 健康检查 + `POST /ocr/invoice` 发票识别，PaddleOCR 中文模型 + 正则提取发票号码/日期/金额/税额/税率/购销方/标的物
- `docker-compose.yml:155-168` — 新增 `erp-paddle-ocr` 服务（`restart: unless-stopped`，仅加入 `erp-internal` 网络，无 host 端口映射，外部不可见）
- `erp-backend/.../ocr/OcrClient.java` — **新建** RestTemplate HTTP 客户端，调用 `POST http://erp-paddle-ocr:18952/ocr/invoice`，返回 `OcrInvoiceResult`
- `erp-backend/.../ocr/OcrProperties.java` — **新建** `@ConfigurationProperties("erp.ocr")`，默认 `baseUrl=http://erp-paddle-ocr:18952`
- `erp-backend/.../ocr/OcrInvoiceResult.java` — **新建** OCR 结果 DTO（发票号/日期/金额/税额/税率/购销方/标的物/置信度）
- `erp-backend/.../ocr/InvoiceOcrService.java` — **新建** `@Async @Transactional triggerOcr(expenseId)`：遍历 `invoice_ledger` PENDING 行 → 读图片 → 调 OCR → 回填字段 → 交叉校验 → `verified_status = MATCH/MISMATCH`
- `erp-backend/.../service/ProjectService.java:131,2184-2187` — 注入 `InvoiceOcrService`；`reviewExpense()` 中 APPROVED 后调用 `invoiceOcrService.triggerOcr()` 异步触发 OCR

**效果：**
- 报销三级审批全部通过（状态变 APPROVED）→ 异步触发 PaddleOCR 扫描所有关联发票图片
- OCR 提取的字段（发票号码/不含税金额/税额/税率/含税金额/日期/往来单位/购方抬头/摘要）自动回填 `invoice_ledger`
- `data_source` 更新为 `EXCEL+OCR`，`ocr_status → DONE`，`ocr_raw_json` 保存完整 OCR 原始返回
- Excel 金额与 OCR 金额交叉校验：不含税+税额 vs 含税 → `verified_status = MATCH/MISMATCH`
- PaddleOCR 容器无宿主机端口映射，仅 `erp-internal` 网络内可见

### 2026-07-03 16:52 — 阶段1：报销 ZIP 解压与发票台账建表

**原因：** 用户通过 ZIP 压缩包提交项目报销（命名规范 `姓名+项目+金额.zip`），内含发票图片和汇总 Excel。需要解包入库并写入发票台账流水表，为后续 OCR 入账做准备。

**改动位置：**
- `erp-backend/.../db/migration/V20260703_001__create_invoice_ledger.sql` — **新建** Flyway 迁移，创建 `invoice_ledger` 审计流水表（19 业务列 + 7 审计列 + 3 索引）
- `erp-backend/.../entity/InvoiceLedger.java` — **新建** JPA 实体，`seq_no` BIGSERIAL 全局流水主键
- `erp-backend/.../repository/InvoiceLedgerRepository.java` — **新建** Spring Data JPA 仓储
- `erp-backend/.../service/ReimbursementZipService.java` — **新建** 核心服务：ZIP 解压、文件名解析 `姓名+项目+金额.zip`、POI 解析 Excel 汇总表（7 列模板）、发票图片 SHA256 哈希、写入 `invoice_ledger` 流水行、`generateTemplateExcel()` 生成 Excel 模板
- `erp-backend/.../service/ProjectService.java:130` — 注入 `ReimbursementZipService`；`submitProjectExpense()` 先保存 expense 再处理文件；检测 `.zip` 后缀走解包流程写入 `invoice_ledger`；检测 `.rar` 暂作普通附件存储
- `erp-backend/.../controller/ProjectController.java:50,359-369` — 注入 `ReimbursementZipService`；新增 `GET /api/projects/expenses/reimbursement-template` 端点，下载 7 列 Excel 模板
- `lab-erp-demo/.../ExpenseSubmissionForm.vue:101-105,292-305,407-422` — 新增 ZIP 命名规范提示文案（`姓名+项目+金额.zip`）；新增「下载 Excel 模板」按钮及 `downloadTemplate()` 方法；新增 `.zip-hint` 样式

**效果：**
- ZIP 上传时自动解压，Excel 数据写入 `invoice_ledger`（`data_source=EXCEL, ocr_status=PENDING`）
- 发票图片落盘并计算 SHA256 哈希
- ZIP 文件名解析出提交人/项目名/总金额，写入发票行
- 用户可通过前端下载模板填入后打包上传

**已知占位（预留后续阶段）：**
- `ocr_status` 始终 `PENDING`，审批通过后异步 OCR 机制待阶段 2 实现
- `tracking_no` 自动生成格式待实现
- `company`/`company_code` 由 OCR 从发票抬头提取，待阶段 2 填充
- `contract_ledger` 合同台账表待阶段 3 建表

### 2026-07-02 10:43 — 员工管理模块新增离职/复职/创建操作日志

**原因：** 员工离职和复职操作无审计记录，无法追溯操作历史；`User` 实体虽有 `departureDate` 字段但从未写入。

**改动位置：**
- `erp-backend/.../entity/UserStatusLog.java` — **新建** JPA 实体，映射 `user_status_log` 表（user_id, action, operator_id, created_at）
- `erp-backend/.../repository/UserStatusLogRepository.java` — **新建** Repository
- `erp-backend/.../db/migration/V20260702_001__create_user_status_log.sql` — **新建** Flyway 迁移脚本
- `erp-backend/.../service/UserService.java:110-111,131-132` — `deactivateUser()` 写入 `departureDate = LocalDate.now()`；`activateUser()` 写入 `departureDate = null`
- `erp-backend/.../controller/AdminUserController.java:65-70,100-113,121-125` — `provisionUser()` 写入 CREATE 日志；`deactivateUser()` 写入 DEACTIVATE 日志；`activateUser()` 写入 ACTIVE 日志；新增 `GET /{userId}/status-history` 查询接口

**效果：** 每次创建账号、离职、复职均硬记录到 `user_status_log`，后端管理员可通过 API 或数据库直接查询任意用户的操作历史。

### 2026-06-30 16:05 — 修复腾讯会议绑定 userid 失败

**原因：** 绑定校验调用 `listAllTmUsers()` 拉取全量腾讯会议用户列表，但 API 返回非2xx时静默返回空列表，导致验出"该腾讯会议账号不存在"，实际是 API 调用失败。

**改动位置：**
- `erp-backend/.../TencentMeetingUserSyncService.java:116,144` — 列表 API 失败时新增日志记录 HTTP 状态码和响应体；新增 `findTmUserById()` 方法直接调用 `GET /v1/users/{userid}` 查询单个用户
- `erp-backend/.../TencentUserMappingController.java:70-81` — 绑定校验改用 `findTmUserById()` 替代 `listAllTmUsers()`，错误信息包含"查询失败"提示

**效果：** 绑定校验直接查询指定 userid，更高效且错误信息明确；API 失败时日志可见具体 HTTP 状态码和响应体。


### 2026-06-22：ERP 账号创建模块集成协议生成功能并接入大模型自然语言识别

**更新：** 在 ERP 账号创建流程中集成 outside 协议生成能力，创建账号后可直接生成三份实习协议 Word 文档并下载；同时接入 OpenCode AI DeepSeek V4 Pro，支持从自然语言文本中自动识别账号信息并填充表单。

**涉及改动：**
- 后端新增 `school_department`、`address` 字段，`agreement_template` 表存储三份协议模板。
- 新增 `AgreementGenerationService` / `AgreementZipService`，使用 Apache POI 重写 outside Python 脚本逻辑，生成 `互联网实习生协议`、`实习生协议`、`实习证明` 三份 `.docx`。
- 新增/扩展接口：
  - `POST /api/admin/users/{userId}/agreement?type=...`（单份生成并保存）
  - `POST /api/admin/users/{userId}/agreements/batch`（勾选多份，打包 zip 下载）
  - `GET/PUT /api/admin/agreement-templates/{code}`（模板管理）
  - `POST /api/admin/users/parse-natural-language`（自然语言解析为账号 JSON）
- 新增 `LlmClient` / `NaturalLanguageParserService`，调用 OpenCode AI `/v1/chat/completions` 提取结构化账号信息。
- 前端 `AdminCreateUserView.vue` 新增「学校院系」「住址」字段；新增自然语言输入框和「智能识别并填充」按钮；创建成功后弹出协议生成对话框。
- 部署镜像版本：`zhangqi_backend:v1.136`、`zhangqi_frontend:v1.160`。

**注意：**
- 默认协议模板通过 `AgreementTemplateInitializer` 在启动时从 classpath 自动写入数据库；后续可通过管理接口替换。
- 旧 `POST .../agreement` 生成 `.txt` 的行为已替换为生成 `.docx`。
- `.env` 中 LLM 配置已切换为 OpenCode AI：`ERP_LLM_BASE_URL`、`ERP_LLM_API_KEY`、`ERP_LLM_MODEL`。

### 2026-05-09：项目流发起时 dataEngineerId 传递了 "userId-ROLE" 导致后端查不到用户

**问题：** `CreateDeliveryProjectView.vue` 中数据工程师下拉框的 option value 用了 `"${u.userId}-${u.role}"` 格式（如 `"000010-DATA_ENGINEER"`），提交给后端 `/api/projects/initiate` 时后端直接用这个值查数据库 `userRepository.findById()`，查不到，报 "指定的数据工程师不存在"。

**修复：** 将 option `id` 改为纯 `String(u.userId || '')`，与后端数据库 userId 一致。

### 2026-05-09：项目流发起时数据工程师候选列表不完整，且组队时同一用户以 DATA/DATA_ENGINEER 两个角色重复出现

**问题 1：** `CreateDeliveryProjectView.vue` 从 `workflow_member_role` 表查 PROJECT 类型候选人，只返回已加入过项目的用户，系统中未参与过 PROJECT 的 DATA 用户不可见。

**修复 1：** 直接调用 `/api/users` 全量拉取，过滤 data 角色，并按 userId 归一化去重（DATA_ENGINEER 和 DATA 视为同一用户，只保留一条）。

**问题 2：** `ProjectDetail.vue` 的 `memberCandidates` 去重 key 为 `${userId}-${role}`，同一用户在 `workflow_member_role` 表中同时有 DATA 和 DATA_ENGINEER 记录时显示两行。

**修复 2：** `appendCandidate` 中 dedup 时将 `DATA_ENGINEER` 归一化为 `DATA`，使同一用户只出现一次。

### 2026-05-11：组队阶段数据工程师兼任 Manager 时权责比中不显示本人，且有幽灵成员

**问题：** `buildInitialTeamState()` 初始化 `teamMembers` 用纯 `userId`（如 `"000037"`），而 `memberCandidates` 中 entry.id 是 `${userId}-DATA` 格式。导致：
1. 数据工程师 Manager 本人在权责比列表中不可见
2. `submitBuildTeam` 中 `includes` 匹配失败，报错"请在团队成员列表中保留该数据工程师"
3. `selectedMemberDetails` 过滤时匹配不上，显示空 name 的幽灵成员

**修复：** `buildInitialTeamState()` 中将 `initialTeamMembers` 统一为 `${userId}-DATA` 格式与 `memberCandidates` 对齐；`submitBuildTeam` 中的 `includes` 改为 `startsWith(userId + '-')` 匹配。

### 2026-05-11：替换 Manager 后旧 Manager 的 managerWeight 未被清零

**问题：** `applyProjectResponsibilityAllocation()` 只更新新 Manager 的权重，不遍历所有成员清零旧 Manager 的 `manager_weight`。导致替换 Manager（如焦淼→李昊天）后，旧 Manager 的管理权责比仍然保留，权责比总和超过 100（如 125）。

**修复：** 在设置新 Manager 权重前，先将项目所有成员的 `manager_weight` 清零。

### 2026-05-16：项目流可行性报告上传按钮不可见

**问题：** `ProjectDetail.vue` 中 `canUploadProjectAsset` 计算属性（判断当前用户是否为被选中的数据工程师 + INITIATED 阶段）已正确实现，`triggerFileInput()` 函数和隐藏 `<input type="file">` 也已就绪，但模板中从未使用 `canUploadProjectAsset`，也从未调用 `triggerFileInput()`。导致数据工程师在 INITIATED 阶段看不到任何上传入口，无法通过 UI 上传可行性报告。

**修复：** 在 PROJECT 流的智能信息面板（`product-flow-grid`）中，可行性报告状态行新增「上传可行性报告」按钮，绑定 `v-if="canUploadProjectAsset"` 和 `@click="triggerFileInput"`，并添加 `.execution-row` 样式使按钮与状态文本同行显示。

### 2026-06-22：server-mgmt-api 容器网络隔离导致 ERP 后端无法访问

**问题：** `server-mgmt-api` 容器实际运行在 `server-mgmt_default` 网络（由 `/home/iiiioooo/Workspace/服务器管理/docker-compose.yml` 单独启动），而 `erp-backend` 在 `zkr_erp-internal` 网络。两者不在同一 Docker 网络，`ServerMgmtProxyController` 配置的目标地址 `http://server-mgmt-api:17000` 不可解析，导致前端「服务器管理」页面加载服务器/用户列表均失败。

**修复：** 停止旧独立实例，在 ZKR 项目根目录用 `docker compose up -d server-mgmt-api` 启动，使其自动加入 `zkr_erp-internal` 网络。迁移旧数据卷 `server-mgmt_server-mgmt-data` → `zkr_server-mgmt-data`。

### 2026-06-22：劳动关系资料模块 Finance 域无法访问 ERP 域 API

**问题：** `LaborRelationsView.vue` 前端页面注册在 Finance 路由 `/finance/labor-relations` 下，使用 `finance_token`，但它调用的 `/api/admin/users` 和 `/api/admin/users/users/{userId}/documents` 是 ERP 域接口（SecurityConfig 中 `/api/**` 要求 `requireErpDomain`），Finance token 被拦截返回 401。此外 `AdminUserController.getAllUsers()` 还要求 `requireProvisionAdmin()`（仅允许 guojianwen/jiaomiao/Zhangqi 等特定账号），普通 Finance 用户即使换 ERP token 也被拒绝。

**修复：** 新增 Finance 域接口：
- `erp-backend/src/main/java/com/smartlab/erp/finance/controller/FinanceLaborRelationsController.java` — `@RequestMapping("/api/finance/labor-relations")`，走 `requireFinanceDomain`，不要求 provision admin。
- `erp-backend/src/main/java/com/smartlab/erp/finance/service/FinanceLaborRelationsService.java` — 复用原有文件存储逻辑，`GET /users` 直接返回带 `hasAgreement/hasIdCard/hasStudentCard` 标记的用户列表，消除前端 N+1 查询。
- 前端 `LaborRelationsView.vue` 将 `USERS_BASE` 和 `DOCS_BASE` 从 `/api/admin/users` 改为 `/api/finance/labor-relations/users`。`fetchUsers` 移除逐用户拉取文档的循环，改用后端预计算标记。

### 2026-06-22：服务器状态硬编码 'ok'，缺少真实 SSH 连通检测

**问题：** `ServerManagementView.vue` 中服务器状态硬编码为 `'ok'`，无法反映真实的 SSH 连通性。原 `outside/服务器管理` 项目有 `playbook-ping.yml` 可测试 SSH，但未暴露为 API。

**修复：**
- `server-mgmt/api/main.py` 新增 `GET /api/servers/status` 端点，执行 `ansible-playbook playbook-ping.yml` 并解析 PLAY RECAP，返回每台服务器 `ok/unreachable/auth_failed/unknown`。
- 新增 `server-mgmt/tests/test_status.py`（7 个单测）。
- `ServerManagementView.vue` 新增真实状态标签、刷新按钮、每次切换到「服务器」Tab 自动检测。

### 2026-06-23：项目文件管理器（Provision Admin 专用）

**功能：** 跨项目文件统一管理视图，用虚拟目录（folder + mapping）而非物理移动来组织文件。支持自动扫描、手动创建文件夹、移动文件、下载。整理效果同步至各项目详情页。

**设计方案：**
- 虚拟目录模型：`project_file_folder`（树结构，`parent_id` 自关联）+ `project_file_mapping`（记录每个文件所属虚拟文件夹和来源表）
- 数据来源覆盖 5 张表：`project_asset`、`execution_file`、`project_expense_file`、`finance_expense_submission`、`project_cost_adjustment`
- 启动时自动扫描已有文件，按来源类型创建默认系统文件夹（`项目资料`、`执行文件`、`费用报销`、`财务报销`、`成本调整`）

**涉及改动：**
- 后端新增 `erp-backend/src/main/java/com/smartlab/erp/projectfile/`：
  - `ProjectFileFolder` / `ProjectFileFolderRepository` — 虚拟文件夹实体
  - `ProjectFileMapping` / `ProjectFileMappingRepository` — 文件到虚拟文件夹的映射
  - `ProjectFileManagerController` — REST API（`/api/admin/project-files`）
  - `ProjectFileManagerService` — 核心逻辑（scan、tree、createFolder、deleteFolder、moveFile、download）
  - `ProjectFileMappingInitializer` — 启动时扫描已有文件并建立映射
  - `ProjectFileSourceType` — 文件来源类型枚举
- 前端新增：
  - `ProjectFileManagerView.vue` — 全局文件管理页面，路由 `/admin/project-files`
  - `ProjectFileTree.vue` — 可复用的只读文件树组件，用于 ProjectDetail 同步
  - `App.vue` 导航栏新增「📁 项目文件」快捷按钮，仅 Provision Admin 可见
  - `ProjectDetail.vue` 新增「项目文件目录」面板，读取虚拟树展示同步结果
- 部署镜像版本：`zhangqi_backend:v1.137`、`zhangqi_frontend:v1.161`。

**注意事项：**
- 权限控制：仅 Provision Admin（`Zhangqi`、`guojianwen`、`jiaomiao`）可访问全局文件管理器
- ProjectDetail 中的文件树面板同样仅管理员可见，普通成员不可见
- 移动文件操作调用 `PATCH /files/{mappingId}/move`，修改 `folder_id` 即完成虚拟移动
- `ProjectFileMapping` 不生成物理 ID（自增 Long id），由 `source_type` + `source_id` + `project_id` 唯一索引保证不重复映射

### 2026-06-24：项目文件管理器下载修复

**问题：** `ProjectFileTree.vue` 和 `ProjectFileManagerView.vue` 中文件下载使用 `window.open()` / `<a href>` 直接发送 GET 请求，未携带 JWT token，导致后端返回「当前账号未登录或登录已过期」。

**修复：** 改为 `request.get(url, { responseType: 'blob' })` 通过 axios 拦截器自动注入 Authorization header，获取 blob 后创建 ObjectURL 触发下载。

**部署版本：** `zhangqi_frontend:v1.162`。

### 2026-06-24：合并腾讯会议API集成（PR #5 from aurura12）

**内容：** 合并社区贡献者 `aurura12` 提交的腾讯会议企业自建应用集成代码。

**主要改动：**
- 后端新增 `meeting/` 模块（controller、service、entity、repository、webhook），包含腾讯会议签名/Token 服务、会议创建管理、用户映射
- 新增 Flyway 迁移：`meeting_record`、`meeting_participant`、`tencent_user_mapping`，sys_user 新增 `phone` 列
- 前端新增 `MeetingCenterView.vue`、路由 `/meetings`
- ManagerDashboard、WorkspaceView、App.vue 新增「会议中心」入口

**适配修复（本次）：**
- `nginx.conf`：proxy_pass 从 `erp-backend:8101` 改回 `zkr-erp-backend:8101`（PR 的容器名不同）
- `application.properties`：server.port 从 8080 改回 8101（与 application.yml 一致）
- `vite.config.js`：dev proxy 从 8081 改回 8101
- `docker-compose.yml`：移除 postgres 5432 端口直接暴露（安全考虑）

**部署版本：** `zhangqi_backend:v1.138`、`zhangqi_frontend:v1.163`。
