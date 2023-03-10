package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.sowcerext.model.ModelCluster;
import net.minecraft.network.chat.Component;

import java.io.Closeable;
import java.io.IOException;

public class EyeCandyProperties implements Closeable {

    public Component name;

    public ModelCluster model;

    public EyeCandyProperties(Component name, ModelCluster model) {
        this.name = name;
        this.model = model;
    }

    @Override
    public void close() throws IOException {
        model.close();
    }
}
