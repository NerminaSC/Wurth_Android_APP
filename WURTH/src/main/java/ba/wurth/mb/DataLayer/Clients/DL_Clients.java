package ba.wurth.mb.DataLayer.Clients;

import android.content.ContentValues;
import android.database.Cursor;

import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.PaymentDate;
import ba.wurth.mb.Classes.Objects.PaymentMethod;
import ba.wurth.mb.Classes.wurthMB;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class DL_Clients {
	//private Context ctx;
	private static 	String methodName = "";
	private static String className = "DL_Clients";

	private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
	private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

	public static Cursor Get(String searchWord) {

        String sql = "";

        switch (wurthMB.getUser().AccessLevelID) {
            case 1: //Director
                sql = "";
                break;
            case 2: //Manager
                sql = " AND (PARTNER.Region LIKE '" + wurthMB.getUser().Region.substring(0,2) + "%' OR PARTNER.KomercijalistaID = " + wurthMB.getUser()._userid + ") ";
                break;
            case 5: //Sales Persons
            case 9: //Sales user (WEB)
                sql = " AND Partner.KomercijalistaID = " + wurthMB.getUser()._userid + " ";
                break;
            case 7: //Key account manager
                sql = " AND (PARTNER_DETALJI.KAM = " + wurthMB.getUser()._userid + " OR PARTNER.KomercijalistaID = " + wurthMB.getUser()._userid + ") ";
                break;
            case 8: //Area sales manager
                sql = " AND (PARTNER.Region LIKE '" + wurthMB.getUser().Region.substring(0,4) + "%' OR PARTNER.KomercijalistaID = " + wurthMB.getUser()._userid + ")  ";
                break;
            default:break;
        }

		Cursor cur = db_readonly.rawQuery("SELECT Partner.ID  AS _id, Clients.ClientID AS ClientID, Clients._clientid AS _clientid, 0 AS DeliveryPlaceID, 0 AS _deliveryplaceid, PARTNER.Naziv AS Name, PARTNER.Adresa AS Address, Partner.Kod AS Code, PARTNER.IDBroj " +

                "    FROM PARTNER" +

                "    LEFT JOIN KOMERCIJALISTI ON PARTNER.KomercijalistaID = KOMERCIJALISTI.ID " +
                "    INNER JOIN Clients ON PARTNER.ID = Clients._clientid " +
                "    LEFT JOIN PARTNER_DETALJI ON PARTNER.ID = PARTNER_DETALJI.CustomerID " +
				"    LEFT JOIN KOMERCIJALISTI KAM ON PARTNER_DETALJI.KAM = KAM.ID " +

                "    WHERE (PARTNER.Naziv LIKE '%" + searchWord.trim() + "%' OR PARTNER.Kod LIKE '%" + searchWord.trim() + "%') " +
				"    AND PARTNER.ParentID IS NOT NULL " +
				"    AND PARTNER.ParentID > 0 " +
				"    AND PARTNER.Naziv <> '' " +
				"    AND Clients.Active = 1  " +
                "    AND Clients.AccountID = " + wurthMB.getUser().AccountID + " " +

                sql +

                " UNION " +

                " SELECT Partner.ID  AS _id, Clients.ClientID AS ClientID, Clients._clientid AS _clientid, DeliveryPlaces.DeliveryPlaceID AS DeliveryPlaceID, DeliveryPlaces._deliveryplaceid AS _deliveryplaceid, PARTNER.Naziv AS Name, PARTNER.Adresa AS Address, Partner.Kod AS Code, PARTNER.IDBroj " +

                "    FROM PARTNER " +

                "    LEFT JOIN KOMERCIJALISTI ON PARTNER.KomercijalistaID = KOMERCIJALISTI.ID " +
                "    INNER JOIN DeliveryPlaces ON PARTNER.ID = DeliveryPlaces._deliveryplaceid " +
                "    INNER JOIN Clients ON DeliveryPlaces.ClientID = Clients.ClientID " +
                "    LEFT JOIN PARTNER_DETALJI ON PARTNER.ID = PARTNER_DETALJI.CustomerID " +
				"    LEFT JOIN KOMERCIJALISTI KAM ON PARTNER_DETALJI.KAM = KAM.ID " +

                "    WHERE (PARTNER.Naziv LIKE '%" + searchWord.trim() + "%' OR PARTNER.Kod LIKE '%" + searchWord.trim() + "%') " +
				"    AND PARTNER.ParentID IS NOT NULL " +
                "    AND PARTNER.ParentID > 0 " +
				"    AND PARTNER.Naziv <> '' " +
				"    AND DeliveryPlaces.Active = 1 " +
                "    AND DeliveryPlaces.AccountID = " + wurthMB.getUser().AccountID + " " +
                "    AND Clients.AccountID = " + wurthMB.getUser().AccountID + " " +

                sql +

                "    Order By PARTNER.Naziv", null);

        cur.getCount();
        return cur;
	}

    public static Cursor Get_All(String searchWord) {

        String sql = "";

        Cursor cur = db_readonly.rawQuery("SELECT Partner.ID  AS _id, Clients.ClientID AS ClientID, Clients._clientid AS _clientid, 0 AS DeliveryPlaceID, 0 AS _deliveryplaceid, PARTNER.Naziv AS Name, PARTNER.Adresa AS Address, Partner.Kod AS Code, PARTNER.IDBroj " +

                "    FROM PARTNER" +

                "    INNER JOIN Clients ON PARTNER.ID = Clients._clientid " +

                "    WHERE (PARTNER.Naziv LIKE '%" + searchWord.trim() + "%' OR PARTNER.Kod LIKE '%" + searchWord.trim() + "%') " +
                "    AND Clients.Active = 1  " +
                "    AND Clients.AccountID = " + wurthMB.getUser().AccountID + " " +

                sql +

                " UNION " +

                " SELECT Partner.ID  AS _id, Clients.ClientID AS ClientID, Clients._clientid AS _clientid, DeliveryPlaces.DeliveryPlaceID AS DeliveryPlaceID, DeliveryPlaces._deliveryplaceid AS _deliveryplaceid, PARTNER.Naziv AS Name, PARTNER.Adresa AS Address, Partner.Kod AS Code, PARTNER.IDBroj " +

                "    FROM PARTNER " +

                "    INNER JOIN DeliveryPlaces ON PARTNER.ID = DeliveryPlaces._deliveryplaceid " +
                "    INNER JOIN Clients ON DeliveryPlaces.ClientID = Clients.ClientID " +

                "    WHERE (PARTNER.Naziv LIKE '%" + searchWord.trim() + "%' OR PARTNER.Kod LIKE '%" + searchWord.trim() + "%') " +
                "    AND DeliveryPlaces.Active = 1 " +
                "    AND DeliveryPlaces.AccountID = " + wurthMB.getUser().AccountID + " " +
                "    AND Clients.AccountID = " + wurthMB.getUser().AccountID + " " +

                sql +

                "    Order By PARTNER.Naziv", null);

        cur.getCount();
        return cur;
    }

	public static Client GetByID(long id) {
		
		methodName = "GetByID";
		
		try {
			
			final Cursor cur;
            cur = db_readonly.rawQuery("select Clients._id, Clients.ClientID, Clients._clientid, Clients.AccountID, PARTNER.* "
                    + " FROM Clients "
                    + " INNER JOIN PARTNER ON Clients._clientid = PARTNER.ID "
                    + " WHERE Clients._id = " + id, null);

	        Client tempClient = null;
	        
	        if (cur.moveToFirst()) {
		         tempClient = new Client() {{
		        	_id = cur.getLong(cur.getColumnIndex("_id"));
		        	ClientID = cur.getLong(cur.getColumnIndex("ClientID"));
		        	AccountID = cur.getInt(cur.getColumnIndex("AccountID"));
		        	Name = cur.getString(cur.getColumnIndex("Naziv"));
		        	Address = cur.getString(cur.getColumnIndex("Adresa"));
		        	Telephone = cur.getString(cur.getColumnIndex("Telefon"));
		        	Mobile = cur.getString(cur.getColumnIndex("Mobitel"));
		        	Fax = cur.getString(cur.getColumnIndex("Faks"));
		        	City = cur.getString(cur.getColumnIndex("Grad"));
		        	Description = cur.getString(cur.getColumnIndex("Opis"));
		        	//Longitude = cur.getLong(cur.getColumnIndex("Longitude"));
		        	//Latitude = cur.getLong(cur.getColumnIndex("Latitude"));
		        	//Sync = cur.getInt(cur.getColumnIndex("Latitude"));
		        	WATNumber = cur.getString(cur.getColumnIndex("PorezniBroj"));
		        	WATType = cur.getInt(cur.getColumnIndex("VrstaObveznika"));
		        	//WebSite = cur.getString(cur.getColumnIndex("Website"));
		        	EmailAddress = cur.getString(cur.getColumnIndex("EmailAdresa"));
		        	IDNumber = cur.getString(cur.getColumnIndex("IDBroj"));
		        	PDVNumber  = cur.getString(cur.getColumnIndex("PDVBroj"));
		        	Owner = cur.getString(cur.getColumnIndex("Vlasnik"));
		        	code = cur.getString(cur.getColumnIndex("Kod"));
		        	//DiscountPercentage  = cur.getDouble(cur.getColumnIndex("DiscountPercentage"));
		        	//DeliveryDelay  = cur.getInt(cur.getColumnIndex("DeliveryDelay"));
		        	//PaymentDelay  = cur.getInt(cur.getColumnIndex("PaymentDelay"));
                     Potencijal = cur.getInt(cur.getColumnIndex("Potencijal"));
                     Veleprodaja = cur.getInt(cur.getColumnIndex("Veleprodaja"));
                     BrzaIsporuka = cur.getInt(cur.getColumnIndex("BrzaIsporuka"));
		        }};
		        
		        final Cursor c = db_readonly.rawQuery("Select * From ClientPaymentMethods Where ClientID = " + cur.getLong(cur.getColumnIndex("ClientID")), null);
		        while (c.moveToNext()) {
		        	final Cursor _c = db_readonly.rawQuery("Select * From PaymentMethods Where PaymentMethodID = " + c.getInt(c.getColumnIndex("PaymentMethodID")), null);
		        	if (_c.moveToFirst()) { 
			        	PaymentMethod item = new PaymentMethod(){{
			        		PaymentMethodID = c.getInt(c.getColumnIndex("PaymentMethodID"));
			        		Name = _c.getString(_c.getColumnIndex("Name"));
			        	}};
			        	tempClient.PaymentMethods.add(item);
			        	_c.close();
		        	}
	        	}	        	
		        c.close();	

		        
		        final Cursor curClientPaymentDates = db_readonly.rawQuery("Select * From ClientPaymentDates Where ClientID = " + cur.getLong(cur.getColumnIndex("ClientID")), null);
		        while (curClientPaymentDates.moveToNext()) {
			        final Cursor curPaymentDates = db_readonly.rawQuery("Select * From PaymentDates Where PaymentDateID = " + curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("PaymentDateID")), null);
			        if (curPaymentDates.moveToFirst()) {
			        	PaymentDate item = new PaymentDate(){{
			        		PaymentDateID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("PaymentDateID"));
			        		ProductCategoryID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("ProductCategoryID"));
			        		ProductID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("ProductID"));
			        		Name = curPaymentDates.getString(curPaymentDates.getColumnIndex("Name"));
			        		Description = curPaymentDates.getString(curPaymentDates.getColumnIndex("Description"));
			        		Delay = curPaymentDates.getInt(curPaymentDates.getColumnIndex("Delay"));
			        	}};
			        	tempClient.PaymentDates.add(item);
			        	curPaymentDates.close();
			        }
		        }
		        curClientPaymentDates.close();
		        
		        
	        }
	        cur.close();
	        return tempClient;			
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, "", e);
			return null;
		}
		finally {

		}
	}
	
	public static Client GetByClientID(long ClientID) {
		
		methodName = "GetByClientID";
		
		try {
			
			final Cursor cur;
            cur = db_readonly.rawQuery("select Clients._id, Clients.ClientID, Clients._clientid, Clients.AccountID, Clients.Latitude, Clients.Longitude, PARTNER.* "
                    + " FROM Clients "
                    + " INNER JOIN PARTNER ON Clients._clientid = PARTNER.ID "
                    + " WHERE Clients.ClientID = " + ClientID, null);

	        Client tempClient = null;
	        
	        if (cur.moveToFirst()) {
                tempClient = new Client() {{
                    _id = cur.getLong(cur.getColumnIndex("_id"));
                    ClientID = cur.getLong(cur.getColumnIndex("ClientID"));
                    AccountID = cur.getInt(cur.getColumnIndex("AccountID"));
                    Name = cur.getString(cur.getColumnIndex("Naziv"));
                    Address = cur.getString(cur.getColumnIndex("Adresa"));
                    Telephone = cur.getString(cur.getColumnIndex("Telefon"));
                    Mobile = cur.getString(cur.getColumnIndex("Mobitel"));
                    Fax = cur.getString(cur.getColumnIndex("Fax"));
                    City = cur.getString(cur.getColumnIndex("Grad"));
                    Description = cur.getString(cur.getColumnIndex("Opis"));
                    Longitude = cur.getLong(cur.getColumnIndex("Longitude"));
                    Latitude = cur.getLong(cur.getColumnIndex("Latitude"));
                    //Sync = cur.getInt(cur.getColumnIndex("Latitude"));
                    WATNumber = cur.getString(cur.getColumnIndex("PorezniBroj"));
                    WATType = cur.getInt(cur.getColumnIndex("VrstaObveznika"));
                    //WebSite = cur.getString(cur.getColumnIndex("Website"));
                    EmailAddress = cur.getString(cur.getColumnIndex("EmailAdresa"));
                    IDNumber = cur.getString(cur.getColumnIndex("IDBroj"));
                    PDVNumber  = cur.getString(cur.getColumnIndex("PDVBroj"));
                    Owner = cur.getString(cur.getColumnIndex("Vlasnik"));
                    code = cur.getString(cur.getColumnIndex("Kod"));
                    Potencijal = cur.getInt(cur.getColumnIndex("Potencijal"));
                    Veleprodaja = cur.getInt(cur.getColumnIndex("Veleprodaja"));
                    BrzaIsporuka = cur.getInt(cur.getColumnIndex("BrzaIsporuka"));
                    //DiscountPercentage  = cur.getDouble(cur.getColumnIndex("DiscountPercentage"));
                    //DeliveryDelay  = cur.getInt(cur.getColumnIndex("DeliveryDelay"));
                    //PaymentDelay  = cur.getInt(cur.getColumnIndex("PaymentDelay"));
                }};

                final Cursor c = db_readonly.rawQuery("Select * From ClientPaymentMethods Where ClientID = " + cur.getLong(cur.getColumnIndex("ClientID")), null);
		        while (c.moveToNext()) {
		        	final Cursor _c = db_readonly.rawQuery("Select * From PaymentMethods Where PaymentMethodID = " + c.getInt(c.getColumnIndex("PaymentMethodID")), null);
		        	if (_c.moveToFirst()) { 
			        	PaymentMethod item = new PaymentMethod(){{
			        		PaymentMethodID = c.getInt(c.getColumnIndex("PaymentMethodID"));
			        		Name = _c.getString(_c.getColumnIndex("Name"));
			        	}};
			        	tempClient.PaymentMethods.add(item);
			        	_c.close();
		        	}
	        	}	        	
		        c.close();	

		        
		        final Cursor curClientPaymentDates = db_readonly.rawQuery("Select * From ClientPaymentDates Where ClientID = " + cur.getLong(cur.getColumnIndex("ClientID")), null);
		        while (curClientPaymentDates.moveToNext()) {
			        final Cursor curPaymentDates = db_readonly.rawQuery("Select * From PaymentDates Where PaymentDateID = " + curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("PaymentDateID")), null);
			        if (curPaymentDates.moveToFirst()) {
			        	PaymentDate item = new PaymentDate(){{
			        		PaymentDateID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("PaymentDateID"));
			        		ProductCategoryID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("ProductCategoryID"));
			        		ProductID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("ProductID"));
			        		Name = curPaymentDates.getString(curPaymentDates.getColumnIndex("Name"));
			        		Description = curPaymentDates.getString(curPaymentDates.getColumnIndex("Description"));
			        		Delay = curPaymentDates.getInt(curPaymentDates.getColumnIndex("Delay"));
			        	}};
			        	tempClient.PaymentDates.add(item);
			        	curPaymentDates.close();
			        }
		        }
		        curClientPaymentDates.close();
	        }
	        cur.close();
	        return tempClient;			
		}
		catch (Exception e) {
			wurthMB.AddError(className + " " + methodName, "", e);
			return null;
		}
		finally {

		}
	}
	
	public static int AddOrUpdate(Client tempClient) {

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
	
	public static int Delete(long _id){
		try
		{
			db.delete("Clients", "_id=?", new String[] { Long.toString(_id) });
			return 1;
		}
		catch (Exception e) {
			return -1;
		}
	}

    public static Long Get_ClientID(long _id) {
        try {
            Cursor cur = db_readonly.rawQuery("Select ClientID From Clients Where _id = " + _id, null);
            cur.moveToFirst();
            return cur.getLong(0);
        }
        catch (Exception e) {
        }
        return 0L;
    }
	
	public static Cursor Get_DeliveryPlacesByClientLongID(long _ClientID, String searhText) {
        Cursor cur = null;
        try {
            cur = db_readonly.rawQuery("select DeliveryPlaces._id, DeliveryPlaces.DeliveryPlaceID, Partner.Naziv AS Name, Partner.Adresa AS Address, Partner.Kod AS Code "
                    + " FROM DeliveryPlaces"
                    + " INNER JOIN Clients ON  DeliveryPlaces.ClientID = Clients.ClientID "
                    + " INNER JOIN Partner ON  Clients._clientid = Partner.ID "
                    + " WHERE (Partner.Naziv like '%" + searhText + "%' OR Partner.Kod like '" + searhText + "%')"
                    + " And DeliveryPlaces.Active = 1 "
                    + " And Clients._id = " + _ClientID
                    + " And Clients.AccountID = " + wurthMB.getUser().AccountID
                    + " And Clients.UserID = " + wurthMB.getUser().UserID
                    + " Order By Partner.Naziv", null);
        }
        catch (Exception e) {

        }
        return cur;
    }

	public static Long Get_DeliveryPlaceID(long _id) {
        try {
            Cursor cur = db_readonly.rawQuery("Select DeliveryPlaceID From DeliveryPlaces Where _id = " + _id, null);
            cur.moveToFirst();
            return cur.getLong(0);
        }
        catch (Exception e) {
        }
        return 0L;
    }

	public static Cursor Get_DeliveryPlaceProperties(long DeliveryPlaceID) {
		try {
			Cursor cur = db_readonly.rawQuery("Select ObjectID, DOE,  (SELECT DeliveryPlacesProperties.Name FROM DeliveryPlacesProperties WHERE DeliveryPlacesProperties.DPPID = ClientDeliveryPlacesProperties.DPPID LIMIT 1) AS Property, (SELECT DeliveryPlacesPropertiesOptions.Name FROM DeliveryPlacesPropertiesOptions WHERE DeliveryPlacesPropertiesOptions.DPPOID = ClientDeliveryPlacesProperties.DPPOID LIMIT 1) AS Value From ClientDeliveryPlacesProperties Where ObjectID = " + DeliveryPlaceID + " AND AccountID = " + wurthMB.getUser().AccountID + " ORDER BY DOE DESC", null);
			return cur;
		}
		catch (Exception e) {
			return null;
		}
	}

	public static Cursor Get_DeliveryPlaceByDeliveryPlaceID(long DeliveryPlaceID) {
		try {
			Cursor cur = db_readonly.rawQuery("Select DeliveryPlaces._id, DeliveryPlaces.ClientID, DeliveryPlaces.DeliveryPlaceID, DeliveryPlaces.Latitude, DeliveryPlaces.Longitude, PARTNER.* From DeliveryPlaces INNER JOIN PARTNER ON DeliveryPlaces._deliveryplaceID = PARTNER.ID Where DeliveryPlaceID = " + DeliveryPlaceID, null);
			cur.moveToFirst();
			return cur;
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static int UpdateDeliveryPlaceLocation(long DeliveryPlaceID, long Latitude, long Longitude) {
		try {

			ContentValues cv = new ContentValues();

			cv.put("Latitude", Latitude);
			cv.put("Longitude", Longitude);
			cv.put("Sync", 0);

			db.update("DeliveryPlaces", cv, "DeliveryPlaceID = " + DeliveryPlaceID, null);
			return 1;
		}
		catch (Exception e) {
			return -1;
		}
		finally {

		}
	}

    public static int UpdateClientLocation(long ClientID, long Latitude, long Longitude) {
        try {

            ContentValues cv = new ContentValues();

            cv.put("Latitude", Latitude);
            cv.put("Longitude", Longitude);
            cv.put("Sync", 0);

            db.update("Clients", cv, "ClientID = " + ClientID, null);
            return 1;
        }
        catch (Exception e) {
            return -1;
        }
        finally {

        }
    }

    public static Cursor Get_ClientClosest(Long Latitude, Long Longitude) {
        Cursor cur = null;
        try {
            cur = db_readonly.rawQuery("select Clients._id, Clients.ClientID, Clients._clientid, Clients.AccountID, Clients.Latitude, Clients.Longitude, PARTNER.* "
                    + " FROM Clients "
                    + " INNER JOIN PARTNER ON Clients._clientid = PARTNER.ID "
                    + " WHERE Clients.Active = 1 "
                    + " AND Clients.Latitude > 0 AND Clients.Longitude > 0 "
                    + " And Clients.AccountID = " + wurthMB.getUser().AccountID
                    + " And Clients.UserID = " + wurthMB.getUser().UserID
                    + " ORDER BY ABS(Clients.Latitude - ?) + ABS(Clients.Longitude - ?) LIMIT 1", new String[] {Long.toString(Latitude), Long.toString(Longitude)});
        }
        catch (Exception e) {

        }
        return cur;
    }

}
