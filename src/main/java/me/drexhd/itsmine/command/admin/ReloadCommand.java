package me.drexhd.itsmine.command.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ItsMine;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.time.StopWatch;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class ReloadCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> reload = LiteralArgumentBuilder.literal("reload");
        LiteralArgumentBuilder<ServerCommandSource> config = LiteralArgumentBuilder.literal("config");
        LiteralArgumentBuilder<ServerCommandSource> claims = LiteralArgumentBuilder.literal("claims");

        reload.executes(ReloadCommand::execute);
        config.executes(ReloadCommand::config);
        reload.then(config);
        reload.then(claims);
        command.then(reload);
    }


    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        config(context);
        return 1;
    }

    public static int config(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        StopWatch watch = new StopWatch();
        watch.start();
        ItsMine.reload();
        watch.stop();
        String timeElapsed = new DecimalFormat("##.##").format(watch.getTime(TimeUnit.MICROSECONDS));
        player.sendSystemMessage(new LiteralText("Reloaded! (Took " + timeElapsed + "μs)").formatted(Formatting.YELLOW), player.getUuid());
        return 1;
    }
}
