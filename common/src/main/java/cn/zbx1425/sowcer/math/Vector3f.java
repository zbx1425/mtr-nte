package cn.zbx1425.sowcer.math;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Vector3f {

#if MC_VERSION >= "11903"

    protected final org.joml.Vector3f impl;

    public Vector3f(float x, float y, float z) {
        this.impl = new org.joml.Vector3f(x, y, z);
    }

    public float x() { return impl.x(); }
    public float y() { return impl.y(); }
    public float z() { return impl.z(); }

    private Vector3f(Vector3f other) {
        this.impl = new org.joml.Vector3f(other.impl);
    }

    public Vector3f(org.joml.Vector3f moj) {
        this.impl = moj;
    }

    public Vector3f copy() {
        return new Vector3f(this);
    }

    public void normalize() {
        impl.normalize();
    }

    public void add(float x, float y, float z) {
        impl.add(x, y, z);
    }

    public void add(Vector3f other) {
        impl.add(other.impl);
    }

    public void sub(Vector3f other) {
        impl.sub(other.impl);
    }

    public void mul(float x, float y, float z) {
        impl.mul(x, y, z);
    }

    public void mul(float n) {
        impl.mul(n);
    }

    public void rot(Vector3f axis, float rad) {
        impl.rotateAxis(rad, axis.x(), axis.y(), axis.z());
    }

    public void rotDeg(Vector3f axis, float deg) {
        impl.rotateAxis((float)Math.toRadians(deg), axis.x(), axis.y(), axis.z());
    }

    public void rotX(float rad) {
        impl.rotateX(rad);
    }

    public void rotY(float rad) {
        impl.rotateY(rad);
    }

    public void rotZ(float rad) {
        impl.rotateZ(rad);
    }

    public void cross(Vector3f other) {
        impl.cross(other.impl);
    }

    #else

    protected final com.mojang.math.Vector3f impl;

    public Vector3f(float x, float y, float z) {
        this.impl = new com.mojang.math.Vector3f(x, y, z);
    }

    public float x() { return impl.x(); }
    public float y() { return impl.y(); }
    public float z() { return impl.z(); }

    private Vector3f(Vector3f other) {
        this.impl = other.impl.copy();
    }

    public Vector3f copy() {
        return new Vector3f(this);
    }

    public void normalize() {
        impl.normalize();
    }

    public void add(float x, float y, float z) {
        impl.add(x, y, z);
    }

    public void add(Vector3f other) {
        impl.add(other.impl);
    }

    public void sub(Vector3f other) {
        impl.sub(other.impl);
    }

    public void mul(float x, float y, float z) {
        impl.mul(x, y, z);
    }

    public void mul(float n) {
        impl.mul(n);
    }

    public void rot(Vector3f axis, float rad) {
        impl.transform(axis.impl.rotation(rad));
    }

    public void rotDeg(Vector3f axis, float deg) {
        impl.transform(axis.impl.rotationDegrees(deg));
    }

    public void rotX(float rad) {
        impl.transform(com.mojang.math.Vector3f.XP.rotation(rad));
    }

    public void rotY(float rad) {
        impl.transform(com.mojang.math.Vector3f.YP.rotation(rad));
    }

    public void rotZ(float rad) {
        impl.transform(com.mojang.math.Vector3f.ZP.rotation(rad));
    }

    public void cross(Vector3f other) {
        impl.cross(other.impl);
    }

#endif

    @Override
    public int hashCode() {
        return impl.hashCode();
    }

    public float distance(Vector3f other) {
        float dx = x() - other.x();
        float dy = y() - other.y();
        float dz = z() - other.z();
        return (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float distanceSq(Vector3f other) {
        float dx = x() - other.x();
        float dy = y() - other.y();
        float dz = z() - other.z();
        return (float)(dx * dx + dy * dy + dz * dz);
    }

    public Vector3f(BlockPos blockPos) {
        this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public Vector3f(Vec3 vec3) {
        this((float)vec3.x, (float)vec3.y, (float)vec3.z);
    }

    public BlockPos toBlockPos() {
        return new BlockPos(Mth.floor(x()), Mth.floor(y()), Mth.floor(z()));
    }

    public Vec3 toVec3() {
        return new Vec3(x(), y(), z());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Vector3f vector3f = (Vector3f) o;

        return impl.equals(vector3f.impl);
    }

    public static final Vector3f ZERO = new Vector3f(0, 0, 0);
    public static final Vector3f XP = new Vector3f(1, 0, 0);
    public static final Vector3f YP = new Vector3f(0, 1, 0);
    public static final Vector3f ZP = new Vector3f(0, 0, 1);
}
