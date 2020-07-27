package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ClaimShower;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.ClaimUtil;
import me.drexhd.itsmine.util.PermissionUtil;
import me.drexhd.itsmine.util.ShowerUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static me.drexhd.itsmine.util.ShowerUtil.silentHideShow;

public class RemoveCommand extends Command implements Admin, Subzone {

    public RemoveCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new RemoveCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> confirm = literal("confirm");

        confirm.executes(context -> delete(context));
        literal().then(thenClaim(confirm));
        command.then(literal());
    }
    @Override
    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Claim claim = getClaim(context);
        if (claim.claimBlockOwner != null && !claim.claimBlockOwner.equals(source.getPlayer().getUuid())) {
            if (admin && ItsMine.permissions().hasPermission(source, PermissionUtil.Command.ADMIN_MODIFY, 2)) {
                source.sendFeedback(new LiteralText("WARNING: This is not your claim...").formatted(Formatting.DARK_RED).formatted(Formatting.BOLD), false);
            } else {
                source.sendFeedback(new LiteralText("You cannot delete that claim").formatted(Formatting.RED), false);
                return 0;
            }
        }
        source.sendFeedback(new LiteralText("").append(new LiteralText("Are you sure you want to delete the claim \"" + claim.name + "\"? ")
                .formatted(Formatting.GOLD))
                .append(new LiteralText("[I'M SURE]")
                .styled(style -> style.withColor(Formatting.DARK_RED).withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, (admin ? "/claim admin" : "/claim") + " remove " + claim.name + " confirm")))), false);
        return 0;
    }

    private int delete(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Claim claim = getClaim(context);
        ServerWorld world = source.getWorld();
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
