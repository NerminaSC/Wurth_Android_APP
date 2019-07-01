package ba.wurth.mb.Fragments.DeliveryPlaces;

import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import ba.wurth.mb.DataLayer.Additional.DL_Additional;
import ba.wurth.mb.R;

public class DeliveryPlaceMerchandisesFragment extends Fragment {

    private LinearLayout llContainer;
    private LinearLayout llTotals;

    private Long DeliveryPlaceID = 0L;

    private Cursor mCursorTotals;
    private Cursor mCursorList;

    private static SimpleDateFormat gDateFormatDataItem = new SimpleDateFormat("EEE dd.MMM.yyyy");
    private static SimpleDateFormat gDateFormatDateItemTime = new SimpleDateFormat("HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().getLong("DeliveryPlaceID", 0L) > 0) DeliveryPlaceID = getArguments().getLong("DeliveryPlaceID", 0L);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.deliveryplace_merchanidise, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        llContainer = (LinearLayout) getView().findViewById(R.id.llContainer);
        llTotals = (LinearLayout) getView().findViewById(R.id.llTotals);

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
                mCursorTotals = DL_Additional.Get_DeliveryPlaceMerchandiseTotals(DeliveryPlaceID);
                mCursorList = DL_Additional.Get_DeliveryPlaceMerchandise(DeliveryPlaceID);
                getActivity().startManagingCursor(mCursorList);
                getActivity().startManagingCursor(mCursorTotals);
            }
            catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            try {

                LayoutInflater mInflater = LayoutInflater.from(getActivity());

                if (mCursorTotals != null){
                    llTotals.removeAllViews();

                    while (mCursorTotals.moveToNext()) {

                        View v = mInflater.inflate(R.layout.list_row, llContainer, false);

                        ((TextView) v.findViewById(R.id.litTitle)).setText(mCursorTotals.getString(mCursorTotals.getColumnIndex("Name")));
                        ((TextView) v.findViewById(R.id.litTotal)).setText(mCursorTotals.getString(mCursorTotals.getColumnIndex("Count")));
                        v.findViewById(R.id.litStatus).setVisibility(View.GONE);
                        v.setBackgroundColor(Color.parseColor("#EEEEEE"));
                        llTotals.addView(v);
                    }
                }

                if (mCursorList != null){

                    Long tempDate = 0L;

                    llContainer.removeAllViews();

                    while (mCursorList.moveToNext()) {

                        View v = mInflater.inflate(R.layout.list_row, llContainer, false);

                        if (tempDate != mCursorList.getLong(mCursorList.getColumnIndex("DOE"))) {
                            v.findViewById(R.id.litHeader).setVisibility(View.VISIBLE);
                            ((TextView) v.findViewById(R.id.litHeader)).setText(gDateFormatDataItem.format(mCursorList.getLong(mCursorList.getColumnIndex("DOE"))));
                        }

                        ((TextView) v.findViewById(R.id.litTitle)).setText(mCursorList.getString(mCursorList.getColumnIndex("Name")));
                        ((TextView) v.findViewById(R.id.litTotal)).setText(mCursorList.getString(mCursorList.getColumnIndex("Count")));
                        ((TextView) v.findViewById(R.id.litSupTotal)).setText(gDateFormatDateItemTime.format(new Date(mCursorList.getLong(mCursorList.getColumnIndex("DOE")))));

                        if (mCursorList.getInt(mCursorList.getColumnIndex("Count")) > 0) {
                            ((TextView) v.findViewById(R.id.litStatus)).setText("+");
                            v.findViewById(R.id.litStatus).setBackgroundColor(Color.parseColor("#27AE60"));
                        }
                        else {
                            ((TextView) v.findViewById(R.id.litStatus)).setText("-");
                            v.findViewById(R.id.litStatus).setBackgroundColor(Color.parseColor("#C0392B"));
                        }

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
