package com.inductivebiblestudyapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager;
import com.inductivebiblestudyapp.ui.actionwrappers.FooterViewActionWrapper;
import com.inductivebiblestudyapp.ui.fragments.UpgradeFragment;

/**
 * 
 * @version 0.2.0-20150812
 *
 */
public class UpgradeActivity extends AppCompatActivity {
	final static private String CLASS_NAME = UpgradeActivity.class
			.getSimpleName();
	
	//private static String TAG_UPGRADE_FRAG_STUB = CLASS_NAME + ".TAG_UPGRADE_FRAG_STUB";	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	/**
	 * See {@link UpgradeFragment} for implementation,
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upgrade);
		if (savedInstanceState == null) {
			// nothing to do
		}
		//rootView is content for all activities
		FooterViewActionWrapper.newInstance(this, findViewById(android.R.id.content));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		UpgradeFragment frag = 
				(UpgradeFragment) getSupportFragmentManager().findFragmentById(R.id.ibs_fragment_upgrade);
		frag.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.upgrade, menu);
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

	
}
