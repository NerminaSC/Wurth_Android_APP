package ba.wurth.mb.Fragments.Visits;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import ba.wurth.mb.Adapters.VisitsAdapter;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class VisitsFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private VisitsAdapter mAdapter;
    private Cursor mCursor;
    private ListView mListView;

    private TextView litDate;
    private EditText txbSearch;
    private ImageButton btnClear;

    private Long ClientID = 0L;
    private Long DeliveryPlaceID = 0L;
    private boolean busy = false;

    private Long limit = 500L;

    private Calendar calendar;
    public static final String DATEPICKER_TAG = "datepicker";

    private SwipeRefreshLayout swipeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            ClientID = getArguments().getLong("ClientID", 0L);
            DeliveryPlaceID = getArguments().getLong("DeliveryPlaceID", 0L);
        }
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
        //swipeLayout.setColorScheme(Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED);

        mListView = (ListView) getView().findViewById(R.id.list);
        litDate = (TextView) getView().findViewById(R.id.litDate);
        txbSearch = (EditText) getView().findViewById(R.id.txbSearch);
        btnClear = (ImageButton) getView().findViewById(R.id.btnClear);

        calendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);

        litDate.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                handler.removeMessages(TRIGGER_SEARCH);
                handler.sendEmptyMessageDelayed(TRIGGER_SEARCH, SEARCH_TRIGGER_DELAY_IN_MS);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        txbSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                handler.removeMessages(TRIGGER_SEARCH);
                handler.sendEmptyMessageDelayed(TRIGGER_SEARCH, SEARCH_TRIGGER_DELAY_IN_MS);
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
            }
        });

        btnClear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                limit = 0L;
                bindData();
                return true;
            }
        });

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

        private String searchText = "";
        private Long VisitDate = 0L;

        @Override
        protected void onPreExecute() {
            busy = true;
            try {
                getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
                searchText = txbSearch.getText().toString();
                VisitDate = Long.parseLong(litDate.getContentDescription().toString());
            }
            catch (Exception ex) {

            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(300);
                mCursor = DL_Wurth.GET_VISITS(searchText, ClientID, DeliveryPlaceID, VisitDate, limit);
                getActivity().startManagingCursor(mCursor);
            }
            catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            try {
                mAdapter = new VisitsAdapter(getActivity(), mCursor, VisitsFragment.this);
                mListView.setAdapter(mAdapter);
                swipeLayout.setRefreshing(false);
            } catch (Exception e) {
                wurthMB.AddError("VisitsFragment", e.getMessage(), e);

            }
            getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
            busy = false;
        }
    }

    private final int TRIGGER_SEARCH = 1;
    private final long SEARCH_TRIGGER_DELAY_IN_MS = 1000;

    private Handler handler = new Handler() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TRIGGER_SEARCH) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    new LongTask().execute();
                } else {
                    new LongTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    };
}
