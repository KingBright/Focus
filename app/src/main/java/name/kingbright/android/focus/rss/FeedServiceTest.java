package name.kingbright.android.focus.rss;

import android.content.Context;

import java.util.List;

import name.kingbright.android.brilliant.log.LogUtil;
import name.kingbright.android.focus.rss.source.Source;
import name.kingbright.android.focus.rss.source.SourceReader;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * @author Jin Liang
 * @since 16/3/17
 */
public class FeedServiceTest {
    public void test(Context context) {
        new SourceReader(context).readSourceList().subscribe(new Action1<List<Source>>() {
            @Override
            public void call(List<Source> sources) {
                testOneByOne(0, sources);
            }
        });
    }

    public void testOneByOne(final int index, final List<Source> sources) {
        if (sources.size() > index) {
            final String url = sources.get(index).url;
            FeedFetcher.getInstance().fetch(url).subscribe(new Subscriber<Rss>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    LogUtil.d("Test", "url : " + url);
                    LogUtil.d("Test", "error : " + e.toString());
                    testOneByOne(index + 1, sources);
                }

                @Override
                public void onNext(Rss rss) {
                    testOneByOne(index + 1, sources);
                }
            });
        } else {
            LogUtil.d("Test", "Finished");
        }
    }
}
