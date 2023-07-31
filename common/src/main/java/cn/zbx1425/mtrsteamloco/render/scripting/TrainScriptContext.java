package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.sowcer.math.Matrices;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import mtr.data.TrainClient;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.mozilla.javascript.Scriptable;

import java.util.concurrent.Future;

public class TrainScriptContext {

    public Future<?> scriptStatus;

    public TrainClient train;
    public TrainExtra trainExtra;
    protected TrainExtra trainExtraWriting;

    public TrainDrawCalls scriptResult;
    private TrainDrawCalls scriptResultWriting;

    public Scriptable state;

    private boolean created = false;

    public TrainScriptContext(TrainClient train) {
        scriptResult = new TrainDrawCalls(train.trainCars);
        scriptResultWriting = new TrainDrawCalls(train.trainCars);
        trainExtra = new TrainExtra(train);
        trainExtraWriting = new TrainExtra(train);
        this.train = train;
    }

    public void tryCallRender(TrainTypeScriptContext jsContext) {
        if (!created) {
            scriptStatus = jsContext.callTrainFunction("createTrain", this);
            created = true;
            return;
        }
        if (scriptStatus == null || scriptStatus.isDone()) {
            scriptStatus = jsContext.callTrainFunction("renderTrain", this);
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
            TrainExtra temp = trainExtraWriting;
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

    public void print(String str) {
        Main.LOGGER.info("<JS> " + str);
    }

    public void playCarSound(ResourceLocation sound, int carIndex, float x, float y, float z, float volume, float pitch, float range) {
        scriptResultWriting.addCarSound(
                carIndex,
                Util.memoize(SoundEvent::createFixedRangeEvent).apply(sound, range),
                new Vector3f(x, y, z), volume, pitch
        );
    }

    public void playAnnSound(ResourceLocation sound, float volume, float pitch) {
        Minecraft.getInstance().execute(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && train.isPlayerRiding(player)) {
                player.playSound(
                        Util.<ResourceLocation, SoundEvent>memoize(pSound -> SoundEvent.createFixedRangeEvent(pSound, 16)).apply(sound),
                        volume, pitch
                );
            }
        });
    }
}
