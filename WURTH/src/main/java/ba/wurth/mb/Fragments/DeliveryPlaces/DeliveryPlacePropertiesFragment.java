package ba.wurth.mb.Fragments.DeliveryPlaces;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.R;

public class DeliveryPlacePropertiesFragment extends Fragment {

    private LinearLayout llContainer;

    private Long DeliveryPlaceID = 0L;

    private Cursor mCursor;

    private static SimpleDateFormat gDateFormatDateItemTime = new SimpleDateFormat("dd.MM.yyyy HH:mm");


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().getLong("DeliveryPlaceID", 0L) > 0) DeliveryPlaceID = getArguments().getLong("DeliveryPlaceID", 0L);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.deliveryplace_properties, container, false);
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
                mCursor = DL_Clients.Get_DeliveryPlaceProperties(DeliveryPlaceID);
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

                    while (mCursor.moveToNext()) {

                        View v = mInflater.inflate(R.layout.list_row, llContainer, false);

                        ((TextView) v.findViewById(R.id.litTitle)).setText(mCursor.getString(mCursor.getColumnIndex("Property")));
                        ((TextView) v.findViewById(R.id.litSubTitle)).setText(mCursor.getString(mCursor.getColumnIndex("Value")));
                        ((TextView) v.findViewById(R.id.litSupTotal)).setText(gDateFormatDateItemTime.format(new Date(mCursor.getLong(mCursor.getColumnIndex("DOE")))));

                        if (mCursor.getString(mCursor.getColumnIndex("Property")).length() > 0) ((TextView) v.findViewById(R.id.litStatus)).setText(mCursor.getString(mCursor.getColumnIndex("Property")).substring(0,1));

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
