package ba.wurth.mb.Activities.Products;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;

import ba.wurth.mb.Activities.Orders.OrderActivity;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Fragments.Products.ProductCatalogFragment;
import ba.wurth.mb.Fragments.Products.ProductGeneralFragment;
import ba.wurth.mb.R;

public class ProductActivity extends AppCompatActivity implements  ActionBar.TabListener, ViewPager.OnPageChangeListener  {

    private ViewPager mViewPager;
    private FragmentAdapter mAdapter;
    public Long ProductID = 0L;
    public Long ArtikalID = 0L;
    public Long Grupa_Artikla = 0L;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product);

        try {

            if (getIntent().hasExtra("ProductID")) ProductID = getIntent().getLongExtra("ProductID", 0L);
            if (getIntent().hasExtra("ArtikalID")) ArtikalID = getIntent().getLongExtra("ArtikalID", 0L);
            if (getIntent().hasExtra("Grupa_Artikla")) Grupa_Artikla = getIntent().getLongExtra("Grupa_Artikla", 0L);

            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Product)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.RelatedProducts)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.SimilarProducts)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.SafetyProducts)).setTabListener(this));

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

            ProductGeneralFragment pg = new ProductGeneralFragment();
            pg.ArtikalID = ArtikalID;
            pg.ProductID = ProductID;

            ProductCatalogFragment pcr = new ProductCatalogFragment();
            pcr.ArtikalID = ArtikalID;
            pcr.ProductID = ProductID;
            pcr.MODE = 0;

            ProductCatalogFragment pcs = new ProductCatalogFragment();
            pcs.ArtikalID = ArtikalID;
            pcs.ProductID = ProductID;
            pcs.MODE = 1;

            ProductCatalogFragment pcz = new ProductCatalogFragment();
            pcz.ArtikalID = ArtikalID;
            pcz.ProductID = ProductID;
            pcz.MODE = 2;

            mAdapter.addFragment(pg);
            mAdapter.addFragment(pcr);
            mAdapter.addFragment(pcs);
            mAdapter.addFragment(pcz);

            mViewPager.setOnPageChangeListener(this);
            mViewPager.setAdapter(mAdapter);

        }
        catch (Exception ex) {

        }
    }

    public void bindData() {
        try {
            ((ProductGeneralFragment) mAdapter.getItem(0)).ArtikalID = ArtikalID;
            ((ProductGeneralFragment) mAdapter.getItem(0)).ProductID = ProductID;
            ((ProductGeneralFragment) mAdapter.getItem(0)).bindData();

            ((ProductCatalogFragment) mAdapter.getItem(1)).ArtikalID = ArtikalID;
            ((ProductCatalogFragment) mAdapter.getItem(1)).ProductID = ProductID;
            ((ProductCatalogFragment) mAdapter.getItem(1)).bindData();

            ((ProductCatalogFragment) mAdapter.getItem(2)).ArtikalID = ArtikalID;
            ((ProductCatalogFragment) mAdapter.getItem(2)).ProductID = ProductID;
            ((ProductCatalogFragment) mAdapter.getItem(2)).bindData();

            ((ProductCatalogFragment) mAdapter.getItem(3)).ArtikalID = ArtikalID;
            ((ProductCatalogFragment) mAdapter.getItem(3)).ProductID = ProductID;
            ((ProductCatalogFragment) mAdapter.getItem(3)).bindData();

            mViewPager.setCurrentItem(0);
        }
        catch (Exception ex) {
            wurthMB.AddError("Product", ex.getMessage(), ex);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_order).setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_order:
                Intent k = new Intent(ProductActivity.this, OrderActivity.class);
                k.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                k.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(k);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            if (mViewPager.getCurrentItem() != 0) {
                mViewPager.setCurrentItem(0);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public class FragmentAdapter extends FragmentPagerAdapter {
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
