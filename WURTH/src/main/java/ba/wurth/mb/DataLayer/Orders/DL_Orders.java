package ba.wurth.mb.DataLayer.Orders;

import android.content.ContentValues;
import android.database.Cursor;
import io.requery.android.database.sqlite.SQLiteDatabase;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.Objects.Order;
import ba.wurth.mb.Classes.Objects.OrderItem;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Visits.DL_Visits;

public class DL_Orders {
	//private Context ctx;
	private static 	String methodName = "";
	private static String className = "DL_Orders";
	private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
	private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();
	
	// Database fields
	private static final String DATABASE_TABLE_ORDERS = "Orders";
	private static final String DATABASE_TABLE_ORDER_ITEMS = "OrderItems";

	public static Cursor GetAll(String searchWord, Long ClientID, Long DeliveryPlaceID, int OrderStatusID, Long OrderDate, Long limit) {

        Cursor cur = null;

        try {

            String sql = "";

            switch (wurthMB.getUser().AccessLevelID) {
                case 1: //Director
                    sql = "";
                    break;
                case 2: //Manager
                    sql = " AND (A.Region LIKE '" + wurthMB.getUser().Region.substring(0,2) + "%' OR B.Region LIKE '" + wurthMB.getUser().Region.substring(0,2) + "%' OR Orders.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                case 5: //Sales Persons
                case 9: //Sales user (WEB)
                    sql = " AND (A.KomercijalistaID = " + wurthMB.getUser()._userid + " OR B.KomercijalistaID = " + wurthMB.getUser()._userid + " OR Orders.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                case 7: //Key Account Manager
                    sql = " AND (C.KAM = " + wurthMB.getUser()._userid + " OR D.KAM = " + wurthMB.getUser()._userid + " OR Orders.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                case 8: //Area sales manager
					sql = " AND (A.Region LIKE '" + wurthMB.getUser().Region.substring(0,4) + "%' OR B.Region LIKE '" + wurthMB.getUser().Region.substring(0,4) + "%' OR Orders.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                default:break;
            }

            cur = db_readonly.rawQuery(" Select Orders._id, Orders.OrderID, Orders.ClientID, Orders.DeliveryPlaceID, Orders.OrderDate, Orders.GrandTotal, Orders.OrderStatusID, Orders.OrderID, Orders.Sync, A.Naziv As ClientName, '' As DeliveryPlace, Users.Firstname || ' ' || Users.Lastname AS Username "

                    + " From Orders "

                    + " LEFT JOIN Clients ON Orders.ClientID = Clients.ClientID "
                    + " LEFT JOIN DeliveryPlaces On Orders.DeliveryPlaceID = DeliveryPlaces.DeliveryPlaceID "
                    + " LEFT JOIN Partner A ON Clients._clientid = A.ID "
                    + " LEFT JOIN Partner B ON DeliveryPlaces._deliveryplaceid = B.ID "
					+ " LEFT JOIN Users ON Orders.UserID = Users.UserID "

                    + " LEFT JOIN PARTNER_DETALJI C ON A.ID = C.CustomerID "
                    + " LEFT JOIN PARTNER_DETALJI D ON B.ID = D.CustomerID "

                    + " LEFT JOIN KOMERCIJALISTI K1 ON A.KomercijalistaID = K1.ID "
                    + " LEFT JOIN KOMERCIJALISTI K2 ON B.KomercijalistaID = K2.ID "

					+ " LEFT JOIN KOMERCIJALISTI KAM1 ON C.KAM = KAM1.ID "
					+ " LEFT JOIN KOMERCIJALISTI KAM2 ON D.KAM = KAM2.ID "

					+ " WHERE Orders.AccountID = " + wurthMB.getUser().AccountID
                    + " AND Orders.OrderStatusID <> 11 And Orders.OrderStatusID <> 12 "
                    + " AND Orders.Active = 1 "
                    + " AND (A.Naziv Like '%" + searchWord + "%' OR B.Naziv Like '%" + searchWord + "%')"

                    + (ClientID > 0L ? " AND Orders.ClientID = " + ClientID : "")
                    + (DeliveryPlaceID > 0L ? " AND Orders.DeliveryPlaceID = " + DeliveryPlaceID : "")
                    + (OrderStatusID > 0 ? " AND Orders.OrderStatusID = " + OrderStatusID : "")
                    + (OrderDate > 0L ? " AND Orders.OrderDate >= " + OrderDate + " AND Orders.OrderDate < " + (OrderDate + 86400000): "")

                    + sql

                    + " ORDER BY Orders.OrderDate DESC "

					+ (limit > 0L ? " LIMIT " + limit : "")

					, null);
        }
        catch (Exception ex) {
            wurthMB.AddError("DL_Orders", ex.getMessage(), ex);
        }
		cur.getCount();
        return cur;
	}

