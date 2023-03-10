package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.font.FontTexture;
import cn.zbx1425.mtrsteamloco.render.font.FontTextureCache;
import cn.zbx1425.mtrsteamloco.render.font.MultipartText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.data.TrainClient;

public class DrawFreeTextNode implements DisplayNode {

    private final String slot;
    private final MultipartText text;
    private final MultipartText.TargetArea targetArea;

    private MultipartText.TargetArea[] cachedAreas;
    private int cachedHash = 0;

    public DrawFreeTextNode(JsonObject jsonObject) {
        slot = jsonObject.get("slot").getAsString();
        text = new MultipartText(jsonObject.get("text").getAsJsonObject());
        JsonArray dstArea = jsonObject.get("dst_area").getAsJsonArray();
        float u1, v1, u2, v2;
        u1 = dstArea.get(0).getAsFloat(); v1 = dstArea.get(1).getAsFloat();
        u2 = u1 + dstArea.get(2).getAsFloat(); v2 = v1 + dstArea.get(3).getAsFloat();
        targetArea = new MultipartText.TargetArea(u1, v1, u2, v2);
    }

    @Override
    public void draw(DisplayContent content, TrainClient train) {
        int newHash = text.isAlwaysDirty ? 0 : text.getTextHash(content, train);
        if (text.isAlwaysDirty || newHash != cachedHash || cachedHash == 0) {
            cachedAreas = text.calculateBounds(targetArea, content.getSlot(slot).aspectRatio,
                    FontTextureCache.FONT_SANS_BOUND_PROVIDER.withContext(content, train));
            cachedHash = newHash;
        }
        for (int i = 0; i < cachedAreas.length; i++) {
            if (cachedAreas[i] == null) continue;
            MultipartText.TargetArea area = cachedAreas[i];
            FontTexture fontTexture = FontTextureCache.getTexture(text.textParts[i].text.getTargetString(content, train));
            if (fontTexture == null) return;
            content.addTextQuad(slot, fontTexture.resourceLocation, area.srcUL, 0, area.srcUR, 1,
                    area.ul, area.vt, area.ur, area.vb, text.textParts[i].color);
        }
    }

}
