// -*- @author aeren_pozitif  -*- //
package dergi.degisim.news;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private LinearLayout ll;
    private ImageView img;
    private ImageButton btn;
    private FloatingActionButton saveBtn;

    private Util u;
    private String content;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newspaper);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Slidr.attach(this);

        u = new Util();

        ll = findViewById(R.id.wrapper);
        img = findViewById(R.id.toolbar_image);
        btn = findViewById(R.id.go_back_newspaper);
        saveBtn = findViewById(R.id.floatingSaveButton);

        btn.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        content = bundle.getString("content");
        final String header = bundle.getString("header");
        final String uri = bundle.getString("uri");
        final long id = bundle.getLong("id");

        prepareContent();

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

        FirebaseFirestore.getInstance().collection("haberler").document(String.valueOf(id)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                long read = task.getResult().getLong("read");
                DocumentReference ref = FirebaseFirestore.getInstance().collection("haberler").document(String.valueOf(id));
                ref.update("read", read+1);
            }
        });
    }

    public void prepareContent() {
        String[] contents = content.split("-*-");
        for (String s : contents) {
            if (s.startsWith("rsmwx")) {
                String[] pic = s.split(":");

                ImageView image = new ImageView(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                image.setFitsSystemWindows(true);
                image.setScaleType(ImageView.ScaleType.FIT_XY);
                image.setLayoutParams(lp);

                Picasso.with(getApplicationContext()).load(pic[1]).resize(800, 600).into(image);
                ll.addView(image);
            } else {
                TextView tw = new TextView(this);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                tw.setTextSize(18);
                tw.setLayoutParams(lp);
                tw.setText(s);

                ll.addView(tw);
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onBackPressed();
    }
}
