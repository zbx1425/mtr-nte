package cn.zbx1425.mtrsteamloco.render.display.template;

import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.display.node.DisplayNode;
import mtr.data.TrainClient;

public interface DisplayTemplate {

    void tick(DisplayContent content, TrainClient train, DisplayNode caller);
}
