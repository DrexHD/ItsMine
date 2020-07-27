package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.ArgumentUtil;
import me.drexhd.itsmine.util.ItemUtil;
import me.drexhd.itsmine.util.MessageUtil;
import me.drexhd.itsmine.util.TimeUtil;
import net.minecraft.command.arguments.ItemStackArgument;
import net.minecraft.command.arguments.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;

public class RentableCommand extends Command implements Admin, Other, Subzone {

    public RentableCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new RentableCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, ItemStackArgument> currency = net.minecraft.server.command.CommandManager.argument("item", ItemStackArgumentType.itemStack()).suggests(ArgumentUtil::itemsSuggestion);
        RequiredArgumentBuilder<ServerCommandSource, Integer> amount = net.minecraft.server.command.CommandManager.argument("count", IntegerArgumentType.integer(1));
//        RequiredArgumentBuilder<ServerCommandSource, String> days = net.minecraft.server.command.CommandManager.argument("rent", word());
        RequiredArgumentBuilder<ServerCommandSource, String> days = timeArgument("rent");
        RequiredArgumentBuilder<ServerCommandSource, String> maxdays = timeArgument("maxrent");
//        RequiredArgumentBuilder<ServerCommandSource, String> maxdays = CommandManager.argument("maxrent", word());
        maxdays.executes(this::makeRentable);

        days.then(maxdays);
        amount.then(days);
        currency.then(amount);
        literal().requires(source -> ItsMine.permissions().hasPermission(source, "itsmine.rent", 2));
        literal().then(thenClaim(currency));
        command.then(literal());
    }

    public int makeRentable(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
//        int min = TimeUtil.convertStringtoSeconds(getString(context, "rent"));
        int min = parseTime(context, "rent");
//        int max = TimeUtil.convertStringtoSeconds(getString(context, "maxrent"));
        int max = parseTime(context, "maxrent");
        int configMax = ItsMineConfig.main().rent().maxRentTime;
        max = Math.min(max, configMax);
        ItemStack revenue = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(IntegerArgumentType.getInteger(context, "count"), false);
        Claim claim = getClaim(context);
        validatePermission(claim, context.getSource().getPlayer().getUuid(), "modify", "rent");
//        if (claim.hasPermission(context.getSource().getPlayer().getUuid(), "modify", "rent") || admin) {
        if (claim.rentManager.isTimeValid(min, max)) {
            claim.rentManager.makeRentable(revenue, min, max);
            MessageUtil.sendTranslatableMessage(context.getSource(), MessageUtil.createMap("%item%", ItemUtil.toName(revenue, revenue.getCount()), "%amount%", String.valueOf(revenue.getCount()), "%claim%", claim.name, "%time%", TimeUtil.convertSecondsToString(min)), ItsMineConfig.main().message().makeRentable);
            return 1;
        } else {
            MessageUtil.sendTranslatableMessage(context.getSource(), ItsMineConfig.main().message().invalidRentTime);
            return -1;
        }
//        } else {
//            MessageUtil.sendMessage(context.getSource(), ItsMineConfig.main().message().noPermission);
//            return -1;
//        }
    }


    @Override
    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        validatePermission(claim, context.getSource().getPlayer().getUuid(), "modify", "rent");
//        if (claim.hasPermission(context.getSource().getPlayer().getUuid(), "modify", "rent") || admin) {
        if (claim.rentManager.isReady()) {
            String state = claim.rentManager.toggle() ? "&aenabled" : "&cdisabled";
            MessageUtil.sendTranslatableMessage(context.getSource(), MessageUtil.createMap("%value%", state, "%claim%", claim.name), ItsMineConfig.main().message().toggleRent);
            return 1;
        } else {
            MessageUtil.sendTranslatableMessage(context.getSource(), MessageUtil.createMap("%claim%", claim.name), ItsMineConfig.main().message().rentNoValues);
            return 0;
        }
//        } else {
//            MessageUtil.sendMessage(context.getSource(), ItsMineConfig.main().message().noPermission);
//            return -1;
//        }
    }
}
