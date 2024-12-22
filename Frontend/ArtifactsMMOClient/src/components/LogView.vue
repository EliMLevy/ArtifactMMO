<template>
    <div>
        <div v-if="logs.length">
            <v-card v-for="log in logs" :key="log.created_at" elevation="4" class="my-2">
                <v-card-title style="font-size: 1em" class="pb-0 text-capitalize d-flex justify-space-between">
                    {{ log.type }}
                    <div>
                        <v-icon size="sm">mdi-clock</v-icon>
                        {{ log.cooldown }}s
                    </div>
                </v-card-title>
                <v-card-subtitle style="font-size: 0.8em">
                    {{ formatDate(log.created_at) }}
                </v-card-subtitle>
                <v-card-item style="font-size: 0.9em" class="pt-0">
                    {{ log.description }}
                    <div v-if="log.content">
                        <div v-if="log.content.hp_restored && log.content.hp_restored > 0">
                            <v-icon>mdi-heart</v-icon>
                            {{ log.content.hp_restored }}
                        </div>
                        <div v-if="log.content.rewards">
                            <div>
                                <v-icon>mdi-cash</v-icon>
                                ${{ log.content.rewards.gold }}
                            </div>
                            <div class="px-2 my-1" v-for="item in log.content.rewards.items" :key="item.code" style="border: 1px solid white; border-radius: 3px; width: fit-content">
                                {{ item.code }} x{{ item.quantity }}
                            </div>
                        </div>
                    </div>
                    <div class="d-flex justify-space-between">
                        <div v-if="log.type == 'fight'" class="d-flex">
                            <div class="mr-2">
                                <v-icon>mdi-arrow-up-bold</v-icon>
                                {{ log.content.fight.xp_gained }}
                            </div>
                            <div>
                                <v-icon>mdi-heart</v-icon>
                                -{{ getCharacterHPFromFightLog(log.content.fight.logs[0]) - getCharacterHPFromFightLog(log.content.fight.logs[log.content.fight.logs.length - 1]) }}
                            </div>
                        </div>
                        <div v-if="log.type == 'gathering'">
                            <v-icon>mdi-arrow-up-bold</v-icon>
                            {{ log.content.gathering.xp_gained }}
                        </div>
                        <div v-if="log.type == 'use'">
                            <div class="px-2 my-1" style="border: 1px solid white; border-radius: 3px; width: fit-content">{{ log.content.item }} x{{ log.content.quantity }}</div>
                        </div>
                        <div v-if="log.type == 'crafting'">
                            <div>
                                <v-icon>mdi-arrow-up-bold</v-icon>
                                {{ log.content.xp_gained }}
                            </div>
                        </div>
                        <div v-if="log.content.drops">
                            <div v-if="log.content.drops.gold">
                                <v-icon>mdi-cash</v-icon>
                                ${{ log.content.drops.gold }}
                            </div>
                        </div>
                      </div>
                      <div v-if="log.content.drops">
                          <div v-if="log.content.drops.items && log.content.drops.items.length > 0">
                              <div class="px-2 my-1" v-for="item in log.content.drops.items" :key="item.code" style="border: 1px solid white; border-radius: 3px; width: fit-content">
                                  {{ item.code }} x{{ item.quantity }}
                              </div>
                          </div>
                      </div>
                </v-card-item>
            </v-card>
        </div>
        <div v-else>No logs available</div>
    </div>
</template>

<script lang="ts">
import type { Log } from '@/ArtifactsTypes';
import { defineComponent, type PropType } from 'vue';

export default defineComponent({
    name: 'LogView',
    props: {
        logs: {
            type: Array as PropType<Log[]>,
            required: true,
        },
    },
    methods: {
        formatDate(dateString: string): string {
            const options: Intl.DateTimeFormatOptions = {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit',
            };
            return new Date(dateString).toLocaleString(undefined, options);
        },
        getCharacterHPFromFightLog(fightLog: string): number {
            const match = fightLog.match(/Character HP: (\d+)\/\d+/);

            if (match) {
                return Number.parseInt(match[1]); // Extracted Character HP value
            } else {
                return 0;
            }
        },
    },
});
</script>

<style scoped>
.log-item {
    padding-bottom: 8px;
}
.log-item:last-of-type {
    border-bottom: none;
}
</style>
