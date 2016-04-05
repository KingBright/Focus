package name.kingbright.android.focus.rss;

import retrofit2.http.GET;
import retrofit2.http.Url;
import rx.Observable;

/**
 * @author Jin Liang
 * @since 16/3/15
 */
public interface FeedService {
    @GET
    Observable<Rss> fetch(@Url String url);
}
