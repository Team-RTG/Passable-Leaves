package teamrtg.passableleaves;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.server.command.CommandTreeBase;

import teamrtg.passableleaves.asm.PLCollisionHandler;
import teamrtg.passableleaves.asm.PassableLeavesCore;

@SuppressWarnings("unused")
@Mod(
    modid = PassableLeaves.MOD_ID,
    name = PassableLeaves.MOD_NAME,
    version = PassableLeaves.MOD_VERSION,
    dependencies = "required-after:" + PassableLeavesCore.MOD_ID +"@[1.0.0,)",
    guiFactory = "teamrtg.passableleaves.PassableLeaves$PLGuiConfigFactory"
)
public class PassableLeaves
{
    static final String MOD_ID      = "passableleaves";
    static final String MOD_NAME    = "Passable Leaves";
    static final String MOD_VERSION = "@MOD_VERSION@";
    static final Logger LOGGER      = LogManager.getLogger(MOD_ID);
    static boolean LOCAL_SERVER = true;

    @Mod.Instance(MOD_ID) private static PassableLeaves instance;
    @Mod.EventHandler public void initPre   (FMLPreInitializationEvent  event) { proxy.preInit(event); }
    @Mod.EventHandler public void init      (FMLInitializationEvent     event) { proxy.init(event); }
    @Mod.EventHandler public void initPost  (FMLPostInitializationEvent event) { proxy.postInit(event); }
    @Mod.EventHandler public void addcommand(FMLServerStartingEvent     event) { proxy.addCommand(event); }

