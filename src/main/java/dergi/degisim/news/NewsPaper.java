// -*- @author aeren_pozitif  -*- //
package dergi.degisim.news;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import dergi.degisim.MainActivity;
import dergi.degisim.R;

public class NewsPaper extends AppCompatActivity implements View.OnClickListener {
    private WebView w;
    private ImageView img;
    private ImageButton btn;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newspaper);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        w = findViewById(R.id.web);
        img = findViewById(R.id.toolbar_image);
        btn = findViewById(R.id.go_back_newspaper);
        btn.setOnClickListener(this);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        String content = bundle.getString("content");
        String header = bundle.getString("header");
        final String uri = bundle.getString("uri");
        final long id = bundle.getLong("id");

        //Try loading images from cache
        Picasso.with(getApplicationContext()).load(uri).networkPolicy(NetworkPolicy.OFFLINE).
        resize(800,600).into(img, new Callback() {
            @Override
            public void onSuccess() {
                //Image is loaded from cache
            }

            @Override
            public void onError() {
                //Image is not in the cache, load from internet
                Picasso.with(getApplicationContext()).load(uri).
                resize(800,600).
                networkPolicy(NetworkPolicy.NO_CACHE).into(img);
            }
        });
        Log.d("CONTENT", content);

        String html = "<font size='4'>" + content + "<font/>";

        w.getSettings().setSupportZoom(true);
        w.loadData(html,"text/html; charset=utf-8", "utf-8");
        w.getSettings().setJavaScriptEnabled(true);
        w.getSettings().setDefaultTextEncodingName("utf-8");
        w.getSettings().setPluginState(WebSettings.PluginState.ON);
        w.setVerticalScrollBarEnabled(true);
        w.setHorizontalScrollBarEnabled(true);

        setTitle(header);

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
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
