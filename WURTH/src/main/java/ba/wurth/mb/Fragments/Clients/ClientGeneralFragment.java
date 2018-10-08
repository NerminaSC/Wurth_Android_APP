package ba.wurth.mb.Fragments.Clients;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.R;

public class ClientGeneralFragment extends Fragment {

    private EditText litName;
    private EditText litAddress;
    private EditText litTelephone;
    private EditText litMobile;
    private EditText litEmailAddress;
    private EditText litWebsite;
    private EditText litDescription;
    private EditText litCode;
    private EditText litCity;
    private EditText litFax;
    private EditText litLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_general, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        litName = (EditText) getView().findViewById(R.id.litName);
        litAddress = (EditText) getView().findViewById(R.id.litAddress);
        litTelephone = (EditText) getView().findViewById(R.id.litTelephone);
        litMobile = (EditText) getView().findViewById(R.id.litMobile);
        litEmailAddress = (EditText) getView().findViewById(R.id.litEmailAddress);
        litWebsite = (EditText) getView().findViewById(R.id.litWebsite);
        litDescription = (EditText) getView().findViewById(R.id.litDescription);
        litCode = (EditText) getView().findViewById(R.id.litCode);
        litCity = (EditText) getView().findViewById(R.id.litCity);
        litFax = (EditText) getView().findViewById(R.id.litFax);
        litLocation = (EditText) getView().findViewById(R.id.litLocation);


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

            if (getArguments() != null && getArguments().getLong("ClientID", 0L) > 0) {
                Client c = DL_Clients.GetByClientID(getArguments().getLong("ClientID"));

                if (c != null) {
                    litName.setText(c.Name);
                    litAddress.setText(c.Address);
                    litTelephone.setText(c.Telephone);
                    litMobile.setText(c.Mobile);
                    litEmailAddress.setText(c.EmailAddress);
                    litWebsite.setText(c.WebSite);
                    litCode .setText(c.code);
                    litDescription.setText(c.Description);
                    litCity.setText(c.City);
                    litFax.setText(c.Fax);

                    if (c.Latitude > 0 && c.Longitude > 0) {
                        litLocation.setText("lat:" + Double.toString(((double) c.Latitude) / 10000000) + "   lon:" + Double.toString(((double) c.Longitude) / 10000000));
                    }
                }
            }

            if (getArguments() != null && getArguments().getLong("DeliveryPlaceID", 0L) > 0) {
                Cursor cur = DL_Clients.Get_DeliveryPlaceByDeliveryPlaceID(getArguments().getLong("DeliveryPlaceID"));
                if (cur != null) {
                    if (cur.getCount() > 0 && cur.moveToFirst()) {
                        litName.setText(cur.getString(cur.getColumnIndex("Naziv")));
                        litAddress.setText(cur.getString(cur.getColumnIndex("Adresa")));
                        litTelephone.setText(cur.getString(cur.getColumnIndex("Telefon")));
                        litFax.setText(cur.getString(cur.getColumnIndex("Fax")));
                        litCity.setText(cur.getString(cur.getColumnIndex("Grad")));
                        litCode.setText(cur.getString(cur.getColumnIndex("Kod")));

                        if (cur.getLong(cur.getColumnIndex("Latitude")) > 0 && cur.getLong(cur.getColumnIndex("Longitude")) > 0) {
                            litLocation.setText("lat:" + Double.toString(((double) cur.getLong(cur.getColumnIndex("Latitude"))) / 10000000) + "   lon:" + Double.toString(((double) cur.getLong(cur.getColumnIndex("Longitude"))) / 10000000));
                        }

                    }
                    cur.close();
                }
            }

        }
        catch (Exception ex) {

        }
    }
}
