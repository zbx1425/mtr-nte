package cn.zbx1425.sowcerext.model;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Face implements Cloneable {

    int[] vertices;

    public Face(int[] vertices) {
        this.vertices = vertices;
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

    @Override
    public Face clone() {
        try {
            Face clone = (Face) super.clone();
            clone.vertices = vertices.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void flip() {
        ArrayUtils.reverse(vertices);
    }
}