package ba.wurth.mb.Classes;

import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ba.wurth.mb.Classes.Objects.PricelistItem;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class Common {
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    @SuppressWarnings("unused")
		double d = Double.parseDouble(str);
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}

    public static boolean isServiceRunning(Context ctx, String srvName) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (srvName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        if (newHeight == 0) {
            //newHeight = (height * scaleWidth) / width;
        }
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

    public static JSONArray remove(final int idx, final JSONArray from) {
        final List<JSONObject> objs = asList(from);
        objs.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objs) {
            ja.put(obj);
        }

        return ja;
    }

    public static JSONArray addItem(final int idx, JSONArray array, JSONObject item){
        final JSONArray ja = new JSONArray();

        try{
            final List<JSONObject> objs = asList(array);
            objs.add(0, item);

            for (final JSONObject obj : objs) {
                ja.put(obj);
            }

        }catch (Exception e){

        }
	    return ja;
    }

    public static List<JSONObject> asList(final JSONArray ja) {
        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<JSONObject>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }

    public static Long getNextBusinessDay(Date date, boolean isSaturday) {

        date = addDays(date, 1);

        while (isHoliday(date, isSaturday)) {
            date = addDays(date, 1);
        }

        return date.getTime();

    }

    public static boolean isHoliday(Date d, boolean isSaturday) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);

        boolean stm = false;

        if (Calendar.SATURDAY == c.get(c.DAY_OF_WEEK) || Calendar.SUNDAY == c.get(c.DAY_OF_WEEK)) {
            stm = true;
        }

        return stm;
    }

    public static Date addDays(Date d, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    public static PointF calculateDerivedPosition(PointF point, double range, double bearing)
    {
        double EarthRadius = 6371000; // m

        double latA = Math.toRadians(point.x);
        double lonA = Math.toRadians(point.y);
        double angularDistance = range / EarthRadius;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(Math.sin(latA) * Math.cos(angularDistance) + Math.cos(latA) * Math.sin(angularDistance) * Math.cos(trueCourse));

        double dlon = Math.atan2(Math.sin(trueCourse) * Math.sin(angularDistance) * Math.cos(latA), Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));

        double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);

        PointF newPoint = new PointF((float) lat, (float) lon);

        return newPoint;

    }

    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371; //kilometers
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;
        return dist * 1000;
    }

    public static JSONArray cur2JsonArray(Cursor cursor) {
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.isNull(i)) rowObject.put(cursor.getColumnName(i), "");
                        else rowObject.put(cursor.getColumnName(i), cursor.getString(i)); }
                    catch (Exception e) { }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        return resultSet;
    }

    public static JSONObject cur2JsonObject(Cursor cursor) {
        cursor.moveToFirst();
        int totalColumn = cursor.getColumnCount();
        JSONObject resultSet = new JSONObject();
        for (int i = 0; i < totalColumn; i++) {
            if (cursor.getColumnName(i) != null) {
                try {
                    if (cursor.isNull(i)) resultSet.put(cursor.getColumnName(i), "");
                    else resultSet.put(cursor.getColumnName(i), cursor.getString(i)); }
                catch (Exception e) { }
            }
        }
        cursor.close();
        return resultSet;
    }

    public static class PriceComparator implements Comparator<PricelistItem>
    {
        public int compare(PricelistItem left, PricelistItem right) {
            return left.KolicinaOD.compareTo(right.KolicinaOD);
        }
    }

    public static boolean isTableExists(SQLiteDatabase db, String tableName)
    {
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }
}
