package com.inductivebiblestudyapp.ui;

import com.inductivebiblestudyapp.ui.activities.MainActivity;
import com.inductivebiblestudyapp.util.PreferenceUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Restarts the main activity when requested. Requires manifest declaration:
 * <pre>
 *&lt;receiver android:name="com.inductivebiblestudyapp.ui.RestartReceiver" android:exported="false">
	&lt;intent-filter>
		&lt;action android:name="{@value #ACTION_RESTART_APP}"/>
	&lt;/intent-filter>
&lt;/receiver>
 * </pre> 
 * @author Jason Jenkins
 * @version 1.0.1-20150828
 */
public class RestartReceiver extends BroadcastReceiver {
	
	/** Clears ALL settings and restarts the app. */
	//Be SURE that this is in the manifest & matches or it will NOT work.
	public static final String ACTION_RESTART_APP = "com.inductivebiblestudyapp.ui.RestartReceiver.ACTION_RESTART_APP";
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	/*
    	 * WARNING: Beware of couple & loops here.
    	 */
    	final String action = intent.getAction();
        if (ACTION_RESTART_APP.equals(action)) {
        	PreferenceUtil.getPreferences(context).edit().clear().commit(); 
        	Intent mainIntent = new Intent(context, MainActivity.class);
    		mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    		context.startActivity(mainIntent);
        }
    }
    
    public static void sendBroadcast(Activity activity) {
		Intent intent = new Intent(RestartReceiver.ACTION_RESTART_APP);
		intent.setAction(RestartReceiver.ACTION_RESTART_APP);
		activity.sendBroadcast(intent);
		activity.finish();
    }
}
