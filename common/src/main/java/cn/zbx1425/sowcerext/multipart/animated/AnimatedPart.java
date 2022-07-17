package cn.zbx1425.sowcerext.multipart.animated;

import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.PartBase;
import cn.zbx1425.sowcerext.multipart.animated.script.FunctionScript;
import cn.zbx1425.sowcerext.reuse.ModelManager;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public class AnimatedPart extends PartBase {

    public RawModel[] rawStates;
    public Vector3f externTranslation = new Vector3f(0, 0, 0);

    public VertArrays[] uploadedStates;

    public int refreshRateMillis = 0;
    public boolean billboard = false;

    public FunctionScript stateFunction = FunctionScript.DEFAULT;

    public Vector3f translateXDirection = new Vector3f(1, 0, 0);
    public Vector3f translateYDirection = new Vector3f(0, 1, 0);
    public Vector3f translateZDirection = new Vector3f(0, 0, 1);
    public FunctionScript translateXFunction = FunctionScript.DEFAULT;
    public FunctionScript translateYFunction = FunctionScript.DEFAULT;
    public FunctionScript translateZFunction = FunctionScript.DEFAULT;

    public Vector3f rotateXDirection = new Vector3f(1, 0, 0);
    public Vector3f rotateYDirection = new Vector3f(0, 1, 0);
    public Vector3f rotateZDirection = new Vector3f(0, 0, 1);
    public FunctionScript rotateXFunction = FunctionScript.DEFAULT;
    public FunctionScript rotateYFunction = FunctionScript.DEFAULT;
    public FunctionScript rotateZFunction = FunctionScript.DEFAULT;

    private Matrix4f lastTransform = null;
    private int lastState = 0;
    private long lastUpdateTime = 0;

    @Override
    public void update(MultipartUpdateProp prop) {
        final boolean shouldUpdate = lastTransform == null || refreshRateMillis <= 0
                || (System.currentTimeMillis() - lastUpdateTime) >= refreshRateMillis;
        if (shouldUpdate) {
            double elapsedTime = (System.currentTimeMillis() - lastUpdateTime) / 1000.0;
            lastUpdateTime = System.currentTimeMillis();
            int state = (int)stateFunction.update(prop, elapsedTime, lastState);
            float translateX = translateXFunction.update(prop, elapsedTime, lastState);
            float translateY = translateYFunction.update(prop, elapsedTime, lastState);
            float translateZ = translateZFunction.update(prop, elapsedTime, lastState);
            float rotateX = rotateXFunction.update(prop, elapsedTime, lastState);
            float rotateY = rotateYFunction.update(prop, elapsedTime, lastState);
            float rotateZ = rotateZFunction.update(prop, elapsedTime, lastState);

            Matrix4f result = new Matrix4f();
            result.setIdentity();

            if (parent != null) result.multiply(parent.getTransform());

            result.multiply(rotateXDirection.rotation(rotateX));
            result.multiply(rotateYDirection.rotation(-rotateY));
            result.multiply(rotateZDirection.rotation(-rotateZ));

            result.translate(new Vector3f(
                    -(translateXDirection.x() * translateX + translateYDirection.x() * translateY + translateZDirection.x() * translateZ + externTranslation.x()),
                    translateXDirection.y() * translateX + translateYDirection.y() * translateY + translateZDirection.y() * translateZ + externTranslation.y(),
                    translateXDirection.z() * translateX + translateYDirection.z() * translateY + translateZDirection.z() * translateZ + externTranslation.z()
            ));

            lastState = state;
            lastTransform = result;
        }
    }

    @Override
    public VertArrays getModel() {
        if (lastState < 0 || lastState >= uploadedStates.length) return null;
        return uploadedStates[lastState];
    }

    @Override
    public Matrix4f getTransform() {
        return lastTransform;
    }

    @Override
    public boolean isStatic() {
        return stateFunction.isStatic()
                && translateXFunction.isStatic() && translateYFunction.isStatic() && translateZFunction.isStatic()
                && rotateXFunction.isStatic() && rotateYFunction.isStatic() && rotateZFunction.isStatic();
    }

    public void uploadStates(ModelManager modelManager, Vector3f translation) {
        externTranslation.add(translation);
        if (rawStates == null || rawStates.length == 0) return;
        uploadedStates = new VertArrays[rawStates.length];
        for (int i = 0; i < rawStates.length; ++i) {
            uploadedStates[i] = modelManager.uploadVertArrays(rawStates[i]);
        }
        rawStates = null;
    }

    public void bakeToStaticModel(RawModel staticModelRef, Vector3f translation) {
        externTranslation.add(translation);
        if (rawStates == null || rawStates.length == 0) return;
        this.update(MultipartUpdateProp.INSTANCE);
        if (lastState < 0 || lastState >= rawStates.length) return;
        RawModel state = rawStates[lastState];
        if (state == null) return;

        RawModel clonedState = state.copy();
        clonedState.sourceLocation = null;
        clonedState.applyMatrix(getTransform());
        staticModelRef.append(clonedState);
        rawStates = null;
    }
}
