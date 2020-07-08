package me.drexhd.itsmine.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.Messages;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.util.ClaimUtil;
import me.drexhd.itsmine.util.TimeUtil;
import me.drexhd.itsmine.util.WorldUtil;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class InfoCommand {
    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, RequiredArgumentBuilder<ServerCommandSource, String> claim) {
        LiteralArgumentBuilder<ServerCommandSource> info = literal("info");
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> claimOwner = argument("claimOwner", GameProfileArgumentType.gameProfile())/*.suggests(PLAYERS_PROVIDER)*/;
        info.executes(context -> info(context, ""));
        claim.executes(context -> info(context, getString(context, "claim")));
        claimOwner.then(claim);
        info.then(claimOwner);
        info.then(claim);
        command.then(info);
    }

    private static int info(CommandContext<ServerCommandSource> context, String claimName) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Claim claim;
        if (claimName.equals("")) claim = ClaimManager.INSTANCE.getClaimAt(new BlockPos(source.getPosition()), source.getWorld().getDimension());
        else claim = ClaimManager.INSTANCE.getClaim(context, claimName);
        ClaimUtil.validateClaim(claim);
        GameProfile owner = claim.claimBlockOwner == null ? null : source.getMinecraftServer().getUserCache().getByUuid(claim.claimBlockOwner);
        BlockPos size = claim.getSize();

        MutableText text = new LiteralText("\n");
        text.append(new LiteralText("Claim Info: " + claim.name).formatted(Formatting.GOLD)).append(new LiteralText("\n"));
        text.append(newInfoLine("Name", new LiteralText(claim.name).formatted(Formatting.WHITE)));
        text.append(newInfoLine("Entities", new LiteralText(String.valueOf(claim.getEntities(source.getWorld()))).formatted(Formatting.AQUA)));
        text.append(newInfoLine("Owner",
                owner != null && claim.customOwnerName == null ? new LiteralText(owner.getName()).formatted(Formatting.GOLD) :
                        claim.customOwnerName != null ? new LiteralText(claim.customOwnerName).formatted(Formatting.GOLD) :
                                new LiteralText(claim.claimBlockOwner == null ? "No Owner" : claim.claimBlockOwner.toString()).formatted(Formatting.RED).formatted(Formatting.ITALIC)));
        text.append(newInfoLine("Size", new LiteralText(size.getX() + (claim.is2d() ? "x" : ("x" + size.getY() + "x")) + size.getZ()).formatted(Formatting.GREEN)));


        text.append(new LiteralText("").append(new LiteralText("* Flags:").formatted(Formatting.YELLOW))
                .append(Messages.Command.getFlags(claim)).append(new LiteralText("\n")));
        MutableText pos = new LiteralText("");
        Text min = newPosLine(claim.min, Formatting.AQUA, Formatting.DARK_AQUA);
        Text max = newPosLine(claim.max, Formatting.LIGHT_PURPLE, Formatting.DARK_PURPLE);


        pos.append(newInfoLine("Position", new LiteralText("")
                .append(new LiteralText("Min ").formatted(Formatting.WHITE).append(min))
                .append(new LiteralText(" "))
                .append(new LiteralText("Max ").formatted(Formatting.WHITE).append(max))));
        text.append(pos);
        text.append(newInfoLine("Dimension", new LiteralText(WorldUtil.getDimensionName(claim.dimension))));
        if (claim.rentManager.isRented()) {
            GameProfile tenant = claim.rentManager.getTenant() == null ? null : source.getMinecraftServer().getUserCache().getByUuid(claim.rentManager.getTenant());
            text.append(newInfoLine("Status", new LiteralText("Rented").formatted(Formatting.RED).styled(style -> {
                java.util.Date time = new java.util.Date((long) claim.rentManager.getUntil() * 1000);
                return style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, newInfoLine("Until", new LiteralText(time.toString()).formatted(Formatting.WHITE)).append(newInfoLine("By", new LiteralText(tenant.getName()).formatted(Formatting.WHITE))).append(newInfoLine("Price", new LiteralText(claim.rentManager.getAmount() + " ").append(new TranslatableText(claim.rentManager.getCurrency().getTranslationKey()).append(new LiteralText(" every " + TimeUtil.convertSecondsToString(claim.rentManager.getMin())).formatted(Formatting.WHITE)))))));
            })));
        } else if (claim.rentManager.isRentable() && !claim.rentManager.isRented()) {
            text.append(newInfoLine("Status", new LiteralText("For Rent").formatted(Formatting.GREEN).styled(style -> style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, newInfoLine("Price", new LiteralText(claim.rentManager.getAmount() + " "), new TranslatableText(claim.rentManager.getCurrency().getTranslationKey()).formatted(Formatting.WHITE), new LiteralText(" every " + TimeUtil.convertSecondsToString(claim.rentManager.getMin())).formatted(Formatting.WHITE)).append(newInfoLine("Max Rent", new LiteralText(TimeUtil.convertSecondsToString(claim.rentManager.getMax())).formatted(Formatting.WHITE))))))));

        } else {
            text.append(newInfoLine("Status", new LiteralText("Not For Rent").formatted(Formatting.GREEN)));
        }
        source.sendFeedback(text, false);
        return 1;
    }

    private static MutableText newPosLine(BlockPos pos, Formatting form1, Formatting form2) {
        return new LiteralText("")
                .append(new LiteralText(String.valueOf(pos.getX())).formatted(form1))
                .append(new LiteralText(" "))
                .append(new LiteralText(String.valueOf(pos.getY())).formatted(form2))
                .append(new LiteralText(" "))
                .append(new LiteralText(String.valueOf(pos.getZ())).formatted(form1));
    }

    private static MutableText newInfoLine(String title, Text... text) {
        MutableText message = new LiteralText("").append(new LiteralText("* " + title + ": ").formatted(Formatting.YELLOW));
        for (Text t : text) {
            message.append(t);
        }
        return message.append(new LiteralText("\n"));
    }
}
