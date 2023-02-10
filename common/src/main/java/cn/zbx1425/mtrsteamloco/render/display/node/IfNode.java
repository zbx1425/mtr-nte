package cn.zbx1425.mtrsteamloco.render.display.node;


import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.font.VariableText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.netty.util.internal.StringUtil;
import mtr.data.TrainClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class IfNode implements DisplayNode {

    private final DisplayNode[] nodes;
    private final DisplayNode noneMatchNode;
    private final VariableText[] criteria;

    public IfNode(DisplayContent content, ResourceManager resources, ResourceLocation basePath, JsonObject jsonObject) throws IOException {
        JsonArray nodeObjs = jsonObject.get("nodes").getAsJsonArray();
        nodes = new DisplayNode[nodeObjs.size()];
        criteria = new VariableText[nodeObjs.size()];
        DisplayNode noneMatchNode = null;
        for (int i = 0; i < nodeObjs.size(); i++) {
            JsonObject entryObj = nodeObjs.get(i).getAsJsonObject();
            if (entryObj.get("then").isJsonNull()) {
                nodes[i] = null;
            } else {
                nodes[i] = DisplayNodeFactory.parse(content, resources, basePath, entryObj.get("then").getAsJsonObject());
            }
            if (StringUtil.isNullOrEmpty(entryObj.get("when").getAsString())) {
                noneMatchNode = nodes[i];
                criteria[i] = null;
            } else {
                criteria[i] = new VariableText(entryObj.get("when").getAsString().trim());
            }
        }
        this.noneMatchNode = noneMatchNode;
    }

    @Override
    public void draw(DisplayContent content, TrainClient train) {
        for (int i = nodes.length - 1; i > 0; i--) {
            if (criteria[i] != null && criteria[i].getTargetBoolean(content, train)) {
                if (nodes[i] != null) nodes[i].draw(content, train);
                return;
            }
        }
        if (noneMatchNode != null) noneMatchNode.draw(content, train);
    }
}
