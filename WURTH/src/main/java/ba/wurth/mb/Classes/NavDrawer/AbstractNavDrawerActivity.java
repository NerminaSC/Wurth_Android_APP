package ba.wurth.mb.Classes.NavDrawer;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

import ba.wurth.mb.Activities.LoginActivity;
import ba.wurth.mb.Activities.Orders.OrderActivity;
import ba.wurth.mb.Activities.SettingsActivity;
import ba.wurth.mb.Activities.Visits.VisitActivity;
import ba.wurth.mb.Adapters.SearchSuggestionsAdapter;
import ba.wurth.mb.BuildConfig;
import ba.wurth.mb.Classes.Common;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.Interfaces.NavDrawerItem;
import ba.wurth.mb.R;
import ba.wurth.mb.Services.BluetoothService;
import io.requery.android.database.sqlite.SQLiteDatabase;

public abstract class AbstractNavDrawerActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private ListView mDrawerList;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private SearchView mSearchView;
    private MenuItem searchMenuItem;

    private SearchSuggestionsAdapter mSearchSuggestionsAdapter;

    private NavDrawerActivityConfiguration navConf;

    protected abstract NavDrawerActivityConfiguration getNavDrawerConfiguration();

    protected abstract void onNavItemSelected(int id);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navConf = getNavDrawerConfiguration();

        setContentView(navConf.getMainLayout());

        mTitle = mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(navConf.getDrawerLayoutId());
        mDrawerList = (ListView) findViewById(navConf.getLeftDrawerId());

        View v = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.navdrawer_header, null, false);

        ((TextView) v.findViewById(R.id.litUser)).setText(wurthMB.getUser().Firstname + " " + wurthMB.getUser().Lastname);
        ((TextView) v.findViewById(R.id.litClient)).setText(wurthMB.getUser().AccountName);

        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new ExportDatabaseFileTask().execute();
                return false;
            }
        });

        mDrawerList.addHeaderView(v);

        mDrawerList.setAdapter(navConf.getBaseAdapter());
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        this.initDrawerShadow();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle
                (
                        this,
                        mDrawerLayout,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close
                ) {
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    protected void initDrawerShadow() {
        mDrawerLayout.setDrawerShadow(navConf.getDrawerShadow(), GravityCompat.START);
    }

    protected int getDrawerIcon() {
        return R.drawable.ic_drawer;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (navConf.getActionMenuItemsToHideWhenDrawerOpen() != null) {
            boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
            for (int iItem : navConf.getActionMenuItemsToHideWhenDrawerOpen()) {
                menu.findItem(iItem).setVisible(!drawerOpen);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        searchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        if (android.os.Build.VERSION.SDK_INT >= 11) {
            setupSearchView(menu.findItem(R.id.action_search));
        }

        menu.findItem(R.id.action_add_client).setVisible(true);

        if (!Common.isServiceRunning(AbstractNavDrawerActivity.this, BluetoothService.class.getName())) {
            menu.findItem(R.id.action_bluetooth).setTitle(getString(R.string.BluetoothServiceStart));
        } else {
            menu.findItem(R.id.action_bluetooth).setTitle(getString(R.string.BluetoothServiceStop));
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            switch (item.getItemId()) {
                case R.id.action_settings:
                    startActivity(new Intent(AbstractNavDrawerActivity.this, SettingsActivity.class));
                    break;
                case R.id.action_bluetooth:
                    if (!Common.isServiceRunning(AbstractNavDrawerActivity.this, BluetoothService.class.getName())) {
                        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                            Toast.makeText(this, getString(R.string.Notification_Bluetooth_AdapterOff), Toast.LENGTH_SHORT).show();
                        } else {
                            startService(new Intent(this, BluetoothService.class));
                            item.setTitle(getString(R.string.BluetoothServiceStop));
                        }
                    } else {
                        stopService(new Intent(this, BluetoothService.class));
                        item.setTitle(getString(R.string.BluetoothServiceStart));
                    }
                    break;
                case R.id.action_logout:
                    logOut();
                    break;
                case R.id.action_search:
                    break;
                case R.id.action_createorder:
                    Intent k = new Intent(AbstractNavDrawerActivity.this, OrderActivity.class);
                    startActivity(k);
                    return true;
                case R.id.action_notevisit:
                    Intent i = new Intent(AbstractNavDrawerActivity.this, VisitActivity.class);
                    startActivity(i);
                    return true;
                default:
                    break;
            }
            return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    protected DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    protected ActionBarDrawerToggle getDrawerToggle() {
        return mDrawerToggle;
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position - 1);
        }
    }

    public void selectItem(int position) {
        NavDrawerItem selectedItem = navConf.getNavItems()[position];

        this.onNavItemSelected(selectedItem.getId());
        mDrawerList.setItemChecked(position, true);

        if (selectedItem.updateActionBarTitle()) {
            setTitle(selectedItem.getLabel());
        }

        if (this.mDrawerLayout.isDrawerOpen(this.mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    private void setupSearchView(MenuItem searchItem) {

        if (isAlwaysExpanded()) {
            mSearchView.setIconifiedByDefault(false);
        } else {
            MenuItemCompat.setShowAsAction(searchItem, MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }

        if (mSearchSuggestionsAdapter == null)
            mSearchSuggestionsAdapter = new SearchSuggestionsAdapter(this);

        mSearchView.setSuggestionsAdapter(mSearchSuggestionsAdapter);

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if (!queryTextFocused) {
                    MenuItemCompat.collapseActionView(searchMenuItem);
                    mSearchView.setQuery("", false);
                }
            }
        });
    }

    protected boolean isAlwaysExpanded() {
        return false;
    }

    private void logOut() {
        try {
            final Dialog dialog = new Dialog(AbstractNavDrawerActivity.this, R.style.CustomDialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.alert);

            ((TextView) dialog.findViewById(R.id.text)).setText(R.string.AreYouSureExit);

            dialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Save Login
                    SQLiteDatabase db = wurthMB.dbHelper.getDB();

                    ContentValues cv = new ContentValues();
                    cv.put("SignOutDate", new Date().getTime());
                    db.update("Logins", cv, "SignOutDate = 0", null);

                    startActivity(new Intent(AbstractNavDrawerActivity.this, LoginActivity.class));
                    dialog.dismiss();
                    wurthMB.setUser(null);
                    finish();
                }
            });

            dialog.findViewById(R.id.dialogButtonCANCEL).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        } catch (Exception ex) {
            Log.d("", ex.getMessage());
        }
    }

    public static final String PACKAGE_NAME = "ba.wurth.mb";
    public static final String DATABASE_NAME = "wurthMB.db";
    private static final File DATA_DIRECTORY_DATABASE = new File(Environment.getDataDirectory() + "/data/" + PACKAGE_NAME + "/databases/" + DATABASE_NAME);
    protected static final File DATABASE_DIRECTORY = new File(Environment.getExternalStorageDirectory(), "WurthMB");


    protected static boolean exportDb() {
        if (!SdIsPresent()) return false;

        File dbFile = DATA_DIRECTORY_DATABASE;
        String filename = "wurthMB.db";

        File exportDir = DATABASE_DIRECTORY;
        File file = new File(exportDir, filename);

        if (!exportDir.exists()) exportDir.mkdirs();

        try {
            file.createNewFile();
            copyFile(dbFile, file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    /**
     * Returns whether an SD card is present and writable
     **/
    public static boolean SdIsPresent() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public class ExportDatabaseFileTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            Notifications.showLoading(AbstractNavDrawerActivity.this);
        }

        @Override
        protected Void doInBackground(final Void... args) {
            try {
                exportDb();
            } catch (Exception e) {
            }

            return null;

        }

        @Override
        protected void onPostExecute(final Void success) {
            try {
                File database = new File(DATABASE_DIRECTORY, "wurthMB.db");

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Optimus - Database Backup");
                intent.putExtra(Intent.EXTRA_TEXT, "");
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_STREAM,
                        FileProvider.getUriForFile(getBaseContext(), "ba.wurth.mb" + ".FileProvider", database));

                startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (Exception e) {
                String temp = e.getMessage();
            }
        }
    }
}