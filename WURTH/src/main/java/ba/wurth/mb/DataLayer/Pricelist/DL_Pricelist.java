package ba.wurth.mb.DataLayer.Pricelist;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Date;

import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Pricelist;
import ba.wurth.mb.Classes.wurthMB;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class DL_Pricelist {
	private static 	String methodName = "";
	private static String className = "DL_Pricelist";

	private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
	private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

	// Database fields
	private static final String DATABASE_TABLE = "PriceList";

	public static Pricelist GetPriceList(Long ClientID, Long ProductID) {

		Pricelist priceList;
		priceList = new Pricelist();
		int DiscountGroupID = 0;
		
		Cursor cur = db_readonly.rawQuery("Select DiscountGroupID From DiscountGroupClients Where ClientID = " + ClientID, null);
		if(cur.moveToFirst()) DiscountGroupID = cur.getInt(0);
		cur.close();

		if (DiscountGroupID > 0){
			Cursor curProduct = db_readonly.rawQuery("Select * From DiscountGroupProducts Where DiscountGroupID = " + DiscountGroupID + " And ProductID = " + ProductID, null);
	        if(curProduct.moveToFirst()){
	    		priceList.DiscountGroupPercentage= curProduct.getDouble(curProduct.getColumnIndex("Percentage"));
	    		priceList.DiscountGroupPaymentDelay = curProduct.getInt(curProduct.getColumnIndex("PaymentDelay"));
	    		priceList.DiscountGroupDeliveryDelay = curProduct.getInt(curProduct.getColumnIndex("DeliveryDelay"));
	        }
	        curProduct.close();
		}
        

		int DiscountGroupActionID = 0;
		Date dt = new Date();
		Cursor cur_Action = db_readonly.rawQuery("Select DiscountGroupActions.DiscountGroupActionID From DiscountGroupActionClients INNER JOIN DiscountGroupActions ON DiscountGroupActionClients.DiscountGroupActionID = DiscountGroupActions.DiscountGroupActionID  Where DiscountGroupActionClients.ClientID = " + ClientID + " AND DiscountGroupActions.startDate < " + dt.getTime() + " AND (endDate = 0 OR endDate >= " + dt.getTime() + ")", null);
		if(cur_Action.moveToFirst()) DiscountGroupActionID = cur_Action.getInt(0);
		cur_Action.close();

		if (DiscountGroupActionID > 0){
			Cursor curProduct = db_readonly.rawQuery("Select * From DiscountGroupActionProducts Where DiscountGroupActionID = " + DiscountGroupActionID + " And ProductID = " + ProductID, null);
	        if(curProduct.moveToFirst()){
	    		priceList.DiscountGroupActionPercentage= curProduct.getDouble(curProduct.getColumnIndex("Percentage"));
	    		priceList.DiscountGroupActionPaymentDelay = curProduct.getInt(curProduct.getColumnIndex("PaymentDelay"));
	    		priceList.DiscountGroupActionDeliveryDelay = curProduct.getInt(curProduct.getColumnIndex("DeliveryDelay"));
	        }
	        curProduct.close();
		}

		Cursor curProduct = db_readonly.rawQuery("Select * From DiscountClientProducts Where ClientID = " + ClientID + " And ProductID = " + ProductID, null);
        if(curProduct.moveToFirst()){
    		priceList.ClientDiscount1= curProduct.getDouble(curProduct.getColumnIndex("Discount1"));
    		priceList.ClientDiscount2= curProduct.getDouble(curProduct.getColumnIndex("Discount2"));
    		priceList.ClientDiscount3= curProduct.getDouble(curProduct.getColumnIndex("Discount3"));
    		priceList.ClientDiscount4= curProduct.getDouble(curProduct.getColumnIndex("Discount4"));
    		priceList.ClientDiscount5= curProduct.getDouble(curProduct.getColumnIndex("Discount5"));
        }
        curProduct.close();

		return priceList;		        
	}
	
	public int AddOrUpdate(Client tempClient) {

		try {

			ContentValues cv = new ContentValues();

			cv.put("ClientID", tempClient.ClientID);
			cv.put("AccountID", wurthMB.getUser().AccountID);
			cv.put("Name", tempClient.Name);
			cv.put("Description", tempClient.Description);
			cv.put("Telephone", tempClient.Telephone);
			cv.put("Fax", tempClient.Fax);
			cv.put("Mobile", tempClient.Mobile);
			cv.put("EmailAddress", tempClient.EmailAddress);
			cv.put("WebSite", tempClient.WebSite);
			cv.put("Address", tempClient.Address);
			cv.put("City", tempClient.City);
			cv.put("CountryID", tempClient.CountryID);
			cv.put("CreditLimit", tempClient.CreditLimit);
			cv.put("Revenue", tempClient.Revenue);
			cv.put("CurrentLimit", tempClient.CurrentLimit);
			cv.put("CheckCreditLimit", tempClient.CheckCreditLimit);
			cv.put("Latitude", tempClient.Latitude);
			cv.put("Longitude", tempClient.Longitude);
			cv.put("Sync", 0);

			if (tempClient._id == 0) db.insert("Clients", null, cv);
			else db.update("Clients", cv, "_id=" + tempClient._id, null);
			return 1;
		}
		catch (Exception e) {
			return -1;
		}
		finally {

		}
	}
	
	public int Delete(Integer _id){
		try
		{
			db.delete("Clients", "_id=?", new String[] { Integer.toString(_id) });
			return 1;
		}
		catch (Exception e) {
			return -1;
		}
	}
}
