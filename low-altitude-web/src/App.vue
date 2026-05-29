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

        <section v-if="activeView === 'dashboard'" class="view-stack">
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
const selectedLevelId = ref(1)
const selectedTimeSlotId = ref(1)

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
const currentLevelText = computed(() => levelLabel(levels.value.find(l => String(l.id) === String(selectedLevelId.value)) || { id: selectedLevelId.value }))
const currentTimeText = computed(() => timeSlotLabel(timeSlots.value.find(t => String(t.id) === String(selectedTimeSlotId.value)) || { id: selectedTimeSlotId.value }))
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
