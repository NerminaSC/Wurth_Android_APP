package ba.wurth.mb.DataLayer.Products;

import android.content.ContentValues;
import android.database.Cursor;
import io.requery.android.database.sqlite.SQLiteDatabase;
import io.requery.android.database.sqlite.SQLiteStatement;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.Objects.Document;
import ba.wurth.mb.Classes.Objects.Product;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Documents.DL_Documents;

public class DL_Products {
    private static String methodName = "";
    private static String className = "DL_Products";

    // Database fields
    private static final String DATABASE_TABLE_PRODUCTS = "Products";
    private static final String DATABASE_TABLE_PRICELIST = "Pricelist";
    private static final String DATABASE_TABLE_PRODUCT_CATEGORIES = "ProductCategories";
    private static final String DATABASE_TABLE_PRODUCT_CATEGORY_ASSOCIATIONS = "ProductCategoryAssociations";

    private static SQLiteDatabase db = wurthMB.dbHelper.getDB();
    private static SQLiteDatabase db_readonly = wurthMB.dbHelper.get_db_readonly();

    public static Cursor GetProductCategories(String searchWord) {
        Cursor cur;
        cur = db_readonly.rawQuery("Select COUNT(" + DATABASE_TABLE_PRODUCTS + ".ProductID) AS ProductCount, " + DATABASE_TABLE_PRODUCT_CATEGORIES + "._id, " + DATABASE_TABLE_PRODUCT_CATEGORIES + ".CategoryID, " + DATABASE_TABLE_PRODUCT_CATEGORIES + ".Name "
                + " From " + DATABASE_TABLE_PRODUCT_CATEGORY_ASSOCIATIONS

                + " Inner Join " + DATABASE_TABLE_PRODUCT_CATEGORIES + " On " + DATABASE_TABLE_PRODUCT_CATEGORY_ASSOCIATIONS + ".CategoryID = " + DATABASE_TABLE_PRODUCT_CATEGORIES + ".CategoryID "
                + " Inner Join " + DATABASE_TABLE_PRODUCTS + " On " + DATABASE_TABLE_PRODUCT_CATEGORY_ASSOCIATIONS + ".ProductID = " + DATABASE_TABLE_PRODUCTS + ".ProductID "

                + " Where " + DATABASE_TABLE_PRODUCT_CATEGORIES + ".AccountID = " + wurthMB.getUser().AccountID
                + " And " + DATABASE_TABLE_PRODUCT_CATEGORIES + ".Active = 1 "
                + " And " + DATABASE_TABLE_PRODUCTS + ".Name Like '%" + searchWord + "%' "
                + " And " + DATABASE_TABLE_PRODUCTS + ".Active = 1 "
                + " GROUP BY " + DATABASE_TABLE_PRODUCT_CATEGORIES + "._id "
                + " HAVING Count(Products ._id) > 0 "
                + " Order by " + DATABASE_TABLE_PRODUCT_CATEGORIES + ".Name Asc", null);

        cur.getCount();
        return cur;
    }

    public static Cursor GetProducts(String searchWord) {

        Cursor cur;

        cur = db_readonly.rawQuery("SELECT Products.* "
                + " FROM Products "
                + " INNER JOIN ProductCategoryAssociations On Products.ProductID = ProductCategoryAssociations.ProductID "
                //+ " INNER JOIN ( SELECT * FROM Pricelist WHERE PRICELIST.AccountID = " + wurthMB.getUser().AccountID + ") Pricelist On Products.ProductID = Pricelist.ProductID "
                + " WHERE Products.AccountID = " + wurthMB.getUser().AccountID
                + " AND Products.Name Like '%" + searchWord + "%' "
                + " AND Products.Active = 1 "
                + " ORDER BY Priority, Name", null);

        return cur;
    }

