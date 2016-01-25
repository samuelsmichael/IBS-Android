package com.inductivebiblestudyapp.ui.actionwrappers;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.ui.activities.FooterInfoActivity;
import com.inductivebiblestudyapp.ui.activities.MainActivity;

/**
 * <p>Simple wrapper to load and prepare constant actions to be taken 
 * when the footer elements are clicked. This allows for consistent actions 
 * across activities and fragments WITHOUT using a childFragment.</p>
 * 
 * Requires the following views be found in the 
 * <code>rootView</code>:
 * <ul>{@link R.id.ibs_footer_about}</li>
 * <li>{@link R.id.ibs_footer_contact}</li>
 * <li>{@link R.id.ibs_footer_privacy}</li>
 * <li>{@link R.id.ibs_footer_terms}</li>
 * </ul>
 * 
 * @author Jason Jenkins
 * @version 0.2.0-20150715
 */
public class FooterViewActionWrapper implements OnClickListener {
	final private Activity mParentActivity;		
	final private boolean mIsHomeEnabled;
	
	/**
	 * Same as calling {@link #newInstance(Activity, View, boolean)} with <code>true</code>. 
	 * @param activity The activity performing the activity launches
	 * @param rootView A view containing the sub views. 
	 */
	public static void newInstance(Activity activity, View rootView) {
		new FooterViewActionWrapper(activity, rootView, true);
	}
	
	/**
	 * Prepares a new launcher by finding views and setting the onclick listener. 
	 * @param activity The activity performing the activity launches
	 * @param rootView A view containing the sub views. 
	 * @param enableHome <code>true</code> to enable home to point to MainActivity
	 * (which will later redirect appropriately),
	 * <code>false</code> to disable clicks (such as when already at home)
	 */
	public static void newInstance(Activity activity, View rootView, boolean enableHome) {
		new FooterViewActionWrapper(activity, rootView, enableHome);
	}
	
	/**
	 * Prepares a new launcher. Always destroy with activity. 
	 * @param activity The activity performing the activity launches
	 * @param rootView A view containing the sub views. 
	 * @param enableHome <code>true</code> to enable home link, <code>false</code> 
	 * to ignore clicks.
	 */
	protected FooterViewActionWrapper(Activity activity, View rootView, boolean enableHome) {
		this.mParentActivity = activity;
		this.mIsHomeEnabled = enableHome;

		rootView.findViewById(R.id.ibs_footer_home).setOnClickListener(this);
		rootView.findViewById(R.id.ibs_footer_about).setOnClickListener(this);
		rootView.findViewById(R.id.ibs_footer_contact).setOnClickListener(this);
		rootView.findViewById(R.id.ibs_footer_privacy).setOnClickListener(this);
		rootView.findViewById(R.id.ibs_footer_terms).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		Intent infoIntent = new Intent(mParentActivity, FooterInfoActivity.class);
		
		switch (v.getId()) {
		case R.id.ibs_footer_home:
			if (mIsHomeEnabled) { //only if home enabled.
				Intent main = new Intent(mParentActivity, MainActivity.class);
				//launches main, which will clear & launch the appropriate activity
				mParentActivity.startActivity(main);
			}
			return; //we do not want to launch info activity
			
		case R.id.ibs_footer_about: 
			infoIntent.setAction(FooterInfoActivity.ACTION_SHOW_ABOUT);
			break;
			
		case R.id.ibs_footer_contact:
			infoIntent.setAction(FooterInfoActivity.ACTION_SHOW_CONTACT);			
			break;
			
		case R.id.ibs_footer_privacy: 
			infoIntent.setAction(FooterInfoActivity.ACTION_SHOW_PRIVACY);
			break;
			
		case R.id.ibs_footer_terms: 
			infoIntent.setAction(FooterInfoActivity.ACTION_SHOW_TERMS);
			break;
		
		}
		mParentActivity.startActivity(infoIntent);
	}
}
