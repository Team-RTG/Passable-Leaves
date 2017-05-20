package passableleaves.config;

import java.io.File;

public class ConfigManager {

    public static File plConfigFile;

    private ConfigPL configPL = new ConfigPL();

    public static void init(String configpath) {

        plConfigFile = new File(configpath + "passableleaves.cfg");

        ConfigPL.init(plConfigFile);
    }

    public ConfigPL passableleaves() {
        return configPL;
    }
}
