package me.drexhd.itsmine.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.util.ArgumentUtil;
import me.drexhd.itsmine.util.ClaimUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RenameCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, boolean admin) {
        LiteralArgumentBuilder<ServerCommandSource> rename = literal("rename");
        RequiredArgumentBuilder<ServerCommandSource, String> claimArgument = ArgumentUtil.getClaims();
        RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("name", word());
        nameArgument.executes((context) -> rename(context, admin));
        claimArgument.then(nameArgument);
        rename.then(claimArgument);
        command.then(rename);
    }

    public static int rename(CommandContext<ServerCommandSource> context, boolean admin) throws CommandSyntaxException {
        String name = getString(context, "claim");
        String newName = getString(context, "name");
        if(!newName.matches("[A-Za-z0-9]+")){
            context.getSource().sendError(new LiteralText("Invalid claim name"));
            return -1;
        }
        Claim claim = ClaimManager.INSTANCE.getClaim(context.getSource().getPlayer().getUuid(), name);
        ClaimUtil.validateClaim(claim);
        if (ClaimManager.INSTANCE.getClaim(context.getSource().getPlayer().getUuid(), newName) != null) {
            context.getSource().sendError(new LiteralText("That name is already taken!"));
            return -1;
        }
        if (!admin && !claim.hasPermission(context.getSource().getPlayer().getUuid(), "modify", "properties")) {
            context.getSource().sendError(new LiteralText("You don't have permission to modify claim properties!"));
            return -1;
        }
        if(claim.isChild) claim.name = ClaimUtil.getParentClaim(claim).name + "." + newName;
        else claim.name = newName;
//        ClaimManager.INSTANCE.updateClaim(claim);
        context.getSource().sendFeedback(new LiteralText("Renamed Claim " + name + " to " + claim.name).formatted(Formatting.GOLD), admin);
        return -1;
    }

    }
