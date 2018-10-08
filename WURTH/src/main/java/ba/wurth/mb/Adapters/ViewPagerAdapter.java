package ba.wurth.mb.Adapters;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import java.util.ArrayList;

import ba.wurth.mb.Activities.Products.ProductActivity;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Order;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Fragments.Products.ProductsListActionsFragment;
import ba.wurth.mb.R;

public class ViewPagerAdapter extends PagerAdapter {

    AppCompatActivity activity;

    ArrayList<Items> imageArray;
    Client client;

    public ViewPagerAdapter(AppCompatActivity act, ArrayList<Items> imgArra, Client mClient) {
        imageArray = imgArra;
        activity = act;
        client = mClient;
    }

    public int getCount() {
        return imageArray.size();
    }

    public Object instantiateItem(View collection, int position) {

        ImageView view = null;

        try {
            view = new ImageView(activity);
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            view.setAdjustViewBounds(true);
            view.setScaleType(ScaleType.FIT_XY);
            view.setTag(imageArray.get(position));

            if (!imageArray.get(position).slika.equals("")) {
                wurthMB.imageLoader.DisplayImage(imageArray.get(position).slika, view);
            }
            else {
                view.setImageResource(R.drawable.no_image);
            }

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View imageView) {

                    try {

                        if (wurthMB.getOrder() != null && wurthMB.getOrder().ClientID != client.ClientID) {

                            final Dialog dialog = new Dialog(activity, R.style.CustomDialog);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.alert);
                            dialog.setCancelable(false);

                            ((TextView) dialog.findViewById(R.id.Title)).setText(R.string.Notification_AttentionNeeded);
                            ((TextView) dialog.findViewById(R.id.text)).setText(R.string.Notification_OrderExist);

                            dialog.findViewById(R.id.dialogButtonNEW).setVisibility(View.GONE);
                            dialog.findViewById(R.id.dialogButtonCANCEL).setVisibility(View.VISIBLE);
                            ((Button) dialog.findViewById(R.id.dialogButtonOK)).setText(R.string.Continue);

                            dialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    wurthMB.setOrder(new Order());

                                    if (client != null) {
                                        Cursor cur = DL_Wurth.GET_Partner(client._clientid);
                                        wurthMB.getOrder().PartnerID = client._clientid;

                                        if (cur != null) {
                                            if (cur.moveToFirst()) {
                                                wurthMB.getOrder().client = DL_Wurth.GET_Client(cur.getLong(cur.getColumnIndex("ClientID")));
                                                wurthMB.getOrder().ClientID = wurthMB.getOrder().client.ClientID;
                                                wurthMB.getOrder().DeliveryPlaceID = cur.getLong(cur.getColumnIndex("DeliveryPlaceID"));
                                                wurthMB.getOrder().PaymentMethodID = 0;
                                            }
                                            cur.close();
                                        }

                                        if (!((Items) imageView.getTag()).PZNA.equals("")) {
                                            Bundle b = new Bundle();
                                            b.putString("Kod_Zbirnog_Naziva", ((Items) imageView.getTag()).PZNA);
                                            ProductsListActionsFragment productsListActionsFragment = ProductsListActionsFragment.newInstance();
                                            productsListActionsFragment.setArguments(b);
                                            productsListActionsFragment.show(activity.getSupportFragmentManager(), "productsListActionsFragment");
                                        } else if (((Items) imageView.getTag()).PZNA.equals("") && !((Items) imageView.getTag()).CatalogCode.equals("")) {
                                            Bundle b = new Bundle();
                                            b.putString("Sifra_Grupe", ((Items) imageView.getTag()).CatalogCode);
                                            ProductsListActionsFragment productsListActionsFragment = ProductsListActionsFragment.newInstance();
                                            productsListActionsFragment.setArguments(b);
                                            productsListActionsFragment.show(activity.getSupportFragmentManager(), "productsListActionsFragment");
                                        } else {
                                            Intent k = new Intent(activity, ProductActivity.class);
                                            k.putExtra("ACTION", 0);
                                            k.putExtra("ArtikalID", ((Items) imageView.getTag()).ArtikalID);
                                            k.putExtra("ProductID", ((Items) imageView.getTag()).ProductID);
                                            activity.startActivity(k);
                                        }
                                    }
                                    dialog.dismiss();
                                }
                            });

