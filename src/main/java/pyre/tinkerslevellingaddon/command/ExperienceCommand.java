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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import pyre.tinkerslevellingaddon.setup.Registration;
import pyre.tinkerslevellingaddon.util.ModUtil;
import pyre.tinkerslevellingaddon.util.ToolLevellingUtil;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.shared.command.HeldModifiableItemIterator;

import java.util.List;

import static pyre.tinkerslevellingaddon.ImprovableModifier.EXPERIENCE_KEY;
import static pyre.tinkerslevellingaddon.ImprovableModifier.LEVEL_KEY;
import static pyre.tinkerslevellingaddon.command.ModCommands.PERMISSION_GAME_COMMANDS;

public class ExperienceCommand {
    
    public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
        subCommand.requires(sender -> sender.hasPermission(PERMISSION_GAME_COMMANDS))
                .then(Commands.argument("targets", EntityArgument.entities())
                        //xp <target> add [<count>]
                        .then(Commands.literal("add")
                                .executes(context -> run(context, ModCommands.Operation.ADD, 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                        .executes(context -> run(context, ModCommands.Operation.ADD))))
                        //xp <target> set <count>
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
        List<LivingEntity> successes = HeldModifiableItemIterator.apply(context, (living, stack) -> {
            if (ModifierUtil.getModifierLevel(stack, Registration.IMPROVABLE.get().getId()) <= 0) {
                return false;
            }
            
            ToolStack tool = ToolStack.copyFrom(stack);
            if (op == ModCommands.Operation.ADD) {
                if(!addXp(tool, count, living)) {
                    throw new SimpleCommandExceptionType(ModUtil.makeTranslation("command", "xp.failure.add.already_max_level", stack.getDisplayName())).create();
                }
            } else {
                if (!setXp(tool, count, living)) {
                    throw new SimpleCommandExceptionType(ModUtil.makeTranslation("command", "xp.failure.set.already_max_level", stack.getDisplayName())).create();
                }
            }
            
            Component error = tool.tryValidate();
            if (error != null) {
                throw ModCommands.TOOL_VALIDATION_ERROR.create(error);
            }
            
            living.setItemInHand(InteractionHand.MAIN_HAND, tool.createStack(stack.getCount()));
            return true;
        });
        
        // success message
        CommandSourceStack source = context.getSource();
        int size = successes.size();
        if (op == ModCommands.Operation.ADD) {
            if (size == 1) {
                source.sendSuccess(ModUtil.makeTranslation("command", "xp.success.add.single", count,
                        successes.get(0).getDisplayName()), true);
            } else {
                source.sendSuccess(ModUtil.makeTranslation("command", "xp.success.add.multiple", count, size), true);
            }
        } else {
            if (size == 1) {
                source.sendSuccess(ModUtil.makeTranslation("command", "xp.success.set.single", count,
                        successes.get(0).getDisplayName()), true);
            } else {
                source.sendSuccess(ModUtil.makeTranslation("command", "xp.success.set.multiple", count, size), true);
            }
        }
        return size;
    }
    
    private static boolean addXp(ToolStack tool, int count, LivingEntity living) {
        ServerPlayer player = living instanceof ServerPlayer p ? p : null;
    
        if (ToolLevellingUtil.canLevelUp(tool.getPersistentData().getInt(LEVEL_KEY))) {
            ToolLevellingUtil.addExperience(tool, count, player);
            return true;
        }
        return false;
    }
    
    private static boolean setXp(ToolStack tool, int count, LivingEntity living) {
        int currentLevel = tool.getPersistentData().getInt(LEVEL_KEY);
        boolean isBroad = ToolLevellingUtil.isBroadTool(tool);
        ServerPlayer player = living instanceof ServerPlayer p ? p : null;
        
        if (ToolLevellingUtil.canLevelUp(currentLevel)) {
            int neededXp = ToolLevellingUtil.getXpNeededForLevel(currentLevel, isBroad);
            int currentXp = tool.getPersistentData().getInt(EXPERIENCE_KEY);
            int xp = Math.max(1, Math.min(count, neededXp - currentXp));
            ToolLevellingUtil.addExperience(tool, xp, player);
            return true;
        }
        return false;
    }
}
