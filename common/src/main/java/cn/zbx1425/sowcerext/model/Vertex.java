package cn.zbx1425.sowcerext.model;

import com.mojang.math.Vector3f;

import java.util.Objects;

public class Vertex {

    public Vector3f position;
    public Vector3f normal;
    public float u, v;

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
        return clone;
    }
}
