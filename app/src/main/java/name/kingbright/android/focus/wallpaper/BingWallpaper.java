package name.kingbright.android.focus.wallpaper;

import android.content.Intent;
import android.net.Uri;
import android.util.Base64;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import java.util.ArrayList;

import name.kingbright.android.brilliant.log.LogUtil;

public class BingWallpaper extends RemoteMuzeiArtSource {
    private static final String TAG = "BingWallpaper";
    private static final String SOURCE_NAME = "BingWallpaperSource";

    private static final long ROTATE_TIME_MILLIS = 60 * 60 * 24 * 1000;

    private BingWallpaperService service;

    public BingWallpaper() {
        super(SOURCE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG,"onCreate");
        service = BingWallpaperServiceCreator.create();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        LogUtil.d(TAG,"onTryUpdate");
        ArrayList<Image> images = service.fetch(1, 0);

        if (images == null || images.size() == 0) {
            throw new RetryException();
        }

        Image image = images.get(0);
        String token = Base64.encodeToString(image.url.getBytes(), Base64.DEFAULT);
        String[] segments = Util.splitCopyright(image.copyright);
        Uri uri = Uri.parse(image.url);
        LogUtil.d(TAG, "artwork name : " + segments[0]);
        LogUtil.d(TAG, "artwork author : " + segments[1]);
        publishArtwork(new Artwork.Builder()
                .title(segments[0])
                .byline(segments[1])
                .imageUri(uri)
                .token(token)
                .viewIntent(new Intent(Intent.ACTION_VIEW, uri))
                .build());

        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
    }
}