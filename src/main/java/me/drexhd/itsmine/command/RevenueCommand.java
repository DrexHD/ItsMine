package me.drexhd.itsmine.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.Messages;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.util.ItemUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RevenueCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, RequiredArgumentBuilder<ServerCommandSource, String> claim) {
        LiteralArgumentBuilder<ServerCommandSource> revenue = literal("revenue");
        RequiredArgumentBuilder<ServerCommandSource, Boolean> claimRevenue = argument("claimRevenue", BoolArgumentType.bool());
        revenue.executes(context -> revenue(context.getSource(), ClaimManager.INSTANCE.getClaimAt(new BlockPos(context.getSource().getPosition()), context.getSource().getWorld().getDimension()), false));
        revenue.requires(source -> ItsMine.permissions().hasPermission(source, "itsmine." + "rent", 2));
        claim.executes(context -> revenue(context.getSource(), ClaimManager.INSTANCE.getClaim(context.getSource().getPlayer().getUuid(), getString(context, "claim")), false));
        claimRevenue.executes(context -> revenue(context.getSource(), ClaimManager.INSTANCE.getClaim(context.getSource().getPlayer().getUuid(), getString(context, "claim")), true));
        claim.then(claimRevenue);
        revenue.then(claim);
        command.then(revenue);
    }

    private static int revenue(ServerCommandSource source, Claim claim, boolean claimrevenue) throws CommandSyntaxException {
        //Show subzones (so you can just claim everything in one place) maybe just all claims
        if (claim == null) {
            source.sendFeedback(Messages.INVALID_CLAIM, true);
            return 0;
        }
        if (!claim.claimBlockOwner.toString().equalsIgnoreCase(source.getPlayer().getUuid().toString())) {
            source.sendFeedback(Messages.NO_PERMISSION, true);
            return 0;
        }

        if (claimrevenue) {
            for (ItemStack itemStack : claim.rentManager.getRevenue()) {
                source.getPlayer().inventory.insertStack(itemStack);
            }
            claim.rentManager.clearRevenue();
            return 1;
        } else {
            if (claim.rentManager.getRevenue().isEmpty()) {
                source.sendFeedback(new LiteralText("No Revenue").formatted(Formatting.RED), true);
                return 0;
            }
            MutableText text = new LiteralText("Revenue\n").formatted(Formatting.AQUA);
            HashMap<Item, Integer> hashMap = new HashMap<>();
            for (ItemStack itemStack : claim.rentManager.getRevenue()) {
                Item item = itemStack.getItem();
                if (hashMap.containsKey(item)) {
                    int i = hashMap.remove(item);
                    hashMap.put(item, i + itemStack.getCount());
                } else {
                    hashMap.put(item, itemStack.getCount());
                }

            }
            AtomicBoolean color = new AtomicBoolean(true);
            hashMap.forEach((currency, integer) -> {
                text.append(new LiteralText(String.valueOf(integer))
                        .append(new LiteralText(" "))
                        .append(new LiteralText(ItemUtil.toName(currency, integer)))
                        .append(new LiteralText(" ")).formatted(color.get() ? Formatting.GOLD : Formatting.YELLOW)).styled(style ->
                        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to claim revenue!").formatted(Formatting.GREEN)))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/claim revenue " + claim.name + " true")));
                color.set(!color.get());
            });
            text.append(new LiteralText("\n"));
            source.sendFeedback(text, true);
            return 1;
        }
    }

}
