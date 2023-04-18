package cn.zbx1425.mtrsteamloco.data;

public interface RailExtraSupplier {

    // "": default, "null": hidden
    String getModelKey();

    void setModelKey(String key);

    boolean getIsSecondaryDir();

    void setIsSecondaryDir(boolean value);

    float getVerticalCurveRadius();

    void setVerticalCurveRadius(float value);

}
