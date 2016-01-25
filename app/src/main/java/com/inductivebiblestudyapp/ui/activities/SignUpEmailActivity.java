package com.inductivebiblestudyapp.ui.activities;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.loaders.EmailSignupAsyncLoader;
import com.inductivebiblestudyapp.data.loaders.SimpleContentAsyncLoader;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.data.model.EmailSignupResponse;
import com.inductivebiblestudyapp.ui.actionwrappers.FooterViewActionWrapper;
import com.inductivebiblestudyapp.util.Utility;

/** Results are {@link Activity#RESULT_OK} if sign-up completes for email.
 * If not, they are on of the other results (including {@link Activity#RESULT_CANCELED}).
 * 
 * @author Jason Jenkins
 * @version 0.1.2-20150828
 * 
 *  */
public class SignUpEmailActivity extends AppCompatActivity {
	//final static private String CLASS_NAME = SignUpEmailActivity.class.getSimpleName();
	
	//public static final String KEY_EMAIL = CLASS_NAME + ".KEY_EMAIL";
		
	public static final int RESULT_FACEBOOK_SIGN_UP = 1;
	public static final int RESULT_TWITTER_SIGN_UP = 2;
	public static final int RESULT_LINKEDIN_SIGN_UP = 3;
	public static final int RESULT_GOOGLE_PLUS_SIGN_UP = 4;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End public constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up_email);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new SignUpEmailFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_up_email, menu);
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
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		super.onBackPressed();
	}

	/**
	 * A placeholder fragment containing a simple view.
	 * @author Jason Jenkins
	 * @version 0.4.0-20150615  */
	public static class SignUpEmailFragment extends Fragment implements OnClickListener,
		LoaderManager.LoaderCallbacks<ContentResponse> {
		
		final static private String CLASS_NAME = SignUpEmailActivity.SignUpEmailFragment.class
				.getSimpleName();
		private static final String KEY_EMAIL_SIGNUP_CONTENT = CLASS_NAME + ".KEY_EMAIL_SIGNUP_CONTENT";
		
		private static final int REQUEST_EMAIL_SIGNUP_LOADER = 0;
		
		public SignUpEmailFragment() {
		}
		
		private TextView mContentView = null;
		private View mProgressView = null;
		
		private ScrollView mScrollView = null;
		
		private EditText mEt_email = null;
		private EditText mEt_password1 = null;
		private EditText mEt_password2 = null;
		private View mSubmitButton = null;
		
		private String mContentMessage = null;

		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putString(KEY_EMAIL_SIGNUP_CONTENT, mContentMessage);
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_signup_email,
					container, false);
			
			mScrollView = (ScrollView) rootView;
			
			mEt_email = (EditText) rootView.findViewById(R.id.ibs_signup_et_email);
			mEt_password1 = (EditText) rootView.findViewById(R.id.ibs_signup_et_password1);
			mEt_password2 = (EditText) rootView.findViewById(R.id.ibs_signup_et_password2);
			
			mSubmitButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_button_submit, this);
			
			rootView.findViewById(R.id.ibs_social_button_email).setVisibility(View.GONE);
			Utility.setOnClickAndReturnView(rootView, R.id.ibs_social_button_facebook, this);
			Utility.setOnClickAndReturnView(rootView, R.id.ibs_social_button_google_plus, this);
			Utility.setOnClickAndReturnView(rootView, R.id.ibs_social_button_linkedin, this);
			Utility.setOnClickAndReturnView(rootView, R.id.ibs_social_button_twitter, this);
			
			mContentView = (TextView) rootView.findViewById(R.id.ibs_signup_email_text);
			mProgressView = Utility.getProgressView(rootView);
			
			if (savedInstanceState != null) {
				mContentMessage = savedInstanceState.getString(KEY_EMAIL_SIGNUP_CONTENT);
			} 
			
			if (mContentMessage == null) {
				getLoaderManager().initLoader(REQUEST_EMAIL_SIGNUP_LOADER, null, this);
				checkIfLoading(true);
				
			} else {
				if (mContentView != null) { //content text is not always in layout
					mContentView.setText(mContentMessage);			
				}
				checkIfLoading(false);
			}
			
			FooterViewActionWrapper.newInstance(getActivity(), rootView);
			
			return rootView;
		}		
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Helper methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		private void setInputEnabled(boolean enabled) {
			if (mSubmitButton == null) {
				return;
			}
			mEt_email.setEnabled(enabled);
			mEt_password1.setEnabled(enabled);
			mEt_password2.setEnabled(enabled);
			mSubmitButton.setEnabled(enabled);
		}
		
		//Default visibility for performance.
		/**
		 * Sets the input errors if not empty. 
		 * @param edit
		 * @param emailError
		 * @return <code>true</code> if there is an error, <code>false</code> if there
		 * is none.
		 */
		static boolean setInputError(EditText edit, final String emailError) {
			if (edit != null && !emailError.isEmpty()) {
				edit.setError(emailError);
				return true;
			}
			return false;
		}

		/**
		 * Performs view safety checks, then animates views (if forced) or checks whether to
		 * animate views based on loader state.
		 * @param force
		 */
		private void checkIfLoading(boolean force) {
			Utility.checkIfLoading(getLoaderManager(), REQUEST_EMAIL_SIGNUP_LOADER, mProgressView, mContentView, force);
		}
			
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Listeners
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		private LoaderManager.LoaderCallbacks<EmailSignupResponse> mEmailSignupCallback = 
			new LoaderManager.LoaderCallbacks<EmailSignupResponse>() {
				@Override
				public void onLoaderReset(Loader<EmailSignupResponse> arg0) {}
				
				@Override
				public void onLoadFinished(Loader<EmailSignupResponse> arg0,
						EmailSignupResponse data) {
					if (data != null) {
						final String emailErrorMsg = data.getEmailErrorMessage();
						final String passErrorMsg = data.getPasswordErrorMessage();
						
						final boolean accountAlreadyExists = !data.isSuccessful();
						
						//to avoid state issues.
						new Handler().post(new Runnable() {							
							@Override
							public void run() {
								boolean emailError = setInputError(mEt_email, emailErrorMsg);
								boolean pass1Error = setInputError(mEt_password1, passErrorMsg);
								boolean pass2Error = setInputError(mEt_password2, passErrorMsg);
								
								setInputEnabled(true);
								
								if ((emailError || pass1Error || pass2Error || accountAlreadyExists) == false) {
									
									//we are good, continue.

									getActivity().setResult(RESULT_OK);
									getActivity().finish();
									return;
								} else if (accountAlreadyExists) { //we cannot let them re-create an account
									setInputError(mEt_email, getString(R.string.ibs_error_emailAccountExists)); 
								} 
								
								//smooth scroll to top to show errors.
								mScrollView.smoothScrollTo(0, 0);
							}
						});
					} else {
						Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
						//getActivity().setResult(RESULT_OK);
						//getActivity().finish();
					}
				}
				
				@Override
				public Loader<EmailSignupResponse> onCreateLoader(int id, Bundle args) {
					final String email = mEt_email.getText().toString();
					final String password = mEt_password1.getText().toString();
					//at this point we assume the passwords are the same.
					
					return new EmailSignupAsyncLoader(getActivity(), email, password);
				}
			};
		
		@Override
		public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
			switch (id) {
			case REQUEST_EMAIL_SIGNUP_LOADER:
				checkIfLoading(true);
				return new SimpleContentAsyncLoader(getActivity(), getString(R.string.ibs_config_load_signupEmail));
			default:
				throw new UnsupportedOperationException("Id is not recognized? " + id);
			}
			
		}
		
		@Override
		public void onLoaderReset(Loader<ContentResponse> loader) {
			// nothing to do yet.
			
		}
		
		 @Override
		public void onLoadFinished(Loader<ContentResponse> loader,
				ContentResponse data) {
			 final int id = loader.getId();
			 
			 //makes it easier to tell if we are still loading
			 getLoaderManager().destroyLoader(id); 
			 switch (id) {
			 case REQUEST_EMAIL_SIGNUP_LOADER:
			 	if (data == null) {
					mContentMessage = null;
					mContentView.setText(R.string.ibs_error_cannotLoadContent);
				} else {
					mContentMessage = data.getContent();
					mContentView.setText(data.getContent());
				}
				checkIfLoading(false);
				break;
			 }
			
		}
		 
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// End Loaders
		////////////////////////////////////////////////////////////////////////////////////////////////		
		
		@Override
		public void onClick(View v) {
			int result = RESULT_CANCELED;
			switch (v.getId()) {
			case R.id.ibs_button_submit:
				if (mEt_password1.getText().toString().equals(mEt_password2.getText().toString())) {
					mEt_email.setError(null);
					mEt_password1.setError(null);
					mEt_password2.setError(null);
					
					//the password are the same
					setInputEnabled(false);
					getLoaderManager().restartLoader(0, null, mEmailSignupCallback);
				} else {
					String error = getString(R.string.ibs_error_passwordsMustMatch);
					mEt_password1.setError(error);
					mEt_password2.setError(error);
				}
				return;
				
			case R.id.ibs_social_button_facebook:
				result = RESULT_FACEBOOK_SIGN_UP;
				break;
				
			case R.id.ibs_social_button_google_plus:
				result = RESULT_GOOGLE_PLUS_SIGN_UP;
				break;
				
			case R.id.ibs_social_button_linkedin:
				result = RESULT_LINKEDIN_SIGN_UP;
				break;
				
			case R.id.ibs_social_button_twitter:
				result = RESULT_TWITTER_SIGN_UP;
				break;
			}
			getActivity().setResult(result);
			getActivity().finish();
		}
	}
}
