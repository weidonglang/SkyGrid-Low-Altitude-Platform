# Phase 05：低空资源可视化前端开发总结

## 一、目标

Phase 05 的目标是把 Phase 01–04 已经完成的后端能力从脚本演示升级为网页演示，使系统具备更直观的答辩展示形态。

核心展示对象包括：

- Gateway / Nacos 服务发现状态。
- Grid 二维网格图。
- Level 高度层与 TimeSlot 时间片选择。
- RouteTemplate 航线模板及经过网格序列。
- 预约申请表单。
- Pre-check 冲突检测结果。
- 审批工作台。
- ResourceOccupancy 占用记录。
- ConflictRecord 冲突记录。

## 二、新增模块

新增前端模块：

```text
low-altitude-web
```

技术栈：

```text
Vue 3
Vite
Element Plus
Axios
ECharts 预留依赖
```

## 三、页面结构

前端使用单页控制台结构，暂不引入复杂路由，降低调试成本。

包含以下 Tab：

1. 运行状态
2. 资源网格
3. 预约申请 / Pre-check
4. 审批工作台
5. 冲突记录

## 四、关键演示路径

### 1. 正常链路

```text
获取 ADMIN Token
→ 刷新基础数据
→ 选择默认航线 RT-DEMO-01
→ 执行 Pre-check
→ 提交预约
→ 审批通过
→ 生成资源占用记录
```

### 2. 风险链路

默认航线经过 `G-02-01`，该网格状态为 `RISK`。

因此 Pre-check 会产生风险提示，但不阻断提交。

### 3. 硬冲突链路

在已有 `OCCUPIED` 记录的情况下，对同一天、同 Grid、同 Level、同 TimeSlot 再次申请，会触发：

```text
SAME_GRID_LEVEL_TIME
```

系统会阻断提交或审批。

### 4. 禁飞区链路

选择经过 `NO_FLY` 网格的航线，例如 `RT-NOFLY-01`，Pre-check 会触发：

```text
NO_FLY_GRID
```

系统直接阻断提交。

## 五、后端配套调整

为支持前端独立运行，本阶段对 Gateway 做了两处兼容增强：

1. 增加 Gateway 全局 CORS 配置。
2. JWT 过滤器放行 OPTIONS 预检请求。

如果使用 Vite proxy，本地开发通常不依赖 CORS；但这两处增强可以避免后续静态部署时出现跨域问题。

## 六、运行方式

先启动后端 5 个服务，再启动前端：

```bat
cd low-altitude-web
npm install
npm run dev
```

打开：

```text
http://127.0.0.1:5173
```

## 七、验收标准

Phase 05 通过标准：

- 页面能获取 ADMIN Token。
- 页面能显示 Gateway health 和服务发现列表。
- 页面能加载 Grid / Level / TimeSlot / RouteTemplate。
- 页面能显示 Grid 二维网格图。
- 页面能执行 Pre-check 并显示 risk / blocking 数量。
- 页面能提交预约申请。
- 页面能审批通过、生成占用记录。
- 页面能取消申请并释放占用。
- 页面能查询冲突记录。
