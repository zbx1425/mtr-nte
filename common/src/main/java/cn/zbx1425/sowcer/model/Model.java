package cn.zbx1425.sowcer.model;

import java.io.Closeable;
import java.util.ArrayList;

public class Model implements Closeable {

    public ArrayList<Mesh> meshList = new ArrayList<>();

    @Override
    public void close() {
        for (Mesh mesh : meshList) {
            mesh.close();
        }
    }
}
