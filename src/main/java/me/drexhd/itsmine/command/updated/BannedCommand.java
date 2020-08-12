package me.drexhd.itsmine.command.updated;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.concurrent.atomic.AtomicInteger;

public class BannedCommand extends Command implements Other, Subzone {
    public BannedCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new BannedCommand(literal);
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        literal().then(thenClaim());
        literal().executes(this::execute);
        command.then(literal());
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        MutableText text = new LiteralText("");
        text.append(new LiteralText("Banned players for Claim ").formatted(Formatting.YELLOW))
                .append(new LiteralText(claim.name).formatted(Formatting.GOLD)).append(new LiteralText("\n"));

        AtomicInteger atomicInteger = new AtomicInteger();
        claim.banManager.getBannedPlayers().forEach((uuid) -> {
            atomicInteger.incrementAndGet();
            MutableText pText = new LiteralText("");
            MutableText owner;
            GameProfile profile = server().getUserCache().getByUuid(uuid);
            if (profile != null) {
                owner = new LiteralText(profile.getName());
            } else {
                owner = new LiteralText(uuid.toString())
                        .styled((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to Copy")))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString())));
            }

            pText.append(new LiteralText(atomicInteger.get() + ". ").formatted(Formatting.GOLD))
                    .append(owner.formatted(Formatting.YELLOW));
            text.append(pText).append(new LiteralText("\n"));
        });
        context.getSource().sendFeedback(text, false);
        return atomicInteger.get();
    }
}
