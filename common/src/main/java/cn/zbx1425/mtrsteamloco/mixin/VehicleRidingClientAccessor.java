package cn.zbx1425.mtrsteamloco.mixin;

import mtr.data.VehicleRidingClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(VehicleRidingClient.class)
public interface VehicleRidingClientAccessor {

    @Accessor(remap = false)
    List<Double> getOffset();
}
