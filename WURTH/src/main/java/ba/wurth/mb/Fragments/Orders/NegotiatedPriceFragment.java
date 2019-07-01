package ba.wurth.mb.Fragments.Orders;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;

import com.fourmob.datetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import ba.wurth.mb.Adapters.AutoCompleteClientsAdapter;
import ba.wurth.mb.Adapters.AutoCompleteProductsAdapter;
import ba.wurth.mb.Adapters.NegotiatedPriceAdapter;
import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.PricelistItem;
import ba.wurth.mb.Classes.Objects.Product;
import ba.wurth.mb.Classes.Objects.Temp_Acquisition;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Temp.DL_Temp;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class NegotiatedPriceFragment extends Fragment {

    private EditText txbQuantity_From;
    private EditText txbQuantity_To;
    private EditText txbNote;

    public AutoCompleteTextView txbClients;
    public AutoCompleteTextView txbProducts;

    private SimpleCursorAdapter mAdapterClients;


    private ImageButton btnClearClients;
    private Button btnPrice;
    private Button btnGenerate;

    private Spinner spPackage;

    private ListView mList;
    private Fragment mFragment;

    private NegotiatedPriceAdapter mAdapter;
    private SimpleCursorAdapter mAdapterProducts;

    TextView litDate;


    public JSONObject jsonObject;
    private java.text.Format dateFormatter = new java.text.SimpleDateFormat("dd.MM.yyyy");


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
        return inflater.inflate(R.layout.negotiated_price, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            txbClients = (AutoCompleteTextView) getView().findViewById(R.id.txbClients);
            btnClearClients = (ImageButton) getView().findViewById(R.id.btnClearClients);
            btnPrice = (Button) getView().findViewById(R.id.btnPrice);
            btnGenerate = (Button) getView().findViewById(R.id.btnGenerate);
            spPackage = (Spinner) getView().findViewById(R.id.spPackage);
            txbQuantity_From = (EditText) getView().findViewById(R.id.txbQuantity_From);
            txbQuantity_To = (EditText) getView().findViewById(R.id.txbQuantity_To);
            txbNote = (EditText) getView().findViewById(R.id.txbNote);

            litDate = (TextView) getView().findViewById(R.id.litDate);

            mList = (ListView) getView().findViewById(R.id.list);
            txbProducts = (AutoCompleteTextView) getView().findViewById(R.id.txbProducts);

            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            litDate.setContentDescription(Long.toString(calendar.getTimeInMillis()));
            litDate.setText(dateFormatter.format(calendar.getTimeInMillis()));


            litDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
                            Calendar c = Calendar.getInstance();
                            c.set(year, month, day, 0, 0, 0);
                            litDate.setText(dateFormatter.format(c.getTimeInMillis()));
                            litDate.setContentDescription(Long.toString(c.getTimeInMillis()));
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false).show(getFragmentManager(), "");
                }
            });

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
                                txbQuantity_From.requestFocus();
                            }
                        }
                    }
                    catch (Exception e) {
                        wurthMB.AddError("NegotiatedPrice", e.getMessage(), e);
                    }

                }
            });

            btnPrice.setOnClickListener(new View.OnClickListener() {
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

                        if (!Common.isNumeric(txbQuantity_From.getText().toString()) || !Common.isNumeric(txbQuantity_To.getText().toString()) || Double.parseDouble(txbQuantity_To.getText().toString()) == 0D || Double.parseDouble(txbQuantity_From.getText().toString()) == 0D || Double.parseDouble(txbQuantity_To.getText().toString()) < Double.parseDouble(txbQuantity_From.getText().toString())) {
                            Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.QuantityNotAvailable), 2);
                            return;
                        }

                        Double pakovanje = Double.parseDouble(spPackage.getContentDescription().toString());
                        final Integer Quantity_From = Integer.parseInt(txbQuantity_From.getText().toString());
                        final Integer Quantity_To = Integer.parseInt(txbQuantity_To.getText().toString());

                        LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        final Dialog ItemDialog = new Dialog(getActivity());
                        ItemDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                        ItemDialog.setContentView(R.layout.list_row_order_item_dialog);
                        ItemDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        ItemDialog.findViewById(R.id.btnUpdateOrder).setVisibility(View.GONE);

                        View extraView = null;
                        Double price = 0D;

                        final ArrayList<PricelistItem> items = DL_Wurth.GET_Pricelist(p.ArtikalID);

                        if (items.size() > 0) {

                            View tbl_Header = mInflater.inflate(R.layout.list_row_order_item_dialog_header, (LinearLayout) ItemDialog.findViewById(R.id.llTable), false);
                            ((LinearLayout) ItemDialog.findViewById(R.id.llTable)).addView(tbl_Header);

                            Double partnerKolicinaOD = 0D;

                            for (int x = 0; x < items.size(); x++) {

                                PricelistItem item = items.get(x);

                                String percentage;

                                View tbl = mInflater.inflate(R.layout.list_row_order_item_dialog_item, (LinearLayout) ItemDialog.findViewById(R.id.llTable), false);

                                final TextView litDiscountStart = ((TextView) tbl.findViewById(R.id.litDiscountStart));
                                final TextView litDiscountEnd = ((TextView) tbl.findViewById(R.id.litDiscountEnd));
                                final TextView txbDiscount = ((EditText) tbl.findViewById(R.id.txbDiscount));
                                final TextView litPrice = ((TextView) tbl.findViewById(R.id.litPrice));
                                TextView litStartDate = ((TextView) tbl.findViewById(R.id.litStartDate));
                                TextView litEndDate = ((TextView) tbl.findViewById(R.id.litEndDate));
                                TextView litPercentage = ((TextView) tbl.findViewById(R.id.litPercentage));

                                final Button btnPricelist = ((Button) tbl.findViewById(R.id.btnPricelist));

                                price = item.OsnovnaCijena;
                                Integer KljucCijene = item.KljucCijene;

                                if (price == 0D) return;

                                Double KolicinaOD = item.KolicinaOD;
                                Double KolicinaDO = item.KolicinaDo;

                                litDiscountStart.setText(Integer.toString(KolicinaOD.intValue()));
                                if (KolicinaDO > 0D) litDiscountEnd.setText(Integer.toString(KolicinaDO.intValue()));

                                txbDiscount.setText("0");

                                Double PopustDO = item.PopustDO;
                                Double PopustOD = item.PopustOD;

                                switch (wurthMB.getUser().AccessLevelID) {
                                    case 1: //Director
                                        PopustDO += item.DodatniPopust;
                                        break;
                                    case 2: //Manager
                                        PopustDO += item.DodatniPopust;
                                        break;
                                    case 5: //Sales Persons
                                    case 9: //Sales user (WEB)
                                        //txbDiscount.setEnabled(false);
                                        break;
                                    case 8: //Area sales manager
                                        break;
                                    default:break;
                                }

                                litDiscountStart.setTag(PopustOD);
                                litDiscountEnd.setTag(PopustDO);

                                txbDiscount.setText(Integer.toString(PopustOD.intValue()));

                                price = price - (price * PopustOD / 100);

                                litPrice.setText(CustomNumberFormat.GenerateFormatCurrency(price) + "/" + KljucCijene);

                                percentage = Integer.toString(((int) (PopustDO - PopustOD)));

                                txbDiscount.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
                                    @Override
                                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
                                    @Override
                                    public void afterTextChanged(Editable editable) {

                                        try {

                                            if (txbDiscount.getText().toString().equals("")) {
                                                btnPricelist.setEnabled(false);
                                                return;
                                            }

                                            Double _PopustOD = (Double) litDiscountStart.getTag();
                                            Double _PopustDO = (Double) litDiscountEnd.getTag();
                                            Double _Discount = Double.parseDouble(txbDiscount.getText().toString().replaceAll("[^0-9]", ""));

                                            if (_Discount >= _PopustOD && _Discount <= _PopustDO) {
                                                btnPricelist.setEnabled(true);
                                            }
                                            else {
                                                btnPricelist.setEnabled(false);
                                            }
                                        }
                                        catch (Exception e) {
                                            wurthMB.AddError("NegotiatedPrice", e.getMessage(), e);
                                        }
                                    }
                                });

                                btnPricelist.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        try {
                                            Double _Discount = Double.parseDouble(txbDiscount.getText().toString().replaceAll("[^0-9]", ""));

                                            JSONObject item = new JSONObject();
                                            item.put("ArtikalID", p.ArtikalID);
                                            item.put("ProductID", p.ProductID);
                                            item.put("Code", p.Code);
                                            item.put("Pakovanje", Double.parseDouble(spPackage.getContentDescription().toString()));
                                            item.put("ProductName", p.Name);
                                            item.put("Quantity_From", Quantity_From);
                                            item.put("Quantity_To", Quantity_To);
                                            item.put("Note","");
                                            item.put("Price","0");
                                            item.put("Discount", _Discount);

                                            boolean special = true;
                                            for (int x = 0; x < items.size(); x++) {
                                                PricelistItem _item = items.get(x);
                                                if (_item.KolicinaOD.intValue() == Quantity_From && (_item.KolicinaDo.intValue() ==  Quantity_To) && _item.PopustDO >= _Discount && _item.PopustOD <= _Discount) {
                                                    special = false;
                                                    break;
                                                }
                                            }

                                            item.put("Special", special);
                                            jsonObject.getJSONArray("items").put(item);

                                            txbProducts.setTag(null);
                                            txbProducts.setText("");
                                            txbQuantity_From.setText("");
                                            txbQuantity_To.setText("");
                                            spPackage.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, new String[] {}));
                                            spPackage.setContentDescription("0");

                                            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                                            inputManager.hideSoftInputFromWindow(txbQuantity_To.getWindowToken(), 0);

                                            txbProducts.requestFocus();

                                            bindData();
                                        } catch (JSONException e) {
                                            wurthMB.AddError("NegotiatedPrice", e.getMessage(), e);
                                        }
                                        ItemDialog.dismiss();
                                    }
                                });

                                if (item.PartnerID > 0) {
                                    partnerKolicinaOD = KolicinaOD;
                                    percentage += " (U)";
                                    //extraView = tbl;
                                    continue;
                                }

                                if (item.DatumOd > 0L) continue;
                                if (item.DatumDo > 0L) continue;
                                if (item.DatumOd > 0L && item.DatumDo > 0L) continue;

                                litPercentage.setText(percentage);
                                //if (partnerKolicinaOD == 0D || partnerKolicinaOD > KolicinaOD) ((LinearLayout) ItemDialog.findViewById(R.id.llTable)).addView(tbl);

                                if (KolicinaOD <= (Quantity_From * pakovanje) && (KolicinaDO > (Quantity_From * pakovanje) || KolicinaDO == 0D)) {
                                    tbl.setBackgroundColor(Color.parseColor("#ff9f9f"));
                                    txbDiscount.requestFocus();
                                }

                                ((LinearLayout) ItemDialog.findViewById(R.id.llTable)).addView(tbl);

                            }

                            if (extraView != null) ((LinearLayout) ItemDialog.findViewById(R.id.llTable)).addView(extraView);
                        }

                        ItemDialog.findViewById(R.id.btnUpdateOrder).setOnClickListener(new View.OnClickListener() {

                            public void onClick(View v) {
                                ItemDialog.dismiss();
                            }
                        });

                        ItemDialog.findViewById(R.id.btnCloseDialog).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                ItemDialog.dismiss();
                            }
                        });


                        ItemDialog.show();
                    }
                    catch (Exception ex) {
                        wurthMB.AddError("NegotiatedPrice", ex.getMessage(), ex);
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
                        jsonObject.put("Date", Long.parseLong(litDate.getContentDescription().toString()));

                        if (jsonObject == null || jsonObject.getLong("PartnerID") == 0 || jsonObject.getJSONArray("items") == null || jsonObject.getJSONArray("items").length() == 0) {
                            return;
                        }

                        Temp_Acquisition temp = new Temp_Acquisition();
                        temp.AccountID = wurthMB.getUser().AccountID;
                        temp.UserID = wurthMB.getUser().UserID;
                        temp.OptionID = 40;
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
                        wurthMB.AddError("NegotiatedPrice", e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            wurthMB.AddError("NegotiatedPrice", e.getMessage(), e);
        }
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
                wurthMB.AddError("NegotiatedPrice", e.getMessage(), e);
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

                    mAdapter = new NegotiatedPriceAdapter(getActivity(), R.layout.list_row_product, jsonObject.getJSONArray("items"), mFragment);
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
