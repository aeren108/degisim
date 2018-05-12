// -*- @author aeren_pozitif  -*- //
package dergi.degisim.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dergi.degisim.ItemClickListener;
import dergi.degisim.R;
import dergi.degisim.util.Util;
import dergi.degisim.news.News;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.NewsViewHolder> {
    private List<News> news;
    private ItemClickListener clickListener;
    private ItemClickListener saveButtonListener;

    private final Context context;

    public RecyclerAdapter(Context context, ItemClickListener clickListener, ItemClickListener saveButtonListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.saveButtonListener = saveButtonListener;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        return new NewsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final NewsViewHolder newsViewHolder, int position) {
        newsViewHolder.title.setText(news.get(newsViewHolder.getAdapterPosition()).getTitle());

        if (Util.checkLoggedIn()) {
            final FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");

            ref.child(usr.getUid()).child("markeds").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String marks;
                    List<String> markeds;

                    if (dataSnapshot.getValue().equals("empty")) {
                        markeds = new ArrayList<>();
                    } else {
                        marks = (String) dataSnapshot.getValue();
                        markeds = Arrays.asList(marks.split(","));
                    }
                    try {
                        if (markeds.contains(String.valueOf(news.get(newsViewHolder.getAdapterPosition()).getID()))) {
                            newsViewHolder.btn.setImageResource(R.drawable.eye_icon);
                        } else {
                            newsViewHolder.btn.setImageResource(R.drawable.filled_save_button);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        Log.d("RV", "Index hatası");
                        newsViewHolder.btn.setImageResource(R.drawable.filled_save_button);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Haberi kaydedemedik :(", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // TODO: 2.05.2018 Handle no user login 
        }

        //Try loading images from cache
        Picasso.with(context).load(news.get(position).getUri()).
        resize(980, 660).
        networkPolicy(NetworkPolicy.OFFLINE).
        into(newsViewHolder.img, new Callback() {
            @Override
            public void onSuccess() {
                //Image is loaded from cache
            }

            @Override
            public void onError() {
                //Image is not in the cache, load from the internet

                Picasso.with(context).load(news.get(newsViewHolder.getAdapterPosition()).getUri()).
                resize(980, 660).
                networkPolicy(NetworkPolicy.NO_CACHE).
                into(newsViewHolder.img);
            }
        });
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    public void setNews(List<News> news) {
        this.news = news;
        notifyDataSetChanged();
    }

    public List<News> getNews() {return news;}

    public void addItem(News n) {
        news.add(n);
        notifyDataSetChanged();
    }

    class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView title;
        ImageView img;
        ImageButton btn;

        NewsViewHolder(final View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            img = itemView.findViewById(R.id.img);
            btn = itemView.findViewById(R.id.save_button);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        if (saveButtonListener != null && !FirebaseAuth.getInstance().getCurrentUser().isAnonymous())
                            saveButtonListener.onClick(itemView, getAdapterPosition());
                        else
                            Log.d("Null", "OnClickListener is null");
                    }
                }
            });

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d("CLICK EVENT", "Clicked on:" + getAdapterPosition());
            if (clickListener != null)
                clickListener.onClick(itemView, getAdapterPosition());
            else
                Log.d("Null", "OnClickListener is null");
        }
    }
}
