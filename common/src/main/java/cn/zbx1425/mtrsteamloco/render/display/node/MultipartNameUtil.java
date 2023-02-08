package cn.zbx1425.mtrsteamloco.render.display.node;

import mtr.data.IGui;
import mtr.data.TrainClient;
import net.minecraft.Util;

public class MultipartNameUtil {

    public static String getTargetName(TrainClient train, String target) {
        String thisRouteName = train.getThisRoute() == null ? "" : train.getThisRoute().name;
        String nextStaName = train.getNextStation() == null ? "" : train.getNextStation().name;
        switch (target) {
            case "$line":
                return thisRouteName;
            case "$line_cjk":
                return Util.memoize(MultipartNameUtil::getCjkMatching).apply(thisRouteName, true);
            case "$line_eng":
                return Util.memoize(MultipartNameUtil::getCjkMatching).apply(thisRouteName, false);
            case "$line_extra":
                return Util.memoize(MultipartNameUtil::getExtra).apply(thisRouteName);
            case "$next_sta":
                return nextStaName;
            case "$next_sta_cjk":
                return Util.memoize(MultipartNameUtil::getCjkMatching).apply(nextStaName, true);
            case "$next_sta_eng":
                return Util.memoize(MultipartNameUtil::getCjkMatching).apply(nextStaName, false);
            case "$next_sta_extra":
                return Util.memoize(MultipartNameUtil::getExtra).apply(nextStaName);
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

    private static String getCjkMatching(String src, boolean isCJK) {
        if (src.contains("||")) src = src.split("\\|\\|", 2)[0];
        String[] stringSplit = src.split("\\|");
        StringBuilder result = new StringBuilder();

        for (final String stringSplitPart : stringSplit) {
            if (result.length() > 0) result.append('|');
            if (IGui.isCjk(stringSplitPart) == isCJK) {
                result.append(stringSplitPart);
            }
        }
        return result.toString();
    }
}
