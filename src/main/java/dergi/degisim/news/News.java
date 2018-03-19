// -*- @author aeren_pozitif  -*- //
package dergi.degisim.news;

/**
 * Created by Eren Colak on 18.01.2018.
 */

public class News {
    private String uri;
    private String title;
    private String content;
    private long id;
    private long read;

    public static final String CONTENT_TOKEN = "*-";

    public News() {

    }

    public News(String uri, String title, String content) {
        this.uri = uri;
        this.title = title;
        this.content = content;
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

    public long getID() {return  id;}

    public void setID(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "[NEWS = [(Title="+title+", Content="+content+")]";
    }

    public long getRead() {
        return read;
    }

    public void setRead(long read) {
        this.read = read;
    }
}
