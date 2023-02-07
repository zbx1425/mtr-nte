package cn.zbx1425.mtrsteamloco.render.display.node;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.Locale;

public class DisplayNodeFactory {

    public static DisplayNode parse(ResourceManager resourceManager, ResourceLocation basePath, JsonObject jsonObject) throws IOException {
        switch (jsonObject.get("class").getAsString().toLowerCase(Locale.ROOT)) {
            case "texture":
                return new TextureNode(jsonObject);
            case "texture_h":
                return new TextureHNode(jsonObject);
            case "sequence":
                return new SequenceNode(resourceManager, basePath, jsonObject);
            case "include":
                return new IncludeNode(resourceManager, basePath, jsonObject);
        }
        throw new IllegalArgumentException("Unknown class " + jsonObject.get("class").getAsString());
    }
}
