package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import com.google.gson.JsonObject;
import mtr.MTRClient;
import mtr.data.TrainClient;

public abstract class DisplayNode {

    private final float fadeInRate, fadeOutRate;
    public float selfOpacity, totalOpacity;

    protected DisplayNode parent;

    protected Boolean isEnabled;
    protected boolean justEnabled;

    DisplayNode(JsonObject jsonObject) {
        if (jsonObject.has("fade_in_time")) {
            float fadeInTime = jsonObject.get("fade_in_time").getAsFloat();
            fadeInRate = fadeInTime == 0 ? 0 : 1 / fadeInTime;
        } else {
            fadeInRate = 0;
        }
        if (jsonObject.has("fade_out_time")) {
            float fadeOutTime = jsonObject.get("fade_out_time").getAsFloat();
            fadeOutRate = fadeOutTime == 0 ? 0 : 1 / fadeOutTime;
        } else {
            fadeOutRate = 0;
        }
    }

    public void tick(DisplayContent content, TrainClient train, boolean enabled) {
        if (isEnabled == null) {
            justEnabled = false;
            selfOpacity = enabled ? 1 : 0;
        } else {
            justEnabled = enabled && !isEnabled;
            if (enabled) {
                selfOpacity = (fadeInRate == 0) ? 1 : Math.min(1, selfOpacity + (float)(fadeInRate * RenderUtil.frameSeconds));
            } else {
                selfOpacity = (fadeInRate == 0) ? 0 : Math.max(0, selfOpacity - (float)(fadeOutRate * RenderUtil.frameSeconds));
            }
        }
        if (parent == null) {
            totalOpacity = selfOpacity;
        } else {
            totalOpacity = selfOpacity * parent.totalOpacity;
        }
        isEnabled = enabled;
        if (totalOpacity > 0) {
            draw(content, train);
        }
    }

    public void draw(DisplayContent content, TrainClient train) {

    }

    protected static int parseHexColor(String src) {
        if (src.length() > 6) {
            return Integer.reverseBytes(Integer.parseInt(src, 16));
        } else {
            return Integer.reverseBytes((Integer.parseInt(src, 16) << 8 | 0xFF));
        }
    }

}
