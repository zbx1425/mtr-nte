package cn.zbx1425.sowcer.math;

import java.nio.FloatBuffer;

public class Matrix4f {

#if MC_VERSION <= "11902"

    protected final com.mojang.math.Matrix4f impl;

    public Matrix4f() {
        this.impl = new com.mojang.math.Matrix4f();
        this.impl.setIdentity();
    }

    public Matrix4f(com.mojang.math.Matrix4f moj) {
        this.impl = moj;
    }

    private Matrix4f(Matrix4f other) {
        this.impl = other.impl.copy();
    }

    public Matrix4f copy() {
        return new Matrix4f(this);
    }

    public com.mojang.math.Matrix4f asMoj() {
        return impl;
    }

    public void multiply(Matrix4f other) {
        impl.multiply(other.impl);
    }

    public void store(FloatBuffer buffer) {
        impl.store(buffer);
    }

    public void load(FloatBuffer buffer) {
        impl.load(buffer);
    }

    public void rotateX(float rad) {
        impl.multiply(com.mojang.math.Vector3f.XP.rotation(rad));
    }

    public void rotateY(float rad) {
        impl.multiply(com.mojang.math.Vector3f.YP.rotation(rad));
    }

    public void rotateZ(float rad) {
        impl.multiply(com.mojang.math.Vector3f.ZP.rotation(rad));
    }

    public void rotate(Vector3f axis, float rad) {
        impl.multiply(axis.impl.rotation(rad));
    }

    public void translate(float x, float y, float z) {
        impl.translate(new com.mojang.math.Vector3f(x, y, z));
    }

    public void multiplyWithTranslation(float x, float y, float z) {
        impl.multiplyWithTranslation(x, y, z);
    }

    public Vector3f transform(Vector3f src) {
        com.mojang.math.Vector4f pos4 = new com.mojang.math.Vector4f(src.x(), src.y(), src.z(), 1.0F);
        pos4.transform(impl);
        return new Vector3f(pos4.x(), pos4.y(), pos4.z());
    }

    public Vector3f transform3(Vector3f src) {
        Vector3f pos3 = src.copy();
        pos3.impl.transform(new com.mojang.math.Matrix3f(impl));
        return pos3;
    }
#endif
}
