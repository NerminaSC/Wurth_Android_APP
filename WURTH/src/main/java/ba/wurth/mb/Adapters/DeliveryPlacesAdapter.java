package ba.wurth.mb.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.R;

public class DeliveryPlacesAdapter extends CursorAdapter {

    // We have two list item view types

    private Context mContext;
    private Fragment mFragment;

    LayoutInflater mInflater;

    public DeliveryPlacesAdapter(Context context, Cursor cursor, Fragment fragment) {
        super(context, cursor);

        mContext = context;
        mFragment = fragment;

        // Get the layout inflater

        mInflater = LayoutInflater.from(mContext);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View v = mInflater.inflate(R.layout.list_row, parent, false);

        ViewHolder holder = new ViewHolder();

        holder.litTitle = (TextView)  v.findViewById(R.id.litTitle);
        holder.litSubTitle = (TextView)  v.findViewById(R.id.litSubTitle);
        holder.litHeader = (TextView)  v.findViewById(R.id.litHeader);
        holder.litTotal = (TextView)  v.findViewById(R.id.litTotal);
        holder.litSupTotal = (TextView)  v.findViewById(R.id.litSupTotal);
        holder.litStatus = (TextView)  v.findViewById(R.id.litStatus);
        v.setTag(holder);

        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    /*ViewHolder vh = (ViewHolder) view.getTag();
                    DeliveryPlaceInfo oi = (DeliveryPlaceInfo) vh.litStatus.getTag();

                    if (oi == null || oi.DeliveryPlaceID == 0) return;

                    Intent k = new Intent(mFragment.getActivity(), DeliveryPlaceActivity.class);
                    k.putExtra("DeliveryPlaceID", oi.DeliveryPlaceID );
                    k.putExtra("ClientID", oi.ClientID );
                    mFragment.startActivity(k);*/
                }
                catch (Exception ex) {
                    wurthMB.AddError("DeliveryPlacesAdapter", ex.getMessage(), ex);
                }
            }
        });

        return v;
    }

    @Override
    public void bindView(View view, Context context,  Cursor cursor) {

        ViewHolder holder  =   (ViewHolder)    view.getTag();

        holder.litTitle.setText(cursor.getString(cursor.getColumnIndex("Name")));
        holder.litTitle.setContentDescription(cursor.getString(cursor.getColumnIndex("_id")));

        holder.litSubTitle.setText(cursor.getString(cursor.getColumnIndex("Address")));
        holder.litSupTotal.setText(cursor.getString(cursor.getColumnIndex("Code")));

        if (cursor.getString(cursor.getColumnIndex("Name")).length() > 0) holder.litStatus.setText(cursor.getString(cursor.getColumnIndex("Name")).substring(0,1));


        DeliveryPlaceInfo oi = new DeliveryPlaceInfo();
        oi._id = cursor.getLong(cursor.getColumnIndex("_id"));
        oi.ClientID = cursor.getLong(cursor.getColumnIndex("ClientID"));
        oi.DeliveryPlaceID = cursor.getLong(cursor.getColumnIndex("_id"));
        holder.litStatus.setTag(oi);
    }

    static class ViewHolder {
        TextView litHeader;
        TextView litTitle;
        TextView litSubTitle;
        TextView litDate;
        TextView litTotal;
        TextView litSupTotal;
        TextView litStatus;
        ImageView icon;
        ProgressBar progress;
        View actionsView;
        int position;
    }

    static class DeliveryPlaceInfo {
        Long _id;
        Long ClientID;
        Long DeliveryPlaceID;
        int Sync;
    }
}

