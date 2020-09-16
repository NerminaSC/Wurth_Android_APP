package ba.wurth.mb.Classes.Objects;

import java.util.ArrayList;

public class Product {
	public long _id = 0;
	public long ProductID = 0;
	public int UOMID = 0;
	public int Priority = 0;
	public String UOMName = "";
	public String Name = "";
    public String CategoryName = "";
	public String Code = "";
    public String Barcode = "";
	public String Description = "";
	public String Content = "";
	public int UnitsInStock = 0;
	public Pricelist PriceList = new Pricelist();
    public ArrayList<Document> documents = new ArrayList<Document>();


    /** WURTH **/
    public String Naziv = "";
    public String Atribut = "";
    public String Zbirni_Naziv = "";
    public long ArtikalID = 0;
    public long Grupa_Artikla = 0;
    public String MjernaJedinica = "";
    public String Kod_Zbirne_Cjen_Razrade = "";
    public int Status_Artikla = -1;
    public int Status_Prezentacije_Artikla = -1;
    public int Zamjenski_Artikal = -1;
}
