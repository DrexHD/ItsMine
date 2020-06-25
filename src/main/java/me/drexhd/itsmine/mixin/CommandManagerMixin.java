package me.drexhd.itsmine.mixin;

import com.mojang.brigadier.CommandDispatcher;
import me.drexhd.itsmine.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Indigo Amann
 */
@Mixin(net.minecraft.server.command.CommandManager.class)
public class CommandManagerMixin {
    @Shadow
    private CommandDispatcher<ServerCommandSource> dispatcher;
    @Inject(method = "<init>", at = @At("RETURN"))
    public void addCommand(net.minecraft.server.command.CommandManager.RegistrationEnvironment environment, CallbackInfo ci) {
        CommandManager.dispatcher = this.dispatcher;
        CommandManager.register();
    }
}
