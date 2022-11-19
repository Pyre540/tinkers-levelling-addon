package pyre.tinkerslevellingaddon.setup;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pyre.tinkerslevellingaddon.ImprovableModifier;
import pyre.tinkerslevellingaddon.TinkersLevellingAddon;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.awt.*;

@Mod.EventBusSubscriber(modid = TinkersLevellingAddon.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    public static final String TOOLTIP_LEVEL_KEY = "modifier.tinkerslevellingaddon.improvable.tooltip.level";
    public static final String TOOLTIP_XP_KEY = "modifier.tinkerslevellingaddon.improvable.tooltip.xp";

    @SubscribeEvent
    static void onTooltipEvent(ItemTooltipEvent event) {
        if (KeyModifier.getActiveModifier() != KeyModifier.NONE) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (ModifierUtil.getModifierLevel(stack, Registration.IMPROVABLE.get().getId()) > 0) {
            ToolStack tool = ToolStack.from(stack);
            ModDataNBT data = tool.getPersistentData();
            int level = data.getInt(ImprovableModifier.LEVEL_KEY);

            MutableComponent levelTooltip = new TranslatableComponent(TOOLTIP_LEVEL_KEY,
                    new TextComponent(getLevelName(level)).withStyle(s -> s.withColor(getLevelColor(level))))
                    .append(new TextComponent(" [" + level + "]").withStyle(ChatFormatting.GRAY));
            //add tooltips under tool durability
            event.getToolTip().add(2, levelTooltip);
            if (ImprovableModifier.canLevelUp(level)) {
                MutableComponent xp = new TextComponent("" + data.getInt(ImprovableModifier.EXPERIENCE_KEY))
                        .withStyle(ChatFormatting.GOLD);
                MutableComponent xpNeeded = new TextComponent("" + ImprovableModifier.getXpNeededForLevel(level + 1,
                        ImprovableModifier.isBroadTool(tool))).withStyle(ChatFormatting.GOLD);
                TranslatableComponent xpTooltip = new TranslatableComponent(TOOLTIP_XP_KEY, xp, xpNeeded);
                event.getToolTip().add(3, xpTooltip);
            }
        }
    }

    private static String getLevelName(int level) {
        if(I18n.exists(TOOLTIP_LEVEL_KEY + "." + level)) {
            return I18n.get(TOOLTIP_LEVEL_KEY + "." + level);
        }

        int i = 1;
        while(I18n.exists(TOOLTIP_LEVEL_KEY + "." + i)) {
            i++;
        }
        return I18n.get(TOOLTIP_LEVEL_KEY + "." + (level % i)) + "+".repeat(level / i);
    }

    private static int getLevelColor(int level) {
        float hue = (0.277777f * level);
        hue = hue - (int) hue;
        return Color.HSBtoRGB(hue, 0.75f, 0.8f);
    }
}
