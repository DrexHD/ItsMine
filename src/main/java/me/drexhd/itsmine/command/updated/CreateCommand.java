package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ClaimShower;
import me.drexhd.itsmine.ItsMine;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.ClaimUtil;
import me.drexhd.itsmine.util.PermissionUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static me.drexhd.itsmine.util.ShowerUtil.silentHideShow;
import static net.minecraft.server.command.CommandManager.argument;

public class CreateCommand extends Command implements Admin, Subzone {

    public CreateCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new CreateCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        RequiredArgumentBuilder<ServerCommandSource, String> name = argument("name", word());
        name.executes(this::createClaim);
        literal().then(name);
        command.then(literal());
    }

    public int createClaim(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        Pair<BlockPos, BlockPos> selectedPositions = ClaimManager.INSTANCE.stickPositions.get(player);
        if (selectedPositions == null) {
            throw new SimpleCommandExceptionType(new LiteralText("You need to select block positions with a stick first.")).create();
        } else if (selectedPositions.getLeft() == null) {
            throw new SimpleCommandExceptionType(new LiteralText("You need to select a second block positions (Right Click) with a stick first.")).create();
        } else if (selectedPositions.getRight() == null) {
            throw new SimpleCommandExceptionType(new LiteralText("You need to select a second block positions (Left Click) with a stick first.")).create();
        }
        BlockPos posA = selectedPositions.getLeft();
        BlockPos posB = selectedPositions.getRight();
        String name = getString(context, "name");
        if (name.length() > 30) {
            throw new SimpleCommandExceptionType(new LiteralText("Invalid claim name (too long)!")).create();
        }
        if (!name.matches("[A-Za-z0-9]+")) {
            throw new SimpleCommandExceptionType(new LiteralText("Invalid claim name (invalid characters)!")).create();
        }
        int x, y = 0, z, mx, my = 255, mz;

        x = Math.min(posA.getX(), posB.getX());
        mx = Math.max(posA.getX(), posB.getX());
        if (!ItsMineConfig.main().claims2d) {
            y = Math.min(posA.getY(), posB.getY());
            my = Math.max(posA.getY(), posB.getY());
        }
        z = Math.min(posA.getZ(), posB.getZ());
        mz = Math.max(posA.getZ(), posB.getZ());

        BlockPos min = new BlockPos(x, y, z);
        BlockPos max = new BlockPos(mx, my, mz);
        BlockPos sub = max.subtract(min);
        sub = sub.add(1, ItsMineConfig.main().claims2d ? 0 : 1, 1);
        int subInt = sub.getX() * (ItsMineConfig.main().claims2d ? 1 : sub.getY()) * sub.getZ();

        UUID uuid = admin ? ClaimManager.serverUUID : source.getPlayer().getUuid();

        Claim claim = new Claim(name, uuid, min, max, source.getWorld().getDimension(), source.getPlayer().getBlockPos(), subzones);

        if ((ClaimManager.INSTANCE.getClaim(uuid, name) == null)) {
            if (!subzones) {
                if (!ClaimManager.INSTANCE.wouldIntersect(claim)) {
                    if ((admin && ItsMine.permissions().hasPermission(source, PermissionUtil.Command.INFINITE_BLOCKS, 2)) || ClaimManager.INSTANCE.useClaimBlocks(uuid, subInt)) {
                        ClaimManager.INSTANCE.addClaim(claim);
                        MutableText message = new LiteralText("")
                                .append(new LiteralText("Your claim was created").formatted(Formatting.GREEN))
                                .append(new LiteralText("(Area: " + sub.getX() + "x" + sub.getY() + "x" + sub.getZ() + ")")
                                        .styled(style -> style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(subInt + " blocks").formatted(Formatting.YELLOW)))));
                        source.sendFeedback(message, false);
                        ClaimUtil.blocksLeft(source);
                        showClaim(source, claim, false, null);
                        if (admin)
                            source.getMinecraftServer().sendSystemMessage(new LiteralText(player.getGameProfile().getName() + " Has created a new claim (" + claim.name + ") using the admin command."), source.getPlayer().getUuid());
                        return 1;
                    } else {
                        throw new SimpleCommandExceptionType(new LiteralText("You don't have enough claim blocks. You have " + ClaimManager.INSTANCE.getClaimBlocks(uuid) + ", you need " + subInt + "(" + (subInt - ClaimManager.INSTANCE.getClaimBlocks(uuid)) + " more)").formatted(Formatting.RED)).create();
                    }
                } else {
                    throw new SimpleCommandExceptionType(new LiteralText("Claim would overlap with another claim!")).create();
                }
            } else {
                Claim parent = getClaimAt(context);
                validatePermission(parent, uuid, "modify", "subzone");
                if (claim.dimension == parent.dimension && parent.includesPosition(claim.min) && parent.includesPosition(claim.max) && !parent.isChild) {
                    if (!ClaimManager.INSTANCE.wouldSubzoneIntersect((claim))) {
                        parent.addSubzone(claim);
                        ClaimManager.INSTANCE.addClaim(claim);
                        showClaim(source, parent, false, null);
                        source.sendFeedback(new LiteralText("").append(new LiteralText("Your subzone was created.").formatted(Formatting.GREEN)), false);
                    } else {
                        player.sendSystemMessage(new LiteralText("Your subzone would overlap with another subzone").formatted(Formatting.RED), player.getUuid());
                    }
                    ClaimManager.INSTANCE.stickPositions.remove(player);
                    return 1;
                } else {
                    player.sendSystemMessage(new LiteralText("Subzone must be inside and in the same dimension as the original claim").formatted(Formatting.RED), player.getUuid());
                }

                return 1;
            }

        } else {
            throw new SimpleCommandExceptionType(new LiteralText("You already have a claim with that name!")).create();
        }
    }

    public int showClaim(ServerCommandSource source, Claim claim, boolean reset, String mode) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        if (!reset && ((ClaimShower)player).getShownClaim() != null && !(!ItsMineConfig.main().claims2d &&((ClaimShower)player).getShownClaim() != claim)) showClaim(source, ((ClaimShower)player).getShownClaim(), true, ((ClaimShower)player).getMode());
        if (reset && ((ClaimShower)player).getShownClaim() != null) claim = ((ClaimShower)player).getShownClaim();
        if (claim != null) {
            if (!claim.dimension.equals(source.getWorld().getDimension())) {
                if (claim == ((ClaimShower)player).getShownClaim()) ((ClaimShower)player).setShownClaim(null); // just so we dont have extra packets on this
                source.sendFeedback(new LiteralText("That claim is not in this dimension").formatted(Formatting.RED), false);
                return 0;
            }
            source.sendFeedback(new LiteralText((!reset ? "Showing" : "Hiding") + " claim: " + claim.name).formatted(Formatting.GREEN), false);
            if(claim.isChild) silentHideShow(player, ClaimUtil.getParentClaim(claim), reset, true, mode);
            else silentHideShow(player, claim, reset, true, mode);

        } else {
            source.sendFeedback(new LiteralText("That is not a valid claim").formatted(Formatting.RED), false);
        }
        return 0;
    }
}
