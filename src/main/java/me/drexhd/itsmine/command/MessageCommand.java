package me.drexhd.itsmine.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.Messages;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static me.drexhd.itsmine.util.ArgumentUtil.getEventMessage;
import static me.drexhd.itsmine.util.ArgumentUtil.getMessageEvent;
import static me.drexhd.itsmine.util.ClaimUtil.*;
import static net.minecraft.server.command.CommandManager.literal;

public class MessageCommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, boolean admin, RequiredArgumentBuilder<ServerCommandSource, String> claim) {
        LiteralArgumentBuilder<ServerCommandSource> message = literal("message");
        RequiredArgumentBuilder<ServerCommandSource, String> messageEvent = getMessageEvent();
        RequiredArgumentBuilder<ServerCommandSource, String> messageArgument = getEventMessage();
        messageArgument.executes(context -> {
            Claim claim1 = ClaimManager.INSTANCE.getClaim(getString(context, "claim"));
            if (claim1.canModifySettings(context.getSource().getPlayer().getUuid())) {
                Claim.Event event = Claim.Event.getById(getString(context, "messageEvent"));

                if (event == null) {
                    context.getSource().sendError(Messages.INVALID_MESSAGE_EVENT);
                    return -1;
                }

                return setEventMessage(context.getSource(), claim1, event, getString(context, "message"));
            }

            return -1;
        });

        messageEvent.then(messageArgument);
        claim.then(messageEvent);
        message.then(claim);
        command.then(message);
    }

}
