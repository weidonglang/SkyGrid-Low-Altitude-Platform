<template>
  <section class="airspace-map-card">
    <div class="map-toolbar" v-if="showToolbar">
      <div>
        <p class="eyebrow">2.5D Airspace Route Map</p>
        <h3>{{ title }}</h3>
      </div>
      <div class="map-badges">
        <span class="mini-badge cyan">{{ currentLevelLabel }}</span>
        <span class="mini-badge violet">{{ currentTimeLabel }}</span>
        <span class="mini-badge slate">{{ selectedDate || 'No date selected' }}</span>
        <span v-if="compareRoutes.length" class="mini-badge orange">Compare × {{ compareRoutes.length }}</span>
        <span v-if="editorPoints.length" class="mini-badge green">Draft × {{ editorPoints.length }}</span>
      </div>
    </div>

    <div class="airspace-map-wrap" :class="{ compact, editing: editorMode }">
      <svg class="airspace-svg" :viewBox="viewBox" preserveAspectRatio="xMidYMid meet" role="img">
        <defs>
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
          <g v-for="tile in positionedTiles" :key="tile.id" class="tile-group" @click="emitGrid(tile.raw)">
            <polygon :points="tile.points" :class="tileClass(tile)" filter="url(#tileGlow)" />
            <polygon v-if="tile.status === 'NO_FLY'" :points="tile.points" class="tile-stripe" />
            <text :x="tile.cx" :y="tile.cy - 5" class="tile-code">{{ tile.gridCode }}</text>
            <text :x="tile.cx" :y="tile.cy + 14" class="tile-status">{{ tile.displayState }}</text>
            <circle v-if="tile.isSelected" :cx="tile.cx" :cy="tile.cy" r="32" class="selected-pulse" />
            <circle v-if="tile.isOccupied" :cx="tile.cx + 30" :cy="tile.cy - 17" r="7" class="occupied-dot" />
            <circle v-if="tile.hasCompare" :cx="tile.cx + 39" :cy="tile.cy + 8" r="6" class="compare-dot" />
            <circle v-if="tile.hasEditor" :cx="tile.cx - 39" :cy="tile.cy + 8" r="6" class="editor-dot" />
            <g v-if="tile.status === 'RISK'" class="map-icon risk-icon">
              <circle :cx="tile.cx - 31" :cy="tile.cy - 18" r="10" />
              <text :x="tile.cx - 31" :y="tile.cy - 14">!</text>
            </g>
            <g v-if="tile.status === 'NO_FLY'" class="map-icon nofly-icon">
              <circle :cx="tile.cx - 31" :cy="tile.cy - 18" r="10" />
              <text :x="tile.cx - 31" :y="tile.cy - 14">×</text>
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
        </g>
      </svg>

      <div class="map-floating-legend">
        <span><i class="legend-dot active"></i>可用</span>
        <span><i class="legend-dot occupied"></i>占用</span>
        <span><i class="legend-dot risk"></i>风险</span>
        <span><i class="legend-dot nofly"></i>禁飞</span>
        <span><i class="legend-line route"></i>当前航线</span>
        <span><i class="legend-line compare"></i>对比航线</span>
        <span><i class="legend-line editor"></i>编辑草稿</span>
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

const emit = defineEmits(['select-grid', 'select-conflict'])

const tileW = 116
const tileH = 66
const tileDepth = 13
const margin = 120

function pointGridId(g) { return Number(g?.gridId || g?.id || g) }
function routeGrids(route) { return [...(route?.grids || [])].sort((a, b) => Number(a.sequenceNo || a.sortOrder || 0) - Number(b.sequenceNo || b.sortOrder || 0)) }

const routeGridIds = computed(() => new Set((props.routeDetail?.grids || []).map(g => pointGridId(g))))
const compareGridIds = computed(() => new Set((props.compareRoutes || []).flatMap(r => (r.grids || []).map(g => pointGridId(g)))))
const editorGridIds = computed(() => new Set((props.editorPoints || []).map(pointGridId)))

const currentLevelLabel = computed(() => props.levelLabel || 'All Levels')
const currentTimeLabel = computed(() => props.timeLabel || 'All TimeSlots')

const occupancySet = computed(() => {
  const set = new Set()
  for (const o of props.occupancies || []) {
    if (o.status && o.status !== 'OCCUPIED') continue
    if (props.selectedLevelId && Number(o.levelId) !== Number(props.selectedLevelId)) continue
    if (props.selectedTimeSlotId && Number(o.timeSlotId) !== Number(props.selectedTimeSlotId)) continue
    if (props.selectedDate && o.bookingDate && o.bookingDate !== props.selectedDate) continue
    set.add(Number(o.gridId))
  }
  return set
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
  const isOccupied = occupancySet.value.has(Number(g.id))
  const conflicts = conflictMap.value.get(Number(g.id)) || []
  const blocking = conflicts.some(c => c.blocking || c.conflictLevel === 'BLOCKING' || c.conflictType === 'HARD')
  const hasRoute = routeGridIds.value.has(Number(g.id))
  const hasCompare = compareGridIds.value.has(Number(g.id))
  const hasEditor = editorGridIds.value.has(Number(g.id))
  let displayState = g.status || 'ACTIVE'
  if (blocking) displayState = 'CONFLICT'
  else if (isOccupied) displayState = 'OCCUPIED'
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
    minX: Math.min(...xs) - tileW,
    maxX: Math.max(...xs) + tileW,
    minY: Math.min(...ys) - tileH,
    maxY: Math.max(...ys) + tileH * 2
  }
})

const translateX = computed(() => margin - bounds.value.minX)
const translateY = computed(() => margin / 2 - bounds.value.minY)
const viewBox = computed(() => {
  const width = bounds.value.maxX - bounds.value.minX + margin * 2
  const height = bounds.value.maxY - bounds.value.minY + margin * 1.4
  return `0 0 ${Math.max(width, 760)} ${Math.max(height, 480)}`
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
    'tile-conflict': tile.hasConflict,
    'tile-blocking': tile.blocking,
    'tile-selected': tile.isSelected
  }
}

function emitGrid(grid) { emit('select-grid', grid) }
function emitConflict(conflict) { emit('select-conflict', conflict) }
</script>
