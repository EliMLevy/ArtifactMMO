<template>
    <v-card>
        <v-card-title>Grand Exchange
            <v-btn variant="outlined" density="compact" style="float: right"  :icon="expanded ? 'mdi-chevron-up' : 'mdi-chevron-down'" @click="expanded = !expanded"></v-btn>
            <v-btn icon="mdi-refresh" density="compact" variant="outlined" style="float: right" :loading="marketStore.loading" @click="marketStore.getMarket()"></v-btn>
        </v-card-title>
        <v-card-text v-if="expanded" style="max-height: 500px; overflow-y: auto;">
            <v-row>
                <v-col>ID</v-col>
                <v-col>Item</v-col>
                <v-col>Price Per Item</v-col>
                <v-col>Quantity</v-col>
            </v-row>
            <div v-if="!marketStore.loading">
                <v-row v-for="row in processedMarketData" :key="row.code">
                    <v-col>{{ row.id }}</v-col>
                    <v-col>{{ row.code }}</v-col>
                    <v-col>{{ Math.floor(row.price / row.quantity * 100)/100 }}</v-col>
                    <v-col>{{ row.quantity }}</v-col>
                </v-row>
            </div>
            <div v-else>
                Loading...
            </div>

        </v-card-text>
    </v-card>
</template>

<script setup lang="ts">
import type { MarketListing } from '@/ArtifactsTypes';
import { useMarketStore } from '@/stores/market';

const expanded = ref(false)

const marketStore = useMarketStore()
marketStore.getMarket()

const processedMarketData = computed(() => {
    const map = {} as {[key: string]: MarketListing}
    marketStore.market.forEach(order => {
        const costPer = order.price / order.quantity
        if(!map[order.code]) {
            map[order.code] = order;  
        } else {
            if(map[order.code].price / map[order.code].quantity > costPer) {
                map[order.code] = order
            }
        }
    })
    return Object.values(map).sort((a, b) => (a.price/a.quantity) - (b.price/b.quantity))
})

</script>