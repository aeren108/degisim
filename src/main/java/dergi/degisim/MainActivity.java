// -*- @author aeren_pozitif  -*- //
package dergi.degisim;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import dergi.degisim.drawer.DrawerItem;
import dergi.degisim.drawer.ItemAdapter;
import dergi.degisim.fragment.HomeFragment;
import dergi.degisim.fragment.PostsFragment;
import dergi.degisim.fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private ListView itemList;
    private ItemAdapter adapter;

    private List<DrawerItem> drawerItems;
    private String[] itemTitles;

    public static CharSequence APP_TITLE;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    HomeFragment homeFragment = new HomeFragment();
                    getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, homeFragment).commit();
                    return true;
                case R.id.navigation_dashboard:
                    PostsFragment postsFragment = new PostsFragment();
                    getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, postsFragment).commit();
                    return true;
                case R.id.navigation_notifications:
                    SettingsFragment settingsFragment = new SettingsFragment();
                    getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, settingsFragment).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        APP_TITLE = getSupportActionBar().getTitle();

        drawer = findViewById(R.id.drawer_layout);
        itemList = findViewById(R.id.drawer_list);

        drawerItems = new ArrayList<>();
        itemTitles = getResources().getStringArray(R.array.drawerlist_items);
        initDrawerList();

        adapter = new ItemAdapter(drawerItems, getApplicationContext());
        itemList.setAdapter(adapter);

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                drawer.closeDrawers();
            }
        });

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toggle = new ActionBarDrawerToggle(this, drawer, R.string.opened, R.string.closed) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(APP_TITLE);
            }
        };

        drawer.setDrawerListener(toggle);

        HomeFragment home = new HomeFragment();
        getSupportFragmentManager().beginTransaction().
        add(R.id.frame, home).commit();
        home.fetchData();
    }

    private void initDrawerList() {
        for (int i = 0; i < itemTitles.length; i++) {
            drawerItems.add(new DrawerItem(itemTitles[i]));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

