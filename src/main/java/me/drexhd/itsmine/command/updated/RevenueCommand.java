package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.Messages;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.ItemUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RevenueCommand extends Command {

    public RevenueCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new RevenueCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> confirm = LiteralArgumentBuilder.literal("confirm");
        confirm.executes(context -> revenue(context, true));
        literal().requires(source -> ItsMine.permissions().hasPermission(source, "itsmine." + "rent", 2));
        literal().then(thenClaim(confirm));
        command.then(literal());
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return revenue(context, false);
    }

    public int revenue(CommandContext<ServerCommandSource> context, boolean claimrevenue) throws CommandSyntaxException {
        //TODO:
        // Show subzones (so you can just claim everything in one place) maybe just all claims
        Claim claim = getClaim(context);
        ServerCommandSource source = context.getSource();
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
                        style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to claim revenue!").formatted(Formatting.GREEN)))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/claim revenue " + claim.name + " true")));
                color.set(!color.get());
            });
            text.append(new LiteralText("\n"));
            source.sendFeedback(text, true);
            return 1;
        }
    }
}
