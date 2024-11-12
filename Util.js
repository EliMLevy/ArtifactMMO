import { API_TOKEN, BASE_URL } from "./index.js";

export const delay = (ms) => new Promise((resolve) => { 
    console.log("Sleeping for", ms)
    return setTimeout(resolve, ms)
});

export async function sendRequest(character, path, method, body) {
    return await sendRequestToURL(BASE_URL + character, path, method, body, true);
}

export async function sendRequestToURL(url, path, method, body) {
    var myHeaders = new Headers();
    myHeaders.append("Accept", "application/json");
    myHeaders.append("Content-Type", "application/json");
    myHeaders.append("Authorization", "Bearer " + API_TOKEN);

    var requestOptions = {
        method: method,
        headers: myHeaders,
        body,
        redirect: "follow",
    };

    try {
        const response = await fetch(url + path, requestOptions);
        const result = await response.json();
        if (result.error) {
            console.log("Failed:", result.error);
        }
        if(response.status != 200) {
            console.log("Unexpected status:", response.status, result)
        }
        return result;
    } catch (error) {
        console.log("error", error);
        return false;
    }
}

export function printCharacter(json) {
    const character = json.data.character;

    console.log("=== Character Summary ===");
    console.log(`Name: ${character.name}`);
    console.log(`Account: ${character.account}`);
    console.log(`Skin: ${character.skin}`);
    console.log(`Level: ${character.level}`);
    console.log(`XP: ${character.xp}/${character.max_xp}`);
    console.log(`Gold: ${character.gold}`);
    console.log(`HP: ${character.hp}/${character.max_hp}`);
    console.log(`Attack Stats: Fire (${character.attack_fire}), Earth (${character.attack_earth}), Water (${character.attack_water}), Air (${character.attack_air})`);
    console.log(`Inventory Max Slots: ${character.inventory_max_items}`);
    console.log("Inventory Items:");
    character.inventory.forEach((item) => {
        if (item.code) {
            console.log(`- Slot ${item.slot}: ${item.code} (x${item.quantity})`);
        }
    });
}

export function printActionDetails(json) {
    const details = json.data.details;

    console.log("\n=== Action Details ===");
    console.log("XP Gained: " + details.xp);
    details.items.forEach((item) => {
        console.log(`Collected x${item.quantity} ${item.code}`);
    });
}

export function printFight(json) {
    const fight = json.data.fight;

    console.log("\n=== Fight Summary ===");
    console.log(`Result: ${fight.result}`);
    console.log(`XP Gained: ${fight.xp}`);
    console.log(`Gold Gained: ${fight.gold}`);
    console.log(`Turns Taken: ${fight.turns}`);
}

export function printDestination(json) {
    const destination = json.data.destination;

    console.log("\n=== Destination ===");
    console.log("Name: " + destination.name);
    console.log(`Location: (${destination.x}, ${destination.y})`);
    if (destination.content) {
        console.log(`Content type: ${destination.content.type}`);
        console.log(`Content code: ${destination.content.code}`);
    }
}

export function printCooldown(json) {
    const cooldown = json.data.cooldown;

    console.log("\n=== Cooldown Information ===");
    console.log(`Cooldown Time: ${cooldown.total_seconds} seconds`);
    console.log(`Cooldown Remaining: ${cooldown.remaining_seconds} seconds`);
    console.log(`Cooldown Reason: ${cooldown.reason}`);
    console.log(`Cooldown Expires At: ${new Date(cooldown.expiration).toLocaleString()}`);


}

export async function repeat(action, times, endOfChain) {
    for (let i = 0; i < times; i++) {
        console.log("Performing action #" + (i + 1));
        const result = await action();
        if (!result) {
            console.log("ERROR");
            return;
        } else {
            if (i + 1 < times || !endOfChain) { 
                // only sleep if there is another iteration remaining (if we are endOfChain)
                await delay(result.data.cooldown.remaining_seconds * 1000);
            } else {
                return result;
            }
        }
    }
}

export async function chainActions(...actions) {
    for (let i = 0; i < actions.length; i++) {
        const action = actions[i];
        console.log("Performing action " + action);
        const result = await action();
        if (!result) {
            console.log("ERROR");
            return;
        } else {
            if (i + 1 < actions.length) {
                await delay(result.data.cooldown.remaining_seconds * 1000);
            } else {
                return result;
            }
        }
    }
}

export async function waitForCooldown(action) {
    const result = await action()
    if (result) {
        await delay(result.data.cooldown.remaining_seconds * 1000);
        return result;
    }
}
