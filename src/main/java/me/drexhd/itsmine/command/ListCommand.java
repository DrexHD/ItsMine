package me.drexhd.itsmine.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.util.PermissionUtil;
import me.drexhd.itsmine.command.admin.AdminCommand;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static me.drexhd.itsmine.command.ClaimCommand.list;
import static me.drexhd.itsmine.util.ArgumentUtil.getPlayers;
import static net.minecraft.server.command.CommandManager.literal;

public class ListCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> list = literal("list");
        RequiredArgumentBuilder<ServerCommandSource, String> player = getPlayers();
        player.requires(source -> ItsMine.permissions().hasPermission(source, PermissionUtil.Command.ADMIN_CHECK_OTHERS, 2));
        list.executes(context -> list(context.getSource(), context.getSource().getName()));
        player.requires(AdminCommand.PERMISSION_CHECK_ADMIN);
        player.executes(context -> list(context.getSource(), getString(context, "player")));
        list.then(player);
        command.then(list);
    }

}
