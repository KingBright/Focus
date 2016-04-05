package name.kingbright.android.brilliant.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;

import java.util.Stack;

import name.kingbright.android.brilliant.log.LogUtil;

/**
 * @author Jin Liang
 * @since 16/3/14
 */
public class FragmentNavigator implements FragmentManager.OnBackStackChangedListener {
    private static final int NOT_FOR_RESULT = 0x00000000;
    private static final String TAG = "FragmentNavigator";
    private FragmentManager fragmentManager;

    private Stack<FragmentRecord> fragmentStack = new Stack<>();
    private int mContainerId;
    /**
     * If allow empty, then it will pop the first fragment added when called back.
     */
    private boolean mAllowEmpty = false;

    public FragmentNavigator() {
    }

    public void allowEmpty(boolean bool) {
        mAllowEmpty = bool;
    }

    public void bind(Activity activity, @IdRes int containerId) {
        if (containerId <= 0) {
            throw new RuntimeException("Container id is not valid.");
        }
        if (activity == null) {
            throw new RuntimeException("Must specify an activity.");
        }
        this.mContainerId = containerId;
        this.fragmentManager = activity.getFragmentManager();
        this.fragmentManager.addOnBackStackChangedListener(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void bind(Fragment fragment, @IdRes int containerId) {
        if (containerId <= 0) {
            throw new RuntimeException("Container id is not valid.");
        }
        if (fragment == null) {
            throw new RuntimeException("Must specify an fragment.");
        }
        this.mContainerId = containerId;
        this.fragmentManager = fragment.getChildFragmentManager();
        this.fragmentManager.addOnBackStackChangedListener(this);
    }

    @Override
    public void onBackStackChanged() {
        int count = this.fragmentManager.getBackStackEntryCount();
        if (count >= fragmentStack.size()) {
            LogUtil.d(TAG, "new fragment(s) is(are) pushed to back stack");
            return;
        } else {
            LogUtil.d(TAG, "some fragment(s) is(are) popped out from stack.");
            while (fragmentStack.size() > 0 && fragmentStack.size() > count) {
                fragmentStack.pop();
            }
        }
    }

    public void startFragment(FragmentIntent intent) {
        startFragmentForResult(intent, NOT_FOR_RESULT);
    }

    public void startFragmentForResult(FragmentIntent intent, int requestCode) {
        if (intent == null) {
            throw new NullPointerException("FragmentIntent should not be null.");
        }
        Class target = intent.target;
        if (target == null) {
            throw new NullPointerException("Target fragment should not be null.");
        }
        // TODO deal with flag

        BaseFragment fragment;
        try {
            fragment = (BaseFragment) target.newInstance();
            fragment.setArguments(intent.arguments);
            fragment.setFragmentNavigator(this);
            if (fragment == null) {
                throw new RuntimeException("Can't initiate instance.");
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        startFragmentForResult(fragment, target.getName(), intent.flag, requestCode);
    }

    private void startFragmentForResult(BaseFragment fragment, String className, int flag, int requestCode) {
        if (fragmentManager == null) {
            throw new RuntimeException("Have you called FragmentNavigator#bind(Activity) first?");
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        String tag = generateFragmentTag(className);
        fragmentStack.add(new FragmentRecord(className, tag));
        if (fragmentStack.empty()) {
            transaction.add(mContainerId, fragment, tag);
        } else {
            transaction.replace(mContainerId, fragment, tag);
        }
        transaction.addToBackStack(tag);
        transaction.show(fragment);
        transaction.commit();
    }

    private String generateFragmentTag(String name) {
        int hit = 0;
        for (FragmentRecord record : fragmentStack) {
            if (record.equals(name)) {
                hit++;
            }
        }
        String tag = new StringBuilder(name).append("_").append(hit).toString();
        return tag;
    }

    public static FragmentIntent newIntent(Class cls, Bundle bundle) {
        return new FragmentIntent(cls, bundle);
    }

    public static FragmentIntent newIntent(Class cls, Bundle bundle, int flag) {
        return new FragmentIntent(cls, bundle, flag);
    }

    /**
     * return true if actually popped something out.
     *
     * @return
     */
    public boolean pop() {
        if (fragmentStack.size() == 1 && !mAllowEmpty) {
            return false;
        }
        return fragmentManager.popBackStackImmediate();
    }

    private class FragmentRecord {
        String className;
        String tag;

        public FragmentRecord(String className, String tag) {
            this.className = className;
            this.tag = tag;
        }
    }
}
