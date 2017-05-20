package passableleaves.util;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;

import net.minecraftforge.fml.common.FMLLog;

import org.apache.logging.log4j.Level;
import passableleaves.config.ConfigPL;


public class Logger {

    public static void debug(String format, Object... data) {

        if (ConfigPL.enableDebugging) {
            FMLLog.log(Level.INFO, "[PassableLeaves-DEBUG] " + format, data);
        }
    }

    public static void info(String format, Object... data) {

        FMLLog.log(Level.INFO, "[PassableLeaves-INFO] " + format, data);
    }

    public static void warn(String format, Object... data) {

        FMLLog.log(Level.WARN, "[PassableLeaves-WARN] " + format, data);
    }

    public static void error(String format, Object... data) {

        FMLLog.log(Level.ERROR, "[PassableLeaves-ERROR] " + format, data);
    }

    public static void fatal(String message, Throwable throwable, Object... data) {

        FMLLog.log(Level.FATAL, "[PassableLeaves-FATAL] " + message, data);
        Minecraft.getMinecraft().crashed(new CrashReport(message, throwable));
    }
}