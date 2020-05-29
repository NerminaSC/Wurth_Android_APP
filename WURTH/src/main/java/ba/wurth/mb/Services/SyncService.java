package ba.wurth.mb.Services;


import android.annotation.TargetApi;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Additional.DL_Additional;
import ba.wurth.mb.DataLayer.Documents.DL_Documents;
import ba.wurth.mb.DataLayer.GPS.DL_GPS;
import ba.wurth.mb.DataLayer.Orders.DL_Orders;
import ba.wurth.mb.DataLayer.Routes.DL_Routes;
import ba.wurth.mb.DataLayer.Sync.DL_Sync;
import ba.wurth.mb.DataLayer.Temp.DL_Temp;
import ba.wurth.mb.DataLayer.Visits.DL_Visits;
import ba.wurth.mb.R;
import ba.wurth.mb.Services.NotificationService.LocalBinder;

public class SyncService extends Service {

    private boolean working = false;
    private boolean workingServer = false;
    private boolean workingGPS = false;
    private Handler mHandler;
    private NetworkTaskSync mTaskSync;
    private NetworkTaskSyncServer mTaskSyncServer;
    private NetworkTaskSyncPricelist mTaskSyncPricelist;
    private NetworkTaskSyncGPS mTaskSyncGPS;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        try {
            Intent i = new Intent(this, NotificationService.class);
            bindService(i, mConnection, 0);
            mHandler = new Handler();

            if (mHandler != null) {
                mHandler.post(rSync);
                mHandler.post(rSyncGPS);
                mHandler.postDelayed(rSyncServer, 1 * 60 * 1000); // 1 minute
                mHandler.postDelayed(rSyncPricelist, 15 * 60 * 1000); // 15 minute
            }

        }
        catch (Exception ex) {

        }
    }

    @Override
    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        if (mTaskSync != null) {
            mTaskSync.cancel(true);
            mTaskSync = null;
        }

        if (mTaskSyncServer != null) {
            mTaskSyncServer.cancel(true);
            mTaskSyncServer = null;
        }

        if (mTaskSyncGPS != null) {
            mTaskSyncGPS.cancel(true);
            mTaskSyncGPS = null;
        }

        if (mTaskSyncServer != null) {
            mTaskSyncServer.cancel(true);
            mTaskSyncServer = null;
        }

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private final Runnable rSync = new Runnable()
    {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void run()
        {
            try {
                if (!working) {
                    if (!wurthMB.SYNC_ENABLED) return;
                    if (mTaskSync != null && mTaskSync.getStatus() == AsyncTask.Status.RUNNING) return;
                    if (((wurthMB) getApplication()).isNetworkAvailable()) {
                        mTaskSync = new NetworkTaskSync();
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
                            mTaskSync.execute();
                        } else {
                            mTaskSync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                }
            }
            catch (Exception e) {
                wurthMB.AddError("SyncService", e.getMessage(), e);
            }
            mHandler.postDelayed(rSync, wurthMB.SYNC_INTERVAL);
        }
    };

    private class NetworkTaskSync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            working = true;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (!wurthMB.dbHelper.getDB().inTransaction()) {

                    int VisitCount = DL_Visits.Sync();
                    if (mBound && VisitCount > 0) mService.setTicker(getString(R.string.Notification_VisitSynced) + ": " + Integer.toString(VisitCount));

                    int OrderCount = DL_Orders.Sync();
                    if (mBound && OrderCount > 0) mService.setTicker(getString(R.string.Notification_OrderSynced) + ": " + Integer.toString(OrderCount));

                    int RouteCount = DL_Routes.Sync();
                    if (mBound && VisitCount > 0) mService.setTicker(getString(R.string.Notification_VisitSynced) + ": " + Integer.toString(RouteCount));

                    if (wurthMB.USE_3G_DOCUMENTS || (!wurthMB.USE_3G_DOCUMENTS && !wurthMB.MOBILE_DATA)) {
                        int DocumentCount = DL_Documents.Sync();
                        if (mBound && DocumentCount > 0) mService.setTicker(getString(R.string.Notification_DocumentSynced) + ": " + Integer.toString(DocumentCount));
                    }

                    DL_Additional.Sync();
                    DL_Temp.Sync_Acquisition();
                    DL_Sync.POST_UserActivityLog();
                }
            }
            catch (Exception e) {
                wurthMB.AddError("SyncService", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            working = false;
        }
    }


    /************************/
    /******* SERVER *********/
    /************************/

    private final Runnable rSyncServer = new Runnable()
    {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void run()
        {
            try {
                if (!workingServer) {
                    if (!wurthMB.SYNC_ENABLED) return;
                    if (mTaskSyncServer != null && mTaskSyncServer.getStatus() == AsyncTask.Status.RUNNING) return;
                    if (((wurthMB) getApplication()).isNetworkAvailable()) {
                        mTaskSyncServer = new NetworkTaskSyncServer();
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
                            mTaskSyncServer.execute();
                        } else {
                            mTaskSyncServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                }
            }
            catch (Exception e) {
                wurthMB.AddError("SyncService", e.getMessage(), e);
            }
            mHandler.postDelayed(rSyncServer, wurthMB.SYNC_INTERVAL_SERVER);
        }
    };

    private class NetworkTaskSyncServer extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            workingServer = true;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (!wurthMB.dbHelper.getDB().inTransaction()) {

                    int count;

                    count = DL_Sync.Load_Clients(wurthMB.getUser().UserID, false);
                    if (mBound && count > 0) mService.setTicker(getString(R.string.Notification_ClientSynced) + ": " + Integer.toString(count));
                }
            }
            catch (Exception e) {
                wurthMB.AddError("SyncService", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            workingServer = false;
        }
    }

    /*********************/
    /**** PRICELIST ******/
    /*********************/

    private final Runnable rSyncPricelist = new Runnable()
    {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void run()
        {
            try {
                if (!wurthMB.SYNC_ENABLED) return;
                if (mTaskSyncPricelist != null && mTaskSyncPricelist.getStatus() == AsyncTask.Status.RUNNING) return;
                if (((wurthMB) getApplication()).isNetworkAvailable()) {
                    mTaskSyncPricelist = new NetworkTaskSyncPricelist();
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
                        mTaskSyncPricelist.execute();
                    } else {
                        mTaskSyncPricelist.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }
            catch (Exception e) {
                wurthMB.AddError("SyncService", e.getMessage(), e);
            }
        }
    };

    private class NetworkTaskSyncPricelist extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (!wurthMB.dbHelper.getDB().inTransaction() && mService != null) {
                    DL_Sync.Load_Actions(wurthMB.getUser().UserID);
                }
            }
            catch (Exception e) {
                wurthMB.AddError("SyncService", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

    /*********************/
    /******* GPS *********/
    /*********************/

    private final Runnable rSyncGPS = new Runnable()
    {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void run()
        {
            try {
                if (!workingGPS) {
                    if (!wurthMB.SYNC_LOCATION) return;
                    if (mTaskSyncGPS != null && mTaskSyncGPS.getStatus() == AsyncTask.Status.RUNNING) return;
                    if (((wurthMB) getApplication()).isNetworkAvailable()) {
                        mTaskSyncGPS = new NetworkTaskSyncGPS();
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
                            mTaskSyncGPS.execute();
                        } else {
                            mTaskSyncGPS.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                }
            }
            catch (Exception e) {
                wurthMB.AddError("SyncService", e.getMessage(), e);
            }
            mHandler.postDelayed(rSyncGPS, wurthMB.LOCATION_SERVICE_INTERVAL);
        }
    };

    private class NetworkTaskSyncGPS extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            workingGPS = true;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (!wurthMB.dbHelper.getDB().inTransaction()) {
                    DL_GPS.Sync();
                }
            }
            catch (Exception e) {
                wurthMB.AddError("SyncService", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            workingGPS = false;
        }
    }




    NotificationService mService;
    boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            LocalBinder binder = (LocalBinder) arg1;
            mService = binder.getService();
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

