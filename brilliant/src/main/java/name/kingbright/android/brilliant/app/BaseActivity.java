package name.kingbright.android.brilliant.app;

import android.os.Bundle;

import com.trello.rxlifecycle.components.RxActivity;

import butterknife.ButterKnife;

/**
 * @author Jin Liang
 * @since 16/1/8
 */
public abstract class BaseActivity extends RxActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateView(savedInstanceState);
        ButterKnife.bind(this);
        onViewCreated();
    }

    protected abstract void onCreateView(Bundle savedInstanceState);

    protected abstract void onViewCreated();
}
