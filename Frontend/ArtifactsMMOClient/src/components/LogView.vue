<template>
  <div  >
    <div v-if="logs.length">
        <v-card v-for="log in logs" :key="log.created_at" elevation="4" class="my-2">
            <v-card-title style="font-size: 1em;" class="pb-0 text-capitalize">{{ log.type }}</v-card-title>
            <v-card-subtitle style="font-size: 0.8em;">
                {{ formatDate(log.created_at) }}
            </v-card-subtitle>
            <v-card-item style="font-size: 0.9em;" class="pt-0">
                {{ log.description }}
            </v-card-item>
        </v-card>
    </div>
    <div v-else>
No logs available
    </div>
</div>
</template>

<script lang="ts">
import type { Log } from "@/ArtifactsTypes";
import { defineComponent, type PropType } from "vue";

export default defineComponent({
  name: "LogView",
  props: {
    logs: {
      type: Array as PropType<Log[]>,
      required: true,
    },
  },
  methods: {
    formatDate(dateString: string): string {
      const options: Intl.DateTimeFormatOptions = {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      };
      return new Date(dateString).toLocaleString(undefined, options);
    },
  },
});
</script>

<style scoped>
.log-item {
  padding-bottom: 8px;
}
.log-item:last-of-type {
  border-bottom: none;
}
</style>
