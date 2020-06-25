package me.drexhd.itsmine.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.util.*;
import net.minecraft.command.arguments.ItemStackArgument;
import net.minecraft.command.arguments.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.literal;

public class RentableCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, RequiredArgumentBuilder<ServerCommandSource, String> claim) {
        LiteralArgumentBuilder<ServerCommandSource> rentable = literal("rentable");
        RequiredArgumentBuilder<ServerCommandSource, ItemStackArgument> currency = net.minecraft.server.command.CommandManager.argument("item", ItemStackArgumentType.itemStack()).suggests(ArgumentUtil::itemsSuggestion);
        RequiredArgumentBuilder<ServerCommandSource, Integer> amount = net.minecraft.server.command.CommandManager.argument("count", IntegerArgumentType.integer(1));
        RequiredArgumentBuilder<ServerCommandSource, String> days = net.minecraft.server.command.CommandManager.argument("rent", word());
        RequiredArgumentBuilder<ServerCommandSource, String> maxdays = CommandManager.argument("maxrent", word());
        maxdays.executes(RentableCommand::makeRentable/*context -> makeRentable(context.getSource(), ClaimManager.INSTANCE.getClaim(getString(context, "claim")), ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false), IntegerArgumentType.getInteger(context, "count"), getString(context, "rent"), getString(context, "maxrent"))*/);
        rentable.requires(source -> ItsMine.permissions().hasPermission(source, "itsmine." + "rent", 2));
        claim.executes(RentableCommand::toggle/*{
            Claim claim1 = ClaimManager.INSTANCE.getClaim(getString(context, "claim"));
            if(claim1.rentManager.getCurrency() != null || claim1.rentManager.getAmount() != 0 || claim1.rentManager.getRentAbleTime() != 0 || claim1.rentManager.getMax() != 0 && claim1.rentManager.isRentable()) {
                context.getSource().sendFeedback(new LiteralText("Can't enable rent for " + claim1.name + ", because no values are set").formatted(Formatting.RED), true);
                return 0;
            }
            if(claim1.rentManager.getTenant() == null){
                String state = claim1.rentManager.isRentable() ? "disabled" : "enabled";
                claim1.rentManager.setRentable(!claim1.rentManager.isRentable());
                context.getSource().sendFeedback(new LiteralText("Renting for " + claim1.name + " has been " + state).formatted(Formatting.GREEN), true);
                return 1;
            } else {
                context.getSource().sendFeedback(new LiteralText("Can't disable rent for " + claim1.name + ", because it is currently being rented").formatted(Formatting.RED), true);
                return 0;
            }
        }*/);

        days.then(maxdays);
        amount.then(days);
        currency.then(amount);
        claim.then(currency);
        rentable.then(claim);
        command.then(rentable);
    }

    private static int makeRentable(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int min = TimeUtil.convertStringtoSeconds(getString(context, "rent"));
        int max = TimeUtil.convertStringtoSeconds(getString(context, "maxrent"));
        int configMax = ItsMineConfig.main().rent().maxRentTime;
        max = Math.min(max, configMax);
        ItemStack revenue = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(IntegerArgumentType.getInteger(context, "count"), false);
        Claim claim = ClaimManager.INSTANCE.getClaim(getString(context, "claim"));
        ClaimUtil.validateClaim(claim);
        if(claim.rentManager.isTimeValid(min, max)) {
            claim.rentManager.makeRentable(revenue, min, max);
            MessageUtil.sendTranslatableMessage(context.getSource(), MessageUtil.createMap("%item%", ItemUtil.toName(revenue, revenue.getCount()),"%amount%", String.valueOf(revenue.getCount()), "%claim%", claim.name, "%time%", TimeUtil.convertSecondsToString(min)), "messages", "makeRentable");
            return 1;
        } else {
            MessageUtil.sendTranslatableMessage(context.getSource(), "messages", "invalidRentTime");
            return 0;
        }
    }

/*    private static int makeRentable(ServerCommandSource source, Claim claim, ItemStack item, int amount, String rentString, String maxrentString) throws CommandSyntaxException {
        int rentTime = TimeUtil.convertStringtoSeconds(rentString);
        int maxrentTime = TimeUtil.convertStringtoSeconds(maxrentString);
        if(claim != null){
            if(rentTime <= maxrentTime){
                RentManager rent = claim.rentManager;
                rent.setRentable(true);
                item.setCount(amount);
                rent.setCurrency(item);
                rent.setRentAbleTime(rentTime);
                if(maxrentTime > ItsMineConfig.main().rent().maxRentTime) maxrentTime = ItsMineConfig.main().rent().maxRentTime;
                rent.setMax(maxrentTime);
                source.sendFeedback(new LiteralText("Claim " + claim.name + " can now be rented for " + amount + " " ).formatted(Formatting.GREEN).append(new TranslatableText(item.getTranslationKey())).append(new LiteralText(" every " + TimeUtil.convertSecondsToString(rentTime)).formatted(Formatting.GREEN)), true);
                return 1;
            }
        }
        return 0;
    }*/

    private static int toggle(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = ClaimManager.INSTANCE.getClaim(getString(context, "claim"));
        ClaimUtil.validateClaim(claim);
        if(claim.rentManager.isReady()) {
            String state = claim.rentManager.toggle() ? "&aenabled" : "&cdisabled";
            MessageUtil.sendTranslatableMessage(context.getSource(), MessageUtil.createMap("%value%", state, "%claim%", claim.name), "messages", "toggleRent");
            return 1;
        } else {
            MessageUtil.sendTranslatableMessage(context.getSource(), MessageUtil.createMap("%claim%", claim.name), "messages", "toggleRentError");
            return 0;
        }
    }

}
