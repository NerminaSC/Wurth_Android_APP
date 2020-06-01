package ba.wurth.mb.Classes;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Order;
import ba.wurth.mb.Classes.Objects.Temp;
import ba.wurth.mb.Classes.Objects.User;
import ba.wurth.mb.DataLayer.DBHelper;
import ba.wurth.mb.DataLayer.Temp.DL_Temp;

//@ReportsCrashes(formKey = "dE9Xak5zdXg4UElhakhBclFfeGttaEE6MA")
@ReportsCrashes(formKey = "")
//@ReportsCrashes(formKey = "", mailTo = "optimus_exceptions@sourcecode.ba", mode = ReportingInteractionMode.SILENT)
public class wurthMB extends Application {

    private static User currentUser;
    private static Client currentClient;
    private static Order currentOrder;
    public static Locale currentLocale;

    public static DBHelper dbHelper;
    public static ArrayList<ba.wurth.mb.Classes.Objects.ErrorObject> ErrorList;
    public static Location currentBestLocation = null;
    public static String IMEI = "0";
    public static String App_Version = "0";
    public static boolean loadComplete = false;
    public static boolean load7Day = false;
    public static boolean loadMonth = false;

    public static ObjectMapper mapperTemp;
    public static ObjectReader readerTemp;
    public static ObjectWriter writterTemp;

    public static boolean MOBILE_DATA = false;

    private static wurthMB instance = null;

    public static ImageLoader imageLoader;

    public wurthMB() {
        try {
            instance = this;
            ErrorList = new ArrayList<ba.wurth.mb.Classes.Objects.ErrorObject>();
        }
        catch (Exception ex) {
            AddError("optimusMB_Application", ex.getMessage(), ex);
        }
    }

