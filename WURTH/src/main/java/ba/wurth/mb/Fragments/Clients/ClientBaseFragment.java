package ba.wurth.mb.Fragments.Clients;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class ClientBaseFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_base, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binData();
    }

    private void binData() {
        try {

            if (getArguments() != null && getArguments().getLong("ClientID", 0L) > 0) {
                Cursor cur = DL_Wurth.GET_ClientDetails(getArguments().getLong("ClientID"));
                if (cur != null) {
                    if (cur.getCount() > 0 && cur.moveToFirst()) {
                        ((EditText) getView().findViewById(R.id.litBaseUserK2)).setText(cur.isNull(cur.getColumnIndex("K2User")) ? "N/A" : cur.getString(cur.getColumnIndex("K2User")));
                        ((EditText) getView().findViewById(R.id.litBaseUserK1)).setText(cur.isNull(cur.getColumnIndex("K1User")) ? "N/A" : cur.getString(cur.getColumnIndex("K1User")));
                        ((EditText) getView().findViewById(R.id.litKAM)).setText(cur.isNull(cur.getColumnIndex("KAMUser")) ? "N/A" : cur.getString(cur.getColumnIndex("KAMUser")));
                        ((EditText) getView().findViewById(R.id.litSpecialUser)).setText(cur.isNull(cur.getColumnIndex("SpecialUser")) ? "N/A" : cur.getString(cur.getColumnIndex("SpecialUser")));
                        ((EditText) getView().findViewById(R.id.litCentralUser)).setText(cur.isNull(cur.getColumnIndex("KupcevKontaktUCentrali")) ? "N/A" : cur.getString(cur.getColumnIndex("KupcevKontaktUCentrali")));
                        ((EditText) getView().findViewById(R.id.litReferentUser)).setText(cur.isNull(cur.getColumnIndex("ReferentNaplate")) ? "N/A" : cur.getString(cur.getColumnIndex("ReferentNaplate")));
                        ((EditText) getView().findViewById(R.id.litDistributionChannel)).setText(cur.isNull(cur.getColumnIndex("KanalDistribucije")) ? "N/A" : cur.getString(cur.getColumnIndex("KanalDistribucije")));
                        ((EditText) getView().findViewById(R.id.litBonitet)).setText(cur.isNull(cur.getColumnIndex("Bonitet")) ? "N/A" : cur.getString(cur.getColumnIndex("Bonitet")));
                        ((EditText) getView().findViewById(R.id.litWholesale)).setText(cur.isNull(cur.getColumnIndex("Veleprodaja")) ? "N/A" : (cur.getString(cur.getColumnIndex("Veleprodaja")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litPrepaid)).setText("N/A");
                        ((EditText) getView().findViewById(R.id.litORSY)).setText(cur.isNull(cur.getColumnIndex("ORSY")) ? "N/A" : (cur.getString(cur.getColumnIndex("ORSY")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litOnlineShop)).setText(cur.isNull(cur.getColumnIndex("OnlineShop")) ? "N/A" : (cur.getString(cur.getColumnIndex("OnlineShop")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litOTD)).setText(cur.isNull(cur.getColumnIndex("OTD")) ? "N/A" : (cur.getString(cur.getColumnIndex("OTD")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litDelivery6h)).setText(cur.isNull(cur.getColumnIndex("BrzaIsporuka")) ? "N/A" : (cur.getString(cur.getColumnIndex("BrzaIsporuka")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litStoreAssociations)).setText(cur.isNull(cur.getColumnIndex("VezaNaProdavnicu")) ? "N/A" : (cur.getString(cur.getColumnIndex("VezaNaProdavnicu")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litTNT)).setText(cur.isNull(cur.getColumnIndex("TNT")) ? "N/A" : (cur.getString(cur.getColumnIndex("TNT")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litCompetitor)).setText(cur.isNull(cur.getColumnIndex("Konkurent")) ? "N/A" : (cur.getString(cur.getColumnIndex("Konkurent")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                    }
                    cur.close();
                }
            }

            if (getArguments() != null && getArguments().getLong("DeliveryPlaceID", 0L) > 0) {
                Cursor cur = DL_Wurth.GET_DeliveryPlaceDetails(getArguments().getLong("DeliveryPlaceID"));
                if (cur != null) {
                    if (cur.getCount() > 0 && cur.moveToFirst()) {
                        ((EditText) getView().findViewById(R.id.litBaseUserK2)).setText(cur.isNull(cur.getColumnIndex("K2User")) ? "N/A" : cur.getString(cur.getColumnIndex("K2User")));
                        ((EditText) getView().findViewById(R.id.litBaseUserK1)).setText(cur.isNull(cur.getColumnIndex("K1User")) ? "N/A" : cur.getString(cur.getColumnIndex("K1User")));
                        ((EditText) getView().findViewById(R.id.litKAM)).setText(cur.isNull(cur.getColumnIndex("KAMUser")) ? "N/A" : cur.getString(cur.getColumnIndex("KAMUser")));
                        ((EditText) getView().findViewById(R.id.litSpecialUser)).setText(cur.isNull(cur.getColumnIndex("SpecialUser")) ? "N/A" : cur.getString(cur.getColumnIndex("SpecialUser")));
                        ((EditText) getView().findViewById(R.id.litCentralUser)).setText(cur.isNull(cur.getColumnIndex("KupcevKontaktUCentrali")) ? "N/A" : cur.getString(cur.getColumnIndex("KupcevKontaktUCentrali")));
                        ((EditText) getView().findViewById(R.id.litReferentUser)).setText(cur.isNull(cur.getColumnIndex("ReferentNaplate")) ? "N/A" : cur.getString(cur.getColumnIndex("ReferentNaplate")));
                        ((EditText) getView().findViewById(R.id.litDistributionChannel)).setText(cur.isNull(cur.getColumnIndex("KanalDistribucije")) ? "N/A" : cur.getString(cur.getColumnIndex("KanalDistribucije")));
                        ((EditText) getView().findViewById(R.id.litBonitet)).setText(cur.isNull(cur.getColumnIndex("Bonitet")) ? "N/A" : cur.getString(cur.getColumnIndex("Bonitet")));
                        ((EditText) getView().findViewById(R.id.litWholesale)).setText(cur.isNull(cur.getColumnIndex("Veleprodaja")) ? "N/A" : (cur.getString(cur.getColumnIndex("Veleprodaja")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litPrepaid)).setText("N/A");
                        ((EditText) getView().findViewById(R.id.litORSY)).setText(cur.isNull(cur.getColumnIndex("ORSY")) ? "N/A" : (cur.getString(cur.getColumnIndex("ORSY")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litOnlineShop)).setText(cur.isNull(cur.getColumnIndex("OnlineShop")) ? "N/A" : (cur.getString(cur.getColumnIndex("OnlineShop")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litOTD)).setText(cur.isNull(cur.getColumnIndex("OTD")) ? "N/A" : (cur.getString(cur.getColumnIndex("OTD")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litDelivery6h)).setText(cur.isNull(cur.getColumnIndex("BrzaIsporuka")) ? "N/A" : (cur.getString(cur.getColumnIndex("BrzaIsporuka")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litStoreAssociations)).setText(cur.isNull(cur.getColumnIndex("VezaNaProdavnicu")) ? "N/A" : (cur.getString(cur.getColumnIndex("VezaNaProdavnicu")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litTNT)).setText(cur.isNull(cur.getColumnIndex("TNT")) ? "N/A" : (cur.getString(cur.getColumnIndex("TNT")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                        ((EditText) getView().findViewById(R.id.litCompetitor)).setText(cur.isNull(cur.getColumnIndex("Konkurent")) ? "N/A" : (cur.getString(cur.getColumnIndex("Konkurent")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));
                    }
                    cur.close();
                }
            }
        }
        catch (Exception ex) {

        }
    }
}
