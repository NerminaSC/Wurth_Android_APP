package ba.wurth.mb.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;

import androidx.cursoradapter.widget.CursorAdapter;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

import ba.wurth.mb.Activities.Products.ProductActivity;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.CustomHttpClient;
import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Order;
import ba.wurth.mb.Classes.Objects.OrderItem;
import ba.wurth.mb.Classes.Objects.PricelistItem;
import ba.wurth.mb.Classes.Objects.Temp_Acquisition;
import ba.wurth.mb.Classes.Objects.ViewHolderPricelist;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Temp.DL_Temp;
import ba.wurth.mb.Fragments.Orders.OrderItemsFragment;
import ba.wurth.mb.Fragments.Products.ProductsListFragment;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class ProductsAdapter extends CursorAdapter {
    LayoutInflater mInflater;
    private int mArtikalID;
    private int mNaziv;
    private int mSifra;
    //private int mOsnovnaCijena;
    private int mStanje_Zaliha;
    private int mProductID;
    private int mAtribut;
    private int mMjernaJedinica;
    private int mStatus_Artikla;
    private int mStatus_Prezentacije_Artikla;

    private int mKod_Zbirne_Cjen_Razrade;

    private int mDatum_Prijema;
    private int mPredefinisana_Dostupnost;
    private int mNarucena_Kolicina;

    private int mZbirni_Naziv;
    private int mGrupniNaziv;

    private boolean init = false;
    private boolean extText = false;

    private Context mContext;
    private Fragment mFragment;
    private Cursor mCursor;

    private Dialog ItemDialog;
    private java.text.Format dateFormatter = new java.text.SimpleDateFormat("dd.MM.yyyy");

    private LayoutInflater li;

    private Order o;

    public ProductsAdapter(Context context, Cursor cursor, Fragment f) {
        super(context, cursor);

        mFragment = f;

        mInflater = LayoutInflater.from(context);

        mArtikalID = cursor.getColumnIndex("ID");
        mNaziv = cursor.getColumnIndex("Naziv");
        mSifra = cursor.getColumnIndex("sifra");
        mAtribut = cursor.getColumnIndex("Atribut");
        //mOsnovnaCijena = cursor.getColumnIndex("OsnovnaCijena");
        mProductID = cursor.getColumnIndex("ProductID");
        mMjernaJedinica = cursor.getColumnIndex("MjernaJedinica");
        mKod_Zbirne_Cjen_Razrade = cursor.getColumnIndex("Kod_Zbirne_Cjen_Razrade");
        mStatus_Artikla = cursor.getColumnIndex("Status_Artikla");
        mStatus_Prezentacije_Artikla = cursor.getColumnIndex("Status_Prezentacije_Artikla");
        mZbirni_Naziv = cursor.getColumnIndex("Zbirni_Naziv");
        mGrupniNaziv = cursor.getColumnIndex("GrupniNaziv");

        mStanje_Zaliha = cursor.getColumnIndex("Stanje_Zaliha");
        mDatum_Prijema = cursor.getColumnIndex("Datum_Prijema");
        mPredefinisana_Dostupnost = cursor.getColumnIndex("Predefinisana_Dostupnost");
        mNarucena_Kolicina = cursor.getColumnIndex("Narucena_Kolicina");

        mContext = context;
        mCursor = cursor;

        li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        o = wurthMB.getOrder();
    }

    @Override
    public void changeCursor(Cursor cursor) {
        mCursor = cursor;
        if (cursor != null && !cursor.isClosed()) super.changeCursor(cursor);
    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {

        if (mCursor == null || mCursor.getCount() == 0) return v;

        final ViewHolderPricelist viewHolder;

        mCursor.moveToPosition(position);

        if (v == null) {
            v = li.inflate(R.layout.list_row_product, parent, false);

            viewHolder = new ViewHolderPricelist();
            viewHolder.txbQuantity = (EditText) v.findViewById(R.id.txbQuantity);
            viewHolder.litTitle = (TextView) v.findViewById(R.id.litTitle);
            viewHolder.litSubTitle = (TextView) v.findViewById(R.id.litSubTitle);
            viewHolder.litSupTotal = (TextView) v.findViewById(R.id.litSupTotal);
            viewHolder.litSubTotal = (TextView) v.findViewById(R.id.litSubTotal);
            viewHolder.litPackage = (TextView) v.findViewById(R.id.litPackage);
            viewHolder.image = (ImageView) v.findViewById(R.id.image);
            viewHolder.btnNote = (Button) v.findViewById(R.id.btnNote);
            viewHolder.btnPricelist = (Button) v.findViewById(R.id.btnPricelist);
            viewHolder.btnInfo = (Button) v.findViewById(R.id.btnInfo);
            viewHolder.btnSendRequest = (Button) v.findViewById(R.id.btnSendRequest);
            viewHolder.btnAvailability = (Button) v.findViewById(R.id.btnAvailability);
            viewHolder.progress = (ProgressBar) v.findViewById(R.id.progress);

            viewHolder.spPackage = (Spinner) v.findViewById(R.id.spPackage);
            viewHolder.position = position;

            viewHolder.txbQuantity.setInputType(0);
            viewHolder.txbQuantity.removeTextChangedListener(null);
            viewHolder.txbQuantity.addTextChangedListener(new textWatcher(viewHolder.txbQuantity, viewHolder));

            viewHolder.spPackage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    Double pakovanje = 0D;

                    try {
                        pakovanje = Double.parseDouble(((SpinnerItem) viewHolder.spPackage.getSelectedItem()).getValue());
                    }
                    catch (Exception ex) {

                    }

                    OrderItem _mOrderItem = (OrderItem) viewHolder.txbQuantity.getTag();


                    if (pakovanje > 0D && pakovanje != _mOrderItem.Pakovanje && wurthMB.getOrder() != null) {

                        java.util.Iterator<OrderItem> itr = wurthMB.getOrder().items.iterator();

                        while (itr.hasNext()) {
                            OrderItem element = itr.next();
                            if (element.ArtikalID == _mOrderItem.ArtikalID) {
                                element.Pakovanje = pakovanje;
                                wurthMB.getOrder().setDiscount(element);
                                wurthMB.getOrder().CalculateTotal();

                                _mOrderItem.Pakovanje = pakovanje;
                                _mOrderItem.ClientDiscountPercentage = element.ClientDiscountPercentage;
                                _mOrderItem.UserDiscountPercentage = element.UserDiscountPercentage;
                                _mOrderItem.Total = element.Total;
                                _mOrderItem.TaxTotal = element.TaxTotal;
                                _mOrderItem.GrandTotal = element.GrandTotal;
                                break;
                            }
                        }

                        viewHolder.txbQuantity.setTag(_mOrderItem);

                        if (_mOrderItem.Quantity > 0) {
                            setInfo(_mOrderItem, viewHolder);
                        }

                        ((ProductsListFragment) mFragment).bindData();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            v.setTag(viewHolder);

            v.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    arg0.findViewById(R.id.txbQuantity).requestFocus();
                }
            });

            viewHolder.btnNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final OrderItem mOrderItem = (OrderItem) viewHolder.txbQuantity.getTag();

                    final Dialog _ItemDialog = new Dialog(mContext);
                    _ItemDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                    _ItemDialog.setContentView(R.layout.list_row_order_item_note_dialog);
                    _ItemDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    final EditText Note = (EditText) _ItemDialog.findViewById(R.id.txbNote);
                    Note.setText(mOrderItem.Note);

                    _ItemDialog.findViewById(R.id.btnUpdateOrder).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            java.util.Iterator<ba.wurth.mb.Classes.Objects.OrderItem> itr = wurthMB.getOrder().items.iterator();
                            while (itr.hasNext()) {
                                ba.wurth.mb.Classes.Objects.OrderItem e = itr.next();
                                if (e.ProductID == mOrderItem.ProductID) {
                                    e.Note = Note.getText().toString();
                                    mOrderItem.Note = Note.getText().toString();
                                    viewHolder.txbQuantity.setTag(mOrderItem);
                                    wurthMB.setOrder(wurthMB.getOrder());
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
                                    if (mFragment instanceof OrderItemsFragment) {
                                        ((OrderItemsFragment) mFragment).bindData();
                                        ((OrderItemsFragment) mFragment).bindTotals();
                                    }
                                    break;
                                }
                            }
                            wurthMB.setOrder(wurthMB.getOrder());
                            _ItemDialog.dismiss();
                        }
                    });

                    _ItemDialog.show();

                }
            });

            viewHolder.btnInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    /*if (mCursor.getInt(mStatus_Prezentacije_Artikla) == 1) {
                        Notifications.showNotification(mContext, "", mContext.getString(R.string.Notification_ProductCanNotBeViewed), 2);
                        return;
                    }*/

                    OrderItem mOrderItem = (OrderItem) viewHolder.txbQuantity.getTag();
                    Intent i = new Intent(mFragment.getActivity(), ProductActivity.class);
                    i.putExtra("ProductID", mOrderItem.ProductID);
                    i.putExtra("ArtikalID", mOrderItem.ArtikalID);
                    mFragment.getActivity().startActivityForResult(i, 0);
                }
            });

            viewHolder.btnAvailability.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final OrderItem mOrderItem = (OrderItem) viewHolder.txbQuantity.getTag();

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) new LongTask(viewHolder.litPackage, mOrderItem, viewHolder.progress).execute(mOrderItem);
                    else new LongTask(viewHolder.litPackage, mOrderItem, viewHolder.progress).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mOrderItem);

                }
            });

            viewHolder.btnSendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final OrderItem mOrderItem = (OrderItem) viewHolder.txbQuantity.getTag();

                    final Dialog dialog = new Dialog(mContext, R.style.CustomDialog);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.alert);

                    ((TextView) dialog.findViewById(R.id.Title)).setText(R.string.Notification_AttentionNeeded);
                    ((TextView) dialog.findViewById(R.id.text)).setText(R.string.AreYouSureSend);

                    dialog.findViewById(R.id.dialogButtonNEW).setVisibility(View.GONE);
                    dialog.findViewById(R.id.dialogButtonCANCEL).setVisibility(View.VISIBLE);
                    ((Button) dialog.findViewById(R.id.dialogButtonOK)).setText(R.string.Send);
                    dialog.findViewById(R.id.note).setVisibility(View.VISIBLE);

                    dialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            try {

                                JSONObject mTemp = new JSONObject();
                                mTemp.put("UserID", wurthMB.getUser().UserID);
                                mTemp.put("ArtikalID", mOrderItem.ArtikalID);
                                mTemp.put("ProductID", mOrderItem.ProductID);
                                mTemp.put("Komentar", ((EditText) dialog.findViewById(R.id.note)).getText().toString());
                                mTemp.put("Datum", System.currentTimeMillis());

                                Temp_Acquisition temp = new Temp_Acquisition();

                                temp.AccountID = wurthMB.getUser().AccountID;
                                temp.UserID = wurthMB.getUser().UserID;
                                temp.OptionID = 30;
                                temp.ID = 0;
                                temp.Sync = 0;
                                temp.DOE = System.currentTimeMillis();
                                temp.jsonObj = mTemp.toString();

                                if (DL_Temp.AddOrUpdate(temp) > 0 ) {
                                    Notifications.showNotification(mContext, "", mContext.getString(R.string.Notification_RequestSent), 0);
                                }
                                else {
                                    Notifications.showNotification(mContext, "", mContext.getString(R.string.SystemError), 1);
                                }
                            }
                            catch (Exception ex) {

                            }
                            dialog.dismiss();
                        }
                    });

                    dialog.findViewById(R.id.dialogButtonCANCEL).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });

            viewHolder.btnPricelist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {

                        if (wurthMB.getOrder().client == null) return;

                        if (wurthMB.getOrder().Sync == 1 && (wurthMB.getOrder().OrderStatusID == 4 || wurthMB.getOrder().OrderStatusID == 5)) return;

                        if (viewHolder.txbQuantity.getTag() == null || !(viewHolder.txbQuantity.getTag() instanceof OrderItem)) return;

                        final OrderItem mOrderItem = (OrderItem) viewHolder.txbQuantity.getTag();

                        if (ItemDialog != null) {
                            ItemDialog.dismiss();
                            ItemDialog = null;
                        }

                        ItemDialog = new Dialog(mContext);
                        ItemDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                        ItemDialog.setContentView(R.layout.list_row_order_item_dialog);
                        ItemDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        ItemDialog.findViewById(R.id.btnUpdateOrder).setVisibility(View.GONE);

                        ((TextView) ItemDialog.findViewById(R.id.lblProductName)).setText(viewHolder.litTitle.getText());
                        ((EditText) ItemDialog.findViewById(R.id.txbNote)).setText(mOrderItem.Note);

                        Double price = mOrderItem.Price_WS;

                        if (mOrderItem.KljucCijene == 2) price = price * 100;
                        if (mOrderItem.KljucCijene == 3) price = price * 1000;

                        ((TextView) ItemDialog.findViewById(R.id.lblPrice)).setText(CustomNumberFormat.GenerateFormatCurrency(price) + "/" + mOrderItem.KljucCijene);
                        ((EditText) ItemDialog.findViewById(R.id.txbNote)).setText(mOrderItem.Note);

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

                                                if (start >= mOrderItem.Pakovanje) mOrderItem.Quantity = start / mOrderItem.Pakovanje;
                                                else if (end >= mOrderItem.Pakovanje) mOrderItem.Quantity = 1;
                                                else Notifications.showNotification(mContext, "", mContext.getString(R.string.Notification_PackageMissmatch), 2);

                                            }

                                        }

                                        mOrderItem.ClientDiscountPercentage = mPriceItem.discount;
                                        mOrderItem.KljucCijene = mPriceItem.priceKey;

                                        if (mPriceItem.priceKey == 1) mOrderItem.Price_WS = mPriceItem.price;
                                        if (mPriceItem.priceKey == 2) mOrderItem.Price_WS = mPriceItem.price / 100;
                                        if (mPriceItem.priceKey == 3) mOrderItem.Price_WS = mPriceItem.price / 1000;

                                        mOrderItem.Price_RT = mOrderItem.Price_WS;
                                        mOrderItem.UserDiscountPercentage = _Discount - ((Double) litDiscountStart.getTag());
                                        //mOrderItem.Note = ((EditText) ItemDialog.findViewById(R.id.txbNote)).getText().toString();

                                        java.util.Iterator<ba.wurth.mb.Classes.Objects.OrderItem> itr = wurthMB.getOrder().items.iterator();
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
                                        }

                                        wurthMB.getOrder().CalculateTotal();
                                        wurthMB.setOrder(wurthMB.getOrder());

                                        ItemDialog.dismiss();
                                        ItemDialog = null;

                                        if (mFragment instanceof ProductsListFragment) {
                                            ((ProductsListFragment) mFragment).bindData();
                                            ((ProductsListFragment) mFragment).bindList();
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
        }
        else viewHolder = (ViewHolderPricelist) v.getTag();

        if (position % 2 == 1) v.setBackgroundColor(mContext.getResources().getColor(R.color.altRow));
        else v.setBackgroundColor(mContext.getResources().getColor(R.color.Row));

        viewHolder.btnSendRequest.setVisibility(View.GONE);
        viewHolder.btnInfo.setVisibility(View.VISIBLE);
        viewHolder.btnPricelist.setVisibility(View.GONE);
        viewHolder.btnNote.setVisibility(View.GONE);
        viewHolder.txbQuantity.setVisibility(View.VISIBLE);
        viewHolder.spPackage.setVisibility(View.VISIBLE);

        viewHolder.Status_Artikla = mCursor.getInt(mStatus_Artikla);
        viewHolder.Status_Prezentacije_Artikla = mCursor.getInt(mStatus_Prezentacije_Artikla);

        String desc = mCursor.getString(mNaziv) + "<br /><strong><font color='#CC0000'>" + mCursor.getString(mSifra) + "</font></strong>";

        if (viewHolder.Status_Artikla > 1) {
            viewHolder.txbQuantity.setVisibility(View.GONE);
            viewHolder.spPackage.setVisibility(View.GONE);
            viewHolder.btnPricelist.setVisibility(View.GONE);

            if (viewHolder.Status_Artikla == 2) desc += "<br />" + mContext.getString(R.string.Discarded);
            if (viewHolder.Status_Artikla == 4) desc += "<br />" + mContext.getString(R.string.DiscardedReason);
            if (viewHolder.Status_Artikla == 3) desc += "<br />" + mContext.getString(R.string.Replaced);
        }

        if (viewHolder.Status_Prezentacije_Artikla == 1) {
            viewHolder.btnInfo.setVisibility(View.GONE);
        }

        viewHolder.litTitle.setText((!mCursor.isNull(mGrupniNaziv) ? mCursor.getString(mGrupniNaziv) : mCursor.getString(mNaziv) + (!mCursor.isNull(mAtribut) ?  ", " + mCursor.getString(mAtribut) : "")));
        viewHolder.litTitle.setTag(mCursor.getLong(mProductID));

        viewHolder.litSubTitle.setText(Html.fromHtml(desc));

        if (!mCursor.isNull(mCursor.getColumnIndex("Velika")) && !mCursor.getString(mCursor.getColumnIndex("Velika")).equals("")) {
            wurthMB.imageLoader.DisplayImage(mCursor.getString(mCursor.getColumnIndex("Velika")), viewHolder.image);
        }
        else {
            viewHolder.image.setImageResource(R.drawable.no_image);
        }

        OrderItem mOrderItem = new OrderItem();
        mOrderItem.ProductID = mCursor.getLong(mProductID);
        mOrderItem.ArtikalID = mCursor.getLong(mArtikalID);
        mOrderItem.ProductName = mCursor.getString(mNaziv);
        mOrderItem.UserDiscountPercentage = 0D;
        mOrderItem.ClientDiscountPercentage = 0D;
        mOrderItem.Pakovanje = 0D;
        mOrderItem.Quantity = 0D;
        mOrderItem.Mjerna_Jedinica = mCursor.getString(mMjernaJedinica);
        if (!mCursor.isNull(mStanje_Zaliha)) mOrderItem.Stanje_Zaliha = mCursor.getDouble(mStanje_Zaliha);
        if (!mCursor.isNull(mNarucena_Kolicina)) mOrderItem.Narucena_Kolicina = mCursor.getDouble(mNarucena_Kolicina);
        if (!mCursor.isNull(mDatum_Prijema)) mOrderItem.Datum_Prijema = mCursor.getLong(mDatum_Prijema);
        if (!mCursor.isNull(mPredefinisana_Dostupnost)) mOrderItem.Predefinisana_Dostupnost = mCursor.getInt(mPredefinisana_Dostupnost);

        boolean exists = false;

        if (o != null) {
            java.util.Iterator<OrderItem> itr = o.items.iterator();
            while (itr.hasNext()) {
                OrderItem element = itr.next();
                if (element.ArtikalID == mCursor.getLong(mArtikalID)) {
                    extText = true;

                    if (element.Quantity % 1 != 0) viewHolder.txbQuantity.setText(Integer.toString((int) element.Quantity));
                    else viewHolder.txbQuantity.setText(Integer.toString((int) element.Quantity));

                    mOrderItem.UserDiscountPercentage = element.UserDiscountPercentage;
                    mOrderItem.ClientDiscountPercentage = element.ClientDiscountPercentage;
                    mOrderItem.Price_RT = element.Price_RT;
                    mOrderItem.Price_WS = element.Price_WS;
                    mOrderItem.Total = element.Total;
                    mOrderItem.GrandTotal = element.GrandTotal;
                    mOrderItem.DiscountTotal = element.DiscountTotal;
                    mOrderItem.KljucCijene = element.KljucCijene;
                    mOrderItem.Quantity = element.Quantity;
                    mOrderItem.Pakovanje = element.Pakovanje;
                    mOrderItem.Note = element.Note;

                    extText = false;
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {
            extText = true;
            viewHolder.txbQuantity.setText("0");
            viewHolder.litSubTotal.setText("");
            viewHolder.litSupTotal.setText("");
            viewHolder.litPackage.setText("");
            mOrderItem.UserDiscountPercentage = 0D;
            mOrderItem.ClientDiscountPercentage = 0D;
            mOrderItem.Quantity = 0D;
            extText = false;
        }

        if (position == 0 && !init) {
            viewHolder.txbQuantity.requestFocus();
            init = true;
        }

        Cursor cur = DL_Wurth.GET_Packages(mCursor.getLong(mArtikalID));
        if (cur != null) {

            int i = 0;
            SpinnerItem[] items;
            SpinnerAdapter adapter;
            items = new SpinnerItem[cur.getCount()];

            while (cur.moveToNext()) {
                items[cur.getPosition()] = new SpinnerItem(new Long(cur.getPosition()), " x " + cur.getString(cur.getColumnIndex("Pakovanje")) + " " + mCursor.getString(mMjernaJedinica), cur.getString(cur.getColumnIndex("Barcode")), cur.getString(cur.getColumnIndex("Pakovanje")));
                if (mOrderItem.Pakovanje == cur.getDouble(cur.getColumnIndex("Pakovanje"))) i = cur.getPosition();
                if (mOrderItem.Pakovanje == 0D && cur.getPosition() == 0) mOrderItem.Pakovanje = cur.getDouble(cur.getColumnIndex("Pakovanje"));
            }

            adapter = new SpinnerAdapter(mFragment.getActivity(), R.layout.simple_dropdown_item_1line, items);
            viewHolder.spPackage.setAdapter(adapter);
            viewHolder.spPackage.setSelection(i);

            cur.close();
        }

        if (mOrderItem.Quantity > 0D) {
            setInfo(mOrderItem, viewHolder);
        }
        else {
            if (wurthMB.getOrder() != null && wurthMB.getOrder().ClientID > 0L) setPrice(viewHolder, mOrderItem.ArtikalID, wurthMB.getOrder().ClientID);
        }

        viewHolder.txbQuantity.setTag(mOrderItem);

        return v;
    }

    private class textWatcher implements TextWatcher {
        private View view;
        private ViewHolderPricelist viewHolder;

        private textWatcher(View view, ViewHolderPricelist viewHolder) {
            this.view = view;
            this.viewHolder = viewHolder;
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
                        if(element.ArtikalID == mOrderItem.ArtikalID) {
                            if (qty == 0D) {
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

                    if (!exists && qty > 0D && mOrderItem.ProductID > 0L) {
                        OrderItem tempOrderItem =  new OrderItem(){{
                            ArtikalID = mOrderItem.ArtikalID;
                            ProductID = mOrderItem.ProductID;
                            UserDiscountPercentage = mOrderItem.UserDiscountPercentage;
                            ClientDiscountPercentage = mOrderItem.ClientDiscountPercentage;
                            Pakovanje = mOrderItem.Pakovanje;
                            KljucCijene = mOrderItem.KljucCijene;
                            ProductName = mOrderItem.ProductName;
                            Quantity = qty;
                            Note = "";
                        }};

                        tempOrderItem = wurthMB.getOrder().setDiscount(tempOrderItem);
                        wurthMB.getOrder().items.add(tempOrderItem);
                        wurthMB.getOrder().CalculateTotal();
                    }

                    java.util.Iterator<OrderItem> _itr = wurthMB.getOrder().items.iterator();
                    while (_itr.hasNext()) {
                        OrderItem element = _itr.next();
                        if(element.ArtikalID == mOrderItem.ArtikalID) {
                            mOrderItem.UserDiscountPercentage = element.UserDiscountPercentage;
                            mOrderItem.ClientDiscountPercentage = element.ClientDiscountPercentage;
                            mOrderItem.Price_RT = element.Price_RT;
                            mOrderItem.Price_WS = element.Price_WS;
                            mOrderItem.Total = element.Total;
                            mOrderItem.GrandTotal = element.GrandTotal;
                            mOrderItem.DiscountTotal = element.DiscountTotal;
                            mOrderItem.KljucCijene = element.KljucCijene;
                            mOrderItem.Quantity = element.Quantity;
                            mOrderItem.ProductName = element.ProductName;
                            break;
                        }
                    }

                    if (qty > 0D) {
                        setInfo(mOrderItem, viewHolder);
                    }
                    else {
                        viewHolder.litSupTotal.setText("");
                        viewHolder.litSubTotal.setText("");
                        viewHolder.litPackage.setText("");
                    }

                    viewHolder.txbQuantity.setTag(mOrderItem);
                }

                if (wurthMB.getOrder() != null && wurthMB.getOrder().items.size() == 0) wurthMB.setOrder(null);

                wurthMB.setOrder(wurthMB.getOrder());
                o = wurthMB.getOrder();

                if (mFragment instanceof ProductsListFragment) {
                    ((ProductsListFragment) mFragment).bindData();
                }
            }
            catch (Exception ex) {
                wurthMB.AddError("Add item in product list", ex.getMessage(), ex);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }


    @Override
    public void bindView(View arg0, Context arg1, Cursor arg2) { }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    private void setInfo(OrderItem mOrderItem, ViewHolderPricelist viewHolder) {

        if (wurthMB.getOrder().client == null) {
            viewHolder.btnPricelist.setVisibility(View.GONE);
        }
        else {
            viewHolder.btnPricelist.setVisibility(View.VISIBLE);
            viewHolder.btnNote.setVisibility(View.VISIBLE);
        }

        Double price = mOrderItem.Price_WS;

        if (price == 0D) {
            viewHolder.btnSendRequest.setVisibility(View.VISIBLE);
            viewHolder.txbQuantity.setVisibility(View.GONE);
            viewHolder.spPackage.setVisibility(View.GONE);
            viewHolder.btnPricelist.setVisibility(View.GONE);
            return;
        }

        if (mOrderItem.KljucCijene == 2) price = price * 100;
        if (mOrderItem.KljucCijene == 3) price = price * 1000;

        price = price - (price * (mOrderItem.UserDiscountPercentage + mOrderItem.ClientDiscountPercentage) / 100);
        viewHolder.litSubTotal.setText(CustomNumberFormat.GenerateFormatCurrency(mOrderItem.Total));
        viewHolder.litSupTotal.setText(CustomNumberFormat.GenerateFormatCurrency(price) + "/" + mOrderItem.KljucCijene);
    }

    private void setPrice(ViewHolderPricelist viewHolder, Long ArtikalID, Long ClientID) {

        try {
            Cursor cur = DL_Wurth.GET_Product_Price(ArtikalID, ClientID);

            if (cur != null) {

                if (cur.moveToFirst()) {
                    Double price = cur.getDouble(cur.getColumnIndex("OsnovnaCijena"));
                    Integer KljucCijene = cur.getInt(cur.getColumnIndex("KljucCijene"));
                    if (price == 0D) {
                        viewHolder.btnSendRequest.setVisibility(View.VISIBLE);
                        viewHolder.txbQuantity.setVisibility(View.GONE);
                        viewHolder.spPackage.setVisibility(View.GONE);
                        return;
                    }
                    viewHolder.litSupTotal.setText(CustomNumberFormat.GenerateFormatCurrency(price) + "/" + KljucCijene);
                }
                else viewHolder.btnSendRequest.setVisibility(View.VISIBLE);
                cur.close();
            }
        }
        catch (Exception ex) {

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
            litPackage.setText("");
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
    
}
