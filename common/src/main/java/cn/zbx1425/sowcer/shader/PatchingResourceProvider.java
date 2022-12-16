package cn.zbx1425.sowcer.shader;

import cn.zbx1425.mtrsteamloco.Main;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
#if MC_VERSION < "11900"
import net.minecraft.server.packs.resources.SimpleResource;
#endif
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
#if MC_VERSION >= "11900"
    public Optional<Resource> getResource(ResourceLocation resourceLocation) {
#else
    public Resource getResource(ResourceLocation resourceLocation) throws IOException {
#endif
        try {
            if (resourceLocation.getPath().contains("_modelmat"))
                resourceLocation = new ResourceLocation(resourceLocation.getNamespace(),
                        resourceLocation.getPath().replace("_modelmat", ""));

            InputStream srcInputStream;
#if MC_VERSION >= "11900"
            Optional<Resource> srcResource = source.getResource(resourceLocation);
            if (srcResource.isEmpty()) {
                return Optional.empty();
            } else {
                srcInputStream = srcResource.get().open();
            }
#else
            Resource srcResource = source.getResource(resourceLocation);
            srcInputStream = srcResource.getInputStream();
#endif
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
#if MC_VERSION >= "11903"
            return Optional.of(new Resource(srcResource.get().source(), () -> newContentStream));
#elif MC_VERSION >= "11900"
            return Optional.of(new Resource(srcResource.get().sourcePackId(), () -> newContentStream));
#else
            return new SimpleResource(srcResource.getSourceName(), resourceLocation, newContentStream, null);
#endif
        } catch (IOException ignored) {
#if MC_VERSION >= "11900"
            return Optional.empty();
#else
            throw ignored;
#endif
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
                .replaceAll("\\bNormal\\b", "normalize(mat3(MODELVIEWMAT * ModelMat) * Normal)")
                .replace("ModelViewMat", "mat4(1.0)")
                .replace("MODELVIEWMAT", "ModelViewMat")
                .replace("overlayColor = texelFetch(Sampler1, UV1, 0);", "overlayColor = vec4(1.0);")
        ;
        return contentParts[0] + "void main" + contentParts[1];
    }
}
