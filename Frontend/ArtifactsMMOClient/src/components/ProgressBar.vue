<template>
  <div class="w-100 h-100 p-2">
    <div class="w-100 h-100 bg-grey-lighten-4 rounded-lg overflow-hidden">
      <div
        class="h-100 bg-green-accent-3 transition-all duration-300"
        :style="{ width: progressPercentage + '%' }"
      ></div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from "vue";

export default defineComponent({
  name: "ProgressBar",
  props: {
    xp: {
      type: Number,
      required: true,
      default: 0,
    },
    max_xp: {
      type: Number,
      required: true,
      default: 100,
    },
    level: {
      type: Number,
      required: true,
      default: 1,
    },
  },
  computed: {
    progressPercentage(): number {
      // Calculate progress as a percentage of max_xp
      return Math.min((this.xp / this.max_xp) * 100, 100);
    },
  },
});
</script>
