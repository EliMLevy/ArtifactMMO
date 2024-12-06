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
        mgr.loadCharacters();
        mgr.runCharacters();

        for(int i = 0; i < 100; i++) {
            mgr.manageCraftingNewGear();
            Thread.sleep(3000);
        }

    }
}
