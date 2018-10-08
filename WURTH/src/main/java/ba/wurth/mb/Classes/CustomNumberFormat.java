package ba.wurth.mb.Classes;

import java.text.NumberFormat;
import java.util.Locale;

public class CustomNumberFormat {
	
	public static String GenerateFormatCurrency(Double value) {
		
		if (value == null) return "";
		
		NumberFormat nf = NumberFormat.getInstance(new Locale("de", "DE"));
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		nf.setGroupingUsed(true);

		String curSymb = "";

		if (wurthMB.getLocale().toString().hashCode() == 3135) curSymb = "KM";
		else curSymb = "";
		
		return nf.format(value) + " " + curSymb;
	}
	
	public static String GenerateFormat(Double value) {
		
		if (value == null) return "";
		
		NumberFormat nf = NumberFormat.getInstance(new Locale("de", "DE"));
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		nf.setGroupingUsed(true);
		
		return nf.format(value);
	}

    public static String GenerateFormatRound(Double value) {

        if (value == null) return "";

        NumberFormat nf = NumberFormat.getInstance(new Locale("de", "DE"));
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        nf.setGroupingUsed(true);

        return nf.format(value);
    }
}
