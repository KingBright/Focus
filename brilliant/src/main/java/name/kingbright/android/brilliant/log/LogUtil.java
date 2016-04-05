package name.kingbright.android.brilliant.log;

import android.util.Log;

/**
 * @author Jin Liang
 * @since 16/1/13
 */
public class LogUtil {
    public static void d(String tag, Object... objects) {
//        Timber.d(tag, objects);
        Log.d(tag, objects[0].toString());
    }
}
