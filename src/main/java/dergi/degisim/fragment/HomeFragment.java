// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.util.Util;
import dergi.degisim.news.News;

public class HomeFragment extends MainFragment implements AdapterView.OnItemClickListener {

    public ArrayList<News> queryItems;

    //d = default, c = category, q = search query
    public char mode = 'd';

    public HomeFragment() {
        u = new Util(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity)getActivity()).categoryList.setOnItemClickListener(this);

        items = new ArrayList<News>();
        catItems = new ArrayList<News>();
        queryItems = new ArrayList<News>();

        for (int i = 0; i < MainFragment.NEWS_AMOUNT; i++) {
            if (u != null)
                u.fetchData("id", i);
        }
    }

    public void performSearchQuery(final String query) {
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

    //onClick func. for list items in catergorylist.
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String[] titles = ((MainActivity)getActivity()).categoryTitles;

        catItems.clear();

        for (int i = 0; i < LOAD_AMOUNT; i++)
            u.fetchCategory(titles[position].toLowerCase(), i);

        mode = 'c';
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(titles[position]);
        currentCategory = titles[position];
        adapter.setNews(catItems);

        ((MainActivity)getActivity()).drawer.closeDrawers();
    }

    @Override
    public void onRefresh() {
        if (mode == 'c') {
            catItems.clear();
            adapter.setNews(catItems);
            for (int i = 0; i < LOAD_AMOUNT; i++) {
                u.fetchCategory(currentCategory, i);
            }
        } else if (mode == 'd'){
            items.clear();
            adapter.setNews(items);
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

        currentCategory = category;
        lastCatFetch = pos;

        srl.setRefreshing(false);
    }

    @Override
    public void onDataSaved(String lastMarkings, long id) {
        this.lastMarkings = lastMarkings;
    }

    @Override
    public void returnDefault() {
        adapter.setNews(items);
        mode = 'd';

        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Değişim Dergisi");
    }

    @Override
    public void loadFeature(int pos) {
        if (mode == 'c') {
            u.fetchCategory(currentCategory, pos);
        } else if (mode == 'd'){
            u.fetchData("id", pos);
        }
    }

    @Override
    public void onStartFeature() {
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Değişim Dergisi");
    }
}