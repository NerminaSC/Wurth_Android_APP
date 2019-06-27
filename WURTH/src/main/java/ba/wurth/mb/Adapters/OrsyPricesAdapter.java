package ba.wurth.mb.Adapters;

import android.content.Context;
import android.database.Cursor;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

import ba.wurth.mb.Classes.Objects.ViewHolderPricelist;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class OrsyPricesAdapter extends ArrayAdapter<JSONObject>
{

    private JSONArray items;
    private Context mContext;

    private Fragment mFragment;

    private LayoutInflater mInflater;
    private static SimpleDateFormat gDateFormatDataItem = new SimpleDateFormat("EEE dd.MMM.yyyy");

    public OrsyPricesAdapter(Context context, int textViewResourceId, JSONArray objects, Fragment f)
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
        try {
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
            ViewHolderPricelist.image.setVisibility(View.GONE);

            ViewHolderPricelist.litSupTotal.setText(element.getString("ID"));
            ViewHolderPricelist.litSubTitle.setText(gDateFormatDataItem.format(element.getLong("DOE")));

            try {
                JSONObject jsonObject = new JSONObject(element.getString("jsonObj"));
                Cursor mCursor = DL_Wurth.GET_Partner(jsonObject.getLong("PartnerID"));
                if (mCursor != null) {
                    if (mCursor.moveToFirst()) {
                        ViewHolderPricelist.litTitle.setText(mCursor.getString(mCursor.getColumnIndex("Name")));
                    }
                }
            } catch (Exception  e) {
                wurthMB.AddError("OrsyPricesAdapter", e.getMessage(), e);
            }
        } catch (Exception e) {
            wurthMB.AddError("OrsyPricesAdapter", e.getMessage(), e);
        }

        return v;
    }
}