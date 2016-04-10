package name.kingbright.android.focus.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.kingbright.android.brilliant.log.LogUtil;
import name.kingbright.android.brilliant.widgets.ImageView;
import name.kingbright.android.focus.R;

/**
 * @author Jin Liang
 * @since 16/4/6
 */
public class RichTextView extends LinearLayout implements ViewTreeObserver.OnGlobalLayoutListener {
    private static RecycledViewPool recycledViewPool = new RecycledViewPool();

    private static final String TAG = "RichTextView";

    private static Pattern IMAGE_TAG_PATTERN = Pattern.compile("\\<img(.*?)\\>");
    private static Pattern VIDEO_TAG_PATTERN = Pattern.compile("\\<video(.*?)\\>");
    private static Pattern IMAGE_WIDTH_PATTERN = Pattern.compile("width=\"(.*?)\"");
    private static Pattern IMAGE_HEIGHT_PATTERN = Pattern.compile("height=\"(.*?)\"");
    private static Pattern IMAGE_SRC_PATTERN = Pattern.compile("src=\"(.*?)\"");

    private ArrayList<View> mViews;
    private ArrayList<ImageHolder> mImages;
    private ArrayList<VideoHolder> mVideos;

    private ArrayList<RenderItem> mRenderList;

    private boolean mDisplayed = false;
    private boolean mLayouted = false;
    private Runnable mDisplayRunnable = new Runnable() {
        @Override
        public void run() {
            if (mDisplayed) {
                return;
            }
            if (mRenderList != null) {
                LogUtil.d(TAG, "display");
                mViews = new ArrayList<>();
                Context context = getContext();
                LayoutInflater inflater = LayoutInflater.from(context);
                for (RenderItem renderItem : mRenderList) {
                    View view = renderItem.getView(inflater, context);
                    mViews.add(view);
                    addView(view);
                }
            }
        }
    };

    public RichTextView(Context context) {
        super(context);
        init();
    }

