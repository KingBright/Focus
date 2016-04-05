package name.kingbright.android.brilliant.utils;

import android.content.Context;

/**
 * @author Jin Liang
 * @since 16/3/14
 */
public class BUtil {
    /**
     * Get application context. Throws NullPointerException if context is null.
     *
     * @param context
     * @return
     */
    public static Context getAppContext(Context context) {
        if (context == null) {
            throw new NullPointerException("Context is null.");
        }
        return context.getApplicationContext();
    }

}
