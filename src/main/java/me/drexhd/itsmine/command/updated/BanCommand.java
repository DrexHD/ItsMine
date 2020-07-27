package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;

public class BanCommand extends Command implements Admin, Other, Subzone {

    private final boolean ban;
    public SuggestionProvider<ServerCommandSource> BAN_PROVIDER = (source, builder) -> {
        List<String> strings = new ArrayList<>();
        Claim claim = getClaim(source);
        for (UUID uuid : claim.banManager.getBannedPlayers()) {
            strings.add(getName(uuid));
        }
        return CommandSource.suggestMatching(strings, builder);
    };

    public BanCommand(String literal, boolean ban) {
        super(literal);
        this.ban = ban;
    }

    @Override
    public Command copy() {
        return new BanCommand(literal, ban);
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, String> user = ban ? userArgument() : bannedArgument();
        user.executes(this::execute);
        user.then(thenClaim());
        literal().then(user);
        command.then(literal());
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        UUID uuid = getUser(context);
        String name = getName(uuid);
        validatePermission(claim, uuid, "modify", "ban");
        if ((claim.banManager.isBanned(uuid) && ban) || (!claim.banManager.isBanned(uuid) && !ban)) {
            throw new SimpleCommandExceptionType(new LiteralText(name + " is " + (ban ? "already" : "not") + " banned!")).create();
        } else {
            if (ban) {
                claim.banManager.ban(uuid);
                BlockPos loc = ItsMineConfig.main().spawnSection().getBlockPos();
                getOnlineUser(uuid).ifPresent(player -> {
                    player.sendSystemMessage(new LiteralText("You have been banned from " + claim.name + "!").formatted(Formatting.RED), player.getUuid());
                    player.teleport(loc.getX(), loc.getY(), loc.getZ());
                });
            } else {
                claim.banManager.unban(uuid);
            }
            context.getSource().sendFeedback(new LiteralText(name + (ban ? " is now" : " is no longer") + " banned").formatted(Formatting.YELLOW), false);
        }
        return 1;
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> bannedArgument() {
        return argument("user", word()).suggests(BAN_PROVIDER);
    }

}