    @SidedProxy private static CommonProxy proxy;
    private abstract static class CommonProxy {
        void preInit   (FMLPreInitializationEvent  event) {
            LOGGER.debug("Initialising configuration");
            PLConfig.init(event);
            LOGGER.debug("Registering network messages");
            NetworkDispatcher.init();
            LOGGER.debug("Registering a new ConfigSyncHandler");
            MinecraftForge.EVENT_BUS.register(new ConfigSyncHandler());
        }
        void init      (FMLInitializationEvent     event) {

        }
        void postInit  (FMLPostInitializationEvent event) {
            PLConfig.sync();
        }
        void addCommand(FMLServerStartingEvent     event) {
            LOGGER.debug("Registering /" + PLCommandTree.CMD_ROOT + " command");
            event.registerServerCommand(new PLCommandTree());
        }
    }
    public static final class ClientProxy extends CommonProxy {
        @Override public void preInit   (FMLPreInitializationEvent  event) { super.preInit(event); }
        @Override public void init      (FMLInitializationEvent     event) { super.init(event); }
        @Override public void postInit  (FMLPostInitializationEvent event) { super.postInit(event); }
        @Override public void addCommand(FMLServerStartingEvent     event) { super.addCommand(event);}
    }
    public static final class ServerProxy extends CommonProxy {
        @Override public void preInit   (FMLPreInitializationEvent  event) { super.preInit(event); }
        @Override public void init      (FMLInitializationEvent     event) { super.init(event); }
        @Override public void postInit  (FMLPostInitializationEvent event) { super.postInit(event); }
        @Override public void addCommand(FMLServerStartingEvent     event) { super.addCommand(event); }
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    static final class PLCommandTree extends CommandTreeBase {

        private static final int    ACCESS_ALL                = 0; // everyone
        private static final int    ACCESS_OPS                = 2; // only server Ops
        private static final int    ACCESS_ADMIN              = 4; // only admins

        private static final String LANG_KEY_BASE             = MOD_ID+".command";
        private static final String LANG_KEY_PREFIX_ERROR     = LANG_KEY_BASE+".prefix.error";
        private static final String LANG_KEY_SETTINGS_HEADER  = LANG_KEY_BASE+".setting.header";
        private static final String LANG_KEY_CURRENT_SETTING  = LANG_KEY_BASE+".setting.current";
        private static final String LANG_KEY_VALUE_INVALID    = LANG_KEY_BASE+".setting.invalid";
        private static final String LANG_KEY_VALUE_OUTOFRANGE = LANG_KEY_BASE+".setting.valueOutOfRange";
        private static final String LANG_KEY_ADDENDUM_STATUS  = LANG_KEY_BASE+".addendum.status";
        private static final String LANG_KEY_ADDENDUM_SAVE    = LANG_KEY_BASE+".addendum.save";

        private static final Style  STYLE_ERROR               = new Style().setUnderlined(true).setColor(TextFormatting.DARK_RED);
        private static final Style  STYLE_DKGREEN             = new Style().setColor(TextFormatting.DARK_GREEN);
        private static final Style  STYLE_GOLD                = new Style().setColor(TextFormatting.GOLD);
        private static final Style  STYLE_DKAQUA              = new Style().setColor(TextFormatting.DARK_AQUA);
        private static final Style  STYLE_AQUA                = new Style().setColor(TextFormatting.AQUA);
        private static final Style  STYLE_GRAY                = new Style().setColor(TextFormatting.GRAY);

        private static final String CMD_ROOT                  = "passableleaves";

        PLCommandTree() {
            this.addSubcommand(new CommandStatus(this.getName()));
            this.addSubcommand(new CommandSave(this.getName()));
            this.addSubcommand(new CommandTreeDamage(this.getName()));
            this.addSubcommand(new CommandTreeSpeed(this.getName()));
        }

        @Override public int getRequiredPermissionLevel() { return ACCESS_ALL; } // gives everyone access to '/passableleaves status'
        @Override public String getName() { return CMD_ROOT; }
        @Override public String getUsage(ICommandSender sender) {
            ITextComponent ret = new TextComponentString("");
            getSubCommands().forEach(cmd -> {
                if (cmd instanceof CommandTreeBase) {
                    ((CommandTreeBase)cmd).getSubCommands().forEach(rcmd -> ret.appendText("\n").appendText(rcmd.getUsage(sender)));
                }
                else {
                    ret.appendText("\n").appendText(cmd.getUsage(sender));
                }
            });
            return ret.getFormattedText();
        }

        static final class CommandStatus extends CommandBase {
            private static final String NAME = "status";
            private final String parentName;
            CommandStatus(String parentName) { this.parentName = parentName; }
            @Override public int getRequiredPermissionLevel() { return ACCESS_ALL; }
            @Override public String getName() { return NAME; }
            @Override public String getUsage(ICommandSender sender) { return getUsageForBasicCommand(this, this.parentName, LANG_KEY_ADDENDUM_STATUS); }
            @Override public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
                sender.sendMessage(new TextComponentString("  ")
                    .appendSibling(new TextComponentTranslation(LANG_KEY_SETTINGS_HEADER).setStyle(STYLE_DKGREEN)).appendText(":")
                );
                sender.sendMessage(new TextComponentString("    ")
                    .appendSibling(new TextComponentTranslation(PLConfig.fallDamageReduction.getLanguageKey()).setStyle(STYLE_DKAQUA)).appendText(" : ")
                    .appendSibling(new TextComponentString(PLConfig.fallDamageReduction.getString()).setStyle(STYLE_AQUA))
                );
                sender.sendMessage(new TextComponentString("    ")
                    .appendSibling(new TextComponentTranslation(PLConfig.fallDamageThreshold.getLanguageKey()).setStyle(STYLE_DKAQUA)).appendText(" : ")
                    .appendSibling(new TextComponentString(PLConfig.fallDamageThreshold.getString()).setStyle(STYLE_AQUA))
                );
                sender.sendMessage(new TextComponentString("    ")
                    .appendSibling(new TextComponentTranslation(PLConfig.speedReductionHorizontal.getLanguageKey()).setStyle(STYLE_DKAQUA)).appendText(" : ")
                    .appendSibling(new TextComponentString(PLConfig.speedReductionHorizontal.getString()).setStyle(STYLE_AQUA))
                );
                sender.sendMessage(new TextComponentString("    ")
                    .appendSibling(new TextComponentTranslation(PLConfig.speedReductionVertical.getLanguageKey()).setStyle(STYLE_DKAQUA)).appendText(" : ")
                    .appendSibling(new TextComponentString(PLConfig.speedReductionVertical.getString()).setStyle(STYLE_AQUA))
                );
            }
        }

