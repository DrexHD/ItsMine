package me.drexhd.itsmine.command.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.command.ClaimCommand;
import net.minecraft.server.command.ServerCommandSource;

public class ListCommand {
    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> list = LiteralArgumentBuilder.literal("list");

        list.executes(context -> ClaimCommand.list(context.getSource(), ClaimManager.serverProfile));
        command.then(list);
    }

}
