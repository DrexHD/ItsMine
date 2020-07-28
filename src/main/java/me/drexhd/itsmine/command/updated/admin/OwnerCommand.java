package me.drexhd.itsmine.command.updated.admin;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class OwnerCommand extends Command {

    public OwnerCommand(String literal) {
        super(literal);
        this.others = true;
    }

    @Override
    public Command copy() {
        return new OwnerCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, String> owner = ownerArgument();
        owner.executes(this::setOwner);
        literal().then(thenClaim(owner));
        command.then(literal());
    }

    public int setOwner(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        UUID uuid = getOwner(context);
        String name = getName(uuid);
        ServerCommandSource source = context.getSource();
        GameProfile oldOwner = source.getMinecraftServer().getUserCache().getByUuid(claim.claimBlockOwner);
        source.sendFeedback(new LiteralText("Set the Claim Owner to ")
                        .formatted(Formatting.YELLOW).append(new LiteralText(name).formatted(Formatting.GOLD)).append(new LiteralText(" from "))
                        .append(new LiteralText(oldOwner == null ? "(" + claim.claimBlockOwner + ")" : oldOwner.getName()).formatted(Formatting.GOLD))
                        .append(new LiteralText(" for ").formatted(Formatting.YELLOW)).append(new LiteralText(claim.name).formatted(Formatting.GOLD))
                , false);
        //Update ClaimList
        ClaimManager.INSTANCE.removeClaim(claim);
        claim.claimBlockOwner = uuid;
        ClaimManager.INSTANCE.addClaim(claim);
        return 1;
    }
}
