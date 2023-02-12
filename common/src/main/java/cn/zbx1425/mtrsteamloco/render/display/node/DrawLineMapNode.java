package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.font.VariableText;
import cn.zbx1425.mtrsteamloco.render.display.template.LineMapTemplate;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;

public class DrawLineMapNode implements DisplayNode {

    public final String slot;
    private final LineMapTemplate template;
    public final VariableText target;
    public final float u1, v1, u2, v2;
    public final boolean towardsRight;
    public final int color;

    public DrawLineMapNode(LineMapTemplate template, JsonObject jsonObject) {
        this.template = template;
        slot = jsonObject.get("slot").getAsString();
        target = new VariableText(jsonObject.get("target").getAsString());

        JsonArray dstArea = jsonObject.get("dst_area").getAsJsonArray();
        u1 = dstArea.get(0).getAsFloat(); v1 = dstArea.get(1).getAsFloat();
        u2 = u1 + dstArea.get(2).getAsFloat(); v2 = v1 + dstArea.get(3).getAsFloat();

        towardsRight = jsonObject.get("direction").getAsString().equals("right");
        color = jsonObject.has("color") ? RenderUtil.parseHexColor(jsonObject.get("color").getAsString()) : -1;
    }

    @Override
    public void draw(DisplayContent content, TrainClient train) {
        template.tick(content, train, this);
    }

}
