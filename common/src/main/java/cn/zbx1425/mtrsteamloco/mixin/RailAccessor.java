package cn.zbx1425.mtrsteamloco.mixin;

import mtr.data.Rail;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Rail.class, remap = false)
public interface RailAccessor {
    @Accessor("r1") @Final
    double getR1();

    @Accessor("r2") @Final
    double getR2();

    @Accessor @Final
    boolean getIsStraight1();

    @Accessor @Final
    boolean getIsStraight2();

    @Invoker
    double invokeGetPositionY(double value);

}
