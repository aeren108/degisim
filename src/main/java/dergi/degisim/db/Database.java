// -*- @author aeren_pozitif  -*- //
package dergi.degisim.db;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.util.Log;

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

import dergi.degisim.news.News;
import dergi.degisim.news.NewsPaper;

public class Database {
    private DataListener dataListener;

    private final FirebaseFirestore fs;
    private final FirebaseAuth auth;
    private final FirebaseDatabase db;

    private String lastMarkings;

    public Database() {
        fs = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
    }

    public Database(DataListener dataListener) {
        fs = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        this.dataListener = dataListener;
    }

    public static boolean checkLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null && !user.isAnonymous();
    }

    public void fetchData(String orderBy, final int pos) {
        Query q = fs.collection("haberler").orderBy(orderBy, Query.Direction.DESCENDING);
        q.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (pos >= documentSnapshots.getDocuments().size()) {
                    Log.d("DB", "Pos is higher than list size");
                    return;
                }

                DocumentSnapshot ds = documentSnapshots.getDocuments().get(pos);
                News n = ds.toObject(News.class);

                synchronized (this) {
                    if (dataListener != null)
                        dataListener.onDataFetched(n, pos);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                if (dataListener != null)
                    dataListener.onError(DataListener.DATAFETCH_ERROR);
            }
        });
    }

    public void fetchData(final int pos) {

        fs.collection("haberler").document(String.valueOf(pos)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot ds) {
                if (!ds.exists())
                    return;

                News n = ds.toObject(News.class);

                synchronized (this) {
                    if (dataListener != null)
                        dataListener.onDataFetched(n, pos);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (dataListener != null)
                    dataListener.onError(DataListener.DATAFETCH_ERROR);
            }
        });
    }

    public void fetchCategory(final String category, String orderID, final int pos) {
        Query q = fs.collection("haberler").
        orderBy(orderID, Query.Direction.DESCENDING).
        whereEqualTo("category", category);

        q.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (pos >= documentSnapshots.getDocuments().size()) {
                    Log.d("sddsf","hamam böceği");
                    return;
                }

                DocumentSnapshot ds = documentSnapshots.getDocuments().get(pos);
                News n = ds.toObject(News.class);

                synchronized (this) {
                    if (dataListener != null)
                        dataListener.onCategoryFetched(category, n, pos);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                if (dataListener != null)
                    dataListener.onError(DataListener.CATFETCH_ERROR);
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
                List<String> allMarks;

                if (dataSnapshot.getValue().equals("empty")) {
                    lastMarkings = "";
                    allMarks = new ArrayList<>();
                } else {
                    buffer = (String) dataSnapshot.getValue();
                    allMarks = Arrays.asList(buffer.split(","));
                    lastMarkings = n.getID() + "," + buffer;
                }

                synchronized (this) {
                    if (n != null) {
                        if (allMarks.contains(String.valueOf(n.getID()))) {
                            //This shows that the article is already bookmarked
                            unsaveNews(n);
                            return;
                        } else {
                            if (dataListener != null)
                                dataListener.onDataSaved(lastMarkings, n);
                        }
                        ref.child(usr.getUid()).child("markeds").setValue(lastMarkings);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (dataListener != null)
                    dataListener.onError(DataListener.SAVE_ERROR);
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
                buffer = buffer.replace(id+",", "");
                lastMarkings = buffer;

                ref.child(usr.getUid()).child("markeds").setValue(buffer);

                synchronized (this) {
                    if (dataListener != null)
                        dataListener.onDataUnsaved(lastMarkings, n);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (dataListener != null)
                    dataListener.onError(DataListener.UNSAVE_ERROR);
            }
        });
    }

    public static void openNewspaper(Activity activity, List<News> itemList, int pos) {
        Intent intent = new Intent(activity.getApplicationContext(), NewsPaper.class);
        intent.putExtra("content", itemList.get(pos).getContent());
        intent.putExtra("header", itemList.get(pos).getTitle());
        intent.putExtra("uri", itemList.get(pos).getUri());
        intent.putExtra("date", itemList.get(pos).getDate());
        intent.putExtra("author", itemList.get(pos).getAuthor());
        intent.putExtra("id", itemList.get(pos).getID());
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }
}
