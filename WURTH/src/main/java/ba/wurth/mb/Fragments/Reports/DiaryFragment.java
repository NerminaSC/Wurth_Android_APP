package ba.wurth.mb.Fragments.Reports;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.fourmob.datetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import ba.wurth.mb.Adapters.DiaryAdapter;
import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class DiaryFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private DiaryAdapter mAdapter;
    private Cursor mCursor;
    private ListView mListView;

    private TextView litDate;
    private EditText txbSearch;
    private ImageButton btnClear;

    private Integer TypeID = 0;
    private Long PartnerID = 0L;
    private boolean busy = false;

    private Long limit = 500L;

    private Spinner spSections;
    private SpinnerItem[] section_items;
    private SpinnerAdapter adapter_sectioniotems;

    private Calendar calendar;
    public static final String DATEPICKER_TAG = "datepicker";

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
        return inflater.inflate(R.layout.list_diary, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView = (ListView) getView().findViewById(R.id.list);
        litDate = (TextView) getView().findViewById(R.id.litDate);
        txbSearch = (EditText) getView().findViewById(R.id.txbSearch);
        btnClear = (ImageButton) getView().findViewById(R.id.btnClear);
        spSections = (Spinner) getView().findViewById(R.id.spSection);

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

        section_items = new SpinnerItem[6];
        section_items[0] = new SpinnerItem(0L, "-", "", "");
        section_items[1] = new SpinnerItem(1L, getString(R.string.Orders), "", "");
        section_items[2] = new SpinnerItem(9L, getString(R.string.Visits), "", "");
        section_items[3] = new SpinnerItem(32L, getString(R.string.Tasks), "", "");
        section_items[4] = new SpinnerItem(31L, "Memo", "", "");
        section_items[5] = new SpinnerItem(33L, "Activities", "", "");

        adapter_sectioniotems = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, section_items);
        spSections.setAdapter(adapter_sectioniotems);

        spSections.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TypeID = section_items[i].getId().intValue();
                bindData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (getArguments() != null && getArguments().getLong("PartnerID", 0L) > 0) {
            PartnerID = getArguments().getLong("PartnerID", 0L);
        }

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
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) new LongTask().execute();
                else new LongTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        catch (Exception ex) {

        }
    }

    private class LongTask extends AsyncTask<Void, Void, Void> {


        private String searchText = "";
        private Long _date = 0L;

        @Override
        protected void onPreExecute() {

            busy = true;

            try {
                getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
                searchText = txbSearch.getText().toString();
                _date = Long.parseLong(litDate.getContentDescription().toString());
            }
            catch (Exception ex) {
                wurthMB.AddError("Diary list", ex.getMessage(), ex);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mCursor = DL_Wurth.GET_Diary(searchText, TypeID, _date, PartnerID, limit);
            }
            catch (Exception ex) {
                wurthMB.AddError("Diary list", ex.getMessage(), ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            try {
                if (isAdded()) {
                    getActivity().startManagingCursor(mCursor);
                    mAdapter = new DiaryAdapter(getActivity(), mCursor, DiaryFragment.this);
                    mListView.setAdapter(mAdapter);
                    getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
                }
            } catch (Exception ex) {
                wurthMB.AddError("Diary list", ex.getMessage(), ex);
            }
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
