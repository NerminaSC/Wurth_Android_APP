package ba.wurth.mb.DataLayer.Temp;


import android.content.ContentValues;
import android.database.Cursor;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Order;
import ba.wurth.mb.Classes.Objects.Temp;
import ba.wurth.mb.Classes.Objects.Temp_Acquisition;
import ba.wurth.mb.Classes.wurthMB;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class DL_Temp {
    private static String TAG = "DataLayer";
    private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
    private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

    public static Temp Get() {
        try
        {
            Cursor cur = db_readonly.rawQuery("SELECT * FROM TempTable WHERE AccountID=? AND UserID=?", new String[]{ Long.toString(wurthMB.getUser().AccountID), Long.toString(wurthMB.getUser().UserID) });
            if ( cur != null && cur.getCount() > 0 && cur.moveToFirst()) {
                Temp temp = wurthMB.readerTemp.readValue(cur.getString(cur.getColumnIndex("data")));
                cur.close();
                if (temp != null && temp.order != null) return temp;
            }
        }
        catch (Exception e) {
            wurthMB.AddError(TAG, e.getMessage(), e);
        }
        return null;
    }

    public static Cursor GetByOptionID(int OptionID) {
        Cursor cur = null;
        try
        {
            cur = db_readonly.rawQuery("SELECT * FROM TEMP_ACQUISITION WHERE AccountID=? AND UserID=? AND OptionID = ?", new String[]{ Long.toString(wurthMB.getUser().AccountID), Long.toString(wurthMB.getUser().UserID), Integer.toString(OptionID) });
        }
        catch (Exception e) {
            wurthMB.AddError(TAG, e.getMessage(), e);
        }
        return cur;
    }

    public static Order GET_Order() {
        Order o = null;
        try
        {
            Cursor cur = db_readonly.rawQuery("SELECT * FROM TempTable WHERE AccountID=? AND UserID=?", new String[]{ Long.toString(wurthMB.getUser().AccountID), Long.toString(wurthMB.getUser().UserID) });
            if ( cur != null && cur.getCount() > 0 && cur.moveToFirst()) {
                Temp temp = wurthMB.readerTemp.readValue(cur.getString(cur.getColumnIndex("data")));
                if (temp != null && temp.order != null) o = temp.order;
            }
            if (cur != null) cur.close();
        }
        catch (Exception e) {
            wurthMB.AddError(TAG, e.getMessage(), e);
        }
        return o;
    }

    public static Client GET_Client() {

        try
        {
            Cursor cur = db_readonly.rawQuery("SELECT * FROM TempTable WHERE AccountID=? AND UserID=?", new String[]{ Long.toString(wurthMB.getUser().AccountID), Long.toString(wurthMB.getUser().UserID) });
            if ( cur != null && cur.getCount() > 0 && cur.moveToFirst()) {
                Temp temp = wurthMB.readerTemp.readValue(cur.getString(cur.getColumnIndex("data")));
                cur.close();
                if (temp != null && temp.client != null) return temp.client;
            }
        }
        catch (Exception e) {
            wurthMB.AddError(TAG, e.getMessage(), e);
        }
        return null;
    }

    public static int AddOrUpdate(Temp temp) {
        try {
            final Temp t = temp;
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO && t != null) {
                new Thread() {
                    @Override
                    public void run() {
                        try{
                            ContentValues cv = new ContentValues();
                            cv.put("AccountID", wurthMB.getUser().AccountID);
                            cv.put("UserID", wurthMB.getUser().UserID);
                            cv.put("data", wurthMB.writterTemp.writeValueAsString(t));
                            db.delete("TempTable", "AccountID=? AND UserID=?", new String[]{ Long.toString(wurthMB.getUser().AccountID), Long.toString(wurthMB.getUser().UserID) });
                            db.insert("TempTable", null, cv);
                        } catch (Exception e) {
                            wurthMB.AddError("TEMP AddOrUpdate Thread", e.getMessage(), e); }
                    }
                }.start();
                return 1;
            }
        }
        catch (Exception e) {
            wurthMB.AddError("TEMP AddOrUpdate", e.getMessage(), e);
        }
        return -1;
    }

    public static int Delete(){
        try
        {
            db.delete("TempTable", "AccountID=? AND UserID=?", new String[]{ Long.toString(wurthMB.getUser().AccountID), Long.toString(wurthMB.getUser().UserID) });
            return 1;
        }
        catch (Exception e) {
            return -1;
        }
    }

    public static int AddOrUpdate(Temp_Acquisition temp) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("ID", temp.ID);
            cv.put("AccountID", temp.AccountID);
            cv.put("OptionID", temp.OptionID);
            cv.put("jsonObj", temp.jsonObj);
            cv.put("UserID", temp.UserID);
            cv.put("DOE", temp.DOE);
            cv.put("Sync", temp.Sync);

            if (db.update("TEMP_ACQUISITION",cv, " ID = ?", new String[] {Long.toString(temp.ID)}) == 0)
            db.insert("TEMP_ACQUISITION", null, cv);
            return 1;
        }
        catch (Exception e) {
            wurthMB.AddError("TEMP AddOrUpdate", e.getMessage(), e);
        }
        return -1;
    }

    public static int Sync_Acquisition() {

        Cursor cur = null;

        try {

            cur = db_readonly.rawQuery("select * from TEMP_ACQUISITION Where Sync = 0 And AccountID = " + wurthMB.getUser().AccountID + " Order by DOE ASC", null);

            while (cur.moveToNext()) {

                JSONObject jsonObj = new JSONObject();
                jsonObj.put("ID", cur.getLong(cur.getColumnIndex("ID")));
                jsonObj.put("AccountID", cur.getLong(cur.getColumnIndex("AccountID")));
                jsonObj.put("UserID", cur.getLong(cur.getColumnIndex("UserID")));
                jsonObj.put("OptionID", cur.getLong(cur.getColumnIndex("OptionID")));
                jsonObj.put("jsonObj", cur.getString(cur.getColumnIndex("jsonObj")));
                jsonObj.put("DOE", cur.getLong(cur.getColumnIndex("DOE")));

                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("jsonObj", jsonObj.toString()));

                String response = CustomHttpClient.executeHttpPost(wurthMB.getUser().URL + "POST_Temp_Acquisition", postParameters).toString();

                if (response == null || response.equals("")) return -1;

                JSONObject json_data = new JSONObject(response);

                if (json_data.getInt("ID") > 0) {
                    ContentValues cv = new ContentValues();
                    cv.put("Sync", 1);
                    cv.put("ID", json_data.getInt("ID"));
                    db.update("TEMP_ACQUISITION", cv, "_id=?", new String[] { cur.getString(cur.getColumnIndex("_id")) });
                }
            }
        }
        catch (Exception e) { wurthMB.AddError("Sync_Acquisition", e.getMessage(), e); }

        if (cur != null && !cur.isClosed()) cur.close();

        return 1;
    }
}

