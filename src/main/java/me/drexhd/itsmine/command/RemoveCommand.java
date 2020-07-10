package me.drexhd.itsmine.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ClaimShower;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.util.ClaimUtil;
import me.drexhd.itsmine.util.PermissionUtil;
import me.drexhd.itsmine.util.ShowerUtil;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static me.drexhd.itsmine.util.ShowerUtil.silentHideShow;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RemoveCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, RequiredArgumentBuilder<ServerCommandSource, String> claim, boolean admin) {
        LiteralArgumentBuilder<ServerCommandSource> delete = literal("remove");
        LiteralArgumentBuilder<ServerCommandSource> confirm = literal("confirm");
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> claimOwner = argument("claimOwner", GameProfileArgumentType.gameProfile())/*.suggests(PLAYERS_PROVIDER)*/;

        confirm.executes(context -> delete(context, admin));
        claim.executes(context -> requestDelete(context, admin));
        claim.then(confirm);
        if (admin) {
            claimOwner.then(claim);
            delete.then(claimOwner);
        } else {
            delete.then(claim);
        }
        command.then(delete);
    }

    private static int requestDelete(CommandContext<ServerCommandSource> context, boolean admin) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Claim claim = ClaimManager.INSTANCE.getClaim(context, getString(context, "claim"));
        ClaimUtil.validateClaim(claim);
        if (claim.claimBlockOwner != null && !claim.claimBlockOwner.equals(source.getPlayer().getUuid())) {
            if (admin && ItsMine.permissions().hasPermission(source, PermissionUtil.Command.ADMIN_MODIFY, 2)) {
                source.sendFeedback(new LiteralText("WARNING: This is not your claim...").formatted(Formatting.DARK_RED).formatted(Formatting.BOLD), false);
            } else {
                source.sendFeedback(new LiteralText("You cannot delete that claim").formatted(Formatting.RED), false);
                return 0;
            }
        }
        source.sendFeedback(new LiteralText("").append(new LiteralText("Are you sure you want to delete the claim \"" + claim.name + "\"? ").formatted(Formatting.GOLD))
                .append(new LiteralText("[I'M SURE]").styled(style -> style.withColor(Formatting.DARK_RED).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, (admin ? "/claim admin" : "/claim") + " remove " + claim.name + " confirm")))), false);
        return 0;
    }

    private static int delete(CommandContext<ServerCommandSource> context, boolean admin) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Claim claim = ClaimManager.INSTANCE.getClaim(context, getString(context, "claim"));
        ServerWorld world = source.getWorld();
        ClaimUtil.validateClaim(claim);
        if (!claim.claimBlockOwner.equals(source.getPlayer().getUuid())) {
            if (admin && ItsMine.permissions().hasPermission(source, PermissionUtil.Command.ADMIN_MODIFY, 2)) {
                source.sendFeedback(new LiteralText("Deleting a claim belonging to somebody else").formatted(Formatting.DARK_RED).formatted(Formatting.BOLD), false);
            } else {
                source.sendFeedback(new LiteralText("You cannot delete that claim").formatted(Formatting.RED), false);
                return 0;
            }
        }
        if (!claim.isChild) {
            ClaimManager.INSTANCE.releaseBlocksToOwner(claim);
            ShowerUtil.update(claim, world, true);
            ClaimManager.INSTANCE.removeClaim(claim);
            for (Claim subzone : claim.subzones) {
                ClaimManager.INSTANCE.removeClaim(subzone);
            }
        } else {
            Claim parent = ClaimUtil.getParentClaim(claim);
            ShowerUtil.update(parent, world, true);
            ClaimUtil.getParentClaim(claim).removeSubzone(claim);
            ClaimManager.INSTANCE.removeClaim(claim);
            ShowerUtil.update(parent, world, false);
        }
        source.getWorld().getPlayers().forEach(playerEntity -> {
            if (((ClaimShower) playerEntity).getShownClaim() != null && ((ClaimShower) playerEntity).getShownClaim().name.equals(claim.name))
                silentHideShow(playerEntity, claim, true, true, ((ClaimShower) playerEntity).getMode());
        });

        source.sendFeedback(new LiteralText("Deleted the claim \"" + claim.name + "\"").formatted(Formatting.GREEN), false);
        return 0;
    }
}
