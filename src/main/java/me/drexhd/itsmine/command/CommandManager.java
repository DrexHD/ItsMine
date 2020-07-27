package me.drexhd.itsmine.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drexhd.itsmine.command.updated.Admin;
import me.drexhd.itsmine.command.updated.Other;
import me.drexhd.itsmine.command.updated.Subzone;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;


public class CommandManager {

    public static CommandDispatcher<ServerCommandSource> dispatcher;
    private static ArrayList<Command> commands = new ArrayList<>();


    public static void register() {
        LiteralArgumentBuilder<ServerCommandSource> main = LiteralArgumentBuilder.literal("itsmine");
        LiteralArgumentBuilder<ServerCommandSource> alias = LiteralArgumentBuilder.literal("claim");
        addCommand(new me.drexhd.itsmine.command.updated.BanCommand("ban", true));
        addCommand(new me.drexhd.itsmine.command.updated.BanCommand("unban", false));
        addCommand(new me.drexhd.itsmine.command.updated.BannedCommand("banned"));
        addCommand(new me.drexhd.itsmine.command.updated.BlockCommand("blocks"));
        addCommand(new me.drexhd.itsmine.command.updated.CreateCommand("create"));
        addCommand(new me.drexhd.itsmine.command.updated.ExpandCommand("expand", true));
        addCommand(new me.drexhd.itsmine.command.updated.ExpandCommand("shrink", false));
        addCommand(new me.drexhd.itsmine.command.updated.FlagCommand("flags"));
        addCommand(new me.drexhd.itsmine.command.updated.FlyCommand("fly"));
        addCommand(new me.drexhd.itsmine.command.updated.HelpCommand("help"));
        addCommand(new me.drexhd.itsmine.command.updated.InfoCommand("info"));
        addCommand(new me.drexhd.itsmine.command.updated.ListCommand("list"));
        addCommand(new me.drexhd.itsmine.command.updated.MessageCommand("message"));
        addCommand(new me.drexhd.itsmine.command.updated.PermissionCommand("permissions"));
        addCommand(new me.drexhd.itsmine.command.updated.RemoveCommand("remove"));
        addCommand(new me.drexhd.itsmine.command.updated.RenameCommand("rename"));
        addCommand(new me.drexhd.itsmine.command.updated.RentableCommand("rentable"));
        addCommand(new me.drexhd.itsmine.command.updated.RentCommand("rent"));
        addCommand(new me.drexhd.itsmine.command.updated.RevenueCommand("revenue"));
        addCommand(new me.drexhd.itsmine.command.updated.ShowCommand("show", true));
        addCommand(new me.drexhd.itsmine.command.updated.ShowCommand("hide", false));
        addCommand(new me.drexhd.itsmine.command.updated.StickCommand("stick"));
        addCommand(new me.drexhd.itsmine.command.updated.TrustCommand("trust", true));
        addCommand(new me.drexhd.itsmine.command.updated.TrustCommand("distrust", false));
        addCommand(new me.drexhd.itsmine.command.updated.TrustedCommand("trusted"));
        register(main);
        register(alias);
        dispatcher.register(main);
        dispatcher.register(alias);
//        register(main, dispatcher);
//        register(alias, dispatcher);


    }

    public static void addCommand(Command command) {
        commands.add(command);
    }

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        LiteralArgumentBuilder<ServerCommandSource> admin = LiteralArgumentBuilder.literal("admin");
        LiteralArgumentBuilder<ServerCommandSource> other = LiteralArgumentBuilder.literal("other");
        LiteralArgumentBuilder<ServerCommandSource> subzone = LiteralArgumentBuilder.literal("subzone");
        for (Command command : commands) {
            command.admin(false);
            command.others(false);
            command.register(literal);
            if (command instanceof Admin) {
                Command cmd = command.copy();
                cmd.admin(true);
                cmd.others(true);
                cmd.register(admin);
            }
            if (command instanceof Other) {
                Command cmd = command.copy();
                cmd.admin(false);
                cmd.others(true);
                cmd.register(other);
            }
            if (command instanceof Subzone) {
                Command cmd = command.copy();
                cmd.admin(false);
                cmd.others(false);
                cmd.subzones(true);
                cmd.register(subzone);
            }
        }
        literal.then(admin);
        literal.then(other);
        literal.then(subzone);
    }


}
