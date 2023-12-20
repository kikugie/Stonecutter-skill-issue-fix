package dev.kikugie;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("testmod");

    @Override
    public void onInitialize() {
        /*? if ~1.20.1 {*//*
        System.out.println("This 1.20.1");
        *//*?} */

        /*? if ~1.19.4 {*/
        System.out.println("This 1.19.4");
        /*?} */

        /*? if ~1.20 */
        /*System.out.println("This 1.20");*/


        /*? if ~1.19 */
        System.out.println("This 1.19");
    }
}
