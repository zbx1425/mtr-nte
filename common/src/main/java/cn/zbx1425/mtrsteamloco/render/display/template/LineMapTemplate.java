package cn.zbx1425.mtrsteamloco.render.display.template;

import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.display.node.DisplayNode;
import cn.zbx1425.mtrsteamloco.render.display.node.DrawLineMapNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;

import java.util.*;

public class LineMapTemplate implements DisplayTemplate {

    private final int xLeft, yNorm, yPass, yHighlight, srcWidth, srcHeight;
    private final int capsuleWidth;
    private final Map<String, Integer> capsuleX = new HashMap<>();
    private final Map<String, Integer> capsuleLeftX = new HashMap<>();
    private final Map<String, Integer> capsuleRightX = new HashMap<>();
    private final float highlightOnDuration, highlightOffDuration;
    private final float progressOnDuration, progressOffDuration;
    private final boolean progressSlide;

    public LineMapTemplate(JsonObject jsonObject) {
        JsonArray srcArea = jsonObject.get("src_area").getAsJsonArray();
        xLeft = srcArea.get(0).getAsInt();
        yNorm = srcArea.get(1).getAsInt();
        srcWidth = srcArea.get(2).getAsInt();
        srcHeight = srcArea.get(3).getAsInt();
        JsonObject variantsY = jsonObject.get("variants_y").getAsJsonObject();
        yHighlight = variantsY.has("highlight") ? variantsY.get("highlight").getAsInt() : yNorm;
        yPass = variantsY.has("passed") ? variantsY.get("passed").getAsInt() : yNorm;

        JsonObject animations = jsonObject.get("animations").getAsJsonObject();
        if (animations.has("highlight")) {
            JsonObject highlightAnimation = animations.get("highlight").getAsJsonObject();
            highlightOnDuration = highlightAnimation.get("duration_on").getAsFloat();
            highlightOffDuration = highlightAnimation.get("duration_off").getAsFloat();
        } else {
            highlightOnDuration = highlightOffDuration = 0;
        }
        if (animations.has("progress")) {
            JsonObject progressAnimation = animations.get("progress").getAsJsonObject();
            progressOnDuration = progressAnimation.get("duration_on").getAsFloat();
            progressOffDuration = progressAnimation.get("duration_off").getAsFloat();
            progressSlide = progressAnimation.get("type").getAsString().equals("slide");
        } else {
            progressOnDuration = progressOffDuration = 0;
            progressSlide = false;
        }

        capsuleWidth = jsonObject.get("capsule_width").getAsInt();
        List<Map.Entry<String, Integer>> capsulesXList =
                jsonObject.get("capsules_x").getAsJsonObject().entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().getAsInt()))
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .toList();
        for (int i = 0; i < capsulesXList.size(); i++) {
            String key = capsulesXList.get(i).getKey().toLowerCase(Locale.ROOT);
            int thisX = capsulesXList.get(i).getValue();
            int prevX = i > 0 ? capsulesXList.get(i - 1).getValue() : xLeft;
            int nextX = i < capsulesXList.size() - 1 ? capsulesXList.get(i + 1).getValue() : xLeft + srcWidth;
            capsuleX.put(key, thisX);
            capsuleLeftX.put(key, prevX);
            capsuleRightX.put(key, nextX);
        }
    }

    @Override
    public void tick(DisplayContent content, TrainClient train, DisplayNode untypedCaller) {
        DrawLineMapNode caller = (DrawLineMapNode)untypedCaller;
        String targetKey = caller.target.getTargetString(train).toLowerCase(Locale.ROOT);
        if (capsuleX.containsKey(targetKey)) {
            int highlightMoreX = caller.towardsRight
                    ? capsuleLeftX.get(targetKey) + capsuleWidth : capsuleRightX.get(targetKey);
            int highlightLessX = caller.towardsRight
                    ? capsuleX.get(targetKey) : capsuleX.get(targetKey) + capsuleWidth;
            int highlightStaticX = caller.towardsRight
                    ? capsuleX.get(targetKey) + capsuleWidth : capsuleX.get(targetKey);
            int highlightAnimatedX;
            if (progressOnDuration == 0 && progressOffDuration == 0) {
                highlightAnimatedX = highlightLessX;
            } else {
                if (progressSlide) {
                    if (RenderUtil.runningSeconds % (progressOnDuration + progressOffDuration) > progressOffDuration) {
                        highlightAnimatedX = highlightLessX;
                    } else {
                        float moreToLessRatio = (float) ((RenderUtil.runningSeconds % (progressOnDuration + progressOffDuration)) / progressOffDuration);
                        highlightAnimatedX = (int) (moreToLessRatio * highlightLessX + (1 - moreToLessRatio) * highlightMoreX);
                    }
                } else {
                    highlightAnimatedX = (RenderUtil.runningSeconds % (progressOnDuration + progressOffDuration) > progressOffDuration) ? highlightMoreX : highlightLessX;
                }
            }
            int highlightLeftX = caller.towardsRight ? highlightAnimatedX : highlightStaticX;
            int highlightRightX = caller.towardsRight ? highlightStaticX : highlightAnimatedX;
            boolean useHighlightTexture;
            if (highlightOnDuration == 0 && highlightOffDuration == 0) {
                useHighlightTexture = true;
            } else {
                useHighlightTexture = RenderUtil.runningSeconds % (highlightOnDuration + highlightOffDuration) > highlightOffDuration;
            }
            if (useHighlightTexture) {
                content.addHAreaQuad(caller.slot, xLeft, caller.towardsRight ? yPass : yNorm, srcWidth, srcHeight,
                        caller.u1, caller.v1, caller.u2, caller.v2, xLeft, highlightLeftX,
                        caller.color, caller.totalOpacity);
                content.addHAreaQuad(caller.slot, xLeft, yHighlight, srcWidth, srcHeight,
                        caller.u1, caller.v1, caller.u2, caller.v2, highlightLeftX, highlightRightX,
                        caller.color, caller.totalOpacity);
                content.addHAreaQuad(caller.slot, xLeft, caller.towardsRight ? yNorm : yPass, srcWidth, srcHeight,
                        caller.u1, caller.v1, caller.u2, caller.v2, highlightRightX, xLeft + srcWidth,
                        caller.color, caller.totalOpacity);
            } else {
                content.addHAreaQuad(caller.slot, xLeft, caller.towardsRight ? yPass : yNorm, srcWidth, srcHeight,
                        caller.u1, caller.v1, caller.u2, caller.v2, xLeft, highlightAnimatedX,
                        caller.color, caller.totalOpacity);
                content.addHAreaQuad(caller.slot, xLeft, caller.towardsRight ? yNorm : yPass, srcWidth, srcHeight,
                        caller.u1, caller.v1, caller.u2, caller.v2, highlightAnimatedX, xLeft + srcWidth,
                        caller.color, caller.totalOpacity);
            }
        } else {
            content.addQuad(caller.slot, xLeft, yPass, srcWidth, srcHeight, caller.u1, caller.v1, caller.u2, caller.v2,
                    caller.color, caller.totalOpacity);
        }
    }

}
