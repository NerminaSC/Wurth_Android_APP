package ba.wurth.mb.Fragments.Orders;

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

import org.json.JSONArray;

import ba.wurth.mb.Adapters.OrsyPricesAdapter;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Temp.DL_Temp;
import ba.wurth.mb.R;

public class OrsyPricesFragment extends Fragment {

    private OrsyPricesAdapter mAdapter;
    private ListView mListView;
    private Cursor mCursor;

    private EditText txbSearch;
    private ImageButton btnClear;

    private Long PartnerID = 0L;
    private boolean busy = false;


    private SwipeRefreshLayout swipeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            PartnerID = getArguments().getLong("PartnerID", 0L);
        }
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
        txbSearch = (EditText) getView().findViewById(R.id.txbSearch);
        btnClear = (ImageButton) getView().findViewById(R.id.btnClear);

        txbSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                handler.removeMessages(TRIGGER_SEARCH);
                handler.sendEmptyMessageDelayed(TRIGGER_SEARCH, SEARCH_TRIGGER_DELAY_IN_MS);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });


        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txbSearch.setText("");
            }
        });
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

        @Override
        protected void onPreExecute() {

            busy = true;

            try {
                getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
            }
            catch (Exception ex) {
                wurthMB.AddError("NegotiatedPricesFragment", ex.getMessage(), ex);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mCursor = DL_Temp.GetByOptionID(41);
                getActivity().startManagingCursor(mCursor);
            }
            catch (Exception ex) {
                wurthMB.AddError("NegotiatedPricesFragment", ex.getMessage(), ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            try {
                JSONArray items = Common.cur2JsonArray(mCursor);
                mAdapter = new OrsyPricesAdapter(getActivity(), R.layout.list_row_product, items, OrsyPricesFragment.this);
                mListView.setAdapter(mAdapter);
                swipeLayout.setRefreshing(false);
                getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
            } catch (Exception ex) {
                wurthMB.AddError("NegotiatedPricesFragment", ex.getMessage(), ex);
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
