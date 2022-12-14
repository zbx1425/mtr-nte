package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.sowcerext.model.ModelCluster;
import net.minecraft.network.chat.Component;

public class EyeCandyProperties {

    public Component name;

    public ModelCluster model;

    public EyeCandyProperties(Component name, ModelCluster model) {
        this.name = name;
        this.model = model;
    }
}
