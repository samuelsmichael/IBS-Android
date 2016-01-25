package com.inductivebiblestudyapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class SigninCompatActivity extends AppCompatActivity {
	final static private String LOGTAG = SigninCompatActivity.class
			.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//build it & check for signed in
		SigninHeadlessFragment.get(this); //FIXME bad coupling going on here
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		boolean consumed = false;
		if (SigninHeadlessFragment.get(this) != null) {
			consumed = SigninHeadlessFragment.get(this).consumeActivityResult(requestCode, resultCode, data);
		}
		if (!consumed) {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	/**
	 * Called onAttachToActivity
	 * @return
	 */
	public boolean onCheckIfSignedIn() {
		return  true;
	}
	
}
