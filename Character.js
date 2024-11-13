import { attack, collect, craft, getCharacter, rest, move, depositItem, useItem, getMap, getResource } from "./BaseActions.js";
import { delay } from "./Util.js";
import { copper, bank, gudgeonFishing, ashForest, chicken, miningWorkshop } from "./index.js";

/**
 * States:
 * - "idle"
 * - "move" x: number, y: number
 * - attack
 * - rest
 * - collect
 * - craft code: string, quantity: number
 * - use item code: string, quantity: number
 * - deposit all
 * - all actions loop
 */

export default class Character {
    constructor(name) {
        this.name = name;
        this.defaultState = { state: "all actions loop" };
        this.currentState = undefined;
        this.pendingActions = [];
        this.characterState = undefined;
        this.currentMap = undefined;
        this.skills = {
            mining_level: undefined,
            woodcutting_level: undefined,
            fishing_level: undefined,
        };
    }

    async actionLoop() {
        while (true) {
            this.characterState = await getCharacter(this.name);
            if(!this.currentMap) {
                this.currentMap = await getMap(this.characterState.data.x, this.characterState.data.y)
            }
            this.skills.mining_level = this.characterState.data.mining_level;
            this.skills.woodcutting_level = this.characterState.data.woodcutting_level;
            this.skills.fishing_level = this.characterState.data.fishing_level;
            if (this.characterState.data.hp < 50) {
                this.currentState = { state: "rest" };
            }
            if (this.characterState.data.cooldown && new Date() < new Date(this.characterState.data.cooldown_expiration)) {
                console.log(this.name, "cooldown for ", this.characterState.data.cooldown, " sec");
                await delay(this.characterState.data.cooldown * 1000);
            }

            if (!this.currentState) {
                // Try to take a state from the pending actions
                if (this.pendingActions.length > 0) {
                    this.currentState = this.pendingActions.shift();
                } else {
                    // Otherwise default to default state
                    this.currentState = this.defaultState;
                }
            }
            try {
                // Do the action
                console.log(this.name, "Next state: ", this.currentState);
                let result;
                switch (this.currentState.state) {
                    case "idle":
                        await delay(3000);
                        break;
                    case "move":
                        result = await move(this.name, this.currentState.x, this.currentState.y);
                        this.currentMap = undefined;                        
                        break;
                    case "attack":
                        result = await attack(this.name);
                        break;
                    case "rest":
                        result = await rest(this.name);
                        break;
                    case "collect":
                        result = await collect(this.name);
                        break;
                    case "craft":
                        result = await craft(this.name, this.currentState.code, this.currentState.quantity);
                        break;
                    case "use item":
                        result = await useItem(this.name, this.currentState.code, this.currentState.quantity);
                        break;
                    case "deposit all":
                        for (let i = 0; i < this.characterState.data.inventory.length; i++) {
                            const slot = this.characterState.data.inventory[i];
                            if (slot && slot.quantity > 0) {
                                console.log("Depositing:", slot);
                                const depositResult = await depositItem(this.name, slot);
                                if (depositResult) {
                                    await delay(depositResult.data.cooldown.remaining_seconds * 1000);
                                }
                            }
                        }
                        break;
                    case "all actions loop":
                        this.allActionLoop();
                        break;
                    case "autopilot":
                        // if we have low health -> rest
                        if (this.characterState.data.hp / this.characterState.data.max_hp < 0.8) {
                            this.addToActionQueue({ state: "rest" });
                            break;
                        }
                        // if we have a lot of items in our inventory -> drop items off
                        const itemsInInventory = this.characterState.data.inventory.reduce((part, a) => part + a, 0);
                        if (itemsInInventory / this.characterState.data.inventory_max_items > 0.8) {
                            this.addToActionQueue({ state: "move", ...bank });
                            this.addToActionQueue({ state: "deposit all" });
                            break;
                        }
                        // If we are on a map that has a resource, see if this skill needs more advancement.
                        // A skill needs more advancement if there is no skill >2 points behind
                        if(this.currentMap && this.currentMap.data.content && this.currentMap.data.content.type === "resource") {
                            const targetSkill = getResource(this.currentMap.data.content.code).data.skill;
                            // There is no skill that is more than 3 behind the targetSkill
                            if(Object.keys(this.skills).every(skill => this.skills[skill] + 3 > this.skills[targetSkill])) {
                                this.addToActionQueue({ state: "collect" });
                                break;
                            }
                        }

                        // Find the lowest skill and do that action
                        const lowestSkill = Object.keys(this.skills).sort((a, b) => this.skills[a] - this.skills[b]);
                        this.performSkill(lowestSkill);
                        break;
                    default:
                        break;
                }
                if (result) {
                    if (result.error) {
                        if (result.error.code == 499) {
                            this.pendingActions.unshift(this.currentState);
                        }
                    }
                }
                this.currentState = undefined;
            } catch (error) {
                console.log("Error: failed to complete state: ", this.currentState.state, error);
            }
        }
    }

    addToActionQueue(action) {
        this.pendingActions.push(action);
    }

    mineCopperLoop() {
        this.addToActionQueue({ state: "move", ...copper });
        for (let i = 0; i < 64; i++) {
            this.addToActionQueue({ state: "collect" });
        }
        this.addToActionQueue({ state: "move", ...miningWorkshop });
        this.addToActionQueue({ state: "craft", code: "copper", quantity: 8 });
        this.addToActionQueue({ state: "move", ...bank });
        this.addToActionQueue({ state: "deposit all" });
    }

    fishGudgeonLoop() {
        this.addToActionQueue({ state: "move", ...gudgeonFishing });
        for (let i = 0; i < 50; i++) {
            this.addToActionQueue({ state: "collect" });
        }
        this.addToActionQueue({ state: "move", ...bank });
        this.addToActionQueue({ state: "deposit all" });
    }

    chopAshwoodLoop() {
        this.addToActionQueue({ state: "move", ...ashForest });
        for (let i = 0; i < 50; i++) {
            this.addToActionQueue({ state: "collect" });
        }
        this.addToActionQueue({ state: "move", ...bank });
        this.addToActionQueue({ state: "deposit all" });
    }

    attackChickenLoop() {
        this.addToActionQueue({ state: "move", ...chicken });
        for (let i = 0; i < 50; i++) {
            this.addToActionQueue({ state: "attack" });
            this.addToActionQueue({ state: "rest" });
        }
        this.addToActionQueue({ state: "move", ...bank });
        this.addToActionQueue({ state: "deposit all" });
    }
    allActionLoop() {
        const actions = [() => this.mineCopperLoop(), () => this.chopAshwoodLoop(), () => this.fishGudgeonLoop(), () => this.attackChickenLoop()];

        actions
            .sort(() => Math.random() - 0.5) // Randomize the order of actions
            .forEach((action) => action()); // Execute each action in the new random order
    }

    performSkill(skill) {
        switch (skill) {
            case "mining_level":
            case "mining":
                this.addToActionQueue({ state: "move", ...copper });
                break;
            case "woodcutting_level":
            case "woodcutting":
                this.addToActionQueue({ state: "move", ...ashForest });
                break;
            case "fishing_level":
            case "fishing":
                this.addToActionQueue({ state: "move", ...gudgeonFishing });
                break;
            default:
                break;
        }
        this.addToActionQueue({ state: "collect" });
    }
}
