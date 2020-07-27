package me.drexhd.itsmine.command.updated.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.command.Command;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.time.StopWatch;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class ReloadCommand extends Command {

    public ReloadCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new ReloadCommand(literal);
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        literal().executes(this::reload);
        command.then(literal());
    }

    public int reload(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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
