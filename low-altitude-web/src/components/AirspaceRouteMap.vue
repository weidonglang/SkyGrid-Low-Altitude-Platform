<template>
  <section class="airspace-map-card">
    <div class="map-toolbar" v-if="showToolbar">
      <div>
        <p class="eyebrow">2.5D Airspace Situation Map</p>
        <h3>{{ title }}</h3>
      </div>
      <div class="map-badges">
        <span class="mini-badge cyan">{{ currentLevelLabel }}</span>
        <span class="mini-badge violet">{{ currentTimeLabel }}</span>
        <span class="mini-badge slate">{{ selectedDate || 'No date selected' }}</span>
        <span v-if="missionRoutes.length" class="mini-badge green">Mission x {{ missionRoutes.length }}</span>
        <span v-if="compareRoutes.length" class="mini-badge orange">Compare x {{ compareRoutes.length }}</span>
        <span v-if="editorPoints.length" class="mini-badge orange">Draft x {{ editorPoints.length }}</span>
      </div>
    </div>

    <div class="airspace-map-wrap" :class="{ compact, editing: editorMode }">
      <svg class="airspace-svg" :viewBox="viewBox" preserveAspectRatio="xMidYMid meet" role="img">
        <defs>
          <pattern id="noFlyHatch" patternUnits="userSpaceOnUse" width="10" height="10" patternTransform="rotate(35)">
            <rect width="10" height="10" fill="rgba(31,41,55,.45)" />
            <path d="M0 0 L0 10" stroke="rgba(248,113,113,.82)" stroke-width="3" />
          </pattern>
          <filter id="routeGlow" x="-30%" y="-30%" width="160%" height="160%">
            <feGaussianBlur stdDeviation="3" result="blur" />
            <feMerge>
              <feMergeNode in="blur" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
          <filter id="tileGlow" x="-40%" y="-40%" width="180%" height="180%">
            <feGaussianBlur stdDeviation="2.2" result="blur" />
            <feMerge>
              <feMergeNode in="blur" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
          <marker id="routeArrow" markerWidth="12" markerHeight="12" refX="9" refY="3" orient="auto" markerUnits="strokeWidth">
            <path d="M0,0 L0,6 L9,3 z" fill="#67e8f9" />
          </marker>
          <marker id="editorArrow" markerWidth="12" markerHeight="12" refX="9" refY="3" orient="auto" markerUnits="strokeWidth">
            <path d="M0,0 L0,6 L9,3 z" fill="#fb923c" />
          </marker>
          <linearGradient id="routeGradient" x1="0%" x2="100%" y1="0%" y2="0%">
            <stop offset="0%" stop-color="#22c55e" />
            <stop offset="55%" stop-color="#22d3ee" />
            <stop offset="100%" stop-color="#38bdf8" />
          </linearGradient>
        </defs>

        <g class="airspace-platform" :transform="`translate(${translateX}, ${translateY})`">
          <g v-if="showScene" class="scene-layer">
            <path v-for="road in sceneRoads" :key="road.key" :d="road.path" class="scene-road" />
            <g v-for="building in sceneBuildings" :key="building.key" class="scene-building">
              <polygon :points="building.base" class="building-base" />
              <polygon :points="building.roof" class="building-roof" />
              <line
                v-for="edge in building.edges"
                :key="edge"
                :x1="edge.split(',')[0]"
                :y1="edge.split(',')[1]"
                :x2="edge.split(',')[2]"
                :y2="edge.split(',')[3]"
                class="building-edge"
              />
              <text :x="building.labelX" :y="building.labelY" class="scene-label">{{ building.label }}</text>
            </g>
            <g v-for="point in scenePoints" :key="point.key" class="scene-point">
              <circle :cx="point.cx" :cy="point.cy" r="12" :class="`point-${point.type}`" />
              <circle :cx="point.cx" :cy="point.cy" r="19" class="point-ring" />
              <text :x="point.cx" :y="point.cy - 21" class="scene-label">{{ point.label }}</text>
            </g>
          </g>

          <g v-for="tile in positionedTiles" :key="tile.id" class="tile-group" @click="emitGrid(tile.raw)">
            <polygon :points="tile.points" :class="tileClass(tile)" filter="url(#tileGlow)" />
            <polygon v-if="tile.status === 'NO_FLY'" :points="tile.points" class="tile-stripe" />
            <text :x="tile.cx" :y="tile.cy - 5" class="tile-code">{{ tile.gridCode }}</text>
            <text :x="tile.cx" :y="tile.cy + 14" class="tile-status">{{ tile.displayState }}</text>
            <circle v-if="tile.isSelected" :cx="tile.cx" :cy="tile.cy" r="32" class="selected-pulse" />
            <circle v-if="tile.isOccupied" :cx="tile.cx + 30" :cy="tile.cy - 17" r="7" class="occupied-dot" />
            <circle v-if="tile.isRunning" :cx="tile.cx + 30" :cy="tile.cy - 17" r="12" class="running-dot" />
            <circle v-if="tile.hasCompare" :cx="tile.cx + 39" :cy="tile.cy + 8" r="6" class="compare-dot" />
            <circle v-if="tile.hasEditor" :cx="tile.cx - 39" :cy="tile.cy + 8" r="6" class="editor-dot" />
            <g v-if="tile.status === 'RISK'" class="map-icon risk-icon">
              <circle :cx="tile.cx - 31" :cy="tile.cy - 18" r="10" />
              <text :x="tile.cx - 31" :y="tile.cy - 14">!</text>
            </g>
            <g v-if="tile.status === 'NO_FLY'" class="map-icon nofly-icon">
              <circle :cx="tile.cx - 31" :cy="tile.cy - 18" r="10" />
              <text :x="tile.cx - 31" :y="tile.cy - 14">X</text>
            </g>
          </g>

          <g v-for="route in missionRouteDraws" :key="route.key" class="mission-route-layer" @click.stop="emitRoute(route.raw)">
            <path v-if="route.path" :d="route.path" :class="['mission-route-shadow', `mission-${route.status}`]" />
            <path
              v-if="route.path"
              :d="route.path"
              :class="['mission-route', `mission-${route.status}`, { active: route.active }]"
              marker-end="url(#routeArrow)"
            />
            <path v-if="route.path && route.status === 'running'" :d="route.path" class="mission-route-flow" />
            <g v-for="(p, idx) in route.points" :key="`${route.key}-${p.gridId}-${idx}`">
              <circle :cx="p.cx" :cy="p.cy - route.offset" r="6" :class="['mission-waypoint', `mission-${route.status}`]" />
            </g>
            <g
              v-if="route.labelPoint"
              class="mission-label"
              :class="{ active: route.active }"
              :transform="`translate(${route.labelPoint.cx + 16}, ${route.labelPoint.cy - route.offset - 18})`"
            >
              <rect x="0" y="-19" :width="route.labelWidth" height="36" rx="8" />
              <text x="10" y="-4">{{ route.code }}</text>
              <text x="10" y="10">{{ route.meta }}</text>
            </g>
          </g>

          <g v-for="route in compareRouteDraws" :key="route.key" class="compare-route-layer">
            <path v-if="route.path" :d="route.path" :class="['compare-route', `compare-route-${route.index % 6}`]" />
            <g v-for="(p, idx) in route.points" :key="`${route.key}-${p.gridId}-${idx}`">
              <circle :cx="p.cx" :cy="p.cy + 26 + route.index * 3" r="5" :class="['compare-node', `compare-node-${route.index % 6}`]" />
            </g>
            <text v-if="route.points[0]" :x="route.points[0].cx - 30" :y="route.points[0].cy + 46 + route.index * 3" class="compare-label">{{ route.code }}</text>
          </g>

          <path v-if="routePath" :d="routePath" class="route-shadow" />
          <path v-if="routePath" :d="routePath" class="route-main" marker-end="url(#routeArrow)" filter="url(#routeGlow)" />
          <path v-if="routePath" :d="routePath" class="route-flow" />

          <path v-if="editorPath" :d="editorPath" class="editor-route-main" marker-end="url(#editorArrow)" />
          <g v-for="(point, index) in editorDrawPoints" :key="`editor-node-${point.gridId}-${index}`" class="editor-node-group">
            <circle :cx="point.cx" :cy="point.cy - 26" r="12" class="editor-node" />
            <text :x="point.cx" :y="point.cy - 22" class="editor-node-label">{{ index + 1 }}</text>
          </g>

          <g v-for="(point, index) in routePoints" :key="`route-node-${point.gridId}-${index}`" class="route-node-group">
            <circle :cx="point.cx" :cy="point.cy" r="13" :class="index === 0 ? 'route-node-start' : index === routePoints.length - 1 ? 'route-node-end' : 'route-node-mid'" />
            <text :x="point.cx" :y="point.cy + 4" class="route-node-label">{{ index === 0 ? 'S' : index === routePoints.length - 1 ? 'E' : index + 1 }}</text>
          </g>

          <g v-for="marker in conflictMarkers" :key="marker.key" class="conflict-marker" @click.stop="emitConflict(marker.raw)">
            <circle :cx="marker.cx" :cy="marker.cy - 38" r="15" :class="marker.blocking ? 'conflict-hard' : 'conflict-risk'" />
            <circle :cx="marker.cx" :cy="marker.cy - 38" r="22" :class="marker.blocking ? 'conflict-hard-ring' : 'conflict-risk-ring'" />
            <text :x="marker.cx" :y="marker.cy - 33" class="conflict-label">{{ marker.blocking ? 'H' : 'R' }}</text>
          </g>

          <g v-for="drone in droneMarkers" :key="drone.key" class="drone-marker" @click.stop="emitRoute(drone.raw)">
            <circle :cx="drone.cx" :cy="drone.cy - 52" r="18" :class="`drone-${drone.status}`" />
            <path :d="`M ${drone.cx - 12} ${drone.cy - 52} L ${drone.cx} ${drone.cy - 62} L ${drone.cx + 12} ${drone.cy - 52} L ${drone.cx} ${drone.cy - 46} Z`" class="drone-body" />
            <text :x="drone.cx" :y="drone.cy - 72" class="scene-label">{{ drone.code }}</text>
          </g>
        </g>
      </svg>

      <div class="map-floating-legend">
        <span><i class="legend-dot active"></i>空闲</span>
        <span><i class="legend-dot occupied"></i>已预约</span>
        <span><i class="legend-dot running"></i>执行中</span>
        <span><i class="legend-dot risk"></i>风险</span>
        <span><i class="legend-dot nofly"></i>禁飞</span>
        <span><i class="legend-line route"></i>任务航线</span>
        <span><i class="legend-dot conflict"></i>冲突</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, default: '低空航线态势图' },
  grids: { type: Array, default: () => [] },
  routeDetail: { type: Object, default: null },
  missionRoutes: { type: Array, default: () => [] },
  sceneFeatures: { type: Object, default: () => ({}) },
  activeRouteId: { type: [Number, String], default: null },
  showScene: { type: Boolean, default: true },
  compareRoutes: { type: Array, default: () => [] },
  editorPoints: { type: Array, default: () => [] },
  editorMode: { type: Boolean, default: false },
  conflicts: { type: Array, default: () => [] },
  occupancies: { type: Array, default: () => [] },
  selectedGridId: { type: [Number, String], default: null },
  selectedLevelId: { type: [Number, String], default: null },
  selectedTimeSlotId: { type: [Number, String], default: null },
  selectedDate: { type: String, default: '' },
  levelLabel: { type: String, default: 'All Levels' },
  timeLabel: { type: String, default: 'All TimeSlots' },
  showToolbar: { type: Boolean, default: true },
  compact: { type: Boolean, default: false }
})

