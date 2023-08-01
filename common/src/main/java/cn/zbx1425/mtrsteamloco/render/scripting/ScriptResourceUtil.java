package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.mtrsteamloco.render.font.FontTextureCache;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.awt.Font;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScriptResourceUtil {

    protected static List<Map.Entry<ResourceLocation, String>> scriptsToExecute;
    protected static ResourceLocation relativeBase;

    public static ResourceManager manager() {
        return MtrModelRegistryUtil.resourceManager;
    }

    public static ResourceLocation identifier(String textForm) {
        return new ResourceLocation(textForm);
    }

    public static ResourceLocation idRelative(String textForm) {
        return ResourceUtil.resolveRelativePath(relativeBase, textForm, null);
    }

    public static String readString(ResourceLocation identifier) {
        try {
            return ResourceUtil.readResource(manager(), identifier);
        } catch (IOException e) {
            return null;
        }
    }

    public static void includeScript(ResourceLocation identifier) throws IOException {
        scriptsToExecute.add(new AbstractMap.SimpleEntry<>(identifier, ResourceUtil.readResource(manager(), identifier)));
    }

    public static Font getBuiltinFont(boolean supportCjk, boolean serif) {
        return FontTextureCache.FONT_SERIF;
    }

    public static int getParticleTypeId(ResourceLocation identifier) {
        Optional<ParticleType<?>> particleType = BuiltInRegistries.PARTICLE_TYPE.getOptional(identifier);
        return particleType.map(BuiltInRegistries.PARTICLE_TYPE::getId).orElse(-1);
    }
}
