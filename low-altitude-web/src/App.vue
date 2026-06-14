<template>
  <el-config-provider>
    <div class="command-shell">
      <aside class="control-sidebar">
        <div class="brand-block">
          <div class="brand-mark">LA</div>
          <div>
            <h1>低空航线态势控制台</h1>
            <p>Route · Grid · Level · TimeSlot</p>
          </div>
        </div>

        <div class="sidebar-health">
          <div class="health-row"><span>Gateway</span><b :class="healthOk ? 'ok' : 'bad'">{{ healthOk ? 'UP' : 'UNKNOWN' }}</b></div>
          <div class="health-row"><span>Nacos Services</span><b>{{ serviceList.length }}</b></div>
          <div class="health-row"><span>Token</span><b :class="token ? 'ok' : 'bad'">{{ token ? 'READY' : 'EMPTY' }}</b></div>
        </div>

        <button class="primary-action" @click="getDevToken" :disabled="loading.token">
          {{ loading.token ? '获取中...' : '获取 ADMIN Token' }}
        </button>
        <button class="secondary-action" @click="loadAll" :disabled="loading.all">
          {{ loading.all ? '刷新中...' : '刷新全量态势数据' }}
        </button>

        <nav class="main-nav">
          <button v-for="item in navItems" :key="item.key" :class="{ active: activeView === item.key }" @click="activeView = item.key">
            <span class="nav-icon">{{ item.icon }}</span>
            <span>{{ item.label }}</span>
          </button>
        </nav>
      </aside>

      <main class="workspace">
        <header class="hero-header">
          <div>
            <p class="eyebrow">Low-altitude Digital Operation Console</p>
            <h2>{{ currentTitle }}</h2>
            <p>以 2.5D 空域图为中心，统一展示航线模板、低空网格、高度层、时间片、资源占用、冲突检测、Outbox 事件链路与服务治理指标。</p>
          </div>
          <div class="hero-actions">
            <el-date-picker v-model="selectedDate" type="date" value-format="YYYY-MM-DD" size="large" />
            <el-select v-model="selectedLevelId" size="large" placeholder="高度层" style="width: 190px">
              <el-option v-for="l in levels" :key="l.id" :label="levelLabel(l)" :value="l.id" />
            </el-select>
            <el-select v-model="selectedTimeSlotId" size="large" placeholder="时间片" style="width: 205px">
              <el-option v-for="t in timeSlots" :key="t.id" :label="timeSlotLabel(t)" :value="t.id" />
            </el-select>
          </div>
        </header>

        <section class="kpi-grid">
          <div class="kpi-card"><span>Grid</span><b>{{ grids.length }}</b><small>低空区域网格</small></div>
          <div class="kpi-card"><span>Routes</span><b>{{ routeTemplates.length }}</b><small>航线模板</small></div>
          <div class="kpi-card"><span>Occupied</span><b>{{ occupiedCount }}</b><small>当前占用单元</small></div>
          <div class="kpi-card"><span>Conflicts</span><b>{{ activeConflictCount }}</b><small>活跃冲突记录</small></div>
          <div class="kpi-card"><span>Outbox</span><b>{{ outboxSummary.pending ?? 0 }}</b><small>待投递消息</small></div>
          <div class="kpi-card"><span>Notify</span><b>{{ notificationSummary.notifySent ?? 0 }}</b><small>已发送通知</small></div>
        </section>

        <section v-if="activeView === 'dashboard'" class="view-stack cockpit-stack">
          <div class="scenario-strip">
            <button v-for="scenario in cockpitScenarios" :key="scenario.key" :class="{ active: selectedScenario === scenario.key }" @click="applyScenario(scenario.key)">
              <b>{{ scenario.label }}</b>
              <span>{{ scenario.desc }}</span>
            </button>
          </div>

          <div class="cockpit-layout">
            <aside class="cockpit-left glass-panel">
              <p class="eyebrow">Situation Metrics</p>
              <h3>今日低空态势</h3>
              <div class="cockpit-metric-list">
                <div><span>今日任务</span><b>{{ cockpitStats.total }}</b><small>巡检/应急/仓储</small></div>
                <div><span>执行中</span><b>{{ cockpitStats.running }}</b><small>航线流光显示</small></div>
                <div><span>待审批</span><b>{{ cockpitStats.pending }}</b><small>审批工作台处理</small></div>
                <div><span>硬冲突</span><b class="danger">{{ cockpitStats.hardConflicts }}</b><small>不可直接通过</small></div>
                <div><span>风险提示</span><b class="warning">{{ cockpitStats.riskConflicts }}</b><small>建议人工复核</small></div>
                <div><span>MQ 待补偿</span><b>{{ cockpitStats.outboxPending }}</b><small>最终一致性链路</small></div>
              </div>

              <div class="demo-script-panel">
                <p class="eyebrow">Defense Storyline</p>
                <h3>答辩演示步骤</h3>
                <button
                  v-for="step in cockpitDemoSteps"
                  :key="step.key"
                  :class="{ active: selectedDemoStep === step.key }"
                  type="button"
                  @click="selectedDemoStep = step.key"
                >
                  <b>{{ step.title }}</b>
                  <span>{{ step.desc }}</span>
                </button>
              </div>
            </aside>

            <AirspaceRouteMap
              title="低空园区时空资源态势"
              :grids="cockpitGrids"
              :route-detail="selectedCockpitMission"
              :mission-routes="cockpitMissionRoutes"
              :scene-features="cockpitScene"
              :active-route-id="selectedMissionId"
              :conflicts="cockpitConflicts"
              :occupancies="cockpitOccupancies"
              :selected-grid-id="selectedGrid?.id"
              :selected-level-id="selectedLevelId"
              :selected-time-slot-id="selectedTimeSlotId"
              :selected-date="selectedDate"
              :level-label="currentLevelText"
              :time-label="currentTimeText"
              @select-grid="selectedGrid = $event"
              @select-conflict="selectCockpitConflict"
              @select-route="selectCockpitMission"
            />

            <aside class="cockpit-right detail-drawer">
              <p class="eyebrow">Mission Detail</p>
              <h3>{{ selectedCockpitMission?.code || '请选择任务航线' }}</h3>
              <p class="muted">{{ selectedCockpitMission?.name || '点击中间地图的航线、无人机或冲突点，查看任务占用、冲突对象和消解方案。' }}</p>

              <div v-if="selectedCockpitMission" class="mission-detail-grid">
                <div><span>任务类型</span><b>{{ selectedCockpitMission.type }}</b></div>
                <div><span>申请组织</span><b>{{ selectedCockpitMission.org }}</b></div>
                <div><span>高度层</span><b>{{ selectedCockpitMission.levelName }}</b></div>
                <div><span>计划时段</span><b>{{ selectedCockpitMission.timeText }}</b></div>
                <div><span>当前状态</span><b :class="selectedCockpitMission.status">{{ missionStatusText(selectedCockpitMission.status) }}</b></div>
                <div><span>占用网格</span><b>{{ selectedCockpitMission.grids.length }}</b></div>
              </div>

              <div class="route-steps" v-if="selectedCockpitMission?.grids?.length">
                <div v-for="g in selectedCockpitMission.grids" :key="`${selectedCockpitMission.id}-${g.gridId}`" class="route-step">
                  <span>{{ g.sequenceNo }}</span>
                  <div><b>{{ g.gridCode }}</b><small>{{ g.gridName }} / {{ g.gridStatus }}</small></div>
                </div>
              </div>

              <div class="resolution-panel" v-if="selectedCockpitConflict">
                <p class="eyebrow">Conflict Resolution</p>
                <h3>{{ selectedCockpitConflict.conflictType === 'HARD' ? '检测到硬冲突' : '风险复核建议' }}</h3>
                <p>{{ selectedCockpitConflict.message }}</p>
                <div class="resolution-item"><b>方案 A</b><span>延后至 {{ selectedCockpitConflict.nextSlot || '下一个空闲 TimeSlot' }}</span></div>
                <div class="resolution-item"><b>方案 B</b><span>切换至 {{ selectedCockpitConflict.altLevel || '相邻安全高度层' }}</span></div>
                <div class="resolution-item"><b>方案 C</b><span>绕行 {{ selectedCockpitConflict.reroute || '风险网格外侧航线' }}</span></div>
                <div class="action-row">
                  <button class="primary-action inline" type="button" @click="adoptResolution('delay')">采用方案 A</button>
                  <button class="secondary-action inline" type="button" @click="adoptResolution('level')">采用方案 B</button>
                  <button class="secondary-action inline" type="button" @click="adoptResolution('review')">提交人工复核</button>
                </div>
              </div>

              <div v-if="selectedMissionResolution" class="resolution-applied">
                <b>{{ selectedMissionResolution.label }}</b>
                <span>{{ selectedMissionResolution.detail }}</span>
              </div>

              <div class="ai-advisor-panel">
                <div class="ai-advisor-head">
                  <p class="eyebrow">Qwen3 Quick Think</p>
                  <span :class="['ai-status-pill', aiAdvisorSource]">{{ aiAdvisorSource === 'ollama' ? 'Ollama 已接入' : '前端规则预览' }}</span>
                </div>
                <h3>千问3 8B 快速研判</h3>
                <div class="ai-config-line">
                  <span>模型：{{ ollamaModel }}</span>
                  <span>地址：127.0.0.1:11434</span>
                </div>
                <p class="muted">围绕当前任务、Grid/Level/TimeSlot、冲突规则和一致性链路生成答辩可讲的调度建议。</p>
                <div class="action-row">
                  <button class="primary-action inline" type="button" :disabled="aiAdvisorLoading" @click="runAiQuickThink">
                    {{ aiAdvisorLoading ? '研判中...' : '本地研判' }}
                  </button>
                  <button class="secondary-action inline" type="button" @click="aiAdvisorResult = fallbackAiAdvice()">规则预览</button>
                </div>
                <pre class="ai-output">{{ aiAdvisorResult || cockpitAiPreview }}</pre>
              </div>
            </aside>
          </div>

          <div class="cockpit-bottom">
            <div class="level-panel glass-panel">
              <p class="eyebrow">Altitude Layer</p>
              <h3>高度层切换</h3>
              <button v-for="level in cockpitLevels" :key="level.id" :class="{ active: String(selectedLevelId) === String(level.id) }" @click="selectedLevelId = level.id">
                <span>{{ level.levelCode || `L${level.id}` }}</span>
                <b>{{ level.levelName || level.name }}</b>
                <small>{{ levelTaskCount(level.id) }} 个任务</small>
              </button>
            </div>

            <TimeSlotTimeline
              v-model="selectedTimeSlotId"
              :slots="cockpitTimeSlots"
              :occupancies="cockpitAllOccupancies"
              :conflicts="cockpitAllConflicts"
            />

            <div class="consistency-mini glass-panel">
              <p class="eyebrow">Event Chain</p>
              <h3>一致性链路</h3>
              <p class="muted">{{ cockpitScenarioBrief }}</p>
              <div class="mini-chain">
                <div v-for="node in cockpitChain" :key="node.key" :class="['mini-chain-node', node.state]">
                  <b>{{ node.label }}</b>
                  <span>{{ node.value }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section v-if="activeView === 'dashboard-old'" class="view-stack">
          <div class="layout-2-1">
            <AirspaceRouteMap
              title="今日低空航线态势"
              :grids="grids"
              :route-detail="selectedRoute"
              :conflicts="visualConflicts"
              :occupancies="visibleOccupancies"
              :selected-grid-id="selectedGrid?.id"
              :selected-level-id="selectedLevelId"
              :selected-time-slot-id="selectedTimeSlotId"
              :selected-date="selectedDate"
              :level-label="currentLevelText"
              :time-label="currentTimeText"
              @select-grid="selectedGrid = $event"
              @select-conflict="selectedConflict = $event"
            />
            <div class="glass-panel">
              <p class="eyebrow">System Snapshot</p>
              <h3>服务与事件状态</h3>
              <div class="status-list">
                <div v-for="s in serviceList" :key="s"><span>{{ s }}</span><b>UP</b></div>
              </div>
              <div class="event-feed">
                <div v-for="event in recentEvents" :key="event.key" class="event-item">
                  <strong>{{ event.title }}</strong>
                  <span>{{ event.detail }}</span>
                </div>
              </div>
            </div>
          </div>
          <TimeSlotTimeline v-model="selectedTimeSlotId" :slots="timeSlots" :occupancies="visibleOccupancies" />
        </section>

        <section v-if="activeView === 'route-situation'" class="view-stack">
          <div class="layout-left-map-right">
            <RouteTemplatePanel :routes="routeTemplatesWithDetails" v-model="form.routeTemplateId" @select="selectRouteTemplate" />
            <AirspaceRouteMap
              title="航线模板与资源占用叠加图"
              :grids="grids"
              :route-detail="selectedRoute"
              :conflicts="visualConflicts"
              :occupancies="visibleOccupancies"
              :selected-grid-id="selectedGrid?.id"
              :selected-level-id="selectedLevelId"
              :selected-time-slot-id="selectedTimeSlotId"
              :selected-date="selectedDate"
              :level-label="currentLevelText"
              :time-label="currentTimeText"
              @select-grid="selectedGrid = $event"
              @select-conflict="selectedConflict = $event"
            />
            <div class="detail-drawer">
              <p class="eyebrow">Route / Grid Detail</p>
              <h3>{{ selectedRoute?.routeCode || '未选择航线' }}</h3>
              <p class="muted">{{ selectedRoute?.routeName || '请选择左侧航线模板查看路径。' }}</p>
              <div class="route-steps" v-if="selectedRoute?.grids?.length">
                <div v-for="g in sortedRouteGrids" :key="`${g.gridId}-${g.sequenceNo}`" class="route-step">
                  <span>{{ g.sequenceNo }}</span>
                  <div><b>{{ g.gridCode }}</b><small>{{ g.gridName }} / {{ g.gridStatus }}</small></div>
                </div>
              </div>
              <div class="mini-json" v-if="selectedGrid">
                <b>选中 Grid</b>
                <pre>{{ pretty(selectedGrid) }}</pre>
              </div>
            </div>
          </div>
          <TimeSlotTimeline v-model="selectedTimeSlotId" :slots="timeSlots" :occupancies="visibleOccupancies" />
        </section>


        <section v-if="activeView === 'route-editor'" class="view-stack">
          <div class="layout-left-map-right editor-page">
            <div class="glass-panel form-panel">
              <p class="eyebrow">Route Builder</p>
              <h3>航线编辑器</h3>
              <p class="muted">在中间 2.5D 网格中按顺序点击 Grid，系统会生成带序号的草稿航线。保存后会写入 route_template 与 route_template_grid。</p>
              <el-form label-position="top">
                <el-form-item label="航线编码"><el-input v-model="routeDraft.routeCode" /></el-form-item>
                <el-form-item label="航线名称"><el-input v-model="routeDraft.routeName" /></el-form-item>
                <el-form-item label="单点预计通过时间/min"><el-input-number v-model="routeDraft.plannedDurationMinutes" :min="1" :max="60" /></el-form-item>
                <el-form-item label="说明"><el-input v-model="routeDraft.description" type="textarea" :rows="3" /></el-form-item>
              </el-form>
              <div class="route-draft-list">
                <div v-for="(gid, index) in routeDraft.gridIds" :key="`${gid}-${index}`" class="route-draft-item">
                  <span>{{ index + 1 }}</span>
                  <b>{{ gridById(gid)?.gridCode || gid }}</b>
                  <small>{{ gridById(gid)?.gridName || '' }}</small>
                  <button class="ghost-mini" @click="removeDraftPoint(index)">移除</button>
                </div>
              </div>
              <div class="action-row wrap">
                <button class="primary-action inline" @click="saveDraftRoute" :disabled="routeDraft.gridIds.length < 2">保存航线模板</button>
                <button class="secondary-action inline" @click="clearDraftRoute">清空草稿</button>
                <button class="warning-action" @click="useDraftForPrecheck" :disabled="!routeEditorResult">用于预检查</button>
              </div>
              <div v-if="routeEditorResult" class="mini-json"><b>最近保存</b><pre>{{ pretty(routeEditorResult) }}</pre></div>
            </div>
            <AirspaceRouteMap
              title="点击网格编辑航线草稿"
              :grids="grids"
              :route-detail="null"
              :compare-routes="compareRouteDetails"
              :editor-points="routeDraft.gridIds"
              editor-mode
              :conflicts="visualConflicts"
              :occupancies="visibleOccupancies"
              :selected-grid-id="selectedGrid?.id"
              :selected-level-id="selectedLevelId"
              :selected-time-slot-id="selectedTimeSlotId"
              :selected-date="selectedDate"
              :level-label="currentLevelText"
              :time-label="currentTimeText"
              @select-grid="addDraftGrid"
              @select-conflict="selectedConflict = $event"
            />
            <div class="detail-drawer">
              <p class="eyebrow">Editor Guide</p>
              <h3>航线拓扑说明</h3>
              <p class="muted">草稿航线使用橙色虚线显示，左侧列表同步显示经过网格顺序。保存后可在航线库、多航线对比和预约 Pre-check 中直接使用。</p>
              <div class="status-list">
                <div><span>草稿节点</span><b>{{ routeDraft.gridIds.length }}</b></div>
                <div><span>风险点</span><b>{{ draftRiskCount }}</b></div>
                <div><span>禁飞点</span><b>{{ draftNoFlyCount }}</b></div>
              </div>
              <div class="route-steps" v-if="routeDraft.gridIds.length">
                <div v-for="(gid, index) in routeDraft.gridIds" :key="`step-${gid}-${index}`" class="route-step">
                  <span>{{ index + 1 }}</span>
                  <div><b>{{ gridById(gid)?.gridCode }}</b><small>{{ gridById(gid)?.status }} / {{ routeDraft.plannedDurationMinutes }} min</small></div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section v-if="activeView === 'route-compare'" class="view-stack">
          <div class="compare-toolbar glass-panel">
            <div>
              <p class="eyebrow">Multi-route Comparison</p>
              <h3>多航线对比与冲突观察</h3>
              <p class="muted">同时叠加多条航线模板、当前占用和冲突结果，用于观察不同低空巡检任务在 Grid / Level / TimeSlot 上的空间关系。</p>
            </div>
            <el-select v-model="compareRouteIds" multiple collapse-tags collapse-tags-tooltip placeholder="选择要对比的航线" style="min-width: 420px">
              <el-option v-for="r in routeTemplates" :key="r.id" :label="routeLabel(r)" :value="r.id" />
            </el-select>
          </div>
          <div class="layout-2-1">
            <AirspaceRouteMap
              title="多航线叠加态势"
              :grids="grids"
              :route-detail="selectedRoute"
              :compare-routes="compareRouteDetails"
              :conflicts="visualConflicts"
              :occupancies="visibleOccupancies"
              :selected-grid-id="selectedGrid?.id"
              :selected-level-id="selectedLevelId"
              :selected-time-slot-id="selectedTimeSlotId"
              :selected-date="selectedDate"
              :level-label="currentLevelText"
              :time-label="currentTimeText"
              @select-grid="selectedGrid = $event"
              @select-conflict="selectedConflict = $event"
            />
            <div class="glass-panel">
              <p class="eyebrow">Comparison Matrix</p>
              <h3>航线对比摘要</h3>
              <div class="compare-card" v-for="item in routeCompareStats" :key="item.id">
                <b>{{ item.code }}</b>
                <span>{{ item.name }}</span>
                <div class="stat-pills"><span>{{ item.gridCount }} Grid</span><span class="warning">{{ item.riskCount }} Risk</span><span class="danger">{{ item.noFlyCount }} No-fly</span></div>
                <small>{{ item.path }}</small>
              </div>
            </div>
          </div>
        </section>

        <section v-if="activeView === 'planner'" class="view-stack">
          <div class="planner-grid">
            <div class="glass-panel form-panel">
              <p class="eyebrow">Mission Planner</p>
              <h3>预约申请与冲突预检查</h3>
              <el-form label-width="100px" label-position="top">
                <el-form-item label="任务名称"><el-input v-model="form.taskName" /></el-form-item>
                <el-form-item label="航线模板">
                  <el-select v-model="form.routeTemplateId" class="full" @change="loadRouteDetail">
                    <el-option v-for="r in routeTemplates" :key="r.id" :label="routeLabel(r)" :value="r.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="高度层">
                  <el-select v-model="form.levelId" class="full" @change="selectedLevelId = form.levelId">
                    <el-option v-for="l in levels" :key="l.id" :label="levelLabel(l)" :value="l.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="预约日期"><el-date-picker v-model="form.bookingDate" type="date" value-format="YYYY-MM-DD" class="full" @change="selectedDate = form.bookingDate" /></el-form-item>
                <el-form-item label="时间片">
                  <el-select v-model="form.timeSlotIds" multiple class="full" @change="syncTimeSlotFromForm">
                    <el-option v-for="t in timeSlots" :key="t.id" :label="timeSlotLabel(t)" :value="t.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="申请理由"><el-input v-model="form.applyReason" type="textarea" :rows="3" /></el-form-item>
                <div class="action-row">
                  <button class="warning-action" type="button" @click="preCheck" :disabled="loading.preCheck">{{ loading.preCheck ? '检测中...' : '冲突预检查' }}</button>
                  <button class="primary-action inline" type="button" @click="submitBooking" :disabled="loading.submit || (preCheckResult && !preCheckResult.canSubmit)">{{ loading.submit ? '提交中...' : '提交申请' }}</button>
                </div>
              </el-form>
            </div>
            <AirspaceRouteMap
              title="候选航线与冲突点映射"
              :grids="grids"
              :route-detail="selectedRoute"
              :conflicts="preCheckResult?.conflicts || []"
              :occupancies="visibleOccupancies"
              :selected-level-id="form.levelId"
              :selected-time-slot-id="form.timeSlotIds?.[0]"
              :selected-date="form.bookingDate"
              :level-label="formLevelText"
              :time-label="formTimeText"
              compact
              @select-grid="selectedGrid = $event"
              @select-conflict="selectedConflict = $event"
            />
            <div class="glass-panel result-panel">
              <p class="eyebrow">Pre-check Result</p>
              <h3>{{ preCheckResult ? (preCheckResult.canSubmit ? '允许提交' : '阻断提交') : '尚未检测' }}</h3>
              <p class="muted">{{ preCheckResult?.summary || '点击冲突预检查后，系统会把 HARD / RISK / NO_FLY 等规则映射到航线上。' }}</p>
              <div class="stat-pills" v-if="preCheckResult">
                <span><b>{{ preCheckResult.candidateOccupancyCount }}</b> 候选占用</span>
                <span class="danger"><b>{{ preCheckResult.blockingCount }}</b> 阻断</span>
                <span class="warning"><b>{{ preCheckResult.riskCount }}</b> 风险</span>
              </div>
              <div class="conflict-list slim">
                <button v-for="c in preCheckResult?.conflicts || []" :key="`${c.ruleCode}-${c.gridId}-${c.timeSlotId}`" :class="{ hard: c.blocking }" @click="selectedConflict = c">
                  <b>{{ c.ruleCode }}</b>
                  <span>{{ c.gridCode }} / TS {{ c.timeSlotId }}</span>
                  <small>{{ c.blocking ? '建议：更换航线、时间片或高度层' : '建议：人工复核后继续' }}</small>
                </button>
              </div>
            </div>
          </div>
        </section>

        <section v-if="activeView === 'approval'" class="approval-layout">
          <div class="booking-column">
            <div class="panel-title-line">
              <div><p class="eyebrow">Approval Desk</p><h3>预约申请列表</h3></div>
              <el-select v-model="bookingFilter.status" clearable placeholder="状态" style="width: 130px" @change="loadBookings">
                <el-option label="PENDING" value="PENDING" />
                <el-option label="APPROVED" value="APPROVED" />
                <el-option label="REJECTED" value="REJECTED" />
                <el-option label="CANCELLED" value="CANCELLED" />
              </el-select>
            </div>
            <button v-for="b in bookings" :key="b.id" class="booking-card" :class="b.status?.toLowerCase()" @click="viewBooking(b)">
              <strong>{{ b.bookingNo }}</strong>
              <span>{{ b.taskName }}</span>
              <small>{{ b.bookingDate }} · {{ b.status }}</small>
            </button>
          </div>
          <AirspaceRouteMap
            title="审批对象资源占用预览"
            :grids="grids"
            :route-detail="selectedBookingRoute || selectedRoute"
            :conflicts="selectedBookingConflicts"
            :occupancies="selectedBooking?.occupancies || []"
            :selected-level-id="selectedBooking?.levelId || selectedLevelId"
            :selected-time-slot-id="selectedBooking?.timeSlotIds?.[0] || selectedTimeSlotId"
            :selected-date="selectedBooking?.bookingDate || selectedDate"
            :level-label="selectedBooking ? `Level ${selectedBooking.levelId}` : currentLevelText"
            :time-label="selectedBooking?.timeSlotIds?.join(', ') || currentTimeText"
            compact
          />
          <div class="detail-drawer approval-drawer">
            <p class="eyebrow">Booking Detail</p>
            <h3>{{ selectedBooking?.bookingNo || '请选择申请' }}</h3>
            <p class="muted">{{ selectedBooking?.taskName || '点击左侧申请卡片查看详情和审批操作。' }}</p>
            <div v-if="selectedBooking" class="approval-actions">
              <button class="primary-action inline" :disabled="selectedBooking.status !== 'PENDING'" @click="approveBooking(selectedBooking)">审批通过</button>
              <button class="danger-action" :disabled="selectedBooking.status !== 'PENDING'" @click="rejectBooking(selectedBooking)">驳回</button>
              <button class="warning-action" :disabled="!['PENDING','APPROVED'].includes(selectedBooking.status)" @click="cancelBooking(selectedBooking)">取消 / 释放</button>
            </div>
            <div v-if="selectedBooking" class="timeline-mini">
              <div v-for="f in selectedBooking.flows || []" :key="f.id"><b>{{ f.action }}</b><span>{{ f.fromStatus || 'START' }} → {{ f.toStatus }}</span></div>
            </div>
            <div v-if="selectedBooking" class="mini-json"><b>占用记录</b><pre>{{ pretty(selectedBooking.occupancies || []) }}</pre></div>
          </div>
        </section>

        <section v-if="activeView === 'conflicts'" class="conflict-layout">
          <AirspaceRouteMap
            title="冲突记录映射图"
            :grids="grids"
            :route-detail="selectedRoute"
            :conflicts="conflicts"
            :occupancies="visibleOccupancies"
            :selected-grid-id="selectedConflict?.gridId"
            :selected-level-id="selectedLevelId"
            :selected-time-slot-id="selectedTimeSlotId"
            :selected-date="selectedDate"
            :level-label="currentLevelText"
            :time-label="currentTimeText"
            @select-conflict="selectedConflict = $event"
          />
          <div class="conflict-side">
            <div class="panel-title-line">
              <div><p class="eyebrow">Conflict Center</p><h3>冲突消解记录</h3></div>
              <button class="secondary-action tiny" @click="loadConflicts">刷新</button>
            </div>
            <div class="conflict-list">
              <button v-for="c in conflicts" :key="c.id" :class="{ hard: c.conflictType === 'HARD' }" @click="selectedConflict = c">
                <b>{{ c.ruleCode }}</b>
                <span>{{ c.gridCode }} · {{ c.conflictType }} · {{ c.status }}</span>
                <small>{{ c.message }}</small>
              </button>
            </div>
          </div>
          <div class="detail-drawer">
            <p class="eyebrow">Conflict Detail</p>
            <h3>{{ selectedConflict?.ruleCode || '请选择冲突' }}</h3>
            <p class="muted">{{ selectedConflict?.message || '点击左侧冲突卡片或地图冲突标记查看详情。' }}</p>
            <pre v-if="selectedConflict" class="mini-pre">{{ pretty(selectedConflict) }}</pre>
          </div>
        </section>

        <section v-if="activeView === 'events'" class="view-stack">
          <div class="event-chain-panel">
            <p class="eyebrow">Eventual Consistency</p>
            <h3>审批事件链路：Outbox → RabbitMQ → Notify → Audit → Idempotent</h3>
            <div class="chain-line">
              <div class="chain-node"><b>Booking</b><span>APPROVED / CANCELLED</span></div>
              <div class="chain-arrow">→</div>
              <div class="chain-node"><b>Outbox</b><span>{{ outboxSummary.sent || 0 }} SENT</span></div>
              <div class="chain-arrow">→</div>
              <div class="chain-node"><b>RabbitMQ</b><span>booking.exchange</span></div>
              <div class="chain-arrow">→</div>
              <div class="chain-node"><b>Notify</b><span>{{ notificationSummary.notifySent || 0 }} SENT</span></div>
              <div class="chain-arrow">→</div>
              <div class="chain-node"><b>Audit</b><span>{{ notificationSummary.auditTotal || 0 }} LOGS</span></div>
              <div class="chain-arrow">→</div>
              <div class="chain-node"><b>Idempotent</b><span>{{ notificationSummary.idempotentProcessed || 0 }} PROCESSED</span></div>
            </div>
            <div class="action-row">
              <button class="primary-action inline" @click="dispatchOutbox">手动 Dispatch</button>
              <button class="secondary-action inline" @click="loadEventData">刷新事件链路</button>
            </div>
          </div>
          <div class="table-grid-three">
            <div class="glass-panel"><p class="eyebrow">Outbox</p><h3>本地消息表</h3><div class="record-list"><div v-for="m in outboxMessages" :key="m.id" @click="selectedOutbox = m"><b>{{ m.eventType }}</b><span>{{ m.messageKey }}</span><small>{{ m.status }} · retry {{ m.retryCount }}</small><div class="record-actions"><button class="ghost-mini" @click.stop="republishOutboxMessage(m)">重复投递</button><button class="ghost-mini" @click.stop="requeueOutboxMessage(m)">重新入队</button></div></div></div></div>
            <div class="glass-panel"><p class="eyebrow">Notify</p><h3>通知记录</h3><div class="record-list"><div v-for="n in notifyRecords" :key="n.id"><b>{{ n.subject }}</b><span>{{ n.messageKey }}</span><small>{{ n.status }}</small></div></div></div>
            <div class="glass-panel"><p class="eyebrow">Audit</p><h3>审计日志</h3><div class="record-list"><div v-for="a in auditLogs" :key="a.id"><b>{{ a.action }}</b><span>{{ a.messageKey }}</span><small>{{ a.createdAt }}</small></div></div></div>
          </div>
        </section>

        <section v-if="activeView === 'governance'" class="view-stack">
          <div class="layout-2-1">
            <div class="glass-panel">
              <p class="eyebrow">Governance Center</p>
              <h3>限流、降级、重试演示</h3>
              <div class="governance-actions">
                <button class="warning-action" @click="triggerRateLimit">触发限流</button>
                <button class="danger-action" @click="triggerCircuit">触发降级</button>
                <button class="primary-action inline" @click="triggerRetry">触发重试</button>
                <button class="secondary-action inline" @click="loadGovernance">刷新治理摘要</button>
              </div>
              <pre class="mini-pre governance-output">{{ pretty(governanceOutput || governanceSummary) }}</pre>
            </div>
            <div class="glass-panel">
              <p class="eyebrow">Observability</p>
              <h3>监控入口</h3>
              <div class="external-links">
                <a href="http://127.0.0.1:9090" target="_blank">Prometheus :9090</a>
                <a href="http://127.0.0.1:3000" target="_blank">Grafana :3000</a>
                <a href="http://127.0.0.1:15672" target="_blank">RabbitMQ :15672</a>
                <a href="http://127.0.0.1:8848/nacos/" target="_blank">Nacos :8848</a>
              </div>
              <div class="status-list mt16">
                <div><span>RateLimiter</span><b>{{ governanceSummary.rateLimiterName || 'hotGridPreCheck' }}</b></div>
                <div><span>CircuitBreaker</span><b>{{ governanceSummary.circuitBreakerName || 'notifyProbe' }}</b></div>
                <div><span>Retry</span><b>{{ governanceSummary.retryName || 'remoteProbe' }}</b></div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  </el-config-provider>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import AirspaceRouteMap from './components/AirspaceRouteMap.vue'
import TimeSlotTimeline from './components/TimeSlotTimeline.vue'
import RouteTemplatePanel from './components/RouteTemplatePanel.vue'

const apiBase = import.meta.env.VITE_API_BASE || ''
const token = ref(localStorage.getItem('LOW_ALTITUDE_TOKEN') || '')
const activeView = ref('dashboard')
const health = ref({})
const grids = ref([])
const levels = ref([])
const timeSlots = ref([])
const routeTemplates = ref([])
const routeDetails = ref(new Map())
const selectedRoute = ref(null)
const selectedGrid = ref(null)
const selectedConflict = ref(null)
const bookings = ref([])
const selectedBooking = ref(null)
const conflicts = ref([])
const preCheckResult = ref(null)
const outboxMessages = ref([])
const notifyRecords = ref([])
const auditLogs = ref([])
const idempotentRecords = ref([])
const outboxSummary = ref({})
const notificationSummary = ref({})
const governanceSummary = ref({})
const governanceOutput = ref(null)

const loading = reactive({ all: false, token: false, preCheck: false, submit: false })
const loginForm = reactive({ username: 'demo', role: 'ADMIN' })
const bookingFilter = reactive({ status: '' })

const tomorrow = () => {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return d.toISOString().slice(0, 10)
}
const selectedDate = ref(tomorrow())
const selectedLevelId = ref(2)
const selectedTimeSlotId = ref(21)

const form = reactive({
  taskName: '园区光伏板低空巡检',
  orgId: 1,
  applicantUserId: 1,
  applicantName: 'demo',
  routeTemplateId: 1,
  levelId: 1,
  bookingDate: selectedDate.value,
  timeSlotIds: [1, 2],
  applyReason: '例行低空巡检任务',
  description: '2.5D route console demo'
})

const compareRouteIds = ref([])
const routeDraft = reactive({
  routeCode: `RT-USER-${Date.now().toString().slice(-6)}`,
  routeName: '自定义低空巡检航线',
  description: '在 2.5D 航线编辑器中点击网格生成的演示航线',
  gridIds: [],
  plannedDurationMinutes: 5
})
const routeEditorResult = ref(null)
const selectedOutbox = ref(null)
const selectedScenario = ref('normal')
const selectedMissionId = ref('M-001')
const selectedCockpitConflict = ref(null)
const selectedDemoStep = ref('situation')
const appliedResolutions = ref({})
const ollamaModel = ref('qwen3:8b')
const aiAdvisorLoading = ref(false)
const aiAdvisorResult = ref('')
const aiAdvisorSource = ref('fallback')

const cockpitScenarios = [
  { key: 'normal', label: '正常巡检态势', desc: '多航线执行，风险低' },
  { key: 'conflict', label: '冲突高发态势', desc: '硬冲突与风险集中' },
  { key: 'notify-fault', label: '通知服务故障', desc: 'Outbox 待补偿升高' },
  { key: 'rate-limit', label: '限流压测态势', desc: '热点 Grid 高频提交' },
  { key: 'emergency', label: '应急任务插入', desc: '临时任务抢占空域' }
]

const cockpitDemoSteps = [
  { key: 'situation', title: '1. 态势加载', desc: '展示 Grid、Level、TimeSlot 与今日任务密度' },
  { key: 'conflict', title: '2. 冲突检测', desc: '点击红色冲突点说明硬冲突规则' },
  { key: 'resolution', title: '3. 消解方案', desc: '演示改期、改高度层、人工复核' },
  { key: 'ai', title: '4. AI 研判', desc: '调用本地 Qwen3 8B 生成快速调度口径' },
  { key: 'event-chain', title: '5. 一致性链路', desc: '说明 Outbox、RabbitMQ、通知、审计闭环' }
]

const cockpitScene = {
  roads: [
    { path: [{ row: 1, col: 1 }, { row: 2, col: 3 }, { row: 4, col: 5 }, { row: 7, col: 8 }, { row: 10, col: 10 }], dy: 58 },
    { path: [{ row: 9, col: 1 }, { row: 7, col: 3 }, { row: 5, col: 5 }, { row: 3, col: 7 }, { row: 2, col: 10 }], dy: 48 },
    { path: [{ row: 5, col: 1 }, { row: 5, col: 4 }, { row: 5, col: 7 }, { row: 5, col: 10 }], dy: 52 }
  ],
  buildings: [
    { row: 2, col: 3, label: '仓储区', dx: -18, dy: 54 },
    { row: 4, col: 5, label: '调度中心', dx: 20, dy: 58, width: 116, height: 62 },
    { row: 7, col: 4, label: '能源站', dx: -30, dy: 52 },
    { row: 8, col: 8, label: '光伏阵列', dx: 14, dy: 56, width: 120 },
    { row: 3, col: 8, label: '高压走廊', dx: 20, dy: 50, width: 100 }
  ],
  points: [
    { row: 1, col: 1, label: '起降点 A', type: 'launch' },
    { row: 10, col: 10, label: '起降点 B', type: 'launch' },
    { row: 3, col: 7, label: '电塔 P3', type: 'tower' },
    { row: 6, col: 2, label: '巡检点 S2', type: 'poi' },
    { row: 8, col: 6, label: '巡检点 S6', type: 'poi' },
    { row: 4, col: 9, label: '仓顶 W1', type: 'poi' }
  ]
}

const demoLevelFallback = [
  { id: 1, levelCode: 'L1', levelName: '0-60m 地表巡检层' },
  { id: 2, levelCode: 'L2', levelName: '60-120m 常规巡检层' },
  { id: 3, levelCode: 'L3', levelName: '120-180m 通行避让层' },
  { id: 4, levelCode: 'L4', levelName: '180-240m 应急备用层' }
]

const demoGrids = Array.from({ length: 100 }, (_, index) => {
  const row = Math.floor(index / 10) + 1
  const col = (index % 10) + 1
  const noFly = ['02-02', '04-04', '06-07', '09-03'].includes(`${String(row).padStart(2, '0')}-${String(col).padStart(2, '0')}`)
  const risk = ['01-08', '03-04', '05-05', '07-02', '08-08', '10-06'].includes(`${String(row).padStart(2, '0')}-${String(col).padStart(2, '0')}`)
  return {
    id: row * 100 + col,
    gridCode: `G-${String(row).padStart(2, '0')}-${String(col).padStart(2, '0')}`,
    gridName: `园区 ${row} 行 ${col} 列`,
    rowIndex: row,
    colIndex: col,
    status: noFly ? 'NO_FLY' : risk ? 'RISK' : 'ACTIVE'
  }
})

const demoTimeSlots = Array.from({ length: 48 }, (_, index) => {
  const h = Math.floor(index / 2)
  const m = index % 2 === 0 ? '00' : '30'
  const nextH = Math.floor((index + 1) / 2)
  const nextM = (index + 1) % 2 === 0 ? '00' : '30'
  return {
    id: index + 1,
    slotCode: `TS-${String(index + 1).padStart(2, '0')}`,
    slotName: `${String(h).padStart(2, '0')}:${m}-${String(nextH).padStart(2, '0')}:${nextM}`
  }
})

const missionSeeds = [
  ['M-001', '电力巡检 T-001', '电力巡检', '电力运维一组', 2, 21, 'running', [[1, 1], [2, 3], [3, 5], [4, 6], [5, 7]]],
  ['M-002', '光伏阵列 T-002', '光伏巡检', '新能源运维组', 2, 21, 'conflict', [[6, 4], [7, 5], [8, 6], [8, 8], [9, 9]]],
  ['M-003', '仓储屋顶 T-003', '仓储巡检', '园区安防组', 1, 28, 'pending', [[5, 1], [5, 3], [4, 5], [3, 7]]],
  ['M-004', '河道边界 T-004', '边界巡检', '水务巡检组', 3, 30, 'risk', [[9, 1], [8, 2], [7, 3], [6, 4], [5, 5]]],
  ['M-005', '应急插入 T-005', '应急处置', '应急指挥组', 2, 21, 'conflict', [[2, 5], [3, 5], [4, 6], [5, 7], [6, 8]]],
  ['M-006', '南区巡逻 T-006', '安防巡逻', '园区安防组', 1, 36, 'running', [[10, 2], [9, 3], [8, 4], [7, 5], [6, 6]]],
  ['M-007', '高压走廊 T-007', '电力巡检', '电力运维二组', 3, 24, 'risk', [[1, 8], [2, 8], [3, 8], [4, 9]]],
  ['M-008', '仓库补盲 T-008', '仓储巡检', '仓储管理组', 1, 18, 'completed', [[3, 2], [3, 3], [3, 4], [4, 5]]]
]

const extraMissionTypes = ['管线巡检', '光伏巡检', '安防巡逻', '屋顶巡检', '道路巡检']

const cockpitMissionBase = [
  ...missionSeeds.map(toMission),
  ...Array.from({ length: 22 }, (_, index) => {
    const id = index + 9
    const startRow = (index % 8) + 1
    const startCol = ((index * 3) % 8) + 1
    const levelId = (index % 4) + 1
    const slotId = 12 + (index * 2) % 30
    const status = index % 7 === 0 ? 'risk' : index % 5 === 0 ? 'pending' : index % 4 === 0 ? 'completed' : 'running'
    const path = Array.from({ length: 4 }, (_, step) => [
      Math.min(10, startRow + step),
      Math.min(10, startCol + Math.floor(step / 2) + (index % 2))
    ])
    return toMission([
      `M-${String(id).padStart(3, '0')}`,
      `${extraMissionTypes[index % extraMissionTypes.length]} T-${String(id).padStart(3, '0')}`,
      extraMissionTypes[index % extraMissionTypes.length],
      ['电力运维组', '园区安防组', '新能源运维组', '水务巡检组'][index % 4],
      levelId,
      slotId,
      status,
      path
    ])
  })
]

function toMission(seed) {
  const [id, name, type, org, levelId, timeSlotId, status, path] = seed
  const level = demoLevelFallback.find(l => Number(l.id) === Number(levelId)) || demoLevelFallback[0]
  const slot = demoTimeSlots.find(s => Number(s.id) === Number(timeSlotId)) || demoTimeSlots[0]
  const mission = {
    id,
    code: id.replace('M-', 'T-20260603-'),
    name,
    type,
    org,
    levelId,
    levelName: `${level.levelCode} ${level.levelName}`,
    timeSlotId,
    timeText: slot.slotName,
    status,
    meta: `${level.levelCode} / ${slot.slotName}`,
    showDrone: status !== 'completed',
    grids: path.map(([row, col], index) => {
      const grid = demoGrids.find(g => Number(g.rowIndex) === Number(row) && Number(g.colIndex) === Number(col))
      return {
        gridId: grid.id,
        gridCode: grid.gridCode,
        gridName: grid.gridName,
        gridStatus: grid.status,
        rowIndex: row,
        colIndex: col,
        sequenceNo: index + 1,
        plannedDurationMinutes: 5
      }
    })
  }
  mission.routeCode = mission.code
  mission.routeName = mission.name
  return mission
}

function scenarioMissions(key) {
  return cockpitMissionBase.map(mission => {
    const copy = { ...mission, grids: mission.grids.map(g => ({ ...g })) }
    if (key === 'conflict' && ['M-003', 'M-004', 'M-009', 'M-014'].includes(copy.id)) copy.status = 'conflict'
    if (key === 'notify-fault' && copy.id === 'M-001') copy.status = 'running'
    if (key === 'rate-limit' && copy.id.startsWith('M-01')) copy.status = 'pending'
    if (key === 'emergency' && ['M-005', 'M-011', 'M-017'].includes(copy.id)) copy.status = 'conflict'
    const resolution = appliedResolutions.value[copy.id]
    if (resolution) {
      copy.status = resolution.status || copy.status
      copy.timeText = resolution.timeText || copy.timeText
      if (resolution.levelId) {
        const level = demoLevelFallback.find(l => Number(l.id) === Number(resolution.levelId)) || demoLevelFallback[0]
        copy.levelId = level.id
        copy.levelName = `${level.levelCode} ${level.levelName}`
      }
      copy.meta = `${copy.levelName?.split(' ')[0] || `L${copy.levelId}`} / ${copy.timeText}`
    }
    return copy
  })
}

function scenarioConflicts(key, missions) {
  const base = [
    makeConflict(missions.find(m => m.id === 'M-002'), 'HARD', 'SAME_GRID_LEVEL_TIME', '与 T-20260603-001 在 G-05-07 / L2 / 10:00-10:30 发生硬冲突', '10:45-11:15', 'L3', 'G-06-08 -> G-07-09'),
    makeConflict(missions.find(m => m.id === 'M-004'), 'RISK', 'ADJACENT_GRID_OCCUPIED', '相邻 Grid 同高度层存在运行任务，建议人工复核', '15:30-16:00', 'L4', 'G-07-04 -> G-08-05'),
    makeConflict(missions.find(m => m.id === 'M-007'), 'RISK', 'RISK_GRID', '航线经过高压走廊风险区，建议降低速度并复核天气', '12:30-13:00', 'L4', 'G-04-08 -> G-04-09')
  ].filter(Boolean)
  if (key === 'conflict') {
    base.push(
      makeConflict(missions.find(m => m.id === 'M-003'), 'HARD', 'NO_FLY_GRID', '候选航线穿越禁飞网格 G-04-04，系统阻断审批', '14:30-15:00', 'L2', 'G-03-04 -> G-03-05'),
      makeConflict(missions.find(m => m.id === 'M-014'), 'HARD', 'SAME_GRID_LEVEL_TIME', '热点仓储区出现同层同时段重复占用', '16:00-16:30', 'L3', 'G-05-06 -> G-06-06')
    )
  }
  if (key === 'emergency') {
    base.push(makeConflict(missions.find(m => m.id === 'M-005'), 'HARD', 'EMERGENCY_INSERT', '应急任务插入占用原计划航线，需要调整普通巡检任务', '立即执行应急窗口', 'L4', 'G-02-06 -> G-03-06'))
  }
  return base.filter(Boolean).filter(conflict => !appliedResolutions.value[conflict.bookingId]?.resolved)
}

function makeConflict(mission, type, ruleCode, message, nextSlot, altLevel, reroute) {
  if (!mission?.grids?.length) return null
  const point = mission.grids[Math.min(2, mission.grids.length - 1)]
  return {
    id: `${mission.id}-${ruleCode}`,
    bookingId: mission.id,
    bookingNo: mission.code,
    conflictType: type,
    conflictLevel: type === 'HARD' ? 'BLOCKING' : 'RISK',
    ruleCode,
    gridId: point.gridId,
    gridCode: point.gridCode,
    gridName: point.gridName,
    levelId: mission.levelId,
    timeSlotId: mission.timeSlotId,
    bookingDate: selectedDate.value,
    message,
    status: 'ACTIVE',
    blocking: type === 'HARD',
    nextSlot,
    altLevel,
    reroute
  }
}

function missionOccupancies(missions, includeAllSlots = false) {
  return missions.flatMap(mission => mission.grids.map(grid => ({
    bookingId: mission.id,
    bookingNo: mission.code,
    gridId: grid.gridId,
    gridCode: grid.gridCode,
    gridName: grid.gridName,
    levelId: mission.levelId,
    timeSlotId: includeAllSlots ? mission.timeSlotId : mission.timeSlotId,
    bookingDate: selectedDate.value,
    status: mission.status === 'running' ? 'RUNNING' : mission.status === 'conflict' ? 'CONFLICT' : mission.status === 'completed' ? 'RELEASED' : 'OCCUPIED'
  })))
}

const cockpitLevels = computed(() => levels.value.length >= 4 ? levels.value : demoLevelFallback)
const cockpitTimeSlots = computed(() => timeSlots.value.length >= 24 ? timeSlots.value : demoTimeSlots)
const cockpitGrids = computed(() => grids.value.length >= 80 ? grids.value : demoGrids)
const cockpitMissionRoutes = computed(() => scenarioMissions(selectedScenario.value))
const cockpitAllConflicts = computed(() => scenarioConflicts(selectedScenario.value, cockpitMissionRoutes.value))
const cockpitAllOccupancies = computed(() => missionOccupancies(cockpitMissionRoutes.value, true))
const cockpitConflicts = computed(() => cockpitAllConflicts.value.filter(c => String(c.levelId) === String(selectedLevelId.value) && String(c.timeSlotId) === String(selectedTimeSlotId.value)))
const cockpitOccupancies = computed(() => cockpitAllOccupancies.value.filter(o => String(o.levelId) === String(selectedLevelId.value) && String(o.timeSlotId) === String(selectedTimeSlotId.value)))
const selectedCockpitMission = computed(() => cockpitMissionRoutes.value.find(m => String(m.id) === String(selectedMissionId.value)) || cockpitMissionRoutes.value[0])
const cockpitStats = computed(() => {
  const missions = cockpitMissionRoutes.value
  return {
    total: missions.length,
    running: missions.filter(m => m.status === 'running').length,
    pending: missions.filter(m => m.status === 'pending').length,
    hardConflicts: cockpitAllConflicts.value.filter(c => c.conflictType === 'HARD').length,
    riskConflicts: cockpitAllConflicts.value.filter(c => c.conflictType !== 'HARD').length,
    outboxPending: selectedScenario.value === 'notify-fault' ? 7 : Number(outboxSummary.value.pending ?? 2)
  }
})
const selectedMissionResolution = computed(() => appliedResolutions.value[selectedCockpitMission.value?.id])
const cockpitScenarioBrief = computed(() => {
  const scenario = cockpitScenarios.find(s => s.key === selectedScenario.value)
  const step = cockpitDemoSteps.find(s => s.key === selectedDemoStep.value)
  return `${scenario?.label || '当前态势'}：${scenario?.desc || ''}。当前演示节点：${step?.title || '态势加载'}，${step?.desc || '展示低空资源占用闭环'}。`
})
const cockpitAiPreview = computed(() => fallbackAiAdvice())
const cockpitChain = computed(() => [
  { key: 'booking', label: '审批服务', value: `${cockpitStats.value.pending} pending`, state: 'ok' },
  { key: 'occupancy', label: '资源占用', value: `${cockpitOccupancies.value.length} slices`, state: cockpitConflicts.value.length ? 'warn' : 'ok' },
  { key: 'outbox', label: 'Outbox', value: `${cockpitStats.value.outboxPending} pending`, state: cockpitStats.value.outboxPending > 3 ? 'warn' : 'ok' },
  { key: 'mq', label: 'RabbitMQ', value: selectedScenario.value === 'notify-fault' ? 'retrying' : 'normal', state: selectedScenario.value === 'notify-fault' ? 'warn' : 'ok' },
  { key: 'notify', label: '通知服务', value: selectedScenario.value === 'notify-fault' ? 'degraded' : 'sent', state: selectedScenario.value === 'notify-fault' ? 'bad' : 'ok' },
  { key: 'audit', label: '审计日志', value: `${notificationSummary.value.auditTotal || 18} logs`, state: 'ok' }
])

const navItems = [
  { key: 'dashboard', label: '态势总览', icon: '◎' },
  { key: 'route-situation', label: '低空航线态势', icon: '◇' },
  { key: 'route-editor', label: '航线编辑器', icon: '✎' },
  { key: 'route-compare', label: '多航线对比', icon: '⇄' },
  { key: 'planner', label: '预约 / Pre-check', icon: '✦' },
  { key: 'approval', label: '审批工作台', icon: '✓' },
  { key: 'conflicts', label: '冲突记录', icon: '!' },
  { key: 'events', label: '消息一致性', icon: '↻' },
  { key: 'governance', label: '治理监控', icon: '≋' }
]
const currentTitle = computed(() => navItems.find(n => n.key === activeView.value)?.label || '低空航线态势控制台')
const healthOk = computed(() => health.value?.status === 'UP')
const serviceList = computed(() => {
  const details = health.value?.components?.discoveryComposite?.components?.discoveryClient?.details?.services
  if (Array.isArray(details)) return details
  if (typeof details === 'string') return details.split(/\s+/).filter(Boolean)
  return []
})

const http = axios.create({ baseURL: apiBase })
http.interceptors.request.use(config => {
  if (token.value) config.headers.Authorization = `Bearer ${token.value}`
  return config
})
http.interceptors.response.use(
  response => {
    const body = response.data
    if (body && Object.prototype.hasOwnProperty.call(body, 'success')) {
      if (!body.success) throw new Error(body.message || body.code || 'API failed')
      return body.data
    }
    return body
  },
  error => Promise.reject(new Error(error.response?.data?.message || error.response?.data?.code || error.message || 'Request failed'))
)

async function safe(name, fn, fallback = null, silent = false) {
  try { return await fn() } catch (err) { if (!silent) ElMessage.error(`${name}失败：${err.message}`); return fallback }
}
async function getHealth() { health.value = await safe('健康检查', () => axios.get(`${apiBase}/actuator/health`).then(r => r.data), { status: 'DOWN' }, true) }
async function getDevToken() {
  loading.token = true
  try {
    const resp = await axios.post(`${apiBase}/api/auth/dev-token`, null, { params: loginForm }).then(r => r.data)
    if (!resp?.success || !resp?.data?.accessToken) throw new Error(resp?.message || 'Token response invalid')
    token.value = resp.data.accessToken
    localStorage.setItem('LOW_ALTITUDE_TOKEN', token.value)
    ElMessage.success('ADMIN Token 已就绪')
  } catch (err) { ElMessage.error(`获取 Token 失败：${err.message}`) } finally { loading.token = false }
}

async function loadResources() {
  const [gridData, levelData, slotData, routeData] = await Promise.all([
    safe('加载 Grid', () => http.get('/api/resources/grids'), []),
    safe('加载 Level', () => http.get('/api/resources/levels'), []),
    safe('加载 TimeSlot', () => http.get('/api/resources/time-slots'), []),
    safe('加载 RouteTemplate', () => http.get('/api/resources/route-templates'), [])
  ])
  grids.value = gridData || []
  levels.value = levelData || []
  timeSlots.value = slotData || []
  routeTemplates.value = routeData || []
  if (!selectedLevelId.value && levels.value[0]) selectedLevelId.value = levels.value[0].id
  if (!selectedTimeSlotId.value && timeSlots.value[0]) selectedTimeSlotId.value = timeSlots.value[0].id
  if (!form.routeTemplateId && routeTemplates.value[0]) form.routeTemplateId = routeTemplates.value[0].id
  if (!compareRouteIds.value.length && routeTemplates.value.length) compareRouteIds.value = routeTemplates.value.slice(0, Math.min(3, routeTemplates.value.length)).map(r => r.id)
  await preloadRouteDetails()
  await loadRouteDetail()
}

async function preloadRouteDetails() {
  const details = await Promise.all((routeTemplates.value || []).map(r => safe('加载航线详情', () => http.get(`/api/resources/route-templates/${r.id}`), r, true)))
  const map = new Map()
  details.filter(Boolean).forEach(d => map.set(Number(d.id), d))
  routeDetails.value = map
}

async function loadRouteDetail() {
  if (!form.routeTemplateId) return
  selectedRoute.value = routeDetails.value.get(Number(form.routeTemplateId)) || await safe('加载航线详情', () => http.get(`/api/resources/route-templates/${form.routeTemplateId}`), null)
  if (selectedRoute.value) routeDetails.value.set(Number(selectedRoute.value.id), selectedRoute.value)
}

async function selectRouteTemplate(route) {
  form.routeTemplateId = route.id
  await loadRouteDetail()
}

async function loadBookings() {
  const params = {}
  if (bookingFilter.status) params.status = bookingFilter.status
  bookings.value = await safe('加载预约', () => http.get('/api/bookings', { params }), [])
}
async function loadConflicts() { conflicts.value = await safe('加载冲突', () => http.get('/api/bookings/conflicts', { params: { limit: 80 } }), []) }
async function loadEventData() {
  const [outbox, outboxS, notifyS, notify, audits, idem] = await Promise.all([
    safe('加载 outbox', () => http.get('/api/bookings/outbox', { params: { limit: 20 } }), [], true),
    safe('加载 outbox summary', () => http.get('/api/bookings/outbox/summary'), {}, true),
    safe('加载 notification summary', () => http.get('/api/notifications/summary'), {}, true),
    safe('加载 notify', () => http.get('/api/notifications/records', { params: { limit: 20 } }), [], true),
    safe('加载 audit', () => http.get('/api/notifications/audits', { params: { limit: 20 } }), [], true),
    safe('加载 idempotent', () => http.get('/api/notifications/idempotent', { params: { limit: 20 } }), [], true)
  ])
  outboxMessages.value = outbox || []
  outboxSummary.value = outboxS || {}
  notificationSummary.value = notifyS || {}
  notifyRecords.value = notify || []
  auditLogs.value = audits || []
  idempotentRecords.value = idem || []
}
async function loadGovernance() { governanceSummary.value = await safe('加载治理摘要', () => http.get('/api/bookings/governance/summary'), {}, true) }

async function loadAll() {
  loading.all = true
  try {
    await getHealth()
    if (!token.value) await getDevToken()
    await loadResources()
    await loadBookings()
    await loadConflicts()
    await loadEventData()
    await loadGovernance()
    ElMessage.success('态势数据已刷新')
  } finally { loading.all = false }
}

async function preCheck() {
  loading.preCheck = true
  try {
    form.bookingDate = selectedDate.value || form.bookingDate
    form.levelId = selectedLevelId.value || form.levelId
    preCheckResult.value = await http.post('/api/bookings/pre-check', form)
    selectedConflict.value = preCheckResult.value?.conflicts?.[0] || null
    ElMessage.success(preCheckResult.value.canSubmit ? '预检查通过：可提交' : '预检查阻断：存在硬冲突')
  } catch (err) { ElMessage.error(`预检查失败：${err.message}`) } finally { loading.preCheck = false }
}
async function submitBooking() {
  loading.submit = true
  try {
    const result = await http.post('/api/bookings', form)
    selectedBooking.value = result
    await loadBookings()
    await loadConflicts()
    ElMessage.success(`预约已提交：${result.bookingNo}`)
    activeView.value = 'approval'
  } catch (err) { ElMessage.error(`提交失败：${err.message}`) } finally { loading.submit = false }
}
async function viewBooking(row) {
  selectedBooking.value = await safe('加载申请详情', () => http.get(`/api/bookings/${row.id}`), row)
  const routeId = selectedBooking.value?.routeTemplateId
  if (routeId) selectedRoute.value = routeDetails.value.get(Number(routeId)) || selectedRoute.value
}
async function approveBooking(row) {
  await ElMessageBox.confirm(`确认审批通过 ${row.bookingNo}？系统会执行二次冲突检测。`, '审批通过', { type: 'warning' })
  const updated = await http.post(`/api/bookings/${row.id}/approve`, { operatorUserId: 1, operatorName: 'admin', comment: 'approved from 2.5D route console' })
  selectedBooking.value = updated
  await loadBookings(); await loadConflicts(); await loadEventData(); await loadGovernance()
  ElMessage.success('审批通过，资源占用与 Outbox 事件已生成')
}
async function rejectBooking(row) {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回申请', { inputValue: '资源安排不符合当前计划' })
  selectedBooking.value = await http.post(`/api/bookings/${row.id}/reject`, { operatorUserId: 1, operatorName: 'admin', rejectReason: value })
  await loadBookings(); ElMessage.success('已驳回')
}
async function cancelBooking(row) {
  await ElMessageBox.confirm(`确认取消 ${row.bookingNo}？已占用资源会释放。`, '取消申请', { type: 'warning' })
  selectedBooking.value = await http.post(`/api/bookings/${row.id}/cancel`, { operatorUserId: 1, operatorName: 'admin', cancelReason: 'cancelled from 2.5D route console' })
  await loadBookings(); await loadConflicts(); await loadEventData(); await loadGovernance()
  ElMessage.success('已取消并释放资源')
}
async function dispatchOutbox() { await safe('Outbox dispatch', () => http.post('/api/bookings/outbox/dispatch', null, { params: { limit: 20 } })); await loadEventData(); ElMessage.success('Outbox dispatch 已触发') }
async function triggerRateLimit() {
  const results = []
  for (let i = 0; i < 10; i++) results.push(await safe('限流演示', () => http.get('/api/bookings/governance/rate-limited', { params: { label: `web-${Date.now()}-${i}` } }), { code: 'RATE_LIMITED' }, true))
  governanceOutput.value = { type: 'RateLimiter', results }
  await loadGovernance()
}
async function triggerCircuit() { governanceOutput.value = await safe('降级演示', () => http.get('/api/bookings/governance/circuit', { params: { fail: true } }), {}) }
async function triggerRetry() { governanceOutput.value = await safe('重试演示', () => http.get('/api/bookings/governance/retry', { params: { key: crypto.randomUUID?.() || `${Date.now()}`, failTimes: 2 } }), {}) }

function gridById(id) { return grids.value.find(g => Number(g.id) === Number(id)) }
function addDraftGrid(grid) {
  selectedGrid.value = grid
  if (!grid?.id) return
  if (!routeDraft.gridIds.includes(grid.id)) routeDraft.gridIds.push(grid.id)
  else ElMessage.info('该 Grid 已在草稿航线中')
}
function removeDraftPoint(index) { routeDraft.gridIds.splice(index, 1) }
function clearDraftRoute() {
  routeDraft.gridIds.splice(0, routeDraft.gridIds.length)
  routeDraft.routeCode = `RT-USER-${Date.now().toString().slice(-6)}`
  routeDraft.routeName = '自定义低空巡检航线'
  routeEditorResult.value = null
}
async function saveDraftRoute() {
  if (routeDraft.gridIds.length < 2) { ElMessage.warning('至少选择两个 Grid 才能保存航线'); return }
  const created = await http.post('/api/resources/route-templates', {
    routeCode: routeDraft.routeCode,
    routeName: routeDraft.routeName,
    description: routeDraft.description,
    enabled: true,
    createdBy: 'web-route-editor'
  })
  let detail = created
  for (let i = 0; i < routeDraft.gridIds.length; i++) {
    detail = await http.post(`/api/resources/route-templates/${created.id}/grids`, {
      gridId: routeDraft.gridIds[i],
      sequenceNo: i + 1,
      plannedDurationMinutes: routeDraft.plannedDurationMinutes || 5
    })
  }
  routeEditorResult.value = detail
  await loadResources()
  form.routeTemplateId = detail.id
  await loadRouteDetail()
  ElMessage.success(`航线已保存：${detail.routeCode}`)
}
function useDraftForPrecheck() {
  if (!routeEditorResult.value) return
  form.routeTemplateId = routeEditorResult.value.id
  selectedRoute.value = routeEditorResult.value
  activeView.value = 'planner'
}
async function republishOutboxMessage(message) {
  if (!message?.id) return
  await safe('重复投递', () => http.post(`/api/bookings/outbox/${message.id}/republish`))
  selectedOutbox.value = message
  await loadEventData()
  ElMessage.success('已重复投递，用于幂等演示')
}
async function requeueOutboxMessage(message) {
  if (!message?.id) return
  await safe('重新入队', () => http.post(`/api/bookings/outbox/${message.id}/requeue`))
  selectedOutbox.value = message
  await loadEventData()
  ElMessage.success('已重新入队')
}

function syncTimeSlotFromForm() { if (form.timeSlotIds?.length) selectedTimeSlotId.value = form.timeSlotIds[0] }
function applyScenario(key) {
  selectedScenario.value = key
  selectedCockpitConflict.value = null
  aiAdvisorResult.value = ''
  selectedDemoStep.value = key === 'notify-fault' ? 'event-chain' : key === 'conflict' ? 'conflict' : key === 'emergency' ? 'resolution' : 'situation'
  const preferred = key === 'emergency' ? 'M-005' : key === 'conflict' ? 'M-002' : 'M-001'
  selectedMissionId.value = preferred
  const mission = cockpitMissionRoutes.value.find(m => m.id === preferred) || cockpitMissionRoutes.value[0]
  if (mission) {
    selectedLevelId.value = mission.levelId
    selectedTimeSlotId.value = mission.timeSlotId
  }
}
function selectCockpitMission(route) {
  if (!route?.id) return
  selectedMissionId.value = route.id
  selectedCockpitConflict.value = cockpitAllConflicts.value.find(c => String(c.bookingId) === String(route.id)) || null
  aiAdvisorResult.value = ''
  selectedLevelId.value = route.levelId || selectedLevelId.value
  selectedTimeSlotId.value = route.timeSlotId || selectedTimeSlotId.value
}
function selectCockpitConflict(conflict) {
  selectedConflict.value = conflict
  selectedCockpitConflict.value = conflict
  if (conflict?.bookingId) selectedMissionId.value = conflict.bookingId
  selectedDemoStep.value = 'conflict'
  aiAdvisorResult.value = ''
}
function levelTaskCount(levelId) {
  return cockpitMissionRoutes.value.filter(m => String(m.levelId) === String(levelId)).length
}
function missionStatusText(status) {
  return {
    running: '执行中',
    risk: '风险复核',
    conflict: '冲突待处理',
    completed: '已完成',
    pending: '待审批'
  }[status] || status || '未知'
}
function levelIdFromLabel(label) {
  const match = String(label || '').match(/L(\d+)/i)
  return match ? Number(match[1]) : null
}
function adoptResolution(type) {
  const conflict = selectedCockpitConflict.value
  const mission = selectedCockpitMission.value
  if (!conflict || !mission) return
  const nextLevelId = type === 'level' ? levelIdFromLabel(conflict.altLevel) : null
  const patch = {
    resolved: type !== 'review',
    status: type === 'review' ? 'pending' : type === 'delay' ? 'pending' : 'risk',
    label: type === 'delay' ? '已采用方案 A：改期避让' : type === 'level' ? '已采用方案 B：高度层避让' : '已提交人工复核',
    detail: type === 'delay'
      ? `任务调整到 ${conflict.nextSlot || '下一个空闲 TimeSlot'}，等待审批服务重新占用资源。`
      : type === 'level'
        ? `任务切换到 ${conflict.altLevel || '相邻安全高度层'}，保留原时间片重新做冲突检测。`
        : `任务保持阻断状态，交由审批员结合天气、组织优先级和现场风险复核。`,
    timeText: type === 'delay' ? (conflict.nextSlot || mission.timeText) : mission.timeText,
    levelId: nextLevelId || mission.levelId
  }
  appliedResolutions.value = { ...appliedResolutions.value, [mission.id]: patch }
  if (type !== 'review') selectedCockpitConflict.value = null
  selectedDemoStep.value = type === 'review' ? 'resolution' : 'event-chain'
  aiAdvisorResult.value = fallbackAiAdvice()
  ElMessage.success(patch.label)
}
function buildAiPrompt() {
  const mission = selectedCockpitMission.value
  const conflict = selectedCockpitConflict.value
  return [
    '你是低空时空资源调度平台的快速研判助手，模型定位为本地 Ollama 千问3 8B 快速思考。',
    '请用简洁中文输出：风险判断、推荐方案、审批口径、消息一致性关注点。',
    `场景：${cockpitScenarioBrief.value}`,
    `任务：${mission?.code || '未选择'}，${mission?.name || ''}，${mission?.levelName || ''}，${mission?.timeText || ''}`,
    `占用网格：${mission?.grids?.map(g => g.gridCode).join(', ') || '无'}`,
    `冲突：${conflict ? `${conflict.conflictType}/${conflict.ruleCode}/${conflict.message}` : '当前无显式冲突'}`,
    `统计：任务 ${cockpitStats.value.total}，执行中 ${cockpitStats.value.running}，待审批 ${cockpitStats.value.pending}，硬冲突 ${cockpitStats.value.hardConflicts}，风险 ${cockpitStats.value.riskConflicts}，Outbox 待补偿 ${cockpitStats.value.outboxPending}`
  ].join('\n')
}
function fallbackAiAdvice() {
  const mission = selectedCockpitMission.value
  const conflict = selectedCockpitConflict.value
  if (!mission) return '请选择任务后生成研判。'
  const riskLine = conflict
    ? `${conflict.conflictType === 'HARD' ? '硬冲突阻断' : '风险复核'}：${conflict.message}`
    : `${mission.status === 'running' ? '当前任务可继续执行' : '当前任务需结合审批状态复核'}。`
  const actionLine = conflict
    ? `建议优先采用 ${conflict.conflictType === 'HARD' ? '改期或切换高度层' : '人工复核后放行'}；备选绕行路径为 ${conflict.reroute || '风险网格外侧航线'}。`
    : `建议保持 ${mission.levelName} / ${mission.timeText}，持续观察相邻 Grid 占用和通知链路。`
  return [
    `风险判断：${riskLine}`,
    `推荐方案：${actionLine}`,
    `审批口径：说明任务 ${mission.code} 已映射为 Grid + Level + TimeSlot 占用单元，审批通过前必须完成冲突检测。`,
    `一致性关注：Outbox 当前 ${cockpitStats.value.outboxPending} 条待补偿，审批后需确认 RabbitMQ、通知服务、审计日志最终一致。`
  ].join('\n')
}
async function runAiQuickThink() {
  aiAdvisorLoading.value = true
  aiAdvisorSource.value = 'fallback'
  const prompt = buildAiPrompt()
  try {
    const data = await http.post('/api/bookings/ai/quick-think', {
      model: ollamaModel.value,
      prompt,
      think: false
    })
    aiAdvisorResult.value = data.response || fallbackAiAdvice()
    aiAdvisorSource.value = data.response ? 'ollama' : 'fallback'
  } catch (backendError) {
    try {
      const response = await fetch('http://127.0.0.1:11434/api/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model: ollamaModel.value,
          prompt,
          stream: false,
          think: false,
          options: { temperature: 0.2, num_predict: 360 }
        })
      })
      if (!response.ok) throw new Error(`Ollama HTTP ${response.status}`)
      const data = await response.json()
      aiAdvisorResult.value = data.response || fallbackAiAdvice()
      aiAdvisorSource.value = data.response ? 'ollama' : 'fallback'
    } catch (browserError) {
      aiAdvisorResult.value = `${fallbackAiAdvice()}\n\n本地 Ollama 未返回，已使用前端规则研判。请确认 booking-service 已启动，ollama serve 已启动，且模型名为 ${ollamaModel.value}。`
      aiAdvisorSource.value = 'fallback'
    }
  } finally {
    aiAdvisorLoading.value = false
  }
}

