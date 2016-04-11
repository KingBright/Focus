package name.kingbright.android.focus.rss;

import name.kingbright.android.brilliant.net.BNet;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import rx.Observable;

/**
 * @author Jin Liang
 * @since 16/3/15
 */
public class FeedFetcher {
    private static FeedFetcher ourInstance = new FeedFetcher();

    public static FeedFetcher getInstance() {
        return ourInstance;
    }

    private FeedService service;

    private FeedFetcher() {
        BNet.Config config = new BNet.Config();
        config.baseUrl = "http://kingbright.name";
        config.factory = SimpleXmlConverterFactory.createNonStrict();
        config.cacheTime = 3600;
        config.staleTime = 3600 * 24;

        service = BNet.createService(config, FeedService.class);
    }

    public Observable<Rss> fetch(String feedUrl) {
        return service.fetch(feedUrl);
    }
}
