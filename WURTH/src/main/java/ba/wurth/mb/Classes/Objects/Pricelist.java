package ba.wurth.mb.Classes.Objects;

import java.util.Date;

public class Pricelist {
	public long PriceDate = new Date().getTime();
	public double Price_WS = 0;
	public double Price_RT = 0;
	public double RT_Base = 0;
	public int RT_TaxID = 0;
	public double RT_TaxValue = 0;
	public double WS_Base = 0;
	public int WS_TaxID = 0;
	public double WS_TaxValue = 0;
	
	public double DiscountPerecentage = 0;
	public int PaymentDelay = 0;
	public int DeliveryDelay = 0;
	
	public double DiscountGroupPercentage = 0;
	public int DiscountGroupPaymentDelay = 0;
	public int DiscountGroupDeliveryDelay = 0;

	public double DiscountGroupActionPercentage = 0;
	public int DiscountGroupActionPaymentDelay = 0;
	public int DiscountGroupActionDeliveryDelay = 0;

	public double ClientDiscount1 = 0;
	public double ClientDiscount2 = 0;
	public double ClientDiscount3 = 0;
	public double ClientDiscount4 = 0;
	public double ClientDiscount5 = 0;

}
