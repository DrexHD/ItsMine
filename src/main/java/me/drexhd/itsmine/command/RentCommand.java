package me.drexhd.itsmine.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.util.ClaimUtil;
import me.drexhd.itsmine.util.ItemUtil;
import me.drexhd.itsmine.util.MessageUtil;
import me.drexhd.itsmine.util.TimeUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;

import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RentCommand {
    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, RequiredArgumentBuilder<ServerCommandSource, String> claim) {
        LiteralArgumentBuilder<ServerCommandSource> rent = literal("rent");
        RequiredArgumentBuilder<ServerCommandSource, String> days = argument("time", word());
        days.executes(RentCommand::rent);
        rent.requires(source -> ItsMine.permissions().hasPermission(source, "itsmine." + "rent", 2));
        claim.then(days);
        rent.then(claim);
        command.then(rent);
    }

    private static int rent(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = ClaimManager.INSTANCE.getClaim(context.getSource().getPlayer().getUuid(), getString(context, "claim"));
        ClaimUtil.validateClaim(claim);
        int rent = TimeUtil.convertStringtoSeconds(getString(context, "time"));
        ServerCommandSource source = context.getSource();
        UUID uuid = source.getPlayer().getUuid();
        if(!claim.rentManager.isRentable()) {
            MessageUtil.sendTranslatableMessage(context.getSource(), MessageUtil.createMap("%claim%", claim.name), ItsMineConfig.main().message().notForRent);
            return 0;
        }
        if (claim.rentManager.isRented()) {
            /*Extend claim rent*/
            if (claim.rentManager.canExtend(uuid)) {
                if (claim.rentManager.wouldExceed(rent)) {
                    MessageUtil.sendTranslatableMessage(context.getSource(), "messages", "invalidRentExtendTime1");
                    return 0;
                } else if (!claim.rentManager.isTimeValid(rent)) {
                    MessageUtil.sendTranslatableMessage(context.getSource(), "messages", "invalidRentExtendTime2");
                    return 0;
                } else {
                    ItemStack hand = source.getPlayer().getMainHandStack();
                    if (claim.rentManager.hasEnough(hand, rent)) {
                        hand.setCount(claim.rentManager.removeItemStack(hand, rent).getCount());
                        int amount = claim.rentManager.extend(rent);
                        MessageUtil.sendTranslatableMessage(context.getSource(), MessageUtil.createMap("%claim%", claim.name, "%time%", TimeUtil.convertSecondsToString(rent), "%amount%", String.valueOf(amount), "%item%", ItemUtil.toName(claim.rentManager.getCurrency(), amount)), ItsMineConfig.main().message().extendRent);
                        return 1;
                    } else {
                        MessageUtil.sendTranslatableMessage(context.getSource(), MessageUtil.createMap("%item%", ItemUtil.toName(claim.rentManager.getCurrency(), 2)), ItsMineConfig.main().message().notEnough);
                        return 0;
                    }
                }
            } else {
                MessageUtil.sendTranslatableMessage(context.getSource(), "messages", "alreadyRented");
                return 0;
            }
        } else {
            /*Rent claim*/
            if (claim.rentManager.wouldExceed(rent)) {
                MessageUtil.sendTranslatableMessage(context.getSource(), "messages", "invalidRentExtendTime1");
                return 0;
            } else if (!claim.rentManager.isTimeValid(rent)) {
                MessageUtil.sendTranslatableMessage(context.getSource(), "messages", "invalidRentExtendTime2");
                return 0;
            } else {
                ItemStack hand = source.getPlayer().getMainHandStack();
                if (claim.rentManager.hasEnough(hand, rent)) {
                    hand.setCount(claim.rentManager.removeItemStack(hand, rent).getCount());
                    int amount = claim.rentManager.rent(uuid, rent);
                    MessageUtil.sendTranslatableMessage(context.getSource(), MessageUtil.createMap("%claim%", claim.name, "%time%", TimeUtil.convertSecondsToString(rent), "%amount%", String.valueOf(amount), "%item%", ItemUtil.toName(claim.rentManager.getCurrency(), amount)), ItsMineConfig.main().message().rent);
                    return 1;
                } else {
                    MessageUtil.sendTranslatableMessage(context.getSource(), MessageUtil.createMap("%item%", ItemUtil.toName(claim.rentManager.getCurrency(), 2)), ItsMineConfig.main().message().notEnough);
                    return 0;
                }
            }
        }
    }

