package ba.wurth.mb.Activities;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

import ba.wurth.mb.Activities.Activities.ActivityActivity;
import ba.wurth.mb.Activities.Clients.ClientAddActivity;
import ba.wurth.mb.Activities.Orders.OrderActivity;
import ba.wurth.mb.Activities.Products.ProductsActivity;
import ba.wurth.mb.Activities.Reports.LRCActivity;
import ba.wurth.mb.Activities.Routes.RouteActivity;
import ba.wurth.mb.Activities.Tasks.TaskCreateActivity;
import ba.wurth.mb.Activities.Visits.VisitActivity;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.ImageLoader;
import ba.wurth.mb.Classes.NavDrawer.AbstractNavDrawerActivity;
import ba.wurth.mb.Classes.NavDrawer.NavDrawerActivityConfiguration;
import ba.wurth.mb.Classes.NavDrawer.NavDrawerAdapter;
import ba.wurth.mb.Classes.NavDrawer.NavMenuItem;
import ba.wurth.mb.Classes.NavDrawer.NavMenuSection;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.Fragments.Clients.ClientsFragment;
import ba.wurth.mb.Fragments.Orders.NegotiatedPriceFragment;
import ba.wurth.mb.Fragments.Orders.NegotiatedPricesFragment;
import ba.wurth.mb.Fragments.Orders.OrdersFragment;
import ba.wurth.mb.Fragments.Orders.OrsyPriceFragment;
import ba.wurth.mb.Fragments.Orders.OrsyPricesFragment;
import ba.wurth.mb.Fragments.Orders.ProposalsFragment;
import ba.wurth.mb.Fragments.Orders.RFQFragment;
import ba.wurth.mb.Fragments.Reports.ReportsFragment;
import ba.wurth.mb.Fragments.Routes.RoutesFragment;
import ba.wurth.mb.Fragments.StartFragment;
import ba.wurth.mb.Fragments.Synchronization.SyncFragment;
import ba.wurth.mb.Fragments.Tasks.TasksFragment;
import ba.wurth.mb.Fragments.Visits.VisitsFragment;
import ba.wurth.mb.Interfaces.NavDrawerItem;
import ba.wurth.mb.R;
import ba.wurth.mb.Services.LocationService;
import ba.wurth.mb.Services.NotificationService;
import ba.wurth.mb.Services.SyncService;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class HomeActivity extends AbstractNavDrawerActivity {

    private Dialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null ) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content, new StartFragment(), "HOME").commit();
            //getSupportFragmentManager().beginTransaction().add(R.id.content, new SyncFragment(), "SYNC").addToBackStack(null).commit();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*** Check if services is running ***/
        if (!Common.isServiceRunning(HomeActivity.this, SyncService.class.getName()) && wurthMB.SYNC_ENABLED) startService(new Intent(this,SyncService.class));
        if (!Common.isServiceRunning(HomeActivity.this, LocationService.class.getName())) startService(new Intent(this,LocationService.class));
        if (!Common.isServiceRunning(HomeActivity.this, NotificationService.class.getName())) startService(new Intent(this,NotificationService.class));

        checkSettings();

        if (wurthMB.getOrder() != null && wurthMB.getOrder().ClientID > 0) {
            Client c = DL_Wurth.GET_Client(wurthMB.getOrder().ClientID);
            if (c != null) {
                getSupportActionBar().setTitle(c.Name);
                getSupportActionBar().setSubtitle(c.code);
            }
        }
        else {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setSubtitle("");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                quitApp();
                return true;
            }
            else {

            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected NavDrawerActivityConfiguration getNavDrawerConfiguration() {

        NavDrawerItem[] menu = new NavDrawerItem[] {

                NavMenuSection.create(100, getString(R.string.Actions).toUpperCase()),
                NavMenuItem.create(101,getString(R.string.CreateOrder), "icon_createorder_drawer", false, this),
                NavMenuItem.create(102,getString(R.string.NoteVisit), "icon_notevisit_drawer", false, this),
                NavMenuItem.create(103,getString(R.string.AddClient), "icon_addclient_drawer", false, this),
                NavMenuItem.create(104,getString(R.string.CreateRoute), "icon_createroute_drrawer", false, this),
                NavMenuItem.create(105,getString(R.string.MakeTask), "icon_create_task_drawer", false, this),
                NavMenuItem.create(106,getString(R.string.CheckLRC), "icon_lrc_drawer", false, this),
                NavMenuItem.create(107,getString(R.string.Activity), "icon_note_activity_drawer", false, this),
                NavMenuItem.create(108,getString(R.string.NegotiatePrice), getString(R.string.NegotiatePrice), false, this),
                NavMenuItem.create(109,getString(R.string.OrsyPrice), getString(R.string.OrsyPrice), false, this),

                NavMenuSection.create(200, getString(R.string.Sections).toUpperCase()),
                NavMenuItem.create(201, getString(R.string.Clients), getString(R.string.Clients), false, this),
                NavMenuItem.create(202, getString(R.string.Orders), getString(R.string.Orders), false, this),
                NavMenuItem.create(203, getString(R.string.Products), getString(R.string.Products), false, this),
                NavMenuItem.create(204, getString(R.string.Visits), getString(R.string.Visits), false, this),
                NavMenuItem.create(205, getString(R.string.Draft), getString(R.string.Draft), false, this),
                NavMenuItem.create(206, getString(R.string.Routes), getString(R.string.Routes), false, this),
                NavMenuItem.create(207, getString(R.string.Proposals), getString(R.string.Proposals), false, this),
                NavMenuItem.create(208,getString(R.string.Tasks), getString(R.string.Tasks), false, this),
                NavMenuItem.create(209,getString(R.string.Requests), getString(R.string.Requests), false, this),
                NavMenuItem.create(210,getString(R.string.NegotiatedPrice), getString(R.string.NegotiatedPrice), false, this),
                NavMenuItem.create(211,getString(R.string.OrsyPrices), getString(R.string.OrsyPrices), false, this),

                NavMenuItem.create(400, getString(R.string.Reports),  getString(R.string.Reports), false, this),

                NavMenuSection.create(300, getString(R.string.Options).toUpperCase()),
                NavMenuItem.create(301, getString(R.string.SynchronizeData), getString(R.string.SynchronizeData), false, this),
                NavMenuItem.create(302, "System", "System", false, this),
                NavMenuItem.create(309, getString(R.string.Quit), getString(R.string.Quit), false, this)

        };

        NavDrawerActivityConfiguration navDrawerActivityConfiguration = new NavDrawerActivityConfiguration();
        navDrawerActivityConfiguration.setMainLayout(R.layout.home);
        navDrawerActivityConfiguration.setDrawerLayoutId(R.id.drawer_layout);
        navDrawerActivityConfiguration.setLeftDrawerId(R.id.left_drawer);
        navDrawerActivityConfiguration.setNavItems(menu);
        navDrawerActivityConfiguration.setDrawerShadow(R.drawable.drawer_shadow);
        navDrawerActivityConfiguration.setDrawerOpenDesc(R.string.navigation_drawer_open);
        navDrawerActivityConfiguration.setDrawerCloseDesc(R.string.navigation_drawer_close);
        navDrawerActivityConfiguration.setBaseAdapter(new NavDrawerAdapter(this, R.layout.navdrawer_item, menu ));
        return navDrawerActivityConfiguration;
    }

    @Override
    protected void onNavItemSelected(int id) {

        getSupportFragmentManager().popBackStackImmediate();

        switch (id) {
            case 101:
                startActivity(new Intent(this, OrderActivity.class));
                break;
            case 102:
                startActivity(new Intent(this, VisitActivity.class));
                break;
            case 103:
                startActivity(new Intent(this, ClientAddActivity.class));
                break;
            case 104:
                startActivity(new Intent(this, RouteActivity.class));
                break;
            case 105:
                startActivity(new Intent(this, TaskCreateActivity.class));
                break;
            case 106:
                startActivity(new Intent(this, LRCActivity.class));
                break;
            case 107:
                startActivity(new Intent(this, ActivityActivity.class));
                break;
            case 108:
                getSupportFragmentManager().beginTransaction().add(R.id.content, new NegotiatedPriceFragment(), "NEGOTIATED PRICE").addToBackStack(null).commit();
                break;
            case 109:
                getSupportFragmentManager().beginTransaction().add(R.id.content, new OrsyPriceFragment(), "ORSY PRICE").addToBackStack(null).commit();
                break;


            case 201:
                getSupportFragmentManager().beginTransaction().add(R.id.content, new ClientsFragment(), "CLIENTS").addToBackStack(null).commit();
                break;

            case 202:
                getSupportFragmentManager().beginTransaction().add(R.id.content, new OrdersFragment(), "ORDERS").addToBackStack(null).commit();
                break;

            case 203:
                startActivity(new Intent(this, ProductsActivity.class));
                break;

            case 204:
                getSupportFragmentManager().beginTransaction().add(R.id.content, new VisitsFragment(), "VISITS").addToBackStack(null).commit();
                break;

            case 205:
                Fragment of = new OrdersFragment();
                Bundle b = new Bundle();
                b.putInt("OrderStatusID", 1);
                of.setArguments(b);
                getSupportFragmentManager().beginTransaction().add(R.id.content, of, "ORDERSTATUS").addToBackStack(null).commit();
                break;

            case 206:
                getSupportFragmentManager().beginTransaction().add(R.id.content, new RoutesFragment(), "ROUTES").addToBackStack(null).commit();
                break;

            case 207:
                Fragment pf = new ProposalsFragment();
                Bundle bf = new Bundle();
                bf.putInt("OrderStatusID", 11);
                pf.setArguments(bf);
                getSupportFragmentManager().beginTransaction().add(R.id.content, pf, "PROPOSALS").addToBackStack(null).commit();
                break;

            case 208:
                getSupportFragmentManager().beginTransaction().add(R.id.content, new TasksFragment(), "TASKS").addToBackStack(null).commit();
                break;

            case 209:
                Fragment RFQf = new RFQFragment();
                Bundle RFQfBundle = new Bundle();
                RFQfBundle.putInt("OrderStatusID", 12);
                RFQf.setArguments(RFQfBundle);
                getSupportFragmentManager().beginTransaction().add(R.id.content, RFQf, "PROPOSALS").addToBackStack(null).commit();
                break;

            case 210:
                getSupportFragmentManager().beginTransaction().add(R.id.content, new NegotiatedPricesFragment(), "NEGOTIATED PRICE").addToBackStack(null).commit();
                break;

            case 211:
                getSupportFragmentManager().beginTransaction().add(R.id.content, new OrsyPricesFragment(), "ORSY PRICE").addToBackStack(null).commit();
                break;

            case 301:
                getSupportFragmentManager().beginTransaction().add(R.id.content, new SyncFragment(), "SYNC").addToBackStack(null).commit();
                break;

            case 302:

                mDialog = new Dialog(HomeActivity.this, android.R.style.Theme_Black_NoTitleBar);
                mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mDialog.setContentView(R.layout.system);

                mDialog.findViewById(R.id.btnCheckInternet).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            new NetworkTask().execute("http://www.sourcecode.ba");
                        } catch (Exception e) {

                        }
                    }
                });

                mDialog.findViewById(R.id.btnCheckService).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            new NetworkTask().execute("http://ws.optimus.ba/CUSTOM/MB/Wurth/Wurth.asmx");
                        } catch (Exception e) {

                        }
                    }
                });

                mDialog.findViewById(R.id.btnClearDatabase).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        try {

                            SQLiteDatabase db = wurthMB.dbHelper.getDB();

                            ContentValues cv = new ContentValues();
                            cv.put("SignOutDate", new Date().getTime());
                            db.update("Logins", cv, "SignOutDate = 0", null);

                            wurthMB.setUser(null);
                            HomeActivity.this.deleteDatabase("wurthMB.db");

                            Intent exit_intent = new Intent(HomeActivity.this, FinishActivity.class);
                            exit_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(exit_intent);

                            mDialog.dismiss();
                            finish();
                            android.os.Process.killProcess( android.os.Process.myPid() );
                        }
                        catch (Exception ex) {
                        }
                        return true;
                    }
                });

                mDialog.findViewById(R.id.btnExit).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            Intent exit_intent = new Intent(HomeActivity.this, FinishActivity.class);
                            exit_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(exit_intent);
                            mDialog.dismiss();
                            finish();
                        }
                        catch (Exception ex) {

                        }
                    }
                });

                mDialog.findViewById(R.id.btnApplication).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            startActivity(new Intent(HomeActivity.this, DownloadActivity.class));
                            mDialog.dismiss();
                            finish();
                        }
                        catch (Exception ex) {

                        }
                    }
                });

                mDialog.findViewById(R.id.btnGetDatabase).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            startActivity(new Intent(HomeActivity.this, DownloadActivity.class));
                            mDialog.dismiss();
                            finish();
                        }
                        catch (Exception ex) {

                        }
                    }
                });

                mDialog.findViewById(R.id.btnRefreshDatabase).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            /*SQLiteDatabase db = wurthMB.dbHelper.getDB();
                            db.execSQL("VACUUM");
                            Notifications.showNotification(HomeActivity.this, "", "Baza je uspješno osvježena", 2);
                            mDialog.dismiss();*/
                        }
                        catch (Exception ex) {

                        }
                    }
                });

                mDialog.show();
                break;

            case 309:
                quitApp();
                break;

            case 400:
                getSupportFragmentManager().beginTransaction().add(R.id.content, new ReportsFragment(), "REPORTS").addToBackStack(null).commit();
                break;

            case 909:
                break;

            default:
                break;
        }
    }

    private void checkSettings() {
        try {

            /// Check is user available
            if (wurthMB.getUser() == null) {
                Intent exit_intent = new Intent(HomeActivity.this, FinishActivity.class);
                exit_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(exit_intent);
                finish();
                return;
            }

            /// Check localization
            if (wurthMB.getLocale() != wurthMB.getUser().locale) {
                wurthMB.currentLocale = wurthMB.getUser().locale;
                Configuration config = new Configuration();
                config.locale = wurthMB.getLocale();
                Locale.setDefault(wurthMB.getLocale());
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
            }

            if (wurthMB.imageLoader == null) wurthMB.imageLoader = new ImageLoader(this.getApplicationContext());
        }
        catch (Exception ex) {

        }
    }

    private void quitApp() {
        try {
            final Dialog dialog = new Dialog(HomeActivity.this, R.style.CustomDialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.alert);

            ((TextView) dialog.findViewById(R.id.text)).setText(R.string.AreYouSureExit);

            dialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent exit_intent = new Intent(HomeActivity.this, FinishActivity.class);
                    exit_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(exit_intent);
                    dialog.dismiss();
                    finish();
                }
            });

            dialog.findViewById(R.id.dialogButtonCANCEL).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
        catch (Exception ex) { }
    }

    private class NetworkTask extends AsyncTask<String, Boolean, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                mDialog.findViewById(R.id.marker_progress).setVisibility(View.VISIBLE);
                ((TextView) mDialog.findViewById(R.id.litText)).setText(getString(R.string.PleaseWait));
            } catch (Exception e) {
            }
        }


        @Override
        protected Boolean doInBackground(String... sUrl) {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return false;
                }


            } catch (Exception e) {
                return false;
            } finally {

                if (connection != null) connection.disconnect();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            try {
                mDialog.findViewById(R.id.marker_progress).setVisibility(View.GONE);
                if (result) {
                    if(mDialog != null) ((TextView) mDialog.findViewById(R.id.litText)).setText(getString(R.string.Success));
                }
                else {
                    if(mDialog != null) ((TextView) mDialog.findViewById(R.id.litText)).setText(getString(R.string.Error));
                }
            } catch (Exception e) {
            }

            return;
        }
    }
}
