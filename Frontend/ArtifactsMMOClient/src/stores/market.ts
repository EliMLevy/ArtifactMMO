import { defineStore } from 'pinia';
import axios from 'axios';
import type { MarketListing } from '@/ArtifactsTypes';

export const useMarketStore = defineStore('marketStore', {
    state: () => ({
        market: [] as MarketListing[],
        loading: false,
    }),
    actions: {
        async getMarket() {
            try {
                this.loading = true;
                let page1 = await axios.get('https://api.artifactsmmo.com/grandexchange/orders?size=100', {
                    headers: {
                        Accept: 'application/json',
                        Authorization: `Bearer ${import.meta.env.VITE_API_BEARER_TOKEN}`,
                    },
                });
                this.market = page1.data.data;
                for(let i = 2; i < page1.data.pages; i++) {
                    let nextPage = await axios.get('https://api.artifactsmmo.com/grandexchange/orders?size=100&page=' + i, {
                        headers: {
                            Accept: 'application/json',
                            Authorization: `Bearer ${import.meta.env.VITE_API_BEARER_TOKEN}`,
                        },
                    });
                    this.market.push(...nextPage.data.data);
                }
                this.loading = false;
            } catch (error) {
                console.error('Failed to fetch bank items:', error);
            }
        },
    },
});
