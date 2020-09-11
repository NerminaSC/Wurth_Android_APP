package ba.wurth.mb.Fragments.Products;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import ba.wurth.mb.Activities.Documents.GalleryImagesActivity;
import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Order;
import ba.wurth.mb.Classes.Objects.OrderItem;
import ba.wurth.mb.Classes.Objects.PricelistItem;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class ProductGeneralFragment extends Fragment {

    private TextView litTitle;
    private TextView litMainTitle;
    private TextView litDesc;
    private TextView litTechDesc;
    private TextView litTotal;
    private TextView litSupTotal;
    private TextView litCode;
    private TextView litPackage;
    private ImageView img;
    private EditText txbQuantity;
    private Spinner spPackage;
    private Button btnPricelist;
    private Button btnAvailability;
    private ProgressBar progress;

    private java.text.Format dateFormatter = new java.text.SimpleDateFormat("dd.MM.yyyy");

    public Long ProductID;
    public Long ArtikalID;

    private OrderItem mOrderItem;
    private Dialog ItemDialog;
    private LayoutInflater mInflater;
    private textWatcher mTextWatcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.product_catalog_item, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            litTitle = (TextView) getView().findViewById(R.id.litTitle);
            litMainTitle = (TextView) getView().findViewById(R.id.litMainTitle);
            litDesc = (TextView) getView().findViewById(R.id.litDesc);
            litTechDesc = (TextView) getView().findViewById(R.id.litTechDesc);
            litSupTotal = (TextView) getView().findViewById(R.id.litSupTotal);
            litTotal = (TextView) getView().findViewById(R.id.litTotal);
            litCode = (TextView) getView().findViewById(R.id.litCode);
            litPackage = (TextView) getView().findViewById(R.id.litPackage);
            img = (ImageView) getView().findViewById(R.id.img);
            txbQuantity = (EditText) getView().findViewById(R.id.txbQuantity);
            spPackage = (Spinner) getView().findViewById(R.id.spPackage);
            btnPricelist = (Button) getView().findViewById(R.id.btnPricelist);
            btnAvailability = (Button) getView().findViewById(R.id.btnAvailability);
            progress = (ProgressBar) getView().findViewById(R.id.progress);

            btnAvailability.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final OrderItem mOrderItem = (OrderItem) txbQuantity.getTag();
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) new NetworkTask(litPackage, mOrderItem, progress).execute(mOrderItem);
                    else new NetworkTask(litPackage, mOrderItem, progress).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mOrderItem);
                }
            });


            btnPricelist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {

                        if (wurthMB.getOrder().client == null) return;

                        if (wurthMB.getOrder().Sync == 1 && (wurthMB.getOrder().OrderStatusID == 4 || wurthMB.getOrder().OrderStatusID == 5)) return;

                        if (ItemDialog != null) {
                            ItemDialog.dismiss();
                            ItemDialog = null;
                        }

                        ItemDialog = new Dialog(getActivity());
                        ItemDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                        ItemDialog.setContentView(R.layout.list_row_order_item_dialog);
                        ItemDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        ItemDialog.findViewById(R.id.btnUpdateOrder).setVisibility(View.GONE);

                        ((TextView) ItemDialog.findViewById(R.id.lblProductName)).setText(litTitle.getText());
                        ((EditText) ItemDialog.findViewById(R.id.txbNote)).setText(mOrderItem.Note);

                        Double price = mOrderItem.Price_WS;

                        if (mOrderItem.KljucCijene == 2) price = price * 100;
                        if (mOrderItem.KljucCijene == 3) price = price * 1000;

                        ((TextView) ItemDialog.findViewById(R.id.lblPrice)).setText(CustomNumberFormat.GenerateFormatCurrency(price) + "/" + mOrderItem.KljucCijene);


                        View extraView = null;

                        ArrayList<PricelistItem> items = DL_Wurth.GET_Pricelist(mOrderItem.ArtikalID);

                        if (items.size() > 0) {

                            View tbl_Header = mInflater.inflate(R.layout.list_row_order_item_dialog_header, (LinearLayout) ItemDialog.findViewById(R.id.llTable), false);
                            ((LinearLayout) ItemDialog.findViewById(R.id.llTable)).addView(tbl_Header);

                            Double partnerKolicinaOD = 0D;

                            for (int x = 0; x < items.size(); x++) {

                                PricelistItem item = items.get(x);

                                String percentage;

                                View tbl = mInflater.inflate(R.layout.list_row_order_item_dialog_item, (LinearLayout) ItemDialog.findViewById(R.id.llTable), false);

                                final TextView litDiscountStart = ((TextView) tbl.findViewById(R.id.litDiscountStart));
                                final TextView litDiscountEnd = ((TextView) tbl.findViewById(R.id.litDiscountEnd));
                                final TextView txbDiscount = ((EditText) tbl.findViewById(R.id.txbDiscount));
                                final TextView litPrice = ((TextView) tbl.findViewById(R.id.litPrice));
                                TextView litStartDate = ((TextView) tbl.findViewById(R.id.litStartDate));
                                TextView litEndDate = ((TextView) tbl.findViewById(R.id.litEndDate));
                                TextView litPercentage = ((TextView) tbl.findViewById(R.id.litPercentage));

                                final Button btnPricelist = ((Button) tbl.findViewById(R.id.btnPricelist));

                                price = item.OsnovnaCijena;
                                Integer KljucCijene = item.KljucCijene;

                                if (price == 0D) return;

                                Double KolicinaOD = item.KolicinaOD;
                                Double KolicinaDO = item.KolicinaDo;

                                litDiscountStart.setText(Integer.toString(KolicinaOD.intValue()));
                                if (KolicinaDO > 0D) litDiscountEnd.setText(Integer.toString(KolicinaDO.intValue()));

                                txbDiscount.setText(Double.toString((double) (mOrderItem.UserDiscountPercentage + mOrderItem.ClientDiscountPercentage)));
                                txbDiscount.setTag(Double.toString(mOrderItem.UserDiscountPercentage + mOrderItem.ClientDiscountPercentage));

                                Double PopustDO = item.PopustDO;
                                Double PopustOD = item.PopustOD;

                                switch (wurthMB.getUser().AccessLevelID) {
                                    case 1: //Director
                                        PopustDO += item.DodatniPopust;
                                        break;
                                    case 2: //Manager
                                        PopustDO += item.DodatniPopust;
                                        break;
                                    case 5: //Sales Persons
                                    case 9: //Sales user (WEB)
                                        //txbDiscount.setEnabled(false);
                                        break;
                                    case 8: //Area sales manager
                                        break;
                                    default:break;
                                }

                                litDiscountStart.setTag(PopustOD);
                                litDiscountEnd.setTag(PopustDO);

                                txbDiscount.setText(Integer.toString(PopustOD.intValue()));
                                txbDiscount.setTag(PopustOD);

                                price = price - (price * PopustOD / 100);

                                litPrice.setText(CustomNumberFormat.GenerateFormatCurrency(price) + "/" + KljucCijene);

                                percentage = Integer.toString(((int) (PopustDO - PopustOD)));

                                PriceItem mPriceItem = new PriceItem();
                                mPriceItem.discount = PopustOD;
                                mPriceItem.price = item.OsnovnaCijena;
                                mPriceItem.priceKey = KljucCijene;
                                mPriceItem.startQuantity = KolicinaOD;
                                mPriceItem.endQuantity = KolicinaDO;
                                btnPricelist.setTag(mPriceItem);


                                txbDiscount.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
                                    @Override
                                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
                                    @Override
                                    public void afterTextChanged(Editable editable) {

                                        try {

                                            if (txbDiscount.getText().toString().equals("")) {
                                                btnPricelist.setEnabled(false);
                                                return;
                                            }

                                            Double _PopustOD = (Double) litDiscountStart.getTag();
                                            Double _PopustDO = (Double) litDiscountEnd.getTag();
                                            Double _Discount = Double.parseDouble(txbDiscount.getText().toString().replaceAll("[^0-9]", ""));

                                            if (_Discount >= _PopustOD && _Discount <= _PopustDO) {

                                                btnPricelist.setEnabled(true);

                                                Double price = mOrderItem.Price_WS;
                                                if (mOrderItem.KljucCijene == 2) price = price * 100;
                                                if (mOrderItem.KljucCijene == 3) price = price * 1000;
                                                price = price - (price * _Discount / 100);

                                                litPrice.setText(CustomNumberFormat.GenerateFormatCurrency(price) + "/" + mOrderItem.KljucCijene);
                                            }
                                            else {
                                                btnPricelist.setEnabled(false);
                                            }
                                        }
                                        catch (Exception ex) {

                                        }
                                    }
                                });

                                btnPricelist.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                       // Double _Discount = Double.parseDouble(txbDiscount.getText().toString().replaceAll("[^0-9]", ""));
                                        Double _Discount = Double.parseDouble(txbDiscount.getTag().toString());
                                        Double start = Double.parseDouble(litDiscountStart.getText().toString().replaceAll("[^0-9]", ""));
                                        Double end = Common.isNumeric(litDiscountEnd.getText().toString()) ? Double.parseDouble(litDiscountEnd.getText().toString().replaceAll("[^0-9]", "")) : 0D;

                                        if (mOrderItem.Quantity * mOrderItem.Pakovanje < start || ((mOrderItem.Quantity * mOrderItem.Pakovanje > end && end != 0D)) || end == 0) {

                                            if (end != 0 && end < mOrderItem.Pakovanje) {
                                                Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.Notification_PackageMissmatch), 2);
                                                ItemDialog.dismiss();
                                                ItemDialog = null;
                                                return;
                                            }

                                            if (start >= mOrderItem.Pakovanje) mOrderItem.Quantity = start / mOrderItem.Pakovanje;
                                            else if (end >= mOrderItem.Pakovanje) mOrderItem.Quantity = 1;
                                            else Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.Notification_PackageMissmatch), 2);

                                        }

                                        mOrderItem.UserDiscountPercentage = _Discount - ((Double) litDiscountStart.getTag());
                                        txbQuantity.setText(Integer.toString((int) mOrderItem.Quantity));

                                        ItemDialog.dismiss();
                                        ItemDialog = null;
                                    }
                                });

                                if (item.PartnerID > 0) {
                                    partnerKolicinaOD = KolicinaOD;
                                    percentage += " (U)";
                                    //extraView = tbl;
                                }

                                if (item.DatumOd > 0L) litStartDate.setText(dateFormatter.format(item.DatumOd));
                                if (item.DatumDo > 0L) litEndDate.setText(dateFormatter.format(item.DatumDo));
                                if (item.DatumOd > 0L && item.DatumDo > 0L) percentage += " (A)";

                                if (KolicinaOD <= (mOrderItem.Quantity * mOrderItem.Pakovanje) && (KolicinaDO > (mOrderItem.Quantity * mOrderItem.Pakovanje) || KolicinaDO == 0D)) {
                                    tbl.setBackgroundColor(Color.parseColor("#ff9f9f"));
                                    txbDiscount.requestFocus();
                                }

                                litPercentage.setText(percentage);
                                //if (partnerKolicinaOD == 0D || partnerKolicinaOD > KolicinaOD) ((LinearLayout) ItemDialog.findViewById(R.id.llTable)).addView(tbl);

                                ((LinearLayout) ItemDialog.findViewById(R.id.llTable)).addView(tbl);

                            }

                            if (extraView != null) ((LinearLayout) ItemDialog.findViewById(R.id.llTable)).addView(extraView);
                        }

                        ItemDialog.findViewById(R.id.btnUpdateOrder).setOnClickListener(new View.OnClickListener() {

                            public void onClick(View v) {
                                ItemDialog.dismiss();
                                ItemDialog = null;
                            }
                        });

                        ItemDialog.findViewById(R.id.btnCloseDialog).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                ItemDialog.dismiss();
                                ItemDialog = null;
                            }
                        });

                        ItemDialog.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                txbQuantity.setText("0");
                                ItemDialog.dismiss();
                                ItemDialog = null;
                            }
                        });

                        ItemDialog.show();

                        ItemDialog.findViewById(R.id.txbNote).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ItemDialog.findViewById(R.id.txbNote).setFocusableInTouchMode(true);
                                ItemDialog.findViewById(R.id.txbNote).setFocusable(true);
                            }
                        });
                    }
                    catch (Exception ex) {
                        wurthMB.AddError("OrderListItemDialog", ex.getMessage(), ex);
                    }                    
                }
            });

            bindData();
        }
        catch (Exception ex) {

        }
    }

    public void bindData() {

        txbQuantity.removeTextChangedListener(mTextWatcher);

        mOrderItem = new OrderItem();
        mOrderItem.ProductID = ProductID;
        mOrderItem.ArtikalID = ArtikalID;
        mOrderItem.UserDiscountPercentage = 0D;
        mOrderItem.ClientDiscountPercentage = 0D;
        mOrderItem.Pakovanje = 0D;
        mOrderItem.Quantity = 0D;

        litPackage.setText("");
        litTotal.setText("");
        litSupTotal.setText("");
        txbQuantity.setTag(null);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) new LongTask().execute(ArtikalID);
        else new LongTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ArtikalID);

    }

    private class LongTask extends AsyncTask<Long,  Void, Cursor> {

        @Override
        protected void onPreExecute() {
            if (getView() != null) getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
        }

        @Override
        protected Cursor doInBackground(Long... params) {

            Cursor cur = null;

            try {
                cur = DL_Wurth.GET_Product(params[0]);
            }
            catch (Exception e) {
                wurthMB.AddError("Product Catalog Item", e.getMessage(), e);
            }

            return cur;

        }

        @Override
        protected void onPostExecute(Cursor cur) {

            try {

                if (cur != null && cur.moveToFirst() && getView() != null) {

                    String Naziv = "";

                    if (!cur.isNull(cur.getColumnIndex("Naziv"))) Naziv = cur.getString(cur.getColumnIndex("Naziv"));

                    if (cur.getInt(cur.getColumnIndex("Status_Artikla")) > 1) {
                        txbQuantity.setVisibility(View.GONE);
                        btnPricelist.setVisibility(View.GONE);
                        spPackage.setVisibility(View.GONE);
                        btnPricelist.setVisibility(View.GONE);

                        if (cur.getInt(cur.getColumnIndex("Status_Artikla")) == 2) Naziv += "<br />" + getString(R.string.Discarded);
                        if (cur.getInt(cur.getColumnIndex("Status_Artikla")) == 4) Naziv += "<br />" + getString(R.string.DiscardedReason);
                        if (cur.getInt(cur.getColumnIndex("Status_Artikla")) == 3) Naziv += "<br />" + getString(R.string.Replaced);
                    }

                    mOrderItem.Mjerna_Jedinica = cur.getString(cur.getColumnIndex("MjernaJedinica"));
                    mOrderItem.ProductName = cur.getString(cur.getColumnIndex("Naziv"));
                    mOrderItem.Grupa_Artikla = cur.getLong(cur.getColumnIndex("Grupa_Artikla"));

                    if (!cur.isNull(cur.getColumnIndex("Stanje_Zaliha"))) mOrderItem.Stanje_Zaliha = cur.getDouble(cur.getColumnIndex("Stanje_Zaliha"));
                    if (!cur.isNull(cur.getColumnIndex("Narucena_Kolicina"))) mOrderItem.Narucena_Kolicina = cur.getDouble(cur.getColumnIndex("Narucena_Kolicina"));
                    if (!cur.isNull(cur.getColumnIndex("Datum_Prijema"))) mOrderItem.Datum_Prijema = cur.getLong(cur.getColumnIndex("Datum_Prijema"));
                    if (!cur.isNull(cur.getColumnIndex("Predefinisana_Dostupnost"))) mOrderItem.Predefinisana_Dostupnost = cur.getInt(cur.getColumnIndex("Predefinisana_Dostupnost"));

                    if (!cur.isNull(cur.getColumnIndex("Pakovanje"))) mOrderItem.Pakovanje = cur.getDouble(cur.getColumnIndex("Pakovanje"));
                    if (!cur.isNull(cur.getColumnIndex("KljucCijene"))) mOrderItem.KljucCijene = cur.getInt(cur.getColumnIndex("KljucCijene"));

                    if (!cur.isNull(cur.getColumnIndex("OsnovnaCijena"))) {
                        Double price =  cur.getDouble(cur.getColumnIndex("OsnovnaCijena"));
                        if (mOrderItem.KljucCijene == 2) price = price / 100;
                        if (mOrderItem.KljucCijene == 3) price = price / 1000;
                        mOrderItem.Price_WS = price;
                    }
                    else {
                        txbQuantity.setVisibility(View.GONE);
                        spPackage.setVisibility(View.GONE);
                    }

                    if (cur.getInt(cur.getColumnIndex("Status_Artikla")) > 1) {
                        txbQuantity.setVisibility(View.GONE);
                        spPackage.setVisibility(View.GONE);
                        btnPricelist.setVisibility(View.GONE);
                    }

                    if (!cur.isNull(cur.getColumnIndex("Zbirni_Naziv"))) litTitle.setText(cur.getString(cur.getColumnIndex("Zbirni_Naziv")) + (cur.isNull(cur.getColumnIndex("Atribut")) ? ", " + cur.getString(cur.getColumnIndex("Atribut")) : ""));
                    else litTitle.setText(cur.getString(cur.getColumnIndex("Naziv")));

                    litMainTitle.setText(Html.fromHtml(Naziv));

                    String data = "";

                    data += "<h1>" + getString(R.string.Description) + "</h1>";
                    if (!cur.isNull(cur.getColumnIndex("Naslov_Opisa"))) data += "<h2>" + cur.getString(cur.getColumnIndex("Naslov_Opisa")) + "</h2>";

                    if (!cur.isNull(cur.getColumnIndex("Opis"))) {

                        data += "<ul style='line-height:20px;'>";

                        String tempString = cur.getString(cur.getColumnIndex("Opis"));

                        for (int i = 0; i < tempString.length(); i++) {
                            if (tempString.charAt(i) == '\n') {
                                data += "<br />";
                            }

                            if (tempString.charAt(i) == '§') {

                                if (i != 0) data += "</li>" ;

                                if (i != tempString.length() - 1) {

                                    if (tempString.charAt(i + 1) != '§') {
                                        data += "<li>";
                                    }
                                    else {
                                        data += "<li style='margin-left:25px;'>";
                                        i++;
                                    }
                                }
                            }

                            if (i == tempString.length() - 1) {
                                data += "</li>";
                            }

                            if (tempString.charAt(i) != '\n' && i != tempString.length() - 1 && tempString.charAt(i) != '§') data += tempString.charAt(i);
                        }
                        data += "</ul>";
                    }


                    data += "<br /><h1>" + getString(R.string.TechDesc) + "</h1>";

                    if (!cur.isNull(cur.getColumnIndex("Tehnicki_Podaci"))) {

                        data += "<table>";

                        String tempString = cur.getString(cur.getColumnIndex("Tehnicki_Podaci"));

                        for (int i = 0; i < tempString.length(); i++) {
                            if (i == 0) {
                                data += "<tr><td style='padding:10px;'>";
                            }

                            if (tempString.charAt(i) == '\n') {
                                data += "</td></tr><tr><td>";
                            }

                            if (tempString.charAt(i) == '§') {
                                data += "</td><td style='padding:10px;'>";
                            }

                            if (i == tempString.length() - 1) {
                                data += "</td></tr>";
                            }

                            if (tempString.charAt(i) != '\n' && i != tempString.length() - 1 && tempString.charAt(i) != '§') data += tempString.charAt(i);
                        }
                        data += "</table>";
                    }

                    ((WebView) getView().findViewById(R.id.wwContent)).loadData(data, "text/html; charset=utf-8", "UTF-8");

                    if (!cur.isNull(cur.getColumnIndex("sifra"))) litCode.setText(cur.getString(cur.getColumnIndex("sifra")));

                    if (!cur.isNull(cur.getColumnIndex("Velika")) && !cur.getString(cur.getColumnIndex("Velika")).equals("")) {
                        wurthMB.imageLoader.DisplayImage(cur.getString(cur.getColumnIndex("Velika")), img);

                        final Long ArtikalID = cur.getLong(cur.getColumnIndex("ID"));

                        img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getActivity(), GalleryImagesActivity.class);
                                i.putExtra("selectedIntex", 0);
                                i.putExtra("OptionID", 4);
                                i.putExtra("_id", ArtikalID);
                                startActivity(i);
                            }
                        });
                    }
                    else {
                        img.setImageResource(R.drawable.no_image);
                    }

                    if (!cur.isNull(cur.getColumnIndex("OsnovnaCijena"))) litSupTotal.setText(CustomNumberFormat.GenerateFormatCurrency(cur.getDouble(cur.getColumnIndex("OsnovnaCijena"))));

                    litPackage.setText("");
                    litTotal.setText("");
                    litSupTotal.setText("");

                    if (wurthMB.getOrder() != null) {
                        java.util.Iterator<OrderItem> itr = wurthMB.getOrder().items.iterator();
                        while (itr.hasNext()) {
                            OrderItem element = itr.next();
                            if (element.ArtikalID == ArtikalID) {
                                mOrderItem = element;
                                break;
                            }
                        }
                    }

                    Cursor cur_Package = DL_Wurth.GET_Packages(cur.getLong(cur.getColumnIndex("ID")));
                    if (cur_Package != null) {

                        int i = 0;
                        SpinnerItem[] items;
                        SpinnerAdapter adapter;
                        items = new SpinnerItem[cur_Package.getCount()];

                        while (cur_Package.moveToNext()) {
                            items[cur_Package.getPosition()] = new SpinnerItem(new Long(cur_Package.getPosition()), " x " + cur_Package.getString(cur_Package.getColumnIndex("Pakovanje")) + " " + cur.getString(cur.getColumnIndex("MjernaJedinica")), cur_Package.getString(cur_Package.getColumnIndex("Barcode")), cur_Package.getString(cur_Package.getColumnIndex("Pakovanje")));
                            if (mOrderItem.Pakovanje == cur_Package.getDouble(cur_Package.getColumnIndex("Pakovanje"))) i = cur_Package.getPosition();
                        }

                        adapter = new SpinnerAdapter(getActivity(), R.layout.simple_dropdown_item_1line, items);
                        spPackage.setAdapter(adapter);
                        spPackage.setSelection(i);

                        spPackage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                try {
                                    txbQuantity.setText(txbQuantity.getText());
                                }
                                catch (Exception ex) {

                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });

                        cur_Package.close();
                    }

                    Cursor _cur = DL_Wurth.GET_ProductDocuments(ArtikalID);

                    if (_cur != null) {

                        LinearLayout llDocuments = (LinearLayout) getView().findViewById(R.id.llDocuments);
                        while (_cur.moveToNext()) {
                            TextView tv = new TextView(getActivity().getApplicationContext());
                            String Name = "";

                            switch (_cur.getInt(_cur.getColumnIndex("TipDokumentaID"))) {
                                case 1: //Manual
                                    Name = getActivity().getString(R.string.Manual) + " " + (_cur.getPosition() + 1 );
                                    break;
                                case 4: //Certificate
                                    Name = getActivity().getString(R.string.Certificate) + " " + (_cur.getPosition() + 1 );
                                    break;
                                case 6: //Video
                                    Name = getActivity().getString(R.string.Video) + " " + (_cur.getPosition() + 1 );
                                    break;

                                default:
                                    break;
                            }

                            tv.setText(Name);
                            tv.setTextSize(getActivity().getResources().getDimension(R.dimen.textSizeSmall));
                            tv.setId(_cur.getPosition());
                            tv.setTextColor(getActivity().getResources().getColor(R.color.module));
                            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                            if (!_cur.isNull(_cur.getColumnIndex("Dokument"))) tv.setTag(_cur.getString(_cur.getColumnIndex("Dokument")));
                            else tv.setTag("");

                            tv.setContentDescription(_cur.getString(_cur.getColumnIndex("TipDokumentaID")));

                            tv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {

                                        if (view.getTag().equals("")) return;

                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) view.getTag()));

                                        //if (view.getContentDescription() != null && view.getContentDescription().equals("1")) intent.setType("application/pdf");
                                        //if (view.getContentDescription() != null && view.getContentDescription().equals("4")) intent.setType("application/pdf");
                                        //if (view.getContentDescription() != null && view.getContentDescription().equals("6")) intent.setType("application/pdf");

                                        PackageManager pm = getActivity().getPackageManager();
                                        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
                                        if (activities.size() > 0) {
                                            startActivity(intent);
                                        } else {
                                            // Do something else here. Maybe pop up a Dialog or Toast
                                        }
                                    }
                                    catch (Exception ex) {

                                    }

                                }
                            });

                            llDocuments.addView(tv);
                        }

                        _cur.close();
                    }

                    txbQuantity.setTag(mOrderItem);
                    cur.close();
                }

                mTextWatcher = new textWatcher(txbQuantity);
                txbQuantity.addTextChangedListener(mTextWatcher);
                txbQuantity.setText(Integer.toString((int) mOrderItem.Quantity));

                getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);

            } catch (Exception e) {
                wurthMB.AddError("Product Catalog Item", e.getMessage(), e);
            }
        }
    }

    private class textWatcher implements TextWatcher {

        private View view;

        private textWatcher(View view) {
            this.view = view;
        }

        public void afterTextChanged(Editable s) {
            try {

                litSupTotal.setText("");
                litTotal.setText("");
                litPackage.setText("");

                if (ba.wurth.mb.Classes.Common.isNumeric(((TextView) view).getText().toString())) {

                    final OrderItem mOrderItem = (OrderItem) view.getTag();
                    final Double qty = Double.parseDouble(((TextView) view).getText().toString());
                    final Double Pakovanje = Double.parseDouble(((SpinnerItem) spPackage.getSelectedItem()).getValue());

                    if (wurthMB.getOrder() == null) wurthMB.setOrder(new Order());

                    boolean exists = false;

                    java.util.Iterator<OrderItem> itr = wurthMB.getOrder().items.iterator();

                    while (itr.hasNext()) {
                        OrderItem element = itr.next();
                        if(element.ArtikalID == mOrderItem.ArtikalID) {
                            if (qty == 0D) {
                                wurthMB.getOrder().items.remove(element);

                                mOrderItem.Pakovanje = Pakovanje;
                                mOrderItem.UserDiscountPercentage = 0D;
                                mOrderItem.ClientDiscountPercentage = 0D;
                                mOrderItem.Price_RT = 0D;
                                mOrderItem.Price_WS = 0D;
                                mOrderItem.Total = 0D;
                                mOrderItem.GrandTotal = 0D;
                                mOrderItem.DiscountTotal = 0D;
                                mOrderItem.Quantity = 0D;

                            }
                            else {

                                element.Quantity = qty;
                                element.Pakovanje = Pakovanje;
                                element = wurthMB.getOrder().setDiscount(element);

                                mOrderItem.Quantity = element.Quantity;
                                mOrderItem.Pakovanje = element.Pakovanje;
                                mOrderItem.ClientDiscountPercentage = element.ClientDiscountPercentage;
                                mOrderItem.UserDiscountPercentage = element.UserDiscountPercentage;
                                mOrderItem.Price_RT = element.Price_RT;
                                mOrderItem.Price_WS = element.Price_WS;
                                mOrderItem.Total = element.Total;
                                mOrderItem.TaxTotal = element.TaxTotal;
                                mOrderItem.GrandTotal = element.GrandTotal;

                            }

                            wurthMB.getOrder().CalculateTotal();
                            exists = true;
                            break;
                        }
                    }

                    if (!exists && qty > 0D && mOrderItem.ArtikalID > 0L ) {

                        mOrderItem.Quantity = qty;
                        mOrderItem.Pakovanje = Pakovanje;

                        OrderItem _mOrderItem = wurthMB.getOrder().setDiscount(mOrderItem);
                        wurthMB.getOrder().items.add(_mOrderItem);

                        mOrderItem.Pakovanje = _mOrderItem.Pakovanje;
                        mOrderItem.ClientDiscountPercentage = _mOrderItem.ClientDiscountPercentage;
                        mOrderItem.UserDiscountPercentage = _mOrderItem.UserDiscountPercentage;
                        mOrderItem.Price_RT = _mOrderItem.Price_RT;
                        mOrderItem.Price_WS = _mOrderItem.Price_WS;
                        mOrderItem.Total = _mOrderItem.Total;
                        mOrderItem.TaxTotal = _mOrderItem.TaxTotal;
                        mOrderItem.GrandTotal = _mOrderItem.GrandTotal;

                        wurthMB.getOrder().CalculateTotal();
                    }

                    txbQuantity.setTag(mOrderItem);

                    Double price = mOrderItem.Price_WS;

                    if (mOrderItem.KljucCijene == 2) price = price * 100;
                    if (mOrderItem.KljucCijene == 3) price = price * 1000;

                    price = price - (price * (mOrderItem.UserDiscountPercentage + mOrderItem.ClientDiscountPercentage) / 100);
                    litTotal.setText(CustomNumberFormat.GenerateFormatCurrency(mOrderItem.Total));
                    litSupTotal.setText(CustomNumberFormat.GenerateFormatCurrency(price) + "/" + mOrderItem.KljucCijene);
                }

                wurthMB.setOrder(wurthMB.getOrder());

                if (wurthMB.getOrder() == null) return;
                if (wurthMB.getOrder() != null && wurthMB.getOrder().client == null){
                    txbQuantity.setVisibility(View.GONE);
                    spPackage.setVisibility(View.GONE);
                    btnPricelist.setVisibility(View.GONE);
                    return;
                }

                if (wurthMB.getOrder().items.size() == 0) return;

            }
            catch (Exception ex) {
                wurthMB.AddError("Add item in product list", ex.getMessage(), ex);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    private void setIn1fo(OrderItem mOrderItem) {

        if (wurthMB.getOrder() == null) return;
        if (wurthMB.getOrder() != null && wurthMB.getOrder().client == null){
            txbQuantity.setVisibility(View.GONE);
            spPackage.setVisibility(View.GONE);
            btnPricelist.setVisibility(View.GONE);
            return;
        }

        if (wurthMB.getOrder().items.size() == 0) return;

        Double price = mOrderItem.Price_WS;

        if (mOrderItem.KljucCijene == 2) price = price * 100;
        if (mOrderItem.KljucCijene == 3) price = price * 1000;

        price = price - (price * (mOrderItem.UserDiscountPercentage + mOrderItem.ClientDiscountPercentage) / 100);
        litTotal.setText(CustomNumberFormat.GenerateFormatCurrency(mOrderItem.Total));
        litSupTotal.setText(CustomNumberFormat.GenerateFormatCurrency(price) + "/" + mOrderItem.KljucCijene);

        /*String delivery = "";
        delivery += "" + getString(R.string.Quantity) + ": " + CustomNumberFormat.GenerateFormatRound(mOrderItem.Pakovanje * mOrderItem.Quantity) + " " + mOrderItem.Mjerna_Jedinica + "<br />";

        if (mOrderItem.Stanje_Zaliha != 0D && mOrderItem.Stanje_Zaliha >= mOrderItem.Quantity * mOrderItem.Pakovanje) {
            delivery += "<font color='#2D9B68'>" + getString(R.string.AvailableNow) + "</font>";
        }
        else if (mOrderItem.Stanje_Zaliha != 0D && mOrderItem.Stanje_Zaliha  == mOrderItem.Quantity * mOrderItem.Pakovanje) {
            delivery += "<font color='#DED84C'>" + getString(R.string.QuantitySameAsStock) + "</font>";
        }
        else {
            long numOfDays = 0L;

            if (mOrderItem.Narucena_Kolicina > 0D && mOrderItem.Datum_Prijema > 0L) {
                long diff = mOrderItem.Datum_Prijema - System.currentTimeMillis();
                numOfDays = diff / (1000*60*60*24);
            }
            else if (mOrderItem.Predefinisana_Dostupnost > 0) {
                numOfDays = mOrderItem.Predefinisana_Dostupnost;
            }
            delivery += "<font color='#FF0000'>" + getString(R.string.QuantityNotAvailable) + " (" + numOfDays + ") </font>";
        }

        litPackage.setText(Html.fromHtml(delivery));*/

    }

    private class PriceItem {
        public double price = 0;
        public double discount = 0;
        public int priceKey = 0;
        public double startQuantity = -1;
        public double endQuantity = -1;
    }

    private class NetworkTask extends AsyncTask<OrderItem, Void, String> {

        private TextView litPackage;
        private ProgressBar progress;
        private OrderItem orderItem;

        public NetworkTask(TextView _litPackage, OrderItem _orderItem, ProgressBar _progress) {
            litPackage = _litPackage;
            progress = _progress;
            orderItem = _orderItem;
        }

        @Override
        protected void onPreExecute() {
            if (!((wurthMB) getActivity().getApplicationContext()).isNetworkAvailable()) {
                cancel(true);
                Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.NoInternet), 1);
                return;
            }
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(OrderItem... params) {
            try {
                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("productId", Long.toString(orderItem.ArtikalID)));
                postParameters.add(new BasicNameValuePair("packaging", Integer.toString((int) orderItem.Pakovanje)));
                postParameters.add(new BasicNameValuePair("userID", Long.toString(wurthMB.getUser()._userid)));
                postParameters.add(new BasicNameValuePair("externalKey", "fd0ac005-6e2d-4ff4-8a2c-7da5070e24f5"));

                String response = CustomHttpClient.executeHttpsPost("https://eshop.wurth.ba/ws/external.asmx/GetLiveStatus", postParameters).toString();
                //String response = CustomHttpClient.executeHttpPost("http://www.wurth.ba/WS/External.asmx/GetLiveStatus", postParameters).toString();

                return response.replaceAll("\n","").replaceAll("\r", "");
            }
            catch (Exception e) {
                Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.ServiceNotAvailable),1);
                return "";
            }
        }

        @Override
        protected void onPostExecute(String params) {
            try {
                litPackage.setText(Html.fromHtml(params));
                progress.setVisibility(View.GONE);
            }
            catch (Exception e) {
                Notifications.showNotification(getActivity(), "", getActivity().getString(R.string.ServiceNotAvailable),1);
            }
        }
    }
}
