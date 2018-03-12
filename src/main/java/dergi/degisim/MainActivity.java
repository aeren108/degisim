// -*- @author aeren_pozitif  -*- //
package dergi.degisim;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import dergi.degisim.drawer.DrawerItem;
import dergi.degisim.fragment.HomeFragment;
import dergi.degisim.fragment.PostsFragment;
import dergi.degisim.fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    public DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    public ListView applicationList;
    public ListView categoryList;

    private ArrayAdapter applicationAdapter;
    private ArrayAdapter categoryAdapter;

    private List<DrawerItem> categoryItems;
    private List<DrawerItem> applicationItems;

    public String[] applicationTitles;
    public String[] categoryTitles;

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

        drawer = findViewById(R.id.drawer_layout);
        categoryList = findViewById(R.id.category_list);
        applicationList = findViewById(R.id.application_list);

        categoryItems = new ArrayList<>();
        applicationItems = new ArrayList<>();

        categoryTitles = getResources().getStringArray(R.array.category_items);
        applicationTitles = getResources().getStringArray(R.array.application_items);

        initDrawerLists();

        categoryAdapter = new ArrayAdapter(this, R.layout.drawer_list, R.id.title, categoryTitles);
        categoryList.setAdapter(categoryAdapter);

        applicationAdapter = new ArrayAdapter(this, R.layout.drawer_list, R.id.title, applicationTitles);
        applicationList.setAdapter(applicationAdapter);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toggle = new ActionBarDrawerToggle(this, drawer, R.string.opened, R.string.closed);

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        HomeFragment home = new HomeFragment();
        getSupportFragmentManager().beginTransaction().
        replace(R.id.frame, home).commit();
        for (int i = 0; i < HomeFragment.NEWS_AMOUNT; i++) {
            home.fetchData(i);
        }
    }

    private void initDrawerLists() {
        for (int i = 0; i < categoryTitles.length; i++) {
            categoryItems.add(new DrawerItem(categoryTitles[i]));
        }

        for (int i = 0; i < applicationTitles.length; i++) {
            applicationItems.add(new DrawerItem(applicationTitles[i]));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }
}