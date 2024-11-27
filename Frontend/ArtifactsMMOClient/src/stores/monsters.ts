import type { Monster } from "@/ArtifactsTypes";
import axios from "axios";
import { defineStore } from "pinia";
import { ref } from "vue";



export const useMonsterStore = defineStore("monsterStore", {
  // State: A reactive array to store monsters
  state: () => ({
        monsters: [] as Monster[],
        isLoading: false,
        error: null
  }),
  actions: {
    async loadMonsters() {
        this.isLoading = true;
        this.error = null;
        try {
          const response = await axios.get(
            "/all_monsters.json"
          );
          this.monsters = response.data.data;
        } catch (error: any) {
          this.error =
            error.response?.data?.message || "Failed to load characters.";
        } finally {
          this.isLoading = false;
        }
    }
  }  
});
