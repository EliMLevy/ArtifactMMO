<template>
  <v-card class="pa-3">
    <v-card-title>
        Bank ({{ bankStore.bank.length }})
        <v-btn icon="mdi-refresh" density="compact" variant="outlined" style="float: right" :loading="bankStore.loading" @click="bankStore.getBank()"></v-btn>
    </v-card-title>
    <!-- Search Bar -->
    <v-text-field
      v-model="searchQuery"
      label="Search Items"
      clearable
      variant="outlined"
      class="px-4"
    />

    <!-- Bank Items List -->
    <v-list style="max-height: 400px; border-radius: 4px">
      <div v-if="filteredBankItems.length">
        <v-list-item v-for="item in filteredBankItems" :key="item.code">
            <v-list-item-title>{{ item.code }}</v-list-item-title>
            <v-list-item-subtitle>Quantity: {{ item.quantity }}</v-list-item-subtitle>
        </v-list-item>
      </div>

      <v-list-item v-else>
          <v-list-item-title>No items found</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-card class="pa-3">
</template>

<script lang="ts">
import { defineComponent, computed, ref, onMounted } from "vue";
import { useBankStore } from "@/stores/bank";

export default defineComponent({
  name: "BankViewer",
  setup() {
    const bankStore = useBankStore();
    const searchQuery = ref(""); // Search query state

    // Fetch bank items when the component is mounted
    onMounted(() => {
      bankStore.getBank();
    });

    // Computed property for filtering bank items
    const filteredBankItems = computed(() => {
      return bankStore.bank.filter(item =>
        item.code.toLowerCase().includes(searchQuery.value.toLowerCase())
      );
    });

    return {
        bankStore,
      searchQuery,
      filteredBankItems,
    };
  },
});
</script>

<style scoped>
/* Optional: Add some styling */
</style>
