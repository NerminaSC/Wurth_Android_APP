package ba.wurth.mb.Fragments.Products;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

import ba.wurth.mb.Activities.Products.ProductActivity;
import ba.wurth.mb.Adapters.CatalogProductAdapter;
import ba.wurth.mb.Classes.CategoryProductItem;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class ProductCatalogFragment extends Fragment {

    private GridView gridView;
    private CatalogProductAdapter catalogAdapter;
    private ArrayList<CategoryProductItem> data;

    public Long ProductID;
    public Long ArtikalID;
    public int MODE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = new ArrayList<CategoryProductItem>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.catalog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridView = (GridView) getView().findViewById(R.id.gridView);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                try {
                    switch (Integer.parseInt(data.get(i).getAdditional())) {

                        case 1:
                            ((ProductActivity) getActivity()).ArtikalID = data.get(i).getArtikalID();
                            ((ProductActivity) getActivity()).ProductID = data.get(i).getProductID();
                            ((ProductActivity) getActivity()).bindData();
                            break;

                        case 2:
                            getActivity().setResult(0, new Intent().putExtra("AssociationType", data.get(i).getAdditional()).putExtra("ArtikalID", data.get(i).getArtikalID()).putExtra("ProductID", data.get(i).getProductID()).putExtra("CategoryID", 0L));
                            getActivity().finish();
                            break;

                        case 3:
                            Long CategoryID = DL_Wurth.GET_ProductCategoryID(data.get(i).getArtikalID());
                            getActivity().setResult(0, new Intent().putExtra("AssociationType", data.get(i).getAdditional()).putExtra("ProductID", 0L).putExtra("ArtikalID", 0L).putExtra("CategoryID", CategoryID));
                            getActivity().finish();
                            break;

                        default:
                            ((ProductActivity) getActivity()).ArtikalID = data.get(i).getArtikalID();
                            ((ProductActivity) getActivity()).ProductID = data.get(i).getProductID();
                            ((ProductActivity) getActivity()).bindData();
                            break;
                    }
                }
                catch (Exception ex) {

                }
            }
        });

        bindData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void bindData() {
        try {

            new LongTask().execute();
        }
        catch (Exception ex) {

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
            }
            catch ( Exception ex) {

            }
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Cursor cur = null;

                switch (MODE) {
                    case 0: //Related
                        cur = DL_Wurth.GET_Related(ArtikalID);
                        break;
                    case 1: //Similar
                        cur = DL_Wurth.GET_Similar(ArtikalID);
                        break;
                    case 2: //Safety
                        cur = DL_Wurth.GET_Safety(ArtikalID);
                        break;
                    default:
                        break;
                }

                if (cur != null) {
                    while (cur.moveToNext()) {

                        String Name;
                        String Code;
                        String Url;

                        switch (cur.getInt(cur.getColumnIndex("NivoPovezanosti"))) {

                            case 1:
                                Name = cur.getString(cur.getColumnIndex("Naziv")) + ", " + cur.getString(5);
                                Code = cur.getString(2);
                                Url = cur.getString(3);
                                break;

                            case 2:
                                if (!cur.isNull(cur.getColumnIndex("Zbirni_Naziv")) && !cur.getString(cur.getColumnIndex("Zbirni_Naziv")).equals("")) Name = cur.getString(cur.getColumnIndex("Zbirni_Naziv"));
                                else Name = cur.getString(cur.getColumnIndex("Naziv")) + ", " + cur.getString(5);
                                Code = cur.getString(2);
                                Url = cur.getString(3);
                                break;

                            case 3:
                                Name = cur.getString(cur.getColumnIndex("Grupni_Naziv"));
                                Code = cur.getString(cur.getColumnIndex("Grupni_Naziv"));
                                Url = cur.getString(cur.getColumnIndex("Grupna_Slika"));
                                break;

                            default:
                                if (!cur.isNull(4) && !cur.isNull(5)) Name = cur.getString(4) + ", " + cur.getString(5);
                                else Name = cur.getString(1);
                                Code = cur.getString(2);
                                Url = cur.getString(3);
                                break;

                        }

                        data.add(new CategoryProductItem(0L, cur.getLong(0), Name, Code, Url, cur.getString(6)));
                    }
                    cur.close();
                }
                Thread.sleep(300);
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
                    catalogAdapter = new CatalogProductAdapter(getActivity(), R.layout.catalog_item_product, data);
                    gridView.setAdapter(catalogAdapter);
                    getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
                }
            } catch (Exception e) {
                wurthMB.AddError("Product list", e.getMessage(), e);
            }
        }
    }
}
