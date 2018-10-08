package ba.wurth.mb.DataLayer.Additional;

import android.content.ContentValues;
import android.database.Cursor;
import io.requery.android.database.sqlite.SQLiteDatabase;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.wurthMB;

public class DL_Additional {
	private static 	String methodName = "";
	private static String className = "DL_Additional";

	private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
    private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

    public static Cursor Get_DeliveryPlacesProperties() {
        Cursor cur = db_readonly.rawQuery("select * From DeliveryPlacesProperties WHERE AccountID = " + wurthMB.getUser().AccountID + " Order By Name ", null);
        return cur;
    }

    public static Cursor Get_DeliveryPlacesPropertiesOptions(int DPPID) {
        Cursor cur = db_readonly.rawQuery("select * From DeliveryPlacesPropertiesOptions WHERE AccountID = " + wurthMB.getUser().AccountID + " AND DPPID = " + DPPID + " Order By Name ", null);
        return cur;
    }

    public static Cursor Get_Merchandise() {
        Cursor cur = db_readonly.rawQuery("select * From Merchandise WHERE AccountID = " + wurthMB.getUser().AccountID + " Order By Name ", null);
        return cur;
    }

    public static Cursor Get_DeliveryPlaceMerchandise(Long DeliveryPlaceID) {

        Cursor cur = db_readonly.rawQuery("select DeliveryPlaceMerchandise.Count, DOE, Merchandise.Name From DeliveryPlaceMerchandise INNER JOIN Merchandise ON DeliveryPlaceMerchandise.MerchandiseID = Merchandise.MerchandiseID WHERE DeliveryPlaceMerchandise.AccountID = " + wurthMB.getUser().AccountID + " AND DeliveryPlaceID = " + DeliveryPlaceID + " ORDER BY DeliveryPlaceMerchandise.DOE DESC", null);
        return cur;
    }

    public static Cursor Get_DeliveryPlaceMerchandiseTotals(Long DeliveryPlaceID) {

        Cursor cur = db_readonly.rawQuery("select SUM(DeliveryPlaceMerchandise.Count) AS Count, Merchandise.Name From DeliveryPlaceMerchandise INNER JOIN Merchandise ON DeliveryPlaceMerchandise.MerchandiseID = Merchandise.MerchandiseID WHERE DeliveryPlaceMerchandise.AccountID = " + wurthMB.getUser().AccountID + " AND DeliveryPlaceID = " + DeliveryPlaceID + " ORDER BY Merchandise.Name DESC", null);
        return cur;
    }

    public static Cursor Get_Competitions(String searchWord) {
        Cursor cur = db_readonly.rawQuery("select _id, CompetitionID, Name, Address from Competitions"
                + " where Name like '%" + searchWord + "%'"
                + " And Active = 1 "
                + " And AccountID = " + wurthMB.getUser().AccountID
                + " Order By Name", null);
        return cur;
    }

    public static Cursor Get_Competition_Items(Long CompetitionID, String searchWord) {
        Cursor cur = db_readonly.rawQuery("select _id, ItemID, Name, Description from Competition_Items"
                + " WHERE CompetitionID = " + CompetitionID
                + " AND Name like '%" + searchWord + "%'"
                + " AND Active = 1 "
                + " AND AccountID = " + wurthMB.getUser().AccountID
                + " ORDER BY Name", null);
        return cur;
    }

    public static Cursor Get_Competition_Items_Values(Long DeliveryPlaceID) {
        Cursor cur = db_readonly.rawQuery("select Competition_Items_Values.*, Competition_Items.Name AS ItemName, Competitions.Name AS CompetitionName FROM Competition_Items_Values"
                + " INNER JOIN Competition_Items ON Competition_Items_Values.ItemID = Competition_Items.ItemID "
                + " INNER JOIN Competitions ON Competition_Items.CompetitionID = Competitions.CompetitionID "
                + " where Competition_Items_Values.DeliveryPlaceID = " + DeliveryPlaceID
                + " Order By Competition_Items_Values.DOE DESC", null);
        return cur;
    }

    public static Cursor Get_Product_Placement_Values(Long DeliveryPlaceID) {
        Cursor cur = db_readonly.rawQuery("select DeliveryPlaceProductPlacement.*, Products.Name AS ProductName FROM DeliveryPlaceProductPlacement"
                + " INNER JOIN Products ON DeliveryPlaceProductPlacement.ProductID = Products.ProductID "
                + " WHERE DeliveryPlaceProductPlacement.DeliveryPlaceID = " + DeliveryPlaceID
                + " ORDER BY DeliveryPlaceProductPlacement.DOE DESC", null);
        return cur;
    }

