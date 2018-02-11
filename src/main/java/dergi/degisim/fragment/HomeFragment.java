// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

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
    LinearLayoutManager m;

    private FirebaseStorage storage;

    private static ArrayList<News> items;

    public static final int NEWS_AMOUNT = 3; //Temporary value
    public static final int LOAD_AMOUNT = 2; //Temporary value
    public static boolean FETCHED = false;

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
                    }
                    isScrolling = false;
                }

                rv.invalidateItemDecorations();
                rv.invalidate();
            }
        });

        storage = FirebaseStorage.getInstance();

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
                fs.collection("haberler").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if ((pos) >= task.getResult().getDocuments().size())
                            return;

                        DocumentSnapshot ds = task.getResult().getDocuments().get(pos);

                        News n = new News();
                        n.setTitle(ds.getString("header"));
                        n.setContent(ds.getString("content"));
                        n.setPath(ds.getString("img"));
                        n.setUri(Uri.parse(ds.getString("uri")));
                        n.formatContent();

                        adapter.addItem(n);
                        rv.invalidate();

                        if (firstFetch && pos == NEWS_AMOUNT - 1) {
                            FETCHED = true;
                        }

                        Log.d("FIRESTORE INFO", n.toString());
                        Log.d("FIRESTORE INFO", "Size: " + task.getResult().getDocuments().size());
                        Log.d("SCROLL INFO", String.valueOf(pos));
                    } else
                        return;
                    }
                });
            }
        });
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity()
        .getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
            return cm.getActiveNetworkInfo() != null;
    }
}