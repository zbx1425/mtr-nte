package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import cn.zbx1425.sowcerext.model.ModelCluster;
import net.minecraft.network.chat.Component;

import java.io.Closeable;
import java.io.IOException;

public class EyeCandyProperties implements Closeable {

    public Component name;

    public ModelCluster model;
    public ScriptHolder script;

    public EyeCandyProperties(Component name, ModelCluster model, ScriptHolder script) {
        this.name = name;
        this.model = model;
        this.script = script;
    }

    @Override
    public void close() throws IOException {
        if (model != null) model.close();
    }
}