const routeTemplatesWithDetails = computed(() => routeTemplates.value.map(r => routeDetails.value.get(Number(r.id)) || r))
const compareRouteDetails = computed(() => compareRouteIds.value.map(id => routeDetails.value.get(Number(id))).filter(Boolean))
const draftRiskCount = computed(() => routeDraft.gridIds.filter(id => gridById(id)?.status === 'RISK').length)
const draftNoFlyCount = computed(() => routeDraft.gridIds.filter(id => gridById(id)?.status === 'NO_FLY').length)
const routeCompareStats = computed(() => compareRouteDetails.value.map(route => {
  const gridsInRoute = [...(route.grids || [])].sort((a, b) => Number(a.sequenceNo || 0) - Number(b.sequenceNo || 0))
  return {
    id: route.id,
    code: route.routeCode,
    name: route.routeName,
    gridCount: gridsInRoute.length,
    riskCount: gridsInRoute.filter(g => g.gridStatus === 'RISK').length,
    noFlyCount: gridsInRoute.filter(g => g.gridStatus === 'NO_FLY').length,
    path: gridsInRoute.map(g => g.gridCode).join(' → ')
  }
}))
const sortedRouteGrids = computed(() => [...(selectedRoute.value?.grids || [])].sort((a, b) => Number(a.sequenceNo || 0) - Number(b.sequenceNo || 0)))
const activeConflictCount = computed(() => conflicts.value.filter(c => c.status !== 'RESOLVED').length)
const visibleOccupancies = computed(() => {
  const list = []
  for (const b of bookings.value || []) if (Array.isArray(b.occupancies)) list.push(...b.occupancies)
  if (selectedBooking.value?.occupancies) list.push(...selectedBooking.value.occupancies)
  return list
})
const occupiedCount = computed(() => visibleOccupancies.value.filter(o => o.status === 'OCCUPIED').length || governanceSummary.value.occupancyOccupied || 0)
const visualConflicts = computed(() => preCheckResult.value?.conflicts?.length ? preCheckResult.value.conflicts : conflicts.value)
const selectedBookingConflicts = computed(() => conflicts.value.filter(c => Number(c.bookingId) === Number(selectedBooking.value?.id)))
const selectedBookingRoute = computed(() => selectedBooking.value?.routeTemplateId ? routeDetails.value.get(Number(selectedBooking.value.routeTemplateId)) : null)
const currentLevelText = computed(() => levelLabel(cockpitLevels.value.find(l => String(l.id) === String(selectedLevelId.value)) || { id: selectedLevelId.value }))
const currentTimeText = computed(() => timeSlotLabel(cockpitTimeSlots.value.find(t => String(t.id) === String(selectedTimeSlotId.value)) || { id: selectedTimeSlotId.value }))
const formLevelText = computed(() => levelLabel(levels.value.find(l => String(l.id) === String(form.levelId)) || { id: form.levelId }))
const formTimeText = computed(() => (form.timeSlotIds || []).map(id => timeSlotLabel(timeSlots.value.find(t => String(t.id) === String(id)) || { id })).join(', '))
const recentEvents = computed(() => [
  { key: 'health', title: '服务发现', detail: `${serviceList.value.length} services registered in Nacos` },
  { key: 'outbox', title: 'Outbox', detail: `${outboxSummary.value.sent || 0} sent / ${outboxSummary.value.pending || 0} pending` },
  { key: 'notify', title: 'Notify', detail: `${notificationSummary.value.notifySent || 0} sent notifications` },
  { key: 'conflict', title: 'Conflict', detail: `${activeConflictCount.value} active conflicts` }
])

function routeLabel(route) { return `${route.routeCode || route.id} / ${route.routeName || ''}` }
function levelLabel(level) { return `${level.levelCode || level.id} / ${level.levelName || ''}` }
function timeSlotLabel(slot) { return `${slot.slotCode || slot.id} / ${slot.slotName || slot.startTime || ''}` }
function pretty(obj) { return JSON.stringify(obj || {}, null, 2) }

onMounted(async () => { await loadAll() })
</script>
