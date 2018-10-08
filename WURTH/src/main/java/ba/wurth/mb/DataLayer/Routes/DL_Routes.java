package ba.wurth.mb.DataLayer.Routes;

import android.content.ContentValues;
import android.database.Cursor;
import io.requery.android.database.sqlite.SQLiteDatabase;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.Objects.Route;
import ba.wurth.mb.Classes.wurthMB;

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
		}
		catch (Exception e) {
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
		}
		catch (Exception e) {
			return -1;
		}
		finally {

		}
	}
	
	public static int Delete(long _id){
		try
		{
			db.delete(DATABASE_TABLE, "_id=?", new String[] { Long.toString(_id) });
			return 1;
		}
		catch (Exception e) {
			return -1;
		}
	}

}
