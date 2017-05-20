package passableleaves.core;

import java.util.Map;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

/**
 * This class was originally written by HellFirePvP for the Appalachia addon for RTG.
 * It was extracted from Appalachia by WhichOnesPink so that it could be a standalone mod.
 * The complete source code for this mod can be found on GitHub.
 * Class: PLCore
 * Created by HellFirePvP
 * Date: 12.02.2017 / 15:23
 */
@IFMLLoadingPlugin.Name(value = "PLCore")
@IFMLLoadingPlugin.MCVersion(value = "1.10.2")
@IFMLLoadingPlugin.TransformerExclusions({"passableleaves.core"})
@IFMLLoadingPlugin.SortingIndex(1001)
public class PLCore implements IFMLLoadingPlugin {

    public static boolean isDebofEnabled = false;

    public PLCore() {
        FMLLog.info("[PLCore] Initialized.");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        isDebofEnabled = (boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass() {
        return "passableleaves.core.PLTransformer";
    }

}