    public static Cursor GetProposals(String searchWord, Long ClientID, Long DeliveryPlaceID, int OrderStatusID, Long OrderDate) {

        Cursor cur = null;

        try {
            String sql = "";

            switch (wurthMB.getUser().AccessLevelID) {
                case 1: //Director
                    sql = "";
                    break;
                case 2: //Manager
                    sql = " AND (K1.Region LIKE '" + wurthMB.getUser().Region.substring(0,2) + "%' OR K2.Region LIKE '" + wurthMB.getUser().Region.substring(0,2) + "%' OR Orders.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                case 5: //Sales Persons
                case 9: //Sales user (WEB)
                    sql = " AND (A.KomercijalistaID = " + wurthMB.getUser()._userid + " OR B.KomercijalistaID = " + wurthMB.getUser()._userid + " OR Orders.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                case 7: //Key Account Manager
                    sql = " AND (C.KAM = " + wurthMB.getUser()._userid + " OR D.KAM = " + wurthMB.getUser()._userid + " OR Orders.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                case 8: //Area sales manager
                    sql = " AND (K1.Region LIKE '" + wurthMB.getUser().Region.substring(0,4) + "%' OR K2.Region LIKE '" + wurthMB.getUser().Region.substring(0,4) + "%' OR Orders.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                default:break;
            }

			cur = db_readonly.rawQuery(" Select Orders._id, Orders.OrderID, Orders.ClientID, Orders.DeliveryPlaceID, Orders.OrderDate, Orders.GrandTotal, Orders.OrderStatusID, Orders.OrderID, Orders.Sync, A.Naziv As ClientName, '' As DeliveryPlace, Users.Firstname || ' ' || Users.Lastname AS Username "

					+ " From Orders "

                    + " Left Join Clients On Orders.ClientID = Clients.ClientID "
                    + " Left Join DeliveryPlaces On Orders.DeliveryPlaceID = DeliveryPlaces.DeliveryPlaceID "

                    + " Left Join Partner A On Clients._clientid = A.ID "
                    + " Left Join Partner B On DeliveryPlaces._deliveryplaceid = B.ID "
					+ " LEFT JOIN Users ON Orders.UserID = Users.UserID "

                    + " LEFT JOIN PARTNER_DETALJI C ON A.ID = C.CustomerID "
                    + " LEFT JOIN PARTNER_DETALJI D ON B.ID = D.CustomerID "

                    + " LEFT JOIN KOMERCIJALISTI K1 ON A.KomercijalistaID = K1.ID "
                    + " LEFT JOIN KOMERCIJALISTI K2 ON B.KomercijalistaID = K2.ID "

                    + " Where Orders.AccountID = " + wurthMB.getUser().AccountID
                    + " And Orders.UserID = " + wurthMB.getUser().UserID
                    + " And Orders.Active = 1 "
                    + " And (A.Naziv Like '%" + searchWord + "%' OR B.Naziv Like '%" + searchWord + "%')"

                    + (ClientID > 0L ? " AND Orders.ClientID = " + ClientID : "")
                    + (DeliveryPlaceID > 0L ? " AND Orders.DeliveryPlaceID = " + DeliveryPlaceID : "")
                    + (OrderStatusID > 0 ? " AND Orders.OrderStatusID = " + OrderStatusID : "")
                    + (OrderDate > 0L ? " AND Orders.OrderDate >= " + OrderDate + " AND Orders.OrderDate < " + (OrderDate + 86400000): "")

                    + sql

                    + " Order By Orders.OrderDate Desc" , null);
        }
        catch (Exception ex) {

        }

		cur.getCount();
        return cur;
    }