                            dialog.findViewById(R.id.dialogButtonCANCEL).setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            dialog.show();
                        } else if (wurthMB.getOrder() != null && wurthMB.getOrder().ClientID == client.ClientID) {
                            if (!((Items) imageView.getTag()).PZNA.equals("")) {
                                Bundle b = new Bundle();
                                b.putString("Kod_Zbirnog_Naziva", ((Items) imageView.getTag()).PZNA);
                                ProductsListActionsFragment productsListActionsFragment = ProductsListActionsFragment.newInstance();
                                productsListActionsFragment.setArguments(b);
                                productsListActionsFragment.show(activity.getSupportFragmentManager(), "productsListActionsFragment");
                            } else if (((Items) imageView.getTag()).PZNA.equals("") && !((Items) imageView.getTag()).CatalogCode.equals("")) {
                                Bundle b = new Bundle();
                                b.putString("Sifra_Grupe", ((Items) imageView.getTag()).CatalogCode);
                                ProductsListActionsFragment productsListActionsFragment = ProductsListActionsFragment.newInstance();
                                productsListActionsFragment.setArguments(b);
                                productsListActionsFragment.show(activity.getSupportFragmentManager(), "productsListActionsFragment");
                            } else {
                                Intent k = new Intent(activity, ProductActivity.class);
                                k.putExtra("ACTION", 0);
                                k.putExtra("ArtikalID", ((Items) imageView.getTag()).ArtikalID);
                                k.putExtra("ProductID", ((Items) imageView.getTag()).ProductID);
                                activity.startActivity(k);
                            }
                        } else {
                            wurthMB.setOrder(new Order());

                            if (client != null) {
                                Cursor cur = DL_Wurth.GET_Partner(client._clientid);
                                wurthMB.getOrder().PartnerID = client._clientid;

                                if (cur != null) {
                                    if (cur.moveToFirst()) {
                                        wurthMB.getOrder().client = DL_Wurth.GET_Client(cur.getLong(cur.getColumnIndex("ClientID")));
                                        wurthMB.getOrder().ClientID = wurthMB.getOrder().client.ClientID;
                                        wurthMB.getOrder().DeliveryPlaceID = cur.getLong(cur.getColumnIndex("DeliveryPlaceID"));
                                        wurthMB.getOrder().PaymentMethodID = 0;
                                    }
                                    cur.close();
                                }

                                if (!((Items) imageView.getTag()).PZNA.equals("")) {
                                    Bundle b = new Bundle();
                                    b.putString("Kod_Zbirnog_Naziva", ((Items) imageView.getTag()).PZNA);
                                    ProductsListActionsFragment productsListActionsFragment = ProductsListActionsFragment.newInstance();
                                    productsListActionsFragment.setArguments(b);
                                    productsListActionsFragment.show(activity.getSupportFragmentManager(), "productsListActionsFragment");
                                } else if (((Items) imageView.getTag()).PZNA.equals("") && !((Items) imageView.getTag()).CatalogCode.equals("")) {
                                    Bundle b = new Bundle();
                                    b.putString("Sifra_Grupe", ((Items) imageView.getTag()).CatalogCode);
                                    ProductsListActionsFragment productsListActionsFragment = ProductsListActionsFragment.newInstance();
                                    productsListActionsFragment.setArguments(b);
                                    productsListActionsFragment.show(activity.getSupportFragmentManager(), "productsListActionsFragment");
                                } else {
                                    Intent k = new Intent(activity, ProductActivity.class);
                                    k.putExtra("ACTION", 0);
                                    k.putExtra("ArtikalID", ((Items) imageView.getTag()).ArtikalID);
                                    k.putExtra("ProductID", ((Items) imageView.getTag()).ProductID);
                                    activity.startActivity(k);
                                }
                            }
                        }
                    } catch (Exception e) {
                        wurthMB.AddError("ViewPagerAdapter", e.getMessage(), e);
                    }
                    return true;
                }

            });

            ((ViewPager) collection).addView(view, 0);

        } catch (Exception e) {
            wurthMB.AddError("ViewPagerAdapter", e.getMessage(), e);
        }
        return view;
    }

    @Override
    public void destroyItem(View v, int arg1, Object arg2) {
        ((ViewPager) v).removeView((View) arg2);
    }

    @Override
    public boolean isViewFromObject(View v, Object arg1) {
        return v == arg1;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    public static class Items {
        public long ArtikalID = 0L;
        public long ProductID = 0L;
        public long PotencijalOD = 0;
        public long PotencijalDO = 0;
        public int ImageSize = 0;
        public String slika = "";
        public String PZNA = "";
        public String CatalogCode = "";

    }

}
