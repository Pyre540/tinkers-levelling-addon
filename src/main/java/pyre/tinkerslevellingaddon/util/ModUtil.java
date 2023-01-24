package pyre.tinkerslevellingaddon.util;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import pyre.tinkerslevellingaddon.TinkersLevellingAddon;

public class ModUtil {
    
    public static ResourceLocation getResource(String name) {
        return new ResourceLocation(TinkersLevellingAddon.MOD_ID, name);
    }
    
    public static boolean canTranslate(String base, String name) {
        return I18n.exists(Util.makeDescriptionId(base, getResource(toSnakeCase(name))));
    }
    
    public static MutableComponent makeTranslation(String base, String name) {
        return new TranslatableComponent(Util.makeDescriptionId(base, getResource(toSnakeCase(name))));
    }
    
    public static MutableComponent makeTranslation(String base, String name, TextColor color) {
        return new TranslatableComponent(Util.makeDescriptionId(base, getResource(toSnakeCase(name))))
                .withStyle(s -> s.withColor(color));
    }
    
    public static MutableComponent makeTranslation(String base, String name, Object... arguments) {
        return new TranslatableComponent(Util.makeDescriptionId(base, getResource(toSnakeCase(name))), arguments);
    }
    
    public static MutableComponent makeTranslation(String base, String name, ChatFormatting color, Object... arguments) {
        return new TranslatableComponent(Util.makeDescriptionId(base, getResource(toSnakeCase(name))), arguments)
                .withStyle(color);
    }
    
    public static MutableComponent makeText(Number value, ChatFormatting color) {
        return new TextComponent(value.toString()).withStyle(color);
    }
    
    public static MutableComponent makeText(String text, TextColor color) {
        return new TextComponent(text).withStyle(s -> s.withColor(color));
    }
    
    public static MutableComponent makeText(Number value, TextColor color) {
        return new TextComponent(value.toString()).withStyle(s -> s.withColor(color));
    }
    
    private static String toSnakeCase(String text) {
        return text.replaceAll("((?!<_)([A-Z]))", "_$1").toLowerCase();
    }
    
    private ModUtil() {
        //hide constructor
    }
}
