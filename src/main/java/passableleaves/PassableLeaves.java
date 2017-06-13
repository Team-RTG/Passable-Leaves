package passableleaves;

import java.io.File;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import passableleaves.config.ConfigManager;
import passableleaves.proxy.ClientProxy;
import passableleaves.proxy.CommonProxy;
import passableleaves.reference.ModInfo;


@SuppressWarnings({"WeakerAccess", "unused"})
@Mod(
    modid = ModInfo.MOD_ID,
    name = ModInfo.MOD_NAME,
    version = ModInfo.MOD_VERSION,
    dependencies = "required-after:forge@[" + ModInfo.MCF_MINVER + "," + ModInfo.MCF_MAXVER + ")",
    acceptableRemoteVersions = "*"
)
public class PassableLeaves {

    @Instance(ModInfo.MOD_ID)
    public static PassableLeaves instance;
    public static String configPath;

    @SidedProxy(clientSide = ClientProxy.LOCATION, serverSide = CommonProxy.LOCATION)
    public static CommonProxy proxy;

    private ConfigManager configManager = new ConfigManager();

    @EventHandler
    public void initPre(FMLPreInitializationEvent event) {

        instance = this;

        configPath = event.getModConfigurationDirectory() + File.separator + ModInfo.CONFIG_DIRECTORY + File.separator;
        ConfigManager.init(configPath);

        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void initPost(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
