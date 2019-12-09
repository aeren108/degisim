// -*- @author aeren_pozitif  -*- //
package dergi.degisim;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dergi.degisim.adapter.ViewPagerAdapter;
import dergi.degisim.fragment.HomeFragment;
import dergi.degisim.fragment.MainFragment;
import dergi.degisim.fragment.MarkedFragment;
import dergi.degisim.fragment.WeeklyFragment;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
                                                               MenuItem.OnActionExpandListener,
                                                               FirebaseAuth.AuthStateListener,
                                                               ViewPager.OnPageChangeListener,
                                                               BottomNavigationView.OnNavigationItemSelectedListener{

    public BottomNavigationView navigation;
    public DrawerLayout drawer;
    private NavigationView drawerNav;

    private ActionBarDrawerToggle toggle;

    private ViewPager pager;
    private ViewPagerAdapter pagerAdapter;
    private MenuItem prevItem;

    private MainFragment curFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar bar = findViewById(R.id.toolbar);
        setSupportActionBar(bar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawer = findViewById(R.id.drawer_layout);
        navigation = findViewById(R.id.navigation);
        drawerNav = findViewById(R.id.drawer_nav);
        pager = findViewById(R.id.pager);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        curFragment = (MainFragment) pagerAdapter.getItem(pager.getCurrentItem());
        toggle = new ActionBarDrawerToggle(this, drawer, R.string.opened, R.string.closed);

        drawerNav.setNavigationItemSelectedListener(curFragment);
        navigation.setOnNavigationItemSelectedListener(this);
        drawer.addDrawerListener(toggle);

        pagerAdapter.addFragment(new HomeFragment());
        pagerAdapter.addFragment(new MarkedFragment());
        pagerAdapter.addFragment(new WeeklyFragment());
        pager.addOnPageChangeListener(this);
        pager.setAdapter(pagerAdapter);

        toggle.syncState();

        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        searchItem.setOnActionExpandListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        pager.setCurrentItem(0);

        curFragment = (MainFragment) pagerAdapter.getItem(0);
        ((HomeFragment) curFragment).performSearchQuery(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) { return false; }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) { return true; }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        Fragment curFragment = pagerAdapter.getItem(pager.getCurrentItem());
        if (curFragment instanceof HomeFragment) {
            ((HomeFragment) curFragment).returnDefault();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        curFragment = (MainFragment) pagerAdapter.getItem(pager.getCurrentItem());

        if (curFragment instanceof HomeFragment) {
            if (((HomeFragment) curFragment).mode == MainFragment.CATEGORY ||
                ((HomeFragment) curFragment).mode == MainFragment.SEARCH) {
                ((HomeFragment) curFragment).returnDefault();
            }
        } else if (curFragment instanceof WeeklyFragment) {
            if (((WeeklyFragment) curFragment).mode == MainFragment.CATEGORY) {
                ((WeeklyFragment) curFragment).returnDefault();
            }
        } else
            super.onBackPressed();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser usr = firebaseAuth.getCurrentUser();
        if (usr != null) {
            if (usr.isAnonymous()) {
                drawerNav.getMenu().getItem(4).setTitle("GİRİŞ YAP");
                drawerNav.invalidate();
            } else {
                drawerNav.getMenu().getItem(4).setTitle("ÇIKIŞ YAP");
                drawerNav.invalidate();
            }
        } else {
            drawerNav.getMenu().getItem(4).setTitle("GİRİŞ YAP");
            drawerNav.invalidate();

            FirebaseAuth.getInstance().signInAnonymously().
                    addOnSuccessListener(authResult -> Log.d("AUTH", "Logged in anonymously")).
                    addOnFailureListener(e -> Log.d("AUTH", "Couldn't logged in anonymously"));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                pager.setCurrentItem(0);
                break;
            case R.id.navigation_dashboard:
                pager.setCurrentItem(1);
                break;
            case R.id.navigation_notifications:
                pager.setCurrentItem(2);
                break;
        }
        return false;
    }

    @Override
    public void onPageSelected(int position) {
        if (prevItem != null)
            prevItem.setChecked(false);
        else
            navigation.getMenu().getItem(0).setChecked(false);

        MenuItem item = navigation.getMenu().getItem(position);
        item.setChecked(true);
        prevItem = item;

        curFragment = (MainFragment) pagerAdapter.getItem(pager.getCurrentItem());
        curFragment.onStartFeature();

        drawerNav.setNavigationItemSelectedListener(curFragment);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
}