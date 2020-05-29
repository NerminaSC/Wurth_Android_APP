package ba.wurth.mb.Classes.Objects;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Date;

import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;

public class Order {
	public long _id = 0;
	public long OrderID = 0;
	public long ClientID = 0;
    public long PartnerID = 0;
    public Client client;
	public long UserID = wurthMB.getUser().UserID;
	public long OrderDate = new Date().getTime();
    public long DeliveryDate = OrderDate;
    public int PaymentMethodID = 0;
	public int OrderStatusID = 0;
	public String PaymentMethodName = "";
	public String OrderStatusName = "";
	public String OrderReference = "";
    public String ClientName = "";
    public String DeliveryPlaceName = "";
	public String Note = "";
	//public long DeliveryDate = new Date().getTime() + (wurthMB.getUser().DeliveryDelay > 0 ? wurthMB.getUser().DeliveryDelay * 86400000 : 86400000);
	public long PaymentDate = new Date().getTime() + (wurthMB.getUser().PaymentDelay > 0 ? wurthMB.getUser().PaymentDelay * 86400000 : 86400000);
	public double Total = 0;
	public double TaxTotal = 0;
	public double DiscountPercentage = 0;
	public double DiscountTotal = 0;
	public double GrandTotal = 0;
	public long Latitude = 0;
	public long Longitude = 0;
	public long DeliveryPlaceID = 0;
	public ArrayList<OrderItem> items = new ArrayList<OrderItem>();
	public int Sync = 0;
	public int Active = 1;
	public int IsDeleted = 0;
	public long AccountID = wurthMB.getUser().AccountID;
    public long VisitID = 0L;
    public long _VisitID = 0L;
    public Visit visit;
	public String Relations = "{}";
	public long DOE = new Date().getTime();
    public int WATType = 1;
	
	public void CalculateTotal () {

        final Order order = this;

        try{
            double _grandTotal = 0;
            double _taxTotal = 0;
            double _discountTotal = 0;
            double _total = 0;

            order.GrandTotal = 0;
            order.TaxTotal = 0;
            order.DiscountTotal = 0;
            order.Total = 0;

            for (OrderItem element: order.items) {

                if(element.Kod_Zbirne_Cjen_Razrade != null && !element.Kod_Zbirne_Cjen_Razrade.equals("")) element = order.setDiscount(element);

                if (wurthMB.getOrder().WATType == 1) {
                    element.Tax = 17.00;
                    element.DiscountTotal = element.Price_WS * (element.DiscountProductPercentage / 100);
                    element.DiscountTotal = element.DiscountTotal + ((element.Price_WS) * (element.DiscountGroupPercentage / 100));
                    element.DiscountTotal = element.DiscountTotal + ((element.Price_WS) * (element.DiscountGroupActionPercentage / 100));
                    element.DiscountTotal = element.DiscountTotal + ((element.Price_WS) * (element.ClientDiscountPercentage / 100));
                    element.DiscountTotal = element.DiscountTotal + ((element.Price_WS) * (element.UserDiscountPercentage / 100));

                    element.DiscountPercentage = Math.round(((1 - ((element.Price_WS - element.DiscountTotal) / element.Price_WS)) * 100) * 100) / 100.00000d;

                    element.Total = element.Quantity * element.Pakovanje * (element.Price_WS - element.DiscountTotal);
                    element.TaxTotal = element.Total * (element.Tax / 100);
                }
                else {
                    element.Tax = 17.00;
                    element.DiscountTotal = element.Price_WS * (element.DiscountProductPercentage / 100);
                    element.DiscountTotal = element.DiscountTotal + ((element.Price_WS) * (element.DiscountGroupPercentage / 100));
                    element.DiscountTotal = element.DiscountTotal + ((element.Price_WS) * (element.DiscountGroupActionPercentage / 100));
                    element.DiscountTotal = element.DiscountTotal + ((element.Price_WS) * (element.ClientDiscountPercentage / 100));
                    element.DiscountTotal = element.DiscountTotal + ((element.Price_WS) * (element.UserDiscountPercentage / 100));

                    element.DiscountPercentage = Math.round(((1 - ((element.Price_WS - element.DiscountTotal) / element.Price_WS)) * 100) * 100) / 100.00000d;

                    element.Total = element.Quantity * element.Pakovanje * (element.Price_WS - element.DiscountTotal);
                    element.TaxTotal = (element.Quantity * element.Pakovanje * element.Price_WS) * (element.Tax / 100);
                }
                element.GrandTotal = element.Total + element.TaxTotal;

                _grandTotal += element.GrandTotal;
                _taxTotal += element.TaxTotal;
                _discountTotal += element.DiscountTotal;
                _total += element.Total;
            }

            order.GrandTotal = _grandTotal;
            order.TaxTotal = _taxTotal;
            order.DiscountTotal = _discountTotal;
            order.Total = _total;

            wurthMB.setOrder(order);

        } catch (Exception e) {
            wurthMB.AddError("Order Calculate Totals", e.getMessage(), e);
        }
	}

