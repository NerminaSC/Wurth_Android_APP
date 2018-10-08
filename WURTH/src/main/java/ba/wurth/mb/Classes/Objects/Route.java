package ba.wurth.mb.Classes.Objects;

import java.util.Date;


public class Route {
	public long _id = 0;
	public long RouteID = 0;
	public long AccountID = 0;
	public long UserID = 0;
	public String Name = "";
	public String Description = "";
	public String code = "";
    public String raw = "{}";
	public Integer Active = 1;
	public long DOE = new Date().getTime();
	public Integer Sync = 0;

	public Route() {

	}
}
