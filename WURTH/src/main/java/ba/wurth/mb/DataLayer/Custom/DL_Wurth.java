package ba.wurth.mb.DataLayer.Custom;

import android.database.Cursor;

import io.requery.android.database.sqlite.SQLiteDatabase;

import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.PaymentDate;
import ba.wurth.mb.Classes.Objects.PaymentMethod;
import ba.wurth.mb.Classes.Objects.PricelistItem;
import ba.wurth.mb.Classes.wurthMB;
import io.requery.android.database.sqlite.SQLiteStatement;

public class DL_Wurth {
    private static String methodName = "";
    private static String className = "DL_Products";
    public static final String TABLE_PRODUCTS_FTS = "PRODUCTS_FTS";
    public static final String TABLE_CLIENTS_FTS = "CLIENTS_FTS";
    public static final String TABLE_DELIVERYPLACES_FTS = "DELIVERYPLACES_FTS";

    // Database fields

    private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
    private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

    public static void updateFTS(ArrayList<String> product_id, ArrayList<String> client_id, ArrayList<String> delivery_place_id) {

        db.beginTransaction();
        try {


            if (product_id != null && product_id.size() > 0) {
                db.execSQL("DELETE FROM PRODUCTS_FTS WHERE PRODUCTS_FTS.ProductID IN (" + TextUtils.join(",", product_id) + ")");
                db.execSQL("INSERT INTO PRODUCTS_FTS (ProductID, ArtikalID, Name, Code, Description, Keyword, Keyword1, Barcode) " +
                        " SELECT Products.ProductID, Artikli.ID, Artikli.Naziv, Artikli.sifra, " +
                        " CASE WHEN Artikli.Zbirni_Naziv IS NULL THEN ARTIKAL_GRUPE.Naziv ELSE Artikli.Zbirni_Naziv END, " +
                        " REPLACE(REPLACE(Artikli.sifra, ' ' ,''), '-',''), " +
                        " REPLACE(REPLACE(SUBSTR(Artikli.sifra, 2, LENGTH(Artikli.sifra)-1), ' ' ,''), '-',''), " +
                        " Artikli.BarCode " +
                        " FROM ARTIKLI " +
                        " LEFT JOIN ARTIKAL_GRUPE ON ARTIKLI.Grupa_Artikla = ARTIKAL_GRUPE.ID " +
                        " LEFT JOIN Products ON ARTIKLI.ID = Products._productid " +
                        " WHERE Artikli.Naziv IS NOT NULL " +
                        " AND Products.ProductID IN (" + TextUtils.join(",", product_id) + ")");
            }


            if (client_id != null && client_id.size() > 0) {
                db.execSQL("DELETE FROM CLIENTS_FTS WHERE CLIENTS_FTS.ClientID IN (" + TextUtils.join(",", client_id) + ")");
                db.execSQL("INSERT INTO CLIENTS_FTS (ClientID, PartnerID, Name, Code, Keyword) " +
                        " SELECT Clients.ClientID, PARTNER.ID, PARTNER.Naziv, PARTNER.Kod, REPLACE(PARTNER.Kod, ' ', '') FROM PARTNER " +
                        " LEFT JOIN Clients ON PARTNER.ID = Clients._clientid " +
                        " AND Clients.ClientID IN (" + TextUtils.join(",", client_id) + ")");
            }

            if (delivery_place_id != null && delivery_place_id.size() > 0) {
                db.execSQL("DELETE FROM DELIVERYPLACES_FTS WHERE DELIVERYPLACES_FTS.DeliveryPlaceID IN (" + TextUtils.join(",", delivery_place_id) + ")");
                db.execSQL("INSERT INTO DELIVERYPLACES_FTS (DeliveryPlaceID, PartnerID, Name, Code, Keyword) " +
                        " SELECT DeliveryPlaces.DeliveryPlaceID, PARTNER.ID, PARTNER.Naziv, PARTNER.Kod, REPLACE(PARTNER.Kod, ' ', '') FROM PARTNER " +
                        " LEFT JOIN DeliveryPlaces ON PARTNER.ID = DeliveryPlaces._deliveryplaceid " +
                        " AND DeliveryPlaces.DeliveryPlaceID IN (" + TextUtils.join(",", delivery_place_id) + ")");
            }

            db.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.endTransaction();
    }

    public static Cursor GET_Categories(Long CategoryID, String searchWord) {
        Cursor cur;
        cur = db_readonly.rawQuery("Select ID, RoditeljID, Naziv, Sifra, Slika FROM ARTIKAL_GRUPE "
                + " Where ARTIKAL_GRUPE.Naziv Like '%" + searchWord + "%'"
                + (CategoryID > 0L ? " AND ARTIKAL_GRUPE.RoditeljID = " + CategoryID : "")
                + " Order by ARTIKAL_GRUPE.Naziv Asc", null);

        cur.getCount();
        return cur;
    }

    public static Cursor GET_Products(String searchWord, Long CategoryID, Long ProductID) {

        Cursor cur;
        cur = db_readonly.rawQuery("SELECT Products._id, ARTIKLI.ID, ARTIKLI.MjernaJedinica, ARTIKLI.Naziv, ARTIKLI.Kod_Zbirne_Cjen_Razrade, " +
                " ARTIKLI.Atribut, ARTIKLI.sifra, ARTIKLI.Zbirni_Naziv, ARTIKLI.Stanje_Zaliha, ARTIKLI.Predefinisana_Dostupnost, ARTIKLI.Datum_Prijema, ARTIKLI.Narucena_Kolicina, ARTIKLI.Predefinisana_Dostupnost, ARTIKLI.Zbirni_Naziv, " +
                " ARTIKLI.Status_Artikla, ARTIKLI.Status_Prezentacije_Artikla, ARTIKLI.MjernaJedinica, " +
                " Products.ProductID, ARTIKAL_SLIKE.Velika, ARTIKAL_GRUPE.NAZIV AS GrupniNaziv   " +

                " FROM ARTIKLI" +

                " INNER JOIN Products On ARTIKLI.ID = Products._productid " +
                " LEFT JOIN ARTIKAL_GRUPE On ARTIKLI.Grupa_Artikla = ARTIKAL_GRUPE.ID " +
                //" LEFT Join CJENIK On ARTIKLI.ID = CJENIK.ArtikalID " +
                " LEFT Join ARTIKAL_SLIKE On ARTIKLI.ID = ARTIKAL_SLIKE.ArtikalID " +

                " WHERE ARTIKLI.Grupa_Artikla = ?" +
                " AND (Products.Active = 1 OR Products.Active IS NULL) " +
                //" AND (Cjenik.PopustOD = 0  OR Cjenik.PopustOD IS NULL) " +
                " AND ARTIKLI.Status_Artikla <> 0 " +

                " ORDER BY ARTIKLI.Sifra", new String[]{Long.toString(CategoryID)});

        cur.getCount();
        return cur;
    }

    public static Cursor GET_Products_Actions(String searchWord, Long ClientID, Long ProductID) {
        Cursor cur = db_readonly.rawQuery("SELECT AKCIJE.IDAkcije AS _id, ProductID, AKCIJE.* " +

                " FROM AKCIJE " +
                " LEFT JOIN ARTIKLI ON AKCIJE.IDArtikla = ARTIKLI.ID " +
                " LEFT JOIN Products ON ARTIKLI.ID = Products._productid " +

                " WHERE DatumOd <= " + (System.currentTimeMillis()) + " AND DatumDo >= " + (System.currentTimeMillis()) + " " +

                //" AND ((PotencijalOd = 0 AND PotencijalDo = 0) " +
                //" OR (PotencijalOd <= (SELECT Potencijal FROM PARTNER INNER JOIN CLIENTS ON PARTNER.ID = CLIENTS._clientid WHERE Clients.AccountID = " + wurthMB.getUser().AccountID +  " AND CLIENTS.ClientID = " + ClientID + ") AND (PotencijalDo = 0 OR PotencijalDo >= (SELECT Potencijal FROM PARTNER INNER JOIN CLIENTS ON PARTNER.ID = CLIENTS._clientid WHERE CLIENTS.ClientID > 0))))" +
                " ORDER BY AKCIJE.OrderNo ASC, TimeStamp DESC", null);

        cur.getCount();
        return cur;
    }

    public static Cursor GET_Products_ByCategoryCode(String CategoryCode) {

        Cursor cur;
        cur = db_readonly.rawQuery("SELECT Products._id, ARTIKLI.ID, ARTIKLI.MjernaJedinica, ARTIKLI.Naziv, ARTIKLI.Kod_Zbirne_Cjen_Razrade, " +
                " ARTIKLI.Atribut, ARTIKLI.sifra, ARTIKLI.Zbirni_Naziv, ARTIKLI.Stanje_Zaliha, ARTIKLI.Predefinisana_Dostupnost, ARTIKLI.Datum_Prijema, ARTIKLI.Narucena_Kolicina, ARTIKLI.Predefinisana_Dostupnost, ARTIKLI.Zbirni_Naziv, " +
                " ARTIKLI.Status_Artikla, ARTIKLI.Status_Prezentacije_Artikla, ARTIKLI.MjernaJedinica, " +
                " Products.ProductID, CJENIK.OsnovnaCijena, ARTIKAL_SLIKE.Velika " +

                " FROM ARTIKLI" +

                " INNER JOIN Products On ARTIKLI.ID = Products._productid " +
                " LEFT Join CJENIK On ARTIKLI.ID = CJENIK.ArtikalID " +
                " LEFT Join ARTIKAL_SLIKE On ARTIKLI.ID = ARTIKAL_SLIKE.ArtikalID " +

                " WHERE ARTIKLI.Kod_Zbirnog_Naziva = '" + CategoryCode + "'" +
                " AND (Products.Active = 1 OR Products.Active IS NULL) " +
                " AND (Cjenik.PopustOD = 0  OR Cjenik.PopustOD IS NULL) " +
                " AND ARTIKLI.Status_Prezentacije_Artikla <> 1 " +
                " ORDER BY ARTIKLI.Naziv", null);

        cur.getCount();
        return cur;
    }

    public static Cursor GET_Product(Long ArtikalID) {
        Cursor cur;
        cur = db_readonly.rawQuery("SELECT Products._id, ARTIKLI.*, ARTIKAL_SLIKE.Velika, " +
                        " ARTIKLI.MjernaJedinica, ARTIKAL_PAKOVANJA.Pakovanje, ARTIKAL_PAKOVANJA.KodPakovanja, " +
                        " Products.ProductID, CJENIK.OsnovnaCijena, CJENIK.KljucCijene " +

                        " FROM ARTIKLI " +

                        " INNER JOIN Products On ARTIKLI.ID = Products._productid " +
                        " LEFT Join CJENIK On ARTIKLI.ID = CJENIK.ArtikalID " +
                        " LEFT Join ARTIKAL_PAKOVANJA On ARTIKLI.ID = ARTIKAL_PAKOVANJA.ArtikalID " +
                        " LEFT Join ARTIKAL_SLIKE On ARTIKLI.ID = ARTIKAL_SLIKE.ArtikalID " +

                        " WHERE ARTIKLI.ID = ? ORDER BY Cjenik.PopustOD LIMIT 1",
                //" AND ARTIKLI.Status_Prezentacije_Artikla <> 1 " +
                //" AND (Cjenik.PopustOD = 0 OR Cjenik.PopustOD IS NULL)",
                new String[]{Long.toString(ArtikalID)});

        cur.getCount();
        return cur;
    }

    public static Cursor GET_Product_Price(Long ArtikalID, Long ClientID) {
        Cursor cur;
        cur = db_readonly.rawQuery("SELECT CJENIK.OsnovnaCijena, CJENIK.KljucCijene " +

                " FROM CJENIK " +

                " WHERE ArtikalID = " + ArtikalID +

                " AND ((DatumOD = 0 AND DatumDO = 0) OR (DatumOd <= " + System.currentTimeMillis() + " AND DatumDo >= " + System.currentTimeMillis() + ")) " +

                " AND (" +
                "        (PotencijalOd = 0 AND PotencijalDo = 0)  " +
                "        OR (" +
                "            PotencijalOd >= (SELECT Potencijal FROM PARTNER INNER JOIN CLIENTS ON PARTNER.ID = CLIENTS._clientid WHERE Clients.AccountID = 178 AND CLIENTS.ClientID = " + ClientID + ") " +
                "            AND " +
                "            PotencijalDo <= (SELECT Potencijal FROM PARTNER INNER JOIN CLIENTS ON PARTNER.ID = CLIENTS._clientid WHERE Clients.AccountID = 178 AND CLIENTS.ClientID = " + ClientID + ") " +
                "            ) " +
                "    ) " +

                " ORDER BY CJENIK.OsnovnaCijena ASC " +

                " LIMIT 1 ", null);

        cur.getCount();
        return cur;
    }

    public static Long GET_ProductCategoryID(Long ArtikalID) {
        Long CategoryID = 0L;
        Cursor cur = db_readonly.rawQuery("SELECT ARTIKLI.Grupa_Artikla " +
                " FROM ARTIKLI " +
                " WHERE ARTIKLI.ID = ?", new String[]{Long.toString(ArtikalID)});

        if (cur.moveToFirst()) CategoryID = cur.getLong(0);
        cur.close();
        return CategoryID;
    }

    public static Cursor GET_ProductCategory(Long CategoryID) {
        Cursor cur = db_readonly.rawQuery("SELECT ARTIKAL_GRUPE.* " +
                " FROM ARTIKAL_GRUPE " +
                " WHERE ARTIKAL_GRUPE.ID = ?", new String[]{Long.toString(CategoryID)});

        return cur;
    }

    public static Cursor GET_Product_ByBarcode(String barcode) {
        Cursor cur;
        cur = db_readonly.rawQuery("SELECT ARTIKAL_PAKOVANJA.ArtikalID " +

                " FROM ARTIKAL_PAKOVANJA " +

                " WHERE ARTIKAL_PAKOVANJA.Barcode = '" + barcode + "' LIMIT 1", null);

        cur.getCount();
        return cur;
    }

    public static Cursor GET_ProductImages(Long ArtikalID) {
        Cursor cur;
        cur = db_readonly.rawQuery("SELECT ARTIKAL_SLIKE.Velika " +

                " FROM ARTIKAL_SLIKE " +

                " WHERE ARTIKAL_SLIKE.ArtikalID = ? AND Velika IS NOT NULL", new String[]{Long.toString(ArtikalID)});

        cur.getCount();
        return cur;
    }

    public static Cursor GET_ProductDocuments(Long ArtikalID) {
        Cursor cur;
        cur = db_readonly.rawQuery("SELECT ARTIKLI_DOKUMENTI.* " +

                " FROM ARTIKLI_DOKUMENTI " +

                " WHERE ARTIKLI_DOKUMENTI.ArtikalID = ? ORDER BY ARTIKLI_DOKUMENTI.TipDokumentaID", new String[]{Long.toString(ArtikalID)});

        cur.getCount();
        return cur;
    }

    public static Cursor GET_Related(Long ArtikalID) {
        Cursor cur;
        cur = db_readonly.rawQuery("Select ARTIKLI.ID, ARTIKLI.Naziv, ARTIKLI.sifra, ARTIKAL_SLIKE.Velika, ARTIKLI.Zbirni_Naziv, ARTIKLI.Atribut, VEZANI_ARTIKLI.NivoPovezanosti, ARTIKAL_GRUPE.Naziv AS Grupni_Naziv, ARTIKAL_GRUPE.Slika AS Grupna_Slika " +

                " FROM VEZANI_ARTIKLI " +

                " INNER JOIN ARTIKLI ON VEZANI_ARTIKLI.VezaniArtikalID = ARTIKLI.ID " +
                " LEFT JOIN ARTIKAL_GRUPE On ARTIKLI.Grupa_Artikla = ARTIKAL_GRUPE.ID " +
                " LEFT JOIN ARTIKAL_SLIKE On ARTIKLI.ID = ARTIKAL_SLIKE.ArtikalID " +

                " WHERE VEZANI_ARTIKLI.ArtikalID = " + ArtikalID +
                " Order by VEZANI_ARTIKLI.NivoPovezanosti, ARTIKLI.Naziv Asc", null);

        cur.getCount();
        return cur;
    }

    public static Cursor GET_Similar(Long ArtikalID) {
        Cursor cur;
        cur = db_readonly.rawQuery("Select ARTIKLI.ID, ARTIKLI.Naziv, ARTIKLI.sifra, ARTIKAL_SLIKE.Velika, ARTIKLI.Zbirni_Naziv, ARTIKLI.Atribut, SLICNI_ARTIKLI.NivoPovezanosti, ARTIKAL_GRUPE.Naziv AS Grupni_Naziv, ARTIKAL_GRUPE.Slika AS Grupna_Slika " +

                " FROM SLICNI_ARTIKLI " +

                " INNER JOIN ARTIKLI ON SLICNI_ARTIKLI.VezaniArtikalID = ARTIKLI.ID " +
                " LEFT JOIN ARTIKAL_GRUPE On ARTIKLI.Grupa_Artikla = ARTIKAL_GRUPE.ID " +
                " LEFT JOIN ARTIKAL_SLIKE On ARTIKLI.ID = ARTIKAL_SLIKE.ArtikalID " +

                " WHERE SLICNI_ARTIKLI.ArtikalID = " + ArtikalID +
                " Order by SLICNI_ARTIKLI.NivoPovezanosti, ARTIKLI.Naziv Asc", null);

        cur.getCount();
        return cur;
    }

    public static Cursor GET_Safety(Long ArtikalID) {
        Cursor cur;
        cur = db_readonly.rawQuery("Select ARTIKLI.ID, ARTIKLI.Naziv, ARTIKLI.sifra, ARTIKAL_SLIKE.Velika, ARTIKLI.Zbirni_Naziv, ARTIKLI.Atribut, ARTIKLI_ZASTITE_NA_RADU.NivoPovezanosti, ARTIKAL_GRUPE.Naziv AS Grupni_Naziv, ARTIKAL_GRUPE.Slika AS Grupna_Slika " +

                " FROM ARTIKLI_ZASTITE_NA_RADU " +

                " INNER JOIN ARTIKLI ON ARTIKLI_ZASTITE_NA_RADU.VezaniArtikalID = ARTIKLI.ID " +
                " LEFT JOIN ARTIKAL_GRUPE On ARTIKLI.Grupa_Artikla = ARTIKAL_GRUPE.ID " +
                " LEFT JOIN ARTIKAL_SLIKE On ARTIKLI.ID = ARTIKAL_SLIKE.ArtikalID " +

                " WHERE ARTIKLI_ZASTITE_NA_RADU.ArtikalID = " + ArtikalID +
                " Order by ARTIKLI_ZASTITE_NA_RADU.NivoPovezanosti, ARTIKLI.Naziv Asc", null);

        cur.getCount();
        return cur;
    }

    public static Cursor GET_Search(String searchText) {

        String searchTextFull = searchText;
        String searchTextCompact = searchText.replace(" ", "");
        String searchTextLight = searchText;

        if (searchText.length() > 0 && searchText.startsWith("00")) {
            searchTextLight = searchText.substring(1, searchText.length());
        }

        searchTextLight = searchText.substring(1, searchText.length());

        Cursor cur = null;

        try {
            cur = db_readonly.rawQuery("SELECT 0 AS _id, PRODUCTS_FTS.ArtikalID AS ID, 0 AS SectionID, PRODUCTS_FTS.Name, PRODUCTS_FTS.Code AS Code, 'Artikli' AS Section, 1 AS SectionType, PRODUCTS_FTS.Name AS Zbirni_Naziv, ARTIKLI.Status_Artikla, ARTIKLI.Status_Prezentacije_Artikla, ARTIKLI.Zamjenski_Artikal, B.sifra AS Zamjenski_Sifra " +
                            " FROM PRODUCTS_FTS " +
                            " INNER JOIN ARTIKLI ON PRODUCTS_FTS.ArtikalID = ARTIKLI.ID " +
                            " LEFT JOIN ARTIKLI B ON ARTIKLI.Zamjenski_Artikal = B.ID " +
                            " WHERE PRODUCTS_FTS MATCH 'Keyword:" + searchTextCompact + "* OR Keyword1:" + searchTextCompact + "* OR Name:" + searchText + "*' AND ARTIKLI.Status_Artikla <> 0 AND ARTIKLI.Status_Artikla <> 2 " +
                            //" WHERE Keyword LIKE '%" + searchText + "%' OR Name LIKE '%" + searchText + "%' AND ARTIKLI.Status_Artikla <> 0 " +

                    /*
                    " UNION ALL " +

                    " SELECT _id, ClientID AS ID, ClientID AS SectionID, Name AS Name, Clients.code AS Code, 'Kupci' AS Section, 2 AS SectionType, Name AS Zbirni_Naziv " +
                    " FROM Clients " +
                    " WHERE (Code LIKE '" + searchText + "%' OR Name LIKE '%" + searchText + "%') AND Active = 1 AND AccountID = " + wurthMB.getUser().AccountID +
                    */
                            " ORDER BY Code, Name "
                    , null);
        } catch (Exception ex) {
            wurthMB.AddError("GET_Search", ex.getMessage(), ex);
        }

        cur.getCount();

        return cur;
    }

    public static Cursor GET_ProductsSearch(String searchText) {

        String searchTextFull = searchText;
        String searchTextCompact = searchText.replace(" ", "");
        String searchTextLight = searchText;

        if (searchText.length() > 0 && searchText.startsWith("00")) {
            searchTextLight = searchText.substring(1, searchText.length());
        }

        Cursor cur = null;
        try {
            cur = db_readonly.rawQuery("SELECT PRODUCTS_FTS.ArtikalID AS _id, PRODUCTS_FTS.Name, PRODUCTS_FTS.Code, " +
                            " PRODUCTS_FTS.Description, PRODUCTS_FTS.Barcode,  ARTIKLI.Status_Artikla, " +
                            " ARTIKLI.Status_Prezentacije_Artikla, ARTIKLI.Zamjenski_Artikal, B.sifra AS Zamjenski_Sifra " +

                            " FROM PRODUCTS_FTS " +

                            " INNER JOIN ARTIKLI ON PRODUCTS_FTS.ArtikalID = ARTIKLI.ID " +
                            " LEFT JOIN ARTIKLI B ON ARTIKLI.Zamjenski_Artikal = B.ID " +

                            " WHERE PRODUCTS_FTS MATCH 'Keyword:" + searchTextCompact + "* OR Keyword1:" + searchTextCompact + "* OR Name:" + searchText + "*' " +
                            //" AND ARTIKLI.Status_Artikla <> 0 " +
                            " AND ARTIKLI.Status_Artikla <> 2 " +

                            " ORDER BY Code, Name "
                    , null);
        } catch (Exception ex) {
            wurthMB.AddError("GET_ProductsSearch", ex.getMessage(), ex);
        }

        cur.getCount();

        return cur;
    }

    public static Cursor GET_ClientContacts(Long ClientID) {
        Cursor cur = null;
        try {
            cur = db_readonly.rawQuery("SELECT PARTNER_KONTAKTI.* " +
                            " FROM PARTNER " +
                            " INNER JOIN PARTNER_KONTAKTI ON PARTNER.ID = PARTNER_KONTAKTI.PartnerID " +
                            " INNER JOIN Clients ON PARTNER.ID = Clients._clientid " +
                            " WHERE Clients.ClientID = ? " +
                            " ORDER BY PARTNER_KONTAKTI.Ime ASC "
                    , new String[]{Long.toString(ClientID)});
        } catch (Exception ex) {
            wurthMB.AddError("GET_ClientContacts", ex.getMessage(), ex);
        }

        cur.getCount();

        return cur;
    }

    public static Cursor GET_ClientDetails(Long ClientID) {
        Cursor cur = null;
        try {
            cur = db_readonly.rawQuery("SELECT PARTNER_DETALJI.*, PARTNER.BrzaIsporuka, PARTNER.Veleprodaja, A.IME AS K2User, B.IME AS K1User, C.IME AS KAMUser, D.IME AS SpecialUser " +
                            " FROM Clients " +
                            " INNER JOIN PARTNER_DETALJI ON Clients._clientid = PARTNER_DETALJI.CustomerID " +
                            " INNER JOIN PARTNER ON Clients._clientid = PARTNER.ID " +
                            " LEFT JOIN KOMERCIJALISTI AS A ON PARTNER.KomercijalistaID = A.ID " +
                            " LEFT JOIN KOMERCIJALISTI AS B ON PARTNER_DETALJI.K1 = B.ID " +
                            " LEFT JOIN KOMERCIJALISTI AS C ON PARTNER_DETALJI.KAM = C.ID " +
                            " LEFT JOIN KOMERCIJALISTI AS D ON PARTNER_DETALJI.KomercijalistaSpecijalista = D.ID " +
                            " WHERE Clients.ClientID = ? "
                    , new String[]{Long.toString(ClientID)});
        } catch (Exception ex) {
            wurthMB.AddError("GET_ClientDetails", ex.getMessage(), ex);
        }

        cur.getCount();

        return cur;
    }

    public static Cursor GET_ClientImages(Long ClientID) {
        Cursor cur = null;
        try {
            cur = db_readonly.rawQuery("SELECT Documents.*, Visits.startDT " +
                            " FROM Documents " +
                            " INNER JOIN Visits ON Documents.ItemID = VISITS.VisitID " +
                            " WHERE Visits.ClientID = ? AND OptionID = 9 ORDER BY Visits.startDT DESC "
                    , new String[]{Long.toString(ClientID)});
        } catch (Exception ex) {
            wurthMB.AddError("GET_ClientImages", ex.getMessage(), ex);
        }

        cur.getCount();

        return cur;
    }

    public static Cursor GET_ClientMandatory(Long ClientID, Long DeliveryPlaceID) {
        Cursor cur = null;
        try {
            if (ClientID > 0L) {
                cur = db_readonly.rawQuery("SELECT PARTNER.* " +
                                " FROM Clients " +
                                " INNER JOIN PARTNER_DETALJI ON Clients._clientid = PARTNER_DETALJI.CustomerID " +
                                " INNER JOIN PARTNER ON Clients._clientid = PARTNER.ID " +
                                " WHERE Clients.ClientID = ? "
                        , new String[]{Long.toString(ClientID)});
            }

            if (DeliveryPlaceID > 0L) {
                cur = db_readonly.rawQuery("SELECT PARTNER.* " +
                                " FROM DeliveryPlaces " +
                                " INNER JOIN PARTNER_DETALJI ON DeliveryPlaces._deliveryplaceid = PARTNER_DETALJI.CustomerID " +
                                " INNER JOIN PARTNER ON DeliveryPlaces._deliveryplaceid = PARTNER.ID " +
                                " WHERE DeliveryPlaces.DeliveryPlaceID = ? "
                        , new String[]{Long.toString(DeliveryPlaceID)});
            }
        } catch (Exception ex) {
            wurthMB.AddError("GET_ClientMandatory", ex.getMessage(), ex);
        }

        cur.getCount();

        return cur;
    }

    public static Cursor GET_ClientBusinessCategory(Long ClientID) {
        Cursor cur = null;
        try {
            cur = db_readonly.rawQuery("SELECT PARTNER.*, PARTNER_BRANSE.*, BRANSE.Naziv AS NazivBranse " +
                            " FROM Clients " +
                            " INNER JOIN PARTNER ON Clients._clientid = PARTNER.ID " +
                            " INNER JOIN PARTNER_BRANSE ON PARTNER.ID = PARTNER_BRANSE.PartnerID " +
                            " INNER JOIN BRANSE ON PARTNER_BRANSE.Bransa = BRANSE.KodBranse " +
                            " WHERE Clients.ClientID = ? "
                    , new String[]{Long.toString(ClientID)});
        } catch (Exception ex) {
            wurthMB.AddError("GET_ClientDetails", ex.getMessage(), ex);
        }

        cur.getCount();

        return cur;
    }

    public static Cursor GET_BusinessCategory_ByCode(String code) {
        Cursor cur = null;
        try {
            cur = db_readonly.rawQuery("SELECT * FROM BRANSE WHERE KodBranse='" + code + "'", null);
        } catch (Exception ex) {
            wurthMB.AddError("GET_BusinessCategory_ByCode", ex.getMessage(), ex);
        }

        cur.getCount();

        return cur;
    }

    public static Cursor GET_BusinessCategory_1st() {
        Cursor cur = null;
        try {
            cur = db_readonly.rawQuery("SELECT Naziv || ' ' || '(' || KodBranse || ')' AS Naziv, KodBranse FROM BRANSE WHERE LENGTH(KodBranse) = 2 ORDER BY KodBranse ", null);
        } catch (Exception ex) {
            wurthMB.AddError("GET_BusinessCategory_1st", ex.getMessage(), ex);
        }

        cur.getCount();

        return cur;
    }

    public static Cursor GET_BusinessCategory_2st(String code) {
        Cursor cur = null;
        try {
            cur = db_readonly.rawQuery("SELECT Naziv || ' ' || '(' || KodBranse || ')' AS Naziv, KodBranse FROM BRANSE WHERE LENGTH(KodBranse) = 4 AND KodBranse LIKE '" + code + "%' ORDER BY KodBranse ", null);
        } catch (Exception ex) {
            wurthMB.AddError("GET_BusinessCategory_2st", ex.getMessage(), ex);
        }

        cur.getCount();

        return cur;
    }

    public static Cursor GET_BusinessCategory_3st(String code) {
        Cursor cur = null;
        try {
            cur = db_readonly.rawQuery("SELECT Naziv || ' ' || '(' || KodBranse || ')' AS Naziv, KodBranse FROM BRANSE WHERE LENGTH(KodBranse) > 4 AND KodBranse LIKE '" + code + "%' ORDER BY KodBranse ", null);
        } catch (Exception ex) {
            wurthMB.AddError("GET_BusinessCategory_3st", ex.getMessage(), ex);
        }

        cur.getCount();

        return cur;
    }

    public static Cursor GET_Packages(Long ArtikalID) {

        Cursor cur;
        cur = db_readonly.rawQuery("SELECT ARTIKAL_PAKOVANJA.* " +

                " FROM ARTIKAL_PAKOVANJA " +

                " WHERE ARTIKAL_PAKOVANJA.ArtikalID = ?" +
                " ORDER BY ARTIKAL_PAKOVANJA.Pakovanje ASC", new String[]{Long.toString(ArtikalID)});

        cur.getCount();
        return cur;
    }

    public static ArrayList<PricelistItem> GET_Pricelist(Long ArtikalID) {

        ArrayList<PricelistItem> items = new ArrayList<PricelistItem>();

        try {

            long _PartnerID = (wurthMB.getOrder() != null && wurthMB.getOrder().client != null) ? wurthMB.getOrder().client._clientid : 0;
            if (wurthMB.getOrder() != null && wurthMB.getOrder().client != null && wurthMB.getOrder().client._parentid > 0)
                _PartnerID = wurthMB.getOrder().client._parentid;
            long _Potencijal = (wurthMB.getOrder() != null && wurthMB.getOrder().client != null) ? wurthMB.getOrder().client.Potencijal : 0;
            ArrayList<String> _Branse = (wurthMB.getOrder() != null && wurthMB.getOrder().client != null) ? wurthMB.getOrder().client.Branse : new ArrayList<String>();
            String _KanalDistribucije = (wurthMB.getOrder() != null && wurthMB.getOrder().client != null) ? wurthMB.getOrder().client.KanalDistribucije : "";

            Cursor cur;
            cur = db_readonly.rawQuery(" SELECT 0 AS Type, CJENIK.* FROM CJENIK " +

                    " WHERE ArtikalID = " + ArtikalID +
                    " AND (PartnerID = 0 OR PartnerID = " + _PartnerID + ") " +
                    " AND Bransa = '' " +
                    " AND PotencijalOD = 0 " +
                    " AND PotencijalDO = 0 " +
                    " AND DatumOD = 0 " +
                    " AND DatumDO = 0 " +

                    " GROUP BY KolicinaOD, KolicinaDO, KanalDistribucije " +

                    " UNION " +  //ACTION

                    " SELECT 1 AS Type, CJENIK.* FROM CJENIK " +
                    " WHERE ArtikalID = " + ArtikalID +
                    " AND PartnerID = 0 " +
                    " AND DatumOD <= " + System.currentTimeMillis() +
                    " AND DatumDO >= " + System.currentTimeMillis() +

                    " GROUP BY KolicinaOD, KanalDistribucije, Bransa, PotencijalOD " +

                    " UNION " + // BRANCHES

                    " SELECT 2 AS Type, CJENIK.* FROM CJENIK " +
                    " WHERE ArtikalID = " + ArtikalID +
                    " AND PartnerID = 0 " +
                    " AND NOT (Bransa = '') " + // AND PotencijalOD = 0 AND PotencijalDO = 0) " +
                    " AND DatumOD = 0 " +
                    " AND DatumDO = 0 " +

                    " GROUP BY KolicinaOD, KanalDistribucije, Bransa, PotencijalOD " +

                    " ORDER BY DatumDO, KolicinaOD, KanalDistribucije desc, Bransa, PartnerID DESC ", null);

            cur.getCount();

            boolean priceDefinedForAllByClient = false;

            // ADD Partner PRICES
            cur.moveToPosition(-1);

            while (cur.moveToNext()) {

                PricelistItem pricelistItem = new PricelistItem();

                long PartnerID = !cur.isNull(cur.getColumnIndex("PartnerID")) ? cur.getLong(cur.getColumnIndex("PartnerID")) : 0L;
                String Bransa = !cur.isNull(cur.getColumnIndex("Bransa")) ? cur.getString(cur.getColumnIndex("Bransa")) : "";
                int PotencijalOD = !cur.isNull(cur.getColumnIndex("PotencijalOD")) ? cur.getInt(cur.getColumnIndex("PotencijalOD")) : 0;
                int PotencijalDO = !cur.isNull(cur.getColumnIndex("PotencijalDO")) ? cur.getInt(cur.getColumnIndex("PotencijalDO")) : 0;
                int KljucCijene = !cur.isNull(cur.getColumnIndex("KljucCijene")) ? cur.getInt(cur.getColumnIndex("KljucCijene")) : 0;
                int CijenaPonude = !cur.isNull(cur.getColumnIndex("CijenaPonude")) ? cur.getInt(cur.getColumnIndex("CijenaPonude")) : 0;
                int Pakovanje = !cur.isNull(cur.getColumnIndex("Pakovanje")) ? cur.getInt(cur.getColumnIndex("Pakovanje")) : 0;
                double KolicinaDo = !cur.isNull(cur.getColumnIndex("KolicinaDO")) ? cur.getDouble(cur.getColumnIndex("KolicinaDO")) : 0;
                double KolicinaOD = !cur.isNull(cur.getColumnIndex("KolicinaOD")) ? cur.getDouble(cur.getColumnIndex("KolicinaOD")) : 0;
                double PopustOD = !cur.isNull(cur.getColumnIndex("PopustOD")) ? cur.getDouble(cur.getColumnIndex("PopustOD")) : 0;
                double PopustDO = !cur.isNull(cur.getColumnIndex("PopustDO")) ? cur.getDouble(cur.getColumnIndex("PopustDO")) : 0;
                double DodatniPopust = !cur.isNull(cur.getColumnIndex("DodatniPopust")) ? cur.getDouble(cur.getColumnIndex("DodatniPopust")) : 0;
                double OsnovnaCijena = !cur.isNull(cur.getColumnIndex("OsnovnaCijena")) ? cur.getDouble(cur.getColumnIndex("OsnovnaCijena")) : 0;
                int AkcijskaCijena = !cur.isNull(cur.getColumnIndex("AkcijskaCijena")) ? cur.getInt(cur.getColumnIndex("AkcijskaCijena")) : 0;
                Long DatumOd = !cur.isNull(cur.getColumnIndex("DatumOD")) ? cur.getLong(cur.getColumnIndex("DatumOD")) : 0L;
                Long DatumDo = !cur.isNull(cur.getColumnIndex("DatumDO")) ? cur.getLong(cur.getColumnIndex("DatumDO")) : 0L;

                if (priceDefinedForAllByClient) break;

                if (PartnerID > 0) {
                    pricelistItem.ArtikalID = ArtikalID;
                    pricelistItem.PartnerID = PartnerID;
                    pricelistItem.PotencijalOD = PotencijalOD;
                    pricelistItem.PotencijalDO = PotencijalDO;
                    pricelistItem.Bransa = Bransa;
                    pricelistItem.OsnovnaCijena = OsnovnaCijena;
                    pricelistItem.KljucCijene = KljucCijene;
                    pricelistItem.PopustOD = PopustOD;
                    pricelistItem.PopustDO = PopustDO;
                    pricelistItem.KolicinaOD = KolicinaOD;
                    pricelistItem.KolicinaDo = KolicinaDo;
                    pricelistItem.DodatniPopust = DodatniPopust;
                    pricelistItem.DatumOd = DatumOd;
                    pricelistItem.DatumDo = DatumDo;
                    pricelistItem.CijenaPonude = CijenaPonude;
                    pricelistItem.AkcijskaCijena = AkcijskaCijena;
                    pricelistItem.Pakovanje = Pakovanje;
                    items.add(pricelistItem);
                    if (KolicinaDo == 0) priceDefinedForAllByClient = true;
                }
            }

            // ADD Branches PRICES
            cur.moveToPosition(-1);

            while (cur.moveToNext()) {

                PricelistItem pricelistItem = new PricelistItem();

                long PartnerID = !cur.isNull(cur.getColumnIndex("PartnerID")) ? cur.getLong(cur.getColumnIndex("PartnerID")) : 0L;
                String Bransa = !cur.isNull(cur.getColumnIndex("Bransa")) ? cur.getString(cur.getColumnIndex("Bransa")) : "";
                String KanalDistribucije = !cur.isNull(cur.getColumnIndex("KanalDistribucije")) ? cur.getString(cur.getColumnIndex("KanalDistribucije")) : "";
                int PotencijalOD = !cur.isNull(cur.getColumnIndex("PotencijalOD")) ? cur.getInt(cur.getColumnIndex("PotencijalOD")) : 0;
                int PotencijalDO = !cur.isNull(cur.getColumnIndex("PotencijalDO")) ? cur.getInt(cur.getColumnIndex("PotencijalDO")) : 0;
                int KljucCijene = !cur.isNull(cur.getColumnIndex("KljucCijene")) ? cur.getInt(cur.getColumnIndex("KljucCijene")) : 0;
                int CijenaPonude = !cur.isNull(cur.getColumnIndex("CijenaPonude")) ? cur.getInt(cur.getColumnIndex("CijenaPonude")) : 0;
                int Pakovanje = !cur.isNull(cur.getColumnIndex("Pakovanje")) ? cur.getInt(cur.getColumnIndex("Pakovanje")) : 0;
                double KolicinaDo = !cur.isNull(cur.getColumnIndex("KolicinaDO")) ? cur.getDouble(cur.getColumnIndex("KolicinaDO")) : 0;
                double KolicinaOD = !cur.isNull(cur.getColumnIndex("KolicinaOD")) ? cur.getDouble(cur.getColumnIndex("KolicinaOD")) : 0;
                double PopustOD = !cur.isNull(cur.getColumnIndex("PopustOD")) ? cur.getDouble(cur.getColumnIndex("PopustOD")) : 0;
                double PopustDO = !cur.isNull(cur.getColumnIndex("PopustDO")) ? cur.getDouble(cur.getColumnIndex("PopustDO")) : 0;
                double DodatniPopust = !cur.isNull(cur.getColumnIndex("DodatniPopust")) ? cur.getDouble(cur.getColumnIndex("DodatniPopust")) : 0;
                double OsnovnaCijena = !cur.isNull(cur.getColumnIndex("OsnovnaCijena")) ? cur.getDouble(cur.getColumnIndex("OsnovnaCijena")) : 0;
                int AkcijskaCijena = !cur.isNull(cur.getColumnIndex("AkcijskaCijena")) ? cur.getInt(cur.getColumnIndex("AkcijskaCijena")) : 0;
                Long DatumOd = !cur.isNull(cur.getColumnIndex("DatumOD")) ? cur.getLong(cur.getColumnIndex("DatumOD")) : 0L;
                Long DatumDo = !cur.isNull(cur.getColumnIndex("DatumDO")) ? cur.getLong(cur.getColumnIndex("DatumDO")) : 0L;

                if (cur.getInt(cur.getColumnIndex("Type")) == 2 /*&& !priceDefinedForAllByClient*/) {

                    if (PotencijalOD <= _Potencijal && (PotencijalDO == 0 || PotencijalDO >= _Potencijal) && (KanalDistribucije.equals("") || KanalDistribucije.equals(_KanalDistribucije))) {

                        boolean exists = false;
                        boolean branch_exists = false;

                        // CHECK IF BRANCHES IS IN ARRAY
                        for (int x = 0; x < _Branse.size(); x++) {
                            if (_Branse.get(x).startsWith(Bransa)) {
                                branch_exists = true;
                                break;
                            }
                        }
                        if (!branch_exists) continue;

                        // CHECK IF MAIN BRANCHE IS ALREADY ADDED
                        for (int x = 0; x < items.size(); x++) {

                            if (items.get(x).PartnerID == 0 && items.get(x).KolicinaOD <= KolicinaOD && (items.get(x).KolicinaDo == 0 || (items.get(x).KolicinaDo >= KolicinaDo && KolicinaDo != 0))) {

                                if (items.get(x).Bransa.length() < Bransa.length()) { //CHILD BRANCHES HAVE TO ADD
                                    items.get(x).ArtikalID = ArtikalID;
                                    items.get(x).PartnerID = PartnerID;
                                    items.get(x).PotencijalOD = PotencijalOD;
                                    items.get(x).PotencijalDO = PotencijalDO;
                                    items.get(x).Bransa = Bransa;
                                    items.get(x).OsnovnaCijena = OsnovnaCijena;
                                    items.get(x).KljucCijene = KljucCijene;
                                    items.get(x).PopustOD = PopustOD;
                                    items.get(x).PopustDO = PopustDO;
                                    items.get(x).KolicinaOD = KolicinaOD;
                                    items.get(x).KolicinaDo = KolicinaDo;
                                    items.get(x).DodatniPopust = DodatniPopust;
                                    items.get(x).DatumOd = DatumOd;
                                    items.get(x).DatumDo = DatumDo;
                                    items.get(x).CijenaPonude = CijenaPonude;
                                    items.get(x).AkcijskaCijena = AkcijskaCijena;
                                    items.get(x).Pakovanje = Pakovanje;
                                } else if (items.get(x).PopustDO < PopustDO && items.get(x).Bransa.length() == Bransa.length()) {
                                    items.get(x).ArtikalID = ArtikalID;
                                    items.get(x).PartnerID = PartnerID;
                                    items.get(x).PotencijalOD = PotencijalOD;
                                    items.get(x).PotencijalDO = PotencijalDO;
                                    items.get(x).Bransa = Bransa;
                                    items.get(x).OsnovnaCijena = OsnovnaCijena;
                                    items.get(x).KljucCijene = KljucCijene;
                                    items.get(x).PopustOD = PopustOD;
                                    items.get(x).PopustDO = PopustDO;
                                    items.get(x).KolicinaOD = KolicinaOD;
                                    items.get(x).KolicinaDo = KolicinaDo;
                                    items.get(x).DodatniPopust = DodatniPopust;
                                    items.get(x).DatumOd = DatumOd;
                                    items.get(x).DatumDo = DatumDo;
                                    items.get(x).CijenaPonude = CijenaPonude;
                                    items.get(x).AkcijskaCijena = AkcijskaCijena;
                                    items.get(x).Pakovanje = Pakovanje;
                                }
                                exists = true;
                                break;
                            }
                        }


                        // CHECK IF PARTNER EXISTS
                        /*for (int x = 0; x < items.size(); x++) {
                            if (items.get(x).PartnerID > 0 && items.get(x).KolicinaOD <= KolicinaOD && (items.get(x).KolicinaDo == 0 || (items.get(x).KolicinaDo >= KolicinaDo && KolicinaDo != 0))) {
                                exists = true;
                                break;
                            }
                        }
                        if (exists) continue;

                        // CHECK FOR POTENTIONS
                        for (int x = 0; x < items.size(); x++) {
                            if (items.get(x).PartnerID == 0 && items.get(x).KolicinaOD <= KolicinaOD && (items.get(x).KolicinaDo == 0 || (items.get(x).KolicinaDo >= KolicinaDo && KolicinaDo != 0))) {
                                if (items.get(x).PopustOD >= PopustOD) exists = true;
                                break;
                            }
                        }*/

                        if (exists) continue;

                        if (Bransa.length() == 0) {
                            for (int x = 0; x < items.size(); x++) {
                                if (items.get(x).PartnerID == 0 && items.get(x).KolicinaOD <= KolicinaOD && (items.get(x).KolicinaDo == 0 || (items.get(x).KolicinaDo >= KolicinaDo && KolicinaDo != 0))) {
                                    exists = true;
                                    break;
                                }
                            }
                        }

                        if (exists) continue;

                        pricelistItem.ArtikalID = ArtikalID;
                        pricelistItem.PartnerID = PartnerID;
                        pricelistItem.PotencijalOD = PotencijalOD;
                        pricelistItem.PotencijalDO = PotencijalDO;
                        pricelistItem.Bransa = Bransa;
                        pricelistItem.OsnovnaCijena = OsnovnaCijena;
                        pricelistItem.KljucCijene = KljucCijene;
                        pricelistItem.PopustOD = PopustOD;
                        pricelistItem.PopustDO = PopustDO;
                        pricelistItem.KolicinaOD = KolicinaOD;
                        pricelistItem.KolicinaDo = KolicinaDo;
                        pricelistItem.DodatniPopust = DodatniPopust;
                        pricelistItem.DatumOd = DatumOd;
                        pricelistItem.DatumDo = DatumDo;
                        pricelistItem.CijenaPonude = CijenaPonude;
                        pricelistItem.AkcijskaCijena = AkcijskaCijena;
                        pricelistItem.Pakovanje = Pakovanje;
                        items.add(pricelistItem);

                    }
                }
            }

            // ADD BASIC AND ACTIONS PRICES
            cur.moveToPosition(-1);

            while (cur.moveToNext()) {

                PricelistItem pricelistItem = new PricelistItem();

                long PartnerID = !cur.isNull(cur.getColumnIndex("PartnerID")) ? cur.getLong(cur.getColumnIndex("PartnerID")) : 0L;
                String Bransa = !cur.isNull(cur.getColumnIndex("Bransa")) ? cur.getString(cur.getColumnIndex("Bransa")) : "";
                String KanalDistribucije = !cur.isNull(cur.getColumnIndex("KanalDistribucije")) ? cur.getString(cur.getColumnIndex("KanalDistribucije")) : "";
                int PotencijalOD = !cur.isNull(cur.getColumnIndex("PotencijalOD")) ? cur.getInt(cur.getColumnIndex("PotencijalOD")) : 0;
                int PotencijalDO = !cur.isNull(cur.getColumnIndex("PotencijalDO")) ? cur.getInt(cur.getColumnIndex("PotencijalDO")) : 0;
                int KljucCijene = !cur.isNull(cur.getColumnIndex("KljucCijene")) ? cur.getInt(cur.getColumnIndex("KljucCijene")) : 0;
                int CijenaPonude = !cur.isNull(cur.getColumnIndex("CijenaPonude")) ? cur.getInt(cur.getColumnIndex("CijenaPonude")) : 0;
                int Pakovanje = !cur.isNull(cur.getColumnIndex("Pakovanje")) ? cur.getInt(cur.getColumnIndex("Pakovanje")) : 0;
                double KolicinaDo = !cur.isNull(cur.getColumnIndex("KolicinaDO")) ? cur.getDouble(cur.getColumnIndex("KolicinaDO")) : 0;
                double KolicinaOD = !cur.isNull(cur.getColumnIndex("KolicinaOD")) ? cur.getDouble(cur.getColumnIndex("KolicinaOD")) : 0;
                double PopustOD = !cur.isNull(cur.getColumnIndex("PopustOD")) ? cur.getDouble(cur.getColumnIndex("PopustOD")) : 0;
                double PopustDO = !cur.isNull(cur.getColumnIndex("PopustDO")) ? cur.getDouble(cur.getColumnIndex("PopustDO")) : 0;
                double DodatniPopust = !cur.isNull(cur.getColumnIndex("DodatniPopust")) ? cur.getDouble(cur.getColumnIndex("DodatniPopust")) : 0;
                double OsnovnaCijena = !cur.isNull(cur.getColumnIndex("OsnovnaCijena")) ? cur.getDouble(cur.getColumnIndex("OsnovnaCijena")) : 0;
                int AkcijskaCijena = !cur.isNull(cur.getColumnIndex("AkcijskaCijena")) ? cur.getInt(cur.getColumnIndex("AkcijskaCijena")) : 0;
                Long DatumOd = !cur.isNull(cur.getColumnIndex("DatumOD")) ? cur.getLong(cur.getColumnIndex("DatumOD")) : 0L;
                Long DatumDo = !cur.isNull(cur.getColumnIndex("DatumDO")) ? cur.getLong(cur.getColumnIndex("DatumDO")) : 0L;


                if (cur.getInt(cur.getColumnIndex("Type")) == 0 && PartnerID == 0 /*&& !priceDefinedForAllByClient*/ && (KanalDistribucije.equals("") || KanalDistribucije.equals(_KanalDistribucije))) {
                    boolean exists = false;

                    for (int x = 0; x < items.size(); x++) {
                        if ((items.get(x).PartnerID == 0 && items.get(x).KolicinaOD <= KolicinaOD && ((items.get(x).KolicinaDo == 0 && items.get(x).PopustOD > PopustOD) || (items.get(x).KolicinaDo >= KolicinaDo && KolicinaDo != 0))) || (items.get(x).PartnerID > 0 && items.get(x).KolicinaOD == KolicinaOD)) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        pricelistItem.ArtikalID = ArtikalID;
                        pricelistItem.PartnerID = PartnerID;
                        pricelistItem.PotencijalOD = PotencijalOD;
                        pricelistItem.PotencijalDO = PotencijalDO;
                        pricelistItem.Bransa = Bransa;
                        pricelistItem.OsnovnaCijena = OsnovnaCijena;
                        pricelistItem.KljucCijene = KljucCijene;
                        pricelistItem.PopustOD = PopustOD;
                        pricelistItem.PopustDO = PopustDO;
                        pricelistItem.KolicinaOD = KolicinaOD;
                        pricelistItem.KolicinaDo = KolicinaDo;
                        pricelistItem.DodatniPopust = DodatniPopust;
                        pricelistItem.DatumOd = DatumOd;
                        pricelistItem.DatumDo = DatumDo;
                        pricelistItem.CijenaPonude = CijenaPonude;
                        pricelistItem.AkcijskaCijena = AkcijskaCijena;
                        pricelistItem.Pakovanje = Pakovanje;
                        items.add(pricelistItem);
                    }
                }

                if (cur.getInt(cur.getColumnIndex("Type")) == 1 && (KanalDistribucije.equals("") || KanalDistribucije.equals(_KanalDistribucije))) { //ACTIONS
                    if (PotencijalOD <= _Potencijal && (PotencijalDO == 0 || PotencijalDO >= _Potencijal)) {
                        if (Bransa.equals("")) { // ONLY POTENTION

                            boolean exists = false;

                            for (int x = 0; x < items.size(); x++) {
                                if ((items.get(x).PartnerID == 0 && items.get(x).KolicinaOD <= KolicinaOD && ((items.get(x).KolicinaDo == 0 && items.get(x).PopustOD > PopustOD) || (items.get(x).KolicinaDo >= KolicinaDo && KolicinaDo != 0))) || (items.get(x).PartnerID > 0 && items.get(x).KolicinaOD == KolicinaOD)) {
                                    exists = true;
                                    break;
                                }
                            }

                            if (exists) continue;

                            pricelistItem.ArtikalID = ArtikalID;
                            pricelistItem.PartnerID = PartnerID;
                            pricelistItem.PotencijalOD = PotencijalOD;
                            pricelistItem.PotencijalDO = PotencijalDO;
                            pricelistItem.Bransa = Bransa;
                            pricelistItem.OsnovnaCijena = OsnovnaCijena;
                            pricelistItem.KljucCijene = KljucCijene;
                            pricelistItem.PopustOD = PopustOD;
                            pricelistItem.PopustDO = PopustDO;
                            pricelistItem.KolicinaOD = KolicinaOD;
                            pricelistItem.KolicinaDo = KolicinaDo;
                            pricelistItem.DodatniPopust = DodatniPopust;
                            pricelistItem.DatumOd = DatumOd;
                            pricelistItem.DatumDo = DatumDo;
                            pricelistItem.CijenaPonude = CijenaPonude;
                            pricelistItem.AkcijskaCijena = AkcijskaCijena;
                            pricelistItem.Pakovanje = Pakovanje;
                            items.add(pricelistItem);

                        } else { // WITH BRANCHES

                            boolean exists = false;

                            // CHECK IF BRANCHES IS IN ARRAY
                            for (int x = 0; x < _Branse.size(); x++) {
                                if (_Branse.get(x).startsWith(Bransa)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) continue;

                            for (int x = 0; x < items.size(); x++) {
                                if (items.get(x).PopustOD < PopustOD && items.get(x).PartnerID == 0 && items.get(x).KolicinaOD >= KolicinaOD && (items.get(x).KolicinaDo == 0 || (items.get(x).KolicinaDo >= KolicinaDo && KolicinaDo != 0))) {
                                    items.get(x).ArtikalID = ArtikalID;
                                    items.get(x).PartnerID = PartnerID;
                                    items.get(x).PotencijalOD = PotencijalOD;
                                    items.get(x).PotencijalDO = PotencijalDO;
                                    items.get(x).Bransa = Bransa;
                                    items.get(x).OsnovnaCijena = OsnovnaCijena;
                                    items.get(x).KljucCijene = KljucCijene;
                                    items.get(x).PopustOD = PopustOD;
                                    items.get(x).PopustDO = PopustDO;
                                    items.get(x).KolicinaOD = KolicinaOD;
                                    items.get(x).KolicinaDo = KolicinaDo;
                                    items.get(x).DodatniPopust = DodatniPopust;
                                    items.get(x).DatumOd = DatumOd;
                                    items.get(x).DatumDo = DatumDo;
                                    items.get(x).CijenaPonude = CijenaPonude;
                                    items.get(x).AkcijskaCijena = AkcijskaCijena;
                                    items.get(x).Pakovanje = Pakovanje;
                                    exists = false;
                                }
                            }

                            if (!exists) continue;

                            pricelistItem.ArtikalID = ArtikalID;
                            pricelistItem.PartnerID = PartnerID;
                            pricelistItem.PotencijalOD = PotencijalOD;
                            pricelistItem.PotencijalDO = PotencijalDO;
                            pricelistItem.Bransa = Bransa;
                            pricelistItem.OsnovnaCijena = OsnovnaCijena;
                            pricelistItem.KljucCijene = KljucCijene;
                            pricelistItem.PopustOD = PopustOD;
                            pricelistItem.PopustDO = PopustDO;
                            pricelistItem.KolicinaOD = KolicinaOD;
                            pricelistItem.KolicinaDo = KolicinaDo;
                            pricelistItem.DodatniPopust = DodatniPopust;
                            pricelistItem.DatumOd = DatumOd;
                            pricelistItem.DatumDo = DatumDo;
                            pricelistItem.CijenaPonude = CijenaPonude;
                            pricelistItem.AkcijskaCijena = AkcijskaCijena;
                            pricelistItem.Pakovanje = Pakovanje;
                            items.add(pricelistItem);
                        }
                    }
                }
            }
            Collections.sort(items, new Common.PriceComparator());
        } catch (Exception ex) {

        }
        return items;
    }

    public static Client GetByID(long id) {

        methodName = "GetByID";

        try {

            final Cursor cur;
            cur = db_readonly.rawQuery("select Clients._id, Clients.ClientID, Clients._clientid, Clients.Latitude, Clients.Longitude, Clients.AccountID, PARTNER.* "
                    + " FROM Clients "
                    + " INNER JOIN PARTNER ON Clients._clientid = PARTNER.ID "
                    + " WHERE Clients._id = " + id, null);

            Client tempClient = null;

            if (cur.moveToFirst()) {
                tempClient = new Client() {{
                    _id = cur.getLong(cur.getColumnIndex("_id"));
                    ClientID = cur.getLong(cur.getColumnIndex("ClientID"));
                    _clientid = cur.getLong(cur.getColumnIndex("_clientid"));
                    AccountID = cur.getInt(cur.getColumnIndex("AccountID"));
                    Name = cur.getString(cur.getColumnIndex("Naziv"));
                    Address = cur.getString(cur.getColumnIndex("Adresa"));
                    Telephone = cur.getString(cur.getColumnIndex("Telefon"));
                    Mobile = cur.getString(cur.getColumnIndex("Mobitel"));
                    Fax = cur.getString(cur.getColumnIndex("Fax"));
                    City = cur.getString(cur.getColumnIndex("Grad"));
                    Description = cur.getString(cur.getColumnIndex("Opis"));
                    Longitude = cur.getLong(cur.getColumnIndex("Longitude"));
                    Latitude = cur.getLong(cur.getColumnIndex("Latitude"));
                    //Sync = cur.getInt(cur.getColumnIndex("Latitude"));
                    WATNumber = cur.getString(cur.getColumnIndex("PorezniBroj"));
                    WATType = cur.getInt(cur.getColumnIndex("VrstaObveznika"));
                    //WebSite = cur.getString(cur.getColumnIndex("Website"));
                    EmailAddress = cur.getString(cur.getColumnIndex("EmailAdresa"));
                    IDNumber = cur.getString(cur.getColumnIndex("IDBroj"));
                    PDVNumber = cur.getString(cur.getColumnIndex("PDVBroj"));
                    Owner = cur.getString(cur.getColumnIndex("Vlasnik"));
                    code = cur.getString(cur.getColumnIndex("Kod"));
                    //DiscountPercentage  = cur.getDouble(cur.getColumnIndex("DiscountPercentage"));
                    //DeliveryDelay  = cur.getInt(cur.getColumnIndex("DeliveryDelay"));
                    //PaymentDelay  = cur.getInt(cur.getColumnIndex("PaymentDelay"));
                    Potencijal = cur.getInt(cur.getColumnIndex("Potencijal"));
                    Veleprodaja = cur.getInt(cur.getColumnIndex("Veleprodaja"));
                    BrzaIsporuka = cur.getInt(cur.getColumnIndex("BrzaIsporuka"));
                }};

                final Cursor c = db_readonly.rawQuery("Select * From ClientPaymentMethods Where ClientID = " + cur.getLong(cur.getColumnIndex("ClientID")), null);
                while (c.moveToNext()) {
                    final Cursor _c = db_readonly.rawQuery("Select * From PaymentMethods Where PaymentMethodID = " + c.getInt(c.getColumnIndex("PaymentMethodID")), null);
                    if (_c.moveToFirst()) {
                        PaymentMethod item = new PaymentMethod() {{
                            PaymentMethodID = c.getInt(c.getColumnIndex("PaymentMethodID"));
                            Name = _c.getString(_c.getColumnIndex("Name"));
                        }};
                        tempClient.PaymentMethods.add(item);
                        _c.close();
                    }
                }
                c.close();


                final Cursor curClientPaymentDates = db_readonly.rawQuery("Select * From ClientPaymentDates Where ClientID = " + cur.getLong(cur.getColumnIndex("ClientID")), null);
                while (curClientPaymentDates.moveToNext()) {
                    final Cursor curPaymentDates = db_readonly.rawQuery("Select * From PaymentDates Where PaymentDateID = " + curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("PaymentDateID")), null);
                    if (curPaymentDates.moveToFirst()) {
                        PaymentDate item = new PaymentDate() {{
                            PaymentDateID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("PaymentDateID"));
                            ProductCategoryID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("ProductCategoryID"));
                            ProductID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("ProductID"));
                            Name = curPaymentDates.getString(curPaymentDates.getColumnIndex("Name"));
                            Description = curPaymentDates.getString(curPaymentDates.getColumnIndex("Description"));
                            Delay = curPaymentDates.getInt(curPaymentDates.getColumnIndex("Delay"));
                        }};
                        tempClient.PaymentDates.add(item);
                        curPaymentDates.close();
                    }
                }
                curClientPaymentDates.close();


            }
            cur.close();
            return tempClient;
        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, "", e);
            return null;
        } finally {

        }
    }

    public static Client GET_Client(long ClientID) {

        methodName = "GetByID";

        try {

            final Cursor cur;
            cur = db_readonly.rawQuery("select Clients._id, Clients.ClientID, Clients._clientid, Clients.Latitude, Clients.Longitude, Clients.AccountID, PARTNER.* "
                    + " FROM Clients "
                    + " INNER JOIN PARTNER ON Clients._clientid = PARTNER.ID "
                    + " WHERE Clients.ClientID = " + ClientID, null);

            Client tempClient = null;

            if (cur.moveToFirst()) {
                tempClient = new Client() {{
                    _id = cur.getLong(cur.getColumnIndex("_id"));
                    ClientID = cur.getLong(cur.getColumnIndex("ClientID"));
                    _clientid = cur.getLong(cur.getColumnIndex("ID"));
                    AccountID = cur.getInt(cur.getColumnIndex("AccountID"));
                    Name = cur.getString(cur.getColumnIndex("Naziv"));
                    Address = cur.getString(cur.getColumnIndex("Adresa"));
                    Telephone = cur.getString(cur.getColumnIndex("Telefon"));
                    Mobile = cur.getString(cur.getColumnIndex("Mobitel"));
                    Fax = cur.getString(cur.getColumnIndex("Fax"));
                    City = cur.getString(cur.getColumnIndex("Grad"));
                    Description = cur.getString(cur.getColumnIndex("Opis"));
                    Longitude = cur.getLong(cur.getColumnIndex("Longitude"));
                    Latitude = cur.getLong(cur.getColumnIndex("Latitude"));
                    //Sync = cur.getInt(cur.getColumnIndex("Latitude"));
                    WATNumber = cur.getString(cur.getColumnIndex("PorezniBroj"));
                    WATType = cur.getInt(cur.getColumnIndex("VrstaObveznika"));
                    //WebSite = cur.getString(cur.getColumnIndex("Website"));
                    EmailAddress = cur.getString(cur.getColumnIndex("EmailAdresa"));
                    IDNumber = cur.getString(cur.getColumnIndex("IDBroj"));
                    PDVNumber = cur.getString(cur.getColumnIndex("PDVBroj"));
                    Owner = cur.getString(cur.getColumnIndex("Vlasnik"));
                    code = cur.getString(cur.getColumnIndex("Kod"));
                    //DiscountPercentage  = cur.getDouble(cur.getColumnIndex("DiscountPercentage"));
                    //DeliveryDelay  = cur.getInt(cur.getColumnIndex("DeliveryDelay"));
                    //PaymentDelay  = cur.getInt(cur.getColumnIndex("PaymentDelay"));
                    Potencijal = cur.getInt(cur.getColumnIndex("Potencijal"));
                    Veleprodaja = cur.getInt(cur.getColumnIndex("Veleprodaja"));
                    BrzaIsporuka = cur.getInt(cur.getColumnIndex("BrzaIsporuka"));
                    KanalDistribucije = cur.getString(cur.getColumnIndex("KanalDistribucije"));
                    _parentid = !cur.isNull(cur.getColumnIndex("ParentID")) ? cur.getLong(cur.getColumnIndex("ParentID")) : 0L;
                }};

                final Cursor c = db_readonly.rawQuery("Select * From ClientPaymentMethods Where ClientID = " + cur.getLong(cur.getColumnIndex("ClientID")), null);
                while (c.moveToNext()) {
                    final Cursor _c = db_readonly.rawQuery("Select * From PaymentMethods Where PaymentMethodID = " + c.getInt(c.getColumnIndex("PaymentMethodID")), null);
                    if (_c.moveToFirst()) {
                        PaymentMethod item = new PaymentMethod() {{
                            PaymentMethodID = c.getInt(c.getColumnIndex("PaymentMethodID"));
                            Name = _c.getString(_c.getColumnIndex("Name"));
                        }};
                        tempClient.PaymentMethods.add(item);
                        _c.close();
                    }
                }
                c.close();


                final Cursor curClientPaymentDates = db_readonly.rawQuery("Select * From ClientPaymentDates Where ClientID = " + cur.getLong(cur.getColumnIndex("ClientID")), null);
                while (curClientPaymentDates.moveToNext()) {
                    final Cursor curPaymentDates = db_readonly.rawQuery("Select * From PaymentDates Where PaymentDateID = " + curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("PaymentDateID")), null);
                    if (curPaymentDates.moveToFirst()) {
                        PaymentDate item = new PaymentDate() {{
                            PaymentDateID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("PaymentDateID"));
                            ProductCategoryID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("ProductCategoryID"));
                            ProductID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("ProductID"));
                            Name = curPaymentDates.getString(curPaymentDates.getColumnIndex("Name"));
                            Description = curPaymentDates.getString(curPaymentDates.getColumnIndex("Description"));
                            Delay = curPaymentDates.getInt(curPaymentDates.getColumnIndex("Delay"));
                        }};
                        tempClient.PaymentDates.add(item);
                        curPaymentDates.close();
                    }
                }
                curClientPaymentDates.close();


            }

            Cursor c = db_readonly.rawQuery("SELECT * FROM PARTNER_BRANSE WHERE PartnerID = " + cur.getLong(cur.getColumnIndex("_clientid")), null);
            while (c.moveToNext()) {
                tempClient.Branse.add(c.getString(c.getColumnIndex("Bransa")));
            }
            c.close();

            cur.close();

            return tempClient;
        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, "", e);
            return null;
        } finally {

        }
    }

    public static Client GET_DeliveryPlace(long DeliveryPlaceID) {

        methodName = "GetByID";

        try {

            final Cursor cur;
            cur = db_readonly.rawQuery("select Clients._id, Clients.ClientID, Clients._clientid, Clients.Latitude, Clients.Longitude, Clients.AccountID, B.* "
                    + " FROM Clients "
                    + " INNER JOIN DeliveryPlaces ON DeliveryPlaces.ClientID = Clients.ClientID "
                    + " INNER JOIN PARTNER A ON Clients._clientid = A.ID "
                    + " INNER JOIN PARTNER B ON DeliveryPlaces._deliveryplaceid = B.ID "
                    + " WHERE DeliveryPlaces.DeliveryPlaceID = " + DeliveryPlaceID, null);

            Client tempClient = null;

            if (cur.moveToFirst()) {
                tempClient = new Client() {{
                    _id = cur.getLong(cur.getColumnIndex("_id"));
                    ClientID = cur.getLong(cur.getColumnIndex("ClientID"));
                    _clientid = cur.getLong(cur.getColumnIndex("ID"));
                    AccountID = cur.getInt(cur.getColumnIndex("AccountID"));
                    Name = cur.getString(cur.getColumnIndex("Naziv"));
                    Address = cur.getString(cur.getColumnIndex("Adresa"));
                    Telephone = cur.getString(cur.getColumnIndex("Telefon"));
                    Mobile = cur.getString(cur.getColumnIndex("Mobitel"));
                    Fax = cur.getString(cur.getColumnIndex("Fax"));
                    City = cur.getString(cur.getColumnIndex("Grad"));
                    Description = cur.getString(cur.getColumnIndex("Opis"));
                    Longitude = cur.getLong(cur.getColumnIndex("Longitude"));
                    Latitude = cur.getLong(cur.getColumnIndex("Latitude"));
                    //Sync = cur.getInt(cur.getColumnIndex("Latitude"));
                    WATNumber = cur.getString(cur.getColumnIndex("PorezniBroj"));
                    WATType = cur.getInt(cur.getColumnIndex("VrstaObveznika"));
                    //WebSite = cur.getString(cur.getColumnIndex("Website"));
                    EmailAddress = cur.getString(cur.getColumnIndex("EmailAdresa"));
                    IDNumber = cur.getString(cur.getColumnIndex("IDBroj"));
                    PDVNumber = cur.getString(cur.getColumnIndex("PDVBroj"));
                    Owner = cur.getString(cur.getColumnIndex("Vlasnik"));
                    code = cur.getString(cur.getColumnIndex("Kod"));
                    //DiscountPercentage  = cur.getDouble(cur.getColumnIndex("DiscountPercentage"));
                    //DeliveryDelay  = cur.getInt(cur.getColumnIndex("DeliveryDelay"));
                    //PaymentDelay  = cur.getInt(cur.getColumnIndex("PaymentDelay"));
                    Potencijal = cur.getInt(cur.getColumnIndex("Potencijal"));
                    Veleprodaja = cur.getInt(cur.getColumnIndex("Veleprodaja"));
                    BrzaIsporuka = cur.getInt(cur.getColumnIndex("BrzaIsporuka"));
                    KanalDistribucije = cur.getString(cur.getColumnIndex("KanalDistribucije"));
                }};

                final Cursor c = db_readonly.rawQuery("Select * From ClientPaymentMethods Where ClientID = " + cur.getLong(cur.getColumnIndex("ClientID")), null);
                while (c.moveToNext()) {
                    final Cursor _c = db_readonly.rawQuery("Select * From PaymentMethods Where PaymentMethodID = " + c.getInt(c.getColumnIndex("PaymentMethodID")), null);
                    if (_c.moveToFirst()) {
                        PaymentMethod item = new PaymentMethod() {{
                            PaymentMethodID = c.getInt(c.getColumnIndex("PaymentMethodID"));
                            Name = _c.getString(_c.getColumnIndex("Name"));
                        }};
                        tempClient.PaymentMethods.add(item);
                        _c.close();
                    }
                }
                c.close();


                final Cursor curClientPaymentDates = db_readonly.rawQuery("Select * From ClientPaymentDates Where ClientID = " + cur.getLong(cur.getColumnIndex("ClientID")), null);
                while (curClientPaymentDates.moveToNext()) {
                    final Cursor curPaymentDates = db_readonly.rawQuery("Select * From PaymentDates Where PaymentDateID = " + curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("PaymentDateID")), null);
                    if (curPaymentDates.moveToFirst()) {
                        PaymentDate item = new PaymentDate() {{
                            PaymentDateID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("PaymentDateID"));
                            ProductCategoryID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("ProductCategoryID"));
                            ProductID = curClientPaymentDates.getInt(curClientPaymentDates.getColumnIndex("ProductID"));
                            Name = curPaymentDates.getString(curPaymentDates.getColumnIndex("Name"));
                            Description = curPaymentDates.getString(curPaymentDates.getColumnIndex("Description"));
                            Delay = curPaymentDates.getInt(curPaymentDates.getColumnIndex("Delay"));
                        }};
                        tempClient.PaymentDates.add(item);
                        curPaymentDates.close();
                    }
                }
                curClientPaymentDates.close();


            }
            cur.close();
            return tempClient;
        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, "", e);
            return null;
        } finally {

        }
    }

    public static Cursor GET_Partner(long PartnerID) {

        Cursor cur = null;

        methodName = "GetByID";

        try {
            cur = db_readonly.rawQuery("select (CASE WHEN DeliveryPlaces.DeliveryPlaceID IS NULL THEN Partner.ID ELSE P.ID END) AS _id, Clients.ClientID, Clients._clientid, "
                    + " (CASE WHEN DeliveryPlaces.DeliveryPlaceID IS NULL THEN 0 ELSE DeliveryPlaces.DeliveryPlaceID END) AS DeliveryPlaceID, "
                    + " (CASE WHEN DeliveryPlaces._deliveryplaceid IS NULL THEN 0 ELSE DeliveryPlaces._deliveryplaceid END) AS _deliveryplaceid, "
                    + " (CASE WHEN DeliveryPlaces.DeliveryPlaceID IS NULL THEN Partner.Naziv ELSE P.Naziv END) AS Name, PARTNER.Adresa AS Address, "
                    + " (CASE WHEN DeliveryPlaces.DeliveryPlaceID IS NULL THEN Partner.Kod ELSE P.Kod END) AS Code, PARTNER.IDBroj, "
                    + " (CASE WHEN DeliveryPlaces.DeliveryPlaceID IS NULL THEN Clients.Latitude ELSE DeliveryPlaces.Latitude END) AS Latitude, "
                    + " (CASE WHEN DeliveryPlaces.DeliveryPlaceID IS NULL THEN Clients.Longitude ELSE DeliveryPlaces.Longitude END) AS Longitude "

                    + " FROM Clients "

                    + " LEFT JOIN PARTNER ON Clients._clientid = PARTNER.ID "

                    + " LEFT JOIN (SELECT * FROM DeliveryPlaces WHERE Active) DeliveryPlaces ON Clients.ClientID = DeliveryPlaces.ClientID "
                    + " LEFT JOIN PARTNER P ON DeliveryPlaces._deliveryplaceid = P.ID "

                    + " WHERE PARTNER.ID =  " + PartnerID + " OR P.ID = " + PartnerID, null);

            cur.getCount();

        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, "", e);
        }

        return cur;
    }

    public static Cursor GET_Partner_Details(long PartnerID) {

        Cursor cur = null;

        methodName = "GET_Partner_Details";

        try {
            cur = db_readonly.rawQuery("SELECT * "
                    + " FROM PARTNER "
                    + " LEFT JOIN PARTNER_DETALJI ON PARTNER.ID = PARTNER_DETALJI.CustomerId "
                    + " WHERE PARTNER.ID =  " + PartnerID, null);

            cur.getCount();

        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, "", e);
        }

        return cur;
    }

    public static Long FIND_Partner(long ID) {

        long PartnerID = 0;

        methodName = "GET_Partner_Details";

        try {
            if (wurthMB.getOrder() != null) {
                if (wurthMB.getOrder().DeliveryPlaceID > 0) {
                    Cursor cur = db_readonly.rawQuery("SELECT PARTNER.ID FROM DeliveryPlaces INNER JOIN PARTNER ON DeliveryPlaces._deliveryplaceid = PARTNER.ID WHERE PARTNER.ParentID IS NOT NULL AND DeliveryPlaceID =  " + wurthMB.getOrder().DeliveryPlaceID, null);
                    if (cur.moveToFirst()) {
                        PartnerID = cur.getLong(0);
                    }
                    cur.close();
                    return PartnerID;
                }

                if (wurthMB.getOrder().ClientID > 0) {
                    Cursor cur = db_readonly.rawQuery("SELECT PARTNER.ID FROM Clients INNER JOIN PARTNER ON Clients._clientid = PARTNER.ID WHERE PARTNER.ParentID IS NOT NULL AND ClientID =  " + wurthMB.getOrder().ClientID, null);
                    if (cur.moveToFirst()) {
                        PartnerID = cur.getLong(0);
                    }
                    cur.close();
                    return PartnerID;
                }
            }

            if (PartnerID == 0) {


            }

        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, "", e);
        }

        return PartnerID;
    }

    public static Cursor GET_Partner_Contacts(long PartnerID) {

        Cursor cur = null;

        methodName = "GET_Partner_Contacts";

        try {
            cur = db_readonly.rawQuery("SELECT * "
                    + " FROM PARTNER_KONTAKTI "
                    + " WHERE PARTNER_KONTAKTI.PartnerID =  " + PartnerID, null);

            cur.getCount();

        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, "", e);
        }

        return cur;
    }

    public static Cursor GET_Partner_Branches(long PartnerID) {

        Cursor cur = null;

        methodName = "GET_Partner_Branches";

        try {
            cur = db_readonly.rawQuery("SELECT * "
                    + " FROM PARTNER_BRANSE "
                    + " WHERE PARTNER_BRANSE.PartnerID =  " + PartnerID, null);

            cur.getCount();

        } catch (Exception e) {
            wurthMB.AddError(className + " " + methodName, "", e);
        }

        return cur;
    }

    public static Cursor GET_VISITS(String searchWord, Long ClientID, Long DeliveryPlaceID, Long VisitDate, Long limit) {

        Cursor cur = null;

        try {

            String sql = "";

            switch (wurthMB.getUser().AccessLevelID) {
                case 1: //Director
                    sql = "";
                    break;
                case 2: //Manager
                    sql = " AND (PARTNER.Region LIKE '" + wurthMB.getUser().Region.substring(0, 2) + "%' OR Visits.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                case 5: //Sales Persons
                case 9: //Sales user (WEB)
                    sql = " AND (Partner.KomercijalistaID = " + wurthMB.getUser()._userid + " OR P.KomercijalistaID = " + wurthMB.getUser()._userid + " OR Visits.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                case 7: //Key Account Manager
                    sql = " AND (C.KAM = " + wurthMB.getUser()._userid + " OR D.KAM = " + wurthMB.getUser()._userid + " OR Visits.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                case 8: //Area sales manager
                    sql = " AND (PARTNER.Region LIKE '" + wurthMB.getUser().Region.substring(0, 4) + "%' OR Visits.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                default:
                    break;
            }

            cur = db_readonly.rawQuery("Select Visits.*, Partner.Naziv As ClientName, Partner.Naziv As DeliveryPlace, Users.Firstname || ' ' || Users.Lastname AS Username  "
                            + " FROM Visits "

                            + " INNER JOIN Clients On Visits.ClientID = Clients.ClientID "
                            + " INNER JOIN Partner On Clients._clientid = Partner.ID "

                            + " LEFT JOIN DeliveryPlaces On Visits.DeliveryPlaceID = DeliveryPlaces.DeliveryPlaceID "
                            + " LEFT JOIN Users On Visits.UserID = Users.UserID "
                            + " LEFT JOIN PARTNER P ON DeliveryPlaces._deliveryplaceid = P.ID "

                            + " LEFT JOIN PARTNER_DETALJI C ON Partner.ID = C.CustomerID "
                            + " LEFT JOIN PARTNER_DETALJI D ON P.ID = D.CustomerID "

                            + " LEFT JOIN KOMERCIJALISTI K1 ON PARTNER.KomercijalistaID = K1.ID "
                            + " LEFT JOIN KOMERCIJALISTI K2 ON P.KomercijalistaID = K2.ID "

                            + " LEFT JOIN KOMERCIJALISTI KAM1 ON C.KAM = KAM1.ID "
                            + " LEFT JOIN KOMERCIJALISTI KAM2 ON D.KAM = KAM2.ID "

                            + " WHERE Visits.AccountID = " + wurthMB.getUser().AccountID
                            // + " AND Visits.UserID = " + wurthMB.getUser().UserID
                            // + " AND Clients.UserID = " + wurthMB.getUser().UserID
                            + " AND (Partner.Naziv Like '%" + searchWord + "%' OR DeliveryPlaces.Name Like '%" + searchWord + "%' OR Users.Firstname || ' ' || Users.Lastname Like '%" + searchWord + "%')"
                            + (ClientID > 0L ? " AND Clients.ClientID = " + ClientID : "")
                            + (DeliveryPlaceID > 0L ? " AND DeliveryPlaces.DeliveryPlaceID = " + DeliveryPlaceID : "")
                            + (VisitDate > 0L ? " AND Visits.startDT >= " + VisitDate + " AND Visits.startDT < " + (VisitDate + 86400000) : "")

                            + sql

                            + " ORDER BY Visits.startDT Desc"

                            + (limit > 0L ? " LIMIT " + limit : "")

                    , null);

            cur.getCount();
        } catch (Exception ex) {
            Log.d("GetAll", ex.getMessage());
        }

        return cur;
    }

    public static Cursor GET_OBJECTS(long ClientID, String searhText) {
        Cursor cur = null;
        try {
            cur = db_readonly.rawQuery("SELECT PARTNER.ID AS _id, PARTNER.ID, Clients.ClientID, PARTNER.Naziv AS Name, PARTNER.Adresa AS Address, PARTNER.Kod AS Code "

                    + " FROM Clients "

                    + " INNER JOIN PARTNER ON Clients._clientid = PARTNER.ParentID "

                    + " WHERE Clients.ClientID = " + ClientID
                    + " AND PARTNER.Naziv like '%" + searhText + "%'"
                    + " And Clients.AccountID = " + wurthMB.getUser().AccountID
                    //+ " And Clients.UserID = " + wurthMB.getUser().UserID
                    + " Order By PARTNER.Naziv", null);
        } catch (Exception e) {

        }
        cur.getCount();
        return cur;
    }

    public static Cursor GET_Diary(String searchText, int SectionID, long date, Long PartnerID, Long limit) {

        Cursor cur = null;

        try {

            String _sql = "";
            String __sql = "";

            switch (wurthMB.getUser().AccessLevelID) {
                case 1: //Director
                    _sql = "";
                    __sql = "";
                    break;
                case 2: //Manager
                    _sql = " AND (A.Region LIKE '" + wurthMB.getUser().Region.substring(0, 2) + "%' OR B.Region LIKE '" + wurthMB.getUser().Region.substring(0, 2) + "%' OR Orders.UserID = " + wurthMB.getUser().UserID + " ) ";
                    __sql = " AND (A.Region LIKE '" + wurthMB.getUser().Region.substring(0, 2) + "%' OR B.Region LIKE '" + wurthMB.getUser().Region.substring(0, 2) + "%' OR Visits.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                case 5: //Sales Persons
                case 9: //Sales user (WEB)
                    _sql = " AND (A.KomercijalistaID = " + wurthMB.getUser()._userid + " OR B.KomercijalistaID = " + wurthMB.getUser()._userid + "  OR Orders.UserID = " + wurthMB.getUser().UserID + " ) ";
                    __sql = " AND (A.KomercijalistaID = " + wurthMB.getUser()._userid + " OR B.KomercijalistaID = " + wurthMB.getUser()._userid + "  OR Visits.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                case 7: //Key Account Manager
                    _sql = " AND (C.KAM = " + wurthMB.getUser()._userid + " OR D.KAM = " + wurthMB.getUser()._userid + " OR Orders.UserID = " + wurthMB.getUser().UserID + " ) ";
                    __sql = " AND (C.KAM = " + wurthMB.getUser()._userid + " OR D.KAM = " + wurthMB.getUser()._userid + " OR Visits.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                case 8: //Area sales manager
                    _sql = " AND (A.Region LIKE '" + wurthMB.getUser().Region.substring(0, 4) + "%' OR B.Region LIKE '" + wurthMB.getUser().Region.substring(0, 4) + "%' OR Orders.UserID = " + wurthMB.getUser().UserID + " ) ";
                    __sql = " AND (A.Region LIKE '" + wurthMB.getUser().Region.substring(0, 4) + "%' OR B.Region LIKE '" + wurthMB.getUser().Region.substring(0, 4) + "%' OR Visits.UserID = " + wurthMB.getUser().UserID + " ) ";
                    break;
                default:
                    break;
            }


            String sql = "";

            if (SectionID == 0 || SectionID == 1) {

                if (!sql.equals("")) sql += " UNION ";

                sql += " SELECT Orders._id AS _id, Orders.OrderID AS ID, 1 AS SectionID, OrderStatusID AS Type, "
                        + " (CASE WHEN A.Naziv IS NULL THEN B.Naziv ELSE A.Naziv END) AS Name, "
                        + " Orders.OrderDate AS Date "

                        + " FROM Orders "

                        + " LEFT JOIN Clients ON Orders.ClientID = Clients.ClientID "
                        + " LEFT JOIN DeliveryPlaces On Orders.DeliveryPlaceID = DeliveryPlaces.DeliveryPlaceID "

                        + " LEFT JOIN Partner A ON Clients._clientid = A.ID "
                        + " LEFT JOIN Partner B ON DeliveryPlaces._deliveryplaceid = B.ID "

                        + " LEFT JOIN PARTNER_DETALJI C ON A.ID = C.CustomerID "
                        + " LEFT JOIN PARTNER_DETALJI D ON B.ID = D.CustomerID "

                        + " LEFT JOIN KOMERCIJALISTI K1 ON A.KomercijalistaID = K1.ID "
                        + " LEFT JOIN KOMERCIJALISTI K2 ON B.KomercijalistaID = K2.ID "

                        + " LEFT JOIN KOMERCIJALISTI KAM1 ON C.KAM = KAM1.ID "
                        + " LEFT JOIN KOMERCIJALISTI KAM2 ON D.KAM = KAM2.ID "

                        + " WHERE Orders.AccountID = " + wurthMB.getUser().AccountID

                        + _sql

                        + (PartnerID > 0 ? " AND (A.ID = " + PartnerID + " OR B.ID = " + PartnerID + ") " : "")
                        + (date > 0L ? " AND Orders.OrderDate >= " + date + " AND Orders.OrderDate < " + (date + 86400000) : "");
            }

            if (SectionID == 0 || SectionID == 9) {

                if (!sql.equals("")) sql += " UNION ";

                sql += " SELECT Visits._id AS _id, Visits.VisitID AS ID, 9 AS SectionID, 0 AS Type, "
                        + " (CASE WHEN A.Naziv IS NULL THEN B.Naziv ELSE A.Naziv END) AS Name, "
                        + "Visits.startDT AS Date " +

                        " FROM Visits "

                        + " LEFT JOIN Clients ON Visits.ClientID = Clients.ClientID "
                        + " LEFT JOIN DeliveryPlaces On Visits.DeliveryPlaceID = DeliveryPlaces.DeliveryPlaceID "

                        + " LEFT JOIN Partner A ON Clients._clientid = A.ID "
                        + " LEFT JOIN Partner B ON DeliveryPlaces._deliveryplaceid = B.ID "

                        + " LEFT JOIN PARTNER_DETALJI C ON A.ID = C.CustomerID "
                        + " LEFT JOIN PARTNER_DETALJI D ON B.ID = D.CustomerID "

                        + " LEFT JOIN KOMERCIJALISTI K1 ON A.KomercijalistaID = K1.ID "
                        + " LEFT JOIN KOMERCIJALISTI K2 ON B.KomercijalistaID = K2.ID "

                        + " LEFT JOIN KOMERCIJALISTI KAM1 ON C.KAM = KAM1.ID "
                        + " LEFT JOIN KOMERCIJALISTI KAM2 ON D.KAM = KAM2.ID "

                        + " WHERE Visits.AccountID = " + wurthMB.getUser().AccountID
                        + " AND (A.Naziv LIKE '%" + searchText + "%' OR B.Naziv LIKE '%" + searchText + "%') "

                        + __sql

                        + (PartnerID > 0 ? " AND (A.ID = " + PartnerID + " OR B.ID = " + PartnerID + ") " : "")
                        + (date > 0L ? " AND Visits.dt >= " + date + " AND Visits.dt < " + (date + 86400000) : "");
            }

            if ((SectionID == 0 || SectionID == 32) && PartnerID == 0) {

                if (!sql.equals("")) sql += " UNION ";

                sql += " SELECT Tasks._id AS _id, Tasks.TaskID AS ID, 32 AS SectionID, 0 AS Type, TaskLog.Parameters AS Name, TaskLog.DOE AS Date " +
                        " FROM TaskLog " +
                        " INNER JOIN Tasks ON TaskLog.TaskID = Tasks.TaskID " +
                        " WHERE TaskLog.UserID = " + wurthMB.getUser().UserID +
                        " AND TaskLog.Parameters LIKE '%" + searchText + "%' " +
                        (date > 0L ? " AND TaskLog.DOE >= " + date + " AND TaskLog.DOE < " + (date + 86400000) : "");
            }

            if ((SectionID == 0 || SectionID == 31) && PartnerID == 0) {

                if (!sql.equals("")) sql += " UNION ";

                sql += " SELECT 0 AS _id, TEMP_ACQUISITION.ID AS ID, 31 AS SectionID, 0 AS Type, TEMP_ACQUISITION.jsonObj AS Name, TEMP_ACQUISITION.DOE AS Date " +
                        " FROM TEMP_ACQUISITION " +
                        " WHERE TEMP_ACQUISITION.UserID = " + wurthMB.getUser().UserID +
                        " AND TEMP_ACQUISITION.OptionID = 31 " +
                        " AND TEMP_ACQUISITION.jsonObj LIKE '%" + searchText + "%' " +
                        (date > 0L ? " AND TEMP_ACQUISITION.DOE >= " + date + " AND TEMP_ACQUISITION.DOE < " + (date + 86400000) : "");
            }

            if ((SectionID == 0 || SectionID == 33) && PartnerID == 0) {

                if (!sql.equals("")) sql += " UNION ";

                sql += " SELECT 0 AS _id, User_Activity_Log.ID AS ID, 33 AS SectionID, 0 AS Type, User_Activity_Log.ItemName AS Name, User_Activity_Log.DOE AS Date " +
                        " FROM User_Activity_Log " +
                        " WHERE User_Activity_Log.UserID = " + wurthMB.getUser().UserID +
                        " AND User_Activity_Log.ItemName LIKE '%" + searchText + "%' " +
                        (date > 0L ? " AND User_Activity_Log.DOE >= " + date + " AND User_Activity_Log.DOE < " + (date + 86400000) : "");
            }

            cur = db_readonly.rawQuery(sql

                            + " ORDER BY Date DESC "

                            + (limit > 0L ? " LIMIT " + limit : "")

                    , null);
        } catch (Exception ex) {
            wurthMB.AddError("GET_Diary", ex.getMessage(), ex);
        }

        cur.getCount();

        return cur;
    }

    public static Cursor GET_User(Long UserID) {

        Cursor cur;
        cur = db_readonly.rawQuery("SELECT Users.* " +
                " FROM Users " +
                " WHERE Users.UserID = ? ", new String[]{Long.toString(UserID)});

        cur.getCount();
        cur.moveToFirst();
        return cur;
    }

    public static Cursor GET_Users_Associated() {

        String region = wurthMB.getUser().Region.length() > 0 ? wurthMB.getUser().Region.substring(0, 1) : "";
        Cursor cur;
        cur = db_readonly.rawQuery("SELECT Users.* " +
                " FROM Users " +
                " INNER JOIN KOMERCIJALISTI ON Users._userid = KOMERCIJALISTI.ID " +
                " WHERE AccountID = ? " +
                " AND UserID > 0 " +
                " AND KOMERCIJALISTI.Region LIKE '" + region + "%' " +
                " ORDER BY Firstname, Lastname ", new String[]{Long.toString(wurthMB.getUser().AccountID)});

        cur.getCount();
        return cur;
    }

}
