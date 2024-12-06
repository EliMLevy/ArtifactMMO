package com.elimelvy.artifacts;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {

        // JsonObject characterJoe = AtomicActions.getCharacter("Joe");
        // Character joe = Character.fromJson(characterJoe);
        

        // Thread joeThread = new Thread(joe);
        // joe.setTask("attack", "cow");
        // joeThread.start();


        // joeThread.join();

        CharacterManager mgr = new CharacterManager();
        Bank.getInstance().refreshBankItems();
        mgr.loadCharacters();
        mgr.runCharacters();

        mgr.pickItemToCraft();
        mgr.launchCraftingManager();
        boolean finished = false;
        while(!finished) {
            finished = mgr.runCraftingManager();
            Thread.sleep(5000);
        }

        mgr.standbyMode();

    }
}
