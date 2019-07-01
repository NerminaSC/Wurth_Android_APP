package ba.wurth.mb.Fragments.Tasks;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.fourmob.datetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import ba.wurth.mb.Adapters.TasksAdapter;
import ba.wurth.mb.Classes.Objects.Task;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Tasks.DL_Tasks;
import ba.wurth.mb.R;

public class TasksFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private TasksAdapter mAdapter;
    private ListView mListView;

    private TextView litDate;
    private EditText txbSearch;
    private ImageButton btnClear;

    private Button btnSent;
    private Button btnRecieved;
    private Button btnAll;

    private boolean busy = false;

    private ArrayList<Task> items;

    private Calendar calendar;
    public static final String DATEPICKER_TAG = "datepicker";

    private int STATUS = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        litDate.setText(day + "." + (month + 1) + "." + year);
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, 0, 0, 0);
        litDate.setContentDescription(Long.toString(c.getTimeInMillis()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tasks, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView = (ListView) getView().findViewById(R.id.list);
        litDate = (TextView) getView().findViewById(R.id.litDate);
        txbSearch = (EditText) getView().findViewById(R.id.txbSearch);
        btnClear = (ImageButton) getView().findViewById(R.id.btnClear);
        btnSent = (Button) getView().findViewById(R.id.btnSent);
        btnRecieved = (Button) getView().findViewById(R.id.btnRecieved);
        btnAll = (Button) getView().findViewById(R.id.btnAll);

        calendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);

        litDate.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                bindData();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        txbSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                bindData();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        litDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.setYearRange(2010, calendar.get(Calendar.YEAR) + 1);
                datePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txbSearch.setText("");
                litDate.setText("");
                litDate.setContentDescription("0");
                bindData();
            }
        });

        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                STATUS = 0;
                bindData();
            }
        });

        btnSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                STATUS = 2;
                bindData();
            }
        });

        btnRecieved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                STATUS = 1;
                bindData();
            }
        });

        getView().findViewById(R.id.llFilter).setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindData();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void bindData() {
        try {
            if (!busy) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    new LongTask().execute();
                } else {
                    new LongTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
        catch (Exception ex) {

        }
    }

    private class LongTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

            busy = true;

            try {
                getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
            }
            catch (Exception ex) {
                wurthMB.AddError("Tasks", ex.getMessage(), ex);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                Thread.sleep(300);
                Cursor cur = DL_Tasks.Get();

                items = new ArrayList<Task>();

                if (cur != null) {

                    while (cur.moveToNext()) {

                        JSONObject jsonObject = new JSONObject(cur.getString(cur.getColumnIndex("Parameters")));

                        JSONArray users = jsonObject.getJSONArray("Users");

                        boolean userExists = false;

                        for(int i = 0; i < users.length(); i++) {
                            if (users.getJSONObject(i).getLong("UserID") == wurthMB.getUser().UserID && (STATUS == 0 || STATUS == 1)) {
                                userExists = true;
                                break;
                            }
                        }

                        if (!jsonObject.isNull("CreatedBy") && jsonObject.getLong("CreatedBy") == wurthMB.getUser().UserID && (STATUS == 0 || STATUS == 2)) {
                            userExists = true;
                        }

                        if (userExists) {
                            Task mItem = new Task();
                            mItem.TaskID = cur.getLong(cur.getColumnIndex("TaskID"));
                            mItem.Parameters = cur.getString(cur.getColumnIndex("Parameters"));
                            mItem.Name = cur.getString(cur.getColumnIndex("Name"));
                            mItem.Description = cur.getString(cur.getColumnIndex("Description"));
                            mItem.StatusID = cur.getInt(cur.getColumnIndex("StatusID"));
                            items.add(mItem);
                        }
                    }
                    cur.close();
                }
            }
            catch (Exception ex) {
                wurthMB.AddError("Tasks", ex.getMessage(), ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            try {
                mAdapter = new TasksAdapter(getActivity(), R.layout.list, items, TasksFragment.this);
                mListView.setAdapter(mAdapter);
                getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
            } catch (Exception ex) {
                wurthMB.AddError("Tasks", ex.getMessage(), ex);
            }
            busy = false;
        }
    }
}
