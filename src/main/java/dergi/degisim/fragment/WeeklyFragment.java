// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import dergi.degisim.ItemClickListener;
import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.RecyclerAdapter;
import dergi.degisim.database.DataListener;
import dergi.degisim.database.Util;
import dergi.degisim.news.News;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

public class WeeklyFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
                                                        AdapterView.OnItemClickListener,
                                                        DataListener, FragmentFeature{

    private RecyclerView rv;
    private RecyclerAdapter adapter;
    private LinearLayoutManager m;

    private SwipeRefreshLayout srl;

    private ArrayList<News> items;
    public ArrayList<News> catItems; //cat represents 'CATegory'

    private Util u;

    private int lastFetch;
    private int lastCatFetch;

    public boolean isScrolling = false;
    public boolean catMode = false;

    private String currentCategory = "";
    private String lastMarkings = "";

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

        srl = view.findViewById(R.id.swiper);
        srl.setOnRefreshListener(this);
        srl.setColorSchemeColors(android.R.color.holo_blue_bright,
                                 android.R.color.holo_green_light,
                                 android.R.color.holo_orange_light,
                                 android.R.color.holo_red_light);

        m = new LinearLayoutManager(getContext());
        rv = view.findViewById(R.id.list);
        adapter = new RecyclerAdapter(getActivity(), new ItemClickListener() {

            @Override
            public void onClick(View v, int pos) { //LIST ITEMS CLICK LISTENER
                if (!catMode) {
                    Log.d("NEWS", "Clicked on: " + pos + ". item");
                    Util.openNewspaper(getActivity(), items, pos);
                } else {
                    Log.d("NEWS", "Clicked on: " + pos + ". item");
                    Util.openNewspaper(getActivity(), catItems, pos);
                }
            }
        }, new ItemClickListener() {
            @Override
            public void onClick(View v, int pos) { //SAVE BUTTON LISTENER
                final ArrayList<String> marks = (ArrayList<String>) Arrays.asList(lastMarkings.split(","));
                final News n = adapter.getNews().get(pos);
                String snackbar;

                if (!marks.contains(String.valueOf(n.getID()))) {
                    u.saveNews(n);
                    snackbar = " Haber kaydedilenlerden çıkarıldı";
                } else {
                    u.unsaveNews(n);
                    snackbar = "Haber kaydedildi";
                }

                Snackbar s = Snackbar.make(view, snackbar, Snackbar.LENGTH_SHORT);
                s.setAction("Geri Al", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (marks.contains(String.valueOf(n.getID()))) {
                            u.saveNews(n);
                        } else {
                            u.unsaveNews(n);
                        }
                    }
                });
                s.setActionTextColor(Color.YELLOW);
                s.show();
            }

        });

        //SCROLL LISTENER
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_TOUCH_SCROLL)
                    isScrolling = true;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItems = m.getItemCount();
                int visibleItems = m.getChildCount();
                int outItems = m.findFirstVisibleItemPosition();

                if (isScrolling && (visibleItems + outItems) == totalItems) {
                    if (catMode) {
                        for (int i = 0; i < HomeFragment.LOAD_AMOUNT; i++)
                            u.fetchCategory(currentCategory, lastCatFetch + 1 + i);
                    } else {
                        for (int i = 0; i < HomeFragment.LOAD_AMOUNT; i++)
                            u.fetchData("read", lastFetch + 1 + i);
                    }

                    isScrolling = false;
                }
            }
        });

        rv.setHasFixedSize(true);
        rv.setLayoutManager(m);

        items = new ArrayList<>();
        catItems = new ArrayList<>();

        adapter.setNews(items);
        rv.setAdapter(adapter);
        rv.invalidate();

        for (int i = 0; i < HomeFragment.NEWS_AMOUNT; i++) {
            u.fetchData("read", i);
        }
    }

    @Override
    public void onRefresh() {
        if (catMode) {
            catItems.clear();
            for (int i = 0; i < HomeFragment.LOAD_AMOUNT; i++) {
                u.fetchCategory(currentCategory, i);
            }
        } else {
            items.clear();
            for (int i = 0; i < HomeFragment.LOAD_AMOUNT; i++) {
                u.fetchData("read", i);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String[] titles = ((MainActivity)getActivity()).categoryTitles;

        catItems.clear();
        catMode = true;

        for (int i = 0; i < HomeFragment.LOAD_AMOUNT; i++)
            u.fetchCategory(titles[position].toLowerCase(), i);

        currentCategory = titles[position];

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
    public void onError(int errorType) {
        if (errorType == Util.DATAFETCH_ERROR || errorType == Util.CATFETCH_ERROR) {
            Toast.makeText(getContext(), "Veriler yüklenemiyor, interinitini bi' kontrol et", Toast.LENGTH_LONG).show();
        } else if (errorType == Util.SAVE_ERROR || errorType == Util.UNSAVE_ERROR) {
            Toast.makeText(getContext(), "Haber kaydedilemedi", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void loadFeature() {
        ((MainActivity) getActivity()).categoryList.setOnItemClickListener(this);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Haftanın Enleri");
    }
}
