package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.mtrsteamloco.BuildConfig;
import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.mixin.ClientCacheAccessor;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.mtrsteamloco.render.scripting.util.GraphicsTexture;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mtr.client.ClientData;
import mtr.mappings.Utilities;
import mtr.mappings.UtilitiesClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
#if MC_VERSION >= "11903"
import net.minecraft.core.registries.BuiltInRegistries;
#else
import net.minecraft.core.Registry;
#endif
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

@SuppressWarnings("unused")
public class ScriptResourceUtil {

    protected static List<Map.Entry<ResourceLocation, String>> scriptsToExecute;
    protected static ResourceLocation relativeBase;
    private static final Logger LOGGER = LoggerFactory.getLogger("MTR-NTE JS");

    public static void init(ResourceManager resourceManager) {
        hasNotoSansCjk = UtilitiesClient.hasResource(NOTO_SANS_CJK_LOCATION);
    }

    public static void includeScript(Object pathOrIdentifier) throws IOException {
        ResourceLocation identifier;
        if (pathOrIdentifier instanceof ResourceLocation) {
            identifier = (ResourceLocation) pathOrIdentifier;
        } else {
            identifier = idRelative(pathOrIdentifier.toString());
        }
        scriptsToExecute.add(new AbstractMap.SimpleEntry<>(identifier, ResourceUtil.readResource(manager(), identifier)));
    }

    public static void print(Object... objects) {
        StringBuilder sb = new StringBuilder();
        for (Object object : objects) {
            sb.append(object.toString());
            sb.append(" ");
        }
        Main.LOGGER.info(sb.toString().trim());
    }

    public static ResourceManager manager() {
        return MtrModelRegistryUtil.resourceManager;
    }

    public static ResourceLocation identifier(String textForm) {
        return new ResourceLocation(textForm);
    }

    public static ResourceLocation idRelative(String textForm) {
        if (relativeBase == null) throw new RuntimeException("Cannot use idRelative in functions.");
        return ResourceUtil.resolveRelativePath(relativeBase, textForm, null);
    }

    public static InputStream readStream(ResourceLocation identifier) throws IOException {
        final List<Resource> resources = UtilitiesClient.getResources(manager(), identifier);
        if (resources.isEmpty()) throw new FileNotFoundException(identifier.toString());
        return Utilities.getInputStream(resources.get(0));
    }

    public static String readString(ResourceLocation identifier) {
        try {
            return ResourceUtil.readResource(manager(), identifier);
        } catch (IOException e) {
            return null;
        }
    }

    private static final ResourceLocation NOTO_SANS_CJK_LOCATION = new ResourceLocation(mtr.MTR.MOD_ID, "font/noto-sans-cjk-tc-medium.otf");
    private static final ResourceLocation NOTO_SANS_LOCATION = new ResourceLocation(mtr.MTR.MOD_ID, "font/noto-sans-semibold.ttf");
    private static final ResourceLocation NOTO_SERIF_LOCATION = new ResourceLocation(mtr.MTR.MOD_ID, "font/noto-serif-cjk-tc-semibold.ttf");
    private static boolean hasNotoSansCjk = false;

    public static Font getSystemFont(String fontName) {
        ClientCacheAccessor clientCache = (ClientCacheAccessor) ClientData.DATA_CACHE;
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        switch (fontName) {
            case "Noto Sans" -> {
                if (clientCache.getFont() == null || (hasNotoSansCjk && !clientCache.getFont().canDisplay('草'))) {
                    try {
                        if (hasNotoSansCjk) {
                            clientCache.setFont(Font.createFont(Font.TRUETYPE_FONT,
                                    Utilities.getInputStream(resourceManager.getResource(NOTO_SANS_CJK_LOCATION))));
                        } else {
                            clientCache.setFont(Font.createFont(Font.TRUETYPE_FONT,
                                    Utilities.getInputStream(resourceManager.getResource(NOTO_SANS_LOCATION))));
                        }
                    } catch (Exception ex) {
                        Main.LOGGER.warn("Failed loading font", ex);
                    }
                }
                return clientCache.getFont();
            }
            case "Noto Serif" -> {
                if (clientCache.getFontCjk() == null) {
                    try {
                        clientCache.setFontCjk(Font.createFont(Font.TRUETYPE_FONT,
                                Utilities.getInputStream(resourceManager.getResource(NOTO_SERIF_LOCATION))));
                    } catch (Exception ex) {
                        Main.LOGGER.warn("Failed loading font", ex);
                    }
                }
                return clientCache.getFont();
            }
            default -> {
                return Font.getFont(fontName);
            }
        }
    }

    private static final FontRenderContext FONT_CONTEXT = new FontRenderContext(new AffineTransform(), true, false);

    public static FontRenderContext getFontRenderContext() {
        return FONT_CONTEXT;
    }

    public static BufferedImage readBufferedImage(ResourceLocation identifier) throws IOException {
        try (InputStream is = readStream(identifier)) {
            return GraphicsTexture.createArgbBufferedImage(ImageIO.read(is));
        }
    }

    public static Font readFont(ResourceLocation identifier) throws IOException, FontFormatException {
        try (InputStream is = readStream(identifier)) {
            return Font.createFont(Font.TRUETYPE_FONT, is);
        }
    }

    public static int getParticleTypeId(ResourceLocation identifier) {
#if MC_VERSION >= "11903"
        Optional<ParticleType<?>> particleType = BuiltInRegistries.PARTICLE_TYPE.getOptional(identifier);
        return particleType.map(BuiltInRegistries.PARTICLE_TYPE::getId).orElse(-1);
#else
        Optional<ParticleType<?>> particleType = Registry.PARTICLE_TYPE.getOptional(identifier);
        return particleType.map(Registry.PARTICLE_TYPE::getId).orElse(-1);
#endif
    }

    public static CompoundTag parseNbtString(String text) throws CommandSyntaxException {
        return TagParser.parseTag(text);
    }

    public static String getMTRVersion() {
        String mtrModVersion;
        try {
            mtrModVersion = (String) mtr.Keys.class.getField("MOD_VERSION").get(null);
        } catch (ReflectiveOperationException ignored) {
            mtrModVersion = "0.0.0-0.0.0";
        }
        return mtrModVersion;
    }

    public static String getNTEVersion() {
        return BuildConfig.MOD_VERSION;
    }

    public static int getNTEVersionInt() {
        int[] components = Arrays.stream(BuildConfig.MOD_VERSION.split("\\+", 2)[0].split("\\.", 3))
                .mapToInt(Integer::parseInt).toArray();
        return components[0] * 10000 + components[1] * 100 + components[2];
    }

    public static int getNTEProtoVersion() {
        return BuildConfig.MOD_PROTOCOL_VERSION;
    }

}
