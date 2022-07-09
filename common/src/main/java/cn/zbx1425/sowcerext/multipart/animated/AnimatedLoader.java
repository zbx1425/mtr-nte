package cn.zbx1425.sowcerext.multipart.animated;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.loader.CsvModelLoader;
import cn.zbx1425.sowcerext.model.loader.ObjModelLoader;
import cn.zbx1425.sowcerext.multipart.MultipartContainer;
import cn.zbx1425.sowcerext.multipart.animated.script.FunctionScript;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Locale;

public class AnimatedLoader {

    public static MultipartContainer loadModel(ResourceManager resourceManager, ResourceLocation objLocation) throws IOException {
        AnimatedLoader loader = new AnimatedLoader();
        loader.load(resourceManager, objLocation, new Vector3f(0, 0, 0));
        loader.buildingContainer.parts.add(new StaticPart(VertArrays.createAll(loader.staticModel.upload(DEFAULT_MAPPING), DEFAULT_MAPPING, null)));
        return loader.buildingContainer;
    }

    private final MultipartContainer buildingContainer = new MultipartContainer();
    private final RawModel staticModel = new RawModel();
    private static final VertAttrMapping DEFAULT_MAPPING = new VertAttrMapping.Builder()
            .set(VertAttrType.POSITION, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.COLOR, VertAttrSrc.MATERIAL)
            .set(VertAttrType.UV_TEXTURE, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.UV_LIGHTMAP, VertAttrSrc.ENQUEUE)
            .set(VertAttrType.NORMAL, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.MATRIX_MODEL, VertAttrSrc.ENQUEUE)
            .build();

