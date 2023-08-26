package cn.zbx1425.mtrsteamloco.render.scripting.train;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import cn.zbx1425.sowcer.math.Matrices;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import mtr.data.TrainClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.*;

import java.util.concurrent.Future;

@SuppressWarnings("unused")
public class TrainScriptContext {

    public Future<?> scriptStatus;

    public TrainClient train;
    public TrainWrapper trainExtra;
    protected TrainWrapper trainExtraWriting;

    public TrainDrawCalls scriptResult;
    private TrainDrawCalls scriptResultWriting;

    public Scriptable state;

    private boolean created = false;

    public TrainScriptContext(TrainClient train) {
        this.scriptResult = new TrainDrawCalls(train.trainCars);
        this.scriptResultWriting = new TrainDrawCalls(train.trainCars);
        this.train = train;
        this.trainExtra = new TrainWrapper(train);
        this.trainExtraWriting = new TrainWrapper(train);
    }

    public void tryCallRender(ScriptHolder jsContext) {
        if (!created) {
            trainExtra = new TrainWrapper(train);
            trainExtraWriting = new TrainWrapper(train);
            scriptStatus = jsContext.callTrainFunction("createTrain", this);
            created = true;
            return;
        }
        if (scriptStatus == null || scriptStatus.isDone()) {
            scriptStatus = jsContext.callTrainFunction("renderTrain", this);
        }
    }

    public void tryCallDispose(ScriptHolder jsContext) {
        if (created) {
            jsContext.callTrainFunction("disposeTrain", this);
            created = false;
        }
    }

    public void scriptFinished() {
        synchronized (this) {
            TrainDrawCalls temp = scriptResultWriting;
            scriptResultWriting = scriptResult;
            scriptResult = temp;
            scriptResultWriting.reset();
        }
    }

    public void extraFinished() {
        synchronized (this) {
            TrainWrapper temp = trainExtraWriting;
            trainExtraWriting = trainExtra;
            trainExtra = temp;
            trainExtraWriting.reset();
        }
    }

    public void drawCarModel(ModelCluster model, int carIndex, Matrices poseStack) {
        scriptResultWriting.addCarModel(carIndex, model, poseStack == null ? Matrix4f.IDENTITY : poseStack.last().copy());
    }

    public void drawConnModel(ModelCluster model, int carIndex, Matrices poseStack) {
        scriptResultWriting.addConnModel(carIndex, model, poseStack == null ? Matrix4f.IDENTITY : poseStack.last().copy());
    }

    public void drawConnStretchTexture(ResourceLocation location, int carIndex) {
        scriptResultWriting.drawConnStretchTexture(carIndex, location);
    }

    public void print(String str) {
        Main.LOGGER.info("<JS> " + str);
    }

    public void playCarSound(ResourceLocation sound, int carIndex, float x, float y, float z, float volume, float pitch) {
        scriptResultWriting.addCarSound(
                carIndex,
#if MC_VERSION >= "11903"
                SoundEvent.createVariableRangeEvent(sound),
#else
                new SoundEvent(sound),
#endif
                new Vector3f(x, y, z), volume, pitch
        );
    }

    public void playAnnSound(ResourceLocation sound, float volume, float pitch) {
        Minecraft.getInstance().execute(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && train.isPlayerRiding(player)) {
                player.playSound(
#if MC_VERSION >= "11903"
                        SoundEvent.createVariableRangeEvent(sound),
#else
                        new SoundEvent(sound),
#endif
                        volume, pitch
                );
            }
        });
    }
}
