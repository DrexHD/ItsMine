package me.drexhd.itsmine.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.Messages;
import me.drexhd.itsmine.claim.Claim;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class ClaimUtil {

    public static BlockPos getPosOnGround(BlockPos pos, World world) {
        BlockPos blockPos = new BlockPos(pos.getX(), 256, pos.getZ());

        do {
            blockPos = blockPos.down();
            if(blockPos.getY() < 1){
                    return pos;
            }
        } while (!world.getBlockState(blockPos).isFullCube(world, pos));

        return blockPos.up();
    }

    public static Claim getParentClaim(Claim subzone){
        AtomicReference<Claim> parentClaim = new AtomicReference<>();
        if(subzone.isChild){
            ClaimManager.INSTANCE.getClaimList().forEach((claim) -> {
                for(Claim subzone2 : claim.subzones){
                    if(subzone2 == subzone){
                        parentClaim.set(claim);
                    }
                }
            });
            return parentClaim.get();
        }
        return subzone;
    }

    public static void validateClaim(Claim claim) throws CommandSyntaxException {
        if (claim == null) throw new SimpleCommandExceptionType(Messages.INVALID_CLAIM).create();
    }

    public static void validateCanAccess(ServerPlayerEntity player, Claim claim, boolean admin) throws CommandSyntaxException {
        if (!admin && !claim.hasPermission(player.getGameProfile().getId(), "modify", "flags")) {
            throw new SimpleCommandExceptionType(Messages.NO_PERMISSION).create();
        }
    }


    public static int setEventMessage(ServerCommandSource source, Claim claim, Claim.Event event, String message) {
        switch (event) {
            case ENTER_CLAIM:
                claim.enterMessage = message.equalsIgnoreCase("reset") ? null : message;
                break;
            case LEAVE_CLAIM:
                claim.leaveMessage = message.equalsIgnoreCase("reset") ? null : message;
                break;
        }

        if (message.equalsIgnoreCase("reset")) {
            source.sendFeedback(new LiteralText("Reset ").append(new LiteralText(event.id).formatted(Formatting.GOLD)
                            .append(new LiteralText(" Event Message for claim ").formatted(Formatting.YELLOW))
                            .append(new LiteralText(claim.name).formatted(Formatting.GOLD))).formatted(Formatting.YELLOW)
                    , false);
            return -1;
        }

        source.sendFeedback(new LiteralText("Set ").append(new LiteralText(event.id).formatted(Formatting.GOLD)
                        .append(new LiteralText(" Event Message for claim ").formatted(Formatting.YELLOW))
                        .append(new LiteralText(claim.name).formatted(Formatting.GOLD)).append(new LiteralText(" to:").formatted(Formatting.YELLOW)))
                        .append(new LiteralText("\n")).append(new LiteralText(ChatColor.translate(message)))
                        .formatted(Formatting.YELLOW)
                , false);
        return 1;
    }

    public static void readClaimFiles() {
        File claims = new File(ItsMine.getDirectory() + "/world/claims.dat");
        File claims_old = new File(ItsMine.getDirectory() + "/world/claims.dat_old");
        if (!claims.exists()) {
            if (claims_old.exists()) {}
            else return;
        }
        try {
            if (!claims.exists() && claims_old.exists()) throw new FileNotFoundException();
            ClaimManager.INSTANCE.fromNBT(NbtIo.readCompressed(new FileInputStream(claims)));
        } catch (IOException e) {
            System.out.println("Could not load " + claims.getName() + ":");
            e.printStackTrace();
            if (claims_old.exists()) {
                System.out.println("Attempting to load backup claims...");
                try {
                    ClaimManager.INSTANCE.fromNBT(NbtIo.readCompressed(new FileInputStream(claims_old)));
                } catch (IOException e2) {
                    throw new RuntimeException("Could not load claims.dat_old - Crashing server to save data. Remove or fix claims.dat or claims.dat_old to continue");
                }
            }
        }
    }

}