    public static wurthMB getInstance() {
        return instance;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void AddError(final String _Title,final String _Message, final Exception _ex){
        if (_ex != null){
            String[] params = {_Title, _Message, _ex.getMessage()};
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) new LongTask().execute(params);
            else new LongTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }
    }

    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) MOBILE_DATA = true;
            else MOBILE_DATA = false;

            if (activeNetwork != null) return activeNetwork.isConnectedOrConnecting();
        }
        catch (Exception e) { }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        if (getLocale() != null)
        {
            Locale.setDefault(getLocale());
            Configuration config = new Configuration(newConfig); // get Modifiable Config from actual config changed
            config.locale = getLocale();
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        try {

            ACRA.init(this);
            MyCrashSender crashSender = new MyCrashSender();
            ACRA.getErrorReporter().setReportSender(crashSender);

            dbHelper = DBHelper.getHelper(instance);

            JsonFactory jsonFactory = new JsonFactory();
            jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, true);

            mapperTemp = new ObjectMapper(jsonFactory);
            mapperTemp.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
            mapperTemp.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false);
            mapperTemp.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
            mapperTemp.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

            readerTemp = mapperTemp.reader(Temp.class);
            writterTemp = mapperTemp.writer();

            SharedPreferences prefs = this.getSharedPreferences("optimusMBprefs", MODE_PRIVATE);

            SYNC_ENABLED = prefs.getInt("SyncEnabled", -1) == 0 ? false : true;
            USE_3G = prefs.getInt("Use3G", -1) == 0 ? false : true;
            SYNC_LOCATION = prefs.getInt("SyncLocations", -1) == 0 ? false : true;
            USE_3G_DOCUMENTS = prefs.getInt("Sync3GDocuments", -1) == 0 ? false : true;
            LOCATION_SERVICE_ENABLED = prefs.getInt("LocationServiceEnabled", -1) == 0 ? false : true;

            LOCATION_SERVICE_INTERVAL = prefs.getInt("LocationInterval", 5 * 60 * 1000);
            SYNC_INTERVAL = prefs.getInt("ActivitesInterval", 30 * 1000);
            SYNC_INTERVAL_SERVER = prefs.getInt("ServerInterval", 6 * 60 * 60 * 1000);

            imageLoader = new ImageLoader(this.getApplicationContext());
        }

        catch (Exception ex) {
            AddError("optimusMB_Application_OnCreate", ex.getMessage(), ex);
        }
    }

    public static User getUser()
    {
        if (currentUser == null || currentUser.UserID == 0L) {

            Cursor cur = dbHelper.getDB().rawQuery("SELECT * FROM Logins WHERE SignOutDate = 0", null);

            if (cur != null && cur.getCount() > 0 && cur.moveToFirst()) {

                currentUser = new User();
                currentUser.UserID = cur.getInt(cur.getColumnIndex("UserID"));
                currentUser._userid = cur.getInt(cur.getColumnIndex("_userid"));
                currentUser.Lastname = cur.getString(cur.getColumnIndex("Lastname"));
                currentUser.Firstname = cur.getString(cur.getColumnIndex("Firstname"));
                currentUser.AccountID = cur.getLong(cur.getColumnIndex("AccountID"));
                currentUser.AccountName = cur.getString(cur.getColumnIndex("AccountName"));
                currentUser.AccessLevelID = cur.getInt(cur.getColumnIndex("AccessLevelID"));
                currentUser.DiscountPercentage = cur.getDouble(cur.getColumnIndex("DiscountPercentage"));
                currentUser.PaymentDelay = cur.getInt(cur.getColumnIndex("PaymentDelay"));
                currentUser.DeliveryDelay = cur.getInt(cur.getColumnIndex("DeliveryDelay"));
                currentUser.EmailAddress = cur.getString(cur.getColumnIndex("EmailAddress"));
                currentUser.URL = cur.getString(cur.getColumnIndex("URL"));

                try {
                    currentUser.Parameters = new JSONObject(cur.getString(cur.getColumnIndex("Parameters")));

                    if (!currentUser.Parameters.isNull("Accounts")) {
                        currentUser.ApplicationAccountID_EMS = currentUser.Parameters.getJSONObject("Accounts").getLong("ApplicationAccountID_EMS");
                        currentUser.ApplicationAccountID_OPTIMUS = currentUser.Parameters.getJSONObject("Accounts").getLong("ApplicationAccountID_OPTIMUS");
                        currentUser.ApplicationUserID_EMS = currentUser.Parameters.getJSONObject("Accounts").getLong("ApplicationUserID_EMS");
                        currentUser.ApplicationUserID_OPTIMUS = currentUser.Parameters.getJSONObject("Accounts").getLong("ApplicationUserID_OPTIMUS");
                    }

                } catch (JSONException e) { }

                try{
                    currentUser.data = new JSONObject(cur.getString(cur.getColumnIndex("data")));

                }catch (Exception e){

                }

                if (!cur.getString(cur.getColumnIndex("Language")).equals("")){
                    currentLocale = new Locale(cur.getString(cur.getColumnIndex("Language")));
                    currentUser.locale = new Locale(cur.getString(cur.getColumnIndex("Language")));
                }
                else {
                    currentLocale = Locale.getDefault();
                    currentUser.locale = Locale.getDefault();
                }

                try {
                    Cursor _cur = dbHelper.getDB().rawQuery("SELECT Region FROM KOMERCIJALISTI WHERE ID = " + currentUser._userid + " LIMIT 1", null);
                    if (_cur != null) {
                        if (_cur.moveToFirst()) {
                            currentUser.Region = _cur.getString(0);
                        }
                        _cur.close();
                    }
                } catch (Exception e) {

                }
            }
            if (cur != null) cur.close();
        }
        return currentUser;
    }
    public static Order getOrder() {
        if (currentOrder == null) {
            try {
                currentOrder = DL_Temp.GET_Order();
                //if (currentOrder != null && currentOrder.ClientID > 0) currentOrder.client = DL_Wurth.GET_Client(currentOrder.ClientID);
            } catch (Exception ex) {
                AddError("getOrder", ex.getMessage(), ex);
            }
        }
        return currentOrder;
    }

    public static Client getClient()
    {
        /*if (currentClient == null) {
            try {
                currentClient = DL_Temp.GET_Client();
            } catch (Exception ex) {
                AddError("GET_Client", ex.getMessage(), ex);
            }
        }*/
        return currentClient;
    }

    public static void setUser(User user)
    {
        currentUser = user;
    }

    public static void setOrder(Order order)
    {
        try {
            currentOrder = order;

            if (order == null) {
                DL_Temp.Delete();
                return;
            }

            Temp temp = new Temp();
            temp.order = order;
            DL_Temp.AddOrUpdate(temp);
        }
        catch (Exception ex) {
            AddError("setOrder", ex.getMessage(), ex);
        }
    }

    public static void setClient(Client client)
    {
        try {
            currentClient = client;
            Temp temp = DL_Temp.Get();
            if (temp == null && client != null) {
                temp = new Temp();
                temp.client = client;
                DL_Temp.AddOrUpdate(temp);
            }
            else if (temp != null && client == null) {
                temp.client = client;
                DL_Temp.AddOrUpdate(temp);
            }
        }
        catch (Exception ex) {
            AddError("setClient", ex.getMessage(), ex);
        }
    }

    public static Locale getLocale() {
        try {
            if (currentLocale == null ) {
                currentLocale = getUser().locale;
            }
        }
        catch (Exception e) { }
        return currentLocale;
    }


    /******************/
    /*** CONST SYNC ***/
    /******************/

    public static boolean SYNC_ENABLED = true;
    public static boolean USE_3G = true;
    public static boolean SYNC_LOCATION = true;
    public static boolean USE_3G_DOCUMENTS = true;
    public static boolean LOCATION_SERVICE_ENABLED = true;

    public static int SYNC_INTERVAL = 30 * 1000; // 30 seconds
    public static int SYNC_INTERVAL_SERVER = 1 * 60 * 60 * 1000; // 1 hour
    public static int LOCATION_SERVICE_INTERVAL = 5 * 60 * 1000; // 5 minutes

    private static class LongTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... params) {

            try {
                if (wurthMB.getUser() != null) {
                    JSONObject tempObject = new JSONObject();
                    tempObject.put("Message", params[0] + " <br /> " + params[1] + " <br /> " + params[2] + " <br />Account: " + wurthMB.getUser().AccountID + " <br />" + "User: " + wurthMB.getUser().Firstname + " " + wurthMB.getUser().Lastname + " <br />" + "App Version: " + wurthMB.App_Version + " <br />" + "IMEI: " + wurthMB.IMEI);
                    tempObject.put("UserID", wurthMB.getUser().UserID);
                    tempObject.put("UserName", wurthMB.getUser().Firstname + " " + wurthMB.getUser().Lastname);
                    tempObject.put("IMEI", wurthMB.IMEI);
                    tempObject.put("Version", wurthMB.App_Version);

                    ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                    postParameters.add(new BasicNameValuePair("jsonObj", tempObject.toString()));
                    CustomHttpClient.executeHttpPost(wurthMB.getUser().URL + "LogErrors", postParameters).toString();
                }
            }
            catch (Exception ex) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {

        }
    }

    public class MyCrashSender implements ReportSender {
        @Override
        public void send(CrashReportData report) throws ReportSenderException {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Message", report.toJSON());
                jsonObject.put("UserID", wurthMB.getUser().UserID);

                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("jsonObj", jsonObject.toString()));
                CustomHttpClient.executeHttpPost(wurthMB.getUser().URL + "LogErrors", postParameters).toString();
            }
            catch (Exception ex) {

            }
        }
    }
}
