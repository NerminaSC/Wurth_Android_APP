package ba.wurth.mb.Fragments.Products;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;

import ba.wurth.mb.Activities.Products.ProductsActivity;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Products.DL_Products;
import ba.wurth.mb.R;

public class ProductsCategoryListFragment extends Fragment {

    private EditText txbSearch;
    private ImageButton btnClear;
    private Cursor cursor;
    private SimpleCursorAdapter cursorAdapter;
    private ListView list;
    private Fragment mFragment;

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
            }
        });

        if (getArguments() != null && getArguments().getString("searchWord") != null) txbSearch.setText(getArguments().getString("searchWord"));
        if (getActivity().getIntent().hasExtra("searchWord")) txbSearch.setText(getActivity().getIntent().getStringExtra("searchWord"));

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

        new LongTask().execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.product_categories_list, container, false);
    }

    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try
            {
                if (cursorAdapter == null) return;

                cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                    public Cursor runQuery(CharSequence _constraint) {
                        cursor = DL_Products.GetProductCategories(txbSearch.getText().toString());
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

    @Override
    public void onResume() {
        super.onResume();
    }

    private class LongTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            try {
                if (getView() != null) {
                    getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
                }
            }
            catch (Exception ex) {

            }
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Thread.sleep(300);
                cursor = DL_Products.GetProductCategories(txbSearch.getText().toString());
            }
            catch (Exception e) {
                wurthMB.AddError("Product Category list", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {

            try {
                if (getView() != null) {
                    String[] from = new String[] { "Name", "ProductCount" };
                    int[] to = new int[] { R.id.litTitle, R.id.litTotal };

                    cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_row_product_category, cursor, from, to);
                    list.setAdapter(cursorAdapter);

                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Long CategoryID = cursor.getLong(cursor.getColumnIndex("CategoryID"));
                            if (getActivity() instanceof ProductsActivity) {
                                ((ProductsActivity) getActivity()).setProductListTab(CategoryID, 0L, txbSearch.getText().toString(), true);
                            }
                        }
                    });

                    getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
                }
            } catch (Exception e) {

            }
        }
    }
}
