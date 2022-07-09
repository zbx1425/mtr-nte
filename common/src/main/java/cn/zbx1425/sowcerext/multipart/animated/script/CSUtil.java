package cn.zbx1425.sowcerext.multipart.animated.script;

import com.google.common.primitives.Doubles;

import java.time.Duration;
import java.util.Locale;

public class CSUtil {

    public static boolean tryParseDouble(String str) {
        return Doubles.tryParse(str) != null;
    }

    public static boolean tryParseTime(String str) {
        try {
            Duration.parse(str);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String substring(String haystack, int begin, int length) {
        return haystack.substring(begin, begin + length);
    }

}
