package ba.wurth.mb.Fragments.Routes;

import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ba.wurth.mb.Activities.Routes.RouteActivity;
import ba.wurth.mb.Adapters.AutoCompleteClientsAdapter;
import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Route;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Routes.DL_Routes;
import ba.wurth.mb.Fragments.Map.MapRouteFragment;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class RouteInfoFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private TextView txbDate;
    private EditText txbName;
    private EditText txbDesc;

    private Spinner spDays;
    private Spinner spDaily;
    private Spinner spType;
    private Spinner spWeekNumber;

    private LinearLayout llDaily;
    private LinearLayout llDate;
    private LinearLayout llLocations;

    private Button btnAddClient;
    private Button btnUpdate;
    private Button btnCancel;

    public AutoCompleteTextView txbClients;
    public AutoCompleteTextView txbDeliveryPlaces;

    private ImageButton btnClearClients;
    private ImageButton btnClearDeliveryPlace;

    private SimpleCursorAdapter mAdapterClients;
    private SimpleCursorAdapter mAdapterDeliveryPlaces;

    private SpinnerItem[] daily_items;
    private SpinnerItem[] days_items;
    private SpinnerItem[] type_items;
    private SpinnerItem[] week_number_items;

    private SpinnerAdapter adapter_daily;
    private SpinnerAdapter adapter_days;
    private SpinnerAdapter adapter_type;
    private SpinnerAdapter adapter_week_number;

    private Calendar calendar;

    private Fragment mFragment;
    private LayoutInflater mInflater;

    private Route mRoute;

    private SimpleDateFormat formatter = new SimpleDateFormat("d.MM.yyyy");
    private SimpleDateFormat tf = new SimpleDateFormat("HH:mm");

    public static final String STARTTIME_TAG = "1";
    public static final String ENDTIME_TAG = "2";
    public String CURRENT_TAG = "0";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment = this;
        mRoute = ((RouteActivity) getActivity()).mRoute;
        mInflater = LayoutInflater.from(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.route_info, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txbDate = (TextView) getView().findViewById(R.id.txbDate);
        txbName = (EditText) getView().findViewById(R.id.txbName);
        txbDesc = (EditText) getView().findViewById(R.id.txbDesc);

        llDaily = (LinearLayout) getView().findViewById(R.id.llDaily);
        llDate = (LinearLayout) getView().findViewById(R.id.llDate);
        llLocations = (LinearLayout) getView().findViewById(R.id.llLocations);

        spDays = (Spinner) getView().findViewById(R.id.spDays);
        spDaily = (Spinner) getView().findViewById(R.id.spDaily);
        spType = (Spinner) getView().findViewById(R.id.spType);
        spWeekNumber = (Spinner) getView().findViewById(R.id.spWeekNumber);

        btnAddClient = (Button) getView().findViewById(R.id.btnAddClient);
        btnUpdate = (Button) getView().findViewById(R.id.btnUpdate);
        btnCancel = (Button) getView().findViewById(R.id.btnCancel);

        txbClients = (AutoCompleteTextView) getView().findViewById(R.id.txbClients);
        txbDeliveryPlaces = (AutoCompleteTextView) getView().findViewById(R.id.txbDeliveryPlaces);

        btnClearClients = (ImageButton) getView().findViewById(R.id.btnClearClients);
        btnClearDeliveryPlace = (ImageButton) getView().findViewById(R.id.btnClearDeliveryPlace);

        calendar = Calendar.getInstance();

        daily_items = new SpinnerItem[2];
        daily_items[0] = new SpinnerItem(1L, getString(R.string.Route_Daily), "", "");
        daily_items[1] = new SpinnerItem(2L, getString(R.string.Route_Date), "", "");
        adapter_daily = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, daily_items);
        spDaily.setAdapter(adapter_daily);
        // spDaily.setSelection(1);
        // spDaily.setEnabled(false);

        days_items = new SpinnerItem[7];
        days_items[0] = new SpinnerItem(1L, getString(R.string.Monday), "", "");
        days_items[1] = new SpinnerItem(2L, getString(R.string.Tuesday), "", "");
        days_items[2] = new SpinnerItem(3L, getString(R.string.Wednesday), "", "");
        days_items[3] = new SpinnerItem(4L, getString(R.string.Thursday), "", "");
        days_items[4] = new SpinnerItem(5L, getString(R.string.Friday), "", "");
        days_items[5] = new SpinnerItem(6L, getString(R.string.Saturday), "", "");
        days_items[6] = new SpinnerItem(0L, getString(R.string.Sunday), "", "");

        adapter_days = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, days_items);
        spDays.setAdapter(adapter_days);

        week_number_items = new SpinnerItem[5];
        week_number_items[0] = new SpinnerItem(0L, "0", "", "");
        week_number_items[1] = new SpinnerItem(1L, "1", "", "");
        week_number_items[2] = new SpinnerItem(2L, "2", "", "");
        week_number_items[3] = new SpinnerItem(3L, "3", "", "");
        week_number_items[4] = new SpinnerItem(4L, "4", "", "");
        adapter_week_number = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, week_number_items);
        spWeekNumber.setAdapter(adapter_week_number);

        bindListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRoute = ((RouteActivity) getActivity()).mRoute;
        bindData();
    }

    private void bindListeners() {
        try {

            btnClearClients.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    txbClients.setText("");
                    txbClients.setContentDescription("0");
                    txbDeliveryPlaces.setText("");
                    txbDeliveryPlaces.setContentDescription("0");
                }
            });

            btnClearDeliveryPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    txbDeliveryPlaces.setText("");
                    txbDeliveryPlaces.setContentDescription("0");
                }
            });

            mAdapterClients = new AutoCompleteClientsAdapter(getActivity());

            mAdapterClients.setFilterQueryProvider(new FilterQueryProvider() {
                public Cursor runQuery(CharSequence str) {
                    try {
                        Cursor cur = DL_Clients.Get(txbClients.getText().toString());
                        return cur;
                    } catch (Exception ex) {
                    }
                    return null;
                }
            });

            mAdapterClients.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
                public CharSequence convertToString(Cursor cur) {
                    try {
                        int index = cur.getColumnIndex("Name");
                        return cur.getString(index);
                    } catch (Exception ex) {
                    }
                    return "";
                }
            });

            mAdapterDeliveryPlaces = new SimpleCursorAdapter(getActivity(), R.layout.simple_dropdown_item_1line, null, new String[]{"Name"}, new int[]{android.R.id.text1}, 0);

            mAdapterDeliveryPlaces.setFilterQueryProvider(new FilterQueryProvider() {
                public Cursor runQuery(CharSequence str) {
                    try {
                        if (Long.parseLong(txbClients.getContentDescription().toString()) > 0) {
                            Cursor cur = DL_Clients.Get_DeliveryPlacesByClientLongID(Long.parseLong(txbClients.getContentDescription().toString()), txbDeliveryPlaces.getText().toString().replaceAll("^\\s+", ""));
                            return cur;
                        }
                    } catch (Exception ex) {
                    }
                    return null;
                }
            });

            mAdapterDeliveryPlaces.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
                public CharSequence convertToString(Cursor cur) {
                    try {
                        int index = cur.getColumnIndex("Name");
                        return cur.getString(index);
                    } catch (Exception ex) {
                    }
                    return "";
                }
            });

            txbClients.setAdapter(mAdapterClients);
            txbClients.setThreshold(0);

            txbClients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    txbClients.setContentDescription(Long.toString(l));

                    if (Long.parseLong(txbDeliveryPlaces.getContentDescription().toString()) > 0)
                        txbDeliveryPlaces.setText("");
                    txbDeliveryPlaces.setContentDescription("0");

                    Cursor cur = DL_Wurth.GET_Partner(l);

                    if (cur != null) {
                        if (cur.moveToFirst()) {
                            txbDeliveryPlaces.setContentDescription(cur.getString(cur.getColumnIndex("DeliveryPlaceID")));
                        }
                        cur.close();
                    }

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
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(txbDeliveryPlaces.getWindowToken(), 0);
                }
            });

            btnAddClient.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        /*if (txbDeliveryPlaces.getContentDescription() != null && !txbDeliveryPlaces.getContentDescription().toString().equals("0")) {

                            Cursor cur = DL_Clients.Get_DeliveryPlaceByDeliveryPlaceID(Long.parseLong(txbDeliveryPlaces.getContentDescription().toString()));

                            if (cur != null) {

                                if (cur.moveToFirst()) {
                                    JSONObject jsonObj = new JSONObject();
                                    jsonObj.put("ClientID", cur.getLong(cur.getColumnIndex("ClientID")));
                                    jsonObj.put("DeliveryPlaceID", cur.getLong(cur.getColumnIndex("DeliveryPlaceID")));
                                    jsonObj.put("Name", cur.getString(cur.getColumnIndex("Naziv")));
                                    jsonObj.put("Duration", 30);
                                    jsonObj.put("Longitude", cur.getLong(cur.getColumnIndex("Longitude")));
                                    jsonObj.put("Latitude", cur.getLong(cur.getColumnIndex("Latitude")));
                                    jsonObj.put("Note", "");
                                    jsonObj.put("Visit_Type", 1);
                                    data.put(jsonObj);

                                    bindList();

                                    if (jsonObj.getString("Latitude").equals("0") || jsonObj.getString("Longitude").equals("0")) {
                                        Notifications.showNotification(getActivity(), "", getString(R.string.Notification_MissingGeoLocation), 2);
                                    }
                                }
                                cur.close();
                            }
                        }*/

                        if (txbClients.getContentDescription() != null && !txbClients.getContentDescription().toString().equals("0")) {

                            Cursor cur = DL_Wurth.GET_Partner(Long.parseLong(txbClients.getContentDescription().toString()));

                            if (cur != null) {
                                if (cur.moveToFirst()) {
                                    JSONObject jsonObj = new JSONObject();
                                    jsonObj.put("client_id", cur.getLong(cur.getColumnIndex(("ClientID"))));
                                    jsonObj.put("delivery_place_id", cur.getLong(cur.getColumnIndex(("DeliveryPlaceID"))));
                                    jsonObj.put("latitude", cur.getLong(cur.getColumnIndex(("Latitude"))));
                                    jsonObj.put("longitude", cur.getLong(cur.getColumnIndex(("Longitude"))));
                                    jsonObj.put("name", cur.getString(cur.getColumnIndex(("Name"))));
                                    jsonObj.put("start_time", Calendar.getInstance().getTimeInMillis());
                                    jsonObj.put("note", "");
                                    jsonObj.put("duration", 1800);
                                    jsonObj.put("activity_id", 0);
                                    jsonObj.put("target", 0);
                                    jsonObj.put("last_visit_time", 0);

                                    if (jsonObj.getDouble("latitude") == 0D || jsonObj.getDouble("longitude") == 0D) {
                                        Notifications.showNotification(getActivity(), "", getString(R.string.Notification_MissingGeoLocation), 2);
                                    }

                                    if (mRoute != null) {
                                        JSONObject raw = new JSONObject(mRoute.raw);
                                        if (raw.isNull("nodes"))
                                            raw.put("nodes", new JSONArray());
                                        raw.getJSONArray("nodes").put(jsonObj);
                                        mRoute.raw = raw.toString();
                                    }

                                    bindList();
                                    //((MapRouteFragment) ((RouteActivity) getActivity()).mAdapter.getItem(1)).showPins();
                                }
                                cur.close();
                            }
                        }

                        txbClients.setText("");
                        txbClients.setContentDescription("0");
                    } catch (Exception ex) {
                        wurthMB.AddError("RouteInfoFragment", ex.getMessage(), ex);
                    }
                }
            });

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        if (mRoute != null) {

                            JSONObject raw = new JSONObject(mRoute.raw);

                            raw.put("route_type", 2);
                            raw.put("type", ((SpinnerItem) spDaily.getSelectedItem()).getId());
                            raw.put("day", ((SpinnerItem) spDays.getSelectedItem()).getId());
                            raw.put("week_number", ((SpinnerItem) spWeekNumber.getSelectedItem()).getId());
                            raw.put("date", txbDate.getContentDescription());
                            raw.put("locked", false);
                            raw.put("approved", new JSONObject());
                            raw.put("hosts", new JSONArray());

                            JSONArray users = new JSONArray();
                            JSONObject user = new JSONObject();
                            user.put("user_id", wurthMB.getUser().UserID);
                            user.put("name", wurthMB.getUser().Firstname + wurthMB.getUser().Lastname);
                            users.put(user);
                            raw.put("users", users);

                            /*if (mRoute.Date == 0D && mRoute.Day == -1) {
                                Notifications.showNotification(getActivity(), "", getString(R.string.Notification_Date), 2);
                                return;
                            }*/

                            if (mRoute.Name.equals("")) {
                                Notifications.showNotification(getActivity(), "", getString(R.string.Notification_MissingName), 2);
                                return;
                            }

                            if (!raw.has("nodes") || ((JSONArray) raw.get("nodes")).length() == 0) {
                                Notifications.showNotification(getActivity(), "", getString(R.string.Notification_MissingClients), 2);
                                return;
                            }

                            Notifications.showLoading(getActivity());

                            mRoute.raw = raw.toString();
                            mRoute.Sync = 0;
                            mRoute.UserID = wurthMB.getUser().UserID;
                            mRoute.AccountID = wurthMB.getUser().AccountID;

                            if (DL_Routes.AddOrUpdate(mRoute) > 0) {
                                Notifications.hideLoading(getActivity());
                                Notifications.showNotification(getActivity(), "", getString(R.string.Notification_RouteSaved), 0);
                                getActivity().finish();
                            } else {
                                Notifications.hideLoading(getActivity());
                                Notifications.showNotification(getActivity(), "", getString(R.string.SystemError), 1);
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().finish();
                }
            });

            getView().findViewById(R.id.llActions).setVisibility(View.VISIBLE);

        } catch (Exception ex) {
            String temp = ex.getMessage();
        }
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {

        Calendar c = Calendar.getInstance();

        switch (Integer.parseInt(datePickerDialog.getTag())) {
            case 0:
                try {
                    txbDate.setText(day + "." + (month + 1) + "." + year);
                    c.set(year, month, day, 0, 0, 0);
                    txbDate.setContentDescription(Long.toString(c.getTimeInMillis()));
                } catch (Exception e) {
                }
                break;
            default:
                break;
        }
    }

    public void bindData() {
        try {

            txbDate.setText(formatter.format(new Date(System.currentTimeMillis())));
            txbDate.setContentDescription(Long.toString(System.currentTimeMillis()));

            final DatePickerDialog datePickerDialogStartDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
            datePickerDialogStartDate.setYearRange(2010, calendar.get(Calendar.YEAR) + 2);

            txbDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datePickerDialogStartDate.show(getFragmentManager(), "0");
                }
            });

            if (mRoute != null) {
                bindList();
                txbName.setText(mRoute.Name);
                txbDesc.setText(mRoute.Description);

                if (adapter_days != null) {
                    JSONObject jsonObject = new JSONObject(mRoute.raw);
                    if (jsonObject.has("day")) {
                        int i = 0;
                        for (SpinnerItem item : days_items) {
                            if (item.getId() == jsonObject.getInt("day")) {
                                break;
                            }
                            i++;
                        }
                        if (i < days_items.length) spDays.setSelection(i);
                    }
                }

                if (adapter_week_number != null) {
                    JSONObject jsonObject = new JSONObject(mRoute.raw);
                    if (jsonObject.has("week_number")) {
                        int i = 0;
                        for (SpinnerItem item : week_number_items) {
                            if (item.getId() == jsonObject.getInt("week_number")) {
                                break;
                            }
                            i++;
                        }
                        if (i < week_number_items.length) spWeekNumber.setSelection(i);
                    }
                }

                if (adapter_type != null) {
                    JSONObject jsonObject = new JSONObject(mRoute.raw);
                    if (jsonObject.has("type")) {
                        int i = 0;
                        for (SpinnerItem item : type_items) {
                            if (item.getId() == jsonObject.getInt("type")) {
                                break;
                            }
                            i++;
                        }
                        if (i < type_items.length) spType.setSelection(i);
                    }
                }
            }

            txbName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    mRoute.Name = charSequence.toString();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            txbDesc.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    mRoute.Description = charSequence.toString();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            spDaily.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (i == 0) {
                        llDaily.setVisibility(View.VISIBLE);
                        llDate.setVisibility(View.GONE);
                    } else {
                        llDaily.setVisibility(View.GONE);
                        llDate.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        } catch (Exception ex) {
            String temp = ex.getMessage();
        }
    }

    public void bindList() {
        try {
            if (mRoute != null) {

                final JSONObject raw = new JSONObject(mRoute.raw);

                llLocations.removeAllViews();

                for (int i = 0; i < raw.getJSONArray("nodes").length(); i++) {

                    JSONObject jsonObj = raw.getJSONArray("nodes").getJSONObject(i);

                    View v = mInflater.inflate(R.layout.list_row, llLocations, false);
                    ((TextView) v.findViewById(R.id.litTitle)).setText(jsonObj.getString("name"));
                    ((TextView) v.findViewById(R.id.litTotal)).setText(Integer.toString(jsonObj.getInt("duration") / 60) + " min.");
                    ((TextView) v.findViewById(R.id.litSupTitle)).setText(tf.format(new Date(jsonObj.getLong("start_time"))) + " - " + tf.format(new Date(jsonObj.getLong("start_time") + jsonObj.getLong("duration") * 1000)));
                    ((TextView) v.findViewById(R.id.litSubTitle)).setText(jsonObj.getString("note"));
                    ((TextView) v.findViewById(R.id.litStatus)).setText(Integer.toString(i + 1));
                    v.setTag(jsonObj.toString());

                    final int j = i;

                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {

                                final Dialog ItemDialog = new Dialog(getActivity());
                                ItemDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                                ItemDialog.setContentView(R.layout.route_item_dialog);
                                ItemDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                final Spinner spType = (Spinner) ItemDialog.findViewById(R.id.spType);
                                final EditText txbDuration = (EditText) ItemDialog.findViewById(R.id.txbDuration);
                                final EditText txbNote = (EditText) ItemDialog.findViewById(R.id.txbNote);

                                final TextView lit_StartTime = (TextView) ItemDialog.findViewById(R.id.lit_StartTime);
                                final TextView lit_EndTime = (TextView) ItemDialog.findViewById(R.id.lit_EndTime);

                                JSONObject item = new JSONObject(view.getTag().toString());

                                calendar.setTimeInMillis(Long.parseLong(txbDate.getContentDescription().toString()));

                                calendar.set(Calendar.HOUR_OF_DAY, 9);
                                calendar.set(Calendar.MINUTE, 30);
                                lit_StartTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));

                                calendar.set(Calendar.HOUR_OF_DAY, 10);
                                calendar.set(Calendar.MINUTE, 0);
                                lit_EndTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));

                                if(item.has("start_time") && item.has("duration")){
                                    lit_StartTime.setText(tf.format(new Date(item.getLong("start_time"))));
                                    lit_StartTime.setContentDescription(String.valueOf(item.getLong("start_time")));

                                    lit_EndTime.setText(tf.format(new Date(item.getLong("start_time") + item.getLong("duration") * 1000)));
                                    lit_EndTime.setContentDescription(String.valueOf(item.getLong("start_time") + item.getLong("duration") * 1000));
                                }


                                final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(null, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true, false);

                                timePickerDialog.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
                                        try {
                                            Calendar c = Calendar.getInstance();
                                            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                            c.set(Calendar.MINUTE, minute);
                                            c.set(Calendar.SECOND, 0);

                                            switch (Integer.parseInt(CURRENT_TAG)) {
                                                case 1:
                                                    // if (c.getTimeInMillis() > Long.parseLong(lit_EndTime.getContentDescription().toString())) return;
                                                    lit_StartTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                                    lit_StartTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                                    break;
                                                case 2:
                                                    // if (c.getTimeInMillis() < Long.parseLong(lit_StartTime.getContentDescription().toString())) return;
                                                    lit_EndTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                                    lit_EndTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                                    break;

                                                default:
                                                    break;
                                            }

                                        } catch (Exception e) {

                                        }
                                    }
                                });

                                ItemDialog.findViewById(R.id.lit_StartTime).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CURRENT_TAG = STARTTIME_TAG;
                                        Calendar c = Calendar.getInstance();
                                        c.setTimeInMillis(Long.parseLong(lit_StartTime.getContentDescription().toString()));
                                        timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                                        timePickerDialog.show(getFragmentManager(), STARTTIME_TAG);
                                    }
                                });

                                ItemDialog.findViewById(R.id.lit_EndTime).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CURRENT_TAG = ENDTIME_TAG;
                                        Calendar c = Calendar.getInstance();
                                        c.setTimeInMillis(Long.parseLong(lit_EndTime.getContentDescription().toString()));
                                        timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                                        timePickerDialog.show(getFragmentManager(), ENDTIME_TAG);
                                    }
                                });

                                ItemDialog.findViewById(R.id.btnCloseDialog).setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        ItemDialog.dismiss();
                                    }
                                });

                                ItemDialog.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        try {
                                            JSONArray clients = raw.getJSONArray("nodes");
                                            clients = Common.remove(j, clients);
                                            raw.put("nodes", clients);
                                            mRoute.raw = raw.toString();
                                            bindList();
                                            ItemDialog.dismiss();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                ItemDialog.findViewById(R.id.btnUpdateOrder).setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        try {

                                            boolean overlap = false;

                                            Long start = Long.parseLong(lit_StartTime.getContentDescription().toString());
                                            Long end = Long.parseLong(lit_EndTime.getContentDescription().toString());

                                            for (int i = 0; i < raw.getJSONArray("nodes").length(); i++) {

                                                if (i == j) continue;

                                                Long _start = raw.getJSONArray("nodes").getJSONObject(i).getLong("start_time");
                                                Long _end = raw.getJSONArray("nodes").getJSONObject(i).getLong("start_time") + raw.getJSONArray("nodes").getJSONObject(i).getLong("duration");
                                                if (start >= _start && start < _end) {
                                                    overlap = true;
                                                    break;
                                                }
                                                if (end >= _start && end < _end) {
                                                    overlap = true;
                                                    break;
                                                }
                                            }

                                            if (overlap) {
                                                Notifications.showNotification(getActivity(), "", getString(R.string.Notification_VisitOverlap), 2);
                                                return;
                                            }

                                            raw.getJSONArray("nodes").getJSONObject(j).put("note", txbNote.getText());
                                            raw.getJSONArray("nodes").getJSONObject(j).put("start_time", Long.parseLong(lit_StartTime.getContentDescription().toString()));
                                            raw.getJSONArray("nodes").getJSONObject(j).put("duration", (Long.parseLong(lit_EndTime.getContentDescription().toString()) - Long.parseLong(lit_StartTime.getContentDescription().toString())) / 1000);

                                            mRoute.raw = raw.toString();
                                        } catch (JSONException exx) {
                                        }
                                        bindList();
                                        ItemDialog.dismiss();
                                    }
                                });

                           /* SpinnerItem[] visitype_items = new SpinnerItem[3];
                            visitype_items[0] = new SpinnerItem(1L, getString(R.string.Visit_Reason_1), "", "");
                            visitype_items[1] = new SpinnerItem(2L, getString(R.string.Visit_Reason_2), "", "");
                            visitype_items[2] = new SpinnerItem(3L, getString(R.string.Visit_Reason_3), "", "");

                            SpinnerAdapter adapter = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, visitype_items);
                            spType.setAdapter(adapter);

                            try {
                                txbNote.setText(raw.getJSONArray("clients").getJSONObject(j).getString("Note"));
                                txbDuration.setText(Integer.toString(raw.getJSONArray("clients").getJSONObject(j).getInt("stopTime") / 60));
                                spType.setSelection(raw.getJSONArray("clients").getJSONObject(j).getInt("Visit_Type"));

                                if (raw.getJSONArray("clients").getJSONObject(j).getLong("start") > 0) {
                                    lit_StartTime.setContentDescription(raw.getJSONArray("clients").getJSONObject(j).getString("start"));
                                    lit_StartTime.setText(tf.format(new Date(raw.getJSONArray("clients").getJSONObject(j).getLong("start"))));
                                }

                                if (raw.getJSONArray("clients").getJSONObject(j).getLong("end") > 0) {
                                    lit_EndTime.setContentDescription(raw.getJSONArray("clients").getJSONObject(j).getString("end"));
                                    lit_EndTime.setText(tf.format(new Date(raw.getJSONArray("clients").getJSONObject(j).getLong("end"))));
                                }

                            } catch (JSONException exx) {
                            }*/

                                ItemDialog.show();
                            } catch (Exception e) {

                            }
                        }
                    });

                    llLocations.addView(v);
                    ((MapRouteFragment) ((RouteActivity) getActivity()).mAdapter.getItem(1)).showPins();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
