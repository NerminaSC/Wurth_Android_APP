package ba.wurth.mb.Classes.Objects;

import java.util.Date;

public class ErrorObject {
	public long UserID = 0;
	public long ClientID = 0;
	public long AccountID = 0;
	public String Title = "";
	public String Message = "";
	public Date d = new Date();
	public Exception ex;
	
	public ErrorObject(){

	}
}
