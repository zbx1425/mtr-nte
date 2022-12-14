package cn.zbx1425.sowcer.batch;

import cn.zbx1425.sowcer.math.Matrix4f;

import java.util.Objects;

/** Additional property that's to be passed to the Shader. Set when enqueue. Affects batching. */
public class ShaderProp {

    /**
     * If null: You supply MATRIX_MODEL with matrices derived from the PoseStack passed into EntityRenderer.
     *   (MATRIX_MODEL with VertAttrSrc.INSTANCE_BUF cannot be used since matrix depends on camera pose.)
     * If not null: You supply MATRIX_MODEL in world space. You don't use the PoseStack passed into EntityRenderer.
     *   (MATRIX_MODEL with VertAttrSrc.INSTANCE_BUF possible.)
     * In either case you supply the normals in world coordinates.
     */
    public Matrix4f viewMatrix = null;

    public static ShaderProp DEFAULT = new ShaderProp();

    public ShaderProp() {

    }

    public ShaderProp setViewMatrix(Matrix4f viewMatrix) {
        this.viewMatrix = viewMatrix;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShaderProp that = (ShaderProp) o;
        return Objects.equals(viewMatrix, that.viewMatrix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(viewMatrix);
    }
}
