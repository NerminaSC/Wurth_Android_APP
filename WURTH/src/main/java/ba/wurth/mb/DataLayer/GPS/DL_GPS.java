package ba.wurth.mb.DataLayer.GPS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.GPS.Log;
import ba.wurth.mb.Classes.wurthMB;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class DL_GPS {
	//private Context ctx;
	private static String methodName = "";
	private static String className = "DL_GPS";
	private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
	private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

	// Database fields
	public static final String KEY_ROWID = "_id";

	public DL_GPS(Context context) {
		//this.ctx = context;
	}

	public static int AddOrUpdate(Log tempGPS) {

		methodName = "AddOrUpdate";
		
		db.beginTransaction();

		try {
			ContentValues cv = new ContentValues();
			cv.put("AccountID", wurthMB.getUser().AccountID);
			cv.put("UserID", wurthMB.getUser().UserID);
			cv.put("Latitude", tempGPS.Latitude * 10000000);
			cv.put("Longitude", tempGPS.Longitude * 10000000);
			cv.put("Speed", tempGPS.Speed);
			cv.put("Altitude", tempGPS.Altitude);
			cv.put("Accuracy", tempGPS.Accuracy);
			cv.put("Bearing", tempGPS.Bearing);
			cv.put("Time", tempGPS.Time);
			cv.put("DOE", new Date().getTime());
			cv.put("Sync", 0);

			db.insert("GPS", null, cv);
			db.setTransactionSuccessful();
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, "", e);
			return -1;
		}
		db.endTransaction();
		return 1;
	}

	public static Exception Sync() {
		
		methodName = "Sync";
		
		Cursor cur = null;
		
		JSONObject jsonObj = new JSONObject();
		
		try {
			
			// Delivery places
			cur = db_readonly.rawQuery("select * from GPS Where Sync = 0 And AccountID = " + wurthMB.getUser().AccountID + " And UserID = " + wurthMB.getUser().UserID + " Order by Time ASC LIMIT 10", null);

			ArrayList<Object> dataArrays = new ArrayList<Object>();

	        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                ArrayList<Object> dataList = new ArrayList<Object>();

                dataList.add(cur.getLong(cur.getColumnIndex("AccountID")));
                dataList.add(cur.getLong(cur.getColumnIndex("UserID")));
                dataList.add(cur.getLong(cur.getColumnIndex("Latitude")));
                dataList.add(cur.getLong(cur.getColumnIndex("Longitude")));
                dataList.add(cur.getFloat(cur.getColumnIndex("Speed")));
                dataList.add(cur.getDouble(cur.getColumnIndex("Altitude")));
                dataList.add(cur.getLong(cur.getColumnIndex("Time")));
                dataList.add(cur.getFloat(cur.getColumnIndex("Accuracy")));
                dataList.add(cur.getFloat(cur.getColumnIndex("Bearing")));
                dataList.add(cur.getString(cur.getColumnIndex("_id")));
                dataArrays.add(dataList);		        	
	        }
	        cur.close();
	        
			JSONArray tempArray = new JSONArray();
			Iterator<Object> itr = dataArrays.iterator();
	        while (itr.hasNext()){
				ArrayList<Object> el = (ArrayList<Object>) itr.next();
				
	        	JSONObject tempObject = new JSONObject();
	        	tempObject.put("AccountID", el.get(0));
	        	tempObject.put("UserID", el.get(1));
	        	tempObject.put("Latitude", el.get(2));
	        	tempObject.put("Longitude", el.get(3));
	        	tempObject.put("Speed", el.get(4));
	        	tempObject.put("Altitude", el.get(5));
	        	tempObject.put("dt", el.get(6));
	        	tempObject.put("Accuracy", el.get(7));
	        	tempObject.put("Bearing", el.get(8));
	        	tempObject.put("IMEI", wurthMB.IMEI);
	        	
	        	tempArray.put(tempObject);
	        }
	        jsonObj.put("GPS", tempArray.toString());
	        // End of delivery places
	        
	        if (tempArray.length() > 0 ) {
				ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				postParameters.add(new BasicNameValuePair("jsonObj", jsonObj.toString()));

				String response = CustomHttpClient.executeHttpPost(wurthMB.getUser().URL + "UpdateLocation", postParameters).toString();
				
				if (response == null || response.equals("")) return null;
				
				JSONObject json_data = new JSONObject(response);
				
				if (json_data.getInt("Status") == 1) {
					Iterator<Object> _itr = dataArrays.iterator();
					db.beginTransaction();
			        while (_itr.hasNext()) {
			        	ArrayList<Object> _el = (ArrayList<Object>) _itr.next();
						ContentValues cv = new ContentValues();
						cv.put("Sync", 1);
						db.update("GPS", cv, "_id=?", new String[] { (String) _el.get(9) });
			        }
			        db.setTransactionSuccessful();
			        db.endTransaction();
				}
	        }
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, "", e);
		}
		
		if (cur != null && !cur.isClosed()) cur.close();

		return null;
	}	
}
