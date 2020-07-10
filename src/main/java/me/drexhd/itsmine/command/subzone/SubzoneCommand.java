package me.drexhd.itsmine.command.subzone;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.command.*;
import net.minecraft.server.command.ServerCommandSource;

import static me.drexhd.itsmine.util.ArgumentUtil.getSubzones;
import static net.minecraft.server.command.CommandManager.literal;

public class SubzoneCommand {
    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, CommandDispatcher dispatcher, boolean admin) {
        LiteralArgumentBuilder<ServerCommandSource> subzone = literal("subzone");
        subzone.requires(source -> ItsMine.permissions().hasPermission(source, "itsmine." + "subzone", 2));
        registerSubzone(subzone, dispatcher, admin);
        command.then(subzone);
    }

    public static void registerSubzone(LiteralArgumentBuilder<ServerCommandSource> command, CommandDispatcher dispatcher, boolean admin) {
        CreateCommand.register(command, admin);
        ExpandCommand.register(command, admin);
        InfoCommand.register(command, getSubzones(), admin);
        MessageCommand.register(command, admin, getSubzones());
        PermissionCommand.register(command, admin, getSubzones());
        RemoveCommand.register(command, getSubzones(), admin);
        RenameCommand.register(command, admin);
        RentableCommand.register(command, getSubzones());
        RentCommand.register(command, getSubzones());
        RevenueCommand.register(command, getSubzones());
        FlagCommand.register(command, admin, getSubzones());
        TrustCommand.register(command, dispatcher, getSubzones(), admin);
        TrustedCommand.register(command);
    }
}