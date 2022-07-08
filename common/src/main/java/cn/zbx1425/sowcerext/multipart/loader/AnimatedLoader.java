package cn.zbx1425.sowcerext.multipart.loader;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.loader.CsvModelLoader;
import cn.zbx1425.sowcerext.model.loader.ObjModelLoader;
import cn.zbx1425.sowcerext.multipart.MultipartContainer;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

public class AnimatedLoader {

    public static MultipartContainer loadModel(ResourceManager resourceManager, ResourceLocation objLocation) throws IOException {
        AnimatedLoader loader = new AnimatedLoader();
        loader.load(resourceManager, objLocation, new Vector3f(0, 0, 0));
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

        for (final String line : rawModelLines) {
            final String trimLine = line.trim().replaceAll("\\s*(;|#|//).+", "");
            if (StringUtils.isEmpty(trimLine)) {
                continue;
            }

            if (trimLine.contains("=")) {
                final String[] tokens = trimLine.split("=");
                if (tokens.length != 2) {
                    continue;
                }

                final String key = tokens[0].trim().toLowerCase().replaceAll("\\s", "");
                final String value = tokens[1].trim().toLowerCase().replace("\\", "/").replaceAll("\\.wav|\\s|.+/", "");
                if (StringUtils.isEmpty(value)) continue;

                if (section.equals("object")) {
                    switch (key) {
                        case "position":
                            buildingPart.translationToBake = parseVectorValue(value);
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
                            buildingPart.stateFunction = new AnimatedFormula(value);
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
                            buildingPart.translateXFunction = new AnimatedFormula(value);
                            break;
                        case "translateyfunction":
                            buildingPart.translateYFunction = new AnimatedFormula(value);
                            break;
                        case "translatezfunction":
                            buildingPart.translateZFunction = new AnimatedFormula(value);
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
                            buildingPart.rotateXFunction = new AnimatedFormula(value);
                            break;
                        case "rotateyfunction":
                            buildingPart.rotateYFunction = new AnimatedFormula(value);
                            break;
                        case "rotatezfunction":
                            buildingPart.rotateZFunction = new AnimatedFormula(value);
                            break;
                        case "rotatexdamping":
                        case "rotateydamping":
                        case "rotatezdamping":
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
                            break;
                        default:
                            Main.LOGGER.warn("Unknown ANIMATED command: " + tokens[0]);
                            break;
                    }
                } else if (section.equals("include")) {

                } else {
                    Main.LOGGER.warn("Unsupported ANIMATED section: " + section);
                }
            } else if (trimLine.startsWith("[") && trimLine.endsWith("]")) {
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
