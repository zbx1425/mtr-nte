package cn.zbx1425.sowcerext.model.integration;

import cn.zbx1425.sowcerext.model.Face;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.Vertex;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import cn.zbx1425.sowcer.math.Vector3f;

public class RawMeshBuilder implements VertexConsumer {

    public RawMesh mesh;
    public int light;
    public int color;

    private final VertexFormat.Mode mode;
    private Vertex buildingVertex = new Vertex();

    public RawMeshBuilder(RawMesh mesh, VertexFormat.Mode mode) {
        this.mesh = mesh;
        this.mode = mode;
    }

    @Override
    public VertexConsumer vertex(double d, double e, double f) {
        buildingVertex.position = new Vector3f((float) d, (float) e, (float) f);
        return this;
    }

    @Override
    public VertexConsumer normal(float f, float g, float h) {
        buildingVertex.normal = new Vector3f(f, g, h);
        return this;
    }

    @Override
    public VertexConsumer uv(float f, float g) {
        buildingVertex.u = f;
        buildingVertex.v = g;
        return this;
    }

    @Override
    public void endVertex() {
        mesh.vertices.add(buildingVertex);
        buildingVertex = new Vertex();
        if (mesh.vertices.size() % mode.primitiveLength == 0) {
            mesh.faces.add(new Face(mesh.vertices.size() - mode.primitiveLength, mesh.vertices.size() - 1));
        }
    }

    @Override
    public VertexConsumer color(int i, int j, int k, int l) {
        defaultColor(i, j, k, l);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int i, int j) {
        return this;
    }

    @Override
    public VertexConsumer uv2(int i, int j) {
        light = i + j << 16;
        return this;
    }


    @Override
    public void defaultColor(int r, int g, int b, int a) {
        color = r << 24 | g << 16 | b << 8 | a;
    }

    @Override
    public void unsetDefaultColor() {

    }
}
