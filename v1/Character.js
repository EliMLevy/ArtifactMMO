import { attack, collect, craft, getCharacter, rest, move, depositItem, useItem, getMap, getResource, withdrawItem, equip, recycle } from "./BaseActions.js";
import { delay } from "./Util.js";
import { bank, gudgeonFishing, spruceForest, iron } from "./index.js";

const INVENTORY_THRESHOLD = 0.9;
const LOW_HP_THRESHOLD = 0.5;
const IDLE_DELAY = 3000;
const ANTI_THROTTLING_DELAY = 500;
/**
 * Encapsulates actions and character state handling in an improved, modular way.
 */
export default class Character {
    constructor(name) {
        this.name = name;
        this.defaultState = { state: "autopilot" };
        this.currentState = undefined;
        this.lastState = undefined;
        this.oldLocation = undefined; // If we need to drop off and rememeber where to return to
        this.pendingActions = [];
        this.clipboardQueue = []; // Used for more complex loops that involve multiple actions
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
            await this.updateCharacterState();

            if (this.hasCooldown()) {
                await this.handleCooldown();
            }

            if (this.shouldRest()) {
                await rest(this.name);
                continue;
            }

            if (this.isInventoryFull()) {
                if (this.currentMap && this.currentMap.data.content.code === "bank") {
                    await this.depositAllItems();
                    if (this.oldLocation) {
                        this.pendingActions.unshift({ state: "move", x: this.oldLocation.data.x, y: this.oldLocation.data.y });
                        this.oldLocation = undefined;
                    }
                    continue;
                } else {
                    this.oldLocation = this.currentMap;
                    await move(this.name, bank.x, bank.y);
                    this.currentMap = undefined;
                    continue;
                }
            }

            if (!this.currentState) {
                this.currentState = this.pendingActions.shift() || this.defaultState;
            }

            try {
                await this.performCurrentState();
            } catch (error) {
                console.error("Error executing state", this.currentState, error);
            }
        }
    }

    async updateCharacterState() {
        const oldState = this.characterState;
        try {
            this.characterState = await getCharacter(this.name);
            if (!this.currentMap || this.characterState.data.x != this.currentMap.data.x || this.characterState.data.y != this.currentMap.data.y) {
                const { x, y } = this.characterState.data;
                this.currentMap = await getMap(x, y);
            }
            this.updateSkills();
        } catch (error) {
            console.log("Failed to parse character state", error, this.characterState);
            this.characterState = oldState;
        }
    }

    updateSkills() {
        const { mining_level, woodcutting_level, fishing_level } = this.characterState.data;
        this.skills = { mining_level, woodcutting_level, fishing_level };
    }

    shouldRest() {
        return this.characterState.data.hp < LOW_HP_THRESHOLD * this.characterState.data.max_hp;
    }

    hasCooldown() {
        const { cooldown, cooldown_expiration } = this.characterState.data;
        return cooldown && new Date() < new Date(cooldown_expiration);
    }

    async handleCooldown() {
        const cooldownTime = this.characterState.data.cooldown * 1000;
        console.log(`${this.name} cooldown for ${this.characterState.data.cooldown} seconds`);
        await delay(cooldownTime);
    }

    async performCurrentState() {
        await delay(ANTI_THROTTLING_DELAY);
        console.log(`${this.name} performing state: `, this.currentState);
        let result;

        switch (this.currentState.state) {
            case "idle":
                await delay(IDLE_DELAY);
                break;
            case "move":
                await this.moveCharacter();
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
            case "recycle":
                result = await recycle(this.name, this.currentState.code, this.currentState.quantity);
                break;
            case "use item":
                result = await useItem(this.name, this.currentState.code, this.currentState.quantity);
                break;
            case "equip":
                result = await equip(this.name, this.currentState.code, this.currentState.slot);
                break;
            case "deposit all":
                await this.depositAllItems();
                break;
            case "withdraw":
                result = await withdrawItem(this.name, this.currentState.code, this.currentState.quantity);
                break;
            case "autopilot":
                await this.autoPilotActions();
                break;
            case "copy queue":
                // Copy all actions from the queue into a clipboard for pasting later
                this.clipboardQueue = [];
                this.clipboardQueue.push({ state: "copy queue" });
                this.pendingActions.forEach((elem) => this.clipboardQueue.push({ ...elem }));
                break;
            case "paste queue":
                // Paste all the actions from the clipboard into the main queue
                if (this.clipboardQueue && this.clipboardQueue.length > 0) {
                    this.clipboardQueue.forEach((elem) => this.pendingActions.push(elem));
                }
                break;
            default:
                console.warn("Unknown state:", this.currentState.state);
                break;
        }

        this.handleActionResult(result);
        this.lastState = this.currentState;
        this.currentState = undefined;
    }

    async moveCharacter() {
        const { x, y } = this.currentState;
        await move(this.name, x, y);
        this.currentMap = undefined;
    }

    async depositAllItems() {
        for (const slot of this.characterState.data.inventory) {
            if (slot && slot.quantity > 0) {
                console.log("Depositing:", slot);
                const result = await depositItem(this.name, slot);
                if (result) await delay(result.data.cooldown.remaining_seconds * 1000);
            }
        }
    }

    async autoPilotActions() {
        if (this.shouldRest()) {
            this.queueState("rest");
        } else if (this.isInventoryFull()) {
            await this.manageFullInventory();
        } else {
            await this.gatherResourcesOrTrain();
        }
    }

    isInventoryFull() {
        const totalItems = this.characterState.data.inventory.reduce((sum, slot) => sum + slot.quantity, 0);
        return totalItems / this.characterState.data.inventory_max_items > INVENTORY_THRESHOLD;
    }

    async manageFullInventory() {
        this.queueState("move", bank);
        this.queueState("deposit all");
    }

    async gatherResourcesOrTrain() {
        // If the character is standing on a resource
        if (this.currentMap && this.currentMap.data.content?.type === "resource") {
            // If the skill gained from this resource needs more training
            const skillOfResource = this.getResourceSkill(this.currentMap.data.content.code);
            if (skillOfResource && this.skillNeedsTraining(skillOfResource)) {
                this.queueState("collect");
                return;
            }
        }
        await this.trainLowestSkill();
    }

    async getResourceSkill(code) {
        const resource = await getResource(code);
        if (resource) {
            return resource.data.skill;
        }
    }

    async skillNeedsTraining(resourceSkill) {
        // If there is a skill that is more than three levels behind then this skill does not need training
        return Object.values(this.skills).every((skill) => skill + 3 > this.skills[resourceSkill]);
    }

    async trainLowestSkill() {
        const lowestSkill = Object.keys(this.skills).reduce((lowest, skill) => (this.skills[skill] < this.skills[lowest] ? skill : lowest));
        const skillLocation = this.getSkillLocation(lowestSkill);

        if (skillLocation) {
            this.queueState("move", skillLocation);
            this.queueState("collect");
        }
    }

    getSkillLocation(skill) {
        const skillLocations = {
            mining_level: iron,
            woodcutting_level: spruceForest,
            fishing_level: gudgeonFishing,
        };
        return skillLocations[skill];
    }

    queueState(state, location = {}) {
        this.pendingActions.push({ state, ...location });
    }

    clearQueue() {
        this.pendingActions = [];
    }

    handleActionResult(result) {
        if (result?.error?.code === 499) {
            // Cooldown. try again
            this.pendingActions.unshift(this.currentState);
            return;
        }

        if (this.currentState.repeat && this.currentState.repeat > 0 && (result == undefined || result.error == undefined)) {
            this.currentState.repeat--;
            this.pendingActions.unshift(this.currentState);
        }
    }
}
