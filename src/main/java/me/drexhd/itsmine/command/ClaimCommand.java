package me.drexhd.itsmine.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.Messages;
import me.drexhd.itsmine.claim.Claim;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public class ClaimCommand {
    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, CommandDispatcher dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> claims = literal("claims");
        claims.executes(context -> list(context.getSource(), context.getSource().getPlayer().getGameProfile()));
        dispatcher.register(claims);
    }

    public static int list(ServerCommandSource source, GameProfile gameProfile) {
        if (gameProfile == null) {
            source.sendError(Messages.INVALID_PLAYER);
            return -1;
        }

        List<Claim> claims = ClaimManager.INSTANCE.getPlayerClaims(gameProfile.getId());
        if (claims.isEmpty()) {
            source.sendFeedback(new LiteralText("No Claims").formatted(Formatting.RED), false);
            return -1;
        }


        MutableText text = new LiteralText("\n").append(new LiteralText("Claims (" + gameProfile.getName() + "): ").formatted(Formatting.GOLD)).append("\n ");
        boolean nextColor = false;
        for (Claim claim : claims) {
            if(!claim.isChild) {
                MutableText cText = new LiteralText(claim.name).formatted(nextColor ? Formatting.AQUA : Formatting.DARK_AQUA).styled((style) -> {
                    MutableText hoverText = new LiteralText("Click for more Info").formatted(Formatting.GREEN);
                    if (claim.subzones.size() > 0) {
                        hoverText.append("\n\nSubzones:");
                        for (Claim subzone : claim.subzones) {
                            hoverText.append("\n- " + subzone.name);
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
