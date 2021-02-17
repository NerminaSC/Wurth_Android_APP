package ba.wurth.mb.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;

import ba.wurth.mb.Classes.wurthMB;
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

        try {
            String section = "";
            String id = cursor.getString(cursor.getColumnIndex("_id"));

            if (cursor.getInt(cursor.getColumnIndex("Status_Artikla")) == 2) {
                section = "<font color='#ff0000'>Artikal je izbačen</font>";
            }

            if (cursor.getInt(cursor.getColumnIndex("Status_Artikla")) == 3) {
                section = "ZAMJENJEN";

                if (!cursor.isNull(cursor.getColumnIndex("Zamjenski_Artikal")) && cursor.getLong(cursor.getColumnIndex("Zamjenski_Artikal")) > 0L) {
                    section = "Artikal je zamjenjen sa " + cursor.getString(cursor.getColumnIndex("Zamjenski_Sifra"));
                    id = cursor.getString(cursor.getColumnIndex("Zamjenski_Artikal"));
                }
            }

            if (cursor.getInt(cursor.getColumnIndex("Status_Artikla")) == 4) {
                section = "<font color='#ff0000'>Artikal je izbačen</font>";

                if (!cursor.isNull(cursor.getColumnIndex("Zamjenski_Artikal")) && cursor.getLong(cursor.getColumnIndex("Zamjenski_Artikal")) > 0L) {
                    section += ", preporuka " + cursor.getString(cursor.getColumnIndex("Zamjenski_Sifra"));
                    id = cursor.getString(cursor.getColumnIndex("Zamjenski_Artikal"));
                }
            }

            /*if (cursor.getInt(cursor.getColumnIndex("Status_Artikla")) == 5 && cursor.getCount() < 10) {

                // ovako nece moci predugo traje
                Integer unit_in_stock = Integer.parseInt(new wurthMB.GET_LiveStatus(cursor.getLong(cursor.getColumnIndex("_id"))).execute().get());

                if(unit_in_stock > 0){
                    section = "";
                }

                if(unit_in_stock == 0){

                    if (!cursor.isNull(cursor.getColumnIndex("Zamjenski_Artikal")) && cursor.getLong(cursor.getColumnIndex("Zamjenski_Artikal")) > 0L) {
                        section = "Preporuka " + cursor.getString(cursor.getColumnIndex("Zamjenski_Sifra"));
                    }

                    if(!cursor.isNull(cursor.getColumnIndex("Zamjenski_Artikal")) && cursor.getLong(cursor.getColumnIndex("Zamjenski_Artikal")) == 0L){
                        section = "<font color='#ff0000'>IZBAČEN</font>";
                    }
                }
            }*/

            ((TextView) view.findViewById(R.id.litTitle)).setText(cursor.getString(1));
            view.findViewById(R.id.litTitle).setContentDescription(id);
            ((TextView) view.findViewById(R.id.litCode)).setText(cursor.getString(2));
            ((TextView) view.findViewById(R.id.litSection)).setText(Html.fromHtml(section));
        }
        catch (Exception e){

        }
    }
}