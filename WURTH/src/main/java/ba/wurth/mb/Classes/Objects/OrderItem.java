package ba.wurth.mb.Classes.Objects;

public class OrderItem {
	public long ProductID = 0;
    public String ProductName = "";
	public double Quantity = 0;
	public double Price_RT = 0;
	public double Price_WS = 0;
	public double Tax = 0;
	public double DiscountPercentage = 0;
	public double ClientDiscountPercentage = 0;
	public double UserDiscountPercentage = 0;
	public String Note = "";
	public double Total = 0;
	public double TaxTotal = 0;
	public double DiscountTotal = 0;
	public double GrandTotal = 0;
	public double DiscountGroupPercentage = 0;
	public double DiscountGroupActionPercentage = 0;
	public double DiscountProductPercentage = 0;

    public String Kod_Zbirne_Cjen_Razrade = "";
    public long ArtikalID = 0;
    public long Grupa_Artikla = 0;
    public double Pakovanje = 0D;
    public String Pakovanje_Barcode = "";
    public String Pakovanje_KodPakovanja = "";
    public String Mjerna_Jedinica = "";

    public double Stanje_Zaliha = 0D;
    public double Narucena_Kolicina = 0D;
    public long Datum_Prijema = 0L;
    public int Predefinisana_Dostupnost = 0;

    public int KljucCijene = 1;
	public boolean Special = false;

}
