package me.drexhd.itsmine.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.claim.permission.map.InvertedMap;
import me.drexhd.itsmine.util.ArgumentUtil;
import me.drexhd.itsmine.util.MessageUtil;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static me.drexhd.itsmine.util.ArgumentUtil.PLAYERS_PROVIDER;
import static me.drexhd.itsmine.util.ClaimUtil.validateClaim;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TrustCommand {
    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, CommandDispatcher dispatcher, RequiredArgumentBuilder<ServerCommandSource, String> claim, boolean admin) {
        {
            LiteralArgumentBuilder<ServerCommandSource> trust = literal("trust");
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = argument("player", GameProfileArgumentType.gameProfile()).suggests(PLAYERS_PROVIDER);
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> claimOwner = argument("claimOwner", GameProfileArgumentType.gameProfile())/*.suggests(EMPTY)*/;

            player.executes((context -> executeTrust(context, true, "", admin)));
            claim.executes((context -> executeTrust(context, true, getString(context, "claim"), admin)));

            if (admin) {
                claimOwner.then(claim);
                player.then(claimOwner);
            } else {
                player.then(claim);
            }
            trust.then(player);
            command.then(trust);
            dispatcher.register(trust);
        }
        {
            LiteralArgumentBuilder<ServerCommandSource> distrust = literal("distrust");
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = argument("player", GameProfileArgumentType.gameProfile()).suggests(PLAYERS_PROVIDER);
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> claimOwner = argument("claimOwner", GameProfileArgumentType.gameProfile())/*.suggests(EMPTY)*/;

            player.executes((context -> executeTrust(context, false, "", admin)));
            claim.executes((context -> executeTrust(context, false, getString(context, "claim"), admin)));

            claimOwner.then(claim);
            player.then(claim);
            player.then(claimOwner);
            distrust.then(player);
            command.then(distrust);
            dispatcher.register(distrust);
        }
    }

    private static int executeTrust(CommandContext<ServerCommandSource> context, boolean set, @Nullable String claimName, boolean admin) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        //Get and validate Claim
        Claim claim;
        if (claimName.equals("")) claim = ClaimManager.INSTANCE.getClaimAt(new BlockPos(source.getPosition()), source.getWorld().getDimension());
        else claim = ClaimManager.INSTANCE.getClaim(context, claimName);
        GameProfile gameProfile = ArgumentUtil.getGameProfile(GameProfileArgumentType.getProfileArgument(context, "player"), context);
        validateClaim(claim);

        //Get and validate GameProfile
        if (gameProfile != null) {
            return setTrust(context, claim, gameProfile, set, admin);
        } else {
            context.getSource().sendFeedback(new LiteralText("Unknown player!").formatted(Formatting.RED), false);
            return 0;
        }
    }

    static int setTrust(CommandContext<ServerCommandSource> context, Claim claim, GameProfile target, boolean set, boolean admin) throws CommandSyntaxException {
        if (claim.canModifySettings(context.getSource().getPlayer().getUuid()) || admin) {
            if (set) {
                claim.permissionManager.playerPermissions.put(target.getId(), new InvertedMap());
            } else {
                claim.permissionManager.playerPermissions.remove(target.getId());
            }
            context.getSource().sendFeedback(new LiteralText(target.getName() + (set ? " now" : " no longer") + " has all the permissions").formatted(Formatting.YELLOW), false);
        } else {
            MessageUtil.sendTranslatableMessage(context.getSource(), ItsMineConfig.main().message().noPermission);
        }
        return 1;
    }
}
