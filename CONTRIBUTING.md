# Contributing

感谢参与 Low Altitude Platform。为了让协作更顺畅，请在提交 Issue 或 Pull Request 前阅读本说明。

## 开发流程

1. Fork 仓库并创建功能分支，分支名建议使用 `feat/...`、`fix/...`、`docs/...`。
2. 本地完成改动并补充必要测试或文档。
3. 提交前运行后端和前端基础检查：

```powershell
mvn test
cd low-altitude-web
npm ci
npm run build
```

4. 创建 Pull Request，并在描述中说明变更内容、验证方式和潜在影响。

## 提交规范

推荐使用简短、明确的提交信息：

```text
feat: add booking conflict pre-check
fix: handle duplicate notification message
docs: update deployment guide
```

## 代码约定

- 后端遵循现有 Spring Boot、MyBatis 和分层结构。
- 前端遵循现有 Vue 3、Element Plus 和组件拆分方式。
- 不提交 `target/`、`node_modules/`、`dist/`、日志文件或本地配置。
- 不在代码和文档中提交真实密钥、生产密码、内网地址或个人凭据。

## Issue 建议

报告问题时请尽量提供：

- 复现步骤
- 实际结果和期望结果
- 运行环境，包括 JDK、Maven、Node.js、Docker 和操作系统版本
- 相关日志、截图或接口响应
