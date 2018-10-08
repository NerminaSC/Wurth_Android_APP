package ba.wurth.mb.Activities.Reports;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;


public class LRCActivity extends AppCompatActivity  {

    private SimpleCursorAdapter mAdapterClients;

    public AutoCompleteTextView txbClients;
    public TextView txbID;

    private ImageButton btnClearClients;

    private Button btnUpdate;
    private Button btnCancel;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lrc);

        try {

            getSupportActionBar().setTitle("LRC");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            txbClients = (AutoCompleteTextView) findViewById(R.id.txbClients);
            txbID = (TextView) findViewById(R.id.txbID);

            btnClearClients = (ImageButton) findViewById(R.id.btnClearClients);

            bindListeners();
        }
        catch (Exception ex) {
            wurthMB.AddError("LRC", ex.getMessage(), ex);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void bindListeners() {
        try {

            btnUpdate = (Button) findViewById(R.id.btnUpdate);
            btnCancel = (Button) findViewById(R.id.btnCancel);

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        if (txbID.getText().toString().equals("")) {
                            Notifications.showNotification(LRCActivity.this,"", getString(R.string.EmptyFields), 2);
                            return;
                        }
                        new LongTask().execute(txbID.getText().toString());
                    }
                    catch (Exception ex) {

                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            btnClearClients.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    txbClients.setText("");
                    txbID.setText("");
                    ((TextView) findViewById(R.id.txbContent)).setText("");
                    txbClients.setContentDescription("0");
                }
            });

            mAdapterClients = new SimpleCursorAdapter(this, R.layout.simple_dropdown_item_multiline, null, new String[] { "Name" }, new int[] {android.R.id.text1}, 0);

            mAdapterClients.setFilterQueryProvider(new FilterQueryProvider() {
                public Cursor runQuery(CharSequence str) {
                    try {
                        Cursor cur = DL_Clients.Get(txbClients.getText().toString());
                        return cur;
                    }
                    catch (Exception ex) { }
                    return null;
                }
            });

            mAdapterClients.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
                public CharSequence convertToString(Cursor cur) {
                    try {
                        int index = cur.getColumnIndex("Name");
                        return cur.getString(index);
                    }
                    catch (Exception ex) {}
                    return "";
                }
            });

            txbClients.setAdapter(mAdapterClients);
            txbClients.setThreshold(0);

            txbClients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    Cursor cur = DL_Wurth.GET_Partner(l);

                    txbClients.setContentDescription(Long.toString(l));
                    if (cur != null) {

                        if (cur.moveToFirst()) {
                            txbID.setText(cur.getString(cur.getColumnIndex("IDBroj")));
                        }
                        cur.close();

                    }

                    InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(txbClients.getWindowToken(), 0);

                }
            });

        }
        catch (Exception ex) {

        }
    }

    private class LongTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            if (!((wurthMB) getApplication()).isNetworkAvailable()) {
                cancel(true);
                Notifications.showNotification(LRCActivity.this, "", getString(R.string.NoInternet), 1);
                return;
            }
            ((TextView) findViewById(R.id.txbContent)).setText("");
            findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("AccountID", Long.toString(wurthMB.getUser().AccountID)));
                postParameters.add(new BasicNameValuePair("ClientID", "0"));
                postParameters.add(new BasicNameValuePair("ClientCode", params[0]));

                String response = CustomHttpClient.executeHttpPost(wurthMB.getUser().URL + "GET_Client_LRC", postParameters).toString();
                return response;
            }
            catch (Exception e) {
                findViewById(R.id.progressContainer).setVisibility(View.GONE);
                Notifications.showNotification(LRCActivity.this, "", getString(R.string.ServiceNotAvailable),1);
                return "";
            }
        }

        @Override
        protected void onPostExecute(String params) {

            try {

                String html = "";

                JSONObject jsonObject = new JSONObject(params);

                Iterator<String> keysIterator = jsonObject.keys();
                while (keysIterator.hasNext())
                {
                    String keyStr = keysIterator.next();

                    html += "<b>" + keyStr + "</b>: ";

                    try {
                        JSONArray array = (JSONArray) jsonObject.get(keyStr);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject _jsonObject = array.getJSONObject(i);
                            Iterator<String> iterator = _jsonObject.keys();
                            while (iterator.hasNext()) {
                                html += "<br />";
                                String _keyStr = iterator.next();
                                html += "<b>&nbsp;&nbsp;" + _keyStr + "</b>: ";

                                try {
                                    JSONArray _array = (JSONArray) _jsonObject.get(_keyStr);
                                    for (int j = 0; j < _array.length(); j++) {
                                        JSONObject __jsonObject = _array.getJSONObject(j);
                                        Iterator<String> _iterator = __jsonObject.keys();
                                        while (_iterator.hasNext()) {
                                            html += "<br />";
                                            String __keyStr = _iterator.next();
                                            html += "&nbsp;&nbsp;&nbsp;&nbsp;<b>" + __keyStr + "</b>: ";


                                            try {
                                                JSONArray __array = (JSONArray) __jsonObject.get(__keyStr);
                                                for (int k = 0; k < __array.length(); k++) {
                                                    JSONObject ___jsonObject = __array.getJSONObject(k);
                                                    Iterator<String> __iterator = ___jsonObject.keys();
                                                    while (__iterator.hasNext()) {
                                                        html += "<br />";
                                                        String ___keyStr = __iterator.next();
                                                        html += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + ___keyStr + ": ";
                                                        html += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + ___jsonObject.getString(__keyStr);
                                                    }
                                                }
                                            }
                                            catch (Exception exx) {
                                                html += "&nbsp;&nbsp;&nbsp;&nbsp;" + __jsonObject.getString(__keyStr);
                                            }
                                        }
                                    }
                                }
                                catch (Exception exx) {
                                    html += "&nbsp;&nbsp;" + _jsonObject.getString(_keyStr);
                                }
                            }
                        }
                    }
                    catch (Exception exx) {
                        html += jsonObject.getString(keyStr);
                    }

                    html += "<br />";
                }

                ((TextView )findViewById(R.id.txbContent)).setText(Html.fromHtml(html));


            }
            catch (Exception e) {
                Notifications.showNotification(LRCActivity.this, "", getString(R.string.ServiceNotAvailable), 1);
            }

            findViewById(R.id.progressContainer).setVisibility(View.GONE);

        }
    }
}