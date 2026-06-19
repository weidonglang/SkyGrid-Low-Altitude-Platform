# SkyGrid Configuration Guide

## Local Defaults

SkyGrid includes local demo defaults for:

- MySQL
- Redis
- RabbitMQ
- Nacos
- JWT secret

These values are for local development only and are exposed through environment-variable fallbacks in `application.yml`.

## Required Environment Variables for Non-local Use

```text
NACOS_ADDR
MYSQL_HOST
MYSQL_PORT
MYSQL_USER
MYSQL_PASSWORD
REDIS_HOST
REDIS_PORT
RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USER
RABBITMQ_PASSWORD
JWT_SECRET
```

## Local Files Ignored by Git

```text
.env
.env.*
logs/
target/
low-altitude-web/node_modules/
low-altitude-web/dist/
```

## Public Repository Rule

Do not commit production passwords, real JWT secrets, cloud endpoints, access tokens, or private IP addresses. Use `.env.example` and `application-prod.example.yml` style files for documentation.
