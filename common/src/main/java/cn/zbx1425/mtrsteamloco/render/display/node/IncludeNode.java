package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class IncludeNode implements DisplayNode {

    private final DisplayNode node;

    public IncludeNode(DisplayContent content, ResourceManager resources, ResourceLocation basePath, JsonObject jsonObject) throws IOException {
        ResourceLocation source = ResourceUtil.resolveRelativePath(basePath, jsonObject.get("source").getAsString(), ".json");
        node = DisplayNodeFactory.parse(content, resources, source, Main.JSON_PARSER.parse(ResourceUtil.readResource(resources, source)).getAsJsonObject());
    }

    @Override
    public void draw(DisplayContent content, TrainClient train) {
        node.draw(content, train);
    }

}
