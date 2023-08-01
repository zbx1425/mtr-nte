package cn.zbx1425.mtrsteamloco.render.scripting.util;

import mtr.MTRClient;

public class ScriptTimingUtil {

    public static float elapsed() {
        return MTRClient.getGameTick() / 20;
    }

    public static float delta() {
        return MTRClient.getLastFrameDuration() / 20;
    }
}
