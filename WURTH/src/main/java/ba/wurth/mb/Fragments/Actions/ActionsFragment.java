package ba.wurth.mb.Fragments.Actions;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

import ba.wurth.mb.Activities.Products.ProductActivity;
import ba.wurth.mb.Adapters.ViewPagerAdapter;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Order;
import ba.wurth.mb.Classes.VerticalViewPager;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Fragments.Products.ProductsListActionsFragment;
import ba.wurth.mb.R;

public class ActionsFragment extends Fragment {

    private Cursor cursor;

    public Long ProductID = 0L;
    public Long ArtikalID = 0L;
    public Long CategoryID = 0L;
    public Long ClientID = 0L;
    public Long DeliveryPlaceID = 0L;
    public String searchWord = "";

    GridView grid_images;
    Client client = null;

    ArrayList<ViewPagerAdapter.Items> ArtikalID_Array = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.action_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && getArguments().getLong("ClientID", 0L) > 0) ClientID = getArguments().getLong("ClientID", 0L);
        if (getArguments() != null && getArguments().getLong("DeliveryPlaceID", 0L) > 0) DeliveryPlaceID = getArguments().getLong("DeliveryPlaceID", 0L);
        if (getArguments() != null && getArguments().getLong("ProductID", 0L) > 0) ProductID = getArguments().getLong("ProductID", 0L);
        if (getArguments() != null && getArguments().getLong("ArtikalID", 0L) > 0) ArtikalID = getArguments().getLong("ArtikalID", 0L);

        grid_images = (GridView) getView().findViewById(R.id.grid_images);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) new LongTask().execute();
        else new LongTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


