// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import dergi.degisim.news.News;

public class HomeFragment extends MainFragment {

    public ArrayList<News> queryItems;

    public HomeFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queryItems = new ArrayList<>();
    }

    public void performSearchQuery(final String query) {
        queryItems.clear();

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
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Haberleri bulamadık :(", Toast.LENGTH_LONG).show();
            }
        });

        mode = MainFragment.SEARCH;
    }

    @Override
    public void onRefresh() {
        if (mode == MainFragment.CATEGORY) {
            catItems.clear();
            adapter.setNews(catItems);
            for (int i = 0; i < LOAD_AMOUNT; i++) {
                u.fetchCategory(currentCategory, "id", i);
            }
        } else if (mode == MainFragment.DEFAULT){
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
    public void returnDefault() {
        adapter.setNews(items);
        mode = MainFragment.DEFAULT;

        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Değişim Dergisi");
    }

    @Override
    public void loadFeature(int pos) {
        if (mode == MainFragment.CATEGORY) {
            u.fetchCategory(currentCategory, "id", pos);
        } else if (mode == MainFragment.DEFAULT){
            u.fetchData("id", pos);
        }
    }

    @Override
    public void onDataFetched(News n, int pos) {
        super.onDataFetched(n, pos);
    }

    @Override
    public void onStartFeature() {
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Değişim");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        super.onNavigationItemSelected(item);

        if (item.getItemId() == R.id.all) {
            adapter.setNews(items);
            mode = MainFragment.DEFAULT;

            ((MainActivity)getActivity()).getSupportActionBar().setTitle("Değişim");

            item.setChecked(true);
            return  true;
        } else {
            for (int id : CATEGORIES) {
                if (item.getItemId() == id) {
                    String category = item.getTitle().toString().toLowerCase();
                    Log.d("CATD", item.getTitle().toString());
                    catItems.clear();

                    for (int i = 0; i < LOAD_AMOUNT; i++)
                        u.fetchCategory(category, "id", i);

                    mode = MainFragment.CATEGORY;
                    ((MainActivity) getActivity()).getSupportActionBar().setTitle(item.getTitle().toString());
                    currentCategory = category;
                    adapter.setNews(catItems);

                    item.setChecked(true);
                    return true;
                }
            }
        }
        return false;
    }
}