// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dergi.degisim.ItemClickListener;
import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.RecyclerAdapter;
import dergi.degisim.database.DataListener;
import dergi.degisim.database.Util;
import dergi.degisim.news.News;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

public class HomeFragment extends Fragment implements AdapterView.OnItemClickListener,
                                                      SwipeRefreshLayout.OnRefreshListener,
                                                      DataListener, FragmentFeature {
    private RecyclerView rv;
    private RecyclerAdapter adapter;
    private LinearLayoutManager m;

    private SwipeRefreshLayout srl;

    private ArrayList<News> items;
    public ArrayList<News> catItems; //cat represents 'CATegory'
    public ArrayList<News> queryItems;

    public Util u;

    public static final int NEWS_AMOUNT = 3; //Temporary value
    public static final int LOAD_AMOUNT = 2; //Temporary value

    private volatile int lastFetch;
    private volatile int lastCatFetch;
    public boolean isScrolling = false;

    private String currentCategory = "";
    public char mode = 'd'; //d = default, c = category, q = search query

    public String lastMarkings = "";

    public HomeFragment() {
        u = new Util(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        srl = view.findViewById(R.id.swiper);
        srl.setOnRefreshListener(this);

        m = new LinearLayoutManager(getContext());
        rv = view.findViewById(R.id.list);
        adapter = new RecyclerAdapter(getActivity(), new ItemClickListener() {
            @Override
            public void onClick(View v, int pos) { //LIST ITEMS CLICK LISTENER
                if (mode == 'd') {
                    Log.d("NEWS", "Clicked on: " + pos + ". item");
                    Util.openNewspaper(getActivity(), items, pos);
                } else if (mode == 'c'){
                    Log.d("NEWS", "Clicked on: " + pos + ". item");
                    Util.openNewspaper(getActivity(), catItems, pos);
                } else {
                    Log.d("NEWS", "Clicked on: " + pos + ". item");
                    Util.openNewspaper(getActivity(), queryItems, pos);
                }
            }
        }, new ItemClickListener() {
            @Override
            public void onClick(View v, int pos) { //SAVE BUTTON LISTENER
                final List<String> marks = Arrays.asList(lastMarkings.split(","));

                final News n = adapter.getNews().get(pos);
                String snackbar;

                if (!marks.contains(String.valueOf(n.getID()))) {
                    u.saveNews(n);
                    snackbar = "Haber kaydedildi";
                } else {
                    u.unsaveNews(n);
                    snackbar = " Haber kaydedilenlerden çıkarıldı";
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
                    if (mode == 'd') {
                        for (int i = 0; i < LOAD_AMOUNT; i++) {
                            Log.d("FETCH", "ID: " + lastFetch + 1 + i);
                            u.fetchData("id", lastFetch + 1 + i);
                        }
                    } else if (mode == 'c'){
                        for (int i = 0; i < LOAD_AMOUNT; i++) {
                            u.fetchCategory(currentCategory, lastCatFetch + i + 1);
                        }
                    }
                    isScrolling = false;
                }
            }
        });

        rv.setHasFixedSize(true);
        rv.setLayoutManager(m);
        rv.setItemViewCacheSize(20);
        rv.setDrawingCacheEnabled(true);
        rv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        rv.invalidate();

        items = new ArrayList<>();
        catItems = new ArrayList<>();
        queryItems = new ArrayList<>();

        adapter.setNews(items);
        rv.setAdapter(adapter);
        rv.invalidate();

        for (int i = 0; i < HomeFragment.NEWS_AMOUNT; i++) {
            if (u != null)
                u.fetchData("id", i);
        }
    }

    public void performSearchQuery(final String query) {

        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        final Query q = fs.collection("haberler").orderBy("id", Query.Direction.DESCENDING);
        q.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                Log.d("QUERY", "Searching for: " + query);

                for (DocumentSnapshot ds : documentSnapshots) {
                    String toSearch = ds.getString("header").toLowerCase();

                    if (toSearch.contains(query.toLowerCase())) {
                        Log.d("FOUND", "Found news: " + toSearch);

                        News n = new News();
                        n.setTitle(ds.getString("header"));
                        n.setContent(ds.getString("content"));
                        n.setUri(ds.getString("uri"));
                        n.setID(ds.getLong("id"));
                        n.setRead(ds.getLong("read"));

                        queryItems.add(n);
                        adapter.setNews(queryItems);
                        rv.setAdapter(adapter);
                        rv.invalidate();
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Haberleri bulamadık :(", Toast.LENGTH_LONG).show();
            }
        });

        mode = 'q';
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String[] titles = ((MainActivity)getActivity()).categoryTitles;

        catItems.clear();

        for (int i = 0; i < LOAD_AMOUNT; i++)
            u.fetchCategory(titles[position].toLowerCase(), i);

        mode = 'c';
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(titles[position]);
        currentCategory = titles[position];

        ((MainActivity)getActivity()).drawer.closeDrawers();
    }

    @Override
    public void onRefresh() {
        if (mode == 'c') {
            catItems.clear();
            for (int i = 0; i < LOAD_AMOUNT; i++) {
                u.fetchCategory(currentCategory, i);
            }
        } else if (mode == 'd'){
            items.clear();
            for (int i = 0; i < LOAD_AMOUNT; i++) {
                u.fetchData("id", i);
            }
        } else {
            srl.setRefreshing(false);
        }
    }

    @Override
    public void onDataFetched(News n, int pos) {
        for (News news : items) {
            if (news.getID() == n.getID())
                return;
        }

        adapter.addItem(n);
        rv.invalidate();

        lastFetch = pos;

        Log.d("DB", "Last fetch: " + lastFetch);
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
        ((MainActivity)getActivity()).categoryList.setOnItemClickListener(this);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Değişim Dergisi");
    }
}