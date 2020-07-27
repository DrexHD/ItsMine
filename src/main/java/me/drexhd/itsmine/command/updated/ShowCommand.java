package me.drexhd.itsmine.command.updated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.ClaimShower;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.ClaimUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static me.drexhd.itsmine.util.ShowerUtil.silentHideShow;

public class ShowCommand extends Command implements Other{

    private final boolean show;

    public ShowCommand(String literal, boolean show) {
        super(literal);
        this.show = show;
        this.suggestCurrent = true;
    }

    @Override
    public Command copy() {
        return new ShowCommand(literal, show);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        literal().then(thenClaim());
        literal().executes(this::execute);
        command.then(literal());
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Claim claim = getClaim(context);
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (!show && ((ClaimShower) player).getShownClaim() != null) claim = ((ClaimShower) player).getShownClaim();
            if (!claim.dimension.equals(source.getWorld().getDimension())) {
                if (claim == ((ClaimShower) player).getShownClaim())
                    ((ClaimShower) player).setShownClaim(null); // just so we dont have extra packets on this
                source.sendFeedback(new LiteralText("That claim is not in this dimension").formatted(Formatting.RED), false);
                return 0;
            }
            source.sendFeedback(new LiteralText((show ? "Showing" : "Hiding") + " claim: " + claim.name).formatted(Formatting.GREEN), false);
            if (claim.isChild) silentHideShow(player, ClaimUtil.getParentClaim(claim), !show, true, "outline");
            else silentHideShow(player, claim, !show, true, "outline");
        return 0;
    }
}
