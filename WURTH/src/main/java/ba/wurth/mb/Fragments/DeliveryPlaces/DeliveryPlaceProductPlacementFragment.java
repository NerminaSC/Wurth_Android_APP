package ba.wurth.mb.Fragments.DeliveryPlaces;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.DataLayer.Additional.DL_Additional;
import ba.wurth.mb.R;

public class DeliveryPlaceProductPlacementFragment extends Fragment {

    private LinearLayout llContainer;

    private Long DeliveryPlaceID = 0L;
    private Cursor mCursor;
    private String array_spinner[];

    private static SimpleDateFormat gDateFormatDataItem = new SimpleDateFormat("EEE dd.MMM.yyyy");
    private static SimpleDateFormat gDateFormatDateItemTime = new SimpleDateFormat("HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().getLong("DeliveryPlaceID", 0L) > 0) DeliveryPlaceID = getArguments().getLong("DeliveryPlaceID", 0L);

        array_spinner = new String[5];
        array_spinner[0]="A";
        array_spinner[1]="B";
        array_spinner[2]="C";
        array_spinner[3]="D";
        array_spinner[4]="E";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.deliveryplace_product_placement, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        llContainer = (LinearLayout) getView().findViewById(R.id.llContainer);

        bindData();
        bindListeners();
    }

    private void bindListeners() {
        try {

        }
        catch (Exception ex) {

        }
    }

    public void bindData() {
        try {

            if (DeliveryPlaceID > 0L) {
                new LongTask().execute();
            }
        }
        catch (Exception ex) {

        }
    }

    private class LongTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(300);
                mCursor = DL_Additional.Get_Product_Placement_Values(DeliveryPlaceID);
                getActivity().startManagingCursor(mCursor);
            }
            catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            try {
                if (mCursor != null){

                    LayoutInflater mInflater = LayoutInflater.from(getActivity());

                    llContainer.removeAllViews();

                    Long tempDate = 0L;

                    while (mCursor.moveToNext()) {

                        View v = mInflater.inflate(R.layout.list_row, llContainer, false);

                        if (tempDate != mCursor.getLong(mCursor.getColumnIndex("DOE"))) {
                            v.findViewById(R.id.litHeader).setVisibility(View.VISIBLE);
                            ((TextView) v.findViewById(R.id.litHeader)).setText(gDateFormatDataItem.format(mCursor.getLong(mCursor.getColumnIndex("DOE"))));
                        }

                        ((TextView) v.findViewById(R.id.litTitle)).setText(mCursor.getString(mCursor.getColumnIndex("ProductName")));
                        ((TextView) v.findViewById(R.id.litSubTitle)).setText(getString(R.string.Facing) + ": " + array_spinner[mCursor.getInt(mCursor.getColumnIndex("Col6"))]);
                        ((TextView) v.findViewById(R.id.litSupTotal)).setText(gDateFormatDateItemTime.format(new Date(mCursor.getLong(mCursor.getColumnIndex("DOE")))));
                        ((TextView) v.findViewById(R.id.litTotal)).setText(CustomNumberFormat.GenerateFormatCurrency(mCursor.getDouble(mCursor.getColumnIndex("Col1"))));

                        if (mCursor.getString(mCursor.getColumnIndex("ProductName")).length() > 0) ((TextView) v.findViewById(R.id.litStatus)).setText(mCursor.getString(mCursor.getColumnIndex("ProductName")).substring(0,1));

                        llContainer.addView(v);
                        llContainer.addView(mInflater.inflate(R.layout.divider, llContainer, false));
                    }
                }
            } catch (Exception e) {

            }

            getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
        }
    }
}
