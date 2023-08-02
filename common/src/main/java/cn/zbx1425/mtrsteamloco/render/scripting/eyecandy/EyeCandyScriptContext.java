package cn.zbx1425.mtrsteamloco.render.scripting.eyecandy;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import cn.zbx1425.sowcer.math.Matrices;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.Scriptable;

import java.util.concurrent.Future;

public class EyeCandyScriptContext {

    public Future<?> scriptStatus;

    public BlockEyeCandy.BlockEntityEyeCandy entity;

    public EyeCandyDrawCalls scriptResult;
    private EyeCandyDrawCalls scriptResultWriting;

    public Scriptable state;

    private boolean created = false;

    public EyeCandyScriptContext(BlockEyeCandy.BlockEntityEyeCandy entity) {
        scriptResult = new EyeCandyDrawCalls();
        scriptResultWriting = new EyeCandyDrawCalls();
        this.entity = entity;
    }

    public void tryCallRender(ScriptHolder jsContext) {
        if (!created) {
            scriptStatus = jsContext.callEyeCandyFunction("createBlock", this);
            created = true;
            return;
        }
        if (scriptStatus == null || scriptStatus.isDone()) {
            scriptStatus = jsContext.callEyeCandyFunction("renderBlock", this);
        }
    }

    public void scriptFinished() {
        synchronized (this) {
            EyeCandyDrawCalls temp = scriptResultWriting;
            scriptResultWriting = scriptResult;
            scriptResult = temp;
            scriptResultWriting.reset();
        }
    }

    public void drawModel(ModelCluster model, Matrices poseStack) {
        scriptResultWriting.addModel(model, poseStack == null ? Matrix4f.IDENTITY : poseStack.last().copy());
    }

    public void print(String str) {
        Main.LOGGER.info("<JS> " + str);
    }

    public void playSound(ResourceLocation sound, float volume, float pitch, float range) {
        scriptResultWriting.addSound(
                SoundEvent.createFixedRangeEvent(sound, range),
                volume, pitch
        );
    }
}
