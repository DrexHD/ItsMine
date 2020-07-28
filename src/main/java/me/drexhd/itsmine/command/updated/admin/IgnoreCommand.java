package me.drexhd.itsmine.command.updated.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.PermissionUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class IgnoreCommand extends Command {

    public IgnoreCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new IgnoreCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        literal().requires(source -> ItsMine.permissions().hasPermission(source, PermissionUtil.Command.ADMIN_IGNORE_CLAIMS, 2));
        literal().executes(this::execute);
        command.then(literal());
    }

    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        UUID uuid = context.getSource().getPlayer().getUuid();
        boolean isIgnoring = ClaimManager.INSTANCE.ignoringClaims.contains(uuid);
        if (isIgnoring) ClaimManager.INSTANCE.ignoringClaims.remove(uuid);
        else ClaimManager.INSTANCE.ignoringClaims.add(uuid);
        context.getSource().sendFeedback(new LiteralText("You are " + (isIgnoring ? "no longer" : "now") + " ignoring claims.").formatted(Formatting.GREEN), false);
        return 0;
    }
}
