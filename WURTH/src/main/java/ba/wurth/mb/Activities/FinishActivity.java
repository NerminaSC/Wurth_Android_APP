package ba.wurth.mb.Activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;

import ba.wurth.mb.Services.LocationService;
import ba.wurth.mb.Services.NotificationService;
import ba.wurth.mb.Services.SyncService;

public class FinishActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		stopService(new Intent(this,NotificationService.class));
		stopService(new Intent(this,LocationService.class));
		stopService(new Intent(this,SyncService.class));

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();

		finish();			
	}
}
