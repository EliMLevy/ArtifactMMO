import type { MapEntry } from "@/ArtifactsTypes";
import axios from "axios";
import { defineStore } from "pinia";



export const useMapsStore = defineStore("mapsStore", {
  // State: A reactive array to store monsters
  state: () => ({
    maps: {} as {[key: string]: MapEntry[]},
    isLoading: false,
    error: null,
  }),
  actions: {
    async loadMaps() {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await axios.get("/all_maps.json");
        this.maps = response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || "Failed to load Maps.";
      } finally {
        this.isLoading = false;
      }
    },
  },
});
