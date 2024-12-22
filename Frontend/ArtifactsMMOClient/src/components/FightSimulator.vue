<template>
    <v-card>
        <v-card-title class="d-flex justify-space-between">
            Fight Simulator
            <v-btn variant="outlined" density="compact" :icon="expanded ? 'mdi-chevron-up' : 'mdi-chevron-down'" @click="expanded = !expanded"></v-btn>
        </v-card-title>
        <v-card-item v-if="expanded">
            <div class="d-flex">
                <v-select
                    label="Character"
                    :items="charactersStore.characters.map((c) => c.name)"
                    variant="outlined"
                    density="compact"
                    hide-details
                    class="mr-3"
                    v-model:model-value="selectedCharacter"
                ></v-select>
                VS
                <v-select
                    label="Monster"
                    :items="monstersStore.monsters.map((m) => m.name)"
                    variant="outlined"
                    density="compact"
                    hide-details
                    class="ml-3"
                    v-model:model-value="selectedMonster"
                ></v-select>
            </div>
            <div class="d-flex flex-wrap justify-center align-center">
                <SelectGearBtn
                    class="ma-2"
                    icon="mdi-sword"
                    slot="weapon"
                    :current-gear="characterInfo ? charactersStore.activeGear[characterInfo.name]['weapon_slot'] : undefined"
                    @update:gear="updateGear('weapon_slot', $event)"
                />
                <SelectGearBtn
                    class="ma-2"
                    icon="mdi-shield"
                    slot="shield"
                    :current-gear="characterInfo ? charactersStore.activeGear[characterInfo.name]['shield_slot'] : undefined"
                    @update:gear="updateGear('shield_slot', $event)"
                />
                <SelectGearBtn
                    class="ma-2"
                    icon="mdi-hat-fedora"
                    slot="helmet"
                    :current-gear="characterInfo ? charactersStore.activeGear[characterInfo.name]['helmet_slot'] : undefined"
                    @update:gear="updateGear('helmet_slot', $event)"
                />
                <SelectGearBtn
                    class="ma-2"
                    icon="mdi-tshirt-crew-outline"
                    slot="body_armor"
                    :current-gear="characterInfo ? charactersStore.activeGear[characterInfo.name]['body_armor_slot'] : undefined"
                    @update:gear="updateGear('body_armor_slot', $event)"
                />
                <SelectGearBtn
                    class="ma-2"
                    icon="mdi-seat-legroom-extra"
                    slot="leg_armor"
                    :current-gear="characterInfo ? charactersStore.activeGear[characterInfo.name]['leg_armor_slot'] : undefined"
                    @update:gear="updateGear('leg_armor_slot', $event)"
                />
                <SelectGearBtn
                    class="ma-2"
                    icon="mdi-shoe-sneaker"
                    slot="boots"
                    :current-gear="characterInfo ? charactersStore.activeGear[characterInfo.name]['boots_slot'] : undefined"
                    @update:gear="updateGear('boots_slot', $event)"
                />
                <SelectGearBtn
                    class="ma-2"
                    icon="mdi-necklace"
                    slot="amulet"
                    :current-gear="characterInfo ? charactersStore.activeGear[characterInfo.name]['amulet_slot'] : undefined"
                    @update:gear="updateGear('amulet_slot', $event)"
                />
                <SelectGearBtn
                    class="ma-2"
                    icon="mdi-ring"
                    slot="ring"
                    :current-gear="characterInfo ? charactersStore.activeGear[characterInfo.name]['ring1_slot'] : undefined"
                    @update:gear="updateGear('ring1_slot', $event)"
                />
                <SelectGearBtn
                    class="ma-2"
                    icon="mdi-ring"
                    slot="ring"
                    :current-gear="characterInfo ? charactersStore.activeGear[characterInfo.name]['ring2_slot'] : undefined"
                    @update:gear="updateGear('ring2_slot', $event)"
                />
                <v-btn v-if="selectedCharacter" color="primary" @click="() => charactersStore.resetArmor(selectedCharacter)">
                    Reset
                </v-btn>
            </div>
            <div class="pa-5 my-3" style="border: 1px solid white; border-radius: 3px" v-if="characterInfo && characterWeaponAttackInfo">
                <v-row>
                    <v-icon> mdi-sword </v-icon>
                    {{ selectedCharacter }} damage:
                </v-row>
                <v-row v-for="element in ['fire', 'water', 'earth', 'air'].filter((e) => characterWeaponAttackInfo && characterWeaponAttackInfo[e] > 0)">
                    <v-col>
                        <FireIcon v-if="element == 'fire'" style="width: 20px" />
                        <EarthIcon v-if="element == 'earth'" style="width: 20px" />
                        <AirIcon v-if="element == 'air'" style="width: 20px" />
                        <WaterIcon v-if="element == 'water'" style="width: 20px" />
                    </v-col>
                    <v-col>
                        {{ characterWeaponAttackInfo ? characterWeaponAttackInfo[element] : '-' }}
                    </v-col>
                    <v-col> X </v-col>
                    <v-col>
                        {{ characterArmorStats ? Math.floor((1 + characterArmorStats['dmg_' + element] / 100) * 100) / 100 : '-' }}
                    </v-col>
                    <v-col> X </v-col>
                    <v-col>
                        {{ monsterInfo ? Math.floor((1 - monsterInfo['res_' + element] / 100) * 100) / 100 : '-' }}
                    </v-col>
                    <v-col> = </v-col>
                    <v-col>
                        {{ characterInfo && monsterInfo ? characterElementAttack(element) : '-' }}
                    </v-col>
                </v-row>
            </div>

            <div class="pa-5 my-3" style="border: 1px solid white; border-radius: 3px" v-if="monsterInfo">
                <v-row>
                    <v-icon> mdi-shield </v-icon>
                    {{ selectedMonster }} damage:
                </v-row>
                <v-row v-for="element in ['fire', 'water', 'earth', 'air'].filter((e) => monsterInfo['attack_' + e] > 0)">
                    <v-col>
                        <FireIcon v-if="element == 'fire'" style="width: 20px" />
                        <EarthIcon v-if="element == 'earth'" style="width: 20px" />
                        <AirIcon v-if="element == 'air'" style="width: 20px" />
                        <WaterIcon v-if="element == 'water'" style="width: 20px" />
                    </v-col>
                    <v-col>
                        {{ monsterInfo ? monsterInfo[('attack_' + element) as keyof Monster] : '-' }}
                    </v-col>
                    <v-col> X </v-col>
                    <v-col>
                        {{ characterArmorStats ? Math.floor((1 - characterArmorStats['res_' + element] / 100) * 100) / 100 : '-' }}
                    </v-col>
                    <v-col> = </v-col>
                    <v-col>
                        {{ characterInfo && monsterInfo ? monsterElementAttack(element) : '-' }}
                    </v-col>
                </v-row>
            </div>
            <div
                class="pa-5 my-3"
                style="border: 1px solid white; border-radius: 3px; text-align: center"
                v-if="monsterInfo && characterInfo && winner"
                :style="{
                    backgroundColor: winner.winner == selectedCharacter ? 'green' : 'red',
                }"
            >
                {{ winner.winner }} won after {{ winner.rounds }} rounds with {{ Math.floor(winner.winnerHealth) }} hp remaining
            </div>
        </v-card-item>
    </v-card>
