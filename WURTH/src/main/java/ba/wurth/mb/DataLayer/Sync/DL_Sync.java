package ba.wurth.mb.DataLayer.Sync;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import ba.wurth.mb.Activities.IntroActivity;
import ba.wurth.mb.Classes.Base64;
import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.DeliveryPlace;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Fragments.Synchronization.SyncFragment;
import io.requery.android.database.sqlite.SQLiteDatabase;
import io.requery.android.database.sqlite.SQLiteStatement;

public class DL_Sync {
    private static 	String methodName = "";
    private static String className = "DL_Sync";

    private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
    private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

    public static SyncFragment.SyncTask mThreadReference = null;
    public static IntroActivity.checkDatabase mThreadReferenceIntro = null;

    public static int Load_Additional() {
        try {

            methodName = "Load_Additional";

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("AccountID", Long.toString(wurthMB.getUser().AccountID)));

            String response = CustomHttpClient.executeHttpPost(wurthMB.getUser().URL + "GET_Additional", postParameters).toString();

            if (response == null || response.equals("")) return 0;

            db.beginTransactionNonExclusive();

            try {

                JSONObject jObject = new JSONObject(response);

                JSONArray jArrayUOM = jObject.getJSONArray("UOM");
                JSONArray jArrayOrderStatus = jObject.getJSONArray("OrderStatus");
                JSONArray jArrayPaymentMethods = jObject.getJSONArray("PaymentMethods");
                JSONArray PaymentDates = jObject.getJSONArray("PaymentDates");
                JSONArray jArrayTax = jObject.getJSONArray("Tax");
                JSONArray jArrayDiscountGroups = jObject.getJSONArray("DiscountGroups");
                JSONArray jArrayDiscountGroupActions = jObject.getJSONArray("DiscountGroupActions");
                JSONArray jArrayDiscountClientProducts = jObject.getJSONArray("DiscountClientProducts");
                JSONArray jArrayNotificationMessages = jObject.getJSONArray("NotificationMessages");
                JSONArray jArrayDeliveryPlacesProperties = jObject.getJSONArray("DeliveryPlacesProperties");
                JSONArray jArrayDeliveryPlacesPropertiesOptions = jObject.getJSONArray("DeliveryPlacesPropertiesOptions");
                JSONArray jArrayClientDeliveryPlacesProperties = jObject.getJSONArray("ClientDeliveryPlacesProperties");
                JSONArray jArrayMerchandise = jObject.getJSONArray("Merchandise");
                JSONArray jArrayDeliveryPlaceMerchandise = jObject.getJSONArray("DeliveryPlaceMerchandise");
                JSONArray jArrayCompetitions = jObject.getJSONArray("Competitions");
                JSONArray jArrayDeliveryPlaceProductPlacement = jObject.getJSONArray("DeliveryPlaceProductPlacement");

                db.delete("OrderStatus", null, null);
                for (int i = 0; i < jArrayOrderStatus.length(); i++) {
                    JSONObject json_data = jArrayOrderStatus.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("OrderStatusID", json_data.getInt("OrderStatusID"));
                    values.put("Name", json_data.getString("Name"));
                    values.put("Color", json_data.getString("Color"));
                    db.insert("OrderStatus", null, values);
                }

				/* Povrat */
                ContentValues tempValues = new ContentValues();
                tempValues.put("OrderStatusID", 10);
                tempValues.put("Name", "Povrat");
                tempValues.put("Color", "#000000");
                db.insert("OrderStatus", null, tempValues);

                db.delete("UOM", null, null);
                for (int i = 0; i < jArrayUOM.length(); i++) {
                    JSONObject json_data = jArrayUOM.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("UOMID", json_data.getInt("UomID"));
                    values.put("Name", json_data.getString("Name"));
                    db.insert("UOM", null, values);
                }

                db.delete("PaymentMethods", "AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
                for (int i = 0; i < jArrayPaymentMethods.length(); i++) {
                    JSONObject json_data = jArrayPaymentMethods.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("AccountID", json_data.getInt("AccountID"));
                    values.put("PaymentMethodID", json_data.getInt("PaymentMethodID"));
                    values.put("Name", json_data.getString("Name"));
                    values.put("Description", json_data.getString("Description"));
                    db.insert("PaymentMethods", null, values);

                    JSONArray jArrayClientPaymentMethods = json_data.getJSONArray("Clients");
                    db.delete("ClientPaymentMethods", "PaymentMethodID=?", new String[]{json_data.getString("PaymentMethodID")});
                    for (int x = 0; x < jArrayClientPaymentMethods.length(); x++) {
                        ContentValues _values = new ContentValues();
                        _values.put("PaymentMethodID", json_data.getInt("PaymentMethodID"));
                        _values.put("ClientID", Long.toString((Integer) jArrayClientPaymentMethods.get(x)));
                        db.insert("ClientPaymentMethods", null, _values);
                    }
                }

                db.delete("PaymentDates", "AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
                for (int i = 0; i < PaymentDates.length(); i++) {
                    JSONObject json_data = PaymentDates.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("AccountID", json_data.getInt("AccountID"));
                    values.put("PaymentDateID", json_data.getInt("PaymentDateID"));
                    values.put("Name", json_data.getString("Name"));
                    values.put("Description", json_data.getString("Description"));
                    values.put("Delay", json_data.getInt("Delay"));
                    db.insert("PaymentDates", null, values);

                    JSONArray jArrayClientPaymentDates = json_data.getJSONArray("Clients");
                    db.delete("ClientPaymentDates", "PaymentDateID=?", new String[]{json_data.getString("PaymentDateID")});
                    for (int x = 0; x < jArrayClientPaymentDates.length(); x++) {
                        JSONObject _json_data = jArrayClientPaymentDates.getJSONObject(x);
                        ContentValues _values = new ContentValues();
                        _values.put("PaymentDateID", json_data.getInt("PaymentDateID"));
                        _values.put("ClientID", _json_data.getInt("ClientID"));
                        _values.put("ProductCategoryID", _json_data.getInt("ProductCategoryID"));
                        _values.put("ProductID", _json_data.getInt("ProductID"));
                        db.insert("ClientPaymentDates", null, _values);
                    }
                }

                db.delete("Tax", null, null);
                for (int i = 0; i < jArrayTax.length(); i++) {
                    JSONObject json_data = jArrayTax.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("TaxID", json_data.getInt("TaxID"));
                    values.put("Name", json_data.getString("Name"));
                    values.put("Description", json_data.getString("Description"));
                    values.put("Percentage", json_data.getDouble("Percentage"));
                    db.insert("Tax", null, values);
                }

                db.delete("DiscountGroups", "AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
                for (int i = 0; i < jArrayDiscountGroups.length(); i++) {
                    JSONObject json_data = jArrayDiscountGroups.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("DiscountGroupID", json_data.getInt("DiscountGroupID"));
                    values.put("AccountID", json_data.getInt("AccountID"));
                    values.put("Name", json_data.getString("Name"));
                    values.put("Description", json_data.getString("Description"));
                    values.put("Percentage", json_data.getDouble("Percentage"));
                    values.put("DeliveryDelay", json_data.getInt("DeliveryDelay"));
                    values.put("PaymentDelay", json_data.getInt("PaymentDelay"));
                    db.insert("DiscountGroups", null, values);

                    JSONArray jArrayDiscountGroupClients = json_data.getJSONArray("Clients");
                    db.delete("DiscountGroupClients", "DiscountGroupID=?", new String[]{json_data.getString("DiscountGroupID")});
                    for (int x = 0; x < jArrayDiscountGroupClients.length(); x++) {
                        ContentValues _values = new ContentValues();
                        _values.put("DiscountGroupID", json_data.getInt("DiscountGroupID"));
                        _values.put("ClientID", Long.toString((Integer) jArrayDiscountGroupClients.get(x)));
                        db.insert("DiscountGroupClients", null, _values);
                    }

                    JSONArray jArrayDiscountGroupProducts = json_data.getJSONArray("Products");
                    db.delete("DiscountGroupProducts", "DiscountGroupID=?", new String[]{json_data.getString("DiscountGroupID")});
                    for (int x = 0; x < jArrayDiscountGroupProducts.length(); x++) {
                        JSONObject _json_data = jArrayDiscountGroupProducts.getJSONObject(x);
                        ContentValues _values = new ContentValues();
                        _values.put("DiscountGroupID", json_data.getInt("DiscountGroupID"));
                        _values.put("ProductID", _json_data.getLong("ProductID"));
                        _values.put("ProductGroupID", _json_data.getLong("ProductGroupID"));
                        _values.put("Percentage", _json_data.getDouble("Percentage"));
                        _values.put("PaymentDelay", _json_data.getInt("PaymentDelay"));
                        _values.put("DeliveryDelay", _json_data.getInt("DeliveryDelay"));
                        db.insert("DiscountGroupProducts", null, _values);
                    }
                }

                db.delete("DiscountGroupActions", "AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
                for (int i = 0; i < jArrayDiscountGroupActions.length(); i++) {
                    JSONObject json_data = jArrayDiscountGroupActions.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("DiscountGroupActionID", json_data.getInt("DiscountGroupActionID"));
                    values.put("AccountID", json_data.getInt("AccountID"));
                    values.put("Name", json_data.getString("Name"));
                    values.put("Description", json_data.getString("Description"));
                    values.put("Percentage", json_data.getDouble("Percentage"));
                    values.put("DeliveryDelay", json_data.getInt("DeliveryDelay"));
                    values.put("PaymentDelay", json_data.getInt("PaymentDelay"));
                    values.put("startDate", json_data.getInt("startDate"));
                    values.put("endDate", json_data.getInt("endDate"));
                    db.insert("DiscountGroupActions", null, values);

                    JSONArray jArrayDiscountGroupClients = json_data.getJSONArray("Clients");
                    db.delete("DiscountGroupActionClients", "DiscountGroupActionID=?", new String[]{json_data.getString("DiscountGroupActionID")});
                    for (int x = 0; x < jArrayDiscountGroupClients.length(); x++) {
                        ContentValues _values = new ContentValues();
                        _values.put("DiscountGroupActionID", json_data.getInt("DiscountGroupActionID"));
                        _values.put("ClientID", Long.toString((Integer) jArrayDiscountGroupClients.get(x)));
                        db.insert("DiscountGroupActionClients", null, _values);
                    }

                    JSONArray jArrayDiscountGroupProducts = json_data.getJSONArray("Products");
                    db.delete("DiscountGroupActionProducts", "DiscountGroupActionID=?", new String[]{json_data.getString("DiscountGroupActionID")});
                    for (int x = 0; x < jArrayDiscountGroupProducts.length(); x++) {
                        JSONObject _json_data = jArrayDiscountGroupProducts.getJSONObject(x);
                        ContentValues _values = new ContentValues();
                        _values.put("DiscountGroupActionID", json_data.getInt("DiscountGroupActionID"));
                        _values.put("ProductID", _json_data.getLong("ProductID"));
                        _values.put("ProductGroupID", _json_data.getLong("ProductGroupID"));
                        _values.put("Percentage", _json_data.getDouble("Percentage"));
                        _values.put("PaymentDelay", _json_data.getInt("PaymentDelay"));
                        _values.put("DeliveryDelay", _json_data.getInt("DeliveryDelay"));
                        db.insert("DiscountGroupActionProducts", null, _values);
                    }
                }

                db.delete("DiscountClientProducts", "AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
                for (int i = 0; i < jArrayDiscountClientProducts.length(); i++) {
                    JSONObject json_data = jArrayDiscountClientProducts.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("AccountID", json_data.getInt("AccountID"));
                    values.put("ClientID", json_data.getInt("ClientID"));
                    values.put("ProductID", json_data.getInt("ProductID"));
                    values.put("Discount1", json_data.getDouble("Discount1"));
                    values.put("Discount2", json_data.getDouble("Discount2"));
                    values.put("Discount3", json_data.getDouble("Discount3"));
                    values.put("Discount4", json_data.getDouble("Discount4"));
                    values.put("Discount5", json_data.getDouble("Discount5"));
                    db.insert("DiscountClientProducts", null, values);
                }

                db.delete("NotificationMessages", "AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
                for (int i = 0; i < jArrayNotificationMessages.length(); i++) {
                    JSONObject json_data = jArrayNotificationMessages.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("MessageID", json_data.getInt("MessageID"));
                    values.put("AccountID", json_data.getInt("AccountID"));
                    values.put("Message", json_data.getString("Message"));
                    values.put("Type", json_data.getInt("Type"));
                    db.insert("NotificationMessages", null, values);
                }

                db.delete("DeliveryPlacesProperties", "AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
                for (int i = 0; i < jArrayDeliveryPlacesProperties.length(); i++) {
                    JSONObject json_data = jArrayDeliveryPlacesProperties.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("DPPID", json_data.getInt("DPPID"));
                    values.put("AccountID", wurthMB.getUser().AccountID);
                    values.put("code", json_data.getString("code"));
                    values.put("Name", json_data.getString("Name"));
                    values.put("Description", json_data.getString("Description"));
                    values.put("Active", json_data.getInt("Active"));
                    values.put("DOE", json_data.getLong("DOE"));
                    db.insert("DeliveryPlacesProperties", null, values);
                }

                db.delete("DeliveryPlacesPropertiesOptions", "AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
                for (int i = 0; i < jArrayDeliveryPlacesPropertiesOptions.length(); i++) {
                    JSONObject json_data = jArrayDeliveryPlacesPropertiesOptions.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("AccountID", wurthMB.getUser().AccountID);
                    values.put("DPPID", json_data.getInt("DPPID"));
                    values.put("DPPOID", json_data.getInt("DPPOID"));
                    values.put("code", json_data.getString("code"));
                    values.put("Name", json_data.getString("Name"));
                    values.put("Description", json_data.getString("Description"));
                    values.put("Active", json_data.getInt("Active"));
                    values.put("DOE", json_data.getLong("DOE"));
                    db.insert("DeliveryPlacesPropertiesOptions", null, values);
                }

                db.delete("ClientDeliveryPlacesProperties", "AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
                for (int i = 0; i < jArrayClientDeliveryPlacesProperties.length(); i++) {
                    JSONObject json_data = jArrayClientDeliveryPlacesProperties.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("AccountID", wurthMB.getUser().AccountID);
                    values.put("ObjectID", json_data.getLong("ObjectID"));
                    values.put("DPPID", json_data.getInt("DPPID"));
                    values.put("DPPOID", json_data.getInt("DPPOID"));
                    values.put("DOE", json_data.getLong("DOE"));
                    values.put("Sync", 1);
                    db.insert("ClientDeliveryPlacesProperties", null, values);
                }


                db.delete("Merchandise", "AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
                for (int i = 0; i < jArrayMerchandise.length(); i++) {
                    JSONObject json_data = jArrayMerchandise.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("AccountID", wurthMB.getUser().AccountID);
                    values.put("MerchandiseID", json_data.getLong("MerchandiseID"));
                    values.put("Name", json_data.getString("Name"));
                    values.put("Description", json_data.getString("Description"));
                    values.put("Active", json_data.getInt("Active"));
                    db.insert("Merchandise", null, values);
                }

                db.delete("DeliveryPlaceMerchandise", "AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
                for (int i = 0; i < jArrayDeliveryPlaceMerchandise.length(); i++) {
                    JSONObject json_data = jArrayDeliveryPlaceMerchandise.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("AccountID", wurthMB.getUser().AccountID);
                    values.put("MerchandiseID", json_data.getLong("MerchandiseID"));
                    values.put("DeliveryPlaceID", json_data.getLong("DeliveryPlaceID"));
                    values.put("Notes", json_data.getString("Notes"));
                    values.put("Count", json_data.getDouble("Count"));
                    values.put("Type", json_data.getInt("Type"));
                    values.put("DOE", json_data.getLong("DOE"));
                    values.put("Sync", 1);
                    db.insert("DeliveryPlaceMerchandise", null, values);
                }


                db.delete("Competition_Items_Values", "AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
                for (int i = 0; i < jArrayCompetitions.length(); i++) {
                    JSONObject json_data = jArrayCompetitions.getJSONObject(i);

                    ContentValues values = new ContentValues();
                    values.put("CompetitionID", json_data.getLong("CompetitionID"));
                    values.put("AccountID", json_data.getLong("AccountID"));
                    values.put("Address", json_data.getString("Address"));
                    values.put("City", json_data.getString("City"));
                    values.put("CountryID", json_data.getLong("CountryID"));
                    values.put("Description", json_data.getString("Description"));
                    values.put("EmailAddress", json_data.getString("EmailAddress"));
                    values.put("Fax", json_data.getString("Fax"));
                    values.put("Latitude", json_data.getLong("Latitude"));
                    values.put("Longitude", json_data.getLong("Longitude"));
                    values.put("Mobile", json_data.getString("Mobile"));
                    values.put("Name", json_data.getString("Name"));
                    values.put("Telephone", json_data.getString("Telephone"));
                    values.put("Website", json_data.getString("Website"));
                    values.put("WATNumber", json_data.getString("WATNumber"));
                    values.put("IDNumber", json_data.getString("IDNumber"));
                    values.put("PDVNumber", json_data.getString("PDVNumber"));
                    values.put("Owner", json_data.getString("Owner"));
                    values.put("code", json_data.getString("code"));
                    values.put("WATType", json_data.getLong("WATType"));
                    values.put("Active", json_data.getInt("IsDeleted") == 1 ? 0 : json_data.getInt("Active"));
                    values.put("DOE", json_data.getLong("DOE"));
                    values.put("Sync", 1);

                    Cursor cursor = db_readonly.rawQuery("select 1 from Competitions Where CompetitionID = ?", new String[]{ json_data.getString("CompetitionID") });
                    boolean exists = (cursor.getCount() > 0);
                    cursor.close();

                    if (!exists) db.insert("Competitions", null, values);
                    else db.update("Competitions", values, "CompetitionID = ?" , new String[]{ json_data.getString("CompetitionID") });


                    /*** Competition Items ***/
                    JSONArray jArrayCompetitionItems = json_data.getJSONArray("Items");
                    for (int j = 0; j < jArrayCompetitionItems.length(); j++) {
                        JSONObject _json_data = jArrayCompetitionItems.getJSONObject(j);

                        ContentValues _values = new ContentValues();
                        _values.put("ItemID", _json_data.getLong("ItemID"));
                        _values.put("CompetitionID", _json_data.getLong("CompetitionID"));
                        _values.put("AccountID", wurthMB.getUser().AccountID);
                        _values.put("Name", _json_data.getString("Name"));
                        _values.put("Description", _json_data.getString("Description"));
                        _values.put("code", _json_data.getString("code"));
                        _values.put("OptionID", _json_data.getLong("OptionID"));
                        _values.put("Active", _json_data.getInt("IsDeleted") == 1 ? 0 : _json_data.getInt("Active"));
                        _values.put("DOE", _json_data.getLong("DOE"));
                        _values.put("Sync", 1);

                        Cursor _cursor = db_readonly.rawQuery("select 1 from Competition_Items Where ItemID = ?", new String[]{ _json_data.getString("ItemID") });
                        boolean _exists = (_cursor.getCount() > 0);
                        _cursor.close();

                        if (!_exists) db.insert("Competition_Items", null, _values);
                        else db.update("Competition_Items", _values, "ItemID = ?" , new String[]{ _json_data.getString("ItemID") });
                    }

                    /*** Competition Items Values ***/
                    JSONArray jArrayCompetitionItemsValues = json_data.getJSONArray("Values");
                    for (int j = 0; j < jArrayCompetitionItemsValues.length(); j++) {
                        JSONObject _json_data = jArrayCompetitionItemsValues.getJSONObject(j);

                        ContentValues _values = new ContentValues();
                        _values.put("ItemID", _json_data.getLong("ItemID"));
                        _values.put("DeliveryPlaceID", _json_data.getLong("DeliveryPlaceID"));
                        _values.put("AccountID", wurthMB.getUser().AccountID);
                        _values.put("Note", _json_data.getString("Note"));
                        _values.put("Value", _json_data.getDouble("Value"));
                        _values.put("Col1", _json_data.getDouble("Col1"));
                        _values.put("Col2", _json_data.getDouble("Col2"));
                        _values.put("Col3", _json_data.getDouble("Col3"));
                        _values.put("Col4", _json_data.getString("Col4"));
                        _values.put("Col5", _json_data.getString("Col5"));
                        _values.put("Col6", _json_data.getString("Col6"));
                        _values.put("UserID", _json_data.getLong("UserID"));
                        _values.put("DOE", _json_data.getLong("DOE"));
                        _values.put("Sync", 1);
                        db.insert("Competition_Items_Values", null, _values);
                    }
                }

                db.delete("DeliveryPlaceProductPlacement", "AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
                for (int i = 0; i < jArrayDeliveryPlaceProductPlacement.length(); i++) {
                    JSONObject json_data = jArrayDeliveryPlaceProductPlacement.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("AccountID", wurthMB.getUser().AccountID);
                    values.put("ProductCategoryID", json_data.getLong("ProductCategoryID"));
                    values.put("ProductID", json_data.getLong("ProductID"));
                    values.put("DeliveryPlaceID", json_data.getLong("DeliveryPlaceID"));
                    values.put("UserID", json_data.getLong("UserID"));
                    values.put("Note", json_data.getString("Note"));
                    values.put("Col1", json_data.getDouble("Col1"));
                    values.put("Col2", json_data.getDouble("Col2"));
                    values.put("Col3", json_data.getDouble("Col3"));
                    values.put("Col4", json_data.getString("Col4"));
                    values.put("Col5", json_data.getString("Col5"));
                    values.put("Col6", json_data.getString("Col6"));
                    values.put("DOE", json_data.getLong("DOE"));
                    values.put("Active", 1);
                    values.put("Sync", 1);
                    db.insert("DeliveryPlaceProductPlacement", null, values);
                }

                db.setTransactionSuccessful();
                db.endTransaction();

            } catch (JSONException e) {
                wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
                db.endTransaction();
                return -1;
            }
        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
            db.endTransaction();
            return -1;
        } finally {

        }
        return 1;
    }

    public static int Load_Clients(long userID, boolean load_complete) {

        int ret = 0;

        try {

            methodName = "Load_Clients_AsFile";

            long _DOE = 0;
            Cursor _cursor = db_readonly.rawQuery("SELECT MAX(DOE) FROM Clients", null);
            if (_cursor.moveToFirst()) _DOE = _cursor.getLong(0);
            _cursor.close();

            if (load_complete) _DOE = 0;

            if (wurthMB.loadMonth) _DOE = new Date().getTime() - (30 * 24 * 60 * 60 * 1000);

            if (mThreadReference != null) mThreadReference.doProgress("Download klijenata");
            if (mThreadReferenceIntro != null) mThreadReferenceIntro.doProgress("Download klijenata");

            try {

                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("UserID", Long.toString(userID)));
                postParameters.add(new BasicNameValuePair("DOE", Long.toString(_DOE + 1000)));

                JSONObject resp = new JSONObject(new CustomHttpClient().executeHttpPost("http://wurth.api.optimus.ba/services/wurth.asmx/GET_Clients_Count", postParameters));

                Integer total_count_to_transfer = resp.getInt("Clients_Count");
                Integer records_page_size = 1000;
                Integer records_page_count = 1;

                records_page_count = (total_count_to_transfer + records_page_size - 1) / records_page_size;

                db.beginTransactionNonExclusive();

                //** PARSE JSON **//
                if (mThreadReference != null) mThreadReference.doProgress("Unos klijenata");
                if (mThreadReferenceIntro != null) mThreadReferenceIntro.doProgress("Unos klijenata");

                for (int page = 0; page < records_page_count; page++) {

                    postParameters.clear();

                    postParameters.add(new BasicNameValuePair("UserID", Long.toString(userID)));
                    postParameters.add(new BasicNameValuePair("DOE", Long.toString(_DOE)));
                    postParameters.add(new BasicNameValuePair("offset", Long.toString(page * records_page_size)));
                    postParameters.add(new BasicNameValuePair("limit", Long.toString(records_page_size)));

                    JsonFactory jfactory = new JsonFactory();
                    JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpPostStream("http://wurth.api.optimus.ba/services/wurth.asmx/GET_Clients", postParameters));
                    JsonToken token = jsonParser.nextToken();

                    if (token == JsonToken.START_OBJECT) {

                        while (token != JsonToken.END_OBJECT) {

                            token = jsonParser.nextToken();

                            if (token == JsonToken.FIELD_NAME) {

                                /** Clients **/
                                if (0 == jsonParser.getCurrentName().compareToIgnoreCase("Clients")) {

                                    token = jsonParser.nextToken();

                                    if (token == JsonToken.START_ARRAY) {

                                        while (token != JsonToken.END_ARRAY) {

                                            token = jsonParser.nextToken();

                                            if (token == JsonToken.START_OBJECT) {

                                                Client tempClient = new Client();
                                                tempClient.AccountID = wurthMB.getUser().AccountID;
                                                tempClient.UserID = wurthMB.getUser().UserID;

                                                while (token != JsonToken.END_OBJECT) {

                                                    token = jsonParser.nextToken();

                                                    if (token == JsonToken.FIELD_NAME) {

                                                        String objectName = jsonParser.getCurrentName();

                                                        jsonParser.nextToken();

                                                        if (0 == objectName.compareToIgnoreCase("ClientID"))
                                                            tempClient.ClientID = jsonParser.getValueAsLong(0L);
                                                        if (0 == objectName.compareToIgnoreCase("_id"))
                                                            tempClient._clientid = jsonParser.getValueAsLong(0L);
                                                        if (0 == objectName.compareToIgnoreCase("UserID"))
                                                            tempClient.UserID = jsonParser.getValueAsLong(0L);
                                                        if (0 == objectName.compareToIgnoreCase("Address"))
                                                            tempClient.Address = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("CheckCreditLimit"))
                                                            tempClient.CheckCreditLimit = jsonParser.getValueAsInt(0);
                                                        if (0 == objectName.compareToIgnoreCase("City"))
                                                            tempClient.City = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("CountryID"))
                                                            tempClient.CountryID = jsonParser.getValueAsInt(0);
                                                        if (0 == objectName.compareToIgnoreCase("CreditLimit"))
                                                            tempClient.CreditLimit = jsonParser.getValueAsDouble(0D);
                                                        if (0 == objectName.compareToIgnoreCase("CurrentLimit"))
                                                            tempClient.CurrentLimit = jsonParser.getValueAsDouble(0D);
                                                        if (0 == objectName.compareToIgnoreCase("MaxOrderCount"))
                                                            tempClient.MaxOrderCount = jsonParser.getValueAsDouble(0D);
                                                        if (0 == objectName.compareToIgnoreCase("MaxOrderValue"))
                                                            tempClient.MaxOrderValue = jsonParser.getValueAsDouble(0D);
                                                        if (0 == objectName.compareToIgnoreCase("Description"))
                                                            tempClient.Description = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("EmailAddress"))
                                                            tempClient.EmailAddress = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("Fax"))
                                                            tempClient.Fax = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("Latitude"))
                                                            tempClient.Latitude = jsonParser.getValueAsLong(0L);
                                                        if (0 == objectName.compareToIgnoreCase("Longitude"))
                                                            tempClient.Longitude = jsonParser.getValueAsLong(0L);
                                                        if (0 == objectName.compareToIgnoreCase("Mobile"))
                                                            tempClient.Mobile = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("Name"))
                                                            tempClient.Name = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("Revenue"))
                                                            tempClient.Revenue = jsonParser.getValueAsDouble(0D);
                                                        if (0 == objectName.compareToIgnoreCase("Telephone"))
                                                            tempClient.Telephone = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("Website"))
                                                            tempClient.WebSite = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("WATNumber"))
                                                            tempClient.WATNumber = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("IDNumber"))
                                                            tempClient.IDNumber = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("PDVNumber"))
                                                            tempClient.PDVNumber = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("Owner"))
                                                            tempClient.Owner = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("code"))
                                                            tempClient.code = jsonParser.getValueAsString("");
                                                        if (0 == objectName.compareToIgnoreCase("WATType"))
                                                            tempClient.WATType = jsonParser.getValueAsInt(0);
                                                        if (0 == objectName.compareToIgnoreCase("DiscountPercentage") && !jsonParser.getValueAsString("").equals(""))
                                                            tempClient.DiscountPercentage = Double.parseDouble(jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("PaymentDelay") && !jsonParser.getValueAsString("").equals(""))
                                                            tempClient.PaymentDelay = Integer.parseInt(jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("DeliveryDelay") && !jsonParser.getValueAsString("").equals(""))
                                                            tempClient.DeliveryDelay = Integer.parseInt(jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("Active"))
                                                            tempClient.Active = jsonParser.getValueAsInt(0);
                                                        tempClient.Sync = 1;
                                                        if (0 == objectName.compareToIgnoreCase("DOE"))
                                                            tempClient.DOE = jsonParser.getValueAsLong(0L);
                                                        if (0 == objectName.compareToIgnoreCase("IsDeleted"))
                                                            tempClient.IsDeleted = jsonParser.getValueAsInt(0);

                                                        if (0 == objectName.compareToIgnoreCase("DeliveryPlaces")) {

                                                            JsonToken _token = jsonParser.getCurrentToken();

                                                            while (_token != JsonToken.END_ARRAY) {

                                                                _token = jsonParser.nextToken();

                                                                if (_token == JsonToken.START_OBJECT) {

                                                                    DeliveryPlace tempItem = new DeliveryPlace();
                                                                    tempItem.AccountID = wurthMB.getUser().AccountID;

                                                                    while (_token != JsonToken.END_OBJECT) {

                                                                        _token = jsonParser.nextToken();

                                                                        if (_token == JsonToken.FIELD_NAME) {

                                                                            String fieldname = jsonParser.getCurrentName();
                                                                            jsonParser.nextToken();

                                                                            if (0 == fieldname.compareToIgnoreCase("DeliveryPlaceID"))
                                                                                tempItem.DeliveryPlaceID = jsonParser.getValueAsLong(0L);
                                                                            if (0 == fieldname.compareToIgnoreCase("_id"))
                                                                                tempItem._deliveryplaceid = jsonParser.getValueAsLong(0L);
                                                                            if (0 == fieldname.compareToIgnoreCase("ClientID"))
                                                                                tempItem.ClientID = jsonParser.getValueAsLong(0L);
                                                                            if (0 == fieldname.compareToIgnoreCase("UserID"))
                                                                                tempItem.UserID = jsonParser.getValueAsLong(0L);
                                                                            if (0 == fieldname.compareToIgnoreCase("code"))
                                                                                tempItem.code = jsonParser.getValueAsString("");
                                                                            if (0 == fieldname.compareToIgnoreCase("Name"))
                                                                                tempItem.Name = jsonParser.getValueAsString("");
                                                                            if (0 == fieldname.compareToIgnoreCase("Address"))
                                                                                tempItem.Address = jsonParser.getValueAsString("");
                                                                            if (0 == fieldname.compareToIgnoreCase("City"))
                                                                                tempItem.City = jsonParser.getValueAsString();
                                                                            if (0 == fieldname.compareToIgnoreCase("ResponsiblePerson"))
                                                                                tempItem.ResponsiblePerson = jsonParser.getValueAsString("");
                                                                            if (0 == fieldname.compareToIgnoreCase("Telephone"))
                                                                                tempItem.Telephone = jsonParser.getValueAsString("");
                                                                            if (0 == fieldname.compareToIgnoreCase("Priority"))
                                                                                tempItem.Priority = jsonParser.getValueAsInt(0);
                                                                            if (0 == fieldname.compareToIgnoreCase("Active"))
                                                                                tempItem.Active = jsonParser.getValueAsInt(0);
                                                                            if (0 == fieldname.compareToIgnoreCase("DOE"))
                                                                                tempItem.DOE = jsonParser.getValueAsLong(0L);
                                                                            if (0 == fieldname.compareToIgnoreCase("Latitude"))
                                                                                tempItem.Latitude = jsonParser.getValueAsLong(0L);
                                                                            if (0 == fieldname.compareToIgnoreCase("Longitude"))
                                                                                tempItem.Longitude = jsonParser.getValueAsLong(0L);
                                                                            if (0 == fieldname.compareToIgnoreCase("IsDeleted"))
                                                                                tempItem.IsDeleted = jsonParser.getValueAsInt(0);
                                                                        }
                                                                    }
                                                                    tempClient.DeliveryPlaces.add(tempItem);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                ContentValues values = new ContentValues();
                                                values.put("ClientID", tempClient.ClientID);
                                                values.put("_clientid", tempClient._clientid);
                                                values.put("AccountID", tempClient.AccountID);
                                                values.put("UserID", tempClient.UserID);
                                                values.put("Address", tempClient.Address);
                                                values.put("CheckCreditLimit", tempClient.CheckCreditLimit);
                                                values.put("City", tempClient.City);
                                                values.put("CountryID", tempClient.CountryID);
                                                values.put("CreditLimit", tempClient.CreditLimit);
                                                values.put("CurrentLimit", tempClient.CurrentLimit);
                                                values.put("MaxOrderValue", tempClient.MaxOrderValue);
                                                values.put("MaxOrderCount", tempClient.MaxOrderCount);
                                                values.put("Description", tempClient.Description);
                                                values.put("EmailAddress", tempClient.EmailAddress);
                                                values.put("Fax", tempClient.Fax);
                                                values.put("Latitude", tempClient.Latitude);
                                                values.put("Longitude", tempClient.Longitude);
                                                values.put("Mobile", tempClient.Mobile);
                                                values.put("Name", tempClient.Name);
                                                values.put("Revenue", tempClient.Revenue);
                                                values.put("Telephone", tempClient.Telephone);
                                                values.put("Website", tempClient.WebSite);
                                                values.put("WATNumber", tempClient.WATNumber);
                                                values.put("IDNumber", tempClient.IDNumber);
                                                values.put("PDVNumber", tempClient.PDVNumber);
                                                values.put("Owner", tempClient.Owner);
                                                values.put("code", tempClient.code);
                                                values.put("WATType", tempClient.WATType);
                                                values.put("DiscountPercentage", tempClient.DiscountPercentage);
                                                values.put("PaymentDelay", tempClient.PaymentDelay);
                                                values.put("DeliveryDelay", tempClient.DeliveryDelay);
                                                values.put("Active", tempClient.IsDeleted == 1 ? 0 : tempClient.Active);
                                                values.put("DOE", tempClient.DOE);
                                                values.put("Sync", 1);

                                                if (tempClient.ClientID > 0) {
                                                    if (db.update("Clients", values, "ClientID = ?", new String[]{Long.toString(tempClient.ClientID)}) == 0)
                                                        db.insert("Clients", null, values);
                                                } else db.insert("Clients", null, values);

                                                /****************************/
                                                /** Update Delivery Places **/
                                                /****************************/

                                                java.util.Iterator<ba.wurth.mb.Classes.Objects.DeliveryPlace> itr = tempClient.DeliveryPlaces.iterator();
                                                while (itr.hasNext()) {
                                                    ba.wurth.mb.Classes.Objects.DeliveryPlace element = itr.next();
                                                    ContentValues _values = new ContentValues();
                                                    _values.put("DeliveryPlaceID", element.DeliveryPlaceID);
                                                    _values.put("_deliveryplaceid", element._deliveryplaceid);
                                                    _values.put("AccountID", element.AccountID);
                                                    _values.put("ClientID", element.ClientID);
                                                    _values.put("UserID", element.UserID);
                                                    _values.put("code", element.code);
                                                    _values.put("Name", element.Name);
                                                    _values.put("Address", element.Address);
                                                    _values.put("City", element.City);
                                                    _values.put("ResponsiblePerson", element.ResponsiblePerson);
                                                    _values.put("Telephone", element.Telephone);
                                                    _values.put("Priority", element.Priority);
                                                    _values.put("Latitude", element.Latitude);
                                                    _values.put("Longitude", element.Longitude);
                                                    _values.put("Active", element.IsDeleted == 1 ? 0 : element.Active);
                                                    _values.put("DOE", element.DOE);
                                                    _values.put("Sync", 1);

                                                    if (element.DeliveryPlaceID > 0) {
                                                        if (db.update("DeliveryPlaces", _values, "DeliveryPlaceID = ?", new String[]{Long.toString(element.DeliveryPlaceID)}) == 0)
                                                            db.insert("DeliveryPlaces", null, _values);
                                                    } else
                                                        db.insert("DeliveryPlaces", null, _values);
                                                }
                                                //ret++;
                                                //if (mThreadReference != null && ret % 100 == 0) mThreadReference.doProgress(Integer.toString(ret) + "/" + Integer.toString(total_count_to_transfer));

                                            }
                                        }
                                    }
                                }

                                /** end of clients **/

                                /** Partneri **/
                                if (0 == jsonParser.getCurrentName().compareToIgnoreCase("Partneri")) {

                                    token = jsonParser.nextToken();

                                    if (token == JsonToken.START_ARRAY) {

                                        while (token != JsonToken.END_ARRAY) {

                                            token = jsonParser.nextToken();

                                            if (token == JsonToken.START_OBJECT) {

                                                ContentValues cv = new ContentValues();

                                                while (token != JsonToken.END_OBJECT) {

                                                    token = jsonParser.nextToken();

                                                    if (token == JsonToken.FIELD_NAME) {

                                                        String objectName = jsonParser.getCurrentName();

                                                        jsonParser.nextToken();

                                                        if (0 == objectName.compareToIgnoreCase("Adresa"))
                                                            cv.put("Adresa", jsonParser.getValueAsString(""));
                                                        //if (0 == objectName.compareToIgnoreCase("BrojSudskogRegistra")) cv.put("BrojSudskogRegistra", jsonParser.getValueAsInt(0));
                                                        if (0 == objectName.compareToIgnoreCase("BrzaIsporuka"))
                                                            cv.put("BrzaIsporuka", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("Datum_Modifikacije"))
                                                            cv.put("Datum_Modifikacije", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("Drzava"))
                                                            cv.put("Drzava", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("EmailAdresa"))
                                                            cv.put("EmailAdresa", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("Fax"))
                                                            cv.put("Fax", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("Grad"))
                                                            cv.put("Grad", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("ID"))
                                                            cv.put("ID", jsonParser.getValueAsLong(0L));
                                                        if (0 == objectName.compareToIgnoreCase("IDBroj"))
                                                            cv.put("IDBroj", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("Kod"))
                                                            cv.put("Kod", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("KomercijalistaID"))
                                                            cv.put("KomercijalistaID", jsonParser.getValueAsLong(0L));
                                                        if (0 == objectName.compareToIgnoreCase("KreditniLimit"))
                                                            cv.put("KreditniLimit", jsonParser.getValueAsLong(0L));
                                                        if (0 == objectName.compareToIgnoreCase("Mobitel"))
                                                            cv.put("Mobitel", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("Naziv"))
                                                            cv.put("Naziv", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("Opis"))
                                                            cv.put("Opis", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("ParentID"))
                                                            cv.put("ParentID", jsonParser.getValueAsLong(0L));
                                                        if (0 == objectName.compareToIgnoreCase("PDVBroj"))
                                                            cv.put("PDVBroj", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("Potencijal"))
                                                            cv.put("Potencijal", jsonParser.getValueAsInt(0));
                                                        if (0 == objectName.compareToIgnoreCase("ProvjeraLimita"))
                                                            cv.put("ProvjeraLimita", jsonParser.getValueAsInt(0));
                                                        //if (0 == objectName.compareToIgnoreCase("SudskiRegistar")) cv.put("SudskiRegistar", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("Telefon"))
                                                            cv.put("Telefon", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("TrenutniLimit"))
                                                            cv.put("TrenutniLimit", jsonParser.getValueAsDouble(0D));
                                                        if (0 == objectName.compareToIgnoreCase("Veleprodaja"))
                                                            cv.put("Veleprodaja", jsonParser.getValueAsInt(0));
                                                        if (0 == objectName.compareToIgnoreCase("Vlasnik"))
                                                            cv.put("Vlasnik", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("VrstaObveznika"))
                                                            cv.put("VrstaObveznika", jsonParser.getValueAsInt(0));
                                                        if (0 == objectName.compareToIgnoreCase("Region"))
                                                            cv.put("Region", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("JSON"))
                                                            cv.put("JSON", jsonParser.getValueAsString(""));
                                                        if (0 == objectName.compareToIgnoreCase("KanalDistribucije"))
                                                            cv.put("KanalDistribucije", jsonParser.getValueAsString(""));

                                                        if (0 == objectName.compareToIgnoreCase("Details")) {

                                                            db.delete("PARTNER_DETALJI", "CustomerId = ?", new String[]{cv.getAsString("ID")});

                                                            JsonToken _token = jsonParser.getCurrentToken();

                                                            while (_token != JsonToken.END_ARRAY) {

                                                                _token = jsonParser.nextToken();

                                                                if (_token == JsonToken.START_OBJECT) {

                                                                    ContentValues _cv = new ContentValues();
                                                                    while (_token != JsonToken.END_OBJECT) {

                                                                        _token = jsonParser.nextToken();

                                                                        if (_token == JsonToken.FIELD_NAME) {

                                                                            String fieldname = jsonParser.getCurrentName();
                                                                            jsonParser.nextToken();

                                                                            if (0 == fieldname.compareToIgnoreCase("CustomerId"))
                                                                                _cv.put("CustomerId", jsonParser.getValueAsLong(0L));
                                                                            if (0 == fieldname.compareToIgnoreCase("K1"))
                                                                                _cv.put("K1", jsonParser.getValueAsLong(0L));
                                                                            if (0 == fieldname.compareToIgnoreCase("KAM"))
                                                                                _cv.put("KAM", jsonParser.getValueAsInt(0));
                                                                            if (0 == fieldname.compareToIgnoreCase("ReferentNaplate"))
                                                                                _cv.put("ReferentNaplate", jsonParser.getValueAsInt(0));
                                                                            if (0 == fieldname.compareToIgnoreCase("ORSY"))
                                                                                _cv.put("ORSY", jsonParser.getValueAsInt(0));
                                                                            if (0 == fieldname.compareToIgnoreCase("TNT"))
                                                                                _cv.put("TNT", jsonParser.getValueAsInt(0));
                                                                            if (0 == fieldname.compareToIgnoreCase("OTD"))
                                                                                _cv.put("OTD", jsonParser.getValueAsInt(0));
                                                                            if (0 == fieldname.compareToIgnoreCase("Konkurent"))
                                                                                _cv.put("Konkurent", jsonParser.getValueAsInt(0));
                                                                            if (0 == fieldname.compareToIgnoreCase("KanalDistribucije"))
                                                                                _cv.put("KanalDistribucije", jsonParser.getValueAsString(""));
                                                                            if (0 == fieldname.compareToIgnoreCase("Bonitet"))
                                                                                _cv.put("Bonitet", jsonParser.getValueAsString(""));
                                                                            if (0 == fieldname.compareToIgnoreCase("OnlineShop"))
                                                                                _cv.put("OnlineShop", jsonParser.getValueAsInt(0));
                                                                            if (0 == fieldname.compareToIgnoreCase("KupcevKontaktUCentrali"))
                                                                                _cv.put("KupcevKontaktUCentrali", jsonParser.getValueAsInt(0));
                                                                            if (0 == fieldname.compareToIgnoreCase("VezaNaProdavnicu"))
                                                                                _cv.put("VezaNaProdavnicu", jsonParser.getValueAsString(""));
                                                                        }
                                                                    }
                                                                    db.insert("PARTNER_DETALJI", null, _cv);
                                                                }
                                                            }
                                                        }

                                                        if (0 == objectName.compareToIgnoreCase("Branches")) {

                                                            db.delete("PARTNER_BRANSE", "PartnerID = ?", new String[]{cv.getAsString("ID")});

                                                            JsonToken _token = jsonParser.getCurrentToken();

                                                            while (_token != JsonToken.END_ARRAY) {

                                                                _token = jsonParser.nextToken();

                                                                if (_token == JsonToken.START_OBJECT) {

                                                                    ContentValues _cv = new ContentValues();
                                                                    while (_token != JsonToken.END_OBJECT) {

                                                                        _token = jsonParser.nextToken();

                                                                        if (_token == JsonToken.FIELD_NAME) {

                                                                            String fieldname = jsonParser.getCurrentName();
                                                                            jsonParser.nextToken();

                                                                            if (0 == fieldname.compareToIgnoreCase("PartnerID"))
                                                                                _cv.put("PartnerID", jsonParser.getValueAsLong(0L));
                                                                            if (0 == fieldname.compareToIgnoreCase("Bransa"))
                                                                                _cv.put("Bransa", jsonParser.getValueAsString(""));
                                                                            if (0 == fieldname.compareToIgnoreCase("PlaniraniPrometBranse"))
                                                                                _cv.put("PlaniraniPrometBranse", jsonParser.getValueAsDouble(0D));
                                                                            if (0 == fieldname.compareToIgnoreCase("UkupniPlaniraniPromet"))
                                                                                _cv.put("UkupniPlaniraniPromet", jsonParser.getValueAsDouble(0D));
                                                                            if (0 == fieldname.compareToIgnoreCase("BrojUposlenika"))
                                                                                _cv.put("BrojUposlenika", jsonParser.getValueAsInt(0));
                                                                            if (0 == fieldname.compareToIgnoreCase("Potenencijal"))
                                                                                _cv.put("Potenencijal", jsonParser.getValueAsInt(0));
                                                                            if (0 == fieldname.compareToIgnoreCase("DanPosjete"))
                                                                                _cv.put("DanPosjete", jsonParser.getValueAsInt(0));
                                                                            if (0 == fieldname.compareToIgnoreCase("FrekvencijaPosjete"))
                                                                                _cv.put("FrekvencijaPosjete", jsonParser.getValueAsInt(0));
                                                                            if (0 == fieldname.compareToIgnoreCase("Komercijalista"))
                                                                                _cv.put("Komercijalista", jsonParser.getValueAsString(""));
                                                                            if (0 == fieldname.compareToIgnoreCase("Osnovna"))
                                                                                _cv.put("Osnovna", jsonParser.getValueAsString(""));
                                                                        }
                                                                    }
                                                                    db.insert("PARTNER_BRANSE", null, _cv);
                                                                }
                                                            }
                                                        }

                                                        if (0 == objectName.compareToIgnoreCase("Contacts")) {

                                                            db.delete("PARTNER_KONTAKTI", "PartnerID = ?", new String[]{cv.getAsString("ID")});

                                                            JsonToken _token = jsonParser.getCurrentToken();

                                                            while (_token != JsonToken.END_ARRAY) {

                                                                _token = jsonParser.nextToken();

                                                                if (_token == JsonToken.START_OBJECT) {

                                                                    ContentValues _cv = new ContentValues();

                                                                    while (_token != JsonToken.END_OBJECT) {

                                                                        _token = jsonParser.nextToken();

                                                                        if (_token == JsonToken.FIELD_NAME) {

                                                                            String fieldname = jsonParser.getCurrentName();
                                                                            jsonParser.nextToken();

                                                                            if (0 == fieldname.compareToIgnoreCase("PartnerID"))
                                                                                _cv.put("PartnerID", jsonParser.getValueAsLong(0L));
                                                                            if (0 == fieldname.compareToIgnoreCase("Ime"))
                                                                                _cv.put("Ime", jsonParser.getValueAsString(""));
                                                                            if (0 == fieldname.compareToIgnoreCase("Prezime"))
                                                                                _cv.put("Prezime", jsonParser.getValueAsString(""));
                                                                            if (0 == fieldname.compareToIgnoreCase("Titula"))
                                                                                _cv.put("Titula", jsonParser.getValueAsString(""));
                                                                            if (0 == fieldname.compareToIgnoreCase("Pozicija"))
                                                                                _cv.put("Pozicija", jsonParser.getValueAsString(""));
                                                                            if (0 == fieldname.compareToIgnoreCase("TipKontakta"))
                                                                                _cv.put("TipKontakta", jsonParser.getValueAsString(""));
                                                                            if (0 == fieldname.compareToIgnoreCase("Broj"))
                                                                                _cv.put("Broj", jsonParser.getValueAsString(""));
                                                                            if (0 == fieldname.compareToIgnoreCase("Email"))
                                                                                _cv.put("Email", jsonParser.getValueAsString(""));
                                                                            if (0 == fieldname.compareToIgnoreCase("DatumRodjenja"))
                                                                                _cv.put("DatumRodjenja", jsonParser.getValueAsString(""));
                                                                        }
                                                                    }

                                                                    db.insert("PARTNER_KONTAKTI", null, _cv);

                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                ret++;
                                                if (db.update("PARTNER", cv, "ID = ?", new String[]{cv.getAsString("ID")}) == 0)
                                                    db.insert("PARTNER", null, cv);
                                                if (mThreadReference != null && ret % 100 == 0) mThreadReference.doProgress(Integer.toString(ret) + "/" + Integer.toString(total_count_to_transfer));
                                            }
                                        }
                                    }
                                }
                                /** end of partneri **/
                            }
                        }
                    }
                }

                db.setTransactionSuccessful();
                db.endTransaction();
                if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));

            } catch (Exception e) {
                wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
                ret = -1;
            }
        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
            ret = -1;
        } finally {

        }

        if (db.inTransaction()) db.endTransaction();

        return ret;
    }

    public static int Load_Documents(long userID) {
        int ret = 0;
        try {

            methodName = "Load_Documents";

            long _DOE = 0;
            Cursor _cursor = db_readonly.rawQuery("select MAX(dt) from Documents Where AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
            if (_cursor.moveToFirst()) _DOE = _cursor.getLong(0);
            _cursor.close();

            if (wurthMB.loadComplete){
                _DOE = 0;
            }

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("UserID", Long.toString(userID)));
            postParameters.add(new BasicNameValuePair("DOE", Long.toString(_DOE)));

            db.beginTransactionNonExclusive();

            JsonFactory jfactory = new JsonFactory();
            JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpPostStream(wurthMB.getUser().URL + "GET_Documents", postParameters));
            JsonToken token = jsonParser.nextToken();

            if (token == JsonToken.START_ARRAY) {

                while (token != JsonToken.END_ARRAY) {

                    token = jsonParser.nextToken();

                    if (token == JsonToken.START_OBJECT) {

                        ContentValues cv = new ContentValues();
                        cv.put("AccountID", wurthMB.getUser().AccountID);

                        while (token != JsonToken.END_OBJECT) {

                            token = jsonParser.nextToken();

                            if (token == JsonToken.FIELD_NAME) {

                                String objectName = jsonParser.getCurrentName();

                                jsonParser.nextToken();

                                if (0 == objectName.compareToIgnoreCase("Content")) cv.put("data", Base64.decode(jsonParser.getValueAsString("")));
                                if (0 == objectName.compareToIgnoreCase("DocumentID")) cv.put("DocumentID", jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("IsDeleted")) cv.put("IsDeleted", jsonParser.getValueAsInt(0));
                                if (0 == objectName.compareToIgnoreCase("Active")) cv.put("Active", jsonParser.getValueAsInt(0));
                                if (0 == objectName.compareToIgnoreCase("dt")) cv.put("dt", jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("Description")) cv.put("Description", jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("Name")) cv.put("Name", jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("DocumentType")) cv.put("DocumentType", jsonParser.getValueAsInt(0));
                                if (0 == objectName.compareToIgnoreCase("Latitude")) cv.put("Latitude", jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("Longitude")) cv.put("Longitude", jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("Type")) cv.put("Type", jsonParser.getValueAsInt(0));
                                if (0 == objectName.compareToIgnoreCase("OptionID")) cv.put("OptionID", jsonParser.getValueAsInt(0));
                                if (0 == objectName.compareToIgnoreCase("ItemID")) cv.put("ItemID", jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("url")) cv.put("url", jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("fileContentType")) cv.put("fileContentType", jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("fileName")) cv.put("fileName", jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("fileSize")) cv.put("fileSize", jsonParser.getValueAsInt(0));

                            }
                        }

                        if (cv.getAsInteger("IsDeleted") == 1) {
                            cv.remove("Active");
                            cv.put("Active", 0);
                        }
                        cv.remove("IsDeleted");

                        if (db.update("Documents", cv, "DocumentID=?", new String[]{ cv.getAsString("DocumentID") }) == 0) db.insert("Documents", null, cv);

                        ret++;
                        if (mThreadReference != null && ret % 250 == 0) mThreadReference.doProgress(Integer.toString(ret));
                        
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));
                
            }
        }
        catch (Exception e) {
            ret = -1;
        }

        if (db.inTransaction()) db.endTransaction();

        return ret;
    }

    public static int Load_Orders(long userID) {
        int ret = 0;
        try {

            methodName = "Load_Orders";

            long _DOE = 0;
            Cursor _cursor = db_readonly.rawQuery("SELECT MAX(modification_date) FROM Orders WHERE AccountID = ?", new String[] {Long.toString(wurthMB.getUser().AccountID)});
            if (_cursor.moveToFirst()) _DOE = _cursor.getLong(0);
            _cursor.close();

            if (wurthMB.loadComplete) {
                //_DOE = (long) System.currentTimeMillis() - (long) (30L * 24L * 60L * 60L * 1000L);
                try {
                    SharedPreferences prefs = wurthMB.getInstance().getSharedPreferences("optimusMBprefs", wurthMB.getInstance().MODE_PRIVATE);
                    if (prefs.getLong("initial_start_date", 0) != 0L) {
                        _DOE = prefs.getLong("initial_start_date", 0L);
                    }
                } catch (Exception e) {

                }
            }

            try {

                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("UserID", Long.toString(userID)));
                postParameters.add(new BasicNameValuePair("DOE", Long.toString(_DOE + 1000)));

                JSONObject resp = new JSONObject(new CustomHttpClient().executeHttpPost("http://wurth.api.optimus.ba/services/wurth.asmx/GET_Order_Count", postParameters));

                Integer total_count_to_transfer = resp.getInt("OrderCount");
                Integer records_page_size = 5000;
                Integer records_page_count = 1;

                records_page_count = (total_count_to_transfer + records_page_size - 1) / records_page_size;

                db.beginTransactionNonExclusive();

                for (int page = 0; page < records_page_count; page++) {

                    postParameters.clear();

                    postParameters.add(new BasicNameValuePair("UserID", Long.toString(userID)));
                    postParameters.add(new BasicNameValuePair("DOE", Long.toString(_DOE)));
                        postParameters.add(new BasicNameValuePair("offset", Long.toString(page * records_page_size)));
                    postParameters.add(new BasicNameValuePair("limit", Long.toString(records_page_size)));

                    JsonFactory jfactory = new JsonFactory();
                    JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpPostStream("http://wurth.api.optimus.ba/services/wurth.asmx/GET_Order", postParameters));
                    JsonToken token = jsonParser.nextToken();

                    String sql = "INSERT INTO " +
                            " OrderItems (ClientDiscountPercentage, DiscountPercentage, DiscountTotal, GrandTotal, Note, Price_RT, Price_WS, " +
                            " ProductID, ProductName, Quantity, Tax, " +
                            " TaxTotal, Total, UserDiscountPercentage, DiscountGroupActionPercentage, DiscountGroupPercentage, DiscountProductPercentage, Order_ID, OrderID ) " +
                            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


                    SQLiteStatement stmt = db.compileStatement(sql);

                    if (token == JsonToken.START_ARRAY) {

                        while (token != JsonToken.END_ARRAY) {

                            token = jsonParser.nextToken();

                            if (token == JsonToken.START_OBJECT) {

                                ContentValues cv = new ContentValues();

                                while (token != JsonToken.END_OBJECT) {

                                    token = jsonParser.nextToken();

                                    if (token == JsonToken.FIELD_NAME) {

                                        String objectName = jsonParser.getCurrentName();

                                        jsonParser.nextToken();

                                        if (0 == objectName.compareToIgnoreCase("OrderID"))
                                            cv.put("OrderID", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("AccountID"))
                                            cv.put("AccountID", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("ClientID"))
                                            cv.put("ClientID", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("UserID"))
                                            cv.put("UserID", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("PaymentMethodID"))
                                            cv.put("PaymentMethodID", jsonParser.getValueAsInt(0));
                                        if (0 == objectName.compareToIgnoreCase("OrderDate"))
                                            cv.put("OrderDate", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("OrderReference"))
                                            cv.put("OrderReference", jsonParser.getValueAsString(""));
                                        if (0 == objectName.compareToIgnoreCase("Relations"))
                                            cv.put("Relations", jsonParser.getValueAsString(""));
                                        if (0 == objectName.compareToIgnoreCase("Note"))
                                            cv.put("Note", jsonParser.getValueAsString(""));
                                        if (0 == objectName.compareToIgnoreCase("DeliveryDate"))
                                            cv.put("DeliveryDate", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("PaymentDate"))
                                            cv.put("PaymentDate", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("Total"))
                                            cv.put("Total", jsonParser.getValueAsDouble(0D));
                                        if (0 == objectName.compareToIgnoreCase("TaxTotal"))
                                            cv.put("TaxTotal", jsonParser.getValueAsDouble(0D));
                                        if (0 == objectName.compareToIgnoreCase("DiscountPercentage"))
                                            cv.put("DiscountPercentage", jsonParser.getValueAsDouble(0D));
                                        if (0 == objectName.compareToIgnoreCase("DiscountTotal"))
                                            cv.put("DiscountTotal", jsonParser.getValueAsDouble(0D));
                                        if (0 == objectName.compareToIgnoreCase("GrandTotal"))
                                            cv.put("GrandTotal", jsonParser.getValueAsDouble(0D));
                                        if (0 == objectName.compareToIgnoreCase("OrderStatusID"))
                                            cv.put("OrderStatusID", jsonParser.getValueAsInt(0));
                                        if (0 == objectName.compareToIgnoreCase("DeliveryPlaceID"))
                                            cv.put("DeliveryPlaceID", jsonParser.getValueAsInt(0));
                                        if (0 == objectName.compareToIgnoreCase("Active"))
                                            cv.put("Active", jsonParser.getValueAsInt(0));
                                        if (0 == objectName.compareToIgnoreCase("Sync"))
                                            cv.put("Sync", jsonParser.getValueAsInt(0));
                                        if (0 == objectName.compareToIgnoreCase("DOE"))
                                            cv.put("DOE", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("VisitID"))
                                            cv.put("VisitID", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("IsDeleted"))
                                            cv.put("IsDeleted", jsonParser.getValueAsInt(0));
                                        if (0 == objectName.compareToIgnoreCase("modification_date"))
                                            cv.put("modification_date", jsonParser.getValueAsLong(0));

                                        if (0 == objectName.compareToIgnoreCase("Items")) {

                                            long _id = 0;

                                        /*if (cv.getAsInteger("IsDeleted") == 1){
                                            db.delete("Orders", "OrderID=?", new String[] { Long.toString(cv.getAsLong("OrderID")) });
                                        }
                                        else {
                                            cv.remove("IsDeleted");
                                            if (db.update("Orders", cv, "OrderID=?", new String[]{ Long.toString(cv.getAsLong("OrderID")) }) == 0) _id = db.insert("Orders", null, cv);
                                            db.delete("OrderItems", "OrderID=?", new String[]{ Long.toString(cv.getAsLong("OrderID")) });
                                        }*/

                                            cv.remove("IsDeleted");
                                            if (db.update("Orders", cv, "OrderID=?", new String[]{Long.toString(cv.getAsLong("OrderID"))}) == 0)
                                                _id = db.insert("Orders", null, cv);
                                            db.delete("OrderItems", "OrderID=?", new String[]{Long.toString(cv.getAsLong("OrderID"))});

                                            if (_id == 0) {
                                                Cursor cur = db_readonly.rawQuery("SELECT _id FROM Orders WHERE Orders.OrderID = ?", new String[]{cv.getAsString("OrderID")});
                                                if (cur != null) {
                                                    if (cur.getCount() > 0 && cur.moveToFirst())
                                                        _id = cur.getLong(0);
                                                    cur.close();
                                                }
                                            }

                                            JsonToken _token = jsonParser.getCurrentToken();

                                            while (_token != JsonToken.END_ARRAY) {

                                                _token = jsonParser.nextToken();

                                                if (_token == JsonToken.START_OBJECT) {

                                                    while (_token != JsonToken.END_OBJECT) {

                                                        _token = jsonParser.nextToken();

                                                        if (_token == JsonToken.FIELD_NAME) {

                                                            String fieldname = jsonParser.getCurrentName();
                                                            jsonParser.nextToken();

                                                            if (0 == fieldname.compareToIgnoreCase("ClientDiscountPercentage"))
                                                                stmt.bindDouble(1, jsonParser.getValueAsDouble(0D));
                                                            if (0 == fieldname.compareToIgnoreCase("DiscountPercentage"))
                                                                stmt.bindDouble(2, jsonParser.getValueAsDouble(0D));
                                                            if (0 == fieldname.compareToIgnoreCase("DiscountTotal"))
                                                                stmt.bindDouble(3, jsonParser.getValueAsDouble(0D));
                                                            if (0 == fieldname.compareToIgnoreCase("GrandTotal"))
                                                                stmt.bindDouble(4, jsonParser.getValueAsDouble(0D));
                                                            if (0 == fieldname.compareToIgnoreCase("Note"))
                                                                stmt.bindString(5, jsonParser.getValueAsString(""));
                                                            if (0 == fieldname.compareToIgnoreCase("Price_RT"))
                                                                stmt.bindDouble(6, jsonParser.getValueAsDouble(0D));
                                                            if (0 == fieldname.compareToIgnoreCase("Price_WS"))
                                                                stmt.bindDouble(7, jsonParser.getValueAsDouble(0D));
                                                            if (0 == fieldname.compareToIgnoreCase("ProductID"))
                                                                stmt.bindLong(8, jsonParser.getValueAsLong(0L));
                                                            if (0 == fieldname.compareToIgnoreCase("ProductName"))
                                                                stmt.bindString(9, jsonParser.getValueAsString(""));
                                                            if (0 == fieldname.compareToIgnoreCase("Quantity"))
                                                                stmt.bindDouble(10, jsonParser.getValueAsDouble(0D));
                                                            if (0 == fieldname.compareToIgnoreCase("Tax"))
                                                                stmt.bindDouble(11, jsonParser.getValueAsDouble(0D));
                                                            if (0 == fieldname.compareToIgnoreCase("TaxTotal"))
                                                                stmt.bindDouble(12, jsonParser.getValueAsDouble(0D));
                                                            if (0 == fieldname.compareToIgnoreCase("Total"))
                                                                stmt.bindDouble(13, jsonParser.getValueAsDouble(0D));
                                                            if (0 == fieldname.compareToIgnoreCase("UserDiscountPercentage"))
                                                                stmt.bindDouble(14, jsonParser.getValueAsDouble(0D));
                                                            if (0 == fieldname.compareToIgnoreCase("DiscountGroupActionPercentage"))
                                                                stmt.bindDouble(15, jsonParser.getValueAsDouble(0D));
                                                            if (0 == fieldname.compareToIgnoreCase("DiscountGroupPercentage"))
                                                                stmt.bindDouble(16, jsonParser.getValueAsDouble(0D));
                                                            if (0 == fieldname.compareToIgnoreCase("DiscountProductPercentage"))
                                                                stmt.bindDouble(17, jsonParser.getValueAsDouble(0D));
                                                        }
                                                    }

                                                    stmt.bindLong(19, cv.getAsLong("OrderID"));
                                                    stmt.bindDouble(18, _id);
                                                    stmt.execute();
                                                    stmt.clearBindings();
                                                }
                                            }
                                        }
                                    }
                                }
                                ret++;
                                if (mThreadReference != null && ret % 50 == 0)
                                    mThreadReference.doProgress(Integer.toString(ret));

                            }
                        }
                    }
                }

                db.setTransactionSuccessful();
                db.endTransaction();

                if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));

            } catch (Exception e) {
                wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
                ret = -1;
            }

            finally {

            }


        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
            ret = -1;
        } finally {

        }

        if (db.inTransaction()) db.endTransaction();

        return ret;
    }

