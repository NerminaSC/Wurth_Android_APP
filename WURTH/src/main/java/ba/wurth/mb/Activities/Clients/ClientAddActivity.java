package ba.wurth.mb.Activities.Clients;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.json.JSONObject;

import java.util.ArrayList;

import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Temp_Acquisition;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Temp.DL_Temp;
import ba.wurth.mb.Fragments.Clients.Add.ClientAddAutoServiceFragment;
import ba.wurth.mb.Fragments.Clients.Add.ClientAddBranchFragment;
import ba.wurth.mb.Fragments.Clients.Add.ClientAddContactsFragment;
import ba.wurth.mb.Fragments.Clients.Add.ClientAddFleetFragment;
import ba.wurth.mb.Fragments.Clients.Add.ClientAddFragment;
import ba.wurth.mb.Fragments.Clients.Add.ClientAddMachineFragment;
import ba.wurth.mb.Fragments.Clients.Add.ClientAddMapFragment;
import ba.wurth.mb.Fragments.Clients.Add.ClientAddTerminsFragment;
import ba.wurth.mb.Fragments.Clients.Add.ClientAddWashroomFragment;
import ba.wurth.mb.Fragments.Clients.Add.ClientAddWorkingTimeFragment;
import ba.wurth.mb.R;

public class ClientAddActivity extends AppCompatActivity implements  ActionBar.TabListener, ViewPager.OnPageChangeListener  {

    private ViewPager mViewPager;
    private FragmentAdapter mAdapter;

    private Button btnUpdate;
    private Button btnCancel;

    public JSONObject mTemp;
    //public JSONArray clientArray;

