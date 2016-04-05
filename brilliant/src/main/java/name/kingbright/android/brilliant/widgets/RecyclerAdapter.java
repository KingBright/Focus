package name.kingbright.android.brilliant.widgets;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Jin Liang
 * @since 16/1/6
 */
public abstract class RecyclerAdapter<T extends AbsViewBinder, V> extends RecyclerView.Adapter<T> {
    private static final String TAG = "RecyclerAdapter";
    private LinkedList<V> mList;

    public RecyclerAdapter() {
        mList = new LinkedList<V>();
    }

    @Override
    public final T onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        return createView(inflater, parent, viewType);
    }

    protected abstract T createView(LayoutInflater inflater, ViewGroup parent, int viewType);

    @Override
    public final void onBindViewHolder(T holder, int position) {
        V data = mList.get(position);
        holder.setData(data);
        holder.bind(data);
    }

    @Override
    public final int getItemCount() {
        return mList.size();
    }

    /**
     * Append a single element
     *
     * @param item
     */
    public final void append(V item) {
        if (item == null) {
            return;
        }
        mList.add(item);
        notifyDataSetChanged();
    }

    /**
     * Append a collection of element.
     *
     * @param items
     */
    public final void append(Collection<V> items) {
        if (items == null || items.size() == 0) {
            return;
        }
        mList.addAll(items);
        notifyDataSetChanged();
    }

    public final void clear() {
        if (mList.isEmpty()) {
            return;
        }
        mList.clear();
        notifyDataSetChanged();
    }

    public final void remove(V item) {
        if (item == null) {
            return;
        }
        if (mList.contains(item)) {
            mList.remove(item);
            notifyDataSetChanged();
        }
    }

    public final void remove(Collection<V> items) {
        if (items == null || items.size() == 0) {
            return;
        }
        if (mList.removeAll(items)) {
            notifyDataSetChanged();
        }
    }

    public void update(Collection<V> items) {
        if (items == null || items.size() == 0) {
            return;
        }
        if (mList.size() > 0) {
            items.clear();
        }
        mList.addAll(items);
        notifyDataSetChanged();
    }
}
