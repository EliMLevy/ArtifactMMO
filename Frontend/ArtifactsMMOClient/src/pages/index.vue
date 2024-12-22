<template>
    <div class="ma-4">
        <!-- Error Message -->
        <div v-if="characterStore.error" class="error">Error: {{ characterStore.error }}</div>

        <!-- Character Cards -->
        <v-container>
            <v-row>
                <v-col cols="6">
                    <BankViewer />
                </v-col>
                <v-col cols="6">
                    <FightSimulator />
                    <MarketViewer class="my-5" />
                    <CurrentEvents />
                </v-col>
            </v-row>
            <v-row v-if="characterStore.characters.length" class="g-4">
              <v-col>
                <v-row>
                  <v-col>
                    <v-btn-toggle v-model="toggle" divided>
                        <v-btn variant="outlined" icon="mdi-view-grid-compact"></v-btn>
                        <v-btn variant="outlined" icon="mdi-view-grid"></v-btn>
                        <v-btn variant="outlined" icon="mdi-crop-square"></v-btn>
                    </v-btn-toggle>
                  </v-col>
                </v-row>
                <v-row>
                  <CharacterCard v-for="character in characterStore.characters" :key="character.name" :character="character" :logs="characterStore.logs" :panel-size="toggle" />
                </v-row>
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
import { useCharacterStore } from '@/stores/characters';
import { onMounted } from 'vue';
import CharacterCard from '@/components/CharacterCard.vue';
import { useItemsStore } from '@/stores/items';
import { useMonsterStore } from '@/stores/monsters';
import { useMapsStore } from '@/stores/maps';
import MarketViewer from '@/components/MarketViewer.vue';

export default {
    components: {
        CharacterCard,
        MarketViewer,
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
            mapsStore.loadMaps();

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
            toggle: 2,
        };
    },
};
</script>

<style scoped>
.error {
    color: red;
}
</style>
