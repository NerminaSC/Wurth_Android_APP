package ba.wurth.mb.Fragments.Products;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import ba.wurth.mb.Activities.Orders.OrderActivity;
import ba.wurth.mb.Adapters.ProductsAdapter;
import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Products.DL_Products;
import ba.wurth.mb.R;

public class ProductsListFragment extends Fragment {

    private EditText txbSearch;
    private ImageButton btnClear;
    private Cursor cursor;
    private ProductsAdapter cursorAdapter;
    private ListView list;
    private TextView litTotal;
    private TextView litItems;
    private TextView litGrandTotal;
    private Button btnOrder;
    private Fragment mFragment;

    public Long ProductID = 0L;
    public Long ArtikalID = 0L;
    public Long CategoryID = 0L;
    public int AssociationType = 0;
    public String searchWord = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        litGrandTotal = (TextView) getView().findViewById(R.id.litGrandTotal);
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

        getActivity().findViewById(R.id.llTotals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), OrderActivity.class);
                i.putExtra("ACTION", 1);
                startActivity(i);
                getActivity().finish();
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

        for(View touchable : (getView().findViewById(R.id.llNumbers)).getTouchables()) touchable.setOnClickListener(customKeyboardHandler);

        if (getArguments() != null && getArguments().getString("searchWord") != null) {
            searchWord = getArguments().getString("searchWord");
        }

        if (getArguments() != null && getArguments().getLong("ProductID", 0L) > 0) {
            ProductID = getArguments().getLong("ProductID", 0L);
        }

        if (getArguments() != null && getArguments().getLong("Grupa_Artikla", 0L) > 0) {
            CategoryID = getArguments().getLong("Grupa_Artikla", 0L);
        }

        if( getArguments() != null)  getArguments().clear();

        txbSearch.setText(searchWord);
    }

    View.OnClickListener customKeyboardHandler = new View.OnClickListener() {
        public void onClick(View v) {

            if (getActivity().getCurrentFocus() == null || getActivity().getCurrentFocus().getId() != R.id.txbQuantity) return;

            EditText curr = (EditText) getActivity().getCurrentFocus();

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

        public void afterTextChanged(Editable s) { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try {

                if (cursorAdapter == null) return;

                searchWord = txbSearch.getText().toString();

                cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                    public Cursor runQuery(CharSequence _constraint) {
                        try {

                            switch (AssociationType) {
                                case 1:
                                    cursor = DL_Products.GetProductsWithPrices(searchWord, CategoryID, ProductID);
                                    break;

                                case 2:
                                    Cursor c = DL_Wurth.GET_Product(ArtikalID);
                                    if (c != null) {
                                        String CategoryCode = "";
                                        if (c.getCount() > 0 && c.moveToFirst()) {
                                            CategoryCode = c.getString(c.getColumnIndex("Kod_Zbirnog_Naziva"));
                                        }
                                        c.close();
                                        cursor = DL_Products.GetProductsWithPrices_ByCode(searchWord, CategoryID, ProductID, CategoryCode);
                                    }
                                    break;

                                case 3:
                                    cursor = DL_Products.GetProductsWithPrices(searchWord, CategoryID, ProductID);
                                    break;

                                default:
                                    cursor = DL_Products.GetProductsWithPrices(searchWord, CategoryID, ProductID);
                                    break;
                            }
                        }
                        catch (Exception ex) {

                        }
                        return cursor;
                    }
                });

                cursorAdapter.notifyDataSetChanged();

                cursorAdapter.getFilter().filter(s.toString(), new Filter.FilterListener() {
                    public void onFilterComplete(int count) {

                    }
                });
            }
            catch (Exception e) {

            }
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void bindList() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) new LongTask().execute();
        else new LongTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void bindData() {
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

                String CategoryCode = "";

                Thread.sleep(300);

                switch (AssociationType) {
                    case 1:
                        cursor = DL_Wurth.GET_Products(searchWord, CategoryID, ProductID);
                        break;

                    case 2:
                        Cursor c = DL_Wurth.GET_Product(ArtikalID);
                        if (c != null) {
                            if (c.getCount() > 0 && c.moveToFirst()) {
                                CategoryCode = c.getString(c.getColumnIndex("Kod_Zbirnog_Naziva"));
                            }
                            c.close();
                            cursor = DL_Wurth.GET_Products_ByCategoryCode(CategoryCode);
                        }
                        break;

                    case 3:
                        cursor = DL_Wurth.GET_Products(searchWord, CategoryID, ProductID);
                        break;

                    default:
                        if (CategoryID > 0) cursor = DL_Wurth.GET_Products(searchWord, CategoryID, ProductID);
                        break;
                }
                AssociationType = 0;
                ArtikalID = 0L;

            }
            catch (Exception e) {
                wurthMB.AddError("Product list", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {

            try {
                if (getView() != null && isAdded() && cursor != null) {

                    int index = list.getFirstVisiblePosition();
                    View v = list.getChildAt(0);
                    int top = (v == null) ? 0 : v.getTop();

                    cursorAdapter = new ProductsAdapter(getActivity(), cursor, mFragment);
                    list.setAdapter(cursorAdapter);

                    list.setSelectionFromTop(index, top);

                    getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
                }
            } catch (Exception e) {
                wurthMB.AddError("Product list", e.getMessage(), e);
            }
        }
    }
}
