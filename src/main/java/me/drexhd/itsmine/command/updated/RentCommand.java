package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.ItemUtil;
import me.drexhd.itsmine.util.MessageUtil;
import me.drexhd.itsmine.util.TimeUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;

import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;

public class RentCommand extends Command {

    public RentCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new RentCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        this.others = true;
        RequiredArgumentBuilder<ServerCommandSource, String> days = argument("time", word());
        days.executes(this::rent);

        literal().requires(source -> ItsMine.permissions().hasPermission(source, "itsmine." + "rent", 2));
        literal().then(thenClaim(days));
        command.then(literal());
    }


    public int rent(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
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
}
