# 开发者参与指南

## 概述

代码托管在 Git，开发者在本地修改代码后推送到 Git，服务器从 Git 拉取最新代码并重新构建容器镜像。

## 完整流程

```
本地开发 → Git push → 服务器 pull → 重新构建镜像 → 更新容器
```

---

## 第一步：本地开发环境准备

### 1.1 克隆代码

```bash
git clone https://github.com/kangkang0218/ZKR.git
cd ZKR
```

### 1.2 前端开发（可选本地预览）

```bash
cd lab-erp-demo
npm install
npm run dev   # 本地预览，无需登录公司网络
```

修改 `vite.config.js` 中的 `target` 指向公司服务器地址，可预览实际效果。

### 1.3 后端开发

使用 IDE（IntelliJ IDEA / VS Code + Extension Pack for Java）打开 `erp-backend` 目录，基于 `pom.xml` 加载 Maven 项目。

本地运行需要：
- Java 17+
- PostgreSQL（可连接公司数据库，或本地启动一个）
- 正确配置 `.env`

### 1.4 提交代码

```bash
git checkout -b feature/xxx
git add .
git commit -m "描述改动"
git push -u origin feature/xxx
# 然后在 GitHub 上提 Pull Request
```

---

## 第二步：服务器部署更新

**以下操作在服务器上执行**（公司当前这台机器）

### 2.1 拉取最新代码

```bash
cd /home/a/zhangqi/workspace/ZKR
git pull origin main
```

### 2.2 重新构建镜像

```bash
# 前端
docker build --build-arg APP_VERSION=v1.102 \
  -t 127.0.0.1:5555/zhangqi_frontend:v1.102 \
  ./lab-erp-demo

docker push 127.0.0.1:5555/zhangqi_frontend:v1.102

# 后端
docker build --build-arg APP_VERSION=v1.68 \
  -t 127.0.0.1:5555/zhangqi_backend:v1.68 \
  ./erp-backend

docker push 127.0.0.1:5555/zhangqi_backend:v1.68
```

### 2.3 更新 docker-compose.yml

编辑 `docker-compose.yml`，将对应的镜像 tag 改为新版本：

```yaml
services:
  erp-backend:
    image: 127.0.0.1:5555/zhangqi_backend:v1.68   # 改这里
  lab-erp-demo:
    image: 127.0.0.1:5555/zhangqi_frontend:v1.102  # 改这里
```

### 2.4 重新部署

```bash
docker compose up -d erp-backend lab-erp-demo
```

### 2.5 验证

```bash
docker compose ps
docker inspect zkr-lab-erp-demo --format '{{.Config.Image}}'
docker inspect zkr-erp-backend --format '{{.Config.Image}}'
```

---

## 快速部署脚本

在服务器上保存为 `deploy.sh`，以后每次更新只需运行：

```bash
#!/bin/bash
set -e

VERSION_FE=$(date +%Y%m%d%H%M)
VERSION_BE=$(date +%Y%m%d%H%M)

echo "=== 拉取最新代码 ==="
git pull origin main

echo "=== 构建前端镜像 v${VERSION_FE} ==="
docker build --build-arg APP_VERSION=v${VERSION_FE} \
  -t 127.0.0.1:5555/zhangqi_frontend:v${VERSION_FE} \
  -t 127.0.0.1:5555/zhangqi_frontend:latest \
  ./lab-erp-demo

echo "=== 推送前端镜像 ==="
docker push 127.0.0.1:5555/zhangqi_frontend:v${VERSION_FE}
docker push 127.0.0.1:5555/zhangqi_frontend:latest

echo "=== 构建后端镜像 v${VERSION_BE} ==="
docker build --build-arg APP_VERSION=v${VERSION_BE} \
  -t 127.0.0.1:5555/zhangqi_backend:v${VERSION_BE} \
  -t 127.0.0.1:5555/zhangqi_backend:latest \
  ./erp-backend

echo "=== 推送后端镜像 ==="
docker push 127.0.0.1:5555/zhangqi_backend:v${VERSION_BE}
docker push 127.0.0.1:5555/zhangqi_backend:latest

echo "=== 更新 docker-compose.yml ==="
sed -i "s|zhangqi_frontend:.*|zhangqi_frontend:v${VERSION_FE}|" docker-compose.yml
sed -i "s|zhangqi_backend:.*|zhangqi_backend:v${VERSION_BE}|" docker-compose.yml

echo "=== 重新部署 ==="
docker compose up -d erp-backend lab-erp-demo

echo "=== 完成 ==="
docker inspect zkr-lab-erp-demo --format 'Frontend: {{.Config.Image}}'
docker inspect zkr-erp-backend --format 'Backend: {{.Config.Image}}'
```

---

## 权限说明

| 操作 | 权限要求 |
|------|----------|
| Git clone / push | GitHub 账号（已授权） |
| docker pull / push | 服务器 Docker daemon 访问权 |
| 修改 docker-compose.yml | 服务器文件写权限 |

---

## 注意事项

1. **不要在运行时容器内修改代码** — 所有改动通过 Git 管理，重启后不保留
2. **每个版本号只能用一次** — 不要复用旧 tag 覆盖新内容
3. **提交前本地验证** — 确保代码能正常构建
4. **敏感信息不上 Git** — 所有密钥通过 `.env` 环境变量注入，`application.yml` 中无硬编码密钥
