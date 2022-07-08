package cn.zbx1425.sowcerext.multipart.loader;

import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.object.VertArray;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.PartBase;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public class AnimatedPart extends PartBase {

    public RawModel[] unbakedStates;
    public Vector3f translationToBake = new Vector3f(0, 0, 0);

    public VertArrays[] bakedStates;

    public AnimatedFormula stateFunction = AnimatedFormula.DEFAULT;

    public Vector3f translateXDirection = new Vector3f(1, 0, 0);
    public Vector3f translateYDirection = new Vector3f(0, 1, 0);
    public Vector3f translateZDirection = new Vector3f(0, 0, 1);
    public AnimatedFormula translateXFunction = AnimatedFormula.DEFAULT;
    public AnimatedFormula translateYFunction = AnimatedFormula.DEFAULT;
    public AnimatedFormula translateZFunction = AnimatedFormula.DEFAULT;

    public Vector3f rotateXDirection = new Vector3f(1, 0, 0);
    public Vector3f rotateYDirection = new Vector3f(0, 1, 0);
    public Vector3f rotateZDirection = new Vector3f(0, 0, 1);
    public AnimatedFormula rotateXFunction = AnimatedFormula.DEFAULT;
    public AnimatedFormula rotateYFunction = AnimatedFormula.DEFAULT;
    public AnimatedFormula rotateZFunction = AnimatedFormula.DEFAULT;

    @Override
    public void update(MultipartUpdateProp prop) {
        stateFunction.update(prop);
        translateXFunction.update(prop);
        translateYFunction.update(prop);
        translateZFunction.update(prop);
        rotateXFunction.update(prop);
        rotateYFunction.update(prop);
        rotateZFunction.update(prop);
    }

    @Override
    public VertArrays getModel() {
        int stateNum = (int)stateFunction.getValue();
        if (stateNum < 0 || stateNum >= bakedStates.length) return null;
        return bakedStates[stateNum];
    }

    @Override
    public Matrix4f getTransform() {
        Matrix4f result = new Matrix4f();
        result.setIdentity();

        float translateX = translateXFunction.getValue(), translateY = translateYFunction.getValue(), translateZ = translateZFunction.getValue();
        result.multiplyWithTranslation(
                translateXDirection.x() * translateX + translateYDirection.x() * translateY + translateZDirection.x() * translateZ,
                translateXDirection.y() * translateX + translateYDirection.y() * translateY + translateZDirection.y() * translateZ,
                translateXDirection.z() * translateX + translateYDirection.z() * translateY + translateZDirection.z() * translateZ
        );

        float rotateX = rotateXFunction.getValue(), rotateY = rotateYFunction.getValue(), rotateZ = rotateZFunction.getValue();
        result.multiply(rotateXDirection.rotation(rotateX));
        result.multiply(rotateYDirection.rotation(rotateY));
        result.multiply(rotateZDirection.rotation(rotateZ));

        return result;
    }

    @Override
    public boolean isStatic() {
        return translateXFunction.isStatic() && translateYFunction.isStatic() && translateZFunction.isStatic()
                && rotateXFunction.isStatic() && rotateYFunction.isStatic() && rotateZFunction.isStatic();
    }

    public void bake(VertAttrMapping mapping, Vector3f translation) {
        bakedStates = new VertArrays[unbakedStates.length];
        for (int i = 0; i < unbakedStates.length; ++i) {
            RawModel state = unbakedStates[i];
            state.applyTranslation(translation.x(), translation.y(), translation.z());
            state.applyTranslation(translationToBake.x(), translationToBake.y(), translationToBake.z());
            bakedStates[i] = VertArrays.createAll(state.upload(mapping), mapping, null);
        }
        unbakedStates = null;
    }

    public void bakeToStaticModel(RawModel staticModelRef, Vector3f translation) {
        int stateNum = (int)stateFunction.getValue();
        if (stateNum < 0 || stateNum >= unbakedStates.length) return;
        RawModel state = unbakedStates[stateNum];
        state.applyTranslation(translation.x(), translation.y(), translation.z());
        state.applyTranslation(translationToBake.x(), translationToBake.y(), translationToBake.z());
        state.applyMatrix(getTransform());
        staticModelRef.append(state);
    }
}
