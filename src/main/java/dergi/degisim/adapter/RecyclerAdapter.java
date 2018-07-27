// -*- @author aeren_pozitif  -*- //
package dergi.degisim.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
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
import java.util.List;

import dergi.degisim.ItemClickListener;
import dergi.degisim.R;
import dergi.degisim.fragment.MainFragment;
import dergi.degisim.util.Util;
import dergi.degisim.news.News;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<News> news;
    private List<Object> items;

    private ItemClickListener clickListener;
    private ItemClickListener saveButtonListener;

    private final Context context;

    private static final int BANNER = 0;
    private static final int NEWS = 1;

    public RecyclerAdapter(Context context, ItemClickListener clickListener, ItemClickListener saveButtonListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.saveButtonListener = saveButtonListener;

        items = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == NEWS) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_container, parent, false);
            return new NewsViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_container, parent, false);
            return new AdViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == NEWS) {
            NewsViewHolder newsViewHolder = (NewsViewHolder) holder;

            News n = (News) items.get(position);
            newsViewHolder.title.setText(n.getTitle());

            if (Util.checkLoggedIn()) {
                final FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");

                ref.child(usr.getUid()).child("markeds").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            if (MainFragment.LAST_MARKINGS.contains(String.valueOf(n.getID()))) {
                                newsViewHolder.btn.setImageResource(R.drawable.bookmarked);
                            } else {
                                newsViewHolder.btn.setImageResource(R.drawable.bookmark);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            Log.d("RV", "Index hatası");
                            newsViewHolder.btn.setImageResource(R.drawable.bookmark);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        newsViewHolder.btn.setImageResource(R.drawable.bookmark);
                    }
                });
            }

            //Try loading images from cache
            Picasso.with(context).load(n.getUri()).
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
                    Picasso.with(context).load(n.getUri()).
                    resize(980, 660).
                    networkPolicy(NetworkPolicy.NO_CACHE).
                    into(newsViewHolder.img);
                }
            });
        } else {
            AdViewHolder adViewHolder = (AdViewHolder) holder;
            AdView adView = (AdView) items.get(position);

            if (adViewHolder.cardView.getChildCount() > 0) {
                adViewHolder.cardView.removeAllViews();
            }
            if (adView.getParent() != null) {
                ((ViewGroup) adView.getParent()).removeView(adView);
            }

            adViewHolder.cardView.addView(adView);
        }
    }

    public int getRealPosition(int pos) {
        return pos - (pos / MainFragment.ITEMS_PER_AD);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if ((position % MainFragment.ITEMS_PER_AD) == 0 && position != 0)
            return BANNER;
        return NEWS;
    }

    public void setNews(List<News> news) {
        this.news = news;
        items.clear();
        items.addAll(news);

        for (int i = MainFragment.ITEMS_PER_AD; i <= items.size(); i+=MainFragment.ITEMS_PER_AD) {
            AdView adView = new AdView(context);
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
            adView.loadAd(new AdRequest.Builder().build());
            items.add(i, adView);
        }

        Log.d("TGAA", items.toString());

        notifyDataSetChanged();
    }

    public List<News> getNews() {
        return news;
    }

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
                    if (FirebaseAuth.getInstance().getCurrentUser() != null &&
                        !FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {
                        if (saveButtonListener != null)
                            saveButtonListener.onClick(itemView, getAdapterPosition());
                        else
                            Log.d("Null", "OnClickListener is null");
                    } else {
                        Toast.makeText(context, "Kaydetmek için giriş yap", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d("CLICK EVENT", "Clicked on:" + getAdapterPosition());
            if (clickListener != null)
                clickListener.onClick(itemView, getRealPosition(getAdapterPosition()));
            else
                Log.d("Null", "OnClickListener is null");
        }
    }

    class AdViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;

        public AdViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.ad_card_view);
        }
    }
}
