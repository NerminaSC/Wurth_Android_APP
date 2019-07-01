package ba.wurth.mb.DataLayer.Activities;

import android.database.Cursor;

import ba.wurth.mb.Classes.wurthMB;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class DL_Activities {
	
	private static 	String methodName = "";
	private static String className = "DL_Activities";
	private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
	private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

	public static Cursor Get(String searchWord) {
		
		methodName = "Get";
				
		Cursor cur = null;
		
		try {
			cur = db_readonly.rawQuery("select * FROM Activity "
					+ " where Name like '%" + searchWord + "%'" 
					+ " And Active = 1 "
					+ " And ((RecieverID = 0 AND RecieverGroup = '') OR RecieverID = " + wurthMB.getUser().ApplicationUserID +  ") "
					+ " And AccountID = " + wurthMB.getUser().AccountID
					+ " Order By Name", null);

            cur.getCount();
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
		}

		return cur;
	}

	public static Cursor GetByID(Long _id) {
		Cursor cur = db_readonly.rawQuery("SELECT * FROM Activity WHERE _id = " + _id, null);
		cur.moveToFirst();
        return cur;
	}
}
