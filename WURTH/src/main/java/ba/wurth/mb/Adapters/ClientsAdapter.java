package ba.wurth.mb.Adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ba.wurth.mb.R;

public class ClientsAdapter extends BaseAdapter {

    public static abstract class Row {}

    public static final class Section extends Row {
        public final String text;

        public Section(String text) {
            this.text = text;
        }
    }

    public static final class Item extends Row {
        public final String title;
        public final String subtitle;
        public final String desc;
        public final Long ClientID;
        public final Long DeliveryPlaceID;
        public final Long _id;


        public Item(String title, String subtitle, String desc, Long ClientID, Long DeliveryPlaceID, Long _id) {
            this.title = title;
            this.subtitle = subtitle;
            this.desc = desc;
            this.ClientID = ClientID;
            this.DeliveryPlaceID = DeliveryPlaceID;
            this._id = _id;
        }
    }

    private List<Row> rows;

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Row getItem(int position) {
        return rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof Section) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (getItemViewType(position) == 0) { // Item

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_alphabet_row, parent, false);
            }

            Item item = (Item) getItem(position);

            TextView litTitle = (TextView) view.findViewById(R.id.litTitle);
            litTitle.setText(item.title);

            TextView litSubTitle = (TextView) view.findViewById(R.id.litSubTitle);
            litSubTitle.setText(Html.fromHtml(item.desc));

        } else { // Section

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_alphabet_row_header, parent, false);
            }

            Section section = (Section) getItem(position);
            TextView litHeader = (TextView) view.findViewById(R.id.litHeader);
            litHeader.setText(section.text);
        }

        return view;
    }

}