package name.kingbright.android.brilliant.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trello.rxlifecycle.components.RxFragment;

import butterknife.ButterKnife;
import name.kingbright.android.brilliant.log.LogUtil;

/**
 * @author Jin Liang
 * @since 16/1/5
 */
public abstract class BaseFragment extends RxFragment {
    private FragmentNavigator fragmentNavigator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.d(getClass().getName(), "onCreateView");
        View view = createView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);
        return view;
    }

    protected abstract View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    void setFragmentNavigator(FragmentNavigator fragmentNavigator) {
        this.fragmentNavigator = fragmentNavigator;
    }

    protected FragmentNavigator getFragmentNavigator() {
        return fragmentNavigator;
    }
}
