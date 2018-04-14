// -*- @author aeren_pozitif  -*- //
package dergi.degisim;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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

import dergi.degisim.news.News;

public class Utilities {
    private DataListener dataListener;
    private Context context;

    private final FirebaseFirestore fs;
    private final FirebaseAuth auth;
    private final FirebaseDatabase db;

    private static String lastMarkings;

    public Utilities(Context context) {
        fs = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        this.context = context;
    }

    public Utilities(Context context, DataListener dataListener) {
        fs = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        this.context = context;
        this.dataListener = dataListener;
    }

    private boolean checkLoggedIn() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null)
            return false;
        else if (user.isAnonymous())
            return false;

        Log.d("DB", "ID: " + user.getUid());
        return true;
    }

    public void fetchData(String query, final int pos) {
        Query q = fs.collection("haberler").orderBy(query, Query.Direction.DESCENDING);
        q.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (pos >= documentSnapshots.getDocuments().size()) {
                    Log.d("DB", "Pos is higher than list size");
                    return;
                }

                DocumentSnapshot ds = documentSnapshots.getDocuments().get(pos);

                News n = new News();
                n.setTitle(ds.getString("header"));
                n.setContent(ds.getString("content"));
                n.setUri(ds.getString("uri"));
                n.setID(ds.getLong("id"));
                n.setRead(ds.getLong("read"));

                Log.d("DB", "Fetching " + pos + " ,info: \n" + n.toString());

                if (dataListener != null)
                    dataListener.onDataFetched(n, pos);
            }
        });
    }

    public void fetchCategory(final String category, final int pos) {
        Query q = fs.collection("haberler").whereEqualTo("category", category).
        orderBy("id", Query.Direction.DESCENDING);

        q.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

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

                Log.d("DB", "Fetching " + category + " category: " + pos + " info: \n" + n.toString());

                if (dataListener != null)
                    dataListener.onCategoryFetched(category, n, pos);
            }
        });
    }

    public void saveNews(final News n) {
        if (!checkLoggedIn())
            return;

        final FirebaseUser usr = auth.getCurrentUser();
        final DatabaseReference ref = db.getReference("users");

        ref.child(usr.getUid()).child("markeds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String buffer;
                List<String> allMarks = null;

                if (dataSnapshot.getValue().equals("empty")) {
                    buffer = "";
                    allMarks = new ArrayList<>();
                } else {
                    buffer = (String) dataSnapshot.getValue();
                    allMarks = Arrays.asList(buffer.split(","));
                    lastMarkings = buffer;
                }

                if (allMarks.contains(String.valueOf(n.getID()))) {
                    Toast.makeText(context, "Bu haber zaten kaydedildi", Toast.LENGTH_SHORT).show();
                    return;
                }

                ref.child(usr.getUid()).child("markeds").setValue(n.getID() + "," + buffer);

                if (dataListener != null)
                    dataListener.onDataSaved(lastMarkings);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Haberi kaydedemedik :(", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void unsaveNews(final News n) {
        if (!checkLoggedIn())
            return;

        final FirebaseUser usr = auth.getCurrentUser();
        final DatabaseReference ref = db.getReference("users");

        ref.child(usr.getUid()).child("markeds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String id = String.valueOf(n.getID());

                if (dataSnapshot.getValue().equals("empty")) {
                    return;
                }

                String buffer = (String) dataSnapshot.getValue();
                lastMarkings = buffer;
                buffer.replace(id+",", "");

                ref.child(usr.getUid()).child("markeds").setValue(buffer);

                if (dataListener != null)
                    dataListener.onDataSaved(lastMarkings);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Haberi kaydedemedik :(", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }
}