const emit = defineEmits(['select-grid', 'select-conflict', 'select-route'])

const tileW = 86
const tileH = 48
const tileDepth = 10
const margin = 130

function pointGridId(g) { return Number(g?.gridId || g?.id || g) }
function routeGrids(route) { return [...(route?.grids || [])].sort((a, b) => Number(a.sequenceNo || a.sortOrder || 0) - Number(b.sequenceNo || b.sortOrder || 0)) }
function stateWeight(status) {
  return { RELEASED: 0, OCCUPIED: 1, RESERVED: 2, RUNNING: 3, CONFLICT: 4 }[status] || 1
}
function normalizeMissionStatus(status) {
  const value = String(status || '').toLowerCase()
  if (['running', 'risk', 'conflict', 'completed', 'pending'].includes(value)) return value
  if (value === 'approved') return 'running'
  return 'pending'
}

const routeGridIds = computed(() => new Set((props.routeDetail?.grids || []).map(g => pointGridId(g))))
const compareGridIds = computed(() => new Set((props.compareRoutes || []).flatMap(r => (r.grids || []).map(g => pointGridId(g)))))
const editorGridIds = computed(() => new Set((props.editorPoints || []).map(pointGridId)))

const currentLevelLabel = computed(() => props.levelLabel || 'All Levels')
const currentTimeLabel = computed(() => props.timeLabel || 'All TimeSlots')