    public static Cursor GetProductsWithPrices(String searchWord, Long CategoryID, Long ProductID) {

        Cursor cur;
        cur = db_readonly.rawQuery("SELECT Products._id, Products.ProductID, Products.Name, Products.Code, Products.UnitsInStock, " +
                        " Pricelist.DiscountPercentage, Pricelist.Price_WS, Pricelist.Price_RT ," +
                        " DiscountGroupProducts.Percentage AS DiscountGroupPercentage, DiscountGroupProducts.PaymentDelay AS DiscountGroupPaymentDelay, DiscountGroupProducts.DeliveryDelay AS DiscountGroupDeliveryDelay, " +
                        " dcp.Discount1 AS ClientDiscount1, dcp.Discount2 AS ClientDiscount2, dcp.Discount3 AS ClientDiscount3, dcp.Discount4 AS ClientDiscount4, dcp.Discount5 AS ClientDiscount5, " +
                        " dgap.Percentage AS DiscountGroupActionPercentage, dgap.PaymentDelay AS DiscountGroupActionPaymentDelay, dgap.DeliveryDelay AS DiscountGroupActionDeliveryDelay " +

                        " FROM ProductCategoryAssociations" +

                        " LEFT Join Products On ProductCategoryAssociations.ProductID = Products.ProductID " +
                        " LEFT Join Pricelist On Products.ProductID = Pricelist.ProductID " +
                        " LEFT JOIN DiscountGroupProducts ON Products.ProductID  = DiscountGroupProducts.ProductID " +
                        " LEFT JOIN (SELECT DiscountGroupID FROM DiscountGroupClients Where ClientID = " + (wurthMB.getOrder() != null ? wurthMB.getOrder().ClientID : 0) + " ) dgc  ON DiscountGroupProducts.DiscountGroupID = dgc.DiscountGroupID " +
                        " LEFT JOIN (SELECT * FROM DiscountClientProducts Where ClientID = " + (wurthMB.getOrder() != null ? wurthMB.getOrder().ClientID : 0) + " ) dcp  ON DiscountGroupProducts.ProductID = dcp.ProductID " +
                        " LEFT JOIN (SELECT * FROM DiscountGroupActionProducts Where DiscountGroupActionID = (Select DiscountGroupActions.DiscountGroupActionID From DiscountGroupActionClients INNER JOIN DiscountGroupActions ON DiscountGroupActionClients.DiscountGroupActionID = DiscountGroupActions.DiscountGroupActionID  Where DiscountGroupActionClients.ClientID = 0  AND DiscountGroupActions.startDate < " + System.currentTimeMillis() + " AND (endDate = 0 OR endDate >= " + System.currentTimeMillis() + ")) ) dgap ON Products.ProductID = dgap.ProductID " +

                        " WHERE Products.AccountID = ? " +
                        " AND Products.Active = ? " +
                        " AND (" + DATABASE_TABLE_PRODUCTS + ".Name LIKE '%" + searchWord + "%' OR " + DATABASE_TABLE_PRODUCTS + ".code Like '%" + searchWord + "%' )" +
                        (CategoryID > 0L ? " AND " + DATABASE_TABLE_PRODUCT_CATEGORY_ASSOCIATIONS + ".CategoryID = " + CategoryID : "") +
                        (ProductID > 0L ? " AND " + DATABASE_TABLE_PRODUCTS + ".ProductID = " + ProductID : "") +
                        " ORDER BY Products.Priority, Products.Name", new String[] { Long.toString(wurthMB.getUser().AccountID), "1"} );

        cur.getCount();
        return cur;
    }

