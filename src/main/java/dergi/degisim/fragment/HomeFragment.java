// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import dergi.degisim.news.NewsPaper;
import dergi.degisim.R;
import dergi.degisim.RecyclerAdapter;
import dergi.degisim.news.News;
import dergi.degisim.ItemClickListener;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

public class HomeFragment extends Fragment {
    private RecyclerView rv;
    private RecyclerAdapter adapter;
    LinearLayoutManager m;

    private FirebaseStorage storage;

    private static ArrayList<News> items;
    private boolean end = false;

    public static final int NEWS_AMOUNT = 3;
    public static boolean FETCHED = false;

    public boolean isScrolling = false;

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!isConnected()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("İNTERNET BAĞLANTISI YOK");
            alert.setMessage("İnternet bağlantınızı kontrol edin ve uygulamayı yeniden başlatın");

            alert.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
            });
        }

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

        storage = FirebaseStorage.getInstance();

        rv.setHasFixedSize(true);
        rv.setLayoutManager(m);
        rv.setItemViewCacheSize(20);
        rv.setDrawingCacheEnabled(true);
        rv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        rv.invalidate();

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
                    loadMore(totalItems);
                    isScrolling = false;
                }

                rv.invalidateItemDecorations();
                rv.invalidate();
            }
        });

        if (!FETCHED) {
            items = new ArrayList<>();
        }

        adapter.setNews(items);
        rv.setAdapter(adapter);
        rv.invalidate();
    }

    public void fetchData() {
        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        fs.collection("haberler").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (int i = 0; i < NEWS_AMOUNT; i++) {
                        DocumentSnapshot ds = task.getResult().getDocuments().get(i);

                        News n = new News();
                        n.setTitle(ds.getString("header"));
                        n.setContent(ds.getString("content"));
                        n.setPath(ds.getString("img"));
                        n.formatContent();

                        items.add(n);

                        fetchImage(n, i, true);
                        Log.d("FIRESTORE INFO", n.toString());
                    }
                    Log.d("FIRESTORE INFO", "Size: " + items.size());

                    adapter.setNews(items);
                    rv.setAdapter(adapter);
                    rv.invalidate();
                } else
                    throw new RuntimeException("Datas couldn't got received, check your internet connection.");
            }
        });
    }

    private void fetchImage(final News n, final int pos, final boolean firstFetch) {
        StorageReference s = storage.getReferenceFromUrl("gs://degisim-44155.appspot.com/").child("images/" + n.getPath());
        Log.d("STORAGE INFO", n.getPath());
        s.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                items.get(pos).setUri(uri);
                Log.d("STORAGE INFO", "Got the URI for:" + n.getPath());
                Log.d("STORAGE INFO", "URI:" + uri.toString());

                if (firstFetch) {
                    adapter.setNews(items);
                    rv.setAdapter(adapter);

                    if (pos == NEWS_AMOUNT - 1)
                        FETCHED = true;
                }

                rv.invalidate();

                Log.d("FETCH STATUS", String.valueOf(FETCHED));

            }
        });
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity()
        .getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
            return cm.getActiveNetworkInfo() != null;
    }

    private void loadMore(final int start) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                FirebaseFirestore fs = FirebaseFirestore.getInstance();
                fs.collection("haberler").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {

                        if ((start) >= task.getResult().getDocuments().size())
                            return;

                        DocumentSnapshot ds = task.getResult().getDocuments().get(start);

                        News n = new News();
                        n.setTitle(ds.getString("header"));
                        n.setContent(ds.getString("content"));
                        n.setPath(ds.getString("img"));
                        n.formatContent();

                        adapter.addNews(n);
                        rv.invalidate();

                        fetchImage(n, start, false);
                        Log.d("FIRESTORE INFO", n.toString());
                        Log.d("FIRESTORE INFO", "Size: " + task.getResult().getDocuments().size());
                        Log.d("SCROLL INFO", String.valueOf(start));
                    } else
                        throw new RuntimeException("Datas couldn't got received, check your internet connection.");
                    }
                });
            }
        });
    }
}