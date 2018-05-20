// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.Arrays;

import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.auth.LoginActivity;
import dergi.degisim.news.News;
import dergi.degisim.util.Util;

public class MarkedFragment extends MainFragment {


    //A TextView which shows up when there is no bookmarked news
    private TextView empty;

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

        empty = view.findViewById(R.id.empty);
    }

    public void loadMarkedNews(final int pos) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("users").child(String.valueOf(ID)).child("markeds");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String allMarkeds = (String) dataSnapshot.getValue();
                if (!allMarkeds.equals("empty") || !allMarkeds.isEmpty()) {
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
        if (Util.checkLoggedIn()) {
            items.clear();
            for (int i = 0; i < MainFragment.LOAD_AMOUNT; i++)
                loadMarkedNews(i);
        } else {
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
        }
    }

    @Override
    public void onCategoryFetched(String category, News n, int pos) {
        //There is no categorization feature in MarkedFragment so do nothing
    }

    @Override
    public void returnDefault() {
        // TODO: 12.05.2018 Handle returnDefault func. of marked fragment
    }

    @Override
    public void loadFeature(int pos) {
        if (Util.checkLoggedIn()) {
            loadMarkedNews(pos);
        }
    }

    @Override
    public void onStartFeature() {
        ((MainActivity) getActivity()).categoryList.setOnItemClickListener(null);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Kaydedilenler");

        if (Util.checkLoggedIn()) {
            for (int i = 0; i < MainFragment.LOAD_AMOUNT; i++)
                loadMarkedNews(i);
        } else {
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
        }
    }
}