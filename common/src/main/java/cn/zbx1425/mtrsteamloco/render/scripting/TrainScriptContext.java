package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.sowcer.math.Matrices;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import mtr.data.TrainClient;
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
            scriptStatus = jsContext.callCreateTrain(this);
            created = true;
            return;
        }
        if (scriptStatus == null || scriptStatus.isDone()) {
            scriptStatus = jsContext.callRenderTrain(this);
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

    public void drawCarModel(int carIndex, ModelCluster model, Matrices poseStack) {
        scriptResultWriting.enqueueCar(carIndex, model, poseStack == null ? Matrix4f.IDENTITY : poseStack.last());
    }

    public void drawConnectionModel(int carIndex, ModelCluster model, Matrices poseStack) {
        scriptResultWriting.enqueueConnection(carIndex, model, poseStack == null ? Matrix4f.IDENTITY : poseStack.last());
    }

    public void print(String str) {
        Main.LOGGER.info("<JS> " + str);
    }
}
