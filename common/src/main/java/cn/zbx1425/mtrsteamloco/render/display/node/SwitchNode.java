package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.font.VariableText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.HashMap;

public class SwitchNode implements DisplayNode {

    private final DisplayNode noneMatchNode;
    private final VariableText target;
    private final HashMap<String, DisplayNode> nodes;

    public SwitchNode(DisplayContent content, ResourceManager resources, ResourceLocation basePath, JsonObject jsonObject) throws IOException {
        JsonArray nodeObjs = jsonObject.get("nodes").getAsJsonArray();
        target = new VariableText(jsonObject.get("target").getAsString());
        nodes = new HashMap<>(nodeObjs.size());
        DisplayNode noneMatchNode = null;
        for (int i = 0; i < nodeObjs.size(); i++) {
            JsonObject entryObj = nodeObjs.get(i).getAsJsonObject();
            DisplayNode currentNode;
            if (entryObj.get("then").isJsonNull()) {
                currentNode = null;
            } else {
                currentNode = DisplayNodeFactory.parse(content, resources, basePath, entryObj.get("then").getAsJsonObject());
            }
            if (entryObj.get("when").isJsonNull()) {
                noneMatchNode = currentNode;
            } else if (entryObj.get("when").isJsonArray()) {
                JsonArray matchArray = entryObj.get("when").getAsJsonArray();
                for (int j = 0; j < matchArray.size(); j++) {
                    nodes.put(matchArray.get(j).getAsString().trim(), currentNode);
                }
            } else {
                nodes.put(entryObj.get("when").getAsString().trim(), currentNode);
            }
        }
        this.noneMatchNode = noneMatchNode;
    }

    @Override
    public void draw(DisplayContent content, TrainClient train) {
        String toMatch = target.getTargetString(content, train);
        if (nodes.containsKey(toMatch)) {
            DisplayNode nodeToShow = nodes.get(toMatch);
            if (nodeToShow != null) nodeToShow.draw(content, train);
        } else {
            if (noneMatchNode != null) noneMatchNode.draw(content, train);
        }
    }
}
