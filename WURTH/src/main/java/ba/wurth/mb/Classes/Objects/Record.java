package ba.wurth.mb.Classes.Objects;

public class Record {
	public Long _id = 0L;
	public Long ID;
	public Long ItemID;
	public Long TravelOrderID;
	public Long ProjectID;
	public Integer OptionID;
	public boolean AdditionalTextRequired;
	public String MandatoryOptions = "";
	public Integer Duration;
	public Integer Reminder;
	public Integer Snooze;
	public boolean Billable;
	public boolean Prolific;
	public Long AccountID;
	public Long UserID;
	public String UserName;
	public Long startLatitude;
	public Long startLongitude;
	public Long endLatitude;
	public Long endLongitude;
	public String IP = "";
	public Integer MediaID;
	public Long GroupID;
	public Long ResourceID;
	public Long ResourceItemID;
	public Long startTime = System.currentTimeMillis();
	public Long endTime = System.currentTimeMillis();
	public String Reference = "";
	public String Description = "";
	
	public Long ClientID;
	public Long DeliveryPlaceID;
	public Integer Version;
	public Integer Locked;
	public Integer Deviation;
	public String Licence = "";
	public String GroupName = "";
	public String ItemName;
	public Long DOE = 0l;
	public boolean Active = true;
	public boolean Sync = false;
}
