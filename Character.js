import { attack, collect, craft, getCharacter, rest, move, depositItem, useItem } from "./BaseActions.js";
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
 */

export default class Character {
    constructor(name) {
        this.name = name;
        this.defaultState = { state: "idle" };
        this.currentState = undefined;
        this.pendingActions = [];
        this.characterState = undefined;
        this.isLooping = false;
    }

    async actionLoop() {
        while (true) {
            this.characterState = await getCharacter(this.name);
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
                    if (this.isLooping) {
                        this.pendingActions.push(this.currentState);
                    }
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
        this.isLooping = true;
    }

    fishGudgeonLoop() {
        this.addToActionQueue({ state: "move", ...gudgeonFishing });
        for (let i = 0; i < 50; i++) {
            this.addToActionQueue({ state: "collect" });
        }
        this.addToActionQueue({ state: "move", ...bank });
        this.addToActionQueue({ state: "deposit all" });
        this.isLooping = true;
    }

    chopAshwoodLoop() {
        this.addToActionQueue({ state: "move", ...ashForest });
        for (let i = 0; i < 50; i++) {
            this.addToActionQueue({ state: "collect" });
        }
        this.addToActionQueue({ state: "move", ...bank });
        this.addToActionQueue({ state: "deposit all" });
        this.isLooping = true;
    }

    attackChickenLoop() {
        this.addToActionQueue({ state: "move", ...chicken });
        for (let i = 0; i < 50; i++) {
            this.addToActionQueue({ state: "attack" });
            this.addToActionQueue({ state: "rest" });
        }
        this.addToActionQueue({ state: "move", ...bank });
        this.addToActionQueue({ state: "deposit all" });
        this.isLooping = true;
    }
    allActionLoop() {
        const actions = [() => this.mineCopperLoop(), () => this.chopAshwoodLoop(), () => this.fishGudgeonLoop(), () => this.attackChickenLoop()];

        actions
            .sort(() => Math.random() - 0.5) // Randomize the order of actions
            .forEach((action) => action()); // Execute each action in the new random order
    }
}
