package ba.wurth.mb.Fragments.Orders;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ba.wurth.mb.Adapters.AutoCompleteClientsAdapter;
import ba.wurth.mb.Adapters.AutoCompleteProductsAdapter;
import ba.wurth.mb.Adapters.OrsyPriceAdapter;
import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Product;
import ba.wurth.mb.Classes.Objects.Temp_Acquisition;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Temp.DL_Temp;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class OrsyPriceFragment extends Fragment {

    private EditText txbQuantity;
    private EditText txbPrice;
    private EditText txbNote;

    public AutoCompleteTextView txbClients;
    public AutoCompleteTextView txbProducts;

    private SimpleCursorAdapter mAdapterClients;


    private ImageButton btnClearClients;
    private Button btnAdd;
    private Button btnGenerate;

    private Spinner spPackage;

    private ListView mList;
    private Fragment mFragment;

    private OrsyPriceAdapter mAdapter;
    private SimpleCursorAdapter mAdapterProducts;

    public JSONObject jsonObject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment = this;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("items", new JSONArray());
        } catch (JSONException e) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.orsy_price, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txbClients = (AutoCompleteTextView) getView().findViewById(R.id.txbClients);
        btnClearClients = (ImageButton) getView().findViewById(R.id.btnClearClients);
        btnAdd = (Button) getView().findViewById(R.id.btnAdd);
        btnGenerate = (Button) getView().findViewById(R.id.btnGenerate);
        spPackage = (Spinner) getView().findViewById(R.id.spPackage);
        txbQuantity = (EditText) getView().findViewById(R.id.txbQuantity);
        txbPrice = (EditText) getView().findViewById(R.id.txbPrice);
        txbNote = (EditText) getView().findViewById(R.id.txbNote);

        mList = (ListView) getView().findViewById(R.id.list);
        txbProducts = (AutoCompleteTextView) getView().findViewById(R.id.txbProducts);

        btnClearClients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txbClients.setText("");
                txbClients.setContentDescription("0");
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

        txbClients.setAdapter(mAdapterClients);
        txbClients.setThreshold(0);

        txbClients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                txbClients.setContentDescription(Long.toString(l));
                Cursor cur = DL_Wurth.GET_Partner(l);
                txbProducts.requestFocus();
            }
        });

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

                                p.Grupa_Artikla = _cur.getLong(_cur.getColumnIndex("Grupa_Artikla"));
                                p.ProductID = _cur.getLong(_cur.getColumnIndex("ProductID"));
                                p.Name = _cur.getString(_cur.getColumnIndex("Naziv"));
                                p.Naziv = _cur.getString(_cur.getColumnIndex("Naziv"));
                                p.Code = _cur.getString(_cur.getColumnIndex("sifra"));
                                p.MjernaJedinica = _cur.getString(_cur.getColumnIndex("MjernaJedinica"));
                                p.Status_Artikla = _cur.getInt(_cur.getColumnIndex("Status_Artikla"));
                                p.Status_Prezentacije_Artikla = _cur.getInt(_cur.getColumnIndex("Status_Prezentacije_Artikla"));
                                txbProducts.setText(p.Code);
                            }
                            _cur.close();
                        }

                        if (p.Status_Artikla != 0 && p.Status_Artikla != 1) {
                            Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.Notification_ProductCanNotBeAdded), 2);
                        }
                        else {
                            Cursor cur = DL_Wurth.GET_Packages(p.ArtikalID);
                            if (cur != null) {

                                SpinnerItem[] items;
                                SpinnerAdapter adapter;
                                items = new SpinnerItem[cur.getCount()];

                                while (cur.moveToNext()) {
                                    items[cur.getPosition()] = new SpinnerItem(new Long(cur.getPosition()), " x " + cur.getString(cur.getColumnIndex("Pakovanje")) + " " + p.MjernaJedinica , cur.getString(cur.getColumnIndex("Barcode")), cur.getString(cur.getColumnIndex("Pakovanje")));
                                }

                                adapter = new SpinnerAdapter(mFragment.getActivity(), R.layout.simple_dropdown_item_1line, items);
                                spPackage.setAdapter(adapter);

                                spPackage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                        Double pakovanje;
                                        pakovanje = Double.parseDouble(((SpinnerItem) spPackage.getSelectedItem()).getValue());
                                        spPackage.setContentDescription(Double.toString(pakovanje));
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> adapterView) {

                                    }
                                });

                                cur.close();
                            }

                            txbProducts.setTag(p);
                            txbQuantity.requestFocus();
                        }
                    }
                }
                catch (Exception e) {
                    wurthMB.AddError("OrsyPriceFragment", e.getMessage(), e);
                }

            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    final Product p = (Product) txbProducts.getTag();

                    if (p == null) return;

                    long _PartnerID = Long.parseLong(txbClients.getContentDescription().toString());

                    if (_PartnerID == 0L) return;

                    if (spPackage.getContentDescription() == null || Double.parseDouble(spPackage.getContentDescription().toString()) == 0) {
                        Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.Notification_PackageMissing), 2);
                        return;
                    }

                    if (!Common.isNumeric(txbQuantity.getText().toString()) || !Common.isNumeric(txbPrice.getText().toString())) {
                        Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.QuantityNotAvailable), 2);
                        return;
                    }

                    Double pakovanje = Double.parseDouble(spPackage.getContentDescription().toString());
                    Double Quantity = Double.parseDouble(txbQuantity.getText().toString());
                    Double Price = Double.parseDouble(txbPrice.getText().toString());

                    JSONObject item = new JSONObject();
                    item.put("ArtikalID", p.ArtikalID);
                    item.put("ProductID", p.ProductID);
                    item.put("Code", p.Code);
                    item.put("Pakovanje", pakovanje);
                    item.put("ProductName", p.Name);
                    item.put("Quantity", Quantity);
                    item.put("Note","");
                    item.put("Price",Price);

                    jsonObject.getJSONArray("items").put(item);

                    txbProducts.setTag(null);
                    txbProducts.setText("");
                    txbQuantity.setText("");
                    txbPrice.setText("");
                    spPackage.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, new String[] {}));
                    spPackage.setContentDescription("0");

                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(txbPrice.getWindowToken(), 0);

                    txbProducts.requestFocus();

                    bindData();


                }
                catch (Exception ex) {
                    wurthMB.AddError("OrsyPriceFragment", ex.getMessage(), ex);
                }
            }
        });

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    jsonObject.put("PartnerID", Long.parseLong(txbClients.getContentDescription().toString()));
                    jsonObject.put("Note", txbNote.getText().toString());
                    jsonObject.put("UserID", wurthMB.getUser().UserID);
                    jsonObject.put("Date", System.currentTimeMillis());

                    if (jsonObject == null || jsonObject.getLong("PartnerID") == 0 || jsonObject.getJSONArray("items") == null || jsonObject.getJSONArray("items").length() == 0) {
                        return;
                    }

                    Temp_Acquisition temp = new Temp_Acquisition();
                    temp.AccountID = wurthMB.getUser().AccountID;
                    temp.UserID = wurthMB.getUser().UserID;
                    temp.OptionID = 41;
                    temp.ID = 0;
                    temp.Sync = 0;
                    temp.DOE = System.currentTimeMillis();
                    temp.jsonObj = jsonObject.toString();

                    if (DL_Temp.AddOrUpdate(temp) > 0 ) {
                        Notifications.showNotification(getActivity(), "", getString(R.string.Notification_Saved), 0);
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    }
                    else {
                        Notifications.showNotification(getActivity(), "", getString(R.string.SystemError), 1);
                    }
                } catch (Exception e) {
                    wurthMB.AddError("OrsyPriceFragment", e.getMessage(), e);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        bindData();
    }

    public void bindData() {
        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) new LongTask().execute();
            else new LongTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        catch (Exception ex) {

        }
    }

    private class LongTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            try {
                if (getView() != null) getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
            }
            catch (Exception e) {
                wurthMB.AddError("OrsyPriceFragment", e.getMessage(), e);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
            }
            catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            try {
                if (getView() != null) {

                    int index = mList.getFirstVisiblePosition();
                    View v = mList.getChildAt(0);
                    int top = (v == null) ? 0 : v.getTop();

                    mAdapter = new OrsyPriceAdapter(getActivity(), R.layout.list_row_product, jsonObject.getJSONArray("items"), mFragment);
                    mList.setAdapter(mAdapter);

                    mList.setSelectionFromTop(index, top);

                    getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
                }
            } catch (Exception ex) {
                wurthMB.AddError("Order Items", ex.getMessage(), ex);
            }
        }
    }

}
