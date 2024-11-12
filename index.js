import Character from "./Character.js";

export const BASE_URL = `https://api.artifactsmmo.com/my/`;
export const API_TOKEN = "";

export const weaponCraftingHall = { x: 2, y: 1 };
export const ashForest = { x: -1, y: 0 };
export const chicken = { x: 0, y: 1 };
export const copper = { x: 2, y: 0 };
export const bank = { x: 4, y: 1 };
export const miningWorkshop = { x: 1, y: 5 };
export const yellowSlime = { x: 1, y: -2 };
export const gudgeonFishing = {x: 4, y: 2}

// goFishing()
// mineCopper()
// collectWood()
// fightLoop();

const Bobby = new Character("Bobby");
// Bobby.addToActionQueue({ state: "move", ...yellowSlime });
// for(let i = 0; i < 100; i++) {
//     Bobby.addToActionQueue({ state: "attack" });
//     Bobby.addToActionQueue({ state: "rest" });
// }
Bobby.allActionLoop();
Bobby.actionLoop();

const Stuart = new Character("Stuart");
Stuart.allActionLoop()
Stuart.actionLoop();



const George = new Character("George");
George.allActionLoop()
George.actionLoop();


const Tim = new Character("Tim");
Tim.allActionLoop();
Tim.actionLoop();


const Joe = new Character("Joe");
Joe.allActionLoop();
Joe.actionLoop();
