package name.kingbright.android.focus.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.drawee.view.MultiDraweeHolder;
import com.facebook.imagepipeline.image.ImageInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.kingbright.android.brilliant.log.LogUtil;

/**
 * Modified from <code>https://github.com/zzhoujay/RichText</code> to support fresco
 *
 * @author Jin Liang
 * @since 16/4/5
 */
public class RichTextView extends TextView {
    private static final String TAG = "RichTextView";
    private MultiDraweeHolder<GenericDraweeHierarchy> mDraweeHolder;

    private static Pattern IMAGE_TAG_PATTERN = Pattern.compile("\\<img(.*?)\\>");
    private static Pattern IMAGE_WIDTH_PATTERN = Pattern.compile("width=\"(.*?)\"");
    private static Pattern IMAGE_HEIGHT_PATTERN = Pattern.compile("height=\"(.*?)\"");
    private static Pattern IMAGE_SRC_PATTERN = Pattern.compile("src=\"(.*?)\"");

    private Drawable placeHolder, errorImage;//占位图，错误图
    private OnImageClickListener onImageClickListener;//图片点击回调
    private OnURLClickListener onURLClickListener;//超链接点击回调
    private HashMap<String, ImageHolder> mImages;
    private ImageFixListener mImageFixListener;
    private int d_w = 200;
    private int d_h = 200;

    private boolean mClickToRetry = true;

    public RichTextView(Context context) {
        this(context, null);
    }

    public RichTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mDraweeHolder = new MultiDraweeHolder();

        if (placeHolder == null) {
            placeHolder = new ColorDrawable(Color.GRAY);
        }
        placeHolder.setBounds(0, 0, d_w, d_h);
        if (errorImage == null) {
            errorImage = new ColorDrawable(Color.GRAY);
        }
        errorImage.setBounds(0, 0, d_w, d_h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDraweeHolder.onDetach();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        mDraweeHolder.onDetach();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDraweeHolder.onAttach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        mDraweeHolder.onAttach();
    }

    /**
     * Whether to allow click the placeholder to retry if image not loaded successfully.
     *
     * @param retry
     */
    public void setClickRetry(boolean retry) {
        mClickToRetry = retry;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;
        if (mClickToRetry) {
            handled = mDraweeHolder.onTouchEvent(event);
        }
        return handled || super.onTouchEvent(event);
    }