    private void load(ResourceManager resourceManager, ResourceLocation objLocation, Vector3f translation) throws IOException {
        String rawModelData = ResourceUtil.readResource(resourceManager, objLocation);
        String[] rawModelLines = rawModelData.split("[\\r\\n]+");
        String section = "";

        AnimatedPart buildingPart = new AnimatedPart();
        Vector3f includeTranslation = new Vector3f(0, 0, 0);

        for (String line : rawModelLines) {
            if (line.contains(";")) line = line.split(";", 2)[0];
            final String trimLine = line.trim();
            if (StringUtils.isEmpty(trimLine)) continue;

            if (trimLine.startsWith("[") && trimLine.endsWith("]")) {
                if (section.equals("object")) {
                    if (buildingPart.isStatic()) {
                        buildingPart.bakeToStaticModel(staticModel, translation);
                    } else if (buildingPart.unbakedStates != null && buildingPart.unbakedStates.length > 0) {
                        buildingPart.bake(DEFAULT_MAPPING, translation);
                        buildingContainer.parts.add(buildingPart);
                    }
                    buildingPart = new AnimatedPart();
                }
                section = trimLine.substring(1, trimLine.length() - 1).trim().replace(" ", "").toLowerCase();
                if (section.equals("include")) {
                    includeTranslation = new Vector3f(0, 0, 0);
                }
            } else {
                if (section.equals("object")) {
                    if (!trimLine.contains("=")) continue;
                    final String[] tokens = trimLine.split("=", 2);
                    if (tokens.length != 2) continue;

                    final String key = tokens[0].trim().toLowerCase();
                    final String value = tokens[1].trim().toLowerCase();
                    if (StringUtils.isEmpty(value)) continue;
                    switch (key) {
                        case "position":
                            buildingPart.externTranslation = parseVectorValue(value);
                            break;
                        case "states":
                            String[] states = value.split(",");
                            buildingPart.unbakedStates = new RawModel[states.length];
                            for (int i = 0; i < states.length; ++i) {
                                String crntState = states[i].trim().toLowerCase(Locale.ROOT);
                                if (StringUtils.isEmpty(crntState)) continue;
                                ResourceLocation crntStateLocation = ResourceUtil.resolveRelativePath(objLocation, crntState, null);
                                String crntStatExt = FilenameUtils.getExtension(crntState);
                                if (crntStatExt.equals("obj")) {
                                    buildingPart.unbakedStates[i] = ObjModelLoader.loadModel(resourceManager, crntStateLocation);
                                } else if (crntStatExt.equals("csv")) {
                                    buildingPart.unbakedStates[i] = CsvModelLoader.loadModel(resourceManager, crntStateLocation);
                                } else {
                                    Main.LOGGER.warn("Unsupported model format in ANIMATED: " + crntState);
                                }
                            }
                            break;
                        case "statefunction":
                            buildingPart.stateFunction = new FunctionScript(value);
                            break;
                        case "translatexdirection":
                            buildingPart.translateXDirection = parseVectorValue(value);
                            break;
                        case "translateydirection":
                            buildingPart.translateYDirection = parseVectorValue(value);
                            break;
                        case "translatezdirection":
                            buildingPart.translateZDirection = parseVectorValue(value);
                            break;
                        case "translatexfunction":
                            buildingPart.translateXFunction = new FunctionScript(value);
                            break;
                        case "translateyfunction":
                            buildingPart.translateYFunction = new FunctionScript(value);
                            break;
                        case "translatezfunction":
                            buildingPart.translateZFunction = new FunctionScript(value);
                            break;
                        case "rotatexdirection":
                            buildingPart.rotateXDirection = parseVectorValue(value);
                            break;
                        case "rotateydirection":
                            buildingPart.rotateYDirection = parseVectorValue(value);
                            break;
                        case "rotatezdirection":
                            buildingPart.rotateZDirection = parseVectorValue(value);
                            break;
                        case "rotatexfunction":
                            buildingPart.rotateXFunction = new FunctionScript(value);
                            break;
                        case "rotateyfunction":
                            buildingPart.rotateYFunction = new FunctionScript(value);
                            break;
                        case "rotatezfunction":
                            buildingPart.rotateZFunction = new FunctionScript(value);
                            break;
                        case "rotatexdamping":
                        case "rotateydamping":
                        case "rotatezdamping":
                            Main.LOGGER.warn("ANIMATED command that isn't currently supported: " + key);
                            break;
                        case "textureshiftxdirection":
                        case "textureshiftydirection":
                        case "textureshiftxfunction":
                        case "textureshiftyfunction":
                        case "trackfollowerfunction":
                        case "textureoverride":
                            Main.LOGGER.warn("ANIMATED command that cannot and will not be supported: " + key);
                            break;
                        case "refreshrate":
                            buildingPart.refreshRateMillis = (int)(Float.parseFloat(value) * 1000F);
                            break;
                        default:
                            Main.LOGGER.warn("Unknown ANIMATED command in Object: " + tokens[0]);
                            break;
                    }
                } else if (section.equals("include")) {
                    if (trimLine.contains("=")) {
                        final String[] tokens = trimLine.split("=");
                        if (tokens.length != 2) continue;

                        final String key = tokens[0].trim().toLowerCase();
                        final String value = tokens[1].trim().toLowerCase().replace("\\", "/");
                        if (StringUtils.isEmpty(value)) continue;

                        switch (key) {
                            case "position":
                                includeTranslation = parseVectorValue(value);
                                break;
                            default:
                                Main.LOGGER.warn("Unknown ANIMATED commandin Include: " + tokens[0]);
                                break;
                        }
                    } else {
                        ResourceLocation crntStateLocation = ResourceUtil.resolveRelativePath(objLocation, trimLine, null);
                        String crntStatExt = FilenameUtils.getExtension(trimLine);
                        if (crntStatExt.equals("obj")) {
                            RawModel model = ObjModelLoader.loadModel(resourceManager, crntStateLocation);
                            model.applyTranslation(translation.x() + includeTranslation.x(), translation.y() + includeTranslation.y(),
                                    translation.z() + includeTranslation.z());
                            staticModel.append(model);
                        } else if (crntStatExt.equals("csv")) {
                            RawModel model = CsvModelLoader.loadModel(resourceManager, crntStateLocation);
                            model.applyTranslation(translation.x() + includeTranslation.x(), translation.y() + includeTranslation.y(),
                                    translation.z() + includeTranslation.z());
                            staticModel.append(model);
                        } else if (crntStatExt.equals("animated")) {
                            load(resourceManager, crntStateLocation, new Vector3f(translation.x() + includeTranslation.x(),
                                    translation.y() + includeTranslation.y(), translation.z() + includeTranslation.z()));
                        } else {
                            Main.LOGGER.warn("Unsupported model format in ANIMATED: " + trimLine);
                        }
                    }
                } else {
                    Main.LOGGER.warn("Unsupported ANIMATED section: " + section);
                }
            }
        }

        if (section.equals("object")) {
            if (buildingPart.isStatic()) {
                buildingPart.bakeToStaticModel(staticModel, translation);
            } else if (buildingPart.unbakedStates != null && buildingPart.unbakedStates.length > 0) {
                buildingPart.bake(DEFAULT_MAPPING, translation);
                buildingContainer.parts.add(buildingPart);
            }
        }
    }

    private static Vector3f parseVectorValue(String value) {
        String[] tokens = value.split(",");
        if (tokens.length != 3) return new Vector3f(0, 0, 0);
        return new Vector3f(Float.parseFloat(tokens[0].trim()), Float.parseFloat(tokens[1].trim()), Float.parseFloat(tokens[2].trim()));
    }
}
