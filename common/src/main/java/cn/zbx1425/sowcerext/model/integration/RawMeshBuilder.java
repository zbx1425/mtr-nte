package cn.zbx1425.sowcerext.model.integration;

import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcerext.model.Face;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.Vertex;
import cn.zbx1425.sowcer.math.Vector3f;
import net.minecraft.resources.ResourceLocation;

import java.util.stream.IntStream;

public class RawMeshBuilder {

    private final RawMesh mesh;

    private final int faceSize;
    private Vertex buildingVertex = new Vertex();

    public RawMeshBuilder(int faceSize, String renderType, ResourceLocation texture) {
        this.faceSize = faceSize;
        this.mesh = new RawMesh(new MaterialProp());
        mesh.setRenderType(renderType);
        mesh.materialProp.texture = texture;
        mesh.materialProp.attrState.setColor(255, 255, 255, 255);
    }

    public RawMesh getMesh() {
        return mesh;
    }

    public RawMeshBuilder reset() {
        mesh.vertices.clear();
        mesh.faces.clear();
        setNewDefaultVertex();
        return this;
    }

    public RawMeshBuilder vertex(Vector3f position) {
        buildingVertex.position = position;
        return this;
    }

    public RawMeshBuilder vertex(double d, double e, double f) {
        buildingVertex.position = new Vector3f((float) d, (float) e, (float) f);
        return this;
    }

    public RawMeshBuilder normal(float f, float g, float h) {
        buildingVertex.normal = new Vector3f(f, g, h);
        return this;
    }

    public RawMeshBuilder uv(float f, float g) {
        buildingVertex.u = f;
        buildingVertex.v = g;
        return this;
    }

    public RawMeshBuilder endVertex() {
        mesh.vertices.add(buildingVertex);
        setNewDefaultVertex();
        if (mesh.vertices.size() % faceSize == 0) {
            mesh.faces.addAll(Face.triangulate(IntStream.range(mesh.vertices.size() - faceSize, mesh.vertices.size()).toArray(), false));
        }
        return this;
    }

    public RawMeshBuilder color(int r, int g, int b, int a) {
        mesh.materialProp.attrState.setColor(r, g, b, a);
        return this;
    }

    public RawMeshBuilder lightMapUV(short u, short v) {
        mesh.materialProp.attrState.setLightmapUV(u, v);
        return this;
    }

    private RawMeshBuilder setNewDefaultVertex() {
        buildingVertex = new Vertex(new Vector3f(0, 0, 0));
        buildingVertex.normal = new Vector3f(0, 0, 0);
        buildingVertex.u = 0;
        buildingVertex.v = 0;
        return this;
    }
}
