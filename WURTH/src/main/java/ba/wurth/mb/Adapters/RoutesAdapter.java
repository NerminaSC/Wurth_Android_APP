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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ba.wurth.mb.Activities.Routes.RouteActivity;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Route;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Routes.DL_Routes;
import ba.wurth.mb.Fragments.Routes.RoutesFragment;
import ba.wurth.mb.R;

public class RoutesAdapter extends CursorAdapter {

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

    private int mColName;
    private int mColDescription;
    private int mColSync;
    private int mColRaw;
    private int mCol_id;

    private View actionsView;

    static final int MIN_DISTANCE = 100;
    private float downX, downY, upX, upY;

    private static SimpleDateFormat gDateFormatDataItem = new SimpleDateFormat("EEE dd.MMM.yyyy");

    public RoutesAdapter(Context context, Cursor cursor, Fragment fragment, Boolean... params) {
        super(context, cursor);

        mContext = context;
        mFragment = fragment;

        // Get the layout inflater

        mInflater = LayoutInflater.from(mContext);

        mCol_id = cursor.getColumnIndex("_id");
        mColName = cursor.getColumnIndex("Name");
        mColDescription = cursor.getColumnIndex("Description");
        mColSync = cursor.getColumnIndex("Sync");
        mColRaw = cursor.getColumnIndex("raw");

        checkedItems = new ArrayList<Long>();

        if (params != null && params.length > 0 && params[0]) {
            showCheckBox = true;
        }
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
        return VIEW_TYPE_GROUP_CONT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        final View v = mInflater.inflate(R.layout.list_row, parent, false);

        ViewHolder holder = new ViewHolder();

        holder.litTitle = (TextView) v.findViewById(R.id.litTitle);
        holder.litSubTitle = (TextView) v.findViewById(R.id.litSubTitle);
        holder.litHeader = (TextView) v.findViewById(R.id.litHeader);
        holder.litTotal = (TextView) v.findViewById(R.id.litTotal);
        holder.litSupTotal = (TextView) v.findViewById(R.id.litSupTotal);
        holder.litStatus = (TextView) v.findViewById(R.id.litStatus);
        holder.chk = (CheckBox) v.findViewById(R.id.chk);

        if (showCheckBox) {
            holder.chk.setVisibility(View.VISIBLE);
            holder.chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (ext) return;
                    if (b) {
                        if (!(checkedItems.contains(Long.parseLong(compoundButton.getContentDescription().toString())))) {
                            checkedItems.add(Long.parseLong(compoundButton.getContentDescription().toString()));
                        }
                    }
                    else {
                        if ((checkedItems.contains(Long.parseLong(compoundButton.getContentDescription().toString())))) {
                            checkedItems.remove(Long.parseLong(compoundButton.getContentDescription().toString()));
                        }
                    }
                }
            });
        }

        holder.litStatus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onRightToLeftSwipe(v);
            }
        });

        v.setTag(holder);

        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ViewHolder vh = (ViewHolder) view.getTag();
                    ItemInfo info = (ItemInfo) vh.litStatus.getTag();

                    if (info == null || info._id == 0) return;

                    Intent k = new Intent(mFragment.getActivity(), RouteActivity.class);
                    k.putExtra("_id", info._id);
                    mFragment.getActivity().startActivity(k);

                }
                catch (Exception ex) {

                }
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

        int nViewType;

        ViewHolder holder  =   (ViewHolder)    view.getTag();

        nViewType = VIEW_TYPE_GROUP_CONT;

        try {

            JSONObject jsonObject = new JSONObject(cursor.getString(mColRaw));

            holder.litStatus.setText(Integer.toString(jsonObject.getJSONArray("clients").length()));
            String statusText = "";

            switch (jsonObject.getInt("StatusID")) {
                case 1:
                    statusText = mContext.getString(R.string.Draft);
                    holder.litStatus.setBackgroundColor(Color.parseColor("#354B60"));
                    break;
                case 2:
                    statusText = mContext.getString(R.string.Sent);
                    holder.litStatus.setBackgroundColor(Color.parseColor("#2D9B68"));
                    break;
                case 5:
                    statusText = mContext.getString(R.string.Rejected);
                    holder.litStatus.setBackgroundColor(Color.parseColor("#CE3234"));
                    break;
                case 7:
                    statusText = mContext.getString(R.string.OrderStatus_Approved);
                    holder.litStatus.setBackgroundColor(Color.parseColor("#9B9B36"));
                    break;
                default:break;
            }

            holder.litSupTotal.setText(statusText);

            if (jsonObject.getInt("daily") == 1) {
                holder.litTotal.setText(jsonObject.getString("date"));
            }
            else {
                String day = "";

                switch (jsonObject.getInt("day")) {
                    case 1:
                        day = mFragment.getString(R.string.Monday);
                        break;
                    case 2:
                        day = mFragment.getString(R.string.Tuesday);
                        break;
                    case 3:
                        day = mFragment.getString(R.string.Wednesday);
                        break;
                    case 4:
                        day = mFragment.getString(R.string.Thursday);
                        break;
                    case 5:
                        day = mFragment.getString(R.string.Friday);
                        break;
                    case 6:
                        day = mFragment.getString(R.string.Saturday);
                        break;
                    case 7:
                        day = mFragment.getString(R.string.Sunday);
                        break;
                    default:
                        break;
                }
                holder.litTotal.setText(day);
            }
        } catch (JSONException e) {

        }


        holder.litTitle.setText(cursor.getString(mColName));
        holder.litTitle.setContentDescription(cursor.getString(mCol_id));
        holder.litSubTitle.setText(cursor.getString(mColDescription));

        holder.chk.setContentDescription(cursor.getString(cursor.getColumnIndex("_id")));

        if (showCheckBox) {
            ext = true;
            if (checkedItems.contains(cursor.getLong(cursor.getColumnIndex("_id")))) {
                holder.chk.setChecked(true);
            }
            else holder.chk.setChecked(false);
            ext = false;
        }

        ItemInfo oi = new ItemInfo();
        oi._id = cursor.getLong(mCol_id);
        oi.RouteID = cursor.getLong(mCol_id);
        oi.Sync = cursor.getInt(mColSync);
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

                if (((ItemInfo) vh.litStatus.getTag()).Sync == 1) {
                    actionsView.findViewById(R.id.btnDelete).setVisibility(View.GONE);
                    actionsView.findViewById(R.id.btnEdit).setVisibility(View.GONE);
                }
                else {
                    actionsView.findViewById(R.id.btnCancel).setVisibility(View.GONE);
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

                                    ItemInfo oi = (ItemInfo) vh.litStatus.getTag();

                                    if (oi != null && oi.Sync == 0) {
                                        DL_Routes.Delete(oi._id);
                                        ((RoutesFragment) mFragment).bindData();
                                        Notifications.showNotification(mContext, mContext.getString(R.string.Success), mContext.getString(R.string.Notification_RouteDeleted), 0);
                                    } else {
                                        Notifications.showNotification(mContext, mContext.getString(R.string.Error), mContext.getString(R.string.Error), 1);
                                    }
                                } catch (Exception e) {
                                    wurthMB.AddError("RoutesAdapter", e.getMessage(), e);
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
                            ItemInfo oi = (ItemInfo) vh.litStatus.getTag();

                            if (oi == null || oi._id == 0) return;

                            Intent k = new Intent(mFragment.getActivity(), RouteActivity.class);
                            k.putExtra("_id", oi._id);
                            mFragment.getActivity().startActivity(k);
                            actionsView.setVisibility(View.GONE);
                        }
                        catch (Exception ex) {

                        }
                    }
                });

                actionsView.findViewById(R.id.btnCopy).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        try {
                            ItemInfo oi = (ItemInfo) vh.litStatus.getTag();

                            if (oi == null || oi._id == 0) return;

                            Route o = DL_Routes.GetByID(oi._id);

                            if (o != null && o._id > 0) {
                                o._id = 0L;
                                o.RouteID = 0L;
                                o.Sync = 0;
                                o.DOE = System.currentTimeMillis();

                                //if (o.Date > 0L) o.Date = System.currentTimeMillis();

                                if (DL_Routes.AddOrUpdate(o) > 0) {
                                    ((RoutesFragment) mFragment).bindData();
                                    Notifications.showNotification(mContext, mContext.getString(R.string.Success), mContext.getString(R.string.Notification_RouteCopied), 0);
                                } else {
                                    Notifications.showNotification(mContext, mContext.getString(R.string.Error), mContext.getString(R.string.Error), 1);
                                }
                            }
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

    static class ItemInfo {
        Long _id;
        Long RouteID;
        int OrderStatusID;
        int Sync;
    }
}

