package pyre.tinkerslevellingaddon.network;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import pyre.tinkerslevellingaddon.ImprovableModifier;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.util.ModUtil;

public class ClientPacketHandler {

    public static void handleLevelUpMessage(int level, Component toolName) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (Config.enableLevelUpMessage.get()) {
            MutableComponent message;
            if (ModUtil.canTranslate("message", "level_up." + level)) {
                message = ModUtil.makeTranslation("message", "level_up." + level, toolName);
            } else {
                MutableComponent levelComponent = ModUtil.makeText(level, ChatFormatting.GOLD);
                message = ModUtil.makeTranslation("message", "level_up.generic", toolName, levelComponent);
            }
            message.withStyle(style -> style.withColor(ImprovableModifier.IMPROVABLE_MODIFIER_COLOR));
            player.sendMessage(message, Util.NIL_UUID);
        }
        SoundEvent soundEvent = Config.levelUpSound.get().getSoundEvent();
        if (soundEvent != null) {
            player.getLevel().playSound(player, player.getX(), player.getY(), player.getZ(),
                    soundEvent, player.getSoundSource(), 1, 1);
        }
    }
}
