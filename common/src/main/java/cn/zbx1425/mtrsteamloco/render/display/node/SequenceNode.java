package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class SequenceNode extends DisplayNode {

    private final DisplayNode[] nodes;

    public SequenceNode(DisplayContent content, ResourceManager resources, ResourceLocation basePath, JsonObject jsonObject) throws IOException {
        super(jsonObject);
        JsonArray nodeObjs = jsonObject.get("nodes").getAsJsonArray();
        nodes = new DisplayNode[nodeObjs.size()];
        for (int i = 0; i < nodeObjs.size(); i++) {
            nodes[i] = DisplayNodeFactory.parse(content, resources, basePath, nodeObjs.get(i).getAsJsonObject());
            nodes[i].parent = this;
        }
    }

    @Override
    public void tick(DisplayContent content, TrainClient train, boolean enabled) {
        super.tick(content, train, enabled);
        for (DisplayNode node : nodes) {
            node.tick(content, train, enabled);
        }
    }

}