    public RichTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RichTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RichTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        recycledViewPool.onViewAttached();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recycledViewPool.onViewDetached();
    }

    private void recycle() {
        if (mImages != null) {
            mImages.clear();
        }
        if (mRenderList != null) {
            mRenderList.clear();
            mRenderList = null;
        }

        if (getChildCount() != 0) {
            removeAllViews();
        }

        if (mViews != null) {
            for (View view : mViews) {
                recycledViewPool.putView(view);
            }
            mViews.clear();
        }

        mDisplayed = false;
    }

    public void setHtmlText(String text) {
        recycle();
        LogUtil.d(TAG, "setHtmlText " + hashCode());
        // Find all image tags
        matchImages(text);
        // Remove image span and replace with ImageView
        Spanned spanned = Html.fromHtml(text);
        SpannableStringBuilder builder;
        if (spanned instanceof SpannableStringBuilder) {
            builder = (SpannableStringBuilder) spanned;
        } else {
            builder = new SpannableStringBuilder(spanned);
        }

        ImageSpan[] imageSpans = builder.getSpans(0, builder.length(), ImageSpan.class);

        mRenderList = new ArrayList<>();
        if (imageSpans == null || imageSpans.length == 0) {
            mRenderList.add(new TextRenderItem(spanned));
        } else {
            for (int i = imageSpans.length - 1; i >= 0; i--) {
                ImageSpan imageSpan = imageSpans[i];
                int imageStart = builder.getSpanStart(imageSpan);
                int imageEnd = builder.getSpanEnd(imageSpan);
                if (imageEnd == builder.length() - 1) {
                    builder.delete(imageStart, imageEnd);
                    mRenderList.add(0, new ImageRenderItem(mImages.remove(mImages.size() - 1)));
                    continue;
                }

                CharSequence charSequence = builder.subSequence(imageEnd, builder.length() - 1);
                builder.delete(imageStart, builder.length() - 1);

                mRenderList.add(0, new TextRenderItem(charSequence));
                mRenderList.add(0, new ImageRenderItem(mImages.remove(mImages.size() - 1)));
            }
            if (builder.length() > 0) {
                mRenderList.add(0, new TextRenderItem(builder));
            }
        }

        if (mLayouted) {
            mDisplayRunnable.run();
        }
    }

    /**
     * 从文本中拿到<img/>标签,并获取图片url和宽高
     */
    private void matchImages(String text) {
        mImages = new ArrayList<>();
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
            Uri uri = Uri.parse(src);
            holder = new ImageHolder(uri);
            widthMatcher = IMAGE_WIDTH_PATTERN.matcher(image);
            if (widthMatcher.find()) {
                holder.width = parseStringToInteger(getTextBetweenQuotation(widthMatcher.group().trim().substring(6)));
            }

            heightMatcher = IMAGE_HEIGHT_PATTERN.matcher(image);
            if (heightMatcher.find()) {
                holder.height = parseStringToInteger(getTextBetweenQuotation(heightMatcher.group().trim().substring(6)));
            }

            mImages.add(holder);
        }
    }

    private int parseStringToInteger(String str) {
        int result = -1;
        if (!TextUtils.isEmpty(str)) {
            try {
                result = Integer.parseInt(str);
            } catch (Exception e) {
            }
            try {
                double f = Double.parseDouble(str);
                return (int) f;
            } catch (Exception e) {
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onGlobalLayout() {
        mLayouted = true;
        mDisplayRunnable.run();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    private class ImageHolder {
        private final Uri src;
        public int width = -1, height = -1;

        public ImageHolder(Uri src) {
            this.src = src;
        }
    }

    private class VideoHolder {
        private final Uri src;
        public int width = -1, height = -1;

        public VideoHolder(Uri src) {
            this.src = src;
        }
    }

    private class VideoRenderItem extends RenderItem<VideoHolder, VideoView> {

        public VideoRenderItem(VideoHolder data) {
            super(data);
        }

        @Override
        protected Class getViewClass() {
            return VideoView.class;
        }

        @Override
        protected VideoView getView(LayoutInflater inflater, View view, Context context, VideoHolder data) {
            return null;
        }
    }

    private class TextRenderItem extends RenderItem<CharSequence, AppCompatTextView> {

        public TextRenderItem(CharSequence data) {
            super(data);
        }

        @Override
        protected Class getViewClass() {
            return AppCompatTextView.class;
        }

        @Override
        protected AppCompatTextView getView(LayoutInflater inflater, View view, Context context, CharSequence data) {
            AppCompatTextView textView = view != null ? (AppCompatTextView) view : (AppCompatTextView) inflater.inflate(R.layout.text_view, RichTextView.this, false);
            textView.setText(data);
            return textView;
        }
    }

    private class ImageRenderItem extends RenderItem<ImageHolder, ImageView> {

        public ImageRenderItem(ImageHolder data) {
            super(data);
        }

        @Override
        protected Class getViewClass() {
            return ImageView.class;
        }

        @Override
        protected View getView(LayoutInflater inflater, View view, Context context, ImageHolder data) {
            ImageView imageView = view != null ? (ImageView) view : (ImageView) inflater.inflate(R.layout.image_view, RichTextView.this, false);
            PipelineDraweeControllerBuilder builder = imageView.getControllerBuilder();
            builder.setAutoPlayAnimations(true).setControllerListener(new ControllerListener<ImageInfo>() {
                ImageView imageView;

                @Override
                public void onSubmit(String id, Object callerContext) {
                    imageView = (ImageView) callerContext;
                }

                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    LogUtil.d(TAG, "image loaded");
                    int imageWidth = imageInfo.getWidth();
                    int imageHeight = imageInfo.getHeight();

                    int width = RichTextView.this.getWidth();
                    LogUtil.d(TAG, "TextView size : " + width);
                    LayoutParams params;
                    float ratio = (float) width / (float) imageWidth;
                    float actualWidth, actualHeight;
                    if (ratio < 2) {
                        actualWidth = width;
                        actualHeight = ratio * imageHeight;
                    } else {
                        ratio = 2;
                        actualWidth = ratio * imageWidth;
                        actualHeight = ratio * imageHeight;
                    }
                    params = new LayoutParams((int) actualWidth, (int) actualHeight);
                    imageView.setLayoutParams(params);
                    LogUtil.d(TAG, "set ImageView size : " + actualWidth + "*" + actualHeight);
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
            });
            imageView.setImageURI(data.src, imageView);
            return imageView;
        }
    }

    private abstract class RenderItem<T, K extends View> {
        private T data;

        public RenderItem(T data) {
            this.data = data;
        }

        public K getView(LayoutInflater inflater, Context context) {
            View view = recycledViewPool.getView(getViewClass());
            return (K) getView(inflater, view, context, data);
        }

        protected abstract Class getViewClass();

        protected abstract View getView(LayoutInflater inflater, View view, Context context, T data);
    }

    private static class RecycledViewPool {
        private SparseArray<ArrayList<View>> recycledViews = new SparseArray<>();
        private ArrayList<String> typeList = new ArrayList<>();
        private int attachCount;

        public void putView(View view) {
            int type = getViewType(view.getClass());
            ArrayList<View> list = recycledViews.get(type);
            if (list == null) {
                list = new ArrayList<>();
                recycledViews.append(type, list);
            }
            list.add(view);
        }

        private String getViewName(Class cls) {
            return cls.getName();
        }

        private int getViewType(Class cls) {
            String name = getViewName(cls);
            if (!typeList.contains(name)) {
                typeList.add(name);
            }
            return typeList.indexOf(name);
        }

        public View getView(Class cls) {
            int type = getViewType(cls);
            ArrayList<View> list = recycledViews.get(type);
            if (list == null || list.size() == 0) {
                return null;
            } else {
                View view = list.remove(0);
                return view;
            }
        }

        public void onViewAttached() {
            attachCount++;
        }

        public void onViewDetached() {
            attachCount--;
            if (attachCount == 0) {
                recycledViews.clear();
                typeList.clear();
            }
        }
    }
}
