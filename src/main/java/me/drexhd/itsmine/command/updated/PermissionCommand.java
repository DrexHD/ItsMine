package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.Messages;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.claim.permission.Permission;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.config.sections.MessageSection;
import me.drexhd.itsmine.util.MessageUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.UUID;

import static me.drexhd.itsmine.util.ArgumentUtil.getPermissions;
import static net.minecraft.server.command.CommandManager.argument;

public class PermissionCommand extends Command implements Admin, Other, Subzone {

    public PermissionCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new PermissionCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, Boolean> set = argument("set", BoolArgumentType.bool());
        set.executes(this::set);
        LiteralArgumentBuilder<ServerCommandSource> reset = literal("reset");
        reset.executes(this::reset);
        RequiredArgumentBuilder<ServerCommandSource, String> permNode = getPermissions();
        permNode.then(set);
        permNode.then(reset);
        permNode.executes(this::queryPermission);
        RequiredArgumentBuilder<ServerCommandSource, String> player = userArgument();
        player.then(permNode);
        player.executes(this::queryPermissions);

        literal().then(thenClaim(player));
        literal().executes((context) -> HelpCommand.sendPage(context.getSource(), Messages.SETTINGS_AND_PERMISSIONS, 1, "Claim Permissions and Flags", "/claim help perms_and_flags %page%"));
        command.then(literal());
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        return showTrusted(context.getSource(), claim);
    }

    public int set(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        UUID uuid = getUser(context);
        String name = getName(uuid);
        boolean value = BoolArgumentType.getBool(context, "set");
        String input = StringArgumentType.getString(context, "permission");
        MessageSection messageSection = ItsMineConfig.main().message();
        String formattedValue = value ? messageSection.getTrue() : messageSection.getFalse();
        ServerCommandSource source = context.getSource();
        if (claim.canModifySettings(source.getPlayer().getUuid()) || admin) {
            MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%permission%", input, "%value%", formattedValue, "%player%", name), ItsMineConfig.main().message().permissionSet);
            claim.permissionManager.setPermission(uuid, input, value);
        }
        return 0;
    }

    public int reset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        UUID uuid = getUser(context);
        String name = getName(uuid);
        String input = StringArgumentType.getString(context, "permission");
        ServerCommandSource source = context.getSource();
        if (claim.canModifySettings(source.getPlayer().getUuid()) || admin) {
            MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%permission%", input, "%player%", name), ItsMineConfig.main().message().permissionReset);
            claim.permissionManager.clearPermission(uuid, input);
        }
        return 0;
    }

    public int queryPermission(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        UUID userUUID = getUser(context);
        String name = getName(userUUID);
        String input = StringArgumentType.getString(context, "permission");
        boolean value = claim.hasPermission(userUUID, input, null);
        MessageSection messageSection = ItsMineConfig.main().message();
        String formattedValue = value ? messageSection.getTrue() : messageSection.getFalse();
        UUID sourceUUID = context.getSource().getPlayer().getUuid();
        if (claim.canModifySettings(sourceUUID) || admin) {
            if (Permission.isValid(input)) {
                MessageUtil.sendTranslatableMessage(context.getSource(), MessageUtil.createMap("%claim%", claim.name, "%permission%", input, "%value%", formattedValue, "%player%", name), ItsMineConfig.main().message().permissionQuery);
                return 1;
            } else {
                MessageUtil.sendTranslatableMessage(context.getSource(), "messages", "invalidPermission");
                return 0;
            }
        }
        return 0;
    }

    public int queryPermissions(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        UUID userUUID = getUser(context);
        String name = getName(userUUID);
        UUID uuid = context.getSource().getPlayer().getUuid();
        if (claim.canModifySettings(uuid) || admin) {
            MessageUtil.sendText(context.getSource(), new LiteralText("Permissions (")
                    .formatted(Formatting.YELLOW)
                    .append(name)
                    .formatted(Formatting.GOLD).append(")")
                    .formatted(Formatting.YELLOW)
                    .append(Messages.Command.getPermission(claim, uuid)));
            return 1;
        } else {
            MessageUtil.sendTranslatableMessage(context.getSource(), ItsMineConfig.main().message().invalidPermission);
            return 0;
        }
    }
}
