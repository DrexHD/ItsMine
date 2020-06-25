package me.drexhd.itsmine.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.drexhd.itsmine.command.admin.AdminCommand;
import me.drexhd.itsmine.command.subzone.SubzoneCommand;
import net.minecraft.server.command.ServerCommandSource;

import static me.drexhd.itsmine.util.ArgumentUtil.getClaims;


public class CommandManager {

    public static CommandDispatcher<ServerCommandSource> dispatcher;


    public static void register() {
        LiteralArgumentBuilder<ServerCommandSource> main = LiteralArgumentBuilder.literal("itsmine");
        LiteralArgumentBuilder<ServerCommandSource> alias = LiteralArgumentBuilder.literal("claim");
//        LiteralArgumentBuilder<ServerCommandSource> test = LiteralArgumentBuilder.literal("convert");
        register(main, dispatcher);
        register(alias, dispatcher);
//        test.executes(CommandManager::generateCommands);
//        dispatcher.register(test);
        dispatcher.register(main);
        dispatcher.register(alias);
    }


    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, CommandDispatcher dispatcher) {
        AdminCommand.register(command, dispatcher);
        BlockCommand.register(command);
        CreateCommand.register(command);
        ClaimCommand.register(command, dispatcher);
//        DebugCommand.register(command);
        ExpandCommand.register(command, false);
        FlyCommand.register(command);
        HelpCommand.register(command);
        InfoCommand.register(command, getClaims());
        ListCommand.register(command);
        MessageCommand.register(command, false, getClaims());
        PermissionCommand.register(command, false, getClaims());
        RemoveCommand.register(command, getClaims(), false);
        RenameCommand.register(command, false);
        RentableCommand.register(command, getClaims());
        RentCommand.register(command, getClaims());
        RevenueCommand.register(command, getClaims());
        FlagCommand.register(command, false, getClaims());
        ShowCommand.register(command);
        StickCommand.register(command);
        SubzoneCommand.register(command, dispatcher, false);
        TransferCommand.register(command);
        TrustCommand.register(command, dispatcher, getClaims(), false);
        TrustedCommand.register(command);
    }

    private static int generateCommands(CommandContext<ServerCommandSource> context) {
/*        Scoreboard scoreboard = context.getSource().getMinecraftServer().getScoreboard();
        ItsMine.scoreboard = scoreboard;
        ItsMine.convert = scoreboard.getKnownPlayers();
        ItsMine.source = context.getSource();*/
/*        Scoreboard scoreboard = context.getSource().getMinecraftServer().getScoreboard();
        ItsMine.convert = scoreboard.getKnownPlayers();
        System.out.println("running");
        int size = scoreboard.getKnownPlayers().size();
        int i = 0;
        for(String player : scoreboard.getKnownPlayers()) {
            ScoreboardObjective minutes = scoreboard.getObjective("minutes");
            ScoreboardObjective votes = scoreboard.getObjective("totalVotes");
            int min = scoreboard.getPlayerScore(player, minutes).getScore();
            int vote = scoreboard.getPlayerScore(player, votes).getScore();
            if(min >= 300 && vote >= 6) run(context.getSource(), player, "player");
            if(min >= 1500 && vote >= 24) run(context.getSource(), player, "player_plus");
            if(min >= 5000 && vote >= 48) run(context.getSource(), player, "member");
            if(min >= 10000 && vote >= 72) run(context.getSource(), player, "kilocrafter");
            if(min >= 30000 && vote >= 150) run(context.getSource(), player, "kilocrafter_plus");
            i++;
            System.out.println(i + " / " + size);
        }
        return 1;*/
        return 1;
    }

}
