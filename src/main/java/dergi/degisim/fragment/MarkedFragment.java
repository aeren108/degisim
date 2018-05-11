// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.database.Util;
import dergi.degisim.news.News;

public class MarkedFragment extends MainFragment {

    public MarkedFragment() {
        u = new Util(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_markeds, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        items = new ArrayList<>();
    }

    public void loadMarkedNews(final int pos) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("users").child(String.valueOf(ID)).child("markeds");
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
                    } else {
                        srl.setRefreshing(false);
                        empty.setVisibility(TextView.VISIBLE);
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
        if (checkLoggedIn(true)) {
            items.clear();
            for (int i = 0; i < MainFragment.LOAD_AMOUNT; i++)
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
    public void onDataSaved(String lastMarkings, long id) {}

    @Override
    public void loadFeature(int pos) {
        loadMarkedNews(pos);
    }

    @Override
    public void onStartFeature() {
        ((MainActivity) getActivity()).categoryList.setOnItemClickListener(null);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Kaydedilenler");

        if (checkLoggedIn(true))
            for (int i = 0; i < MainFragment.LOAD_AMOUNT; i++)
                loadMarkedNews(i);
    }

    @Override
    public void openNewspaper() {

    }
}