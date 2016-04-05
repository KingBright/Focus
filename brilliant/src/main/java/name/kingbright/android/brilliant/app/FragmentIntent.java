package name.kingbright.android.brilliant.app;

import android.os.Bundle;

/**
 * @author Jin Liang
 * @since 16/3/14
 */
public class FragmentIntent {

    public static final int FLAG_FRAGMENT_NORMAL = 0x00000001;

    public Class target;

    public Bundle arguments;

    public int flag;

    public FragmentIntent(Class cls) {
        this(cls, null, FLAG_FRAGMENT_NORMAL);
    }

    public FragmentIntent(Class cls, Bundle bundle) {
        this(cls, bundle, FLAG_FRAGMENT_NORMAL);
    }

    public FragmentIntent(Class cls, Bundle bundle, int flag) {
        this.target = cls;
        this.arguments = bundle;
        this.flag = flag;
    }
}