</template>

<script setup lang="ts">
import { useCharacterStore } from '@/stores/characters';
import { useMonsterStore } from '@/stores/monsters';
import EarthIcon from './icons/EarthIcon.vue';
import FireIcon from './icons/FireIcon.vue';
import WaterIcon from './icons/WaterIcon.vue';
import AirIcon from './icons/AirIcon.vue';
import type { Character, CharacterName, GearSlot, Monster } from '@/ArtifactsTypes';
import { useItemsStore } from '@/stores/items';

const charactersStore = useCharacterStore();
const monstersStore = useMonsterStore();
const itemsStore = useItemsStore();

const selectedCharacter = ref(undefined as CharacterName | undefined);
const selectedMonster = ref(undefined as string | undefined);

const expanded = ref(false)

const characterInfo = computed(() => {
    return charactersStore.characters.find((c) => c.name == selectedCharacter.value);
});

const monsterInfo = computed(() => {
    return monstersStore.monsters.find((m) => m.name == selectedMonster.value);
});

const characterWeaponAttackInfo = computed(() => {
    if (characterInfo.value) {
        const weapon_item = itemsStore.getItem(charactersStore.activeGear[characterInfo.value.name]['weapon_slot']);
        if (weapon_item) {
            return {
                fire: weapon_item['effects'].find((e) => e.name == 'attack_fire')?.value,
                water: weapon_item['effects'].find((e) => e.name == 'attack_water')?.value,
                earth: weapon_item['effects'].find((e) => e.name == 'attack_earth')?.value,
                air: weapon_item['effects'].find((e) => e.name == 'attack_air')?.value,
            } as { [key: string]: number };
        }
    }
});
const characterArmorStats = computed(() => {
    if (characterInfo.value) {
        const result = {
            dmg_fire: 0,
            dmg_water: 0,
            dmg_earth: 0,
            dmg_air: 0,
            res_fire: 0,
            res_water: 0,
            res_earth: 0,
            res_air: 0,
        };
        const armorSlots: GearSlot[] = ['shield_slot', 'helmet_slot', 'body_armor_slot', 'leg_armor_slot', 'boots_slot', 'ring1_slot', 'ring2_slot', 'amulet_slot'];
        armorSlots.forEach((slot) => {
            if (characterInfo.value) {
                const slot_item = itemsStore.getItem(charactersStore.activeGear[characterInfo.value.name][slot]);
                let dmg_fire = slot_item.effects.find((e) => e.name == 'dmg_fire')?.value;
                let dmg_water = slot_item.effects.find((e) => e.name == 'dmg_water')?.value;
                let dmg_earth = slot_item.effects.find((e) => e.name == 'dmg_earth')?.value;
                let dmg_air = slot_item.effects.find((e) => e.name == 'dmg_air')?.value;
                result.dmg_fire += dmg_fire ? dmg_fire : 0;
                result.dmg_water += dmg_water ? dmg_water : 0;
                result.dmg_earth += dmg_earth ? dmg_earth : 0;
                result.dmg_air += dmg_air ? dmg_air : 0;

                let res_fire = slot_item.effects.find((e) => e.name == 'res_fire')?.value;
                let res_water = slot_item.effects.find((e) => e.name == 'res_water')?.value;
                let res_earth = slot_item.effects.find((e) => e.name == 'res_earth')?.value;
                let res_air = slot_item.effects.find((e) => e.name == 'res_air')?.value;

                result.res_fire += res_fire ? res_fire : 0;
                result.res_water += res_water ? res_water : 0;
                result.res_earth += res_earth ? res_earth : 0;
                result.res_air += res_air ? res_air : 0;
            }
        });
        return result as { [key: string]: number };
    }
});

