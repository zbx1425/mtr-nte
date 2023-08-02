package cn.zbx1425.mtrsteamloco.render.scripting.util;

public class RateLimit {

    private long lastTime = 0;
    private final long interval;

    public RateLimit(float interval) {
        this.interval = (long)(interval * 1000);
    }

    public boolean shouldUpdate() {
        long now = System.currentTimeMillis();
        if (now - lastTime > interval) {
            lastTime = now;
            return true;
        }
        return false;
    }

    public void resetCoolDown() {
        lastTime = 0;
    }
}
