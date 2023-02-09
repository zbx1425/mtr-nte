package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.display.FontTexture;
import cn.zbx1425.mtrsteamloco.render.display.FontTextureCache;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;

public class DrawFreeTextNode implements DisplayNode {

    private final String slot;
    private final VariableText target;
    private final float u1, v1, u2, v2;

    public DrawFreeTextNode(JsonObject jsonObject) {
        slot = jsonObject.get("slot").getAsString();
        target = new VariableText(jsonObject.get("text").getAsString());
        JsonArray dstArea = jsonObject.get("dst_area").getAsJsonArray();
        u1 = dstArea.get(0).getAsFloat(); v1 = dstArea.get(1).getAsFloat();
        u2 = u1 + dstArea.get(2).getAsFloat(); v2 = v1 + dstArea.get(3).getAsFloat();
    }

    @Override
    public void tick(DisplayContent content, TrainClient train) {
        FontTexture fontTexture = FontTextureCache.getTexture(target.getTargetString(train));
        if (fontTexture == null) return;
        content.addTextQuad(slot, fontTexture.resourceLocation, 0, 0, content.imgWidth, content.imgHeight, u1, v1, u2, v2, 0xFF000000);
    }

}
