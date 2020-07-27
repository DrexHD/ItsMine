package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.command.Command;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class FlyCommand extends Command {

    public FlyCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new FlyCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        literal().executes(this::toggleFly);
        command.then(literal());
    }

    public int toggleFly(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        UUID uuid = context.getSource().getPlayer().getUuid();
        boolean enabled = ClaimManager.INSTANCE.flyers.contains(uuid);
        if (enabled) {
            source.sendFeedback(new LiteralText("Disabled Ability to fly in Claims").formatted(Formatting.RED), false);
            ClaimManager.INSTANCE.flyers.remove(uuid);
            return -1;
        } else {
            ClaimManager.INSTANCE.flyers.add(uuid);
            source.sendFeedback(new LiteralText("Enabled Ability to fly in Claims").formatted(Formatting.GREEN), false);
            return 1;
        }
    }
}
