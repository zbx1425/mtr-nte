package cn.zbx1425.mtrsteamloco.render.scripting.util;

@SuppressWarnings("unused")
public class StateTracker {

    private String lastState;
    private String currentState;
    private double currentStateTime;
    private boolean firstTimeCurrentState;

    public void setState(String value) {
        if (value != null && !value.equals(currentState)) {
            lastState = currentState;
            currentState = value;
            currentStateTime = TimingUtil.elapsed();
            firstTimeCurrentState = true;
        } else if (value != null) {
            firstTimeCurrentState = false;
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
