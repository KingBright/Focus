package name.kingbright.android.focus.rss.source;

/**
 * @author Jin Liang
 * @since 16/1/4
 */
public class Source {
    public String title;
    public String description;
    public String url;

    private Source(String title, String description, String url) {
        this.title = title;
        this.description = description;
        this.url = url;
    }

    public Source() {
        this(null, null, null);
    }

    public Source(String title, String description) {
        this(title, description, null);
    }

    public static Source New(String title, String description, String url) {
        return new Source(title, description, url);
    }
}
