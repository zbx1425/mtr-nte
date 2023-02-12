package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import mtr.data.TrainClient;

public interface DisplayNode {

    void draw(DisplayContent content, TrainClient train);

}
