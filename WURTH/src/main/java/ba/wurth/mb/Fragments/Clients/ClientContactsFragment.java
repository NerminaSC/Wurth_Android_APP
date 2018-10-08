package ba.wurth.mb.Fragments.Clients;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class ClientContactsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_contacts, container, false);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) new LongTask().execute();
        else new LongTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private class LongTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected void onPreExecute() {
            try {
            }
            catch (Exception ex) {

            }
        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            try {
                if (getArguments() != null && getArguments().getLong("ClientID", 0L) > 0) {
                    return DL_Wurth.GET_ClientContacts(getArguments().getLong("ClientID"));
                }
            }
            catch (Exception ex) {
                wurthMB.AddError("Client Contacts", ex.getMessage(), ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Cursor cur) {

            try {

                LayoutInflater mInflater = LayoutInflater.from(getActivity());

                if (cur != null) {
                    LinearLayout ll = (LinearLayout) getView().findViewById(R.id.llList);
                    ll.removeAllViews();

                    while (cur.moveToNext()) {

                        View v = mInflater.inflate(R.layout.list_row, ll, false);

                        ((TextView) v.findViewById(R.id.litTitle)).setText(cur.getString(cur.getColumnIndex("Ime")) + " " + cur.getString(cur.getColumnIndex("Prezime")));
                        ((TextView) v.findViewById(R.id.litSubTitle)).setText(Html.fromHtml(cur.getString(cur.getColumnIndex("TipKontakta")) + ": " + cur.getString(cur.getColumnIndex("Broj")) + "<br />Email: " + cur.getString(cur.getColumnIndex("Email"))));
                        ((TextView) v.findViewById(R.id.litTotal)).setText(cur.getString(cur.getColumnIndex("Pozicija")));

                        if (cur.getString(cur.getColumnIndex("Ime")).length() > 0) ((TextView) v.findViewById(R.id.litStatus)).setText(cur.getString(cur.getColumnIndex("Ime")).substring(0,1));

                        ll.addView(v);
                        ll.addView(mInflater.inflate(R.layout.divider, ll, false));
                    }

                    cur.close();
                }
            }
            catch (Exception ex) {
                wurthMB.AddError("Client Contacts", ex.getMessage(), ex);
            }
        }
    }
}
