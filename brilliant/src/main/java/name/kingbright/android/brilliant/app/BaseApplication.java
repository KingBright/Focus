package name.kingbright.android.brilliant.app;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import name.kingbright.android.brilliant.imageloader.ImageLoader;

/**
 * @author Jin Liang
 * @since 16/3/16
 */
public class BaseApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
