package me.drexhd.itsmine.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.Messages;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.claim.permission.Permission;
import me.drexhd.itsmine.config.sections.MessageSection;
import me.drexhd.itsmine.util.ClaimUtil;
import me.drexhd.itsmine.util.MessageUtil;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static me.drexhd.itsmine.util.ArgumentUtil.*;
import static me.drexhd.itsmine.util.ClaimUtil.validateClaim;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PermissionCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, boolean admin, RequiredArgumentBuilder<ServerCommandSource, String> claim) {
        LiteralArgumentBuilder<ServerCommandSource> permissions = literal("permissions");
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = argument("player", GameProfileArgumentType.gameProfile()).suggests(PLAYERS_PROVIDER);
        RequiredArgumentBuilder<ServerCommandSource, String> permNode = getPermissions();
        RequiredArgumentBuilder<ServerCommandSource, Boolean> set = argument("set", BoolArgumentType.bool());
        LiteralArgumentBuilder<ServerCommandSource> reset = literal("reset");
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> claimOwner = argument("claimOwner", GameProfileArgumentType.gameProfile())/*.suggests(PLAYERS_PROVIDER)*/;


        permissions.executes((context) -> HelpCommand.sendPage(context.getSource(), Messages.SETTINGS_AND_PERMISSIONS, 1, "Claim Permissions and Flags", "/claim help perms_and_flags %page%"));
        claim.executes(PermissionCommand::queryTrusted);
        player.executes(context -> queryPermissions(context, admin));
        set.executes(context -> set(context, admin));
        reset.executes(context -> reset(context, admin));
        permNode.executes(context -> queryPermission(context, admin));

        permNode.then(set);
        permNode.then(reset);
        player.then(permNode);
        claim.then(player);
        permissions.then(claim);
        claimOwner.then(claim);
        permissions.then(claimOwner);
        command.then(permissions);
    }

    private static int queryTrusted(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = ClaimManager.INSTANCE.getClaim(context, getString(context, "claim"));
        validateClaim(claim);
        return TrustedCommand.showTrustedList(context, claim, false);
    }

    private static int set(CommandContext<ServerCommandSource> context, boolean admin) throws CommandSyntaxException {
        Claim claim = ClaimManager.INSTANCE.getClaim(context, getString(context, "claim"));
        ClaimUtil.validateClaim(claim);
        GameProfile gameProfile = getGameProfile(GameProfileArgumentType.getProfileArgument(context, "player"), context);
        boolean permission = BoolArgumentType.getBool(context, "set");
        String input = StringArgumentType.getString(context, "permission");
        MessageSection messageSection = ItsMineConfig.main().message();
        String value = permission ? messageSection.getTrue() : messageSection.getFalse();
        ServerCommandSource source = context.getSource();
        if (claim.canModifySettings(source.getPlayer().getUuid()) || admin) {
            MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%permission%", input, "%value%", value, "%player%", gameProfile.getName()), "messages", "permissionSet");
            claim.permissionManager.setPermission(gameProfile.getId(), input, permission);
        }
        return 0;
    }

    private static int reset(CommandContext<ServerCommandSource> context, boolean admin) throws CommandSyntaxException {
        Claim claim = ClaimManager.INSTANCE.getClaim(context, getString(context, "claim"));
        ClaimUtil.validateClaim(claim);
        GameProfile gameProfile = getGameProfile(GameProfileArgumentType.getProfileArgument(context, "player"), context);
        String input = StringArgumentType.getString(context, "permission");
        ServerCommandSource source = context.getSource();
        if (claim.canModifySettings(source.getPlayer().getUuid()) || admin) {
            MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%permission%", input, "%player%", gameProfile.getName()), "messages", "permissionReset");
            claim.permissionManager.clearPermission(gameProfile.getId(), input);
        }
        return 0;
    }

    private static int queryPermission(CommandContext<ServerCommandSource> context, boolean admin) throws CommandSyntaxException {
        Claim claim = ClaimManager.INSTANCE.getClaim(context, getString(context, "claim"));
        ClaimUtil.validateClaim(claim);
        GameProfile gameProfile = getGameProfile(GameProfileArgumentType.getProfileArgument(context, "player"), context);
        String input = StringArgumentType.getString(context, "permission");
        boolean permission = claim.hasPermission(gameProfile.getId(), input);
        MessageSection messageSection = ItsMineConfig.main().message();
        String value = permission ? messageSection.getTrue() : messageSection.getFalse();
        UUID uuid = context.getSource().getPlayer().getUuid();
        if (claim.canModifySettings(uuid) || admin) {
            if (Permission.isValid(input)) {
                MessageUtil.sendTranslatableMessage(context.getSource(), MessageUtil.createMap("%claim%", claim.name, "%permission%", input, "%value%", value, "%player%", gameProfile.getName()), "messages", "permissionQuery");
                return 1;
            } else {
                MessageUtil.sendTranslatableMessage(context.getSource(), "messages", "invalidPermission");
                return 0;
            }
        }
        return 0;
    }

    private static int queryPermissions(CommandContext<ServerCommandSource> context, boolean admin) throws CommandSyntaxException {
        Claim claim = ClaimManager.INSTANCE.getClaim(context.getSource().getPlayer().getUuid(), getString(context, "claim"));
        GameProfile gameProfile = getGameProfile(GameProfileArgumentType.getProfileArgument(context, "player"), context);
        UUID uuid = context.getSource().getPlayer().getUuid();
        if (claim.canModifySettings(uuid) || admin) {
            MessageUtil.sendText(context.getSource(), new LiteralText("Permissions (").formatted(Formatting.YELLOW).append(gameProfile.getName()).formatted(Formatting.GOLD).append(")").formatted(Formatting.YELLOW).append(Messages.Command.getPermissions(claim, gameProfile.getId())));
            return 1;
        } else {
            MessageUtil.sendTranslatableMessage(context.getSource(), "messages", "invalidPermission");
            return 0;
        }
    }



/*
    TODO: Return a list of all permissions of a player when you do not type a permission eg. /claim permission shop DrexHD
*/


}
