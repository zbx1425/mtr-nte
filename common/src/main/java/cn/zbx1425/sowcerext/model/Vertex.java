package cn.zbx1425.sowcerext.model;

import cn.zbx1425.sowcer.math.Vector3f;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class Vertex {

    public Vector3f position;
    public Vector3f normal;
    public float u, v;

    public int color;
    public int light;

    public Vertex() {

    }

    public Vertex(Vector3f position) {
        this.position = position;
        this.normal = new Vector3f(0, 0, 0);
    }

    public Vertex(Vector3f position, Vector3f normal) {
        this.position = position;
        this.normal = normal;
    }

    public Vertex(DataInputStream dis) throws IOException {
        this.position = new Vector3f(dis.readFloat(), dis.readFloat(), dis.readFloat());
        this.normal = new Vector3f(dis.readFloat(), dis.readFloat(), dis.readFloat());
        this.u = dis.readFloat();
        this.v = dis.readFloat();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return Float.compare(vertex.u, u) == 0 && Float.compare(vertex.v, v) == 0 && position.equals(vertex.position) && normal.equals(vertex.normal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, normal, u, v);
    }

    public Vertex copy() {
        Vertex clone = new Vertex(position.copy(), normal.copy());
        clone.u = u;
        clone.v = v;
        clone.color = color;
        clone.light = light;
        return clone;
    }

    public void serializeTo(DataOutputStream dos) throws IOException {
        dos.writeFloat(this.position.x());
        dos.writeFloat(this.position.y());
        dos.writeFloat(this.position.z());
        dos.writeFloat(this.normal.x());
        dos.writeFloat(this.normal.y());
        dos.writeFloat(this.normal.z());
        dos.writeFloat(this.u);
        dos.writeFloat(this.v);
    }
}
