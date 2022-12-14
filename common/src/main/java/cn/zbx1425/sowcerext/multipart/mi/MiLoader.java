package cn.zbx1425.sowcerext.multipart.mi;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.multipart.MultipartContainer;
import cn.zbx1425.sowcerext.multipart.PartBase;
import cn.zbx1425.sowcerext.reuse.AtlasManager;
import cn.zbx1425.sowcerext.reuse.ModelManager;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cn.zbx1425.sowcer.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MiLoader {

    public static MultipartContainer loadModel(ResourceManager resourceManager, ModelManager modelManager, AtlasManager atlasManager, ResourceLocation objLocation) throws IOException {
        JsonObject configData = Main.JSON_PARSER.parse(ResourceUtil.readResource(resourceManager, objLocation)).getAsJsonObject();
        JsonObject miData = Main.JSON_PARSER.parse(ResourceUtil.readResource(resourceManager,
                ResourceUtil.resolveRelativePath(objLocation, configData.get("miProject").getAsString(), ""))).getAsJsonObject();
        float timelineFps = configData.get("timelineFps").getAsFloat();

        HashMap<String, MiPart> idToParts = new HashMap<>();
        HashMap<String, MiPart> nameToParts = new HashMap<>();
        for (JsonElement part : miData.get("timelines").getAsJsonArray()) {
            JsonObject partObj = part.getAsJsonObject();
            MiPart miPart = new MiPart();
            for (Map.Entry<String, JsonElement> keyFrame : partObj.get("keyframes").getAsJsonObject().entrySet()) {
                float time = Float.parseFloat(keyFrame.getKey()) / timelineFps;
                JsonObject keyFrameObj = keyFrame.getValue().getAsJsonObject();
                miPart.translateX.spline.put(time, frameValue(keyFrameObj, "POS_X") / 16);
                miPart.translateZ.spline.put(time, frameValue(keyFrameObj, "POS_Y") / 16);
                miPart.translateY.spline.put(time, frameValue(keyFrameObj, "POS_Z") / 16);
                final float deg2Rad = (float)(Math.PI / 180F);
                miPart.rotateX.spline.put(time, frameValue(keyFrameObj, "ROT_X") * deg2Rad);
                miPart.rotateZ.spline.put(time, frameValue(keyFrameObj, "ROT_Y") * deg2Rad);
                miPart.rotateY.spline.put(time, frameValue(keyFrameObj, "ROT_Z") * deg2Rad);
            }
            miPart.name = partObj.get("name").getAsString();
            idToParts.put(partObj.get("id").getAsString(), miPart);
        }
        for (JsonElement part : miData.get("timelines").getAsJsonArray()) {
            JsonObject partObj = part.getAsJsonObject();
            MiPart miPart = idToParts.get(partObj.get("id").getAsString());
            String parent = partObj.get("parent").getAsString();
            if (!StringUtils.isEmpty(parent) && !parent.equals("root")) {
                miPart.parent = idToParts.get(parent);
            }
        }

        MultipartContainer container = new MultipartContainer();
        container.parts.addAll(idToParts.values());
        container.topologicalSort();
        for (PartBase part : container.parts) {
            MiPart miPart = (MiPart) part;
            if (!StringUtils.isEmpty(miPart.name)) {
                if (miPart.parent != null && !StringUtils.isEmpty(((MiPart) miPart.parent).name)) {
                    miPart.name = ((MiPart) miPart.parent).name + "." + miPart.name;
                }
                nameToParts.put(miPart.name, miPart);

                if (configData.get("models").getAsJsonObject().has(miPart.name)) {
                    JsonObject modelObj = configData.get("models").getAsJsonObject().get(miPart.name).getAsJsonObject();
                    RawModel model;
                    Vector3f offset = modelObj.has("offset") ? parseVectorValue(modelObj.get("offset").getAsString())
                            : new Vector3f(0, 0, 0);
                    Vector3f translation = modelObj.has("position") ? parseVectorValue(modelObj.get("position").getAsString())
                            : new Vector3f(0, 0, 0);
                    if (modelObj.has("model")) {
                        model = modelManager.loadRawModel(resourceManager,
                                ResourceUtil.resolveRelativePath(objLocation, modelObj.get("model").getAsString(), ""), atlasManager);
                        Vector3f pivot = modelObj.has("pivot") ? parseVectorValue(modelObj.get("pivot").getAsString())
                                : new Vector3f(0, 0, 0);
                        model.applyTranslation(-pivot.x(), -pivot.y(), -pivot.z());
                        offset.add(pivot);
                    } else {
                        model = null;
                    }
                    miPart.setModel(model, modelManager);
                    miPart.internalOffset = offset;
                    miPart.externalOffset = translation;
                }
            }
        }

        for (JsonElement copy : configData.get("copyKeyframes").getAsJsonArray()) {
            JsonObject copyObj = copy.getAsJsonObject();
            Vector3f srcSpan = parseVectorValue(copyObj.get("src").getAsString());
            float destBegin = copyObj.get("dest").getAsFloat();
            Vector3f mirror = parseVectorValue(copyObj.get("mirror").getAsString());
            boolean mirrorX = mirror.x() != 0;
            JsonObject modelReplace = copyObj.get("modelReplace").getAsJsonObject();
            for (Map.Entry<String, MiPart> entry : idToParts.entrySet()) {
                MiPart srcPart = entry.getValue();
                MiPart destPart;
                if (modelReplace.has(srcPart.name)) {
                    destPart = nameToParts.get(modelReplace.get(srcPart.name).getAsString());
                } else {
                    destPart = srcPart;
                }
                copyKeyFrames(srcPart.translateX, destPart.translateX, srcSpan, destBegin, mirrorX ? -1 : 1, 0);
                copyKeyFrames(srcPart.translateY, destPart.translateY, srcSpan, destBegin, 1, 0);
                copyKeyFrames(srcPart.translateZ, destPart.translateZ, srcSpan, destBegin, 1, 0);
                copyKeyFrames(srcPart.rotateX, destPart.rotateX, srcSpan, destBegin, 1, 0);
                copyKeyFrames(srcPart.rotateY, destPart.rotateY, srcSpan, destBegin, mirrorX ? -1 : 1, 0);
                copyKeyFrames(srcPart.rotateZ, destPart.rotateZ, srcSpan, destBegin, mirrorX ? -1 : 1, 0);
            }
        }


        return container;
    }

    private static Vector3f parseVectorValue(String value) {
        String[] tokens = value.split(",");
        return new Vector3f(
                tokens.length >= 1 ? Float.parseFloat(tokens[0].trim()) : 0,
                tokens.length >= 2 ? Float.parseFloat(tokens[1].trim()) : 0,
                tokens.length >= 3 ? Float.parseFloat(tokens[2].trim()) : 0
        );
    }
    
    private static float frameValue(JsonObject keyFrameObj, String element) {
        return keyFrameObj.has(element) ? keyFrameObj.get(element).getAsFloat() : 0F;
    }

    private static void copyKeyFrames(MiPart.FloatSpline srcSpline, MiPart.FloatSpline destSpline, Vector3f srcSpan, float destBegin,
                                      float mul, float add) {
        HashMap<Float, Float> tempMap = new HashMap<>();
        for (Map.Entry<Float, Float> entry : srcSpline.spline.entrySet()) {
            if (entry.getKey() < srcSpan.x()) continue;
            if (entry.getKey() > srcSpan.y()) break;
            tempMap.put(entry.getKey() - srcSpan.x() + destBegin, entry.getValue() * mul + add);
        }
        destSpline.spline.putAll(tempMap);
    }
}