    public static Exception Sync() {

        if (wurthMB.getUser() == null || wurthMB.getUser().URL.equals("")) {
            return null;
        }

        Cursor cur = null;

        JSONObject jsonObj = new JSONObject();
        JSONObject jsonObj_Clients = new JSONObject();
        JSONObject jsonObj_DeliveryPlaces = new JSONObject();
        JSONObject jsonObj_DeliveryPlacesProperties = new JSONObject();
        JSONObject jsonObj_DeliveryPlaceMerchandise = new JSONObject();
        JSONObject jsonObj_Competitions = new JSONObject();
        JSONObject jsonObj_ProductPlacement = new JSONObject();

        try {

            methodName = "Sync";

            // Clients
            cur = db_readonly.rawQuery("select * from Clients Where AccountID = " + wurthMB.getUser().AccountID + " And Sync = 0 ORDER BY DOE ASC", null);

            ArrayList<Object> dataArrays_Clients = new ArrayList<Object>();

            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                ArrayList<Object> dataList = new ArrayList<Object>();
                dataList.add(cur.getLong(cur.getColumnIndex("ClientID")));
                dataList.add(cur.getLong(cur.getColumnIndex("Latitude")));
                dataList.add(cur.getLong(cur.getColumnIndex("Longitude")));
                dataList.add(cur.getLong(cur.getColumnIndex("DOE")));
                dataList.add(cur.getLong(cur.getColumnIndex("AccountID")));
                dataList.add(cur.getString(cur.getColumnIndex("code")));
                dataArrays_Clients.add(dataList);
            }
            cur.close();

            JSONArray tempArray_Clients = new JSONArray();
            Iterator<Object> itr_Clients = dataArrays_Clients.iterator();
            while (itr_Clients.hasNext()){
                @SuppressWarnings("unchecked")
                ArrayList<Object> el = (ArrayList<Object>) itr_Clients.next();
                JSONObject tempObject = new JSONObject();
                tempObject.put("ClientID", el.get(0));
                tempObject.put("Latitude", el.get(1));
                tempObject.put("Longitude", el.get(2));
                tempObject.put("DOE", el.get(3));
                tempObject.put("AccountID", el.get(4));
                tempObject.put("code", el.get(5));
                tempArray_Clients.put(tempObject);
            }
            jsonObj.put("Clients", tempArray_Clients.toString());

            cur.close();
            // End of Clients


            // Delivery places
            cur = db_readonly.rawQuery("select * from DeliveryPlaces Where AccountID = " + wurthMB.getUser().AccountID + " And Sync = 0 ORDER BY DOE ASC", null);

            ArrayList<Object> dataArrays_DeliveryPlaces = new ArrayList<Object>();

            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                ArrayList<Object> dataList = new ArrayList<Object>();
                dataList.add(cur.getLong(cur.getColumnIndex("DeliveryPlaceID")));
                dataList.add(cur.getLong(cur.getColumnIndex("Latitude")));
                dataList.add(cur.getLong(cur.getColumnIndex("Longitude")));
                dataList.add(cur.getLong(cur.getColumnIndex("DOE")));
                dataList.add(cur.getLong(cur.getColumnIndex("AccountID")));
                dataList.add(cur.getString(cur.getColumnIndex("code")));
                dataArrays_DeliveryPlaces.add(dataList);
            }
            cur.close();

            JSONArray tempArray_DeliveryPlaces = new JSONArray();
            Iterator<Object> itr_DeliveryPlaces = dataArrays_DeliveryPlaces.iterator();
            while (itr_DeliveryPlaces.hasNext()){
                @SuppressWarnings("unchecked")
                ArrayList<Object> el = (ArrayList<Object>) itr_DeliveryPlaces.next();
                JSONObject tempObject = new JSONObject();
                tempObject.put("DeliveryPlaceID", el.get(0));
                tempObject.put("Latitude", el.get(1));
                tempObject.put("Longitude", el.get(2));
                tempObject.put("DOE", el.get(3));
                tempObject.put("AccountID", el.get(4));
                tempObject.put("code", el.get(5));

                tempArray_DeliveryPlaces.put(tempObject);
            }
            jsonObj.put("DeliveryPlaces", tempArray_DeliveryPlaces.toString());
            // End of delivery places


            if (tempArray_Clients.length() > 0 || tempArray_DeliveryPlaces.length() > 0) {
                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("jsonObj", jsonObj.toString()));

                String response = CustomHttpClient.executeHttpPost(wurthMB.getUser().URL + "MobileUpdates", postParameters).toString();

                if (response == null || response.equals("")) return null;

                JSONObject json_data = new JSONObject(response);

                if (json_data.getInt("DeliveryPlaces_Status") == 1) {
                    ContentValues cv = new ContentValues();
                    cv.put("Sync", 1);
                    db.update("DeliveryPlaces", cv, "AccountID=? And Sync=?", new String[] { Long.toString(wurthMB.getUser().AccountID), "0" });
                    db.update("Clients", cv, "AccountID=? And Sync=?", new String[] { Long.toString(wurthMB.getUser().AccountID), "0" });
                }
            }
            cur = null;

        }
        catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
        }

        if (cur != null && !cur.isClosed()) cur.close();

        return null;
    }

}
