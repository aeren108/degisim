// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;

import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.util.Util;
import dergi.degisim.news.News;

public class WeeklyFragment extends MainFragment implements AdapterView.OnItemClickListener {

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

        catItems = new ArrayList<News>();

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
                u.fetchCategory(currentCategory, i);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String[] titles = ((MainActivity)getActivity()).categoryTitles;

        catItems.clear();
        catMode = true;

        for (int i = 0; i < MainFragment.LOAD_AMOUNT; i++)
            u.fetchCategory(titles[position].toLowerCase(), i);

        currentCategory = titles[position];
        adapter.setNews(catItems);

        ((MainActivity)getActivity()).drawer.closeDrawers();
    }

    @Override
    public void onDataFetched(News n, int pos) {
        for (News news : items) {
            if (news.getID() == n.getID())
                return;
        }

        items.add(n);
        adapter.setNews(items);
        adapter.notifyDataSetChanged();
        rv.invalidate();

        lastFetch = pos;
        srl.setRefreshing(false);
    }

    @Override
    public void onCategoryFetched(String category, News n, int pos) {
        for (News news : catItems) {
            if (news.getID() == n.getID())
                return;
        }

        catItems.add(n);
        adapter.setNews(catItems);
        rv.invalidate();
        adapter.notifyDataSetChanged();

        currentCategory = category;
        lastCatFetch = pos;

        srl.setRefreshing(false);
    }

    @Override
    public void onDataSaved(String lastMarkings, long id) {
        this.lastMarkings = lastMarkings;
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
            u.fetchCategory(currentCategory, pos);
        } else {
            u.fetchData("read", pos);
        }
    }

    @Override
    public void onStartFeature() {
        ((MainActivity) getActivity()).categoryList.setOnItemClickListener(this);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Haftanın Enleri");
    }
}
