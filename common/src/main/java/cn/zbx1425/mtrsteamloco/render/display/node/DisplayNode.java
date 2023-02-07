package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.display.DisplaySink;
import mtr.data.TrainClient;

public interface DisplayNode {

    void tick(DisplaySink sink, TrainClient train);
}
