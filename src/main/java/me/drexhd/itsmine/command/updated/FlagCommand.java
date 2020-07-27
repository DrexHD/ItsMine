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
import me.drexhd.itsmine.claim.flag.Flag;
import me.drexhd.itsmine.claim.permission.Permission;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.config.sections.MessageSection;
import me.drexhd.itsmine.util.MessageUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static me.drexhd.itsmine.util.ArgumentUtil.getFlags;
import static me.drexhd.itsmine.util.ClaimUtil.validateCanAccess;
import static net.minecraft.server.command.CommandManager.argument;

public class FlagCommand extends Command implements Admin, Other, Subzone {

    public FlagCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new FlagCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, Boolean> value = argument("value", BoolArgumentType.bool());
        value.executes(this::set);
        LiteralArgumentBuilder<ServerCommandSource> reset = literal("reset");
        reset.executes(this::reset);

        RequiredArgumentBuilder<ServerCommandSource, String> flags = getFlags();
        flags.then(value);
        flags.then(reset);
        flags.executes(this::query);

        literal().then(thenClaim(flags)) ;
        literal().executes(this::execute);
        command.then(literal());
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        context.getSource().sendFeedback(new LiteralText("\n")
                .append(new LiteralText("Flags: " + claim.name)
                        .formatted(Formatting.YELLOW))
                .append(new LiteralText("\n"))
                .append(Messages.Command.getFlags(claim))
                .append(new LiteralText("\n")), false);
        return 1;
    }

    public int query(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        validateCanAccess(player, claim, admin);
        String input = StringArgumentType.getString(context, "flag");

        if (Flag.isValid(input)) {
            return queryFlag(source, claim, input);
        } else if (Permission.isValid(input)) {
            return queryPermission(source, claim, input);
        }

        MessageUtil.sendTranslatableMessage(source, "messages", "invalidFlag");
        return -1;
    }

    public int queryFlag(ServerCommandSource source, Claim claim, String flag) {
        boolean enabled = claim.flagManager.hasFlag(flag);
        MessageSection messageSection = ItsMineConfig.main().message();
        String value = enabled ? messageSection.getTrue() : messageSection.getFalse();
        MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%flag%", flag, "%value%", value), ItsMineConfig.main().message().flagQuery);
        return 1;
    }

    public int queryPermission(ServerCommandSource source, Claim claim, String permission) {
        boolean enabled = claim.permissionManager.defaults.hasPermission(permission);
        MessageSection messageSection = ItsMineConfig.main().message();
        String value = enabled ? messageSection.getTrue() : messageSection.getFalse();
        MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%flag%", permission, "%value%", value), ItsMineConfig.main().message().flagQuery);
        return 1;
    }


    public int reset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        validateCanAccess(player, claim, admin);
        String input = StringArgumentType.getString(context, "flag");

        if (Flag.isValid(input)) {
            return resetFlag(source, claim, input);
        } else if (Permission.isValid(input)) {
            return resetPermission(source, claim, input);
        }

        MessageUtil.sendTranslatableMessage(source, "messages", "invalidFlag");
        return -1;
    }

    public int resetPermission(ServerCommandSource source, Claim claim, String permission) {
        claim.permissionManager.defaults.clearPermission(permission);
        MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%flag%", permission), ItsMineConfig.main().message().flagReset);
        return 1;
    }

    public int resetFlag(ServerCommandSource source, Claim claim, String flag) {
        claim.flagManager.clearFlag(flag);
        MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%flag%", flag), ItsMineConfig.main().message().flagReset);
        return 1;
    }

    public int set(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        validateCanAccess(player, claim, admin);
        String input = StringArgumentType.getString(context, "flag");
        boolean bool = BoolArgumentType.getBool(context, "value");

        if (Flag.isValid(input)) {
            return setFlag(source, claim, input, bool);
        } else if (Permission.isValid(input)) {
            return setPermission(source, claim, input, bool);
        }

        MessageUtil.sendTranslatableMessage(source, "messages", "invalidFlag");
        return -1;
    }

    public static int setFlag(ServerCommandSource source, Claim claim, String flag, boolean value) {
        claim.flagManager.setFlag(flag, value);
        MessageSection messageSection = ItsMineConfig.main().message();
        String formattedValue = value ? messageSection.getTrue() : messageSection.getFalse();
        MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%flag%", flag, "%value%", formattedValue), ItsMineConfig.main().message().flagSet);
        return 0;
    }

    public static int setPermission(ServerCommandSource source, Claim claim, String permission, boolean value) {
        claim.permissionManager.defaults.setPermission(permission, value);
        MessageSection messageSection = ItsMineConfig.main().message();
        String formattedValue = value ? messageSection.getTrue() : messageSection.getFalse();
        MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%permission%", permission, "%value%", formattedValue), ItsMineConfig.main().message().defaultPermissionSet);
        return 1;
    }

}
