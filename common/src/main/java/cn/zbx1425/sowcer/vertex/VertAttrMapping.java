package cn.zbx1425.sowcer.vertex;

import cn.zbx1425.sowcer.object.InstanceBuf;
import cn.zbx1425.sowcer.object.VertBuf;
import org.lwjgl.opengl.GL33;

import java.util.HashMap;

public class VertAttrMapping {

    public final HashMap<VertAttrType, VertAttrSrc> sources;
    public final HashMap<VertAttrType, Integer> pointers = new HashMap<>();
    public final int strideVertex, strideInstance;

    private VertAttrMapping(HashMap<VertAttrType, VertAttrSrc> sources) {
        this.sources = sources;

        int strideVertex = 0, strideInstance = 0;
        for (VertAttrType attrType : VertAttrType.values()) {
            switch (sources.get(attrType)) {
                case VERTEX_BUF:
                    pointers.put(attrType, strideVertex);
                    strideVertex += attrType.byteSize;
                    break;
            }
        }
        if (strideVertex % 2 != 0) strideVertex++;
        if (strideInstance % 2 != 0) strideInstance++;

        this.strideVertex = strideVertex;
        this.strideInstance = strideInstance;
    }

    public void setupAttrsToVao(VertBuf vertexBuf, InstanceBuf instanceBuf) {
        for (VertAttrType attrType : VertAttrType.values()) {
            switch (sources.get(attrType)) {
                case GLOBAL:
                    attrType.toggleAttrArray(false);
                    break;
                case VERTEX_BUF:
                    attrType.toggleAttrArray(true);
                    vertexBuf.bind(GL33.GL_ARRAY_BUFFER);
                    attrType.setupAttrPtr(strideVertex, pointers.get(attrType));
                    break;
            }
        }
    }

    public static class Builder {

        private final HashMap<VertAttrType, VertAttrSrc> sources;

        public Builder() {
            sources = new HashMap<>(VertAttrType.values().length);
            for (VertAttrType attrType : VertAttrType.values()) {
                sources.put(attrType, VertAttrSrc.VERTEX_BUF);
            }
        }

        public Builder set(VertAttrType type, VertAttrSrc src) {
            sources.put(type, src);
            return this;
        }

        public VertAttrMapping build() {
            return new VertAttrMapping(sources);
        }
    }
}
