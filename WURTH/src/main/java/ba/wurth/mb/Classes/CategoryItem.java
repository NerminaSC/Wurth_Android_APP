package ba.wurth.mb.Classes;

public class CategoryItem {
    private Long CategoryID;
    private Long ParentID;
    private String Name;
    private String Url;
    private String Code;


    public CategoryItem(Long CategoryID, Long ParentID, String Name, String Code, String Url) {
        super();
        this.CategoryID = CategoryID;
        this.ParentID = ParentID;
        this.Name = Name;
        this.Url = Url;
        this.Code = Code;
    }


    public String getName() {
        return Name;
    }
    public String getUrl() {
        return Url;
    }
    public String getCode() {
        return Code;
    }
    public Long getCategoryID() {
        return CategoryID;
    }
    public Long getParentID() {
        return ParentID;
    }


    public void setName(String Name) {
        this.Name = Name;
    }
}