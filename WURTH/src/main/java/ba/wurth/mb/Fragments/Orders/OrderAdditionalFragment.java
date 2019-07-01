package ba.wurth.mb.Fragments.Orders;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.fourmob.datetimepicker.date.DatePickerDialog;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class OrderAdditionalFragment extends Fragment implements DatePickerDialog.OnDateSetListener  {

    private EditText txbCompanyName;
    private EditText txbName;
    private EditText txbAddress;
    private EditText txbPostalcode;
    private EditText txbCity;
    private EditText txbTelephone;
    private EditText txbFax;

    private TextView litDeliveryDate;

    public Spinner spSalutation;
    public Spinner spCountry;

    private SpinnerItem[] salutatiom_items;
    private SpinnerItem[] country_items;

    private SpinnerAdapter adapter_salution;
    private SpinnerAdapter adapter_country;

    private Calendar calendar;
    public static final String DELIVERYDATE_TAG = "1";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.order_additional, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        litDeliveryDate = (TextView) getView().findViewById(R.id.litDeliveryDate);

        txbCompanyName = (EditText) getView().findViewById(R.id.txbCompanyName);
        txbName = (EditText) getView().findViewById(R.id.txbName);
        txbAddress = (EditText) getView().findViewById(R.id.txbAddress);
        txbPostalcode = (EditText) getView().findViewById(R.id.txbPostalcode);
        txbCity = (EditText) getView().findViewById(R.id.txbCity);
        txbTelephone = (EditText) getView().findViewById(R.id.txbTelephone);
        txbFax = (EditText) getView().findViewById(R.id.txbFax);

        spSalutation = (Spinner) getView().findViewById(R.id.spSalutation);
        spCountry = (Spinner) getView().findViewById(R.id.spCountry);

        calendar = Calendar.getInstance();

        bindData();
        bindListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {

        Calendar c = Calendar.getInstance();

        switch (Integer.parseInt(datePickerDialog.getTag())) {
            case 1:
                litDeliveryDate.setText(day + "." + (month + 1) + "." + year);
                c.set(year, month, day, 0, 0, 0);
                litDeliveryDate.setContentDescription(Long.toString(c.getTimeInMillis()));
                wurthMB.getOrder().DeliveryDate = c.getTimeInMillis();
                wurthMB.setOrder(wurthMB.getOrder());
                break;
            default:
                break;
        }
        saveOrder();
    }

    private void bindListeners() {
        try {

            calendar.setTimeInMillis(wurthMB.getOrder().DeliveryDate);
            final DatePickerDialog datePickerDialogDeliveryDate = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);

            datePickerDialogDeliveryDate.setYearRange(2010, calendar.get(Calendar.YEAR) + 2);

            txbCompanyName.addTextChangedListener(new textWatcher());
            txbName.addTextChangedListener(new textWatcher());
            txbAddress.addTextChangedListener(new textWatcher());
            txbPostalcode.addTextChangedListener(new textWatcher());
            txbCity.addTextChangedListener(new textWatcher());
            txbTelephone.addTextChangedListener(new textWatcher());
            txbFax.addTextChangedListener(new textWatcher());

            litDeliveryDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datePickerDialogDeliveryDate.show(getFragmentManager(), DELIVERYDATE_TAG);
                }
            });

            spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    saveOrder();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spSalutation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    saveOrder();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        }
        catch (Exception ex) {

        }
    }

    public void bindData() {
        try {

            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

            salutatiom_items = new SpinnerItem[3];
            salutatiom_items[0] = new SpinnerItem(1L, getString(R.string.Mr), "", "");
            salutatiom_items[1] = new SpinnerItem(2L, getString(R.string.Mrs), "", "");
            salutatiom_items[2] = new SpinnerItem(3L, getString(R.string.Miss), "", "");
            adapter_salution = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, salutatiom_items);
            spSalutation.setAdapter(adapter_salution);

            country_items = new SpinnerItem[3];
            country_items[0] = new SpinnerItem(1L, "Bosnia and Herzegovina", "", "");
            country_items[1] = new SpinnerItem(2L, "Serbia", "", "");
            country_items[2] = new SpinnerItem(3L, "Croatia", "", "");
            adapter_country = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, country_items);
            spCountry.setAdapter(adapter_country);

            if (wurthMB.getOrder() != null) {

                if (wurthMB.getOrder().DeliveryDate != wurthMB.getOrder().OrderDate) litDeliveryDate.setText(df.format(new Date(wurthMB.getOrder().DeliveryDate)));
                litDeliveryDate.setContentDescription(Long.toString(wurthMB.getOrder().DeliveryDate));

                JSONObject relation = new JSONObject(wurthMB.getOrder().Relations);

                if (relation.has("OrderAdditional_CompanyName")) txbCompanyName.setText(relation.getString("OrderAdditional_CompanyName"));
                if (relation.has("OrderAdditional_Name")) txbName.setText(relation.getString("OrderAdditional_Name"));
                if (relation.has("OrderAdditional_Address")) txbAddress.setText(relation.getString("OrderAdditional_Address"));
                if (relation.has("OrderAdditional_City")) txbCity.setText(relation.getString("OrderAdditional_City"));
                if (relation.has("OrderAdditional_Fax")) txbFax.setText(relation.getString("OrderAdditional_Fax"));
                if (relation.has("OrderAdditional_Postalcode")) txbPostalcode.setText(relation.getString("OrderAdditional_Postalcode"));
                if (relation.has("OrderAdditional_Telephone")) txbTelephone.setText(relation.getString("OrderAdditional_Telephone"));


                if (relation.has("OrderAdditional_Salutation")) {
                    int i = 0;
                    for (SpinnerItem item : salutatiom_items) {
                        if (item.getName().equals(relation.getString("OrderAdditional_Salutation"))) {
                            break;
                        }
                        i++;
                    }
                    spSalutation.setSelection(i);
                }

                if (relation.has("OrderAdditional_Country")) {
                    int i = 0;
                    for (SpinnerItem item : country_items) {
                        if (item.getName().equals(relation.getString("OrderAdditional_Country"))) {
                            break;
                        }
                        i++;
                    }
                    spCountry.setSelection(i);
                }

                if (wurthMB.getOrder().Sync == 1 && (wurthMB.getOrder().OrderStatusID == 4 || wurthMB.getOrder().OrderStatusID == 5)) {

                    try {
                        ViewGroup group = (ViewGroup) getView().findViewById(R.id.llContainer);
                        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                            View view = group.getChildAt(i);
                            view.setEnabled(false);
                            if (view instanceof EditText) {
                                //((EditText) view).setTextAppearance(getActivity(), R.style.label_textbox_readonly);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        catch (Exception ex) {

        }
    }

    private void saveOrder() {
        try {
            if (wurthMB.getOrder() != null) {

                JSONObject relation = new JSONObject(wurthMB.getOrder().Relations);

                relation.put("OrderAdditional_CompanyName", txbCompanyName.getText());
                relation.put("OrderAdditional_Name", txbName.getText());
                relation.put("OrderAdditional_Address", txbAddress.getText());
                relation.put("OrderAdditional_City", txbCity.getText());
                relation.put("OrderAdditional_Fax", txbFax.getText());
                relation.put("OrderAdditional_Postalcode", txbPostalcode.getText());
                relation.put("OrderAdditional_Telephone", txbTelephone.getText());
                relation.put("OrderAdditional_Salutation", salutatiom_items[spSalutation.getSelectedItemPosition()].getName());
                relation.put("OrderAdditional_Country", country_items[spCountry.getSelectedItemPosition()].getName());

                wurthMB.getOrder().Relations = relation.toString();

                wurthMB.setOrder(wurthMB.getOrder());
            }
        }
        catch (Exception ex) {

        }
    }

    private class textWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            try {
                if (wurthMB.getOrder() != null)  {
                    saveOrder();
                }
            }
            catch (Exception ex) {
            }
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
