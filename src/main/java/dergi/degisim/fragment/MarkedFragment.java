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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dergi.degisim.ItemClickListener;
import dergi.degisim.R;
import dergi.degisim.RecyclerAdapter;
import dergi.degisim.auth.LoginActivity;
import dergi.degisim.news.News;
import dergi.degisim.news.NewsPaper;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

//TODO: Implement recycler view
public class MarkedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private String id;
    private List<String> markeds;
    private List<News> items;

    private TextView empty;
    private RecyclerView rv;
    private RecyclerAdapter adapter;
    private LinearLayoutManager m;
    private SwipeRefreshLayout srl;

    private boolean isScrolling;
    private int lastFetch;

    public MarkedFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_markeds, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (checkLoggedIn()) {
            loadMarkedNews();
            for (int i = 0; i <= HomeFragment.NEWS_AMOUNT; i++) {
                fetchData(i);
            }
        }

        empty = view.findViewById(R.id.empty);
        items = new ArrayList<>();

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
                News n = adapter.getNews().get(pos);
                final String[] data = unsaveNews(n); //data array is storing mark datas before marking and after marking

                Snackbar s = Snackbar.make(view, "Haber Kaydedildi", Snackbar.LENGTH_SHORT);
                s.setAction("Geri Al", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseDatabase.getInstance().getReference("users").
                                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                                child("markeds").setValue(data[1]);
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
                        fetchData(lastFetch + 1 + i);
                    }

                    isScrolling = false;
                }
            }
        });

        if (checkLoggedIn())
            for (int i = 0; i < HomeFragment.LOAD_AMOUNT; i++)
                fetchData(i);

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

    private String[] unsaveNews(News n) {
        String[] markeds = new String[2];

        return markeds;
    }

    private void loadMarkedNews() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("users").child(String.valueOf(id)).child("markeds");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String allMarkeds = (String) dataSnapshot.getValue();
                if (!allMarkeds.equals("empty")) {
                    empty.setVisibility(TextView.INVISIBLE);
                    String[] seperatedMarks = allMarkeds.split(",");
                    markeds = Arrays.asList(seperatedMarks);
                    Log.d("MARK", "Load is complete");
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

    private void fetchData(final int pos) {
        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        fs.collection("haberler").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (pos >= documentSnapshots.getDocuments().size() || markeds == null) {
                    Log.d("MARK", "Pos is higher than list size or markeds list is null");
                    srl.setRefreshing(false);
                    return;
                }

                DocumentSnapshot ds = documentSnapshots.getDocuments().get(Integer.parseInt(markeds.get(pos)));

                News n = new News();
                n.setTitle(ds.getString("header"));
                n.setContent(ds.getString("content"));
                n.setUri(ds.getString("uri"));
                n.setID(ds.getLong("id"));
                n.setRead(ds.getLong("read"));

                for (News nw : items)
                    if (items.contains(nw))
                        return;

                items.add(n);
                adapter.setNews(items);
                rv.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                rv.invalidate();

                lastFetch = pos;
                srl.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        if (checkLoggedIn()) {
            loadMarkedNews();
            items.clear();
            for (int i = 0; i <= HomeFragment.NEWS_AMOUNT; i++)
                fetchData(i);
        }
    }
}