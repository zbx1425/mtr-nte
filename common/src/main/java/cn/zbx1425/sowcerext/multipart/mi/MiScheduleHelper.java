package cn.zbx1425.sowcerext.multipart.mi;

import mtr.MTRClient;
import net.minecraft.client.Minecraft;

public class MiScheduleHelper {

    private float lastRenderedTick = 0;

    public float currentFrameTime = 0;
    private float targetFrameTime = 0;

    public void elapse() {
        final float lastFrameDuration = MTRClient.getLastFrameDuration();
        final float ticksElapsed = Minecraft.getInstance().isPaused() || lastRenderedTick == MTRClient.getGameTick() ? 0 : lastFrameDuration;
        currentFrameTime = Math.min(currentFrameTime + ticksElapsed / 20, targetFrameTime);
        lastRenderedTick = MTRClient.getGameTick();
    }

    public void play(float begin, float end) {
        if (targetFrameTime != end) {
            targetFrameTime = end;
            currentFrameTime = begin;
        } else {
            currentFrameTime = Math.min(Math.max(currentFrameTime, begin), targetFrameTime);
        }
    }

    public void pause() {
        targetFrameTime = currentFrameTime;
    }

    public void stop() {
        targetFrameTime = 0;
        currentFrameTime = 0;
    }
}
