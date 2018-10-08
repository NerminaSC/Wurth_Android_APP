package ba.wurth.mb.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import ba.wurth.mb.R;

public class AutoCompleteProductsAdapter extends SimpleCursorAdapter
{
    private static final String[] mFields = { "_id", "Name" };
    private static final String[] mVisible = { "Name" };
    private static final int[] mViewIds = { android.R.id.text1 };
    private Context mContext;

    public AutoCompleteProductsAdapter(Context context)
    {
        super(context, R.layout.search_list_item, null, mVisible, mViewIds, 0);
        mContext = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String section = "";

        if (cursor.getInt(cursor.getColumnIndex("Status_Artikla")) == 2) {
            section = "<font color='#ff0000'>IZBAČEN</font>";
        }

        if (cursor.getInt(cursor.getColumnIndex("Status_Artikla")) == 3) {
            section = "ZAMJENJEN";
        }

        if (cursor.getInt(cursor.getColumnIndex("Status_Artikla")) == 4) {
            section = "<font color='#ff0000'>IZBAČEN SA PREPORUKOM</font>";
        }

        if (!cursor.isNull(cursor.getColumnIndex("Zamjenski_Artikal")) && cursor.getLong(cursor.getColumnIndex("Zamjenski_Artikal")) > 0L) {
            section += ", preporuka " + cursor.getString(cursor.getColumnIndex("Zamjenski_Sifra"));
        }

        ((TextView) view.findViewById(R.id.litTitle)).setText(cursor.getString(1));
        view.findViewById(R.id.litTitle).setContentDescription(cursor.getInt(cursor.getColumnIndex("Status_Artikla")) == 3 && !cursor.isNull(cursor.getColumnIndex("Zamjenski_Artikal")) && cursor.getLong(cursor.getColumnIndex("Zamjenski_Artikal")) > 0 ? cursor.getString(cursor.getColumnIndex("Zamjenski_Artikal")) : cursor.getString(cursor.getColumnIndex("_id")));
        ((TextView) view.findViewById(R.id.litCode)).setText(cursor.getString(2));
        ((TextView) view.findViewById(R.id.litSection)).setText(Html.fromHtml(section));
    }
}