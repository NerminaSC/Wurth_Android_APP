package ba.wurth.mb.Classes.Objects;

import java.util.ArrayList;
import java.util.Date;

public class Visit {
	public long _id = 0;
	public long VisitID = 0;
	public long ClientID = 0;
	public long UserID = 0;
	public long DeliveryPlaceID = 0;
	public long dt = new Date().getTime();
    public long startDT = new Date().getTime();
    public long endDT = new Date().getTime();
	public String Note = "";
	public Long Latitude = 0L;
	public Long Longitude = 0L;
	public Integer Sync = 0;
    public ArrayList<Document> documents = new ArrayList<Document>();

	public Visit() {

	}
}
