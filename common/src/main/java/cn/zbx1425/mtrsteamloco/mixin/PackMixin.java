package cn.zbx1425.mtrsteamloco.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;

@Mixin(Pack.class)
public class PackMixin {

    @Inject(method = "readPackInfo", at = @At("RETURN"), cancellable = true)
    private static void readPackInfo(String id, Pack.ResourcesSupplier resources, CallbackInfoReturnable<Pack.Info> cir) {
        Pack.Info originalInfo = cir.getReturnValue();
        if (originalInfo == null) return;

        try (PackResources packResources = resources.open(id)) {
            IoSupplier<InputStream> ioSupplier = packResources.getResource(PackType.CLIENT_RESOURCES,
                    new ResourceLocation("mtr", "mtr_custom_resources.json"));
            if (ioSupplier != null) {
                cir.setReturnValue(new Pack.Info(
                        originalInfo.description(),
                        SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES),
                        originalInfo.requestedFeatures()));
            }
        }
    }
}