const occupancySet = computed(() => {
  const set = new Set()
  for (const o of props.occupancies || []) {
    if (o.status && !['OCCUPIED', 'RESERVED', 'RUNNING', 'CONFLICT'].includes(o.status)) continue
    if (props.selectedLevelId && Number(o.levelId) !== Number(props.selectedLevelId)) continue
    if (props.selectedTimeSlotId && Number(o.timeSlotId) !== Number(props.selectedTimeSlotId)) continue
    if (props.selectedDate && o.bookingDate && o.bookingDate !== props.selectedDate) continue
    set.add(Number(o.gridId))
  }
  return set
})

const occupancyMap = computed(() => {
  const map = new Map()
  for (const o of props.occupancies || []) {
    if (props.selectedLevelId && Number(o.levelId) !== Number(props.selectedLevelId)) continue
    if (props.selectedTimeSlotId && Number(o.timeSlotId) !== Number(props.selectedTimeSlotId)) continue
    if (props.selectedDate && o.bookingDate && o.bookingDate !== props.selectedDate) continue
    const current = map.get(Number(o.gridId))
    if (!current || stateWeight(o.status) > stateWeight(current.status)) map.set(Number(o.gridId), o)
  }
  return map
})

const conflictMap = computed(() => {
  const map = new Map()
  for (const c of props.conflicts || []) {
    const id = Number(c.gridId)
    if (!map.has(id)) map.set(id, [])
    map.get(id).push(c)
  }
  return map
})

