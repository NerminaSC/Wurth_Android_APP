package ba.wurth.mb.Fragments.Orders;

import android.content.Intent;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;

import ba.wurth.mb.Activities.Products.ProductsActivity;
import ba.wurth.mb.Adapters.AutoCompleteProductsAdapter;
import ba.wurth.mb.Adapters.OrderProductItemsAdapter;
import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.OrderItem;
import ba.wurth.mb.Classes.Objects.Product;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class OrderItemsFragment extends Fragment {

    private TextView litTotal;
    private TextView litGrandTotal;
    private TextView litItems;
    private EditText txbQuantity;

    public AutoCompleteTextView txbProducts;

    private Button btnAdd;
    private Button btnAddProduct;
    private Spinner spPackage;

    private ListView mList;
    private Fragment mFragment;

    private OrderProductItemsAdapter mAdapter;
    private SimpleCursorAdapter mAdapterProducts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.order_items, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        litTotal = (TextView) getView().findViewById(R.id.litTotal);
        litItems = (TextView) getView().findViewById(R.id.litItems);
        litGrandTotal = (TextView) getView().findViewById(R.id.litGrandTotal);
        btnAdd = (Button) getView().findViewById(R.id.btnAdd);
        btnAddProduct = (Button) getView().findViewById(R.id.btnAddProduct);
        spPackage = (Spinner) getView().findViewById(R.id.spPackage);
        txbQuantity = (EditText) getView().findViewById(R.id.txbQuantity);

        mList = (ListView) getView().findViewById(R.id.list);
        txbProducts = (AutoCompleteTextView) getView().findViewById(R.id.txbProducts);

        getActivity().findViewById(R.id.llTotals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(),ProductsActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(),ProductsActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        });

        if (wurthMB.getOrder().Sync == 1 && (wurthMB.getOrder().OrderStatusID == 4 || wurthMB.getOrder().OrderStatusID == 5)) btnAdd.setVisibility(View.GONE);
        if (wurthMB.getOrder().Sync == 1 && (wurthMB.getOrder().OrderStatusID == 4 || wurthMB.getOrder().OrderStatusID == 5)) getView().findViewById(R.id.llAddProduct).setVisibility(View.GONE);

        mAdapterProducts = new AutoCompleteProductsAdapter(getActivity());

        mAdapterProducts.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                try {
                    if (txbProducts.getText().toString().length() < 4) return null;
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

                        spPackage.setAdapter(new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, new String[]{}));

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
                                p.Kod_Zbirne_Cjen_Razrade = _cur.getString(_cur.getColumnIndex("Kod_Zbirne_Cjen_Razrade"));
                                p.Zamjenski_Artikal = _cur.getInt(_cur.getColumnIndex("Zamjenski_Artikal"));
                                p.UnitsInStock = _cur.getInt(_cur.getColumnIndex("UnitsInStock"));

                                txbProducts.setText(p.Code);
                            }
                            _cur.close();
                        }

                        if(p.Status_Artikla == 2){
                            Notifications.showNotification(getActivity(), "", "Artikal je izbačen", 2);
                        }else if (p.Status_Artikla != 0 && p.Status_Artikla != 1 && p.Status_Artikla != 5) {
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
                catch (Exception ex) {

                }

            }
        });

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    final Product p = (Product) txbProducts.getTag();

                    if (p.ProductID == 0L || p.ArtikalID == 0L) {
                        return;
                    }


                    if (spPackage.getContentDescription() == null || Double.parseDouble(spPackage.getContentDescription().toString()) == 0) {
                        Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.Notification_PackageMissing), 2);
                        return;
                    }

                    java.util.Iterator<ba.wurth.mb.Classes.Objects.OrderItem> itr = wurthMB.getOrder().items.iterator();
                    while (itr.hasNext()) {
                        ba.wurth.mb.Classes.Objects.OrderItem e = itr.next();
                        if (e.ProductID == p.ProductID /*&& e.Pakovanje == Double.parseDouble(spPackage.getContentDescription().toString())*/) {
                            Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.Notification_ProductCanNotBeAdded), 2);
                            return;
                        }
                    }

                    /*** STATUS = 5 ***/
                    if (p.Status_Artikla == 5) {

                        Integer unit_in_stock = Integer.parseInt(new wurthMB.GET_LiveStatus(p.ArtikalID).execute().get());

                        if(Double.parseDouble(txbQuantity.getText().toString()) > unit_in_stock){

                            if(p.Zamjenski_Artikal > 0){
                                p.ArtikalID = p.Zamjenski_Artikal;
                                p.Zamjenski_Artikal = 0;

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
                                        p.Kod_Zbirne_Cjen_Razrade = _cur.getString(_cur.getColumnIndex("Kod_Zbirne_Cjen_Razrade"));
                                        p.Zamjenski_Artikal = _cur.getInt(_cur.getColumnIndex("Zamjenski_Artikal"));
                                        p.UnitsInStock = _cur.getInt(_cur.getColumnIndex("UnitsInStock"));

                                        txbProducts.setText(p.Code);
                                    }
                                    _cur.close();
                                }
                            }else {
                                Notifications.showNotification(getActivity(), "", "Artikal je izbačen", 2);
                                return;
                            }
                        }
                    }

                    OrderItem tempOrderItem =  new OrderItem(){{
                        ArtikalID = p.ArtikalID;
                        ProductID = p.ProductID;
                        Grupa_Artikla = p.Grupa_Artikla;
                        UserDiscountPercentage = 0D;
                        ClientDiscountPercentage = 0D;
                        Pakovanje = Double.parseDouble(spPackage.getContentDescription().toString());
                        ProductName = p.Name;
                        Quantity = Double.parseDouble(txbQuantity.getText().toString());
                        Kod_Zbirne_Cjen_Razrade = p.Kod_Zbirne_Cjen_Razrade;
                        Note = "";
                    }};

                    tempOrderItem = wurthMB.getOrder().setDiscount(tempOrderItem);

                     if (tempOrderItem.Price_WS == 0) {
                        Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.Notification_ProductNoPrioe), 2);
                        return;
                    }

                    wurthMB.getOrder().items.add(tempOrderItem);
                    wurthMB.getOrder().CalculateTotal();
                    wurthMB.setOrder(wurthMB.getOrder());

                    bindTotals();

                    txbProducts.setTag(null);
                    txbProducts.setText("");
                    txbQuantity.setText("");
                    spPackage.setAdapter(new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, new String[]{}));
                    spPackage.setContentDescription("0");

                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(txbQuantity.getWindowToken(), 0);

                    txbProducts.requestFocus();

                }
                catch (Exception ex) {

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

    public void bindTotals() {
        try {
            if(wurthMB.getOrder().client != null) {
                litItems.setText(getString(R.string.Items) + ": " + wurthMB.getOrder().items.size());
                litTotal.setText(CustomNumberFormat.GenerateFormatCurrency(wurthMB.getOrder().Total));
                litGrandTotal.setText(CustomNumberFormat.GenerateFormatCurrency(wurthMB.getOrder().GrandTotal));
            }
            else {
                litItems.setText("");
                litTotal.setText("");
                litGrandTotal.setText("");
            }
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
            catch (Exception ex) {
                wurthMB.AddError("Order Items", ex.getMessage(), ex);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(300);
                wurthMB.getOrder().CalculateTotal();
            }
            catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            try {
                if (getView() != null && isAdded()) {

                    int index = mList.getFirstVisiblePosition();
                    View v = mList.getChildAt(0);
                    int top = (v == null) ? 0 : v.getTop();

                    mAdapter = new OrderProductItemsAdapter(getActivity(),R.layout.list_row_product, wurthMB.getOrder().items, mFragment);
                    mList.setAdapter(mAdapter);

                    mList.setSelectionFromTop(index, top);

                    getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);

                    bindTotals();
                }
            } catch (Exception ex) {
                wurthMB.AddError("Order Items", ex.getMessage(), ex);
            }
        }
    }

}
