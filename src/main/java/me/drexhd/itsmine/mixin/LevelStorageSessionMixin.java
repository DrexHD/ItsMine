package me.drexhd.itsmine.mixin;

import me.drexhd.itsmine.ClaimManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.nio.file.Path;

@Mixin(LevelStorage.Session.class)
public class LevelStorageSessionMixin {
    @Shadow
    @Final
    private Path directory;

    @Inject(method = "method_27426", at = @At("HEAD"))
    public void saveWorld(RegistryTracker registryTracker, SaveProperties saveProperties, CompoundTag compoundTag, CallbackInfo ci) {
        if (ClaimManager.INSTANCE != null) {
            File claimDataFile = new File(directory.toFile(), "claims.dat");
            if (claimDataFile.exists()) {
                File old = new File(directory.toFile(), "claims.dat_old");
                System.out.println("Saving NBT File: " + claimDataFile.getName() + " " + claimDataFile.length()+ "b " + claimDataFile.getAbsolutePath());
                if(claimDataFile.length() > 45){
                    System.out.println("Creating backup of NBT File: " + claimDataFile.getName());
                    if (old.exists()) old.delete();
                    claimDataFile.renameTo(old);
                    claimDataFile.delete();
                } else {
                    System.out.println("Aborting backup!" + claimDataFile.getName() + " may be broken, keeping " + old.getName());
                }
            }
            try {
                claimDataFile.createNewFile();
                CompoundTag tag = ClaimManager.INSTANCE.toNBT();
                NbtIo.writeCompressed(tag, new FileOutputStream(claimDataFile));
            } catch (IOException e) {
                System.out.println("Could not save " + claimDataFile.getName() + ":");
                e.printStackTrace();
            }
        }
    }

}
