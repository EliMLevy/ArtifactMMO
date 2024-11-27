<template>
  <div class="w-100">
    <div class="mx-1">
      <v-select
        label="Action"
        :items="Object.keys(options)"
        hide-details
        density="compact"
        variant="outlined"
        class="ml-2"
        v-model:model-value="selected.action"
      ></v-select>
    </div>

    <div v-if="selected.action && options[selected.action].suboptions">
      <div
        class="mx-1 my-3"
        v-for="option in Object.keys(options[selected.action].suboptions)"
      >
        <v-select
          :label="option"
          v-if="options[selected.action].suboptions[option] instanceof Array"
          :items="options[selected.action].suboptions[option]"
          hide-details
          density="compact"
          variant="outlined"
          class="ml-2"
          v-model:model-value="selected[option]"
        ></v-select>
        <v-text-field
          v-else
          :label="option"
          hide-details
          density="compact"
          variant="outlined"
          class="ml-2"
          v-model:model-value="selected[option]"
        ></v-text-field>
      </div>
    </div>

    <div class="ma-3 w-100 d-flex justify-center">
      <v-btn
        append-icon="mdi-clipboard-text-outline"
        color="primary"
        @click="handleCopy"
      >
        Copy
      </v-btn>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useItemsStore } from "@/stores/items";
import { useMonsterStore } from "@/stores/monsters";

const props = defineProps({
  characterName: String,
});

const monstersStore = useMonsterStore();
const itemsStore = useItemsStore();

const options = {
  "Deposit inventory": {
    action: "deposit all",
    title: "Deposit inventory",
  },
  Idle: {
    action: "idle",
    "title": "idle",
  },
  Attack: {
    action: "attack",
    title: "Attack",
    suboptions: {
      Monster: [] as string[],
    },
  },
  Craft: {
    action: "craft",
    title: "Craft",
    suboptions: {
      Item: [] as string[],
      Quantity: "number",
    },
  },
  Collect: {
    action: "collect",
    title: "Collect",
    suboptions: {
      Resource: [] as string[],
    },
  },
  Train: {
    action: "train",
    title: "Train",
    suboptions: {
        Type: ["mining", "woodcutting", "fishing", "lowest"]
    }
  },
  Tasks: {
    action: "tasks",
    title: "Tasks",
    suboptions: {
        Type: ["monsters", "items"]
    }
  }
};

onMounted(async () => {
  await monstersStore.loadMonsters();
  options["Attack"].suboptions.Monster = monstersStore.monsters
    .map((m) => m.code)
    .sort();

  await itemsStore.loadItems();
  options["Craft"].suboptions.Item = Object.values(itemsStore.items)
    .map((m) => m.code)
    .sort();

  options["Collect"].suboptions.Resource = Object.values(itemsStore.items)
    .filter((i) => i.type == "resource" && i.recipe == undefined)
    .map((m) => m.code)
    .sort();
});

let selected = ref({} as any);

function handleCopy() {
  if (selected.value.action) {
    let cmd = "";
    if (selected.value.action == "Deposit inventory") {
      cmd = `python3 character_api_cli.py submit-plan  ${props.characterName} "[{'action':'deposit all'}]"`;
    } else if (selected.value.action == "Idle") {
      cmd = `python3 character_api_cli.py set-default  ${props.characterName} idle`;
    } else if (selected.value.action == "Attack" && selected.value.Monster) {
      cmd = `python3 character_api_cli.py set-default  ${props.characterName} attack ${selected.value.Monster}`;
    } else if (
      selected.value.action == "Craft" &&
      selected.value.Item &&
      selected.value.Quantity
    ) {
      cmd = `python3 character_api_cli.py submit-plan ${props.characterName} "[{'action': 'plan and craft', 'code': '${selected.value.Item}', 'quantity': ${selected.value.Quantity}}]"`;
    } else if(selected.value.action == "Collect" &&  selected.value.Resource) {
        cmd = `python3 character_api_cli.py set-default ${props.characterName} collect ${selected.value.Resource}`;
    } else if(selected.value.action == "Train" && selected.value.Type) {
        if(selected.value.Type == "lowest") {
            cmd = `python3 character_api_cli.py set-default ${props.characterName} "collect loop"`;
        } else {
            cmd = `python3 character_api_cli.py set-default ${props.characterName} collecting ${selected.value.Type} `;
        }
    } else if(selected.value.action == "Tasks" && selected.value.Type) {
        cmd = `python3 character_api_cli.py set-default ${props.characterName} tasks ${selected.value.Type} `;
    }
    console.log("Copying", cmd);
    navigator.clipboard.writeText(cmd);
  }
}
</script>
