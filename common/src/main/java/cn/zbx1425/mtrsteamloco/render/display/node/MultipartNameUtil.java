package cn.zbx1425.mtrsteamloco.render.display.node;

import mtr.data.IGui;
import mtr.data.TrainClient;
import net.minecraft.Util;

public class MultipartNameUtil {

    public static String getTargetName(TrainClient train, String target) {
        switch (target) {
            case "line":
                return train.getThisRoute().name;
            case "line_eng":
                return Util.memoize(MultipartNameUtil::getEnglish).apply(train.getThisRoute().name);
            case "line_extra":
                return Util.memoize(MultipartNameUtil::getExtra).apply(train.getThisRoute().name);
            case "next_sta":
                return train.getNextStation().name;
            case "next_sta_eng":
                return Util.memoize(MultipartNameUtil::getEnglish).apply(train.getNextStation().name);
            case "next_sta_extra":
                return Util.memoize(MultipartNameUtil::getExtra).apply(train.getNextStation().name);
            default:
                return target;
        }
    }

    private static String getExtra(String src) {
        if (src.contains("||")) {
            return src.split("\\|\\|", 2)[1];
        } else {
            return "";
        }
    }

    private static String getEnglish(String src) {
        if (src.contains("||")) src = src.split("\\|\\|", 2)[0];
        String[] stringSplit = src.split("\\|");
        StringBuilder result = new StringBuilder();

        for (final String stringSplitPart : stringSplit) {
            if (result.length() > 0) result.append('|');
            final boolean isCJK = IGui.isCjk(stringSplitPart);
            if (!isCJK) {
                result.append(stringSplitPart);
            }
        }
        return result.toString();
    }
}
