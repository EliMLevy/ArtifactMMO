package com.elimelvy.artifacts;

import com.elimelvy.artifacts.model.Character;
import com.google.gson.JsonObject;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {

        JsonObject character = AtomicActions.getCharacter("Joe");
        System.out.println(character);
        Character joe = Character.fromJson(character);
        System.out.println(joe);

        System.out.println( "Hello World!" );
    }
}
