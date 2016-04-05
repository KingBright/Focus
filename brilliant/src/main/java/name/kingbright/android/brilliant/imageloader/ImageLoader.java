package name.kingbright.android.brilliant.imageloader;

import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

/**
 * @author Jin Liang
 * @since 16/4/5
 */
public class ImageLoader {
    public static void init(Context context) {
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context).setWebpSupportEnabled(true).build();
        Fresco.initialize(context, config);
    }
}
