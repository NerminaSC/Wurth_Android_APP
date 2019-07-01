package ba.wurth.mb.Fragments.Routes;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fourmob.datetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import ba.wurth.mb.Adapters.RoutesAdapter;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Routes.DL_Routes;
import ba.wurth.mb.R;

public class RoutesFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private RoutesAdapter mAdapter;
    private Cursor mCursor;
    private ListView mListView;

    private TextView litDate;
    private EditText txbSearch;
    private ImageButton btnClear;

    private boolean busy = false;

    private Calendar calendar;
    public static final String DATEPICKER_TAG = "datepicker";

    private SwipeRefreshLayout swipeLayout;

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
        return inflater.inflate(R.layout.list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bindData();
            }
        });
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);

        mListView = (ListView) getView().findViewById(R.id.list);
        litDate = (TextView) getView().findViewById(R.id.litDate);
        txbSearch = (EditText) getView().findViewById(R.id.txbSearch);
        btnClear = (ImageButton) getView().findViewById(R.id.btnClear);

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


        getView().findViewById(R.id.llFilter).setVisibility(View.GONE);

    }

    @Override
    public void onResume() {
        super.onResume();
        bindData();
    }

    public void bindData() {
        try {
            if (!busy) new LongTask().execute();
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
                wurthMB.AddError("Routes", ex.getMessage(), ex);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(300);
                mCursor = DL_Routes.Get();
                getActivity().startManagingCursor(mCursor);
            }
            catch (Exception ex) {
                wurthMB.AddError("Routes", ex.getMessage(), ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            try {
                mAdapter = new RoutesAdapter(getActivity(), mCursor, RoutesFragment.this, false);
                mListView.setAdapter(mAdapter);
                swipeLayout.setRefreshing(false);
                getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
            } catch (Exception ex) {
                wurthMB.AddError("Routes", ex.getMessage(), ex);
            }
            busy = false;
        }
    }
}
