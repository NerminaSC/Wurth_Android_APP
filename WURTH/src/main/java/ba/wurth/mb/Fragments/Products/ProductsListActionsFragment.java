package ba.wurth.mb.Fragments.Products;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import ba.wurth.mb.Activities.Orders.OrderActivity;
import ba.wurth.mb.Adapters.ProductsAdapter;
import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.R;

public class ProductsListActionsFragment extends DialogFragment {

    private EditText txbSearch;
    private ImageButton btnClear;
    private Cursor cursor;
    private ProductsAdapter cursorAdapter;
    private ListView list;
    private TextView litTotal;
    private TextView litItems;
    private Button btnOrder;
    private Fragment mFragment;

    public Long ProductID = 0L;
    public Long ArtikalID = 0L;
    public Long CategoryID = 0L;
    public Long ClientID = 0L;
    public String Kod_Zbirnog_Naziva = "";
    public String Sifra_Grupe = "";
    public String searchWord = "";


    public ProductsListActionsFragment() {

    }

    public static ProductsListActionsFragment newInstance() {
        return new ProductsListActionsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.product_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFragment = this;

        list = (ListView) getView().findViewById(R.id.list);
        txbSearch = (EditText) getView().findViewById(R.id.txbSearch);
        btnClear = (ImageButton) getView().findViewById(R.id.btnClear);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txbSearch.setText("");
                searchWord = "";
                CategoryID = 0L;
                ProductID = 0L;
            }
        });

        litTotal = (TextView) getView().findViewById(R.id.litTotal);
        litItems = (TextView) getView().findViewById(R.id.litItems);

        btnOrder = (Button) getView().findViewById(R.id.btnOrder);

        txbSearch.addTextChangedListener(filterTextWatcher);
        txbSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);

        txbSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(txbSearch.getWindowToken(), 0);
                }
            }
        });

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), OrderActivity.class);
                i.putExtra("ACTION", 1);
                startActivity(i);
                getActivity().finish();
            }
        });

        for (View touchable : (getView().findViewById(R.id.llNumbers)).getTouchables()) touchable.setOnClickListener(customKeyboardHandler);

        if (getArguments() != null && getArguments().getString("searchWord") != null) {
            searchWord = getArguments().getString("searchWord");
        }

        if (getArguments() != null && getArguments().getString("Kod_Zbirnog_Naziva") != null) {
            Kod_Zbirnog_Naziva = getArguments().getString("Kod_Zbirnog_Naziva");
        }

        if (getArguments() != null && getArguments().getString("Sifra_Grupe") != null) {
            Sifra_Grupe = getArguments().getString("Sifra_Grupe");
        }

        if (getArguments() != null && getArguments().getLong("ProductID", 0L) > 0) {
            ProductID = getArguments().getLong("ProductID", 0L);
        }

        if (getArguments() != null && getArguments().getLong("CategoryID", 0L) > 0) {
            CategoryID = getArguments().getLong("CategoryID", 0L);
        }

        if (getArguments() != null && getArguments().getLong("ClientID", 0L) > 0) {
            ClientID = getArguments().getLong("ClientID", 0L);
        }

        txbSearch.setText(searchWord);

    }

    View.OnClickListener customKeyboardHandler = new View.OnClickListener() {
        public void onClick(View v) {

            if (getDialog().getCurrentFocus() == null || getDialog().getCurrentFocus().getId() != R.id.txbQuantity) return;

            EditText curr = (EditText) getDialog().getCurrentFocus();

            if( ((Button)v).getText().toString().equals(">") ){

                View nextField = curr.focusSearch(View.FOCUS_DOWN);

                if (nextField != null && nextField.getId() == R.id.txbQuantity) nextField.requestFocus();

                else {
                    if (!((list.getLastVisiblePosition() == list.getAdapter().getCount() - 1 && list.getChildAt(list.getChildCount() - 1).getBottom() <= list.getHeight()))) {
                        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                        if (currentapiVersion >= android.os.Build.VERSION_CODES.FROYO){
                            list.smoothScrollToPosition(list.getLastVisiblePosition() + 1);
                            nextField = curr.focusSearch(View.FOCUS_DOWN);
                            if (nextField != null && nextField.getId() == R.id.txbQuantity) nextField.requestFocus();
                        }
                    }
                }
            }

            else if( ((Button)v).getText().toString().equals("<") ){

                View nextField = curr.focusSearch(View.FOCUS_UP);

                if (nextField != null && nextField.getId() == R.id.txbQuantity) nextField.requestFocus();

                else {
                    if (list.getFirstVisiblePosition() > 0) {
                        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                        if (currentapiVersion >= android.os.Build.VERSION_CODES.FROYO){
                            list.smoothScrollToPosition(list.getFirstVisiblePosition() - 1);
                            nextField = curr.focusSearch(View.FOCUS_UP);
                            if (nextField != null && nextField.getId() == R.id.txbQuantity) nextField.requestFocus();
                        }
                    }
                }
            }

            else if( ((Button)v).getText().toString().equals("x") ){
                String newValue = curr.getText().toString().substring(0, curr.getText().toString().length() - 1);

                if (newValue.equals("")) newValue = "0";

                if (ba.wurth.mb.Classes.Common.isNumeric(newValue)) curr.setText(newValue);
            }

            else {
                String newValue;

                if (curr.getText().toString().equals("0") || curr.getText().toString().equals("0.00")){
                    if (((Button) v).getText().toString().equals(".")) newValue = "0" + ((Button) v).getText().toString();
                    else newValue = ((Button) v).getText().toString();
                }
                else {
                    newValue = curr.getText().toString() + ((Button) v).getText().toString();
                }

                if (ba.wurth.mb.Classes.Common.isNumeric(newValue)) curr.setText(newValue);
            }
        }
    };

    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
            searchWord = txbSearch.getText().toString();
            handler.removeMessages(TRIGGER_SEARCH);
            handler.sendEmptyMessageDelayed(TRIGGER_SEARCH, SEARCH_TRIGGER_DELAY_IN_MS);
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        public void onTextChanged(CharSequence s, int start, int before, int count) { }
    };

    public void bindData() {
        try {
            if (wurthMB.getOrder() != null) {
                litItems.setText(getString(R.string.Items) + ": " + wurthMB.getOrder().items.size());
                if (wurthMB.getOrder() != null && wurthMB.getOrder().ClientID > 0L) litTotal.setText(CustomNumberFormat.GenerateFormatCurrency(wurthMB.getOrder().Total));
            }
            else {
                litItems.setText(getString(R.string.Items) + ": 0");
                litTotal.setText(CustomNumberFormat.GenerateFormatCurrency(0D));
            }
        }
        catch (Exception ex) {

        }
    }

    private class LongTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            try {
                //if (CategoryID == 0L) cancel(true);
                if (getView() != null) {
                    getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
                }
            }
            catch ( Exception ex) {

            }
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                cursor = wurthMB.dbHelper.getDB().rawQuery("SELECT Products._id, ARTIKLI.ID, ARTIKLI.MjernaJedinica, ARTIKLI.Naziv, ARTIKLI.Kod_Zbirne_Cjen_Razrade, " +
                        " ARTIKLI.Atribut, ARTIKLI.sifra, ARTIKLI.Zbirni_Naziv, ARTIKLI.Stanje_Zaliha, ARTIKLI.Predefinisana_Dostupnost, ARTIKLI.Datum_Prijema, ARTIKLI.Narucena_Kolicina, ARTIKLI.Predefinisana_Dostupnost, ARTIKLI.Zbirni_Naziv, " +
                        " ARTIKLI.Status_Artikla, ARTIKLI.Status_Prezentacije_Artikla, ARTIKLI.MjernaJedinica, " +
                        " Products.ProductID, ARTIKAL_SLIKE.Velika, ARTIKAL_GRUPE.NAZIV AS GrupniNaziv   " +

                        " FROM ARTIKLI" +

                        " INNER JOIN Products On ARTIKLI.ID = Products._productid " +
                        " LEFT JOIN ARTIKAL_GRUPE On ARTIKLI.Grupa_Artikla = ARTIKAL_GRUPE.ID " +
                        " LEFT Join ARTIKAL_SLIKE On ARTIKLI.ID = ARTIKAL_SLIKE.ArtikalID " +

                        " WHERE (Products.Active = 1 OR Products.Active IS NULL) " +
                        " AND ARTIKLI.Status_Artikla <> 0 " +

                        (!Kod_Zbirnog_Naziva.equals("") ? " AND ARTIKLI.Kod_Zbirnog_Naziva = '" + Kod_Zbirnog_Naziva + "' " : "") +
                        (!Sifra_Grupe.equals("") ? " AND ARTIKAL_GRUPE.sifra = '" + Sifra_Grupe + "' " : "") +

                        " ORDER BY ARTIKLI.Sifra", null);

                cursor.getCount();

            }
            catch (Exception e) {
                wurthMB.AddError("Product list", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {

            try {
                if (getView() != null) {
                    cursorAdapter = new ProductsAdapter(getActivity(), cursor, mFragment);
                    list.setAdapter(cursorAdapter);
                    getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
                }
            } catch (Exception e) {
                wurthMB.AddError("Product list", e.getMessage(), e);
            }
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
