import { defineStore } from 'pinia';
import axios from 'axios';
import type { Character, CharacterName, GearSetup, GearSlot, Log } from '@/ArtifactsTypes';

const gear_slots: GearSlot[] = ['weapon_slot', 'shield_slot', 'shield_slot', 'helmet_slot', 'body_armor_slot', 'leg_armor_slot', 'boots_slot', 'ring1_slot', 'ring2_slot', 'amulet_slot'];

export const useCharacterStore = defineStore('characters', {
    state: () => ({
        characters: [] as Character[],
        activeGear: {
            Bobby: {} as GearSetup,
            Stuart: {} as GearSetup,
            George: {} as GearSetup,
            Tim: {} as GearSetup,
            Joe: {} as GearSetup,
        },
        logs: [] as Log[],
        isLoading: false,
        error: null as string | null,
    }),

    actions: {
        async loadData() {
            this.isLoading = true;
            this.error = null;
            try {
                const response = await axios.get('https://api.artifactsmmo.com/my/characters', {
                    headers: {
                        Accept: 'application/json',
                        Authorization: `Bearer ${import.meta.env.VITE_API_BEARER_TOKEN}`,
                    },
                });
                this.characters = response.data.data;
                this.characters.forEach((character) => {
                    gear_slots.forEach((slot) => {
                        if (this.activeGear[character.name as CharacterName]) {
                            if (!this.activeGear[character.name as CharacterName][slot]) {
                                this.activeGear[character.name as CharacterName][slot] = character[slot];
                            }
                        }
                    });
                });
            } catch (error: any) {
                this.error = error.response?.data?.message || 'Failed to load characters.';
            } finally {
                this.isLoading = false;
            }
        },

        async loadLogs() {
            this.isLoading = true;
            this.error = null;
            try {
                const response = await axios.get('https://api.artifactsmmo.com/my/logs?size=100', {
                    headers: {
                        Accept: 'application/json',
                        Authorization: `Bearer ${import.meta.env.VITE_API_BEARER_TOKEN}`,
                    },
                });
                this.logs = response.data.data;
            } catch (error: any) {
                this.error = error.response?.data?.message || 'Failed to load logs.';
            } finally {
                this.isLoading = false;
            }
        },
        updateCharacterGear(name: CharacterName, slot: GearSlot, gear_code: string) {
            this.activeGear[name][slot] = gear_code;
        },
        resetArmor(name: CharacterName) {
            const character = this.characters.find(c => c.name == name)
            if(character) {
                gear_slots.forEach(slot => this.activeGear[name][slot] = character[slot])
            }
        }
    },
});
