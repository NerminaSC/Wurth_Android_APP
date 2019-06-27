package ba.wurth.mb.Activities.DeliveryPlaces;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import androidx.fragment.app.FragmentManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Date;

import ba.wurth.mb.Activities.Orders.OrderActivity;
import ba.wurth.mb.Activities.Visits.VisitActivity;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Additional.DL_Additional;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Products.DL_Products;
import ba.wurth.mb.Fragments.DeliveryPlaces.DeliveryPlaceCompetitionFragment;
import ba.wurth.mb.Fragments.DeliveryPlaces.DeliveryPlaceGeneralFragment;
import ba.wurth.mb.Fragments.DeliveryPlaces.DeliveryPlaceMerchandisesFragment;
import ba.wurth.mb.Fragments.DeliveryPlaces.DeliveryPlaceProductPlacementFragment;
import ba.wurth.mb.Fragments.DeliveryPlaces.DeliveryPlacePropertiesFragment;
import ba.wurth.mb.R;

public class DeliveryPlaceActivity extends AppCompatActivity implements  ActionBar.TabListener, ViewPager.OnPageChangeListener  {

    private ViewPager mViewPager;
    private FragmentAdapter mAdapter;

    private Long DeliveryPlaceID = 0L;
    private Long ClientID = 0L;

    private int PropertyID;
    private int ValueID;
    private Long ProductID = 0L;
    private int col1;
    private int col2;
    private int col3;
    private int col4;
    private int col5;
    private int col6;
    private Long MerchandiseID;
    private Double Quantity;
    private Long CompetitionID = 0L;
    private double Price = 0D;

    private Dialog dialog;

