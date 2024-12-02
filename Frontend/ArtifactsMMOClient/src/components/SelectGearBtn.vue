<template>
  <div
    class="d-flex pa-3 armor-icon-container"
    style="border: 1px solid white; border-radius: 3px"
  >
    <!-- <v-icon ref="btn">{{ icon }}</v-icon> -->
    <img
      ref="btn"
      v-if="currentGear && currentGear.length > 0"
      width="30px"
      height="30px"
      :src="`https://artifactsmmo.com/images/items/${currentGear}.png`"
      alt=""
    />
    <div
      ref="btn"
      v-else
      class="text-caption"
      style="
        max-width: 30px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
      "
    >
      {{ slot }}
    </div>
    <v-dialog :activator="btn">
      <template v-slot:default="{ isActive }">
        <v-card>
          <v-card-title> {{ slot }} selection! </v-card-title>

          <v-card-item>
            <v-switch
              label="Only in bank"
              v-model:model-value="onlyInBank"
              color="primary"
            ></v-switch>
            <v-table density="compact" hover>
              <thead>
                <tr>
                  <th></th>
                  <th></th>
                  <th
                    colspan="4"
                    class="text-center"
                    style="border: 1px solid white; border-radius: 3px 0px 0 0"
                  >
                    <v-icon>mdi-sword</v-icon>
                  </th>
                  <th
                    colspan="4"
                    class="text-center"
                    style="border: 1px solid white; border-radius: 0px 3px 0 0"
                  >
                    <v-icon>mdi-shield</v-icon>
                  </th>
                </tr>
                <tr>
                  <th>code</th>
                  <th>
                    <HeartIcon style="width: 16px" />
                  </th>
                  <th style="border-left: 1px solid white">
                    <FireIcon style="width: 16px" />
                  </th>
                  <th>
                    <WaterIcon style="width: 16px" />
                  </th>
                  <th>
                    <EarthIcon style="width: 16px" />
                  </th>
                  <th style="border-right: 1px solid white">
                    <AirIcon style="width: 16px" />
                  </th>
                  <th style="border-left: 1px solid white">
                    <FireIcon style="width: 16px" />
                  </th>
                  <th>
                    <WaterIcon style="width: 16px" />
                  </th>
                  <th>
                    <EarthIcon style="width: 16px" />
                  </th>
                  <th style="border-right: 1px solid white">
                    <AirIcon style="width: 16px" />
                  </th>
                  <th>lvl</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="item in relevantGear" 
                  :key="item.code"
                  @click="selectItem(item.code, isActive)"
                  style="cursor: pointer;"
                >
                  <td
                    :style="{
                      backgroundColor:
                        item.code == currentGear ? 'green' : 'none',
                    }"
                  >
                    {{ item.code }}
                  </td>
                  <td
                    :style="{
                      backgroundColor:
                        item.code == currentGear ? 'green' : 'none',
                    }"
                  >
                    {{ item.hp }}
                  </td>
                  <td
                    style="border-left: 1px solid white"
                    :style="{
                      backgroundColor:
                        item.code == currentGear ? 'green' : 'none',
                    }"
                  >
                    {{ item.dmg_fire | item.attack_fire }}
                  </td>
                  <td
                    :style="{
                      backgroundColor:
                        item.code == currentGear ? 'green' : 'none',
                    }"
                  >
                    {{ item.dmg_water | item.attack_water }}
                  </td>
                  <td
                    :style="{
                      backgroundColor:
                        item.code == currentGear ? 'green' : 'none',
                    }"
                  >
                    {{ item.dmg_earth | item.attack_earth }}
                  </td>
                  <td
                    style="border-right: 1px solid white"
                    :style="{
                      backgroundColor:
                        item.code == currentGear ? 'green' : 'none',
                    }"
                  >
                    {{ item.dmg_air | item.attack_air }}
                  </td>
                  <td
                    style="border-left: 1px solid white"
                    :style="{
                      backgroundColor:
                        item.code == currentGear ? 'green' : 'none',
                    }"
                  >
                    {{ item.res_fire }}
                  </td>
                  <td
                    :style="{
                      backgroundColor:
                        item.code == currentGear ? 'green' : 'none',
                    }"
                  >
                    {{ item.res_water }}
                  </td>
                  <td
                    :style="{
                      backgroundColor:
                        item.code == currentGear ? 'green' : 'none',
                    }"
                  >
                    {{ item.res_earth }}
                  </td>
                  <td style="border-right: 1px solid white" :style="{backgroundColor: item.code == currentGear ? 'green' : 'none'}">
                    {{ item.res_air }}
                  </td>
                  <td
                    :style="{
                      backgroundColor:
                        item.code == currentGear ? 'green' : 'none',
                    }"
                  >
                    {{ item.level }}
                  </td>
                </tr>
              </tbody>
            </v-table>
          </v-card-item>

          <template v-slot:actions>
            <v-btn
              class="ml-auto"
              text="Close"
              @click="isActive.value = false"
            ></v-btn>
          </template>
        </v-card>
      </template>
    </v-dialog>
  </div>
</template>

<script setup>
import FireIcon from "./icons/FireIcon.vue";
import WaterIcon from "./icons/WaterIcon.vue";
import AirIcon from "./icons/AirIcon.vue";
import EarthIcon from "./icons/EarthIcon.vue";
import HeartIcon from "./icons/HeartIcon.vue";
import { useItemsStore } from "@/stores/items";
import { useBankStore } from "@/stores/bank";

const props = defineProps({
  icon: String,
  slot: String,
  currentGear: String,
});

const emit = defineEmits(['update:gear']);

const btn = ref(undefined);

const itemsStore = useItemsStore();
const bankStore = useBankStore();

const onlyInBank = ref(true);
const relevantGear = ref(loadGear());

function selectItem(code, isActive) {
  emit('update:gear', code);
  isActive.value = false;
}

function loadGear() {
  // Find all the gear for this slot in the items store
  const allGear = Object.values(itemsStore.items).filter(
    (item) => item.type == props.slot
  );
  // Make sure they are also present in the bank
  const result = allGear.filter(
    (item) =>
      !onlyInBank.value |
      bankStore.bank.some((bankItem) => item.code == bankItem.code)
  );
  console.log(onlyInBank.value);
  // return them as an array of formatted objects
  return result.map((item) => {
    const effects = {
      code: item.code,
      hp: 0,
      dmg_fire: 0,
      dmg_water: 0,
      dmg_earth: 0,
      dmg_air: 0,
      res_fire: 0,
      res_water: 0,
      res_earth: 0,
      res_air: 0,
      level: item.level,
    };
    item.effects.forEach((effect) => (effects[effect.name] = effect.value));
    return effects;
  });
}

watch(
  () => itemsStore.items,
  () => {
    relevantGear.value = loadGear();
  }
);
watch(
  () => bankStore.bank,
  () => {
    relevantGear.value = loadGear();
  }
);
watch(
  () => onlyInBank.value,
  () => {
    relevantGear.value = loadGear();
  }
);
</script>
