#!/bin/bash
set -e

echo "=== 拉取最新代码 ==="
git pull origin main

VERSION_FE=$(date +%Y%m%d%H%M)
VERSION_BE=$(date +%Y%m%d%H%M)

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
docker inspect zkr-lab-erp-demo --format "Frontend: {{.Config.Image}}"
docker inspect zkr-erp-backend --format "Backend: {{.Config.Image}}"
