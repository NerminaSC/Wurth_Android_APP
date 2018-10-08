package ba.wurth.mb.Fragments.Clients;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class ClientImagesFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_images, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binData();
    }

    private void binData() {
        try {
            GridView gridView = (GridView) getView().findViewById(R.id.gridview);
            gridView.setAdapter(new MyAdapter(getActivity()));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    private class MyAdapter extends BaseAdapter
    {
        private List<Item> items = new ArrayList<Item>();
        private LayoutInflater inflater;

        public MyAdapter(Context context)
        {
            inflater = LayoutInflater.from(context);

            SimpleDateFormat tf = new SimpleDateFormat("dd.mm.yyyy HH:MM");

            if (getArguments() != null && getArguments().getLong("ClientID", 0L) > 0) {
                Cursor cur = DL_Wurth.GET_ClientImages(getArguments().getLong("ClientID"));

                while (cur.moveToNext()) {
                    if (!cur.isNull(cur.getColumnIndex("data"))) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(cur.getBlob(cur.getColumnIndex("data")), 0, cur.getBlob(cur.getColumnIndex("data")).length);
                        items.add(new Item(tf.format(new Date(cur.getLong(cur.getColumnIndex("startDT")) * 1000)), Common.getResizedBitmap(bitmap, 256, 256)));
                    }
                }
                cur.close();
            }
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i)
        {
            return items.get(i);
        }

        @Override
        public long getItemId(int i)
        {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            View v = view;
            ImageView picture;
            TextView name;

            if(v == null)
            {
                v = inflater.inflate(R.layout.client_images_item, viewGroup, false);
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                v.setTag(R.id.text, v.findViewById(R.id.text));
            }

            picture = (ImageView)v.getTag(R.id.picture);
            name = (TextView)v.getTag(R.id.text);

            Item item = (Item)getItem(i);

            picture.setImageBitmap(item.bmp);
            name.setText(item.name);

            return v;
        }

        private class Item
        {
            final String name;
            final Bitmap bmp;

            Item(String name, Bitmap bmp)
            {
                this.name = name;
                this.bmp = bmp;
            }
        }
    }

}
