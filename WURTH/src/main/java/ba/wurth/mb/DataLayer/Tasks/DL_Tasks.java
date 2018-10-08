package ba.wurth.mb.DataLayer.Tasks;

import android.content.ContentValues;
import android.database.Cursor;
import io.requery.android.database.sqlite.SQLiteDatabase;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.Objects.Task;
import ba.wurth.mb.Classes.wurthMB;

public class DL_Tasks {
	//private Context ctx;
	private static String methodName = "";
	private static String className = "DL_Tasks";
	private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
    private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();


    // Database fields
	private static final String DATABASE_TABLE = "Tasks";

	public static Cursor Get() {
		Cursor cur = db_readonly.rawQuery("select * from " + DATABASE_TABLE
									+ " where AccountID = " + wurthMB.getUser().AccountID
									+ " And Active = 1", null);
        return cur;
	}

    public static Cursor GET_Log(Long TaskID) {
        Cursor cur = db_readonly.rawQuery("SELECT * FROM TaskLog WHERE TaskID = " + TaskID + " ORDER BY DOE DESC", null);
        return cur;
    }

	public static Task GetByID(long id) {
		
		methodName = "GetByID";
		
		try {
			
			final Cursor cur;
	        cur = db_readonly.rawQuery("select * from " + DATABASE_TABLE + " where _id = " + id, null);
	        Task temp = null;
	        
	        if (cur.moveToFirst()) {
                temp = new Task() {{
		        	_id = cur.getLong(cur.getColumnIndex("_id"));
                    TaskID = cur.getLong(cur.getColumnIndex("TaskID"));
		        	AccountID = cur.getInt(cur.getColumnIndex("AccountID"));
                    StatusID = cur.getInt(cur.getColumnIndex("StatusID"));
                    Name = cur.getString(cur.getColumnIndex("Name"));
		        	Description = cur.getString(cur.getColumnIndex("Description"));
		        	Parameters = cur.getString(cur.getColumnIndex("Parameters"));
		        }};
		        
	        }
	        cur.close();
	        return temp;
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
			return null;
		}
		finally {

		}
	}

