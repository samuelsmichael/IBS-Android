package com.inductivebiblestudyapp.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.auth.SigninCompatActivity;
import com.inductivebiblestudyapp.data.loaders.ProfileFetchAsyncLoader;
import com.inductivebiblestudyapp.data.loaders.ProfileUpdateAsyncLoader;
import com.inductivebiblestudyapp.data.loaders.SimpleContentAsyncLoader;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.data.model.ProfileResponse;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.data.model.ProfileResponse.Version;
import com.inductivebiblestudyapp.ui.RestartReceiver;
import com.inductivebiblestudyapp.util.Utility;

/**
 * The setup account activity. 
 * @author Jason Jenkins
 * @version 0.4.0-20150915
 */
public class SetupAccountActivity extends SigninCompatActivity {
	
	final static private String CLASS_NAME = SetupAccountActivity.class
			.getSimpleName();
	
	private static final String TAG_SETUP_FRAG = CLASS_NAME + ".TAG_SETUP_FRAG";
	private static final String TAG_CONGRATS_FRAG = CLASS_NAME + ".TAG_CONGRATS_FRAG";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup_account);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new SetupAccountFragment(), TAG_SETUP_FRAG).commit();
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setup_account, menu);
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
	}
			
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * A placeholder fragment containing a simple view.
	 * @version 0.5.0-20150623
	 */
	public static class SetupAccountFragment extends Fragment implements OnClickListener, 
		LoaderManager.LoaderCallbacks<ContentResponse> {
		
		final static private String CLASS_NAME = SetupAccountActivity.SetupAccountFragment.class
				.getSimpleName();
		final static private String LOGTAG = CLASS_NAME;
		
		private static final String KEY_CONTENT = CLASS_NAME + ".KEY_CONTENT";	
		
		private static final int REQUEST_CONTENT_LOADER = 0;
		private static final int REQUEST_UPDATE_LOADER = 1;
		private static final int REQUEST_VERSION_LOADER = 2;
		
		
		public SetupAccountFragment() {
		}

		//private OnSetupFragmentListener mListener = null;
		
		private View mProgressView = null;
		private TextView mContentView = null;
				
		private EditText mEt_firstName = null;
		private EditText mEt_lastName = null;
		private EditText mEt_address = null;
		private EditText mEt_city = null;
		private EditText mEt_state = null;
		private EditText mEt_zip = null;
		
		/** Radios in this group have an id equal to their position & tags equal to their translation id. */
		private RadioGroup mRadioGroupTranslations = null;
		
		private View mSubmitButton = null;
				
		private String mContentMessage = null;
		
		/** Set in {@link #checkRequiredInputForError(EditText)} & used in {@link #onClick(View)} */
		private boolean mInputHasErrors = false;
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putString(KEY_CONTENT, mContentMessage);
		}
		
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_setup_account,
					container, false);
			mSubmitButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_button_submit, this); 
			
			initInputs(rootView);
			
			mRadioGroupTranslations = (RadioGroup) rootView.findViewById(R.id.ibs_setup_radiogroup_translations);
			getLoaderManager().initLoader(REQUEST_VERSION_LOADER, null, mVersionCallback);
			
			mContentView = (TextView) rootView.findViewById(R.id.ibs_setup_text);
			mProgressView = Utility.getProgressView(rootView);			
			
			if (savedInstanceState != null) {
				mContentMessage = savedInstanceState.getString(KEY_CONTENT);
			} 
			
			if (mContentMessage == null) {
				getLoaderManager().initLoader(REQUEST_CONTENT_LOADER, null, this);
			} else {
				mContentView.setText(mContentMessage);
			}

			checkIfLoadingContent(false);
			
			enableViews(!checkIfSending());
			
			return rootView;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			try {
				//mListener = (OnSetupFragmentListener) getActivity();
			} catch (ClassCastException e) {
				Log.w(LOGTAG, "Activity must implement: " + OnSetupFragmentListener.class.getSimpleName());
				throw e;
			}
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Helper methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		private void initInputs(View rootView) {
			CurrentUser user = new CurrentUser(getActivity());
			mEt_firstName = findEditInput(rootView, R.id.ibs_setup_et_name_first);
			mEt_firstName.setText(user.getFirstName());
			
			mEt_lastName = findEditInput(rootView, R.id.ibs_setup_et_name_last);
			mEt_lastName.setText(user.getLastName());
			
			mEt_address = findEditInput(rootView, R.id.ibs_setup_et_address);
			mEt_address.setText(user.getAddress());
			
			mEt_city = findEditInput(rootView, R.id.ibs_setup_et_city);
			mEt_city.setText(user.getCity());
			
			mEt_state = findEditInput(rootView, R.id.ibs_setup_et_state);
			mEt_state.setText(user.getState());
			
			mEt_zip = findEditInput(rootView, R.id.ibs_setup_et_zip);	
			mEt_zip.setText(user.getZip());
			
		}
		
		/** Enables or disables all input views given the input. */
		private void enableViews(boolean enabled) {
			if (mSubmitButton != null) {
				mEt_firstName.setEnabled(enabled);
				mEt_lastName.setEnabled(enabled);
				mEt_address.setEnabled(enabled);
				mEt_city.setEnabled(enabled);
				mEt_state.setEnabled(enabled);
				mEt_zip.setEnabled(enabled);
				
				mRadioGroupTranslations.setEnabled(enabled);
				
				mSubmitButton.setEnabled(enabled);
			}
		}

		
		private static EditText findEditInput(View rootView, int id){
			EditText input = (EditText) rootView.findViewById(id);
			return input;
		}
		
		/** Used for required inputs. */
		private String checkRequiredInputForError(EditText input) {
			String out = input.getText().toString().trim();
			input.setError(null);
			if (out.isEmpty()) {
				mInputHasErrors = true;
				input.setError(getString(R.string.ibs_error_cannotBeEmpty));
			}
			return out;
		}
		
		/** Used for optional inputs. */
		private String getOptionalInput(EditText input) {
			String out = input.getText().toString().trim();
			input.setError(null);
			return out;
		}
		
		/**
		 * Performs view safety checks, then animates views (if forced) or checks whether to
		 * animate views based on loader state.
		 * @param force
		 */
		private void checkIfLoadingContent(boolean force) {
			Utility.checkIfLoading(getLoaderManager(), REQUEST_CONTENT_LOADER, mProgressView, mContentView, force);
		}
		
		private boolean checkIfSending() {
			return Utility.checkIfLoading(getLoaderManager(), REQUEST_UPDATE_LOADER);
		}
			
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Listeners
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		@Override
		public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
			checkIfLoadingContent(true);
			return new SimpleContentAsyncLoader(getActivity(), getString(R.string.ibs_config_load_setup));
			
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
			 
			 	if (data == null) {
					mContentMessage = null;
					mContentView.setText(R.string.ibs_error_cannotLoadContent);
				} else {
					mContentMessage = data.getContent();
					mContentView.setText(data.getContent());
				}
				checkIfLoadingContent(false);				
			
		}
		 
		private LoaderManager.LoaderCallbacks<UpdateResult> mProfileUpdateCallbacks = 
				new LoaderManager.LoaderCallbacks<UpdateResult>() {					
					@Override
					public void onLoaderReset(Loader<UpdateResult> arg0) {}
					
					@Override
					public void onLoadFinished(Loader<UpdateResult> arg0, final UpdateResult result) {
						getLoaderManager().destroyLoader(arg0.getId());
						
						
						new Handler().post(new Runnable() {
							
							@Override
							public void run() {
								if (result != null && result.isSuccessful()) {
									Log.d(LOGTAG, "Success!");

									//mListener.onSetupSubmit(new Bundle());
									((SetupAccountActivity) getActivity()).switchFragments(
											new SetupSuccessfulFragment(), 
											SetupAccountActivity.TAG_CONGRATS_FRAG);
									} else {
										Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
										enableViews(true);
									}
							}
						});			
						
					}
					
					@Override
					public Loader<UpdateResult> onCreateLoader(int arg0, Bundle arg1) {
						return new ProfileUpdateAsyncLoader(getActivity(), arg1);
					}
				};
				
		private LoaderManager.LoaderCallbacks<ProfileResponse> mVersionCallback = 
				new LoaderManager.LoaderCallbacks<ProfileResponse> (){
			
				@Override
				public Loader<ProfileResponse> onCreateLoader(int arg0, Bundle arg1) {
					if (mSubmitButton != null) {
						mSubmitButton.setEnabled(false);
					}
					return new ProfileFetchAsyncLoader(getActivity());
				}
				
				@Override
				public void onLoaderReset(Loader<ProfileResponse> arg0) {}
				
				@SuppressLint("InflateParams") 
				@Override
				public void onLoadFinished(Loader<ProfileResponse> loader,
						final ProfileResponse response) {
					getLoaderManager().destroyLoader(loader.getId());		
					
					if (response != null && !response.isSuccessful()) {
						//response was not successful, thus clear preferences and return to top.
						// WARNING: Beware of loops here
						RestartReceiver.sendBroadcast(getActivity());
						return;
					}
					
					new Handler().post(new Runnable() {
						
						@Override
						public void run() {
							if (response != null) {
								Version[] versions = response.getVersions();
								LayoutInflater inflater = LayoutInflater.from(getActivity());
								int position = 0;
								for (Version version : versions) {
									RadioButton cb = (RadioButton) inflater.inflate(R.layout.radiobutton_version, null);
									cb.setText(version.getName());
									cb.setTag(version.getVersionId());
									cb.setId(position++);
																		
									mRadioGroupTranslations.addView(cb);
									
									mSubmitButton.setEnabled(true);
								}
							} else {
								TextView tv = new TextView(getActivity());
								tv.setText(R.string.ibs_error_cannotLoadTranslations);
								tv.setTextAppearance(getActivity(), android.R.attr.textAppearanceLarge);
								tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
								tv.setGravity(Gravity.CENTER);
								mRadioGroupTranslations.removeAllViews();
								mRadioGroupTranslations.addView(tv);
							}
						}
					});					
				}
			};
 
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// End Loaders
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		 
		@Override
		public void onClick(View v) {
			mInputHasErrors = false;
			Bundle args = new Bundle();
			args.putString(ProfileUpdateAsyncLoader.KEY_FIRSTNAME, checkRequiredInputForError(mEt_firstName));
			args.putString(ProfileUpdateAsyncLoader.KEY_LASTNAME, checkRequiredInputForError(mEt_lastName));
			args.putString(ProfileUpdateAsyncLoader.KEY_ADDRESS, getOptionalInput(mEt_address));
			args.putString(ProfileUpdateAsyncLoader.KEY_STATE, getOptionalInput(mEt_state));
			args.putString(ProfileUpdateAsyncLoader.KEY_CITY, getOptionalInput(mEt_city));
			args.putString(ProfileUpdateAsyncLoader.KEY_ZIP, getOptionalInput(mEt_zip));
			
			String selectedVersion = "";
			
			final int checkedId = mRadioGroupTranslations.getCheckedRadioButtonId();
			
			if (checkedId < 0) {
				Utility.toastMessage(getActivity(), getString(R.string.ibs_error_selectTranslationVersion));
			} else {
				selectedVersion = (String) mRadioGroupTranslations.getChildAt(checkedId).getTag();
			}
			
			if (!mInputHasErrors && checkedId >= 0) {				
				enableViews(false);
				
				args.putString(ProfileUpdateAsyncLoader.KEY_TRANSLATIONS, selectedVersion);		
				getLoaderManager().initLoader(REQUEST_UPDATE_LOADER, args, mProfileUpdateCallbacks);
			}
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Listener interface
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		public static interface OnSetupFragmentListener {
			public void onSetupSubmit(Bundle contents);
		}
	}
	
	/**
	 * A simple {@link Fragment} subclass.
	 * @version 0.1.0-20150602
	 */
	public static class SetupSuccessfulFragment extends Fragment implements OnClickListener, 
		LoaderManager.LoaderCallbacks<ContentResponse>  {
		final static private String CLASS_NAME = SetupAccountActivity.SetupAccountFragment.class
				.getSimpleName();
		//final static private String LOGTAG = CLASS_NAME;
		
		private static final String KEY_CONTENT = CLASS_NAME + ".KEY_CONTENT";	
		
		private static final int REQUEST_CONTENT_LOADER = 0;
		
		//figure out prefetching
		
		public SetupSuccessfulFragment() {
			// Required empty public constructor
		}

		private View mProgressView = null;
		private TextView mContentView = null;
		
		private String mContentMessage = null;
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putString(KEY_CONTENT, mContentMessage);
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// Inflate the layout for this fragment
			View rootView = inflater.inflate(R.layout.fragment_setup_successful, container,
					false);			
			rootView.findViewById(R.id.ibs_button_signin).setOnClickListener(this);
			
			mContentView = (TextView) rootView.findViewById(R.id.ibs_setupsuccess_text);
			mProgressView = Utility.getProgressView(rootView);
			
			CurrentUser.userHasCompletedSetup(getActivity(), true);
			
			if (savedInstanceState != null) {
				mContentMessage = savedInstanceState.getString(KEY_CONTENT);
			} 
			
			if (mContentMessage == null) {
				getLoaderManager().initLoader(REQUEST_CONTENT_LOADER, null, this);
			} else {
				mContentView.setText(mContentMessage);
			}

			checkIfLoading(false);
			
			return rootView;
		}
		
		/**
		 * Performs view safety checks, then animates views (if forced) or checks whether to
		 * animate views based on loader state.
		 * @param force
		 */
		private void checkIfLoading(boolean force) {
			Utility.checkIfLoading(getLoaderManager(), REQUEST_CONTENT_LOADER, mProgressView, mContentView, force);
		}
			
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Listeners
		////////////////////////////////////////////////////////////////////////////////////////////////
		

		
		@Override
		public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
			return new SimpleContentAsyncLoader(getActivity(), getString(R.string.ibs_config_load_setupConfirm));
			
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
			 
			 	if (data == null) {
					mContentMessage = null;
					mContentView.setText(R.string.ibs_error_cannotLoadContent);
				} else {
					mContentMessage = data.getContent();
					mContentView.setText(data.getContent());
				}
				checkIfLoading(false);				
			
		}
		 
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// End Loaders
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		@Override
		public void onClick(View v) {
			startActivity(new Intent(getActivity(), ProfileActivity.class));
		}

	}
}
