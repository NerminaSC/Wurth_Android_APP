package ba.wurth.mb.Adapters;

import android.content.Context;
import android.content.Intent;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;

import java.util.ArrayList;

import ba.wurth.mb.Activities.Clients.ClientActivity;
import ba.wurth.mb.Activities.Products.ProductsActivity;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class SearchSuggestionsAdapter extends SimpleCursorAdapter {
    private static final String[] mFields = {"_id", "result"};
    private static final String[] mVisible = {"result"};
    private static final int[] mViewIds = {android.R.id.text1};
    private Context mContext;

    public SearchSuggestionsAdapter(Context context) {
        super(context, R.layout.search_list_item, null, mVisible, mViewIds, 0);
        mContext = context;
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        return new SuggestionsCursor(constraint);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.litTitle)).setText(cursor.getString(3));
        view.findViewById(R.id.litTitle).setContentDescription(cursor.getString(2));

        ((TextView) view.findViewById(R.id.litSection)).setText(Html.fromHtml(cursor.getString(5)));
        view.findViewById(R.id.litSection).setContentDescription(cursor.getString(6));

        ((TextView) view.findViewById(R.id.litCode)).setText(cursor.getString(4));
        view.findViewById(R.id.litCode).setContentDescription(cursor.getString(1));

        final int Status = Integer.parseInt(cursor.getString(7));
        final int ViewStatus = Integer.parseInt(cursor.getString(8));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (Integer.parseInt(v.findViewById(R.id.litSection).getContentDescription().toString())) {
                    case 1: //PRODUCTS

                        if (ViewStatus == 1) {
                            Notifications.showNotification(mContext, "", mContext.getString(R.string.Notification_ProductCanNotBeViewed), 2);
                            return;
                        }

                        Intent k = new Intent(mContext, ProductsActivity.class);
                        k.putExtra("ACTION", 0);
                        k.putExtra("ProductID", Long.parseLong(v.findViewById(R.id.litTitle).getContentDescription().toString()));
                        k.putExtra("ArtikalID", Long.parseLong(v.findViewById(R.id.litCode).getContentDescription().toString()));
                        k.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        if (mContext instanceof ProductsActivity)
                            ((ProductsActivity) mContext).finish();

                        mContext.startActivity(k);
                        break;

                    case 2: //CLIENTS
                        Intent c = new Intent(mContext, ClientActivity.class);
                        c.putExtra("ClientID", Long.parseLong(v.findViewById(R.id.litTitle).getContentDescription().toString()));
                        c.putExtra("searchWord", ((TextView) v.findViewById(R.id.litTitle)).getText());
                        mContext.startActivity(c);
                        break;

                    default:
                        break;
                }
            }
        });
    }

    private static class SuggestionsCursor extends AbstractCursor {
        private ArrayList<SuggstionItem> mResults;

        public SuggestionsCursor(CharSequence constraint) {
            mResults = new ArrayList<SuggstionItem>();

            try {

                if (!TextUtils.isEmpty(constraint)) {

                    if (constraint.length() < 4) return;

                    Cursor cur = DL_Wurth.GET_Search(constraint.toString().replace(" ", ""));

                    if (cur != null) {
                        while (cur.moveToNext()) {

                            String section = "";
                            Long ID = cur.getLong(cur.getColumnIndex("ID"));

                            // STATUS 1
                            if (!cur.isNull(cur.getColumnIndex("Status_Artikla")) && cur.getInt(cur.getColumnIndex("Status_Artikla")) == 0 || cur.getInt(cur.getColumnIndex("Status_Artikla")) == 1) {
                                section = cur.getString(cur.getColumnIndex("Section"));
                            }

                            // STATUS 2
                            if (!cur.isNull(cur.getColumnIndex("Status_Artikla")) && cur.getInt(cur.getColumnIndex("Status_Artikla")) == 2) {
                                section = "<font color='#ff0000'>Artikal je izbačen</font>";
                            }

                            // STATUS 3
                            if (!cur.isNull(cur.getColumnIndex("Status_Artikla")) && cur.getInt(cur.getColumnIndex("Status_Artikla")) == 3) {
                                section = "ZAMJENJEN";

                                if (!cur.isNull(cur.getColumnIndex("Zamjenski_Artikal")) && cur.getLong(cur.getColumnIndex("Zamjenski_Artikal")) > 0L) {
                                    section = "Artikal je zamjenjen sa " + cur.getString(cur.getColumnIndex("Zamjenski_Sifra"));
                                    ID = cur.getLong(cur.getColumnIndex("Zamjenski_Artikal"));
                                }
                            }

                            // STATUS 4
                            if (!cur.isNull(cur.getColumnIndex("Status_Artikla")) && cur.getInt(cur.getColumnIndex("Status_Artikla")) == 4) {
                                section = "<font color='#ff0000'>Artikal je izbačen</font>";

                                if (!cur.isNull(cur.getColumnIndex("Zamjenski_Artikal")) && cur.getLong(cur.getColumnIndex("Zamjenski_Artikal")) > 0L) {
                                    section += ", preporuka " + cur.getString(cur.getColumnIndex("Zamjenski_Sifra"));
                                    ID = cur.getLong(cur.getColumnIndex("Zamjenski_Artikal"));
                                }
                            }

                            // STATUS 5
                           /* if (!cur.isNull(cur.getColumnIndex("Status_Artikla")) && cur.getInt(cur.getColumnIndex("Status_Artikla")) == 5 && cur.getCount() < 10) {
                                Integer unit_in_stock = Integer.parseInt(new wurthMB.GET_LiveStatus(cur.getLong(cur.getColumnIndex("ID"))).execute().get());

                                // količina na stanju je veća od tražene količine
                                if (unit_in_stock > 0) {
                                    section = "";
                                }

                                // količina na stanju je manja od tražene količine
                                if (unit_in_stock == 0) {

                                    //postoji zamjenski artikal
                                    if (!cur.isNull(cur.getColumnIndex("Zamjenski_Artikal")) && cur.getLong(cur.getColumnIndex("Zamjenski_Artikal")) > 0L) {
                                        section = "Preporuka " + cur.getString(cur.getColumnIndex("Zamjenski_Sifra"));
                                        ID = cur.getLong(cur.getColumnIndex("Zamjenski_Artikal"));
                                    }
                                    // ne postoji zamjenski artikal
                                    if (!cur.isNull(cur.getColumnIndex("Zamjenski_Artikal")) && cur.getLong(cur.getColumnIndex("Zamjenski_Artikal")) == 0L)
                                        section = "<font color='#ff0000'>IZBAČEN</font>";
                                }
                            }*/

                            SuggstionItem item = new SuggstionItem();
                            item._id = cur.getLong(cur.getColumnIndex("_id"));
                            item.ID = ID;
                            item.SectionID = cur.getLong(cur.getColumnIndex("SectionID"));
                            item.Name = cur.isNull(cur.getColumnIndex("Zbirni_Naziv")) ? cur.getString(cur.getColumnIndex("Name")) : (cur.getString(cur.getColumnIndex("Zbirni_Naziv")).equals("") ? cur.getString(cur.getColumnIndex("Name")) : cur.getString(cur.getColumnIndex("Zbirni_Naziv")));
                            item.Code = cur.getString(cur.getColumnIndex("Code"));
                            item.Status = cur.getInt(cur.getColumnIndex("Status_Artikla"));
                            item.ViewStatus = !cur.isNull(cur.getColumnIndex("Status_Prezentacije_Artikla")) ? cur.getInt(cur.getColumnIndex("Status_Prezentacije_Artikla")) : 0;
                            item.Section = section;
                            item.SectionType = cur.getInt(cur.getColumnIndex("SectionType"));
                            mResults.add(item);
                        }
                        cur.close();
                    }
                }
            } catch (Exception ex) {
                Log.d(ex.getMessage(), "");
            }

        }

        @Override
        public int getCount() {
            return mResults.size();
        }

        @Override
        public String[] getColumnNames() {
            return mFields;
        }

        @Override
        public long getLong(int column) {
            if (column == 0) {
                return mPos;
            }
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public String getString(int column) {
            if (column == 0) return Long.toString(mResults.get(mPos)._id);
            if (column == 1) return Long.toString(mResults.get(mPos).ID);
            if (column == 2) return Long.toString(mResults.get(mPos).SectionID);
            if (column == 3) return mResults.get(mPos).Name;
            if (column == 4) return mResults.get(mPos).Code;
            if (column == 5) return mResults.get(mPos).Section;
            if (column == 6) return Integer.toString(mResults.get(mPos).SectionType);
            if (column == 7) return Integer.toString(mResults.get(mPos).Status);
            if (column == 8) return Integer.toString(mResults.get(mPos).ViewStatus);

            return "";
        }

        @Override
        public short getShort(int column) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public int getInt(int column) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public float getFloat(int column) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public double getDouble(int column) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public boolean isNull(int column) {
            return false;
        }
    }

    private static class SuggstionItem {
        public long _id = 0L;
        public long ID = 0L;
        public long SectionID = 0L;
        public String Name = "";
        public String Code = "";
        public String Section = "";
        public int SectionType = -1;
        public int Status = -1;
        public int ViewStatus = -1;
    }
}