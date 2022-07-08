package cn.zbx1425.sowcer.model;

import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcer.object.IndexBuf;
import cn.zbx1425.sowcer.object.VertBuf;

import java.io.Closeable;

public class Mesh implements Closeable {

    public VertBuf vertBuf;
    public IndexBuf indexBuf;

    public MaterialProp materialProp;

    public Mesh(VertBuf vertBuf, IndexBuf indexBuf, MaterialProp materialProp) {
        this.vertBuf = vertBuf;
        this.indexBuf = indexBuf;
        this.materialProp = materialProp;
    }

    @Override
    public void close() {
        vertBuf.close();
        indexBuf.close();
    }
}
