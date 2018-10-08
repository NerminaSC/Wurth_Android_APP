package ba.wurth.mb.Classes.Objects;

import java.util.ArrayList;
import java.util.Date;

public class DeliveryPlace {
	public long _id = 0;
    public long _deliveryplaceid = 0;
    public long DeliveryPlaceID = 0;
	public long ClientID = 0;
	public long AccountID = 0;
	public long UserID = 0;
	public String Name = "";
	public String Description = "";
	public String Telephone = "";
	public String Fax = "";
	public String Mobile = "";
	public String EmailAddress = "";
	public String WebSite = "";
	public String Address = "";
	public String City = "";
	public Integer CountryID = 0;
	public Double CreditLimit = 0.0;
	public Double Revenue = 0.0;
	public Double CurrentLimit = 0.0;
	public Integer CheckCreditLimit = 0;
	public String WATNumber = "";
	public String IDNumber = "";
	public String PDVNumber = "";
	public int WATType = 0;
	public String ResponsiblePerson = "";
	public String code = "";
	public Double DiscountPercentage = 0D;
	public int PaymentDelay = 0;
	public int DeliveryDelay = 0;
	public Long Latitude = 0L;
	public Long Longitude = 0L;
	public Integer Sync = 0;
	public ArrayList<PaymentMethod> PaymentMethods = new ArrayList<PaymentMethod>();
	public ArrayList<PaymentDate> PaymentDates = new ArrayList<PaymentDate>();
	public int IsDeleted = 0;
	public int Active = 1;
	public int Priority = 1;
	public long DOE = new Date().getTime(); 
}
