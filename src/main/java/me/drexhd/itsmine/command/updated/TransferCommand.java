//Abandoned until I have time to approach this in a better way
/*
package me.drexhd.itsmine.command.updated;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.ArgumentUtil;
import me.drexhd.itsmine.util.ClaimUtil;
import me.drexhd.itsmine.util.PermissionUtil;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.server.command.CommandManager.argument;

public class TransferCommand extends Command {

    private Map<UUID, Claim> pendingTransfers = new HashMap<>();

    public TransferCommand(String literal, boolean others, boolean admin, boolean subzones, boolean suggestCurrent) {
        super(literal, false, admin, subzones, suggestCurrent);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> transfer = literal("transfer");
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ArgumentUtil.getClaims();
        RequiredArgumentBuilder<ServerCommandSource, String> player = userArgument();
        LiteralArgumentBuilder<ServerCommandSource> confirm = literal("confirm");
        confirm.executes(context -> {
            final String string = "-accept-";
            ServerPlayerEntity p = EntityArgumentType.getPlayer(context, "player");
            String input = getString(context, "claim");
            String claimName = input.replace(string, "");
            Claim claim1 = ClaimManager.INSTANCE.getClaim(context.getSource().getPlayer().getUuid(), claimName);
            ClaimUtil.validateClaim(claim1);
            if (input.startsWith(string)) {
                return acceptTransfer(context.getSource());
            }
            return transfer(context.getSource(), claim1, p, false);
        });
        player.executes(context -> requestTransfer(context.getSource(), ClaimManager.INSTANCE.getClaim(context.getSource().getPlayer().getUuid(), getString(context, "claim")), EntityArgumentType.getPlayer(context, "player"), false));
        player.then(confirm);
        literal().then(thenClaim(player));
        literal().executes(context -> requestTransfer(context.getSource(), ClaimManager.INSTANCE.getClaimAt(new BlockPos(context.getSource().getPosition()), context.getSource().getWorld().getDimension()), EntityArgumentType.getPlayer(context, "player"), false));
        command.then(literal());
    }

    public int acceptTransfer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Claim claim = ClaimManager.INSTANCE.getClaim()
        ServerPlayerEntity player = source.getMinecraftServer().getPlayerManager().getPlayer(claim.claimBlockOwner);
        if (player != null) {
            player.sendSystemMessage(new LiteralText("").append(new LiteralText(source.getPlayer().getGameProfile().getName() + " has taken ownership of the claim \"" + claim.name + "\"").formatted(Formatting.YELLOW)), player.getUuid());
        }
        UUID newUUID = source.getPlayer().getGameProfile().getId();
        //Update claim
        ClaimManager.INSTANCE.removeClaim(claim);
        claim.claimBlockOwner = newUUID;
        ClaimManager.INSTANCE.addClaim(claim);
        //Update subzones
        claim.subzones.forEach(subzone -> {
            ClaimManager.INSTANCE.removeClaim(subzone);
            subzone.claimBlockOwner = newUUID;
            ClaimManager.INSTANCE.addClaim(subzone);
        });
        return 0;
    }

    public int requestTransfer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Claim claim = getClaim(context);
        UUID uuid = getUser(context);
        String name = getName(uuid);
        source.sendFeedback(new LiteralText("").append(new LiteralText("Are you sure you want to transfer ownership of \"" + claim.name + "\" to " + name + "? ").formatted(Formatting.GOLD))
                .append(new LiteralText("[YES]").styled(style -> style.withColor(Formatting.DARK_RED).withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, (admin ? "/claim admin" : "/claim") + " transfer " + claim.name + " " + name + " confirm")))), false);
        return 0;
    }

    public int transfer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Claim claim = getClaim(context);
        UUID playerUUID = getUser(context);
        String playerName = getName(playerUUID);

        if (claim.isChild) {
            throw new SimpleCommandExceptionType(new LiteralText("You can't transfer ownership of subzones!")).create();
        }
        if (source.getPlayer().getUuid().toString().equals(playerUUID)) {
            throw new SimpleCommandExceptionType(new LiteralText("You can't transfer ownership to yourself!")).create();
        }
        for (Claim claim2 : ClaimManager.INSTANCE.getPlayerClaims(playerUUID)) {
            if (claim2.name.equals(claim.name)) {
                source.sendFeedback(new LiteralText("Transfer receiver can't accept, because they already have a claim with the name " + claim.name).formatted(Formatting.RED), false);
                return -1;
            }
        }
        if (!claim.claimBlockOwner.equals(source.getPlayer().getGameProfile().getId())) {
            if (admin && ItsMine.permissions().hasPermission(source, PermissionUtil.Command.ADMIN_MODIFY, 2)) {
                source.sendFeedback(new LiteralText("Transfering ownership of a claim belonging to somebody else").formatted(Formatting.DARK_RED).formatted(Formatting.BOLD), false);
            } else {
                throw new SimpleCommandExceptionType(new LiteralText("You can't transfer ownership of that claim!")).create();
            }
        }
        GameProfile profile = source.getWorld().getServer().getUserCache().getByUuid(claim.claimBlockOwner);
        source.sendFeedback(new LiteralText("Transferring ownership of the claim \"" + claim.name + "\" to " + playerName + " if they accept").formatted(Formatting.GREEN), false);
        getOnlineUser(playerUUID).sendSystemMessage(new LiteralText("").append(new LiteralText("Do you want to accept ownership of the claim \"" + claim.name + "\" from " + (profile == null ? "Not Present" : profile.getName()) + "? ").formatted(Formatting.GOLD))
                .append(new LiteralText("[ACCEPT]").styled(style -> style.withColor(Formatting.GREEN).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/claim transfer -accept-" + claim.name + " " + playerName + " confirm")))), playerUUID);
        pendingTransfers.put(playerUUID, claim);
        return 0;
    }
}
*/
