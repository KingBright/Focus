package name.kingbright.android.focus.rss.source;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import name.kingbright.android.brilliant.log.LogUtil;
import name.kingbright.android.brilliant.utils.BUtil;
import name.kingbright.android.focus.R;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Jin Liang
 * @since 16/3/14
 */
public class SourceReader {
    private static final String SEPARATOR = "\\|";

    private static final String TAG = "SourceReader";

    private Context mContext;

    private List<Source> mList = new ArrayList<>();

    public SourceReader(Context context) {
        mContext = BUtil.getAppContext(context);
    }

    public Observable<List<Source>> readSourceList() {
        LogUtil.d(TAG, "readSourceList");
        return Observable.create(new Observable.OnSubscribe<List<Source>>() {
            @Override
            public void call(Subscriber<? super List<Source>> subscriber) {
                LogUtil.d(TAG, "start reading");
                String[] list = mContext.getResources().getStringArray(R.array.source_list);
                if (list == null || list.length == 0) {
                    LogUtil.d(TAG, "error");
                    subscriber.onError(new Throwable("Can't find any built-in source list."));
                    subscriber.onCompleted();
                    return;
                }
                for (String text : list) {
                    Source item = parseItem(text);
                    if (item != null) {
                        mList.add(item);
                    }
                }
                LogUtil.d(TAG, "read successfully");
                subscriber.onNext(mList);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    private Source parseItem(String item) {
        if (TextUtils.isEmpty(item)) {
            return null;
        }

        String[] segments = item.split(SEPARATOR);
        if (segments == null || segments.length != 2) {
            return null;
        }
        return Source.New(segments[0], null, segments[1]);
    }
}
