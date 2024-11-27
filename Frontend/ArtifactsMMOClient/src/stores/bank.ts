import { defineStore } from "pinia";
import axios from "axios";
import type { Item } from "@/ArtifactsTypes";


export const useBankStore = defineStore("bankStore", {
  state: () => ({
    bank: [] as Item[],
    loading: false
  }),
  actions: {
    async getBank() {
      try {
        this.loading = true
        const response = await axios.get(
          "https://api.artifactsmmo.com/my/bank/items?size=100",
          {
            headers: {
              Accept: "application/json",
              Authorization: `Bearer ${import.meta.env.VITE_API_BEARER_TOKEN}`,
            },
          }
        );
        this.bank = response.data.data;
        this.loading = false;

      } catch (error) {
        console.error("Failed to fetch bank items:", error);
      }
    },
  },
});
