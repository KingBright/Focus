package name.kingbright.android.focus.wallpaper;

import name.kingbright.android.brilliant.net.BNet;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Jin Liang
 * @since 16/4/11
 */
public class BingWallpaperServiceCreator {
    public static BingWallpaperService create() {
        BNet.Config config = new BNet.Config();
        config.baseUrl = Constants.BASE_URL;
        config.factory = GsonConverterFactory.create();
        config.cacheTime = 3600 * 24;
        config.staleTime = 3600 * 24;
        return BNet.createService(config, BingWallpaperService.class);
    }
}
