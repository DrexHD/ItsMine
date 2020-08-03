package me.drexhd.itsmine.command.updated.admin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.claim.permission.Permission;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.config.sections.MessageSection;
import me.drexhd.itsmine.util.ArgumentUtil;
import me.drexhd.itsmine.util.MessageUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.Map;

public class DefaultPermCommand extends Command {

    public DefaultPermCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new DefaultPermCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> reset = LiteralArgumentBuilder.literal("reset");
        LiteralArgumentBuilder<ServerCommandSource> list = LiteralArgumentBuilder.literal("list");
        RequiredArgumentBuilder<ServerCommandSource, Boolean> value = RequiredArgumentBuilder.argument("value", BoolArgumentType.bool());
        RequiredArgumentBuilder<ServerCommandSource, String> permission = ArgumentUtil.getFlags(); //"flag"
        list.executes(this::query);
        literal().executes(this::query);
        reset.executes(this::reset);
        value.executes(this::set);

        permission.then(value);
        permission.then(reset);
        literal().then(list);
        literal().then(permission);
        command.then(literal());
    }

    private int query(CommandContext<ServerCommandSource> context) {
        MutableText text = new LiteralText("Global Default Permission List:\n").formatted(Formatting.GOLD);
        for (Map.Entry<String, Boolean> entry : ClaimManager.INSTANCE.getDefaultPerms().get().entrySet()) {
            text.append(new LiteralText(entry.getKey()).formatted(entry.getValue() ? Formatting.GREEN : Formatting.RED)).append(" ");
        }
        context.getSource().sendFeedback(text, false);
        return 1;
    }

    private int reset(CommandContext<ServerCommandSource> context) {
        String permission = StringArgumentType.getString(context, "flag");
        if (ClaimManager.INSTANCE.getDefaultPerms().isPermissionSet(permission)) {
            if(Permission.isValid(permission)) {
                ClaimManager.INSTANCE.getDefaultPerms().remove(permission);
                MessageUtil.sendMessage(context.getSource(), "&eDefault flag &6" + permission + " &ehas been &6reset");
                return 1;
            } else {
                MessageUtil.sendMessage(context.getSource(), ItsMineConfig.main().message().invalidPermission);
                return 0;
            }
        } else {
            MessageUtil.sendMessage(context.getSource(), "&eDefault flag &6" + permission + " &ewas not set");
            return 0;
        }
    }

    private int set(CommandContext<ServerCommandSource> context) {
        String permission = StringArgumentType.getString(context, "flag");
        boolean value = BoolArgumentType.getBool(context, "value");
        if(Permission.isValid(permission)) {
            ClaimManager.INSTANCE.getDefaultPerms().add(permission, value);
            MessageSection messageSection = ItsMineConfig.main().message();
            String valueString = value ? messageSection.getTrue() : messageSection.getFalse();
            MessageUtil.sendMessage(context.getSource(), "&eDefault flag &6" + permission + " &ehas been set to " + valueString);
            return 1;
        } else {
            MessageUtil.sendMessage(context.getSource(), ItsMineConfig.main().message().invalidPermission);
            return 0;
        }
    }
}
