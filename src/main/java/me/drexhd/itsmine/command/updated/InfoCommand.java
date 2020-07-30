package me.drexhd.itsmine.command.updated;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.Messages;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.TimeUtil;
import me.drexhd.itsmine.util.WorldUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class InfoCommand extends Command implements Other{


    public InfoCommand(String literal) {
        super(literal);
        this.suggestCurrent = true;
    }

    @Override
    public Command copy() {
        return new InfoCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        literal().then(thenClaim());
        literal().executes(this::execute);
        command.then(literal());
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Claim claim = getClaim(context);
        UUID ownerUUID = claim.claimBlockOwner;
        String ownerName = "";
        if (ownerUUID.equals(ClaimManager.serverUUID)) {
            ownerName = "Server";
        } else {
            GameProfile owner = source.getMinecraftServer().getUserCache().getByUuid(ownerUUID);
            if (owner != null && owner.isComplete()) {
                ownerName = owner.getName();
            }
        }
        BlockPos size = claim.getSize();

        MutableText text = new LiteralText("\n");
        text.append(new LiteralText("Claim Info: " + claim.name).formatted(Formatting.GOLD))
                .append(new LiteralText("\n"))
                .append(newInfoLine("Name", new LiteralText(claim.name).formatted(Formatting.WHITE)))
                .append(newInfoLine("Entities", new LiteralText(String.valueOf(claim.getEntities(source.getWorld()))).formatted(Formatting.AQUA)))
                .append(newInfoLine("Owner", ownerName.equals("") ?
                        new LiteralText(ownerUUID.toString()).formatted(Formatting.RED, Formatting.ITALIC).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, claim.claimBlockOwner.toString()))) :
                        new LiteralText(ownerName).formatted(Formatting.GOLD)))
                .append(newInfoLine("Size", new LiteralText(size.getX() + (claim.is2d() ? "x" : ("x" + size.getY() + "x")) + size.getZ()).formatted(Formatting.GREEN)))
                .append(new LiteralText("").append(new LiteralText("* Flags:").formatted(Formatting.YELLOW))
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
                return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, newInfoLine("Until", new LiteralText(time.toString()).formatted(Formatting.WHITE)).append(newInfoLine("By", new LiteralText(tenant.getName()).formatted(Formatting.WHITE))).append(newInfoLine("Price", new LiteralText(claim.rentManager.getAmount() + " ").append(new TranslatableText(claim.rentManager.getCurrency().getTranslationKey()).append(new LiteralText(" every " + TimeUtil.convertSecondsToString(claim.rentManager.getMin())).formatted(Formatting.WHITE)))))));
            })));
        } else if (claim.rentManager.isRentable() && !claim.rentManager.isRented()) {
            text.append(newInfoLine("Status", new LiteralText("For Rent").formatted(Formatting.GREEN).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, newInfoLine("Price", new LiteralText(claim.rentManager.getAmount() + " "), new TranslatableText(claim.rentManager.getCurrency().getTranslationKey()).formatted(Formatting.WHITE), new LiteralText(" every " + TimeUtil.convertSecondsToString(claim.rentManager.getMin())).formatted(Formatting.WHITE)).append(newInfoLine("Max Rent", new LiteralText(TimeUtil.convertSecondsToString(claim.rentManager.getMax())).formatted(Formatting.WHITE))))))));

        } else {
            text.append(newInfoLine("Status", new LiteralText("Not For Rent").formatted(Formatting.GREEN)));
        }
        source.sendFeedback(text, false);
        return 1;
    }


    private MutableText newPosLine(BlockPos pos, Formatting form1, Formatting form2) {
        return new LiteralText("")
                .append(new LiteralText(String.valueOf(pos.getX())).formatted(form1))
                .append(new LiteralText(" "))
                .append(new LiteralText(String.valueOf(pos.getY())).formatted(form2))
                .append(new LiteralText(" "))
                .append(new LiteralText(String.valueOf(pos.getZ())).formatted(form1));
    }

    private MutableText newInfoLine(String title, Text... text) {
        MutableText message = new LiteralText("").append(new LiteralText("* " + title + ": ").formatted(Formatting.YELLOW));
        for (Text t : text) {
            message.append(t);
        }
        return message.append(new LiteralText("\n"));
    }
}
