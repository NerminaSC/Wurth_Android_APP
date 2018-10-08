package ba.wurth.mb.Activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import io.requery.android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.R;

public class LoginActivity extends Activity {
    private SQLiteDatabase db = wurthMB.dbHelper.getDB();
    public static LoginActivity me = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.login);

        me = this;

        findViewById(R.id.btnLogin).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                new ExportDatabaseFileTask().execute();
                return false;
            }
        });


        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            try {
                if (!((wurthMB) getApplication()).isNetworkAvailable()) {
                    Notifications.showNotification(getApplicationContext(), "", getString(R.string.NoInternet),2);
                    return;
                }

                HashMap<String, String> data = new HashMap<String, String>();
                data.put("EmailAddress", ((EditText) findViewById(R.id.txbUsername)).getText().toString() + "@wurth.ba");
                data.put("Password", ((EditText) findViewById(R.id.txbPassword)).getText().toString());
                data.put("Domain", ((EditText) findViewById(R.id.txbDomain)).getText().toString());
                NetworkTask asyncHttpPost = new NetworkTask(data);
                asyncHttpPost.execute("http://wurth.api.optimus.ba/services/wurth.asmx/Authenticate");
            }
            catch(Exception ex) {}
            }
        });


        findViewById(R.id.btnLogin).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {
                    if (!((wurthMB) getApplication()).isNetworkAvailable()) {
                        Notifications.showNotification(getApplicationContext(), "", getString(R.string.NoInternet),2);
                        return true;
                    }

                    HashMap<String, String> data = new HashMap<String, String>();
                    data.put("EmailAddress", ((EditText) findViewById(R.id.txbUsername)).getText().toString() + "@wurth.ba");
                    data.put("Password", ((EditText) findViewById(R.id.txbPassword)).getText().toString());
                    data.put("Domain", ((EditText) findViewById(R.id.txbDomain)).getText().toString());
                    NetworkTask asyncHttpPost = new NetworkTask(data);
                    asyncHttpPost.execute("http://beta.ws.optimus.ba/MB/Wurth.asmx/Authenticate");
                }
                catch(Exception ex) {}

                return true;
            }
        });

        String[] array_spinner=new String[2];
        array_spinner[0]="BHS";
        array_spinner[1]="English";

        Spinner s = (Spinner) findViewById(R.id.spLanguages);
        ArrayAdapter<?> adapter = new ArrayAdapter<Object>(this, android.R.layout.simple_spinner_item, array_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = parent.getItemAtPosition(pos).toString();

                if (selected.equals("English")) wurthMB.currentLocale = new Locale("en");
                if (selected.equals("BHS")) wurthMB.currentLocale = new Locale("ba");

                Configuration config = getBaseContext().getResources().getConfiguration();

                if (wurthMB.getLocale() != null)
                {
                    config.locale = wurthMB.getLocale();
                    Locale.setDefault(wurthMB.getLocale());
                }
                else {
                    config.locale = Locale.getDefault();
                    Locale.setDefault(Locale.getDefault());
                }

                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

            }
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        wurthMB.currentLocale = new Locale("ba");

        Configuration config = getBaseContext().getResources().getConfiguration();

        if (wurthMB.getLocale() != null)
        {
            config.locale = wurthMB.getLocale();
            Locale.setDefault(wurthMB.getLocale());
        }
        else {
            config.locale = Locale.getDefault();
            Locale.setDefault(Locale.getDefault());
        }

        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent exit_intent = new Intent(this, FinishActivity.class);
            exit_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(exit_intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class NetworkTask extends AsyncTask<String, Void, String> {

        private HashMap<String, String> mData = null;

        public NetworkTask(HashMap<String, String> data) {
            mData = data;
        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try {
                // set up post data
                ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                Iterator<String> it = mData.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    nameValuePair.add(new BasicNameValuePair(key, mData.get(key)));
                }
                response = CustomHttpClient.executeHttpPost(params[0], nameValuePair).toString();

            }
            catch (Exception e) {

            }
            return response;
        }

        @Override
        protected void onPreExecute() {
            try {
                Notifications.showLoading(LoginActivity.this);
            }
            catch (Exception e) {
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json = new JSONObject(result);
                Long UserID = json.getLong("UserID");

                if (UserID > 0) {
                    ContentValues cv = new ContentValues();
                    cv.put("AccountID", json.getLong("AccountID"));
                    cv.put("AccountName", json.getString("AccountName"));
                    cv.put("UserID", json.getLong("UserID"));
                    cv.put("_userid", json.getLong("_id"));
                    cv.put("Lastname", json.getString("Lastname"));
                    cv.put("Firstname", json.getString("Firstname"));
                    cv.put("EmailAddress", json.getString("EmailAddress"));
                    cv.put("AccessLevelID", json.getInt("AccessLevelID"));
                    cv.put("DiscountPercentage", json.getDouble("DiscountPercentage"));
                    cv.put("PaymentDelay", json.getInt("PaymentDelay"));
                    cv.put("DeliveryDelay", json.getInt("DeliveryDelay"));
                    cv.put("hasDeliveryPlaces", json.getInt("hasDeliveryPlaces"));
                    cv.put("URL", json.getString("URL"));
                    cv.put("Parameters", json.getString("Parameters"));
                    cv.put("AccessLevelID", json.getInt("AccessLevelID"));

                    if (wurthMB.getLocale() != null) cv.put("Language", wurthMB.getLocale().getLanguage());
                    else {
                        cv.put("Language", "ba");
                        wurthMB.currentLocale = new Locale("ba");
                    }

                    cv.put("LoginDate", new Date().getTime());
                    cv.put("SignOutDate", 0);

                    if (wurthMB.currentBestLocation != null) {
                        cv.put("Latitude", (long) (wurthMB.currentBestLocation.getLatitude() * 10000000));
                        cv.put("Longitude", (long) (wurthMB.currentBestLocation.getLongitude() * 10000000));
                    }

                    db.beginTransaction();
                    db.insert("Logins", null, cv);
                    db.setTransactionSuccessful();
                    db.endTransaction();

                    Intent k = new Intent(LoginActivity.this, IntroActivity.class);
                    startActivity(k);
                    Notifications.hideLoading(LoginActivity.this);
                    finish();

                } else if (UserID == 0) {
                    Notifications.hideLoading(LoginActivity.this);
                    Notifications.showNotification(LoginActivity.this, "", getString(R.string.InvalidUsernamePassword),1);
                }
                else {
                    Notifications.hideLoading(LoginActivity.this);
                    Notifications.showNotification(LoginActivity.this, "", getString(R.string.ServiceOutOfOrder),1);
                }
            } catch (Exception e) {
                Notifications.hideLoading(LoginActivity.this);
                Notifications.showNotification(LoginActivity.this, "", getString(R.string.ServiceNotAvailable),1);
            }

            if (db.inTransaction()) db.endTransaction();

        }
    }



    public static final String PACKAGE_NAME = "ba.wurth.mb";
    public static final String DATABASE_NAME = "wurthMB.db";
    protected static final File DATABASE_DIRECTORY = new File(Environment.getExternalStorageDirectory(), "OptimusMB");


    protected static boolean exportDb(){

        if( ! SdIsPresent() ) return false;

        File dbFile = null;
        try {
            dbFile = new File(me.getPackageManager().getPackageInfo(me.getPackageName(), 0).applicationInfo.dataDir + "/databases/" + DATABASE_NAME );
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String filename = "wurthMB.db";

        File exportDir = DATABASE_DIRECTORY;
        File file = new File(exportDir, filename);

        if (!exportDir.exists()) exportDir.mkdirs();

        try {
            file.createNewFile();
            copyFile(dbFile, file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    /** Returns whether an SD card is present and writable **/
    public static boolean SdIsPresent() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public class ExportDatabaseFileTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            Notifications.showLoading(LoginActivity.this);
        }

        @Override
        protected Void doInBackground(final Void... args) {
            return null;

        }

        @Override
        protected void onPostExecute(final Void success) {

            exportDb();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"export@sourcecode.ba"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "OptimusMB - Database Backup");
            intent.putExtra(Intent.EXTRA_TEXT, "");
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(DATABASE_DIRECTORY, "wurthMB.db")));
            startActivity(Intent.createChooser(intent, "Send Email"));
            Notifications.hideLoading(LoginActivity.this);
        }
    }
}
