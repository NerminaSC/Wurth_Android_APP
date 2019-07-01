package ba.wurth.mb.DataLayer.Activities;

import android.content.ContentValues;
import android.database.Cursor;

import ba.wurth.mb.Classes.Objects.Record;
import ba.wurth.mb.Classes.wurthMB;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class DL_UserActivityLog {

	private static 	String methodName = "";
	private static String className = "DL_UserActivityLog";
	private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
	private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

	public static Cursor Get(String searchWord) {
		Cursor cur;
		
		cur = db_readonly.rawQuery("SELECT User_Activity_Log.*, Project.Name AS ProjectName FROM User_Activity_Log "
        		+ " LEFT JOIN Project ON User_Activity_Log.ProjectID = Project.ProjectID "
				+ " Where User_Activity_Log.AccountID = " + wurthMB.getUser().ApplicationAccountID
        		+ " And User_Activity_Log.UserID = " + wurthMB.getUser().ApplicationUserID
				+ " And User_Activity_Log.Active = 1 "
				+ " Order By User_Activity_Log.startTime Desc" , null);
		
        return cur;
	}	
	
	public static Cursor Get(Long startTime, Long endTime) {
		Cursor cur;
		
		cur = db_readonly.rawQuery("SELECT User_Activity_Log.*, Project.Name AS ProjectName, Host.Name AS HostName, Host.Label AS HostLabel FROM User_Activity_Log "
        		+ " LEFT JOIN Project ON User_Activity_Log.ProjectID = Project.ProjectID "
        		+ " LEFT JOIN Host ON User_Activity_Log.ResourceItemID = Host.HostID "
				+ " Where User_Activity_Log.AccountID = " + wurthMB.getUser().ApplicationAccountID
        		+ " And User_Activity_Log.UserID = " + wurthMB.getUser().ApplicationUserID
				+ " And User_Activity_Log.Active = 1 "
				+ " And User_Activity_Log.startTime >= " + startTime
				+ " And User_Activity_Log.endTime < " + endTime
				+ " Order By User_Activity_Log.startTime Desc" , null);
		
        return cur;
	}	
	
	public static Record GetByID(long _id) {
		
		methodName = "GetByID";
		
		try {
			
			final Cursor cur = db_readonly.rawQuery("Select * FROM User_Activity_Log WHERE _id = ?", new String[]{ Double.toString(_id) });

	        Record record = null;

	        if (cur.moveToFirst()){
	        	record = new Record() {{
	        		
	        		_id =  cur.getLong(cur.getColumnIndex("_id"));
		        	
		        	ID = cur.getLong(cur.getColumnIndex("ID"));
					AccountID = cur.getLong(cur.getColumnIndex("AccountID"));
					UserID = cur.getLong(cur.getColumnIndex("UserID"));
					OptionID = cur.getInt(cur.getColumnIndex("OptionID"));
					ItemID = cur.getLong(cur.getColumnIndex("ItemID"));
					startTime = cur.getLong(cur.getColumnIndex("startTime"));
					endTime = cur.getLong(cur.getColumnIndex("endTime"));
					ProjectID = cur.getLong(cur.getColumnIndex("ProjectID"));
					ClientID = cur.getLong(cur.getColumnIndex("ClientID"));
					DeliveryPlaceID = cur.getLong(cur.getColumnIndex("DeliveryPlaceID"));
					startLatitude = cur.getLong(cur.getColumnIndex("startLatitude"));
					startLongitude = cur.getLong(cur.getColumnIndex("startLongitude"));
					endLatitude = cur.getLong(cur.getColumnIndex("endLatitude"));
					endLongitude = cur.getLong(cur.getColumnIndex("endLongitude"));
					Billable = cur.getInt(cur.getColumnIndex("Billable")) == 1 ? true : false;
					Version = cur.getInt(cur.getColumnIndex("Version"));
					Reference = cur.getString(cur.getColumnIndex("Reference"));
					Locked = cur.getInt(cur.getColumnIndex("Locked"));
					Description = cur.getString(cur.getColumnIndex("Description"));
					Duration = cur.getInt(cur.getColumnIndex("Duration"));
					IP = cur.getString(cur.getColumnIndex("IP"));
					Licence = cur.getString(cur.getColumnIndex("Licence"));
					GroupID = cur.getLong(cur.getColumnIndex("GroupID"));
					MediaID = cur.getInt(cur.getColumnIndex("MediaID"));
					TravelOrderID = cur.getLong(cur.getColumnIndex("TravelOrderID"));
					ResourceID = cur.getLong(cur.getColumnIndex("ResourceID"));
					ResourceItemID = cur.getLong(cur.getColumnIndex("ResourceItemID"));
					Deviation = cur.getInt(cur.getColumnIndex("Deviation"));
					UserName = cur.getString(cur.getColumnIndex("UserName"));
					GroupName = cur.getString(cur.getColumnIndex("GroupName"));
					ItemName = cur.getString(cur.getColumnIndex("ItemName"));
					Prolific = cur.getInt(cur.getColumnIndex("Prolific")) == 1 ? true : false;
					DOE = cur.getLong(cur.getColumnIndex("DOE"));
					Sync = cur.getInt(cur.getColumnIndex("Sync"))== 1 ? true : false;
		        }};
	        }
	        cur.close();
	        return record;			
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
			return null;
		}
	}	
	
	public static Long AddOrUpdate (Record record) {

		methodName = "AddOrUpdate";
		
		db.beginTransaction();
		
		try {
			
			ContentValues values = new ContentValues();
			
        	values.put("ID", record.ID);
			values.put("AccountID", record.AccountID);
			values.put("UserID", record.UserID);
			values.put("OptionID", record.OptionID);
			values.put("ItemID", record.ItemID);
			values.put("startTime", record.startTime);
			values.put("endTime", record.endTime);
			values.put("ProjectID", record.ProjectID);
			values.put("ClientID", record.ClientID);
			values.put("DeliveryPlaceID", record.DeliveryPlaceID);
			values.put("startLatitude", record.startLatitude);
			values.put("startLongitude", record.startLongitude);
			values.put("endLatitude", record.endLatitude);
			values.put("endLongitude", record.endLongitude);
			values.put("Billable", record.Billable);
			values.put("Version", record.Version);
			values.put("Reference", record.Reference);
			values.put("Locked", record.Locked);
			values.put("Description", record.Description);
			values.put("Duration", record.Duration);
			values.put("IP", record.IP);
			values.put("Licence", record.Licence);
			values.put("GroupID", record.GroupID);
			values.put("MediaID", record.MediaID);
			values.put("TravelOrderID", record.TravelOrderID);
			values.put("ResourceID", record.ResourceID);
			values.put("ResourceItemID", record.ResourceItemID);
			values.put("Deviation", record.Deviation);
			values.put("UserName", record.UserName);
			values.put("GroupName",record.GroupName);
			values.put("ItemName", record.ItemName);
			values.put("Prolific", record.Prolific ? 1 : 0);
			values.put("DOE", record.DOE);
			values.put("Active", record.Active ? 1 : 0);
			values.put("Sync", record.Sync ? 1 : 0);
			
			if (record._id == 0) {
				db.insert("User_Activity_Log", null, values);
				String query = "SELECT ROWID from User_Activity_Log Order by ROWID DESC limit 1";
				Cursor c = db_readonly.rawQuery(query, null);
				if (c != null && c.moveToFirst()) {
					record._id = c.getLong(0);
				}
				c.close();
			}
			else  db.update("User_Activity_Log", values, "_id=" + record._id, null);

			db.setTransactionSuccessful();
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
			return -1L;
		}
		
		db.endTransaction();
		return record._id;
	}

	public static int Delete(long _id){
		
		methodName = "Delete";
		
		try {
			ContentValues values = new ContentValues();
			values.put("Active", 0);
			values.put("Sync", 0);
			values.put("DOE", System.currentTimeMillis());
			db.update("User_Activity_Log", values, "_id=?", new String[]{ Long.toString(_id)});
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
			return -1;
		}
		return 1;
	}

}
