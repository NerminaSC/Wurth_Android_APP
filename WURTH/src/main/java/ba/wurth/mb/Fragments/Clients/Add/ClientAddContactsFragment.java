package ba.wurth.mb.Fragments.Clients.Add;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import ba.wurth.mb.Activities.Clients.ClientAddActivity;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.R;

public class ClientAddContactsFragment extends Fragment {

    public JSONObject mTemp;

    private Button btnAddContact;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTemp = ((ClientAddActivity) getActivity()).mTemp;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_add_contacts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnAddContact = (Button) getView().findViewById(R.id.btnAddContact);

        bindList();
        bindListeners();
    }

    private void bindList() {
        try {

            if (mTemp != null && mTemp.has(getString(R.string.Contacts))) {

                final JSONArray jsonArray = mTemp.getJSONArray(getString(R.string.Contacts));

                LayoutInflater mInflater = LayoutInflater.from(getActivity());

                LinearLayout ll = (LinearLayout) getView().findViewById(R.id.llContacts);
                ll.removeAllViews();

                for(int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    View v = mInflater.inflate(R.layout.list_row, ll, false);

                    ((TextView) v.findViewById(R.id.litTitle)).setText(jsonObject.getString(getString(R.string.Firstname)) + " " + jsonObject.getString(getString(R.string.Lastname)));

                    v.setTag(i);

                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            try {
                                mTemp.put(getString(R.string.Contacts), Common.remove(Integer.parseInt(view.getTag().toString()), jsonArray));
                                bindList();
                            }
                            catch (Exception exx) {

                            }
                            return true;
                        }
                    });

                    ll.addView(v);
                    ll.addView(mInflater.inflate(R.layout.divider, ll, false));

                }
            }
        }
        catch (Exception ex) {

        }
    }

    private void bindListeners() {

        try {

             btnAddContact.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     try {


                         if (mTemp == null) mTemp = new JSONObject();

                         if (!mTemp.has(getString(R.string.Contacts))) mTemp.put(getString(R.string.Contacts), new JSONArray());

                         JSONObject client = new JSONObject();

                         ViewGroup group = (ViewGroup) getView().findViewById(R.id.llContainer);
                         for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                             View view = group.getChildAt(i);
                             if (view instanceof EditText) {
                                 String id = getResources().getResourceName(view.getId()).split("lit_")[1];
                                 int resID = getResources().getIdentifier(id, "string", "ba.wurth.mb");
                                 client.put(getString(resID), ((EditText) view).getText().toString());
                             }
                         }

                         if (client.getString(getString(R.string.Firstname)).equals("") || client.getString(getString(R.string.Lastname)).equals("") || client.getString(getString(R.string.Telephone)).equals("") || client.getString(getString(R.string.EmailAddress)).equals("") ) {
                             Notifications.showNotification(getActivity(), "", getString(R.string.Notification_MissingField), 2);
                             return;
                         }

                         mTemp.getJSONArray(getString(R.string.Contacts)).put(client);

                         bindList();

                         for (int i = 0, count = group.getChildCount(); i < count; ++i) {
                             View view = group.getChildAt(i);
                             if (view instanceof EditText) {
                                 ((EditText) view).setText("");
                             }
                         }
                     }
                     catch (Exception ex) {

                     }
                 }
             });
         }
         catch (Exception ex) {

         }
    }

}
