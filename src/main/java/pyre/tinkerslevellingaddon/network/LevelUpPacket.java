package pyre.tinkerslevellingaddon.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class LevelUpPacket {

    private final int level;
    private final ITextComponent toolName;

    public LevelUpPacket(int level, ITextComponent toolName) {
        this.level = level;
        this.toolName = toolName;
    }

    public LevelUpPacket(PacketBuffer buf) {
        level = buf.readInt();
        toolName = buf.readComponent();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(level);
        buf.writeComponent(toolName);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandler.handleLevelUpMessage(level, toolName)));
        return true;
    }
}
