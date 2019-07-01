package ba.wurth.mb.Activities.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Record;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Activities.DL_UserActivityLog;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Fragments.Activities.ActivityFragment;
import ba.wurth.mb.R;

public class ActivityActivity extends AppCompatActivity implements  ActionBar.TabListener, ViewPager.OnPageChangeListener  {

    private ViewPager mViewPager;
    private FragmentAdapter mAdapter;

    private Button btnUpdate;
    private Button btnCancel;

    public Record mRecord;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        try {

            getSupportActionBar().setTitle(R.string.Activities);
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.General)).setTabListener(this));
            //getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Photos)).setTabListener(this));
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

            mRecord = new Record();
            mRecord.AccountID = wurthMB.getUser().AccountID;
            mRecord.UserID = wurthMB.getUser().UserID;
            mRecord.UserName = wurthMB.getUser().Firstname + " " + wurthMB.getUser().Lastname;
            mRecord.OptionID = 2;
            mRecord.MediaID = 2;
            mRecord.Sync = false;

            mViewPager = (ViewPager) findViewById(R.id.pager);

            mAdapter = new FragmentAdapter(getSupportFragmentManager());

            Bundle b = new Bundle();

            ActivityFragment af = new ActivityFragment();

            af.setArguments(b);

            mAdapter.addFragment(af);

            mViewPager.setOnPageChangeListener(this);
            mViewPager.setAdapter(mAdapter);

            btnUpdate = (Button) findViewById(R.id.btnUpdate);
            btnCancel = (Button) findViewById(R.id.btnCancel);

            bindListeners();

        }
        catch (Exception ex) {
            wurthMB.AddError("Activity", ex.getMessage(), ex);
        }
    }

    private void bindListeners() {
        try {
            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mRecord == null) return;

                    if (mRecord.endTime < mRecord.startTime) {
                        Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_VisitEndDateHigher), 2);
                        return;
                    }

                    if (wurthMB.currentBestLocation != null) {
                        mRecord.startLatitude = (long) (wurthMB.currentBestLocation.getLatitude() * 10000000);
                        mRecord.endLatitude = (long) (wurthMB.currentBestLocation.getLatitude() * 10000000);
                        mRecord.startLongitude = (long) (wurthMB.currentBestLocation.getLongitude() * 10000000);
                        mRecord.endLongitude = (long) (wurthMB.currentBestLocation.getLongitude() * 10000000);
                    }

                    mRecord.Duration = (int) ((mRecord.endTime - mRecord.startTime) / 1000);
                    mRecord.DOE = System.currentTimeMillis();

                    Notifications.showLoading(ActivityActivity.this);
                    if (DL_UserActivityLog.AddOrUpdate(mRecord) > 0 ) {
                        Notifications.hideLoading(ActivityActivity.this);
                        Notifications.showNotification(ActivityActivity.this, "", getString(R.string.DatabaseUpdated), 0);
                        finish();
                    }
                    else {
                        Notifications.hideLoading(ActivityActivity.this);
                        Notifications.showNotification(ActivityActivity.this, "", getString(R.string.SystemError), 1);
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