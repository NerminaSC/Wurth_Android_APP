package ba.wurth.mb.Classes;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import ba.wurth.mb.R;

public class Notifications {
	
	private static Dialog dialog;
	
	final static Handler mHandler = new Handler();
	private static String ToastMessage = "";
	private static String ToastTitle = "";
	private static int ToastType = 0;
	private static Context _ctx;

	final static Runnable mUpdateResults = new Runnable() {

		public void run() {
	    	
	    	LayoutInflater inflater = LayoutInflater.from(_ctx);
	    	View view = inflater.inflate(R.layout.toast, null);
	    	TextView tv = (TextView) view.findViewById(R.id.text);
	    	TextView tvTitle = (TextView) view.findViewById(R.id.Title);
	    	
	    	tv.setText(ToastMessage);

    		tvTitle.setText(ToastTitle);
    		tvTitle.setText(tvTitle.getText().toString().toUpperCase());
	    	
	    	switch (ToastType){
		    	case 0:
		    		view.setBackgroundColor(_ctx.getResources().getColor(R.color.transparent_green));
		    		break;
		    	case 1:
		    		view.setBackgroundColor(_ctx.getResources().getColor(R.color.transparent_red));
		    		break;
		    	case 2:
		    		view.setBackgroundColor(_ctx.getResources().getColor(R.color.transparent_blue));
		    		break;
		    	default:
		    		view.setBackgroundColor(_ctx.getResources().getColor(R.color.transparent_blue));
		    		break;
	    	}
			
			Toast toast = new Toast(_ctx);
			toast.setGravity(Gravity.FILL, 0, 0);
			toast.setDuration(500);
			toast.setView(view);
			toast.show();			
			
	    }
	};
	
	public static void showNotification(Context ctx, String Title, String Message, int Type ) {
		_ctx = ctx;
		ToastMessage = Message;
		ToastTitle = Title;
		ToastType = Type;
		mHandler.post(mUpdateResults);
	}	
	
	public static void showLoading(Context ctx) {
		
		try {
			
			if (dialog != null) {
				dialog.dismiss();
				dialog = null;
			}
			
			dialog = new Dialog(ctx, R.style.CustomDialog);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.loading);
			
	        dialog.findViewById(R.id.btnCancel).setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	dialog.dismiss();
	            	dialog = null;
	            }
	    	});
	        
			dialog.show();
		}
		catch (Exception e) { }
	}

	public static void hideLoading(Context ctx) {
		try {
			if (dialog != null) {
				dialog.dismiss();
				dialog = null;
			}
		}
		catch (Exception e) { }
	}
}
