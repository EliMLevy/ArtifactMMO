<template>
  <v-card class="pa-4">
    <!-- Name and Level -->
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

    <!-- Location -->
    <v-row class="mb-4">
      <v-col>
        <strong>Location:</strong>
      </v-col>
      <v-col>
        <span>{{ character.x }}, {{ character.y }}</span>
      </v-col>
    </v-row>

    <!-- Collecting Skills -->
    <v-row>
      <v-col>
        <strong>Collecting Skills:</strong>
      </v-col>
    </v-row>
    <v-row class="mb-4">
      <v-col>
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
      <v-col>
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
      <v-col>
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
      <v-col>
        <strong>Crafting Skills:</strong>
      </v-col>
    </v-row>
    <v-row>
      <v-col>
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
      <v-col>
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
      <v-col>
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
  </v-card>
</template>

<script lang="ts">
import { defineComponent, type PropType } from "vue";
import type { Character } from "@/ArtifactsTypes";

export default defineComponent({
  name: "CharacterCard",
  props: {
    character: {
      type: Object as PropType<Character>,
      required: true,
    },
  },
  computed: {
    levelProgress(): number {
      return Math.min((this.character.xp / this.character.max_xp) * 100, 100);
    },
  },
});
</script>

<style scoped>
.skill-container {
  margin-bottom: 8px;
}
</style>
