package ba.wurth.mb.Classes.Objects;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class User {

    public long UserID = 0;
    public long _userid = 0;
    public long AccountID = 0;
    public int AccessLevelID = -1;

    public long ApplicationUserID = 0;
    public long ApplicationAccountID = 0;

    public String AccountName = "Client";
    public String Firstname = "";
    public String Lastname = "";
    public String EmailAddress = "";
    public String URL = "";
    public String URL_Wurth = "http://ws.wurth.mb.optimus.ba/wurth.asmx/";

    public String URL_EMS = "http://ws.optimus.ba/ems/Android.asmx/";

    public Locale locale = null;

    public ArrayList<ApplicationAssociation> Associations = new ArrayList<ApplicationAssociation>();
    public ArrayList<WorkingHour> WorkingHours = new ArrayList<WorkingHour>();
    public ArrayList<Long> UserGroups = new ArrayList<Long>();

	public int hasDeliveryPlaces = 1;
	public double DiscountPercentage = 0;
	public int PaymentDelay = 0;
	public int DeliveryDelay = 0;

    public String Region = "";
    public JSONObject Parameters = new JSONObject();

    public long ApplicationUserID_EMS = 0;
    public long ApplicationAccountID_EMS = 0;

    public long ApplicationUserID_OPTIMUS = 0;
    public long ApplicationAccountID_OPTIMUS = 270;

}
