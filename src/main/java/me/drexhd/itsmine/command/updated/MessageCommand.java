package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drexhd.itsmine.Messages;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.ChatColor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static me.drexhd.itsmine.util.ArgumentUtil.getEventMessage;
import static me.drexhd.itsmine.util.ArgumentUtil.getMessageEvent;

public class MessageCommand extends Command implements Admin, Other, Subzone{

    public MessageCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new MessageCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, String> messageEvent = getMessageEvent();
        RequiredArgumentBuilder<ServerCommandSource, String> messageArgument = getEventMessage();
        messageArgument.executes(this::execute);

        messageEvent.then(messageArgument);
        literal().then(thenClaim(messageEvent));
        command.then(literal());
    }

    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        ServerCommandSource source = context.getSource();
        if (claim.canModifySettings(source.getPlayer().getUuid()) || admin) {
            Claim.Event event = Claim.Event.getById(getString(context, "messageEvent"));
            if (event == null) {
                throw new SimpleCommandExceptionType(Messages.INVALID_MESSAGE_EVENT).create();
            }
            String message = getString(context, "message");
            switch (event) {
                case ENTER_CLAIM:
                    claim.enterMessage = message.equalsIgnoreCase("reset") ? null : message;
                    break;
                case LEAVE_CLAIM:
                    claim.leaveMessage = message.equalsIgnoreCase("reset") ? null : message;
                    break;
            }
            if (message.equalsIgnoreCase("reset")) {
                source.sendFeedback(new LiteralText("Reset ").append(new LiteralText(event.id).formatted(Formatting.GOLD)
                                .append(new LiteralText(" Event Message for claim ").formatted(Formatting.YELLOW))
                                .append(new LiteralText(claim.name).formatted(Formatting.GOLD))).formatted(Formatting.YELLOW)
                        , false);
                return -1;
            } else {
                source.sendFeedback(new LiteralText("Set ").append(new LiteralText(event.id).formatted(Formatting.GOLD)
                                .append(new LiteralText(" Event Message for claim ").formatted(Formatting.YELLOW))
                                .append(new LiteralText(claim.name).formatted(Formatting.GOLD)).append(new LiteralText(" to:").formatted(Formatting.YELLOW)))
                                .append(new LiteralText("\n")).append(new LiteralText(ChatColor.translate(message)))
                                .formatted(Formatting.YELLOW)
                        , false);
                return 1;
            }
        }
        return -1;
    }
}
