package teamrtg.passableleaves.asm;

import java.util.Map;

import com.google.common.eventbus.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

/**
 * This class was originally written by HellFirePvP for the Appalachia addon for RTG.
 * It was extracted from Appalachia by WhichOnesPink so that it could be a standalone mod.
 * The complete source code for this mod can be found on GitHub.
 * Class: PassableLeavesCore
 * @author HellFirePvP
 * @since 2017.02.12
 * @author srs-bsns
 * @since 2017.10.04
 */
@IFMLLoadingPlugin.Name(PassableLeavesCore.MOD_NAME)
@IFMLLoadingPlugin.MCVersion(Loader.MC_VERSION)
@IFMLLoadingPlugin.TransformerExclusions("teamrtg.passableleaves.asm")
@IFMLLoadingPlugin.SortingIndex(1001)
public class PassableLeavesCore implements IFMLLoadingPlugin {

    public static final String MOD_ID   = "passableleavescore";
           static final String MOD_NAME = "PassableLeavesCore";
           static final Logger LOGGER   = LogManager.getLogger(MOD_ID);

    private static boolean deobfEnvironment;
    static boolean isDeobf() { return !deobfEnvironment;}

    public PassableLeavesCore() { LOGGER.debug("PassableLeavesCore coremod initialized"); }
    @Override public String[] getASMTransformerClass() { return new String[] {"teamrtg.passableleaves.asm.PassableLeavesTransformer"}; }
    @Override public String getModContainerClass() { return "teamrtg.passableleaves.asm.PassableLeavesCore$PLCoreModContainer"; }
    @Override public String getSetupClass() { return null; }
    @Override public void injectData(Map<String, Object> data) { deobfEnvironment = (boolean) data.get("runtimeDeobfuscationEnabled"); }
    @Override public String getAccessTransformerClass() { return null; }

    @SuppressWarnings("unused")
    public static final class PLCoreModContainer extends DummyModContainer {
        public PLCoreModContainer() {
            super(new ModMetadata());
            ModMetadata meta  = getMetadata();
            meta.modId        = MOD_ID;
            meta.name         = MOD_NAME;
            meta.version      = "@COREMOD_VERSION@";
            meta.description  = "A transformer for BlockLeaves.";
            meta.logoFile     = "/assets/passableleaves/logo.png";
            meta.authorList.add("Team-RTG");
        }
        @Override public VersionRange acceptableMinecraftVersionRange() { return VersionParser.parseRange("@ACCEPTABLE_MC_VER_RANGE@"); }
        @Override public ModContainer getMod() { return this; }
        @Override public boolean registerBus(EventBus bus, LoadController controller) { return true; }// required for error handling
    }
}