package ba.wurth.mb.Classes.Objects;

import java.util.Date;

public class Document {
	public long _id = 0;
	public long DocumentID = 0;
	public long UserID = 0;
	public long dt = new Date().getTime();
	public Long Latitude = 0L;
	public Long Longitude = 0L;
	public byte[] data;
    public int DocumentType = 0;
    public int Type = 0;
    public int OptionID = 0;
    public long ItemID = 0;
    public String Name = "";
    public String Description = "";
    public String fileName = "";
    public int fileSize = 0;
    public String fileContentType = "";
    public String url = "";
    public Integer Active = 1;
    public Integer Sync = 0;

}
