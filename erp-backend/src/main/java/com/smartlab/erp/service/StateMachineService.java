package com.smartlab.erp.service;

import com.smartlab.erp.entity.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 状态机服务
 * 定义和管理项目流、产品流、科研流的生命周期规则
 */
@Component
public class StateMachineService {

    /**
     * 【修复】将 record 改为静态内部类，兼容性更强，解决 static 报错
     * 状态转换验证结果
     */
    public static class TransitionValidation {
        private final boolean valid;
        private final String error;

        public TransitionValidation(boolean valid, String error) {
            this.valid = valid;
            this.error = error;
        }

        // Getter 方法
        public boolean isValid() {
            return valid;
        }

        public String getError() {
            return error;
        }

        // 静态工厂方法
        public static TransitionValidation valid() {
            return new TransitionValidation(true, null);
        }

        public static TransitionValidation invalid(String error) {
            return new TransitionValidation(false, error);
        }
    }

    /**
     * 状态定义
     */
    private static final Map<ProjectStatus, ProjectStateDef> PROJECT_STATES = new EnumMap<>(ProjectStatus.class);
    private static final Map<ProductStatus, ProductStateDef> PRODUCT_STATES = new EnumMap<>(ProductStatus.class);

    static {
        // 初始化项目流状态（轨道 A：线索 -> 投标 -> 立项 -> 实施 -> 验收）
        PROJECT_STATES.put(ProjectStatus.LEAD, new ProjectStateDef(
                "线索",
                "#90CAF9",
                List.of(ProjectStatus.BIDDING),
                Map.of("hasContact", false)
        ));

        PROJECT_STATES.put(ProjectStatus.BIDDING, new ProjectStateDef(
                "投标",
                "#FFB74D",
                List.of(ProjectStatus.INITIATED),
                Map.of("hasProposal", true)
        ));

        PROJECT_STATES.put(ProjectStatus.INITIATED, new ProjectStateDef(
                "立项",
                "#BA68C8",
                List.of(ProjectStatus.IMPLEMENTING),
                Map.of("hasApproval", true, "minBudget", 0)
        ));

        PROJECT_STATES.put(ProjectStatus.IMPLEMENTING, new ProjectStateDef(
                "实施",
                "#81C784",
                List.of(ProjectStatus.ACCEPTANCE),
                Map.of("hasProgressReport", true)
        ));

        PROJECT_STATES.put(ProjectStatus.ACCEPTANCE, new ProjectStateDef(
                "验收",
                "#4FC3F7",
                List.of(ProjectStatus.COMPLETED, ProjectStatus.IMPLEMENTING),
                Map.of("hasAcceptanceDoc", true, "signOff", true)
        ));

        PROJECT_STATES.put(ProjectStatus.COMPLETED, new ProjectStateDef(
                "已完成",
                "#66BB6A",
                List.of(),
                Map.of("finalReport", true, "allBudgetUsed", true)
        ));

        // 初始化产品流状态（轨道 B：IDEA → PROMOTION → DEMO_EXECUTION → MEETING_DECISION → TESTING → LAUNCHED / SHELVED）
        PRODUCT_STATES.put(ProductStatus.IDEA, new ProductStateDef(
                "创意孵化",
                "#FF7043",
                List.of(ProductStatus.PROMOTION),
                Map.of()
        ));

        PRODUCT_STATES.put(ProductStatus.PROMOTION, new ProductStateDef(
                "推广组队",
                "#FFC107",
                List.of(ProductStatus.DEMO_EXECUTION),
                Map.of()
        ));

        PRODUCT_STATES.put(ProductStatus.DEMO_EXECUTION, new ProductStateDef(
                "Demo 实施",
                "#42A5F5",
                List.of(ProductStatus.MEETING_DECISION),
                Map.of()
        ));

        PRODUCT_STATES.put(ProductStatus.MEETING_DECISION, new ProductStateDef(
                "虚拟会议决策",
                "#7E57C2",
                List.of(ProductStatus.TESTING, ProductStatus.SHELVED),
                Map.of()
        ));

        PRODUCT_STATES.put(ProductStatus.TESTING, new ProductStateDef(
                "测试与上线",
                "#66BB6A",
                List.of(ProductStatus.LAUNCHED, ProductStatus.SHELVED),
                Map.of()
        ));

        PRODUCT_STATES.put(ProductStatus.LAUNCHED, new ProductStateDef(
                "已转化为正式项目",
                "#26A69A",
                List.of(),
                Map.of()
        ));

        PRODUCT_STATES.put(ProductStatus.SHELVED, new ProductStateDef(
                "搁置/流产",
                "#B0BEC5",
                List.of(),
                Map.of()
        ));
    }

