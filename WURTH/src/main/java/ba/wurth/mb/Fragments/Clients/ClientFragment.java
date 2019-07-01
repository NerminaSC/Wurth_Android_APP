package ba.wurth.mb.Fragments.Clients;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

import ba.wurth.mb.Activities.HomeActivity;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Temp.DL_Temp;
import ba.wurth.mb.Fragments.Orders.OrdersFragment;
import ba.wurth.mb.Fragments.Visits.VisitsFragment;
import ba.wurth.mb.R;

public class ClientFragment extends Fragment implements  ActionBar.TabListener, ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private FragmentAdapter mAdapter;

    private Button btnUpdate;
    private Button btnCancel;
    private Fragment f;

    private Long ClientID = 0L;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            f = this;

            ((HomeActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            ((HomeActivity) getActivity()).getSupportActionBar().addTab(((HomeActivity) getActivity()).getSupportActionBar().newTab().setText(getString(R.string.General)).setTabListener(this));
            //((HomeActivity) getActivity()).getSupportActionBar().addTab(((HomeActivity) getActivity()).getSupportActionBar().newTab().setText(getString(R.string.DeliveryPlaces)).setTabListener(this));
            ((HomeActivity) getActivity()).getSupportActionBar().addTab(((HomeActivity) getActivity()).getSupportActionBar().newTab().setText(getString(R.string.Orders)).setTabListener(this));
            ((HomeActivity) getActivity()).getSupportActionBar().addTab(((HomeActivity) getActivity()).getSupportActionBar().newTab().setText(getString(R.string.Visits)).setTabListener(this));

            if (getArguments() != null && getArguments().getLong("ClientID", 0L) > 0) {
                ClientID = getArguments().getLong("ClientID");
            }
        }
        catch (Exception ex) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            mViewPager = (ViewPager) view.findViewById(R.id.pager);

            mAdapter = new FragmentAdapter(getChildFragmentManager());

            Bundle b = new Bundle();
            b.putLong("ClientID", ClientID);

            VisitsFragment v = new VisitsFragment();
            v.setArguments(b);

            OrdersFragment o = new OrdersFragment();
            o.setArguments(b);

            ClientGeneralFragment c = new ClientGeneralFragment();
            c.setArguments(b);


            mAdapter.addFragment(c);
            //mAdapter.addFragment(new TestFragment());
            mAdapter.addFragment(o);
            mAdapter.addFragment(v);

            mViewPager.setOnPageChangeListener(this);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setCurrentItem(0);

            bindListeners();
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

    private void bindListeners() {
        try {


            btnUpdate = (Button) getView().findViewById(R.id.btnUpdate);
            btnCancel = (Button) getView().findViewById(R.id.btnCancel);

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DL_Temp.Delete();
                    wurthMB.setOrder(null);
                    getActivity().getSupportFragmentManager().beginTransaction().remove(f).commit();
                }
            });

            getView().setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if( i == KeyEvent.KEYCODE_BACK )
                    {
                        return true;
                    }
                    return false;
                }
            });

            if (wurthMB.getOrder().Sync == 1 && (wurthMB.getOrder().OrderStatusID == 4 || wurthMB.getOrder().OrderStatusID == 5)) getView().findViewById(R.id.llActions).setVisibility(View.GONE);

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
