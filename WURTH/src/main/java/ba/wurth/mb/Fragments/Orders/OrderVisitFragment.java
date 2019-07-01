package ba.wurth.mb.Fragments.Orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.R;

public class OrderVisitFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.order_visit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        litStartDate = (TextView) getView().findViewById(R.id.litStartDate);
        litStartTime = (TextView) getView().findViewById(R.id.litStartTime);
        litEndDate = (TextView) getView().findViewById(R.id.litEndDate);
        litEndTime = (TextView) getView().findViewById(R.id.litEndTime);

        txbNote = (EditText) getView().findViewById(R.id.txbNote);

        calendar = Calendar.getInstance();

        bindData();
        if (wurthMB.getOrder().Sync == 0 ) bindListeners();
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
                if (wurthMB.getOrder().visit != null) wurthMB.getOrder().visit.startDT = Long.parseLong(litStartTime.getContentDescription().toString()) +  c.getTimeInMillis();
                break;
            case 1:
                litEndDate.setText(day + "." + (month + 1) + "." + year);
                c.set(year, month, day, 0, 0, 0);
                litEndDate.setContentDescription(Long.toString(c.getTimeInMillis()));
                if (wurthMB.getOrder().visit != null) wurthMB.getOrder().visit.endDT = Long.parseLong(litEndTime.getContentDescription().toString()) +  c.getTimeInMillis();
                break;
            default:
                break;
        }
    }


    private void bindListeners() {
        try {

            if (wurthMB.getOrder().visit != null) calendar.setTimeInMillis(wurthMB.getOrder().visit.startDT);
            final DatePickerDialog datePickerDialogStartDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
            final TimePickerDialog timePickerDialogStartTime = TimePickerDialog.newInstance(null, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), true, false);

            if (wurthMB.getOrder().visit != null) calendar.setTimeInMillis(wurthMB.getOrder().visit.endDT);
            final DatePickerDialog datePickerDialogEndDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
            final TimePickerDialog timePickerDialogEndTime = TimePickerDialog.newInstance(null, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), true, false);

            timePickerDialogStartTime.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
                    Calendar c = Calendar.getInstance();
                    litStartTime.setText(hourOfDay + ":" + minute);
                    c.set(0, 0, 0, hourOfDay, minute, 0);
                    litStartTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                    if (wurthMB.getOrder().visit != null) wurthMB.getOrder().visit.startDT = Long.parseLong(litStartDate.getContentDescription().toString()) +  c.getTimeInMillis();
                }
            });

            timePickerDialogEndTime.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
                    Calendar c = Calendar.getInstance();
                    litEndTime.setText(hourOfDay + ":" + minute);
                    c.set(0, 0, 0, hourOfDay, minute, 0);
                    litEndTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                    if (wurthMB.getOrder().visit != null) wurthMB.getOrder().visit.startDT = Long.parseLong(litEndDate.getContentDescription().toString()) +  c.getTimeInMillis();
                }
            });


            litStartDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datePickerDialogStartDate.setYearRange(2010, calendar.get(Calendar.YEAR) + 2);
                    datePickerDialogStartDate.show(getFragmentManager(), STARTDATE_TAG);
                }
            });

            litEndDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datePickerDialogEndDate.setYearRange(2010, calendar.get(Calendar.YEAR) + 2);
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
        }
        catch (Exception ex) {

        }
    }

    public void bindData() {
        try {

            if (wurthMB.getOrder().visit != null) {

                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                SimpleDateFormat tf = new SimpleDateFormat("HH:mm");

                txbNote.setText(wurthMB.getOrder().visit.Note);

                calendar.setTimeInMillis(wurthMB.getOrder().visit.startDT);
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                litStartDate.setText(df.format(calendar.getTimeInMillis()));
                litStartDate.setContentDescription(Long.toString(calendar.getTimeInMillis()));

                calendar.setTimeInMillis(wurthMB.getOrder().visit.startDT);
                calendar.set(Calendar.YEAR, 0);
                calendar.set(Calendar.MONDAY, 0);
                calendar.set(Calendar.DATE, 0);
                litStartTime.setText(tf.format(calendar.getTimeInMillis()));
                litStartTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));

                calendar.setTimeInMillis(wurthMB.getOrder().visit.endDT);
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                litEndDate.setText(df.format(calendar.getTimeInMillis()));
                litEndDate.setContentDescription(Long.toString(calendar.getTimeInMillis()));

                calendar.setTimeInMillis(wurthMB.getOrder().visit.endDT);
                calendar.set(Calendar.YEAR, 0);
                calendar.set(Calendar.MONDAY, 0);
                calendar.set(Calendar.DATE, 0);
                litEndTime.setText(tf.format(calendar.getTimeInMillis()));
                litEndTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
            }
        }
        catch (Exception ex) {

        }
    }
}
