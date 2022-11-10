package cn.zbx1425.sowcer.shader;

import cn.zbx1425.mtrsteamloco.Main;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

public class PatchingResourceProvider implements ResourceProvider {

    private final ResourceProvider source;

    public PatchingResourceProvider(ResourceProvider source) {
        this.source = source;
    }

    @Override
    public Optional<Resource> getResource(ResourceLocation resourceLocation) {
        try {
            if (resourceLocation.getPath().contains("_modelmat"))
                resourceLocation = new ResourceLocation(resourceLocation.getNamespace(),
                        resourceLocation.getPath().replace("_modelmat", ""));
            Optional<Resource> srcResource = source.getResource(resourceLocation);

            InputStream srcInputStream;
            if (srcResource.isEmpty()) {
                return Optional.empty();
            } else {
                srcInputStream = srcResource.get().open();
            }
            String returningContent = "";

            if (resourceLocation.getPath().endsWith(".json")) {
                String srcContent = IOUtils.toString(srcInputStream, StandardCharsets.UTF_8);
                JsonObject data = Main.JSON_PARSER.parse(srcContent).getAsJsonObject();
                data.addProperty("vertex", data.get("vertex").getAsString() + "_modelmat");
                data.get("attributes").getAsJsonArray().add("ModelMat");
                // data.get("attributes").getAsJsonArray().remove(new JsonPrimitive("UV1"));
                JsonElement sampler1Object = null;
                for (JsonElement elem : data.get("samplers").getAsJsonArray()) {
                    if (Objects.equals(elem.getAsJsonObject().get("name").getAsString(), "Sampler1"))
                        sampler1Object = elem;
                }
                if (sampler1Object != null) data.get("samplers").getAsJsonArray().remove(sampler1Object);
                returningContent = data.toString();
                srcInputStream.close();
            } else if (resourceLocation.getPath().endsWith(".vsh")) {
                String srcContent = IOUtils.toString(srcInputStream, StandardCharsets.UTF_8);
                returningContent = patchVertexShaderSource(srcContent);
                srcInputStream.close();
            } else {
                return srcResource;
            }

            final InputStream newContentStream = new ByteArrayInputStream(returningContent.getBytes(StandardCharsets.UTF_8));
            return Optional.of(new Resource(srcResource.get().sourcePackId(), () -> newContentStream));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    public static String patchVertexShaderSource(String srcContent) {
        String[] contentParts = srcContent.split("void main");
        contentParts[0] = contentParts[0]
                .replace("#version 150", "#version 330 core")
                .replace("uniform mat4 ModelViewMat;", "uniform mat4 ModelViewMat;\nlayout(location = 6) in mat4 ModelMat;")
        // .replace("uniform sampler2D Sampler1;", "")
        ;
        contentParts[1] = contentParts[1]
                .replaceAll("\\bPosition\\b", "(MODELVIEWMAT * ModelMat * vec4(Position, 1.0)).xyz")
                .replaceAll("\\bNormal\\b", "normalize(mat3(ModelMat) * Normal)")
                .replace("ModelViewMat", "mat4(1.0)")
                .replace("MODELVIEWMAT", "ModelViewMat")
                .replace("overlayColor = texelFetch(Sampler1, UV1, 0);", "overlayColor = vec4(1.0);")
        ;
        return contentParts[0] + "void main" + contentParts[1];
    }
}
