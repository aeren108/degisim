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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import dergi.degisim.ItemClickListener;
import dergi.degisim.R;
import dergi.degisim.RecyclerAdapter;
import dergi.degisim.news.News;
import dergi.degisim.news.NewsPaper;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

public class HomeFragment extends Fragment {
    private RecyclerView rv;
    private RecyclerAdapter adapter;
    private LinearLayoutManager m;

    private static ArrayList<News> items;

    public static final int NEWS_AMOUNT = 3; //Temporary value
    public static final int LOAD_AMOUNT = 2; //Temporary value
    public static boolean FETCHED = false;

    private int lastFetch;

    public boolean isScrolling = false;

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        m = new LinearLayoutManager(getContext());
        rv = view.findViewById(R.id.list);
        adapter = new RecyclerAdapter(getActivity(), new ItemClickListener() {

            @Override
            public void onClick(View v, int pos) {
                Log.d("NEWS", "Clicked on: " + pos + ". item");
                Intent intent = new Intent(getActivity(), NewsPaper.class);
                intent.putExtra("content", items.get(pos).getContent());
                intent.putExtra("header", items.get(pos).getTitle());
                intent.putExtra("uri", items.get(pos).getUri().toString());
                startActivity(intent);
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
                    for (int i = 0; i < LOAD_AMOUNT; i++) {
                        fetchData(totalItems + i, false);
                        Log.d("POS OF FIRST DEBUG" , "POS : " + lastFetch);
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

        if (!FETCHED) {
            items = new ArrayList<>();
        }

        adapter.setNews(items);
        rv.setAdapter(adapter);
        rv.invalidate();
    }

    public void fetchData(final int pos, final boolean firstFetch) {
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

                if (firstFetch) {
                    FETCHED = true;
                }

                lastFetch = pos;
                Log.d("POS OF" , "POS : " + lastFetch);
            }
        });
    }
}