package ba.wurth.mb.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.ViewHolderPricelist;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Fragments.Orders.NegotiatedPriceFragment;
import ba.wurth.mb.R;

public class NegotiatedPriceAdapter extends ArrayAdapter<JSONObject>
{

    private JSONArray items;
    private Context mContext;

    private Dialog ItemDialog;
    private Fragment mFragment;

    private LayoutInflater mInflater;
    private Client c;
    private boolean extText = false;

    public NegotiatedPriceAdapter(Context context, int textViewResourceId, JSONArray objects, Fragment f)
    {
        super(context, textViewResourceId);
        mFragment = f;
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        items = objects;
    }

    @Override
    public int getCount()
    {
        return this.items.length();
    }

    @Override
    public JSONObject getItem(int index)
    {
        try {
            if (index <= getCount()) return this.items.getJSONObject(index);
            return this.items.getJSONObject(getCount() - 1);
        }
        catch (Exception e) { }
        return null;
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent)
    {
        final ViewHolderPricelist ViewHolderPricelist;
        final JSONObject element = getItem(position);

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


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final Dialog _ItemDialog = new Dialog(mContext);
                    _ItemDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                    _ItemDialog.setContentView(R.layout.list_row_order_item_note_dialog);
                    _ItemDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    final EditText Note = (EditText) _ItemDialog.findViewById(R.id.txbNote);
                    try {
                        Note.setText(((NegotiatedPriceFragment) mFragment).jsonObject.getJSONArray("items").getJSONObject(position).getString("Note"));
                    } catch (JSONException e) {
                    }

                    _ItemDialog.findViewById(R.id.btnUpdateOrder).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            try {
                                ((NegotiatedPriceFragment) mFragment).jsonObject.getJSONArray("items").getJSONObject(position).put("Note", Note.getText().toString());
                            } catch (JSONException e) {
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
                            try {
                                ((NegotiatedPriceFragment) mFragment).jsonObject.put("items", Common.remove(position, ((NegotiatedPriceFragment) mFragment).jsonObject.getJSONArray("items")));
                            } catch (JSONException e) {
                            }
                            ((NegotiatedPriceFragment) mFragment).bindData();
                            _ItemDialog.dismiss();
                        }
                    });

                    _ItemDialog.show();
                }
            });


            v.setTag(ViewHolderPricelist);
        }

        else  ViewHolderPricelist = (ViewHolderPricelist) v.getTag();


        if (position % 2 == 1) v.setBackgroundColor(mContext.getResources().getColor(R.color.altRow));
        else v.setBackgroundColor(mContext.getResources().getColor(R.color.Row));

        ViewHolderPricelist.btnSendRequest.setVisibility(View.GONE);
        ViewHolderPricelist.btnPricelist.setVisibility(View.GONE);
        ViewHolderPricelist.txbQuantity.setVisibility(View.GONE);
        ViewHolderPricelist.spPackage.setVisibility(View.GONE);
        ViewHolderPricelist.btnAvailability.setVisibility(View.GONE);
        ViewHolderPricelist.btnInfo.setVisibility(View.GONE);
        ViewHolderPricelist.btnNote.setVisibility(View.GONE);

        try {
            Cursor mCursor = DL_Wurth.GET_Product(element.getLong("ArtikalID"));

            if (mCursor != null) {

                if (mCursor.moveToFirst()) {
                    String desc = "<strong><font color='#CC0000'>" + mCursor.getString(mCursor.getColumnIndex("sifra")) + "</font></strong><br />" + mCursor.getString(mCursor.getColumnIndex("Naziv"));

                    ViewHolderPricelist.litTitle.setText(element.getString("ProductName"));
                    ViewHolderPricelist.litTitle.setTag(element.getLong("ProductID"));

                    ViewHolderPricelist.litSubTitle.setText(Html.fromHtml(desc));

                    if (!mCursor.isNull(mCursor.getColumnIndex("Velika")) && !mCursor.getString(mCursor.getColumnIndex("Velika")).equals("")) {
                        wurthMB.imageLoader.DisplayImage(mCursor.getString(mCursor.getColumnIndex("Velika")), ViewHolderPricelist.image);
                    }
                    else {
                        ViewHolderPricelist.image.setImageResource(R.drawable.no_image);
                    }

                    ViewHolderPricelist.litSupTotal.setText(element.getString("Quantity_From") + " - " + element.getString("Quantity_To"));
                    ViewHolderPricelist.litSubTotal.setText(element.getString("Discount") + " %");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return v;
    }
}