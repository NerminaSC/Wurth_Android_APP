package ba.wurth.mb.Activities.Orders;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import ba.wurth.mb.Activities.Visits.VisitActivity;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Order;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Orders.DL_Orders;
import ba.wurth.mb.DataLayer.Visits.DL_Visits;
import ba.wurth.mb.Fragments.Orders.OrderAdditionalFragment;
import ba.wurth.mb.Fragments.Orders.OrderItemsFragment;
import ba.wurth.mb.Fragments.Orders.OrderPropertiesFragment;
import ba.wurth.mb.R;
import ba.wurth.mb.Services.BluetoothService;

public class OrderActivity extends AppCompatActivity implements  ActionBar.TabListener, ViewPager.OnPageChangeListener  {

    private ViewPager mViewPager;
    private FragmentAdapter mAdapter;

    private Button btnUpdate;
    private Button btnComplete;
    private Button btnCancel;
    private Button btnSignature;
    private Fragment f;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order);

        try {

            BluetoothService.activity = this;

            getSupportActionBar().setTitle(R.string.Order);

            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            //getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.General)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.OrderProperties)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Items)).setTabListener(this));
            getSupportActionBar().addTab(getSupportActionBar().newTab().setText(getString(R.string.Additional)).setTabListener(this));

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

            if (wurthMB.getOrder() == null) {
                if (getIntent().hasExtra("_id")) {
                    Order o = DL_Orders.GetByID(getIntent().getLongExtra("_id", 0L));
                    if (o == null) {
                        finish();
                        return;
                    }
                    else {
                        wurthMB.setOrder(o);
                    }
                }
                else {
                    wurthMB.setOrder(new Order());
                }

                if (getIntent().hasExtra("PartnerID")) {
                    wurthMB.getOrder().PartnerID = getIntent().getLongExtra("PartnerID", 0L);
                    wurthMB.setOrder(wurthMB.getOrder());
                }

                if (getIntent().hasExtra("ClientID")) {
                    wurthMB.getOrder().ClientID = getIntent().getLongExtra("ClientID", 0L);
                    wurthMB.getOrder().ClientName = DL_Clients.GetByClientID(getIntent().getLongExtra("ClientID", 0L)).Name;
                    wurthMB.setOrder(wurthMB.getOrder());
                }

                if (getIntent().hasExtra("DeliveryPlaceID")) {
                    wurthMB.getOrder().DeliveryPlaceID = getIntent().getLongExtra("DeliveryPlaceID", 0L);

                    Cursor _cur = DL_Clients.Get_DeliveryPlaceByDeliveryPlaceID(getIntent().getLongExtra("DeliveryPlaceID", 0L));
                    if( _cur != null) {
                        if (_cur.getCount() > 0 && _cur.moveToFirst()) {
                            wurthMB.getOrder().DeliveryPlaceName = _cur.getString(_cur.getColumnIndex("Naziv"));
                        }
                        _cur.close();
                    }
                    wurthMB.setOrder(wurthMB.getOrder());
                }

                //mAdapter.addFragment(new OrderGeneralFragment());
                mAdapter.addFragment(new OrderPropertiesFragment());
                mAdapter.addFragment(new OrderItemsFragment());
                mAdapter.addFragment(new OrderAdditionalFragment());

                mViewPager.setOnPageChangeListener(this);
                mViewPager.setAdapter(mAdapter);
                mViewPager.setCurrentItem(0);

                bindListeners();
            }
            else {
                if (getIntent().hasExtra("ACTION") && getIntent().getIntExtra("ACTION", 0) == 1) {

                    if (wurthMB.getOrder() == null) {
                        wurthMB.setOrder(new Order());
                    }

                    if (getIntent().hasExtra("PartnerID")) {
                        wurthMB.getOrder().PartnerID = getIntent().getLongExtra("PartnerID", 0L);
                        wurthMB.setOrder(wurthMB.getOrder());
                    }

                    if (getIntent().hasExtra("ClientID")) {
                        wurthMB.getOrder().ClientID = getIntent().getLongExtra("ClientID", 0L);
                        wurthMB.getOrder().ClientName = DL_Clients.GetByClientID(getIntent().getLongExtra("ClientID", 0L)).Name;
                        wurthMB.setOrder(wurthMB.getOrder());
                    }

                    if (getIntent().hasExtra("DeliveryPlaceID")) {
                        wurthMB.getOrder().DeliveryPlaceID = getIntent().getLongExtra("DeliveryPlaceID", 0L);

                        Cursor _cur = DL_Clients.Get_DeliveryPlaceByDeliveryPlaceID(getIntent().getLongExtra("DeliveryPlaceID", 0L));
                        if( _cur != null) {
                            if (_cur.getCount() > 0 && _cur.moveToFirst()) {
                                wurthMB.getOrder().DeliveryPlaceName = _cur.getString(_cur.getColumnIndex("Name"));
                            }
                            _cur.close();
                        }
                        wurthMB.setOrder(wurthMB.getOrder());
                    }

                    //mAdapter.addFragment(new OrderGeneralFragment());
                    mAdapter.addFragment(new OrderPropertiesFragment());
                    mAdapter.addFragment(new OrderItemsFragment());
                    mAdapter.addFragment(new OrderAdditionalFragment());

                    mViewPager.setOnPageChangeListener(OrderActivity.this);
                    mViewPager.setAdapter(mAdapter);
                    mViewPager.setCurrentItem(0);

                    bindListeners();
                }
                else {
                    final Dialog dialog = new Dialog(OrderActivity.this, R.style.CustomDialog);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.alert);
                    dialog.setCancelable(false);

                    ((TextView) dialog.findViewById(R.id.Title)).setText(R.string.Notification_AttentionNeeded);
                    ((TextView) dialog.findViewById(R.id.text)).setText(R.string.Notification_OrderExist);

                    dialog.findViewById(R.id.dialogButtonNEW).setVisibility(View.VISIBLE);
                    dialog.findViewById(R.id.dialogButtonCANCEL).setVisibility(View.GONE);
                    ((Button) dialog.findViewById(R.id.dialogButtonOK)).setText(R.string.Continue);

                    dialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            //mAdapter.addFragment(new OrderGeneralFragment());
                            mAdapter.addFragment(new OrderPropertiesFragment());
                            mAdapter.addFragment(new OrderItemsFragment());
                            mAdapter.addFragment(new OrderAdditionalFragment());

                            mViewPager.setOnPageChangeListener(OrderActivity.this);
                            mViewPager.setAdapter(mAdapter);
                            mViewPager.setCurrentItem(0);

                            bindListeners();

                            dialog.dismiss();
                        }
                    });

                    dialog.findViewById(R.id.dialogButtonNEW).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            wurthMB.setOrder(new Order());

                            //mAdapter.addFragment(new OrderGeneralFragment());
                            mAdapter.addFragment(new OrderPropertiesFragment());
                            mAdapter.addFragment(new OrderItemsFragment());
                            mAdapter.addFragment(new OrderAdditionalFragment());

                            mViewPager.setOnPageChangeListener(OrderActivity.this);
                            mViewPager.setAdapter(mAdapter);
                            mViewPager.setCurrentItem(0);

                            bindListeners();

                            dialog.dismiss();
                        }
                    });

                    dialog.findViewById(R.id.dialogButtonCANCEL).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            if (getIntent() != null && getIntent().getLongExtra("_id", 0L) > 0) {
                                Order o = DL_Orders.GetByID(getIntent().getLongExtra("_id", 0));
                                if (o == null) {
                                    finish();
                                    return;
                                }
                                else {
                                    wurthMB.setOrder(o);
                                }
                            }
                            else {
                                wurthMB.setOrder(new Order());
                            }

                            if (getIntent().hasExtra("PartnerID")) {
                                wurthMB.getOrder().PartnerID = getIntent().getLongExtra("PartnerID", 0L);
                                wurthMB.setOrder(wurthMB.getOrder());
                            }

                            if (getIntent().hasExtra("ClientID")) {
                                wurthMB.getOrder().ClientID = getIntent().getLongExtra("ClientID", 0L);
                                wurthMB.getOrder().ClientName = DL_Clients.GetByClientID(getIntent().getLongExtra("ClientID", 0L)).Name;
                                wurthMB.setOrder(wurthMB.getOrder());
                            }

                            if (getIntent().hasExtra("DeliveryPlaceID")) {
                                wurthMB.getOrder().DeliveryPlaceID = getIntent().getLongExtra("DeliveryPlaceID", 0L);

                                Cursor _cur = DL_Clients.Get_DeliveryPlaceByDeliveryPlaceID(getIntent().getLongExtra("DeliveryPlaceID", 0L));
                                if( _cur != null) {
                                    if (_cur.getCount() > 0 && _cur.moveToFirst()) {
                                        wurthMB.getOrder().DeliveryPlaceName = _cur.getString(_cur.getColumnIndex("Name"));
                                    }
                                    _cur.close();
                                }
                                wurthMB.setOrder(wurthMB.getOrder());
                            }

                            //mAdapter.addFragment(new OrderGeneralFragment());
                            mAdapter.addFragment(new OrderPropertiesFragment());
                            mAdapter.addFragment(new OrderItemsFragment());
                            mAdapter.addFragment(new OrderAdditionalFragment());

                            mViewPager.setOnPageChangeListener(OrderActivity.this);
                            mViewPager.setAdapter(mAdapter);
                            mViewPager.setCurrentItem(0);

                            bindListeners();

                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }

            }

        }
        catch (Exception ex) {
            wurthMB.AddError("Order", ex.getMessage(), ex);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (wurthMB.getOrder() != null) {

                if (wurthMB.getOrder().Sync == 1) {
                    wurthMB.setOrder(null);
                    finish();
                    return true;
                }

                final Dialog dialog = new Dialog(OrderActivity.this, R.style.CustomDialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.alert);

                ((TextView) dialog.findViewById(R.id.Title)).setText(R.string.Notification_AttentionNeeded);
                ((TextView) dialog.findViewById(R.id.text)).setText(R.string.AreYouSureCancel);

                dialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        wurthMB.setOrder(null);
                        finish();
                    }
                });

                dialog.findViewById(R.id.dialogButtonCANCEL).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
            else super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
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
    protected void onDestroy() {
        super.onDestroy();
        BluetoothService.activity = null;
    }

    private void bindListeners() {
        try {

            btnUpdate = (Button) findViewById(R.id.btnUpdate);
            btnComplete = (Button) findViewById(R.id.btnComplete);
            btnCancel = (Button) findViewById(R.id.btnCancel);
            btnSignature = (Button) findViewById(R.id.btnSignature);

            btnComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mViewPager.setCurrentItem(0);
                }
            });

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (wurthMB.getOrder() != null) {
                        wurthMB.getOrder().CalculateTotal();

                        if (wurthMB.getOrder().ClientID == 0) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_ClientNotSelected), 2);
                            return;
                        }

                        wurthMB.getOrder().PartnerID = DL_Wurth.FIND_Partner(wurthMB.getOrder().PartnerID);
                        //CHECK FOR CLIENT ID
                        if (wurthMB.getOrder().PartnerID == 0) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_ClientNotSelected), 0);
                            return;
                        }

                        /*if (wurthMB.getUser().hasDeliveryPlaces == 1 && wurthMB.getOrder().DeliveryPlaceID == 0) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_DeliveryPlaceNotSelected), 2);
                            return;
                        }*/

                        if (wurthMB.getOrder().PaymentMethodID == 0) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_PaymentMethodNotSelected), 2);
                            return;
                        }

                        if (wurthMB.getOrder().items.size() == 0) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_ItemMissing), 2);
                            return;
                        }

                        if (wurthMB.getOrder().PaymentDate < wurthMB.getOrder().OrderDate) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_OrderDateHigherPaymentDate), 2);
                            return;
                        }

                        if (wurthMB.getOrder().DeliveryDate < wurthMB.getOrder().OrderDate) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_OrderDateHigherDeliveryDate), 2);
                            return;
                        }

                        if (wurthMB.getOrder().visit != null && wurthMB.getOrder().visit.startDT > wurthMB.getOrder().visit.endDT) {
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_VisitEndDateHigher), 2);
                            return;
                        }

                        if (wurthMB.currentBestLocation != null) {
                            wurthMB.getOrder().Latitude = (long) (wurthMB.currentBestLocation.getLatitude() * 10000000);
                            wurthMB.getOrder().Longitude = (long) (wurthMB.currentBestLocation.getLongitude() * 10000000);
                        }

                        if (wurthMB.getOrder().visit != null && wurthMB.getOrder().visit._id == 0) {
                            wurthMB.getOrder().visit.ClientID = wurthMB.getOrder().ClientID;
                            wurthMB.getOrder().visit.DeliveryPlaceID = wurthMB.getOrder().DeliveryPlaceID;
                            wurthMB.getOrder().visit.Longitude = wurthMB.getOrder().Longitude;
                            wurthMB.getOrder().visit.Latitude = wurthMB.getOrder().Latitude;
                            wurthMB.getOrder().visit.startDT = wurthMB.getOrder().DOE;
                            wurthMB.getOrder().visit.endDT = System.currentTimeMillis();
                            DL_Visits.AddOrUpdate(wurthMB.getOrder().visit);
                            wurthMB.getOrder()._VisitID = wurthMB.getOrder().visit._id;
                        }

                        try {
                            JSONObject relation = new JSONObject(wurthMB.getOrder().Relations);

                            relation.put("DOE", System.currentTimeMillis());
                            relation.put("PartnerID", wurthMB.getOrder().PartnerID);

                            if (wurthMB.getOrder().OrderID == 0L) {
                                relation.put("Action", "New");
                                relation.put("RefOrderID", "0");
                            }
                            else {
                                relation.put("Action", "Edit");
                                relation.put("RefOrderID", Long.toString(wurthMB.getOrder().OrderID));
                            }
                            wurthMB.getOrder().Relations = relation.toString();
                        }
                        catch (JSONException exx) {

                        }

                        if (wurthMB.getOrder().OrderStatusID == 11) {
                            final Dialog ProposalDialog = new Dialog(OrderActivity.this);
                            ProposalDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                            ProposalDialog.setContentView(R.layout.proposal_dialog);
                            ProposalDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                            try {
                                JSONObject relation = new JSONObject(wurthMB.getOrder().Relations);

                                if (relation.has("Proposal_Name")) {
                                    ((EditText) ProposalDialog.findViewById(R.id.lblName)).setText(relation.getString("Proposal_Name"));
                                }

                                if (relation.has("Proposal_Note")) {
                                    ((EditText) ProposalDialog.findViewById(R.id.txbNote)).setText(relation.getString("Proposal_Note"));
                                }

                                if (relation.has("Proposal_IsOpened")) {
                                    ((CheckBox) ProposalDialog.findViewById(R.id.chkOpenedProposal)).setChecked(relation.getBoolean("Proposal_IsOpened"));
                                }

                                if (relation.has("Proposal_StartDate")) {
                                    ((EditText) ProposalDialog.findViewById(R.id.txbStartDate)).setText(relation.getString("Proposal_StartDate"));
                                }

                                if (relation.has("Proposal_EndDate")) {
                                    ((EditText) ProposalDialog.findViewById(R.id.txbToDate)).setText(relation.getString("Proposal_EndDate"));
                                }

                                ProposalDialog.findViewById(R.id.btnUpdate).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        try {

                                            JSONObject relation = new JSONObject(wurthMB.getOrder().Relations);

                                            relation.put("Proposal_Name", ((EditText) ProposalDialog.findViewById(R.id.lblName)).getText().toString());
                                            relation.put("Proposal_Note", ((EditText) ProposalDialog.findViewById(R.id.txbNote)).getText().toString());
                                            relation.put("Proposal_IsOpened", ((CheckBox) ProposalDialog.findViewById(R.id.chkOpenedProposal)).isChecked());
                                            relation.put("Proposal_StartDate", ((EditText) ProposalDialog.findViewById(R.id.txbStartDate)).getText().toString());
                                            relation.put("Proposal_EndDate", ((EditText) ProposalDialog.findViewById(R.id.txbToDate)).getText().toString());

                                            wurthMB.getOrder().Relations = relation.toString();

                                            Notifications.showLoading(getApplicationContext());

                                            if (DL_Orders.AddOrUpdate(wurthMB.getOrder()) > 0 ) {
                                                Notifications.hideLoading(getApplicationContext());
                                                Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_OrderSaved), 0);

                                                if (wurthMB.getOrder().visit != null && wurthMB.getOrder().visit._id > 0L) {
                                                    Intent k = new Intent(OrderActivity.this, VisitActivity.class);
                                                    k.putExtra("_id", wurthMB.getOrder().visit._id);
                                                    k.putExtra("_OrderID", wurthMB.getOrder()._id);
                                                    startActivity(k);
                                                }
                                                wurthMB.setOrder(null);
                                                finish();
                                            }
                                            else {
                                                Notifications.hideLoading(getApplicationContext());
                                                Notifications.showNotification(getApplicationContext(), "", getString(R.string.SystemError), 1);
                                            }

                                        }
                                        catch (Exception exx) {

                                        }
                                    }
                                });

                            }
                            catch (JSONException exx) {

                            }
                            ProposalDialog.show();
                            return;
                        }

                        Notifications.showLoading(getApplicationContext());

                        if (DL_Orders.AddOrUpdate(wurthMB.getOrder()) > 0 ) {
                            Notifications.hideLoading(getApplicationContext());
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_OrderSaved), 0);

                            if (wurthMB.getOrder().visit != null && wurthMB.getOrder().visit._id > 0L) {
                                Intent k = new Intent(OrderActivity.this, VisitActivity.class);
                                k.putExtra("_id", wurthMB.getOrder().visit._id);
                                k.putExtra("_OrderID", wurthMB.getOrder()._id);
                                startActivity(k);
                            }
                            wurthMB.setOrder(null);
                            finish();
                        }
                        else {
                            Notifications.hideLoading(getApplicationContext());
                            Notifications.showNotification(getApplicationContext(), "", getString(R.string.SystemError), 1);
                        }
                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (wurthMB.getOrder() != null) {

                        if (wurthMB.getOrder().Sync == 1) {
                            wurthMB.setOrder(null);
                            finish();
                            return;
                        }

                        final Dialog dialog = new Dialog(OrderActivity.this, R.style.CustomDialog);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.alert);
                        dialog.setCancelable(false);

                        ((TextView) dialog.findViewById(R.id.Title)).setText(R.string.Notification_AttentionNeeded);
                        ((TextView) dialog.findViewById(R.id.text)).setText(R.string.AreYouSureCancel);

                        dialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                dialog.dismiss();
                                wurthMB.setOrder(null);
                                finish();
                            }
                        });

                        dialog.findViewById(R.id.dialogButtonCANCEL).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                }
            });

            btnSignature.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialog = new Dialog(OrderActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.order_signature);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    if (wurthMB.getOrder() != null && wurthMB.getOrder().client != null) {
                        Client c = DL_Wurth.GET_Client(wurthMB.getOrder().ClientID);
                        if (c != null) ((TextView) dialog.findViewById(R.id.txbClient_Code)).setText(c.code);
                    }

                    dialog.findViewById(R.id.btnUpdate).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                GestureOverlayView gestureView = (GestureOverlayView) dialog.findViewById(R.id.gv);
                                gestureView.setDrawingCacheEnabled(true);
                                Bitmap bm = Bitmap.createBitmap(gestureView.getDrawingCache());
                                //compress to specified format (PNG), quality - which is ignored for PNG, and out stream
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                                byteArrayOutputStream.close();

                                if (wurthMB.getOrder() != null) {
                                    JSONObject relation = new JSONObject(wurthMB.getOrder().Relations);
                                    relation.put("Signature", encodedImage);
                                    relation.put("Client_Number", ((EditText) dialog.findViewById(R.id.txbClient_Number)).getText().toString());
                                    relation.put("Client_Code", ((TextView) dialog.findViewById(R.id.txbClient_Code)).getText().toString());
                                    relation.put("Client_Password", ((EditText) dialog.findViewById(R.id.txbPassword)).getText().toString());
                                    wurthMB.getOrder().Relations = relation.toString();
                                    wurthMB.setOrder(wurthMB.getOrder());
                                    //((OrderPropertiesFragment) mAdapter.getItem(0)).bindData();
                                }
                            }
                            catch (Exception exx) {

                            }
                            dialog.dismiss();
                        }
                    });


                    dialog.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    
                    
                    dialog.show();
                }
            });

            if (wurthMB.getOrder().Sync == 1 && (wurthMB.getOrder().OrderStatusID == 4 || wurthMB.getOrder().OrderStatusID == 5)) findViewById(R.id.llActions).setVisibility(View.GONE);

        }
        catch (Exception ex) {

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
            Fragment fragment = mAdapter.getItem(tab.getPosition());

            switch (tab.getPosition()) {
                case 1:
                    if (fragment instanceof OrderItemsFragment) {
                        if (mAdapter.getItem(0) instanceof OrderPropertiesFragment){
                            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
                            inputManager.hideSoftInputFromWindow(((OrderPropertiesFragment) mAdapter.getItem(0)).txbClients.getWindowToken(), 0);
                            inputManager.hideSoftInputFromWindow(((OrderPropertiesFragment) mAdapter.getItem(0)).txbDeliveryPlaces.getWindowToken(), 0);
                        }
                        ((OrderItemsFragment) fragment).bindData();
                    }
                    break;
                case 2:
                    if (fragment instanceof OrderAdditionalFragment) {
                        if (mAdapter.getItem(0) instanceof OrderPropertiesFragment){
                            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
                            inputManager.hideSoftInputFromWindow(((OrderPropertiesFragment) mAdapter.getItem(0)).txbClients.getWindowToken(), 0);
                            inputManager.hideSoftInputFromWindow(((OrderPropertiesFragment) mAdapter.getItem(0)).txbDeliveryPlaces.getWindowToken(), 0);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        catch (Exception ex) {

        }
    }

    public void bindItems() {
        try {
            ((OrderItemsFragment) mAdapter.getItem(1)).bindData();
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
    public void onPageSelected(int position) {
        try {
            getSupportActionBar().setSelectedNavigationItem(position);

            if (position == 0) {
                btnComplete.setVisibility(View.GONE);
                btnUpdate.setVisibility(View.VISIBLE);
            }

            if (position == 1) {
                btnComplete.setVisibility(View.VISIBLE);
                btnUpdate.setVisibility(View.GONE);
            }
        }
        catch (Exception ex) {

        }
    }
}