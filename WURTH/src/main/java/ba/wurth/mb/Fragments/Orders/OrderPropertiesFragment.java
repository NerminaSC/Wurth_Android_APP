package ba.wurth.mb.Fragments.Orders;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import ba.wurth.mb.Activities.Tasks.TaskActivity;
import ba.wurth.mb.Adapters.AutoCompleteClientsAdapter;
import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.OrderItem;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Tasks.DL_Tasks;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class OrderPropertiesFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private SimpleCursorAdapter mAdapterClients;
    private SimpleCursorAdapter mAdapterDeliveryPlaces;

    public AutoCompleteTextView txbClients;
    public AutoCompleteTextView txbDeliveryPlaces;

    private ImageButton btnClearClients;
    private ImageButton btnClearDeliveryPlace;

    private EditText txbNote;
    private EditText txbOrder_CodeUser;
    private EditText txbOrder_CodeClient;

    private CheckBox chkCompleteOrder;

    private TextView litOrderDate;
    private TextView litPaymentDate;

    public Spinner spStatus;
    public Spinner spPaymentMethod;
    public Spinner spOrderType;
    public Spinner spDeliveryType;

    private SpinnerItem[] ordertype_items;
    private SpinnerItem[] paymentmethod_items;
    private SpinnerItem[] orderstatus_items;
    private SpinnerItem[] deliverytype_items;

    private SpinnerAdapter adapter_orderstatus;
    private SpinnerAdapter adapter_paymentmethod;
    private SpinnerAdapter adapter_ordertype;
    private SpinnerAdapter adapter_deliverytype;


    private Calendar calendar;
    public static final String ORDERDATE_TAG = "0";
    public static final String PAYMENTDATE_TAG = "2";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.order_properties, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        litOrderDate = (TextView) getView().findViewById(R.id.litOrderDate);
        litPaymentDate = (TextView) getView().findViewById(R.id.litPaymentDate);

        txbNote = (EditText) getView().findViewById(R.id.txbNote);
        txbOrder_CodeUser = (EditText) getView().findViewById(R.id.txbOrder_CodeUser);
        txbOrder_CodeClient = (EditText) getView().findViewById(R.id.txbOrder_CodeClient);

        chkCompleteOrder = (CheckBox) getView().findViewById(R.id.chkCompleteOrder);

        spStatus = (Spinner) getView().findViewById(R.id.spStatus);
        spPaymentMethod = (Spinner) getView().findViewById(R.id.spPaymentMethod);
        spOrderType = (Spinner) getView().findViewById(R.id.spOrderType);
        spDeliveryType = (Spinner) getView().findViewById(R.id.spDeliveryType);

        calendar = Calendar.getInstance();

        txbClients = (AutoCompleteTextView) getView().findViewById(R.id.txbClients);
        txbDeliveryPlaces = (AutoCompleteTextView) getView().findViewById(R.id.txbDeliveryPlaces);

        btnClearClients = (ImageButton) getView().findViewById(R.id.btnClearClients);
        btnClearDeliveryPlace = (ImageButton) getView().findViewById(R.id.btnClearDeliveryPlace);

        bindData();
        bindListeners();
        checkTasks();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {

        Calendar c = Calendar.getInstance();

        switch (Integer.parseInt(datePickerDialog.getTag())) {
            case 0:
                litOrderDate.setText(day + "." + (month + 1) + "." + year);
                c.set(year, month, day, 0, 0, 0);
                litOrderDate.setContentDescription(Long.toString(c.getTimeInMillis()));
                wurthMB.getOrder().OrderDate = c.getTimeInMillis();
                break;
            case 2:
                litPaymentDate.setText(day + "." + (month + 1) + "." + year);
                c.set(year, month, day, 0, 0, 0);
                litPaymentDate.setContentDescription(Long.toString(c.getTimeInMillis()));
                wurthMB.getOrder().PaymentDate = c.getTimeInMillis();
                break;
            default:
                break;
        }
        saveOrder();
    }

    private void bindListeners() {
        try {

            calendar.setTimeInMillis(wurthMB.getOrder().OrderDate);
            final DatePickerDialog datePickerDialogOrderDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);

            calendar.setTimeInMillis(wurthMB.getOrder().PaymentDate);
            final DatePickerDialog datePickerDialogPaymentDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);

            datePickerDialogOrderDate.setYearRange(2010, calendar.get(Calendar.YEAR) + 2);
            datePickerDialogPaymentDate.setYearRange(2010, calendar.get(Calendar.YEAR) + 2);

            litOrderDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datePickerDialogOrderDate.show(getFragmentManager(), ORDERDATE_TAG);
                }
            });

            litPaymentDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datePickerDialogPaymentDate.show(getFragmentManager(), PAYMENTDATE_TAG);
                }
            });

            spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    saveOrder();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spPaymentMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    saveOrder();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spDeliveryType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    saveOrder();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spOrderType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    saveOrder();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


            txbNote.addTextChangedListener(new textWatcher());

            btnClearClients.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    txbClients.setText("");
                    txbClients.setContentDescription("0");
                    txbDeliveryPlaces.setText("");
                    txbDeliveryPlaces.setContentDescription("0");
                    wurthMB.getOrder().ClientID = 0L;
                    wurthMB.getOrder().client = null;
                    wurthMB.getOrder().DeliveryPlaceID = 0L;
                    wurthMB.setOrder(wurthMB.getOrder());
                }
            });

            btnClearDeliveryPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    txbDeliveryPlaces.setText("");
                    txbDeliveryPlaces.setContentDescription("0");
                    wurthMB.getOrder().DeliveryPlaceID = 0L;
                    wurthMB.setOrder(wurthMB.getOrder());
                }
            });

            mAdapterClients = new AutoCompleteClientsAdapter(getActivity());

            mAdapterClients.setFilterQueryProvider(new FilterQueryProvider() {
                public Cursor runQuery(CharSequence str) {
                    try {
                        Cursor cur = DL_Clients.Get(txbClients.getText().toString());
                        return cur;
                    }
                    catch (Exception ex) { }
                    return null;
                }
            });

            mAdapterClients.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
                public CharSequence convertToString(Cursor cur) {
                    try {
                        int index = cur.getColumnIndex("Name");
                        return cur.getString(index);
                    }
                    catch (Exception ex) {}
                    return "";
                }
            });

            mAdapterDeliveryPlaces = new SimpleCursorAdapter(getActivity(), R.layout.simple_dropdown_item_1line, null, new String[] { "Name" }, new int[] {android.R.id.text1}, 0);

            mAdapterDeliveryPlaces.setFilterQueryProvider(new FilterQueryProvider() {
                public Cursor runQuery(CharSequence str) {
                    try {
                        if (Long.parseLong(txbClients.getContentDescription().toString()) > 0) {
                            Cursor cur = DL_Clients.Get_DeliveryPlacesByClientLongID(Long.parseLong(txbClients.getContentDescription().toString()), txbDeliveryPlaces.getText().toString().replaceAll("^\\s+", ""));
                            return cur;
                        }
                    }
                    catch (Exception ex) { }
                    return null;
                }
            });

            mAdapterDeliveryPlaces.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
                public CharSequence convertToString(Cursor cur) {
                    try {
                        int index = cur.getColumnIndex("Name");
                        return cur.getString(index);
                    }
                    catch (Exception ex) {}
                    return "";
                }
            });

            txbClients.setAdapter(mAdapterClients);
            txbClients.setThreshold(0);

            txbClients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    txbClients.setContentDescription(Long.toString(l));

                    if (Long.parseLong(txbDeliveryPlaces.getContentDescription().toString()) > 0) txbDeliveryPlaces.setText("");
                    txbDeliveryPlaces.setContentDescription("0");

                    Cursor cur = DL_Wurth.GET_Partner(l);
                    wurthMB.getOrder().PartnerID = l;

                    if (cur != null) {
                        if (cur.moveToFirst()) {
                            wurthMB.getOrder().client = DL_Wurth.GET_Client(cur.getLong(cur.getColumnIndex("ClientID")));
                            wurthMB.getOrder().ClientID = wurthMB.getOrder().client.ClientID;
                            wurthMB.getOrder().DeliveryPlaceID = cur.getLong(cur.getColumnIndex("DeliveryPlaceID"));
                            wurthMB.getOrder().PaymentMethodID = 0;
                            wurthMB.getOrder().items = new ArrayList<OrderItem>();

                            txbDeliveryPlaces.setContentDescription(cur.getString(cur.getColumnIndex("DeliveryPlaceID")));
                            saveOrder();
                            checkTasks();
                        }
                        cur.close();
                    }

                    bindPaymentMethods();
                    bindDeliveryType();
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(txbClients.getWindowToken(), 0);

                    //txbDeliveryPlaces.requestFocus();
                }
            });

            txbDeliveryPlaces.setAdapter(mAdapterDeliveryPlaces);
            txbDeliveryPlaces.setThreshold(0);

            txbDeliveryPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    txbDeliveryPlaces.setContentDescription(Long.toString(l));
                    wurthMB.getOrder().DeliveryPlaceID = l;
                    wurthMB.getOrder().DeliveryPlaceID = DL_Clients.Get_DeliveryPlaceID(l);
                    saveOrder();

                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(txbDeliveryPlaces.getWindowToken(), 0);
                }
            });
        }
        catch (Exception ex) {

        }
    }

    public void bindData() {
        try {

            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

            /* Order Status */
            orderstatus_items = new SpinnerItem[6];
            orderstatus_items[0] = new SpinnerItem(4L, getString(R.string.Order), "", "");
            orderstatus_items[1] = new SpinnerItem(1L, getString(R.string.OrderStatus_Template), "", "");
            orderstatus_items[2] = new SpinnerItem(12L, getString(R.string.OrderStatus_GetOffer), "", "");
            orderstatus_items[3] = new SpinnerItem(11L, getString(R.string.Proposal), "", "");
            orderstatus_items[4] = new SpinnerItem(13L, getString(R.string.Orsy), "", "");
            orderstatus_items[5] = new SpinnerItem(14L, getString(R.string.ArrangedPrice), "", "");
            adapter_orderstatus = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, orderstatus_items);
            spStatus.setAdapter(adapter_orderstatus);

            /* Order Type */
            ordertype_items = new SpinnerItem[5];
            ordertype_items[0] = new SpinnerItem(0L, getString(R.string.OrderType_Visit), "", "");
            ordertype_items[1] = new SpinnerItem(1L, getString(R.string.OrderType_Telephone), "", "");
            ordertype_items[2] = new SpinnerItem(2L, getString(R.string.OrderType_Tele), "", "");
            ordertype_items[3] = new SpinnerItem(3L, getString(R.string.OrderType_Email), "", "");
            ordertype_items[4] = new SpinnerItem(4L, getString(R.string.OrderType_Other), "", "");
            adapter_ordertype = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, ordertype_items);
            spOrderType.setAdapter(adapter_ordertype);

            if (wurthMB.getOrder() != null) {

                litOrderDate.setText(df.format(new Date(wurthMB.getOrder().OrderDate)));
                litOrderDate.setContentDescription(Long.toString(wurthMB.getOrder().OrderDate));

                litPaymentDate.setText(df.format(new Date(wurthMB.getOrder().PaymentDate)));
                litPaymentDate.setContentDescription(Long.toString(wurthMB.getOrder().PaymentDate));

                txbNote.setText(wurthMB.getOrder().Note);
                
                JSONObject relation = new JSONObject(wurthMB.getOrder().Relations);

                if (adapter_orderstatus != null) {
                    if (relation.has("OrderStatusID")) {
                        int i = 0;
                        for (SpinnerItem item : orderstatus_items) {
                            if (item.getId() == relation.getInt("OrderStatusID")) {
                                break;
                            }
                            i++;
                        }
                        if (i < orderstatus_items.length) spStatus.setSelection(i);
                    }
                }

                if (adapter_paymentmethod != null) {
                    if (relation.has("PaymentMethodID")) {
                        int i = 0;
                        for (SpinnerItem item : paymentmethod_items) {
                            if (item.getId() ==  relation.getInt("PaymentMethodID") ) {
                                break;
                            }
                            if (item.getId() == 303L && relation.getInt("PaymentMethodID") == 1 ) {
                                break;
                            }
                            if (item.getId() == 304L && relation.getInt("PaymentMethodID") == 2) {
                                break;
                            }
                            i++;
                        }
                        if (i < paymentmethod_items.length) spPaymentMethod.setSelection(i);
                    }
                }

                if (adapter_ordertype != null) {
                    if (relation.has("OrderTypeID")) {
                        int i = 0;
                        for (SpinnerItem item : ordertype_items) {
                            if (item.getId() == relation.getInt("OrderTypeID")) {
                                break;
                            }
                            i++;
                        }
                        if (i < ordertype_items.length) spOrderType.setSelection(i);
                    }
                }


                if (relation.has("CompleteOrder")) { chkCompleteOrder.setChecked(relation.getBoolean("CompleteOrder")); }
                if (relation.has("CodeUser")) { txbOrder_CodeUser.setText(relation.getString("CodeUser")); }
                if (relation.has("CodeClient")) { txbOrder_CodeClient.setText(relation.getString("CodeClient")); }

                //if (wurthMB.getOrder()._VisitID > 0L) getView().findViewById(R.id.llVisit).setVisibility(View.GONE);

                if (wurthMB.getOrder().ClientID > 0L) {
                    Client c  = DL_Wurth.GET_Client(wurthMB.getOrder().ClientID);
                    if (c != null) {
                        wurthMB.getOrder().client = c;
                        txbClients.setText(c.Name);
                        txbClients.setContentDescription(Long.toString(c._id));
                        if (wurthMB.getOrder().DeliveryPlaceID == 0L) txbDeliveryPlaces.requestFocus();
                        bindPaymentMethods();
                        bindDeliveryType();
                    }
                }
                else if (wurthMB.getOrder().Sync == 0) {
                    txbClients.requestFocus();
                    InputMethodManager imm =(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }

                if (wurthMB.getOrder().DeliveryPlaceID > 0L) {
                    Cursor c = DL_Clients.Get_DeliveryPlaceByDeliveryPlaceID(wurthMB.getOrder().DeliveryPlaceID);
                    if (c != null && c.getCount() > 0 && c.moveToFirst()) {
                        txbDeliveryPlaces.setText(c.getString(c.getColumnIndex("Naziv")));
                        txbDeliveryPlaces.setContentDescription(c.getString(c.getColumnIndex("DeliveryPlaceID")));
                        c.close();
                    }
                }
                else if (wurthMB.getOrder().Sync == 0) {
                    InputMethodManager imm =(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }

                if (wurthMB.getOrder()._id == 0 && wurthMB.getOrder().ClientID == 0 && wurthMB.currentBestLocation != null) {

                    Cursor cur = DL_Clients.Get_ClientClosest((long) (wurthMB.currentBestLocation.getLatitude() * 10000000D), (long) (wurthMB.currentBestLocation.getLongitude() * 10000000D));

                    if (cur != null) {

                        if (cur.moveToFirst()) {

                            Double Distance = Common.distFrom(wurthMB.currentBestLocation.getLatitude(), wurthMB.currentBestLocation.getLongitude(), cur.getDouble(cur.getColumnIndex("Latitude")) / 10000000D, cur.getDouble(cur.getColumnIndex("Longitude")) / 10000000D);

                            if (Distance > 100) return;



                            txbClients.setText(cur.getString(cur.getColumnIndex("Naziv")));
                            txbClients.setContentDescription(cur.getString(cur.getColumnIndex("_clientid")));

                            if (Long.parseLong(txbDeliveryPlaces.getContentDescription().toString()) > 0) txbDeliveryPlaces.setText("");
                            txbDeliveryPlaces.setContentDescription("0");

                            Cursor _cur = DL_Wurth.GET_Partner(cur.getLong(cur.getColumnIndex("_clientid")));
                            wurthMB.getOrder().PartnerID = cur.getLong(cur.getColumnIndex("_clientid"));

                            if (_cur != null) {
                                if (_cur.moveToFirst()) {
                                    wurthMB.getOrder().client = DL_Wurth.GET_Client(_cur.getLong(_cur.getColumnIndex("ClientID")));
                                    wurthMB.getOrder().ClientID = wurthMB.getOrder().client.ClientID;
                                    wurthMB.getOrder().DeliveryPlaceID = _cur.getLong(_cur.getColumnIndex("DeliveryPlaceID"));
                                    wurthMB.getOrder().PaymentMethodID = 0;
                                    txbDeliveryPlaces.setContentDescription(_cur.getString(_cur.getColumnIndex("DeliveryPlaceID")));
                                    saveOrder();
                                    checkTasks();
                                }
                                _cur.close();
                            }

                            bindPaymentMethods();
                            bindDeliveryType();

                        }
                        cur.close();
                    }
                }

                if (wurthMB.getOrder().Sync == 1 && (wurthMB.getOrder().OrderStatusID == 4 || wurthMB.getOrder().OrderStatusID == 5)) {

                    try {
                        ViewGroup group = (ViewGroup) getView().findViewById(R.id.llContainer);
                        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                            View view = group.getChildAt(i);
                            view.setEnabled(false);
                            if (view instanceof EditText) {
                                //((EditText) view).setTextAppearance(getActivity(), R.style.label_textbox_readonly);
                            }
                        }

                        txbClients.setEnabled(false);
                        btnClearClients.setEnabled(false);

                    } catch (Exception e) {
                    }
                }
            }

            bindPaymentMethods();
            bindDeliveryType();
        }
        catch (Exception ex) {

        }
    }

    private class textWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            try {
                saveOrder();
            }
            catch (Exception ex) {
            }
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }


    private void bindPaymentMethods() {
        try {
            /* Payment method */
            if (wurthMB.getOrder().client != null && wurthMB.getOrder().client.Veleprodaja == 1) paymentmethod_items = new SpinnerItem[2];
            else  paymentmethod_items = new SpinnerItem[1];

            paymentmethod_items[0] = new SpinnerItem(303L, getString(R.string.Retail), "", "");
            if (wurthMB.getOrder().client != null && wurthMB.getOrder().client.Veleprodaja == 1) paymentmethod_items[1] = new SpinnerItem(304L, getString(R.string.Wholesale), "", "");

            adapter_paymentmethod = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, paymentmethod_items);
            spPaymentMethod.setAdapter(adapter_paymentmethod);


            if (wurthMB.getOrder() != null) {
                JSONObject relation = new JSONObject(wurthMB.getOrder().Relations);
                if (adapter_paymentmethod != null) {
                    if (relation.has("PaymentMethodID")) {
                        int i = 0;
                        for (SpinnerItem item : paymentmethod_items) {
                            if (item.getId() == relation.getInt("PaymentMethodID")) {
                                break;
                            }
                            i++;
                        }
                        if (i < paymentmethod_items.length) spPaymentMethod.setSelection(i);
                    }
                }
            }
        }
        catch (Exception ex) {

        }
    }

    private void bindDeliveryType() {
        try {
            /* Delivery type */
            if (wurthMB.getOrder().client != null && wurthMB.getOrder().client.BrzaIsporuka == 1) deliverytype_items = new SpinnerItem[3];
            else deliverytype_items = new SpinnerItem[2];

            deliverytype_items[0] = new SpinnerItem(2L, getString(R.string.Normal), "", "");
            deliverytype_items[1] = new SpinnerItem(0L, getString(R.string.OrderDeliveryType_Fast24), "", "");

            if (wurthMB.getOrder().client != null && wurthMB.getOrder().client.BrzaIsporuka == 1) deliverytype_items[2] = new SpinnerItem(1L, getString(R.string.OrderDeliveryType_Fast6), "", "");

            adapter_deliverytype = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, deliverytype_items);
            spDeliveryType.setAdapter(adapter_deliverytype);


            if (wurthMB.getOrder() != null) {
                JSONObject relation = new JSONObject(wurthMB.getOrder().Relations);
                if (adapter_deliverytype != null) {
                    if (relation.has("DeliveryTypeID")) {
                        int i = 0;
                        for (SpinnerItem item : deliverytype_items) {
                            if (item.getId() == relation.getInt("DeliveryTypeID")) {
                                break;
                            }
                            i++;
                        }
                        if (i < deliverytype_items.length) spDeliveryType.setSelection(i);
                    }
                }
            }
        }
        catch (Exception ex) {

        }
    }

    private void saveOrder() {
        try {
            if (wurthMB.getOrder() != null) {

                JSONObject relation = new JSONObject(wurthMB.getOrder().Relations);

                wurthMB.getOrder().OrderStatusID = orderstatus_items[spStatus.getSelectedItemPosition()].getId().intValue();
                relation.put("OrderStatusID", orderstatus_items[spStatus.getSelectedItemPosition()].getId());
                relation.put("OrderStatusName", orderstatus_items[spStatus.getSelectedItemPosition()].getName());
                wurthMB.getOrder().PaymentMethodID = paymentmethod_items[spPaymentMethod.getSelectedItemPosition()].getId().intValue();
                relation.put("PaymentMethodID", paymentmethod_items[spPaymentMethod.getSelectedItemPosition()].getId());
                relation.put("PaymentMethodName", paymentmethod_items[spPaymentMethod.getSelectedItemPosition()].getName());
                relation.put("OrderTypeID", ordertype_items[spOrderType.getSelectedItemPosition()].getId());
                relation.put("OrderTypeName", ordertype_items[spOrderType.getSelectedItemPosition()].getName());
                relation.put("DeliveryTypeID", deliverytype_items[spDeliveryType.getSelectedItemPosition()].getId());
                relation.put("DeliveryTypeName", deliverytype_items[spDeliveryType.getSelectedItemPosition()].getName());

                relation.put("CompleteOrder", chkCompleteOrder.isChecked());
                relation.put("CodeUser", txbOrder_CodeUser.getText().toString());
                relation.put("CodeClient", txbOrder_CodeClient.getText().toString());

                wurthMB.getOrder().Relations = relation.toString();

                wurthMB.getOrder().Note = txbNote.getText().toString();

                wurthMB.setOrder(wurthMB.getOrder());
            }
        }
        catch (Exception ex) {

        }
    }

    private void checkTasks() {
        try {
            if (wurthMB.getOrder() != null && wurthMB.getOrder().ClientID > 0) {

                final JSONObject jsonObject = DL_Tasks.GET_CLIENT_TaskPending(wurthMB.getOrder().ClientID);

                if (jsonObject != null) {
                    final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_taks_pending);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    ((TextView) dialog.findViewById(R.id.litName)).setText(jsonObject.getString("Name"));
                    ((TextView) dialog.findViewById(R.id.litDescription)).setText(jsonObject.getString("Description"));

                    dialog.findViewById(R.id.btnOrder).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.findViewById(R.id.btnTasks).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            try {
                                Intent i = new Intent(getActivity(), TaskActivity.class);
                                i.putExtra("TaskID", jsonObject.getLong("TaskID"));
                                getActivity().startActivity(i);
                                dialog.dismiss();
                            }
                            catch (Exception ex) { }
                        }
                    });

                    dialog.show();
                }
            }
        }
        catch (Exception ex) {

        }
    }
}
