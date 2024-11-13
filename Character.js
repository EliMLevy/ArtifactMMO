import { attack, collect, craft, getCharacter, rest, move, depositItem, useItem, getMap, getResource } from "./BaseActions.js";
import { delay } from "./Util.js";
import { copper, bank, gudgeonFishing, ashForest, chicken, miningWorkshop } from "./index.js";

const INVENTORY_THRESHOLD = 0.8;
const LOW_HP_THRESHOLD = 0.5;
const IDLE_DELAY = 3000;

/**
 * Encapsulates actions and character state handling in an improved, modular way.
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
            await this.updateCharacterState();

            if (this.shouldRest()) {
                this.setCurrentState("rest");
            } else if (this.hasCooldown()) {
                await this.handleCooldown();
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
        this.characterState = await getCharacter(this.name);
        if (!this.currentMap) {
            const { x, y } = this.characterState.data;
            this.currentMap = await getMap(x, y);
        }
        this.updateSkills();
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
            case "use item":
                result = await useItem(this.name, this.currentState.code, this.currentState.quantity);
                break;
            case "deposit all":
                await this.depositAllItems();
                break;
            case "autopilot":
                await this.autoPilotActions();
                break;
            default:
                console.warn("Unknown state:", this.currentState.state);
                break;
        }

        this.handleActionResult(result);
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
        const resource = await getResource(code)
        if(resource) {
            return resource.data.skill;
        }

    }

    async skillNeedsTraining(skill) {
        // If there is a skill that is more than three levels behind then this skill does not need training
        return Object.values(this.skills).every((skill) => skill + 3 > this.skills[resourceSkill]);
    }

    async trainLowestSkill() {
        const lowestSkill = Object.keys(this.skills).sort((a, b) => this.skills[a] - this.skills[b])[0];
        const skillLocation = this.getSkillLocation(lowestSkill);

        if (skillLocation) {
            this.queueState("move", skillLocation);
            this.queueState("collect");
        }
    }

    getSkillLocation(skill) {
        const skillLocations = {
            mining_level: copper,
            woodcutting_level: ashForest,
            fishing_level: gudgeonFishing,
        };
        return skillLocations[skill];
    }

    queueState(state, location = {}) {
        this.pendingActions.push({ state, ...location });
    }

    handleActionResult(result) {
        if (result?.error?.code === 499) {
            this.pendingActions.unshift(this.currentState);
        }
    }
}