    /**
     * 设置富文本
     *
     * @param text 富文本
     */
    public void setRichText(String text) {
        mDraweeHolder.clear();
        matchImages(text);

        Spanned spanned = Html.fromHtml(text, asyncImageGetter, null);
        SpannableStringBuilder spannableStringBuilder;
        if (spanned instanceof SpannableStringBuilder) {
            spannableStringBuilder = (SpannableStringBuilder) spanned;
        } else {
            spannableStringBuilder = new SpannableStringBuilder(spanned);
        }

        // 处理图片得点击事件
        ImageSpan[] imageSpans = spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), ImageSpan.class);
        final List<String> imageUrls = new ArrayList<>();

        for (int i = 0, size = imageSpans.length; i < size; i++) {
            ImageSpan imageSpan = imageSpans[i];
            String imageUrl = imageSpan.getSource();
            int start = spannableStringBuilder.getSpanStart(imageSpan);
            int end = spannableStringBuilder.getSpanEnd(imageSpan);
            imageUrls.add(imageUrl);

            final int finalI = i;
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (onImageClickListener != null) {
                        onImageClickListener.imageClicked(imageUrls, finalI);
                    }
                }
            };
            ClickableSpan[] clickableSpans = spannableStringBuilder.getSpans(start, end, ClickableSpan.class);
            if (clickableSpans != null && clickableSpans.length != 0) {
                for (ClickableSpan cs : clickableSpans) {
                    spannableStringBuilder.removeSpan(cs);
                }
            }
            spannableStringBuilder.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 处理超链接点击事件
        URLSpan[] urlSpans = spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), URLSpan.class);

        for (int i = 0, size = urlSpans == null ? 0 : urlSpans.length; i < size; i++) {
            URLSpan urlSpan = urlSpans[i];

            int start = spannableStringBuilder.getSpanStart(urlSpan);
            int end = spannableStringBuilder.getSpanEnd(urlSpan);

            spannableStringBuilder.removeSpan(urlSpan);
            spannableStringBuilder.setSpan(new CallableURLSpan(urlSpan.getURL(), onURLClickListener), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        super.setText(spanned);
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void addToHolder(DraweeHolder target) {
        mDraweeHolder.add(target);
    }

    /**
     * 从文本中拿到<img/>标签,并获取图片url和宽高
     */
    private void matchImages(String text) {
        mImages = new HashMap<>();
        ImageHolder holder;
        Matcher imageMatcher, srcMatcher, widthMatcher, heightMatcher;
        imageMatcher = IMAGE_TAG_PATTERN.matcher(text);
        while (imageMatcher.find()) {
            String image = imageMatcher.group().trim();
            srcMatcher = IMAGE_SRC_PATTERN.matcher(image);
            String src = null;
            if (srcMatcher.find()) {
                src = getTextBetweenQuotation(srcMatcher.group().trim().substring(4));
            }
            if (TextUtils.isEmpty(src)) {
                continue;
            }
            holder = new ImageHolder(src);
            widthMatcher = IMAGE_WIDTH_PATTERN.matcher(image);
            if (widthMatcher.find()) {
                holder.width = parseStringToInteger(getTextBetweenQuotation(widthMatcher.group().trim().substring(6)));
            }

            heightMatcher = IMAGE_HEIGHT_PATTERN.matcher(image);
            if (heightMatcher.find()) {
                holder.height = parseStringToInteger(getTextBetweenQuotation(heightMatcher.group().trim().substring(6)));
            }

            mImages.put(holder.src, holder);
        }
    }

    private int parseStringToInteger(String integerStr) {
        int result = -1;
        if (!TextUtils.isEmpty(integerStr)) {
            try {
                result = Integer.parseInt(integerStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 从双引号之间取出字符串
     */
    @Nullable
    private static String getTextBetweenQuotation(String text) {
        Pattern pattern = Pattern.compile("\"(.*?)\"");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 异步加载图片
     */
    private Html.ImageGetter asyncImageGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            LogUtil.d(TAG, "loading image : " + source);
            Uri uri = Uri.parse(source);

            GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources()).build();
            DraweeHolder holder = new DraweeHolder(hierarchy);
            final Drawable drawable = holder.getTopLevelDrawable();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(uri)
                    .setOldController(holder.getController())
                    .setControllerListener(new ControllerListener<ImageInfo>() {
                        @Override
                        public void onSubmit(String id, Object callerContext) {

                        }

                        @Override
                        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                            int width = imageInfo.getWidth();
                            int height = imageInfo.getHeight();
                            if (drawable != null) {
                                drawable.setBounds(0, 0, width, height);
                                setText(getText());
                            }
                        }

                        @Override
                        public void onIntermediateImageSet(String id, ImageInfo imageInfo) {

                        }

                        @Override
                        public void onIntermediateImageFailed(String id, Throwable throwable) {

                        }

                        @Override
                        public void onFailure(String id, Throwable throwable) {

                        }

                        @Override
                        public void onRelease(String id) {

                        }
                    })
                    .build();
            holder.setController(controller);
            addToHolder(holder);
            return drawable;
        }
    };

    private static class CallableURLSpan extends URLSpan {

        private OnURLClickListener onURLClickListener;

        public CallableURLSpan(String url, OnURLClickListener onURLClickListener) {
            super(url);
            this.onURLClickListener = onURLClickListener;
        }

        @SuppressWarnings("unused")
        public CallableURLSpan(Parcel src, OnURLClickListener onURLClickListener) {
            super(src);
            this.onURLClickListener = onURLClickListener;
        }

        @Override
        public void onClick(View widget) {
            if (onURLClickListener != null && onURLClickListener.urlClicked(getURL())) {
                return;
            }
            super.onClick(widget);
        }
    }

    public static class ImageHolder {
        public static final int DEFAULT = 0;
        public static final int CENTER_CROP = 1;
        public static final int CENTER_INSIDE = 2;

        @IntDef({DEFAULT, CENTER_CROP, CENTER_INSIDE})
        public @interface ScaleType {
        }

        private final String src;
        public int width = -1, height = -1;
        public int scaleType = DEFAULT;

        public ImageHolder(String src) {
            this.src = src;
        }
    }

    @SuppressWarnings("unused")
    public ImageHolder getImageHolder(String url) {
        return mImages.get(url);
    }

    @SuppressWarnings("unused")
    public void setPlaceHolder(Drawable placeHolder) {
        this.placeHolder = placeHolder;
        this.placeHolder.setBounds(0, 0, d_w, d_h);
    }

    @SuppressWarnings("unused")
    public void setErrorImage(Drawable errorImage) {
        this.errorImage = errorImage;
        this.errorImage.setBounds(0, 0, d_w, d_h);
    }

    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    public void setImageFixListener(ImageFixListener mImageFixListener) {
        this.mImageFixListener = mImageFixListener;
    }

    /**
     * 设置超链接点击回调事件（需在setRichText方法之前调用）
     *
     * @param onURLClickListener 回调
     */
    public void setOnURLClickListener(OnURLClickListener onURLClickListener) {
        this.onURLClickListener = onURLClickListener;
    }

    public interface OnImageClickListener {
        /**
         * 图片被点击后的回调方法
         *
         * @param imageUrls 本篇富文本内容里的全部图片
         * @param position  点击处图片在imageUrls中的位置
         */
        void imageClicked(List<String> imageUrls, int position);
    }

    public interface OnURLClickListener {

        /**
         * 超链接点击得回调方法
         *
         * @param url 点击得url
         * @return true：已处理，false：未处理（会进行默认处理）
         */
        boolean urlClicked(String url);
    }

    public interface ImageFixListener {
        /**
         * 修复图片尺寸的方法
         *
         * @param holder ImageHolder对象
         */
        void onFix(ImageHolder holder);
    }
}
