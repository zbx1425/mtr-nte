package cn.zbx1425.sowcerext.model;

import cn.zbx1425.sowcer.math.Vector3f;
import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Face {

    public int[] vertices;

    public Face(int[] vertices) {
        this.vertices = vertices;
    }

    public Face(DataInputStream dis) throws IOException {
        this.vertices = new int[] { dis.readInt(), dis.readInt(), dis.readInt() };
    }

    public static List<Face> triangulate(int[] vertices, boolean isFace2) {
        List<Face> result = new ArrayList<>();
        if (vertices.length > 3) {
            for (int i = 2; i < vertices.length; i++) {
                result.add(new Face(new int[] { vertices[0], vertices[i - 1], vertices[i]}));
            }
        } else {
            result.add(new Face(new int[] { vertices[0], vertices[1], vertices[2]}));
        }
        if (isFace2) {
            ArrayUtils.reverse(vertices);
            if (vertices.length > 3) {
                for (int i = 2; i < vertices.length; i++) {
                    result.add(new Face(new int[] { vertices[0], vertices[i - 1], vertices[i]}));
                }
            } else {
                result.add(new Face(new int[] { vertices[0], vertices[1], vertices[2]}));
            }
        }
        return result;
    }

    public static List<Face> triangulate(int begin, int end, boolean isFace2) {
        return triangulate(IntStream.range(begin, end + 1).toArray(), isFace2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Face csvFace = (Face) o;
        return Arrays.equals(vertices, csvFace.vertices);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vertices);
    }

    public Face copy() {
        return new Face(Arrays.copyOf(vertices, vertices.length));
    }

    public void flip() {
        ArrayUtils.reverse(vertices);
    }

    public void serializeTo(DataOutputStream dos) throws IOException {
        assert this.vertices.length == 3;
        dos.writeInt(this.vertices[0]);
        dos.writeInt(this.vertices[1]);
        dos.writeInt(this.vertices[2]);
    }
}