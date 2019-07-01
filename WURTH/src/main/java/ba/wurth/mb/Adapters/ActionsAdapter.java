package ba.wurth.mb.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import ba.wurth.mb.Activities.Clients.ClientActivity;
import ba.wurth.mb.Activities.Products.ProductsActivity;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Order;
import ba.wurth.mb.Classes.Objects.ViewHolderPricelist;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class ActionsAdapter extends CursorAdapter {
    LayoutInflater mInflater;
    private int mIDAkcije;
    private int mProductID;
    private int mNaziv;
    private int mDatumOd;
    private int mDatumDo;
    private int mKomentar;
    private int mSlika;
    private int mPotencijalOd;
    private int mmPotencijalDo;
    private int mBransa;
    private int mIDArtikla;

    private boolean init = false;
    private boolean extText = false;

    private Context mContext;
    private Fragment mFragment;
    private Cursor mCursor;
    private Long mClientID;

    private ArrayList<Integer> ArtikalID = new ArrayList<Integer>();

    private Dialog ItemDialog;
    private java.text.Format dateFormatter = new java.text.SimpleDateFormat("dd.MM.yyyy");

    private LayoutInflater li;

    private Order o;

    public ActionsAdapter(Context context, Cursor cursor, Fragment f, Long ClientID) {
        super(context, cursor);

        mFragment = f;

        mInflater = LayoutInflater.from(context);

        mIDAkcije = cursor.getColumnIndex("IDAkcije");
        mProductID = cursor.getColumnIndex("ProductID");
        mNaziv = cursor.getColumnIndex("Naziv");
        mDatumOd = cursor.getColumnIndex("DatumOd");
        mDatumDo = cursor.getColumnIndex("DatumDo");
        mKomentar = cursor.getColumnIndex("Komentar");
        mSlika = cursor.getColumnIndex("Slika");
        mPotencijalOd = cursor.getColumnIndex("PotencijalOd");
        mmPotencijalDo = cursor.getColumnIndex("PotencijalDo");
        mBransa = cursor.getColumnIndex("Bransa");
        mIDArtikla = cursor.getColumnIndex("IDArtikla");


        mContext = context;
        mCursor = cursor;
        mClientID = ClientID;

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
            v = li.inflate(R.layout.list_row_actions, parent, false);

            viewHolder = new ViewHolderPricelist();
            viewHolder.litTitle = (TextView) v.findViewById(R.id.litTitle);
            viewHolder.litSubTitle = (TextView) v.findViewById(R.id.litSubTitle);
            viewHolder.litSupTotal = (TextView) v.findViewById(R.id.litSupTotal);
            viewHolder.litSupTitle = (TextView) v.findViewById(R.id.litSupTitle);
            viewHolder.image = (ImageView) v.findViewById(R.id.img);
            v.setTag(viewHolder);

            viewHolder.image.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View arg0) {


                    if (wurthMB.getOrder() != null && wurthMB.getOrder().ClientID != mClientID) {

                        final Dialog dialog = new Dialog(mContext, R.style.CustomDialog);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.alert);
                        dialog.setCancelable(false);

                        ((TextView) dialog.findViewById(R.id.Title)).setText(R.string.Notification_AttentionNeeded);
                        ((TextView) dialog.findViewById(R.id.text)).setText(R.string.Notification_OrderExist);

                        dialog.findViewById(R.id.dialogButtonNEW).setVisibility(View.GONE);
                        dialog.findViewById(R.id.dialogButtonCANCEL).setVisibility(View.VISIBLE);
                        ((Button) dialog.findViewById(R.id.dialogButtonOK)).setText(R.string.Continue);

                        dialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                wurthMB.setOrder(new Order());
                                Client client = DL_Wurth.GET_Client(mClientID);

                                if (client != null) {
                                    Cursor cur = DL_Wurth.GET_Partner(client._clientid);
                                    wurthMB.getOrder().PartnerID = client._clientid;

                                    if (cur != null) {
                                        if (cur.moveToFirst()) {
                                            wurthMB.getOrder().client = DL_Wurth.GET_Client(cur.getLong(cur.getColumnIndex("ClientID")));
                                            wurthMB.getOrder().ClientID = wurthMB.getOrder().client.ClientID;
                                            wurthMB.getOrder().DeliveryPlaceID = cur.getLong(cur.getColumnIndex("DeliveryPlaceID"));
                                            wurthMB.getOrder().PaymentMethodID = 0;
                                        }
                                        cur.close();
                                    }

                                    Intent k = new Intent(mContext, ProductsActivity.class);
                                    k.putExtra("ACTION", 0);
                                    k.putExtra("ArtikalID", Long.parseLong(arg0.getTag().toString().split(",")[0]));
                                    k.putExtra("ProductID", Long.parseLong(arg0.getTag().toString().split(",")[1]));
                                    k.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    if (mContext instanceof ClientActivity) ((ClientActivity) mContext).finish();
                                    mContext.startActivity(k);
                                }
                                dialog.dismiss();
                            }
                        });

                        dialog.findViewById(R.id.dialogButtonCANCEL).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                    else if (wurthMB.getOrder() != null && wurthMB.getOrder().ClientID == mClientID) {
                        Intent k = new Intent(mContext, ProductsActivity.class);
                        k.putExtra("ACTION", 0);
                        k.putExtra("ArtikalID", Long.parseLong(arg0.getTag().toString().split(",")[0]));
                        k.putExtra("ProductID", Long.parseLong(arg0.getTag().toString().split(",")[1]));
                        k.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        if (mContext instanceof ClientActivity) ((ClientActivity) mContext).finish();
                        mContext.startActivity(k);
                    }
                    else {
                        wurthMB.setOrder(new Order());
                        Client client = DL_Wurth.GET_Client(mClientID);

                        if (client != null) {
                            Cursor cur = DL_Wurth.GET_Partner(client._clientid);
                            wurthMB.getOrder().PartnerID = client._clientid;

                            if (cur != null) {
                                if (cur.moveToFirst()) {
                                    wurthMB.getOrder().client = DL_Wurth.GET_Client(cur.getLong(cur.getColumnIndex("ClientID")));
                                    wurthMB.getOrder().ClientID = wurthMB.getOrder().client.ClientID;
                                    wurthMB.getOrder().DeliveryPlaceID = cur.getLong(cur.getColumnIndex("DeliveryPlaceID"));
                                    wurthMB.getOrder().PaymentMethodID = 0;
                                }
                                cur.close();
                            }

                            Intent k = new Intent(mContext, ProductsActivity.class);
                            k.putExtra("ACTION", 0);
                            k.putExtra("ArtikalID", Long.parseLong(arg0.getTag().toString().split(",")[0]));
                            k.putExtra("ProductID", Long.parseLong(arg0.getTag().toString().split(",")[1]));
                            k.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            if (mContext instanceof ClientActivity) ((ClientActivity) mContext).finish();
                            mContext.startActivity(k);
                        }
                    }

                }
            });
        }
        else viewHolder = (ViewHolderPricelist) v.getTag();

        Boolean exists = false;

        if (!mCursor.getString(mBransa).equals("")) {
            Cursor _cur = wurthMB.dbHelper.getDB().rawQuery("SELECT * FROM PARTNER_BRANSE INNER JOIN CLIENTS ON PARTNER_BRANSE.PartnerID = CLIENTS._clientid WHERE CLIENTS.ClientID = " + mClientID, null);
            while (_cur.moveToNext()) {
                for (int i = 0; i < mCursor.getString(mBransa).split(",").length; i++) {
                    if (_cur.getString(_cur.getColumnIndex("Bransa")).substring(0,1).equals(mCursor.getString(mBransa).split(",")[i])) {
                        exists = true;
                    }
                }
            }
            _cur.close();
        }
        else exists = true;

        for (int i = 0; i < ArtikalID.size(); i++){
            if (ArtikalID.get(i) == mCursor.getInt(mIDArtikla)) {
                exists = false;
                break;
            }
        }

        if (!exists) {
            viewHolder.litTitle.setVisibility(View.GONE);
            viewHolder.image.setVisibility(View.GONE);
            viewHolder.litSubTitle.setVisibility(View.GONE);
        }
        else {
            viewHolder.litTitle.setVisibility(View.VISIBLE);
            viewHolder.image.setVisibility(View.VISIBLE);
            viewHolder.litSubTitle.setVisibility(View.VISIBLE);
            ArtikalID.add(mCursor.getInt(mIDArtikla));
        }

        String desc = mCursor.getString(mNaziv) + "<br /><strong><font color='#CC0000'>" + mCursor.getString(mKomentar) + "</font></strong>";

        viewHolder.litTitle.setText(mCursor.getString(mNaziv));
        viewHolder.image.setTag(mCursor.getString(mIDArtikla) + "," + mCursor.getString(mProductID));

        viewHolder.litSubTitle.setText(Html.fromHtml(desc));

        if (!mCursor.isNull(mSlika) && !mCursor.getString(mSlika).equals("")) {
            wurthMB.imageLoader.DisplayImage(mCursor.getString(mSlika), viewHolder.image);
        }
        else {
            viewHolder.image.setImageResource(R.drawable.no_image);
        }

        return v;
    }

    @Override
    public void bindView(View arg0, Context arg1, Cursor arg2) { }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

}
