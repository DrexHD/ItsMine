package me.drexhd.itsmine.command.updated.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drexhd.itsmine.command.Command;
import net.minecraft.server.command.ServerCommandSource;

public class DefaultPermCommand extends Command {

    public DefaultPermCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new DefaultPermCommand(literal);
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> command) {

    }
}
