package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.data.RailExtraSupplier;
import cn.zbx1425.mtrsteamloco.data.RailModelRegistry;
import mtr.data.MessagePackHelper;
import mtr.data.Rail;
import mtr.data.RailType;
import mtr.data.TransportMode;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.StringUtils;
import org.msgpack.core.MessagePacker;
import org.msgpack.value.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.Map;

@Mixin(value = Rail.class, priority = 1425)
public class RailMixin implements RailExtraSupplier {

    private String modelKey;

    @Override
    public String getModelKey() {
        if (modelKey != null) {
            return modelKey;
        } else {
            if (((Rail)(Object)this).railType == RailType.SIDING) {
                return "nte_builtin_depot";
            } else {
                return "nte_builtin_concrete_sleeper";
            }
        }
    }

    @Inject(method = "<init>(Ljava/util/Map;)V", at = @At("TAIL"))
    private void fromMessagePack(Map<String, Value> map, CallbackInfo ci) {
        MessagePackHelper messagePackHelper = new MessagePackHelper(map);
        modelKey = messagePackHelper.getString("nte_model_key", null);
        if (StringUtils.isEmpty(modelKey)) modelKey = null;
    }

    @Inject(method = "toMessagePack", at = @At("TAIL"))
    private void toMessagePack(MessagePacker messagePacker, CallbackInfo ci) throws IOException {
        messagePacker.packString("nte_model_key").packString(modelKey);
    }

    @Inject(method = "messagePackLength", at = @At("TAIL"), cancellable = true)
    private void messagePackLength(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() + 1);
    }

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("TAIL"))
    private void fromPacket(FriendlyByteBuf packet, CallbackInfo ci) {
        if (packet.readableBytes() <= 4) return;
        if (packet.readInt() != 0x25141425) {
            packet.readerIndex(packet.readerIndex() - 4);
            return;
        }
        modelKey = packet.readUtf();
    }

    @Inject(method = "writePacket", at = @At("TAIL"))
    private void toPacket(FriendlyByteBuf packet, CallbackInfo ci) {
        packet.writeInt(0x25141425);
        packet.writeUtf(modelKey);
    }

    @Redirect(method = "renderSegment", remap = false, at = @At(value = "INVOKE", target = "Ljava/lang/Math;round(D)J"))
    private long redirectRenderSegmentRound(double r) {
        if (ClientConfig.getRailRenderLevel() < 2) return Math.round(r);

        Rail instance = (Rail)(Object)this;
        if (instance.transportMode == TransportMode.TRAIN && instance.railType != RailType.NONE) {
            return Math.round(r / RailModelRegistry.getRepeatInterval(getModelKey()));
        } else {
            return Math.round(r);
        }
    }

}
