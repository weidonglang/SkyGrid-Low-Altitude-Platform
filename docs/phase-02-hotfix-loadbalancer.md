# Phase 02 Hotfix: Gateway lb:// 路由 503 修复

## 问题

Gateway `/actuator/health` 正常，Nacos 能看到服务列表，但访问 `/api/auth/dev-token` 时返回 503，导致 smoke-test 将错误文本误当作 token。

## 原因

`low-altitude-gateway` 使用 `lb://user-org-service` 等路由，但 POM 中缺少 `spring-cloud-starter-loadbalancer`，导致 Gateway 无法真正执行基于服务名的负载均衡转发。

## 修复

在 `low-altitude-gateway/pom.xml` 中新增：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

同时修复 `scripts/phase02-smoke-test.bat`，避免 PowerShell 错误文本被写入 Authorization Header。

## 操作

1. IDEA Maven Reload。
2. 只重启 `low-altitude-gateway`。
3. 执行：

```bat
scripts\phase02-smoke-test.bat
```
