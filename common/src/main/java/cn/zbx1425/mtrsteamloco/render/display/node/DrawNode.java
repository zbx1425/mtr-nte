package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.sowcer.util.AttrUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;

public class DrawNode implements DisplayNode {

    private final String slot;
    private final int x1, y1, x2, y2;
    private final float u1, v1, u2, v2;
    private final int color;

    public DrawNode(JsonObject jsonObject) {
        slot = jsonObject.get("slot").getAsString();
        JsonArray srcArea = jsonObject.get("src_area").getAsJsonArray();
        x1 = srcArea.get(0).getAsInt(); y1 = srcArea.get(1).getAsInt();
        x2 = x1 + srcArea.get(2).getAsInt(); y2 = y1 + srcArea.get(3).getAsInt();
        JsonArray dstArea = jsonObject.get("dst_area").getAsJsonArray();
        u1 = dstArea.get(0).getAsFloat(); v1 = dstArea.get(1).getAsFloat();
        u2 = u1 + dstArea.get(2).getAsFloat(); v2 = v1 + dstArea.get(3).getAsFloat();
        color = jsonObject.has("color") ? RenderUtil.parseHexColor(jsonObject.get("color").getAsString()) : -1;
    }

    @Override
    public void draw(DisplayContent content, TrainClient train) {
        content.addQuad(slot, x1, y1, x2, y2, u1, v1, u2, v2, color);
    }

}