function updateGear(slot: string, gearCode: string) {
    console.log('Updating', slot, gearCode);
    if (characterInfo.value) {
        charactersStore.updateCharacterGear(characterInfo.value.name, slot as GearSlot, gearCode);
    }
}

const characterElementAttack = (element: string) => {
    if (characterWeaponAttackInfo.value && characterArmorStats.value && monsterInfo.value) {
        return Math.floor(characterWeaponAttackInfo.value[element] * (1 + characterArmorStats.value['dmg_' + element] / 100) * (1 - monsterInfo.value['res_' + element] / 100) * 100) / 100;
    }
};

const characterTotalAttack = computed(() => {
    const fire = characterElementAttack('fire');
    const water = characterElementAttack('water');
    const air = characterElementAttack('air');
    const earth = characterElementAttack('earth');
    return (fire ? fire : 0) + (water ? water : 0) + (air ? air : 0) + (earth ? earth : 0);
});

const monsterElementAttack = (element: string) => {
    if (characterArmorStats.value && monsterInfo.value) {
        return Math.floor(monsterInfo.value['attack_' + element] * (1 - characterArmorStats.value['res_' + element] / 100) * 100) / 100;
    }
};

const monsterTotalAttack = computed(() => {
    const fire = monsterElementAttack('fire');
    const water = monsterElementAttack('water');
    const air = monsterElementAttack('air');
    const earth = monsterElementAttack('earth');
    return (fire ? fire : 0) + (water ? water : 0) + (air ? air : 0) + (earth ? earth : 0);
});

const winner = computed(() => {
    if (selectedCharacter.value == undefined || selectedMonster.value == undefined) {
        return;
    }
    if (monsterInfo.value && characterInfo.value) {
        let monster_hp = monsterInfo.value.hp;
        let character_hp = characterInfo.value.max_hp;
        let fightWinner = selectedMonster.value;
        let rounds = 0;
        let logs = [];
        for (let i = 0; i < 100; i++) {
            monster_hp -= characterTotalAttack.value;
            logs.push(`character does ${characterTotalAttack.value} dmg`);
            if (monster_hp <= 0) {
                fightWinner = selectedCharacter.value;
                break;
            }

            character_hp -= monsterTotalAttack.value;
            logs.push(`monster does ${monsterTotalAttack.value} dmg`);
            if (character_hp <= 0) {
                fightWinner = selectedMonster.value;
                break;
            }
            rounds++;
        }
        return {
            winner: fightWinner,
            rounds,
            winnerHealth: fightWinner == selectedMonster.value ? monster_hp : character_hp,
            logs,
        };
    }
});
</script>

<style scoped>
.armor-icon-container {
    transition: all;
    transition-duration: 0.2s;
}
.armor-icon-container:hover {
    background-color: gray;
    cursor: pointer;
}
</style>