function baseIso(row, col) {
  const x = (Number(col || 1) - Number(row || 1)) * tileW / 2
  const y = (Number(col || 1) + Number(row || 1)) * tileH / 2
  return { x, y }
}

const rawTiles = computed(() => (props.grids || []).map(g => {
  const { x, y } = baseIso(g.rowIndex, g.colIndex)
  const occupancy = occupancyMap.value.get(Number(g.id))
  const isRunning = occupancy?.status === 'RUNNING'
  const isOccupied = occupancySet.value.has(Number(g.id))
  const conflicts = conflictMap.value.get(Number(g.id)) || []
  const blocking = conflicts.some(c => c.blocking || c.conflictLevel === 'BLOCKING' || c.conflictType === 'HARD')
  const hasRoute = routeGridIds.value.has(Number(g.id))
  const hasCompare = compareGridIds.value.has(Number(g.id))
  const hasEditor = editorGridIds.value.has(Number(g.id))
  let displayState = g.status || 'ACTIVE'
  if (blocking) displayState = 'CONFLICT'
  else if (isRunning) displayState = 'RUNNING'
  else if (isOccupied) displayState = occupancy?.status || 'OCCUPIED'
  else if (hasEditor) displayState = 'DRAFT'
  else if (hasRoute) displayState = 'ROUTE'
  else if (hasCompare) displayState = 'COMPARE'
  return {
    id: g.id,
    raw: g,
    row: g.rowIndex,
    col: g.colIndex,
    gridCode: g.gridCode,
    gridName: g.gridName,
    status: g.status || 'ACTIVE',
    displayState,
    x,
    y,
    cx: x,
    cy: y,
    isSelected: String(props.selectedGridId || '') === String(g.id),
    isOccupied,
    isRunning,
    hasRoute,
    hasCompare,
    hasEditor,
    hasConflict: conflicts.length > 0,
    blocking
  }
}))

const bounds = computed(() => {
  if (rawTiles.value.length === 0) return { minX: -200, maxX: 600, minY: 0, maxY: 520 }
  const xs = rawTiles.value.map(t => t.x)
  const ys = rawTiles.value.map(t => t.y)
  return {
    minX: Math.min(...xs) - tileW * 1.6,
    maxX: Math.max(...xs) + tileW * 1.7,
    minY: Math.min(...ys) - tileH * 1.4,
    maxY: Math.max(...ys) + tileH * 2.4
  }
})

