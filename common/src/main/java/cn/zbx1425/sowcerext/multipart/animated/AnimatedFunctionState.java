package cn.zbx1425.sowcerext.multipart.animated;

import java.util.HashMap;

public class AnimatedFunctionState {

    public HashMap<Long, Double> lastResults = new HashMap<>();

    public double getLastResult(long id) {
        return lastResults.getOrDefault(id, 0.0);
    }

    public void setLastResult(long id, double value) {
        lastResults.put(id, value);
    }
}
