<template>
  <div class="ma-4">

    <!-- Error Message -->
    <div v-if="characterStore.error" class="error">
      Error: {{ characterStore.error }}
    </div>

    <!-- Character Cards -->
    <v-container>
      <v-row>
        <v-col cols="6">
          <BankViewer />
        </v-col>
        <v-col cols="6">
          <FightSimulator />
        </v-col>
      </v-row>
      <v-row v-if="characterStore.characters.length" class="g-4">
        <v-col
          v-for="character in characterStore.characters"
          :key="character.name"
          cols="12"
          md="12"
          lg="12"
        >
          <CharacterCard :character="character" :logs="characterStore.logs" />
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
import { useItemsStore } from "@/stores/items";
import { useMonsterStore } from "@/stores/monsters";
import { useMapsStore } from "@/stores/maps";

export default {
  components: {
    CharacterCard,
  },
  setup() {
    const characterStore = useCharacterStore();
    const monstersStore = useMonsterStore();
    const itemsStore = useItemsStore();
    const mapsStore = useMapsStore();

    const loadCharacters = () => {
      characterStore.loadData();
      characterStore.loadLogs();
    };
    let loadIntervalId = 0;
    onMounted(async () => {
      loadCharacters();
      monstersStore.loadMonsters();
      itemsStore.loadItems();
      mapsStore.loadMaps()

      loadIntervalId = setInterval(() => {
        characterStore.loadData();
        characterStore.loadLogs();
      }, 5000);
    });

    onUnmounted(() => {
      clearInterval(loadIntervalId);
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
