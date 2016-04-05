package name.kingbright.android.brilliant.widgets;

import android.content.Context;
import android.util.AttributeSet;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * @author Jin Liang
 * @since 16/3/14
 */

public class ImageView extends SimpleDraweeView {
    public ImageView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    public ImageView(Context context) {
        super(context);
    }

    public ImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
