package name.kingbright.android.focus.fragments;

import android.app.ProgressDialog;
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
import android.widget.Toast;

import java.util.List;

import butterknife.Bind;
import name.kingbright.android.brilliant.app.BaseFragment;
import name.kingbright.android.brilliant.app.FragmentIntent;
import name.kingbright.android.brilliant.json.JsonUtil;
import name.kingbright.android.brilliant.log.LogUtil;
import name.kingbright.android.brilliant.widgets.AbsViewBinder;
import name.kingbright.android.brilliant.widgets.RecyclerAdapter;
import name.kingbright.android.brilliant.widgets.SwipeRefreshRecyclerView;
import name.kingbright.android.brilliant.widgets.recyclerview.Mode;
import name.kingbright.android.focus.R;
import name.kingbright.android.focus.rss.FeedFetcher;
import name.kingbright.android.focus.rss.Rss;
import name.kingbright.android.focus.rss.source.Source;
import name.kingbright.android.focus.rss.source.SourceReader;
import rx.Observer;

/**
 * @author Jin Liang
 * @since 16/1/5
 */
public class RssChannelFragment extends BaseFragment {
    private static final String TAG = "RecommendRssFragment";
    @Bind(R.id.listView)
    SwipeRefreshRecyclerView mListView;

    private SourceReader mReader;
    private RecyclerAdapter mAdapter;
    private Observer<? super List<Source>> mCallback = new Observer<List<Source>>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            mListView.setRefreshing(false);
        }

        @Override
        public void onNext(List<Source> sources) {
            mAdapter.update(sources);
            mListView.setRefreshing(false);
        }
    };

    @Nullable
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.rss_channel_fragment, container, false);
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
                View view = LayoutInflater.from(context).inflate(R.layout.channel_item_view, parent, false);
                return new RssViewBinder(view);
            }
        };
        mListView.setAdapter(mAdapter);

        mReader = new SourceReader(getActivity());
        mReader.readSourceList().subscribe(mCallback);
    }

    private void refresh() {
        mReader.readSourceList().subscribe(mCallback);
    }

    class RssViewBinder extends AbsViewBinder<Source> {
        @Bind(R.id.title)
        TextView mTitle;

        public RssViewBinder(View view) {
            super(view);
        }

        @Override
        public void bind(Source source) {
            mTitle.setText(source.title);
        }

        @Override
        protected void onViewClick(View view, final Source data) {
            if (data == null || TextUtils.isEmpty(data.url)) {
                Toast.makeText(getContext(), R.string.err_rss_url_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            final ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getString(R.string.trying_to_load));
            dialog.setCancelable(false);
            dialog.show();
            FeedFetcher.getInstance().fetch(data.url).subscribe(new Observer<Rss>() {
                @Override
                public void onCompleted() {
                    LogUtil.d("Rss", "complete");
                }

                @Override
                public void onError(Throwable e) {
                    LogUtil.d("Rss", e);
                    e.printStackTrace();
                    Toast.makeText(getAppContext(), R.string.loading_error, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                @Override
                public void onNext(Rss rssFeed) {
                    LogUtil.d("Rss", "data get");
                    Bundle bundle = new Bundle();
                    rssFeed.url = data.url;
                    bundle.putString("rss", JsonUtil.toJson(rssFeed));
                    FragmentIntent fragmentIntent = new FragmentIntent(RssItemListFragment.class, bundle);
                    getFragmentNavigator().startFragment(fragmentIntent);
                    dialog.dismiss();
                }
            });
        }
    }
}
