package name.kingbright.android.focus.wallpaper;

import java.util.ArrayList;

import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @author Jin Liang
 * @since 16/4/11
 */
public interface BingWallpaperService {
    @GET("HPImageArchive.aspx?format=js&n={num}&idx={idx}")
    ArrayList<Image> fetch(@Path("num") int num, @Path("idx") int idx);
}
