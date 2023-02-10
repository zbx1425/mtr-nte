package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.display.template.LineMapTemplate;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.Locale;

public class DisplayNodeFactory {

    public static DisplayNode parse(DisplayContent content, ResourceManager resourceManager, ResourceLocation basePath, JsonObject jsonObject) throws IOException {
        switch (jsonObject.get("class").getAsString().toLowerCase(Locale.ROOT)) {
            case "draw":
                return new DrawNode(jsonObject);
            case "draw_line_map":
                return new DrawLineMapNode((LineMapTemplate)content.getTemplate(jsonObject.get("template").getAsString()), jsonObject);
            case "draw_sheet_text":

            case "draw_free_text":
                return new DrawFreeTextNode(jsonObject);
            case "sequence":
                return new SequenceNode(content, resourceManager, basePath, jsonObject);
            case "include":
                return new IncludeNode(content, resourceManager, basePath, jsonObject);
            case "cycle":
                return new CycleNode(content, resourceManager, basePath, jsonObject);
            case "switch":
                return new SwitchNode(content, resourceManager, basePath, jsonObject);
            case "if":
                return new IfNode(content, resourceManager, basePath, jsonObject);
        }
        throw new IllegalArgumentException("Unknown class " + jsonObject.get("class").getAsString());
    }
}