    public static Cursor GetProductsWithPrices_ByCode(String searchWord, Long CategoryID, Long ProductID, String CategoryCode) {

        Cursor cur;
        cur = db_readonly.rawQuery("SELECT Products._id, Products.ProductID, Products.Name, Products.Code, Products.UnitsInStock, " +
                " Pricelist.DiscountPercentage, Pricelist.Price_WS, Pricelist.Price_RT ," +
                " DiscountGroupProducts.Percentage AS DiscountGroupPercentage, DiscountGroupProducts.PaymentDelay AS DiscountGroupPaymentDelay, DiscountGroupProducts.DeliveryDelay AS DiscountGroupDeliveryDelay, " +
                " dcp.Discount1 AS ClientDiscount1, dcp.Discount2 AS ClientDiscount2, dcp.Discount3 AS ClientDiscount3, dcp.Discount4 AS ClientDiscount4, dcp.Discount5 AS ClientDiscount5, " +
                " dgap.Percentage AS DiscountGroupActionPercentage, dgap.PaymentDelay AS DiscountGroupActionPaymentDelay, dgap.DeliveryDelay AS DiscountGroupActionDeliveryDelay " +

                " FROM ProductCategoryAssociations" +

                " LEFT Join Products On ProductCategoryAssociations.ProductID = Products.ProductID " +
                " LEFT Join ARTIKLI On Products._productid = ARTIKLI.ID " +
                " LEFT Join Pricelist On Products.ProductID = Pricelist.ProductID " +
                " LEFT JOIN DiscountGroupProducts ON Products.ProductID  = DiscountGroupProducts.ProductID " +
                " LEFT JOIN (SELECT DiscountGroupID FROM DiscountGroupClients Where ClientID = " + (wurthMB.getOrder() != null ? wurthMB.getOrder().ClientID : 0) + " ) dgc  ON DiscountGroupProducts.DiscountGroupID = dgc.DiscountGroupID " +
                " LEFT JOIN (SELECT * FROM DiscountClientProducts Where ClientID = " + (wurthMB.getOrder() != null ? wurthMB.getOrder().ClientID : 0) + " ) dcp  ON DiscountGroupProducts.ProductID = dcp.ProductID " +
                " LEFT JOIN (SELECT * FROM DiscountGroupActionProducts Where DiscountGroupActionID = (Select DiscountGroupActions.DiscountGroupActionID From DiscountGroupActionClients INNER JOIN DiscountGroupActions ON DiscountGroupActionClients.DiscountGroupActionID = DiscountGroupActions.DiscountGroupActionID  Where DiscountGroupActionClients.ClientID = 0  AND DiscountGroupActions.startDate < " + System.currentTimeMillis() + " AND (endDate = 0 OR endDate >= " + System.currentTimeMillis() + ")) ) dgap ON Products.ProductID = dgap.ProductID " +

                " WHERE Products.AccountID = ? " +
                " AND Products.Active = ? " +
                " AND ARTIKLI.Kod_Zbirnog_Naziva = '" + CategoryCode + "' " +
                " AND (" + DATABASE_TABLE_PRODUCTS + ".Name LIKE '%" + searchWord + "%' OR " + DATABASE_TABLE_PRODUCTS + ".code Like '%" + searchWord + "%' )" +
                (CategoryID > 0L ? " AND " + DATABASE_TABLE_PRODUCT_CATEGORY_ASSOCIATIONS + ".CategoryID = " + CategoryID : "") +
                (ProductID > 0L ? " AND " + DATABASE_TABLE_PRODUCTS + ".ProductID = " + ProductID : "") +
                " ORDER BY Products.Priority, Products.Name", new String[] { Long.toString(wurthMB.getUser().AccountID), "1"} );

        cur.getCount();
        return cur;
    }

    public static long GetIDByLongID(long _id) {
        long ProductID = 0;

        try {
            Cursor _cur = db_readonly.rawQuery("Select ProductID From Products Where _id = " + _id, null);

            if (_cur.moveToFirst()) ProductID = _cur.getLong(0);

            if (_cur != null) _cur.close();
        } catch (Exception e) {

        }
        return ProductID;
    }

