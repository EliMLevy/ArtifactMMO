<template>
  <div class="ma-4">
    <!-- Load Characters Button -->
    <v-btn
      :loading="characterStore.isLoading"
      @click="loadCharacters"
      color="primary"
      class="mb-4"
    >
      {{ characterStore.isLoading ? "Loading..." : "Load Characters" }}
    </v-btn>

    <!-- Error Message -->
    <div v-if="characterStore.error" class="error">
      Error: {{ characterStore.error }}
    </div>

    <!-- Character Cards -->
    <v-container>
      <v-row v-if="characterStore.characters.length" class="g-4">
        <v-col
          v-for="character in characterStore.characters"
          :key="character.name"
          cols="12"
          md="6"
          lg="4"
        >
          <CharacterCard :character="character" />
        </v-col>
      </v-row>

      <!-- No Characters Found -->
      <div v-else-if="!characterStore.isLoading" class="text-center mt-4">
        <strong>No characters found.</strong>
      </div>
    </v-container>
  </div>
</template>

<script lang="ts">
import { useCharacterStore } from "@/stores/characters";
import { onMounted } from "vue";
import CharacterCard from "@/components/CharacterCard.vue";

export default {
  components: {
    CharacterCard,
  },
  setup() {
    const characterStore = useCharacterStore();

    const loadCharacters = () => {
      characterStore.loadData();
    };

    onMounted(() => {
      loadCharacters();
    });

    return {
      characterStore,
      loadCharacters,
    };
  },
};
</script>

<style scoped>
.error {
  color: red;
}
</style>
