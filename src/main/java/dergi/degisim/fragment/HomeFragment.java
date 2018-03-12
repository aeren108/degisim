// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import dergi.degisim.ItemClickListener;
import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.RecyclerAdapter;
import dergi.degisim.news.News;
import dergi.degisim.news.NewsPaper;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

public class HomeFragment extends Fragment implements AdapterView.OnItemClickListener{
    private RecyclerView rv;
    private RecyclerAdapter adapter;
    private LinearLayoutManager m;

    private static ArrayList<News> items;
    public static ArrayList<News> catItems;

    public static final int NEWS_AMOUNT = 3; //Temporary value
    public static final int LOAD_AMOUNT = 2; //Temporary value

    private int lastFetch;
    private int lastCatFetch = 0;
    public boolean isScrolling = false;

    private String currentCategory = "";
    public boolean catMode = false;

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity)getActivity()).categoryList.setOnItemClickListener(this);

        m = new LinearLayoutManager(getContext());
        rv = view.findViewById(R.id.list);
        adapter = new RecyclerAdapter(getActivity(), new ItemClickListener() {

            @Override
            public void onClick(View v, int pos) {
                if (!catMode) {
                    Log.d("NEWS", "Clicked on: " + pos + ". item");
                    Intent intent = new Intent(getActivity(), NewsPaper.class);
                    intent.putExtra("content", items.get(pos).getContent());
                    intent.putExtra("header", items.get(pos).getTitle());
                    intent.putExtra("uri", items.get(pos).getUri());
                    startActivity(intent);
                } else {
                    Log.d("NEWS", "Clicked on: " + pos + ". item");
                    Intent intent = new Intent(getActivity(), NewsPaper.class);
                    intent.putExtra("content", catItems.get(pos).getContent());
                    intent.putExtra("header", catItems.get(pos).getTitle());
                    intent.putExtra("uri", catItems.get(pos).getUri());
                    startActivity(intent);
                }
            }
        });

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
                    if (!catMode) {
                        for (int i = 0; i < LOAD_AMOUNT; i++) {
                            fetchData(lastFetch + 1 + i);
                            Log.d("POS OF FIRST DEBUG", "POS : " + lastFetch);
                        }
                    } else {
                        for (int i = 0; i < LOAD_AMOUNT; i++) {
                            fetchCategory(currentCategory, lastCatFetch + i + 1);
                        }
                    }

                    isScrolling = false;
                }

                rv.invalidateItemDecorations();
                rv.invalidate();
            }
        });

        rv.setHasFixedSize(true);
        rv.setLayoutManager(m);
        rv.setItemViewCacheSize(20);
        rv.setDrawingCacheEnabled(true);
        rv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        rv.invalidate();

        if (items == null) {
            items = new ArrayList<>();
        }
        if (catItems == null) {
            catItems = new ArrayList<>();
        }

        adapter.setNews(items);
        rv.setAdapter(adapter);
        rv.invalidate();
    }

    public void fetchData(final int pos) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                FirebaseFirestore fs = FirebaseFirestore.getInstance();
                Query q = fs.collection("haberler").orderBy("id", Query.Direction.DESCENDING);
                q.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (pos >= documentSnapshots.getDocuments().size())
                            return;

                        DocumentSnapshot ds = documentSnapshots.getDocuments().get(pos);
                        News n = ds.toObject(News.class);
                        n.setTitle(ds.getString("header"));
                        n.formatContent();

                        adapter.addItem(n);
                        rv.invalidate();
                    }
                });

                lastFetch = pos;
            }
        });
    }

    public void fetchCategory(final String category, final int pos) {
        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        fs.collection("haberler").whereEqualTo("category", category).
        orderBy("id", Query.Direction.DESCENDING).
        get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (pos >= documentSnapshots.getDocuments().size())
                    return;

                DocumentSnapshot ds = documentSnapshots.getDocuments().get(pos);
                News n = new News();
                n.setTitle(ds.getString("header"));
                n.setContent(ds.getString("content"));
                n.setUri(ds.getString("uri"));
                n.formatContent();

                Log.d("INFF", n.toString());

                catItems.add(n);
                adapter.setNews(catItems);
                rv.invalidate();
                Log.d("DBB", n.getTitle() );
                adapter.notifyDataSetChanged();

                currentCategory = category;
                lastCatFetch = pos;

                Log.d("POTT", currentCategory);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String[] titles = ((MainActivity)getActivity()).categoryTitles;
        if (!currentCategory.equalsIgnoreCase(titles[position])) {
            if (titles[position].equalsIgnoreCase("Bilim")) {
                for (int i = 0; i < 2; i++)
                    fetchCategory("bilim", i);
            }

            if (titles[position].equalsIgnoreCase("Sanat")) {
                for (int i = 0; i < 2; i++)
                    fetchCategory("sanat", i);
            }
        }

        catItems.clear();

        catMode = true;
        currentCategory = titles[position];     

        ((MainActivity)getActivity()).drawer.closeDrawers();
    }
}
