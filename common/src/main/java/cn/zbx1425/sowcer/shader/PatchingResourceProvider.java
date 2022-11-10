package cn.zbx1425.sowcer.shader;

import cn.zbx1425.mtrsteamloco.Main;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class PatchingResourceProvider {

    public static String patchVertexShaderSource(String srcContent) {
        String[] contentParts = srcContent.split("void main");
        if (!contentParts[1].contains("fog_distance")) {
            return srcContent;
        }
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
