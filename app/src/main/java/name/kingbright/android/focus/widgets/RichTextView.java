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
public class RichTextView extends LinearLayout {

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
    private Runnable mDisplayRunnable = new Runnable() {
        @Override
        public void run() {
            mViews = new ArrayList<>();
            mDisplayed = true;
            Context context = getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            for (RenderItem renderItem : mRenderList) {
                View view = renderItem.getView(inflater, context);
                mViews.add(view);
                addView(view);
            }
        }
    };
    private boolean mDisplayed = false;

    public RichTextView(Context context) {
        super(context);
    }

    public RichTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RichTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RichTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!mDisplayed) {
            mDisplayRunnable.run();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        recycledViewPool.onViewAttached();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAllViews();
        for (View view : mViews) {
            recycledViewPool.putView(view);
        }
        recycledViewPool.onViewDetached();
    }

    public void setHtmlText(String text) {
        mDisplayed = false;
        // Find all image tags
        matchImages(text);
        LogUtil.d(TAG, "Images : " + mImages.size());
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

        if (getWidth() > 0) {
            mDisplayRunnable.run();
        }
        LogUtil.d(TAG, "render items : " + mRenderList.size());
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
                    int imageWidth = imageInfo.getWidth();
                    int imageHeight = imageInfo.getHeight();

                    int width = RichTextView.this.getWidth();
                    LayoutParams params;
                    float ratio = (float) width / (float) imageWidth;
                    if (ratio < 2) {
                        float actualHeight = ratio * imageHeight;
                        params = new LayoutParams(width, (int) actualHeight);
                    } else {
                        ratio = 2;
                        float actualWidth = ratio * imageWidth;
                        float actualHeight = ratio * imageHeight;
                        params = new LayoutParams((int) actualWidth, (int) actualHeight);
                    }
                    imageView.setLayoutParams(params);
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
            LogUtil.d(TAG, "put cache view : " + view.getClass().getName() + " with type : " + type);
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
                LogUtil.d(TAG, "get cached view");
                return list.remove(0);
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
