package me.drexhd.itsmine.command.updated.admin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.Message;
import net.minecraft.server.command.ServerCommandSource;

import java.util.UUID;

public class BlocksCommand extends Command {

    private boolean addBlocks;

    public BlocksCommand(boolean addBlocks) {
        super("");
        this.addBlocks = addBlocks;
    }

    @Override
    public Command copy() {
        return new BlocksCommand(this.addBlocks);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, Integer> amount = RequiredArgumentBuilder.argument("amount", IntegerArgumentType.integer(1));
        amount.executes(this::execute);
        RequiredArgumentBuilder<ServerCommandSource, String> user = userArgument();
        user.then(amount);

        LiteralArgumentBuilder<ServerCommandSource> blocks = LiteralArgumentBuilder.literal(addBlocks ? "addBlocks" : "setBlocks");
        blocks.then(user);
        command.then(blocks);
    }

    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        UUID uuid = getUser(context);
        String name = getName(uuid);
        int blocks = IntegerArgumentType.getInteger(context, "amount");
        if (addBlocks) {
            ClaimManager.INSTANCE.addClaimBlocks(uuid, blocks);
            context.getSource().sendFeedback(new Message("&aGave " + blocks + " claim blocks to " + name).build(), false);
        } else {
            ClaimManager.INSTANCE.setClaimBlocks(uuid, blocks);
            context.getSource().sendFeedback(new Message("&aSet claim blocks to " + blocks + " for " + name).build(), false);
        }
        return blocks;
    }

}
