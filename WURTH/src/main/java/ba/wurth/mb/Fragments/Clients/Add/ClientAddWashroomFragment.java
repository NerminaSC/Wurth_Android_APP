package ba.wurth.mb.Fragments.Clients.Add;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import ba.wurth.mb.Activities.Clients.ClientAddActivity;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.R;

public class ClientAddWashroomFragment extends Fragment {

    public JSONObject mTemp;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTemp = ((ClientAddActivity) getActivity()).mTemp;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_add_washroom, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                        if (!mTemp.isNull(getString(resID))) ((EditText) view).setText(mTemp.getString(getString(resID)));

                        try {
                            View prevView = group.getChildAt(group.indexOfChild(view) - 1);
                            if (view != null && view instanceof TextView) {
                                if (!mTemp.isNull(getString(resID))) ((TextView) prevView).setText(mTemp.getString(getString(resID)));
                            }
                        } catch (JSONException e) {
                            wurthMB.AddError("ClientAddWashroomFragment", "", e);
                        }
                    }
                }
            }

            if (((ClientAddActivity) getActivity()).mPartner != null) {
                JSONObject client = ((ClientAddActivity) getActivity()).mPartner;
                Iterator<String> iter = client.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    try {
                        Object value = client.get(key);
                        int resID = getResources().getIdentifier("_lit_" + key, "id", "ba.wurth.mb");
                        if (resID > 0 && !value.toString().toUpperCase().equals("NULL")) ((TextView) getView().findViewById(resID)).setText(value.toString());
                    } catch (Exception e) {
                    }
                }
            }
        }
        catch (Exception ex) {
            wurthMB.AddError("ClientAddWashroomFragment", "", ex);
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
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
