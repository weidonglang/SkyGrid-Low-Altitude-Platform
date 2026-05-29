<template>
  <div class="route-panel">
    <div class="panel-title-line">
      <div>
        <p class="eyebrow">Route Library</p>
        <h3>航线模板库</h3>
      </div>
      <span class="count-badge">{{ routes.length }}</span>
    </div>
    <div class="route-card-list">
      <button
        v-for="route in routes"
        :key="route.id"
        class="route-card"
        :class="{ active: String(modelValue) === String(route.id), nofly: isNoFly(route), risk: riskCount(route) > 0 }"
        @click="$emit('update:modelValue', route.id); $emit('select', route)"
      >
        <div class="route-card-top">
          <strong>{{ route.routeCode || `RT-${route.id}` }}</strong>
          <span>{{ route.routeName || 'Unnamed route' }}</span>
        </div>
        <p>{{ route.description || route.remark || '低空巡检航线模板' }}</p>
        <div class="route-meta-row">
          <span>{{ route.grids?.length || route.gridCount || 0 }} grids</span>
          <span>{{ riskCount(route) }} risk</span>
          <span>{{ isNoFly(route) ? 'NO-FLY' : 'available' }}</span>
        </div>
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  routes: { type: Array, default: () => [] },
  modelValue: { type: [Number, String], default: null }
})
defineEmits(['update:modelValue', 'select'])
function riskCount(route) { return (route.grids || []).filter(g => g.gridStatus === 'RISK' || g.status === 'RISK').length }
function isNoFly(route) { return (route.grids || []).some(g => g.gridStatus === 'NO_FLY' || g.status === 'NO_FLY') }
</script>
