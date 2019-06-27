package ba.wurth.mb.Activities.Clients;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import java.util.ArrayList;

import ba.wurth.mb.Activities.DeliveryPlaces.DeliveryPlaceLocationActivity;
import ba.wurth.mb.Activities.Orders.OrderActivity;
import ba.wurth.mb.Activities.Visits.VisitActivity;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Fragments.Actions.ActionsFragment;
import ba.wurth.mb.Fragments.Clients.ClientBaseFragment;
import ba.wurth.mb.Fragments.Clients.ClientBusinessCategoryFragment;
import ba.wurth.mb.Fragments.Clients.ClientContactsFragment;
import ba.wurth.mb.Fragments.Clients.ClientGeneralFragment;
import ba.wurth.mb.Fragments.Clients.ClientImagesFragment;
import ba.wurth.mb.Fragments.Clients.ClientMandatoryFragment;
import ba.wurth.mb.Fragments.Clients.ClientSOAFragment;
import ba.wurth.mb.Fragments.DeliveryPlaces.DeliveryPlacesFragment;
import ba.wurth.mb.Fragments.Orders.OrdersFragment;
import ba.wurth.mb.Fragments.Orders.ProposalsFragment;
import ba.wurth.mb.Fragments.Orders.RFQFragment;
import ba.wurth.mb.Fragments.Reports.DiaryFragment;
import ba.wurth.mb.Fragments.Reports.ReportsDailyFragment;
import ba.wurth.mb.Fragments.Reports.ReportsProductFragment;
import ba.wurth.mb.Fragments.Visits.VisitsFragment;
import ba.wurth.mb.R;

public class ClientActivity extends AppCompatActivity implements  ActionBar.TabListener, ViewPager.OnPageChangeListener  {

    private ViewPager mViewPager;
    private FragmentAdapter mAdapter;
    private Long ClientID = 0L;
    private Long DeliveryPlaceID = 0L;
    private Long PartnerID = 0L;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);

        try {

            getSupportActionBar().setTitle(R.string.Client);

            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.General)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.BaseInfo)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.MandatoryInfo)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Orders)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Requests)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Actions)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Diary)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Proposals)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.BusinessCategory)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Contacts)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Visits)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Photos)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Soa)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Report)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Products)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.DeliveryPlaces)).setTabListener(this));

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

            if (getIntent().hasExtra("ClientID")) {
                ClientID = getIntent().getLongExtra("ClientID", 0L);
                b.putLong("ClientID", ClientID);
            }

            if (getIntent().hasExtra("DeliveryPlaceID")) {
                DeliveryPlaceID = getIntent().getLongExtra("DeliveryPlaceID", 0L);
                b.putLong("DeliveryPlaceID", DeliveryPlaceID);
            }

            if (getIntent().hasExtra("_id")) {
                PartnerID = getIntent().getLongExtra("_id", 0L);
                b.putLong("PartnerID", PartnerID);
            }

            ClientGeneralFragment cg = new ClientGeneralFragment();
            DeliveryPlacesFragment dp = new DeliveryPlacesFragment();
            OrdersFragment o = new OrdersFragment();
            DiaryFragment d = new DiaryFragment();
            ProposalsFragment p = new ProposalsFragment();
            RFQFragment r = new RFQFragment();
            VisitsFragment v = new VisitsFragment();
            ClientSOAFragment csoa = new ClientSOAFragment();
            ClientContactsFragment cc = new ClientContactsFragment();
            ClientBaseFragment cb = new ClientBaseFragment();
            ClientMandatoryFragment cm = new ClientMandatoryFragment();
            ClientBusinessCategoryFragment cbc = new ClientBusinessCategoryFragment();
            ReportsDailyFragment rd = new ReportsDailyFragment();
            ReportsProductFragment rp = new ReportsProductFragment();
            ActionsFragment pla = new ActionsFragment();
            ClientImagesFragment cif = new ClientImagesFragment();

            cg.setArguments(b);
            dp.setArguments(b);
            o.setArguments(b);
            d.setArguments(b);
            p.setArguments(b);
            r.setArguments(b);
            v.setArguments(b);
            csoa.setArguments(b);
            cc.setArguments(b);
            cb.setArguments(b);
            cm.setArguments(b);
            cbc.setArguments(b);
            rd.setArguments(b);
            rp.setArguments(b);
            pla.setArguments(b);
            cif.setArguments(b);

            mAdapter.addFragment(cg);
            mAdapter.addFragment(cb);
            mAdapter.addFragment(cm);
            mAdapter.addFragment(o);
            mAdapter.addFragment(r);
            mAdapter.addFragment(pla);
            mAdapter.addFragment(d);
            mAdapter.addFragment(p);
            mAdapter.addFragment(cbc);
            mAdapter.addFragment(cc);
            mAdapter.addFragment(v);
            mAdapter.addFragment(cif);
            mAdapter.addFragment(csoa);
            mAdapter.addFragment(rd);
            mAdapter.addFragment(rp);
            mAdapter.addFragment(dp);

            mViewPager.setOnPageChangeListener(this);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setCurrentItem(0);

        }
        catch (Exception ex) {
            wurthMB.AddError("Client", ex.getMessage(), ex);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_client).setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_createorder:
                Intent k = new Intent(ClientActivity.this, OrderActivity.class);
                if (ClientID > 0L) k.putExtra("ClientID", ClientID);
                if (DeliveryPlaceID > 0L) k.putExtra("DeliveryPlaceID", DeliveryPlaceID);
                if (PartnerID > 0L) k.putExtra("PartnerID", PartnerID);
                startActivity(k);
                return true;
            case R.id.action_notevisit:
                Intent i = new Intent(ClientActivity.this, VisitActivity.class);
                if (ClientID > 0L) i.putExtra("ClientID", ClientID);
                if (DeliveryPlaceID > 0L) i.putExtra("DeliveryPlaceID", DeliveryPlaceID);
                if (PartnerID > 0L) i.putExtra("PartnerID", PartnerID);
                startActivity(i);
                return true;
            case R.id.action_client_location:
                if (ClientID > 0L && DeliveryPlaceID == 0L) {
                    Intent m = new Intent(ClientActivity.this, ClientLocationActivity.class);
                    m.putExtra("ClientID", getIntent().getLongExtra("ClientID", 0L));
                    startActivity(m);
                }
                if (DeliveryPlaceID > 0L ) {
                    Intent m = new Intent(ClientActivity.this, DeliveryPlaceLocationActivity.class);
                    m.putExtra("DeliveryPlaceID", getIntent().getLongExtra("DeliveryPlaceID", 0L));
                    startActivity(m);
                }
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