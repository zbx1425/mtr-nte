package cn.zbx1425.sowcerext.multipart.mi;

import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.PartBase;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import java.util.*;

public class MiPart extends PartBase {

    private final VertArrays model;

    public String name;

    public Vector3f internalOffset = new Vector3f(0, 0, 0);
    public Vector3f externalOffset = new Vector3f(0, 0, 0);

    public FloatSpline translateX = new FloatSpline();
    public FloatSpline translateY = new FloatSpline();
    public FloatSpline translateZ = new FloatSpline();
    public FloatSpline rotateX = new FloatSpline();
    public FloatSpline rotateY = new FloatSpline();
    public FloatSpline rotateZ = new FloatSpline();

    private Matrix4f lastTransform = null;

    private boolean isVisible = true;

    public MiPart(VertArrays model) {
        this.model = model;
    }

    @Override
    public void update(MultipartUpdateProp prop) {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        float time = prop.miKeyframeTime;

        result.multiplyWithTranslation(externalOffset.x(), externalOffset.y(), externalOffset.z());

        result.multiplyWithTranslation(translateX.getValue(time), translateY.getValue(time), translateZ.getValue(time));

        if (parent != null) result.multiply(parent.getTransform(prop));

        result.multiplyWithTranslation(internalOffset.x(), internalOffset.y(), internalOffset.z());

        result.multiply(Vector3f.XP.rotation(rotateX.getValue(time)));
        result.multiply(Vector3f.YP.rotation(rotateY.getValue(time)));
        result.multiply(Vector3f.ZP.rotation(rotateZ.getValue(time)));

        lastTransform = result;

        isVisible = prop.miHiddenParts == null || !prop.miHiddenParts.contains(name);
    }

    @Override
    public VertArrays getModel(MultipartUpdateProp prop) {
        return isVisible ? model : null;
    }

    @Override
    public Matrix4f getTransform(MultipartUpdateProp prop) {
        return lastTransform;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    public static class FloatSpline {

        public TreeMap<Float, Float> spline = new TreeMap<>();

        public float getValue(float key) {
            if (spline.size() < 1) {
                return 0F;
            }
            Map.Entry<Float, Float> floorEntry = spline.floorEntry(key);
            Map.Entry<Float, Float> ceilingEntry = spline.ceilingEntry(key);
            if (floorEntry == null) {
                return ceilingEntry.getValue();
            } else if (ceilingEntry == null) {
                return floorEntry.getValue();
            } else if (Objects.equals(floorEntry.getKey(), ceilingEntry.getKey())) {
                return floorEntry.getValue();
            } else {
                return floorEntry.getValue() + (ceilingEntry.getValue() - floorEntry.getValue()) *
                        ((key - floorEntry.getKey()) / (ceilingEntry.getKey() - floorEntry.getKey()));
            }
        }
    }
}
