package cn.zbx1425.mtrsteamloco.render.scripting.util;

import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import mtr.MTRClient;
import net.minecraft.client.Minecraft;

@SuppressWarnings("unused FieldCanBeLocal")
public class TimingUtil {

    private static double timeElapsed;
    private static double frameDelta;

    public static void tick() {
        final float lastFrameDuration = MTRClient.getLastFrameDuration();
        if (!Minecraft.getInstance().isPaused()) {
            timeElapsed += lastFrameDuration / 20;
            frameDelta = lastFrameDuration / 20;
        } else {
            frameDelta = 0;
        }
    }

    private static double timeElapsedForScript = 0;
    private static double frameDeltaForScript = 0;

    public static void prepareForScript(ScriptHolder scriptHolder) {
        timeElapsedForScript = timeElapsed;
        frameDeltaForScript = timeElapsed - scriptHolder.lastExecuteTime;
        scriptHolder.lastExecuteTime = timeElapsed;
    }

    public static double elapsed() {
        return timeElapsedForScript;
    }

    public static double delta() {
        return frameDeltaForScript;
    }
}
