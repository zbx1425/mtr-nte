package cn.zbx1425.sowcerext.model;

import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;

public class RawModel {

    public ResourceLocation sourceLocation;

    public HashMap<MaterialProp, RawMesh> meshList = new HashMap<>();

    public Model upload(VertAttrMapping mapping) {
        Model model = new Model();
        for (RawMesh mesh : meshList.values()) {
            if (mesh.faces.size() == 0) continue;
            if (!mesh.checkVertIndex()) throw new IndexOutOfBoundsException("RawModel contains invalid vertex index");
            model.meshList.add(mesh.upload(mapping));
        }
        return model;
    }

    public void append(RawMesh nextMesh) {
        if (meshList.containsKey(nextMesh.materialProp)) {
            RawMesh mesh = meshList.get(nextMesh.materialProp);
            mesh.append(nextMesh);
        } else {
            RawMesh newMesh = new RawMesh(nextMesh.materialProp);
            meshList.put(nextMesh.materialProp, newMesh);
            newMesh.append(nextMesh);
        }
    }

    public void append(Collection<RawMesh> nextMesh) {
        for (RawMesh mesh : nextMesh) append(mesh);
    }

    public void append(RawModel nextModel) {
        append(nextModel.meshList.values());
    }

    public void applyMatrix(Matrix4f matrix) {
        for (RawMesh mesh : meshList.values()) mesh.applyMatrix(matrix);
    }

    public void applyTranslation(float x, float y, float z) {
        for (RawMesh mesh : meshList.values()) mesh.applyTranslation(x, y, z);
    }

    public void applyRotation(Vector3f axis, float angle) {
        for (RawMesh mesh : meshList.values()) mesh.applyRotation(axis, angle);
    }

    public void applyScale(float x, float y, float z) {
        for (RawMesh mesh : meshList.values()) mesh.applyScale(x, y, z);
    }

    public void applyMirror(boolean vx, boolean vy, boolean vz, boolean nx, boolean ny, boolean nz) {
        for (RawMesh mesh : meshList.values()) mesh.applyMirror(vx, vy, vz, nx, ny, nz);
    }

    public void applyShear(Vector3f dir, Vector3f shear, float ratio) {
        for (RawMesh mesh : meshList.values()) mesh.applyShear(dir, shear, ratio);
    }

    public RawModel copy() {
        RawModel result = new RawModel();
        result.sourceLocation = this.sourceLocation;
        for (RawMesh mesh : this.meshList.values()) result.meshList.put(mesh.materialProp, mesh.copy());
        return result;
    }
}
