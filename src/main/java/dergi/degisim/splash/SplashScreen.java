// -*- @author aeren_pozitif  -*- //
package dergi.degisim.splash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import dergi.degisim.MainActivity;
import dergi.degisim.R;
import dergi.degisim.fragment.HomeFragment;

public class SplashScreen extends AppCompatActivity {
    public static final int SPLASH_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Loader(getApplicationContext()).execute();
    }

    @SuppressLint("StaticFieldLeak")
    class Loader extends AsyncTask<Void, Void, Void> {
        private Context context;

        Loader(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                Query q = firestore.collection("haberler").orderBy("id", Query.Direction.DESCENDING);
                q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (int i = 0; i < HomeFragment.NEWS_AMOUNT; i++) {
                                DocumentSnapshot ds = task.getResult().getDocuments().get(i);
                                Picasso.with(getApplicationContext()).load(ds.getString("uri")).fetch();

                                Log.d("SPLASH INFO", "Cached image: " + ds.getString("img"));
                            }
                        }
                    }
                });
                Thread.sleep(SPLASH_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void avoid) {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }

            cancel(true);
            finish();
        }
    }
}
