// -*- @author aeren_pozitif  -*- //
package dergi.degisim.database;

import dergi.degisim.news.News;

public interface DataListener {
    void onDataFetched(News n, int pos);
    void onCategoryFetched(String category, News n, int pos);
    void onDataSaved(String lastMarkings, long id);
}
