// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.auth.LoginActivity;
import dergi.degisim.news.News;
import dergi.degisim.db.Database;

public class MarkedFragment extends MainFragment {

    //A TextView which shows up when there is no bookmarked news
    private TextView empty;
    private FrameLayout frame;

    public MarkedFragment() {
        db = new Database(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_markeds, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        empty = view.findViewById(R.id.empty);
        frame = view.findViewById(R.id.frame);

        mode = MainFragment.DEFAULT;
    }

    public void loadMarkedNews(final int pos) {
        if (!MainFragment.LAST_MARKINGS.isEmpty()) {
            if (frame != null && empty != null)
                frame.removeView(empty);
            if (pos < MainFragment.LAST_MARKINGS.size()) {
                try {
                    db.fetchData(Integer.parseInt(MainFragment.LAST_MARKINGS.get(pos)));
                    lastFetch = pos;
                } catch (NumberFormatException e) {
                    srl.setRefreshing(false);
                }
            } else {
                srl.setRefreshing(false);
            }
        } else {
            srl.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        items.clear();
        if (Database.checkLoggedIn()) {
            for (int i = 0; i < MainFragment.LOAD_AMOUNT; i++)
                loadMarkedNews(i);
            frame.removeView(empty);
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Kullanıcı Girişi Yok");
            alert.setMessage("Kullanıcı girişi yapılmadığından dolayı kaydedilenler gösterilemiyor.");
            alert.setPositiveButton("Tamam", null).setNegativeButton("Giriş Yap", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseAuth.getInstance().removeAuthStateListener(((MainActivity)getActivity()));
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    getActivity().finish();
                }
            }).show();
            srl.setRefreshing(false);
        }
    }

    @Override
    public void onCategoryFetched(String category, News n, int pos) {
        //There is no categorization feature in MarkedFragment so do nothing
    }

    @Override
    public void returnDefault() {
        onRefresh();
    }

    @Override
    public void loadFeature(int pos) {
        if (Database.checkLoggedIn()) {
            loadMarkedNews(pos);
        }
    }

    @Override
    public void onStartFeature() {
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Kaydedilenler");

        if (Database.checkLoggedIn()) {
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
    public void onDataFetched(News n, int pos) {
        for (News news : items) {
            if (news.getID() == n.getID())
                return;
        }

        items.add(n);
        adapter.setNews(items);

        Log.d("fetch", "Position: " + pos + "th fetch: " + lastFetch);
        srl.setRefreshing(false);
    }

    @Override
    public void onDataSaved(String lastMarkings, News n) {
        super.onDataSaved(lastMarkings, n);
        onRefresh();
    }

    @Override
    public void onDataUnsaved(String lastMarkings, News n) {
        super.onDataUnsaved(lastMarkings, n);
        onRefresh();
    }
}