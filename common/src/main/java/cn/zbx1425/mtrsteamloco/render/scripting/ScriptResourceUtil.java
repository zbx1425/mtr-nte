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
import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.Context;
import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.Scriptable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.AttributedString;
import java.util.*;
import java.util.List;

@SuppressWarnings("unused")
public class ScriptResourceUtil {

    protected static Context activeContext;
    protected static Scriptable activeScope;
    private static final Stack<ResourceLocation> scriptLocationStack = new Stack<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("MTR-NTE JS");

    public static void init(ResourceManager resourceManager) {
        hasNotoSansCjk = UtilitiesClient.hasResource(NOTO_SANS_CJK_LOCATION);
    }

    public static void executeScript(Context rhinoCtx, Scriptable scope, ResourceLocation scriptLocation, String script) {
        scriptLocationStack.push(scriptLocation);
        rhinoCtx.evaluateString(scope, script, scriptLocation.toString(), 1, null);
        scriptLocationStack.pop();
    }

    public static void includeScript(Object pathOrIdentifier) throws IOException {
        if (activeContext == null) throw new RuntimeException(
                "Cannot use include in functions, as by that time NTE no longer processes scripts."
        );
        ResourceLocation identifier;
        if (pathOrIdentifier instanceof ResourceLocation) {
            identifier = (ResourceLocation) pathOrIdentifier;
        } else {
            identifier = idRelative(pathOrIdentifier.toString());
        }
        executeScript(activeContext, activeScope, identifier, ResourceUtil.readResource(manager(), identifier));
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

    public static ResourceManager mgr() {
        return MtrModelRegistryUtil.resourceManager;
    }

    public static ResourceLocation identifier(String textForm) {
        return new ResourceLocation(textForm);
    }
    public static ResourceLocation id(String textForm) {
        return new ResourceLocation(textForm);
    }

    public static ResourceLocation idRelative(String textForm) {
        if (scriptLocationStack.empty()) throw new RuntimeException(
                "Cannot use idRelative in functions."
        );
        return ResourceUtil.resolveRelativePath(scriptLocationStack.peek(), textForm, null);
    }
    public static ResourceLocation idr(String textForm) {
        if (scriptLocationStack.empty()) throw new RuntimeException(
                "Cannot use idr in functions."
        );
        return ResourceUtil.resolveRelativePath(scriptLocationStack.peek(), textForm, null);
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
    private static Font NOTO_SANS_MAYBE_CJK;

    public static Font getSystemFont(String fontName) {
        ClientCacheAccessor clientCache = (ClientCacheAccessor) ClientData.DATA_CACHE;
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        switch (fontName) {
            case "Noto Sans" -> {
                if (NOTO_SANS_MAYBE_CJK == null) {
                    if (hasNotoSansCjk) {
                        try {
                            NOTO_SANS_MAYBE_CJK = Font.createFont(Font.TRUETYPE_FONT,
                                    Utilities.getInputStream(resourceManager.getResource(NOTO_SANS_CJK_LOCATION)));
                        } catch (Exception ex) {
                            Main.LOGGER.warn("Failed loading font", ex);
                        }
                    } else {
                        if (clientCache.getFont() == null) {
                            try {
                                clientCache.setFont(Font.createFont(Font.TRUETYPE_FONT,
                                        Utilities.getInputStream(resourceManager.getResource(NOTO_SANS_LOCATION))));
                            } catch (Exception ex) {
                                Main.LOGGER.warn("Failed loading font", ex);
                            }
                        }
                        NOTO_SANS_MAYBE_CJK = clientCache.getFont();
                    }
                }
                return NOTO_SANS_MAYBE_CJK;
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
                return clientCache.getFontCjk();
            }
            default -> {
                return new Font(fontName, Font.PLAIN, 1);
            }
        }
    }

    private static final FontRenderContext FONT_CONTEXT = new FontRenderContext(new AffineTransform(), true, false);

    public static FontRenderContext getFontRenderContext() {
        return FONT_CONTEXT;
    }

    public static AttributedString ensureStrFonts(String text, Font font) {
        AttributedString result = new AttributedString(text);
        if (text.isEmpty()) return result;
        result.addAttribute(TextAttribute.FONT, font, 0, text.length());
        for (int characterIndex = 0; characterIndex < text.length(); characterIndex++) {
            final char character = text.charAt(characterIndex);
            if (!font.canDisplay(character)) {
                Font defaultFont = null;
                for (final Font testFont : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
                    if (testFont.canDisplay(character)) {
                        defaultFont = testFont;
                        break;
                    }
                }
                final Font newFont = (defaultFont == null ? new Font(null) : defaultFont)
                        .deriveFont(font.getStyle(), font.getSize2D());
                result.addAttribute(TextAttribute.FONT, newFont, characterIndex, characterIndex + 1);
            }
        }
        return result;
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
