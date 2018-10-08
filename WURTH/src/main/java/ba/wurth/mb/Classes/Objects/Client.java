package ba.wurth.mb.Classes.Objects;

import java.util.ArrayList;
import java.util.Date;

public class Client {
	public long _id = 0;
	public long ClientID = 0;
	public long AccountID = 0;
	public long UserID = 0;
    public long CategoryID = 0;
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
	public String Owner = "";
	public String code = "";
	public Double DiscountPercentage = 0D;
	public int PaymentDelay = 0;
	public int DeliveryDelay = 0;
	public Long Latitude = 0L;
	public Long Longitude = 0L;
    public Double MaxOrderValue = 0.0;
    public Double MaxOrderCount = 0.0;
	public Integer Sync = 0;
	public ArrayList<PaymentMethod> PaymentMethods = new ArrayList<PaymentMethod>();
	public ArrayList<PaymentDate> PaymentDates = new ArrayList<PaymentDate>();
	public int IsDeleted = 0;
	public int Active = 1;
	public long DOE = new Date().getTime(); 
	public ArrayList<DeliveryPlace> DeliveryPlaces = new ArrayList<DeliveryPlace>();


    public Integer Potencijal = 0;
    public Integer Bransa = 0;
    public Integer BrzaIsporuka = 0;
    public Integer Veleprodaja = 0;
    public Long _clientid = 0L;
    public Long _parentid = 0L;
    public ArrayList<String> Branse = new ArrayList<String>();
	public String KanalDistribucije = "";

	public Client() {

	}
}