        static final class CommandSave extends CommandBase {
            private static final String NAME = "save";
            private final String parentName;
            CommandSave(String parentName) { this.parentName = parentName; }
            @Override public int getRequiredPermissionLevel() { return ACCESS_ADMIN; }
            @Override public String getName() { return NAME; }
            @Override public String getUsage(ICommandSender sender) { return getUsageForBasicCommand(this, this.parentName, LANG_KEY_ADDENDUM_SAVE); }
            @Override public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
                sender.sendMessage(new TextComponentString("  ").appendText("Saving current settings to PassableLeaves config.").setStyle(STYLE_DKGREEN));
                PLConfig.sync();
            }
        }

        static final class CommandTreeDamage extends CommandTreeBase {
            private static final String NAME = "damage";
            private final String parentName;
            CommandTreeDamage(String parentName) {
                this.parentName = parentName;
                this.addSubcommand(new CommandFallDamageReduction(this.parentName+" "+this.getName()));
                this.addSubcommand(new CommandFallDamageThreshold(this.parentName+" "+this.getName()));
            }
            @Override public int getRequiredPermissionLevel() { return ACCESS_OPS; }
            @Override public String getName() { return NAME; }
            @Override public String getUsage(ICommandSender sender) {
                return getUsageForBasicCommand(this, this.parentName, "["+CommandFallDamageReduction.NAME+"|"+CommandFallDamageThreshold.NAME+"]");
            }

            static final class CommandFallDamageReduction extends CommandBase {
                private static final String NAME = "reduction";
                private final String parentName;
                private final Property cfgProp = PLConfig.fallDamageReduction;
                CommandFallDamageReduction(String parentName) { this.parentName = parentName; }
                @Override public int getRequiredPermissionLevel() { return ACCESS_OPS; }
                @Override public String getName() { return NAME; }
                @Override public String getUsage(ICommandSender sender) { return getUsageForPropCommand(this, this.parentName, this.cfgProp); }
                @Override public void execute(MinecraftServer server, ICommandSender sender, String[] args) { executor(this, sender, args, this.cfgProp); }
            }

            static final class CommandFallDamageThreshold extends CommandBase {
                private static final String NAME = "threshold";
                private final String parentName;
                private final Property cfgProp = PLConfig.fallDamageThreshold;
                CommandFallDamageThreshold(String parentName) { this.parentName = parentName; }
                @Override public int getRequiredPermissionLevel() { return ACCESS_OPS; }
                @Override public String getName() { return NAME; }
                @Override public String getUsage(ICommandSender sender) { return getUsageForPropCommand(this, this.parentName, this.cfgProp); }
                @Override public void execute(MinecraftServer server, ICommandSender sender, String[] args) { executor(this, sender, args, this.cfgProp); }
            }
        }

        static final class CommandTreeSpeed extends CommandTreeBase {
            private static final String NAME = "speed";
            private final String parentName;
            CommandTreeSpeed(String parentName) {
                this.parentName = parentName;
                this.addSubcommand(new CommandSpeedHorizontal(this.parentName+" "+this.getName()));
                this.addSubcommand(new CommandSpeedVertical(this.parentName+" "+this.getName()));
            }
            @Override public int getRequiredPermissionLevel() { return ACCESS_OPS; }
            @Override public String getName() { return NAME; }
            @Override public String getUsage(ICommandSender sender) {
                return getUsageForBasicCommand(this, this.parentName, "["+CommandSpeedHorizontal.NAME+"|"+CommandSpeedVertical.NAME+"]");
            }

            static final class CommandSpeedHorizontal extends CommandBase {
                private static final String NAME = "horizontal";
                private final String parentName;
                private final Property cfgProp = PLConfig.speedReductionHorizontal;
                CommandSpeedHorizontal(String parentName) { this.parentName = parentName; }
                @Override public int getRequiredPermissionLevel() { return ACCESS_OPS; }
                @Override public String getName() { return NAME; }
                @Override public String getUsage(ICommandSender sender) { return getUsageForPropCommand(this, this.parentName, this.cfgProp); }
                @Override public void execute(MinecraftServer server, ICommandSender sender, String[] args) { executor(this, sender, args, this.cfgProp); }
            }

