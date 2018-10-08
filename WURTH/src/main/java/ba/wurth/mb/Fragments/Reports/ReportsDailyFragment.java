package ba.wurth.mb.Fragments.Reports;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ba.wurth.mb.Adapters.AutoCompleteClientsAdapter;
import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Reports.DL_Reports;
import ba.wurth.mb.R;

public class ReportsDailyFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private Calendar calendar;
    public static final String STARTTIME_TAG = "0";
    public static final String ENDTTIME_TAG = "1";

    private TextView litStartTime;
    private TextView litEndTime;

    private SimpleCursorAdapter mAdapterClients;
    private SimpleCursorAdapter mAdapterDeliveryPlaces;

    public AutoCompleteTextView txbClients;
    public AutoCompleteTextView txbDeliveryPlaces;

    private ImageButton btnClearClients;
    private ImageButton btnClearDeliveryPlace;

    private Cursor cur = null;
    private Cursor curProducts = null;
    private Cursor curProductCategories = null;
    private Cursor curOrders = null;

    Double OrderTotals = 0D;
    int OrderCount = 0;
    int VisitCount = 0;
    int DocumentCount = 0;

    private static SimpleDateFormat gDateFormatDataItem = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reportdaily, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        litStartTime = (TextView) getView().findViewById(R.id.litStartTime);
        litEndTime = (TextView) getView().findViewById(R.id.litEndTime);
        calendar = Calendar.getInstance();

        txbClients = (AutoCompleteTextView) getView().findViewById(R.id.txbClients);
        txbDeliveryPlaces = (AutoCompleteTextView) getView().findViewById(R.id.txbDeliveryPlaces);

        btnClearClients = (ImageButton) getView().findViewById(R.id.btnClearClients);
        btnClearDeliveryPlace = (ImageButton) getView().findViewById(R.id.btnClearDeliveryPlace);

        bindData();
        bindListeners();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        final DatePickerDialog datePickerDialogOrderDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        litStartTime.setText(gDateFormatDataItem.format(new Date(calendar.getTimeInMillis())));
        litStartTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));

        calendar.setTimeInMillis(calendar.getTimeInMillis() + (24 * 60 * 60 * 1000));

        final DatePickerDialog datePickerDialogPaymentDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        litEndTime.setText(gDateFormatDataItem.format(new Date(calendar.getTimeInMillis())));
        litEndTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));

        datePickerDialogOrderDate.setYearRange(2010, calendar.get(Calendar.YEAR) + 2);
        datePickerDialogPaymentDate.setYearRange(2010, calendar.get(Calendar.YEAR) + 2);

        litStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialogOrderDate.show(getFragmentManager(), STARTTIME_TAG);
            }
        });

        litEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialogPaymentDate.show(getFragmentManager(), ENDTTIME_TAG);
            }
        });

        getView().findViewById(R.id.btnGenerate).setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) new LongTask().execute();
                else new LongTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {

        Calendar c = Calendar.getInstance();

        switch (Integer.parseInt(datePickerDialog.getTag())) {
            case 0:
                litStartTime.setText(day + "." + (month + 1) + "." + year);
                c.set(year, month, day, 0, 0, 0);
                litStartTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                break;
            case 1:
                litEndTime.setText(day + "." + (month + 1) + "." + year);
                c.set(year, month, day, 0, 0, 0);
                litEndTime.setContentDescription(Long.toString(c.getTimeInMillis()));
                break;
            default:
                break;
        }
    }

    private void bindData() {
        try {
            if (getArguments() != null && getArguments().getLong("ClientID", 0L) > 0) {
                Client c = DL_Clients.GetByClientID(getArguments().getLong("ClientID", 0L));
                if (c != null) {
                    txbClients.setText(c.Name);
                    txbClients.setContentDescription(Long.toString(c.ClientID));
                }
            }

            if (getArguments() != null && getArguments().getLong("DeliveryPlaceID", 0L) > 0) {
                Cursor c = DL_Clients.Get_DeliveryPlaceByDeliveryPlaceID(getArguments().getLong("DeliveryPlaceID", 0L));
                if (c != null && c.getCount() > 0 && c.moveToFirst()) {
                    txbDeliveryPlaces.setText(c.getString(c.getColumnIndex("Name")));
                    txbDeliveryPlaces.setContentDescription(c.getString(c.getColumnIndex("DeliveryPlaceID")));
                    c.close();
                }
            }
        }
        catch (Exception ex) {

        }
    }

    private void bindListeners() {
        try {

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
                    Cursor cur = DL_Wurth.GET_Partner(l);
                    if (cur != null) {
                        if (cur.moveToFirst()) {
                            txbClients.setContentDescription(cur.getString(cur.getColumnIndex("ClientID")));
                        }
                        cur.close();
                    }
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(txbClients.getWindowToken(), 0);
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

        }
        catch (Exception ex) {

        }
    }

    private class LongTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {

                Long startDate = Long.parseLong(litStartTime.getContentDescription().toString());
                Long endDate = Long.parseLong(litEndTime.getContentDescription().toString());

                curProducts = DL_Reports.GetOrderTotalsByProducts(startDate, endDate, Long.parseLong(txbClients.getContentDescription().toString()), Long.parseLong(txbDeliveryPlaces.getContentDescription().toString()));
                curOrders = DL_Reports.GetTotalsByOrders(startDate, endDate, Long.parseLong(txbClients.getContentDescription().toString()), Long.parseLong(txbDeliveryPlaces.getContentDescription().toString()));

                OrderTotals = DL_Reports.GetOrderTotals(startDate, endDate, Long.parseLong(txbClients.getContentDescription().toString()), Long.parseLong(txbDeliveryPlaces.getContentDescription().toString()));
                OrderCount = DL_Reports.GetOrderCount(startDate, endDate, Long.parseLong(txbClients.getContentDescription().toString()), Long.parseLong(txbDeliveryPlaces.getContentDescription().toString()));
                VisitCount = DL_Reports.GetVisitCount(startDate, endDate, Long.parseLong(txbClients.getContentDescription().toString()), Long.parseLong(txbDeliveryPlaces.getContentDescription().toString()));
                DocumentCount = DL_Reports.GetDocumentCount(startDate, endDate, Long.parseLong(txbClients.getContentDescription().toString()), Long.parseLong(txbDeliveryPlaces.getContentDescription().toString()));

            }
            catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {

            try {

                LayoutInflater mInflater = LayoutInflater.from(getActivity());

                ((TextView) getView().findViewById(R.id.lblTotal)).setText(CustomNumberFormat.GenerateFormatCurrency(OrderTotals));
                ((TextView) getView().findViewById(R.id.lblOrdersCount)).setText(Integer.toString(OrderCount));
                ((TextView) getView().findViewById(R.id.lblVisitsCount)).setText(Integer.toString(VisitCount));
                ((TextView) getView().findViewById(R.id.lblDocumentsCount)).setText(Integer.toString(DocumentCount));


                // Create detail report for products
                if (curProducts != null) {

                    LinearLayout ll = (LinearLayout) getView().findViewById(R.id.containerProducts);
                    ll.removeAllViews();

                    while (curProducts.moveToNext()) {

                        View v = mInflater.inflate(R.layout.list_row, ll, false);

                        ((TextView) v.findViewById(R.id.litTitle)).setText(curProducts.getString(curProducts.getColumnIndex("ProductName")));
                        ((TextView) v.findViewById(R.id.litSubTitle)).setText(curProducts.getString(curProducts.getColumnIndex("sifra")));
                        ((TextView) v.findViewById(R.id.litTotal)).setText(CustomNumberFormat.GenerateFormatCurrency(curProducts.getDouble(curProducts.getColumnIndex("Total"))));
                        ((TextView) v.findViewById(R.id.litSupTotal)).setText(curProducts.getString(curProducts.getColumnIndex("Count")));

                        if (curProducts.getString(curProducts.getColumnIndex("ProductName")).length() > 0) ((TextView) v.findViewById(R.id.litStatus)).setText(curProducts.getString(curProducts.getColumnIndex("ProductName")).substring(0,1));

                        ll.addView(v);
                        ll.addView(mInflater.inflate(R.layout.divider, ll, false));
                    }
                }

                if (curProducts != null && !curProducts.isClosed()) curProducts.close();
                curProducts = null;
                // End of detail report for products

                // Graph
                ((LinearLayout) getView().findViewById(R.id.Graph)).removeAllViews();

                if (curOrders != null && curOrders.getCount() > 0) {
                    GraphView.GraphViewData[] data = new GraphView.GraphViewData[curOrders.getCount()];
                    while (curOrders.moveToNext()) {
                        data[curOrders.getPosition()] = new GraphView.GraphViewData(curOrders.getLong(curOrders.getColumnIndex("OrderDate")), curOrders.getDouble(curOrders.getColumnIndex("GrandTotal")));
                    }

                    GraphView graphView = new LineGraphView(getActivity(), "") {
                        @Override
                        protected String formatLabel(double value, boolean isValueX) {
                            if (isValueX) {
                                // transform number to time
                                return gDateFormatDataItem.format(new Date((long) value));
                            } else {
                                return super.formatLabel(value, isValueX);
                            }
                        }
                    };

                    graphView.addSeries(new GraphViewSeries(data));
                    graphView.setScrollable(true);
                    graphView.setScalable(true);

                    LinearLayout layout = (LinearLayout) getView().findViewById(R.id.Graph);
                    layout.addView(graphView);
                }

                if (curOrders != null && !curOrders.isClosed()) curOrders.close();
                curOrders = null;

                // End of graph

            }
            catch (Exception e) {
                wurthMB.AddError("ReportsDaily", e.getMessage(), e);
            }

            getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
        }
    }
}
