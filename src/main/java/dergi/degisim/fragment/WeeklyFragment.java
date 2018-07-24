// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.util.Util;

public class WeeklyFragment extends MainFragment {

    public boolean catMode;

    public WeeklyFragment() {
        u = new Util(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (int i = 0; i < MainFragment.NEWS_AMOUNT; i++) {
            u.fetchData("read", i);
        }
    }

    @Override
    public void onRefresh() {
        if (catMode) {
            catItems.clear();
            adapter.setNews(catItems);
            for (int i = 0; i < MainFragment.LOAD_AMOUNT; i++) {
                u.fetchCategory(currentCategory, "read", i);
            }
        } else {
            items.clear();
            adapter.setNews(items);
            for (int i = 0; i < MainFragment.LOAD_AMOUNT; i++) {
                u.fetchData("read", i);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        super.onNavigationItemSelected(item);

        if (item.getItemId() == R.id.all) {
            adapter.setNews(items);
            catMode = false;

            ((MainActivity)getActivity()).getSupportActionBar().setTitle("Haftanın Enleri");
            item.setChecked(true);
            return  true;
        } else {
            for (int id : CATEGORIES) {
                if (item.getItemId() == id) {
                    String category = item.getTitle().toString().toLowerCase();

                    catItems.clear();

                    for (int i = 0; i < LOAD_AMOUNT; i++)
                        u.fetchCategory(category, "read", i);

                    catMode = true;
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
        catMode = false;

        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Haftanın Enleri");
    }

    @Override
    public void loadFeature(int pos) {
        if (catMode) {
            u.fetchCategory(currentCategory, "read", pos);
        } else {
            u.fetchData("read", pos);
        }
    }

    @Override
    public void onStartFeature() {
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Haftanın Enleri");
    }
}
