// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import dergi.degisim.news.News;
import dergi.degisim.news.NewsPaper;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

public class HomeFragment extends Fragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView rv;
    private RecyclerAdapter adapter;
    private LinearLayoutManager m;

    private SwipeRefreshLayout srl;

    private static ArrayList<News> items;
    public static ArrayList<News> catItems; //cat represents 'CATegory'
    public ArrayList<News> queryItems;

    public static final int NEWS_AMOUNT = 3; //Temporary value
    public static final int LOAD_AMOUNT = 2; //Temporary value

    private int lastFetch;
    private int lastCatFetch;
    public boolean isScrolling = false;

    private String currentCategory = "";
    public char mode = 'd'; //d = default, c = category, q = search query

    private String lastMarkings = "";

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity)getActivity()).categoryList.setOnItemClickListener(this);

        srl = view.findViewById(R.id.swiper);
        srl.setOnRefreshListener(this);

        m = new LinearLayoutManager(getContext());
        rv = view.findViewById(R.id.list);
        adapter = new RecyclerAdapter(getActivity(), new ItemClickListener() {
            @Override
            public void onClick(View v, int pos) { //LIST ITEMS CLICK LISTENER
                if (mode == 'd') {
                    Log.d("NEWS", "Clicked on: " + pos + ". item");
                    Intent intent = new Intent(getActivity(), NewsPaper.class);
                    intent.putExtra("content", items.get(pos).getContent());
                    intent.putExtra("header", items.get(pos).getTitle());
                    intent.putExtra("uri", items.get(pos).getUri());
                    intent.putExtra("id", items.get(pos).getID());
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                } else if (mode == 'c'){
                    Log.d("NEWS", "Clicked on: " + pos + ". item");
                    Intent intent = new Intent(getActivity(), NewsPaper.class);
                    intent.putExtra("content", catItems.get(pos).getContent());
                    intent.putExtra("header", catItems.get(pos).getTitle());
                    intent.putExtra("uri", catItems.get(pos).getUri());
                    intent.putExtra("id", catItems.get(pos).getID());
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                } else {
                    Log.d("NEWS", "Clicked on: " + pos + ". item");
                    Intent intent = new Intent(getActivity(), NewsPaper.class);
                    intent.putExtra("content", queryItems.get(pos).getContent());
                    intent.putExtra("header", queryItems.get(pos).getTitle());
                    intent.putExtra("uri", queryItems.get(pos).getUri());
                    intent.putExtra("id", queryItems.get(pos).getID());
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            }
        }, new ItemClickListener() {
            @Override
            public void onClick(View v, int pos) { //SAVE BUTTON LISTENER
                News n = adapter.getNews().get(pos);
                saveNews(n);

                Snackbar s = Snackbar.make(view, "Haber Kaydedildi", Snackbar.LENGTH_SHORT);
                s.setAction("Geri Al", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!lastMarkings.equals(""))
                            FirebaseDatabase.getInstance().getReference("users").
                            child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                            child("markeds").setValue(lastMarkings);
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
                            fetchData(lastFetch + 1 + i);
                        }
                    } else if (mode == 'c'){
                        for (int i = 0; i < LOAD_AMOUNT; i++) {
                            fetchCategory(currentCategory, lastCatFetch + i + 1);
                        }
                    } else {

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

        if (items == null)
            items = new ArrayList<>();
        if (catItems == null)
            catItems = new ArrayList<>();
        queryItems = new ArrayList<>();


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
                        if (pos >= documentSnapshots.getDocuments().size()) {
                            Log.d("RET", "Returng index out of bound");
                            return;
                        }

                        DocumentSnapshot ds = documentSnapshots.getDocuments().get(pos);

                        News n = new News();
                        n.setTitle(ds.getString("header"));
                        n.setContent(ds.getString("content"));
                        n.setUri(ds.getString("uri"));
                        n.setID(ds.getLong("id"));
                        n.setRead(ds.getLong("read"));

                        for (News news : items) {
                            if (news.getID() == n.getID())
                                return;
                        }
                        adapter.addItem(n);
                        rv.invalidate();
                    }
                });

                lastFetch = pos;
                srl.setRefreshing(false);
            }
        });
    }

    public void fetchCategory(final String category, final int pos) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
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
                        n.setID(ds.getLong("id"));
                        n.setRead(ds.getLong("read"));

                        Log.d("CAT", "Fetching category: " + pos + " info: \n" + n.toString());

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
                });
            }
        });
    }

    public void performSearchQuery(final String query) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
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
            }
        });

        mode = 'q';
    }

    private void saveNews(final News n) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser usr = auth.getCurrentUser();
        if (usr == null)
            return;

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference ref = db.getReference("users");
        ref.child(usr.getUid()).child("markeds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String buffer;
                List<String> allMarks = null;
                if (dataSnapshot.getValue().equals("empty")) {
                    buffer = "";
                    allMarks = new ArrayList<>();
                }
                else {
                    buffer = (String) dataSnapshot.getValue();
                    allMarks = Arrays.asList(buffer.split(","));
                    lastMarkings = buffer;
                }

                if (allMarks.contains(String.valueOf(n.getID())))
                    return;


                ref.child(usr.getUid()).child("markeds").setValue(n.getID() + "," + buffer);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Kaydedilemedi", Toast.LENGTH_LONG).show();
                ref.removeEventListener(this);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String[] titles = ((MainActivity)getActivity()).categoryTitles;

        catItems.clear();

        for (int i = 0; i < LOAD_AMOUNT; i++)
            fetchCategory(titles[position].toLowerCase(), i);

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
                fetchCategory(currentCategory, i);
            }
        } else if (mode == 'd'){
            items.clear();
            for (int i = 0; i < LOAD_AMOUNT; i++) {
                fetchData(i);
            }
        } else {
            srl.setRefreshing(false);
        }
    }
}