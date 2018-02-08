// -*- @author aeren_pozitif  -*- //
package dergi.degisim.news;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import dergi.degisim.R;

public class NewsPaper extends AppCompatActivity {
    private TextView text;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newspaper);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        text = findViewById(R.id.content);
        img = findViewById(R.id.toolbar_image);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        String content = bundle.getString("content");
        String header = bundle.getString("header");
        String uri = bundle.getString("uri");
        Log.d("CONTENT", content);
        text.setText(content);
        setTitle(header);
        Picasso.with(getApplicationContext()).load(uri).resize(800,600).into(img);
    }
}
