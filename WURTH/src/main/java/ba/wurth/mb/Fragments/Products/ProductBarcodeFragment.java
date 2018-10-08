package ba.wurth.mb.Fragments.Products;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Order;
import ba.wurth.mb.Classes.Objects.OrderItem;
import ba.wurth.mb.Classes.Objects.Pricelist;
import ba.wurth.mb.Classes.Objects.Product;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.DataLayer.Pricelist.DL_Pricelist;
import ba.wurth.mb.DataLayer.Products.DL_Products;
import ba.wurth.mb.R;

public class ProductBarcodeFragment extends Fragment {

    private Button btnScan;
    private Dialog productDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.product_barcode, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnScan = (Button) getView().findViewById(R.id.btnScan);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    boolean isZxingInstalled;

                    try
                    {
                        ApplicationInfo info = getActivity().getPackageManager().getApplicationInfo("com.google.zxing.client.android", 0);
                        isZxingInstalled = true;
                    }
                    catch(PackageManager.NameNotFoundException e){
                        isZxingInstalled = false;
                    }

                    if(isZxingInstalled) //If it is then intent Zxing application
                    {
                        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                        intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
                        startActivityForResult(intent, 0);
                    }
                    else //It's not then redirect user to PlayStore-ZxingPlage
                    {
                        if(isPlayStoreInstalled())
                        {
                            Intent DownloadZxing = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.zxing.client.android"));
                            startActivity(DownloadZxing);
                        }
                        else //Toast message indicating No PlayStore Found
                        {
                            Toast.makeText(getActivity(), "Install PlayStore First", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                catch (Exception ex) {
                    wurthMB.AddError("ProductBarcodeFragment", ex.getMessage(), ex);
                }
            }
        });
    }

    private boolean isPlayStoreInstalled() {
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=dummy"));
        PackageManager manager = getActivity().getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(market, 0);

        return list.size() > 0;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if(requestCode == 0) {

                if(resultCode == -1)  {

                    String contents = data.getStringExtra("SCAN_RESULT");
                    String format = data.getStringExtra("SCAN_RESULT_FORMAT");

                    final Product p = DL_Products.GetByBarcode(contents);

                    if (p == null) {
                        Notifications.showNotification(getActivity().getApplicationContext(), "", getString(R.string.Notification_ProductNotInDatabase),2);
                    }
                    else {

                        if (productDialog != null) {
                            productDialog.dismiss();
                            productDialog = null;
                        }

                        productDialog = new Dialog(getActivity());
                        productDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                        Long ClientID = 0L;
                        Double DiscountPercentage = 0D;
                        int _WATType = 1;

                        if (wurthMB.getClient() != null) {
                            ClientID = wurthMB.getClient().ClientID;
                            DiscountPercentage = wurthMB.getClient().DiscountPercentage;
                            _WATType = wurthMB.getClient().WATType;
                        }

                        if (wurthMB.getOrder() != null) {
                            ClientID = wurthMB.getOrder().ClientID;
                            if (wurthMB.getClient() == null) {
                                Client c = DL_Clients.GetByID(wurthMB.getOrder().ClientID);
                                if (c != null) {
                                    DiscountPercentage = c.DiscountPercentage;
                                    _WATType = c.WATType;
                                }
                            }
                        }

                        final Pricelist tempProductPriceList = DL_Pricelist.GetPriceList(ClientID, p.ProductID);
                        final int WATType = _WATType;

                        productDialog.setContentView(R.layout.list_row_order_item_dialog);
                        productDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        ((TextView) productDialog.findViewById(R.id.lblProductName)).setText(p.Name);

                        Double Price = 0D;
                        Double PriceWithDiscount = 0D;

                        if (wurthMB.getOrder() != null && wurthMB.getOrder().PaymentMethodID == 303) Price = p.PriceList.Price_RT;
                        else  Price = p.PriceList.Price_WS;

                        PriceWithDiscount = Price - (Price * (DiscountPercentage / 100));

                        OrderItem el = null;

                        if (wurthMB.getOrder() != null) {
                            java.util.Iterator<OrderItem> itr = wurthMB.getOrder().items.iterator();
                            while (itr.hasNext()) {
                                OrderItem e = itr.next();
                                if (e.ProductID == p.ProductID) {
                                    el = e;
                                    break;
                                }
                            }
                        }

                        ((TextView) productDialog.findViewById(R.id.lblPrice)).setText(CustomNumberFormat.GenerateFormatCurrency(Price));
                        ((TextView) productDialog.findViewById(R.id.lblDiscount)).setText(CustomNumberFormat.GenerateFormat(DiscountPercentage) + " %");
                        ((TextView) productDialog.findViewById(R.id.lblPriceWithDiscount)).setText(CustomNumberFormat.GenerateFormatCurrency(PriceWithDiscount));
                        ((TextView) productDialog.findViewById(R.id.lblQuantity)).setText("(" + p.UOMName + ")");

                        ((EditText) productDialog.findViewById(R.id.txbUserDiscount)).setText(el != null ? Double.toString(el.UserDiscountPercentage) : "0");
                        ((EditText) productDialog.findViewById(R.id.txbQuantity)).setText(el != null ? Double.toString(el.Quantity) : "0");

                        if (el != null) {
                            ((EditText) productDialog.findViewById(R.id.txbNote)).setText(el.Note);
                        }

                        ((EditText) productDialog.findViewById(R.id.txbUserDiscount)).addTextChangedListener(new TextWatcher(){
                            public void afterTextChanged(Editable s) {
                                if (Common.isNumeric(s.toString())) {
                                    ((TextView) productDialog.findViewById(R.id.lblTotal)).setText(CustomNumberFormat.GenerateFormatCurrency(CalculateTotals(WATType == 1 ? p.PriceList.Price_WS : p.PriceList.Price_RT,  Double.parseDouble(((EditText) productDialog.findViewById(R.id.txbQuantity)).getText().toString()), tempProductPriceList != null ? tempProductPriceList.DiscountPerecentage : 0D, 0D,  Double.parseDouble(((EditText) productDialog.findViewById(R.id.txbUserDiscount)).getText().toString()))));
                                }
                                else {
                                    ((EditText) productDialog.findViewById(R.id.txbUserDiscount)).setText("0.0");
                                }
                            }
                            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
                            public void onTextChanged(CharSequence s, int start, int before, int count){}
                        });

                        ((EditText) productDialog.findViewById(R.id.txbQuantity)).addTextChangedListener(new TextWatcher(){
                            public void afterTextChanged(Editable s) {
                                if (Common.isNumeric(s.toString())) {
                                    ((TextView) productDialog.findViewById(R.id.lblTotal)).setText(CustomNumberFormat.GenerateFormatCurrency(CalculateTotals(WATType == 1 ? p.PriceList.Price_WS : p.PriceList.Price_RT,  Double.parseDouble(((EditText) productDialog.findViewById(R.id.txbQuantity)).getText().toString()), tempProductPriceList != null ? tempProductPriceList.DiscountPerecentage : 0D, 0D,  Double.parseDouble(((EditText) productDialog.findViewById(R.id.txbUserDiscount)).getText().toString()))));
                                }
                                else {
                                    ((EditText) productDialog.findViewById(R.id.txbQuantity)).setText("0.0");
                                }
                            }
                            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
                            public void onTextChanged(CharSequence s, int start, int before, int count){}
                        });


                        productDialog.findViewById(R.id.ibtnMinus_Quantity).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                double Quantity = Double.parseDouble(((EditText) productDialog.findViewById(R.id.txbQuantity)).getText().toString());
                                Quantity--;
                                if (Quantity < 0) Quantity = 0;
                                ((EditText) productDialog.findViewById(R.id.txbQuantity)).setText(Double.toString(Quantity));
                            }
                        });

                        productDialog.findViewById(R.id.ibtnPlus_Quantity).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                double Quantity = Double.parseDouble(((EditText) productDialog.findViewById(R.id.txbQuantity)).getText().toString());
                                Quantity++;
                                ((EditText) productDialog.findViewById(R.id.txbQuantity)).setText(Double.toString(Quantity));
                            }
                        });

                        productDialog.findViewById(R.id.ibtnMinus_UserDiscount).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                double UserDiscount = Double.parseDouble(((EditText) productDialog.findViewById(R.id.txbUserDiscount)).getText().toString());
                                if (!(UserDiscount - 1 < wurthMB.getUser().DiscountPercentage)) UserDiscount--;
                                else Notifications.showNotification(getActivity(), "", getString(R.string.AllowedUserDiscount),2);
                                ((EditText) productDialog.findViewById(R.id.txbUserDiscount)).setText(Double.toString(UserDiscount));
                            }
                        });

                        productDialog.findViewById(R.id.ibtnPlus_UserDiscount).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                double UserDiscount = Double.parseDouble(((EditText) productDialog.findViewById(R.id.txbUserDiscount)).getText().toString());
                                if (!(UserDiscount + 1 > wurthMB.getUser().DiscountPercentage)) UserDiscount++;
                                else Notifications.showNotification(getActivity(), "", getString(R.string.AllowedUserDiscount),2);
                                ((EditText) productDialog.findViewById(R.id.txbUserDiscount)).setText(Double.toString(UserDiscount));
                            }
                        });


                        productDialog.findViewById(R.id.btnUpdateOrder).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                final EditText txbQuantity = ((EditText) productDialog.findViewById(R.id.txbQuantity));
                                final EditText txbUserDiscount = ((EditText) productDialog.findViewById(R.id.txbUserDiscount));
                                final EditText txbNote = ((EditText) productDialog.findViewById(R.id.txbNote));

                                if (txbUserDiscount.getText().toString().length() == 0 || txbQuantity.getText().toString().length() == 0 || Double.parseDouble(txbQuantity.getText().toString()) == 0) {
                                    Notifications.showNotification(getActivity(), "", getString(R.string.EmptyFields), 1);
                                }
                                else {

                                    boolean exists = false;

                                    if (wurthMB.getOrder() == null) {
                                        wurthMB.setOrder(new Order());
                                    }

                                    java.util.Iterator<OrderItem> itr = wurthMB.getOrder().items.iterator();
                                    while (itr.hasNext()) {
                                        OrderItem e = itr.next();
                                        if(e.ProductID == p.ProductID) {
                                            if (Double.parseDouble(txbQuantity.getText().toString()) == 0) {
                                                wurthMB.getOrder().items.remove(e);
                                            }
                                            else {
                                                e.Quantity = Double.parseDouble(txbQuantity.getText().toString());
                                                e.UserDiscountPercentage = Double.parseDouble(txbUserDiscount.getText().toString());
                                                e.Note = txbNote.getText().toString();
                                            }
                                            wurthMB.getOrder().CalculateTotal();
                                            exists = true;
                                            break;
                                        }
                                    }

                                    if (!exists && Double.parseDouble(txbQuantity.getText().toString()) > 0 && p.ProductID > 0L) {
                                        OrderItem tempOrderItem =  new OrderItem(){{
                                            ProductID = p.ProductID;
                                            Quantity = Double.parseDouble(txbQuantity.getText().toString());
                                            ClientDiscountPercentage = DiscountPercentage;
                                            UserDiscountPercentage = Double.parseDouble(txbUserDiscount.getText().toString());
                                            Note = txbNote.getText().toString();
                                        }};
                                        wurthMB.getOrder().items.add(tempOrderItem);
                                        wurthMB.getOrder().CalculateTotal();
                                    }

                                    wurthMB.setOrder(wurthMB.getOrder());

                                    Notifications.showNotification(getActivity(), "", getString(R.string.Notification_ProductAdded), 0);

                                    productDialog.dismiss();
                                    productDialog = null;

                                    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                                    intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
                                    startActivityForResult(intent, 0);

                                }
                            }
                        });

                        productDialog.findViewById(R.id.btnCloseDialog).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if (productDialog != null) productDialog.dismiss();
                                productDialog = null;
                            }
                        });

                        productDialog.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if (wurthMB.getOrder() != null) {
                                    java.util.Iterator<OrderItem> itr = wurthMB.getOrder().items.iterator();
                                    while (itr.hasNext()) {
                                        OrderItem e = itr.next();
                                        if(e.ProductID == p.ProductID) {
                                            wurthMB.getOrder().items.remove(e);
                                            break;
                                        }
                                    }

                                    wurthMB.getOrder().CalculateTotal();
                                }

                                wurthMB.setOrder(wurthMB.getOrder());

                                if (productDialog != null) productDialog.dismiss();
                                productDialog = null;
                            }
                        });

                        productDialog.show();

                        ((EditText) productDialog.findViewById(R.id.txbQuantity)).selectAll();
                        InputMethodManager imm =(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                }
                else if (resultCode == 0) {

                }
            }
        }
        catch ( Exception ex) {
            wurthMB.AddError("QR Code Product", ex.getMessage(), ex);
        }
    }

    private Double CalculateTotals(Double Price, Double Quantity, Double DiscountPercentage, Double ClientDiscountPercentage, Double UserDiscountPercentage) {
        Double total;
        total = Price * Quantity;
        total = total - (DiscountPercentage * total / 100);
        total = total - (ClientDiscountPercentage * total / 100);
        total = total - (UserDiscountPercentage * total / 100);
        return total;
    }
}