const translateX = computed(() => margin - bounds.value.minX)
const translateY = computed(() => margin / 2 - bounds.value.minY)
const viewBox = computed(() => {
  const width = bounds.value.maxX - bounds.value.minX + margin * 2
  const height = bounds.value.maxY - bounds.value.minY + margin * 1.4
  return `0 0 ${Math.max(width, 760)} ${Math.max(height, 520)}`
})

const tileById = computed(() => {
  const m = new Map()
  for (const t of rawTiles.value) m.set(Number(t.id), t)
  return m
})

const positionedTiles = computed(() => rawTiles.value.map(t => {
  const top = `${t.cx},${t.cy - tileH / 2}`
  const right = `${t.cx + tileW / 2},${t.cy}`
  const bottom = `${t.cx},${t.cy + tileH / 2 + tileDepth}`
  const left = `${t.cx - tileW / 2},${t.cy}`
  return { ...t, points: `${top} ${right} ${bottom} ${left}` }
}))

function toDrawPoints(route) {
  return routeGrids(route).map(g => {
    const id = pointGridId(g)
    const tile = tileById.value.get(id)
    return tile ? { ...g, cx: tile.cx, cy: tile.cy, gridId: id } : null
  }).filter(Boolean)
}
function pathFromPoints(points, dy = 0) {
  if (points.length < 2) return ''
  return points.map((p, index) => `${index === 0 ? 'M' : 'L'} ${p.cx} ${p.cy + dy}`).join(' ')
}

const routePoints = computed(() => toDrawPoints(props.routeDetail))
const routePath = computed(() => pathFromPoints(routePoints.value, -2))
const editorDrawPoints = computed(() => (props.editorPoints || []).map(pointGridId).map(id => {
  const tile = tileById.value.get(Number(id))
  return tile ? { gridId: id, cx: tile.cx, cy: tile.cy } : null
}).filter(Boolean))
const editorPath = computed(() => pathFromPoints(editorDrawPoints.value, -26))

const compareRouteDraws = computed(() => (props.compareRoutes || []).map((route, index) => {
  const points = toDrawPoints(route)
  return {
    key: `${route.id || route.routeCode || index}`,
    code: route.routeCode || `R${index + 1}`,
    index,
    points,
    path: pathFromPoints(points, 26 + index * 3)
  }
}).filter(r => r.points.length >= 1))

const missionRouteDraws = computed(() => (props.missionRoutes || []).map((route, index) => {
  const points = toDrawPoints(route)
  const offset = 16 + (index % 4) * 9
  const status = normalizeMissionStatus(route.status)
  const labelPoint = points[Math.min(1, points.length - 1)]
  return {
    key: `${route.id || route.code || index}`,
    raw: route,
    code: route.code || `T-${index + 1}`,
    meta: route.meta || `${route.levelName || ''} / ${route.timeText || ''}`,
    status,
    active: String(props.activeRouteId || '') === String(route.id || route.code || ''),
    offset,
    points,
    path: pathFromPoints(points, -offset),
    labelPoint,
    labelWidth: Math.max(132, Math.min(250, String(route.code || '').length * 8 + 110))
  }
}).filter(r => r.points.length >= 2))

const droneMarkers = computed(() => missionRouteDraws.value
  .filter(route => route.raw.showDrone !== false && ['running', 'conflict', 'risk'].includes(route.status))
  .map(route => {
    const idx = route.status === 'running' ? Math.floor(route.points.length / 2) : route.points.length - 1
    const p = route.points[Math.max(0, Math.min(idx, route.points.length - 1))]
    return { key: `drone-${route.key}`, raw: route.raw, code: route.code, status: route.status, cx: p.cx, cy: p.cy - route.offset }
  }))

const conflictMarkers = computed(() => {
  const items = []
  let idx = 0
  for (const c of props.conflicts || []) {
    const tile = tileById.value.get(Number(c.gridId))
    if (!tile) continue
    items.push({
      key: `${c.ruleCode || 'conflict'}-${c.gridId}-${c.timeSlotId || 'all'}-${idx++}`,
      raw: c,
      cx: tile.cx,
      cy: tile.cy,
      blocking: Boolean(c.blocking || c.conflictLevel === 'BLOCKING' || c.conflictType === 'HARD')
    })
  }
  return items
})

