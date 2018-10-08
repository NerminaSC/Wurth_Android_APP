package ba.wurth.mb.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

import ba.wurth.mb.R;

public class AutoCompleteClientsAdapter extends SimpleCursorAdapter
{
    private static final String[] mFields = { "_id", "Name" };
    private static final String[] mVisible = { "Name" };
    private static final int[] mViewIds = { android.R.id.text1 };
    private Context mContext;

    public AutoCompleteClientsAdapter(Context context)
    {
        super(context, R.layout.search_list_item, null, mVisible, mViewIds, 0);
        mContext = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.litTitle)).setText(cursor.getString(5));
        view.findViewById(R.id.litTitle).setContentDescription(cursor.getString(0));
        ((TextView) view.findViewById(R.id.litSection)).setText(cursor.getString(7));
    }
}