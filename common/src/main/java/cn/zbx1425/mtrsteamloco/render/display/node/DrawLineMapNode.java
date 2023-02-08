package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.display.template.LineMapTemplate;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;

import java.util.Locale;

public class DrawLineMapNode implements DisplayNode {

    public final String slot;
    private final String templateKey;
    private final String target;
    public final float u1, v1, u2, v2;
    public final boolean towardsRight;

    public DrawLineMapNode(JsonObject jsonObject) {
        slot = jsonObject.get("slot").getAsString();
        templateKey = jsonObject.get("template").getAsString();
        target = jsonObject.get("target").getAsString().toLowerCase(Locale.ROOT);

        JsonArray dstArea = jsonObject.get("dst_area").getAsJsonArray();
        u1 = dstArea.get(0).getAsFloat(); v1 = dstArea.get(1).getAsFloat();
        u2 = u1 + dstArea.get(2).getAsFloat(); v2 = v1 + dstArea.get(3).getAsFloat();

        towardsRight = jsonObject.get("direction").getAsString().equals("right");
    }

    @Override
    public void tick(DisplayContent content, TrainClient train) {
        LineMapTemplate template = (LineMapTemplate)content.getTemplate(templateKey);
        template.tick(content, train, this);
    }

    public String getTargetName(TrainClient train) {
        return MultipartNameUtil.getTargetName(train, target);
    }

}