/*    private static int rent(ServerCommandSource source, Claim claim, String rentString) throws CommandSyntaxException {
        if (claim == null) {
            source.sendFeedback(Messages.INVALID_CLAIM, true);
            return 0;
        }
        int rentTime = TimeUtil.convertStringtoSeconds(rentString);
        int rentAbleTime = claim.rentManager.getRentAbleTime();
        int maxrentAbleTime = claim.rentManager.getMax();
        ItemStack currency = claim.rentManager.getCurrency();
        ItemStack handItem = source.getPlayer().inventory.getMainHandStack();
        ItemStack revenue = handItem.copy();
        if (rentTime % claim.rentManager.getRentAbleTime() != 0) {
            source.sendFeedback(new LiteralText("You have to rent this claim for a time by a multiple of " + TimeUtil.convertSecondsToString(rentAbleTime, 'c', 'c')).formatted(Formatting.RED), true);
            return 0;
        }
        int rentAmount = rentTime / claim.rentManager.getRentAbleTime();
        if (currency.getItem() != handItem.getItem() || handItem.getCount() < claim.rentManager.getAmount() * rentAmount) {
            source.sendFeedback(new LiteralText("You don't have enough " + new TranslatableText(currency.getTranslationKey())).formatted(Formatting.RED), false);
            return 0;
        }
        if (!claim.rentManager.isRentable()) {
            source.sendFeedback(new LiteralText(claim.name + " is not for rent").formatted(Formatting.RED), true);
            return 0;
        }
        if (rentTime > claim.rentManager.getMax()) {
            source.sendFeedback(new LiteralText("You can't rent this claim for longer than " + TimeUtil.convertSecondsToString(maxrentAbleTime, 'c', 'c')).formatted(Formatting.RED), true);
            return 0;
        }
        if (claim.rentManager.getTenant() == null) {
            //Setup for claim rent
            claim.rentManager.setTenant(source.getPlayer().getUuid());
            claim.rentManager.setUntil(claim.rentManager.getUnixTime() + rentTime);
            //Remove items from player
            handItem.setCount(handItem.getCount() - claim.rentManager.getAmount() * rentAmount);
            revenue.setCount(claim.rentManager.getAmount() * rentAmount);
            claim.rentManager.addRevenue(revenue);
            source.sendFeedback(new LiteralText("Renting " + claim.name + " for " + claim.rentManager.getAmount() * rentAmount + " " + claim.rentManager.getCurrency().getName().asString() + " for " + TimeUtil.convertSecondsToString(rentTime, '2', 'a')).formatted(Formatting.GREEN), true);
            return 1;
        } else if (claim.rentManager.getTenant().toString().equalsIgnoreCase(source.getPlayer().getUuid().toString())) {
            if (claim.rentManager.getTimeLeft() + rentTime <= maxrentAbleTime) {
                //Setup for claim rent
                claim.rentManager.setUntil(claim.rentManager.getUnixTime() + rentTime + claim.rentManager.getTimeLeft());
                //Remove items from player
                handItem.setCount(handItem.getCount() - claim.rentManager.getAmount() * rentAmount);
                revenue.setCount(claim.rentManager.getAmount() * rentAmount);
                claim.rentManager.addRevenue(revenue);
                source.sendFeedback(new LiteralText("Extended rent " + claim.name + " by " + TimeUtil.convertSecondsToString(rentTime, '2', 'a') + "for " + claim.rentManager.getAmount() * rentAmount + " ").formatted(Formatting.GREEN).append(new TranslatableText(claim.rentManager.getCurrency().getTranslationKey())).formatted(Formatting.GREEN), true);
                return 1;
            } else {
                source.sendFeedback(new LiteralText("Rent would exceed the limit by " + TimeUtil.convertSecondsToString(claim.rentManager.getTimeLeft() + rentTime - maxrentAbleTime, 'c', 'c')).formatted(Formatting.RED), true);
                return 0;
            }
        } else {
            source.sendFeedback(new LiteralText("This claim is already rented").formatted(Formatting.RED), true);
            return 0;
        }
    }*/

}
