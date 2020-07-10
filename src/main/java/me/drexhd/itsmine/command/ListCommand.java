package me.drexhd.itsmine.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.util.ArgumentUtil;
import me.drexhd.itsmine.util.PermissionUtil;
import me.drexhd.itsmine.command.admin.AdminCommand;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import static me.drexhd.itsmine.command.ClaimCommand.list;
import static me.drexhd.itsmine.util.ArgumentUtil.PLAYERS_PROVIDER;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ListCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> list = literal("list");
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = argument("player", GameProfileArgumentType.gameProfile()).suggests(PLAYERS_PROVIDER);

        player.requires(source -> ItsMine.permissions().hasPermission(source, PermissionUtil.Command.ADMIN_CHECK_OTHERS, 2));
        list.executes(context -> list(context.getSource(), context.getSource().getPlayer().getGameProfile()));
        player.requires(AdminCommand.PERMISSION_CHECK_ADMIN);
        player.executes(context -> list(context.getSource(), ArgumentUtil.getGameProfile(GameProfileArgumentType.getProfileArgument(context, "player"), context)));
        list.then(player);
        command.then(list);
    }

}
