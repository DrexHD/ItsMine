package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.Message;
import net.minecraft.server.command.ServerCommandSource;

public class ColorCommand extends Command {
    public ColorCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new ColorCommand(literal);
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, String> input = RequiredArgumentBuilder.argument("input", StringArgumentType.greedyString());
        input.executes(this::execute);
        literal().then(input);
        command.then(literal());
    }

    public int execute(CommandContext<ServerCommandSource> context) {
        String input = StringArgumentType.getString(context, "input");
        context.getSource().sendFeedback(new Message(input).build(), false);
        return 1;
    }
}