const sceneRoads = computed(() => (props.sceneFeatures.roads || defaultRoads()).map((road, index) => ({
  key: road.key || `road-${index}`,
  path: pathFromGridPath(road.path || [], road.dy || 36)
})).filter(r => r.path))

const scenePoints = computed(() => (props.sceneFeatures.points || defaultScenePoints()).map((point, index) => {
  const base = baseIso(point.row, point.col)
  return { key: point.key || `point-${index}`, cx: base.x, cy: base.y + (point.dy || 0), label: point.label, type: point.type || 'poi' }
}))

const sceneBuildings = computed(() => (props.sceneFeatures.buildings || defaultBuildings()).map((item, index) => {
  const base = baseIso(item.row, item.col)
  const width = item.width || 96
  const height = item.height || 52
  const depth = item.depth || 32
  const x = base.x + (item.dx || 0)
  const y = base.y + (item.dy || 0)
  const basePoly = `${x - width / 2},${y} ${x},${y - height / 2} ${x + width / 2},${y} ${x},${y + height / 2}`
  const roof = `${x - width / 2},${y - depth} ${x},${y - height / 2 - depth} ${x + width / 2},${y - depth} ${x},${y + height / 2 - depth}`
  const edges = [
    `${x - width / 2},${y},${x - width / 2},${y - depth}`,
    `${x},${y - height / 2},${x},${y - height / 2 - depth}`,
    `${x + width / 2},${y},${x + width / 2},${y - depth}`,
    `${x},${y + height / 2},${x},${y + height / 2 - depth}`
  ]
  return { key: item.key || `building-${index}`, base: basePoly, roof, edges, label: item.label || '', labelX: x - width / 2, labelY: y - depth - height / 2 - 8 }
}))

function tileClass(tile) {
  return {
    'tile-base': true,
    'tile-active': tile.status === 'ACTIVE',
    'tile-risk': tile.status === 'RISK',
    'tile-nofly': tile.status === 'NO_FLY',
    'tile-route': tile.hasRoute,
    'tile-compare': tile.hasCompare,
    'tile-editor': tile.hasEditor,
    'tile-occupied': tile.isOccupied,
    'tile-running': tile.isRunning,
    'tile-conflict': tile.hasConflict,
    'tile-blocking': tile.blocking,
    'tile-selected': tile.isSelected
  }
}

function emitGrid(grid) { emit('select-grid', grid) }
function emitConflict(conflict) { emit('select-conflict', conflict) }
function emitRoute(route) { emit('select-route', route) }

function pathFromGridPath(items, dy = 0) {
  const points = items.map(item => {
    const base = baseIso(item.row, item.col)
    return `${base.x} ${base.y + dy}`
  })
  if (points.length < 2) return ''
  return points.map((p, index) => `${index === 0 ? 'M' : 'L'} ${p}`).join(' ')
}
function defaultRoads() {
  return [
    { path: [{ row: 1, col: 1 }, { row: 3, col: 4 }, { row: 7, col: 8 }, { row: 10, col: 10 }], dy: 58 },
    { path: [{ row: 9, col: 1 }, { row: 7, col: 4 }, { row: 4, col: 7 }, { row: 2, col: 10 }], dy: 48 }
  ]
}
function defaultBuildings() {
  return [
    { row: 2, col: 3, label: '仓储区', dx: -18, dy: 54 },
    { row: 4, col: 5, label: '调度中心', dx: 20, dy: 58, width: 116, height: 62 },
    { row: 7, col: 4, label: '能源站', dx: -30, dy: 52 },
    { row: 8, col: 8, label: '光伏阵列', dx: 14, dy: 56, width: 120 }
  ]
}
function defaultScenePoints() {
  return [
    { row: 1, col: 1, label: '起降点 A', type: 'launch' },
    { row: 10, col: 10, label: '起降点 B', type: 'launch' },
    { row: 3, col: 7, label: '电塔 P3', type: 'tower' },
    { row: 6, col: 2, label: '巡检点 S2', type: 'poi' },
    { row: 8, col: 6, label: '巡检点 S6', type: 'poi' }
  ]
}
</script>
