package ba.wurth.mb.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ba.wurth.mb.Classes.CategoryItem;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.R;

public class CatalogAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList data = new ArrayList();
    private LayoutInflater inflater;
    private Bitmap bmp;

    public CatalogAdapter(Context context, int layoutResourceId, ArrayList data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.inflater = ((Activity) context).getLayoutInflater();
        this.bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.Name = (TextView) row.findViewById(R.id.Name);
            holder.image = (ImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        CategoryItem item = (CategoryItem) data.get(position);
        holder.Name.setText(item.getName());

        if (item.getUrl() != null && !item.getUrl().equals("")) wurthMB.imageLoader.DisplayImage(item.getUrl(), holder.image);
        else holder.image.setImageBitmap(bmp);

        return row;
    }

    static class ViewHolder {
        TextView Name;
        ImageView image;
    }
}