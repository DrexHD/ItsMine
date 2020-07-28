package me.drexhd.itsmine.command.updated.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.command.Command;
import me.drexhd.itsmine.util.MessageUtil;
import net.minecraft.server.command.ServerCommandSource;

public class ModInfoCommand extends Command {

    public ModInfoCommand(String literal) {
        super(literal);
    }

    @Override
    public Command copy() {
        return new ModInfoCommand(literal);
    }

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        literal().executes(this::execute);
        command.then(literal());
    }

    public int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        int subzones = 0;
        int claims = 0;
        int totalArea = 0;
        for (Claim claim : ClaimManager.INSTANCE.getClaimList()) {
            if (claim.isChild) {
                subzones++;
            } else {
                totalArea += claim.getArea();
                claims++;
            }
        }
        MessageUtil.sendMessage(source, "&6ItsMine" + "\n" + "&e* Loaded Claims: &b" + claims + "\n" + "&e* Loaded Subzones: &3" + subzones + "\n" + "&e* Total Area: &b" + totalArea + " (Ø" + totalArea / claims + ")" + "\n" + "&e* DataVersion: &6" + ClaimManager.INSTANCE.getDataVersion());
        return 1;
    }
}
