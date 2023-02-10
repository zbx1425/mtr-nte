package cn.zbx1425.mtrsteamloco.render.font;

import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;

public class MultipartText {

    public final TextPart[] textParts;

    public final int alignH, alignVOut, alignVIn;
    public final int overflowH;
    public final float scrollSpeed;
    public boolean isAlwaysDirty;
    // public final float scrollMarginH;

    public MultipartText(JsonObject jsonObject) {
        alignH = jsonObject.has("align_h") ? jsonObject.get("align_h").getAsInt() : 0;
        alignVOut = jsonObject.has("align_v_out") ? jsonObject.get("align_v_out").getAsInt() : 0;
        alignVIn = jsonObject.has("align_v_in") ? jsonObject.get("align_v_in").getAsInt() : 0;
        if (jsonObject.has("overflow_h")) {
            switch (jsonObject.get("overflow_h").getAsString()) {
                case "stretch":
                    overflowH = 1;
                    break;
                case "fit":
                    overflowH = 2;
                    break;
                case "scroll":
                    overflowH = -1;
                    break;
                case "always_scroll":
                    overflowH = -2;
                    break;
                default:
                    overflowH = 0;
                    break;
            }
        } else {
            overflowH = 2;
        }
        scrollSpeed = jsonObject.has("scroll_speed") ? jsonObject.get("scroll_speed").getAsFloat() : 0.1f;
        // scrollMarginH = jsonObject.has("scroll_margin_h") ? jsonObject.get("scroll_margin_h").getAsFloat() : 1f;

        JsonArray partArray = jsonObject.get("parts").getAsJsonArray();
        textParts = new TextPart[partArray.size()];
        for (int i = 0; i < partArray.size(); i++) {
            textParts[i] = new TextPart(partArray.get(i).getAsJsonObject());
        }
    }

    public TargetArea[] calculateBounds(TargetArea target, float aspectRatio, TextBoundProvider boundProvider) {
        TargetArea[] result = new TargetArea[textParts.length];
        float targetWidth = target.ur - target.ul, targetHeight = target.vb - target.vt;

        // Lay parts next to each other
        float unscaledMaxU = 0, unscaledMaxV = 0;
        for (int i = 0; i < textParts.length; i++) {
            unscaledMaxU += textParts[i].marginL;
            float partWidth = boundProvider.measureWidth(textParts[i].text) * textParts[i].sizeV / aspectRatio;
            result[i] = new TargetArea(unscaledMaxU, 0, unscaledMaxU + partWidth, textParts[i].sizeV);
            unscaledMaxV = Math.max(unscaledMaxV, textParts[i].sizeV);
            unscaledMaxU += partWidth + textParts[i].marginR;
        }

        // Apply internal vertical align
        for (TargetArea area : result) {
            if (alignVIn == 1) { // Bottom
                area.vt = unscaledMaxV - area.vb;
                area.vb = unscaledMaxV;
            } else if (alignVIn == 0) { // Center
                float sizeV = area.vb;
                area.vt = unscaledMaxV / 2 - sizeV / 2;
                area.vb = unscaledMaxV / 2 + sizeV / 2;
            }
        }

        // Scale for horizontal overflow
        float scaledMaxV = unscaledMaxV;
        float scaledMaxU = unscaledMaxU;
        if (unscaledMaxU > targetWidth) {
            if (overflowH == 1) { // Stretch
                float ratio = targetWidth / unscaledMaxU;
                for (TargetArea targetArea : result) {
                    targetArea.ul *= ratio; targetArea.ur *= ratio;
                }
                scaledMaxU = targetWidth;
            } else if (overflowH == 2) { // Fit
                float ratio = targetWidth / unscaledMaxU;
                for (TargetArea targetArea : result) {
                    targetArea.ul *= ratio; targetArea.ur *= ratio;
                    targetArea.vt *= ratio; targetArea.vb *= ratio;
                }
                scaledMaxV = unscaledMaxV * ratio;
                scaledMaxU = targetWidth;
            }
        }

        // Apply external vertical align
        if (alignVOut == 1) { // Bottom
            for (TargetArea targetArea : result) {
                targetArea.vt += targetHeight - scaledMaxV; targetArea.vb += targetHeight - scaledMaxV;
            }
        } else if (alignVOut == 0) { // Center
            for (TargetArea targetArea : result) {
                targetArea.vt += targetHeight / 2 - scaledMaxV / 2; targetArea.vb += targetHeight / 2 - scaledMaxV / 2;
            }
        }

        if (overflowH == -2 || (overflowH == -1 && scaledMaxU > targetWidth)) {
            // Apply scroll
            float totalWidth = scaledMaxU + targetWidth;
            float currentOffset = (float)((RenderUtil.runningSeconds * scrollSpeed) % totalWidth) - targetWidth;
            for (TargetArea area : result) {
                area.ul -= currentOffset; area.ur -= currentOffset;
            }
            isAlwaysDirty = true;
        } else {
            // Apply horizontal align
            if (alignH == 1) { // Right
                for (TargetArea area : result) {
                    area.ul += targetWidth - scaledMaxU; area.ur += targetWidth - scaledMaxU;
                }
            } else if (alignH == 0) { // Center
                for (TargetArea area : result) {
                    area.ul += targetWidth / 2 - scaledMaxU / 2; area.ur += targetWidth / 2 - scaledMaxU / 2;
                }
            }
            isAlwaysDirty = false;
        }

        // Apply clamp and shift into final position
        for (int i = 0; i < result.length; i++) {
            TargetArea area = result[i];
            if ((area.ul < 0 && area.ur < 0) || (area.ul > targetWidth && area.ur > targetWidth)) {
                result[i] = null;
                continue;
            } else if (area.ul < 0 && area.ur > targetWidth) {
                area.srcUL = (0 - area.ul) / (area.ur - area.ul);
                area.srcUR = (targetWidth - area.ul) / (area.ur - area.ul);
                area.ul = 0;
                area.ur = targetWidth;
            } else if (area.ul < 0) {
                area.srcUL = (0 - area.ul) / (area.ur - area.ul);
                area.ul = 0;
            } else if (area.ur > targetWidth) {
                area.srcUR = (targetWidth - area.ul) / (area.ur - area.ul);
                area.ur = targetWidth;
            }
            area.ul += target.ul; area.ur += target.ul;
            area.vt += target.vt; area.vb += target.vt;
        }

        return result;
    }

    public int getTextHash(TrainClient train) {
        int lhs = 0;
        for (TextPart part : textParts) {
            int rhs = part.getTextHash(train);
            lhs ^= rhs + 0x9e3779b9 + (lhs << 6) + (lhs >>> 2);
        }
        return lhs;
    }

    public static class TargetArea {
        public float ul, vt, ur, vb;
        public float srcUL = 0, srcUR = 1;

        public TargetArea(float u1, float v1, float u2, float v2) {
            this.ul = u1;
            this.vt = v1;
            this.ur = u2;
            this.vb = v2;
        }
    }
}