	public static Order GetByID(long _id) {
		
		methodName = "GetByID";

        Order tempOrder = null;

		try {

            final Cursor cur, c;

            cur = db_readonly.rawQuery("Select Orders.*, PaymentMethods.Name As PaymentMethodName, OrderStatus.Name As OrderStatusName, Clients.Name AS ClientName, DeliveryPlaces.Name AS DeliveryPlaceName "
                    + " From " + DATABASE_TABLE_ORDERS
                    + " Left Join PaymentMethods On Orders.PaymentMethodID = PaymentMethods.PaymentMethodID "
                    + " Left Join OrderStatus On Orders.OrderStatusID = OrderStatus.OrderStatusID "
                    + " Left Join Clients On Orders.ClientID = Clients.ClientID "
                    + " Left Join DeliveryPlaces On Orders.DeliveryPlaceID = DeliveryPlaces.DeliveryPlaceID "
                    + " Where Orders._id = ?", new String[]{ Double.toString(_id) });

	        if (cur.moveToFirst()){
		        tempOrder = new Order() {{
		        	_id = cur.getLong(cur.getColumnIndex("_id"));
                    AccountID = cur.getLong(cur.getColumnIndex("AccountID"));
		        	OrderID = cur.getLong(cur.getColumnIndex("OrderID"));
		        	ClientID = cur.getLong(cur.getColumnIndex("ClientID"));
                    ClientName = cur.getString(cur.getColumnIndex("ClientName"));
                    DeliveryPlaceName = cur.getString(cur.getColumnIndex("DeliveryPlaceName"));
		        	UserID = cur.getLong(cur.getColumnIndex("UserID"));
		        	OrderDate = cur.getLong(cur.getColumnIndex("OrderDate"));
		        	PaymentDate = cur.getLong(cur.getColumnIndex("PaymentDate"));
		        	DeliveryDate = cur.getLong(cur.getColumnIndex("DeliveryDate"));
		        	OrderReference = cur.getString(cur.getColumnIndex("OrderReference"));
                    Relations = cur.getString(cur.getColumnIndex("Relations"));
		        	Note = cur.getString(cur.getColumnIndex("Note"));
		        	Total = cur.getDouble(cur.getColumnIndex("Total"));
		        	GrandTotal = cur.getDouble(cur.getColumnIndex("GrandTotal"));
		        	TaxTotal = cur.getDouble(cur.getColumnIndex("TaxTotal"));
		        	DiscountPercentage = cur.getDouble(cur.getColumnIndex("DiscountPercentage"));
		        	DiscountTotal = cur.getDouble(cur.getColumnIndex("DiscountTotal"));
		        	PaymentMethodID = cur.getInt(cur.getColumnIndex("PaymentMethodID"));
		        	PaymentMethodName = cur.getString(cur.getColumnIndex("PaymentMethodName"));
		        	OrderStatusID = cur.getInt(cur.getColumnIndex("OrderStatusID"));
		        	DeliveryPlaceID = cur.getInt(cur.getColumnIndex("DeliveryPlaceID"));
		        	OrderStatusName = cur.getString(cur.getColumnIndex("OrderStatusName"));
		        	Longitude = cur.getLong(cur.getColumnIndex("Longitude"));
		        	Latitude = cur.getLong(cur.getColumnIndex("Latitude"));
                    VisitID = cur.getLong(cur.getColumnIndex("VisitID"));
                    _VisitID = cur.getLong(cur.getColumnIndex("_VisitID"));
		        	Sync  = cur.getInt(cur.getColumnIndex("Sync"));
		        }};
		        
		        c = db_readonly.rawQuery("select * from " + DATABASE_TABLE_ORDER_ITEMS + " where Order_ID = " + _id, null);

		        while (c.moveToNext()) {

		        	OrderItem item = new OrderItem(){{
		        		ProductID = c.getInt(c.getColumnIndex("ProductID"));
		        		ProductName = c.getString(c.getColumnIndex("ProductName"));
		        		Price_RT = c.getDouble(c.getColumnIndex("Price_RT"));
		        		Price_WS = c.getDouble(c.getColumnIndex("Price_WS"));
		        		Quantity = c.getDouble(c.getColumnIndex("Quantity"));
		        		DiscountPercentage = c.getDouble(c.getColumnIndex("DiscountPercentage"));
		        		ClientDiscountPercentage = c.getDouble(c.getColumnIndex("ClientDiscountPercentage"));
		        		UserDiscountPercentage = c.getDouble(c.getColumnIndex("UserDiscountPercentage"));
		        		Note = c.getString(c.getColumnIndex("Note"));
	            		DiscountTotal = c.getDouble(c.getColumnIndex("DiscountTotal"));
	            		Total = c.getDouble(c.getColumnIndex("Total"));
	            		TaxTotal = c.getDouble(c.getColumnIndex("TaxTotal"));
	            		GrandTotal = c.getDouble(c.getColumnIndex("GrandTotal"));
	            		DiscountGroupPercentage = c.getDouble(c.getColumnIndex("DiscountGroupPercentage"));
	            		DiscountGroupActionPercentage = c.getDouble(c.getColumnIndex("DiscountGroupActionPercentage"));
	            		DiscountProductPercentage = c.getDouble(c.getColumnIndex("DiscountProductPercentage"));
		        	}};

                    try {
                        JSONObject jsonObj = new JSONObject(c.getString(c.getColumnIndex("Note")));
                        item.ArtikalID = jsonObj.getLong("ArtikalID");
                        item.Pakovanje = jsonObj.getDouble("Pakovanje");
                        item.Pakovanje_Barcode = jsonObj.getString("Pakovanje_Barcode");
                        item.Pakovanje_KodPakovanja = jsonObj.getString("Pakovanje_KodPakovanja");
                        item.Mjerna_Jedinica = jsonObj.getString("Mjerna_Jedinica");
                        item.Stanje_Zaliha = jsonObj.getDouble("Stanje_Zaliha");
                        item.Narucena_Kolicina = jsonObj.getDouble("Narucena_Kolicina");
                        item.Datum_Prijema = jsonObj.getLong("Datum_Prijema");
                        item.Predefinisana_Dostupnost = jsonObj.getInt("Predefinisana_Dostupnost");
                        item.Note = jsonObj.getString("Note");
                        item.KljucCijene = jsonObj.getInt("KljucCijene");

                    }
                    catch (JSONException ex) {}

		        	tempOrder.items.add(item);
	        	}	        	
		        c.close();		        
	        }

            if (tempOrder._VisitID > 0L) tempOrder.visit = DL_Visits.GetByID(tempOrder._VisitID);

	        cur.close();
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
		}
        return tempOrder;
    }

