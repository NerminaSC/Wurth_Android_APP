package ba.wurth.mb.Classes.Objects;

import java.util.Date;


public class Task {
    public long _id = 0;
    public long TaskID = 0;
    public long AccountID = 0;
    public long StatusID = 0;
    public String Name = "";
    public String Description = "";
    public String Parameters = "";
    public String ParametersLog = "";
    public String Note = "";
    public Integer Active = 1;
    public long DOE = new Date().getTime();

    public Task() {

    }
}
