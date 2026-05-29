# Windows + local MySQL(root / 123123) runbook

This project can be started in two modes.

## Mode A: Docker also starts MySQL

Use this when port 3306 is free.

```bat
scripts\start-infra.bat
```

The Docker MySQL root password is set to `123123` in this patched version.

If you previously started the old MySQL container with another root password, Docker will keep the old data volume and ignore the new password. For a clean Phase-01 database only, run:

```bat
docker compose -f docker-compose.infra.yml down -v
scripts\start-infra.bat
```

## Mode B: use your existing local MySQL root / 123123

Use this when your local MySQL already occupies 3306.

Start only Nacos, Redis and RabbitMQ:

```bat
scripts\start-middleware-no-mysql.bat
```

Create the four databases once:

```bat
mysql -uroot -p123123 < docker\mysql\local-root-init.sql
```

Then start services in this order:

1. user-org-service
2. resource-service
3. booking-service
4. conflict-notify-service
5. low-altitude-gateway

## Required IDEA environment variables

In each Run Configuration, set:

```text
NACOS_ADDR=127.0.0.1:8848;NACOS_REGISTER_IP=127.0.0.1;MYSQL_USER=root;MYSQL_PASSWORD=123123;RABBITMQ_USER=lowaltitude;RABBITMQ_PASSWORD=lowaltitude123
```

## Nacos check

Before starting Spring services, open:

```text
http://127.0.0.1:8848/nacos/
```

Nacos 2.x also needs gRPC port 9848. On Windows:

```bat
netstat -ano | findstr "8848 9848"
```

If 9848 is not listening, the service may print `Client not connected, current status: STARTING` during registration.