            static final class CommandSpeedVertical extends CommandBase {
                private static final String NAME = "vertical";
                private final String parentName;
                private final Property cfgProp = PLConfig.speedReductionVertical;
                CommandSpeedVertical(String parentName) { this.parentName = parentName; }
                @Override public int getRequiredPermissionLevel() { return ACCESS_OPS; }
                @Override public String getName() { return NAME; }
                @Override public String getUsage(ICommandSender sender) { return getUsageForPropCommand(this, this.parentName, this.cfgProp); }
                @Override public void execute(MinecraftServer server, ICommandSender sender, String[] args) { executor(this, sender, args, this.cfgProp); }
            }
        }

        private static String getUsageForBasicCommand(ICommand cmd, String parent, String addendum) {
            return new TextComponentString("    /")
                .appendSibling(new TextComponentString(parent+" "+cmd.getName()).setStyle(STYLE_DKAQUA)).appendText(" ")
                .appendSibling(new TextComponentTranslation(addendum).setStyle(STYLE_AQUA)).getFormattedText();
        }

        private static String getUsageForPropCommand(ICommand cmd, String parent, Property cfgProp) {
            return new TextComponentString("    /")
                .appendSibling(new TextComponentString(parent+" "+cmd.getName()).setStyle(STYLE_DKAQUA)).appendText(" <")
                .appendSibling(new TextComponentString(cfgProp.getMinValue()).setStyle(STYLE_GRAY)).appendText(" - ")
                .appendSibling(new TextComponentString(cfgProp.getMaxValue()).setStyle(STYLE_GRAY)).appendText(">").getFormattedText();
        }

