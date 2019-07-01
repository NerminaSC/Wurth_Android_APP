package ba.wurth.mb.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ba.wurth.mb.Classes.Objects.Temp;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Sync.DL_Sync;
import ba.wurth.mb.DataLayer.Temp.DL_Temp;
import ba.wurth.mb.R;
import io.requery.android.database.sqlite.SQLiteDatabase;

public class IntroActivity extends Activity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.intro);

        try {

            PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            ((TextView) findViewById(R.id.footer).findViewById(R.id.txbVersion)).setText("ver. " + info.versionName);

            wurthMB.App_Version = info.versionName;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                wurthMB.IMEI = telephonyManager.getDeviceId();
            }

            ((TextView) findViewById(R.id.litStatus)).setText(getString(R.string.InitializingDatabase));

            if (check_permissions()){
                new checkDatabase().execute();
            }


        } catch (Exception ex) {
            ex.getMessage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public class checkDatabase extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            try {
                wurthMB.dbHelper.mThreadReference = this;
            }
            catch (Exception ex) {
                wurthMB.AddError("Intro activity checkDatabase", ex.getMessage(), ex);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
                publishProgress(getString(R.string.InitializingDatabase));
                wurthMB.dbHelper.getDB();

                try {

                    int PricelistSyncDate = 0;
                    int LoginDate = 0;
                    SharedPreferences prefs = getSharedPreferences("optimusMBprefs", MODE_PRIVATE);

                    if (((wurthMB) getApplication()).isNetworkAvailable() && !wurthMB.dbHelper.getDB().inTransaction()) {

                        if (prefs.getInt("PricelistSyncDate", 0) != 0) {
                            PricelistSyncDate = prefs.getInt("PricelistSyncDate", 0);
                        }

                        if (PricelistSyncDate != new Date().getDate()) {
                            DL_Sync.mThreadReferenceIntro = this;
                            DL_Sync.Load_Pricelist(wurthMB.getUser().UserID);

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("PricelistSyncDate", new Date().getDate());
                            editor.commit();

                            publishProgress(getString(R.string.OrdersSynchronization));
                            DL_Sync.Load_Orders(wurthMB.getUser().UserID);

                            publishProgress(getString(R.string.VisitsSynchronization));
                            DL_Sync.Load_Visits(wurthMB.getUser().UserID);
                        }
                    }

                    if (prefs.getInt("LoginDate", 0) != 0) LoginDate = prefs.getInt("LoginDate", 0);

                    if (LoginDate != new Date().getDate() && wurthMB.getUser() != null) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("LoginDate", new Date().getDate());
                        editor.commit();

                        SQLiteDatabase db = wurthMB.dbHelper.getDB();

                        ContentValues cv = new ContentValues();
                        cv.put("SignOutDate", new Date().getTime());
                        db.update("Logins", cv, "SignOutDate = 0", null);

                        startActivity(new Intent(IntroActivity.this, LoginActivity.class));
                        wurthMB.setUser(null);
                        finish();
                        cancel(true);
                    }

                } catch (Exception e) {
                    wurthMB.AddError("Intro activity Pricelist", e.getMessage(), e);
                }

            }
            catch (Exception ex) {
                wurthMB.AddError("Intro activity checkDatabase", ex.getMessage(), ex);
            }

            publishProgress(getString(R.string.StartingApplication));

            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            try {

                wurthMB.dbHelper.mThreadReference = null;

                Configuration config = new Configuration(); // get Modifiable Config from actual config changed

                if (wurthMB.getUser() == null) {

                    config.locale = new Locale("ba");
                    Locale.setDefault(new Locale("ba"));
                    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

                    Intent k = new Intent(IntroActivity.this, LoginActivity.class);
                    startActivity(k);
                    finish();
                    return;
                }

                Temp temp = DL_Temp.Get();

                if (temp != null) {
                    wurthMB.setOrder(temp.order);
                    wurthMB.setClient(temp.client);
                }
                else {
                    wurthMB.setOrder(null);
                    wurthMB.setClient(null);
                }

                if (wurthMB.dbHelper.getDB().inTransaction()) wurthMB.dbHelper.getDB().endTransaction();



                if (wurthMB.getLocale() != null)
                {
                    config.locale = wurthMB.getLocale();
                    Locale.setDefault(wurthMB.getLocale());
                }
                else {
                    config.locale = new Locale("ba");
                    Locale.setDefault(new Locale("ba"));
                }

                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

                Intent k = new Intent(IntroActivity.this, HomeActivity.class);
                startActivity(k);
                finish();


            } catch (Exception ex) {
                wurthMB.AddError("Intro activity checkDatabase", ex.getMessage(), ex);
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            ((TextView) findViewById(R.id.litStatus)).setText(values[0]);
        }

        public void doProgress(String str) {
            publishProgress(str);
        }

    }

    private boolean check_permissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (String permission : PERMISSIONS) {
                    if (ActivityCompat.checkSelfPermission(IntroActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(IntroActivity.this, PERMISSIONS, REQUEST_ID_MULTIPLE_PERMISSIONS);
                        return false;
                    }
                }
            }
        } catch (Exception e) {

        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        try {
            switch (requestCode) {

                case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                    Map<String, Integer> perms = new HashMap<>();
                    // Initialize the map with both permissions
                    perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);

                    // Fill with actual results from user
                    if (grantResults.length > 0) {
                        for (int i = 0; i < permissions.length; i++) perms.put(permissions[i], grantResults[i]);
                        // Check for both permissions
                        if (
                                perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                                        && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                        && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                        && perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

                                ) {
                            // process the normal flow
                            //else any one or both the permissions are not granted

                            new checkDatabase().execute();

                        } else {
                            //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                            // shouldShowRequestPermissionRationale will return true
                            //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                            if (
                                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)
                                    ) {

                                show_dialog(getString(R.string.SecuritySettingsApprove),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        check_permissions();
                                                        break;
                                                    case DialogInterface.BUTTON_NEGATIVE:
                                                        // proceed with logic by disabling the related features or quit the app.
                                                        break;
                                                }
                                            }
                                        });
                            }
                            //permission is denied (and never ask again is  checked)
                            //shouldShowRequestPermissionRationale will return false
                            else {
                                Toast.makeText(this, getString(R.string.SecuritySettingsApprove_2), Toast.LENGTH_LONG).show();
                                //proceed with logic by disabling the related features or quit the app.
                            }
                        }
                    }
                }
                break;
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this)) {
                        Toast.makeText(IntroActivity.this, getString(R.string.SecuritySettingsApprove_2), Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else  new checkDatabase().execute();
                }
            }
        } catch (Exception e) {
        }
    }

    private void show_dialog(String message, DialogInterface.OnClickListener okListener) {
        try {
            new AlertDialog.Builder(this)
                    .setMessage(message)
                    .setPositiveButton("OK", okListener)
                    .setNegativeButton("Cancel", okListener)
                    .create()
                    .show();
        } catch (Exception e) {
        }
    }
}
