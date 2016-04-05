package name.kingbright.android.focus;

import name.kingbright.android.brilliant.app.BaseApplication;
import name.kingbright.android.brilliant.imageloader.ImageLoader;

/**
 * @author Jin Liang
 * @since 16/3/16
 */
public class Focus extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.init(this);
    }
}