	public static int UpdateOrderStatus(long _id , int OrderStatusID) {
		
		methodName = "UpdateOrderStatus";
		
		try {
			ContentValues cv = new ContentValues();
			cv.put("OrderStatusID", OrderStatusID);
			cv.put("DOE", new Date().getTime());
			cv.put("Sync", 0);
			db.update(DATABASE_TABLE_ORDERS, cv, "_id=" + _id, null);
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
			return -1;
		}
		return 1;
	}
			
	public static int AddOrUpdate(Order tempOrder) {

		methodName = "AddOrUpdate";
		
		db.beginTransaction();

		try {

			ContentValues cv = new ContentValues();
			cv.put("OrderID", tempOrder.OrderID);
			cv.put("AccountID", wurthMB.getUser().AccountID);
			cv.put("ClientID", tempOrder.ClientID);
			cv.put("UserID", wurthMB.getUser().UserID);
			cv.put("OrderDate", tempOrder.OrderDate);
			cv.put("PaymentDate", tempOrder.PaymentDate);
			cv.put("DeliveryDate", tempOrder.DeliveryDate);
			cv.put("OrderReference", tempOrder.OrderReference);
			cv.put("Relations", tempOrder.Relations.toString());
			cv.put("Note", tempOrder.Note);
			cv.put("Latitude", tempOrder.Latitude);
			cv.put("Longitude", tempOrder.Longitude);
			cv.put("DiscountPercentage", tempOrder.DiscountPercentage);
			cv.put("DiscountTotal", tempOrder.DiscountTotal);
			cv.put("GrandTotal", tempOrder.GrandTotal);
			cv.put("PaymentMethodID", tempOrder.PaymentMethodID);
			cv.put("OrderStatusID", tempOrder.OrderStatusID);
			cv.put("DeliveryPlaceID", tempOrder.DeliveryPlaceID);
			cv.put("TaxTotal", tempOrder.TaxTotal);
			cv.put("Total", tempOrder.Total);
            cv.put("_VisitID", tempOrder._VisitID);
            cv.put("VisitID", tempOrder.VisitID);
			cv.put("Active", 1);
			cv.put("DOE", new Date().getTime());
			cv.put("Sync", 0);

			long _id = tempOrder._id;
			if (tempOrder._id == 0) {
                _id = db.insert(DATABASE_TABLE_ORDERS, null, cv);
                tempOrder._id = _id;
			}
			else {
				db.update(DATABASE_TABLE_ORDERS, cv, "_id=" + _id, null);
			}

			db.delete(DATABASE_TABLE_ORDER_ITEMS, "Order_ID=?", new String[] { Long.toString(_id) });
			
			Iterator<ba.wurth.mb.Classes.Objects.OrderItem> itr = tempOrder.items.iterator();
			while (itr.hasNext()) {
				ba.wurth.mb.Classes.Objects.OrderItem element = itr.next();

                JSONObject jsonObj = new JSONObject();

                try {
                    jsonObj.put("ArtikalID", element.ArtikalID);
                    jsonObj.put("Pakovanje", element.Pakovanje);
                    jsonObj.put("Pakovanje_Barcode", element.Pakovanje_Barcode);
                    jsonObj.put("Pakovanje_KodPakovanja", element.Pakovanje_KodPakovanja);
                    jsonObj.put("Mjerna_Jedinica", element.Mjerna_Jedinica);
                    jsonObj.put("Stanje_Zaliha", element.Stanje_Zaliha);
                    jsonObj.put("Narucena_Kolicina", element.Narucena_Kolicina);
                    jsonObj.put("Datum_Prijema", element.Datum_Prijema);
                    jsonObj.put("Predefinisana_Dostupnost", element.Predefinisana_Dostupnost);
                    jsonObj.put("KljucCijene", element.KljucCijene);
                    jsonObj.put("Note", element.Note);
					jsonObj.put("Special", element.Special);
                }
                catch (JSONException ex) {}
				
				ContentValues cvItems = new ContentValues();
				cvItems.put("Order_ID", _id);
				cvItems.put("OrderID", tempOrder.OrderID);
				cvItems.put("ProductID", element.ProductID);
				cvItems.put("ProductName", element.ProductName);
				cvItems.put("Note", jsonObj.toString());
				cvItems.put("Quantity", element.Quantity);
				cvItems.put("Price_RT", element.Price_RT);
				cvItems.put("Price_WS", element.Price_WS);
				cvItems.put("DiscountPercentage", element.DiscountPercentage);
				cvItems.put("ClientDiscountPercentage", element.ClientDiscountPercentage);
				cvItems.put("UserDiscountPercentage", element.UserDiscountPercentage);
				cvItems.put("DiscountTotal", element.DiscountTotal);
				cvItems.put("Tax", element.Tax);
				cvItems.put("TaxTotal", element.TaxTotal);
				cvItems.put("Total", element.Total);
				cvItems.put("GrandTotal", element.GrandTotal);
				cvItems.put("DiscountGroupPercentage", element.DiscountGroupPercentage);
				cvItems.put("DiscountGroupActionPercentage", element.DiscountGroupActionPercentage);
				cvItems.put("DiscountProductPercentage", element.DiscountProductPercentage);
				db.insert(DATABASE_TABLE_ORDER_ITEMS, null, cvItems);
			}	
			
			db.setTransactionSuccessful();
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
			return -1;
		}
		db.endTransaction();
		return 1;
	}
	
