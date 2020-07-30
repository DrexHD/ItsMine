package me.drexhd.itsmine.command.admin;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.util.ArgumentUtil;
import me.drexhd.itsmine.util.MessageUtil;
import me.drexhd.itsmine.util.WorldUtil;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.lang3.time.StopWatch;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RemoveAllCommand {
    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> removeAll = literal("removeAll");
        LiteralArgumentBuilder<ServerCommandSource> in = literal("in");
        LiteralArgumentBuilder<ServerCommandSource> from = literal("from");
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> gameProfile = argument("player", GameProfileArgumentType.gameProfile());
        RequiredArgumentBuilder<ServerCommandSource, Identifier> dimension = argument("dimension", DimensionArgumentType.dimension());

        dimension.executes(RemoveAllCommand::removeAllIn);
        gameProfile.executes(RemoveAllCommand::removeAllFrom);
        in.then(dimension);
        from.then(gameProfile);
        removeAll.then(in);
        removeAll.then(from);
        command.then(removeAll);
    }

    private static int removeAllIn(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ServerCommandSource source = context.getSource();
        DimensionType dimensionType = DimensionArgumentType.getDimensionArgument(context, "dimension").getDimension();
        int claims = 0;
        int subzones = 0;


        ArrayList<Claim> remove = new ArrayList<>();
        for (Claim claim : ClaimManager.INSTANCE.getClaimList()) {
            if (claim.dimension.equals(dimensionType)) {
                /*We add the claims which are supposed to get removed to a separate Arraylist, to avoid a ConcurrentModificationException */
                remove.add(claim);
                ClaimManager.INSTANCE.releaseBlocksToOwner(claim);
                if (claim.isChild) subzones++;
                else claims++;
            }
        }

        for (Claim claim : remove) {
            ClaimManager.INSTANCE.removeClaim(claim);
        }

        stopWatch.stop();
        String timeElapsed = new DecimalFormat("##.##").format(stopWatch.getTime(TimeUnit.MILLISECONDS));
        MessageUtil.sendMessage(source, "&cRemoved " + claims + " claims and " + subzones + " subzones from the " + WorldUtil.getDimensionName(dimensionType) + " in " + timeElapsed + "ms!");
        return claims + subzones;
    }

    private static int removeAllFrom(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ServerCommandSource source = context.getSource();
        GameProfile gameProfile = ArgumentUtil.getGameProfile(GameProfileArgumentType.getProfileArgument(context, "player"), context);
        if (gameProfile == null || !gameProfile.isComplete()) {
            return 0;
        }
        int claims = 0;
        int subzones = 0;
        ArrayList<Claim> remove = new ArrayList<>();
        for (Claim claim : ClaimManager.INSTANCE.getClaimList()) {
            if (claim.claimBlockOwner != null && claim.claimBlockOwner.equals(gameProfile.getId())) {
                /*We add the claims which are supposed to get removed to a separate Arraylist, to avoid a ConcurrentModificationException */
                remove.add(claim);
                ClaimManager.INSTANCE.releaseBlocksToOwner(claim);
                if (claim.isChild) subzones++;
                else claims++;
            }
        }

        for (Claim claim : remove) {
            ClaimManager.INSTANCE.removeClaim(claim);
        }
        stopWatch.stop();
        String timeElapsed = new DecimalFormat("##.##").format(stopWatch.getTime(TimeUnit.MILLISECONDS));
        MessageUtil.sendMessage(source, "&cRemoved " + claims + " claims and " + subzones + " subzones from  " + gameProfile.getName() + " in " + timeElapsed + "ms!");

        return claims + subzones;
    }
}
