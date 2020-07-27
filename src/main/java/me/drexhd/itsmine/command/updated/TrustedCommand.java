package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import net.minecraft.server.command.ServerCommandSource;

public class TrustedCommand extends Command implements Other, Subzone {

    public TrustedCommand(String literal) {
        super(literal);
        this.suggestCurrent = true;
    }

    @Override
    public Command copy() {
        return new TrustedCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        literal().then(thenClaim());
        literal().executes(this::execute);
        command.then(literal());
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        return showTrusted(context.getSource(), claim);

    }
}
