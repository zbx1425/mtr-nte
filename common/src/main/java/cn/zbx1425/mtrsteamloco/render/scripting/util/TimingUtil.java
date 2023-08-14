package cn.zbx1425.mtrsteamloco.render.scripting.util;

import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;

@SuppressWarnings("unused")
public class TimingUtil {

    private static double timeElapsedForScript = 0;
    private static double frameDeltaForScript = 0;

    public static void prepareForScript(ScriptHolder scriptHolder) {
        timeElapsedForScript = RenderUtil.runningSeconds;
        frameDeltaForScript = timeElapsedForScript - scriptHolder.lastExecuteTime;
        scriptHolder.lastExecuteTime = timeElapsedForScript;
    }

    public static double elapsed() {
        return timeElapsedForScript;
    }

    public static double delta() {
        return frameDeltaForScript;
    }
}
