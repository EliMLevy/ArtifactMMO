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

export async function recycle(character, code, quantity) {
    return await sendRequest(character, "/action/recycling", "POST", JSON.stringify({code, quantity}));
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



export async function depositItem(character, item) {
    const body = { code: item.code, quantity: item.quantity };
    return await sendRequest(character, "/action/bank/deposit", "POST", JSON.stringify(body));
}
export async function withdrawItem(character, code, quantity) {
    const body = { code, quantity };
    return await sendRequest(character, "/action/bank/withdraw", "POST", JSON.stringify(body));
}


export async function useItem(character, code, quantity) {
    const body = { code, quantity }
    return await sendRequest(character, "/action/use", "POST", JSON.stringify(body))
}