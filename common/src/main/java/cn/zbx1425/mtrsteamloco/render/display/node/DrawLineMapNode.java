package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.display.DisplaySink;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;

public class DrawLineMapNode implements DisplayNode {

    private final String slot;
    private final int x1, y1, x2, x3, x4, y4;
    private final float u1, v1, u2, u3, u4, v4;

    public DrawLineMapNode(JsonObject jsonObject) {
        slot = jsonObject.get("slot").getAsString();
        JsonArray srcArea = jsonObject.get("src_area").getAsJsonArray();
        x1 = srcArea.get(0).getAsInt(); y1 = srcArea.get(1).getAsInt();
        x2 = srcArea.get(2).getAsInt(); x3 = srcArea.get(3).getAsInt();
        x4 = x1 + srcArea.get(4).getAsInt(); y4 = y1 + srcArea.get(5).getAsInt();
        JsonArray dstArea = jsonObject.get("dst_area").getAsJsonArray();
        u1 = dstArea.get(0).getAsFloat(); v1 = dstArea.get(1).getAsFloat();
        u4 = u1 + dstArea.get(2).getAsFloat(); v4 = v1 + dstArea.get(3).getAsFloat();
        u2 = u1 + (x2 - x1) * (u4 - u1) / (x4 - x1);
        u3 = u1 + (x3 - x1) * (u4 - u1) / (x4 - x1);
    }

    @Override
    public void tick(DisplaySink sink, TrainClient train) {
        sink.addQuad(slot, x1, y1, x2, y4, u1, v1, u1, v4);
        sink.addQuad(slot, x2, y1, x3, y4, u2, v1, u3, v4);
        sink.addQuad(slot, x3, y1, x4, y4, u3, v1, u4, v4);
    }
}