    public static int Load_Pricelist(long userID) {

        int ret = 0;

        try {

            methodName = "Load_Pricelist";

            if (mThreadReference != null) mThreadReference.doProgress("Download cjenovnika");
            if (mThreadReferenceIntro != null) mThreadReferenceIntro.doProgress("Download cjenovnika");

            int count;
            URL url = new URL("http://ws.wurth.mb.optimus.ba/Files/Wurth/pricelist.json_v2.gz");
            URLConnection connection = url.openConnection();
            connection.connect();

            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream("/sdcard/pricelist.json_v2.gz");

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            /** UNZIP JSON **/

            if (mThreadReference != null) mThreadReference.doProgress("Raspakivanje cjenovnika");

            byte[] buffer = new byte[1024];
            GZIPInputStream gzis = new GZIPInputStream(new FileInputStream("/sdcard/pricelist.json_v2.gz"));

            FileOutputStream out = new FileOutputStream("/sdcard/pricelist.json");

            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            gzis.close();
            out.close();


            /** PARSE JSON **/
            if (mThreadReference != null) mThreadReference.doProgress("Unos podataka cjenovnika");
            if (mThreadReferenceIntro != null) mThreadReferenceIntro.doProgress("Unos podataka cjenovnika");

            JsonFactory jfactory = new JsonFactory();
            JsonParser jsonParser = jfactory.createParser(new File("/sdcard/pricelist.json"));
            JsonToken token = jsonParser.nextToken();

            db.beginTransactionNonExclusive();
            db.delete("CJENIK", "", null);

            String sql = "INSERT INTO CJENIK (ArtikalID,PartnerID,PotencijalOD,PotencijalDO,Bransa,OsnovnaCijena,KljucCijene,PopustOD,PopustDO,KolicinaOD,KolicinaDo,DodatniPopust,DatumOd,DatumDo,CijenaPonude,AkcijskaCijena,Pakovanje,KanalDistribucije ) " +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            SQLiteStatement stmt = db.compileStatement(sql);

            if (token == JsonToken.START_ARRAY) {

                while (token != JsonToken.END_ARRAY) {

                    token = jsonParser.nextToken();

                    if (token == JsonToken.START_OBJECT) {

                        while (token != JsonToken.END_OBJECT) {

                            token = jsonParser.nextToken();

                            if (token == JsonToken.FIELD_NAME) {

                                String objectName = jsonParser.getCurrentName();

                                jsonParser.nextToken();

                                if (0 == objectName.compareToIgnoreCase("ArtikalID")) stmt.bindLong(1, jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("PartnerID")) stmt.bindLong(2, jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("PotencijalOD")) stmt.bindLong(3, jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("PotencijalDO")) stmt.bindLong(4, jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("Bransa")) stmt.bindString(5, jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("OsnovnaCijena")) stmt.bindDouble(6, jsonParser.getValueAsDouble(0D));
                                if (0 == objectName.compareToIgnoreCase("KljucCijene")) stmt.bindLong(7, jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("PopustOD")) stmt.bindDouble(8, jsonParser.getValueAsDouble(0D));
                                if (0 == objectName.compareToIgnoreCase("PopustDO")) stmt.bindDouble(9, jsonParser.getValueAsDouble(0D));
                                if (0 == objectName.compareToIgnoreCase("KolicinaOD")) stmt.bindDouble(10, jsonParser.getValueAsDouble(0D));
                                if (0 == objectName.compareToIgnoreCase("KolicinaDo")) stmt.bindDouble(11, jsonParser.getValueAsDouble(0D));
                                if (0 == objectName.compareToIgnoreCase("DodatniPopust")) stmt.bindDouble(12, jsonParser.getValueAsDouble(0D));
                                if (0 == objectName.compareToIgnoreCase("DatumOd")) stmt.bindLong(13, jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("DatumDo")) stmt.bindLong(14, jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("CijenaPonude")) stmt.bindLong(15, jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("AkcijskaCijena")) stmt.bindLong(16, jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("Pakovanje")) stmt.bindLong(17, jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("KanalDistribucije")) stmt.bindString(18, jsonParser.getValueAsString(""));
                                //if (0 == objectName.compareToIgnoreCase("DatumModifikacije")) stmt.bindString(18, jsonParser.getValueAsString(""));

                            }
                        }
                        stmt.execute();
                        stmt.clearBindings();

                        ret++;
                        if (mThreadReference != null && ret % 1000 == 0) mThreadReference.doProgress(Integer.toString(ret));
                        if (mThreadReferenceIntro != null && ret % 1000 == 0) mThreadReferenceIntro.doProgress(Integer.toString(ret));
                        
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));
                if (mThreadReferenceIntro != null) mThreadReferenceIntro.doProgress(Integer.toString(ret));

            }

        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
            ret = -1;
        } finally {

        }

        if (db.inTransaction()) db.endTransaction();

        return ret;
    }