	public static int Delete(long _id){
		
		methodName = "Delete";
		
		try
		{
			db.beginTransaction();
			db.delete(DATABASE_TABLE_ORDERS, "_id=?", new String[] { Long.toString(_id) });
			db.delete(DATABASE_TABLE_ORDER_ITEMS, "Order_ID=?", new String[] { Long.toString(_id) });
			db.setTransactionSuccessful();
			db.endTransaction();	
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
			return -1;
		}
		return 1;
	}
	
	public static int Sync() {

        Cursor cur = null;

        int ret = 0;

		try {
			
			methodName = "Sync";
			
			cur = db_readonly.rawQuery("Select * From Orders Where Sync = ? AND AccountID = ? AND UserID = ? Order By OrderDate asc", new String[] {"0", Long.toString(wurthMB.getUser().AccountID), Long.toString(wurthMB.getUser().UserID)});

            DL_Visits.Sync();

			ArrayList<Object> dataArrays = new ArrayList<Object>();
	        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {

                if (wurthMB.getOrder() != null && wurthMB.getOrder()._id == cur.getLong(cur.getColumnIndex("_id"))) continue;

                ArrayList<Object> dataList = new ArrayList<Object>();
                dataList.add(cur.getLong(cur.getColumnIndex("OrderID")));
                dataList.add(cur.getLong(cur.getColumnIndex("AccountID")));
                dataList.add(cur.getLong(cur.getColumnIndex("ClientID")));
                dataList.add(cur.getLong(cur.getColumnIndex("UserID")));
                dataList.add(cur.getLong(cur.getColumnIndex("OrderDate")));
                dataList.add(cur.getLong(cur.getColumnIndex("DeliveryDate")));
                dataList.add(cur.getLong(cur.getColumnIndex("PaymentDate")));
                dataList.add(cur.getString(cur.getColumnIndex("OrderReference")));
                dataList.add(cur.getString(cur.getColumnIndex("Note")));
                dataList.add(cur.getDouble(cur.getColumnIndex("DiscountPercentage")));
                dataList.add(cur.getDouble(cur.getColumnIndex("DiscountTotal")));
                dataList.add(cur.getDouble(cur.getColumnIndex("TaxTotal")));
                dataList.add(cur.getDouble(cur.getColumnIndex("Total")));
                dataList.add(cur.getDouble(cur.getColumnIndex("GrandTotal")));
                dataList.add(cur.getInt(cur.getColumnIndex("PaymentMethodID")));
                dataList.add(cur.getInt(cur.getColumnIndex("OrderStatusID")));
                dataList.add(cur.getInt(cur.getColumnIndex("DeliveryPlaceID")));
                dataList.add(cur.getLong(cur.getColumnIndex("Longitude")));
                dataList.add(cur.getLong(cur.getColumnIndex("Latitude")));
                dataList.add(cur.getString(cur.getColumnIndex("Relations")));
                dataList.add(cur.getLong(cur.getColumnIndex("DOE")));
                dataList.add(cur.getLong(cur.getColumnIndex("VisitID")));
                
		        Cursor c = db_readonly.rawQuery("select * from " + DATABASE_TABLE_ORDER_ITEMS + " where Order_ID = " + Long.toString(cur.getLong(cur.getColumnIndex("_id"))), null);
		        ArrayList<Object> _dataArrays = new ArrayList<Object>();
		        while (c.moveToNext()) {
	                ArrayList<Object> _dataList = new ArrayList<Object>();
	                _dataList.add(c.getLong(c.getColumnIndex("ProductID")));
		        	_dataList.add(c.getString(c.getColumnIndex("ProductName")));
		        	_dataList.add(c.getString(c.getColumnIndex("Note")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("Price_RT")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("Price_WS")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("Tax")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("Quantity")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("DiscountPercentage")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("ClientDiscountPercentage")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("UserDiscountPercentage")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("DiscountTotal")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("TaxTotal")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("Total")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("GrandTotal")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("DiscountGroupPercentage")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("DiscountGroupActionPercentage")));
		        	_dataList.add(c.getDouble(c.getColumnIndex("DiscountProductPercentage")));
		        	_dataArrays.add(_dataList);
	        	}	        	
		        c.close();	

		        dataList.add(_dataArrays);
		        dataList.add(cur.getLong(cur.getColumnIndex("_id")));
                dataArrays.add(dataList);		        	
	        }				
	        cur.close();
	        cur = null;
	        
	        
	        Iterator<Object> itr = dataArrays.iterator();
	        while (itr.hasNext()){
				@SuppressWarnings("unchecked")
				ArrayList<Object> el = (ArrayList<Object>) itr.next();
				
				JSONObject tempObject = new JSONObject();
				
				tempObject.put("OrderID", el.get(0));
				tempObject.put("AccountID", el.get(1));
				tempObject.put("ClientID", el.get(2));
				tempObject.put("UserID", el.get(3));
				tempObject.put("OrderDate", el.get(4));
				tempObject.put("DeliveryDate", el.get(5));
				tempObject.put("PaymentDate", el.get(6));
				tempObject.put("OrderReference", el.get(7));
				tempObject.put("Note", el.get(8));
				tempObject.put("DiscountPercentage", el.get(9));
				tempObject.put("DiscountTotal", el.get(10));
				tempObject.put("TaxTotal", el.get(11));
				tempObject.put("Total", el.get(12));
				tempObject.put("GrandTotal", el.get(13));
				tempObject.put("PaymentMethodID", el.get(14));
				tempObject.put("OrderStatusID", el.get(15));
				tempObject.put("DeliveryPlaceID", el.get(16));
				tempObject.put("Longitude", el.get(17));
				tempObject.put("Latitude", el.get(18));
				tempObject.put("Relations", el.get(19));
				tempObject.put("DOE", el.get(20));
                tempObject.put("VisitID", el.get(21));
     
				JSONArray tempArray = new JSONArray();
				@SuppressWarnings("unchecked")
				Iterator<Object> _itr = ((ArrayList<Object>) el.get(22)).iterator();
		        while (_itr.hasNext()){
					@SuppressWarnings("unchecked")
					ArrayList<Object> _el = (ArrayList<Object>) _itr.next();
					
		        	JSONObject tempItem = new JSONObject();
	        		tempItem.put("ProductID", _el.get(0));
	        		tempItem.put("ProductName", _el.get(1));
	        		tempItem.put("Note", _el.get(2));
    				tempItem.put("Price_RT", _el.get(3));
    				tempItem.put("Price_WS", _el.get(4));
    				tempItem.put("Tax", _el.get(5));
	        		tempItem.put("Quantity", _el.get(6));
	        		tempItem.put("DiscountPercentage", _el.get(7));
	        		tempItem.put("ClientDiscountPercentage", _el.get(8));
	        		tempItem.put("UserDiscountPercentage", _el.get(9));
	        		tempItem.put("DiscountTotal", _el.get(10));
	        		tempItem.put("TaxTotal", _el.get(11));
	        		tempItem.put("Total", _el.get(12));
	        		tempItem.put("GrandTotal", _el.get(13));
	        		tempItem.put("DiscountGroupPercentage", _el.get(14));
	        		tempItem.put("DiscountGroupActionPercentage", _el.get(15));
	        		tempItem.put("DiscountProductPercentage", _el.get(16));
	        		tempArray.put(tempItem);
		        }
		        tempObject.put("Items", tempArray.toString());
		        
				ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				postParameters.add(new BasicNameValuePair("tempObject", tempObject.toString()));
				String response = CustomHttpClient.executeHttpPost(wurthMB.getUser().URL + "POST_Orders", postParameters).toString();
				
				if (response == null || response.equals("")) return 0;
				
				JSONObject json_data = new JSONObject(response);
				
				try
				{ 
					if (json_data.getLong("ID") > 0){
						ContentValues cv = new ContentValues();
						db.beginTransaction();
						
						cv.put("OrderID", json_data.getLong("ID"));
						db.update(DATABASE_TABLE_ORDER_ITEMS, cv, "Order_id=?", new String[]{Long.toString((Long) el.get(23))});
						
						cv.put("Sync", 1);
						cv.put("DOE", json_data.getLong("DOE"));
						db.update(DATABASE_TABLE_ORDERS, cv, "_id=?", new String[]{Long.toString((Long) el.get(23))});
						
						db.setTransactionSuccessful();
						db.endTransaction();
                        ret++;
					}
				}
				catch (Exception e) {
					wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
				}
	        }
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, e.getMessage(), e);
		}
		
		if (cur != null && !cur.isClosed()) cur.close();
		
		return ret;
		
	}		
}
