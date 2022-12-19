package cn.zbx1425.sowcer.math;

import java.nio.FloatBuffer;

public class Matrix4f {

#if MC_VERSION >= "11903"

    protected final org.joml.Matrix4f impl;
    public Matrix4f() {
        this.impl = new org.joml.Matrix4f();
        this.impl.identity();
    }

    public Matrix4f(org.joml.Matrix4f moj) {
        this.impl = moj;
    }

    private Matrix4f(Matrix4f other) {
        this.impl = new org.joml.Matrix4f(other.impl);
    }

    public Matrix4f copy() {
        return new Matrix4f(this);
    }

    public org.joml.Matrix4f asMoj() {
        return impl;
    }

    public static Matrix4f translation(float x, float y, float z) {
        Matrix4f result = new Matrix4f();
        result.impl.translation(x, y, z);
        return result;
    }

    public void multiply(Matrix4f other) {
        impl.mul(other.impl);
    }

    public void store(FloatBuffer buffer) {
        buffer
                .put(0,  impl.m00())
                .put(1,  impl.m01())
                .put(2,  impl.m02())
                .put(3,  impl.m03())
                .put(4,  impl.m10())
                .put(5,  impl.m11())
                .put(6,  impl.m12())
                .put(7,  impl.m13())
                .put(8,  impl.m20())
                .put(9,  impl.m21())
                .put(10, impl.m22())
                .put(11, impl.m23())
                .put(12, impl.m30())
                .put(13, impl.m31())
                .put(14, impl.m32())
                .put(15, impl.m33());
    }

    public void load(FloatBuffer buffer) {
        impl.set(buffer);
    }

    public void rotateX(float rad) {
        impl.rotateX(rad);
    }

    public void rotateY(float rad) {
        impl.rotateY(rad);
    }

    public void rotateZ(float rad) {
        impl.rotateZ(rad);
    }

    public void rotate(Vector3f axis, float rad) {
        impl.rotate(rad, axis.impl);
    }

    public void translate(float x, float y, float z) {
        impl.translate(x, y, z);
    }

    public Vector3f transform(Vector3f src) {
        return new Vector3f(impl.transformPosition(src.impl));
    }

    public Vector3f transform3(Vector3f src) {
        return new Vector3f(impl.transformDirection(src.impl));
    }

    public org.joml.Matrix3f getRotationPart() {
        org.joml.Matrix3f result = new org.joml.Matrix3f();
        return impl.get3x3(result);
    }

#else

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

    public static Matrix4f translation(float x, float y, float z) {
        Matrix4f result = new Matrix4f();
        result.impl.translate(x, y, z);
        return result;
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

    public com.mojang.math.Matrix3f getRotationPart() {
        float[] srcValues = new float[16];
        FloatBuffer srcFloatBuffer = FloatBuffer.wrap(srcValues);
        impl.store(srcFloatBuffer);
        ByteBuffer dstBuffer = ByteBuffer.allocate(9 * 4);
        FloatBuffer dstFloatBuffer = dstBuffer.asFloatBuffer();
        dstFloatBuffer.put(srcValues, 0, 3);
        dstFloatBuffer.put(srcValues, 4, 3);
        dstFloatBuffer.put(srcValues, 8, 3);
        com.mojang.math.Matrix3f result = new com.mojang.math.Matrix3f();
        result.load(dstFloatBuffer);
        return result;
    }
#endif
}
