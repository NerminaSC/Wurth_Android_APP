package ba.wurth.mb.Fragments.Tasks;

import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.fourmob.datetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

import ba.wurth.mb.Adapters.AutoCompleteClientsAdapter;
import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Temp_Acquisition;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Temp.DL_Temp;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class TaskCreateFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private Spinner spGroups;
    private Spinner spUsers;
    private SpinnerItem[] items_users;
    private SpinnerItem[] items_groups;
    private Button btnUpdate;
    private Button btnCancel;

    public AutoCompleteTextView txbClients;
    public AutoCompleteTextView txbDeliveryPlaces;

    private ImageButton btnClearClients;
    private ImageButton btnClearDeliveryPlace;

    private SimpleCursorAdapter mAdapterClients;
    private SimpleCursorAdapter mAdapterDeliveryPlaces;

    private Button btnAddGroup;
    private Button btnAddUser;
    private Button btnAddClient;
    private Button btnAddDeliveryPlace;

    private TextView litStartDate;
    private TextView litEndDate;
    private EditText txbName;
    private EditText txbDescription;

    private LinearLayout llGroups;
    private LinearLayout llUsers;
    private LinearLayout llClients;
    private LinearLayout llDeliveryPlaces;

    private JSONArray Clients = new JSONArray();
    private JSONArray DeliveryPlaces = new JSONArray();
    private JSONArray Users = new JSONArray();
    private JSONArray Groups = new JSONArray();

    private Calendar calendar;
    public static final String STARTDATE_TAG = "1";
    public static final String ENDDATE_TAG = "2";

    private LayoutInflater mInflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.task_create, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnUpdate = (Button) getView().findViewById(R.id.btnUpdate);
        btnCancel = (Button) getView().findViewById(R.id.btnCancel);
        btnAddGroup = (Button) getView().findViewById(R.id.btnAddGroup);
        btnAddUser = (Button) getView().findViewById(R.id.btnAddUser);
        btnAddClient = (Button) getView().findViewById(R.id.btnAddClient);
        btnAddDeliveryPlace = (Button) getView().findViewById(R.id.btnAddDeliveryPlace);

        llGroups = (LinearLayout) getView().findViewById(R.id.llGroups);
        llUsers = (LinearLayout) getView().findViewById(R.id.llUsers);
        llClients = (LinearLayout) getView().findViewById(R.id.llClients);
        llDeliveryPlaces = (LinearLayout) getView().findViewById(R.id.llDeliveryPlaces);

        spGroups = (Spinner) getView().findViewById(R.id.spGroups);
        spUsers = (Spinner) getView().findViewById(R.id.spUsers);

        litStartDate = (TextView) getView().findViewById(R.id.litStartDate);
        litEndDate = (TextView) getView().findViewById(R.id.litEndDate);

        txbName = (EditText) getView().findViewById(R.id.txbName);
        txbDescription = (EditText) getView().findViewById(R.id.txbDescription);

        txbClients = (AutoCompleteTextView) getView().findViewById(R.id.txbClients);
        txbDeliveryPlaces = (AutoCompleteTextView) getView().findViewById(R.id.txbDeliveryPlaces);

        btnClearClients = (ImageButton) getView().findViewById(R.id.btnClearClients);
        btnClearDeliveryPlace = (ImageButton) getView().findViewById(R.id.btnClearDeliveryPlace);

        calendar = Calendar.getInstance();

        mInflater = LayoutInflater.from(getActivity());

        bindData();
        bindListeners();
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {

        Calendar c = Calendar.getInstance();

        switch (Integer.parseInt(datePickerDialog.getTag())) {
            case 1:
                litStartDate.setText(day + "." + (month + 1) + "." + year);
                c.set(year, month, day, 0, 0, 0);
                litStartDate.setContentDescription(Long.toString(c.getTimeInMillis()));
                break;
            case 2:
                litEndDate.setText(day + "." + (month + 1) + "." + year);
                c.set(year, month, day, 0, 0, 0);
                litEndDate.setContentDescription(Long.toString(c.getTimeInMillis()));
                break;
            default:
                break;
        }
    }

    private void bindListeners() {
        try {

            final DatePickerDialog datePickerDialogDeliveryDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
            datePickerDialogDeliveryDate.setYearRange(2010, calendar.get(Calendar.YEAR) + 2);

            litStartDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datePickerDialogDeliveryDate.show(getFragmentManager(), STARTDATE_TAG);
                }
            });

            litEndDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datePickerDialogDeliveryDate.show(getFragmentManager(), ENDDATE_TAG);
                }
            });

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

            btnAddUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {

                        boolean exists = false;

                        for (int i =0;i< Users.length();i++) {
                            JSONObject item = Users.getJSONObject(i);
                            if (item.getLong("UserID") == items_users[spUsers.getSelectedItemPosition()].getId()) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("UserID", items_users[spUsers.getSelectedItemPosition()].getId());
                            jsonObject.put("Name", items_users[spUsers.getSelectedItemPosition()].getName());
                            Users.put(jsonObject);
                            bindList();
                        }
                    }
                    catch (Exception ex) { }
                }
            });

            btnAddGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {

                        boolean exists = false;

                        for (int i =0;i< Groups.length();i++) {
                            JSONObject item = Groups.getJSONObject(i);
                            if (item.getLong("GroupID") == items_groups[spGroups.getSelectedItemPosition()].getId()) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("UserID", items_groups[spGroups.getSelectedItemPosition()].getId());
                            jsonObject.put("Name", items_groups[spGroups.getSelectedItemPosition()].getName());
                            Groups.put(jsonObject);
                            bindList();
                        }
                    }
                    catch (Exception ex) { }
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

                        if ((txbDeliveryPlaces.getContentDescription() == null || txbDeliveryPlaces.getContentDescription().toString().equals("0")) && txbClients.getContentDescription() != null && !txbClients.getContentDescription().toString().equals("0")) {

                            boolean exists = false;

                            for (int i =0;i < Clients.length();i++) {
                                JSONObject item = Groups.getJSONObject(i);
                                if (item.getLong("GroupID") == Long.parseLong(txbClients.getContentDescription().toString())) {
                                    exists = true;
                                    break;
                                }
                            }

                            if (!exists) {
                                Cursor cur = DL_Wurth.GET_Partner(Long.parseLong(txbClients.getContentDescription().toString()));

                                if (cur != null) {
                                    if (cur.moveToFirst()) {
                                        JSONObject jsonObj = new JSONObject();
                                        jsonObj.put("ClientID", cur.getLong(cur.getColumnIndex(("ClientID"))));
                                        jsonObj.put("Name", cur.getString(cur.getColumnIndex(("Name"))));
                                        Clients.put(jsonObj);

                                        txbClients.setText("");
                                        txbClients.setContentDescription("0");

                                        bindList();
                                    }
                                    cur.close();
                                }
                            }
                        }
                    }
                    catch (Exception ex) {
                        wurthMB.AddError("Create Task", ex.getMessage(), ex);
                    }
                }
            });

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        JSONObject mTemp = new JSONObject();

                        mTemp.put("Name", txbName.getText());
                        mTemp.put("Description", txbDescription.getText());
                        mTemp.put("StatusID", 2);
                        mTemp.put("AccountID", wurthMB.getUser().AccountID);
                        mTemp.put("ModifiedBy", wurthMB.getUser().UserID);
                        mTemp.put("DOE", System.currentTimeMillis());

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("Mandatory", false);
                        jsonObject.put("CreatedBy", wurthMB.getUser().UserID);
                        jsonObject.put("startDate", Long.parseLong(litStartDate.getContentDescription().toString()));
                        jsonObject.put("endDate", Long.parseLong(litEndDate.getContentDescription().toString()));
                        jsonObject.put("Groups", Groups);
                        jsonObject.put("Users", Users);
                        jsonObject.put("Clients", Clients);
                        jsonObject.put("DeliveryPlaces", DeliveryPlaces);

                        mTemp.put("Parameters", jsonObject);

                        Temp_Acquisition temp = new Temp_Acquisition();

                        temp.AccountID = wurthMB.getUser().AccountID;
                        temp.UserID = wurthMB.getUser().UserID;
                        temp.OptionID = 21;
                        temp.ID = 0;
                        temp.Sync = 0;
                        temp.DOE = System.currentTimeMillis();
                        temp.jsonObj = mTemp.toString();

                        if (DL_Temp.AddOrUpdate(temp) > 0 ) {
                            Notifications.showNotification(getActivity(), "", getString(R.string.Notification_Saved), 0);
                            getActivity().finish();
                        }
                        else {
                            Notifications.hideLoading(getActivity());
                            Notifications.showNotification(getActivity(), "", getString(R.string.SystemError), 1);
                        }
                    }
                    catch (Exception ex) {

                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().finish();
                }
            });

        }
        catch (Exception ex) {

        }
    }

    private void bindData () {
        try {

            Cursor cur = DL_Wurth.GET_Users_Associated();

            if (cur != null) {
                items_users = new SpinnerItem[cur.getCount()];
                while (cur.moveToNext()) {
                    items_users[cur.getPosition()] = new SpinnerItem(cur.getLong(cur.getColumnIndex("UserID")), cur.getString(cur.getColumnIndex("Firstname")) + " " + cur.getString(cur.getColumnIndex("Lastname")) + " (" + cur.getString(cur.getColumnIndex("code")) + ")", "", "");
                }
                cur.close();
            }

            SpinnerAdapter adapter_users = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, items_users);
            spUsers.setAdapter(adapter_users);


            items_groups = new SpinnerItem[0];
            SpinnerAdapter adapter_groups = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, items_groups);
            spGroups.setAdapter(adapter_groups);
        }
        catch (Exception ex) {
            wurthMB.AddError("Create Task", ex.getMessage(), ex);
        }
    }

    public void bindList() {
        try {
            if (Users != null) {
                llUsers.removeAllViews();
                for (int i = 0; i < Users.length(); i++) {
                    JSONObject jsonObj = Users.getJSONObject(i);
                    View v = mInflater.inflate(R.layout.list_row, llUsers, false);
                    ((TextView) v.findViewById(R.id.litTitle)).setText(jsonObj.getString("Name"));
                    ((TextView) v.findViewById(R.id.litStatus)).setText(Integer.toString(i+1));
                    final int j = i;
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            Users = Common.remove(j, Users);
                            bindList();
                            return false;
                        }
                    });
                    llUsers.addView(v);
                }
            }

            if (Groups != null) {
                llGroups.removeAllViews();
                for (int i = 0; i < Groups.length(); i++) {
                    JSONObject jsonObj = Groups.getJSONObject(i);
                    View v = mInflater.inflate(R.layout.list_row, llGroups, false);
                    ((TextView) v.findViewById(R.id.litTitle)).setText(jsonObj.getString("Name"));
                    ((TextView) v.findViewById(R.id.litStatus)).setText(Integer.toString(i+1));
                    final int j = i;
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            Groups = Common.remove(j, Groups);
                            bindList();
                            return false;
                        }
                    });
                    llGroups.addView(v);
                }
            }

            if (Clients != null) {
                llClients.removeAllViews();
                for (int i = 0; i < Clients.length(); i++) {
                    JSONObject jsonObj = Clients.getJSONObject(i);
                    View v = mInflater.inflate(R.layout.list_row, llClients, false);
                    ((TextView) v.findViewById(R.id.litTitle)).setText(jsonObj.getString("Name"));
                    ((TextView) v.findViewById(R.id.litStatus)).setText(Integer.toString(i+1));
                    final int j = i;
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            Clients = Common.remove(j, Clients);
                            bindList();
                            return false;
                        }
                    });
                    llClients.addView(v);
                }
            }            
        }
        catch (Exception ex) {

        }

    }

}
