package me.drexhd.itsmine.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.claim.permission.map.InvertedMap;
import me.drexhd.itsmine.util.MessageUtil;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static me.drexhd.itsmine.util.ArgumentUtil.PLAYERS_PROVIDER;
import static me.drexhd.itsmine.util.ClaimUtil.validateClaim;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TrustCommand {
    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, CommandDispatcher dispatcher, RequiredArgumentBuilder<ServerCommandSource, String> claim, boolean admin) {
        {
            LiteralArgumentBuilder<ServerCommandSource> trust = literal("trust");
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> playerArgument = argument("player", GameProfileArgumentType.gameProfile()).suggests(PLAYERS_PROVIDER);
            playerArgument.executes((context -> executeTrust(context, GameProfileArgumentType.getProfileArgument(context, "player"), true, null, admin)));
            claim.executes((context -> executeTrust(context, GameProfileArgumentType.getProfileArgument(context, "player"), true, getString(context, "claim"), admin)));

            playerArgument.then(claim);
            trust.then(playerArgument);
            command.then(trust);
            dispatcher.register(trust);
        }
        {
            LiteralArgumentBuilder<ServerCommandSource> distrust = literal("distrust");
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> playerArgument = argument("player", GameProfileArgumentType.gameProfile()).suggests(PLAYERS_PROVIDER);

            playerArgument.executes((context -> executeTrust(context, GameProfileArgumentType.getProfileArgument(context, "player"), false, null, admin)));
            claim.executes((context -> executeTrust(context, GameProfileArgumentType.getProfileArgument(context, "player"), false, getString(context, "claim"), admin)));

            playerArgument.then(claim);
            distrust.then(playerArgument);
            command.then(distrust);
            dispatcher.register(distrust);
        }
    }
    private static int executeTrust(CommandContext<ServerCommandSource> context, Collection<GameProfile> targetCollection, boolean set, @Nullable String claimName, boolean admin) throws CommandSyntaxException {
        AtomicInteger integer = new AtomicInteger();

        if(targetCollection.isEmpty()){
            context.getSource().sendFeedback(new LiteralText("No player provided").formatted(Formatting.RED), true);
            return 0;
        }
        ServerPlayerEntity p = context.getSource().getPlayer();
        Claim claim = claimName == null ? ClaimManager.INSTANCE.getClaimAt(p.getBlockPos(), p.world.getDimension()) : ClaimManager.INSTANCE.getClaim(context.getSource().getPlayer().getUuid(), claimName);
        validateClaim(claim);
        targetCollection.iterator().forEachRemaining(gameProfile -> {
            try {
                //This is supposed to check if the player has played before :shrug:
                if(context.getSource().getMinecraftServer().getUserCache().getByUuid(gameProfile.getId()).equals(gameProfile)){
                    integer.set(setTrust(context, claim, gameProfile, set, admin));
                } else {
                    context.getSource().sendFeedback(new LiteralText("Unknown player!").formatted(Formatting.RED), true);
                    integer.set(0);
                }
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        });
        return integer.get();
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
            MessageUtil.sendTranslatableMessage(context.getSource(), "messages", "noPermission");
        }
        return 1;
    }
}
