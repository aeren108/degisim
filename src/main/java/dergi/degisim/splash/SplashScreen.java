package dergi.degisim.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.fragment.HomeFragment;

public class SplashScreen extends AppCompatActivity {
    private AsyncTask<Void, Void, Void> asyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        loadToCache();
        asyncTask.execute();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        asyncTask.cancel(false);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadToCache() {
        asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                final StorageReference storage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://degisim-44155.appspot.com/");
                firestore.collection("haberler").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (int i = 0; i < HomeFragment.NEWS_AMOUNT; i++) {
                            DocumentSnapshot ds = task.getResult().getDocuments().get(i);
                            String path = ds.getString("img");

                            StorageReference ref = storage.child("images/" + path);
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.with(getApplicationContext()).load(uri).fetch(new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {

                                        }
                                    });
                                    Log.d("FETCH INFO", "Got the uri on splash screen: " + uri.toString());
                                }
                            });
                        }
                    }
                });
                return null;
            }
        };
    }
}
