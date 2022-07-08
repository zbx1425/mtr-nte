package cn.zbx1425.sowcerext.model;

import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcer.model.Mesh;
import cn.zbx1425.sowcer.object.IndexBuf;
import cn.zbx1425.sowcer.object.VertBuf;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.math.Vector3f;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RawMesh {

    public MaterialProp materialProp;
    public List<Vertex> vertices = new ArrayList<>();
    public List<Face> faces = new ArrayList<>();

    public RawMesh(MaterialProp materialProp) {
        this.materialProp = materialProp;
    }

    public void append(RawMesh nextMesh) {
        int vertOffset = vertices.size();
        vertices.addAll(nextMesh.vertices);
        for (Face face : nextMesh.faces) {
            Face newFace = face.clone();
            for (int i = 0; i < newFace.vertices.length; ++i) {
                newFace.vertices[i] += vertOffset;
            }
            faces.add(newFace);
        }
    }

    public boolean checkVertIndex() {
        for (Face face : faces) {
            for (int vertIndex : face.vertices) {
                if (vertIndex < 0 || vertIndex >= vertices.size()) return false;
            }
        }
        return true;
    }

    public void distinct() {
        final List<Vertex> distinctVertices = new ArrayList<>(vertices.size());
        final HashSet<Face> distinctFaces = new HashSet<>(faces.size());

        for (Face face : faces) {
            for (int i = 0; i < face.vertices.length; ++i) {
                Vertex vertex = vertices.get(face.vertices[i]);
                int newIndex = distinctVertices.indexOf(vertex);
                if (newIndex == -1) {
                    distinctVertices.add(vertex);
                    newIndex = distinctVertices.size() - 1;
                }
                face.vertices[i] = newIndex;
            }
            distinctFaces.add(face);
        }

        vertices = distinctVertices;
        faces.clear();
        faces.addAll(distinctFaces);
    }

    public void generateNormals() {
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
                        if (vecIsZero(vertices.get(face.vertices[j]).normal)) {
                            vertices.get(face.vertices[j]).normal = new Vector3f(mx, my, mz);
                        }
                    }
                } else {
                    for (int j = 0; j < face.vertices.length; j++) {
                        if (vecIsZero(vertices.get(face.vertices[j]).normal)) {
                            vertices.get(face.vertices[j]).normal = new Vector3f(0.0f, 1.0f, 0.0f);
                        }
                    }
                }
            }
        }
    }

    public Mesh upload(VertAttrMapping mapping) {
        ByteBuffer vertBuf = MemoryTracker.create(vertices.size() * mapping.strideVertex);
        for (int i = 0; i < vertices.size(); ++i) {
            if (shouldWriteVertBuf(mapping, VertAttrType.POSITION)) {
                Vector3f pos = vertices.get(i).position;
                vertBuf.position(getVertBufPos(mapping, i, VertAttrType.POSITION));
                vertBuf.putFloat(pos.x()).putFloat(pos.y()).putFloat(pos.z());
            }
            if (shouldWriteVertBuf(mapping, VertAttrType.NORMAL)) {
                Vector3f normal = vertices.get(i).normal;
                Vector3f mojNormal = new Vector3f(normal.x(), normal.y(), normal.z());
                mojNormal.normalize();
                vertBuf.position(getVertBufPos(mapping, i, VertAttrType.NORMAL));
                vertBuf.put((byte) (mojNormal.x() * 0x7F)).put((byte) (mojNormal.y() * 0x7F)).put((byte) (mojNormal.z() * 0x7F));
            }
            if (shouldWriteVertBuf(mapping, VertAttrType.UV_TEXTURE)) {
                float u = vertices.get(i).u;
                float v = vertices.get(i).v;
                vertBuf.position(getVertBufPos(mapping, i, VertAttrType.UV_TEXTURE));
                vertBuf.putFloat(u).putFloat(v);
            }
        }
        VertBuf vertBufObj = new VertBuf();
        vertBufObj.upload(vertBuf);

        ByteBuffer indexBuf = MemoryTracker.create(faces.size() * 3 * 4);
        for (Face face : faces) {
            for (int j = 0; j < face.vertices.length; ++j) {
                indexBuf.putInt(face.vertices[j]);
            }
        }
        IndexBuf indexBufObj = new IndexBuf(faces.size(), IndexBuf.PrimitiveMode.TRIANGLES, GL11.GL_UNSIGNED_INT);
        indexBufObj.upload(indexBuf);

        return new Mesh(vertBufObj, indexBufObj, materialProp);
    }

    public static boolean shouldWriteVertBuf(VertAttrMapping mapping, VertAttrType type) {
        return mapping.sources.get(type) == VertAttrSrc.VERTEX_BUF;
    }

    protected static int getVertBufPos(VertAttrMapping mapping, int vertId, VertAttrType type) {
        return mapping.strideVertex * vertId + mapping.pointers.get(type);
    }

    private static boolean vecIsZero(Vector3f vec) {
        return vec.x() == 0.0F && vec.y() == 0.0F && vec.z() == 0.0F;
    }

    public void applyTranslation(float x, float y, float z) {
        for (Vertex vertex : vertices) {
            vertex.position.add(x, y, z);
        }
    }

    public void applyRotation(Vector3f axis, float angle) {
        for (Vertex vertex : vertices) {
            vertex.position.transform(axis.rotationDegrees(angle));
            vertex.normal.transform(axis.rotationDegrees(angle));
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
}