    public JSONObject mPartner;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_add);

        try {

            getSupportActionBar().setTitle(R.string.Client);

            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.General)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Contacts)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Fleet)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.MachinePark)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Washrooms)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.AutoService)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Branch)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.WorkingTime)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Termins)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText("Map").setTabListener(this));

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

            mAdapter.addFragment(new ClientAddFragment());
            mAdapter.addFragment(new ClientAddContactsFragment());
            mAdapter.addFragment(new ClientAddFleetFragment());
            mAdapter.addFragment(new ClientAddMachineFragment());
            mAdapter.addFragment(new ClientAddWashroomFragment());
            mAdapter.addFragment(new ClientAddAutoServiceFragment());
            mAdapter.addFragment(new ClientAddBranchFragment());
            mAdapter.addFragment(new ClientAddWorkingTimeFragment());
            mAdapter.addFragment(new ClientAddTerminsFragment());
            mAdapter.addFragment(new ClientAddMapFragment());

            mViewPager.setOnPageChangeListener(this);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setCurrentItem(0);

            mTemp = new JSONObject();

            if (getIntent().hasExtra("mTemp")) {
                mTemp = new JSONObject(getIntent().getStringExtra("mTemp"));
            }

            btnUpdate = (Button) findViewById(R.id.btnUpdate);
            btnCancel = (Button) findViewById(R.id.btnCancel);

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {

                        if (mTemp == null) return;

                        /*if (!mTemp.has(getString(R.string.Name)) || mTemp.getString(getString(R.string.Name)).equals("")
                                || !mTemp.has(getString(R.string.ClientRelation)) || mTemp.getString(getString(R.string.ClientRelation)).equals("")
                                || !mTemp.has(getString(R.string.Address)) || mTemp.getString(getString(R.string.Address)).equals("")
                                || !mTemp.has(getString(R.string.Municipal)) || mTemp.getString(getString(R.string.Municipal)).equals("")
                                || !mTemp.has(getString(R.string.Place)) || mTemp.getString(getString(R.string.Place)).equals("")
                                || !mTemp.has(getString(R.string.PostalCode)) || mTemp.getString(getString(R.string.PostalCode)).equals("")
                                || !mTemp.has(getString(R.string.IDNumber)) || mTemp.getString(getString(R.string.IDNumber)).equals("")
                                || !mTemp.has(getString(R.string.PDVNumber)) || mTemp.getString(getString(R.string.PDVNumber)).equals("")
                                || !mTemp.has(getString(R.string.ClientCourtRegister)) || mTemp.getString(getString(R.string.ClientCourtRegister)).equals("")
                                || !mTemp.has(getString(R.string.ClientMunicipalRegister)) || mTemp.getString(getString(R.string.ClientMunicipalRegister)).equals("")
                                || !mTemp.has(getString(R.string.Bank1)) || mTemp.getString(getString(R.string.Bank1)).equals("")
                                || !mTemp.has(getString(R.string.BankNumber1)) || mTemp.getString(getString(R.string.BankNumber1)).equals("")
                                || !mTemp.has(getString(R.string.Owner)) || mTemp.getString(getString(R.string.Owner)).equals("")
                                || !mTemp.has(getString(R.string.TotalWorkers)) || mTemp.getString(getString(R.string.TotalWorkers)).equals("")
                                || !mTemp.has(getString(R.string.EthicScore)) || mTemp.getString(getString(R.string.EthicScore)).equals("")
                                || !mTemp.has(getString(R.string.ClientBusinessCode)) || mTemp.getString(getString(R.string.ClientBusinessCode)).equals("")

                                || !mTemp.has(getString(R.string.VehicleType1)) || mTemp.getString(getString(R.string.VehicleType1)).equals("")
                                || !mTemp.has(getString(R.string.VehicleType2)) || mTemp.getString(getString(R.string.VehicleType2)).equals("")
                                || !mTemp.has(getString(R.string.VehicleType3)) || mTemp.getString(getString(R.string.VehicleType3)).equals("")
                                || !mTemp.has(getString(R.string.VehicleType4)) || mTemp.getString(getString(R.string.VehicleType4)).equals("")
                                || !mTemp.has(getString(R.string.VehicleType5)) || mTemp.getString(getString(R.string.VehicleType5)).equals("")

                                || !mTemp.has(getString(R.string.MachineParkType1)) || mTemp.getString(getString(R.string.MachineParkType1)).equals("")
                                || !mTemp.has(getString(R.string.MachineParkType2)) || mTemp.getString(getString(R.string.MachineParkType2)).equals("")
                                || !mTemp.has(getString(R.string.MachineParkType3)) || mTemp.getString(getString(R.string.MachineParkType3)).equals("")
                                || !mTemp.has(getString(R.string.MachineParkType4)) || mTemp.getString(getString(R.string.MachineParkType4)).equals("")

                                || !mTemp.has(getString(R.string.WashRoomType1)) || mTemp.getString(getString(R.string.WashRoomType1)).equals("")
                                || !mTemp.has(getString(R.string.WashRoomType2)) || mTemp.getString(getString(R.string.WashRoomType2)).equals("")
                                || !mTemp.has(getString(R.string.WashRoomType3)) || mTemp.getString(getString(R.string.WashRoomType3)).equals("")
                                || !mTemp.has(getString(R.string.WashRoomType4)) || mTemp.getString(getString(R.string.WashRoomType4)).equals("")
                                || !mTemp.has(getString(R.string.WashRoomType5)) || mTemp.getString(getString(R.string.WashRoomType5)).equals("")
                                || !mTemp.has(getString(R.string.WashRoomType6)) || mTemp.getString(getString(R.string.WashRoomType6)).equals("")
                                || !mTemp.has(getString(R.string.WashRoomType7)) || mTemp.getString(getString(R.string.WashRoomType7)).equals("")
                                || !mTemp.has(getString(R.string.WashRoomType8)) || mTemp.getString(getString(R.string.WashRoomType8)).equals("")
                                || !mTemp.has(getString(R.string.WashRoomType9)) || mTemp.getString(getString(R.string.WashRoomType9)).equals("")

                                || !mTemp.has(getString(R.string.AutoServiceType1)) || mTemp.getString(getString(R.string.AutoServiceType1)).equals("")
                                || !mTemp.has(getString(R.string.AutoServiceType2)) || mTemp.getString(getString(R.string.AutoServiceType2)).equals("")
                                || !mTemp.has(getString(R.string.AutoServiceType3)) || mTemp.getString(getString(R.string.AutoServiceType3)).equals("")
                                || !mTemp.has(getString(R.string.AutoServiceType4)) || mTemp.getString(getString(R.string.AutoServiceType4)).equals("")
                                || !mTemp.has(getString(R.string.AutoServiceType5)) || mTemp.getString(getString(R.string.AutoServiceType5)).equals("")
                                || !mTemp.has(getString(R.string.AutoServiceType6)) || mTemp.getString(getString(R.string.AutoServiceType6)).equals("")
                                || !mTemp.has(getString(R.string.AutoServiceType7)) || mTemp.getString(getString(R.string.AutoServiceType7)).equals("")
                                || !mTemp.has(getString(R.string.AutoServiceType8)) || mTemp.getString(getString(R.string.AutoServiceType8)).equals("")
                                || !mTemp.has(getString(R.string.AutoServiceType9)) || mTemp.getString(getString(R.string.AutoServiceType9)).equals("")
                                || !mTemp.has(getString(R.string.AutoServiceType10)) || mTemp.getString(getString(R.string.AutoServiceType10)).equals("")
                                || !mTemp.has(getString(R.string.AutoServiceType11)) || mTemp.getString(getString(R.string.AutoServiceType11)).equals("")
                                || !mTemp.has(getString(R.string.AutoServiceType12)) || mTemp.getString(getString(R.string.AutoServiceType12)).equals("")
                                || !mTemp.has(getString(R.string.AutoServiceType13)) || mTemp.getString(getString(R.string.AutoServiceType13)).equals("")
                                || !mTemp.has(getString(R.string.AutoServiceType14)) || mTemp.getString(getString(R.string.AutoServiceType14)).equals("")

                                || !mTemp.has(getString(R.string.Contacts)) || !mTemp.has(getString(R.string.Branches))

                                )
                        {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_MissingField), 2);
                            return;
                        }*/

                        if (!mTemp.has(getString(R.string.Name)) || mTemp.getString(getString(R.string.Name)).equals("")) {
                            //if (clientJsonObj == null || !clientJsonObj.has("PartnerID") || clientJsonObj.getLong("PartnerID") == 0 || clientJsonObj.getString(getString(R.string.Name)).equals("")) {
                                Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_MissingField) + "\n\r" + getString(R.string.Name).toUpperCase(), 2);
                                return;
                            //}
                        }

                        if (!mTemp.has(getString(R.string.ClientType)) || mTemp.getString(getString(R.string.ClientType)).equals("")) {
                            //if (clientJsonObj == null || !clientJsonObj.has("PartnerID") || clientJsonObj.getLong("PartnerID") == 0 || clientJsonObj.getString(getString(R.string.Name)).equals("")) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_MissingField) + "\n\r" + getString(R.string.ClientType).toUpperCase(), 2);
                            return;
                            //}
                        }

                        if (!mTemp.has(getString(R.string.ClientOrganizationType)) || mTemp.getString(getString(R.string.ClientOrganizationType)).equals("")) {
                            //if (clientJsonObj == null || !clientJsonObj.has("PartnerID") || clientJsonObj.getLong("PartnerID") == 0 || clientJsonObj.getString(getString(R.string.Name)).equals("")) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_MissingField) + "\n\r" + getString(R.string.ClientOrganizationType).toUpperCase(), 2);
                            return;
                            //}
                        }

                        if (!mTemp.has(getString(R.string.EthicScore)) || mTemp.getString(getString(R.string.EthicScore)).equals("")) {
                            //if (clientJsonObj == null || !clientJsonObj.has("PartnerID") || clientJsonObj.getLong("PartnerID") == 0 || clientJsonObj.getString(getString(R.string.Name)).equals("")) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_MissingField) + "\n\r" + getString(R.string.EthicScore).toUpperCase(), 2);
                            return;
                            //}
                        }

                        if (!mTemp.has(getString(R.string.AutoServiceType13)) || mTemp.getString(getString(R.string.AutoServiceType13)).equals("")) {
                            //if (clientJsonObj == null || !clientJsonObj.has("PartnerID") || clientJsonObj.getLong("PartnerID") == 0 || clientJsonObj.getString(getString(R.string.Name)).equals("")) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_MissingField) + "\n\r" + getString(R.string.AutoServiceType13).toUpperCase(), 2);
                            return;
                            //}
                        }

                        if (!mTemp.has("terms") || mTemp.getJSONArray("terms").length() == 0) {
                            //if (clientJsonObj == null || !clientJsonObj.has("PartnerID") || clientJsonObj.getLong("PartnerID") == 0 || clientJsonObj.getString(getString(R.string.Name)).equals("")) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_MissingField) + "\n\r" + getString(R.string.Termins).toUpperCase(), 2);
                            return;
                            //}
                        }

                        if (!mTemp.has("workingTime") || mTemp.getJSONArray("workingTime").length() == 0) {
                            //if (clientJsonObj == null || !clientJsonObj.has("PartnerID") || clientJsonObj.getLong("PartnerID") == 0 || clientJsonObj.getString(getString(R.string.Name)).equals("")) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_MissingField) + "\n\r" + getString(R.string.WorkingTime).toUpperCase(), 2);
                            return;
                            //}
                        }

                        if (!mTemp.has(getString(R.string.Branches)) || mTemp.getJSONArray(getString(R.string.Branches)).length() == 0) {
                            //if (clientJsonObj == null || !clientJsonObj.has("PartnerID") || clientJsonObj.getLong("PartnerID") == 0 || clientJsonObj.getString(getString(R.string.Name)).equals("")) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_MissingField) + "\n\r" + getString(R.string.Branches).toUpperCase(), 2);
                            return;
                            //}
                        }

                        if (!mTemp.has(getString(R.string.Contacts)) || mTemp.getJSONArray(getString(R.string.Contacts)).length() == 0) {
                            //if (clientJsonObj == null || !clientJsonObj.has("PartnerID") || clientJsonObj.getLong("PartnerID") == 0 || clientJsonObj.getString(getString(R.string.Name)).equals("")) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_MissingField) + "\n\r" + getString(R.string.Contacts).toUpperCase(), 2);
                            return;
                            //}
                        }

                        /*if (!mTemp.has(getString(R.string.Address)) || mTemp.getString(getString(R.string.Address)).equals("")) {
                            if (clientJsonObj == null || !clientJsonObj.has("PartnerID") || clientJsonObj.getLong("PartnerID") == 0 || clientJsonObj.getString(getString(R.string.Address)).equals("")) {
                                Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_MissingField) + "\n\r" + getString(R.string.Address).toUpperCase(), 2);
                                return;
                            }
                        }*/

                        Temp_Acquisition temp = new Temp_Acquisition();

                        temp.AccountID = wurthMB.getUser().AccountID;
                        temp.UserID = wurthMB.getUser().UserID;
                        temp.OptionID = 2;
                        temp.ID = 0;
                        temp.Sync = 0;
                        temp.DOE = System.currentTimeMillis();
                        temp.jsonObj = mTemp.toString();

                        //return;

                        if (DL_Temp.AddOrUpdate(temp) > 0 ) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_Saved), 0);
                            finish();
                        }
                        else {
                            Notifications.hideLoading(getApplicationContext());
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.SystemError), 1);
                        }

                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

        }
        catch (Exception ex) {
            wurthMB.AddError("TEMP_Aquisition", ex.getMessage(), ex);
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
        public Fragment getItem(int position) {
            switch (position) {
                case 9:
                    return ClientAddMapFragment.newInstance();
                default:
                    return mFragments.get(position);
            }
        }
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