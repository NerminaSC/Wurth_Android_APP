package ba.wurth.mb.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.R;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar sbLocationInterval;
    private SeekBar sbActivitesInterval;
    private SeekBar sbServerInterval;

    private TextView litLocationInterval;
    private TextView litActivitesInterval;
    private TextView litServerInterval;

    private CheckBox chkSyncEnabled;
    private CheckBox chkUse3G;
    private CheckBox chkSyncLocations;
    private CheckBox chkSync3GDocuments;
    private CheckBox chkLocationServiceEnabled;

    private Button btnDefault;
    private Button btnSave;
    private Button btnCancel;

    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        try {

            getSupportActionBar().setTitle(R.string.Settings);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            prefs = getSharedPreferences("optimusMBprefs", MODE_PRIVATE);

            sbLocationInterval = (SeekBar) findViewById(R.id.sbLocationInterval);
            sbActivitesInterval = (SeekBar) findViewById(R.id.sbActivitesInterval);
            sbServerInterval = (SeekBar) findViewById(R.id.sbServerInterval);

            litLocationInterval = (TextView) findViewById(R.id.litLocationInterval);
            litActivitesInterval = (TextView) findViewById(R.id.litActivitesInterval);
            litServerInterval = (TextView) findViewById(R.id.litServerInterval);

            chkSyncEnabled = (CheckBox) findViewById(R.id.chkSyncEnabled);
            chkUse3G = (CheckBox) findViewById(R.id.chkUse3G);
            chkSyncLocations = (CheckBox) findViewById(R.id.chkSyncLocations);
            chkSync3GDocuments = (CheckBox) findViewById(R.id.chkSync3GDocuments);
            chkLocationServiceEnabled = (CheckBox) findViewById(R.id.chkLocationServiceEnabled);

            btnDefault = (Button) findViewById(R.id.btnDefault);
            btnSave = (Button) findViewById(R.id.btnSave);
            btnCancel = (Button) findViewById(R.id.btnCancel);

            chkSyncEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("SyncEnabled", b ? 1 : 0);
                    editor.commit();
                    wurthMB.SYNC_ENABLED = b;
                }
            });

            chkUse3G.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("Use3G", b ? 1 : 0);
                    editor.commit();
                    wurthMB.USE_3G = b;
                }
            });

            chkSyncLocations.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("SyncLocations", b ? 1 : 0);
                    editor.commit();
                    wurthMB.LOCATION_SERVICE_ENABLED = b;
                }
            });

            chkSync3GDocuments.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("Sync3GDocuments", b ? 1 : 0);
                    editor.commit();
                    wurthMB.USE_3G_DOCUMENTS = b;
                }
            });

            chkLocationServiceEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("LocationServiceEnabled", b ? 1 : 0);
                    editor.commit();
                    //wurthMB.LOCATION_SERVVICE_ENABLED = b;
                }
            });

            sbLocationInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    litLocationInterval.setText(Integer.toString(i + 1) + " min.");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("LocationInterval", seekBar.getProgress() + 1);
                    editor.commit();
                    wurthMB.LOCATION_SERVICE_INTERVAL = (seekBar.getProgress() + 1) * 60 * 1000;
                }
            });

            sbActivitesInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    litActivitesInterval.setText(Integer.toString(i + 1) + " min.");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("ActivitesInterval", seekBar.getProgress() + 1);
                    editor.commit();
                    wurthMB.SYNC_INTERVAL = (seekBar.getProgress() + 1) * 60 * 1000;
                }
            });

            sbServerInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    litServerInterval.setText(Integer.toString(i + 1) + " min.");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("ServerInterval", seekBar.getProgress() + 1);
                    editor.commit();
                    wurthMB.SYNC_INTERVAL_SERVER = (seekBar.getProgress() + 1) * 60 * 1000;
                }
            });

            btnDefault.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("ServerInterval", 60);
                    editor.putInt("LocationInterval", 1);
                    editor.putInt("ActivitesInterval", 1);
                    editor.putInt("SyncEnabled", 1);
                    editor.putInt("Use3G", 1);
                    editor.putInt("SyncLocations", 1);
                    editor.putInt("Sync3GDocuments", 1);
                    editor.putInt("LocationServiceEnabled", 1);
                    editor.commit();

                    bindData();
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            bindData();

        } catch (Exception e1) { }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void bindData() {
        try {

            if (prefs.getInt("LocationInterval", -1) > -1) {
                sbLocationInterval.setProgress(prefs.getInt("LocationInterval", -1));
                litLocationInterval.setText(Integer.toString(prefs.getInt("LocationInterval", -1)) + " min.");
            }

            if (prefs.getInt("ActivitesInterval", -1) > -1) {
                sbActivitesInterval.setProgress(prefs.getInt("ActivitesInterval", -1));
                litActivitesInterval.setText(Integer.toString(prefs.getInt("ActivitesInterval", -1)) + " min.");
            }

            if (prefs.getInt("ServerInterval", -1) > -1) {
                sbServerInterval.setProgress(prefs.getInt("ServerInterval", -1));
                litServerInterval.setText(Integer.toString(prefs.getInt("ServerInterval", -1)) + " min.");
            }

            if (prefs.getInt("SyncEnabled", -1) > -1) chkSyncEnabled.setChecked(prefs.getInt("SyncEnabled", -1) == 1 ? true : false);
            if (prefs.getInt("Use3G", -1) > -1) chkUse3G.setChecked(prefs.getInt("Use3G", -1) == 1 ? true : false);
            if (prefs.getInt("SyncLocations", -1) > -1) chkSyncLocations.setChecked(prefs.getInt("SyncLocations", -1) == 1 ? true : false);
            if (prefs.getInt("Sync3GDocuments", -1) > -1) chkSync3GDocuments.setChecked(prefs.getInt("Sync3GDocuments", -1) == 1 ? true : false);
            if (prefs.getInt("LocationServiceEnabled", -1) > -1) chkLocationServiceEnabled.setChecked(prefs.getInt("LocationServiceEnabled", -1) == 1 ? true : false);

        }
        catch (Exception ex) {

        }
    }
}
