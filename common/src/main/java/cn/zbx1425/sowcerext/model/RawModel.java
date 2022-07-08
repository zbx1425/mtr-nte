package cn.zbx1425.sowcerext.model;

import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;

import java.util.ArrayList;
import java.util.List;

public class RawModel {

    public List<RawMesh> meshList = new ArrayList<>();

    public Model upload(VertAttrMapping mapping) {
        Model model = new Model();
        for (RawMesh mesh : meshList) {
            if (mesh.faces.size() == 0) continue;
            if (!mesh.checkVertIndex()) throw new IndexOutOfBoundsException("RawModel contains invalid vertex index");
            model.meshList.add(mesh.upload(mapping));
        }
        return model;
    }
}
