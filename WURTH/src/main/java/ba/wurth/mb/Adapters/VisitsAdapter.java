package ba.wurth.mb.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ba.wurth.mb.Activities.Visits.VisitActivity;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Visits.DL_Visits;
import ba.wurth.mb.Fragments.Visits.VisitsFragment;
import ba.wurth.mb.R;

public class VisitsAdapter extends CursorAdapter {

    // We have two list item view types

    private Context mContext;
    private Fragment mFragment;

    private View actionsView;

    private static final int VIEW_TYPE_GROUP_START = 0;
    private static final int VIEW_TYPE_GROUP_CONT = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    public List<Integer> checkedItems;

    LayoutInflater mInflater;

    private int mColDate;
    private int mColSync;
    private int mClientName;
    private int mUserName;
    private int mDeliveryPlace;

    static final int MIN_DISTANCE = 100;
    private float downX, downY, upX, upY;

    private static SimpleDateFormat gDateFormatDataItem = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat gDateFormatDateItemTime = new SimpleDateFormat("HH:mm");

    public VisitsAdapter(Context context, Cursor cursor, Fragment fragment) {
        super(context, cursor);

        mContext = context;
        mFragment = fragment;

        // Get the layout inflater

        mInflater = LayoutInflater.from(mContext);

        mColDate = cursor.getColumnIndex("startDT");
        mClientName = cursor.getColumnIndex("ClientName");
        mUserName = cursor.getColumnIndex("Username");
        mDeliveryPlace = cursor.getColumnIndex("DeliveryPlace");
        mColSync = cursor.getColumnIndex("Sync");
        checkedItems = new ArrayList<Integer>();
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

        final View v = mInflater.inflate(R.layout.list_row, parent, false);

        ViewHolder holder = new ViewHolder();

        holder.litTitle = (TextView)  v.findViewById(R.id.litTitle);
        holder.litSubTitle = (TextView)  v.findViewById(R.id.litSubTitle);
        holder.litSupTitle = (TextView)  v.findViewById(R.id.litSupTitle);
        holder.litHeader = (TextView)  v.findViewById(R.id.litHeader);
        holder.litTotal = (TextView)  v.findViewById(R.id.litTotal);
        holder.litSupTotal = (TextView)  v.findViewById(R.id.litSupTotal);
        holder.litStatus = (TextView)  v.findViewById(R.id.litStatus);
        v.setTag(holder);

        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ViewHolder vh = (ViewHolder) view.getTag();
                    VisitInfo oi = (VisitInfo) vh.litStatus.getTag();

                    if (oi == null || oi._id == 0) return;


                    Intent k = new Intent(mFragment.getActivity(), VisitActivity.class);
                    k.putExtra("_id", oi._id);
                    mFragment.getActivity().startActivity(k);
                }
                catch (Exception ex) {

                }
            }
        });

        holder.litStatus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onRightToLeftSwipe(v);
            }
        });

        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onRightToLeftSwipe(view);
                return true;
            }
        });

        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN: {
                        downX = motionEvent.getX();
                        downY = motionEvent.getY();
                        return false;
                    }
                    case MotionEvent.ACTION_UP: {
                        upX = motionEvent.getX();
                        upY = motionEvent.getY();

                        float deltaX = downX - upX;
                        float deltaY = downY - upY;

                        // swipe horizontal?
                        if(Math.abs(deltaX) > MIN_DISTANCE){
                            // left or right
                            if(deltaX < 0) {
                                onRightToLeftSwipe(view);
                                return true;
                            }
                            if(deltaX > 0) {
                                onRightToLeftSwipe(view);
                                return true;
                            }
                        }
                        else {
                            return false; // We don't consume the event
                        }

                        // swipe vertical?
                        if(Math.abs(deltaY) > MIN_DISTANCE){
                            // top or down
                            if(deltaY < 0) {
                                onRightToLeftSwipe(view);
                                return true;
                            }
                            if(deltaY > 0) {
                                onRightToLeftSwipe(view);
                                return true;
                            }
                        }
                        else {
                            return false; // We don't consume the event
                        }

                        return false;
                    }
                    default:
                        break;
                }
                return false;
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

        holder.litHeader.setText(gDateFormatDataItem.format(new Date(cursor.getLong(mColDate))));

        holder.litTitle.setText(cursor.getString(mClientName));
        holder.litTitle.setContentDescription(cursor.getString(cursor.getColumnIndex("_id")));

        holder.litSubTitle.setText(cursor.getString(mDeliveryPlace));
        holder.litSupTitle.setText(cursor.getString(mUserName));

        holder.litSupTotal.setText(gDateFormatDateItemTime.format(new Date(cursor.getLong(mColDate))));


        VisitInfo oi = new VisitInfo();
        oi._id = cursor.getLong(cursor.getColumnIndex("_id"));
        oi.Sync = cursor.getInt(mColSync);
        oi.ClientID = cursor.getLong(cursor.getColumnIndex("ClientID"));
        oi.DeliveryPlaceID = cursor.getLong(cursor.getColumnIndex("DeliveryPlaceID"));
        holder.litStatus.setTag(oi);

        if (holder.actionsView != null) {
            holder.actionsView.setVisibility(View.GONE);
        }

        if (nViewType == VIEW_TYPE_GROUP_START) {
            holder.litHeader.setVisibility(View.VISIBLE);
        }
        else {
            holder.litHeader.setVisibility(View.GONE);
        }

        if (!cursor.isNull(mClientName) && cursor.getString(mClientName).length() > 0) holder.litStatus.setText(cursor.getString(mClientName).substring(0,1));

        if (cursor.getInt(mColSync) == 1) {
            holder.litStatus.setBackgroundColor(Color.parseColor("#27ae60"));
        }
        else {
            holder.litStatus.setBackgroundColor(Color.parseColor("#2980b9"));
        }
    }

    private boolean isNewGroup(Cursor cursor, int position) {
        // Get date values for current and previous data items

        long nWhenThis = cursor.getLong(mColDate);

        cursor.moveToPosition(position - 1);
        long nWhenPrev = cursor.getLong(mColDate);

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


    private final void onRightToLeftSwipe(View _v) {
        try {

            final ViewHolder vh = (ViewHolder) _v.getTag();

            FrameLayout vContainer = ((FrameLayout) _v.findViewById(R.id.vContainer));

            if (actionsView != null) {
                actionsView.setVisibility(View.GONE);
                actionsView = null;
            }

            actionsView = mInflater.inflate(R.layout.actions, null);
            actionsView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, vContainer.getMeasuredHeight(), 1));

            actionsView.findViewById(R.id.btnCancel).setVisibility(View.GONE);

            if (((VisitInfo) vh.litStatus.getTag()).Sync == 1) {
                actionsView.findViewById(R.id.btnDelete).setVisibility(View.GONE);
                actionsView.findViewById(R.id.btnEdit).setVisibility(View.GONE);
            }

            actionsView.findViewById(R.id.btnDelete).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    final Dialog dialog = new Dialog(mContext, R.style.CustomDialog);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.alert);

                    ((TextView) dialog.findViewById(R.id.text)).setText(R.string.AreYouSureDelete);

                    dialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            try {

                                VisitInfo oi = (VisitInfo) vh.litStatus.getTag();

                                if (oi != null && oi.Sync == 0) {
                                    DL_Visits.Delete(oi._id);
                                    ((VisitsFragment) mFragment).bindData();
                                    Notifications.showNotification(mContext, mContext.getString(R.string.Success), mContext.getString(R.string.Notification_VisitDeleted), 0);
                                } else {
                                    Notifications.showNotification(mContext, mContext.getString(R.string.Error), mContext.getString(R.string.Error), 1);
                                }
                            } catch (Exception e) {
                                wurthMB.AddError("OrderAdapter", e.getMessage(), e);
                            }
                            dialog.dismiss();
                        }
                    });

                    dialog.findViewById(R.id.dialogButtonCANCEL).setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });

            actionsView.findViewById(R.id.btnEdit).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        VisitInfo oi = (VisitInfo) vh.litStatus.getTag();

                        if (oi == null || oi._id == 0) return;

                        Intent k = new Intent(mFragment.getActivity(), VisitActivity.class);
                        k.putExtra("_id", oi._id);
                        mFragment.getActivity().startActivity(k);
                        actionsView.setVisibility(View.GONE);
                    }
                    catch (Exception ex) {

                    }
                }
            });

            vContainer.addView(actionsView);
            vh.actionsView = actionsView;

            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.abc_fade_in);
            animation.setStartOffset(0);
            vh.actionsView.startAnimation(animation);
        }
        catch (Exception ex) {
            wurthMB.AddError("Swipe Orders", ex.getMessage(), ex);
        }
    }

    static class ViewHolder {
        TextView litHeader;
        TextView litTitle;
        TextView litSubTitle;
        TextView litSupTitle;
        TextView litDate;
        TextView litTotal;
        TextView litSupTotal;
        TextView litStatus;
        ImageView icon;
        ProgressBar progress;
        View actionsView;
        int position;
    }

    static class VisitInfo {
        Long _id;
        Long VisitID;
        Long ClientID;
        Long DeliveryPlaceID;
        int Sync;
    }
}

