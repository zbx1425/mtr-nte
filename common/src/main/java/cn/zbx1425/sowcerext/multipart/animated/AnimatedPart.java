package cn.zbx1425.sowcerext.multipart.animated;

import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.PartBase;
import cn.zbx1425.sowcerext.multipart.animated.script.FunctionScript;
import cn.zbx1425.sowcerext.reuse.ModelManager;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;

public class AnimatedPart extends PartBase {

    public RawModel[] rawStates;
    public Vector3f externTranslation = new Vector3f(0, 0, 0);

    public ModelCluster[] uploadedStates;

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

    private final Long id;

    public AnimatedPart() {
        id = AnimatedPartStates.getNewPartId();
    }

    @Override
    public void update(MultipartUpdateProp prop) {
        int lastState = prop.animatedPartStates.partStates.getOrDefault(id, -1);
        Matrix4f lastTransform = prop.animatedPartStates.partTransforms.getOrDefault(id, null);
        Long lastUpdateTime = prop.animatedPartStates.partUpdateTimes.getOrDefault(id, 0L);

        final boolean shouldUpdate = lastTransform == null || refreshRateMillis <= 0
                || (System.currentTimeMillis() - lastUpdateTime) >= refreshRateMillis;
        if (shouldUpdate) {
            double elapsedTime = (System.currentTimeMillis() - lastUpdateTime) / 1000.0;
            int state = (int)stateFunction.update(prop, elapsedTime, lastState);
            float translateX = translateXFunction.update(prop, elapsedTime, lastState);
            float translateY = translateYFunction.update(prop, elapsedTime, lastState);
            float translateZ = translateZFunction.update(prop, elapsedTime, lastState);
            float rotateX = rotateXFunction.update(prop, elapsedTime, lastState);
            float rotateY = rotateYFunction.update(prop, elapsedTime, lastState);
            float rotateZ = rotateZFunction.update(prop, elapsedTime, lastState);

            Matrix4f result = new Matrix4f();

            if (parent != null) result.multiply(parent.getTransform(prop));

            result.translate(
                    -(translateXDirection.x() * translateX + translateYDirection.x() * translateY + translateZDirection.x() * translateZ + externTranslation.x()),
                    translateXDirection.y() * translateX + translateYDirection.y() * translateY + translateZDirection.y() * translateZ + externTranslation.y(),
                    translateXDirection.z() * translateX + translateYDirection.z() * translateY + translateZDirection.z() * translateZ + externTranslation.z()
            );

            result.rotate(rotateXDirection, rotateX);
            result.rotate(rotateYDirection, -rotateY);
            result.rotate(rotateZDirection, -rotateZ);

            prop.animatedPartStates.partStates.put(id, state);
            prop.animatedPartStates.partTransforms.put(id, result);
            prop.animatedPartStates.partUpdateTimes.put(id, System.currentTimeMillis());
        }
    }

    @Override
    public ModelCluster getModel(MultipartUpdateProp prop) {
        int lastState = prop.animatedPartStates.partStates.getOrDefault(id, -1);
        if (lastState < 0 || lastState >= uploadedStates.length) return null;
        return uploadedStates[lastState];
    }

    @Override
    public Matrix4f getTransform(MultipartUpdateProp prop) {
        return prop.animatedPartStates.partTransforms.getOrDefault(id, null);
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
        uploadedStates = new ModelCluster[rawStates.length];
        for (int i = 0; i < rawStates.length; ++i) {
            if (rawStates[i] == null) continue;
            uploadedStates[i] = modelManager.uploadVertArrays(rawStates[i]);
        }
    }

    public void bakeToStaticModel(RawModel staticModelRef, Vector3f translation) {
        externTranslation.add(translation);
        if (rawStates == null || rawStates.length == 0) return;

        MultipartUpdateProp prop = new MultipartUpdateProp();
        this.update(prop);
        int lastState = prop.animatedPartStates.partStates.getOrDefault(id, -1);
        if (lastState < 0 || lastState >= rawStates.length) return;
        RawModel state = rawStates[lastState];
        if (state == null) return;

        RawModel clonedState = state.copy();
        clonedState.sourceLocation = null;
        clonedState.applyMatrix(getTransform(prop));
        staticModelRef.append(clonedState);
    }
}
