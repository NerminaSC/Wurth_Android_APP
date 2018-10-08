package ba.wurth.mb.Activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import io.requery.android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.R;

public class SystemActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.system);

        findViewById(R.id.btnCheckInternet).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    new NetworkTask().execute("http://www.sourcecode.ba");
                } catch (Exception e) {

                }
            }
        });

        findViewById(R.id.btnCheckService).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    new NetworkTask().execute("http://ws.optimus.ba/CUSTOM/MB/Wurth/Wurth.asmx");
                } catch (Exception e) {

                }
            }
        });

        findViewById(R.id.btnClearDatabase).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {

                    SQLiteDatabase db = wurthMB.dbHelper.getDB();

                    ContentValues cv = new ContentValues();
                    cv.put("SignOutDate", new Date().getTime());
                    db.update("Logins", cv, "SignOutDate = 0", null);

                    wurthMB.setUser(null);
                    SystemActivity.this.deleteDatabase("wurthMB.db");

                    Intent exit_intent = new Intent(SystemActivity.this, FinishActivity.class);
                    exit_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(exit_intent);

                    finish();
                    android.os.Process.killProcess( android.os.Process.myPid() );
                }
                catch (Exception ex) {
                }
                return true;
            }
        });

        findViewById(R.id.btnExit).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent exit_intent = new Intent(SystemActivity.this, FinishActivity.class);
                    exit_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(exit_intent);
                    finish();
                }
                catch (Exception ex) {

                }
            }
        });

        findViewById(R.id.btnApplication).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    startActivity(new Intent(SystemActivity.this, DownloadActivity.class));
                    finish();
                }
                catch (Exception ex) {

                }
            }
        });

        findViewById(R.id.btnGetDatabase).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    startActivity(new Intent(SystemActivity.this, DownloadActivity.class));
                    finish();
                }
                catch (Exception ex) {

                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private class NetworkTask extends AsyncTask<String, Boolean, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.marker_progress).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.litText)).setText(getString(R.string.PleaseWait));
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
            findViewById(R.id.marker_progress).setVisibility(View.GONE);
            if (result) {
                ((TextView) findViewById(R.id.litText)).setText(getString(R.string.Success));
            }
            else {
                ((TextView) findViewById(R.id.litText)).setText(getString(R.string.Error));
            }

            return;
        }
    }

}
