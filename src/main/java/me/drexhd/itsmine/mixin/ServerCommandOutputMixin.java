package me.drexhd.itsmine.mixin;

import me.drexhd.itsmine.ClaimManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.rcon.RconCommandOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RconCommandOutput.class)
public abstract class ServerCommandOutputMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void saveInstance(MinecraftServer server, CallbackInfo ci) {
        ClaimManager.INSTANCE = new ClaimManager();
        ClaimManager.INSTANCE.server = server;
    }
}