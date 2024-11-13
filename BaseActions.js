import { sendRequest, sendRequestToURL, delay } from "./Util.js";

export async function move(character, x, y) {
    var raw = JSON.stringify({ x, y });
    const result = await sendRequest(character, "/action/move", "POST", raw);
    if (result && result.error && result.error.code === 490) {
        console.log("Already here!");
        result.data = {
            cooldown: {
                remaining_seconds: 0,
            },
        };
    }
    return result;
}

export async function moveWithCooldown(character, x, y) {
    const moveResult = await move(character, x, y);
    if (moveResult) {
        console.log("Wating for ", moveResult.data.cooldown.remaining_seconds);
        await delay(moveResult.data.cooldown.remaining_seconds * 1000);
        return moveResult;
    }
}


export async function attack(character) {
    return await sendRequest(character, "/action/fight", "POST");
}

export async function rest(character) {
    return await sendRequest(character, "/action/rest", "POST");
}

export async function collect(character) {
    return await sendRequest(character, "/action/gathering", "POST");
}


export async function craft(character, itemCode, quantity) {
    var raw = JSON.stringify({
        code: itemCode,
        quantity,
    });
    return await sendRequest(character, "/action/crafting", "POST", raw);
}

export async function unequip(character, slot) {
    var raw = JSON.stringify({
        slot,
    });

    return await sendRequest(character, "/action/unequip", "POST", raw);
}

export async function equip(character, code, slot) {
    var raw = JSON.stringify({ code, slot });
    return await sendRequest(character, "/action/equip", "POST", raw);
}


export async function getCharacter(name) {

    return await sendRequestToURL("https://api.artifactsmmo.com/characters/" + name, "", "GET");

}

export async function getMap(x, y) {
    return await sendRequestToURL(`https://api.artifactsmmo.com/maps/${x}/${y}`, '', "GET");
}

const commonResourceCache = {
    ash_tree: {
        data: {
            name: "Ash Tree",
            code: "ash_tree",
            skill: "woodcutting",
            level: 1,
            drops: [
                {
                    code: "ash_wood",
                    rate: 1,
                    min_quantity: 1,
                    max_quantity: 1,
                },
                {
                    code: "sap",
                    rate: 10,
                    min_quantity: 1,
                    max_quantity: 1,
                },
            ],
        },
    },
    copper_rocks: {
        data: {
            name: "Copper Rocks",
            code: "copper_rocks",
            skill: "mining",
            level: 1,
            drops: [
                {
                    code: "copper_ore",
                    rate: 1,
                    min_quantity: 1,
                    max_quantity: 1,
                },
                {
                    code: "topaz_stone",
                    rate: 600,
                    min_quantity: 1,
                    max_quantity: 1,
                },
                {
                    code: "topaz",
                    rate: 5000,
                    min_quantity: 1,
                    max_quantity: 1,
                },
                {
                    code: "emerald",
                    rate: 5000,
                    min_quantity: 1,
                    max_quantity: 1,
                },
                {
                    code: "emerald_stone",
                    rate: 600,
                    min_quantity: 1,
                    max_quantity: 1,
                },
                {
                    code: "ruby",
                    rate: 5000,
                    min_quantity: 1,
                    max_quantity: 1,
                },
                {
                    code: "ruby_stone",
                    rate: 600,
                    min_quantity: 1,
                    max_quantity: 1,
                },
                {
                    code: "sapphire",
                    rate: 5000,
                    min_quantity: 1,
                    max_quantity: 1,
                },
                {
                    code: "sapphire_stone",
                    rate: 600,
                    min_quantity: 1,
                    max_quantity: 1,
                },
            ],
        },
    },
    gudgeon_fishing_spot: {
        data: {
            name: "Gudgeon Fishing Spot",
            code: "gudgeon_fishing_spot",
            skill: "fishing",
            level: 1,
            drops: [
                {
                    code: "gudgeon",
                    rate: 1,
                    min_quantity: 1,
                    max_quantity: 1,
                },
                {
                    code: "algae",
                    rate: 10,
                    min_quantity: 1,
                    max_quantity: 1,
                },
            ],
        },
    },
};
export async function getResource(code) {
    if(commonResourceCache[code]) return commonResourceCache[code]
    return await sendRequestToURL(`https://api.artifactsmmo.com/resources/${code}`, '', "GET");
}

export async function depositItem(character, item) {
    const body = { code: item.code, quantity: item.quantity };
    return await sendRequest(character, "/action/bank/deposit", "POST", JSON.stringify(body));
}

export async function useItem(character, code, quantity) {
    const body = { code, quantity }
    return await sendRequest(character, "/action/use", "POST", JSON.stringify(body))
}