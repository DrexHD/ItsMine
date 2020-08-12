package me.drexhd.itsmine.mixin;

import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.util.MessageUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

@Mixin(LevelStorage.Session.class)
public class LevelStorageSessionMixin {
    @Shadow
    @Final
    private Path directory;

    @Inject(method = "method_27426", at = @At("HEAD"))
    public void saveWorld(DynamicRegistryManager dynamicRegistryManager, SaveProperties saveProperties, CompoundTag compoundTag, CallbackInfo ci) {
        if (ClaimManager.INSTANCE != null) {
            File claimDataFile = new File(directory.toFile(), "claims.dat");
            if (claimDataFile.exists()) {
                File old = new File(directory.toFile(), "claims.dat_old");
                MessageUtil.log("Saving NBT File: " + claimDataFile.getName() + " " + claimDataFile.length() / 1000 + "kb ");
                if (claimDataFile.length() > 45) {
                    MessageUtil.log("Creating backup of NBT File: " + claimDataFile.getName());
                    if (old.exists()) old.delete();
                    claimDataFile.renameTo(old);
                    claimDataFile.delete();
                } else {
                    MessageUtil.LOGGER.warn("Backup aborted!" + claimDataFile.getName() + " may be broken, keeping " + old.getName());
                }
            }
            try {
                claimDataFile.createNewFile();
                CompoundTag tag = ClaimManager.INSTANCE.toNBT();
                NbtIo.writeCompressed(tag, new FileOutputStream(claimDataFile));
            } catch (IOException e) {
                MessageUtil.LOGGER.error("Could not save " + claimDataFile.getName(), e);
            }
        }
    }

}
