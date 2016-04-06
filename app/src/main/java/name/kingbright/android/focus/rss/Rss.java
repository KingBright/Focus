package name.kingbright.android.focus.rss;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class Rss {
    @Attribute(required = false)
    public String version;

    @Element
    public Channel channel;

    public String url;
}
