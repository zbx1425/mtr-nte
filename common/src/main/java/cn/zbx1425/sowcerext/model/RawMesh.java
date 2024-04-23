package cn.zbx1425.sowcerext.model;

import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcer.model.Mesh;
import cn.zbx1425.sowcer.object.IndexBuf;
import cn.zbx1425.sowcer.object.VertBuf;
import cn.zbx1425.sowcer.util.OffHeapAllocator;
import cn.zbx1425.sowcer.util.DrawContext;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import cn.zbx1425.sowcerext.model.integration.FaceList;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import org.lwjgl.opengl.GL11;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class RawMesh {

    public final MaterialProp materialProp;
    public List<Vertex> vertices = new ArrayList<>();
    public List<Face> faces = new ArrayList<>();

    public RawMesh(MaterialProp materialProp) {
        this.materialProp = materialProp;
    }

    public RawMesh(DataInputStream dis) throws IOException {
        this.materialProp = new MaterialProp(dis);
        int numVertices = dis.readInt();
        this.vertices = new ArrayList<>(numVertices);
        for (int i = 0; i < numVertices; i++) this.vertices.add(new Vertex(dis));
        int numFaces = dis.readInt();
        this.faces = new ArrayList<>(numFaces);
        for (int i = 0; i < numFaces; i++) this.faces.add(new Face(dis));
    }

    public void append(RawMesh nextMesh) {
        if (nextMesh == this) throw new IllegalStateException("Mesh self-appending");
        int vertOffset = vertices.size();
        vertices.addAll(nextMesh.vertices);
        for (Face face : nextMesh.faces) {
            Face newFace = face.copy();
            for (int i = 0; i < newFace.vertices.length; ++i) {
                newFace.vertices[i] += vertOffset;
            }
            faces.add(newFace);
        }
    }

    public void appendTransformed(RawMesh nextMesh, Matrix4f mat, int color, int light) {
        if (nextMesh == this) throw new IllegalStateException("Mesh self-appending");
        int vertOffset = vertices.size();
        for (Vertex vertex : nextMesh.vertices) {
            Vertex newVertex = new Vertex(mat.transform(vertex.position), mat.transform3(vertex.normal));
            newVertex.u = vertex.u;
            newVertex.v = vertex.v;
            newVertex.color = color;
            newVertex.light = light;
            vertices.add(newVertex);
        }
        for (Face face : nextMesh.faces) {
            Face newFace = face.copy();
            for (int i = 0; i < newFace.vertices.length; ++i) {
                newFace.vertices[i] += vertOffset;
            }
            faces.add(newFace);
        }
    }

    public void clear() {
        vertices.clear();
        faces.clear();
    }

    public void validateVertIndex() {
        for (Face face : faces) {
            for (int vertIndex : face.vertices) {
                if (vertIndex < 0 || vertIndex >= vertices.size()) {
                    throw new IndexOutOfBoundsException("RawMesh contains invalid vertex index "
                            + vertIndex + " (Should be 0 to " + (vertices.size() - 1) + ")");
                }
            }
        }
    }

    public void triangulate() {
        List<Face> newFaces = new ArrayList<>();
        for (Face face : faces) {
            newFaces.addAll(Face.triangulate(face.vertices, false));
        }
        faces.clear();
        faces.addAll(newFaces);
    }

    /** Removes duplicate vertices and faces from the mesh. */
    public void distinct() {
        // if (vertices.size() > 10000 || faces.size() > 10000) return;
        // if (vertices.size() > 0) return;

        final List<Vertex> distinctVertices = new ArrayList<>(vertices.size());
        final HashMap<Vertex, Integer> verticesLookup = new HashMap<>(vertices.size());
        final LinkedHashSet<Face> distinctFaces = new LinkedHashSet<>(faces.size());

        for (Face face : faces) {
            for (int i = 0; i < face.vertices.length; ++i) {
                Vertex vertex = vertices.get(face.vertices[i]);
                int newIndex;
                if (verticesLookup.containsKey(vertex)) {
                    newIndex = verticesLookup.get(vertex);
                } else {
                    distinctVertices.add(vertex);
                    newIndex = distinctVertices.size() - 1;
                    verticesLookup.put(vertex, newIndex);
                }
                face.vertices[i] = newIndex;
            }
            distinctFaces.add(face);
        }

        vertices.clear();
        vertices.addAll(distinctVertices);
        faces.clear();
        faces.addAll(distinctFaces);
    }

    /** Generates normals for vertices without a normal vector. Produces duplicate vertices. */
    public void generateNormals() {
        final List<Vertex> newVertices = new ArrayList<>(vertices.size());
        for (Face face : faces) {
            if (face.vertices.length >= 3) {
                int i0 = face.vertices[0];
                int i1 = face.vertices[1];
                int i2 = face.vertices[2];
                double ax = vertices.get(i1).position.x() - vertices.get(i0).position.x();
                double ay = vertices.get(i1).position.y() - vertices.get(i0).position.y();
                double az = vertices.get(i1).position.z() - vertices.get(i0).position.z();
                double bx = vertices.get(i2).position.x() - vertices.get(i0).position.x();
                double by = vertices.get(i2).position.y() - vertices.get(i0).position.y();
                double bz = vertices.get(i2).position.z() - vertices.get(i0).position.z();
                double nx = ay * bz - az * by;
                double ny = az * bx - ax * bz;
                double nz = ax * by - ay * bx;
                double t = nx * nx + ny * ny + nz * nz;
                if (t != 0.0) {
                    t = 1.0 / Math.sqrt(t);
                    float mx = (float) (nx * t);
                    float my = (float) (ny * t);
                    float mz = (float) (nz * t);
                    for (int j = 0; j < face.vertices.length; j++) {
                        Vertex newVert = vertices.get(face.vertices[j]).copy();
                        if (vecIsZero(newVert.normal)) {
                            newVert.normal = new Vector3f(mx, my, mz);
                        }
                        newVertices.add(newVert);
                        face.vertices[j] = newVertices.size() - 1;
                    }
                } else {
                    for (int j = 0; j < face.vertices.length; j++) {
                        Vertex newVert = vertices.get(face.vertices[j]).copy();
                        if (vecIsZero(vertices.get(face.vertices[j]).normal)) {
                            newVert.normal = new Vector3f(0.0f, 1.0f, 0.0f);
                        }
                        newVertices.add(newVert);
                        face.vertices[j] = newVertices.size() - 1;
                    }
                }
            }
        }
        vertices = newVertices;
    }

    public void upload(Mesh mesh, VertAttrMapping mapping) {
        distinct();

        ByteBuffer vertBuf = OffHeapAllocator.allocate(vertices.size() * mapping.strideVertex);
        for (int i = 0; i < vertices.size(); ++i) {
            if (mapping.sources.get(VertAttrType.POSITION).inVertBuf()) {
                Vector3f pos = vertices.get(i).position;
                // vertBuf.position(getVertBufPos(mapping, i, VertAttrType.POSITION));
                vertBuf.putFloat(pos.x()).putFloat(pos.y()).putFloat(pos.z());
            }
            if (mapping.sources.get(VertAttrType.COLOR).inVertBuf()) {
                // vertBuf.position(getVertBufPos(mapping, i, VertAttrType.COLOR));
                vertBuf.putInt(vertices.get(i).color);
            }
            if (mapping.sources.get(VertAttrType.UV_TEXTURE).inVertBuf()) {
                float u = vertices.get(i).u;
                float v = vertices.get(i).v;
                // vertBuf.position(getVertBufPos(mapping, i, VertAttrType.UV_TEXTURE));
                vertBuf.putFloat(u).putFloat(v);
            }
            if (mapping.sources.get(VertAttrType.UV_LIGHTMAP).inVertBuf()) {
                // vertBuf.position(getVertBufPos(mapping, i, VertAttrType.UV_LIGHTMAP));
                vertBuf.putInt(vertices.get(i).light);
            }
            if (mapping.sources.get(VertAttrType.NORMAL).inVertBuf()) {
                Vector3f mojNormal = vertices.get(i).normal.copy();
                mojNormal.normalize();
                // vertBuf.position(getVertBufPos(mapping, i, VertAttrType.NORMAL));
                vertBuf.put((byte) (mojNormal.x() * 0x7F)).put((byte) (mojNormal.y() * 0x7F)).put((byte) (mojNormal.z() * 0x7F));
            }
            for (int k = 0; k < mapping.paddingVertex; k++) vertBuf.put((byte)0);
        }
        mesh.vertBuf.upload(vertBuf, VertBuf.USAGE_STATIC_DRAW);
        OffHeapAllocator.free(vertBuf);

        ByteBuffer indexBuf = OffHeapAllocator.allocate(faces.size() * 3 * 4);
        for (Face face : faces) {
            for (int j = 0; j < face.vertices.length; ++j) {
                indexBuf.putInt(face.vertices[j]);
            }
        }
        mesh.indexBuf.upload(indexBuf, VertBuf.USAGE_STATIC_DRAW);
        mesh.indexBuf.setFaceCount(faces.size());
        OffHeapAllocator.free(indexBuf);

    }

    public Mesh upload(VertAttrMapping mapping) {
        validateVertIndex();
        VertBuf vertBufObj = new VertBuf();
        IndexBuf indexBufObj = new IndexBuf(faces.size(), GL11.GL_UNSIGNED_INT);
        Mesh target = new Mesh(vertBufObj, indexBufObj, materialProp);
        upload(target, mapping);
        return target;
    }

    private static int getVertBufPos(VertAttrMapping mapping, int vertId, VertAttrType type) {
        return mapping.strideVertex * vertId + mapping.pointers.get(type);
    }

    private static boolean vecIsZero(Vector3f vec) {
        return vec.x() == 0.0F && vec.y() == 0.0F && vec.z() == 0.0F;
    }

    public void applyMatrix(Matrix4f matrix) {
        for (Vertex vertex : vertices) {
            vertex.position = matrix.transform(vertex.position);
            vertex.normal = matrix.transform3(vertex.normal);
        }
    }

    public void applyTranslation(float x, float y, float z) {
        for (Vertex vertex : vertices) {
            vertex.position.add(x, y, z);
        }
    }

    public void applyRotation(Vector3f axis, float angle) {
        for (Vertex vertex : vertices) {
            vertex.position.rotDeg(axis, angle);
            vertex.normal.rotDeg(axis, angle);
        }
    }

    public void applyScale(float x, float y, float z) {
        float rx = (float) (1.0 / x);
        float ry = (float) (1.0 / y);
        float rz = (float) (1.0 / z);
        float rx2 = rx * rx;
        float ry2 = ry * ry;
        float rz2 = rz * rz;
        boolean reverse = x * y * z < 0.0;
        for (Vertex vertex : vertices) {
            vertex.position.mul(x, y, z);
            float nx2 = vertex.normal.x() * vertex.normal.x();
            float ny2 = vertex.normal.y() * vertex.normal.y();
            float nz2 = vertex.normal.z() * vertex.normal.z();
            float u = nx2 * rx2 + ny2 * ry2 + nz2 * rz2;
            if (u != 0.0) {
                u = (float) Math.sqrt((nx2 + ny2 + nz2) / u);
                vertex.normal.mul(rx * u, ry * u, rz * u);
            }
        }

        if (reverse) {
            for (Face face : faces) {
                face.flip();
            }
        }
    }

    public void applyMirror(boolean vx, boolean vy, boolean vz, boolean nx, boolean ny, boolean nz) {
        for (Vertex vertex : vertices) {
            vertex.position.mul(vx ? -1 : 1, vy ? -1 : 1, vz ? -1 : 1);
            vertex.normal.mul(nx ? -1 : 1, ny ? -1 : 1, nz ? -1 : 1);
        }

        int numFlips = 0;
        if (vx) numFlips++;
        if (vy) numFlips++;
        if (vz) numFlips++;

        if (numFlips % 2 != 0) {
            for (Face face : faces) {
                face.flip();
            }
        }
    }

    public void applyUVMirror(boolean u, boolean v) {
        for (Vertex vertex : vertices) {
            if (u) vertex.u = 1 - vertex.u;
            if (v) vertex.v = 1 - vertex.v;
        }
    }

    public void applyShear(Vector3f dir, Vector3f shear, float ratio) {
        for (Vertex vertex : vertices) {
            float n1 = ratio * (dir.x() * vertex.position.x() + dir.y() * vertex.position.y() + dir.z() * vertex.position.z());
            Vector3f offset1 = shear.copy();
            offset1.mul(n1);
            vertex.position.add(offset1);
            if (!vecIsZero(vertex.normal)) {
                float n2 = ratio * (shear.x() * vertex.normal.x() + shear.y() * vertex.normal.y() + shear.z() * vertex.normal.z());
                Vector3f offset2 = dir.copy();
                offset2.mul(-n2);
                vertex.normal.add(offset2);
                vertex.normal.normalize();
            }
        }
    }
    
    public void setRenderType(String type) {
        materialProp.translucent = false;
        materialProp.writeDepthBuf = true;
        materialProp.cutoutHack = false;
        materialProp.attrState = materialProp.attrState.copy();
        materialProp.attrState.lightmapUV = null;
        switch (type) {
            case "exterior":
                materialProp.shaderName = "rendertype_entity_cutout";
                break;
            case "exteriortranslucent":
                materialProp.shaderName = "rendertype_entity_translucent_cull";
                materialProp.translucent = true;
                break;
            case "interior":
                materialProp.shaderName = "rendertype_entity_cutout";
                materialProp.attrState.setLightmapUV(15 << 4 | 15 << 20);
                break;
            case "interiortranslucent":
                materialProp.shaderName = "rendertype_entity_translucent_cull";
                materialProp.translucent = true;
                materialProp.attrState.setLightmapUV(15 << 4 | 15 << 20);
                break;
            case "light":
                materialProp.shaderName = "rendertype_beacon_beam";
                materialProp.cutoutHack = true;
                break;
            case "lighttranslucent":
                materialProp.shaderName = "rendertype_beacon_beam";
                materialProp.translucent = true;
                materialProp.writeDepthBuf = false;
                break;
        }
    }

    public void writeBlazeBuffer(FaceList vertexConsumer, Matrix4f matrix, int color, int light, DrawContext drawContext) {
        drawContext.recordBlazeAction(faces.size());
        for (Face face : faces) {
            assert face.vertices.length == 3;
            Vertex[] transformedVertices = new Vertex[face.vertices.length];
            for (int i = 0; i < face.vertices.length; i++) {
                Vector3f transformedPosition = matrix.transform(this.vertices.get(face.vertices[i]).position);
                Vector3f transformedNormal = matrix.transform3(this.vertices.get(face.vertices[i]).normal);
                transformedVertices[i] = new Vertex(transformedPosition, transformedNormal);
                transformedVertices[i].u = this.vertices.get(face.vertices[i]).u;
                transformedVertices[i].v = this.vertices.get(face.vertices[i]).v;
            }
            vertexConsumer.addFace(transformedVertices, color, light);
        }
    }

    public RawMesh copy() {
        RawMesh result = new RawMesh(this.materialProp.copy());
        for (Vertex vertex : this.vertices) result.vertices.add(vertex.copy());
        for (Face face : this.faces) result.faces.add(face.copy());
        return result;
    }

    public RawMesh copyForMaterialChanges() {
        RawMesh result = new RawMesh(this.materialProp.copy());
        result.vertices = vertices;
        result.faces = faces;
        return result;
    }

    public void serializeTo(DataOutputStream dos) throws IOException {
        materialProp.serializeTo(dos);
        dos.writeInt(vertices.size());
        for (Vertex vertex : vertices) vertex.serializeTo(dos);
        dos.writeInt(faces.size());
        for (Face face : faces) face.serializeTo(dos);
    }
}
