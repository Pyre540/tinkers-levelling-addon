package pyre.tinkerslevellingaddon.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import pyre.tinkerslevellingaddon.config.Config;

public class ClientPacketHandler {

    public static void handleLevelUpMessage(int level, ITextComponent toolName) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (Config.enableLevelUpMessage.get()) {
            TranslationTextComponent message;
            if (I18n.exists("message.tinkerslevellingaddon.levelUp." + level)) {
                message = new TranslationTextComponent("message.tinkerslevellingaddon.levelUp." + level, toolName);
            } else {
                ITextComponent levelComponent = new StringTextComponent(String.valueOf(level))
                        .withStyle(s -> s.withColor(TextFormatting.GOLD));
                message = new TranslationTextComponent("message.tinkerslevellingaddon.levelUp.generic", toolName, levelComponent);
            }
            message.withStyle(style -> style.withColor(Color.fromRgb(9337340)));
            player.sendMessage(message, Util.NIL_UUID);
        }
        SoundEvent soundEvent = Config.levelUpSound.get().getSoundEvent();
        if (soundEvent != null) {
            player.level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    soundEvent, player.getSoundSource(), 1, 1);
        }
    }
}
