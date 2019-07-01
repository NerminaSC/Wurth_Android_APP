package ba.wurth.mb.Fragments.Reports;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;

import com.fourmob.datetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ba.wurth.mb.Adapters.AutoCompleteProductsAdapter;
import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.Classes.Objects.Product;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Reports.DL_Reports;
import ba.wurth.mb.R;

public class ReportsProductFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private Calendar calendar;
    public static final String STARTTIME_TAG = "0";
    public static final String ENDTTIME_TAG = "1";

    private TextView litStartTime;
    private TextView litEndTime;

    private Cursor curProducts = null;

    Long ClientID = 0L;
    Long DeliveryPlaceID = 0L;
    Long ProductID= 0L;

    public AutoCompleteTextView txbProducts;
    private SimpleCursorAdapter mAdapterProducts;

    private static SimpleDateFormat gDateFormatDataItem = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reportproduct, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        litStartTime = (TextView) getView().findViewById(R.id.litStartTime);
        litEndTime = (TextView) getView().findViewById(R.id.litEndTime);
        calendar = Calendar.getInstance();
        txbProducts = (AutoCompleteTextView) getView().findViewById(R.id.txbProducts);

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


        calendar.setTimeInMillis(calendar.getTimeInMillis() + (24 * 60 * 60 * 1000));

        final DatePickerDialog datePickerDialogPaymentDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        litEndTime.setText(gDateFormatDataItem.format(new Date(calendar.getTimeInMillis())));
        litEndTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));

        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        final DatePickerDialog datePickerDialogOrderDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        litStartTime.setText(gDateFormatDataItem.format(new Date(calendar.getTimeInMillis())));
        litStartTime.setContentDescription(Long.toString(calendar.getTimeInMillis()));

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
                ClientID = getArguments().getLong("ClientID", 0L);
            }

            if (getArguments() != null && getArguments().getLong("DeliveryPlaceID", 0L) > 0) {
                DeliveryPlaceID = getArguments().getLong("DeliveryPlaceID", 0L);
            }
        }
        catch (Exception ex) {

        }
    }

    private void bindListeners() {
        try {

            mAdapterProducts = new AutoCompleteProductsAdapter(getActivity());

            mAdapterProducts.setFilterQueryProvider(new FilterQueryProvider() {
                public Cursor runQuery(CharSequence str) {
                    try {
                        if (txbProducts.getText().toString().length() < 2) return null;
                        Cursor cur = DL_Wurth.GET_ProductsSearch(txbProducts.getText().toString().replace(" ",""));
                        return cur;
                    }
                    catch (Exception ex) { }
                    return null;
                }
            });

            mAdapterProducts.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
                public CharSequence convertToString(Cursor cur) {
                    try {
                        int index = cur.getColumnIndex("Code");
                        return cur.getString(index);
                    }
                    catch (Exception ex) {}
                    return "";
                }
            });

            txbProducts.setAdapter(mAdapterProducts);
            txbProducts.setThreshold(3);

            txbProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        if (view.findViewById(R.id.litTitle).getContentDescription() != null) {
                            Product p = new Product();
                            p.ArtikalID = Long.parseLong(view.findViewById(R.id.litTitle).getContentDescription().toString());
                            Cursor _cur = DL_Wurth.GET_Product(p.ArtikalID);
                            if (_cur != null) {
                                if (_cur.moveToFirst()) {
                                    ProductID = _cur.getLong(_cur.getColumnIndex("ProductID"));
                                    //txbProducts.setText(p.Code);
                                }
                                _cur.close();
                            }
                        }
                    }
                    catch (Exception ex) {

                    }

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

                curProducts = DL_Reports.GetProductReport(startDate, endDate, ClientID, DeliveryPlaceID, ProductID);

            }
            catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {

            try {

                LayoutInflater mInflater = LayoutInflater.from(getActivity());

                // Create detail report for products
                if (curProducts != null) {

                    LinearLayout ll = (LinearLayout) getView().findViewById(R.id.containerProducts);
                    ll.removeAllViews();

                    while (curProducts.moveToNext()) {

                        View v = mInflater.inflate(R.layout.list_row, ll, false);

                        v.findViewById(R.id.litStatus).setVisibility(View.GONE);

                        ((TextView) v.findViewById(R.id.litTitle)).setText(df.format(curProducts.getLong(curProducts.getColumnIndex("OrderDate"))));
                        ((TextView) v.findViewById(R.id.litSubTitle)).setText(CustomNumberFormat.GenerateFormat(curProducts.getDouble(curProducts.getColumnIndex("UserDiscountPercentage")) + curProducts.getDouble(curProducts.getColumnIndex("ClientDiscountPercentage"))) + "%");
                        ((TextView) v.findViewById(R.id.litTotal)).setText(CustomNumberFormat.GenerateFormatCurrency(curProducts.getDouble(curProducts.getColumnIndex("Price_WS"))));
                        ((TextView) v.findViewById(R.id.litSupTotal)).setText("x " + curProducts.getString(curProducts.getColumnIndex("Quantity")));

                        if (curProducts.getString(curProducts.getColumnIndex("ProductName")).length() > 0) ((TextView) v.findViewById(R.id.litStatus)).setText(curProducts.getString(curProducts.getColumnIndex("ProductName")).substring(0,1));

                        try {
                            //JSONObject jsonObject = new JSONObject(curProducts.getString(curProducts.getColumnIndex("Note")));
                            //((TextView) v.findViewById(R.id.litSubTitle)).setText(jsonObject.getString("Note"));
                        }
                        catch (Exception exx) {

                        }

                        ll.addView(v);
                        ll.addView(mInflater.inflate(R.layout.divider, ll, false));
                    }
                }

                if (curProducts != null && !curProducts.isClosed()) curProducts.close();
                curProducts = null;
                // End of detail report for products


            }
            catch (Exception e) {
                wurthMB.AddError("ReportsDaily", e.getMessage(), e);
            }

            getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
        }
    }
}
