package ba.wurth.mb.DataLayer.Reports;

import android.content.Context;
import android.database.Cursor;
import io.requery.android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import ba.wurth.mb.Classes.wurthMB;

public class DL_Reports {
	//private Context ctx;
	private static String TAG = "DataLayer";
	private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
	private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

	// Database fields
	public static final String KEY_ROWID = "_id";
	public static final String KEY_ORDERID = "OrderID";

	public DL_Reports(Context context) {
		//this.ctx = context;
	}

	public static Double GetOrderTotals(Long startDate, Long endDate, Long ClientID, Long DeliveryPlaceID) {
		try {
			Cursor mCount= db_readonly.rawQuery("select sum(GrandTotal) " +
                    " from Orders "
                    + "Where AccountID = " + wurthMB.getUser().AccountID
                    + (ClientID > 0L ? " And Orders.ClientID = " + ClientID : "")
                    + (DeliveryPlaceID > 0L ? " And Orders.DeliveryPlaceID = " + DeliveryPlaceID : "")
                    + " And OrderStatusID <> 8 AND OrderStatusID > 3 And UserID = " + wurthMB.getUser().UserID + " And OrderDate >=" + startDate + " and OrderDate < " + endDate, null);

			mCount.moveToFirst();
			Double count= mCount.getDouble(0);
			mCount.close();
			return count;
		}
		catch (Exception e) {
			wurthMB.AddError(TAG, "", e);
		}
		return 0D;
	}

	public static int GetOrderCount(Long startDate, Long endDate, Long ClientID, Long DeliveryPlaceID) {
		try {
			Cursor mCount= db_readonly.rawQuery("select count(*) "
                    + "from Orders "
                    + " where AccountID = " + wurthMB.getUser().AccountID
                    + (ClientID > 0L ? " And Orders.ClientID = " + ClientID : "")
                    + (DeliveryPlaceID > 0L ? " And Orders.DeliveryPlaceID = " + DeliveryPlaceID : "")
                    + " And UserID = " + wurthMB.getUser().UserID + " And DOE >=" + startDate + " and DOE < " + endDate, null);

			mCount.moveToFirst();
			int count= mCount.getInt(0);
			mCount.close();
			return count;
		}
		catch (Exception e) {
			wurthMB.AddError(TAG, "", e);
		}
		return 0;
	}
	
	public static int GetVisitCount(Long startDate, Long endDate, Long ClientID, Long DeliveryPlaceID) {
		try {
			Cursor mCount= db_readonly.rawQuery("select count(*) "
                    + "from Visits "
                    + "where AccountID = " + wurthMB.getUser().AccountID
                    + (ClientID > 0L ? " And Visits.ClientID = " + ClientID : "")
                    + (DeliveryPlaceID > 0L ? " And Visits.DeliveryPlaceID = " + DeliveryPlaceID : "")
                    + " And UserID = " + wurthMB.getUser().UserID + " And dt >=" + startDate + " and dt < " + endDate, null);

			mCount.moveToFirst();
			int count= mCount.getInt(0);
			mCount.close();
			return count;
		}
		catch (Exception e) {
			wurthMB.AddError(TAG, "", e);
		}
		return 0;
	}

	public static int GetDocumentCount(Long startDate, Long endDate, Long ClientID, Long DeliveryPlaceID) {
		try {
			Cursor mCount= db_readonly.rawQuery("select count(*) " +
                            " from Documents " +
                            " where AccountID = " + wurthMB.getUser().AccountID +
                            " And UserID = " + wurthMB.getUser().UserID + " And dt >=" + startDate + " and dt < " + endDate, null);

			mCount.moveToFirst();
			int count= mCount.getInt(0);
			mCount.close();
			return count;
		}
		catch (Exception e) {
			wurthMB.AddError(TAG, "", e);
		}
		return 0;
	}
	