    public OrderItem setDiscount(OrderItem element) {
        try{

            ArrayList<PricelistItem> items = DL_Wurth.GET_Pricelist(element.ArtikalID);

            Double price = element.Price_RT;
            Double client_discount = element.ClientDiscountPercentage;
            Double user_discount = element.UserDiscountPercentage;
            int priceKey = element.KljucCijene;

            ArrayList<PriceItem> Prices = new ArrayList<PriceItem>();
            Double Kolicina = element.Quantity * element.Pakovanje;

            if(element.Kod_Zbirne_Cjen_Razrade != null && !element.Kod_Zbirne_Cjen_Razrade.equals("")){
                Double kolicina_zbirne_cjen_razrade = 0D;

                for (OrderItem o_item : wurthMB.getOrder().items) {
                    if(o_item.Kod_Zbirne_Cjen_Razrade != null && o_item.Kod_Zbirne_Cjen_Razrade.equals(element.Kod_Zbirne_Cjen_Razrade) && o_item.ProductID != element.ProductID)
                        kolicina_zbirne_cjen_razrade += o_item.Quantity * o_item.Pakovanje;
                }

                Kolicina += kolicina_zbirne_cjen_razrade;
            }

            for (int x = 0; x < items.size(); x++) {

                PricelistItem item = items.get(x);

                if ((Kolicina >= item.KolicinaOD || item.KolicinaOD == 0) && (Kolicina <= item.KolicinaDo || item.KolicinaDo == 0))
                {
                    PriceItem _item = new PriceItem();
                    _item.priceKey = item.KljucCijene;
                 //   _item.discount = item.PopustOD;
                    _item.discount_from = item.PopustOD;
                    _item.discount_to = item.PopustDO;
                    _item.price = item.OsnovnaCijena;
                    Prices.add(_item);
                }
            }

            if (Prices.size() == 0) {
                if (items.size() > 0) {
                    PricelistItem item = items.get(0);
                    PriceItem _item = new PriceItem();
                    _item.priceKey = item.KljucCijene;
                    //_item.discount = item.PopustOD;
                     _item.discount_from = 0D;
                    _item.price = item.OsnovnaCijena;
                    Prices.add(_item);
                    element.UserDiscountPercentage = 0;
                }
                else {
                    Cursor cur = wurthMB.dbHelper.getDB().rawQuery("SELECT CJENIK.* FROM CJENIK WHERE ArtikalID = " + element.ArtikalID + " LIMIT 1", null);
                    if (cur.moveToFirst()) {
                        PriceItem _item = new PriceItem();
                        _item.priceKey = cur.getInt(cur.getColumnIndex("KljucCijene"));
                        //_item.discount = cur.getDouble(cur.getColumnIndex("PopustOD"));
                        _item.discount_from = 0D;
                        _item.price = cur.getDouble(cur.getColumnIndex("OsnovnaCijena"));
                        Prices.add(_item);
                        element.UserDiscountPercentage = 0;
                    }
                    cur.close();
                }
            }

            for (int i = 0; i < Prices.size(); i++) {

                if ((client_discount + user_discount) <= Prices.get(i).discount_from || ((client_discount + user_discount) > Prices.get(i).discount_to)) {
                    price = Prices.get(i).price;
                    priceKey = Prices.get(i).priceKey;
                    client_discount = Prices.get(i).discount_from;
                    user_discount = 0D;
                    element.KljucCijeneObracunat = false;
                }
            }

            if (priceKey == 2 && !element.KljucCijeneObracunat) price = price / 100;
            if (priceKey == 3 && !element.KljucCijeneObracunat) price = price / 1000;

            element.ClientDiscountPercentage = client_discount;
            element.UserDiscountPercentage = user_discount;
            element.Price_WS = price;
            element.Price_RT = price;
            element.KljucCijene = priceKey;
            element.KljucCijeneObracunat = true;

        } catch (Exception e) {
            wurthMB.AddError("Order Calculate Totals", e.getMessage(), e);
        }

        return element;
    }

    private class PriceItem {
        double price = 0;
       // double discount = 0;
        double discount_from = 0;
        double discount_to = 0;
        int priceKey = 0;
    }
}
