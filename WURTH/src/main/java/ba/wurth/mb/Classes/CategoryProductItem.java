package ba.wurth.mb.Classes;

public class CategoryProductItem {
    private Long ProductID;
    private Long ArtikalID;
    private String Name;
    private String Url;
    private String Code;
    private String Additional;


    public CategoryProductItem(Long ProductID, Long ArtikalID, String Name, String Code, String Url, String Additional) {
        super();
        this.ProductID = ProductID;
        this.ArtikalID = ArtikalID;
        this.Name = Name;
        this.Url = Url;
        this.Code = Code;
        this.Additional = Additional;
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
    public String getAdditional() {
        return Additional;
    }
    public Long getProductID() {
        return ProductID;
    }
    public Long getArtikalID() {
        return ArtikalID;
    }
}