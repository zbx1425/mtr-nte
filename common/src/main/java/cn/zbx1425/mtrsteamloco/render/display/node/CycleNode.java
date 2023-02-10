package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.TreeMap;

public class CycleNode extends DisplayNode {

    private final DisplayNode[] nodes;
    private final TreeMap<Float, Integer> timeOffsets = new TreeMap<>();
    private final float totalDuration;

    public CycleNode(DisplayContent content, ResourceManager resources, ResourceLocation basePath, JsonObject jsonObject) throws IOException {
        super(jsonObject);
        JsonArray nodeObjs = jsonObject.get("nodes").getAsJsonArray();
        nodes = new DisplayNode[nodeObjs.size()];
        float timeOffset = 0;
        for (int i = 0; i < nodeObjs.size(); i++) {
            JsonObject entryObj = nodeObjs.get(i).getAsJsonObject();
            nodes[i] = DisplayNodeFactory.parse(content, resources, basePath, entryObj.get("then").getAsJsonObject());
            timeOffset += entryObj.get("duration").getAsFloat();
            timeOffsets.put(timeOffset, i);
            nodes[i].parent = this;
        }
        totalDuration = timeOffset;
    }

    @Override
    public void tick(DisplayContent content, TrainClient train, boolean enabled) {
        super.tick(content, train, enabled);
        if (nodes.length == 0) return;
        int activeNode = timeOffsets.ceilingEntry((float)(RenderUtil.runningSeconds % totalDuration)).getValue();
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].tick(content, train, enabled && (i == activeNode));
        }
    }

}
