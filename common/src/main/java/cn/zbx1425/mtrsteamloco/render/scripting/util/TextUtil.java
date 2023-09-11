package cn.zbx1425.mtrsteamloco.render.scripting.util;

import mtr.data.IGui;

public class TextUtil {

    public static String getCjkParts(String src) {
        return getCjkMatching(src, true);
    }

    public static String getNonCjkParts(String src) {
        return getCjkMatching(src, false);
    }

    public static String getExtraParts(String src) {
        return getExtraMatching(src, true);
    }

    public static String getNonExtraParts(String src) {
        return getExtraMatching(src, false);
    }

    public static String getNonCjkAndExtraParts(String src) {
        String extraParts = getExtraMatching(src, false).trim();
        return getCjkMatching(src, false).trim() + (extraParts.isEmpty() ? "" : "|" + extraParts);
    }

    public static boolean isCjk(String src) {
        return IGui.isCjk(src);
    }

    private static String getExtraMatching(String src, boolean extra) {
        if (src.contains("||")) {
            return src.split("\\|\\|", 2)[extra ? 1 : 0].trim();
        } else {
            return "";
        }
    }

    private static String getCjkMatching(String src, boolean isCJK) {
        if (src.contains("||")) src = src.split("\\|\\|", 2)[0];
        String[] stringSplit = src.split("\\|");
        StringBuilder result = new StringBuilder();

        for (final String stringSplitPart : stringSplit) {
            if (mtr.data.IGui.isCjk(stringSplitPart) == isCJK) {
                if (result.length() > 0) result.append(' ');
                result.append(stringSplitPart);
            }
        }
        return result.toString().trim();
    }

}
