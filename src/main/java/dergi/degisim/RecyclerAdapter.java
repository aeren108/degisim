// -*- @author aeren_pozitif  -*- //
package dergi.degisim;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import dergi.degisim.news.News;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.NewsViewHolder> {
    private List<News> news;
    private ItemClickListener clickListener;

    private final Context context;

    public RecyclerAdapter(Context context, ItemClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        return new NewsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final NewsViewHolder newsViewHolder, final int position) {
        newsViewHolder.title.setText(news.get(position).getTitle());

        Picasso.with(context).load(news.get(position).getUri()).
        resize(980, 660).
        noFade().
        networkPolicy(NetworkPolicy.OFFLINE).
        into(newsViewHolder.img);
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    public void setNews(List<News> news) {
        this.news = news;
    }

    class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        ImageView img;

        NewsViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            img = itemView.findViewById(R.id.img);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.d("CLICK EVENT", "Clicked on:" + getAdapterPosition());
            if (clickListener!= null)
                clickListener.onClick(view, getAdapterPosition());
            else
                Log.d("Null", "OnClickListener is null");
        }
    }
}
