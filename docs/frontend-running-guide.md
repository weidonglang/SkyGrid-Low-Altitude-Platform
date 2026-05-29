# 前端运行与页面说明

## 1. 前端目录

```text
low-altitude-web
```

技术栈：

```text
Vue 3
Vite
Element Plus
Axios
ECharts 预留
```

## 2. 启动方式

确保后端 Gateway 已运行在：

```text
http://127.0.0.1:8080
```

然后执行：

```bat
scripts\start-web.bat
```

或手动：

```bat
cd low-altitude-web
npm install
npm run dev
```

浏览器打开：

```text
http://127.0.0.1:5173
```

## 3. 页面功能

| 页面 | 功能 |
|---|---|
| 运行状态 | 查看 Gateway health、服务发现状态、获取开发 token |
| 资源网格 | 展示 Grid 二维网格、Grid 状态、航线模板经过网格 |
| 预约申请 / Pre-check | 填写申请、执行预检查、展示硬冲突和风险提示 |
| 审批工作台 | 查询预约、审批、驳回、取消、查看流程和占用 |
| 冲突记录 | 查看 HARD / RISK 冲突记录 |

## 4. 推荐演示流程

1. 打开运行状态页面。
2. 点击获取 ADMIN Token。
3. 刷新基础数据。
4. 进入资源网格页面，展示 ACTIVE / RISK / NO_FLY 网格。
5. 进入预约申请页面，选择默认航线、L1、高度层、时间片，执行 Pre-check。
6. 提交预约申请。
7. 进入审批工作台，审批通过。
8. 查看生成的资源占用记录。
9. 再提交相同资源申请，展示 HARD 冲突阻断。
10. 选择禁飞航线 `RT-NOFLY-01`，展示 NO_FLY 阻断。
11. 进入冲突记录页面查看记录。

## 5. 常见问题

### 页面无法访问后端

检查 Gateway：

```bat
curl http://127.0.0.1:8080/actuator/health
```

### token 失效

重新在运行状态页面获取 ADMIN Token。

### 前端依赖安装失败

删除 `node_modules` 和 `package-lock.json` 后重新安装：

```bat
cd low-altitude-web
rmdir /s /q node_modules
del package-lock.json
npm install
```

### 中文显示问题

前端页面通常能正常显示中文；CMD/PowerShell 中看到乱码多半是终端编码问题，不代表后端数据错误。
