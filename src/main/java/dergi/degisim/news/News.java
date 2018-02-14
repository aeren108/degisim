// -*- @author aeren_pozitif  -*- //
package dergi.degisim.news;

/**
 * Created by Eren Colak on 18.01.2018.
 */

public class News {
    private String uri;
    private String title;
    private String content;

    public static final String CONTENT_TOKEN = "*-";

    public News() {

    }

    public News(String uri, String title, String content) {
        this.uri = uri;
        this.title = title;
        this.content = content;
    }

    public void formatContent() {
        content = content.replace(CONTENT_TOKEN, "\n");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "[NEWS = [(Title="+title+", Content="+content+")]";
    }
}