    public static Task GetByTaskID(long TaskID) {

        methodName = "GetByID";

        try {

            final Cursor cur;
            cur = db_readonly.rawQuery("select * from " + DATABASE_TABLE + " where TaskID = " + TaskID, null);
            Task temp = null;

            if (cur.moveToFirst()) {
                temp = new Task() {{
                    _id = cur.getLong(cur.getColumnIndex("_id"));
                    TaskID = cur.getLong(cur.getColumnIndex("TaskID"));
                    AccountID = cur.getInt(cur.getColumnIndex("AccountID"));
                    StatusID = cur.getInt(cur.getColumnIndex("StatusID"));
                    Name = cur.getString(cur.getColumnIndex("Name"));
                    Description = cur.getString(cur.getColumnIndex("Description"));
                    Parameters = cur.getString(cur.getColumnIndex("Parameters"));
                }};

            }
            cur.close();
            return temp;
        }
        catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
            return null;
        }
        finally {

        }
    }

	public static int AddOrUpdate(Task mItem) {

		try {
			
			ContentValues values = new ContentValues();
			values.put("TaskID", mItem.TaskID);
			values.put("AccountID", mItem.AccountID);
            values.put("StatusID", mItem.StatusID);
			values.put("Description", mItem.Description);
			values.put("Name", mItem.Name);
			values.put("Parameters", mItem.Parameters);
			values.put("Active", mItem.Active);
			values.put("DOE", mItem.DOE);
			values.put("Sync", 1);			

			if (mItem._id == 0) db.insert(DATABASE_TABLE, null, values);
			else db.update(DATABASE_TABLE, values, "_id=" + mItem._id, null);

            AddOrUpdateLog(mItem);
			return 1;
		}
		catch (Exception e) {
			return -1;
		}
		finally {

		}
	}

    public static int AddOrUpdateLog(Task mItem) {

        try {

            ContentValues values = new ContentValues();
            values.put("TaskID", mItem.TaskID);
            values.put("AccountID", mItem.AccountID);
            values.put("UserID", wurthMB.getUser().UserID);
            values.put("StatusID", mItem.StatusID);
            values.put("Parameters", mItem.ParametersLog);
            values.put("DOE", mItem.DOE);
            values.put("Sync", 0);

            db.insert("TaskLog", null, values);
            return 1;
        }
        catch (Exception e) {
            return -1;
        }
        finally {

        }
    }

    public static int UpdateStatus(Long TaskID, int StatusID) {
        try {
            ContentValues values = new ContentValues();
            values.put("TaskID", TaskID);
            values.put("StatusID", StatusID);
            values.put("Sync", 0);
            db.update(DATABASE_TABLE, values, "TaskID=" + TaskID, null);
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

    public static int[] GET_TaskPending() {
        try {

            int ret1 = 0;
            int ret2 = 0;

            Cursor cur = DL_Tasks.Get();

            if (cur != null) {

                while (cur.moveToNext()) {

                    JSONObject jsonObject = new JSONObject(cur.getString(cur.getColumnIndex("Parameters")));

                    JSONArray users = jsonObject.getJSONArray("Users");

                    boolean userExists = false;

                    for(int i = 0; i < users.length(); i++) {
                        if (users.getJSONObject(i).getLong("UserID") == wurthMB.getUser().UserID) {
                            userExists = true;
                            break;
                        }
                    }

                    Date startDate = null, endDate = null;

                    if (!jsonObject.isNull("startDate")) {
                        startDate = new Date(jsonObject.getLong("startDate"));
                    }

                    if (!jsonObject.isNull("endDate")) {
                        endDate = new Date(jsonObject.getLong("endDate"));
                    }

                    if (userExists && startDate != null && startDate.getTime() <= System.currentTimeMillis() && (endDate == null || (endDate.getTime() >= System.currentTimeMillis()))) {
                        ret1++;
                    }

                    if (userExists && cur.getInt(cur.getColumnIndex("StatusID")) == 2) {
                        ret2++;
                    }

                }
                cur.close();
            }

            return new int [] {ret1 ,ret2};
        }
        catch (Exception ex) {
            return new int [] {0 ,0};
        }
    }

    public static JSONObject GET_CLIENT_TaskPending(long ClientID) {

        JSONObject jsonObject = null;

        try {

            Cursor cur = db_readonly.rawQuery("SELECT * FROM Tasks WHERE Parameters LIKE '%\"ClientID\": " + ClientID + "%' AND Parameters LIKE '%\"UserID\": " + wurthMB.getUser().UserID + "%' AND StatusID = 2 ORDER BY DOE", null);

            if (cur != null && cur.getCount() > 0) {

                while (cur.moveToNext()) {

                    JSONObject _jsonObject = new JSONObject(cur.getString(cur.getColumnIndex("Parameters")));
                    Date startDate = null, endDate = null;

                    if (!_jsonObject.isNull("startDate")) startDate = new Date(_jsonObject.getLong("startDate"));
                    if (!_jsonObject.isNull("endDate")) endDate = new Date(_jsonObject.getLong("endDate"));

                    if (startDate != null && startDate.getTime() <= System.currentTimeMillis()) {
                        jsonObject = new JSONObject();
                        jsonObject.put("TaskID", cur.getLong(cur.getColumnIndex("TaskID")));
                        jsonObject.put("Name", cur.getString(cur.getColumnIndex("Name")));
                        jsonObject.put("Description", cur.getString(cur.getColumnIndex("Description")));
                        jsonObject.put("startDate", startDate.getTime());
                        if (endDate != null) jsonObject.put("endDate", endDate.getTime());
                        break;
                    }

                }
                cur.close();
            }


        }
        catch (Exception ex) { }

        return jsonObject;
    }
}
