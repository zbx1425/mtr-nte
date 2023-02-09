package cn.zbx1425.mtrsteamloco.render.display.template;

import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.display.node.DisplayNode;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public interface DisplayTemplate {

    void tick(DisplayContent content, TrainClient train, DisplayNode caller);

}
