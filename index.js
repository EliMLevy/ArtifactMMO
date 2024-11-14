import Character from "./Character.js";
import express from 'express';
import cors from 'cors'

export const BASE_URL = `https://api.artifactsmmo.com/my/`;
export const API_TOKEN = process.env.API_TOKEN;

export const weaponCraftingHall = { x: 2, y: 1 };
export const ashForest = { x: -1, y: 0 };
export const spruceForest = { x: 2, y: 6 };
export const chicken = { x: 0, y: 1 };

export const copper = { x: 2, y: 0 };
export const iron = { x: 1, y: 7 };

export const gudgeonFishing = { x: 4, y: 2 };
export const shrimpFishing = {x: 5, y: 2}


export const bank = { x: 4, y: 1 };
export const miningWorkshop = { x: 1, y: 5 };
export const yellowSlime = { x: 1, y: -2 };

// Initialize express
const app = express();
app.use(express.json());
app.use(cors());

// Store character instances in a map for easy access
const characters = new Map();

// Create and start character instances
const Bobby = new Character("Bobby");
characters.set("Bobby", Bobby);
Bobby.actionLoop();

const Stuart = new Character("Stuart");
characters.set("Stuart", Stuart);
Stuart.actionLoop();

const George = new Character("George");
characters.set("George", George);
George.actionLoop();

const Tim = new Character("Tim");
characters.set("Tim", Tim);
Tim.actionLoop();

const Joe = new Character("Joe");
characters.set("Joe", Joe);
Joe.actionLoop();

// POST endpoint for adding actions to character queues
app.post('/action', (req, res) => {
    const { characterName, action, ...actionParams } = req.body;
    
    // Validate required fields
    if (!characterName || !action) {
        return res.status(400).json({ error: 'Character name and action are required' });
    }

    // Find character
    const character = characters.get(characterName);
    if (!character) {
        return res.status(404).json({ error: 'Character not found' });
    }


    // Add action to character's queue
    character.queueState(action, actionParams);

    res.json({
        lastState: character.lastState,
        defaultState: character.defaultState,
        upcomingActions: character.pendingActions,
        character: character.characterState,
        currentMap: character.currentMap,
    });
});

app.post('/clear', (req, res) => {
    const { characterName } = req.body;

    // Validate required fields
    if (!characterName) {
        return res.status(400).json({ error: "Character name is required" });
    }

    // Find character
    const character = characters.get(characterName);
    if (!character) {
        return res.status(404).json({ error: "Character not found" });
    }

    // Add action to character's queue
    character.clearQueue();

    res.json({
        lastState: character.lastState,
        defaultState: character.defaultState,
        upcomingActions: character.pendingActions,
        character: character.characterState,
        currentMap: character.currentMap,
    });
})

app.post('/default', (req, res) => {
    const { characterName, action, ...actionParams } = req.body;

    // Validate required fields
    if (!characterName || !action) {
        return res.status(400).json({ error: "Character name and action are required" });
    }

    // Find character
    const character = characters.get(characterName);
    if (!character) {
        return res.status(404).json({ error: "Character not found" });
    }

    // Create action object based on the action type
    const actionObject = { state: action, ...actionParams };

    // Add action to character's queue
    character.defaultState = actionObject

    res.json({
        lastState: character.lastState,
        defaultState: character.defaultState,
        upcomingActions: character.pendingActions,
        character: character.characterState,
        currentMap: character.currentMap,
    });
})

app.post('/character', (req, res) => {
    const { characterName } = req.body;
    // Validate required fields
    if (!characterName) {
        return res.status(400).json({ error: "Character name is required" });
    }

    // Find character
    const character = characters.get(characterName);
    if (!character) {
        return res.status(404).json({ error: "Character not found" });
    }

    res.json({
        lastState: character.lastState,
        defaultState: character.defaultState,
        upcomingActions: character.pendingActions,
        character: character.characterState,
        currentMap: character.currentMap,
    });

})

// Start server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
