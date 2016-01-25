package com.inductivebiblestudyapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.ui.fragments.ContactFragment;
import com.inductivebiblestudyapp.ui.fragments.InfoFragment;

/**
 * Provides a simple activity to handle the similar events of the footer activity.
 * @author Jason Jenkins
 * @version 0.2.0-20150715
 */
public class FooterInfoActivity extends AppCompatActivity implements OnClickListener {
	final static private String CLASS_NAME = FooterInfoActivity.class.getSimpleName();
	
	/** Start activity with About loaded. */
	public static final String ACTION_SHOW_ABOUT = CLASS_NAME + ".ACTION_SHOW_ABOUT";
	/** Start activity with Contact fragment loaded. */
	public static final String ACTION_SHOW_CONTACT = CLASS_NAME + ".ACTION_SHOW_CONTACT";
	/** Start activity with Privacy loaded. */
	public static final String ACTION_SHOW_PRIVACY = CLASS_NAME + ".ACTION_SHOW_PRIVACY";
	/** Start activity with Terms  loaded. */
	public static final String ACTION_SHOW_TERMS = CLASS_NAME + ".ACTION_SHOW_TERMS";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End public constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Bundle key. The last fragment to be loaded (and so reloaded). 
	 * String */
	private static final String KEY_FRAG_TAG = CLASS_NAME + ".KEY_FRAG_TAG";
	
	
	private static final String TAG_ABOUT_FRAG = CLASS_NAME + ".TAG_ABOUT_FRAG";
	private static final String TAG_CONTACT_FRAG = CLASS_NAME + ".TAG_CONTACT_FRAG";
	private static final String TAG_PRIVACY_FRAG = CLASS_NAME + ".TAG_PRIVACY_FRAG";
	private static final String TAG_TERMS_FRAG = CLASS_NAME + ".TAG_TERMS_FRAG";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The current fragment loaded. */
	private String mCurrentFragTag = "";
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_FRAG_TAG, mCurrentFragTag);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_footer_info);
		

		if (savedInstanceState != null) {
			//we have previously loaded, so we are reloading.
			mCurrentFragTag = savedInstanceState.getString(KEY_FRAG_TAG);
			
			FragmentManager fm = getSupportFragmentManager();
			Fragment frag = fm.findFragmentByTag(mCurrentFragTag);
			fm.beginTransaction().attach(frag).commit();
			return;
		}
		
		//we are loading for the first time.
		final String action = getIntent().getAction();
		
		if (ACTION_SHOW_ABOUT.equals(action)) {
			loadFragmentByTag(TAG_ABOUT_FRAG);
			
		} else if (ACTION_SHOW_CONTACT.equals(action)) {
			loadFragmentByTag(TAG_CONTACT_FRAG);
			
		} else if (ACTION_SHOW_PRIVACY.equals(action)) {
			loadFragmentByTag(TAG_PRIVACY_FRAG);
			
		} else if (ACTION_SHOW_TERMS.equals(action)) {
			loadFragmentByTag(TAG_TERMS_FRAG);
			
		} else {
			throw new IllegalArgumentException("Need to specify action");
		}
		
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		
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

	/** Simple way to load fragments by tag. */
	private void loadFragmentByTag(String tag) {
		if (TAG_ABOUT_FRAG.equals(tag)) {
			
			switchFragments(
					InfoFragment.newInstance(
							getString(R.string.ibs_title_about), 
							getString(R.string.ibs_config_load_about)), 
					TAG_ABOUT_FRAG);
		} else if (TAG_CONTACT_FRAG.equals(tag)) {
			switchFragments(new ContactFragment(), TAG_CONTACT_FRAG);
			
		} else if (TAG_PRIVACY_FRAG.equals(tag)) {
			switchFragments(
					InfoFragment.newInstance(
							getString(R.string.ibs_title_privacy), 
							getString(R.string.ibs_config_load_privacy)), 
					TAG_PRIVACY_FRAG);
			
		} else if (TAG_TERMS_FRAG.equals(tag)) {
			switchFragments(
					InfoFragment.newInstance(
							getString(R.string.ibs_title_terms), 
							getString(R.string.ibs_config_load_terms)), 
					TAG_TERMS_FRAG);
			
		} 
	}

	
	/**
	 * Switches the visible fragment (with animation) and saves the tag.
	 * @param fragment The fragment to switch in.
	 * @param tag The tag to give the fragment.
	 */
	private void switchFragments(Fragment fragment, String tag){
		FragmentManager fragManager = getSupportFragmentManager();
		//clear the entire backstack
		fragManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		fragManager.beginTransaction()
				.setCustomAnimations(
						android.R.anim.fade_in, 
						android.R.anim.fade_out)
				.replace(R.id.container, fragment, tag)
				.commit();
		mCurrentFragTag = tag;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibs_footer_home:
			Intent main = new Intent(this, MainActivity.class);
			startActivity(main); //launches main, which will clear & launch the appropriate activity
			finish(); //and finishes
			break;
			
		case R.id.ibs_footer_about: 
			loadFragmentByTag(TAG_ABOUT_FRAG);
			break;
			
		case R.id.ibs_footer_contact:
			loadFragmentByTag(TAG_CONTACT_FRAG);			
			break;
			
		case R.id.ibs_footer_privacy: 
			loadFragmentByTag(TAG_PRIVACY_FRAG);
			break;
			
		case R.id.ibs_footer_terms: 
			loadFragmentByTag(TAG_TERMS_FRAG);
			break;
		
		}
	}
	
}
