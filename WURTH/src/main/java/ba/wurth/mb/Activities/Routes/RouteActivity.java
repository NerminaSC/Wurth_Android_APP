package ba.wurth.mb.Activities.Routes;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.ArrayList;

import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Route;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Routes.DL_Routes;
import ba.wurth.mb.Fragments.Map.MapRouteFragment;
import ba.wurth.mb.Fragments.Routes.RouteInfoFragment;
import ba.wurth.mb.R;

public class RouteActivity extends AppCompatActivity implements  ActionBar.TabListener, ViewPager.OnPageChangeListener  {

    private ViewPager mViewPager;
    public FragmentAdapter mAdapter;

    public Route mRoute;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route);

        try {

            getSupportActionBar().setTitle(R.string.Route);

            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Route_Info)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Route_Map)).setTabListener(this));

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            if (wurthMB.getOrder() != null && wurthMB.getOrder().ClientID > 0) {
                Client c = DL_Wurth.GET_Client(wurthMB.getOrder().ClientID);
                if (c != null) {
                    getSupportActionBar().setTitle(c.Name);
                    getSupportActionBar().setSubtitle(c.code);
                }
            }
            else {
                getSupportActionBar().setTitle("");
                getSupportActionBar().setSubtitle("");
            }

            mViewPager = (ViewPager) findViewById(R.id.pager);

            mAdapter = new FragmentAdapter(getSupportFragmentManager());

            Bundle b = new Bundle();

            if (getIntent().hasExtra("_id")) {
                b.putLong("_id", getIntent().getLongExtra("_id", 0L));
                Long RouteID =  getIntent().getLongExtra("_id", 0L);
                mRoute = DL_Routes.GetByID(RouteID);
            }
            else {
                mRoute = new Route();
            }

            RouteInfoFragment ri = new RouteInfoFragment();
            ri.setArguments(b);

            mAdapter.addFragment(ri);
            mAdapter.addFragment(new MapRouteFragment());

            mViewPager.setOnPageChangeListener(RouteActivity.this);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setCurrentItem(0);
        }
        catch (Exception ex) {
            wurthMB.AddError("Order", ex.getMessage(), ex);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class FragmentAdapter extends FragmentStatePagerAdapter {
        private final ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
        public FragmentAdapter(FragmentManager fm) { super(fm); }
        @Override
        public int getCount() { return mFragments.size(); }
        public void addFragment(Fragment fragment) {
            mFragments.add(fragment);
            notifyDataSetChanged();
        }
        @Override
        public Fragment getItem(int position) { return mFragments.get(position); }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        try {
            if( mViewPager != null) mViewPager.setCurrentItem(tab.getPosition());
        }
        catch (Exception ex) {

        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) { }
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) { }
    @Override
    public void onPageScrollStateChanged(int arg0) { }
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) { }
    @Override
    public void onPageSelected(int position) { getSupportActionBar().setSelectedNavigationItem(position); }

}