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
import java.util.Calendar;
import java.util.Date;

import ba.wurth.mb.Activities.Orders.OrderActivity;
import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Order;
import ba.wurth.mb.Classes.Objects.OrderItem;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Orders.DL_Orders;
import ba.wurth.mb.Fragments.Orders.OrdersFragment;
import ba.wurth.mb.Fragments.Orders.ProposalsFragment;
import ba.wurth.mb.Fragments.Orders.RFQFragment;
import ba.wurth.mb.R;

public class OrdersAdapter extends CursorAdapter {

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

    private int mColOrderDate;
    private int mColGrandTotal;
    //private int mColPaymentDate;
    //private int mColDeliveryDate;
    private int mColSync;
    private int mOrderStatusID;
    private int mOrderID;
    //private int mClientID;
    private int mClientName;
    private int mDeliveryPlace;
    private int mUsername;
    //private int m_id;

    private View actionsView;

    static final int MIN_DISTANCE = 100;
    private float downX, downY, upX, upY;

    private static SimpleDateFormat gDateFormatDataItem = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat gDateFormatDateItemTime = new SimpleDateFormat("HH:mm");

    public OrdersAdapter(Context context, Cursor cursor, Fragment fragment, Boolean... params) {
        super(context, cursor);

        mContext = context;
        mFragment = fragment;

        // Get the layout inflater

        mInflater = LayoutInflater.from(mContext);

        // Get and cache column indices
        //m_id = cursor.getColumnIndex("_id");
        mColOrderDate = cursor.getColumnIndex("OrderDate");
        mColGrandTotal = cursor.getColumnIndex("GrandTotal");
        //mColPaymentDate = cursor.getColumnIndex("PaymentDate");
        //mColDeliveryDate = cursor.getColumnIndex("DeliveryDate");
        mOrderStatusID = cursor.getColumnIndex("OrderStatusID");
        mOrderID = cursor.getColumnIndex("OrderID");

        //mClientID = cursor.getColumnIndex("ClientID");
        mClientName = cursor.getColumnIndex("ClientName");
        mDeliveryPlace = cursor.getColumnIndex("DeliveryPlace");
        mUsername = cursor.getColumnIndex("Username");
        mColSync = cursor.getColumnIndex("Sync");
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
        boolean newGroup = isNewGroup(cursor, position);

        // Check item grouping
        if (newGroup) return VIEW_TYPE_GROUP_START;
        else return VIEW_TYPE_GROUP_CONT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        final View v = mInflater.inflate(R.layout.list_row, parent, false);

        ViewHolder holder = new ViewHolder();

        holder.litTitle = (TextView) v.findViewById(R.id.litTitle);
        holder.litSubTitle = (TextView) v.findViewById(R.id.litSubTitle);
        holder.litSupTitle = (TextView) v.findViewById(R.id.litSupTitle);
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
                    OrderInfo oi = (OrderInfo) vh.litStatus.getTag();

                    if (oi == null || oi._id == 0) return;
                    if (oi == null || oi.OrderStatusID == 9) return;

                    Intent k = new Intent(mFragment.getActivity(), OrderActivity.class);
                    k.putExtra("_id", oi._id);
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

        int position = cursor.getPosition();
        int nViewType;

        ViewHolder holder  =   (ViewHolder)    view.getTag();

        if (position == 0) nViewType = VIEW_TYPE_GROUP_START;
        else {
            boolean newGroup = isNewGroup(cursor, position);
            if (newGroup) nViewType = VIEW_TYPE_GROUP_START;
            else nViewType = VIEW_TYPE_GROUP_CONT;
        }

        holder.litHeader.setText(gDateFormatDataItem.format(new Date(cursor.getLong(mColOrderDate))));

        holder.litTitle.setText(cursor.getString(mClientName));
        holder.litTitle.setContentDescription(cursor.getString(cursor.getColumnIndex("_id")));

        holder.litSubTitle.setText(cursor.getString(mOrderID));
        holder.litSupTitle.setText(cursor.getString(mUsername));

        holder.litTotal.setText(CustomNumberFormat.GenerateFormatCurrency(cursor.getDouble(mColGrandTotal)));
        holder.litSupTotal.setText(gDateFormatDateItemTime.format(new Date(cursor.getLong(mColOrderDate))));

        holder.chk.setContentDescription(cursor.getString(cursor.getColumnIndex("_id")));

        if (showCheckBox) {
            ext = true;
            if (checkedItems.contains(cursor.getLong(cursor.getColumnIndex("_id")))) {
                holder.chk.setChecked(true);
            }
            else holder.chk.setChecked(false);
            ext = false;
        }

        OrderInfo oi = new OrderInfo();
        oi._id = cursor.getLong(cursor.getColumnIndex("_id"));
        oi.OrderStatusID = cursor.getInt(mOrderStatusID);
        oi.Sync = cursor.getInt(mColSync);
        oi.ClientID = cursor.getLong(cursor.getColumnIndex("ClientID"));
        oi.OrderID = cursor.getLong(cursor.getColumnIndex("OrderID"));
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

        if (cursor.getString(mClientName).length() > 0) holder.litStatus.setText(cursor.getString(mClientName).substring(0,1));

        if (cursor.getInt(mColSync) == 1) {
            holder.litTotal.setTextColor(Color.parseColor("#27ae60"));
        }
        else holder.litTotal.setTextColor(Color.parseColor("#333333"));


        if (cursor.getInt(mColSync) == 1 && (cursor.getInt(mOrderStatusID) == 4 || cursor.getInt(mOrderStatusID) == 5)) {
            holder.litStatus.setBackgroundColor(Color.parseColor("#27ae60"));
        }
        else if (cursor.getInt(mOrderStatusID) == 3 && cursor.getInt(mColSync) == 0) {
            holder.litStatus.setBackgroundColor(Color.parseColor("#2980b9"));
        }
        else if (cursor.getInt(mOrderStatusID) == 8 || cursor.getInt(mOrderStatusID) == 9) {
            holder.litStatus.setBackgroundColor(Color.parseColor("#C0392B"));
            holder.litTotal.setTextColor(Color.parseColor("#C0392B"));
        }
        else if (cursor.getInt(mOrderStatusID) == 1) {
            holder.litStatus.setBackgroundColor(Color.parseColor("#2c3e50"));
        }
        else {
            holder.litStatus.setBackgroundColor(Color.parseColor("#2980b9"));
        }
    }

