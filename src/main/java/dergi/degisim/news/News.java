// -*- @author aeren_pozitif  -*- //
package dergi.degisim.news;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by Eren Colak on 18.01.2018.
 */

public class News {
    private Uri uri;
    private String title;
    private String content;
    private String path;

    public static final String CONTENT_TOKEN = "*-";

    public News() {

    }

    public News(Uri uri, String title, String content) {
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

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "[NEWS = [(Title="+title+", Content="+content+", Path="+path+")]";
    }
}
