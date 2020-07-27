package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.List;

public class ListCommand extends Command implements Admin{

    public ListCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new ListCommand(literal);
    }

    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, String> user = ownerArgument();
        user.requires(src -> ItsMine.permissions().hasPermission(src, "itsmine.list", 2));
        user.executes(this::list);
        literal().then(user);
        literal().executes(this::list);
        command.then(literal());

        LiteralArgumentBuilder<ServerCommandSource> claims = literal("claims");
        claims.executes(this::list);
        dispatcher().register(claims);
    }

    public int list(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        List<Claim> claims = ClaimManager.INSTANCE.getPlayerClaims(getOwner(context));
        if (claims.isEmpty()) {
            source.sendFeedback(new LiteralText("No Claims").formatted(Formatting.RED), false);
            return -1;
        }


        MutableText text = new LiteralText("\n").append(new LiteralText("Claims (" + getName(getOwner(context)) + "): ").formatted(Formatting.GOLD)).append("\n ");
        boolean nextColor = false;
        for (Claim claim : claims) {
            if(!claim.isChild) {
                MutableText cText = new LiteralText(claim.name).formatted(nextColor ? Formatting.AQUA : Formatting.DARK_AQUA).styled((style) -> {
                    MutableText hoverText = new LiteralText("Click for more Info").formatted(Formatting.AQUA);
                    if (claim.subzones.size() > 0) {
                        hoverText.append(new LiteralText("\n\nSubzones:"));
                        boolean nextColor2 = false;
                        for (Claim subzone : claim.subzones) {
                            hoverText.append(new LiteralText("\n* " + subzone.name).formatted(nextColor2 ? Formatting.AQUA : Formatting.DARK_AQUA));
                            nextColor2 = !nextColor2;
                        }
                    }
                    return style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/claim info " + claim.name)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
                });

                nextColor = !nextColor;
                text.append(cText.append(" "));
            }
        }

        source.sendFeedback(text.append("\n"), false);
        return 1;
    }
}