//    private void populateLayout(LinearLayout container, View[] views , Context mContext) {
//
//        try {
//
//            double size = 0;
//            LinearLayout newLL = null;
//
//            for (int i = 0 ; i < views.length ; i++ ) {
//
//                if (size == 0) {
//                    newLL = new LinearLayout(getActivity());
//                    newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400));
//                    newLL.setGravity(Gravity.CENTER);
//                    newLL.setOrientation(LinearLayout.HORIZONTAL);
//                }
//
//                switch (((ViewPagerAdapter.Items) views[i].getTag()).ImageSize) {
//
//                    case 100: //FULL SCREEN
//                        if (size > 0) {
//                            container.addView(newLL);
//                        }
//
//                        newLL = new LinearLayout(getActivity());
//                        newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400));
//                        newLL.setGravity(Gravity.CENTER);
//                        newLL.setOrientation(LinearLayout.HORIZONTAL);
//                        newLL.addView(views[i]);
//                        container.addView(newLL);
//
//                        size = 0;
//                        break;
//
//                    case 50: //HALF SCREEN
//                        if (size > 0.5) {
//                            container.addView(newLL);
//
//                            newLL = new LinearLayout(getActivity());
//                            newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 550));
//                            newLL.setGravity(Gravity.CENTER);
//                            newLL.setOrientation(LinearLayout.HORIZONTAL);
//                            newLL.addView(views[i]);
//                            size = 0.5;
//                        }
//                        else {
//                            newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 550));
//                            newLL.addView(views[i]);
//                            size += 0.5;
//                        }
//
//                        break;
//                    case 33: // 1/3
//                        if (size > 0.66) {
//                            container.addView(newLL);
//
//                            newLL = new LinearLayout(getActivity());
//                            newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 550));
//                            newLL.setGravity(Gravity.CENTER);
//                            newLL.setOrientation(LinearLayout.HORIZONTAL);
//                            newLL.addView(views[i]);
//                            size = 0.33;
//                        }
//                        else {
//                            newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 550));
//                            newLL.addView(views[i]);
//                            size += 0.33;
//                        }
//                        break;
//                    case 25: // 1/4
//                        if (size > 0.75) {
//                            container.addView(newLL);
//
//                            newLL = new LinearLayout(getActivity());
//                            newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400));
//                            newLL.setGravity(Gravity.CENTER);
//                            newLL.setOrientation(LinearLayout.HORIZONTAL);
//                            newLL.addView(views[i]);
//                            size = 0.25;
//                        }
//                        else {
//                            newLL.addView(views[i]);
//                            size += 0.25;
//                        }
//                        break;
//                    case 66: // 1/4
//                        if (size > 0.33) {
//                            container.addView(newLL);
//
//                            newLL = new LinearLayout(getActivity());
//                            newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 550));
//                            newLL.setGravity(Gravity.CENTER);
//                            newLL.setOrientation(LinearLayout.HORIZONTAL);
//                            newLL.addView(views[i]);
//                            size = 0.66;
//                        }
//                        else {
//                            newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 550));
//                            newLL.addView(views[i]);
//                            size += 0.66;
//                        }
//                        break;
//                    default:
//                        if (size > 0) {
//                            container.addView(newLL);
//                        }
//
//                        newLL = new LinearLayout(getActivity());
//                        newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400));
//                        newLL.setGravity(Gravity.CENTER);
//                        newLL.setOrientation(LinearLayout.HORIZONTAL);
//                        newLL.addView(views[i]);
//                        container.addView(newLL);
//
//                        size = 0;
//                        break;
//                }
//            }
//
//            if (newLL != null && size > 0) container.addView(newLL);
//
//        } catch (Exception e) {
//            wurthMB.AddError("Actions list populate views", e.getMessage(), e);
//        }
//    }

    private class LongTask extends AsyncTask<String, Void, Void> {


        @Override
        protected void onPreExecute() {
            try {
                ArtikalID_Array = new ArrayList<ViewPagerAdapter.Items>();
                client = null;
            }
            catch ( Exception ex) {

            }
        }

        @Override
        protected Void doInBackground(String... params) {
            try {

                //Thread.sleep(500);

                cursor = DL_Wurth.GET_Products_Actions(searchWord, ClientID, ProductID);

                if (cursor != null) {
                    int mIDAkcije = cursor.getColumnIndex("IDAkcije");
                    int mProductID = cursor.getColumnIndex("ProductID");
                    int mNaziv = cursor.getColumnIndex("Naziv");
                    int mDatumOd = cursor.getColumnIndex("DatumOd");
                    int mDatumDo = cursor.getColumnIndex("DatumDo");
                    int mKomentar = cursor.getColumnIndex("Komentar");
                    int mSlika = cursor.getColumnIndex("Slika");
                    int mPZNA = cursor.getColumnIndex("PZNA");
                    int mCatalogCode = cursor.getColumnIndex("CatalogCode");
                    int mImageSize = cursor.getColumnIndex("ImageSize");
                    int mPotencijalOd = cursor.getColumnIndex("PotencijalOd");
                    int mPotencijalDo = cursor.getColumnIndex("PotencijalDo");
                    int mBransa = cursor.getColumnIndex("Bransa");
                    int mIDArtikla = cursor.getColumnIndex("IDArtikla");


                    if(DeliveryPlaceID > 0) client = DL_Wurth.GET_DeliveryPlace(DeliveryPlaceID);
                    else client = DL_Wurth.GET_Client(ClientID);

                    while (cursor.moveToNext()) {

                        Boolean exists = false;

                        if (!cursor.isNull(mBransa) && !cursor.getString(mBransa).equals("")) {
                            Cursor _cur = wurthMB.dbHelper.getDB().rawQuery("SELECT * FROM PARTNER_BRANSE INNER JOIN CLIENTS ON PARTNER_BRANSE.PartnerID = CLIENTS._clientid WHERE CLIENTS.ClientID = " + ClientID, null);
                            while (_cur.moveToNext()) {
                                for (int i = 0; i < cursor.getString(mBransa).split(",").length; i++) {
                                    if (_cur.getString(_cur.getColumnIndex("Bransa")).substring(0, 1).equals(cursor.getString(mBransa).split(",")[i])) {
                                        exists = true;
                                    }
                                }
                            }
                            _cur.close();
                        } else exists = true;

                        if (!exists) continue;

                        if (client != null) {
                            if (!(cursor.getInt(mPotencijalOd) <= client.Potencijal && (cursor.getInt(mPotencijalDo) >= client.Potencijal || cursor.getInt(mPotencijalDo) == 0))) {
                                continue;
                            }
                        }

                        for (int i = 0; i < ArtikalID_Array.size(); i++) {
                            if (ArtikalID_Array.get(i).ArtikalID == cursor.getInt(mIDArtikla) && ArtikalID_Array.get(i).PZNA.equals(cursor.getString(mPZNA)) && ArtikalID_Array.get(i).CatalogCode.equals(cursor.getString(mCatalogCode)) && ArtikalID_Array.get(i).PotencijalDO <= cursor.getInt(mPotencijalDo)) {
                                ArtikalID_Array.get(i).slika = cursor.getString(mSlika);
                                exists = false;
                                break;
                            }
                        }

                        if (!exists) continue;

                        if (!cursor.isNull(mSlika) && !cursor.getString(mSlika).equals("")) {
                            ViewPagerAdapter.Items ArtikalItem = new ViewPagerAdapter.Items();
                            ArtikalItem.ArtikalID = cursor.getInt(mIDArtikla);
                            ArtikalItem.PotencijalOD = cursor.getInt(mPotencijalOd);
                            ArtikalItem.PotencijalDO = cursor.getInt(mPotencijalDo);
                            ArtikalItem.ImageSize = cursor.getInt(mImageSize);
                            ArtikalItem.ProductID = cursor.getInt(mProductID);
                            ArtikalItem.slika = cursor.getString(mSlika);
                            ArtikalItem.PZNA = cursor.getString(mPZNA);
                            ArtikalItem.CatalogCode = cursor.getString(mCatalogCode);
                            ArtikalID_Array.add(ArtikalItem);
                        }
                    }
                    cursor.close();
                }

            }
            catch (Exception e) {
                wurthMB.AddError("Actions list doInBackground", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {

            try {

                if (ArtikalID_Array.size() > 0 && client != null) {

                    if (grid_images.getAdapter() == null) grid_images.setAdapter(new ImageAdapter(getActivity()));



                  /*  VerticalViewPager viewPager = (VerticalViewPager) getView().findViewById(R.id.pager);
                    final ViewPagerAdapter adapter = new ViewPagerAdapter((AppCompatActivity) getActivity(), ArtikalID_Array, client);
                    viewPager.setAdapter(adapter);

                    ((TextView) getView().findViewById(R.id.txb_page)).setText(getString(R.string.Action) + " 1 " + getString(R.string.from) + " " + Integer.toString(adapter.getCount()));

                    viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                        }

                        @Override
                        public void onPageSelected(int position) {
                            ((TextView) getView().findViewById(R.id.txb_page)).setText(getString(R.string.Action) + " " + Integer.toString(position + 1) + " " + getString(R.string.from) + " " + Integer.toString(adapter.getCount()));
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });*/
                }

            } catch (Exception e) {
                wurthMB.AddError("Actions list PostExecute", e.getMessage(), e);
            }
          //  getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);

        }

    }

    private class ImageAdapter extends BaseAdapter
    {
        private Context context;

        public ImageAdapter(Context c)
        {
            context = c;
        }

        //---returns the number of images---
        public int getCount() {
            return ArtikalID_Array.size();
        }

        //---returns the ID of an item---
        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        //---returns an ImageView view---
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ImageView view = new ImageView(getContext());
            view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.setAdjustViewBounds(true);
            view.setScaleType(ImageView.ScaleType.FIT_XY);
            view.setTag(ArtikalID_Array.get(position));

            try {

                if (!ArtikalID_Array.get(position).slika.equals("")) {
                    wurthMB.imageLoader.DisplayImage(ArtikalID_Array.get(position).slika, view);
                }
                else {
                    view.setImageResource(R.drawable.no_image);
                }

                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View imageView) {

                        try {

                            if (wurthMB.getOrder() != null && wurthMB.getOrder().ClientID != client.ClientID) {

                                final Dialog dialog = new Dialog(context, R.style.CustomDialog);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setContentView(R.layout.alert);
                                dialog.setCancelable(false);

                                ((TextView) dialog.findViewById(R.id.Title)).setText(R.string.Notification_AttentionNeeded);
                                ((TextView) dialog.findViewById(R.id.text)).setText(R.string.Notification_OrderExist);

                                dialog.findViewById(R.id.dialogButtonNEW).setVisibility(View.GONE);
                                dialog.findViewById(R.id.dialogButtonCANCEL).setVisibility(View.VISIBLE);
                                ((Button) dialog.findViewById(R.id.dialogButtonOK)).setText(R.string.Continue);

                                dialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        wurthMB.setOrder(new Order());

                                        if (client != null) {
                                            Cursor cur = DL_Wurth.GET_Partner(client._clientid);
                                            wurthMB.getOrder().PartnerID = client._clientid;

                                            if (cur != null) {
                                                if (cur.moveToFirst()) {
                                                    wurthMB.getOrder().client = DL_Wurth.GET_Client(cur.getLong(cur.getColumnIndex("ClientID")));
                                                    wurthMB.getOrder().ClientID = wurthMB.getOrder().client.ClientID;
                                                    wurthMB.getOrder().DeliveryPlaceID = cur.getLong(cur.getColumnIndex("DeliveryPlaceID"));
                                                    wurthMB.getOrder().PaymentMethodID = 0;
                                                }
                                                cur.close();
                                            }

                                            if (!((ViewPagerAdapter.Items) imageView.getTag()).PZNA.equals("")) {
                                                Bundle b = new Bundle();
                                                b.putString("Kod_Zbirnog_Naziva", ((ViewPagerAdapter.Items) imageView.getTag()).PZNA);
                                                ProductsListActionsFragment productsListActionsFragment = ProductsListActionsFragment.newInstance();
                                                productsListActionsFragment.setArguments(b);
                                                productsListActionsFragment.show(((AppCompatActivity)context).getSupportFragmentManager(), "productsListActionsFragment");
                                            } else if (((ViewPagerAdapter.Items) imageView.getTag()).PZNA.equals("") && !((ViewPagerAdapter.Items) imageView.getTag()).CatalogCode.equals("")) {
                                                Bundle b = new Bundle();
                                                b.putString("Sifra_Grupe", ((ViewPagerAdapter.Items) imageView.getTag()).CatalogCode);
                                                ProductsListActionsFragment productsListActionsFragment = ProductsListActionsFragment.newInstance();
                                                productsListActionsFragment.setArguments(b);
                                                productsListActionsFragment.show(getActivity().getSupportFragmentManager(), "productsListActionsFragment");
                                            } else {
                                                Intent k = new Intent(getActivity(), ProductActivity.class);
                                                k.putExtra("ACTION", 0);
                                                k.putExtra("ArtikalID", ((ViewPagerAdapter.Items) imageView.getTag()).ArtikalID);
                                                k.putExtra("ProductID", ((ViewPagerAdapter.Items) imageView.getTag()).ProductID);
                                                getActivity().startActivity(k);
                                            }
                                        }
                                        dialog.dismiss();
                                    }
                                });

                                dialog.findViewById(R.id.dialogButtonCANCEL).setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });

                                dialog.show();
                            } else if (wurthMB.getOrder() != null && wurthMB.getOrder().ClientID == client.ClientID) {
                                if (!((ViewPagerAdapter.Items) imageView.getTag()).PZNA.equals("")) {
                                    Bundle b = new Bundle();
                                    b.putString("Kod_Zbirnog_Naziva", ((ViewPagerAdapter.Items) imageView.getTag()).PZNA);
                                    ProductsListActionsFragment productsListActionsFragment = ProductsListActionsFragment.newInstance();
                                    productsListActionsFragment.setArguments(b);
                                    productsListActionsFragment.show(getActivity().getSupportFragmentManager(), "productsListActionsFragment");
                                } else if (((ViewPagerAdapter.Items) imageView.getTag()).PZNA.equals("") && !((ViewPagerAdapter.Items) imageView.getTag()).CatalogCode.equals("")) {
                                    Bundle b = new Bundle();
                                    b.putString("Sifra_Grupe", ((ViewPagerAdapter.Items) imageView.getTag()).CatalogCode);
                                    ProductsListActionsFragment productsListActionsFragment = ProductsListActionsFragment.newInstance();
                                    productsListActionsFragment.setArguments(b);
                                    productsListActionsFragment.show(getActivity().getSupportFragmentManager(), "productsListActionsFragment");
                                } else {
                                    Intent k = new Intent(getActivity(), ProductActivity.class);
                                    k.putExtra("ACTION", 0);
                                    k.putExtra("ArtikalID", ((ViewPagerAdapter.Items) imageView.getTag()).ArtikalID);
                                    k.putExtra("ProductID", ((ViewPagerAdapter.Items) imageView.getTag()).ProductID);
                                    getActivity().startActivity(k);
                                }
                            } else {
                                wurthMB.setOrder(new Order());

                                if (client != null) {
                                    Cursor cur = DL_Wurth.GET_Partner(client._clientid);
                                    wurthMB.getOrder().PartnerID = client._clientid;

                                    if (cur != null) {
                                        if (cur.moveToFirst()) {
                                            wurthMB.getOrder().client = DL_Wurth.GET_Client(cur.getLong(cur.getColumnIndex("ClientID")));
                                            wurthMB.getOrder().ClientID = wurthMB.getOrder().client.ClientID;
                                            wurthMB.getOrder().DeliveryPlaceID = cur.getLong(cur.getColumnIndex("DeliveryPlaceID"));
                                            wurthMB.getOrder().PaymentMethodID = 0;
                                        }
                                        cur.close();
                                    }

                                    if (!((ViewPagerAdapter.Items) imageView.getTag()).PZNA.equals("")) {
                                        Bundle b = new Bundle();
                                        b.putString("Kod_Zbirnog_Naziva", ((ViewPagerAdapter.Items) imageView.getTag()).PZNA);
                                        ProductsListActionsFragment productsListActionsFragment = ProductsListActionsFragment.newInstance();
                                        productsListActionsFragment.setArguments(b);
                                        productsListActionsFragment.show(getActivity().getSupportFragmentManager(), "productsListActionsFragment");
                                    } else if (((ViewPagerAdapter.Items) imageView.getTag()).PZNA.equals("") && !((ViewPagerAdapter.Items) imageView.getTag()).CatalogCode.equals("")) {
                                        Bundle b = new Bundle();
                                        b.putString("Sifra_Grupe", ((ViewPagerAdapter.Items) imageView.getTag()).CatalogCode);
                                        ProductsListActionsFragment productsListActionsFragment = ProductsListActionsFragment.newInstance();
                                        productsListActionsFragment.setArguments(b);
                                        productsListActionsFragment.show(getActivity().getSupportFragmentManager(), "productsListActionsFragment");
                                    } else {
                                        Intent k = new Intent(getActivity(), ProductActivity.class);
                                        k.putExtra("ACTION", 0);
                                        k.putExtra("ArtikalID", ((ViewPagerAdapter.Items) imageView.getTag()).ArtikalID);
                                        k.putExtra("ProductID", ((ViewPagerAdapter.Items) imageView.getTag()).ProductID);
                                        getActivity().startActivity(k);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            wurthMB.AddError("ViewPagerAdapter", e.getMessage(), e);
                        }
                        return true;
                    }

                });

            } catch (Exception e) {
                String temp = e.getMessage();
            }

            return view;
        }
    }
}
