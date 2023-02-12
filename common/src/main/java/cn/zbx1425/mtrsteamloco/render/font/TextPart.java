package cn.zbx1425.mtrsteamloco.render.font;

import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.display.node.DisplayNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;

public class TextPart {

    public final float sizeV;
    public final float marginL, marginR;
    public final VariableText text;
    public final int color;

    public TextPart(JsonObject jsonObject) {
        text = new VariableText(jsonObject.get("text").getAsString());
        sizeV = jsonObject.get("size_v").getAsFloat();

        if (jsonObject.has("margin")) {
            JsonArray marginArray = jsonObject.get("margin").getAsJsonArray();
            marginL = marginArray.get(0).getAsFloat();
            marginR = marginArray.get(1).getAsFloat();
        } else {
            marginL = marginR = 0;
        }
        if (jsonObject.has("color")) {
            color = RenderUtil.parseHexColor(jsonObject.get("color").getAsString());
        } else {
            color = 0xFF000000;
        }
    }

    public int getTextHash(DisplayContent context, TrainClient train) {
        return text.getTargetString(context, train).hashCode();
    }
}
