// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import dergi.degisim.ItemClickListener;
import dergi.degisim.R;
import dergi.degisim.adapter.RecyclerAdapter;
import dergi.degisim.news.News;
import dergi.degisim.util.DataListener;
import dergi.degisim.util.Util;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

// TODO: 12.05.2018 Make 'LAST_MARKINGS' static
public abstract class MainFragment extends Fragment implements DataListener, SwipeRefreshLayout.OnRefreshListener {

    protected FirebaseFirestore fs = FirebaseFirestore.getInstance();
    protected FirebaseAuth auth = FirebaseAuth.getInstance();

    protected List<News> items;
    protected List<News> catItems;
    protected List<String> markeds;

    protected RecyclerView rv;
    protected RecyclerAdapter adapter;
    protected LinearLayoutManager m;
    protected SwipeRefreshLayout srl;

    protected Util u;

    protected int lastFetch;
    protected int lastCatFetch;
    public boolean isScrolling = false;

    protected static String LAST_MARKINGS = "";
    protected String currentCategory = "";
    protected String ID;

    public static final int LOAD_AMOUNT = 2; //Temporary value
    public static final int NEWS_AMOUNT = 3; //Temporary value

    public MainFragment() {
        u = new Util();
        u.setDataListener(this);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        items = new ArrayList<>();
        catItems = new ArrayList<>();

        srl = view.findViewById(R.id.swiper);
        srl.setOnRefreshListener(this);

        m = new LinearLayoutManager(getContext());
        rv = view.findViewById(R.id.list);
        adapter = new RecyclerAdapter(getActivity(), new ItemClickListener() {
            @Override
            public void onClick(View v, int pos) { //LIST ITEMS CLICK LISTENER
                Log.d("NEWS", "Clicked on: " + pos + ". item");
                Util.openNewspaper(getActivity(), adapter.getNews(), pos);
            }
        }, new ItemClickListener() {
            @Override
            public void onClick(View v, int pos) { //SAVE BUTTON LISTENER
                final News n = adapter.getNews().get(pos);

                // Just calling saveNews() func. because it checks if news is bookmarkde or not,
                // if news is already bookmarked it calls unsave() func
                u.saveNews(n);
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

                    for (int i = 0; i < NEWS_AMOUNT; i++) {
                        loadFeature(lastFetch + 1 + i);
                    }

                    isScrolling = false;
                }
            }
        });

        adapter.setNews(items);
        rv.setAdapter(adapter);
        rv.setLayoutManager(m);
    }

    public abstract void returnDefault();

    public abstract void loadFeature(int pos);

    public abstract void onStartFeature();

    @Override
    public void onDataFetched(News n, int pos) {
        for (News news : items) {
            if (news.getID() == n.getID())
                return;
        }

        items.add(n);
        adapter.notifyDataSetChanged();

        lastFetch = pos;

        Log.d("fetch", "Position: " + pos + "ast fetch: " + lastFetch);
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

        currentCategory = category;
        lastCatFetch = pos;

        Log.i("cat_fetch", "Category " + category + " position: " + pos + "\n last category fetch: " + lastCatFetch);
        srl.setRefreshing(false);
    }

    @Override
    public void onDataSaved(String lastMarkings, final News n) {
        MainFragment.LAST_MARKINGS = lastMarkings;

        Snackbar snackbar = Snackbar.make(getView(), "Haber kaydedildi.", Snackbar.LENGTH_SHORT);
        snackbar.setAction("GERİ AL", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                u.unsaveNews(n);
            }
        });
        snackbar.show();
    }

    @Override
    public void onDataUnsaved(String lastMarkings, final News n) {
        MainFragment.LAST_MARKINGS = lastMarkings;

        Snackbar snackbar = Snackbar.make(getView(), "Haber kaydedildi.", Snackbar.LENGTH_SHORT);
        snackbar.setAction("GERİ AL", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                u.saveNews(n);
            }
        });
        snackbar.show();
    }

    @Override
    public void onError(int errorType) {
        if (errorType == DataListener.DATAFETCH_ERROR || errorType == DataListener.CATFETCH_ERROR) {
            Toast.makeText(getContext(), "Veriler yüklenemiyor, interinitini bi' kontrol et", Toast.LENGTH_LONG).show();
        } else if (errorType == DataListener.SAVE_ERROR || errorType == DataListener.UNSAVE_ERROR) {
            Toast.makeText(getContext(), "Haber kaydedilemedi", Toast.LENGTH_LONG).show();
        }
    }
}
