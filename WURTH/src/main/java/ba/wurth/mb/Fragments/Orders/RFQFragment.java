package ba.wurth.mb.Fragments.Orders;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fourmob.datetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import ba.wurth.mb.Adapters.OrdersAdapter;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Orders.DL_Orders;
import ba.wurth.mb.R;

public class RFQFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private OrdersAdapter mAdapter;
    private Cursor mCursor;
    private ListView mListView;

    private TextView litDate;
    private EditText txbSearch;
    private ImageButton btnClear;

    private Long ClientID = 0L;
    private Long DeliveryPlaceID = 0L;
    private Integer OrderStatusID = 0;
    private boolean busy = false;

    private Calendar calendar;
    public static final String DATEPICKER_TAG = "datepicker";

    private SwipeRefreshLayout swipeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            ClientID = getArguments().getLong("ClientID", 0L);
            DeliveryPlaceID = getArguments().getLong("DeliveryPlaceID", 0L);
            OrderStatusID = getArguments().getInt("OrderStatusID", 12);
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
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);

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

        if (OrderStatusID == 1) {
            getView().findViewById(R.id.llActions).setVisibility(View.VISIBLE);

            getView().findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (mAdapter != null && mAdapter.checkedItems != null) {
                            getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
                            for(Long _id : mAdapter.checkedItems) {
                                DL_Orders.UpdateOrderStatus(_id, 4);
                            }
                            getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
                            bindData();
                        }
                    }
                    catch (Exception Ex) {

                    }
                }
            });

            getView().findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                    catch (Exception Ex) {

                    }
                }
            });
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
                wurthMB.AddError("Orders", ex.getMessage(), ex);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(300);
                mCursor = DL_Orders.GetProposals(txbSearch.getText().toString(), ClientID, DeliveryPlaceID, OrderStatusID, Long.parseLong(litDate.getContentDescription().toString()));
                getActivity().startManagingCursor(mCursor);
            }
            catch (Exception ex) {
                wurthMB.AddError("Orders", ex.getMessage(), ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            try {
                mAdapter = new OrdersAdapter(getActivity(), mCursor, RFQFragment.this, OrderStatusID == 1 ? true : false);
                mListView.setAdapter(mAdapter);
                swipeLayout.setRefreshing(false);
                getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
            } catch (Exception ex) {
                wurthMB.AddError("Orders", ex.getMessage(), ex);
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
