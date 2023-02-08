package cn.zbx1425.mtrsteamloco.render.display.template;

import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.display.node.DisplayNode;
import cn.zbx1425.mtrsteamloco.render.display.node.MultipartNameUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;

import java.util.Locale;

public class LineMapNode implements DisplayNode {

    public final String slot;
    private final LineMapTemplate template;
    private final String target;
    public final float u1, v1, u2, v2;
    public final boolean towardsRight;

    public LineMapNode(LineMapTemplate template, JsonObject jsonObject) {
        this.template = template;
        slot = jsonObject.get("slot").getAsString();
        target = jsonObject.get("target").getAsString().toLowerCase(Locale.ROOT);

        JsonArray dstArea = jsonObject.get("dst_area").getAsJsonArray();
        u1 = dstArea.get(0).getAsFloat(); v1 = dstArea.get(1).getAsFloat();
        u2 = u1 + dstArea.get(2).getAsFloat(); v2 = v1 + dstArea.get(3).getAsFloat();

        towardsRight = jsonObject.get("direction").getAsString().equals("right");
    }

    @Override
    public void tick(DisplayContent content, TrainClient train) {
        template.tick(content, train, this);
    }


    public String getTargetName(TrainClient train) {
        return MultipartNameUtil.getTargetName(train, target);
    }

}
