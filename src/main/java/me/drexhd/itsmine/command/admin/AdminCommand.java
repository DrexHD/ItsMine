package me.drexhd.itsmine.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.command.MessageCommand;
import me.drexhd.itsmine.command.PermissionCommand;
import me.drexhd.itsmine.command.FlagCommand;
import me.drexhd.itsmine.command.RemoveCommand;
import me.drexhd.itsmine.command.subzone.SubzoneCommand;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

import static me.drexhd.itsmine.util.ArgumentUtil.getClaims;
import static net.minecraft.server.command.CommandManager.literal;

public class AdminCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, CommandDispatcher dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> admin = literal("admin");
        admin.requires(PERMISSION_CHECK_ADMIN);
        registerAdmin(admin, dispatcher);
        command.then(admin);
    }

    private static void registerAdmin(LiteralArgumentBuilder<ServerCommandSource> admin, CommandDispatcher dispatcher) {
        BlocksCommand.register(admin);
        CreateCommand.register(admin);
        EntitiesCommand.register(admin);
        ExpandCommand.register(admin);
        IgnoreCommand.register(admin);
        InfoCommand.register(admin);
        ListAllCommand.register(admin);
        MessageCommand.register(admin, true, getClaims());
        OwnerCommand.register(admin);
        PermissionCommand.register(admin, true, getClaims());
        ReloadCommand.register(admin);
        RemoveCommand.register(admin, getClaims(), true);
        RemoveAllCommand.register(admin);
        RenameCommand.register(admin);
        FlagCommand.register(admin, true, getClaims());
        SubzoneCommand.register(admin, dispatcher, true);
    }

    private static Predicate<ServerCommandSource> perm(String str) {
        return perm(str, 2);
    }
    private static Predicate<ServerCommandSource> perm(String str, int op) {
        return source -> ItsMine.permissions().hasPermission(source, "itsmine." + str, op);
    }
    public static final Predicate<ServerCommandSource> PERMISSION_CHECK_ADMIN = src -> perm("admin").test(src);

}
