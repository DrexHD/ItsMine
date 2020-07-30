package me.drexhd.itsmine.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.command.updated.*;
import me.drexhd.itsmine.command.updated.admin.*;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;


public class CommandManager {

    public static CommandDispatcher<ServerCommandSource> dispatcher;
    private static ArrayList<Command> commands = new ArrayList<>();


    public static void register() {
        LiteralArgumentBuilder<ServerCommandSource> main = LiteralArgumentBuilder.literal("itsmine");
        LiteralArgumentBuilder<ServerCommandSource> alias = LiteralArgumentBuilder.literal("claim");
        addCommand(new BanCommand("ban", true));
        addCommand(new BanCommand("unban", false));
        addCommand(new BannedCommand("banned"));
        addCommand(new BlockCommand("blocks"));
        addCommand(new ColorCommand("color"));
        addCommand(new CreateCommand("create"));
        addCommand(new ExpandCommand("expand", true));
        addCommand(new ExpandCommand("shrink", false));
        addCommand(new FlagCommand("flags"));
        addCommand(new FlyCommand("fly"));
        addCommand(new HelpCommand("help"));
        addCommand(new InfoCommand("info"));
        addCommand(new ListCommand("list"));
        addCommand(new MessageCommand("message"));
        addCommand(new PermissionCommand("permissions"));
        addCommand(new RemoveCommand("remove"));
        addCommand(new RenameCommand("rename"));
        addCommand(new RentableCommand("rentable"));
        addCommand(new RentCommand("rent"));
        addCommand(new RevenueCommand("revenue"));
        addCommand(new ShowCommand("show", true));
        addCommand(new ShowCommand("hide", false));
        addCommand(new StickCommand("stick"));
        addCommand(new TrustCommand("trust", true));
        addCommand(new TrustCommand("distrust", false));
        addCommand(new TrustedCommand("trusted"));
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
        admin.requires(src -> ItsMine.permissions().hasPermission(src, "admin", 2));
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

        new BlocksCommand(true).register(admin);
        new BlocksCommand(false).register(admin);
        new IgnoreCommand("ignore").register(admin);
        new ModInfoCommand("info").register(admin);
        new OwnerCommand("setOwner").register(admin);
        new ReloadCommand("reload").register(admin);

        literal.then(admin);
        literal.then(other);
        literal.then(subzone);
    }


}
