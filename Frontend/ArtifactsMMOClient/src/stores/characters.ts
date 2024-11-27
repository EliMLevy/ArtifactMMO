import { defineStore } from "pinia";
import axios from "axios";
import type { Character, Log } from "@/ArtifactsTypes";

export const useCharacterStore = defineStore("characters", {
  state: () => ({
    characters: [] as Character[], // Assuming Character is already defined
    logs: [] as Log[], // Define a Log interface/type
    isLoading: false,
    error: null as string | null,
  }),

  actions: {
    async loadData() {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await axios.get(
          "https://api.artifactsmmo.com/my/characters",
          {
            headers: {
              Accept: "application/json",
              Authorization: `Bearer ${import.meta.env.VITE_API_BEARER_TOKEN}`,
            },
          }
        );
        this.characters = response.data.data;
      } catch (error: any) {
        this.error =
          error.response?.data?.message || "Failed to load characters.";
      } finally {
        this.isLoading = false;
      }
    },

    async loadLogs() {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await axios.get(
          "https://api.artifactsmmo.com/my/logs?size=100",
          {
            headers: {
              Accept: "application/json",
              Authorization: `Bearer ${import.meta.env.VITE_API_BEARER_TOKEN}`,
            },
          }
        );
        this.logs = response.data.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || "Failed to load logs.";
      } finally {
        this.isLoading = false;
      }
    },
  },
});
