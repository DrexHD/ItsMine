package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.ClaimUtil;
import me.drexhd.itsmine.util.ShowerUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.server.command.CommandManager.argument;

public class ExpandCommand extends Command implements Admin, Other, Subzone {

    private boolean expand;

    public ExpandCommand(String literal, boolean expand) {
        super(literal);
        this.expand = expand;
    }

    @Override
    public Command copy() {
        return new ExpandCommand(literal, expand);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, Integer> amount = argument("distance", IntegerArgumentType.integer(1, 1024));
        amount.executes(this::expand);
        literal().then(amount);
        command.then(literal());
    }

    private static void undoExpand(Claim claim, Direction direction, int amount) {
        if (amount < 0) claim.expand(direction, -amount);
        else claim.shrink(direction, amount);
    }

    public int expand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        UUID ownerID = source.getPlayer().getGameProfile().getId();
        Claim claim = getClaimAt(context);
        int amount = IntegerArgumentType.getInteger(context, "distance");
        if(!expand) amount *= -1;
        Direction direction = Direction.getEntityFacingOrder(source.getPlayer())[0];
        if (!claim.hasPermission(ownerID, "modify", "size")) {
            source.sendFeedback(new LiteralText("You do not have border change permissions in that claim").formatted(Formatting.RED), false);
            return 0;
        }
        int oldArea = claim.getArea();


        if (amount > 0) {
            claim.expand(direction, amount);
        } else {
            claim.shrink(direction, -amount);
        }

        if (!claim.canShrink(new BlockPos(direction.getOffsetX() * amount, direction.getOffsetY() * amount, direction.getOffsetZ() * amount))) {
            source.sendFeedback(new LiteralText("You can't shrink your claim that far. It would pass its opposite wall.").formatted(Formatting.RED), false);
            undoExpand(claim, direction, amount);
            return 0;
        }

        if (!claim.isChild) {
            if (ClaimManager.INSTANCE.wouldIntersect(claim)) {
                source.sendFeedback(new LiteralText("Expansion would result in hitting another claim").formatted(Formatting.RED), false);
                undoExpand(claim, direction, amount);
                return 0;
            }

            //Check if shrinking would reset a subzone to be outside of its parent claim
            AtomicBoolean returnVal = new AtomicBoolean();
            returnVal.set(false);
            int finalAmount = amount;
            claim.subzones.forEach(subzone -> {
                if (!subzone.isInside(claim)) {
                    undoExpand(claim, direction, finalAmount);
                    source.sendFeedback(new LiteralText("Shrinking would result in " + subzone.name + " being outside of " + claim.name).formatted(Formatting.RED), true);
                    returnVal.set(true);
                }
            });
            if (returnVal.get()) return 0;

            int newArea = claim.getArea() - oldArea;
            if (!admin && claim.claimBlockOwner != null && ClaimManager.INSTANCE.getClaimBlocks(ownerID) < newArea) {
                source.sendFeedback(new LiteralText("You don't have enough claim blocks. You have " + ClaimManager.INSTANCE.getClaimBlocks(ownerID) + ", you need " + newArea + "(" + (newArea - ClaimManager.INSTANCE.getClaimBlocks(ownerID)) + " more)").formatted(Formatting.RED), false);
                undoExpand(claim, direction, amount);
                ClaimUtil.blocksLeft(source);
                return 0;
            } else if (claim.max.getX() - claim.min.getX() > 1024 || claim.max.getZ() - claim.min.getZ() > 1024) {
                undoExpand(claim, direction, amount);
                source.sendFeedback(new LiteralText("This operation would result in exceeding the maximum claim length limit (1024)").formatted(Formatting.RED), false);
            } else {
                if (!admin && claim.claimBlockOwner != null) ClaimManager.INSTANCE.useClaimBlocks(ownerID, newArea);
                source.sendFeedback(new LiteralText("Your claim was " + (amount > 0 ? "expanded" : "shrunk") + " by " + (amount < 0 ? -amount : amount) + (amount == 1 ? " block " : " blocks ") + direction.getName()).formatted(Formatting.GREEN), false);
                ClaimUtil.blocksLeft(source);
                undoExpand(claim, direction, amount);
                ShowerUtil.update(claim, source.getWorld(), true);
                ClaimManager.INSTANCE.updateClaim(claim);
                if (amount > 0) claim.expand(direction, amount);
                else claim.shrink(direction, -amount);
                ShowerUtil.update(claim, source.getWorld(), false);
                ClaimManager.INSTANCE.updateClaim(claim);
            }
            return 0;
        } else {
            Claim parent = ClaimUtil.getParentClaim(claim);
            if (!claim.isInside(parent)) {
                source.sendFeedback(new LiteralText("Expansion would result in expanding outside of your main claim").formatted(Formatting.RED), false);
                undoExpand(claim, direction, amount);
            } else if (ClaimManager.INSTANCE.wouldSubzoneIntersect((claim))) {
                source.sendFeedback(new LiteralText("Expansion would result in overlapping with another subzone").formatted(Formatting.RED), false);
                undoExpand(claim, direction, amount);
            } else {
                source.sendFeedback(new LiteralText("Your subzone was " + (amount > 0 ? "expanded" : "shrunk") + " by " + (amount < 0 ? -amount : amount) + " blocks " + direction.getName()).formatted(Formatting.GREEN), false);
                //The expansion is undone to hide the claimshower
                undoExpand(claim, direction, amount);
                ShowerUtil.update(parent, source.getWorld(), true);
                ClaimManager.INSTANCE.updateClaim(claim);
                if (amount > 0) claim.expand(direction, amount);
                else claim.shrink(direction, -amount);
                ShowerUtil.update(parent, source.getWorld(), false);
                ClaimManager.INSTANCE.updateClaim(claim);
            }
        }
        return 0;
    }
}
