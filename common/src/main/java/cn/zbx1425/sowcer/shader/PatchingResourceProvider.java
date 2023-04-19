package cn.zbx1425.sowcer.shader;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.sowcer.ContextCapability;
import com.google.gson.JsonArray;
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

                JsonArray attribArray = data.get("attributes").getAsJsonArray();
                int dummyAttribCount = 6 - attribArray.size();
                for (int i = 0; i < dummyAttribCount; i++) {
                    attribArray.add("Dummy" + i);
                }
                attribArray.add("ModelMat");
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
                .replace("uniform mat4 ModelViewMat;", "uniform mat4 ModelViewMat;\nin mat4 ModelMat;")
        ;
        if (ContextCapability.isGL4ES) {
            contentParts[0] = contentParts[0].replace("ivec2", "vec2");
        }
        contentParts[1] = contentParts[1]
                .replaceAll("\\bPosition\\b", "(MODELVIEWMAT * ModelMat * vec4(Position, 1.0)).xyz")
                .replaceAll("\\bNormal\\b", "normalize(mat3(MODELVIEWMAT * ModelMat) * Normal)")
                .replace("ModelViewMat", "mat4(1.0)")
                .replace("MODELVIEWMAT", "ModelViewMat")
        ;
        return contentParts[0] + "void main" + contentParts[1];
    }
}