    private String array_spinner[];

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);

        try {

            getSupportActionBar().setTitle(R.string.DeliveryPlace);

            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.General)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Properties)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Merchandise)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.ProductPlacement)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Competitions)).setTabListener(this));

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

            if (getIntent().hasExtra("DeliveryPlaceID")) {
                DeliveryPlaceID = getIntent().getLongExtra("DeliveryPlaceID", 0L);
                b.putLong("DeliveryPlaceID", DeliveryPlaceID);
            }

            if (getIntent().hasExtra("ClientID")) {
                ClientID = getIntent().getLongExtra("ClientID", 0L);
                b.putLong("ClientID", ClientID);
            }

            DeliveryPlaceGeneralFragment dpg = new DeliveryPlaceGeneralFragment();
            DeliveryPlacePropertiesFragment dpp = new DeliveryPlacePropertiesFragment();
            DeliveryPlaceMerchandisesFragment dpm = new DeliveryPlaceMerchandisesFragment();
            DeliveryPlaceProductPlacementFragment dppp = new DeliveryPlaceProductPlacementFragment();
            DeliveryPlaceCompetitionFragment dpc = new DeliveryPlaceCompetitionFragment();

            dpg.setArguments(b);
            dpp.setArguments(b);
            dpm.setArguments(b);
            dppp.setArguments(b);
            dpc.setArguments(b);

            mAdapter.addFragment(dpg);
            mAdapter.addFragment(dpp);
            mAdapter.addFragment(dpm);
            mAdapter.addFragment(dppp);
            mAdapter.addFragment(dpc);

            mViewPager.setOnPageChangeListener(this);
            mViewPager.setAdapter(mAdapter);

            array_spinner = new String[5];
            array_spinner[0]="A";
            array_spinner[1]="B";
            array_spinner[2]="C";
            array_spinner[3]="D";
            array_spinner[4]="E";
        }
        catch (Exception ex) {
            wurthMB.AddError("DeliveryPlace", ex.getMessage(), ex);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setCurrentItem(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_deliveryplace).setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            case R.id.action_deliveryplace_createorder:
                Intent k = new Intent(DeliveryPlaceActivity.this, OrderActivity.class);
                if (ClientID > 0L) k.putExtra("ClientID", ClientID);
                if (DeliveryPlaceID > 0L) k.putExtra("DeliveryPlaceID", DeliveryPlaceID);
                startActivity(k);
                return true;

            case R.id.action_deliveryplace_notevisit:
                Intent i = new Intent(DeliveryPlaceActivity.this, VisitActivity.class);
                if (ClientID > 0L) i.putExtra("ClientID", ClientID);
                if (DeliveryPlaceID > 0L) i.putExtra("DeliveryPlaceID", DeliveryPlaceID);
                startActivity(i);
                return true;

            case R.id.action_deliveryplace_location:
                Intent m = new Intent(DeliveryPlaceActivity.this, DeliveryPlaceLocationActivity.class);
                m.putExtra("DeliveryPlaceID", getIntent().getLongExtra("DeliveryPlaceID", 0L));
                startActivity(m);
                return true;

            case R.id.action_deliveryplace_properties:

                if (dialog != null) dialog = null;

                dialog = new Dialog(DeliveryPlaceActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_deliveryplace_properties);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                final Spinner spProperties = ((Spinner) dialog.findViewById(R.id.spProperties));
                final Spinner spValues = ((Spinner) dialog.findViewById(R.id.spValues));

                Cursor curStatus = DL_Additional.Get_DeliveryPlacesProperties();
                DeliveryPlaceActivity.this.startManagingCursor(curStatus);

                SimpleCursorAdapter speciesSpinnerAdapterStatus = new SimpleCursorAdapter(DeliveryPlaceActivity.this, R.layout.simple_dropdown_item_1line, curStatus, new String[] { "Name" }, new int[] {android.R.id.text1});
                spProperties.setAdapter(speciesSpinnerAdapterStatus);

                spProperties.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        Cursor cur = (Cursor) parent.getItemAtPosition(pos);
                        PropertyID =  cur.getInt(cur.getColumnIndex("DPPID"));

                        Cursor curDeliveryPlacesPropertiesOptions = DL_Additional.Get_DeliveryPlacesPropertiesOptions(cur.getInt(cur.getColumnIndex("DPPID")));
                        DeliveryPlaceActivity.this.startManagingCursor(curDeliveryPlacesPropertiesOptions);

                        SimpleCursorAdapter speciesSpinnerAdapterStatus = new SimpleCursorAdapter(DeliveryPlaceActivity.this, R.layout.simple_dropdown_item_1line, curDeliveryPlacesPropertiesOptions, new String[] { "Name" }, new int[] {android.R.id.text1});
                        spValues.setAdapter(speciesSpinnerAdapterStatus);

                        spValues.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int _pos, long id) {
                                Cursor _cur = (Cursor) parent.getItemAtPosition(_pos);
                                ValueID =  _cur.getInt(_cur.getColumnIndex("DPPOID"));
                            }
                            public void onNothingSelected(AdapterView<?> parent) { }
                        });
                    }

                    public void onNothingSelected(AdapterView<?> parent) { }
                });


                dialog.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        try {
                            if (DeliveryPlaceID > 0L && PropertyID > 0 && ValueID > 0) {
                                ContentValues cv = new ContentValues();
                                cv.put("DPPID", PropertyID);
                                cv.put("DPPOID", ValueID);
                                cv.put("ObjectID", DeliveryPlaceID);
                                cv.put("AccountID", wurthMB.getUser().AccountID);
                                cv.put("DOE", new Date().getTime());
                                cv.put("Sync", 0);

                                wurthMB.dbHelper.getDB().beginTransaction();
                                wurthMB.dbHelper.getDB().delete("ClientDeliveryPlacesProperties", "AccountID = ? AND ObjectID = ? AND DPPID = ?",new String [] {Long.toString(wurthMB.getUser().AccountID), Long.toString(DeliveryPlaceID), Integer.toString(PropertyID)});
                                wurthMB.dbHelper.getDB().insert("ClientDeliveryPlacesProperties", null, cv);
                                wurthMB.dbHelper.getDB().setTransactionSuccessful();
                                wurthMB.dbHelper.getDB().endTransaction();
                                Notifications.showNotification(DeliveryPlaceActivity.this, getString(R.string.Success), getString(R.string.DatabaseUpdated), 0);

                                if (mAdapter.getItem(mViewPager.getCurrentItem()) instanceof DeliveryPlacePropertiesFragment) {
                                    ((DeliveryPlacePropertiesFragment) mAdapter.getItem(mViewPager.getCurrentItem())).bindData();
                                }
                            }
                        }
                        catch (Exception ex) {

                        }

                        dialog.dismiss();
                    }
                });

                dialog.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

                return true;

            case R.id.action_deliveryplace_product_placement:

                if (dialog != null) dialog = null;

                dialog = new Dialog(DeliveryPlaceActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_deliveryplace_product_placement);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                final Spinner spCol6 = ((Spinner) dialog.findViewById(R.id.spCol6));
                final AutoCompleteTextView txbProducts = ((AutoCompleteTextView) dialog.findViewById(R.id.txbProducts));
                final EditText txbCol1 = ((EditText) dialog.findViewById(R.id.txbCol1));
                final EditText txbCol2 = ((EditText) dialog.findViewById(R.id.txbCol2));
                final EditText txbCol4 = ((EditText) dialog.findViewById(R.id.txbCol4));

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(DeliveryPlaceActivity.this, R.layout.simple_dropdown_item_1line, array_spinner);
                spCol6.setAdapter(adapter);

                SimpleCursorAdapter _mAdapter  = new SimpleCursorAdapter(DeliveryPlaceActivity.this, R.layout.simple_dropdown_item_1line, null, new String[] { "Name" }, new int[] {android.R.id.text1}, 0);

                _mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                    public Cursor runQuery(CharSequence str) {
                        try {
                            Cursor cur = DL_Products.GetProducts(txbProducts.getText().toString().replaceAll("^\\s+", ""));
                            return cur;
                        }
                        catch (Exception ex) { }
                        return null;
                    }
                });

                _mAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
                    public CharSequence convertToString(Cursor cur) {
                        int index = cur.getColumnIndex("Name");
                        return cur.getString(index);
                    }
                });

                txbProducts.setAdapter(_mAdapter);
                txbProducts.setThreshold(0);

                txbProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        ProductID = DL_Products.GetIDByLongID(l);
                    }
                });

                dialog.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        try {
                            col1 = Integer.parseInt(txbCol1.getText().toString());
                            col2 = Integer.parseInt(txbCol2.getText().toString());
                            col4 = Integer.parseInt(txbCol4.getText().toString());
                            col6 = spCol6.getSelectedItemPosition();

                            if (DeliveryPlaceID > 0L && ProductID > 0 && col1 > 0 && col2 > 0) {

                                ContentValues _values = new ContentValues();
                                _values.put("ProductID", ProductID);
                                _values.put("ProductCategoryID", 0);
                                _values.put("DeliveryPlaceID", DeliveryPlaceID);
                                _values.put("AccountID", wurthMB.getUser().AccountID);
                                _values.put("Note", "");
                                _values.put("Col1", col1);
                                _values.put("Col2", col2);
                                _values.put("Col3", col3);
                                _values.put("Col4", col4);
                                _values.put("Col5", col5);
                                _values.put("Col6", col6);
                                _values.put("UserID", wurthMB.getUser().UserID);
                                _values.put("DOE", new Date().getTime());
                                _values.put("Active", 1);
                                _values.put("Sync", 0);
                                wurthMB.dbHelper.getDB().insert("DeliveryPlaceProductPlacement", null, _values);
                                Notifications.showNotification(DeliveryPlaceActivity.this, "", getString(R.string.DatabaseUpdated),0);

                                if (mAdapter.getItem(mViewPager.getCurrentItem()) instanceof DeliveryPlaceProductPlacementFragment) {
                                    ((DeliveryPlaceProductPlacementFragment) mAdapter.getItem(mViewPager.getCurrentItem())).bindData();
                                }
                            }
                        }
                        catch (Exception ex) {

                        }

                        dialog.dismiss();
                    }
                });

                dialog.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

                return true;

            case R.id.action_deliveryplace_merchandises:

                if (dialog != null) dialog = null;
                dialog = new Dialog(DeliveryPlaceActivity.this);

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_deliveryplace_merchandises);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                final Spinner spMerchandise = ((Spinner) dialog.findViewById(R.id.spMerchandise));

                Cursor cur = DL_Additional.Get_Merchandise();
                DeliveryPlaceActivity.this.startManagingCursor(cur);

                SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(DeliveryPlaceActivity.this, R.layout.simple_dropdown_item_1line, cur, new String[] { "Name" }, new int[] {android.R.id.text1});
                spMerchandise.setAdapter(spinnerAdapter);

                spMerchandise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        Cursor cur = (Cursor) parent.getItemAtPosition(pos);
                        MerchandiseID =  cur.getLong(cur.getColumnIndex("MerchandiseID"));
                    }

                    public void onNothingSelected(AdapterView<?> parent) { }
                });

                dialog.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {

                            Quantity = Double.parseDouble(((EditText) dialog.findViewById(R.id.txbQuantity)).getText().toString());

                            if (DeliveryPlaceID > 0L && MerchandiseID > 0 && Quantity != 0) {
                                ContentValues values = new ContentValues();
                                values.put("AccountID", wurthMB.getUser().AccountID);
                                values.put("MerchandiseID", MerchandiseID);
                                values.put("DeliveryPlaceID", DeliveryPlaceID);
                                values.put("Notes", "");
                                values.put("Count", Quantity);
                                values.put("Type", 0);
                                values.put("DOE", System.currentTimeMillis());
                                values.put("Sync", 0);
                                wurthMB.dbHelper.getDB().insert("DeliveryPlaceMerchandise", null, values);
                                Notifications.showNotification(DeliveryPlaceActivity.this, "", getString(R.string.DatabaseUpdated),0);

                                if (mAdapter.getItem(mViewPager.getCurrentItem()) instanceof DeliveryPlaceMerchandisesFragment) {
                                    ((DeliveryPlaceMerchandisesFragment) mAdapter.getItem(mViewPager.getCurrentItem())).bindData();
                                }
                            }
                        }
                        catch (Exception ex) {

                        }
                        dialog.dismiss();
                    }
                });

                dialog.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

                return true;

            case R.id.action_deliveryplace_competitions:

                if (dialog != null) dialog = null;
                dialog = new Dialog(DeliveryPlaceActivity.this);

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_deliveryplace_competitions);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                final Spinner spCompetitiona = ((Spinner) dialog.findViewById(R.id.spCompetitiona));
                final Spinner spProduct = ((Spinner) dialog.findViewById(R.id.spProduct));
                final EditText txbPrice = ((EditText) dialog.findViewById(R.id.txbPrice));

                Cursor curCompetitions = DL_Additional.Get_Competitions("");
                DeliveryPlaceActivity.this.startManagingCursor(curCompetitions);

                SimpleCursorAdapter speciesSpinnerAdapter = new SimpleCursorAdapter(DeliveryPlaceActivity.this, R.layout.simple_dropdown_item_1line, curCompetitions, new String[] { "Name" }, new int[] {android.R.id.text1});
                spCompetitiona.setAdapter(speciesSpinnerAdapter);

                spCompetitiona.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        Cursor cur = (Cursor) parent.getItemAtPosition(pos);
                        CompetitionID =  cur.getLong(cur.getColumnIndex("CompetitionID"));

                        Cursor _cur = DL_Additional.Get_Competition_Items(CompetitionID, "");
                        DeliveryPlaceActivity.this.startManagingCursor(_cur);

                        SimpleCursorAdapter speciesSpinnerAdapterStatus = new SimpleCursorAdapter(DeliveryPlaceActivity.this, R.layout.simple_dropdown_item_1line, _cur, new String[] { "Name" }, new int[] {android.R.id.text1});
                        spProduct.setAdapter(speciesSpinnerAdapterStatus);

                        spProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int _pos, long id) {
                                Cursor _cur = (Cursor) parent.getItemAtPosition(_pos);
                                ProductID =  _cur.getLong(_cur.getColumnIndex("ItemID"));
                            }
                            public void onNothingSelected(AdapterView<?> parent) { }
                        });
                    }

                    public void onNothingSelected(AdapterView<?> parent) { }
                });


                dialog.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        try {

                            Price = Double.parseDouble(txbPrice.getText().toString());

                            if (DeliveryPlaceID > 0L && CompetitionID > 0L && ProductID > 0L && Price > 0D) {

                                ContentValues _values = new ContentValues();
                                _values.put("ItemID", ProductID);
                                _values.put("DeliveryPlaceID", DeliveryPlaceID);
                                _values.put("AccountID", wurthMB.getUser().AccountID);
                                _values.put("Note", "");
                                _values.put("Value", Price);
                                _values.put("Col1", col1);
                                _values.put("Col2", 0);
                                _values.put("Col3", 0);
                                _values.put("Col4", 0);
                                _values.put("Col5", 0);
                                _values.put("Col6", 0);
                                _values.put("UserID", wurthMB.getUser().UserID);
                                _values.put("DOE", new Date().getTime());
                                _values.put("Sync", 0);
                                wurthMB.dbHelper.getDB().insert("Competition_Items_Values", null, _values);
                                Notifications.showNotification(DeliveryPlaceActivity.this, getString(R.string.Success), getString(R.string.DatabaseUpdated), 0);

                                if (mAdapter.getItem(mViewPager.getCurrentItem()) instanceof DeliveryPlaceCompetitionFragment) {
                                    ((DeliveryPlaceCompetitionFragment) mAdapter.getItem(mViewPager.getCurrentItem())).bindData();
                                }
                            }
                        }
                        catch (Exception ex) {

                        }

                        dialog.dismiss();
                    }
                });

                dialog.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

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