package cn.zbx1425.sowcerext.multipart.animated;

import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.multipart.MultipartContainer;
import cn.zbx1425.sowcerext.multipart.animated.script.FunctionScript;
import cn.zbx1425.sowcerext.reuse.AtlasManager;
import cn.zbx1425.sowcerext.reuse.ModelManager;
import cn.zbx1425.sowcerext.util.Logging;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import cn.zbx1425.sowcer.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Locale;

public class AnimatedLoader {

    public static MultipartContainer loadModel(ResourceManager resourceManager, ModelManager modelManager, AtlasManager atlasManager, ResourceLocation objLocation) throws IOException {
        AnimatedLoader loader = new AnimatedLoader();
        loader.load(resourceManager, modelManager, atlasManager, objLocation, new Vector3f(0, 0, 0));
        StaticPart staticPart = new StaticPart(loader.staticModel, modelManager);
        loader.buildingContainer.parts.add(staticPart);
        return loader.buildingContainer;
    }

    private final MultipartContainer buildingContainer = new MultipartContainer();
    private final RawModel staticModel = new RawModel();

    private void load(ResourceManager resourceManager, ModelManager modelManager, AtlasManager atlasManager, ResourceLocation objLocation, Vector3f translation) throws IOException {
        String rawModelData = ResourceUtil.readResource(resourceManager, objLocation);
        String[] rawModelLines = rawModelData.split("[\\r\\n]+");
        String section = "";

        AnimatedPart buildingPart = new AnimatedPart();
        Vector3f includeTranslation = new Vector3f(0, 0, 0);
        for (String line : rawModelLines) {
            if (line.contains(";")) line = line.split(";", 2)[0];
            final String trimLine = line.trim();
            if (StringUtils.isEmpty(trimLine)) continue;

            try {
                if (trimLine.startsWith("[") && trimLine.endsWith("]")) {
                    if (section.equals("object")) {
                        if (buildingPart.isStatic()) {
                            buildingPart.bakeToStaticModel(staticModel, translation);
                        } else if (buildingPart.rawStates != null && buildingPart.rawStates.length > 0) {
                            buildingPart.uploadStates(modelManager, translation);
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
                                buildingPart.rawStates = new RawModel[states.length];
                                for (int i = 0; i < states.length; ++i) {
                                    String crntState = states[i].trim().toLowerCase(Locale.ROOT);
                                    if (StringUtils.isEmpty(crntState)) continue;
                                    ResourceLocation crntStateLocation = ResourceUtil.resolveRelativePath(objLocation, crntState, null);
                                    String crntStatExt = FilenameUtils.getExtension(crntState);
                                    if (crntStatExt.equals("obj") || crntStatExt.equals("csv") || crntStatExt.equals("nmb")) {
                                        buildingPart.rawStates[i] = modelManager.loadRawModel(resourceManager, crntStateLocation, atlasManager);
                                    } else {
                                        Logging.LOGGER.warn("Unsupported model format in ANIMATED: " + crntState);
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
                                Logging.LOGGER.warn("ANIMATED command that isn't currently supported: " + key);
                                break;
                            case "textureshiftxdirection":
                            case "textureshiftydirection":
                            case "textureshiftxfunction":
                            case "textureshiftyfunction":
                            case "trackfollowerfunction":
                            case "textureoverride":
                                // Logging.LOGGER.warn("ANIMATED command that cannot and will not be supported: " + key);
                                break;
                            case "refreshrate":
                                buildingPart.refreshRateMillis = (int) (Float.parseFloat(value) * 1000F);
                                break;
                            case "billboard":
                                // extension
                                buildingPart.billboard = Integer.parseInt(value) != 0;
                            default:
                                Logging.LOGGER.warn("Unknown ANIMATED command in Object: " + tokens[0]);
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
                                    Logging.LOGGER.warn("Unknown ANIMATED command in Include: " + tokens[0]);
                                    break;
                            }
                        } else {
                            ResourceLocation crntStateLocation = ResourceUtil.resolveRelativePath(objLocation, trimLine, null);
                            String crntStatExt = FilenameUtils.getExtension(trimLine);
                            if (crntStatExt.equals("obj") || crntStatExt.equals("csv") || crntStatExt.equals("nmb")) {
                                RawModel model = modelManager.loadRawModel(resourceManager, crntStateLocation, atlasManager).copy();
                                model.sourceLocation = null;
                                model.applyTranslation(translation.x() + includeTranslation.x(), translation.y() + includeTranslation.y(),
                                        translation.z() + includeTranslation.z());
                                staticModel.append(model);
                            } else if (crntStatExt.equals("animated")) {
                                load(resourceManager, modelManager, atlasManager, crntStateLocation, new Vector3f(translation.x() + includeTranslation.x(),
                                        translation.y() + includeTranslation.y(), translation.z() + includeTranslation.z()));
                            } else {
                                Logging.LOGGER.warn("Unsupported model format in ANIMATED: " + trimLine);
                            }
                        }
                    } else {
                        Logging.LOGGER.warn("Unsupported ANIMATED section: " + section);
                    }
                }
            } catch (Exception ex) {
                Logging.LOGGER.error("Failed loading ANIMATED model " + objLocation + ", line \"" + line + "\": ", ex);
            }
        }

        if (section.equals("object")) {
            if (buildingPart.isStatic()) {
                buildingPart.bakeToStaticModel(staticModel, translation);
            } else if (buildingPart.rawStates != null && buildingPart.rawStates.length > 0) {
                buildingPart.uploadStates(modelManager, translation);
                buildingContainer.parts.add(buildingPart);
            }
        }

        staticModel.distinct();
    }

    private static Vector3f parseVectorValue(String value) {
        String[] tokens = value.split(",");
        if (tokens.length != 3) return new Vector3f(0, 0, 0);
        return new Vector3f(Float.parseFloat(tokens[0].trim()), Float.parseFloat(tokens[1].trim()), Float.parseFloat(tokens[2].trim()));
    }
}
