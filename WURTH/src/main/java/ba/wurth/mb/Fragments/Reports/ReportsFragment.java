package ba.wurth.mb.Fragments.Reports;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ba.wurth.mb.Activities.HomeActivity;
import ba.wurth.mb.R;

public class ReportsFragment extends Fragment implements  ActionBar.TabListener, ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private FragmentAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            ((HomeActivity) getActivity()).getSupportActionBar().setTitle(R.string.Reports);

            ((HomeActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            ((HomeActivity) getActivity()).getSupportActionBar().addTab(((HomeActivity) getActivity()).getSupportActionBar().newTab().setText(getString(R.string.ReportDaily)).setTabListener(this));
            //((HomeActivity) getActivity()).getSupportActionBar().addTab(((HomeActivity) getActivity()).getSupportActionBar().newTab().setText(getString(R.string.ReportMonthly)).setTabListener(this));
        }
        catch (Exception ex) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reports, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            mViewPager = (ViewPager) view.findViewById(R.id.pager);

            mAdapter = new FragmentAdapter(getChildFragmentManager());


            mAdapter.addFragment(new ReportsDailyFragment());

            mViewPager.setOnPageChangeListener(ReportsFragment.this);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setCurrentItem(0);

        }
        catch (Exception ex) {

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            ((HomeActivity) getActivity()).getSupportActionBar().removeAllTabs();
            ((HomeActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
        catch (Exception ex) {

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
    public void onPageSelected(int position) { ((HomeActivity) getActivity()).getSupportActionBar().setSelectedNavigationItem(position); }
}

