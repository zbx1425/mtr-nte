package cn.zbx1425.sowcer.batch;

import cn.zbx1425.sowcer.vertex.VertAttrState;

import java.util.Objects;

/** Additional property that's to be passed to the Shader. Set when enqueue. Affects batching. */
public class ShaderProp {

    /**
     * If true: You supply MATRIX_MODEL with matrices derived from the PoseStack passed into EntityRenderer.
     *   (MATRIX_MODEL with VertAttrSrc.INSTANCE_BUF cannot be used since matrix depends on camera pose.)
     * If false: You supply MATRIX_MODEL in world space. You don't use the PoseStack passed into EntityRenderer.
     *   (MATRIX_MODEL with VertAttrSrc.INSTANCE_BUF possible. SowCer does the camera transform for you.)
     * In either case you supply the normals in world coordinates.
     */
    public boolean eyeTransformInModelMatrix = true;

    public static ShaderProp DEFAULT = new ShaderProp();

    public ShaderProp() {

    }

    public ShaderProp setEyeTransformInModelMatrix(boolean eyeTransformInModelMatrix) {
        this.eyeTransformInModelMatrix = eyeTransformInModelMatrix;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShaderProp that = (ShaderProp) o;
        return eyeTransformInModelMatrix == that.eyeTransformInModelMatrix;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eyeTransformInModelMatrix);
    }
}
