package com.inductivebiblestudyapp.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.SigninCompatActivity;
import com.inductivebiblestudyapp.ui.fragments.HomeFragment;
import com.inductivebiblestudyapp.ui.fragments.SignUpSignInFragment;

/**
 * The landing point of the application.
 * @author Jason Jenkins
 * @version 0.5.1-20150716
 */
public class SigninActivity extends SigninCompatActivity implements OnClickListener {
	final static private String CLASS_NAME = SigninActivity.class.getSimpleName();

	/* Bundle key. The last fragment to be loaded (and so reloaded). 
	 * String */
	//private static final String KEY_FRAG_TAG = CLASS_NAME + ".KEY_FRAG_TAG";
		
	private static final String TAG_HOME_FRAG = CLASS_NAME + ".TAG_HOME_FRAG";
	private static final String TAG_SIGN_IN = CLASS_NAME + ".TAG_SIGN_IN";
	private static final String TAG_SIGN_UP = CLASS_NAME + ".TAG_SIGN_UP";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null ) {
			pushFragments(new HomeFragment(), TAG_HOME_FRAG);
		}
		
	}
	
	@Override
	@Deprecated
	public boolean onCheckIfSignedIn() {
		return false;
	}
	
	

	@Override
	public void onBackPressed() {
		FragmentManager fragManager = getSupportFragmentManager();
		final int count = fragManager.getBackStackEntryCount();
		
		if (count > 1){
			fragManager.popBackStack(); //pop fragments before quitting
		} else {
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Switches the visible fragment (with animation) and saves the tag.
	 * @param fragment The fragment to switch in.
	 * @param tag The tag to give the fragment.
	 */
	private void pushFragments(Fragment fragment, String tag){
		FragmentManager fragManager = getSupportFragmentManager();
		fragManager.beginTransaction()
				.addToBackStack(tag)
				.setCustomAnimations(
						android.R.anim.fade_in, 
						android.R.anim.fade_out,
						android.R.anim.fade_in, 
						android.R.anim.fade_out)
				.replace(R.id.container, fragment, tag)
				.commit();
	}
	

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.ibs_button_signIn:
			pushFragments(
					SignUpSignInFragment.newInstance(SignUpSignInFragment.VALUE_SIGN_IN_MODE), 
					SigninActivity.TAG_SIGN_IN);
			break;
		case R.id.ibs_button_signUp:
			pushFragments(
					SignUpSignInFragment.newInstance(SignUpSignInFragment.VALUE_SIGN_UP_MODE), 
					SigninActivity.TAG_SIGN_UP); 
		
			break;
			
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////

	//move to fragments
}