        private static void executor(ICommand cmd, ICommandSender sender, String[] args, Property cfgProp) {
            if (args.length == 0) {
                sender.sendMessage(new TextComponentString("  ")
                    .appendSibling(new TextComponentTranslation(cfgProp.getLanguageKey()).setStyle(STYLE_DKAQUA))
                    .appendText(" ").appendSibling(new TextComponentTranslation(LANG_KEY_CURRENT_SETTING))
                    .appendText(" ").appendSibling(new TextComponentString(cfgProp.getString()).setStyle(STYLE_AQUA))
                );
                return;
            }
            int value;
            try { value = CommandBase.parseInt(args[0]); }
            catch (NumberInvalidException e) {
                sender.sendMessage(new TextComponentString("  ")
                    .appendSibling(new TextComponentTranslation(LANG_KEY_PREFIX_ERROR).setStyle(STYLE_ERROR)).appendText(": ")
                    .appendSibling(new TextComponentTranslation(LANG_KEY_VALUE_INVALID)).appendText(": ")
                    .appendSibling(new TextComponentString(((e.getErrorObjects().length>0)?e.getErrorObjects()[0].toString():"[NULL]")).setStyle(STYLE_GOLD))
                );
                sender.sendMessage(new TextComponentString(cmd.getUsage(sender)));
                return;
            }
            if (value < Integer.valueOf(cfgProp.getMinValue()) || value > Integer.valueOf(cfgProp.getMaxValue())) {
                sender.sendMessage(new TextComponentString("  ")
                    .appendSibling(new TextComponentTranslation(LANG_KEY_PREFIX_ERROR).setStyle(STYLE_ERROR)).appendText(": ")
                    .appendSibling(new TextComponentTranslation(LANG_KEY_VALUE_OUTOFRANGE)).appendText(": ")
                    .appendSibling(new TextComponentString(String.valueOf(value)).setStyle(STYLE_GOLD))
                );
                sender.sendMessage(new TextComponentString(cmd.getUsage(sender)));
                return;
            }

            sender.sendMessage(new TextComponentString("  ")
                .appendText("Setting ").appendSibling(new TextComponentTranslation(cfgProp.getLanguageKey()).setStyle(STYLE_DKAQUA))
                .appendText(" to: ").appendSibling(new TextComponentString(cfgProp.setValue(value).getString()).setStyle(STYLE_AQUA))
            );
            NetworkDispatcher.INSTANCE.sendConfigSyncMessageToAll();
        }
    }

    static final class PLConfig {
        private PLConfig() {}

        private static File          configFile;
        private static Configuration config;

        private static Property fallDamageThreshold;
        private static Property fallDamageReduction;
        private static Property speedReductionHorizontal;
        private static Property speedReductionVertical;

        private static void init(FMLPreInitializationEvent event) {
            if (configFile == null) { configFile = event.getSuggestedConfigurationFile(); }
            if (config == null) { config = new Configuration(configFile); }

            config.setCategoryComment(MOD_ID, "These settings get overridden when connected to a remote server.");

            fallDamageReduction = config.get(
                MOD_ID,
                "Fall Damage Reduction",
                50,
                "The percentage of normal damage taken when taking damage from falling into leaves." + Configuration.NEW_LINE +
                "The damage will be reduced by a further 10% with the Jump Boost potion effect.",
                0,
                100
            ).setLanguageKey(MOD_ID + ".config.fallDamageReduction");

            fallDamageThreshold = config.get(
                MOD_ID,
                "Fall Damage Threshold",
                20,
                "When falling into leaves, the (block) distance a player or mob has to fall before taking damage.",
                5,
                255
            ).setLanguageKey(MOD_ID + ".config.fallDamageThreshold");

            speedReductionHorizontal = config.get(
                MOD_ID,
                "Speed Reduction - Horizontal",
                75,
                "The reduced horizontal speed when passing through leaves. (% of normal)",
                0,
                100
            ).setLanguageKey(MOD_ID+".config.speedReductionHorizontal");

            speedReductionVertical = config.get(
                MOD_ID,
                "Speed Reduction - Vertical",
                75,
                "The reduced vertical speed when passing through leaves. (% of normal)",
                0,
                100
            ).setLanguageKey(MOD_ID+".config.speedReductionVertical");

            if (config.hasChanged()) { config.save(); }
        }

        private static void sync() {
            if (config.hasChanged()) {
                LOGGER.debug("Saving config");
                config.save();
            }
            if (LOCAL_SERVER) {
                LOGGER.debug("Syncing config settings on client");
                PLCollisionHandler.setFallDamageReduction(getFallDamageReduction());
                PLCollisionHandler.setFallDamageThreshold(getFallDamageThreshold());
                PLCollisionHandler.setSpeedReductionHorizontal(getSpeedReductionHorizontal());
                PLCollisionHandler.setSpeedReductionVertical(getSpeedReductionVertical());
            }
        }

        static float   getFallDamageReduction()      { return ((float)fallDamageReduction.getInt())/100; }
        static int     getFallDamageThreshold()      { return fallDamageThreshold.getInt(); }
        static double  getSpeedReductionHorizontal() { return speedReductionHorizontal.getDouble()/100; }
        static double  getSpeedReductionVertical()   { return speedReductionVertical.getDouble()/100; }
    }
    public  static final class PLGuiConfig extends GuiConfig {
        PLGuiConfig(GuiScreen parent) {
            super(parent, getConfigElements(), MOD_ID, false, false, I18n.format(MOD_ID+".config.maintitle"));
            this.titleLine2 = I18n.format(MOD_ID+".config.maintitle2");
        }
        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> ret = Lists.newArrayList();
            PLConfig.config.getCategory(MOD_ID).values().forEach(e -> {
                e.setComment(I18n.format(e.getLanguageKey()+".comment",'\n','\n','\n'));
                ret.add(new ConfigElement(e));
            });
            return ret;
        }
        @Override public void onGuiClosed() {
            super.onGuiClosed();
            PLConfig.sync();
        }
    }
    public  static final class PLGuiConfigFactory implements IModGuiFactory {
        @Override public void initialize(Minecraft mc) {}
        @Override public boolean hasConfigGui() { return true; }
        @Override public GuiScreen createConfigGui(GuiScreen parentScreen) { return new PLGuiConfig(parentScreen); }
        @Override public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return null; }
    }

    public static final class ConfigSyncHandler {
        ConfigSyncHandler() {}

        @SubscribeEvent
        @SideOnly(Side.SERVER)
        public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
            if (event.player instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) event.player;
                IThreadListener listener = player.getServer();
                if (listener != null) {
                    listener.addScheduledTask(() -> NetworkDispatcher.INSTANCE.sendConfigSyncMessageToPlayer(player));
                }
            }
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
            // Reset, so that a client can sync changes to it's own config while disconnected
            LOCAL_SERVER = true;
            PLConfig.sync();
        }
    }
}
