package passableleaves.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import passableleaves.util.Logger;


public class ConfigPL {

    public static Configuration config;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Debugging
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static boolean enableDebugging = false;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Leaves
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static float motionX = 0.75f;
    public static float motionY = 0.75f;
    public static float motionZ = 0.75f;
    public static float fallDistance = 0f;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static void init(File configFile) {

        config = new Configuration(configFile);

        try {

            config.load();

            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // Debugging
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

            //enableDebugging = config.getBoolean("Enable Debugging", "Debugging", enableDebugging, "WARNING: This should only be enabled if you know what you're doing." + Configuration.NEW_LINE);

            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // Leaves
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

            motionX = config.getFloat(
                "Rate of speed when passing through leaves along the X axis.",
                "Leaves",
                motionX,
                0f,
                1f,
                "Higher values = faster speed. Set to 1.0 to pass through leaves without slowing down." +
                    Configuration.NEW_LINE +
                    "You normally want this to be the same as the rate of speed along the Z axis." +
                    Configuration.NEW_LINE
            );

            motionY = config.getFloat(
                "Rate of speed when passing through leaves along the Y axis.",
                "Leaves",
                motionY,
                0f,
                1f,
                "Higher values = faster speed. Set to 1.0 to pass through leaves without slowing down." +
                    Configuration.NEW_LINE +
                    "This setting affects how fast you fall through leaves." +
                    Configuration.NEW_LINE
            );

            motionZ = config.getFloat(
                "Rate of speed when passing through leaves along the Z axis.",
                "Leaves",
                motionZ,
                0f,
                1f,
                "Higher values = faster speed. Set to 1.0 to pass through leaves without slowing down." +
                    Configuration.NEW_LINE +
                    "You normally want this to be the same as the rate of speed along the X axis." +
                    Configuration.NEW_LINE
            );

            fallDistance = config.getFloat(
                "Amount of fall distance to keep upon falling onto leaves.",
                "Leaves",
                fallDistance,
                0f,
                1f,
                "Higher values = more damage taken upon falling onto leaves." +
                    Configuration.NEW_LINE +
                    "For example, most blocks have a value of 1.0 (full damage)." +
                    Configuration.NEW_LINE +
                    "Hay blocks have a value of 0.2 (20% damage)." +
                    Configuration.NEW_LINE +
                    "Slime blocks & cobwebs have a value of 0.0 (no damage); " +
                    Configuration.NEW_LINE
            );
        }
        catch (Exception e) {

            Logger.error("Passable Leaves had a problem loading its configuration.");
        }
        finally {

            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}