    public static Product GetByID(long _id) {
        final Cursor cur;
        Product tempProduct = null;

        try {

            cur = db_readonly.rawQuery("select * from " + DATABASE_TABLE_PRODUCTS + " where _id = " + _id, null);

            if (cur.moveToFirst()) {
                tempProduct = new Product() {{
                    _id = cur.getLong(cur.getColumnIndex("_id"));
                    ProductID = cur.getLong(cur.getColumnIndex("ProductID"));
                    UOMID = cur.getInt(cur.getColumnIndex("UOMID"));
                    Name = cur.getString(cur.getColumnIndex("Name"));
                    Code = cur.getString(cur.getColumnIndex("Code"));
                    Description = cur.getString(cur.getColumnIndex("Description"));
                    Content = cur.getString(cur.getColumnIndex("Content"));
                    UnitsInStock = cur.getInt(cur.getColumnIndex("UnitsInStock"));
                }};
            }
            cur.close();

            if (tempProduct != null && tempProduct.ProductID > 0) {

                Cursor _cur = db_readonly.rawQuery("select _id from Documents where ItemID = " + tempProduct.ProductID + " AND OptionID = 4 AND Active = 1 ORDER BY Name", null);

                if (_cur != null) {
                    while (_cur.moveToNext()) {
                        Document d = DL_Documents.GetByID(_cur.getLong(0));
                        if (d != null) tempProduct.documents.add(d);
                    }
                }

                _cur = db_readonly.rawQuery("select * from " + DATABASE_TABLE_PRICELIST + " where ProductID = " + tempProduct.ProductID, null);

                if (_cur != null && _cur.moveToFirst()) {
                    tempProduct.PriceList.DeliveryDelay = _cur.getInt(_cur.getColumnIndex("DeliveryDelay"));
                    tempProduct.PriceList.DiscountPerecentage = _cur.getDouble(_cur.getColumnIndex("DiscountPercentage"));

                    tempProduct.PriceList.Price_RT = _cur.getDouble(_cur.getColumnIndex("Price_RT"));
                    tempProduct.PriceList.Price_WS = _cur.getDouble(_cur.getColumnIndex("Price_WS"));
                    tempProduct.PriceList.PriceDate = _cur.getLong(_cur.getColumnIndex("PriceDate"));

                    tempProduct.PriceList.RT_Base = _cur.getDouble(_cur.getColumnIndex("RT_Base"));
                    tempProduct.PriceList.RT_TaxID = _cur.getInt(_cur.getColumnIndex("RT_TaxID"));
                    tempProduct.PriceList.RT_TaxValue = _cur.getDouble(_cur.getColumnIndex("RT_TaxValue"));
                    tempProduct.PriceList.WS_Base = _cur.getDouble(_cur.getColumnIndex("WS_Base"));
                    tempProduct.PriceList.WS_TaxID = _cur.getInt(_cur.getColumnIndex("WS_TaxID"));
                    tempProduct.PriceList.WS_TaxValue = _cur.getDouble(_cur.getColumnIndex("WS_TaxValue"));
                }

                _cur = db_readonly.rawQuery("select * from DiscountClientProducts where ProductID = " + tempProduct.ProductID + " AND ClientID = " + (wurthMB.getClient() != null ? wurthMB.getClient().ClientID : 0), null);
                if (_cur != null && _cur.moveToFirst()) {
                    tempProduct.PriceList.ClientDiscount1 = _cur.getDouble(_cur.getColumnIndex("Discount1"));
                    tempProduct.PriceList.ClientDiscount2 = _cur.getDouble(_cur.getColumnIndex("Discount2"));
                    tempProduct.PriceList.ClientDiscount3 = _cur.getDouble(_cur.getColumnIndex("Discount3"));
                    tempProduct.PriceList.ClientDiscount4 = _cur.getDouble(_cur.getColumnIndex("Discount4"));
                    tempProduct.PriceList.ClientDiscount5 = _cur.getDouble(_cur.getColumnIndex("Discount5"));
                }

                _cur = db_readonly.rawQuery("select Name From UOM Where UOMID=?", new String[]{Integer.toString(tempProduct.UOMID)});

                if (_cur != null && _cur.moveToFirst()) tempProduct.UOMName = _cur.getString(0);

                if (_cur != null) _cur.close();
            }
        } catch (Exception e) {
        }
        return tempProduct;
    }

