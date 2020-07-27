package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;

public class RenameCommand extends Command implements Admin, Subzone {

    public RenameCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new RenameCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("name", word());
        nameArgument.executes(this::rename);
        literal().then(thenClaim(nameArgument));
        command.then(literal());
    }

    public int rename(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = getString(context, "claim");
        String newName = getString(context, "name");
        if (newName.length() > 30) {
            throw new SimpleCommandExceptionType(new LiteralText("Invalid claim name (too long)!")).create();
        }
        if (!newName.matches("[A-Za-z0-9]+")) {
            throw new SimpleCommandExceptionType(new LiteralText("Invalid claim name (invalid characters)!")).create();
        }
        Claim claim = getClaim(context);
        if (ClaimManager.INSTANCE.getClaim(context.getSource().getPlayer().getUuid(), newName) != null) {
            throw new SimpleCommandExceptionType(new LiteralText("You already have a claim with that name!")).create();
        }
        if (!admin && !claim.hasPermission(context.getSource().getPlayer().getUuid(), "modify", "properties")) {
            throw new SimpleCommandExceptionType(new LiteralText("You don't have permission to do this!")).create();
        }
        claim.name = newName;
        context.getSource().sendFeedback(new LiteralText("Renamed Claim " + name + " to " + claim.name).formatted(Formatting.GOLD), admin);
        return -1;
    }
}
