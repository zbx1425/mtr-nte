package cn.zbx1425.mtrsteamloco.render.display.node;

import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import mtr.data.TrainClient;

public interface DisplayNode {

    void draw(DisplayContent content, TrainClient train);

    default int parseHexColor(String src) {
        if (src.length() > 6) {
            return Integer.reverseBytes(Integer.parseInt(src, 16));
        } else {
            return Integer.reverseBytes((Integer.parseInt(src, 16) << 8 | 0xFF));
        }
    }
}
