package ba.wurth.mb.Fragments.Clients.Add;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import ba.wurth.mb.Activities.Clients.ClientAddActivity;
import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class ClientAddAutoServiceFragment extends Fragment {

    private SpinnerItem[] autoservicetype13_items;

    private Spinner spAutoServiceType13;

    private SpinnerAdapter adapter_spautoservicetype13;

    public JSONObject mTemp;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTemp = ((ClientAddActivity) getActivity()).mTemp;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_add_autoservice, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spAutoServiceType13 = (Spinner) getView().findViewById(R.id.spAutoServiceType13);

        autoservicetype13_items = new SpinnerItem[3];
        autoservicetype13_items[0] = new SpinnerItem(-1L, "", "", "");
        autoservicetype13_items[1] = new SpinnerItem(1L, getString(R.string.Yes), "", "");
        autoservicetype13_items[2] = new SpinnerItem(0L, getString(R.string.No), "", "");
        adapter_spautoservicetype13 = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, autoservicetype13_items);
        spAutoServiceType13.setAdapter(adapter_spautoservicetype13);


        bindData();
        bindListeners();
    }

    private void bindData() {
        try {

            ViewGroup group = (ViewGroup) getView().findViewById(R.id.llContainer);

            if (mTemp != null) {
                for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                    View view = group.getChildAt(i);

                    if (view instanceof EditText) {
                        String id = getResources().getResourceName(view.getId()).split("lit_")[1];
                        int resID = getResources().getIdentifier(id, "string", "ba.wurth.mb");
                        if (!mTemp.isNull(getString(resID)))
                            ((EditText) view).setText(mTemp.getString(getString(resID)));
                    }
                }

                if (!mTemp.isNull(getString(R.string.AutoServiceType13))) {
                    for (int i = 0; i < autoservicetype13_items.length; i++) {
                        if (autoservicetype13_items[i].getId().toString().equals(mTemp.getString(getString(R.string.AutoServiceType13)))) {
                            spAutoServiceType13.setSelection(i);
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            wurthMB.AddError("ClientAddAutoService", "", ex);
        }
    }

    private void bindListeners() {
         try {
             ViewGroup group = (ViewGroup) getView().findViewById(R.id.llContainer);
             for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                 View view = group.getChildAt(i);
                 if (view instanceof EditText) {
                     ((EditText) view).addTextChangedListener(new textWatcher());
                 }
             }

             spAutoServiceType13.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                 @Override
                 public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { saveTemp();}
                 @Override
                 public void onNothingSelected(AdapterView<?> adapterView) {}
             });

         }
         catch (Exception ex) {

         }
    }

    private class textWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            try {
                saveTemp();
            }
            catch (Exception ex) {
            }
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }


    private void saveTemp() {
        try {

            if (mTemp == null) mTemp = new JSONObject();

            ViewGroup group = (ViewGroup) getView().findViewById(R.id.llContainer);
            for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                View view = group.getChildAt(i);
                if (view instanceof EditText) {
                    String id = getResources().getResourceName(view.getId()).split("lit_")[1];
                    int resID = getResources().getIdentifier(id, "string", "ba.wurth.mb");
                    mTemp.put(getString(resID), ((EditText) view).getText().toString());
                }
            }

            mTemp.put(getString(R.string.AutoServiceType13), autoservicetype13_items[spAutoServiceType13.getSelectedItemPosition()].getId());

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
