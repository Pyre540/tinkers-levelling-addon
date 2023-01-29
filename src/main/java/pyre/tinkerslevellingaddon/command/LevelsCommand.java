package pyre.tinkerslevellingaddon.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.setup.Registration;
import pyre.tinkerslevellingaddon.util.ModUtil;
import pyre.tinkerslevellingaddon.util.ToolLevellingUtil;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.shared.command.HeldModifiableItemIterator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static pyre.tinkerslevellingaddon.ImprovableModifier.*;
import static pyre.tinkerslevellingaddon.command.ModCommands.PERMISSION_GAME_COMMANDS;
import static pyre.tinkerslevellingaddon.command.ModCommands.TOOL_VALIDATION_ERROR;

public class LevelsCommand {
    
    public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
        subCommand.requires(sender -> sender.hasPermission(PERMISSION_GAME_COMMANDS))
                .then(Commands.argument("targets", EntityArgument.entities())
                        // levels <target> add [<count>]
                        .then(Commands.literal("add")
                                .executes(context -> run(context, ModCommands.Operation.ADD, 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                        .executes(context -> run(context, ModCommands.Operation.ADD))))
                        // levels <target> set <count>
                        .then(Commands.literal("set")
                                .then(Commands.argument("count", IntegerArgumentType.integer(0))
                                        .executes(context -> run(context, ModCommands.Operation.SET)))));
    }
    
    private static int run(CommandContext<CommandSourceStack> context, ModCommands.Operation op)
            throws CommandSyntaxException {
        return run(context, op, IntegerArgumentType.getInteger(context, "count"));
    }
    
    private static int run(CommandContext<CommandSourceStack> context, ModCommands.Operation op, int count)
            throws CommandSyntaxException {
        if (count > Config.maxLevel.get() && op == ModCommands.Operation.SET) {
            throw new SimpleCommandExceptionType(ModUtil.makeTranslation("command", "levels.failure.set.invalid_count", Config.maxLevel.get())).create();
        }
        
        List<LivingEntity> successes = HeldModifiableItemIterator.apply(context, (living, stack) -> {
            if (ModifierUtil.getModifierLevel(stack, Registration.IMPROVABLE.get().getId()) <= 0) {
                return false;
            }
            
            ToolStack tool = ToolStack.copyFrom(stack);
            if (op == ModCommands.Operation.ADD) {
                int levelsAdded = addLevel(tool, count, living);
                if (levelsAdded == 0) {
                    throw new SimpleCommandExceptionType(ModUtil.makeTranslation("command", "levels.failure.add.already_max_level", stack.getDisplayName())).create();
                }
            } else {
                if (!setLevel(tool, count)) {
                    throw new SimpleCommandExceptionType(ModUtil.makeTranslation("command", "levels.failure.set.current_level", stack.getDisplayName(), count)).create();
                }
                
            }
            
            Component error = tool.tryValidate();
            if (error != null) {
                throw TOOL_VALIDATION_ERROR.create(error);
            }
            
            living.setItemInHand(InteractionHand.MAIN_HAND, tool.createStack(stack.getCount()));
            return true;
        });
        
        // success message
        CommandSourceStack source = context.getSource();
        int size = successes.size();
        if (op == ModCommands.Operation.ADD) {
            if (size == 1) {
                source.sendSuccess(ModUtil.makeTranslation("command", "levels.success.add.single", count,
                        successes.get(0).getDisplayName()), true);
            } else {
                source.sendSuccess(ModUtil.makeTranslation("command", "levels.success.add.multiple", count, size), true);
            }
        } else {
            if (size == 1) {
                source.sendSuccess(ModUtil.makeTranslation("command", "levels.success.set.single", count,
                        successes.get(0).getDisplayName()), true);
            } else {
                source.sendSuccess(ModUtil.makeTranslation("command", "levels.success.set.multiple", count, size), true);
            }
        }
        return size;
    }
    
    private static int addLevel(ToolStack tool, int count, LivingEntity living) {
        int levelsAdded;
        int currentLevel = tool.getPersistentData().getInt(LEVEL_KEY);
        boolean isBroad = ToolLevellingUtil.isBroadTool(tool);
        ServerPlayer player = living instanceof ServerPlayer p ? p : null;
    
        for (levelsAdded = 0; levelsAdded < count; levelsAdded++) {
            if (ToolLevellingUtil.canLevelUp(currentLevel)) {
                int xp = ToolLevellingUtil.getXpNeededForLevel(currentLevel + 1, isBroad);
                ToolLevellingUtil.addExperience(tool, xp, player);
                currentLevel++;
            } else {
                break;
            }
        }
        return levelsAdded;
    }
    
    private static boolean setLevel(ToolStack tool, int count) {
        ModDataNBT data = tool.getPersistentData();
        int currentLevel = data.getInt(LEVEL_KEY);
        int levelDiff = count - currentLevel;
        
        if (levelDiff == 0) {
            return false;
        }
        if (levelDiff > 0) {
            addLevel(tool, levelDiff, null);
        } else {
            data.putInt(LEVEL_KEY, count);
            trimHistory(SLOT_HISTORY_KEY, data, count);
            trimHistory(STAT_HISTORY_KEY, data, count);
        }
        tool.rebuildStats();
        return true;
    }
    
    private static void trimHistory(ResourceLocation historyKey, ModDataNBT data, int size) {
        String newHistory = Arrays.stream(data.getString(historyKey).split(";"))
                .limit(size)
                .collect(Collectors.joining(";"));
        if (!newHistory.isBlank()) {
            newHistory = newHistory + ";";
        }
        data.putString(historyKey, newHistory);
    }
}
