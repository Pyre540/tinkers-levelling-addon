package pyre.tinkerslevellingaddon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LevelUpPacket {

    private final int level;
    private final Component toolName;

    public LevelUpPacket(int level, Component toolName) {
        this.level = level;
        this.toolName = toolName;
    }

    public LevelUpPacket(FriendlyByteBuf buf) {
        level = buf.readInt();
        toolName = buf.readComponent();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(level);
        buf.writeComponent(toolName);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandler.handleLevelUpMessage(level, toolName)));
        return true;
    }
}
