package ba.wurth.mb.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import ba.wurth.mb.Activities.Orders.OrderActivity;
import ba.wurth.mb.Activities.Tasks.TaskActivity;
import ba.wurth.mb.Activities.Visits.VisitActivity;
import ba.wurth.mb.R;

public class DiaryAdapter extends CursorAdapter {

    // We have two list item view types

    // We have two list item view types

    private Context mContext;
    private Fragment mFragment;

    private static final int VIEW_TYPE_GROUP_START = 0;
    private static final int VIEW_TYPE_GROUP_CONT = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    public ArrayList<Long> checkedItems;

    private boolean showCheckBox = false;
    private boolean ext = false;

    LayoutInflater mInflater;

    private int mDate;
    private int mID;
    private int mName;
    private int mSectionID;
    private int mType;


    private static SimpleDateFormat gDateFormatDataItem = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat gDateFormatDateItemTime = new SimpleDateFormat("HH:mm");

    public DiaryAdapter(Context context, Cursor cursor, Fragment fragment) {
        super(context, cursor);

        mContext = context;
        mFragment = fragment;

        // Get the layout inflater

        mInflater = LayoutInflater.from(mContext);

        // Get and cache column indices
        mDate = cursor.getColumnIndex("Date");
        mID = cursor.getColumnIndex("ID");
        mName = cursor.getColumnIndex("Name");
        mSectionID = cursor.getColumnIndex("SectionID");
        mType = cursor.getColumnIndex("Type");
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        // There is always a group header for the first data item
        if (position == 0) return VIEW_TYPE_GROUP_START;

        // For other items, decide based on current data
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        boolean newGroup = isNewGroup(cursor, position);

        // Check item grouping
        if (newGroup) return VIEW_TYPE_GROUP_START;
        else return VIEW_TYPE_GROUP_CONT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        final View v = mInflater.inflate(R.layout.list_row_diary, parent, false);

        ViewHolder holder = new ViewHolder();

        holder.litTitle = (TextView) v.findViewById(R.id.litTitle);
        holder.litSubTitle = (TextView) v.findViewById(R.id.litSubTitle);
        holder.litHeader = (TextView) v.findViewById(R.id.litHeader);
        holder.litTotal = (TextView) v.findViewById(R.id.litTotal);
        holder.litSupTotal = (TextView) v.findViewById(R.id.litSupTotal);
        holder.icon = (ImageView) v.findViewById(R.id.img);

        v.setTag(holder);

        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ViewHolder vh = (ViewHolder) view.getTag();
                    Info oi = (Info) vh.icon.getTag();

                    if (oi == null) return;
                    //if (oi == null || oi.StatusID == 9) return;

                    switch (oi.SectionID.intValue()) {
                        case 1:
                            Intent k = new Intent(mFragment.getActivity(), OrderActivity.class);
                            k.putExtra("_id", oi._id);
                            mFragment.getActivity().startActivity(k);
                            break;
                        case 9:
                            Intent j = new Intent(mFragment.getActivity(), VisitActivity.class);
                            j.putExtra("_id", oi._id);
                            mFragment.getActivity().startActivity(j);
                            break;
                        case 31:
                            final Dialog dialog = new Dialog(mFragment.getActivity(), R.style.CustomDialog);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.dialog_text);
                            try {
                                JSONObject jObj = new JSONObject(oi.text);
                                ((TextView) dialog.findViewById(R.id.litText)).setText(jObj.getString(("Note")));
                            }
                            catch (Exception exx) {

                            }
                            dialog.show();

                            break;
                        case 32:
                            Intent m = new Intent(mFragment.getActivity(), TaskActivity.class);
                            m.putExtra("TaskID", oi.ID);
                            mFragment.getActivity().startActivity(m);
                            break;
                        default:
                            break;
                    }
                }
                catch (Exception ex) {

                }
            }
        });

        return v;
    }

    @Override
    public void bindView(View view, Context context,  Cursor cursor) {

        int position = cursor.getPosition();
        int nViewType;

        ViewHolder holder  =   (ViewHolder)    view.getTag();

        if (position == 0) nViewType = VIEW_TYPE_GROUP_START;
        else {
            boolean newGroup = isNewGroup(cursor, position);
            if (newGroup) nViewType = VIEW_TYPE_GROUP_START;
            else nViewType = VIEW_TYPE_GROUP_CONT;
        }

        if (nViewType == VIEW_TYPE_GROUP_START) {
            holder.litHeader.setVisibility(View.VISIBLE);
        }
        else {
            holder.litHeader.setVisibility(View.GONE);
        }

        holder.litHeader.setText(gDateFormatDataItem.format(new Date(cursor.getLong(mDate))));


        holder.litTotal.setText(gDateFormatDateItemTime.format(new Date(cursor.getLong(mDate))));
        holder.litTitle.setContentDescription(cursor.getString(cursor.getColumnIndex("ID")));

        switch (cursor.getInt(mSectionID)) {
            case 1:

                switch (cursor.getInt(mType)) {
                    case 1:
                        holder.litTitle.setText(mContext.getString(R.string.Draft).toUpperCase());
                        break;

                    case 11:
                        holder.litTitle.setText(mContext.getString(R.string.Proposal).toUpperCase());
                        break;

                    case 12:
                        holder.litTitle.setText(mContext.getString(R.string.Requests).toUpperCase());
                        break;

                    default:
                        holder.litTitle.setText(mContext.getString(R.string.Order).toUpperCase());
                        break;
                }

                holder.litSubTitle.setText(cursor.getString(mName));
                holder.icon.setImageResource(R.drawable.icon_createorder);
                break;

            case 9:
                holder.litTitle.setText(mContext.getString(R.string.Visit).toUpperCase());
                holder.litSubTitle.setText(cursor.getString(mName));
                holder.icon.setImageResource(R.drawable.icon_notevisit);
                break;

            case 31:
                holder.litTitle.setText("Memo".toUpperCase());

                try {
                    JSONObject jsonObject = new JSONObject(cursor.getString(mName));
                    holder.litSubTitle.setText(jsonObject.getString("Note"));
                }
                catch (Exception exx) {

                }

                holder.icon.setImageResource(R.drawable.icon_memo);
                break;
            case 32:
                holder.litTitle.setText(mContext.getString(R.string.Task).toUpperCase());
                try {
                    JSONObject jsonObject = new JSONObject(cursor.getString(mName));
                    String status = "";

                    switch (jsonObject.getInt("StatusID")) {
                        case 1:
                            status = mContext.getString(R.string.Draft);
                            break;
                        case 2:
                            status = mContext.getString(R.string.Sent);
                            break;
                        case 5:
                            status = mContext.getString(R.string.Rejected);
                            break;
                        case 7:
                            status = mContext.getString(R.string.Completed);
                            break;
                        case 8:
                            status = mContext.getString(R.string.Canceled);
                            break;
                        default:
                            break;
                    }

                    holder.litSubTitle.setText(mContext.getString(R.string.Note) + " :" + jsonObject.getString("Note") + " " + mContext.getString(R.string.Status) + " :" + status);
                }
                catch (Exception exx) {

                }
                holder.icon.setImageResource(R.drawable.icon_task);
                break;

            case 33:
                holder.litTitle.setText(mContext.getString(R.string.Activity).toUpperCase());
                holder.litSubTitle.setText(cursor.getString(mName));
                holder.icon.setImageResource(R.drawable.icon_note_activity);
                break;

            default:
                break;
        }

        Info oi = new Info();
        oi._id = cursor.getLong(cursor.getColumnIndex("_id"));
        oi.ID = cursor.getLong(mID);
        oi.SectionID = cursor.getLong(mSectionID);
        oi.StatusID = cursor.getLong(0);
        oi.text = cursor.getString(mName);
        holder.icon.setTag(oi);
    }

    private boolean isNewGroup(Cursor cursor, int position) {
        // Get date values for current and previous data items

        long nWhenThis = cursor.getLong(mDate);

        cursor.moveToPosition(position - 1);
        long nWhenPrev = cursor.getLong(mDate);

        // Restore cursor position

        cursor.moveToPosition(position);

        // Compare date values, ignore time values

        Calendar calThis = Calendar.getInstance();
        calThis.setTimeInMillis(nWhenThis);

        Calendar calPrev = Calendar.getInstance();
        calPrev.setTimeInMillis(nWhenPrev);

        int nDayThis = calThis.get(Calendar.DAY_OF_YEAR);
        int nDayPrev = calPrev.get(Calendar.DAY_OF_YEAR);

        if (nDayThis != nDayPrev || calThis.get(Calendar.YEAR) != calPrev.get(Calendar.YEAR)) {
            return true;
        }
        return false;
    }

    static class ViewHolder {
        TextView litHeader;
        TextView litTitle;
        TextView litSubTitle;
        TextView litDate;
        TextView litTotal;
        TextView litSupTotal;
        TextView litStatus;
        CheckBox chk;
        ImageView icon;
        ProgressBar progress;
        View actionsView;
        int position;
    }

    static class Info {
        Long _id;
        Long ID;
        Long SectionID;
        Long StatusID;
        String text;
    }
}

