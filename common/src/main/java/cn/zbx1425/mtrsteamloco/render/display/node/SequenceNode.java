package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class SequenceNode implements DisplayNode {

    private final DisplayNode[] nodes;

    public SequenceNode(DisplayContent content, ResourceManager resources, ResourceLocation basePath, JsonObject jsonObject) throws IOException {
        JsonArray nodeObjs = jsonObject.get("nodes").getAsJsonArray();
        nodes = new DisplayNode[nodeObjs.size()];
        for (int i = 0; i < nodeObjs.size(); i++) {
            nodes[i] = DisplayNodeFactory.parse(content, resources, basePath, nodeObjs.get(i).getAsJsonObject());
        }
    }

    @Override
    public void draw(DisplayContent content, TrainClient train) {
        for (DisplayNode node : nodes) {
            node.draw(content, train);
        }
    }

}
