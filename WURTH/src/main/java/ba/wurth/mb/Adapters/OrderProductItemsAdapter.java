package ba.wurth.mb.Adapters;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

import ba.wurth.mb.Activities.Products.ProductsActivity;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Order;
import ba.wurth.mb.Classes.Objects.OrderItem;
import ba.wurth.mb.Classes.Objects.PricelistItem;
import ba.wurth.mb.Classes.Objects.ViewHolderPricelist;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Fragments.Orders.OrderItemsFragment;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class OrderProductItemsAdapter extends ArrayAdapter<OrderItem>
{

    private ArrayList<OrderItem> items;
    private Context mContext;
    private java.text.Format dateFormatter = new java.text.SimpleDateFormat("dd.MM.yyyy");

    private Dialog ItemDialog;
    private Fragment mFragment;

    private LayoutInflater mInflater;
    private Client c;
    private boolean extText = false;

    public OrderProductItemsAdapter(Context context, int textViewResourceId, ArrayList<OrderItem> objects, Fragment f)
    {
        super(context, textViewResourceId);
        mFragment = f;
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        items = objects;
        //if (wurthMB.getOrder() != null && wurthMB.getOrder().ClientID > 0L) c = DL_Clients.GetByClientID(wurthMB.getOrder().ClientID);
    }

    @Override
    public int getCount()
    {
        return this.items.size();
    }

    @Override
    public OrderItem getItem(int index)
    {
        try {
            if (index <= getCount()) return this.items.get(index);
            return this.items.get(getCount() - 1);
        }
        catch (Exception e) { }
        return null;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent)
    {
        final ViewHolderPricelist ViewHolderPricelist;
        final OrderItem element = getItem(position);

        if (v == null) {

            v = mInflater.inflate(R.layout.list_row_product, parent, false);

            ViewHolderPricelist = new ViewHolderPricelist();
            ViewHolderPricelist.txbQuantity = (EditText) v.findViewById(R.id.txbQuantity);
            ViewHolderPricelist.litTitle = (TextView) v.findViewById(R.id.litTitle);
            ViewHolderPricelist.litSubTitle = (TextView) v.findViewById(R.id.litSubTitle);
            ViewHolderPricelist.litSupTotal = (TextView) v.findViewById(R.id.litSupTotal);
            ViewHolderPricelist.litSubTotal = (TextView) v.findViewById(R.id.litSubTotal);
            ViewHolderPricelist.litPackage = (TextView) v.findViewById(R.id.litPackage);
            ViewHolderPricelist.image = (ImageView) v.findViewById(R.id.image);
            ViewHolderPricelist.btnNote = (Button) v.findViewById(R.id.btnNote);
            ViewHolderPricelist.btnPricelist = (Button) v.findViewById(R.id.btnPricelist);
            ViewHolderPricelist.btnInfo = (Button) v.findViewById(R.id.btnInfo);
            ViewHolderPricelist.btnSendRequest = (Button) v.findViewById(R.id.btnSendRequest);
            ViewHolderPricelist.btnAvailability = (Button) v.findViewById(R.id.btnAvailability);
            ViewHolderPricelist.progress = (ProgressBar) v.findViewById(R.id.progress);

            ViewHolderPricelist.spPackage = (Spinner) v.findViewById(R.id.spPackage);
            ViewHolderPricelist.position = position;

            ViewHolderPricelist.txbQuantity.removeTextChangedListener(null);
            ViewHolderPricelist.txbQuantity.addTextChangedListener(new textWatcher(ViewHolderPricelist.txbQuantity, ViewHolderPricelist));

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ViewHolderPricelist.txbQuantity.requestFocus();
                }
            });

            ViewHolderPricelist.btnNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final OrderItem mOrderItem = (OrderItem) ViewHolderPricelist.txbQuantity.getTag();
                    final Dialog _ItemDialog = new Dialog(mContext);

                    _ItemDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                    _ItemDialog.setContentView(R.layout.list_row_order_item_note_dialog);
                    _ItemDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    final EditText Note = (EditText) _ItemDialog.findViewById(R.id.txbNote);
                    final CheckBox Special = (CheckBox) _ItemDialog.findViewById(R.id.chk_NegotiatedPrice);

                    Note.setText(mOrderItem.Note);
                    Special.setChecked(mOrderItem.Special);

                    _ItemDialog.findViewById(R.id.btnUpdateOrder).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            java.util.Iterator<ba.wurth.mb.Classes.Objects.OrderItem> itr = wurthMB.getOrder().items.iterator();
                            while (itr.hasNext()) {
                                ba.wurth.mb.Classes.Objects.OrderItem e = itr.next();
                                if (e.ProductID == mOrderItem.ProductID) {
                                    e.Note = Note.getText().toString();
                                    mOrderItem.Note = Note.getText().toString();
                                    mOrderItem.Special = Special.isChecked();
                                    wurthMB.setOrder(wurthMB.getOrder());
                                    if (mFragment instanceof OrderItemsFragment) {
                                        ((OrderItemsFragment) mFragment).bindData();
                                        ((OrderItemsFragment) mFragment).bindTotals();
                                    }
                                    break;
                                }
                            }
                            _ItemDialog.dismiss();
                        }
                    });

                    _ItemDialog.findViewById(R.id.btnCloseDialog).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            _ItemDialog.dismiss();
                        }
                    });

                    _ItemDialog.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            java.util.Iterator<ba.wurth.mb.Classes.Objects.OrderItem> itr = wurthMB.getOrder().items.iterator();
                            while (itr.hasNext()) {
                                ba.wurth.mb.Classes.Objects.OrderItem e = itr.next();
                                if (e.ProductID == mOrderItem.ProductID) {
                                    wurthMB.getOrder().items.remove(e);
                                    wurthMB.getOrder().CalculateTotal();
                                    wurthMB.setOrder(wurthMB.getOrder());
                                    if (mFragment instanceof OrderItemsFragment) {
                                        ((OrderItemsFragment) mFragment).bindData();
                                        ((OrderItemsFragment) mFragment).bindTotals();
                                    }
                                    break;
                                }
                            }
                            _ItemDialog.dismiss();
                        }
                    });

                    if (wurthMB.getOrder() != null && wurthMB.getOrder().Sync == 1 && wurthMB.getOrder().OrderStatusID == 4) {
                        _ItemDialog.findViewById(R.id.btnDelete).setVisibility(View.GONE);
                        _ItemDialog.findViewById(R.id.btnUpdateOrder).setVisibility(View.GONE);
                    }

                    _ItemDialog.show();

                }
            });

            ViewHolderPricelist.btnInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent k = new Intent(mContext, ProductsActivity.class);
                    k.putExtra("ACTION", 0);
                    k.putExtra("ProductID", element.ProductID);
                    k.putExtra("ArtikalID", element.ArtikalID);
                    k.putExtra("Grupa_Artikla", element.Grupa_Artikla);
                    k.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    mContext.startActivity(k);

                }
            });

            ViewHolderPricelist.btnAvailability.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final OrderItem mOrderItem = (OrderItem) ViewHolderPricelist.txbQuantity.getTag();

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) new LongTask(ViewHolderPricelist.litPackage, mOrderItem, ViewHolderPricelist.progress).execute(mOrderItem);
                    else new LongTask(ViewHolderPricelist.litPackage, mOrderItem, ViewHolderPricelist.progress).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mOrderItem);

                }
            });

            ViewHolderPricelist.btnPricelist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {

                        final OrderItem mOrderItem = (OrderItem) ViewHolderPricelist.txbQuantity.getTag();
                        
                        if (wurthMB.getOrder().client == null) return;

                        if (wurthMB.getOrder().Sync == 1 && (wurthMB.getOrder().OrderStatusID == 4 || wurthMB.getOrder().OrderStatusID == 5)) return;

                        if (ItemDialog != null) {
                            ItemDialog.dismiss();
                            ItemDialog = null;
                        }

                        ItemDialog = new Dialog(mContext);
                        ItemDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                        ItemDialog.setContentView(R.layout.list_row_order_item_dialog);
                        ItemDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        ItemDialog.findViewById(R.id.btnUpdateOrder).setVisibility(View.GONE);

                        ((TextView) ItemDialog.findViewById(R.id.lblProductName)).setText(ViewHolderPricelist.litTitle.getText());
                        ((EditText) ItemDialog.findViewById(R.id.txbNote)).setText(mOrderItem.Note);

                        Double price = mOrderItem.Price_WS;

                        if (mOrderItem.KljucCijene == 2) price = price * 100;
                        if (mOrderItem.KljucCijene == 3) price = price * 1000;

                        ((TextView) ItemDialog.findViewById(R.id.lblPrice)).setText(CustomNumberFormat.GenerateFormatCurrency(price) + "/" + mOrderItem.KljucCijene);

                        long _PartnerID = (wurthMB.getOrder() != null && wurthMB.getOrder().client != null) ? wurthMB.getOrder().client._clientid : 0;

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

                                txbDiscount.setText(Integer.toString((int) (mOrderItem.UserDiscountPercentage + mOrderItem.ClientDiscountPercentage)));

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

                                        PriceItem mPriceItem = (PriceItem) view.getTag();

                                        Double _Discount = Double.parseDouble(txbDiscount.getText().toString().replaceAll("[^0-9]", ""));
                                        Double start = Double.parseDouble(litDiscountStart.getText().toString().replaceAll("[^0-9]", ""));
                                        Double end = Common.isNumeric(litDiscountEnd.getText().toString()) ? Double.parseDouble(litDiscountEnd.getText().toString().replaceAll("[^0-9]", "")) : 0D;

                                        if (!(mOrderItem.Quantity * mOrderItem.Pakovanje >= start && (mOrderItem.Quantity * mOrderItem.Pakovanje <= end || end == 0))) {

                                            if (mOrderItem.Quantity * mOrderItem.Pakovanje < start || ((mOrderItem.Quantity * mOrderItem.Pakovanje > end && end != 0D)) || end == 0) {

                                                if (end != 0 && end < mOrderItem.Pakovanje) {
                                                    Notifications.showNotification(mContext, "", mContext.getString(R.string.Notification_PackageMissmatch), 2);
                                                    ItemDialog.dismiss();
                                                    ItemDialog = null;
                                                    return;
                                                }

                                                if (start >= mOrderItem.Pakovanje) mOrderItem.Quantity = Math.ceil(start / mOrderItem.Pakovanje);
                                                else if (end >= mOrderItem.Pakovanje) mOrderItem.Quantity = 1;
                                                else Notifications.showNotification(mContext, "", mContext.getString(R.string.Notification_PackageMissmatch), 2);

                                            }

                                        }

                                        mOrderItem.ClientDiscountPercentage = (Double) litDiscountStart.getTag();
                                        mOrderItem.KljucCijene = mPriceItem.priceKey;

                                        if (mPriceItem.priceKey == 1) mOrderItem.Price_WS = mPriceItem.price;
                                        if (mPriceItem.priceKey == 2) mOrderItem.Price_WS = mPriceItem.price / 100;
                                        if (mPriceItem.priceKey == 3) mOrderItem.Price_WS = mPriceItem.price / 1000;

                                        mOrderItem.Price_RT = mOrderItem.Price_WS;
                                        mOrderItem.UserDiscountPercentage = _Discount - ((Double) litDiscountStart.getTag());

                                        //mOrderItem.Note = ((EditText) ItemDialog.findViewById(R.id.txbNote)).getText().toString();

                                        /*java.util.Iterator<ba.wurth.mb.Classes.Objects.OrderItem> itr = wurthMB.getOrder().items.iterator();
                                        while (itr.hasNext()) {
                                            ba.wurth.mb.Classes.Objects.OrderItem e = itr.next();
                                            if (e.ProductID == mOrderItem.ProductID) {
                                                e.Quantity = mOrderItem.Quantity;
                                                e.ClientDiscountPercentage = mOrderItem.ClientDiscountPercentage;
                                                e.KljucCijene = mOrderItem.KljucCijene;
                                                e.Price_WS = mOrderItem.Price_WS;
                                                e.UserDiscountPercentage = mOrderItem.UserDiscountPercentage;
                                                e.Note = mOrderItem.Note;
                                                break;
                                            }
                                        }*/

                                        wurthMB.getOrder().CalculateTotal();
                                        wurthMB.setOrder(wurthMB.getOrder());

                                        ItemDialog.dismiss();
                                        ItemDialog = null;
                                        if (mFragment instanceof OrderItemsFragment) {
                                            ((OrderItemsFragment) mFragment).bindData();
                                            ((OrderItemsFragment) mFragment).bindTotals();
                                        }
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
                                java.util.Iterator<ba.wurth.mb.Classes.Objects.OrderItem> itr = wurthMB.getOrder().items.iterator();
                                while (itr.hasNext()) {
                                    ba.wurth.mb.Classes.Objects.OrderItem e = itr.next();
                                    if (e.ProductID == mOrderItem.ProductID) {
                                        wurthMB.getOrder().items.remove(e);
                                        wurthMB.getOrder().CalculateTotal();
                                        if (mFragment instanceof OrderItemsFragment) {
                                            ((OrderItemsFragment) mFragment).bindData();
                                            ((OrderItemsFragment) mFragment).bindTotals();
                                        }
                                        break;
                                    }
                                }
                                wurthMB.setOrder(wurthMB.getOrder());
                                ItemDialog.dismiss();
                                ItemDialog = null;
                            }
                        });

                        ItemDialog.show();
                    }
                    catch (Exception ex) {
                        wurthMB.AddError("OrderListItemDialog", ex.getMessage(), ex);
                    }
                }
            });

            ViewHolderPricelist.spPackage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    Double pakovanje = 0D;

                    try {
                        pakovanje = Double.parseDouble(((SpinnerItem) ViewHolderPricelist.spPackage.getSelectedItem()).getValue());
                    }
                    catch (Exception ex) {

                    }

                    OrderItem _mOrderItem = (OrderItem) ViewHolderPricelist.txbQuantity.getTag();

                    if (pakovanje > 0D && pakovanje != _mOrderItem.Pakovanje) {
                        element.Pakovanje = pakovanje;
                        wurthMB.getOrder().setDiscount(element);
                        wurthMB.getOrder().CalculateTotal();
                        _mOrderItem.Pakovanje = pakovanje;
                        ViewHolderPricelist.txbQuantity.setTag(_mOrderItem);

                        if (_mOrderItem.Quantity > 0) {
                            setInfo(_mOrderItem, ViewHolderPricelist);
                        }

                        ((OrderItemsFragment) mFragment).bindTotals();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            v.setTag(ViewHolderPricelist);
        }

        else  ViewHolderPricelist = (ViewHolderPricelist) v.getTag();


        if (position % 2 == 1) v.setBackgroundColor(mContext.getResources().getColor(R.color.altRow));
        else v.setBackgroundColor(mContext.getResources().getColor(R.color.Row));

        ViewHolderPricelist.btnSendRequest.setVisibility(View.GONE);
        ViewHolderPricelist.btnPricelist.setVisibility(View.VISIBLE);

        Cursor mCursor = DL_Wurth.GET_Product(element.ArtikalID);

        if (mCursor != null) {

            if (mCursor.moveToFirst()) {
                String desc = "<strong><font color='#CC0000'>" + mCursor.getString(mCursor.getColumnIndex("sifra")) + "</font></strong><br />" + mCursor.getString(mCursor.getColumnIndex("Naziv"));

                ViewHolderPricelist.litTitle.setText(element.ProductName);
                ViewHolderPricelist.litTitle.setTag(element.ProductID);

                ViewHolderPricelist.litSubTitle.setText(Html.fromHtml(desc));

                if (!mCursor.isNull(mCursor.getColumnIndex("Velika")) && !mCursor.getString(mCursor.getColumnIndex("Velika")).equals("")) {
                    wurthMB.imageLoader.DisplayImage(mCursor.getString(mCursor.getColumnIndex("Velika")), ViewHolderPricelist.image);
                }
                else {
                    ViewHolderPricelist.image.setImageResource(R.drawable.no_image);
                }


                Cursor cur = DL_Wurth.GET_Packages(element.ArtikalID);
                if (cur != null) {

                    int i = 0;
                    SpinnerItem[] items;
                    SpinnerAdapter adapter;
                    items = new SpinnerItem[cur.getCount()];

                    while (cur.moveToNext()) {
                        items[cur.getPosition()] = new SpinnerItem(new Long(cur.getPosition()), " x " + cur.getString(cur.getColumnIndex("Pakovanje")) + " " + mCursor.getString(mCursor.getColumnIndex("MjernaJedinica")), cur.getString(cur.getColumnIndex("Barcode")),cur.getString(cur.getColumnIndex("Pakovanje")));
                        if (element.Pakovanje == cur.getDouble(cur.getColumnIndex("Pakovanje"))) i = cur.getPosition();
                    }

                    adapter = new SpinnerAdapter(mFragment.getActivity(), R.layout.simple_dropdown_item_1line, items);
                    ViewHolderPricelist.spPackage.setAdapter(adapter);
                    ViewHolderPricelist.spPackage.setSelection(i);

                    cur.close();
                }
                setInfo(element, ViewHolderPricelist);
                ViewHolderPricelist.txbQuantity.setTag(element);
            }
        }


        extText = true;
        if (element.Quantity % 1 != 0) ViewHolderPricelist.txbQuantity.setText(Integer.toString((int) element.Quantity));
        else ViewHolderPricelist.txbQuantity.setText(Integer.toString((int) element.Quantity));
        extText = false;

        if (wurthMB.getOrder() != null && wurthMB.getOrder().Sync == 1 && wurthMB.getOrder().OrderStatusID == 4) {
            ViewHolderPricelist.btnAvailability.setVisibility(View.GONE);
            ViewHolderPricelist.btnPricelist.setVisibility(View.GONE);
            ViewHolderPricelist.btnInfo.setVisibility(View.GONE);
            ViewHolderPricelist.spPackage.setEnabled(false);
            //ViewHolderPricelist.btnNote.setVisibility(View.GONE);
            ViewHolderPricelist.btnSendRequest.setVisibility(View.GONE);
        }

        ViewHolderPricelist.txbQuantity.setTag(element);

        return v;
    }

    private class textWatcher implements TextWatcher {
        private View view;
        private ViewHolderPricelist ViewHolderPricelist;

        private textWatcher(View view, ViewHolderPricelist ViewHolderPricelist) {
            this.view = view;
            this.ViewHolderPricelist = ViewHolderPricelist;
        }

        public void afterTextChanged(Editable s) {
            try {

                if (extText) return;

                if (ba.wurth.mb.Classes.Common.isNumeric(((TextView) view).getText().toString())) {

                    final OrderItem mOrderItem = (OrderItem) view.getTag();
                    final Double qty = Double.parseDouble(((TextView) view).getText().toString());

                    if (wurthMB.getOrder() == null) wurthMB.setOrder(new Order());

                    boolean exists = false;

                    java.util.Iterator<OrderItem> itr = wurthMB.getOrder().items.iterator();

                    while (itr.hasNext()) {
                        OrderItem element = itr.next();
                        if (element.ArtikalID == mOrderItem.ArtikalID) {
                            if (qty == 0) {
                                wurthMB.getOrder().items.remove(element);
                            }
                            else {
                                element.Quantity = qty;
                                element.Pakovanje = mOrderItem.Pakovanje;
                                wurthMB.getOrder().setDiscount(element);
                            }
                            wurthMB.getOrder().CalculateTotal();
                            exists = true;
                            break;
                        }
                    }

                    if (!exists && qty > 0 && mOrderItem.ProductID > 0L) {
                        OrderItem tempOrderItem = new OrderItem(){{
                            ArtikalID = mOrderItem.ArtikalID;
                            ProductID = mOrderItem.ProductID;
                            UserDiscountPercentage = mOrderItem.UserDiscountPercentage;
                            Pakovanje = mOrderItem.Pakovanje;
                            Quantity = qty;
                            ClientDiscountPercentage = c != null ? c.DiscountPercentage : 0;
                            Note = "";
                        }};

                        tempOrderItem = wurthMB.getOrder().setDiscount(tempOrderItem);
                        wurthMB.getOrder().items.add(tempOrderItem);
                        wurthMB.getOrder().CalculateTotal();
                    }

                    java.util.Iterator<OrderItem> _itr = wurthMB.getOrder().items.iterator();
                    while (_itr.hasNext()) {
                        OrderItem element = _itr.next();
                        if (element.ArtikalID == mOrderItem.ArtikalID) {
                            mOrderItem.UserDiscountPercentage = element.UserDiscountPercentage;
                            mOrderItem.Price_RT = element.Price_RT;
                            mOrderItem.Price_WS = element.Price_WS;
                            mOrderItem.Total = element.Total;
                            mOrderItem.GrandTotal = element.GrandTotal;
                            mOrderItem.DiscountTotal = element.DiscountTotal;
                            mOrderItem.KljucCijene = element.KljucCijene;
                            mOrderItem.Quantity = element.Quantity;
                            mOrderItem.Note = element.Note;
                            break;
                        }
                    }

                    if (qty > 0D) {
                        setInfo(mOrderItem, ViewHolderPricelist);
                    }
                    else {
                        ViewHolderPricelist.litSupTotal.setText("");
                        ViewHolderPricelist.litSubTotal.setText("");
                        ViewHolderPricelist.litPackage.setText("");
                    }

                    ViewHolderPricelist.txbQuantity.setTag(mOrderItem);
                }

                if (wurthMB.getOrder() != null && wurthMB.getOrder().items.size() == 0) wurthMB.setOrder(null);

                wurthMB.setOrder(wurthMB.getOrder());

                if (mFragment instanceof OrderItemsFragment) {
                    ((OrderItemsFragment) mFragment).bindTotals();
                }

                handler.removeMessages(TRIGGER_SEARCH);
                handler.sendEmptyMessageDelayed(TRIGGER_SEARCH, SEARCH_TRIGGER_DELAY_IN_MS);
            }
            catch (Exception ex) {
                wurthMB.AddError("Add item in product list", ex.getMessage(), ex);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    private void setInfo(OrderItem mOrderItem, ViewHolderPricelist ViewHolderPricelist) {
        try {
            if (wurthMB.getOrder() == null || wurthMB.getOrder().client == null) {
                ViewHolderPricelist.btnPricelist.setVisibility(View.GONE);
                return;
            }

            Double price = mOrderItem.Price_WS;

            if (price == 0D) return;

            if (mOrderItem.KljucCijene == 2) price = price * 100;
            if (mOrderItem.KljucCijene == 3) price = price * 1000;

            price = price - (price * (mOrderItem.UserDiscountPercentage + mOrderItem.ClientDiscountPercentage) / 100);
            ViewHolderPricelist.litSubTotal.setText(CustomNumberFormat.GenerateFormatCurrency(mOrderItem.Total));
            ViewHolderPricelist.litSupTotal.setText(CustomNumberFormat.GenerateFormatCurrency(price) + "/" + mOrderItem.KljucCijene);
        } catch (Exception e) {

        }
    }


    private class PriceItem {
        public double price = 0;
        public double discount = 0;
        public int priceKey = 0;
        public double startQuantity = -1;
        public double endQuantity = -1;
    }

    private class LongTask extends AsyncTask<OrderItem, Void, String> {

        private TextView litPackage;
        private ProgressBar progress;
        private OrderItem orderItem;

        public LongTask(TextView _litPackage, OrderItem _orderItem, ProgressBar _progress) {
            litPackage = _litPackage;
            progress = _progress;
            orderItem = _orderItem;
        }

        @Override
        protected void onPreExecute() {
            if (!((wurthMB) mContext.getApplicationContext()).isNetworkAvailable()) {
                cancel(true);
                Notifications.showNotification(mContext, "", mContext.getString(R.string.NoInternet), 1);
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

                String response = CustomHttpClient.executeHttpPost("http://www.wurth.ba/WS/External.asmx/GetLiveStatus", postParameters).toString();
                return response.replaceAll("\n","").replaceAll("\r", "");
            }
            catch (Exception e) {
                Notifications.showNotification(mContext, "", mContext.getString(R.string.ServiceNotAvailable),1);
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
                Notifications.showNotification(mContext, "", mContext.getString(R.string.ServiceNotAvailable),1);
            }
        }
    }

    private final int TRIGGER_SEARCH = 1;
    private final long SEARCH_TRIGGER_DELAY_IN_MS = 1000;

    private Handler handler = new Handler() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TRIGGER_SEARCH) {
                if (mFragment instanceof OrderItemsFragment) {
                    ((OrderItemsFragment) mFragment).bindData();
                }
            }
        }
    };
}