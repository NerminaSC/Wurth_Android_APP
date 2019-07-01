package ba.wurth.mb.Fragments.DeliveryPlaces;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.R;

public class DeliveryPlaceGeneralFragment extends Fragment {

    private EditText litName;
    private EditText litAddress;
    private EditText litTelephone;
    private EditText litCity;
    private EditText litFax;
    private EditText litCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.deliveryplace_general, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        litName = (EditText) getView().findViewById(R.id.litName);
        litAddress = (EditText) getView().findViewById(R.id.litAddress);
        litTelephone = (EditText) getView().findViewById(R.id.litTelephone);
        litCity = (EditText) getView().findViewById(R.id.litCity);
        litFax = (EditText) getView().findViewById(R.id.litFax);
        litCode = (EditText) getView().findViewById(R.id.litCode);


        binData();
    }

    private void bindListeners() {
         try {


         }
         catch (Exception ex) {

         }
    }

    private void binData() {
        try {

            if (getArguments() != null && getArguments().getLong("DeliveryPlaceID", 0L) > 0) {
                Cursor cur = DL_Clients.Get_DeliveryPlaceByDeliveryPlaceID(getArguments().getLong("DeliveryPlaceID"));
                getActivity().startManagingCursor(cur);

                if (cur != null) {
                    if (cur.getCount() > 0 && cur.moveToFirst()) {
                        litName.setText(cur.getString(cur.getColumnIndex("Naziv")));
                        litAddress.setText(cur.getString(cur.getColumnIndex("Adresa")));
                        litTelephone.setText(cur.getString(cur.getColumnIndex("Telefon")));
                        litFax.setText(cur.getString(cur.getColumnIndex("Fax")));
                        litCity.setText(cur.getString(cur.getColumnIndex("Grad")));
                        litCode.setText(cur.getString(cur.getColumnIndex("Kod")));
                    }
                }
            }
        }
        catch (Exception ex) {

        }
    }
}
