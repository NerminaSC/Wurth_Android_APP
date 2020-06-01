package ba.wurth.mb.DataLayer.Routes;

import android.app.job.JobInfo;
import android.content.ContentValues;
import android.database.Cursor;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.Objects.CompetitionValue;
import ba.wurth.mb.Classes.Objects.Route;
import ba.wurth.mb.Classes.wurthMB;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class DL_Routes {
    private static String methodName = "";
    private static String className = "DL_Routes";
    private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
    private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

    // Database fields
    private static final String DATABASE_TABLE = "Routes";

    public static Cursor Get() {
        Cursor cur = db_readonly.rawQuery("select * from " + DATABASE_TABLE
                + " where AccountID = " + wurthMB.getUser().AccountID
                + " And UserID = " + wurthMB.getUser().UserID
                + " And Active = 1 "
                + " Order By DOE DESC", null);
        cur.getCount();
        return cur;
    }

    public static Route GetByID(long id) {

        methodName = "GetByID";
        Route tempRoute = null;

        try {

            final Cursor cur;
            cur = db_readonly.rawQuery("select * from " + DATABASE_TABLE + " where _id = " + id, null);

            if (cur.moveToFirst()) {
                tempRoute = new Route() {{
                    _id = cur.getLong(cur.getColumnIndex("_id"));
                    RouteID = cur.getLong(cur.getColumnIndex("RouteID"));
                    AccountID = cur.getInt(cur.getColumnIndex("AccountID"));
                    Name = cur.getString(cur.getColumnIndex("Name"));
                    Description = cur.getString(cur.getColumnIndex("Description"));
                    Sync = cur.getInt(cur.getColumnIndex("Sync"));
                    raw = cur.getString(cur.getColumnIndex("raw"));
                    code = cur.getString(cur.getColumnIndex("code"));
                }};

            }
            cur.close();
        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, "", e);
        }
        return tempRoute;
    }

    public static int AddOrUpdate(Route tempItem) {

        try {

            ContentValues values = new ContentValues();
            values.put("RouteID", tempItem.RouteID);
            values.put("AccountID", wurthMB.getUser().AccountID);
            values.put("UserID", wurthMB.getUser().UserID);
            values.put("Description", tempItem.Description);
            values.put("Name", tempItem.Name);
            values.put("raw", tempItem.raw);
            values.put("code", tempItem.code);
            values.put("Active", tempItem.Active);
            values.put("DOE", tempItem.DOE);
            values.put("Sync", 0);

            if (tempItem._id == 0) db.insert(DATABASE_TABLE, null, values);
            else db.update(DATABASE_TABLE, values, "_id=" + tempItem._id, null);
            return 1;
        } catch (Exception e) {
            return -1;
        } finally {

        }
    }

    public static int Delete(long _id) {
        try {
            db.delete(DATABASE_TABLE, "_id=?", new String[]{Long.toString(_id)});
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }

    public static int OptimizeRoute(long id, int location_type){

        try{
            Cursor c = db_readonly.rawQuery("SELECT * FROM " + DATABASE_TABLE + " WHERE _id = " + id + " LIMIT 1", null);

            if(c != null && c.moveToFirst()){

                Double start_latitude = 0D;
                Double start_longitude = 0D;

                switch (location_type){
                    case 1: // current location
                        start_latitude = wurthMB.currentBestLocation.getLatitude() * 10000000D;
                        start_longitude = wurthMB.currentBestLocation.getLongitude() * 10000000D;
                        break;
                    case 2: // user location
                        start_latitude = wurthMB.getUser().data.has("start_location") ? wurthMB.getUser().data.getJSONObject("start_location").getDouble("latitude") * 10000000D : 0;
                        start_longitude = wurthMB.getUser().data.has("start_location") ? wurthMB.getUser().data.getJSONObject("start_location").getDouble("longitude") * 10000000D : 0;
                        break;
                }

                JSONObject data = new JSONObject(c.getString(c.getColumnIndex("raw")));
                JSONArray nodes = (JSONArray) data.get("nodes");

                JSONObject node = new JSONObject();
                node.put("latitude", start_latitude);
                node.put("longitude", start_longitude);

                nodes = Common.addItem(0, nodes, node);
                nodes.put(node);

                data.put("nodes", nodes);

                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("tempObject", data.toString()));

                String response = CustomHttpClient.executeHttpPost(wurthMB.getUser().URL + "Optimize_Routes", postParameters).toString();

                if (response == null || response.equals("")) return -1;

                JSONObject json_data = new JSONObject(response);

                if(json_data.has("nodes") && ((JSONArray)json_data.get("nodes")).length() > 0){

                    // UPDATE ROUTE
                    ContentValues values = new ContentValues();
                    values.put("raw", json_data.toString());
                    values.put("Sync", 0);
                    db.update(DATABASE_TABLE, values, "_id=" + id, null);
                }
            }

        }catch (Exception e){
            return 0;
        }

        return 1;
    }

    public static int Sync() {

        methodName = "Sync";

        Cursor cur = null;

        int ret = 0;

        try {
            cur = db_readonly.rawQuery("select * from Routes Where Sync = ? AND AccountID = ? ORDER BY DOE ASC LIMIT 1", new String[]{"0", Long.toString(wurthMB.getUser().AccountID)});

            if (cur != null &&   cur.moveToFirst()) {

                JSONObject resp = Common.cur2JsonObject(cur);

                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("tempObject", resp.toString()));

                String response = CustomHttpClient.executeHttpPost(wurthMB.getUser().URL + "POST_Routes", postParameters).toString();

                if (response == null || response.equals("")) return -1;

                JSONObject json_data = new JSONObject(response);

                if (json_data.getLong("ID") > 0) {
                    ContentValues cv = new ContentValues();
                    cv.put("Sync", 1);
                    cv.put("RouteID", json_data.getLong("ID"));
                    db.update(DATABASE_TABLE, cv, "_id=" + resp.getLong("_id"), null);
                    ret++;
                }
                else {

                }
            }
        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
        }

        if (cur != null) cur.close();

        return ret;
    }
}
