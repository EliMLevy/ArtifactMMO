import type { Item } from "@/ArtifactsTypes";
import axios from "axios";
import { defineStore } from "pinia";



export const useItemsStore = defineStore("itemsStore", {
  // State: A reactive array to store monsters
  state: () => ({
        items: [] as Item[],
        isLoading: false,
        error: null
  }),
  actions: {
    async loadItems() {
        this.isLoading = true;
        this.error = null;
        try {
          const response = await axios.get(
            "/all_items.json"
          );
          this.items = response.data;
        } catch (error: any) {
          this.error =
            error.response?.data?.message || "Failed to load items.";
        } finally {
          this.isLoading = false;
        }
    }
  }  
});