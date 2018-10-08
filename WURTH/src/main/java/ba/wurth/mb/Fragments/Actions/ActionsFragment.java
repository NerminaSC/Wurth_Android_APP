package ba.wurth.mb.Fragments.Actions;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ba.wurth.mb.Adapters.ViewPagerAdapter;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.VerticalViewPager;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class ActionsFragment extends Fragment {

    private Cursor cursor;

    public Long ProductID = 0L;
    public Long ArtikalID = 0L;
    public Long CategoryID = 0L;
    public Long ClientID = 0L;
    public Long DeliveryPlaceID = 0L;
    public String searchWord = "";

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

        ArrayList<ViewPagerAdapter.Items> ArtikalID_Array = null;
        Client client = null;

        @Override
        protected void onPreExecute() {
            try {
                getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);

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
                    VerticalViewPager viewPager = (VerticalViewPager) getView().findViewById(R.id.pager);
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
                    });
                }

            } catch (Exception e) {
                wurthMB.AddError("Actions list PostExecute", e.getMessage(), e);
            }
            getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);

        }

    }
}
