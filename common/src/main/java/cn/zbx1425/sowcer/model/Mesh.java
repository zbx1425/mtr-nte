package cn.zbx1425.sowcer.model;

import cn.zbx1425.sowcer.batch.BatchProp;
import cn.zbx1425.sowcer.object.IndexBuf;
import cn.zbx1425.sowcer.object.VertBuf;

import java.io.Closeable;

public class Mesh implements Closeable {

    public VertBuf vertBuf;
    public IndexBuf indexBuf;

    public BatchProp batchProp;

    public Mesh(VertBuf vertBuf, IndexBuf indexBuf, BatchProp batchProp) {
        this.vertBuf = vertBuf;
        this.indexBuf = indexBuf;
        this.batchProp = batchProp;
    }

    @Override
    public void close() {
        vertBuf.close();
        indexBuf.close();
    }
}
