package pyre.tinkerslevellingaddon.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import pyre.tinkerslevellingaddon.TinkersLevellingAddon;

public class Messages {

    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(TinkersLevellingAddon.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(LevelUpPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(LevelUpPacket::new)
                .encoder(LevelUpPacket::toBytes)
                .consumer(LevelUpPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayerEntity player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
