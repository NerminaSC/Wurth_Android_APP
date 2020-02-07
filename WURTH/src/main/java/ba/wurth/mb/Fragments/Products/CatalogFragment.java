package ba.wurth.mb.Fragments.Products;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;

import ba.wurth.mb.Activities.Products.ProductsActivity;
import ba.wurth.mb.Adapters.CatalogAdapter;
import ba.wurth.mb.Classes.CategoryItem;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class CatalogFragment extends Fragment {

    private GridView gridView;
    private TextView litTitle;
    private CatalogAdapter catalogAdapter;
    public ArrayList<CategoryItem> CategoryArray;
    private ArrayList<CategoryItem> data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CategoryArray = new ArrayList<CategoryItem>();
        data = new ArrayList<CategoryItem>();
        CategoryArray.add(new CategoryItem(1L, 0L, "Kompletan katalog", "", ""));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.catalog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridView = (GridView) getView().findViewById(R.id.gridView);
        litTitle = (TextView) getView().findViewById(R.id.litTitle);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {

                    Cursor cur_product =  DL_Wurth.GET_Products("", data.get(i).getCategoryID(), 0L);
                    Cursor cur_category = DL_Wurth.GET_Categories(data.get(i).getCategoryID(), "");

                    if (cur_product.getCount() > 0) {
                        ((ProductsActivity) getActivity()).setProductListTab(data.get(i).getCategoryID(), 0L, "", true, true);
                    }

                    if(cur_category.getCount() > 0){
                        CategoryArray.add((data.get(i)));
                        new LongTask().execute();
                    }

                  /*  Cursor cur = DL_Wurth.GET_Categories(data.get(i).getCategoryID(), "");

                    if (cur.getCount() == 0) {
                        ((ProductsActivity) getActivity()).setProductListTab(data.get(i).getCategoryID(), 0L, "", true, true);
                    } else {
                        CategoryArray.add((data.get(i)));
                       // ((ProductsActivity) getActivity()).setProductListTab(data.get(i).getCategoryID(), 0L, "", true, false);
                        new LongTask().execute();
                    }
*/
                }catch (Exception e){

                }
            }
        });

        if (getActivity().getIntent().getExtras() != null && getActivity().getIntent().hasExtra("ACTION") && getActivity().getIntent().getIntExtra("ACTION", 0) == 0) {
            Long CategoryID = DL_Wurth.GET_ProductCategoryID(getActivity().getIntent().getLongExtra("ArtikalID", 0L));

            int i = 0;
            CategoryArray.clear();

            while (CategoryID != 0L && i < 10) {
                Cursor cur = DL_Wurth.GET_ProductCategory(CategoryID);
                if (cur != null) {
                    if (cur.getCount() > 0 && cur.moveToFirst()) {
                        CategoryID = cur.getLong(cur.getColumnIndex("RoditeljID"));

                        if (i > 0) {
                            CategoryItem item = new CategoryItem(cur.getLong(cur.getColumnIndex("ID")), cur.getLong(cur.getColumnIndex("RoditeljID")), cur.getString(cur.getColumnIndex("Naziv")), cur.getString(cur.getColumnIndex("Sifra")), cur.getString(cur.getColumnIndex("Slika")));
                            CategoryArray.add(item);
                        }
                    }
                    cur.close();
                    i++;
                }
            }
            Collections.reverse(CategoryArray);
        }

        bindData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void bindData() {
        try {
            new LongTask().execute();
        } catch (Exception ex) {

        }
    }

    private class LongTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            try {
                if (getView() != null) {
                    getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
                }
                data.clear();
            } catch (Exception ex) {

            }
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Cursor cur = DL_Wurth.GET_Categories(CategoryArray.get(CategoryArray.size() - 1).getCategoryID(), "");
                if (cur != null) {
                    while (cur.moveToNext()) {
                        data.add(new CategoryItem(cur.getLong(0), cur.getLong(1), cur.getString(2), cur.getString(3), cur.getString(4)));
                    }
                    cur.close();
                }
            } catch (Exception e) {
                wurthMB.AddError("Product list", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {

            try {
                if (getView() != null) {
                    String title = "";
                    for (CategoryItem item : CategoryArray) {
                        if (!title.equals("")) title += " - ";
                        title += item.getName();
                    }
                    litTitle.setText(title);

                    catalogAdapter = new CatalogAdapter(getActivity(), R.layout.catalog_item, data);
                    gridView.setAdapter(catalogAdapter);
                    getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
                }
            } catch (Exception e) {
                wurthMB.AddError("Product list", e.getMessage(), e);
            }
        }
    }
}
