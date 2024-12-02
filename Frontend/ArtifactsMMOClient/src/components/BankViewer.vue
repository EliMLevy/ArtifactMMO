<template>
    <v-card class="pa-3">
        <v-card-title>
            Bank ({{ bankStore.bank.length }})
            <v-btn icon="mdi-refresh" density="compact" variant="outlined" style="float: right" :loading="bankStore.loading" @click="bankStore.getBank()"></v-btn>
        </v-card-title>
        <!-- Search Bar -->
        <v-text-field v-model="searchQuery" label="Search Items" clearable variant="outlined" class="px-4" />

        <v-btn v-for="tag in tags" :key="tag" class="ma-1" :color="selectedTag == tag ? 'primary' : ''" :style="{ border: '1px solid white' }" @click="selectedTag == tag ? selectedTag = undefined : selectedTag = tag">{{ tag }}</v-btn>

        <!-- Bank Items List -->
        <v-list style="max-height: 400px; border-radius: 4px">
            <div v-if="filteredBankItems.length">
                <v-list-item v-for="item in filteredBankItems" :key="item.code">
                    <v-list-item-title>{{ item.code }}</v-list-item-title>
                    <v-list-item-subtitle>Quantity: {{ item.quantity }}</v-list-item-subtitle>
                    <v-list-item-subtitle>Type: {{ item.type }}</v-list-item-subtitle>
                </v-list-item>
            </div>

            <v-list-item v-else>
                <v-list-item-title>No items found</v-list-item-title>
            </v-list-item>
        </v-list>
    </v-card>
</template>

<script lang="ts">
import { defineComponent, computed, ref, onMounted } from 'vue';
import { useBankStore } from '@/stores/bank';
import { useItemsStore } from '@/stores/items';

export default defineComponent({
    name: 'BankViewer',
    setup() {
        const bankStore = useBankStore();
        const itemStore = useItemsStore();
        const searchQuery = ref(''); // Search query state
        const tags = ref(new Set() as Set<string>);
        const selectedTag = ref(undefined as string | undefined);

        // Fetch bank items when the component is mounted
        onMounted(() => {
            bankStore.getBank();
        });

        // Computed property for filtering bank items
        const filteredBankItems = computed(() => {
            return taggedBankItems.value
              .filter((item) => (item && item.code ? item.code.toLowerCase().includes(searchQuery.value ? searchQuery.value.toLowerCase() : '') : true))
              .filter((item) => !selectedTag.value || item.type == selectedTag.value)
        });
        const taggedBankItems = computed(() => {
            return bankStore.bank.map((item) => {
                const type = itemStore.getItem(item.code).type;
                tags.value.add(type);
                return {
                    ...item,
                    type,
                };
            });
        });

        return {
            bankStore,
            searchQuery,
            filteredBankItems,
            tags,
            selectedTag,
        };
    },
});
</script>

<style scoped>
/* Optional: Add some styling */
</style>
