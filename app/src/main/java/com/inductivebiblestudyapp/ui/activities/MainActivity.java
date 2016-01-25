package com.inductivebiblestudyapp.ui.activities;

import com.inductivebiblestudyapp.auth.CurrentUser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * The landing point of the application. This will determine which activity to actually 
 * launch (based on if the user is signed in, awaiting setup, etc.) and then end itself.
 * 
 * @author Jason Jenkins
 * @version 0.2.0-20150716
 */
public class MainActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = null;
		if (CurrentUser.hasCompletedSetup(this)) { //we have setup? Skip to profile!
			intent = new Intent(this, ProfileActivity.class);
			
		} else if (CurrentUser.hasAccessToken(this)) { //we are signed in, prepare to jump
			intent = new Intent(this, SetupAccountActivity.class);
			
		} else {
			intent = new Intent(this, SigninActivity.class);			
		}
		
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		
		finish();		
	}
}
