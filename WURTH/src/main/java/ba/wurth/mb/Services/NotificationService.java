package ba.wurth.mb.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import ba.wurth.mb.Activities.HomeActivity;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.R;

public class NotificationService extends Service {

	private NotificationManager mNoticationManager;
	private NotificationCompat.Builder mBuilder;
	private static final long DELAY = 1 * 10 * 1000;
	private boolean working = false;
	private int mNotificationId = 1; 
	private final IBinder mBinder = new LocalBinder();
    private boolean applicationActive = false;

    private Handler handler = new Handler();

    @Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		mNoticationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(this);
		
		Intent resultIntent = new Intent(this, HomeActivity.class);
		PendingIntent resultPendingIntent =
		    PendingIntent.getActivity(
		    this,
		    0,
		    resultIntent,
		    PendingIntent.FLAG_UPDATE_CURRENT
		);		
		
		
		mBuilder.setSmallIcon(R.drawable.ic_notification).setContentTitle(getString(R.string.ApplicationStatus)).setOnlyAlertOnce(true);
	    mBuilder.setContentText(getString(R.string.StartingApplication));
	    mBuilder.setOngoing(true);
	    mBuilder.setContentIntent(resultPendingIntent);	    
	    
	    mNoticationManager.notify(mNotificationId, mBuilder.build());

        handler.post(r);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
        handler.removeCallbacks(r);
		mNoticationManager.cancelAll();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

    private final Runnable r = new Runnable()
    {
        public void run()
        {
            try {
                if (!working) {
                    working = true;

                    if (((wurthMB) getApplication()).isNetworkAvailable())
                    {
                        if (!applicationActive) {
                            mBuilder.setSmallIcon(R.drawable.ic_notification);
                            mBuilder.setContentText(getString(R.string.ApplicationInOnlineMode));
                            mBuilder.setWhen(System.currentTimeMillis());
                            mNoticationManager.notify(mNotificationId, mBuilder.build());
                            applicationActive = true;
                        }
                    }
                    else {
                        if (applicationActive) {
                            mBuilder.setSmallIcon(R.drawable.ic_notification_off);
                            mBuilder.setContentText(getString(R.string.ApplicationInOfflineMode));
                            mBuilder.setWhen(System.currentTimeMillis());
                            mNoticationManager.notify(mNotificationId, mBuilder.build());
                            applicationActive = false;
                        }
                    }
                    working = false;
                }
            }
            catch (Exception e) {

            }
            handler.postDelayed(r, DELAY);
        }
    };


    public void setTicker (String ContentText) {
    	try {
    		if (mBuilder != null) {
    		    mBuilder.setTicker(ContentText);
    		    mNoticationManager.notify(mNotificationId, mBuilder.build());
    		}
    	}
    	catch (Exception e) { }
    }
    
    public class LocalBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }
}
