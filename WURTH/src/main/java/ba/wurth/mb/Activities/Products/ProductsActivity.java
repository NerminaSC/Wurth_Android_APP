package ba.wurth.mb.Activities.Products;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

import ba.wurth.mb.Adapters.SearchSuggestionsAdapter;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Fragments.Products.CatalogFragment;
import ba.wurth.mb.Fragments.Products.ProductBarcodeFragment;
import ba.wurth.mb.Fragments.Products.ProductsListFragment;
import ba.wurth.mb.R;

public class ProductsActivity extends AppCompatActivity implements  ActionBar.TabListener, ViewPager.OnPageChangeListener  {

    private ViewPager mViewPager;
    private FragmentAdapter mAdapter;

    private SearchView mSearchView;
    private MenuItem searchMenuItem ;
    private SearchSuggestionsAdapter mSearchSuggestionsAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.products);

        try {

            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Catalog)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.List)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Barcode)).setTabListener(this));

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

            mAdapter.addFragment(new CatalogFragment());
            mAdapter.addFragment(new ProductsListFragment());
            mAdapter.addFragment(new ProductBarcodeFragment());

            mViewPager.setOnPageChangeListener(this);
            mViewPager.setAdapter(mAdapter);

            if (getIntent().getExtras() != null && getIntent().hasExtra("ACTION") && getIntent().getIntExtra("ACTION", 0) == 0) {
                Intent i = new Intent(ProductsActivity.this, ProductActivity.class);
                i.putExtra("ProductID",  getIntent().getLongExtra("ProductID", 0L));
                i.putExtra("ArtikalID",  getIntent().getLongExtra("ArtikalID", 0L));
                i.putExtra("Grupa_Artikla",  getIntent().getLongExtra("Grupa_Artikla", 0L));
                startActivityForResult(i, 0);
            }
        }
        catch (Exception ex) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (mAdapter.getItem(1) instanceof ProductsListFragment) {
                ((ProductsListFragment) mAdapter.getItem(1)).bindData();
                ((ProductsListFragment) mAdapter.getItem(1)).bindList();
            }
        } catch (Exception e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getExtras() != null && data.hasExtra("AssociationType")) {
            ((ProductsListFragment) mAdapter.getItem(1)).AssociationType = Integer.parseInt(data.getStringExtra("AssociationType"));
            ((ProductsListFragment) mAdapter.getItem(1)).ProductID = data.getLongExtra("ProductID", 0L);
            ((ProductsListFragment) mAdapter.getItem(1)).ArtikalID = data.getLongExtra("ArtikalID", 0L);
            ((ProductsListFragment) mAdapter.getItem(1)).CategoryID = data.getLongExtra("Grupa_Artikla", 0L);
            mViewPager.setCurrentItem(1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        searchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        if (android.os.Build.VERSION.SDK_INT >= 11) {
            setupSearchView(menu.findItem(R.id.action_search));
        }

        return super.onCreateOptionsMenu(menu);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            if (mViewPager.getCurrentItem() == 0) {
                if (((CatalogFragment) mAdapter.getItem(0)).CategoryArray.size() > 1) {
                    ((CatalogFragment) mAdapter.getItem(0)).CategoryArray.remove(((CatalogFragment) mAdapter.getItem(0)).CategoryArray.size() - 1);
                    ((CatalogFragment) mAdapter.getItem(0)).bindData();
                    return true;
                }
            }

            if (mViewPager.getCurrentItem() == 1) {
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

    public void setProductListTab(Long CategoryID, Long ProductID, String searchWord, boolean reload, boolean set_tab) {
        try {
            if (mAdapter.getItem(1) instanceof ProductsListFragment) {
                ((ProductsListFragment) mAdapter.getItem(1)).CategoryID = CategoryID;
                ((ProductsListFragment) mAdapter.getItem(1)).ProductID = ProductID;
                ((ProductsListFragment) mAdapter.getItem(1)).searchWord = searchWord;
                if(reload) ((ProductsListFragment) mAdapter.getItem(1)).bindList();
            }
            if(set_tab) mViewPager.setCurrentItem(1);
        }
        catch (Exception ex) {

        }
    }


    private void setupSearchView(MenuItem searchItem) {

        if (isAlwaysExpanded()) {
            mSearchView.setIconifiedByDefault(false);
        } else {
            MenuItemCompat.setShowAsAction(searchItem, MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }

        if (mSearchSuggestionsAdapter == null) mSearchSuggestionsAdapter = new SearchSuggestionsAdapter(this);

        mSearchView.setSuggestionsAdapter(mSearchSuggestionsAdapter);

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener()
        {
            @Override
            public boolean onSuggestionClick(int position)
            {
                return true;
            }
            @Override
            public boolean onSuggestionSelect(int position)
            {
                return false;
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {


                return false;
            }
        });

        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if(!queryTextFocused) {
                    MenuItemCompat.collapseActionView(searchMenuItem);
                    mSearchView.setQuery("", false);
                }
            }
        });
    }

    protected boolean isAlwaysExpanded() {
        return false;
    }

}