    public static Product GetByBarcode(String Barcode) {

        final Cursor cur;

        Product tempProduct = null;

        try {

            cur = db_readonly.rawQuery("select * from " + DATABASE_TABLE_PRODUCTS + " WHERE Barcode = '" + Barcode + "' AND AccountID = " + wurthMB.getUser().AccountID + " LIMIT 1", null);

            if (cur.moveToFirst()) {
                tempProduct = new Product() {{
                    _id = cur.getLong(cur.getColumnIndex("_id"));
                    ProductID = cur.getLong(cur.getColumnIndex("ProductID"));
                    UOMID = cur.getInt(cur.getColumnIndex("UOMID"));
                    Name = cur.getString(cur.getColumnIndex("Name"));
                    Code = cur.getString(cur.getColumnIndex("Code"));
                    Description = cur.getString(cur.getColumnIndex("Description"));
                    Content = cur.getString(cur.getColumnIndex("Content"));
                    UnitsInStock = cur.getInt(cur.getColumnIndex("UnitsInStock"));
                }};
            }
            cur.close();

            if (tempProduct != null && tempProduct.ProductID > 0) {

                Cursor _cur = db_readonly.rawQuery("select _id from Documents where _itemid = " + tempProduct._id + " AND OptionID = 4 AND Active = 1", null);

                if (_cur != null) {
                    while (_cur.moveToNext()) {
                        Document d = DL_Documents.GetByID(_cur.getLong(0));
                        if (d != null) tempProduct.documents.add(d);
                    }
                    _cur.close();
                }


                _cur = db_readonly.rawQuery("select * from " + DATABASE_TABLE_PRICELIST + " where ProductID = " + tempProduct.ProductID, null);

                if (_cur != null && _cur.moveToFirst()) {
                    tempProduct.PriceList.DeliveryDelay = _cur.getInt(_cur.getColumnIndex("DeliveryDelay"));
                    tempProduct.PriceList.DiscountPerecentage = _cur.getDouble(_cur.getColumnIndex("DiscountPercentage"));

                    tempProduct.PriceList.Price_RT = _cur.getDouble(_cur.getColumnIndex("Price_RT"));
                    tempProduct.PriceList.Price_WS = _cur.getDouble(_cur.getColumnIndex("Price_WS"));
                    tempProduct.PriceList.PriceDate = _cur.getLong(_cur.getColumnIndex("PriceDate"));

                    tempProduct.PriceList.RT_Base = _cur.getDouble(_cur.getColumnIndex("RT_Base"));
                    tempProduct.PriceList.RT_TaxID = _cur.getInt(_cur.getColumnIndex("RT_TaxID"));
                    tempProduct.PriceList.RT_TaxValue = _cur.getDouble(_cur.getColumnIndex("RT_TaxValue"));
                    tempProduct.PriceList.WS_Base = _cur.getDouble(_cur.getColumnIndex("WS_Base"));
                    tempProduct.PriceList.WS_TaxID = _cur.getInt(_cur.getColumnIndex("WS_TaxID"));
                    tempProduct.PriceList.WS_TaxValue = _cur.getDouble(_cur.getColumnIndex("WS_TaxValue"));
                    _cur.close();
                }

                _cur = db_readonly.rawQuery("select * from DiscountClientProducts where ProductID = " + tempProduct.ProductID + " AND ClientID = " + (wurthMB.getClient() != null ? wurthMB.getClient().ClientID : 0), null);
                if (_cur != null && _cur.moveToFirst()) {
                    tempProduct.PriceList.ClientDiscount1 = _cur.getDouble(_cur.getColumnIndex("Discount1"));
                    tempProduct.PriceList.ClientDiscount2 = _cur.getDouble(_cur.getColumnIndex("Discount2"));
                    tempProduct.PriceList.ClientDiscount3 = _cur.getDouble(_cur.getColumnIndex("Discount3"));
                    tempProduct.PriceList.ClientDiscount4 = _cur.getDouble(_cur.getColumnIndex("Discount4"));
                    tempProduct.PriceList.ClientDiscount5 = _cur.getDouble(_cur.getColumnIndex("Discount5"));
                    _cur.close();
                }

                _cur = db_readonly.rawQuery("select Name From UOM Where UOMID=?", new String[]{Integer.toString(tempProduct.UOMID)});

                if (_cur != null && _cur.moveToFirst()) {
                    tempProduct.UOMName = _cur.getString(0);
                    _cur.close();
                }
            }
        } catch (Exception e) {
        }
        return tempProduct;
    }
}
