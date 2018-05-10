// -*- @author aeren_pozitif  -*- //
package dergi.degisim;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import dergi.degisim.auth.LoginActivity;
import dergi.degisim.drawer.ItemAdapter;
import dergi.degisim.fragment.FragmentFeature;
import dergi.degisim.fragment.HomeFragment;
import dergi.degisim.fragment.MarkedFragment;
import dergi.degisim.fragment.WeeklyFragment;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener, FirebaseAuth.AuthStateListener {

    public DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private ViewPager pager;
    private ViewPagerAdapter pagerAdapter;
    private MenuItem prevItem;

    public ListView applicationList;
    public ListView categoryList;

    private ItemAdapter applicationAdapter;
    private ItemAdapter categoryAdapter;

    public String[] applicationTitles;
    public String[] categoryTitles;

    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation = findViewById(R.id.navigation);
        pager = findViewById(R.id.pager);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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
        });

        drawer = findViewById(R.id.drawer_layout);
        categoryList = findViewById(R.id.category_list);
        applicationList = findViewById(R.id.application_list);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevItem != null)
                    prevItem.setChecked(false);
                else
                    navigation.getMenu().getItem(0).setChecked(false);

                Log.d("page",""+position);
                navigation.getMenu().getItem(position).setChecked(true);
                prevItem = navigation.getMenu().getItem(position);
                Fragment curFragment = pagerAdapter.getItem(pager.getCurrentItem());
                ((FragmentFeature) curFragment).loadFeature();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        pagerAdapter.addFragment(new HomeFragment());
        pagerAdapter.addFragment(new MarkedFragment());
        pagerAdapter.addFragment(new WeeklyFragment());
        pager.setAdapter(pagerAdapter);

        categoryTitles = getResources().getStringArray(R.array.category_items);
        applicationTitles = getResources().getStringArray(R.array.application_items);

        categoryAdapter = new ItemAdapter(Arrays.asList(categoryTitles), getApplicationContext());
        categoryList.setAdapter(categoryAdapter);

        applicationAdapter = new ItemAdapter(Arrays.asList(applicationTitles), getApplicationContext());
        applicationList.setAdapter(applicationAdapter);

        applicationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                        Intent intent = new Intent(getApplication(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    } else {
                        FirebaseAuth.getInstance().signOut();
                    }
                    drawer.closeDrawers();
                }
            }
        });

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toggle = new ActionBarDrawerToggle(this, drawer, R.string.opened, R.string.closed);

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        auth.addAuthStateListener(this);
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

        Fragment curFragment = pagerAdapter.getItem(pager.getCurrentItem());
        ((HomeFragment) curFragment).performSearchQuery(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        pager.setCurrentItem(0);
        Fragment curFragment = pagerAdapter.getItem(pager.getCurrentItem());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.detach(curFragment);
        ft.attach(curFragment);
        ft.commit();
        ((HomeFragment)curFragment).mode = 'd';
        navigation.setSelectedItemId(R.id.navigation_home);

        return true;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser usr = firebaseAuth.getCurrentUser();
        if (usr != null) {
            if (usr.isAnonymous()) {
                applicationAdapter.editItem(0, "Giriş Yap");
                applicationList.setAdapter(applicationAdapter);
                applicationList.invalidate();
            } else {
                applicationAdapter.editItem(0, "Çıkış Yap");
                applicationList.setAdapter(applicationAdapter);
                applicationList.invalidate();
            }
        } else {
            applicationAdapter.editItem(0, "Giriş Yap");
            applicationList.setAdapter(applicationAdapter);
            applicationList.invalidate();

            auth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Log.d("AUTH", "Logged in anonymously");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("AUTH", "Couldn't logged in anonymously");
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        Fragment curFragment = pagerAdapter.getItem(pager.getCurrentItem());

        if (curFragment instanceof HomeFragment) {
            if (((HomeFragment) curFragment).mode == 'c') {
                pager.setCurrentItem(0);
            } else
                super.onBackPressed();
        } if (curFragment instanceof WeeklyFragment) {
            if (((WeeklyFragment) curFragment).catMode) {
                pager.setCurrentItem(2);
            } else
                super.onBackPressed();
        } else
            super.onBackPressed();
    }
}