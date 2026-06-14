<template>
  <div class="timeline-card heat-timeline-card">
    <div class="timeline-head">
      <div>
        <p class="eyebrow">TimeSlot Occupancy Heatline</p>
        <h3>时空占用热力时间轴</h3>
      </div>
      <button class="ghost-button" @click="togglePlay">{{ playing ? '暂停' : '播放' }}</button>
    </div>

    <div class="heatline-scale">
      <span>00:00</span>
      <span>06:00</span>
      <span>12:00</span>
      <span>18:00</span>
      <span>24:00</span>
    </div>

    <div class="heatline-rail">
      <button
        v-for="slot in slots"
        :key="slot.id"
        class="heatline-segment"
        :class="[severityFor(slot.id), { active: String(modelValue) === String(slot.id) }]"
        :title="tooltipFor(slot)"
        @click="select(slot.id)"
      >
        <span>{{ shortLabel(slot) }}</span>
      </button>
    </div>

    <div class="timeslot-rail">
      <button
        v-for="slot in visibleSlots"
        :key="slot.id"
        class="timeslot-chip"
        :class="[severityFor(slot.id), { active: String(modelValue) === String(slot.id) }]"
        @click="select(slot.id)"
      >
        <strong>{{ slot.slotCode || slot.id }}</strong>
        <span>{{ slot.slotName || `${slot.startTime || ''}-${slot.endTime || ''}` }}</span>
        <small>{{ countFor(slot.id, 'occupied') }} occupied / {{ countFor(slot.id, 'conflict') }} conflict</small>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'

const props = defineProps({
  slots: { type: Array, default: () => [] },
  modelValue: { type: [Number, String], default: null },
  occupancies: { type: Array, default: () => [] },
  conflicts: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:modelValue'])
const playing = ref(false)
let timer = null

const visibleSlots = computed(() => {
  if (props.slots.length <= 12) return props.slots
  const selectedIndex = Math.max(0, props.slots.findIndex(s => String(s.id) === String(props.modelValue)))
  const start = Math.max(0, selectedIndex - 3)
  return props.slots.slice(start, start + 8)
})

function select(id) { emit('update:modelValue', id) }
function countFor(id, type = 'occupied') {
  if (type === 'conflict') return (props.conflicts || []).filter(c => String(c.timeSlotId) === String(id)).length
  return (props.occupancies || []).filter(o => String(o.timeSlotId) === String(id) && ['OCCUPIED', 'RESERVED', 'RUNNING', 'CONFLICT'].includes(o.status)).length
}
function severityFor(id) {
  const slotConflicts = (props.conflicts || []).filter(c => String(c.timeSlotId) === String(id))
  if (slotConflicts.some(c => c.blocking || c.conflictType === 'HARD' || c.conflictLevel === 'BLOCKING')) return 'heat-conflict'
  if (slotConflicts.length > 0) return 'heat-risk'
  const states = (props.occupancies || []).filter(o => String(o.timeSlotId) === String(id)).map(o => o.status)
  if (states.includes('RUNNING')) return 'heat-running'
  if (states.includes('OCCUPIED') || states.includes('RESERVED') || states.includes('CONFLICT')) return 'heat-occupied'
  if (states.includes('NO_FLY')) return 'heat-nofly'
  return 'heat-idle'
}
function shortLabel(slot) {
  const label = slot.slotCode || slot.slotName || String(slot.id)
  return String(label).replace('TS-', '').replace('SLOT-', '')
}
function tooltipFor(slot) {
  return `${slot.slotCode || slot.id} ${slot.slotName || ''}: ${countFor(slot.id)} occupied, ${countFor(slot.id, 'conflict')} conflicts`
}
function togglePlay() {
  playing.value = !playing.value
  if (playing.value) {
    timer = setInterval(() => {
      if (!props.slots.length) return
      const idx = props.slots.findIndex(s => String(s.id) === String(props.modelValue))
      const next = props.slots[(idx + 1) % props.slots.length]
      emit('update:modelValue', next.id)
    }, 900)
  } else if (timer) {
    clearInterval(timer)
    timer = null
  }
}
onBeforeUnmount(() => { if (timer) clearInterval(timer) })
</script>
