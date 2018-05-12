// -*- @author aeren_pozitif  -*- //
package dergi.degisim.news;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.r0adkll.slidr.Slidr;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import dergi.degisim.R;
import dergi.degisim.util.Util;

public class NewsPaper extends AppCompatActivity implements View.OnClickListener {
    private WebView w;
    private ImageView img;
    private ImageButton btn;
    private FloatingActionButton saveBtn;

    private Util u;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newspaper);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Slidr.attach(this);

        u = new Util();

        w = findViewById(R.id.web);
        img = findViewById(R.id.toolbar_image);
        btn = findViewById(R.id.go_back_newspaper);
        saveBtn = findViewById(R.id.floatingSaveButton);
        btn.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        final String content = bundle.getString("content");
        final String header = bundle.getString("header");
        final String uri = bundle.getString("uri");
        final long id = bundle.getLong("id");

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.checkLoggedIn()) {
                    News n = new News(uri, header, content);
                    n.setID(id);

                    u.saveNews(n);
                }
            }
        });

        setTitle(header);

        //Try loading images from cache
        Picasso.with(getApplicationContext()).load(uri).networkPolicy(NetworkPolicy.OFFLINE).
        resize(800,600).into(img, new Callback() {
            @Override
            public void onSuccess() {
                //Image is loaded from cache and everyting is okay
            }

            @Override
            public void onError() {
                //Image is not in the cache, fetch from the internet
                Picasso.with(getApplicationContext()).load(uri).
                resize(800,600).
                networkPolicy(NetworkPolicy.NO_CACHE).into(img);
            }
        });

        String html = "<font size='4'>" + content + "<font/>";

        w.loadData(html,"text/html; charset=utf-8", "utf-8");
        w.getSettings().setSupportZoom(true);
        w.getSettings().setDefaultTextEncodingName("utf-8");
        w.setVerticalScrollBarEnabled(true);
        w.setHorizontalScrollBarEnabled(true);

        FirebaseFirestore.getInstance().collection("haberler").document(String.valueOf(id)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                long read = task.getResult().getLong("read");
                DocumentReference ref = FirebaseFirestore.getInstance().collection("haberler").document(String.valueOf(id));
                ref.update("read", read+1);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onBackPressed();
    }
}
