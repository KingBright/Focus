package name.kingbright.android.brilliant.app;

import android.os.Bundle;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.ButterKnife;

/**
 * @author Jin Liang
 * @since 16/3/7
 */
public abstract class BaseAppCompactActivity extends RxAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        create(savedInstanceState);
        ButterKnife.bind(this);
        onCreated();
    }

    protected abstract void create(Bundle savedInstanceState);

    protected abstract void onCreated();
}
