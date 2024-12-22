<template>
    <v-card>
        <v-card-title>
            Current Events
            <v-btn variant="outlined" density="compact" style="float: right;" :icon="expanded ? 'mdi-chevron-up' : 'mdi-chevron-down'" @click="expanded = !expanded"></v-btn>
            <v-btn icon="mdi-refresh" density="compact" variant="outlined" style="float: right" :loading="eventsStore.loading" @click="eventsStore.getEvents()"></v-btn>

        </v-card-title>
        <v-card-text v-if="expanded">
            <v-card v-for="event in eventsStore.events">
                <v-card-title>
                    {{ event.name }}
                    <div style="display: inline; float: right;">
                        <v-icon>mdi-clock</v-icon>
                        {{ Math.floor((Math.abs(new Date(event.expiration) - new Date()) / 1000) / 60) }} min
                    </div>
                </v-card-title>
                <v-card-text>
                    <div>
                        Location: ({{ event.map.x }},{{ event.map.y }})
                    </div>
                    <div>
                        Content: {{ event.map.content.code }}
                    </div>
                    <div>
                        Expires: {{ new Date(event.expiration) }}
                    </div>
                </v-card-text>
            </v-card>
        </v-card-text>
        
    </v-card>
</template>

<script lang="ts" setup>
import { useEventsStore } from '@/stores/events';


const expanded = ref(false)

const eventsStore = useEventsStore()
eventsStore.getEvents()

</script>