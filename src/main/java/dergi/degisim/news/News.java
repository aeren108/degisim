// -*- @author aeren_pozitif  -*- //
package dergi.degisim.news;


public class News {
    private String uri;
    private String title;
    private String content;
    private String author;
    //private Date/String date;
    private long id;
    private long read;
    private boolean isSaved;

    public News() {

    }

    public News(long id, String uri, String title, String content) {
        this.id = id;
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

    public long getRead() {
        return read;
    }

    public void setRead(long read) {
        this.read = read;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "[NEWS = [(Title="+title+", Content="+content+")]";
    }
}
