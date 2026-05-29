#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${ROOT_DIR}"
docker compose -f docker-compose.infra.yml up -d

echo "Infrastructure is starting. Useful URLs:"
echo "- Nacos:    http://localhost:8848/nacos"
echo "- RabbitMQ: http://localhost:15672  user=lowaltitude password=lowaltitude123"
echo "- MySQL:    localhost:3306 user=lowaltitude password=lowaltitude123"
echo "- Redis:    localhost:6379"
