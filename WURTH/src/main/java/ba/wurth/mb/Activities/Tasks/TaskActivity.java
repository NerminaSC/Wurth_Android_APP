package ba.wurth.mb.Activities.Tasks;

import android.app.Dialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import ba.wurth.mb.Adapters.SpinnerAdapter;
import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.Objects.Task;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.DataLayer.Tasks.DL_Tasks;
import ba.wurth.mb.Interfaces.SpinnerItem;
import ba.wurth.mb.R;

public class TaskActivity extends AppCompatActivity  {

    public Task mTask;
    private SpinnerItem[] items_status;
    private JSONArray items;
    private java.text.Format formatter = new SimpleDateFormat("dd.MMM.yyyy HH:mm");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task);

        try {

            getSupportActionBar().setTitle(R.string.Task);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

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

            items_status = new SpinnerItem[4];
            //items_status[0] = new SpinnerItem(2L, getString(R.string.Sent), "");
            items_status[0] = new SpinnerItem(3L, getString(R.string.Progress), "", "");
            items_status[1] = new SpinnerItem(5L, getString(R.string.Rejected), "", "");
            items_status[2] = new SpinnerItem(7L, getString(R.string.Completed), "", "");
            items_status[3] = new SpinnerItem(8L, getString(R.string.Canceled), "", "");

            bindData();
            bindListeners();
        }
        catch (Exception ex) {
            wurthMB.AddError("Task", ex.getMessage(), ex);
        }
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

    private void bindListeners() {
        try {

            findViewById(R.id.btnAddLog).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {

                        final Dialog dialog = new Dialog(TaskActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_taks_log_add);
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        final Spinner spStatus = (Spinner) dialog.findViewById(R.id.spStatus);
                        final EditText txbNote = (EditText) dialog.findViewById(R.id.txbNote);

                        SpinnerAdapter adapter_status;
                        adapter_status = new SpinnerAdapter(TaskActivity.this, R.layout.simple_dropdown_item_1line, items_status);
                        spStatus.setAdapter(adapter_status);

                        dialog.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {

                                try {

                                    mTask.StatusID = items_status[spStatus.getSelectedItemPosition()].getId();
                                    mTask.DOE = System.currentTimeMillis();

                                    JSONObject ParametersLog = new JSONObject();
                                    ParametersLog.put("Note", txbNote.getText());
                                    ParametersLog.put("StatusID", mTask.StatusID);
                                    ParametersLog.put("DOE", mTask.DOE);

                                    mTask.ParametersLog = ParametersLog.toString();

                                    if (DL_Tasks.AddOrUpdate(mTask) > 0) {
                                        Notifications.hideLoading(getApplicationContext());
                                        Notifications.showNotification(getApplicationContext(), "", getString(R.string.Notification_TaskSaved), 0);
                                        bindData();
                                    } else {
                                        Notifications.hideLoading(getApplicationContext());
                                        Notifications.showNotification(getApplicationContext(), "", getString(R.string.SystemError), 1);
                                    }
                                } catch (Exception ex) {

                                }
                                InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                                inputManager.hideSoftInputFromWindow(txbNote.getWindowToken(), 0);

                                dialog.dismiss();
                            }
                        });

                        dialog.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();

                    } catch (Exception ex) {

                    }
                }
            });

            findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

        }
        catch (Exception ex) {

        }
    }

    private void bindData() {
        try {

            if (getIntent().getExtras() != null && getIntent().hasExtra("TaskID")) {

                mTask = DL_Tasks.GetByTaskID(getIntent().getLongExtra("TaskID", 0L));

                if (mTask.StatusID == 2) {
                    DL_Tasks.UpdateStatus(mTask.TaskID, 3);
                    mTask.StatusID = 3;
                }

                if (mTask != null) {

                    String statusText = "";

                    switch ((int) mTask.StatusID) {
                        case 1:
                            statusText = getString(R.string.Draft);
                            break;
                        case 2:
                            statusText = getString(R.string.Sent);
                            break;
                        case 3:
                            statusText = getString(R.string.Progress);
                            break;
                        case 5:
                            statusText = getString(R.string.Rejected);
                            break;
                        case 7:
                            statusText = getString(R.string.Completed);
                            break;
                        case 8:
                            statusText = getString(R.string.Canceled);
                            break;
                        default:
                            break;
                    }

                    ((TextView) findViewById(R.id.txbStatus)).setText(statusText);
                    ((TextView) findViewById(R.id.txbName)).setText(mTask.Name);
                    ((TextView) findViewById(R.id.txbDescription)).setText(mTask.Description);

                    JSONObject jsonObject = new JSONObject(mTask.Parameters);

                    if (!jsonObject.isNull("startDate")) {
                        Date startDate = new Date(jsonObject.getLong("startDate"));
                        ((TextView) findViewById(R.id.txbStartDate)).setText(formatter.format(startDate));
                    }

                    if (!jsonObject.isNull("endDate")) {
                        Date endDate = new Date(jsonObject.getLong("endDate"));
                        ((TextView) findViewById(R.id.txbEndDate)).setText(formatter.format(endDate));
                    }

                    LayoutInflater mInflater = LayoutInflater.from(TaskActivity.this);
                    LinearLayout llClients = (LinearLayout) findViewById(R.id.llClients);
                    LinearLayout llUsers = (LinearLayout) findViewById(R.id.llUsers);
                    LinearLayout llGroups = (LinearLayout) findViewById(R.id.llGroups);

                    if (!jsonObject.isNull("Users")) {
                        llUsers.removeAllViews();
                        for (int i = 0; i < jsonObject.getJSONArray("Users").length(); i++) {
                            JSONObject jsonObj = jsonObject.getJSONArray("Users").getJSONObject(i);
                            View v = mInflater.inflate(R.layout.list_row, llUsers, false);
                            ((TextView) v.findViewById(R.id.litTitle)).setText(jsonObj.getString("Name"));
                            ((TextView) v.findViewById(R.id.litStatus)).setText(Integer.toString(i+1));
                            llUsers.addView(v);
                        }
                    }

                    if (!jsonObject.isNull("Groups")) {
                        llGroups.removeAllViews();
                        for (int i = 0; i < jsonObject.getJSONArray("Groups").length(); i++) {
                            JSONObject jsonObj = jsonObject.getJSONArray("Groups").getJSONObject(i);
                            View v = mInflater.inflate(R.layout.list_row, llUsers, false);
                            ((TextView) v.findViewById(R.id.litTitle)).setText(jsonObj.getString("Name"));
                            ((TextView) v.findViewById(R.id.litStatus)).setText(Integer.toString(i + 1));
                            llGroups.addView(v);
                        }
                    }

                    if (!jsonObject.isNull("Clients")) {
                        llClients.removeAllViews();
                        for (int i = 0; i < jsonObject.getJSONArray("Clients").length(); i++) {
                            JSONObject jsonObj = jsonObject.getJSONArray("Clients").getJSONObject(i);
                            View v = mInflater.inflate(R.layout.list_row, llUsers, false);
                            ((TextView) v.findViewById(R.id.litTitle)).setText(jsonObj.getString("Name"));
                            ((TextView) v.findViewById(R.id.litStatus)).setText(Integer.toString(i + 1));
                            llClients.addView(v);
                        }
                    }

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
                        new LongTask().execute();
                    } else {
                        new LongTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }
        }
        catch (Exception ex) {
            wurthMB.AddError("Task", ex.getMessage(), ex);
        }
    }

    private class LongTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            try {
            }
            catch (Exception ex) {

            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                if (mTask == null) return null;

                Thread.sleep(300);
                Cursor cur = DL_Tasks.GET_Log(mTask.TaskID);

                items = new JSONArray();

                if (cur != null) {
                    while (cur.moveToNext()) {
                        JSONObject jsonObject = new JSONObject(cur.getString(cur.getColumnIndex("Parameters")));
                        items.put(jsonObject);
                    }
                    cur.close();
                }
            }
            catch (Exception ex) {
                wurthMB.AddError("Tasks", ex.getMessage(), ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            try {

                LayoutInflater mInflater = LayoutInflater.from(TaskActivity.this);
                LinearLayout llHistory = (LinearLayout) findViewById(R.id.llHistory);

                llHistory.removeAllViews();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject jsonObj = items.getJSONObject(i);
                    View v = mInflater.inflate(R.layout.list_row, llHistory, false);

                    v.findViewById(R.id.litStatus).setVisibility(View.GONE);

                    String statusText = "";

                    switch (jsonObj.getInt("StatusID")) {
                        case 1:
                            statusText = getString(R.string.Draft);
                            break;
                        case 2:
                            statusText = getString(R.string.Sent);
                            break;
                        case 3:
                            statusText = getString(R.string.Progress);
                            break;
                        case 5:
                            statusText = getString(R.string.Rejected);
                            break;
                        case 7:
                            statusText = getString(R.string.Completed);
                            break;
                        case 8:
                            statusText = getString(R.string.Canceled);
                            break;
                        default:
                            break;
                    }

                    Date date = new Date(jsonObj.getLong("DOE"));

                    ((TextView) v.findViewById(R.id.litTitle)).setText(statusText);
                    ((TextView) v.findViewById(R.id.litSubTitle)).setText(jsonObj.getString("Note"));
                    ((TextView) v.findViewById(R.id.litSupTitle)).setText(formatter.format(date));
                    llHistory.addView(v);
                    llHistory.addView(mInflater.inflate(R.layout.divider, llHistory, false));
                }

            } catch (Exception ex) {
                wurthMB.AddError("Tasks", ex.getMessage(), ex);
            }
        }
    }
}