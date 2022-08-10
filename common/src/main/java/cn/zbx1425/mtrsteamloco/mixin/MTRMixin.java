package cn.zbx1425.mtrsteamloco.mixin;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(mtr.MTR.class)
public class MTRMixin {

    @Redirect(method = "init", remap = false,
            at = @At(value = "INVOKE", target = "Lmtr/Registry;registerPlayerJoinEvent(Ljava/util/function/Consumer;)V"))
    private static void redirectRegisterPlayerJoinEvent(Consumer<ServerPlayer> consumer) {
        if (mtr.Registry.isFabric()) {
            mtr.Registry.registerPlayerJoinEvent(consumer);
        } else {
            EntityEvent.ADD.register((entity, level) -> {
                if (entity instanceof ServerPlayer) {
                    consumer.accept((ServerPlayer) entity);
                }
                return EventResult.pass();
            });
        }
    }
}
