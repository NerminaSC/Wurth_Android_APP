package ba.wurth.mb.DataLayer.Documents;

import android.content.ContentValues;
import android.database.Cursor;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import ba.wurth.mb.Classes.Base64;
import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.Objects.Document;
import ba.wurth.mb.Classes.wurthMB;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class DL_Documents {
	private static String methodName = "";
	private static String className = "DL_GPS";
	private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
	private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

	// Database fields
	private static final String DATABASE_TABLE = "Documents";

	public Cursor Get(String searchWord) {
		Cursor cur;
		cur = db_readonly.rawQuery("select _id, DocumentID, AccountID, ClientID, UserID, Type, dt, DocumentType, Note, Latitude, Longitude, Sync, strftime('%d.%m.%Y %H:%M', (dt / 1000), 'unixepoch', 'localtime') As dtString from Documents Where ClientID = " + wurthMB.getClient().ClientID + " AND UserID = " + wurthMB.getUser().UserID + " order by dt desc", null);
        return cur;
	}

	public static Document GetByID(long _id) {
		
		methodName = "GetByID";

        Document tempDocument = null;

		try {
			final Cursor cur = db_readonly.rawQuery("select * FROM " + DATABASE_TABLE + "  Where _id=?", new String[]{Long.toString(_id)});

	        if( cur.moveToFirst()) {
		        tempDocument = new Document() {{
		        	_id = cur.getLong(cur.getColumnIndex("_id"));
		        	UserID = cur.getLong(cur.getColumnIndex("UserID"));
		        	data = cur.getBlob(cur.getColumnIndex("data"));
		        	dt = cur.getLong(cur.getColumnIndex("dt"));
		        	DocumentType = cur.getInt(cur.getColumnIndex("DocumentType"));
		        	Type = cur.getInt(cur.getColumnIndex("Type"));
		        	Longitude = cur.getLong(cur.getColumnIndex("Longitude"));
		        	Latitude = cur.getLong(cur.getColumnIndex("Latitude"));
                    OptionID = cur.getInt(cur.getColumnIndex("OptionID"));
                    ItemID = cur.getLong(cur.getColumnIndex("ItemID"));
                    fileName = cur.getString(cur.getColumnIndex("fileName"));
                    fileContentType = cur.getString(cur.getColumnIndex("fileContentType"));
                    fileSize = cur.getInt(cur.getColumnIndex("fileSize"));
                    Name = cur.getString(cur.getColumnIndex("Name"));
                    Description = cur.getString(cur.getColumnIndex("Description"));
                    url = cur.getString(cur.getColumnIndex("url"));
                    Active = cur.getInt(cur.getColumnIndex("Active"));
                    Sync = cur.getInt(cur.getColumnIndex("Sync"));
		        }};
	        }
	        cur.close();
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, "", e);
		}
        return tempDocument;
	}
	
	public static int AddOrUpdate(Document tempDocument) {
		
		methodName = "AddOrUpdate";
		
		try {

			ContentValues cv = new ContentValues();
			cv.put("DocumentID", tempDocument.DocumentID);
			cv.put("UserID", wurthMB.getUser().UserID);
			cv.put("AccountID", wurthMB.getUser().AccountID);
			cv.put("dt", tempDocument.dt);
			cv.put("data", tempDocument.data);
			cv.put("Type", tempDocument.Type);
            cv.put("DocumentType", tempDocument.DocumentType);
			cv.put("Latitude", tempDocument.Latitude);
			cv.put("Longitude", tempDocument.Longitude);
            cv.put("OptionID", tempDocument.OptionID);
            cv.put("ItemID", tempDocument.ItemID);
            cv.put("Name", tempDocument.Name);
            cv.put("Description", tempDocument.Description);
            cv.put("fileName", tempDocument.fileName);
            cv.put("fileContentType", tempDocument.fileContentType);
            cv.put("fileSize", tempDocument.fileSize);
            cv.put("Active", tempDocument.Active);
			cv.put("Sync", 0);

			if (tempDocument._id == 0) db.insert(DATABASE_TABLE, null, cv);	
			else db.update(DATABASE_TABLE, cv, "_id=?", new String[] { Long.toString(tempDocument._id) });
			return 1;
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, "", e);
			return -1;
		}
	}
	
	public int Delete(long _id){
		try
		{
			db.delete(DATABASE_TABLE, "_id=?", new String[] { Long.toString(_id) });
			return 1;
		}
		catch (Exception e) {
			return -1;
		}
	}
	
	public static int Sync() {
		
		methodName = "Sync";

		Cursor cur = null;

        int ret = 0;

		try {
			cur = db_readonly.rawQuery("select * from " + DATABASE_TABLE + " Where Sync = ? AND AccountID = ? AND UserID = ? order by dt asc", new String[] {"0", Long.toString(wurthMB.getUser().AccountID), Long.toString(wurthMB.getUser().UserID)});

			ArrayList<Object> dataArrays = new ArrayList<Object>();

	        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                ArrayList<Object> dataList = new ArrayList<Object>();

                dataList.add(cur.getLong(cur.getColumnIndex("_id")));
                dataList.add(cur.getLong(cur.getColumnIndex("DocumentID")));
                dataList.add(cur.getLong(cur.getColumnIndex("AccountID")));
                dataList.add(cur.getLong(cur.getColumnIndex("UserID")));
                dataList.add(cur.getInt(cur.getColumnIndex("DocumentType")));
                dataList.add(cur.getInt(cur.getColumnIndex("Type")));
                dataList.add(cur.getLong(cur.getColumnIndex("Latitude")));
                dataList.add(cur.getLong(cur.getColumnIndex("Longitude")));
                dataList.add(cur.getLong(cur.getColumnIndex("dt")));
                dataList.add(cur.getBlob(cur.getColumnIndex("data")));
                dataList.add(cur.getInt(cur.getColumnIndex("OptionID")));
                dataList.add(cur.getLong(cur.getColumnIndex("ItemID")));
                dataList.add(cur.getString(cur.getColumnIndex("Name")));
                dataList.add(cur.getString(cur.getColumnIndex("Description")));
                dataList.add(cur.getString(cur.getColumnIndex("fileName")));
                dataList.add(cur.getInt(cur.getColumnIndex("fileSize")));
                dataList.add(cur.getString(cur.getColumnIndex("fileContentType")));
                dataArrays.add(dataList);		        	
	        }			
	        
	        cur.close();
	        
	        Iterator<Object> itr = dataArrays.iterator();
	        while (itr.hasNext()){
				@SuppressWarnings("unchecked")
				ArrayList<Object> el = (ArrayList<Object>) itr.next();
				
				JSONObject tempObject = new JSONObject();
	        	tempObject.put("DocumentID", el.get(1));
	        	tempObject.put("AccountID", el.get(2));
	        	tempObject.put("UserID", el.get(3));
	        	tempObject.put("DocumentType", el.get(4));
	        	tempObject.put("Type", el.get(5));
	        	tempObject.put("Latitude", el.get(6));
	        	tempObject.put("Longitude", el.get(7));
	        	tempObject.put("dt", el.get(8));
	        	byte[] data = (byte[]) el.get(9);
                tempObject.put("OptionID", el.get(10));
                tempObject.put("ItemID", el.get(11));
                tempObject.put("Name", el.get(12));
                tempObject.put("Description", el.get(13));
                tempObject.put("fileName", el.get(14));
                tempObject.put("fileSize", el.get(15));
                tempObject.put("fileContentType", el.get(16));
	        	
				ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				postParameters.add(new BasicNameValuePair("tempObject", tempObject.toString()));
				postParameters.add(new BasicNameValuePair("data", Base64.encodeBytes(data)));

				String response = CustomHttpClient.executeHttpPost(wurthMB.getUser().URL + "POST_Documents", postParameters).toString();
				
				if (response == null || response.equals("")) return -1;
				
				JSONObject json_data = new JSONObject(response);
				
				if (json_data.getLong("ID") > 0){
					ContentValues cv = new ContentValues();
					cv.put("Sync", 1);
					cv.put("DocumentID", json_data.getLong("ID"));
					db.update(DATABASE_TABLE, cv, "_id=?" , new String[] { el.get(0).toString() });
                    ret++;
				}
	        }	        
		}
		catch (Exception e) { wurthMB.AddError(className + " " + methodName, e.getMessage(), e); }

		if (cur != null && !cur.isClosed()) cur.close();
		
		return ret;
	}
}
