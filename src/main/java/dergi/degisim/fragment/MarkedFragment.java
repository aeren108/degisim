// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dergi.degisim.ItemClickListener;
import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.RecyclerAdapter;
import dergi.degisim.auth.LoginActivity;
import dergi.degisim.database.DataListener;
import dergi.degisim.database.Utilities;
import dergi.degisim.news.News;
import dergi.degisim.news.NewsPaper;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

public class MarkedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
                                                        DataListener{
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private String id;
    private List<String> markeds;
    private static List<News> items;

    private TextView empty;
    private RecyclerView rv;
    private RecyclerAdapter adapter;
    private LinearLayoutManager m;
    private SwipeRefreshLayout srl;

    private Utilities u;

    private boolean isScrolling;
    private int lastFetch;
    private String lastMarkings = "";

    public MarkedFragment() {
        u = new Utilities(getContext(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_markeds, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).categoryList.setOnItemClickListener(null);

        if (items == null) {
            items = new ArrayList<>();

            if (checkLoggedIn()) {
                for (int i = 0; i <= HomeFragment.NEWS_AMOUNT; i++) {
                    loadMarkedNews(i);
                }
            }
        }

        empty = view.findViewById(R.id.empty);

        srl = view.findViewById(R.id.swiper);
        srl.setOnRefreshListener(this);

        m = new LinearLayoutManager(getContext());
        rv = view.findViewById(R.id.list);
        adapter = new RecyclerAdapter(getActivity(), new ItemClickListener() {
            @Override
            public void onClick(View v, int pos) { //LIST ITEMS CLICK LISTENER
                Log.d("NEWS", "Clicked on: " + pos + ". item");
                Intent intent = new Intent(getActivity(), NewsPaper.class);
                intent.putExtra("content", items.get(pos).getContent());
                intent.putExtra("header", items.get(pos).getTitle());
                intent.putExtra("uri", items.get(pos).getUri());
                intent.putExtra("id", items.get(pos).getID());
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        }, new ItemClickListener() {
            @Override
            public void onClick(View v, int pos) { //SAVE BUTTON LISTENER
                final News n = adapter.getNews().get(pos);
                u.unsaveNews(n);

                Snackbar s = Snackbar.make(view, "Haber kaydedilenlerden çıkarıldı", Snackbar.LENGTH_SHORT);
                s.setAction("Geri Al", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        u.saveNews(n);
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

                    for (int i = 0; i < HomeFragment.NEWS_AMOUNT; i++) {
                        loadMarkedNews(lastFetch + 1 + i);
                    }

                    isScrolling = false;
                }
            }
        });

        if (checkLoggedIn())
            for (int i = 0; i < HomeFragment.LOAD_AMOUNT; i++)
                loadMarkedNews(i);

        rv.setHasFixedSize(true);
        rv.setLayoutManager(m);
        rv.setItemViewCacheSize(20);
        rv.setDrawingCacheEnabled(true);
        rv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        rv.invalidate();

        adapter.setNews(items);
        rv.setAdapter(adapter);
        rv.invalidate();
    }

    private boolean checkLoggedIn() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Kullanıcı Girişi Yok");
            alert.setMessage("Kullanıcı girişi yapılmadığından dolayı kaydedilenler gösterilemiyor.");
            alert.setPositiveButton("Tamam", null).setNegativeButton("Giriş Yap", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    getActivity().finish();
                }
            }).show();
            return false;
        }

        id = user.getUid();
        Log.d("DEBUG", "ID: " + id);
        return true;
    }

    public void loadMarkedNews(final int pos) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("users").child(String.valueOf(id)).child("markeds");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String allMarkeds = (String) dataSnapshot.getValue();
                if (!allMarkeds.equals("empty")) {
                    empty.setVisibility(TextView.INVISIBLE);
                    markeds = Arrays.asList(allMarkeds.split(","));
                    Log.d("MARK", markeds.toString());
                    if (pos < markeds.size()) {
                        try {
                            u.fetchData(Integer.parseInt(markeds.get(pos)));
                            lastFetch = pos;
                        } catch (NumberFormatException e) {
                            srl.setRefreshing(false);
                            empty.setVisibility(TextView.VISIBLE);
                        }
                    }
                } else {
                    srl.setRefreshing(false);
                    empty.setVisibility(TextView.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                srl.setRefreshing(false);
            }
        });
    }


    @Override
    public void onRefresh() {
        if (checkLoggedIn()) {
            items.clear();
            for (int i = 0; i < HomeFragment.LOAD_AMOUNT; i++)
                loadMarkedNews(i);
        }
    }

    @Override
    public void onDataFetched(News n, int pos) {
        for (News nw : items) {
            if (nw.getID() == n.getID())
                return;
        }

        adapter.addItem(n);
        rv.invalidate();

        srl.setRefreshing(false);
    }

    @Override
    public void onCategoryFetched(String category, News n, int pos) {

    }

    @Override
    public void onDataSaved(String lastMarkings, long id) {
        this.lastMarkings = lastMarkings;
    }
}