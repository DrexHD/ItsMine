package me.drexhd.itsmine.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.claim.permission.Permission;
import me.drexhd.itsmine.util.ArgumentUtil;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static me.drexhd.itsmine.util.ClaimUtil.validateClaim;
import static net.minecraft.server.command.CommandManager.literal;

public class TrustedCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> trusted = literal("trusted");
        RequiredArgumentBuilder<ServerCommandSource, String> claimArgument = ArgumentUtil.getClaims();
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> claimOwner = argument("claimOwner", GameProfileArgumentType.gameProfile())/*.suggests(PLAYERS_PROVIDER)*/;

        claimOwner.then(claimArgument);
        trusted.then(claimOwner);
        trusted.then(claimArgument);
        command.then(trusted);

        trusted.executes((context) -> {
            Claim claim = ClaimManager.INSTANCE.getClaimAt(context.getSource().getPlayer().getBlockPos(), context.getSource().getWorld().getDimension());
            validateClaim(claim);
            return showTrustedList(context, claim, false);
        });

        claimArgument.executes((context) -> {
            Claim claim = ClaimManager.INSTANCE.getClaim(context, getString(context, "claim"));
            validateClaim(claim);
            return showTrustedList(context, claim, false);
        });
    }

    static int showTrustedList(CommandContext<ServerCommandSource> context, Claim claim, boolean showSelf) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        int mapSize = claim.permissionManager.playerPermissions.size();

        if (mapSize == 1 && !showSelf) {
            source.sendError(new LiteralText(claim.name + " is not trusting anyone!"));
            return -1;
        }

        MutableText text = new LiteralText("\n");
        text.append(new LiteralText("Trusted players for Claim ").formatted(Formatting.YELLOW))
                .append(new LiteralText(claim.name).formatted(Formatting.GOLD)).append(new LiteralText("\n"));

        AtomicInteger atomicInteger = new AtomicInteger();
        claim.permissionManager.playerPermissions.forEach((uuid, perm) -> {
            atomicInteger.incrementAndGet();
            MutableText pText = new LiteralText("");
            MutableText owner;
            GameProfile profile = source.getMinecraftServer().getUserCache().getByUuid(uuid);
            if (profile != null) {
                owner = new LiteralText(profile.getName());
            } else {
                owner = new LiteralText(uuid.toString()).styled((style) -> style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to Copy"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString())));
            }

            pText.append(new LiteralText(atomicInteger.get() + ". ").formatted(Formatting.GOLD))
                    .append(owner.formatted(Formatting.YELLOW));

            MutableText hover = new LiteralText("");
            hover.append(new LiteralText("Permissions:").formatted(Formatting.WHITE)).append(new LiteralText("\n"));

            int allowed = 0;
            int i = 0;
            boolean nextColor = false;
            MutableText perms = new LiteralText("");

            for (Permission value : Permission.values()) {
                if (claim.permissionManager.hasPermission(uuid, value.id)) {
                    Formatting formatting = nextColor ? Formatting.GREEN : Formatting.DARK_GREEN;
                    perms.append(new LiteralText(value.id).formatted(formatting)).append(new LiteralText(" "));
                    if (i == 3) perms.append(new LiteralText("\n"));
                    allowed++;
                    i++;
                    nextColor = !nextColor;
                }
            }

            if (allowed == Permission.values().length) {
                hover.append(new LiteralText("All " + allowed + " Permissions").formatted(Formatting.YELLOW).formatted(Formatting.ITALIC));
            } else {
                hover.append(perms);
            }

            pText.append(new LiteralText(" ")
                    .append(new LiteralText("(").formatted(Formatting.GOLD))
                    .append(new LiteralText(String.valueOf(allowed)).formatted(Formatting.GREEN))
                    .append(new LiteralText("/").formatted(Formatting.GOLD))
                    .append(new LiteralText(String.valueOf(Permission.values().length)).formatted(Formatting.YELLOW))
                    .append(new LiteralText(")").formatted(Formatting.GOLD))
            );

            pText.styled((style) -> {
                return style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
            });
            text.append(pText).append(new LiteralText("\n"));
        });

        source.sendFeedback(text, false);
        return 1;
    }
}
