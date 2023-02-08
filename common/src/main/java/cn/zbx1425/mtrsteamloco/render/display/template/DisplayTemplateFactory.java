package cn.zbx1425.mtrsteamloco.render.display.template;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.Locale;

public class DisplayTemplateFactory {

    public static DisplayTemplate parse(ResourceManager resources, ResourceLocation basePath, JsonObject jsonObject) throws IOException {
        switch (jsonObject.get("class").getAsString().toLowerCase(Locale.ROOT)) {
            case "include":
                ResourceLocation source = ResourceUtil.resolveRelativePath(basePath, jsonObject.get("source").getAsString(), ".json");
                return parse(resources, source, Main.JSON_PARSER.parse(ResourceUtil.readResource(resources, source)).getAsJsonObject());

        }
        throw new IllegalArgumentException("Unknown class " + jsonObject.get("class").getAsString());
    }
}
