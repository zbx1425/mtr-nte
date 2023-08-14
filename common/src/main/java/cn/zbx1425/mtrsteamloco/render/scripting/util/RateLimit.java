package cn.zbx1425.mtrsteamloco.render.scripting.util;

public class RateLimit {

    private double lastTime = 0;
    private final double interval;

    public RateLimit(double interval) {
        this.interval = interval;
    }

    public boolean shouldUpdate() {
        double now = TimingUtil.elapsed();
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
