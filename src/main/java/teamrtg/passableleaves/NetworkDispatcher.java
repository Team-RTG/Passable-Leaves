package teamrtg.passableleaves;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import teamrtg.passableleaves.PassableLeaves.PLConfig;

final class NetworkDispatcher
{
    static final NetworkDispatcher INSTANCE = new NetworkDispatcher();

    private final SimpleNetworkWrapper dispatcher;

    private NetworkDispatcher() {
        this.dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel(PassableLeaves.MOD_ID);
    }

    // Send to all players when a setting is changed on the server
    void sendConfigSyncMessageToAll(){
        this.dispatcher.sendToAll(new SyncConfigMessage());
    }

    // Send to a single player when connecting to a server
    void sendConfigSyncMessageToPlayer(EntityPlayerMP player){
        this.dispatcher.sendTo(new SyncConfigMessage(), player);
    }

    static void init() {
        INSTANCE.dispatcher.registerMessage(SyncConfigMessage.class, SyncConfigMessage.class, 0, Side.CLIENT);
    }

    @SuppressWarnings("WeakerAccess") // Implementation needs to be publically visible
    public static final class SyncConfigMessage implements IMessage, IMessageHandler<SyncConfigMessage, IMessage>
    {
        public boolean isLocalServer;
        public float   fallDamageReduction;
        public int     fallDamageThreshold;
        public double  speedReductionHorizontal;
        public double  speedReductionVertical;

        public SyncConfigMessage() {
            this.isLocalServer            = false;
            this.fallDamageReduction      = PLConfig.getFallDamageReduction();
            this.fallDamageThreshold      = PLConfig.getFallDamageThreshold();
            this.speedReductionHorizontal = PLConfig.getSpeedReductionHorizontal();
            this.speedReductionVertical   = PLConfig.getSpeedReductionVertical();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeBoolean(FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer());
            buf.writeFloat  (PLConfig.getFallDamageReduction());
            buf.writeInt    (PLConfig.getFallDamageThreshold());
            buf.writeDouble (PLConfig.getSpeedReductionHorizontal());
            buf.writeDouble (PLConfig.getSpeedReductionVertical());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.isLocalServer            = buf.readBoolean();
            this.fallDamageReduction      = buf.readFloat();
            this.fallDamageThreshold      = buf.readInt();
            this.speedReductionHorizontal = buf.readDouble();
            this.speedReductionVertical   = buf.readDouble();
        }

        @Override
        public IMessage onMessage(SyncConfigMessage message, MessageContext ctx) {
            PassableLeaves.LOCAL_SERVER             = message.isLocalServer;
            PassableLeaves.fallDamageReduction      = message.fallDamageReduction;
            PassableLeaves.fallDamageThreshold      = message.fallDamageThreshold;
            PassableLeaves.speedReductionHorizontal = message.speedReductionHorizontal;
            PassableLeaves.speedReductionVertical   = message.speedReductionVertical;
            return null;
        }
    }
}