    private boolean isNewGroup(Cursor cursor, int position) {
        // Get date values for current and previous data items

        long nWhenThis = cursor.getLong(mColOrderDate);

        cursor.moveToPosition(position - 1);
        long nWhenPrev = cursor.getLong(mColOrderDate);

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

            if (((OrderInfo) vh.litStatus.getTag()).Sync == 1 && (((OrderInfo) vh.litStatus.getTag()).OrderStatusID == 4 || ((OrderInfo) vh.litStatus.getTag()).OrderStatusID == 5)) {
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

                                OrderInfo oi = (OrderInfo) vh.litStatus.getTag();

                                if (oi != null && oi.Sync == 0) {
                                    DL_Orders.Delete(oi._id);
                                    ((OrdersFragment) mFragment).bindData();
                                    Notifications.showNotification(mContext, mContext.getString(R.string.Success), mContext.getString(R.string.Notification_OrderDeleted), 0);
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
                        OrderInfo oi = (OrderInfo) vh.litStatus.getTag();

                        if (oi == null || oi._id == 0) return;
                        if (oi == null || oi.OrderStatusID == 9) return;

                        Intent k = new Intent(mFragment.getActivity(), OrderActivity.class);
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
                        OrderInfo oi = (OrderInfo) vh.litStatus.getTag();

                        if (oi == null || oi._id == 0) return;

                        Order o = DL_Orders.GetByID(oi._id);

                        if (o != null && o._id > 0) {
                            o._id = 0L;
                            o.OrderID = 0L;
                            o.OrderStatusID = o.OrderStatusID != 11 && o.OrderStatusID != 12 ? o.OrderStatusID = 1 : o.OrderStatusID;
                            o.Sync = 0;

                            o.DeliveryDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
                            o.PaymentDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
                            o.OrderDate = System.currentTimeMillis();

                            ArrayList<OrderItem> orderItems = o.items;

                            o.items = new ArrayList<>();

                            for (final OrderItem p: orderItems) {

                                OrderItem tempOrderItem =  new OrderItem(){{
                                    ArtikalID = p.ArtikalID;
                                    ProductID = p.ProductID;
                                    Grupa_Artikla = p.Grupa_Artikla;
                                    UserDiscountPercentage = 0D;
                                    ClientDiscountPercentage = 0D;
                                    Pakovanje = p.Pakovanje;
                                    ProductName = p.ProductName;
                                    Quantity = p.Quantity;
                                    Kod_Zbirne_Cjen_Razrade = p.Kod_Zbirne_Cjen_Razrade;
                                    Note = "";
                                }};

                                tempOrderItem = o.setDiscount(tempOrderItem);

                                if (tempOrderItem.Price_WS > 0) {
                                    o.items.add(tempOrderItem);
                                }
                            }

                            try {
                                JSONObject relation = new JSONObject(o.Relations);
                                relation.put("Action", "Copy");
                                relation.put("RefOrderID", Long.toString(o.OrderID));
                                relation.put("OrderStatusID", o.OrderStatusID);

                                if (o.OrderStatusID == 11) {
                                    relation.put("Proposal_Name", "");
                                    relation.put("Proposal_Note", "");
                                    relation.put("Proposal_IsOpened", false);
                                    relation.put("Proposal_StartDate", "");
                                    relation.put("Proposal_EndDate", "");
                                }

                                o.Relations = relation.toString();
                            }
                            catch (JSONException exx) { }

                            if (wurthMB.currentBestLocation != null) {
                                o.Latitude = (long) (wurthMB.currentBestLocation.getLatitude() * 10000000);
                                o.Longitude = (long) (wurthMB.currentBestLocation.getLongitude() * 10000000);
                            }

                            if (DL_Orders.AddOrUpdate(o) > 0) {
                                if (mFragment instanceof  OrdersFragment) ((OrdersFragment) mFragment).bindData();
                                if (mFragment instanceof  ProposalsFragment) ((ProposalsFragment) mFragment).bindData();
                                if (mFragment instanceof RFQFragment) ((RFQFragment) mFragment).bindData();
                                Notifications.showNotification(mContext, mContext.getString(R.string.Success), mContext.getString(R.string.Notification_OrderCopied), 0);
                            } else {
                                Notifications.showNotification(mContext, mContext.getString(R.string.Error), mContext.getString(R.string.Error), 1);
                            }
                        }
                    }
                    catch (Exception ex) {
                        wurthMB.AddError("Swipe Orders copy",ex.getMessage(),ex);
                    }
                }
            });

            actionsView.findViewById(R.id.btnCancel).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        OrderInfo oi = (OrderInfo) vh.litStatus.getTag();

                        if (oi == null || oi._id == 0) return;

                        Order o = DL_Orders.GetByID(oi._id);

                        if (o != null && o._id > 0) {

                            try {
                                JSONObject relation = new JSONObject(o.Relations);
                                relation.put("Action", "Void");
                                relation.put("RefOrderID", Long.toString(o.OrderID));
                                o.Relations = relation.toString();
                            }
                            catch (JSONException exx) { }

                            o._id = 0L;
                            o.OrderID = 0L;
                            o.OrderStatusID = 9;
                            o.DOE = System.currentTimeMillis();
                            o.OrderDate = System.currentTimeMillis();
                            o.DeliveryDate = System.currentTimeMillis();
                            o.PaymentDate = System.currentTimeMillis();
                            o.Sync = 0;

                            for (OrderItem item : o.items) {
                                item.Quantity = item.Quantity * -1;
                                item.DiscountTotal = item.DiscountTotal * -1;
                                item.Total = item.Total * -1;
                                item.TaxTotal = item.TaxTotal * -1;
                                item.GrandTotal = item.GrandTotal * -1;
                            }

                            o.DiscountTotal = o.DiscountTotal * -1;
                            o.Total = o.Total * -1;
                            o.TaxTotal = o.TaxTotal * -1;
                            o.GrandTotal = o.GrandTotal * -1;

                            if (wurthMB.currentBestLocation != null) {
                                o.Latitude = (long) (wurthMB.currentBestLocation.getLatitude() * 10000000);
                                o.Longitude = (long) (wurthMB.currentBestLocation.getLongitude() * 10000000);
                            }

                            if (DL_Orders.AddOrUpdate(o) > 0) {
                                ((OrdersFragment) mFragment).bindData();
                                Notifications.showNotification(mContext, mContext.getString(R.string.Success), mContext.getString(R.string.Notification_OrderCanceled), 0);
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
            wurthMB.AddError("Swipe Orders",ex.getMessage(),ex);
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
        CheckBox chk;
        ImageView icon;
        ProgressBar progress;
        View actionsView;
        int position;
    }

    static class OrderInfo {
        Long _id;
        Long OrderID;
        Long ClientID;
        Long DeliveryPlaceID;
        int OrderStatusID;
        int Sync;
    }
}

