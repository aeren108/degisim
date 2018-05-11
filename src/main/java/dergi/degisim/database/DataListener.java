// -*- @author aeren_pozitif  -*- //
package dergi.degisim.database;

import dergi.degisim.news.News;

public interface DataListener {
    public static final int DATAFETCH_ERROR = 0;
    public static final int CATFETCH_ERROR = 1;
    public static final int SAVE_ERROR = 2;
    public static final int UNSAVE_ERROR = 3;

    void onDataFetched(News n, int pos);
    void onCategoryFetched(String category, News n, int pos);
    void onDataSaved(String lastMarkings, long id);
    void onError(int errorType);
}
