package ba.wurth.mb.DataLayer.Visits;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.Objects.Document;
import ba.wurth.mb.Classes.Objects.Visit;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Documents.DL_Documents;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class DL_Visits {
	private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
    private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

    private static 	String methodName = "";
	private static String className = "DL_Visits";

	// Database fields
	private static final String DATABASE_TABLE = "Visits";

    public static Visit GetByID(Long _id) {

        Visit tempVisit = null;

        try {

            final Cursor cur = db_readonly.rawQuery("select * from " + DATABASE_TABLE + " where _id = " + _id, null);

            if(cur != null && cur.getCount() > 0 && cur.moveToFirst()) {
                tempVisit = new Visit() {{
                    _id = cur.getLong(cur.getColumnIndex("_id"));
                    VisitID = cur.getLong(cur.getColumnIndex("VisitID"));
                    ClientID = cur.getLong(cur.getColumnIndex("ClientID"));
                    DeliveryPlaceID = cur.getLong(cur.getColumnIndex("DeliveryPlaceID"));
                    UserID = cur.getInt(cur.getColumnIndex("UserID"));
                    dt = cur.getLong(cur.getColumnIndex("dt"));
                    startDT = cur.getLong(cur.getColumnIndex("startDT"));
                    endDT = cur.getLong(cur.getColumnIndex("endDT"));
                    Note = cur.getString(cur.getColumnIndex("Note"));
                    Longitude = cur.getLong(cur.getColumnIndex("Longitude"));
                    Latitude = cur.getLong(cur.getColumnIndex("Latitude"));
                    Sync  = cur.getInt(cur.getColumnIndex("Sync"));
                }};
                cur.close();

                Cursor _cur = db_readonly.rawQuery("select _id from Documents where (_itemid = " + tempVisit._id + " OR ItemID = " + tempVisit.VisitID + " ) AND OptionID = 9 AND Active = 1", null);

                if (_cur != null) {
                    while (_cur.moveToNext()) {
                        Document d = DL_Documents.GetByID(_cur.getLong(0));
                        if (d != null) tempVisit.documents.add(d);
                    }
                    _cur.close();
                }
            }
		}
		catch (Exception ex) {
            Log.d("TEST", ex.getMessage());
		}
		return tempVisit;
	}

	public static int AddOrUpdate(Visit tempVisit) {
		
		methodName = "AddOrUpdate";
		
		try {

            db.beginTransaction();

			ContentValues cv = new ContentValues();

			cv.put("VisitID", tempVisit.VisitID);
			cv.put("AccountID", wurthMB.getUser().AccountID);
			cv.put("ClientID", tempVisit.ClientID);
			cv.put("UserID", tempVisit.UserID);
			cv.put("dt", tempVisit.dt);
            cv.put("startDT", tempVisit.startDT);
            cv.put("endDT", tempVisit.endDT);
			cv.put("Note", tempVisit.Note);
			cv.put("Latitude", tempVisit.Latitude);
			cv.put("Longitude", tempVisit.Longitude);
			cv.put("DeliveryPlaceID", tempVisit.DeliveryPlaceID);
			cv.put("Sync", 0);

            long _id = tempVisit._id;
            if (tempVisit._id == 0) {
                _id = db.insert(DATABASE_TABLE, null, cv);
                tempVisit._id = _id;
            }
            else {
                db.update(DATABASE_TABLE, cv, "_id=" + _id, null);
            }

            Iterator<ba.wurth.mb.Classes.Objects.Document> itr = tempVisit.documents.iterator();
            while (itr.hasNext()) {
                ba.wurth.mb.Classes.Objects.Document tempDocument = itr.next();

                if (tempDocument._id > 0) continue;

                ContentValues cvItems = new ContentValues();
                cvItems.put("_itemid", _id);
                cvItems.put("ItemID", tempVisit.VisitID);
                cvItems.put("DocumentID", tempDocument.DocumentID);
                cvItems.put("UserID", wurthMB.getUser().UserID);
                cvItems.put("AccountID", wurthMB.getUser().AccountID);
                cvItems.put("dt", tempDocument.dt);
                cvItems.put("data", tempDocument.data);
                cvItems.put("Type", tempDocument.Type);
                cvItems.put("DocumentType", tempDocument.DocumentType);
                cvItems.put("Latitude", tempDocument.Latitude);
                cvItems.put("Longitude", tempDocument.Longitude);
                cvItems.put("OptionID", tempDocument.OptionID);
                cvItems.put("Name", tempDocument.Name);
                cvItems.put("Description", tempDocument.Description);
                cvItems.put("fileName", tempDocument.fileName);
                cvItems.put("fileContentType", tempDocument.fileContentType);
                cvItems.put("fileSize", tempDocument.fileSize);
                cvItems.put("Active", tempDocument.Active);
                cvItems.put("Sync", 0);
                db.insert("Documents", null, cvItems);
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            return 1;
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, "", e);
			return -1;
		}
	}
	
	public static int Delete(long _id){
        methodName = "Delete";
        try
        {
            db.beginTransaction();
            db.delete("Visits", "_id=?", new String[] { Long.toString(_id) });
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, "", e);
            return -1;
        }
        return 1;
	}
	
	public static int Sync() {
		
		methodName = "Sync";
		
		Cursor cur = null;

        int ret = 0;

		try {
			cur = db_readonly.rawQuery("select " + DATABASE_TABLE + ".* from Visits Where Sync = ? AND AccountID = ? ORDER BY dt ASC", new String[] {"0", Long.toString(wurthMB.getUser().AccountID)});

			ArrayList<Object> dataArrays = new ArrayList<Object>();

	        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {

                ArrayList<Object> dataList = new ArrayList<Object>();

                dataList.add(cur.getLong(cur.getColumnIndex("_id")));
                dataList.add(cur.getLong(cur.getColumnIndex("VisitID")));
                dataList.add(cur.getLong(cur.getColumnIndex("AccountID")));
                dataList.add(cur.getLong(cur.getColumnIndex("UserID")));
                dataList.add(cur.getLong(cur.getColumnIndex("ClientID")));
                dataList.add(cur.getLong(cur.getColumnIndex("Latitude")));
                dataList.add(cur.getLong(cur.getColumnIndex("Longitude")));
                dataList.add(cur.getLong(cur.getColumnIndex("startDT")));
                dataList.add(cur.getLong(cur.getColumnIndex("endDT")));
                dataList.add(cur.getString(cur.getColumnIndex("Note")));
                dataList.add(cur.getLong(cur.getColumnIndex("DeliveryPlaceID")));
                dataList.add(cur.getLong(cur.getColumnIndex("dt")));
                dataArrays.add(dataList);		        	
	        }
	        cur.close();
	        
	        Iterator<Object> itr = dataArrays.iterator();
	        while (itr.hasNext()){
	        	@SuppressWarnings("unchecked")
				ArrayList<Object> el = (ArrayList<Object>) itr.next();
	        	JSONObject tempObject = new JSONObject();
	        	tempObject.put("VisitID", el.get(1));
	        	tempObject.put("AccountID", el.get(2));
	        	tempObject.put("UserID", el.get(3));
	        	tempObject.put("ClientID", el.get(4));
	        	tempObject.put("Latitude", el.get(5));
	        	tempObject.put("Longitude", el.get(6));
	        	tempObject.put("startTime", el.get(7));
	        	tempObject.put("endTime", el.get(8));
	        	tempObject.put("Note", el.get(9));
	        	tempObject.put("DeliveryPlaceID", el.get(10));
                tempObject.put("dt", el.get(11));
	        	
				ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				postParameters.add(new BasicNameValuePair("tempObject", tempObject.toString()));

				String response = CustomHttpClient.executeHttpPost(wurthMB.getUser().URL + "POST_Visits", postParameters).toString();

				if (response == null || response.equals("")) return -1;
				
				JSONObject json_data = new JSONObject(response);
				
				if (json_data.getLong("ID") > 0) {
					ContentValues cv = new ContentValues();
					cv.put("Sync", 1);
					cv.put("VisitID", json_data.getLong("ID"));
					db.update(DATABASE_TABLE, cv, "_id=" + el.get(0), null);

                    cv.remove("Sync");
                    db.update("ORDERS", cv, "_VisitID=" + el.get(0), null);

                    cv.remove("VisitID");

                    cv.put("ItemID", json_data.getLong("ID"));
                    cv.put("Sync", 0);
                    db.update("Documents", cv, "_itemid=" + el.get(0), null);
                    ret++;
				}  
	        }	        
		}
		catch (Exception e) {
		    wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
		}
		
		if (cur != null) cur.close();

		return ret;
	}
}
