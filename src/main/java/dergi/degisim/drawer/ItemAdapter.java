// -*- @author aeren_pozitif  -*- //
package dergi.degisim.drawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dergi.degisim.R;

public class ItemAdapter extends BaseAdapter {
    List<DrawerItem> items;
    Context context;
    TextView title;

    public ItemAdapter(List<DrawerItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void editItem(int pos, String text) {
        items.get(pos).setTitle(text);
        notifyDataSetChanged();
    }

    public void refresh(ArrayList<DrawerItem> ditem) {
        items.clear();
        items.addAll(ditem);
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        if (view == null) {
            View v = LayoutInflater.from(context).inflate(R.layout.drawer_list, null);

            title = v.findViewById(R.id.title);
            title.setText(items.get(position).getTitle());

            return v;
        }

        return view;
    }
}
