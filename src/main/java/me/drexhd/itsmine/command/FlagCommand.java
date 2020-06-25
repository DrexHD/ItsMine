package me.drexhd.itsmine.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.Messages;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.claim.flag.Flag;
import me.drexhd.itsmine.claim.permission.Permission;
import me.drexhd.itsmine.config.sections.MessageSection;
import me.drexhd.itsmine.util.MessageUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static me.drexhd.itsmine.util.ArgumentUtil.getFlags;
import static me.drexhd.itsmine.util.ClaimUtil.validateCanAccess;
import static me.drexhd.itsmine.util.ClaimUtil.validateClaim;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FlagCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, boolean admin, RequiredArgumentBuilder<ServerCommandSource, String> claim) {
        LiteralArgumentBuilder<ServerCommandSource> flags = literal("flags");
        RequiredArgumentBuilder<ServerCommandSource, String> id = getFlags();
        RequiredArgumentBuilder<ServerCommandSource, Boolean> set = argument("set", BoolArgumentType.bool());
        LiteralArgumentBuilder<ServerCommandSource> reset = literal("reset");
        flags.executes((context) -> HelpCommand.sendPage(context.getSource(), Messages.SETTINGS_AND_PERMISSIONS, 1, "Claim Permissions and Flags", "/claim help perms_and_flags %page%"));
        claim.executes((context) -> {
            Claim claim1 = ClaimManager.INSTANCE.getClaim(getString(context, "claim"));
            validateClaim(claim1);
            return queryFlags(context.getSource(), claim1);
        });

        id.executes((context) -> executeFlag(context.getSource(), getString(context, "flag"), getString(context, "claim"), true, false, false, admin));
        set.executes((context) -> executeFlag(context.getSource(), getString(context, "flag"), getString(context, "claim"), false, BoolArgumentType.getBool(context, "set"), false, admin));
        reset.executes((context) -> executeFlag(context.getSource(), getString(context, "flag"), getString(context, "claim"), false, false, true, admin));

        id.then(set);
        id.then(reset);
        claim.then(id);
        flags.then(claim);
        command.then(flags);
    }

    public static int queryFlags(ServerCommandSource source, Claim claim) {
        source.sendFeedback(new LiteralText("\n").append(new LiteralText("Flags: " + claim.name).formatted(Formatting.YELLOW)).append(new LiteralText("\n"))
                .append(Messages.Command.getFlags(claim)).append(new LiteralText("\n")), false);
        return 1;
    }

    public static int executeFlag(ServerCommandSource source, String input, @Nullable String claimName, boolean isQuery, boolean value, boolean reset, boolean admin) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        Claim claim = claimName == null || claimName.isEmpty() ? ClaimManager.INSTANCE.getClaimAt(player.getBlockPos(), player.getEntityWorld().getDimension()) : ClaimManager.INSTANCE.getClaim(claimName);
        validateClaim(claim);
        validateCanAccess(player, claim, admin);
        Flag flag = Flag.byID(input);

        if (flag != null) {
            if (reset) return resetFlag(source, claim, flag.getID());
            return isQuery ? queryFlag(source, claim, flag.getID()) : setFlag(source, claim, flag.getID(), value);
        }
        if (Permission.isValid(input)) {
            if (reset) return resetPermission(source, claim, input);
            return isQuery ? queryPermission(source, claim, input) : setPermission(source, claim, input, value);
        }

        MessageUtil.sendTranslatableMessage(source, "messages", "invalidFlag");
        return -1;
    }

    private static int queryFlag(ServerCommandSource source, Claim claim, String flag) {
        boolean enabled = claim.flagManager.hasFlag(flag);
        MessageSection messageSection = ItsMineConfig.main().message();
        String value = enabled ? messageSection.getTrue() : messageSection.getFalse();
        MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%flag%", flag, "%value%", value), "messages", "flagQuery");
        return 1;
    }

    private static int queryPermission(ServerCommandSource source, Claim claim, String permission) {
        boolean enabled = claim.permissionManager.defaults.hasPermission(permission);
        MessageSection messageSection = ItsMineConfig.main().message();
        String value = enabled ? messageSection.getTrue() : messageSection.getFalse();
        MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%flag%", permission, "%value%", value), "messages", "flagQuery");
        return 1;
    }

    private static int setFlag(ServerCommandSource source, Claim claim, String flag, boolean set) {
        claim.flagManager.setFlag(flag, set);
        MessageSection messageSection = ItsMineConfig.main().message();
        String value = set ? messageSection.getTrue() : messageSection.getFalse();
        MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%flag%", flag, "%value%", value), "messages", "flagSet");
        return 0;
    }

    private static int setPermission(ServerCommandSource source, Claim claim, String permission, boolean set) {
        claim.permissionManager.defaults.setPermission(permission, set);
        MessageSection messageSection = ItsMineConfig.main().message();
        String value = set ? messageSection.getTrue() : messageSection.getFalse();
        MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%permission%", permission, "%value%", value), "messages", "defaultPermissionSet");
        return 1;
    }

    private static int resetPermission(ServerCommandSource source, Claim claim, String permission) {
        claim.permissionManager.defaults.clearPermission(permission);
        MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%flag%", permission), "messages", "flagReset");
        return 1;
    }

    private static int resetFlag(ServerCommandSource source, Claim claim, String flag) {
        claim.flagManager.clearFlag(flag);
        MessageUtil.sendTranslatableMessage(source, MessageUtil.createMap("%claim%", claim.name, "%flag%", flag), "messages", "flagReset");
        return 1;
    }
}
