package name.kingbright.android.brilliant.widgets;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;

import name.kingbright.android.brilliant.widgets.recyclerview.Mode;

/**
 * @author Jin Liang
 * @since 16/1/5
 */
public class SwipeRefreshRecyclerView extends SwipeRefreshLayout implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mRecyclerView;

    public SwipeRefreshRecyclerView(Context context) {
        this(context, null);
    }

    public SwipeRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRecyclerView = new RecyclerView(getContext());
        SwipeRefreshLayout.LayoutParams params = new SwipeRefreshLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mRecyclerView, params);
        this.setOnRefreshListener(this);
    }

    public void setItemAnimator(RecyclerView.ItemAnimator animator) {
        mRecyclerView.setItemAnimator(animator);
    }

    public void setMode(Mode mode) {
        switch (mode) {
            case LIST_VERTICAL: {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                break;
            }
            case LIST_HORIZONTAL: {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                break;
            }
            case GRID_VERTICAL: {
                mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), GridLayoutManager.HORIZONTAL));
                break;
            }
            case GRID_HORIZONTAL: {
                mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), GridLayoutManager.HORIZONTAL));
                break;
            }
            case GRID_STAGGERED_HORIZONTAL: {
                mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
                break;
            }
            case GRID_STAGGERED_VERTICAL: {
                mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
                break;
            }
            default:
                throw new RuntimeException("Not supported mode.");
        }
    }

    /**
     * Only works after calling {@link #setMode(Mode)} with {@link Mode#GRID_HORIZONTAL}, {@link Mode#GRID_HORIZONTAL}, {@link Mode#GRID_STAGGERED_VERTICAL} and {@link Mode#GRID_STAGGERED_HORIZONTAL}
     *
     * @param count
     */
    public void setSpanCount(int count) {
        RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
        if (lm == null) {
            return;
        }
        if (lm instanceof GridLayoutManager) {
            GridLayoutManager glm = (GridLayoutManager) lm;
            glm.setSpanCount(count);
        }
        if (lm instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager glm = (StaggeredGridLayoutManager) lm;
            glm.setSpanCount(count);
        }
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecyclerView.setAdapter(adapter);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration decoration) {
        mRecyclerView.addItemDecoration(decoration);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public void onRefresh() {
        Log.d("RecyclerView", "onRefresh");
    }
}
