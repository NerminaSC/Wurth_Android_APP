package ba.wurth.mb.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import ba.wurth.mb.Classes.Objects.Document;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.R;

public class ImageAdapter extends BaseAdapter{

    private static LayoutInflater inflater = null;
    private Activity activity;

    private String mode = "";
    private ArrayList<Document> items;

    public ImageAdapter(Activity act, String mode, ArrayList<Document> items){
        inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        activity = act;
        this.mode = mode;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        try {
            if(mode.equalsIgnoreCase("grid")){
                if (view == null) {
                    view = inflater.inflate(R.layout.grid_image, null);
                }
            } else if(mode.equalsIgnoreCase("gallery")){
                if (view == null) view = inflater.inflate(R.layout.gallery_images_item, null);

                if (position <= getCount() - 1) {
                    ImageView iv = (ImageView)view.findViewById(R.id.imageView);

                    if (items.get(position).data != null && items.get(position).data.length > 0) {
                        Bitmap b = BitmapFactory.decodeByteArray(items.get(position).data, 0, items.get(position).data.length);
                        iv.setImageBitmap(b);
                    }
                    else if (items.get(position).url != null && !items.get(position).url.equals("")) {
                        wurthMB.imageLoader.DisplayImage(items.get(position).url, iv);
                    }
                }
            }
        }
        catch (Exception ex) {

        }
        return view;
    }
}
