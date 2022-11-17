package pyre.tinkerslevellingaddon.network;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import pyre.tinkerslevellingaddon.config.Config;

public class ClientPacketHandler {

    public static void handleLevelUpMessage(int level, Component toolName) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (Config.enableLevelUpMessage.get()) {
            MutableComponent message;
            if (I18n.exists("message.tinkerslevellingaddon.levelUp." + level)) {
                message = new TranslatableComponent("message.tinkerslevellingaddon.levelUp." + level, toolName);
            } else {
                MutableComponent levelComponent = new TextComponent(String.valueOf(level))
                        .withStyle(s -> s.withColor(ChatFormatting.GOLD));
                message = new TranslatableComponent("message.tinkerslevellingaddon.levelUp.generic", toolName, levelComponent);
            }
            message.withStyle(style -> style.withColor(9337340));
            player.sendMessage(message, Util.NIL_UUID);
        }
        SoundEvent soundEvent = Config.levelUpSound.get().getSoundEvent();
        if (soundEvent != null) {
            player.getLevel().playSound(player, player.getX(), player.getY(), player.getZ(),
                    soundEvent, player.getSoundSource(), 1, 1);
        }
    }
}
