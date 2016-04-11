package name.kingbright.android.focus.wallpaper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jin Liang
 * @since 16/4/11
 */
public class Util {

    private static String regex = "\\(.*?\\)";

    static String[] splitCopyright(String copyright) {
        String[] segments = new String[2];

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(copyright);
        if (matcher.find()) {
            int index = matcher.start();
            segments[0] = copyright.substring(0, index);
            segments[1] = copyright.substring(index);
        } else {
            segments[0] = "";
            segments[1] = copyright;
        }

        return segments;
    }
}
