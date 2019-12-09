// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.db.Database;

public class WeeklyFragment extends MainFragment {

    public WeeklyFragment() {
        db = new Database(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (int i = 0; i < MainFragment.NEWS_AMOUNT; i++) {
            db.fetchData("read", i);
        }
    }

    @Override
    public void onRefresh() {
        if (mode == MainFragment.CATEGORY) {
            catItems.clear();
            adapter.setNews(catItems);
            for (int i = 0; i < MainFragment.LOAD_AMOUNT; i++) {
                db.fetchCategory(currentCategory, "read", i);
            }
        } else if (mode == MainFragment.DEFAULT) {
            items.clear();
            adapter.setNews(items);
            for (int i = 0; i < MainFragment.LOAD_AMOUNT; i++) {
                db.fetchData("read", i);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        super.onNavigationItemSelected(item);

        if (item.getItemId() == R.id.all) {
            adapter.setNews(items);
            mode = MainFragment.DEFAULT;

            ((MainActivity)getActivity()).getSupportActionBar().setTitle("Haftanın Enleri");
            item.setChecked(true);
            return  true;
        } else {
            for (int id : CATEGORIES) {
                if (item.getItemId() == id) {
                    String category = item.getTitle().toString().toLowerCase();

                    catItems.clear();

                    for (int i = 0; i < LOAD_AMOUNT; i++)
                        db.fetchCategory(category, "read", i);

                    mode = MainFragment.CATEGORY;
                    ((MainActivity) getActivity()).getSupportActionBar().setTitle(item.getTitle().toString());
                    currentCategory = category;
                    adapter.setNews(catItems);

                    item.setChecked(true);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void returnDefault() {
        adapter.setNews(items);
        mode = MainFragment.DEFAULT;

        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Haftanın Enleri");
    }

    @Override
    public void loadFeature(int pos) {
        if (mode == MainFragment.CATEGORY) {
            db.fetchCategory(currentCategory, "read", pos);
        } else if (mode == MainFragment.DEFAULT) {
            db.fetchData("read", pos);
        }
    }

    @Override
    public void onStartFeature() {
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Haftanın Enleri");
    }
}