    /**
     * 验证项目状态流转
     */
    public TransitionValidation canTransitionProject(ProjectStatus from, ProjectStatus to, Map<String, Object> context) {
        ProjectStateDef stateDef = PROJECT_STATES.get(from);

        if (stateDef == null) {
            return TransitionValidation.invalid("Invalid source state: " + from);
        }

        // 检查是否是允许的下一个状态
        if (!stateDef.next().contains(to)) {
            return TransitionValidation.invalid(
                    String.format("Cannot transition from %s to %s. Allowed: %s",
                            from, to, stateDef.next())
            );
        }

        // 检查业务规则
        TransitionValidation ruleCheck = checkProjectRules(from, to, context);
        // 【修复】使用 isValid() 方法
        if (!ruleCheck.isValid()) {
            return ruleCheck;
        }

        return TransitionValidation.valid();
    }

    /**
     * 验证产品流状态流转
     */
    public TransitionValidation canTransitionProduct(ProductStatus from, ProductStatus to, Map<String, Object> context) {
        ProductStateDef stateDef = PRODUCT_STATES.get(from);

        if (stateDef == null) {
            return TransitionValidation.invalid("Invalid source state: " + from);
        }

        // 检查是否是允许的下一个状态
        if (!stateDef.next().contains(to)) {
            return TransitionValidation.invalid(
                    String.format("Cannot transition from %s to %s. Allowed: %s",
                            from, to, stateDef.next())
            );
        }

        // 检查业务规则
        TransitionValidation ruleCheck = checkProductRules(from, to, context);
        if (!ruleCheck.isValid()) {
            return ruleCheck;
        }

        return TransitionValidation.valid();
    }

    /**
     * 项目流转规则检查（轨道 A）
     */
    private TransitionValidation checkProjectRules(ProjectStatus from, ProjectStatus to, Map<String, Object> context) {
        // 线索 -> 投标：需要有初步接触记录
        if (from == ProjectStatus.LEAD && to == ProjectStatus.BIDDING) {
            if (!Boolean.TRUE.equals(context.get("hasContact"))) {
                return TransitionValidation.invalid("需要有初步接触记录才能进入投标阶段");
            }
        }

        // 投标 -> 立项：需要有完整的投标方案
        if (from == ProjectStatus.BIDDING && to == ProjectStatus.INITIATED) {
            if (!Boolean.TRUE.equals(context.get("hasProposal"))) {
                return TransitionValidation.invalid("需要有完整的投标方案才能进入立项阶段");
            }
        }

        // 立项 -> 实施：需要有立项审批
        if (from == ProjectStatus.INITIATED && to == ProjectStatus.IMPLEMENTING) {
            if (!Boolean.TRUE.equals(context.get("hasApproval"))) {
                return TransitionValidation.invalid("需要有立项审批才能进入实施阶段");
            }
        }

        // 实施 -> 验收：需要有进度报告
        if (from == ProjectStatus.IMPLEMENTING && to == ProjectStatus.ACCEPTANCE) {
            if (!Boolean.TRUE.equals(context.get("hasProgressReport"))) {
                return TransitionValidation.invalid("需要有进度报告才能进入验收阶段");
            }
        }

        // 验收 -> 已完成：需要有验收文档和签字确认
        if (from == ProjectStatus.ACCEPTANCE && to == ProjectStatus.COMPLETED) {
            if (!Boolean.TRUE.equals(context.get("hasAcceptanceDoc"))) {
                return TransitionValidation.invalid("需要有验收文档才能完成项目");
            }
            if (!Boolean.TRUE.equals(context.get("signOff"))) {
                return TransitionValidation.invalid("需要有客户签字确认才能完成项目");
            }
        }

        // 已完成不可再修改
        if (from == ProjectStatus.COMPLETED && to != ProjectStatus.COMPLETED) {
            return TransitionValidation.invalid("已完成的项目不能再进行状态转换");
        }

        return TransitionValidation.valid();
    }

    /**
     * 产品流转规则检查（轨道 B）
     * 当前版本：仅依赖状态机定义的线性顺序，不引入审批流或复杂 OA 规则。
     */
    private TransitionValidation checkProductRules(ProductStatus from, ProductStatus to, Map<String, Object> context) {
        // LAUNCHED / SHELVED 视为终态，不允许再流转
        if ((from == ProductStatus.LAUNCHED || from == ProductStatus.SHELVED) && to != from) {
            return TransitionValidation.invalid("已结束的产品流不能再进行状态转换");
        }

        return TransitionValidation.valid();
    }

    /**
     * 获取项目状态定义
     */
    public ProjectStateDef getProjectState(ProjectStatus status) {
        return PROJECT_STATES.get(status);
    }

    /**
     * 获取产品流状态定义
     */
    public ProductStateDef getProductState(ProductStatus status) {
        return PRODUCT_STATES.get(status);
    }

    /**
     * Record 定义
     */
    public record ProjectStateDef(
            String name,
            String color,
            List<ProjectStatus> next,
            Map<String, Object> requirements
    ) {}

    public record ProductStateDef(
            String name,
            String color,
            List<ProductStatus> next,
            Map<String, Object> requirements
    ) {}
}