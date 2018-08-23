// -*- @author aeren_pozitif  -*- //
package dergi.degisim.news;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.r0adkll.slidr.Slidr;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import dergi.degisim.R;
import dergi.degisim.fragment.MainFragment;
import dergi.degisim.util.DataListener;
import dergi.degisim.util.Util;

public class NewsPaper extends AppCompatActivity implements View.OnClickListener, DataListener {
    private LinearLayout ll;
    private ImageView img;
    private TextView title;
    private TextView info;
    private ImageButton back;
    private FloatingActionButton fab;

    private Util u;

    private String content;
    private String header;
    private String uri;
    private String date;
    private String author;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newspaper);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Slidr.attach(this);

        u = new Util(this);
        ll = findViewById(R.id.wrapper);
        img = findViewById(R.id.toolbar_image);
        title = findViewById(R.id.title);
        info = findViewById(R.id.info);
        back = findViewById(R.id.go_back_newspaper);
        fab = findViewById(R.id.floatingSaveButton);
        back.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        content = bundle.getString("content");
        header = bundle.getString("header");
        uri = bundle.getString("uri");
        date = bundle.getString("date");
        author = bundle.getString("author");
        id = bundle.getLong("id");
        fab.setOnClickListener(v -> {
            if (Util.checkLoggedIn()) {
                News n = new News(id, uri, header, content);
                u.saveNews(n);
            }
        });

        if (MainFragment.LAST_MARKINGS.contains(String.valueOf(id)))
            fab.setImageResource(R.drawable.bookmarked);
        else
            fab.setImageResource(R.drawable.bookmark);

        prepareContent();
        loadImage();

        title.setText(header);
        info.setText(author + " / " + date);

        FirebaseFirestore.getInstance().collection("haberler").document(String.valueOf(id)).get().addOnCompleteListener(task -> {
            long read = task.getResult().getLong("read");
            DocumentReference ref = FirebaseFirestore.getInstance().collection("haberler").document(String.valueOf(id));
            ref.update("read", read+1);
        });
    }

    private void loadImage() {
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
    }

    public void prepareContent() {
        String[] contents = content.split("-&-");
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        for (String s : contents) {
            if (s.startsWith("rsmwx")) {
                String[] pic = s.split(":%:");

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, (int) (16*density), 0, (int) (16*density));
                lp.gravity = Gravity.CENTER_HORIZONTAL;

                ImageView image = new ImageView(this);
                image.setLayoutParams(lp);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Picasso.with(getApplicationContext()).load(pic[1]).into(image);
                image.invalidate();

                ll.addView(image);
            } else {
                TextView tw = new TextView(this);
                s = s.replace("\\n", System.getProperty("line.separator"));

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                tw.setTextSize(18);
                tw.setLayoutParams(lp);
                tw.setText(s);
                tw.setTextColor(Color.parseColor("#303030"));
                tw.setLineSpacing(0.1f, 1.44f);

                ll.addView(tw);
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onBackPressed();
    }

    @Override
    public void onDataSaved(String lastMarkings, News n) {
        MainFragment.LAST_MARKINGS = Arrays.asList(lastMarkings.split(","));

        Snackbar snackbar = Snackbar.make(ll, "Haber kaydedildi", Snackbar.LENGTH_SHORT);
        snackbar.setAction("GERİ AL", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                u.unsaveNews(n);
            }
        });
        snackbar.show();

        fab.setImageResource(R.drawable.bookmarked);
    }

    @Override
    public void onDataUnsaved(String lastMarkings, News n) {
        MainFragment.LAST_MARKINGS = Arrays.asList(lastMarkings.split(","));

        Snackbar snackbar = Snackbar.make(ll, "Haber kaydedilenlerden çıkarıldı", Snackbar.LENGTH_SHORT);
        snackbar.setAction("GERİ AL", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                u.saveNews(n);
            }
        });
        snackbar.show();

        fab.setImageResource(R.drawable.bookmark);
    }

    @Override
    public void onError(int errorType) {
        if (errorType == DataListener.DATAFETCH_ERROR || errorType == DataListener.CATFETCH_ERROR) {
            Toast.makeText(getApplicationContext(), "Veriler yüklenemiyor, interinitini bi' kontrol et", Toast.LENGTH_LONG).show();
        } else if (errorType == DataListener.SAVE_ERROR || errorType == DataListener.UNSAVE_ERROR) {
            Toast.makeText(getApplicationContext(), "Haber kaydedilemedi", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDataFetched(News n, int pos) {

    }

    @Override
    public void onCategoryFetched(String category, News n, int pos) {

    }
}
