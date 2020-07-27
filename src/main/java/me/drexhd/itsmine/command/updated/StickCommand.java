package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.command.Command;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

public class StickCommand extends Command {

    public StickCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new StickCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        literal().executes(this::execute);
        command.then(literal());
    }

    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Pair<BlockPos, BlockPos> posPair = ClaimManager.INSTANCE.stickPositions.get(context.getSource().getPlayer());
        ServerCommandSource source = context.getSource();
        if (posPair == null) {
            source.sendFeedback(new LiteralText("You can now use a stick or sneak right-/leftclick to create claims. Run this command again to disable").formatted(Formatting.GREEN), false);
            ClaimManager.INSTANCE.stickPositions.put(context.getSource().getPlayer(), new Pair<>(null, null));
        } else {
            source.sendFeedback(new LiteralText("Claim stick disabled. Run this command again to enable").formatted(Formatting.RED), false);
            ClaimManager.INSTANCE.stickPositions.remove(context.getSource().getPlayer());
        }
        return 0;
    }
}
