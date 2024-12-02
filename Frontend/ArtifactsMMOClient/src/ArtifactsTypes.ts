export type CharacterName = "Bobby" | "Stuart" | "George" | "Tim" | "Joe";

export interface InventoryItem {
  slot: number;
  code: string;
  quantity: number;
}

export interface Character {
  name: CharacterName;
  account: string;
  skin: string;
  level: number;
  xp: number;
  max_xp: number;
  gold: number;
  speed: number;
  mining_level: number;
  mining_xp: number;
  mining_max_xp: number;
  woodcutting_level: number;
  woodcutting_xp: number;
  woodcutting_max_xp: number;
  fishing_level: number;
  fishing_xp: number;
  fishing_max_xp: number;
  weaponcrafting_level: number;
  weaponcrafting_xp: number;
  weaponcrafting_max_xp: number;
  gearcrafting_level: number;
  gearcrafting_xp: number;
  gearcrafting_max_xp: number;
  jewelrycrafting_level: number;
  jewelrycrafting_xp: number;
  jewelrycrafting_max_xp: number;
  cooking_level: number;
  cooking_xp: number;
  cooking_max_xp: number;
  alchemy_level: number;
  alchemy_xp: number;
  alchemy_max_xp: number;
  hp: number;
  max_hp: number;
  haste: number;
  critical_strike: number;
  stamina: number;
  attack_fire: number;
  attack_earth: number;
  attack_water: number;
  attack_air: number;
  dmg_fire: number;
  dmg_earth: number;
  dmg_water: number;
  dmg_air: number;
  res_fire: number;
  res_earth: number;
  res_water: number;
  res_air: number;
  x: number;
  y: number;
  cooldown: number;
  cooldown_expiration: string;
  weapon_slot: string;
  shield_slot: string;
  helmet_slot: string;
  body_armor_slot: string;
  leg_armor_slot: string;
  boots_slot: string;
  ring1_slot: string;
  ring2_slot: string;
  amulet_slot: string;
  artifact1_slot: string;
  artifact2_slot: string;
  artifact3_slot: string;
  utility1_slot: string;
  utility1_slot_quantity: number;
  utility2_slot: string;
  utility2_slot_quantity: number;
  task: string;
  task_type: string;
  task_progress: number;
  task_total: number;
  inventory_max_items: number;
  inventory: InventoryItem[];
}

export interface GearSetup {
  weapon_slot: string;
  shield_slot: string;
  helmet_slot: string;
  body_armor_slot: string;
  leg_armor_slot: string;
  boots_slot: string;
  ring1_slot: string;
  ring2_slot: string;
  amulet_slot: string;
  artifact1_slot: string;
  artifact2_slot: string;
  artifact3_slot: string;
}

export interface LogContent {
  item?: string; // For "deposit" type logs
  quantity?: number; // For "deposit" type logs
  gathering?: {
    skill: string;
    resource: string;
    xp_gained: number;
  }; // For "gathering" type logs
  drops?: {
    items: {
      code: string;
      quantity: number;
    }[];
  }; // For "gathering" type logs
}

export interface Log {
  character: string;
  account: string;
  type: "deposit" | "gathering"; // Restrict to known types
  description: string;
  content: LogContent;
  cooldown: number;
  cooldown_expiration: string; // ISO 8601 date-time string
  created_at: string; // ISO 8601 date-time string
}

export interface Item {
  code: string;
  quantity: number;
}

export interface Drop {
  code: string;
  rate: number;
  min_quantity: number;
  max_quantity: number;
}

export interface Monster {
  name: string;
  code: string;
  level: number;
  hp: number;
  attack_fire: number;
  attack_earth: number;
  attack_water: number;
  attack_air: number;
  res_fire: number;
  res_earth: number;
  res_water: number;
  res_air: number;
  min_gold: number;
  max_gold: number;
  drops: Drop[];
}

export interface ItemEffect {
  name: string;
  value: number;
}

export interface RecipeItem {
  code: string;
  quantity: number;
}

export interface Recipe {
  skill: string;
  level: number;
  items: RecipeItem[];
}

export interface Item {
  code: string;
  level: number;
  type: string;
  effects: ItemEffect[];
  recipe?: Recipe;
}

export interface Content {
  type: "resource" | "monster" | "workshop" | "bank" | "tasks_master";
  code: string;
}

export interface MapEntry {
  x: number;
  y: number;
  content: Content;
}

export type GearSlot =
    | 'weapon_slot'
    | 'shield_slot'
    | 'helmet_slot'
    | 'body_armor_slot'
    | 'leg_armor_slot'
    | 'boots_slot'
    | 'ring1_slot'
    | 'ring2_slot'
    | 'amulet_slot'
    | 'artifact1_slot'
    | 'artifact2_slot'
    | 'artifact3_slot';
