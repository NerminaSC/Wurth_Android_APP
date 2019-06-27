package ba.wurth.mb.Fragments.Activities;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ba.wurth.mb.Activities.Activities.ActivityActivity;
import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.Objects.Record;
import ba.wurth.mb.DataLayer.Activities.DL_Activities;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class ActivityFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private EditText txbNote;

    private TextView litStartDate;
    private TextView litStartTime;
    private TextView litEndDate;
    private TextView litEndTime;


    private Calendar calendar;
    public static final String STARTDATE_TAG = "0";
    public static final String ENDDATE_TAG = "1";
    public static final String STARTTIME_TAG = "2";
    public static final String ENDTIME_TAG = "3";

    private Spinner spType;
    private SpinnerItem[] items_type;

    private SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    private SimpleDateFormat tf = new SimpleDateFormat("HH:mm");

    private Record mRecord;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecord = ((ActivityActivity) getActivity()).mRecord;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_general, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        litStartDate = (TextView) getView().findViewById(R.id.litStartDate);
        litStartTime = (TextView) getView().findViewById(R.id.litStartTime);
        litEndDate = (TextView) getView().findViewById(R.id.litEndDate);
        litEndTime = (TextView) getView().findViewById(R.id.litEndTime);

        spType = (Spinner) getView().findViewById(R.id.spType);

        txbNote = (EditText) getView().findViewById(R.id.txbNote);

        calendar = Calendar.getInstance();

        bindData();
        bindListeners();
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

            calendar.setTimeInMillis(mRecord.startTime);

            final DatePickerDialog datePickerDialogStartDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
            final TimePickerDialog timePickerDialogStartTime = TimePickerDialog.newInstance(null, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true, false);

            calendar.setTimeInMillis(mRecord.endTime);
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

            spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        if (mRecord != null) {
                            mRecord.ItemID = items_type[spType.getSelectedItemPosition()].getId();
                            mRecord.ItemName = items_type[spType.getSelectedItemPosition()].getName();
                        }
                    } catch (Exception exx) {

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            txbNote.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    mRecord.Description = editable.toString();
                }
            });
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

            mRecord.startTime = calendar.getTimeInMillis();

            c.setTimeInMillis(Long.parseLong(litEndDate.getContentDescription().toString()));
            calendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, c.get(Calendar.MONTH));
            calendar.set(Calendar.DATE, c.get(Calendar.DATE));

            c.setTimeInMillis(Long.parseLong(litEndTime.getContentDescription().toString()));
            calendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, c.get(Calendar.SECOND));

            mRecord.endTime = calendar.getTimeInMillis();

        }
        catch (Exception ex) {

        }
    }

    public void bindData() {
        try {

            Cursor cur = DL_Activities.Get("");

            if (cur != null) {
                items_type = new SpinnerItem[cur.getCount()];
                while (cur.moveToNext()) {
                    items_type[cur.getPosition()] = new SpinnerItem(cur.getLong(cur.getColumnIndex("ActivityID")), cur.getString(cur.getColumnIndex("Name")), "", "");
                }
                cur.close();
            }

            SpinnerAdapter adapter_type = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, items_type);
            spType.setAdapter(adapter_type);

            calendar.setTimeInMillis(mRecord.startTime);
            litStartDate.setText(df.format(calendar.getTimeInMillis()));
            litStartDate.setContentDescription(Long.toString(calendar.getTimeInMillis()));
            litStartTime.setText(tf.format(calendar.getTimeInMillis()));
            litStartTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));

            calendar.setTimeInMillis(mRecord.endTime);
            litEndDate.setText(df.format(calendar.getTimeInMillis()));
            litEndDate.setContentDescription(Long.toString(calendar.getTimeInMillis()));
            litEndTime.setText(tf.format(calendar.getTimeInMillis()));
            litEndTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));

        }
        catch (Exception ex) {

        }
    }
}
