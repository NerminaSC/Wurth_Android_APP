package ba.wurth.mb.Fragments.Visits;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ba.wurth.mb.Activities.Visits.VisitActivity;
import ba.wurth.mb.Adapters.AutoCompleteClientsAdapter;
import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Visit;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class VisitFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private EditText txbNote;

    private TextView litStartDate;
    private TextView litStartTime;
    private TextView litEndDate;
    private TextView litEndTime;

    private Spinner spType;
    private Spinner spAction;
    private Spinner spReason;

    private Calendar calendar;
    public static final String STARTDATE_TAG = "0";
    public static final String ENDDATE_TAG = "1";
    public static final String STARTTIME_TAG = "2";
    public static final String ENDTIME_TAG = "3";

    private SimpleCursorAdapter mAdapterClients;
    private SimpleCursorAdapter mAdapterDeliveryPlaces;

    private AutoCompleteTextView txbClients;
    private AutoCompleteTextView txbDeliveryPlaces;

    private ImageButton btnClearClients;
    private ImageButton btnClearDeliveryPlace;

    private SpinnerAdapter adapter_reason;
    private SpinnerItem[] visitreason_items;

    private SpinnerAdapter adapter;
    private SpinnerItem[] visittype_items;

    private SpinnerAdapter adapter_action;
    private SpinnerItem[] visitaction_items;

    private Spinner spUsers;
    private SpinnerItem[] items_users;

    private Long _OrderID = 0L;

    private SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    private SimpleDateFormat tf = new SimpleDateFormat("HH:mm");

    private Visit mVisit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVisit = ((VisitActivity) getActivity()).mVisit;

        if (getArguments() != null && getArguments().getLong("_OrderID", 0L) > 0L) {
            _OrderID = getArguments().getLong("_OrderID");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.visit_general, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        litStartDate = (TextView) getView().findViewById(R.id.litStartDate);
        litStartTime = (TextView) getView().findViewById(R.id.litStartTime);
        litEndDate = (TextView) getView().findViewById(R.id.litEndDate);
        litEndTime = (TextView) getView().findViewById(R.id.litEndTime);

        spType = (Spinner) getView().findViewById(R.id.spType);
        spAction = (Spinner) getView().findViewById(R.id.spAction);
        spReason = (Spinner) getView().findViewById(R.id.spReason);

        txbNote = (EditText) getView().findViewById(R.id.txbNote);

        calendar = Calendar.getInstance();

        txbClients = (AutoCompleteTextView) getView().findViewById(R.id.txbClients);
        txbDeliveryPlaces = (AutoCompleteTextView) getView().findViewById(R.id.txbDeliveryPlaces);

        btnClearClients = (ImageButton) getView().findViewById(R.id.btnClearClients);
        btnClearDeliveryPlace = (ImageButton) getView().findViewById(R.id.btnClearDeliveryPlace);

        spUsers = (Spinner) getView().findViewById(R.id.spUsers);

        bindData();

        if (mVisit != null && mVisit.Sync == 0) bindListeners();
        if (mVisit != null && mVisit.Sync == 1) {
            spUsers.setEnabled(false);
            spAction.setEnabled(false);
            spReason.setEnabled(false);
            spType.setEnabled(false);
        }
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
                litStartDate.setText(day + "." + (month + 1) + "." + year);
                c.set(year, month, day, 0, 0, 0);
                litStartDate.setContentDescription(Long.toString(c.getTimeInMillis()));
                bindTime();
                break;
            case 1:
                litEndDate.setText(day + "." + (month + 1) + "." + year);
                c.set(year, month, day, 0, 0, 0);
                litEndDate.setContentDescription(Long.toString(c.getTimeInMillis()));
                bindTime();
                break;
            default:
                break;
        }
    }

    private void bindListeners() {
        try {

            calendar.setTimeInMillis(mVisit.startDT);

            final DatePickerDialog datePickerDialogStartDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
            final TimePickerDialog timePickerDialogStartTime = TimePickerDialog.newInstance(null, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true, false);

            calendar.setTimeInMillis(mVisit.endDT);
            final DatePickerDialog datePickerDialogEndDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
            final TimePickerDialog timePickerDialogEndTime = TimePickerDialog.newInstance(null, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true, false);

            datePickerDialogStartDate.setYearRange(2010, calendar.get(Calendar.YEAR) + 2);
            datePickerDialogEndDate.setYearRange(2010, calendar.get(Calendar.YEAR) + 2);

            timePickerDialogStartTime.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    c.set(Calendar.MINUTE, minute);
                    c.set(Calendar.SECOND, 0);
                    litStartTime.setText(tf.format(c.getTimeInMillis()));
                    litStartTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                    timePickerDialogStartTime.setStartTime(hourOfDay, minute);
                    bindTime();
                }
            });

            timePickerDialogEndTime.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    c.set(Calendar.MINUTE, minute);
                    c.set(Calendar.SECOND, 0);
                    litEndTime.setText(tf.format(c.getTimeInMillis()));
                    litEndTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                    timePickerDialogEndTime.setStartTime(hourOfDay, minute);
                    bindTime();
                }
            });

            litStartDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datePickerDialogStartDate.show(getFragmentManager(), STARTDATE_TAG);
                }
            });

            litEndDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datePickerDialogEndDate.show(getFragmentManager(), ENDDATE_TAG);
                }
            });

            litStartTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timePickerDialogStartTime.show(getFragmentManager(), STARTTIME_TAG);
                }
            });

            litEndTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timePickerDialogEndTime.show(getFragmentManager(), ENDTIME_TAG);
                }
            });


            if (_OrderID == 0L) {
                btnClearClients.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        txbClients.setText("");
                        txbClients.setContentDescription("0");
                        txbDeliveryPlaces.setText("");
                        txbDeliveryPlaces.setContentDescription("0");
                        mVisit.ClientID = 0L;
                        mVisit.DeliveryPlaceID = 0L;
                    }
                });

                btnClearDeliveryPlace.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        txbDeliveryPlaces.setText("");
                        txbDeliveryPlaces.setContentDescription("0");
                        mVisit.DeliveryPlaceID = 0L;
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
                        int index = cur.getColumnIndex("Name");
                        return cur.getString(index);
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
                        int index = cur.getColumnIndex("Name");
                        return cur.getString(index);
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

                        if (cur != null) {
                            if (cur.moveToFirst()) {
                                mVisit.ClientID = cur.getLong(cur.getColumnIndex("ClientID"));
                                mVisit.DeliveryPlaceID = cur.getLong(cur.getColumnIndex("DeliveryPlaceID"));
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
                        mVisit.DeliveryPlaceID = DL_Clients.Get_DeliveryPlaceID(l);

                        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(txbDeliveryPlaces.getWindowToken(), 0);
                    }
                });
            }

            spReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        if (mVisit != null) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("Note", txbNote.getText().toString());
                            jsonObject.put("TypeID", visittype_items[spType.getSelectedItemPosition()].getId());
                            jsonObject.put("ActionID", visitaction_items[spAction.getSelectedItemPosition()].getId());
                            jsonObject.put("ReasonID", visitreason_items[spReason.getSelectedItemPosition()].getId());

                            mVisit.Note = jsonObject.toString();
                        }
                    }
                    catch (Exception exx) {

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        if (mVisit != null) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("Note", txbNote.getText().toString());
                            jsonObject.put("TypeID", visittype_items[spType.getSelectedItemPosition()].getId());
                            jsonObject.put("ActionID", visitaction_items[spAction.getSelectedItemPosition()].getId());
                            jsonObject.put("ReasonID", visitreason_items[spReason.getSelectedItemPosition()].getId());

                            mVisit.Note = jsonObject.toString();
                        }
                    }
                    catch (Exception exx) {

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        if (mVisit != null) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("Note", txbNote.getText().toString());
                            jsonObject.put("TypeID", visittype_items[spType.getSelectedItemPosition()].getId());
                            jsonObject.put("ActionID", visitaction_items[spAction.getSelectedItemPosition()].getId());
                            jsonObject.put("ReasonID", visitreason_items[spReason.getSelectedItemPosition()].getId());

                            mVisit.Note = jsonObject.toString();
                        }
                    }
                    catch (Exception exx) {

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        if (mVisit != null) {
                            mVisit.UserID = items_users[spUsers.getSelectedItemPosition()].getId();
                        }
                    }
                    catch (Exception exx) {

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            txbNote.addTextChangedListener(new textWatcher());
        }
        catch (Exception ex) {

        }
    }

    private void bindTime() {
        try {

            Calendar calendar = Calendar.getInstance();
            Calendar c = Calendar.getInstance();

            c.setTimeInMillis(Long.parseLong(litStartDate.getContentDescription().toString()));
            calendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, c.get(Calendar.MONTH));
            calendar.set(Calendar.DATE, c.get(Calendar.DATE));

            c.setTimeInMillis(Long.parseLong(litStartTime.getContentDescription().toString()));
            calendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, c.get(Calendar.SECOND));

            mVisit.startDT = calendar.getTimeInMillis();


            c.setTimeInMillis(Long.parseLong(litEndDate.getContentDescription().toString()));
            calendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, c.get(Calendar.MONTH));
            calendar.set(Calendar.DATE, c.get(Calendar.DATE));

            c.setTimeInMillis(Long.parseLong(litEndTime.getContentDescription().toString()));
            calendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, c.get(Calendar.SECOND));

            mVisit.endDT = calendar.getTimeInMillis();

        }
        catch (Exception ex) {

        }
    }

    public void bindData() {
        try {

            Cursor cur = DL_Wurth.GET_Users_Associated();

            int position = 0;

            if (cur != null) {
                items_users = new SpinnerItem[cur.getCount()];
                while (cur.moveToNext()) {
                    items_users[cur.getPosition()] = new SpinnerItem(cur.getLong(cur.getColumnIndex("UserID")), cur.getString(cur.getColumnIndex("Firstname")) + " " + cur.getString(cur.getColumnIndex("Lastname")) + " (" + cur.getString(cur.getColumnIndex("code")) + ")", "", "");
                    if (cur.getLong(cur.getColumnIndex("UserID")) == wurthMB.getUser().UserID) position = cur.getPosition();
                }
                cur.close();
            }

            SpinnerAdapter adapter_users = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, items_users);
            spUsers.setAdapter(adapter_users);
            spUsers.setSelection(position);

            visitreason_items = new SpinnerItem[3];
            visitreason_items[0] = new SpinnerItem(1L, getString(R.string.Visit_Reason_1), "", "");
            visitreason_items[1] = new SpinnerItem(2L, getString(R.string.Visit_Reason_2), "", "");
            visitreason_items[2] = new SpinnerItem(3L, getString(R.string.Visit_Reason_3), "", "");

            visittype_items = new SpinnerItem[4];
            visittype_items[0] = new SpinnerItem(1L, getString(R.string.Visit_Type_1), "", "");
            visittype_items[1] = new SpinnerItem(2L, getString(R.string.Visit_Type_2), "", "");
            visittype_items[2] = new SpinnerItem(3L, getString(R.string.Visit_Type_3), "", "");
            visittype_items[3] = new SpinnerItem(4L, getString(R.string.Visit_Type_4), "", "");

            visitaction_items = new SpinnerItem[4];
            visitaction_items[0] = new SpinnerItem(1L, getString(R.string.Visit_Visit), "", "");
            visitaction_items[1] = new SpinnerItem(2L, getString(R.string.Visit_OrderCreated), "", "");
            visitaction_items[2] = new SpinnerItem(3L, getString(R.string.Visit_ProposalAction), "", "");
            visitaction_items[3] = new SpinnerItem(4L, getString(R.string.Visit_Payment), "", "");

            adapter = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, visittype_items);
            spType.setAdapter(adapter);

            adapter_action = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, visitaction_items);
            spAction.setAdapter(adapter_action);

            adapter_reason = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, visitreason_items);
            spReason.setAdapter(adapter_reason);

            if (mVisit != null) {

                if (mVisit.ClientID > 0L) {
                    Client c = DL_Clients.GetByClientID(mVisit.ClientID);
                    if (c != null) {
                        txbClients.setText(c.Name);
                        txbClients.setContentDescription(Long.toString(c._id));
                    }
                }
                else {
                    txbClients.requestFocus();
                    InputMethodManager imm =(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }

                if (mVisit.DeliveryPlaceID > 0L) {
                    Cursor c = DL_Clients.Get_DeliveryPlaceByDeliveryPlaceID(mVisit.DeliveryPlaceID);
                    if (c != null && c.getCount() > 0 && c.moveToFirst()) {
                        txbDeliveryPlaces.setText(c.getString(c.getColumnIndex("Naziv")));
                        txbDeliveryPlaces.setContentDescription(c.getString(c.getColumnIndex("DeliveryPlaceID")));
                        c.close();
                    }
                }

                try {

                    int j = 0;
                    for (SpinnerItem item : items_users) {
                        if (item.getId() == mVisit.UserID) {
                            break;
                        }
                        j++;
                    }
                    if (j < items_users.length) spUsers.setSelection(j);

                    JSONObject jsonObject = new JSONObject(mVisit.Note);
                    txbNote.setText(jsonObject.getString("Note"));

                    if (jsonObject.has("TypeID")) {
                        int i = 0;
                        for (SpinnerItem item : visittype_items) {
                            if (item.getId() == jsonObject.getInt("TypeID")) {
                                break;
                            }
                            i++;
                        }
                        if (i < visittype_items.length) spType.setSelection(i);
                    }

                    if (jsonObject.has("ActionID")) {
                        int i = 0;
                        for (SpinnerItem item : visitaction_items) {
                            if (item.getId() == jsonObject.getInt("ActionID")) {
                                break;
                            }
                            i++;
                        }
                        if (i < visitaction_items.length) spAction.setSelection(i);
                    }

                    if (jsonObject.has("ReasonID")) {
                        int i = 0;
                        for (SpinnerItem item : visitreason_items) {
                            if (item.getId() == jsonObject.getInt("ReasonID")) {
                                break;
                            }
                            i++;
                        }
                        if (i < visitreason_items.length) spReason.setSelection(i);
                    }

                }
                catch (Exception exx) {

                }

                calendar.setTimeInMillis(mVisit.startDT);
                litStartDate.setText(df.format(calendar.getTimeInMillis()));
                litStartDate.setContentDescription(Long.toString(calendar.getTimeInMillis()));
                litStartTime.setText(tf.format(calendar.getTimeInMillis()));
                litStartTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));

                calendar.setTimeInMillis(mVisit.endDT);
                litEndDate.setText(df.format(calendar.getTimeInMillis()));
                litEndDate.setContentDescription(Long.toString(calendar.getTimeInMillis()));
                litEndTime.setText(tf.format(calendar.getTimeInMillis()));
                litEndTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
            }

            if (getArguments() != null && getArguments().getLong("ClientID", 0L) > 0 ) {
                Client c = DL_Clients.GetByClientID(getArguments().getLong("ClientID"));
                if (c != null) {
                    txbClients.setText(c.Name);
                    txbClients.setContentDescription(Long.toString(c._id));
                    mVisit.ClientID = DL_Clients.Get_ClientID(c._id);
                    txbDeliveryPlaces.requestFocus();
                }
            }

            if (getArguments() != null && getArguments().getLong("DeliveryPlaceID", 0L) > 0 ) {
                Cursor _cur = DL_Clients.Get_DeliveryPlaceByDeliveryPlaceID(getArguments().getLong("DeliveryPlaceID"));
                if( _cur != null) {
                    if (_cur.getCount() > 0 && _cur.moveToFirst()) {
                        txbDeliveryPlaces.setText(_cur.getString(_cur.getColumnIndex("Name")));
                        txbDeliveryPlaces.setContentDescription(_cur.getString(_cur.getColumnIndex("_id")));
                        mVisit.DeliveryPlaceID = _cur.getLong(_cur.getColumnIndex("DeliveryPlaceID"));
                    }
                    _cur.close();
                }
            }

            if (mVisit != null && mVisit.ClientID == 0 && mVisit.DeliveryPlaceID == 0 && wurthMB.currentBestLocation != null) {

                Cursor _cur = DL_Clients.Get_ClientClosest((long) (wurthMB.currentBestLocation.getLatitude() * 10000000D), (long) (wurthMB.currentBestLocation.getLongitude() * 10000000D));

                if (_cur != null) {

                    if (_cur.moveToFirst()) {

                        Double Distance = Common.distFrom(wurthMB.currentBestLocation.getLatitude(), wurthMB.currentBestLocation.getLongitude(), _cur.getDouble(_cur.getColumnIndex("Latitude")) / 10000000D, _cur.getDouble(_cur.getColumnIndex("Longitude")) / 10000000D);

                        if (Distance > 100) return;
                        txbClients.setText(_cur.getString(_cur.getColumnIndex("Naziv")));
                        txbClients.setContentDescription(_cur.getString(_cur.getColumnIndex("_clientid")));
                        mVisit.ClientID = _cur.getLong(_cur.getColumnIndex("ClientID"));
                    }
                    _cur.close();
                }
            }
            
        }
        catch (Exception ex) {

        }
    }

    private class textWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            try {
                if (mVisit != null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Note", s.toString());
                    jsonObject.put("TypeID", visittype_items[spType.getSelectedItemPosition()].getId());
                    jsonObject.put("ActionID", visitaction_items[spAction.getSelectedItemPosition()].getId());
                    jsonObject.put("ReasonID", visitreason_items[spReason.getSelectedItemPosition()].getId());

                    mVisit.Note = jsonObject.toString();
                }
            }
            catch (Exception ex) {
            }
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
