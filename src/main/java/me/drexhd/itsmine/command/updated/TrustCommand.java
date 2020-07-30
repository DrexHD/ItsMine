package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.claim.permission.map.InvertedMap;
import me.drexhd.itsmine.command.Command;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;

public class TrustCommand extends Command implements Admin, Other, Subzone {

    private final boolean trust;
    public SuggestionProvider<ServerCommandSource> TRUST_PROVIDER = (source, builder) -> {
        List<String> strings = new ArrayList<>();
        Claim claim = getClaim(source);
        for (UUID uuid : claim.permissionManager.playerPermissions.keySet()) {
            strings.add(getName(uuid));
        }
        return CommandSource.suggestMatching(strings, builder);
    };

    public TrustCommand(String literal, boolean trust) {
        super(literal);
        this.trust = trust;
    }

    @Override
    public Command copy() {
        return new TrustCommand(literal, trust);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, String> player = trust ? userArgument() : trustedArgument();
        player.executes(this::execute);
        player.then(thenClaim());
        literal().then(player);
        command.then(literal());
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        UUID uuid = getUser(context);
        String name = getName(uuid);
        validatePermission(claim, context.getSource().getPlayer().getUuid(), "modify", "permissions");
        if (trust) {
            claim.permissionManager.playerPermissions.put(uuid, new InvertedMap());
        } else {
            claim.permissionManager.playerPermissions.remove(uuid);
        }
        context.getSource().sendFeedback(new LiteralText(name + (trust ? " now" : " no longer") + " has all the permissions").formatted(Formatting.YELLOW), false);
        return 1;
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> trustedArgument() {
        return argument("user", word()).suggests(TRUST_PROVIDER);
    }
}