	public static Cursor GetOrderTotalsByProducts(Long startDate, Long endDate, Long ClientID, Long DeliveryPlaceID) {
		Cursor cur = null;
		try {
			cur = db_readonly.rawQuery("SELECT ARTIKLI.sifra, OrderItems.ProductName, SUM(OrderItems.Quantity) AS Count, Sum(OrderItems.GrandTotal) AS Total " +
                    " FROM Orders " +

                    " INNER JOIN OrderItems ON Orders.OrderID = OrderItems.OrderID " +
                    " INNER JOIN Products ON OrderItems.ProductID = Products.ProductID " +
                    " INNER JOIN ARTIKLI ON Products._productid = ARTIKLI.ID " +

                    " WHERE Orders.AccountID = " + wurthMB.getUser().AccountID +
                    " AND Orders.OrderStatusID > 3 " +
                    " AND Orders.OrderStatusID <> 8 " +
                    " And Orders.UserID = " + wurthMB.getUser().UserID +
                    " And Orders.OrderDate >=" + startDate +
                    " AND Orders.OrderDate < " + endDate +

                    (ClientID > 0L ? " AND Orders.ClientID = " + ClientID  : "") +

                    " GROUP BY OrderItems.ProductID, OrderItems.ProductName " +

                    " ORDER BY OrderItems.ProductName", null);
		}
		catch (Exception e) {
			wurthMB.AddError(TAG, "", e);
		}
		return cur;
	}

	public static Cursor GetProductReport(Long startDate, Long endDate, Long ClientID, Long DeliveryPlaceID, Long ProductID) {
		Cursor cur = null;
		try {
			cur = db_readonly.rawQuery("SELECT ARTIKLI.sifra, Orders.OrderDate, OrderItems.ProductName, OrderItems.Note, OrderItems.Quantity, OrderItems.Total, OrderItems.GrandTotal, OrderItems.Price_WS, OrderItems.UserDiscountPercentage, OrderItems.ClientDiscountPercentage  " +
					" FROM Orders " +

					" INNER JOIN OrderItems ON Orders.OrderID = OrderItems.OrderID " +
					" INNER JOIN Products ON OrderItems.ProductID = Products.ProductID " +
					" INNER JOIN ARTIKLI ON Products._productid = ARTIKLI.ID " +

					" WHERE Orders.AccountID = " + wurthMB.getUser().AccountID +
					" AND Orders.OrderStatusID > 3 " +
					" AND Orders.OrderStatusID <> 8 " +
					" And Orders.OrderDate >=" + startDate +
					" AND Orders.OrderDate < " + endDate +

					(ClientID > 0L ? " AND Orders.ClientID = " + ClientID  : "") +
					" AND OrderItems.ProductID = " + ProductID +

					" ORDER BY Orders.OrderDate DESC", null);

				cur.getCount();
		}
		catch (Exception e) {
			wurthMB.AddError(TAG, "", e);
		}

		return cur;
	}

	public static Cursor GetTotalsByOrders(Long startDate, Long endDate, Long ClientID, Long DeliveryPlaceID) {
		Cursor cur = null;
        try {
    		cur = db_readonly.rawQuery("Select Orders.OrderDate, Orders.GrandTotal"
            		+ " From Orders"
            		+ " Where Orders.AccountID = " + wurthMB.getUser().AccountID
            		+ " And Orders.OrderStatusID <> 8 " 
    				+ " And Orders.UserID = " + wurthMB.getUser().UserID
    				+ " And Orders.OrderDate >=" + startDate + " and Orders.OrderDate < " + endDate
                    + (ClientID > 0L ? " And Orders.ClientID = " + ClientID : "")
                    + (DeliveryPlaceID > 0L ? " And Orders.DeliveryPlaceID = " + DeliveryPlaceID : "")
    				+ " And Orders.Active = 1 Order By Orders.OrderDate ASC", null);
            
        }
        catch (Exception e) {

        }
        return cur;
	}	

}
