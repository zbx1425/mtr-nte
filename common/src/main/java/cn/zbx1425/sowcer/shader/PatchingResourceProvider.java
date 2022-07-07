package cn.zbx1425.sowcer.shader;

import cn.zbx1425.mtrsteamloco.Main;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.server.packs.resources.SimpleResource;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PatchingResourceProvider implements ResourceProvider {

    private final ResourceProvider source;

    public PatchingResourceProvider(ResourceProvider source) {
        this.source = source;
    }

    @Override
    public Resource getResource(ResourceLocation resourceLocation) throws IOException {
        Main.LOGGER.info("getResource called on " + resourceLocation);
        if (resourceLocation.getPath().contains("_modelmat"))
            resourceLocation = new ResourceLocation(resourceLocation.getNamespace(),
                    resourceLocation.getPath().replace("_modelmat", ""));
        Resource srcResource = source.getResource(resourceLocation);
        String returningContent = "";

        if (resourceLocation.getPath().endsWith(".json")) {
            String srcContent = IOUtils.toString(srcResource.getInputStream(), StandardCharsets.UTF_8);
            JsonObject data = JsonParser.parseString(srcContent).getAsJsonObject();
            data.addProperty("vertex", data.get("vertex").getAsString() + "_modelmat");
            data.get("attributes").getAsJsonArray().add("ModelMat");
            data.get("atrributes").getAsJsonArray().remove(new JsonPrimitive("UV1"));
            returningContent = data.toString();
        } else if (resourceLocation.getPath().endsWith(".vsh")) {
            String srcContent = IOUtils.toString(srcResource.getInputStream(), StandardCharsets.UTF_8);
            returningContent = srcContent
                // Add model matrix as vertex attribute
                .replace("uniform mat4 ModelViewMat;", "uniform mat4 ModelViewMat;\nin mat4 ModelMat;")
                .replace("gl_Position = ProjMat * ModelViewMat", "gl_Position = ProjMat * ModelViewMat * ModelMat")
                .replace("IViewRotMat * Position", "IViewRotMat * (ModelMat * vec4(Position, 1.0)).xyz")
                .replace("Light1_Direction, Normal", "Light1_Direction, normalize(mat3(ModelMat) * Normal)")
                .replace("vec4(Normal, 0.0)", "vec4(normalize(mat3(ModelMat) * Normal), 0.0)")

                // Remove overlay process
                .replace("uniform sampler2D Sampler1;", "")
                .replace("overlayColor = texelFetch(Sampler1, UV1, 0);", "overlayColor = vec4(1.0, 1.0, 1.0, 1.0);");
        } else {
            return srcResource;
        }

        InputStream newContentStream = new ByteArrayInputStream(returningContent.getBytes(StandardCharsets.UTF_8));
        return new SimpleResource(srcResource.getSourceName(), resourceLocation, newContentStream, null);
    }
}
