// -*- @author aeren_pozitif  -*- //
package dergi.degisim.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import java.util.Arrays;
import java.util.List;

import dergi.degisim.R;
import dergi.degisim.auth.LoginActivity;
import dergi.degisim.news.News;

//TODO: Implement recycler view
public class MarkedFragment extends Fragment {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private String id;
    private List<String> markeds;

    private TextView empty;

    public MarkedFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_markeds, container, false);

        empty = v.findViewById(R.id.empty);

        if (checkLoggedIn())
            loadMarkedNews();

        return v;
    }

    private boolean checkLoggedIn() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.isAnonymous()) {
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

    private void loadMarkedNews() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("users").child(id);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String allMarkeds = (String) dataSnapshot.child("marks").getValue();
                if (!allMarkeds.equals("empty")) {
                    empty.setVisibility(TextView.INVISIBLE);
                    String[] seperatedMarks = allMarkeds.split(",");
                    markeds = Arrays.asList(seperatedMarks);
                } else {
                    empty.setVisibility(TextView.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        fs.collection("haberler").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for (int i = 0; i < markeds.size();i++) {
                    int id = Integer.parseInt(markeds.get(i));
                    DocumentSnapshot ds = documentSnapshots.getDocuments().get(id);

                    News n = new News();
                    n.setTitle(ds.getString("header"));
                    n.setContent(ds.getString("content"));
                    n.setUri(ds.getString("uri"));
                    n.setID(ds.getLong("id"));
                    n.setRead(ds.getLong("read"));

                    news.add(n);
                    //TODO:Set the list of recycler adapter
                }
            }
        });
    }
}