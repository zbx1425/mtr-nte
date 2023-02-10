package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class IncludeNode extends DisplayNode {

    private final DisplayNode node;

    public IncludeNode(DisplayContent content, ResourceManager resources, ResourceLocation basePath, JsonObject jsonObject) throws IOException {
        super(jsonObject);
        ResourceLocation source = ResourceUtil.resolveRelativePath(basePath, jsonObject.get("source").getAsString(), ".json");
        node = DisplayNodeFactory.parse(content, resources, source, Main.JSON_PARSER.parse(ResourceUtil.readResource(resources, source)).getAsJsonObject());
        node.parent = this;
    }

    @Override
    public void tick(DisplayContent content, TrainClient train, boolean enabled) {
        super.tick(content, train, enabled);
        node.tick(content, train, enabled);
    }

}
