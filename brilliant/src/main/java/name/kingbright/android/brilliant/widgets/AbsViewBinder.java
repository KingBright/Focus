package name.kingbright.android.brilliant.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * @author Jin Liang
 * @since 16/1/5
 */
public abstract class AbsViewBinder<T> extends RecyclerView.ViewHolder {
    private T data;

    public AbsViewBinder(final View view) {
        super(view);
        ButterKnife.bind(this, view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClick(view, data);
            }
        });
    }

    public abstract void bind(T t);

    protected void onViewClick(View view, T data) {
    }

    protected void setData(T data) {
        this.data = data;
    }

}
