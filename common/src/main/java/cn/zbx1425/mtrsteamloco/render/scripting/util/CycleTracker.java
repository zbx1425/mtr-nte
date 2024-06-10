package cn.zbx1425.mtrsteamloco.render.scripting.util;

@SuppressWarnings("unused")
public class CycleTracker {

    private final String[] states;
    private final float[] offsets;
    private final float cycleDuration;

    public CycleTracker(Object[] params) {
        if (params.length % 2 != 0) throw new IllegalArgumentException();
        float offset = 0;
        states = new String[params.length / 2];
        offsets = new float[params.length / 2];
        for (int i = 0; i < params.length; i += 2) {
            states[i / 2] = params[i].toString();
            float elemDuration = Float.parseFloat(params[i + 1].toString());
            offsets[i / 2] = offset;
            offset += elemDuration;
        }
        cycleDuration = offset;
    }

    private String lastState;
    private String currentState;
    private double currentStateTime;
    private int lastStateNum;
    private boolean firstTimeCurrentState;

    public void tick() {
        double time = TimingUtil.elapsed() % cycleDuration;
        int cycleNum = (int) (TimingUtil.elapsed() / cycleDuration);
        for (int i = offsets.length - 1; i >= 0; i--) {
            if (time >= offsets[i]) {
                int stateNum = cycleNum * offsets.length + i;
                currentState = states[i];
                currentStateTime = cycleNum * cycleDuration + offsets[i];
                lastState = states[i == 0 ? offsets.length - 1 : i - 1];
                if (lastStateNum != stateNum) {
                    firstTimeCurrentState = true;
                    lastStateNum = stateNum;
                } else {
                    firstTimeCurrentState = false;
                }
                break;
            }
        }
    }

    public String stateNow() {
        return currentState;
    }

    public String stateLast() {
        return lastState;
    }

    public double stateNowDuration() {
        return TimingUtil.elapsed() - currentStateTime;
    }

    public boolean stateNowFirst() {
        return firstTimeCurrentState;
    }
}