    public static int Load_Products(long userID) {

        int ret = 0;
        ArrayList<String> product_id = new ArrayList<>();

        try {


            SQLiteStatement stmt_packages = db.compileStatement("INSERT INTO  ARTIKAL_PAKOVANJA (ArtikalID, Barcode, KodPakovanja, Pakovanje)  VALUES (?, ?, ?, ?)");
            SQLiteStatement stmt_images = db.compileStatement("INSERT INTO  ARTIKAL_SLIKE (ArtikalID, Mala, Srednja, Velika)  VALUES (?, ?, ?, ?)");
            SQLiteStatement stmt_documents = db.compileStatement("INSERT INTO  ARTIKLI_DOKUMENTI (ID, ArtikalID, TipDokumentaID, TipDokumenta, Dokument)  VALUES (?, ?, ?, ?, ?)");
            SQLiteStatement stmt_safety = db.compileStatement("INSERT INTO  ARTIKLI_ZASTITE_NA_RADU (ArtikalID, VezaniArtikalID, NivoPovezanosti)  VALUES (?, ?, ?)");
            SQLiteStatement stmt_similar = db.compileStatement("INSERT INTO  SLICNI_ARTIKLI (ArtikalID, VezaniArtikalID, NivoPovezanosti)  VALUES (?, ?, ?)");
            SQLiteStatement stmt_binded = db.compileStatement("INSERT INTO  VEZANI_ARTIKLI (ArtikalID, VezaniArtikalID, NivoPovezanosti)  VALUES (?, ?, ?)");

            methodName = "Load_Products";

            long _DOE_Products = 0, _DOE_Product_Categories = 0;
            Cursor _cursor = db_readonly.rawQuery("select MAX(DOE) from Products Where AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
            if (_cursor.moveToFirst()) _DOE_Products = _cursor.getLong(0);
            _cursor.close();

            _cursor = db_readonly.rawQuery("select MAX(DOE) from ProductCategories Where AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
            if (_cursor.moveToFirst()) _DOE_Product_Categories = _cursor.getLong(0);
            _cursor.close();

            /* if (wurthMB.loadComplete) {
                _DOE_Product_Categories = 0;
                _DOE_Products = 0;
            }*/

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("UserID", Long.toString(userID)));
            postParameters.add(new BasicNameValuePair("DOE_Products", Long.toString(_DOE_Products + 1000)));

            JSONObject resp = new JSONObject(new CustomHttpClient().executeHttpPost("http://wurth.api.optimus.ba/services/wurth.asmx/GET_Products_Count", postParameters));

            Integer total_count_to_transfer = resp.getInt("ProductCount");
            Integer records_page_size = 2000;
            Integer records_page_count = 1;

            records_page_count = (total_count_to_transfer + records_page_size - 1) / records_page_size;

            db.beginTransactionNonExclusive();

            for (int page = 0; page < records_page_count; page++) {

                postParameters.clear();

                postParameters.add(new BasicNameValuePair("UserID", Long.toString(userID)));
                postParameters.add(new BasicNameValuePair("DOE_Products", Long.toString(_DOE_Products + 1000)));
                postParameters.add(new BasicNameValuePair("DOE_Product_Categories", Long.toString(_DOE_Product_Categories + 1000)));
                postParameters.add(new BasicNameValuePair("offset", Long.toString(page * records_page_size)));
                postParameters.add(new BasicNameValuePair("limit", Long.toString(records_page_size)));

                JsonFactory jfactory = new JsonFactory();
                JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpPostStream("http://wurth.api.optimus.ba/services/wurth.asmx/GET_Products", postParameters));
                JsonToken token = jsonParser.nextToken();


                if (token == JsonToken.START_OBJECT) {

                    while (token != JsonToken.END_OBJECT) {

                        token = jsonParser.nextToken();

                        if (token == JsonToken.FIELD_NAME) {

                            /** products **/
                            if (0 == jsonParser.getCurrentName().compareToIgnoreCase("Products")) {

                                token = jsonParser.nextToken();

                                if (token == JsonToken.START_ARRAY) {

                                    while (token != JsonToken.END_ARRAY) {

                                        token = jsonParser.nextToken();

                                        if (token == JsonToken.START_OBJECT) {

                                            ContentValues productValues = new ContentValues();
                                            productValues.put("AccountID", wurthMB.getUser().AccountID);

                                            while (token != JsonToken.END_OBJECT) {

                                                token = jsonParser.nextToken();

                                                if (token == JsonToken.FIELD_NAME) {

                                                    String objectName = jsonParser.getCurrentName();

                                                    jsonParser.nextToken();

                                                    if (0 == objectName.compareToIgnoreCase("_id"))
                                                        productValues.put("_productid", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("ProductID"))
                                                        productValues.put("ProductID", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("TypeID"))
                                                        productValues.put("TypeID", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("minAmount"))
                                                        productValues.put("minAmount", jsonParser.getValueAsDouble(0D));
                                                    if (0 == objectName.compareToIgnoreCase("Code"))
                                                        productValues.put("Code", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Barcode"))
                                                        productValues.put("Barcode", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Description"))
                                                        productValues.put("Description", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Content"))
                                                        productValues.put("Content", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Name"))
                                                        productValues.put("Name", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("UnitsInStock"))
                                                        productValues.put("UnitsInStock", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("Priority"))
                                                        productValues.put("Priority", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("UOMID"))
                                                        productValues.put("UOMID", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("Active"))
                                                        productValues.put("Active", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("IsDeleted"))
                                                        productValues.put("IsDeleted", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("DOE"))
                                                        productValues.put("DOE", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("DiscountPercentage"))
                                                        productValues.put("DiscountPercentage", jsonParser.getValueAsDouble(0D));
                                                    if (0 == objectName.compareToIgnoreCase("PaymentDelay"))
                                                        productValues.put("PaymentDelay", jsonParser.getValueAsDouble(0D));
                                                    if (0 == objectName.compareToIgnoreCase("DeliveryDelay"))
                                                        productValues.put("DeliveryDelay", jsonParser.getValueAsDouble(0D));
                                                }
                                            }

                                            if (productValues.getAsInteger("IsDeleted") == 1) {
                                                productValues.remove("Active");
                                                productValues.put("Active", 0);
                                            }
                                            productValues.remove("IsDeleted");

                                            if (productValues.getAsLong("ProductID") > 0) {
                                                product_id.add(productValues.getAsString("ProductID"));
                                                if (db.update("Products", productValues, "ProductID=?", new String[]{productValues.getAsString("ProductID")}) == 0)
                                                    db.insert("Products", null, productValues);
                                            }
                                            //else db.insert("Products", null, productValues);
                                        }
                                    }
                                }

                            }
                            /** end of products **/


                            /** Artikli **/
                            if (0 == jsonParser.getCurrentName().compareToIgnoreCase("Artikli")) {

                                token = jsonParser.nextToken();

                                if (token == JsonToken.START_ARRAY) {

                                    while (token != JsonToken.END_ARRAY) {

                                        token = jsonParser.nextToken();

                                        if (token == JsonToken.START_OBJECT) {

                                            ContentValues cv = new ContentValues();

                                            while (token != JsonToken.END_OBJECT) {

                                                token = jsonParser.nextToken();

                                                if (token == JsonToken.FIELD_NAME) {

                                                    String objectName = jsonParser.getCurrentName();

                                                    jsonParser.nextToken();

                                                    if (0 == objectName.compareToIgnoreCase("Atribut"))
                                                        cv.put("Atribut", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("BarCode"))
                                                        cv.put("BarCode", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Datum_Modifikacije"))
                                                        cv.put("Datum_Modifikacije", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Datum_Prijema"))
                                                        cv.put("Datum_Prijema", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Grupa_Artikla"))
                                                        cv.put("Grupa_Artikla", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("ID"))
                                                        cv.put("ID", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("Kljucne_Rijeci"))
                                                        cv.put("Kljucne_Rijeci", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Kod_Zbirne_Cjen_Razrade"))
                                                        cv.put("Kod_Zbirne_Cjen_Razrade", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Kod_Zbirnog_Naziva"))
                                                        cv.put("Kod_Zbirnog_Naziva", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("MjernaJedinica"))
                                                        cv.put("MjernaJedinica", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Narucena_Kolicina"))
                                                        cv.put("Narucena_Kolicina", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("Naslov_Opisa"))
                                                        cv.put("Naslov_Opisa", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Naziv"))
                                                        cv.put("Naziv", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Naziv_Zbirne_Cjen_Razrade"))
                                                        cv.put("Naziv_Zbirne_Cjen_Razrade", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Opis"))
                                                        cv.put("Opis", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Predefinisana_Dostupnost"))
                                                        cv.put("Predefinisana_Dostupnost", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("Sifra"))
                                                        cv.put("Sifra", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Stanje_Zaliha"))
                                                        cv.put("Stanje_Zaliha", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("Status_Artikla"))
                                                        cv.put("Status_Artikla", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("Status_Prezentacije_Artikla"))
                                                        cv.put("Status_Prezentacije_Artikla", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("Tehnicki_Podaci"))
                                                        cv.put("Tehnicki_Podaci", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Zamjenski_Artikal"))
                                                        cv.put("Zamjenski_Artikal", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("Zbirni_Naziv"))
                                                        cv.put("Zbirni_Naziv", jsonParser.getValueAsString(""));

                                                    if (0 == objectName.compareToIgnoreCase("Packages")) {


                                                        db.delete("ARTIKAL_PAKOVANJA", "ArtikalID = ?", new String[]{cv.getAsString("ID")});

                                                        JsonToken _token = jsonParser.getCurrentToken();

                                                        while (_token != JsonToken.END_ARRAY) {

                                                            _token = jsonParser.nextToken();

                                                            if (_token == JsonToken.START_OBJECT) {

                                                                ContentValues _cv = new ContentValues();

                                                                while (_token != JsonToken.END_OBJECT) {

                                                                    _token = jsonParser.nextToken();

                                                                    if (_token == JsonToken.FIELD_NAME) {

                                                                        String fieldname = jsonParser.getCurrentName();
                                                                        jsonParser.nextToken();

                                                                        if (0 == fieldname.compareToIgnoreCase("ArtikalID"))
                                                                            stmt_packages.bindLong(1, jsonParser.getValueAsLong(0L));
                                                                        if (0 == fieldname.compareToIgnoreCase("Barcode"))
                                                                            stmt_packages.bindString(2, jsonParser.getValueAsString(""));
                                                                        if (0 == fieldname.compareToIgnoreCase("KodPakovanja"))
                                                                            stmt_packages.bindString(3, jsonParser.getValueAsString(""));
                                                                        if (0 == fieldname.compareToIgnoreCase("Pakovanje"))
                                                                            stmt_packages.bindString(4, jsonParser.getValueAsString(""));
                                                                    }
                                                                }


                                                                stmt_packages.execute();
                                                                stmt_packages.clearBindings();
                                                                //if (db.update("ARTIKAL_PAKOVANJA", _cv, "ArtikalID = ?" , new String[] { _cv.getAsString("ArtikalID") }) == 0) db.insert("ARTIKAL_PAKOVANJA", null, _cv);

                                                            }
                                                        }
                                                    }

                                                    if (0 == objectName.compareToIgnoreCase("Images")) {

                                                        db.delete("ARTIKAL_SLIKE", "ArtikalID = ?", new String[]{cv.getAsString("ID")});

                                                        JsonToken _token = jsonParser.getCurrentToken();

                                                        while (_token != JsonToken.END_ARRAY) {

                                                            _token = jsonParser.nextToken();

                                                            if (_token == JsonToken.START_OBJECT) {

                                                                ContentValues _cv = new ContentValues();

                                                                while (_token != JsonToken.END_OBJECT) {

                                                                    _token = jsonParser.nextToken();

                                                                    if (_token == JsonToken.FIELD_NAME) {

                                                                        String fieldname = jsonParser.getCurrentName();
                                                                        jsonParser.nextToken();

                                                                        if (0 == fieldname.compareToIgnoreCase("ArtikalID"))
                                                                            stmt_images.bindLong(1, jsonParser.getValueAsLong(0L));
                                                                        if (0 == fieldname.compareToIgnoreCase("Mala"))
                                                                            stmt_images.bindString(2, jsonParser.getValueAsString(""));
                                                                        if (0 == fieldname.compareToIgnoreCase("Srednja"))
                                                                            stmt_images.bindString(3, jsonParser.getValueAsString(""));
                                                                        if (0 == fieldname.compareToIgnoreCase("Velika"))
                                                                            stmt_images.bindString(4, jsonParser.getValueAsString(""));
                                                                    }
                                                                }

                                                                stmt_images.execute();
                                                                stmt_images.clearBindings();

                                                                // if (db.update("ARTIKAL_SLIKE", _cv, "ArtikalID = ?" , new String[] { _cv.getAsString("ArtikalID") }) == 0) db.insert("ARTIKAL_SLIKE", null, _cv);

                                                            }
                                                        }
                                                    }

                                                    if (0 == objectName.compareToIgnoreCase("Documents")) {

                                                        db.delete("ARTIKLI_DOKUMENTI", "ArtikalID = ?", new String[]{cv.getAsString("ID")});

                                                        JsonToken _token = jsonParser.getCurrentToken();

                                                        while (_token != JsonToken.END_ARRAY) {

                                                            _token = jsonParser.nextToken();

                                                            if (_token == JsonToken.START_OBJECT) {

                                                                ContentValues _cv = new ContentValues();

                                                                while (_token != JsonToken.END_OBJECT) {

                                                                    _token = jsonParser.nextToken();

                                                                    if (_token == JsonToken.FIELD_NAME) {

                                                                        String fieldname = jsonParser.getCurrentName();
                                                                        jsonParser.nextToken();

                                                                        if (0 == fieldname.compareToIgnoreCase("ID"))
                                                                            stmt_documents.bindLong(1, jsonParser.getValueAsLong(0L));
                                                                        if (0 == fieldname.compareToIgnoreCase("ArtikalID"))
                                                                            stmt_documents.bindLong(2, jsonParser.getValueAsLong(0L));
                                                                        if (0 == fieldname.compareToIgnoreCase("TipDokumentaID"))
                                                                            stmt_documents.bindLong(3, jsonParser.getValueAsInt(0));
                                                                        if (0 == fieldname.compareToIgnoreCase("TipDokumenta"))
                                                                            stmt_documents.bindString(4, jsonParser.getValueAsString(""));
                                                                        if (0 == fieldname.compareToIgnoreCase("Dokument"))
                                                                            stmt_documents.bindString(5, jsonParser.getValueAsString(""));
                                                                    }
                                                                }

                                                                stmt_documents.execute();
                                                                stmt_documents.clearBindings();

                                                                // if (db.update("ARTIKLI_DOKUMENTI", _cv, "ArtikalID = ?" , new String[] { _cv.getAsString("ArtikalID") }) == 0) db.insert("ARTIKLI_DOKUMENTI", null, _cv);
                                                            }
                                                        }
                                                    }

                                                    if (0 == objectName.compareToIgnoreCase("Safety")) {

                                                        db.delete("ARTIKLI_ZASTITE_NA_RADU", "ArtikalID = ?", new String[]{cv.getAsString("ID")});

                                                        JsonToken _token = jsonParser.getCurrentToken();

                                                        while (_token != JsonToken.END_ARRAY) {

                                                            _token = jsonParser.nextToken();

                                                            if (_token == JsonToken.START_OBJECT) {

                                                                ContentValues _cv = new ContentValues();

                                                                while (_token != JsonToken.END_OBJECT) {

                                                                    _token = jsonParser.nextToken();

                                                                    if (_token == JsonToken.FIELD_NAME) {

                                                                        String fieldname = jsonParser.getCurrentName();
                                                                        jsonParser.nextToken();

                                                                        if (0 == fieldname.compareToIgnoreCase("ArtikalID"))
                                                                            stmt_safety.bindLong(1, jsonParser.getValueAsLong(0L));
                                                                        if (0 == fieldname.compareToIgnoreCase("VezaniArtikalID"))
                                                                            stmt_safety.bindLong(2, jsonParser.getValueAsLong(0L));
                                                                        if (0 == fieldname.compareToIgnoreCase("NivoPovezanosti"))
                                                                            stmt_safety.bindLong(3, jsonParser.getValueAsInt(0));
                                                                    }
                                                                }

                                                                stmt_safety.execute();
                                                                stmt_safety.clearBindings();

                                                                // if (db.update("ARTIKLI_ZASTITE_NA_RADU", _cv, "ArtikalID = ?" , new String[] { _cv.getAsString("ArtikalID") }) == 0) db.insert("ARTIKLI_ZASTITE_NA_RADU", null, _cv);
                                                            }
                                                        }
                                                    }

                                                    if (0 == objectName.compareToIgnoreCase("Similar")) {

                                                        db.delete("SLICNI_ARTIKLI", "ArtikalID = ?", new String[]{cv.getAsString("ID")});

                                                        JsonToken _token = jsonParser.getCurrentToken();

                                                        while (_token != JsonToken.END_ARRAY) {

                                                            _token = jsonParser.nextToken();

                                                            if (_token == JsonToken.START_OBJECT) {

                                                                ContentValues _cv = new ContentValues();

                                                                while (_token != JsonToken.END_OBJECT) {

                                                                    _token = jsonParser.nextToken();

                                                                    if (_token == JsonToken.FIELD_NAME) {

                                                                        String fieldname = jsonParser.getCurrentName();
                                                                        jsonParser.nextToken();

                                                                        if (0 == fieldname.compareToIgnoreCase("ArtikalID"))
                                                                            stmt_similar.bindLong(1, jsonParser.getValueAsLong(0L));
                                                                        if (0 == fieldname.compareToIgnoreCase("VezaniArtikalID"))
                                                                            stmt_similar.bindLong(2, jsonParser.getValueAsLong(0L));
                                                                        if (0 == fieldname.compareToIgnoreCase("NivoPovezanosti"))
                                                                            stmt_similar.bindLong(3, jsonParser.getValueAsInt(0));
                                                                    }
                                                                }

                                                                stmt_similar.execute();
                                                                stmt_similar.clearBindings();

                                                                // if (db.update("SLICNI_ARTIKLI", _cv, "ArtikalID = ?" , new String[] { _cv.getAsString("ArtikalID") }) == 0) db.insert("SLICNI_ARTIKLI", null, _cv);

                                                            }
                                                        }
                                                    }

                                                    if (0 == objectName.compareToIgnoreCase("Binded")) {

                                                        db.delete("VEZANI_ARTIKLI", "ArtikalID = ?", new String[]{cv.getAsString("ID")});

                                                        JsonToken _token = jsonParser.getCurrentToken();

                                                        while (_token != JsonToken.END_ARRAY) {

                                                            _token = jsonParser.nextToken();

                                                            if (_token == JsonToken.START_OBJECT) {

                                                                ContentValues _cv = new ContentValues();

                                                                while (_token != JsonToken.END_OBJECT) {

                                                                    _token = jsonParser.nextToken();

                                                                    if (_token == JsonToken.FIELD_NAME) {

                                                                        String fieldname = jsonParser.getCurrentName();
                                                                        jsonParser.nextToken();

                                                                        if (0 == fieldname.compareToIgnoreCase("ArtikalID"))
                                                                            stmt_binded.bindLong(1, jsonParser.getValueAsLong(0L));
                                                                        if (0 == fieldname.compareToIgnoreCase("VezaniArtikalID"))
                                                                            stmt_binded.bindLong(2, jsonParser.getValueAsLong(0L));
                                                                        if (0 == fieldname.compareToIgnoreCase("NivoPovezanosti"))
                                                                            stmt_binded.bindLong(3, jsonParser.getValueAsInt(0));
                                                                    }
                                                                }

                                                                stmt_binded.execute();
                                                                stmt_binded.clearBindings();

                                                                //if (db.update("VEZANI_ARTIKLI", _cv, "ArtikalID = ?" , new String[] { _cv.getAsString("ArtikalID") }) == 0) db.insert("VEZANI_ARTIKLI", null, _cv);

                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            if (db.update("ARTIKLI", cv, "ID = ?", new String[]{cv.getAsString("ID")}) == 0)
                                                db.insert("ARTIKLI", null, cv);

                                            ret++;
                                            if (mThreadReference != null && ret % 250 == 0)
                                                mThreadReference.doProgress(Integer.toString(ret));

                                        }
                                    }
                                }
                            }
                            /** end of Artikli **/

                            /** Categories **/
                            if (0 == jsonParser.getCurrentName().compareToIgnoreCase("Categories")) {

                                String sql_Associations = "INSERT INTO  ProductCategoryAssociations (ProductID, CategoryID)  VALUES (?, ?)";

                                SQLiteStatement stmt_Associations = db.compileStatement(sql_Associations);

                                token = jsonParser.nextToken();

                                if (token == JsonToken.START_ARRAY) {

                                    while (token != JsonToken.END_ARRAY) {

                                        token = jsonParser.nextToken();

                                        if (token == JsonToken.START_OBJECT) {

                                            ContentValues productCategoryValues = new ContentValues();
                                            productCategoryValues.put("AccountID", wurthMB.getUser().AccountID);

                                            while (token != JsonToken.END_OBJECT) {

                                                token = jsonParser.nextToken();

                                                if (token == JsonToken.FIELD_NAME) {

                                                    String objectName = jsonParser.getCurrentName();

                                                    jsonParser.nextToken();

                                                    if (0 == objectName.compareToIgnoreCase("CategoryID"))
                                                        productCategoryValues.put("CategoryID", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("ParentID") && jsonParser.getValueAsLong(0L) > 0L)
                                                        productCategoryValues.put("ParentID", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("UserID"))
                                                        productCategoryValues.put("UserID", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("Name"))
                                                        productCategoryValues.put("Name", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Active"))
                                                        productCategoryValues.put("Active", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("IsDeleted"))
                                                        productCategoryValues.put("IsDeleted", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("DOE"))
                                                        productCategoryValues.put("DOE", jsonParser.getValueAsLong(0L));

                                                    if (0 == objectName.compareToIgnoreCase("Associations")) {

                                                        db.delete("ProductCategoryAssociations", "CategoryID=?", new String[]{productCategoryValues.getAsString("CategoryID")});

                                                        JsonToken _token = jsonParser.getCurrentToken();

                                                        if (_token == JsonToken.START_ARRAY) {

                                                            while (_token != JsonToken.END_ARRAY) {

                                                                _token = jsonParser.nextToken();

                                                                if (_token == JsonToken.START_OBJECT) {

                                                                    while (_token != JsonToken.END_OBJECT) {

                                                                        _token = jsonParser.nextToken();

                                                                        if (_token == JsonToken.FIELD_NAME) {

                                                                            String fieldname = jsonParser.getCurrentName();
                                                                            jsonParser.nextToken();

                                                                            if (0 == fieldname.compareToIgnoreCase("ProductID"))
                                                                                stmt_Associations.bindLong(1, jsonParser.getValueAsLong(0L));
                                                                            if (0 == fieldname.compareToIgnoreCase("CategoryID"))
                                                                                stmt_Associations.bindLong(2, jsonParser.getValueAsLong(0L));
                                                                        }
                                                                    }
                                                                    stmt_Associations.execute();
                                                                    stmt_Associations.clearBindings();
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            if (productCategoryValues.getAsInteger("IsDeleted") == 1) {
                                                productCategoryValues.remove("Active");
                                                productCategoryValues.put("Active", 0);
                                            }
                                            productCategoryValues.remove("IsDeleted");

                                            if (productCategoryValues.getAsLong("CategoryID") > 0)
                                                if (db.update("ProductCategories", productCategoryValues, "CategoryID=?", new String[]{productCategoryValues.getAsString("CategoryID")}) == 0)
                                                    db.insert("ProductCategories", null, productCategoryValues);
                                                else
                                                    db.insert("ProductCategories", null, productCategoryValues);
                                            //ret++;
                                            //if (mThreadReference != null && ret % 250 == 0) mThreadReference.doProgress(Integer.toString(ret));
                                        }

                                        if (mThreadReference != null)
                                            mThreadReference.doProgress(Integer.toString(ret));

                                    }
                                }
                            }
                            /** end of Categories **/


                            /** Grupe **/
                            if (0 == jsonParser.getCurrentName().compareToIgnoreCase("Grupe")) {

                                token = jsonParser.nextToken();

                                if (token == JsonToken.START_ARRAY) {

                                    while (token != JsonToken.END_ARRAY) {

                                        token = jsonParser.nextToken();

                                        if (token == JsonToken.START_OBJECT) {

                                            ContentValues cv = new ContentValues();

                                            while (token != JsonToken.END_OBJECT) {

                                                token = jsonParser.nextToken();

                                                if (token == JsonToken.FIELD_NAME) {

                                                    String objectName = jsonParser.getCurrentName();

                                                    jsonParser.nextToken();

                                                    if (0 == objectName.compareToIgnoreCase("Datum_Modifikacije"))
                                                        cv.put("Datum_Modifikacije", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("ID"))
                                                        cv.put("ID", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("Naziv"))
                                                        cv.put("Naziv", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("RoditeljID"))
                                                        cv.put("RoditeljID", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("Sifra"))
                                                        cv.put("Sifra", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Slika"))
                                                        cv.put("Slika", jsonParser.getValueAsString(""));
                                                }

                                            }

                                            if (db.update("ARTIKAL_GRUPE", cv, "ID = ?", new String[]{cv.getAsString("ID")}) == 0)
                                                db.insert("ARTIKAL_GRUPE", null, cv);
                                        }
                                    }
                                }
                            }
                            /** end of Grupe **/

                            /** Brands **/
                            if (0 == jsonParser.getCurrentName().compareToIgnoreCase("Brands")) {

                                String sql_Associations = "INSERT INTO " +
                                        " ProductBrandAssociations (ProductID, BrandID) " +
                                        " VALUES (?, ?)";

                                SQLiteStatement stmt_Associations = db.compileStatement(sql_Associations);

                                token = jsonParser.nextToken();

                                if (token == JsonToken.START_ARRAY) {

                                    while (token != JsonToken.END_ARRAY) {

                                        token = jsonParser.nextToken();

                                        if (token == JsonToken.START_OBJECT) {

                                            ContentValues cv = new ContentValues();
                                            cv.put("AccountID", wurthMB.getUser().AccountID);

                                            while (token != JsonToken.END_OBJECT) {

                                                token = jsonParser.nextToken();

                                                if (token == JsonToken.FIELD_NAME) {

                                                    String objectName = jsonParser.getCurrentName();

                                                    jsonParser.nextToken();

                                                    if (0 == objectName.compareToIgnoreCase("BrandID"))
                                                        cv.put("BrandID", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("ParentID") && jsonParser.getValueAsLong(0L) > 0L)
                                                        cv.put("ParentID", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("UserID"))
                                                        cv.put("UserID", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("Name"))
                                                        cv.put("Name", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Active"))
                                                        cv.put("Active", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("IsDeleted"))
                                                        cv.put("IsDeleted", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("DOE"))
                                                        cv.put("DOE", jsonParser.getValueAsLong(0L));

                                                    /** Associations **/
                                                    if (0 == objectName.compareToIgnoreCase("Associations")) {

                                                        db.delete("ProductBrandAssociations", "BrandID=?", new String[]{cv.getAsString("BrandID")});

                                                        JsonToken _token = jsonParser.getCurrentToken();

                                                        if (_token == JsonToken.START_ARRAY) {

                                                            while (_token != JsonToken.END_ARRAY) {

                                                                _token = jsonParser.nextToken();

                                                                if (_token == JsonToken.START_OBJECT) {

                                                                    while (_token != JsonToken.END_OBJECT) {

                                                                        _token = jsonParser.nextToken();

                                                                        if (_token == JsonToken.FIELD_NAME) {

                                                                            String fieldname = jsonParser.getCurrentName();
                                                                            jsonParser.nextToken();

                                                                            if (0 == fieldname.compareToIgnoreCase("ProductID"))
                                                                                stmt_Associations.bindLong(1, jsonParser.getValueAsLong(0L));
                                                                            if (0 == fieldname.compareToIgnoreCase("BrandID"))
                                                                                stmt_Associations.bindLong(2, jsonParser.getValueAsLong(0L));
                                                                        }
                                                                    }
                                                                    stmt_Associations.execute();
                                                                    stmt_Associations.clearBindings();
                                                                }
                                                            }
                                                        }
                                                    }
                                                    /** End of Associations **/
                                                }
                                            }

                                            if (cv.getAsInteger("IsDeleted") == 1) {
                                                cv.remove("Active");
                                                cv.put("Active", 0);
                                            }
                                            cv.remove("IsDeleted");

                                            if (cv.getAsLong("BrandID") > 0)
                                                if (db.update("ProductBrands", cv, "BrandID=?", new String[]{cv.getAsString("BrandID")}) == 0)
                                                    db.insert("ProductBrands", null, cv);
                                                else db.insert("ProductBrands", null, cv);
                                            //ret++;
                                            //if (mThreadReference != null && ret % 250 == 0) mThreadReference.doProgress(Integer.toString(ret));

                                        }
                                    }
                                }
                            }
                            /** end of Brands **/


                            /** Types **/
                            if (0 == jsonParser.getCurrentName().compareToIgnoreCase("Types")) {

                                token = jsonParser.nextToken();

                                if (token == JsonToken.START_ARRAY) {

                                    while (token != JsonToken.END_ARRAY) {

                                        token = jsonParser.nextToken();

                                        if (token == JsonToken.START_OBJECT) {

                                            ContentValues cv = new ContentValues();
                                            cv.put("AccountID", wurthMB.getUser().AccountID);

                                            while (token != JsonToken.END_OBJECT) {

                                                token = jsonParser.nextToken();

                                                if (token == JsonToken.FIELD_NAME) {

                                                    String objectName = jsonParser.getCurrentName();

                                                    jsonParser.nextToken();

                                                    if (0 == objectName.compareToIgnoreCase("ProductTypeID"))
                                                        cv.put("ProductTypeID", jsonParser.getValueAsLong(0L));
                                                    if (0 == objectName.compareToIgnoreCase("Name"))
                                                        cv.put("Name", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Description"))
                                                        cv.put("Description", jsonParser.getValueAsString(""));
                                                    if (0 == objectName.compareToIgnoreCase("Active"))
                                                        cv.put("Active", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("IsDeleted"))
                                                        cv.put("IsDeleted", jsonParser.getValueAsInt(0));
                                                    if (0 == objectName.compareToIgnoreCase("DOE"))
                                                        cv.put("DOE", jsonParser.getValueAsLong(0L));
                                                }
                                            }

                                            if (cv.getAsInteger("IsDeleted") == 1) {
                                                cv.remove("Active");
                                                cv.put("Active", 0);
                                            }
                                            cv.remove("IsDeleted");

                                            if (cv.getAsLong("ProductTypeID") > 0)
                                                if (db.update("ProductTypes", cv, "ProductTypeID=?", new String[]{cv.getAsString("ProductTypeID")}) == 0)
                                                    db.insert("ProductTypes", null, cv);
                                                else db.insert("ProductTypes", null, cv);
                                        }
                                    }

                                    if (mThreadReference != null)
                                        mThreadReference.doProgress(Integer.toString(ret));

                                }
                            }
                            /** end of Types **/
                        }
                    }
                } else {

                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();


        } catch (Exception e) {
            ret = -1;
            wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
        }

        if (db.inTransaction()) db.endTransaction();


        if (ret > 0) {
            if (mThreadReference != null) mThreadReference.doProgress("Kreiranje indexa");
            DL_Wurth.updateFTS(product_id, null, null);
        }

        return ret;
    }

    public static int Load_Visits(long userID) {
        int ret = 0;

        try {

            methodName = "Load_Visits";

            long dt = 0;
            Cursor _cursor = db_readonly.rawQuery("select MAX(modification_date) from Visits Where AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
            if (_cursor.moveToFirst()) dt  = _cursor.getLong(0);
            _cursor.close();

            if (wurthMB.loadComplete){
                dt = 0; //(long) System.currentTimeMillis() - (long) (30L * 24L * 60L * 60L * 1000L);
            }

            try {

                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("UserID", Long.toString(userID)));
                postParameters.add(new BasicNameValuePair("DOE", Long.toString(dt + 1000)));

                JSONObject resp = new JSONObject(new CustomHttpClient().executeHttpPost("http://wurth.api.optimus.ba/services/wurth.asmx/GET_Visit_Count", postParameters));

                Integer total_count_to_transfer = resp.getInt("VisitCount");
                Integer records_page_size = 5000;
                Integer records_page_count = 1;

                records_page_count = (total_count_to_transfer + records_page_size - 1) / records_page_size;

                db.beginTransactionNonExclusive();

                for (int page = 0; page < records_page_count; page++) {

                    postParameters.clear();

                    postParameters.add(new BasicNameValuePair("UserID", Long.toString(userID)));
                    postParameters.add(new BasicNameValuePair("DOE", Long.toString(dt)));
                    postParameters.add(new BasicNameValuePair("offset", Long.toString(page * records_page_size)));
                    postParameters.add(new BasicNameValuePair("limit", Long.toString(records_page_size)));

                    JsonFactory jfactory = new JsonFactory();
                    JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpPostStream("http://wurth.api.optimus.ba/services/wurth.asmx/GET_Visit", postParameters));
                    JsonToken token = jsonParser.nextToken();

                    if (token == JsonToken.START_ARRAY) {

                        while (token != JsonToken.END_ARRAY) {

                            token = jsonParser.nextToken();

                            if (token == JsonToken.START_OBJECT) {

                                ContentValues cv = new ContentValues();

                                while (token != JsonToken.END_OBJECT) {

                                    token = jsonParser.nextToken();

                                    if (token == JsonToken.FIELD_NAME) {

                                        String objectName = jsonParser.getCurrentName();

                                        jsonParser.nextToken();

                                        if (0 == objectName.compareToIgnoreCase("VisitID"))
                                            cv.put("VisitID", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("AccountID"))
                                            cv.put("AccountID", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("UserID"))
                                            cv.put("UserID", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("ClientID"))
                                            cv.put("ClientID", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("DeliveryPlaceID"))
                                            cv.put("DeliveryPlaceID", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("Latitude"))
                                            cv.put("Latitude", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("Longitude"))
                                            cv.put("Longitude", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("Note"))
                                            cv.put("Note", jsonParser.getValueAsString(""));
                                        if (0 == objectName.compareToIgnoreCase("DRC"))
                                            cv.put("dt", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("startTime"))
                                            cv.put("startDT", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("endTime"))
                                            cv.put("endDT", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("modification_date"))
                                            cv.put("modification_date", jsonParser.getValueAsLong(0L));
                                    }
                                }

                                cv.put("Sync", 1);

                                if (db.update("Visits", cv, "VisitID=?", new String[]{cv.getAsString("VisitID")}) == 0)
                                    db.insert("Visits", null, cv);
                                if (mThreadReference != null && ret % 250 == 0)
                                    mThreadReference.doProgress(Integer.toString(ret));

                                ret++;

                            }
                        }
                    }
                }

                db.setTransactionSuccessful();
                db.endTransaction();

            }
            catch (Exception e) {
                wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
                ret = -1;
            }

            if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));
            
        }
        catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
            ret = -1;
        }
        finally {

        }

        if (db.inTransaction()) db.endTransaction();

        return ret;
    }

    public static int Load_Routes(long userID) {
        int ret = 0;

        try {

            methodName = "Load_Routes";

            long dt = 0;
            Cursor _cursor = db_readonly.rawQuery("SELECT MAX(doe) FROM Routes WHERE AccountID = ?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
            if (_cursor.moveToFirst()) dt  = _cursor.getLong(0);
            _cursor.close();

            if (wurthMB.loadComplete){
                dt = 0; //(long) System.currentTimeMillis() - (long) (30L * 24L * 60L * 60L * 1000L);
            }

            try {

                db.beginTransactionNonExclusive();

                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("UserID", Long.toString(userID)));
                postParameters.add(new BasicNameValuePair("DOE", Long.toString(dt + 1000)));

                    JsonFactory jfactory = new JsonFactory();
                    JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpPostStream("http://wurth.api.optimus.ba/services/wurth.asmx/GET_Routes", postParameters));
                    JsonToken token = jsonParser.nextToken();

                    if (token == JsonToken.START_ARRAY) {

                        while (token != JsonToken.END_ARRAY) {

                            token = jsonParser.nextToken();

                            if (token == JsonToken.START_OBJECT) {

                                ContentValues cv = new ContentValues();

                                while (token != JsonToken.END_OBJECT) {

                                    token = jsonParser.nextToken();

                                    if (token == JsonToken.FIELD_NAME) {

                                        String objectName = jsonParser.getCurrentName();

                                        jsonParser.nextToken();

                                        if (0 == objectName.compareToIgnoreCase("RouteID"))
                                            cv.put("RouteID", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("AccountID"))
                                            cv.put("AccountID", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("UserID"))
                                            cv.put("UserID", jsonParser.getValueAsLong(0L));
                                        if (0 == objectName.compareToIgnoreCase("Name"))
                                            cv.put("Name", jsonParser.getValueAsString(""));
                                        if (0 == objectName.compareToIgnoreCase("code"))
                                            cv.put("code", jsonParser.getValueAsString(""));
                                        if (0 == objectName.compareToIgnoreCase("Description"))
                                            cv.put("Description", jsonParser.getValueAsString(""));
                                        if (0 == objectName.compareToIgnoreCase("raw"))
                                            cv.put("raw", jsonParser.getValueAsString(""));
                                        if (0 == objectName.compareToIgnoreCase("DOE"))
                                            cv.put("DOE", jsonParser.getValueAsLong(0));
                                        if (0 == objectName.compareToIgnoreCase("Active"))
                                            cv.put("Active", jsonParser.getValueAsInt(0));
                                    }
                                }

                                cv.put("Sync", 1);

                                if (db.update("Routes", cv, "RouteID=?", new String[]{cv.getAsString("RouteID")}) == 0)
                                    db.insert("Routes", null, cv);
                                if (mThreadReference != null && ret % 250 == 0)
                                    mThreadReference.doProgress(Integer.toString(ret));

                                ret++;

                            }
                        }
                    }

                db.setTransactionSuccessful();
                db.endTransaction();

            }
            catch (Exception e) {
                wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
                ret = -1;
            }

            if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));

        }
        catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
            ret = -1;
        }
        finally {

        }

        if (db.inTransaction()) db.endTransaction();

        return ret;
    }

    public static int LOAD_Activites() {
        int ret = 0;
        try {

            methodName = "LoadActivites";

            long _DOE = 0;
            Cursor _cursor = db_readonly.rawQuery("select MAX(DOE) from Activity Where AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
            if (_cursor.moveToFirst()) _DOE  = _cursor.getLong(0);
            _cursor.close();

            if (wurthMB.loadComplete) _DOE = 0;

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("AccountID", Long.toString(wurthMB.getUser().ApplicationAccountID_EMS)));
            postParameters.add(new BasicNameValuePair("UserID", Long.toString(wurthMB.getUser().ApplicationUserID_EMS)));
            postParameters.add(new BasicNameValuePair("DOE", Long.toString(_DOE)));

            db.beginTransactionNonExclusive();

            try {

                JsonFactory jfactory = new JsonFactory();
                JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpPostStream(wurthMB.getUser().URL_EMS + "GET_Activity", postParameters));
                JsonToken token = jsonParser.nextToken();

                if (token == JsonToken.START_ARRAY) {

                    while (token != JsonToken.END_ARRAY) {

                        token = jsonParser.nextToken();

                        if (token == JsonToken.START_OBJECT) {

                            ba.wurth.mb.Classes.Objects.Activity tempObject = new ba.wurth.mb.Classes.Objects.Activity();

                            while (token != JsonToken.END_OBJECT) {

                                token = jsonParser.nextToken();

                                if (token == JsonToken.FIELD_NAME) {

                                    String objectName = jsonParser.getCurrentName();

                                    jsonParser.nextToken();

                                    if (0 == objectName.compareToIgnoreCase("ActivityID")) tempObject.ActivityID = jsonParser.getLongValue();
                                    if (0 == objectName.compareToIgnoreCase("GroupID")) tempObject.GroupID = jsonParser.getLongValue();
                                    if (0 == objectName.compareToIgnoreCase("ParentID")) tempObject.ParentID = jsonParser.getLongValue();
                                    if (0 == objectName.compareToIgnoreCase("AccountID")) tempObject.AccountID =  jsonParser.getLongValue();
                                    if (0 == objectName.compareToIgnoreCase("Name")) tempObject.Name = jsonParser.getText();
                                    if (0 == objectName.compareToIgnoreCase("Description")) tempObject.Description =  jsonParser.getText();
                                    if (0 == objectName.compareToIgnoreCase("Priority")) tempObject.Priority = jsonParser.getIntValue();
                                    if (0 == objectName.compareToIgnoreCase("Public")) tempObject.Public = jsonParser.getBooleanValue();
                                    if (0 == objectName.compareToIgnoreCase("Billable")) tempObject.Billable = jsonParser.getBooleanValue();
                                    if (0 == objectName.compareToIgnoreCase("Prolific")) tempObject.Prolific = jsonParser.getBooleanValue();
                                    if (0 == objectName.compareToIgnoreCase("MandatoryOptions")) tempObject.MandatoryOptions = jsonParser.getText();
                                    if (0 == objectName.compareToIgnoreCase("Duration")) tempObject.Duration = jsonParser.getIntValue();
                                    if (0 == objectName.compareToIgnoreCase("AdditionalTextRequired")) tempObject.AdditionalTextRequired = jsonParser.getBooleanValue();
                                    if (0 == objectName.compareToIgnoreCase("Snooze")) tempObject.Snooze = jsonParser.getIntValue();
                                    if (0 == objectName.compareToIgnoreCase("Reminder")) tempObject.Reminder = jsonParser.getIntValue();
                                    if (0 == objectName.compareToIgnoreCase("Reference")) tempObject.Reference = jsonParser.getText();
                                    if (0 == objectName.compareToIgnoreCase("RecieverGroup")) tempObject.RecieverGroup =  jsonParser.getText();
                                    if (0 == objectName.compareToIgnoreCase("RecieverID")) tempObject.RecieverID = jsonParser.getIntValue();
                                    if (0 == objectName.compareToIgnoreCase("Assigment")) tempObject.Assigment = jsonParser.getBooleanValue();
                                    if (0 == objectName.compareToIgnoreCase("Unassigment")) tempObject.Unassigment = jsonParser.getBooleanValue();
                                    if (0 == objectName.compareToIgnoreCase("ResourceID")) tempObject.ResourceID = jsonParser.getLongValue();
                                    if (0 == objectName.compareToIgnoreCase("SingleMode")) tempObject.SingleMode = jsonParser.getBooleanValue();
                                    if (0 == objectName.compareToIgnoreCase("Active")) tempObject.Active = jsonParser.getBooleanValue();
                                    if (0 == objectName.compareToIgnoreCase("Sync")) tempObject.Sync = jsonParser.getBooleanValue();
                                    if (0 == objectName.compareToIgnoreCase("JSDOE")) tempObject.DOE = jsonParser.getLongValue();
                                    if (0 == objectName.compareToIgnoreCase("IsDeleted")) tempObject.IsDeleted = jsonParser.getBooleanValue();

                                }
                            }

                            ContentValues values = new ContentValues();

                            values.put("ActivityID", tempObject.ActivityID);
                            values.put("GroupID", tempObject.GroupID);
                            values.put("ParentID", tempObject.ParentID);
                            values.put("AccountID", wurthMB.getUser().AccountID);
                            values.put("Name", tempObject.Name);
                            values.put("Description", tempObject.Description);
                            values.put("Priority", tempObject.Priority);
                            values.put("Public", tempObject.Public ? 1 : 0);
                            values.put("Billable", tempObject.Billable ? 1 : 0);
                            values.put("Prolific", tempObject.Prolific ? 1 : 0);
                            values.put("MandatoryOptions", tempObject.MandatoryOptions);
                            values.put("Duration", tempObject.Duration);
                            values.put("AdditionalTextRequired", tempObject.AdditionalTextRequired ? 1 : 0);
                            values.put("Snooze", tempObject.Snooze);
                            values.put("Reminder", tempObject.Reminder);
                            values.put("Reference", tempObject.Reference);
                            values.put("RecieverGroup", tempObject.RecieverGroup);
                            values.put("RecieverID", tempObject.RecieverID);
                            values.put("Assigment", tempObject.Assigment ? 1 : 0);
                            values.put("Unassigment", tempObject.Unassigment ? 1 : 0);
                            values.put("ResourceID", tempObject.ResourceID);
                            values.put("SingleMode", tempObject.SingleMode ? 1 : 0);
                            values.put("Active", tempObject.Active ? 1 : 0);
                            values.put("Sync", 1);
                            values.put("DOE", tempObject.DOE);


                            if (tempObject.IsDeleted) {
                                db.delete("Activity", "ActivityID=?", new String[] { Long.toString(tempObject.ActivityID) });
                            }
                            else {

                                long _id = 0;
                                Cursor cur = db_readonly.rawQuery("select _id from Activity Where ActivityID=?", new String[]{ Long.toString(tempObject.ActivityID) });
                                if (cur .moveToFirst()){
                                    _id = cur.getLong(0);
                                }
                                cur.close();

                                if (_id == 0) db.insert("Activity", null, values);
                                else db.update("Activity", values, "ActivityID=?", new String[]{ Long.toString(tempObject.ActivityID) });
                            }
                            ret++;
                            if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));
                        }
                    }
                }

            } catch (Exception e) {
                wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
                ret = -1;
            }

            finally {

            }

            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
            ret = -1;
        } finally {

        }
        return ret;
    }    
    
    public static int LOAD_UserActivityLog() {
        int ret = 0;
        try {

            methodName = "LoadUserActivityLog";

            long _DOE = 0;
            Cursor _cursor = db_readonly.rawQuery("select MAX(DOE) from User_Activity_Log Where AccountID=?", new String[]{Long.toString(wurthMB.getUser().AccountID)});
            if (_cursor.moveToFirst()) _DOE  = _cursor.getLong(0);
            _cursor.close();

            if (wurthMB.loadComplete) _DOE = 0;

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("AccountID", Long.toString(wurthMB.getUser().ApplicationAccountID_EMS)));
            postParameters.add(new BasicNameValuePair("UserID", Long.toString(wurthMB.getUser().ApplicationUserID_EMS)));
            postParameters.add(new BasicNameValuePair("DOE", Long.toString(_DOE)));

            db.beginTransactionNonExclusive();

            try {

                JsonFactory jfactory = new JsonFactory();
                JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpPostStream(wurthMB.getUser().URL_EMS + "GET_UserActivityLog", postParameters));
                JsonToken token = jsonParser.nextToken();

                if (token == JsonToken.START_ARRAY) {

                    while (token != JsonToken.END_ARRAY) {

                        token = jsonParser.nextToken();

                        if (token == JsonToken.START_OBJECT) {

                            ContentValues values = new ContentValues();
                            boolean IsDeleted = false;

                            while (token != JsonToken.END_OBJECT) {

                                token = jsonParser.nextToken();

                                if (token == JsonToken.FIELD_NAME) {

                                    String objectName = jsonParser.getCurrentName();

                                    jsonParser.nextToken();

                                    if (0 == objectName.compareToIgnoreCase("ID")) values.put("ID", jsonParser.getLongValue());
                                    //if (0 == objectName.compareToIgnoreCase("AccountID")) values.put("AccountID", jsonParser.getLongValue());
                                    //if (0 == objectName.compareToIgnoreCase("UserID")) values.put("UserID", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("OptionID")) values.put("OptionID", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("ItemID")) values.put("ItemID", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("JSstartTime")) values.put("startTime", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("JSendTime")) values.put("endTime", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("ProjectID")) values.put("ProjectID", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("ClientID")) values.put("ClientID", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("DeliveryPlaceID")) values.put("DeliveryPlaceID", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("startLatitude")) values.put("startLatitude", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("startLongitude")) values.put("startLongitude", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("endLatitude")) values.put("endLatitude", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("endLongitude")) values.put("endLongitude", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("Billable")) values.put("Billable", jsonParser.getBooleanValue() ? 1 : 0);
                                    if (0 == objectName.compareToIgnoreCase("Version")) values.put("Version", jsonParser.getIntValue());
                                    if (0 == objectName.compareToIgnoreCase("Reference")) values.put("Reference", jsonParser.getText());
                                    if (0 == objectName.compareToIgnoreCase("Locked")) values.put("Locked", jsonParser.getBooleanValue() ? 1 : 0);
                                    if (0 == objectName.compareToIgnoreCase("Description")) values.put("Description", jsonParser.getText());
                                    if (0 == objectName.compareToIgnoreCase("Duration")) values.put("Duration", jsonParser.getIntValue());
                                    if (0 == objectName.compareToIgnoreCase("IP")) values.put("IP", jsonParser.getText());
                                    if (0 == objectName.compareToIgnoreCase("Licence")) values.put("Licence", jsonParser.getText());
                                    if (0 == objectName.compareToIgnoreCase("GroupID")) values.put("GroupID", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("MediaID")) values.put("MediaID", jsonParser.getIntValue());
                                    if (0 == objectName.compareToIgnoreCase("TravelOrderID")) values.put("TravelOrderID", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("ResourceID")) values.put("ResourceID", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("ResourceItemID")) values.put("ResourceItemID", jsonParser.getLongValue());
                                    if (0 == objectName.compareToIgnoreCase("Deviation")) values.put("Deviation", jsonParser.getIntValue());
                                    if (0 == objectName.compareToIgnoreCase("UserName")) values.put("UserName", jsonParser.getText());
                                    if (0 == objectName.compareToIgnoreCase("GroupName")) values.put("GroupName", jsonParser.getText());
                                    if (0 == objectName.compareToIgnoreCase("ItemName")) values.put("ItemName", jsonParser.getText());
                                    if (0 == objectName.compareToIgnoreCase("Prolific")) values.put("Prolific", jsonParser.getBooleanValue() ? 1 : 0);
                                    if (0 == objectName.compareToIgnoreCase("IsDeleted")) IsDeleted = jsonParser.getBooleanValue();
                                    if (0 == objectName.compareToIgnoreCase("JSDOE")) values.put("DOE", jsonParser.getLongValue());
                                }
                            }

                            values.put("Sync", 1);
                            values.put("Active", 1);
                            values.put("AccountID", wurthMB.getUser().AccountID);
                            values.put("UserID", wurthMB.getUser().UserID);

                            if (IsDeleted) {
                                db.delete("User_Activity_Log", "ID=?", new String[] { values.getAsString("ID") });
                            }
                            else {
                                if (db.update("User_Activity_Log", values, "ID=?", new String[]{ values.getAsString("ID") }) == 0) db.insert("User_Activity_Log", null, values);
                            }

                            ret++;

                            if (mThreadReference != null && ret % 25 == 0) mThreadReference.doProgress(Integer.toString(ret));
                        }
                    }
                }

            } catch (Exception e) {
                wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
                ret = -1;
            }

            finally {

            }

            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
            ret = -1;
        } finally {

        }
        if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));
        if (db.inTransaction()) db.endTransaction();
        return ret;
    }

    public static Exception POST_UserActivityLog () {

        methodName = "PostUserActivityLog";

        try {
            if (wurthMB.getUser() == null || wurthMB.getUser().URL.equals("")) {
                return null;
            }

            Cursor cur;
            Long _id = 0L;

            JSONObject jsonObj_UserActivityLog = new JSONObject();

            // Delivery places
            cur = db_readonly.rawQuery("SELECT * FROM User_Activity_Log Where AccountID = ? AND UserID = ? " +
                    " AND Sync = 0 ORDER BY DOE ASC LIMIT 1", new String[]{ Long.toString(wurthMB.getUser().AccountID), Long.toString(wurthMB.getUser().UserID)});

            if (cur.moveToFirst()) {

                _id =  cur.getLong(cur.getColumnIndex("_id"));

                jsonObj_UserActivityLog.put("ID", cur.getLong(cur.getColumnIndex("ID")));
                jsonObj_UserActivityLog.put("AccountID", wurthMB.getUser().ApplicationAccountID_EMS);
                jsonObj_UserActivityLog.put("UserID", wurthMB.getUser().ApplicationUserID_EMS);
                jsonObj_UserActivityLog.put("OptionID", cur.getLong(cur.getColumnIndex("OptionID")));
                jsonObj_UserActivityLog.put("ItemID", cur.getLong(cur.getColumnIndex("ItemID")));
                jsonObj_UserActivityLog.put("startTime", cur.getLong(cur.getColumnIndex("startTime")));
                jsonObj_UserActivityLog.put("endTime", cur.getLong(cur.getColumnIndex("endTime")));
                jsonObj_UserActivityLog.put("ProjectID", cur.getLong(cur.getColumnIndex("ProjectID")));
                jsonObj_UserActivityLog.put("ClientID", cur.getLong(cur.getColumnIndex("ClientID")));
                jsonObj_UserActivityLog.put("DeliveryPlaceID", cur.getLong(cur.getColumnIndex("DeliveryPlaceID")));
                jsonObj_UserActivityLog.put("startLatitude", cur.getLong(cur.getColumnIndex("startLatitude")));
                jsonObj_UserActivityLog.put("startLongitude", cur.getLong(cur.getColumnIndex("startLongitude")));
                jsonObj_UserActivityLog.put("endLatitude", cur.getLong(cur.getColumnIndex("endLatitude")));
                jsonObj_UserActivityLog.put("endLongitude", cur.getLong(cur.getColumnIndex("endLongitude")));
                jsonObj_UserActivityLog.put("Billable", cur.getInt(cur.getColumnIndex("Billable")));
                jsonObj_UserActivityLog.put("Version", cur.getInt(cur.getColumnIndex("Version")));
                jsonObj_UserActivityLog.put("Reference", cur.getString(cur.getColumnIndex("Reference")));
                jsonObj_UserActivityLog.put("Locked", cur.getInt(cur.getColumnIndex("Locked")));
                jsonObj_UserActivityLog.put("Description", cur.getString(cur.getColumnIndex("Description")));
                jsonObj_UserActivityLog.put("Duration", cur.getInt(cur.getColumnIndex("Duration")));
                jsonObj_UserActivityLog.put("IP", cur.getString(cur.getColumnIndex("IP")));
                jsonObj_UserActivityLog.put("Licence", cur.getString(cur.getColumnIndex("Licence")));
                jsonObj_UserActivityLog.put("GroupID", cur.getLong(cur.getColumnIndex("GroupID")));
                jsonObj_UserActivityLog.put("MediaID", cur.getInt(cur.getColumnIndex("MediaID")));
                jsonObj_UserActivityLog.put("TravelOrderID", cur.getLong(cur.getColumnIndex("TravelOrderID")));
                jsonObj_UserActivityLog.put("ResourceID", cur.getLong(cur.getColumnIndex("ResourceID")));
                jsonObj_UserActivityLog.put("ResourceItemID", cur.getLong(cur.getColumnIndex("ResourceItemID")));
                jsonObj_UserActivityLog.put("Deviation", cur.getInt(cur.getColumnIndex("Deviation")));
                jsonObj_UserActivityLog.put("UserName", cur.getString(cur.getColumnIndex("UserName")));
                jsonObj_UserActivityLog.put("GroupName",cur.getString(cur.getColumnIndex("GroupName")));
                jsonObj_UserActivityLog.put("ItemName", cur.getString(cur.getColumnIndex("ItemName")));
                jsonObj_UserActivityLog.put("Prolific", cur.getInt(cur.getColumnIndex("Prolific")));
                jsonObj_UserActivityLog.put("Active", cur.getInt(cur.getColumnIndex("Active")));
                jsonObj_UserActivityLog.put("DOE", cur.getLong(cur.getColumnIndex("DOE")));
            }

            cur.close();

            if (jsonObj_UserActivityLog.length() > 0) {

                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("jsonObj", jsonObj_UserActivityLog.toString()));

                String response = CustomHttpClient.executeHttpPost(wurthMB.getUser().URL_EMS + "POST_UserActivityLog", postParameters).toString();

                if (response == null || response.equals("")) return null;

                JSONObject json_data = new JSONObject(response);

                if (json_data.getLong("ID") > 0){

                    //if (wurthMB.getRecord() != null) wurthMB.getRecord().ID = json_data.getLong("ID");

                    ContentValues cv = new ContentValues();
                    cv.put("ID", json_data.getLong("ID"));

                    if (jsonObj_UserActivityLog.getLong("endTime") == 0) {
                        Long _endTime = 0L;
                        Cursor _cur = db_readonly.rawQuery("SELECT endTime FROM User_Activity_Log Where _id = ? " , new String[]{ Long.toString(_id)});
                        if (_cur.moveToFirst()) _endTime  = _cur.getLong(0);
                        _cur.close();
                        if (_endTime == 0) cv.put("Sync", 1);
                        else cv.put("Sync", 0);
                    }
                    else {
                        cv.put("Sync", 1);
                    }

                    db.update("User_Activity_Log", cv, "_id=?", new String[] { Long.toString(_id)});
                }
            }
        }
        catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
            return e;
        }

        return null;
    }

    public static int Load_Actions(long userID) {
        int ret = 0;
        try {

            methodName = "Load_Actions";

            long _DOE = 0;
            Cursor _cursor = db_readonly.rawQuery("select MAX(DatumOd) from AKCIJE", null);
            if (_cursor.moveToFirst()) _DOE = _cursor.getLong(0);
            _cursor.close();

            if (wurthMB.loadComplete){
                _DOE = 0;
            }

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("UserID", Long.toString(userID)));
            postParameters.add(new BasicNameValuePair("DOE", Long.toString(_DOE)));

            db.beginTransactionNonExclusive();

            if (wurthMB.loadComplete){
                db.execSQL("DELETE FROM AKCIJE");
            }

            JsonFactory jfactory = new JsonFactory();
            JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpPostStream(wurthMB.getUser().URL + "GET_Actions", postParameters));
            JsonToken token = jsonParser.nextToken();

            if (token == JsonToken.START_ARRAY) {

                while (token != JsonToken.END_ARRAY) {

                    token = jsonParser.nextToken();

                    if (token == JsonToken.START_OBJECT) {

                        ContentValues cv = new ContentValues();

                        while (token != JsonToken.END_OBJECT) {

                            token = jsonParser.nextToken();

                            if (token == JsonToken.FIELD_NAME) {

                                String objectName = jsonParser.getCurrentName();

                                jsonParser.nextToken();

                                if (0 == objectName.compareToIgnoreCase("IDAkcije")) cv.put("IDAkcije", jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("Naziv")) cv.put("Naziv", jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("DatumOd")) cv.put("DatumOd", jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("DatumDo")) cv.put("DatumDo", jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("Komentar")) cv.put("Komentar", jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("Slika")) cv.put("Slika", jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("PotencijalOd")) cv.put("PotencijalOd", jsonParser.getValueAsLong(0));
                                if (0 == objectName.compareToIgnoreCase("PotencijalDo")) cv.put("PotencijalDo", jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("Bransa")) cv.put("Bransa", jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("IDArtikla")) cv.put("IDArtikla", jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("PZNA")) cv.put("PZNA", jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("TimeStamp")) cv.put("TimeStamp", jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("OrderNo")) cv.put("OrderNo", jsonParser.getValueAsInt(0));
                                if (0 == objectName.compareToIgnoreCase("CatalogCode")) cv.put("CatalogCode", jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("ImageSize")) cv.put("ImageSize", jsonParser.getValueAsInt(0));
                            }
                        }

                        if (db.update("AKCIJE", cv, "IDAkcije=?", new String[]{ cv.getAsString("IDAkcije") }) == 0) db.insert("AKCIJE", null, cv);

                        ret++;
                        if (mThreadReference != null && ret % 250 == 0) mThreadReference.doProgress(Integer.toString(ret));
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));

            }
        }
        catch (Exception e) {
            ret = -1;
        }

        if (db.inTransaction()) db.endTransaction();

        return ret;
    }

    public static int Load_Branches() {
        int ret = 0;
        try {

            methodName = "Load_Branches";

            long _DOE = 0;

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("UserID", Long.toString(wurthMB.getUser().UserID)));
            postParameters.add(new BasicNameValuePair("DOE", Long.toString(_DOE)));

            db.beginTransactionNonExclusive();

            db.delete("BRANSE", null, null);

            JsonFactory jfactory = new JsonFactory();
            JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpPostStream(wurthMB.getUser().URL + "GET_Branches", postParameters));
            JsonToken token = jsonParser.nextToken();

            if (token == JsonToken.START_ARRAY) {

                while (token != JsonToken.END_ARRAY) {

                    token = jsonParser.nextToken();

                    if (token == JsonToken.START_OBJECT) {

                        ContentValues cv = new ContentValues();

                        while (token != JsonToken.END_OBJECT) {

                            token = jsonParser.nextToken();

                            if (token == JsonToken.FIELD_NAME) {

                                String objectName = jsonParser.getCurrentName();

                                jsonParser.nextToken();

                                if (0 == objectName.compareToIgnoreCase("KodBranse")) cv.put("KodBranse", jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("Naziv")) cv.put("Naziv", jsonParser.getValueAsString(""));
                                if (0 == objectName.compareToIgnoreCase("IDKalkulacije")) cv.put("IDKalkulacije", jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("FaktorKalkulacije")) cv.put("FaktorKalkulacije", jsonParser.getValueAsDouble(0D));

                            }
                        }

                        db.insert("BRANSE", null, cv);

                        ret++;
                        if (mThreadReference != null && ret % 250 == 0) mThreadReference.doProgress(Integer.toString(ret));

                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));
            }
        }
        catch (Exception e) {
            ret = -1;
        }

        if (db.inTransaction()) db.endTransaction();

        return ret;
    }

    public static int Load_Users(long userID) {
        int ret = 0;
        try {

            methodName = "Load_Users";

            long _DOE = 0;

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("UserID", Long.toString(userID)));
            postParameters.add(new BasicNameValuePair("DOE", Long.toString(_DOE)));

            db.beginTransactionNonExclusive();

            db.delete("KOMERCIJALISTI", null, null);
            db.delete("Users", null, null);
            db.delete("Groups", null, null);
            db.delete("Groups_Users_Associations", null, null);
            db.delete("Groups_SalesPersons_Associations", null, null);

            JsonFactory jfactory = new JsonFactory();
            JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpPostStream(wurthMB.getUser().URL + "GET_Users", postParameters));
            JsonToken token = jsonParser.nextToken();

            if (token == JsonToken.START_OBJECT) {

                while (token != JsonToken.END_OBJECT) {

                    token = jsonParser.nextToken();

                    if (token == JsonToken.FIELD_NAME && jsonParser.getCurrentName().compareToIgnoreCase("Komercijalisti") == 0) {

                        token = jsonParser.nextToken();

                        if (token == JsonToken.START_ARRAY) {

                            while (token != JsonToken.END_ARRAY) {

                                token = jsonParser.nextToken();

                                if (token == JsonToken.START_OBJECT) {

                                    ContentValues cv = new ContentValues();

                                    while (token != JsonToken.END_OBJECT) {

                                        token = jsonParser.nextToken();

                                        if (token == JsonToken.FIELD_NAME) {

                                            String objectName = jsonParser.getCurrentName();

                                            jsonParser.nextToken();

                                            if (0 == objectName.compareToIgnoreCase("ID")) cv.put("ID", jsonParser.getValueAsLong(0L));
                                            if (0 == objectName.compareToIgnoreCase("Adresa")) cv.put("Adresa", jsonParser.getValueAsString(""));
                                            //if (0 == objectName.compareToIgnoreCase("Datum_Modifikacije")) cv.put("Datum_Modifikacije", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("Drzava")) cv.put("Drzava", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("EmailAdresa")) cv.put("EmailAdresa", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("Fax")) cv.put("Fax", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("Grad")) cv.put("Grad", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("Ime")) cv.put("Ime", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("Kod")) cv.put("Kod", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("KodRole")) cv.put("KodRole", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("Mobitel")) cv.put("Mobitel", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("NazivRole")) cv.put("NazivRole", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("Prezime")) cv.put("Prezime", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("Region")) cv.put("Region", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("Sifra")) cv.put("Sifra", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("Telefon")) cv.put("Telefon", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("UserName")) cv.put("UserName", jsonParser.getValueAsString(""));
                                        }
                                    }

                                    db.insert("KOMERCIJALISTI", null, cv);

                                    ret++;
                                    if (mThreadReference != null && ret % 250 == 0) mThreadReference.doProgress(Integer.toString(ret));

                                }
                            }

                            if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));

                        }
                    }

                    //token = jsonParser.nextToken();

                    if (token == JsonToken.FIELD_NAME && jsonParser.getCurrentName().compareToIgnoreCase("Users") == 0) {

                        token = jsonParser.nextToken();

                        if (token == JsonToken.START_ARRAY) {

                            while (token != JsonToken.END_ARRAY) {

                                token = jsonParser.nextToken();

                                if (token == JsonToken.START_OBJECT) {

                                    ContentValues cv = new ContentValues();

                                    while (token != JsonToken.END_OBJECT) {

                                        token = jsonParser.nextToken();

                                        if (token == JsonToken.FIELD_NAME) {

                                            String objectName = jsonParser.getCurrentName();

                                            jsonParser.nextToken();

                                            if (0 == objectName.compareToIgnoreCase("AccessLevelID")) cv.put("AccessLevelID", jsonParser.getValueAsInt(0));
                                            if (0 == objectName.compareToIgnoreCase("AccountID")) cv.put("AccountID", jsonParser.getValueAsLong(0));
                                            //if (0 == objectName.compareToIgnoreCase("Active")) cv.put("Active", jsonParser.getValueAsString(""));
                                            //if (0 == objectName.compareToIgnoreCase("Address")) cv.put("Address", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("EmailAddress")) cv.put("EmailAddress", jsonParser.getValueAsString(""));
                                            //if (0 == objectName.compareToIgnoreCase("City")) cv.put("City", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("code")) cv.put("code", jsonParser.getValueAsString(""));
                                            //if (0 == objectName.compareToIgnoreCase("CountryID")) cv.put("CountryID", jsonParser.getValueAsInt(0));
                                            //if (0 == objectName.compareToIgnoreCase("DOE")) cv.put("DOE", jsonParser.getValueAsLong(0));
                                            //if (0 == objectName.compareToIgnoreCase("DRC")) cv.put("DRC", jsonParser.getValueAsLong(0));
                                            //if (0 == objectName.compareToIgnoreCase("IsDeleted")) cv.put("IsDeleted", jsonParser.getValueAsString(""));
                                            //if (0 == objectName.compareToIgnoreCase("Fax")) cv.put("Fax", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("Firstname")) cv.put("Firstname", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("Lastname")) cv.put("Lastname", jsonParser.getValueAsString(""));
                                            //if (0 == objectName.compareToIgnoreCase("Mobile")) cv.put("Mobile", jsonParser.getValueAsString(""));
                                            //if (0 == objectName.compareToIgnoreCase("ModifiedBy")) cv.put("ModifiedBy", jsonParser.getValueAsLong(0));
                                            //if (0 == objectName.compareToIgnoreCase("Password")) cv.put("Password", jsonParser.getValueAsString(""));
                                            //if (0 == objectName.compareToIgnoreCase("SettingID")) cv.put("SettingID", jsonParser.getValueAsInt(0));
                                            //if (0 == objectName.compareToIgnoreCase("Telephone")) cv.put("Telephone", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("UserID")) cv.put("UserID", jsonParser.getValueAsLong(0));
                                            //if (0 == objectName.compareToIgnoreCase("Website")) cv.put("Website", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("id")) cv.put("_userid", jsonParser.getValueAsLong(0));

                                        }
                                    }

                                    db.insert("Users", null, cv);

                                    ret++;
                                    if (mThreadReference != null && ret % 250 == 0) mThreadReference.doProgress(Integer.toString(ret));

                                }
                            }
                            //db.setTransactionSuccessful();
                            //db.endTransaction();
                            if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));

                        }
                    }

                    //token = jsonParser.nextToken();

                    if (token == JsonToken.FIELD_NAME && jsonParser.getCurrentName().compareToIgnoreCase("Groups") == 0) {

                        token = jsonParser.nextToken();

                        if (token == JsonToken.START_ARRAY) {

                            while (token != JsonToken.END_ARRAY) {

                                token = jsonParser.nextToken();

                                if (token == JsonToken.START_OBJECT) {

                                    ContentValues cv = new ContentValues();

                                    while (token != JsonToken.END_OBJECT) {

                                        token = jsonParser.nextToken();

                                        if (token == JsonToken.FIELD_NAME) {

                                            String objectName = jsonParser.getCurrentName();

                                            jsonParser.nextToken();

                                            if (0 == objectName.compareToIgnoreCase("AccountID")) cv.put("AccountID", jsonParser.getValueAsLong(0));
                                            if (0 == objectName.compareToIgnoreCase("GroupID")) cv.put("GroupID", jsonParser.getValueAsLong(0));
                                            if (0 == objectName.compareToIgnoreCase("code")) cv.put("code", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("Description")) cv.put("Description", jsonParser.getValueAsString(""));
                                            //if (0 == objectName.compareToIgnoreCase("DOE")) cv.put("DOE", jsonParser.getValueAsLong(0));
                                            //if (0 == objectName.compareToIgnoreCase("DRC")) cv.put("DRC", jsonParser.getValueAsLong(0));
                                            if (0 == objectName.compareToIgnoreCase("Name")) cv.put("Name", jsonParser.getValueAsString(""));
                                            //if (0 == objectName.compareToIgnoreCase("IsDeleted")) cv.put("IsDeleted", jsonParser.getValueAsString(""));
                                            //if (0 == objectName.compareToIgnoreCase("ModifiedBy")) cv.put("ModifiedBy", jsonParser.getValueAsString(""));
                                            if (0 == objectName.compareToIgnoreCase("_id")) cv.put("_groupid", jsonParser.getValueAsLong(0));


                                            if (0 == objectName.compareToIgnoreCase("SalesPersons")) {

                                                token = jsonParser.nextToken();

                                                while (token != JsonToken.END_ARRAY) {

                                                    ContentValues _cv = new ContentValues();
                                                    _cv.put("GroupID", cv.getAsLong("GroupID"));
                                                    _cv.put("UserID", jsonParser.getValueAsLong(0));
                                                    db.insert("Groups_SalesPersons_Associations", null, _cv);

                                                    token = jsonParser.nextToken();

                                                }
                                            }

                                            if (0 == objectName.compareToIgnoreCase("Users")) {

                                                token = jsonParser.nextToken();

                                                while (token != JsonToken.END_ARRAY) {

                                                    ContentValues _cv = new ContentValues();
                                                    _cv.put("GroupID", cv.getAsLong("GroupID"));
                                                    _cv.put("UserID", jsonParser.getValueAsLong(0));
                                                    db.insert("Groups_Users_Associations", null, _cv);

                                                    token = jsonParser.nextToken();

                                                }
                                            }

                                        }
                                    }

                                    db.insert("Groups", null, cv);

                                    ret++;
                                    if (mThreadReference != null && ret % 250 == 0) mThreadReference.doProgress(Integer.toString(ret));

                                }
                            }
                            //db.setTransactionSuccessful();
                            //db.endTransaction();
                            if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));

                        }
                    }
                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();
        }
        catch (Exception e) {
            ret = -1;
        }

        if (db.inTransaction()) db.endTransaction();

        return ret;
    }

    public static int Load_Stocks() {
        int ret = 0;
        try {

            methodName = "Load_Stocks";

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("offset", "0"));
            postParameters.add(new BasicNameValuePair("limit", "1000000"));

            db.beginTransactionNonExclusive();

            JsonFactory jfactory = new JsonFactory();
            JsonParser jsonParser = jfactory.createParser(CustomHttpClient.executeHttpPostStream(wurthMB.getUser().URL + "GET_Stocks", postParameters));
            JsonToken token = jsonParser.nextToken();

            db.execSQL("UPDATE products SET UnitsInStock = 0");

            if (token == JsonToken.START_ARRAY) {

                while (token != JsonToken.END_ARRAY) {

                    token = jsonParser.nextToken();

                    if (token == JsonToken.START_OBJECT) {

                        ContentValues cv = new ContentValues();

                        while (token != JsonToken.END_OBJECT) {

                            token = jsonParser.nextToken();

                            if (token == JsonToken.FIELD_NAME) {

                                String objectName = jsonParser.getCurrentName();

                                jsonParser.nextToken();

                                if (0 == objectName.compareToIgnoreCase("c0")) cv.put("_productid", jsonParser.getValueAsLong(0L));
                                if (0 == objectName.compareToIgnoreCase("c1")) cv.put("UnitsInStock", jsonParser.getValueAsLong(0L));
                            }
                        }

                        db.update("Products", cv, "_productid=?", new String[]{ cv.getAsString("_productid") });

                        ret++;
                        if (mThreadReference != null && ret % 250 == 0) mThreadReference.doProgress(Integer.toString(ret));
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                if (mThreadReference != null) mThreadReference.doProgress(Integer.toString(ret));

            }
        }
        catch (Exception e) {
            ret = -1;
        }

        if (db.inTransaction()) db.endTransaction();

        return ret;
    }
}
