package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.ClaimUtil;
import me.drexhd.itsmine.util.PermissionUtil;
import net.minecraft.server.command.ServerCommandSource;

import java.util.UUID;

public class BlockCommand extends Command implements Admin {

    public BlockCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new BlockCommand(literal);
    }

    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, String> user = userArgument();
        user.requires(source -> ItsMine.permissions().hasPermission(source, PermissionUtil.Command.ADMIN_CHECK_OTHERS, 2));
        user.executes(this::blocksLeft);

        literal().then(user);
        literal().executes(this::blocksLeft);
        command.then(literal());
    }

    public int blocksLeft(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        UUID uuid = getUser(context);
        return ClaimUtil.blocksLeft(source, uuid);
    }
}
