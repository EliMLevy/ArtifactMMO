<template>
  <v-card class="pa-4">
    <!-- Name and Level -->
     <v-row>
      <!-- Character state -->
      <v-col>
        <v-row class="align-center mb-4">
          <v-col>
            <h3 class="text-h5 m-0">{{ character.name }}</h3>
          </v-col>
          <v-col>
            <v-progress-linear
              :model-value="levelProgress"
              color="blue"
              height="20"
              rounded
              class="mt-1"
            >
              <small>Level {{ character.level }}</small>
            </v-progress-linear>
          </v-col>
        </v-row>
    
        <!-- Task and cooldown -->
        <v-row>
          <v-col>
            {{ character.task }}
            <v-progress-linear
                :model-value="(100 * character.task_progress) / character.task_total"
                color="cyan"
                height="15"
                rounded
              >
                <small>{{ character.task_progress }}/{{ character.task_total }}</small>
              </v-progress-linear>
          </v-col>
          <v-col>
            Cooldown: {{ character.cooldown }}
            <v-progress-linear
                :model-value="cooldownLeft"
                color="sand"
                height="15"
                rounded
              >
              </v-progress-linear>
    
          </v-col>
        </v-row>
    
    
        <!-- Collecting Skills -->
        <v-row>
          <v-col class="pb-0">
            <strong>Collecting Skills:</strong>
          </v-col>
        </v-row>
        <v-row >
          <v-col class="py-0">
            <div class="skill-container">
              <small>Mining</small>
              <v-progress-linear
                :model-value="(100 * character.mining_xp) / character.mining_max_xp"
                color="orange"
                height="15"
                rounded
              >
                <small>{{ character.mining_level }}</small>
              </v-progress-linear>
            </div>
          </v-col>
          <v-col class="py-0">
            <div class="skill-container">
              <small>Woodcutting</small>
              <v-progress-linear
                :model-value="
                  (100 * character.woodcutting_xp) / character.woodcutting_max_xp
                "
                color="green"
                height="15"
                rounded
              >
                <small>{{ character.woodcutting_level }}</small>
              </v-progress-linear>
            </div>
          </v-col>
          <v-col class="py-0">
            <div class="skill-container">
              <small>Fishing</small>
              <v-progress-linear
                :model-value="
                  (100 * character.fishing_xp) / character.fishing_max_xp
                "
                color="teal"
                height="15"
                rounded
              >
                <small>{{ character.fishing_level }}</small>
              </v-progress-linear>
            </div>
          </v-col>
        </v-row>
    
        <!-- Crafting Skills -->
        <v-row>
          <v-col class="pb-0">
            <strong>Crafting Skills:</strong>
          </v-col>
        </v-row>
        <v-row>
          <v-col class="py-0">
            <div class="skill-container">
              <small>Weapon Crafting</small>
              <v-progress-linear
                :model-value="
                  (100 * character.weaponcrafting_xp) /
                  character.weaponcrafting_max_xp
                "
                color="red"
                height="15"
                rounded
              >
                <small>{{ character.weaponcrafting_level }}</small>
              </v-progress-linear>
            </div>
          </v-col>
          <v-col class="py-0">
            <div class="skill-container">
              <small>Gear Crafting</small>
              <v-progress-linear
                :model-value="
                  (100 * character.gearcrafting_xp) / character.gearcrafting_max_xp
                "
                color="purple"
                height="15"
                rounded
              >
                <small>{{ character.gearcrafting_level }}</small>
              </v-progress-linear>
            </div>
          </v-col>
          <v-col class="py-0">
            <div class="skill-container">
              <small>Jewelry Crafting</small>
              <v-progress-linear
                :model-value="
                  (100 * character.jewelrycrafting_xp) /
                  character.jewelrycrafting_max_xp
                "
                color="pink"
                height="15"
                rounded
              >
                <small>{{ character.jewelrycrafting_level }}</small>
              </v-progress-linear>
            </div>
          </v-col>
        </v-row>

        <!-- Control panel -->
         <v-row>
          <control-panel :character-name="character.name" />
         </v-row>
      </v-col>
      <!-- Character Location & inventory -->
      <v-col>

      </v-col>
      <!-- Character Logs -->
      <v-col>
        <!-- Logs -->
        <v-row>
          <v-col>
            <strong>Logs:</strong>
          </v-col>
        </v-row>
        <v-row>
          <v-col style="max-height: 400px; overflow-y: auto;">
            <log-view v-if="filteredLogs.length" :logs="filteredLogs" />
            <p v-else>No logs available for this character.</p>
          </v-col>
        </v-row>
      </v-col>
      
     </v-row>

  </v-card>
</template>

<script lang="ts">
import { defineComponent, computed, type PropType } from "vue";
import type { Character, Log } from "@/ArtifactsTypes";

export default defineComponent({
  name: "CharacterCard",
  props: {
    character: {
      type: Object as PropType<Character>,
      required: true,
    },
    logs: {
      type: Array as PropType<Log[]>,
      required: true,
    },
  },
  data() {
    return {
      cooldownLeft: 0
    }
  },
  computed: {
    levelProgress(): number {
      return Math.min((this.character.xp / this.character.max_xp) * 100, 100);
    },
    filteredLogs(): Log[] {
      return this.logs.filter((log) => log.character === this.character.name);
    },
  },
  mounted() {
    setInterval(() => {
      this.cooldownLeft = 100 - (100 * (new Date(this.character.cooldown_expiration).getTime() - new Date().getTime())/1000) / this.character.cooldown
    }, 100);
  }
});
</script>

<style scoped>
.skill-container {
  margin-bottom: 8px;
}
</style>
