package me.drexhd.itsmine.command.admin;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.util.ArgumentUtil;
import me.drexhd.itsmine.util.ClaimUtil;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OwnerCommand {
    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        {
            LiteralArgumentBuilder<ServerCommandSource> set = literal("setOwner");
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> newOwner = argument("newOwner", GameProfileArgumentType.gameProfile());
            RequiredArgumentBuilder<ServerCommandSource, String> claimArgument = ArgumentUtil.getClaims();

            newOwner.executes((context) -> {
                Claim claim = ClaimManager.INSTANCE.getClaimAt(context.getSource().getPlayer().getBlockPos(), context.getSource().getPlayer().world.getDimension());
                ClaimUtil.validateClaim(claim);
                GameProfile profile = ArgumentUtil.getGameProfile(GameProfileArgumentType.getProfileArgument(context, "newOwner"), context);
                return setOwner(context.getSource(), claim, profile);
            });

            claimArgument.executes((context) -> {
                Claim claim = ClaimManager.INSTANCE.getClaim(context.getSource().getPlayer().getUuid(), getString(context, "claim"));
                ClaimUtil.validateClaim(claim);
                GameProfile profile = ArgumentUtil.getGameProfile(GameProfileArgumentType.getProfileArgument(context, "newOwner"), context);
                return setOwner(context.getSource(), claim, profile);
            });

            newOwner.then(claimArgument);
            set.then(newOwner);
            command.then(set);
        }

        {
            LiteralArgumentBuilder<ServerCommandSource> set = literal("setOwnerName");
            RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("newName", word());
            RequiredArgumentBuilder<ServerCommandSource, String> claimArgument = ArgumentUtil.getClaims();

            nameArgument.executes((context) -> {
                Claim claim = ClaimManager.INSTANCE.getClaimAt(context.getSource().getPlayer().getBlockPos(), context.getSource().getPlayer().world.getDimension());
                ClaimUtil.validateClaim(claim);
                return setOwnerName(context.getSource(), claim, getString(context, "newName"));
            });

            claimArgument.executes((context) -> {
                Claim claim = ClaimManager.INSTANCE.getClaim(context.getSource().getPlayer().getUuid(), getString(context, "claim"));
                ClaimUtil.validateClaim(claim);
                return setOwnerName(context.getSource(), claim, getString(context, "newName"));
            });

            nameArgument.then(claimArgument);
            set.then(nameArgument);
            command.then(set);
        }
    }
    private static int setOwnerName(ServerCommandSource source, Claim claim, String input) {
        String name = input.equals("reset") ? null : input;
        source.sendFeedback(new LiteralText("Set the Custom Owner Name to ")
                        .formatted(Formatting.YELLOW).append(new LiteralText(name == null ? "Reset" : name).formatted(Formatting.GOLD)).append(new LiteralText(" from "))
                        .append(new LiteralText(claim.customOwnerName == null ? "Not Present" : claim.customOwnerName).formatted(Formatting.GOLD))
                        .append(new LiteralText(" for ").formatted(Formatting.YELLOW)).append(new LiteralText(claim.name).formatted(Formatting.GOLD))
                , false);
        claim.customOwnerName = input;
        return 1;
    }
    private static int setOwner(ServerCommandSource source, Claim claim, GameProfile profile) {
        GameProfile oldOwner = source.getMinecraftServer().getUserCache().getByUuid(claim.claimBlockOwner);
        source.sendFeedback(new LiteralText("Set the Claim Owner to ")
                        .formatted(Formatting.YELLOW).append(new LiteralText(profile.getName()).formatted(Formatting.GOLD)).append(new LiteralText(" from "))
                        .append(new LiteralText(oldOwner == null ? "(" + claim.claimBlockOwner + ")" : oldOwner.getName()).formatted(Formatting.GOLD))
                        .append(new LiteralText(" for ").formatted(Formatting.YELLOW)).append(new LiteralText(claim.name).formatted(Formatting.GOLD))
                , false);
        //Update ClaimList
        ClaimManager.INSTANCE.removeClaim(claim);
        claim.claimBlockOwner = profile.getId();
        ClaimManager.INSTANCE.addClaim(claim);
        return 1;
    }
}
