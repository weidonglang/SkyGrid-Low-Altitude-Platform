<template>
  <div class="timeline-card">
    <div class="timeline-head">
      <div>
        <p class="eyebrow">TimeSlot Playback</p>
        <h3>时空播放轴</h3>
      </div>
      <button class="ghost-button" @click="togglePlay">{{ playing ? '暂停' : '播放' }}</button>
    </div>
    <div class="timeslot-rail">
      <button
        v-for="slot in slots"
        :key="slot.id"
        class="timeslot-chip"
        :class="{ active: String(modelValue) === String(slot.id) }"
        @click="select(slot.id)"
      >
        <strong>{{ slot.slotCode || slot.id }}</strong>
        <span>{{ slot.slotName || `${slot.startTime || ''}-${slot.endTime || ''}` }}</span>
        <small>{{ countFor(slot.id) }} occupied</small>
      </button>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, ref } from 'vue'

const props = defineProps({
  slots: { type: Array, default: () => [] },
  modelValue: { type: [Number, String], default: null },
  occupancies: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:modelValue'])
const playing = ref(false)
let timer = null

function select(id) { emit('update:modelValue', id) }
function countFor(id) { return (props.occupancies || []).filter(o => String(o.timeSlotId) === String(id) && o.status === 'OCCUPIED').length }
function togglePlay() {
  playing.value = !playing.value
  if (playing.value) {
    timer = setInterval(() => {
      if (!props.slots.length) return
      const idx = props.slots.findIndex(s => String(s.id) === String(props.modelValue))
      const next = props.slots[(idx + 1) % props.slots.length]
      emit('update:modelValue', next.id)
    }, 1200)
  } else if (timer) {
    clearInterval(timer)
    timer = null
  }
}
onBeforeUnmount(() => { if (timer) clearInterval(timer) })
</script>
