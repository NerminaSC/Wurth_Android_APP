package ba.wurth.mb.Fragments.Clients.Add;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ba.wurth.mb.Activities.Clients.ClientAddActivity;
import ba.wurth.mb.Classes.TypefacedTextView;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.R;

public class ClientAddWorkingTimeFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    public JSONObject mTemp;
    private Calendar calendar;

    private TextView litMonday_StartTime;
    private TextView litMonday_EndTime;

    private TextView litTuesday_StartTime;
    private TextView litTuesday_EndTime;

    private TextView litWednesday_StartTime;
    private TextView litWednesday_EndTime;

    private TextView litThursday_StartTime;
    private TextView litThursday_EndTime;

    private TextView litFriday_StartTime;
    private TextView litFriday_EndTime;

    private TextView litSaturday_StartTime;
    private TextView litSaturday_EndTime;

    private TextView litSunday_StartTime;
    private TextView litSunday_EndTime;

    private TextView litDate;

    private ImageButton btnAddNonWorkingDays;

    public static final String STARTTIME_TAG_MONDAY = "1";
    public static final String ENDTIME_TAG_MONDAY = "2";

    public static final String STARTTIME_TAG_TUESDAY = "3";
    public static final String ENDTIME_TAG_TUESDAY = "4";

    public static final String STARTTIME_TAG_WEDNESDAY = "5";
    public static final String ENDTIME_TAG_WEDNESDAY = "6";

    public static final String STARTTIME_TAG_THURSDAY = "7";
    public static final String ENDTIME_TAG_THURSDAY = "8";

    public static final String STARTTIME_TAG_FRIDAY = "9";
    public static final String ENDTIME_TAG_FRIDAY = "10";

    public static final String STARTTIME_TAG_SATURDAY = "11";
    public static final String ENDTIME_TAG_SATURDAY = "12";

    public static final String STARTTIME_TAG_SUNDAY = "13";
    public static final String ENDTIME_TAG_SUNDAY = "14";

    private String CURRENT_TAG = "0";

    private SimpleDateFormat df = new SimpleDateFormat("dd.MM");
    private SimpleDateFormat tf = new SimpleDateFormat("HH:mm");


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTemp = ((ClientAddActivity) getActivity()).mTemp;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_add_workingtime, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        litMonday_StartTime = (TextView) getView().findViewById(R.id.litMonday_StartTime);
        litMonday_EndTime = (TextView) getView().findViewById(R.id.litMonday_EndTime);

        litTuesday_StartTime = (TextView) getView().findViewById(R.id.litTuesday_StartTime);
        litTuesday_EndTime = (TextView) getView().findViewById(R.id.litTuesday_EndTime);

        litWednesday_StartTime = (TextView) getView().findViewById(R.id.litWednesday_StartTime);
        litWednesday_EndTime = (TextView) getView().findViewById(R.id.litWednesday_EndTime);

        litThursday_StartTime = (TextView) getView().findViewById(R.id.litThursday_StartTime);
        litThursday_EndTime = (TextView) getView().findViewById(R.id.litThursday_EndTime);

        litFriday_StartTime = (TextView) getView().findViewById(R.id.litFriday_StartTime);
        litFriday_EndTime = (TextView) getView().findViewById(R.id.litFriday_EndTime);

        litSaturday_StartTime = (TextView) getView().findViewById(R.id.litSaturday_StartTime);
        litSaturday_EndTime = (TextView) getView().findViewById(R.id.litSaturday_EndTime);

        litSunday_StartTime = (TextView) getView().findViewById(R.id.litSunday_StartTime);
        litSunday_EndTime = (TextView) getView().findViewById(R.id.litSunday_EndTime);

        litDate = (TextView) getView().findViewById(R.id.litDate);

        btnAddNonWorkingDays = (ImageButton) getView().findViewById(R.id.btnAddNonWorkingDays);

        calendar = Calendar.getInstance();

        bindListeners();
        bindData();
        saveTemp();
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        try {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day, 0, 0, 0);
            litDate.setText(df.format(new Date(c.getTimeInMillis())));
            litDate.setContentDescription(Long.toString(c.getTimeInMillis()));
        } catch (Exception e) {

        }
    }

    private void bindData() {
        try {

            ViewGroup group = (ViewGroup) getView().findViewById(R.id.llContainer);

            if (mTemp != null) {
                for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                    View view = group.getChildAt(i);

                    if (view instanceof EditText) {
                        String id = getResources().getResourceName(view.getId()).split("lit_")[1];
                        int resID = getResources().getIdentifier(id, "string", "ba.wurth.mb");
                        if (!mTemp.isNull(getString(resID))) ((EditText) view).setText(mTemp.getString(getString(resID)));

                        try {
                            View prevView = group.getChildAt(group.indexOfChild(view) - 1);
                            if (view != null && view instanceof TextView) {
                                if (!mTemp.isNull(getString(resID))) ((TextView) prevView).setText(mTemp.getString(getString(resID)));
                            }
                        } catch (JSONException e) {
                            wurthMB.AddError("ClientAddWorkingTimeFragment", "", e);
                        }
                    }
                }

                if (!mTemp.isNull("holidays")) {

                    LayoutInflater mInflater = LayoutInflater.from(getActivity());
                    final LinearLayout ll = (LinearLayout) getView().findViewById(R.id.llNonWorkingDays);

                    for (int i = 0; i < mTemp.getJSONArray("holidays").length(); i++) {
                        String jsonObject = mTemp.getJSONArray("holidays").getString(i);

                        View v = mInflater.inflate(R.layout.list_row, ll, false);

                        ((TextView) v.findViewById(R.id.litTitle)).setText(jsonObject);

                        v.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                try {
                                    ll.removeView(view);
                                    saveTemp();
                                }
                                catch (Exception exx) {

                                }
                                return true;
                            }
                        });

                        ll.addView(v);
                        ll.addView(mInflater.inflate(R.layout.divider, ll, false));
                    }
                }


                if (!mTemp.isNull("workingTime")) {
                    for (int i = 0; i < mTemp.getJSONArray("workingTime").length(); i++) {
                        JSONObject jsonObject = mTemp.getJSONArray("workingTime").getJSONObject(i);
                        switch (jsonObject.getInt("day")) {
                            case 1:
                                ((CheckBox) getView().findViewById(R.id.chkMonday)).setChecked(jsonObject.getBoolean("enabled"));
                                litMonday_StartTime.setContentDescription(jsonObject.getString("startTime"));
                                litMonday_EndTime.setContentDescription(jsonObject.getString("endTime"));
                                litMonday_StartTime.setText(tf.format(new Date(jsonObject.getLong("startTime"))));
                                litMonday_EndTime.setText(tf.format(new Date(jsonObject.getLong("endTime"))));
                                break;
                            case 2:
                                ((CheckBox) getView().findViewById(R.id.chkTuesday)).setChecked(jsonObject.getBoolean("enabled"));
                                litTuesday_StartTime.setContentDescription(jsonObject.getString("startTime"));
                                litTuesday_EndTime.setContentDescription(jsonObject.getString("endTime"));
                                litTuesday_StartTime.setText(tf.format(new Date(jsonObject.getLong("startTime"))));
                                litTuesday_EndTime.setText(tf.format(new Date(jsonObject.getLong("endTime"))));
                                break;
                            case 3:
                                ((CheckBox) getView().findViewById(R.id.chkWednesday)).setChecked(jsonObject.getBoolean("enabled"));
                                litWednesday_StartTime.setContentDescription(jsonObject.getString("startTime"));
                                litWednesday_EndTime.setContentDescription(jsonObject.getString("endTime"));
                                litWednesday_StartTime.setText(tf.format(new Date(jsonObject.getLong("startTime"))));
                                litWednesday_EndTime.setText(tf.format(new Date(jsonObject.getLong("endTime"))));
                                break;
                            case 4:
                                ((CheckBox) getView().findViewById(R.id.chkThursday)).setChecked(jsonObject.getBoolean("enabled"));
                                litThursday_StartTime.setContentDescription(jsonObject.getString("startTime"));
                                litThursday_EndTime.setContentDescription(jsonObject.getString("endTime"));
                                litThursday_StartTime.setText(tf.format(new Date(jsonObject.getLong("startTime"))));
                                litThursday_EndTime.setText(tf.format(new Date(jsonObject.getLong("endTime"))));
                                break;
                            case 5:
                                ((CheckBox) getView().findViewById(R.id.chkFriday)).setChecked(jsonObject.getBoolean("enabled"));
                                litFriday_StartTime.setContentDescription(jsonObject.getString("startTime"));
                                litFriday_EndTime.setContentDescription(jsonObject.getString("endTime"));
                                litFriday_StartTime.setText(tf.format(new Date(jsonObject.getLong("startTime"))));
                                litFriday_EndTime.setText(tf.format(new Date(jsonObject.getLong("endTime"))));
                                break;
                            case 6:
                                ((CheckBox) getView().findViewById(R.id.chkSaturday)).setChecked(jsonObject.getBoolean("enabled"));
                                litSaturday_StartTime.setContentDescription(jsonObject.getString("startTime"));
                                litSaturday_EndTime.setContentDescription(jsonObject.getString("endTime"));
                                litSaturday_StartTime.setText(tf.format(new Date(jsonObject.getLong("startTime"))));
                                litSaturday_EndTime.setText(tf.format(new Date(jsonObject.getLong("endTime"))));
                                break;
                            case 7:
                                ((CheckBox) getView().findViewById(R.id.chkSunday)).setChecked(jsonObject.getBoolean("enabled"));
                                litSaturday_StartTime.setContentDescription(jsonObject.getString("startTime"));
                                litSaturday_EndTime.setContentDescription(jsonObject.getString("endTime"));
                                litSaturday_StartTime.setText(tf.format(new Date(jsonObject.getLong("startTime"))));
                                litSaturday_EndTime.setText(tf.format(new Date(jsonObject.getLong("endTime"))));
                                break;

                            default:
                                break;
                        }
                    }
                }
            }

            for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                View view = group.getChildAt(i);
                if (view instanceof EditText) {
                    ((EditText) view).addTextChangedListener(new textWatcher());
                }
            }
        }
        catch (Exception ex) {
            wurthMB.AddError("ClientAddWorkingTimeFragment", "", ex);
        }
    }

    private void bindListeners() {
         try {

             calendar.setTimeInMillis(System.currentTimeMillis());

             calendar.set(Calendar.HOUR_OF_DAY, 7);
             calendar.set(Calendar.MINUTE, 30);
             
             litMonday_StartTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
             litTuesday_StartTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
             litWednesday_StartTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
             litThursday_StartTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
             litFriday_StartTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
             litSaturday_StartTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
             litSunday_StartTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));

             calendar.set(Calendar.HOUR_OF_DAY, 16);
             calendar.set(Calendar.MINUTE, 0);

             litMonday_EndTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
             litTuesday_EndTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
             litWednesday_EndTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
             litThursday_EndTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
             litFriday_EndTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
             litSaturday_EndTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));
             litSunday_EndTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));

             final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
             final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(null, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true, false);

             litDate.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     datePickerDialog.show(getFragmentManager(), "");
                 }
             });

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
                                 litMonday_StartTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litMonday_StartTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             case 2:
                                 litMonday_EndTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litMonday_EndTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             case 3:
                                 litTuesday_StartTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litTuesday_StartTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             case 4:
                                 litTuesday_EndTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litTuesday_EndTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             case 5:
                                 litWednesday_StartTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litWednesday_StartTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             case 6:
                                 litWednesday_EndTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litWednesday_EndTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             case 7:
                                 litThursday_StartTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litThursday_StartTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             case 8:
                                 litThursday_EndTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litThursday_EndTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             case 9:
                                 litFriday_StartTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litFriday_StartTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             case 10:
                                 litFriday_EndTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litFriday_EndTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             case 11:
                                 litSaturday_StartTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litSaturday_StartTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             case 12:
                                 litSaturday_EndTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litSaturday_EndTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             case 13:
                                 litSunday_StartTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litSunday_StartTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             case 14:
                                 litSunday_EndTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                                 litSunday_EndTime.setText(tf.format(new Date(c.getTimeInMillis())));
                                 break;
                             default:break;
                         }

                         saveTemp();

                     } catch (Exception e) {

                     }
                 }
             });

             litMonday_StartTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = STARTTIME_TAG_MONDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litMonday_StartTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             litMonday_EndTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = ENDTIME_TAG_MONDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litMonday_EndTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             litTuesday_StartTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = STARTTIME_TAG_TUESDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litTuesday_StartTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             litTuesday_EndTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = ENDTIME_TAG_TUESDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litTuesday_EndTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             litWednesday_StartTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = STARTTIME_TAG_WEDNESDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litWednesday_StartTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             litWednesday_EndTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = ENDTIME_TAG_WEDNESDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litWednesday_EndTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             litThursday_StartTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = STARTTIME_TAG_THURSDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litThursday_StartTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             litThursday_EndTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = ENDTIME_TAG_THURSDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litThursday_EndTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             litFriday_StartTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = STARTTIME_TAG_FRIDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litFriday_StartTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             litFriday_EndTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = ENDTIME_TAG_FRIDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litFriday_EndTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             litSaturday_StartTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = STARTTIME_TAG_SATURDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litSaturday_StartTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             litSaturday_EndTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = ENDTIME_TAG_SATURDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litSaturday_EndTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             litSunday_StartTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = STARTTIME_TAG_SUNDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litSunday_StartTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             litSunday_EndTime.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CURRENT_TAG = ENDTIME_TAG_SUNDAY;
                     Calendar c = Calendar.getInstance();
                     c.setTimeInMillis(Long.parseLong(litSunday_EndTime.getContentDescription().toString()));
                     timePickerDialog.setStartTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                     timePickerDialog.show(getFragmentManager(), STARTTIME_TAG_MONDAY);
                 }
             });

             btnAddNonWorkingDays.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {

                     if (litDate.getText().toString().equals("")) return;

                     boolean exists = false;

                     LayoutInflater mInflater = LayoutInflater.from(getActivity());

                     final LinearLayout ll = (LinearLayout) getView().findViewById(R.id.llNonWorkingDays);

                     for (int i = 0; i < ll.getChildCount(); i++) {
                         if (ll.getChildAt(i).findViewById(R.id.litTitle) != null && ((TypefacedTextView) ll.getChildAt(i).findViewById(R.id.litTitle)).getText().toString().equals(litDate.getText().toString())) {
                             exists = true;
                             break;
                         }
                     }

                     if (exists) return;

                     View v = mInflater.inflate(R.layout.list_row, ll, false);

                     ((TextView) v.findViewById(R.id.litTitle)).setText(litDate.getText());

                     v.setOnLongClickListener(new View.OnLongClickListener() {
                         @Override
                         public boolean onLongClick(View view) {
                             try {
                                 ll.removeView(view);
                                 saveTemp();
                             }
                             catch (Exception exx) {

                             }
                             return true;
                         }
                     });

                     ll.addView(v);
                     ll.addView(mInflater.inflate(R.layout.divider, ll, false));

                     saveTemp();
                 }
             });

         }
         catch (Exception ex) {

         }
    }

    private class textWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            try {
                saveTemp();
            }
            catch (Exception ex) {
            }
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }


    private void saveTemp() {
        try {

            if (mTemp == null) mTemp = new JSONObject();

            ViewGroup group = (ViewGroup) getView().findViewById(R.id.llContainer);

            for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                View view = group.getChildAt(i);

                if (view instanceof EditText) {
                    String id = getResources().getResourceName(view.getId()).split("lit_")[1];
                    int resID = getResources().getIdentifier(id, "string", "ba.wurth.mb");
                    mTemp.put(getString(resID), ((EditText) view).getText().toString());
                }
            }

            JSONArray workingTime = new JSONArray();

            JSONObject jsonObject_Monday = new JSONObject();
            jsonObject_Monday.put("day", 1);
            jsonObject_Monday.put("enabled", ((CheckBox) getView().findViewById(R.id.chkMonday)).isChecked());
            jsonObject_Monday.put("startTime", Long.parseLong(litMonday_StartTime.getContentDescription().toString()));
            jsonObject_Monday.put("endTime", Long.parseLong(litMonday_EndTime.getContentDescription().toString()));
            workingTime.put(jsonObject_Monday);

            JSONObject jsonObject_Tuesday = new JSONObject();
            jsonObject_Tuesday.put("day", 2);
            jsonObject_Tuesday.put("enabled", ((CheckBox) getView().findViewById(R.id.chkTuesday)).isChecked());
            jsonObject_Tuesday.put("startTime", Long.parseLong(litTuesday_StartTime.getContentDescription().toString()));
            jsonObject_Tuesday.put("endTime", Long.parseLong(litTuesday_EndTime.getContentDescription().toString()));
            workingTime.put(jsonObject_Tuesday);

            JSONObject jsonObject_Wednesday = new JSONObject();
            jsonObject_Wednesday.put("day", 3);
            jsonObject_Wednesday.put("enabled", ((CheckBox) getView().findViewById(R.id.chkWednesday)).isChecked());
            jsonObject_Wednesday.put("startTime", Long.parseLong(litWednesday_StartTime.getContentDescription().toString()));
            jsonObject_Wednesday.put("endTime", Long.parseLong(litWednesday_EndTime.getContentDescription().toString()));
            workingTime.put(jsonObject_Wednesday);

            JSONObject jsonObject_Thursday = new JSONObject();
            jsonObject_Thursday.put("day", 4);
            jsonObject_Thursday.put("enabled", ((CheckBox) getView().findViewById(R.id.chkThursday)).isChecked());
            jsonObject_Thursday.put("startTime", Long.parseLong(litThursday_StartTime.getContentDescription().toString()));
            jsonObject_Thursday.put("endTime", Long.parseLong(litThursday_EndTime.getContentDescription().toString()));
            workingTime.put(jsonObject_Thursday);

            JSONObject jsonObject_Friday = new JSONObject();
            jsonObject_Friday.put("day", 5);
            jsonObject_Friday.put("enabled", ((CheckBox) getView().findViewById(R.id.chkFriday)).isChecked());
            jsonObject_Friday.put("startTime", Long.parseLong(litFriday_StartTime.getContentDescription().toString()));
            jsonObject_Friday.put("endTime", Long.parseLong(litFriday_EndTime.getContentDescription().toString()));
            workingTime.put(jsonObject_Friday);

            JSONObject jsonObject_Saturday = new JSONObject();
            jsonObject_Saturday.put("day", 6);
            jsonObject_Saturday.put("enabled", ((CheckBox) getView().findViewById(R.id.chkSaturday)).isChecked());
            jsonObject_Saturday.put("startTime", Long.parseLong(litSaturday_StartTime.getContentDescription().toString()));
            jsonObject_Saturday.put("endTime", Long.parseLong(litSaturday_EndTime.getContentDescription().toString()));
            workingTime.put(jsonObject_Saturday);

            JSONObject jsonObject_Sunday = new JSONObject();
            jsonObject_Sunday.put("day", 7);
            jsonObject_Sunday.put("enabled", ((CheckBox) getView().findViewById(R.id.chkSunday)).isChecked());
            jsonObject_Sunday.put("startTime", Long.parseLong(litSunday_StartTime.getContentDescription().toString()));
            jsonObject_Sunday.put("endTime", Long.parseLong(litSunday_EndTime.getContentDescription().toString()));
            workingTime.put(jsonObject_Sunday);
            
            mTemp.put("workingTime", workingTime);

            JSONArray holidays = new JSONArray();

            final LinearLayout ll = (LinearLayout) getView().findViewById(R.id.llNonWorkingDays);

            for (int i = 0; i < ll.getChildCount(); i++) {
                if (ll.getChildAt(i).findViewById(R.id.litTitle) != null) {
                    holidays.put(((TypefacedTextView) ll.getChildAt(i).findViewById(R.id.litTitle)).getText());
                }
            }

            mTemp.put("holidays", holidays);

        }
        catch (Exception ex) {

        }
    }
}
