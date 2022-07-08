package cn.zbx1425.sowcerext.multipart.loader;

import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;

public class AnimatedFormula {

    private float lastValue = 0;

    public static final AnimatedFormula DEFAULT = new AnimatedFormula("");

    public AnimatedFormula(String expression) {

    }

    public void update(MultipartUpdateProp prop) {

    }

    public float getValue() {
        return 0;
    }

    public boolean isStatic() {
        return true;
    }
}
