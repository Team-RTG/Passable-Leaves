package teamrtg.passableleaves;

import java.util.Map;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
@IFMLLoadingPlugin.Name("PassableLeavesCore")
@IFMLLoadingPlugin.MCVersion(Loader.MC_VERSION)
@IFMLLoadingPlugin.TransformerExclusions("teamrtg.passableleaves")
@IFMLLoadingPlugin.SortingIndex(1001)
public class PassableLeavesCore implements IFMLLoadingPlugin {

    static final Logger LOGGER = LogManager.getLogger("PassableLeavesCore");
    private static boolean deobfEnvironment;
    static boolean isDeobf() { return !deobfEnvironment;}

    public PassableLeavesCore() { LOGGER.info("PassableLeavesCore coremod initialized"); }
    @Override public String[] getASMTransformerClass() { return new String[] {"teamrtg.passableleaves.PassableLeavesTransformer"}; }
    @Override public String getModContainerClass() { return null; }
    @Override public String getSetupClass() { return null; }
    @Override public void injectData(Map<String, Object> data) { deobfEnvironment = (boolean) data.get("runtimeDeobfuscationEnabled"); }
    @Override public String getAccessTransformerClass() { return null; }

}