package cn.zbx1425.sowcerext.multipart.animated;

import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.PartBase;
import cn.zbx1425.sowcerext.multipart.animated.script.FunctionScript;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public class AnimatedPart extends PartBase {

    public RawModel[] unbakedStates;
    public Vector3f externTranslation = new Vector3f(0, 0, 0);

    public VertArrays[] bakedStates;

    public int refreshRateMillis = 0;

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
            stateFunction.update(prop, elapsedTime, lastState);
            translateXFunction.update(prop, elapsedTime, lastState);
            translateYFunction.update(prop, elapsedTime, lastState);
            translateZFunction.update(prop, elapsedTime, lastState);
            rotateXFunction.update(prop, elapsedTime, lastState);
            rotateYFunction.update(prop, elapsedTime, lastState);
            rotateZFunction.update(prop, elapsedTime, lastState);

            Matrix4f result = new Matrix4f();
            result.setIdentity();

            float rotateX = rotateXFunction.getValue(), rotateY = rotateYFunction.getValue(), rotateZ = rotateZFunction.getValue();
            result.multiply(rotateXDirection.rotation(rotateX));
            result.multiply(rotateYDirection.rotation(-rotateY));
            result.multiply(rotateZDirection.rotation(rotateZ));

            float translateX = translateXFunction.getValue(), translateY = translateYFunction.getValue(), translateZ = translateZFunction.getValue();
            result.translate(new Vector3f(
                    -(translateXDirection.x() * translateX + translateYDirection.x() * translateY + translateZDirection.x() * translateZ + externTranslation.x()),
                    translateXDirection.y() * translateX + translateYDirection.y() * translateY + translateZDirection.y() * translateZ + externTranslation.y(),
                    translateXDirection.z() * translateX + translateYDirection.z() * translateY + translateZDirection.z() * translateZ + externTranslation.z()
            ));

            lastState = (int)stateFunction.getValue();
            lastTransform = result;
        }
    }

    @Override
    public VertArrays getModel() {
        if (lastState < 0 || lastState >= bakedStates.length) return null;
        return bakedStates[lastState];
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

    @Override
    public PartBase clone() {
        AnimatedPart result = new AnimatedPart();
        result.bakedStates = this.bakedStates;
        result.refreshRateMillis = this.refreshRateMillis;
        result.stateFunction = this.stateFunction;
        result.translateXDirection = this.translateXDirection;
        result.translateYDirection = this.translateYDirection;
        result.translateZDirection = this.translateZDirection;
        result.translateXFunction = this.translateXFunction;
        result.translateYFunction = this.translateYFunction;
        result.translateZFunction = this.translateZFunction;
        result.rotateXDirection = this.rotateXDirection;
        result.rotateYDirection = this.rotateYDirection;
        result.rotateZDirection = this.rotateZDirection;
        result.rotateXFunction = this.rotateXFunction;
        result.rotateYFunction = this.rotateYFunction;
        result.rotateZFunction = this.rotateZFunction;
        return result;
    }

    public void bake(VertAttrMapping mapping, Vector3f translation) {
        bakedStates = new VertArrays[unbakedStates.length];
        externTranslation.add(translation);
        for (int i = 0; i < unbakedStates.length; ++i) {
            RawModel state = unbakedStates[i];
            bakedStates[i] = VertArrays.createAll(state.upload(mapping), mapping, null);
        }
        unbakedStates = null;
    }

    public void bakeToStaticModel(RawModel staticModelRef, Vector3f translation) {
        int stateNum = (int)stateFunction.getValue();
        if (stateNum < 0 || stateNum >= unbakedStates.length) return;
        RawModel state = unbakedStates[stateNum];
        externTranslation.add(translation);
        this.update(new MultipartUpdateProp());
        state.applyMatrix(getTransform());
        staticModelRef.append(state);
    }
}
