package name.kingbright.android.focus.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.OnClick;
import name.kingbright.android.brilliant.app.BaseFragment;
import name.kingbright.android.brilliant.json.JsonUtil;
import name.kingbright.android.brilliant.log.LogUtil;
import name.kingbright.android.brilliant.widgets.AbsViewBinder;
import name.kingbright.android.brilliant.widgets.RecyclerAdapter;
import name.kingbright.android.brilliant.widgets.SwipeRefreshRecyclerView;
import name.kingbright.android.brilliant.widgets.recyclerview.Mode;
import name.kingbright.android.focus.R;
import name.kingbright.android.focus.rss.FeedFetcher;
import name.kingbright.android.focus.rss.Item;
import name.kingbright.android.focus.rss.Rss;
import name.kingbright.android.focus.rss.source.Source;
import name.kingbright.android.focus.widgets.RichTextView;
import rx.Observer;

/**
 * @author Jin Liang
 * @since 16/4/5
 */
public class RssItemListFragment extends BaseFragment {
    private static final String TAG = "RssItemListFragment";
    @Bind(R.id.listView)
    SwipeRefreshRecyclerView mListView;

    @Bind(R.id.subscribe)
    View mSubscribe;

    private RecyclerAdapter mAdapter;

    private Rss mRss;

    @Nullable
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.rss_item_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView.setMode(Mode.LIST_VERTICAL);
        mListView.setItemAnimator(new DefaultItemAnimator());
        mListView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mAdapter = new RecyclerAdapter<RssViewBinder, Source>(getAppContext()) {
            @Override
            protected RssViewBinder createView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                Context context = parent.getContext();
                View view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
                return new RssViewBinder(view);
            }
        };
        mListView.setAdapter(mAdapter);

        Bundle bundle = getArguments();
        mRss = JsonUtil.toObject(bundle.getString("rss"), Rss.class);
        if (mRss == null) {
            mAdapter.update(null);
        } else {
            mAdapter.update(mRss.channel.itemList);
        }
    }

    private void refresh() {
        if (mRss == null) {
            mListView.setRefreshing(false);
        } else {
            FeedFetcher.getInstance().fetch(mRss.url).subscribe(new Observer<Rss>() {
                @Override
                public void onCompleted() {
                    LogUtil.d("Rss", "complete");
                }

                @Override
                public void onError(Throwable e) {
                    LogUtil.d("Rss", e);
                    e.printStackTrace();
                    mListView.setRefreshing(false);
                }

                @Override
                public void onNext(Rss rssFeed) {
                    LogUtil.d("Rss", "data get");
                    mAdapter.update(rssFeed.channel.itemList);
                    mListView.setRefreshing(false);
                }
            });
        }
    }

    @OnClick(R.id.subscribe)
    void subscribe(View view) {
//        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_INDEFINITE)
//                .setAction("Action", null).show();
    }

    class RssViewBinder extends AbsViewBinder<Item> {
        @Bind(R.id.title)
        TextView mTitle;
        @Bind(R.id.content)
        RichTextView mContent;

        public RssViewBinder(View view) {
            super(view);
        }

        @Override
        public void bind(Item item) {
            mTitle.setText(item.title);
            if (!TextUtils.isEmpty(item.content)) {
                if (TextUtils.isEmpty(item.description)) {
                    mContent.setHtmlText(item.content);
                } else if (item.content.length() > item.description.length()) {
                    mContent.setHtmlText(item.content);
                }
            } else {
                mContent.setHtmlText(item.description);
            }
        }

        @Override
        protected void onViewClick(View view, Item item) {
        }
    }
}
