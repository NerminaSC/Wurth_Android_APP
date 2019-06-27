package ba.wurth.mb.Fragments.Clients;

import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class ClientMandatoryFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_mandatory, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binData();
    }

    private void binData() {
        try {





            if (getArguments() != null && getArguments().getLong("ClientID", 0L) > 0) {
                Cursor cur = DL_Wurth.GET_ClientMandatory(getArguments().getLong("ClientID"), 0L);
                if (cur != null) {
                    if (cur.getCount() > 0 && cur.moveToFirst()) {
                        ((EditText) getView().findViewById(R.id.litPDVNumber)).setText(cur.isNull(cur.getColumnIndex("PDVBroj")) ? "N/A" : cur.getString(cur.getColumnIndex("PDVBroj")));
                        ((EditText) getView().findViewById(R.id.litIDNumber)).setText(cur.isNull(cur.getColumnIndex("IDBroj")) ? "N/A" : cur.getString(cur.getColumnIndex("IDBroj")));
                        /*((EditText) getView().findViewById(R.id.litCourtRegister)).setText(cur.isNull(cur.getColumnIndex("KAMUser")) ? "N/A" : cur.getString(cur.getColumnIndex("KAMUser")));
                        ((EditText) getView().findViewById(R.id.litCourtRegisterNumber)).setText(cur.isNull(cur.getColumnIndex("SpecialUser")) ? "N/A" : cur.getString(cur.getColumnIndex("SpecialUser")));
                        ((EditText) getView().findViewById(R.id.litTypeOfBusiness)).setText(cur.isNull(cur.getColumnIndex("KupcevKontaktUCentrali")) ? "N/A" : cur.getString(cur.getColumnIndex("KupcevKontaktUCentrali")));
                        ((EditText) getView().findViewById(R.id.litSourceOfTypeBusiness)).setText(cur.isNull(cur.getColumnIndex("ReferentNaplate")) ? "N/A" : cur.getString(cur.getColumnIndex("ReferentNaplate")));
                        ((EditText) getView().findViewById(R.id.litBank)).setText(cur.isNull(cur.getColumnIndex("KanalDistribucije")) ? "N/A" : cur.getString(cur.getColumnIndex("KanalDistribucije")));
                        ((EditText) getView().findViewById(R.id.litBankNumber1)).setText(cur.isNull(cur.getColumnIndex("Bonitet")) ? "N/A" : cur.getString(cur.getColumnIndex("Bonitet")));
                        ((EditText) getView().findViewById(R.id.litBankNumber2)).setText("");
                        ((EditText) getView().findViewById(R.id.litBankNumber3)).setText(cur.isNull(cur.getColumnIndex("ORSY")) ? "N/A" : (cur.getString(cur.getColumnIndex("ORSY")).equals("1") ? getString(R.string.Yes) : getString(R.string.No)));*/
                    }
                    cur.close();
                }
            }

            if (getArguments() != null && getArguments().getLong("DeliveryPlaceID", 0L) > 0) {
                Cursor cur = DL_Wurth.GET_ClientMandatory(0L, getArguments().getLong("DeliveryPlaceID"));
                if (cur != null) {
                    if (cur.getCount() > 0 && cur.moveToFirst()) {
                        ((EditText) getView().findViewById(R.id.litPDVNumber)).setText(cur.isNull(cur.getColumnIndex("PDVBroj")) ? "N/A" : cur.getString(cur.getColumnIndex("PDVBroj")));
                        ((EditText) getView().findViewById(R.id.litIDNumber)).setText(cur.isNull(cur.getColumnIndex("IDBroj")) ? "N/A" : cur.getString(cur.getColumnIndex("IDBroj")));
                    }
                    cur.close();
                }
            }

        }
        catch (Exception ex) {

        }
    }
}
