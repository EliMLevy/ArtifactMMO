import { defineStore } from 'pinia';
import axios from 'axios';
import type { ArtifactsEvent } from '@/ArtifactsTypes';

export const useEventsStore = defineStore('eventsStore', {
    state: () => ({
        events: [] as ArtifactsEvent[],
        loading: false,
    }),
    actions: {
        async getEvents() {
            try {
                this.loading = true;
                let page1 = await axios.get('https://api.artifactsmmo.com/events/active ', {
                    headers: {
                        Accept: 'application/json',
                    },
                });
                this.events = page1.data.data;
                console.log(this.events)
                this.loading = false;
            } catch (error) {
                console.error('Failed to fetch events:', error);
            }
        },
    },
});
