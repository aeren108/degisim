// -*- @author aeren_pozitif  -*- //
package dergi.degisim.util;

import dergi.degisim.news.News;

public interface DataListener {
    int DATAFETCH_ERROR = 0;
    int CATFETCH_ERROR = 1;
    int SAVE_ERROR = 2;
    int UNSAVE_ERROR = 3;

    void onDataFetched(News n, int pos);
    void onCategoryFetched(String category, News n, int pos);
    void onDataSaved(String lastMarkings, News n);
    void onDataUnsaved(String lastMarkings, News n);
    void onError(int errorType);
}
