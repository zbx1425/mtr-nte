package cn.zbx1425.mtrsteamloco.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
#if MC_VERSION >= "11903"
import net.minecraft.server.packs.resources.IoSupplier;
#endif
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.util.function.Supplier;

@Mixin(Pack.class)
public class PackMixin {

#if MC_VERSION >= "11903"
    @Shadow @Final private Pack.ResourcesSupplier resources;
#else
    @Shadow @Final private Supplier<PackResources> supplier;
#endif
    @Shadow @Final private String id;
    @Unique private Boolean isMTRPack = null;

    @Inject(method = "getCompatibility", at = @At("HEAD"), cancellable = true)
    private void getCompatibility(CallbackInfoReturnable<PackCompatibility> cir) {
        if (isMTRPack == null) {
#if MC_VERSION >= "11903"
            try (PackResources packResources = resources.open(id)) {
                IoSupplier<InputStream> ioSupplier;
#else
            try (PackResources packResources = supplier.get()) {
                InputStream ioSupplier;
#endif
                try {
                    ioSupplier = packResources.getResource(PackType.CLIENT_RESOURCES,
                            new ResourceLocation("mtr", "mtr_custom_resources.json"));
                    isMTRPack = (ioSupplier != null);
                } catch (Exception ignored) {
                    isMTRPack = false;
                }
            }
        }
        if (isMTRPack) cir.setReturnValue(PackCompatibility.COMPATIBLE);
    }
}
