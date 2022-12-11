package pyre.tinkerslevellingaddon.setup;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pyre.tinkerslevellingaddon.ImprovableModifier;
import pyre.tinkerslevellingaddon.TinkersLevellingAddon;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.util.SlotAndStatUtil;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static slimeknights.tconstruct.common.TinkerTags.Items.ARMOR;

@Mod.EventBusSubscriber(modid = TinkersLevellingAddon.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    private static final Color IMPROVABLE_MODIFIER_COLOR = Color.fromRgb(9337340);

    private static final String TOOLTIP_LEVEL_KEY = "tooltip.tinkerslevellingaddon.improvable.tooltip.level";
    private static final String TOOLTIP_LEVEL_NAME_KEY = "tooltip.tinkerslevellingaddon.improvable.tooltip.level.";
    private static final String TOOLTIP_XP_KEY = "tooltip.tinkerslevellingaddon.improvable.tooltip.xp";
    private static final String TOOLTIP_HOLD_ALT_KEY = "tooltip.tinkerslevellingaddon.improvable.tooltip.holdAlt";
    private static final String TOOLTIP_ALT_KEY_KEY = "key.tinkerslevellingaddon.alt";

    private static final String TOOLTIP_MODIFIERS_GAINED_KEY = "tooltip.tinkerslevellingaddon.improvable.tooltip.info.modifiers";
    private static final String TOOLTIP_MODIFIERS_KEY = "tooltip.tinkerslevellingaddon.improvable.tooltip.info.modifiers.";
    private static final String TOOLTIP_STATS_GAINED_KEY = "tooltip.tinkerslevellingaddon.improvable.tooltip.info.stats";
    private static final String TOOLTIP_STATS_KEY = "tooltip.tinkerslevellingaddon.improvable.tooltip.info.stats.";
    private static final String TOOLTIP_NEXT_LEVEL_KEY = "tooltip.tinkerslevellingaddon.improvable.tooltip.info.nextLevel";
    private static final String TOOLTIP_NEXT_MODIFIER_KEY = "tooltip.tinkerslevellingaddon.improvable.tooltip.info.nextLevel.modifier";
    private static final String TOOLTIP_NEXT_STAT_KEY = "tooltip.tinkerslevellingaddon.improvable.tooltip.info.nextLevel.stat";

    private static final String TOOLTIP_MODIFIER_KEY = "tooltip.tinkerslevellingaddon.improvable.tooltip.info.modifier.";
    private static final String TOOLTIP_STAT_KEY = "tooltip.tinkerslevellingaddon.improvable.tooltip.info.stat.";

    private static final ITextComponent TOOLTIP_HOLD_ALT = new TranslationTextComponent(TOOLTIP_HOLD_ALT_KEY,
            new TranslationTextComponent(TOOLTIP_ALT_KEY_KEY).withStyle(s -> s.withItalic(true).withColor(IMPROVABLE_MODIFIER_COLOR)));
    private static final ITextComponent TOOLTIP_MODIFIERS_GAINED =
            new TranslationTextComponent(TOOLTIP_MODIFIERS_GAINED_KEY).withStyle(s -> s.withUnderlined(true).withColor(IMPROVABLE_MODIFIER_COLOR));
    private static final ITextComponent TOOLTIP_STATS_GAINED =
            new TranslationTextComponent(TOOLTIP_STATS_GAINED_KEY).withStyle(s -> s.withUnderlined(true).withColor(IMPROVABLE_MODIFIER_COLOR));
    private static final ITextComponent TOOLTIP_NEXT_LEVEL =
            new TranslationTextComponent(TOOLTIP_NEXT_LEVEL_KEY).withStyle(s -> s.withUnderlined(true).withColor(IMPROVABLE_MODIFIER_COLOR));

    @SubscribeEvent
    static void onTooltipEvent(ItemTooltipEvent event) {
        KeyModifier activeModifierKey = KeyModifier.getActiveModifier();
        if (activeModifierKey == KeyModifier.CONTROL || activeModifierKey == KeyModifier.SHIFT) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (ModifierUtil.getModifierLevel(stack, Registration.IMPROVABLE.get()) <= 0) {
            return;
        }

        for (int i = event.getToolTip().size() - 1; i >= 0; i--) {
            if (event.getToolTip().get(i) == TooltipUtil.TOOLTIP_HOLD_SHIFT ||
                    event.getToolTip().get(i) == TooltipUtil.TOOLTIP_HOLD_CTRL) {
                event.getToolTip().add(i + 1, TOOLTIP_HOLD_ALT);
                break;
            }
        }

        List<ITextComponent> infoEntries = new ArrayList<>();
        ToolStack tool = ToolStack.from(stack);
        ModDataNBT data = tool.getPersistentData();
        if (activeModifierKey == KeyModifier.ALT) {
            infoEntries.add(event.getToolTip().get(0));
            infoEntries.addAll(prepareLevelInfo(tool, data));
            event.getToolTip().clear();
            event.getToolTip().addAll(infoEntries);
        } else {
            infoEntries = prepareGeneralInfo(tool, data);
            //add tooltips under tool durability
            for (int i = 2; i < infoEntries.size() + 2; i++) {
                event.getToolTip().add(i, infoEntries.get(i - 2));
            }
        }
    }

    private static List<ITextComponent> prepareGeneralInfo(ToolStack tool, ModDataNBT data) {
        List<ITextComponent> infoEntries = new ArrayList<>();
        int level = data.getInt(ImprovableModifier.LEVEL_KEY);

        ITextComponent levelTooltip = new TranslationTextComponent(TOOLTIP_LEVEL_KEY,
                new StringTextComponent(getLevelName(level)).withStyle(s -> s.withColor(getLevelColor(level))))
                .append(new StringTextComponent(" [" + level + "]").withStyle(TextFormatting.GRAY));
        infoEntries.add(levelTooltip);

        if (ImprovableModifier.canLevelUp(level)) {
            ITextComponent xp = new StringTextComponent("" + data.getInt(ImprovableModifier.EXPERIENCE_KEY))
                    .withStyle(TextFormatting.GOLD);
            ITextComponent xpNeeded = new StringTextComponent("" + ImprovableModifier.getXpNeededForLevel(level + 1,
                    ImprovableModifier.isBroadTool(tool))).withStyle(TextFormatting.GOLD);
            TranslationTextComponent xpTooltip = new TranslationTextComponent(TOOLTIP_XP_KEY, xp, xpNeeded);
            infoEntries.add(xpTooltip);
        }
        return infoEntries;
    }

    private static List<ITextComponent> prepareLevelInfo(ToolStack tool, ModDataNBT data) {
        List<ITextComponent> infoEntries = new ArrayList<>();
        boolean isArmor = tool.hasTag(ARMOR);

        String modifierHistory = data.getString(ImprovableModifier.MODIFIER_HISTORY_KEY);
        if (!modifierHistory.isEmpty()) {
            Map<String, Long> gainedModifiers = Arrays.stream(modifierHistory.split(";"))
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            infoEntries.add(TOOLTIP_MODIFIERS_GAINED);
            for (Map.Entry<String, Long> entry : gainedModifiers.entrySet()) {
                TranslationTextComponent modifierEntry = new TranslationTextComponent(TOOLTIP_MODIFIERS_KEY + entry.getKey(),
                        new StringTextComponent("" + entry.getValue())
                                .withStyle(s -> s.withColor(SlotAndStatUtil.getModifierColor(entry.getKey()))));
                infoEntries.add(modifierEntry);
            }
        }

        String statHistory = data.getString(ImprovableModifier.STAT_HISTORY_KEY);
        if (!statHistory.isEmpty()) {
            if (!infoEntries.isEmpty()) {
                infoEntries.add(StringTextComponent.EMPTY);
            }
            Map<String, Double> gainedStats = Arrays.stream(statHistory.split(";"))
                    .sorted()
                    .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new,
                            Collectors.summingDouble(s -> isArmor ?
                                    Config.getArmorStatValue(SlotAndStatUtil.getStatForName(s)) :
                                    Config.getToolStatValue(SlotAndStatUtil.getStatForName(s)))));
            //vanilla multiplies knockback resistance by 10
            gainedStats.computeIfPresent(SlotAndStatUtil.KNOCKBACK_RESISTANCE, (k, v) -> v * 10);
            infoEntries.add(TOOLTIP_STATS_GAINED);
            for (Map.Entry<String, Double> entry : gainedStats.entrySet()) {
                TranslationTextComponent statEntry = new TranslationTextComponent(TOOLTIP_STATS_KEY + entry.getKey(),
                        new StringTextComponent("" + entry.getValue())
                                .withStyle(s -> s.withColor(SlotAndStatUtil.getStatColor(entry.getKey()))));
                infoEntries.add(statEntry);
            }
        }

        List<ITextComponent> nextLevelInfo = prepareNextLevelInfo(data, isArmor);
        if (!infoEntries.isEmpty() && !nextLevelInfo.isEmpty()) {
            infoEntries.add(StringTextComponent.EMPTY);
        }
        infoEntries.addAll(nextLevelInfo);

        return infoEntries;
    }

    private static List<ITextComponent> prepareNextLevelInfo(ModDataNBT data, boolean isArmor) {
        List<ITextComponent> infoEntries = new ArrayList<>();
        int level = data.getInt(ImprovableModifier.LEVEL_KEY);
        boolean canLevelUp = ImprovableModifier.canLevelUp(level);
        boolean isRandomModifier = isArmor ? Config.armorModifierTypeRandomOrder.get() :
                Config.toolsModifierTypeRandomOrder.get();
        boolean isRandomStat = isArmor ? Config.armorStatTypeRandomOrder.get() : Config.toolsStatTypeRandomOrder.get();

        if (canLevelUp && (Config.enableModifierSlots.get() || Config.enableStats.get()) &&
                (!isRandomModifier || !isRandomStat)) {
            infoEntries.add(TOOLTIP_NEXT_LEVEL);
            if (Config.enableModifierSlots.get() && !isRandomModifier) {
                String nextSlot = isArmor ? SlotAndStatUtil.getArmorSlotForLevel(level + 1) :
                        SlotAndStatUtil.getToolSlotForLevel(level + 1);
                infoEntries.add(new TranslationTextComponent(TOOLTIP_NEXT_MODIFIER_KEY,
                        new TranslationTextComponent(TOOLTIP_MODIFIER_KEY + nextSlot)
                                .withStyle(s -> s.withColor(SlotAndStatUtil.getModifierColor(nextSlot)))));
            }
            if (Config.enableStats.get() && !isRandomStat) {
                String nextStat = isArmor ? SlotAndStatUtil.getArmorStatForLevel(level + 1) :
                        SlotAndStatUtil.getToolStatForLevel(level + 1);
                double statValue = isArmor ? Config.getArmorStatValue(SlotAndStatUtil.getStatForName(nextStat)) :
                        Config.getToolStatValue(SlotAndStatUtil.getStatForName(nextStat));
                //vanilla multiplies knockback resistance by 10
                if (nextStat.equals(SlotAndStatUtil.KNOCKBACK_RESISTANCE)) {
                    statValue *= 10;
                }
                infoEntries.add(new TranslationTextComponent(TOOLTIP_NEXT_STAT_KEY,
                        new StringTextComponent(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(statValue))
                                .withStyle(s -> s.withColor(SlotAndStatUtil.getStatColor(nextStat))),
                        new TranslationTextComponent(TOOLTIP_STAT_KEY + nextStat)));
            }
        }

        return infoEntries;
    }

    private static String getLevelName(int level) {
        if(I18n.exists(TOOLTIP_LEVEL_NAME_KEY + level)) {
            return I18n.get(TOOLTIP_LEVEL_NAME_KEY + level);
        }

        int i = 1;
        while(I18n.exists(TOOLTIP_LEVEL_NAME_KEY + i)) {
            i++;
        }
        StringBuilder plusSigns = new StringBuilder();
        for (int j = 0; j < level / i; j++) {
            plusSigns.append("+");
        }

        return I18n.get(TOOLTIP_LEVEL_NAME_KEY + (level % i)) + plusSigns;
    }

    private static Color getLevelColor(int level) {
        float hue = (0.277777f * level);
        hue = hue - (int) hue;
        return Color.fromRgb(java.awt.Color.HSBtoRGB(hue, 0.75f, 0.8f));
    }
}
