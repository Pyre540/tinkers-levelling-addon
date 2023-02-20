package pyre.tinkerslevellingaddon.setup;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pyre.tinkerslevellingaddon.ImprovableModifier;
import pyre.tinkerslevellingaddon.TinkersLevellingAddon;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.util.ModUtil;
import pyre.tinkerslevellingaddon.util.ToolLevellingUtil;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = TinkersLevellingAddon.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipEventHandler {
    
    private static final Component TOOLTIP_HOLD_ALT =  ModUtil.makeTranslation("tooltip", "hold_alt",
            ModUtil.makeTranslation("key", "alt", ImprovableModifier.IMPROVABLE_MODIFIER_COLOR)
                    .withStyle(s -> s.withItalic(true)));
    private static final Component TOOLTIP_MODIFIERS_GAINED =
            ModUtil.makeTranslation("tooltip", "info.slots", ImprovableModifier.IMPROVABLE_MODIFIER_COLOR)
                    .withStyle(s -> s.withUnderlined(true));
    private static final Component TOOLTIP_STATS_GAINED =
            ModUtil.makeTranslation("tooltip", "info.stats", ImprovableModifier.IMPROVABLE_MODIFIER_COLOR)
                    .withStyle(s -> s.withUnderlined(true));
    private static final Component TOOLTIP_NEXT_LEVEL =
            ModUtil.makeTranslation("tooltip", "info.next_level", ImprovableModifier.IMPROVABLE_MODIFIER_COLOR)
                    .withStyle(s -> s.withUnderlined(true));

    @SubscribeEvent
    static void onTooltipEvent(ItemTooltipEvent event) {
        KeyModifier activeModifierKey = KeyModifier.getActiveModifier();
        if (event.getPlayer() == null || activeModifierKey == KeyModifier.CONTROL || activeModifierKey == KeyModifier.SHIFT) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (ModifierUtil.getModifierLevel(stack, Registration.IMPROVABLE.get().getId()) <= 0) {
            return;
        }

        for (int i = event.getToolTip().size() - 1; i >= 0; i--) {
            if (event.getToolTip().get(i) == TooltipUtil.TOOLTIP_HOLD_SHIFT ||
                    event.getToolTip().get(i) == TooltipUtil.TOOLTIP_HOLD_CTRL) {
                event.getToolTip().add(i + 1, TOOLTIP_HOLD_ALT);
                break;
            }
        }

        List<Component> infoEntries = new ArrayList<>();
        ToolStack tool = ToolStack.from(stack);
        if (activeModifierKey == KeyModifier.ALT) {
            infoEntries.add(event.getToolTip().get(0));
            infoEntries.addAll(prepareLevelInfo(tool));
            event.getToolTip().clear();
            event.getToolTip().addAll(infoEntries);
        } else {
            infoEntries = prepareGeneralInfo(tool);
            //add tooltips under tool durability
            for (int i = 2; i < infoEntries.size() + 2; i++) {
                event.getToolTip().add(i, infoEntries.get(i - 2));
            }
        }
    }

    private static List<Component> prepareGeneralInfo(ToolStack tool) {
        List<Component> infoEntries = new ArrayList<>();
        int level = tool.getPersistentData().getInt(ImprovableModifier.LEVEL_KEY);
        
        MutableComponent fullLevelName = ModUtil.makeTranslation("tooltip", "level.name", ChatFormatting.GRAY,
                getLevelName(level), ModUtil.makeText(level, ChatFormatting.GRAY));
        infoEntries.add(ModUtil.makeTranslation("tooltip", "level", fullLevelName));

        if (ToolLevellingUtil.canLevelUp(level)) {
            MutableComponent xp = ModUtil.makeText(tool.getPersistentData().getInt(ImprovableModifier.EXPERIENCE_KEY), ChatFormatting.GOLD);
            MutableComponent xpNeeded = ModUtil.makeText(ToolLevellingUtil.getXpNeededForLevel(level + 1, ToolLevellingUtil.isBroadTool(tool)), ChatFormatting.GOLD);
            MutableComponent xpValue = ModUtil.makeTranslation("tooltip", "xp.value", ChatFormatting.GRAY, xp, xpNeeded);
            infoEntries.add(ModUtil.makeTranslation("tooltip","xp", xpValue));
        }
        return infoEntries;
    }

    private static List<Component> prepareLevelInfo(ToolStack tool) {
        List<Component> infoEntries = new ArrayList<>();

        String modifierHistory = tool.getPersistentData().getString(ImprovableModifier.SLOT_HISTORY_KEY);
        if (!modifierHistory.isBlank()) {
            Map<String, Long> gainedModifiers = Arrays.stream(modifierHistory.split(";"))
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            infoEntries.add(TOOLTIP_MODIFIERS_GAINED);
            for (Map.Entry<String, Long> entry : gainedModifiers.entrySet()) {
                MutableComponent value = ModUtil.makeText(entry.getValue(), ToolLevellingUtil.getSlotColor(entry.getKey()));
                infoEntries.add(ModUtil.makeTranslation("tooltip", "info.slots." + entry.getKey(), value));
            }
        }

        String statHistory = tool.getPersistentData().getString(ImprovableModifier.STAT_HISTORY_KEY);
        if (!statHistory.isBlank()) {
            if (!infoEntries.isEmpty()) {
                infoEntries.add(TextComponent.EMPTY);
            }
            Map<String, Double> gainedStats = new LinkedHashMap<>();
            Arrays.stream(statHistory.split(";"))
                    .sorted()
                    .forEach(s -> gainedStats.merge(s, ToolLevellingUtil.getStatValue(tool, s), Double::sum));
            //vanilla multiplies knockback resistance by 10
            gainedStats.computeIfPresent(ToolLevellingUtil.KNOCKBACK_RESISTANCE, (k, v) -> v * 10);
            infoEntries.add(TOOLTIP_STATS_GAINED);
            for (Map.Entry<String, Double> entry : gainedStats.entrySet()) {
                MutableComponent value = ModUtil.makeText(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(entry.getValue()),
                        ToolLevellingUtil.getStatColor(entry.getKey()));
                infoEntries.add(ModUtil.makeTranslation("tooltip", "info.stats." + entry.getKey(), value));
            }
        }

        List<Component> nextLevelInfo = prepareNextLevelInfo(tool);
        if (!infoEntries.isEmpty() && !nextLevelInfo.isEmpty()) {
            infoEntries.add(TextComponent.EMPTY);
        }
        infoEntries.addAll(nextLevelInfo);

        return infoEntries;
    }

    private static List<Component> prepareNextLevelInfo(ToolStack tool) {
        List<Component> infoEntries = new ArrayList<>();
        int level = tool.getPersistentData().getInt(ImprovableModifier.LEVEL_KEY);
        boolean canLevelUp = ToolLevellingUtil.canLevelUp(level);
        boolean knowNextSlot = ToolLevellingUtil.canPredictNextSlot(tool);
        boolean knowNextStat = ToolLevellingUtil.canPredictNextStat(tool);

        if (canLevelUp && (knowNextSlot || knowNextStat)) {
            infoEntries.add(TOOLTIP_NEXT_LEVEL);
            if (knowNextSlot) {
                String nextSlot = ToolLevellingUtil.getSlot(tool, level + 1);
                MutableComponent slot = ModUtil.makeTranslation("tooltip", "slot." + nextSlot,
                        ToolLevellingUtil.getSlotColor(nextSlot));
                infoEntries.add(ModUtil.makeTranslation("tooltip", "info.next_level.slot", slot));
            }
            if (knowNextStat) {
                String nextStat = ToolLevellingUtil.getStat(tool, level + 1);
                double statValue = ToolLevellingUtil.getStatValue(tool, nextStat);
                //vanilla multiplies knockback resistance by 10
                if (nextStat.equals(ToolLevellingUtil.KNOCKBACK_RESISTANCE)) {
                    statValue *= 10;
                }
                MutableComponent value = ModUtil.makeText(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(statValue),
                        ToolLevellingUtil.getStatColor(nextStat));
                MutableComponent name = ModUtil.makeTranslation("tooltip", "stat." + nextStat);
                infoEntries.add(ModUtil.makeTranslation("tooltip", "info.next_level.stat", value, name));
            }
        }

        return infoEntries;
    }

    private static MutableComponent getLevelName(int level) {
        TextColor levelColor = getLevelColor(level);
        if(ModUtil.canTranslate("tooltip", "level." + level)) {
            return ModUtil.makeTranslation("tooltip", "level." + level, levelColor);
        }

        int i = 1;
        while(ModUtil.canTranslate("tooltip", "level." + i)) {
            i++;
        }
        int tier = level / i;
        String suffix = Config.squashLevelPluses.get() && level > 0 ? "+" + tier : "+".repeat(tier);
        return ModUtil.makeTranslation("tooltip", "level." + (level % i), levelColor)
                .append(ModUtil.makeText(suffix, levelColor));
    }

    private static TextColor getLevelColor(int level) {
        float hue = (0.277777f * level);
        hue = hue - (int) hue;
        return TextColor.fromRgb(Color.HSBtoRGB(hue, 0.75f, 0.8f));
    }
}
