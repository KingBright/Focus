package name.kingbright.android.focus.rss;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.NamespaceList;
import org.simpleframework.xml.Root;

import java.util.List;

@NamespaceList({
        @Namespace(reference = "http://www.w3.org/2005/Atom", prefix = "atom"),
        @Namespace(reference = "http://purl.org/rss/1.0/modules/content/", prefix = "content"),
        @Namespace(reference = "http://wellformedweb.org/CommentAPI/", prefix = "wfw"),
        @Namespace(reference = "http://purl.org/dc/elements/1.1/", prefix = "dc"),
        @Namespace(reference = "http://purl.org/rss/1.0/modules/syndication/", prefix = "sy"),
        @Namespace(reference = "http://purl.org/rss/1.0/modules/slash/", prefix = "slash")
})
@Root(strict = false)
public class Channel {
    // Tricky part in Simple XML because the link is named twice
    @ElementList(entry = "link", inline = true, required = false)
    public List<Link> links;

    @ElementList(name = "item", required = true, inline = true)
    public List<Item> itemList;

    @Element
    String title;
    @Element(required = false)
    String language;

    @Element(name = "ttl", required = false)
    int ttl;

    @Element(name = "pubDate", required = false)
    String pubDate;
}