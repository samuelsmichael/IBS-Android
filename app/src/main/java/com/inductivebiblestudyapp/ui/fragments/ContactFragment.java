package com.inductivebiblestudyapp.ui.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.loaders.ContactAsyncLoader;
import com.inductivebiblestudyapp.data.loaders.SimpleContentAsyncLoader;
import com.inductivebiblestudyapp.data.model.ContactResponse;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.ui.dialogs.MessageToolTip;
import com.inductivebiblestudyapp.util.DialogStateHolder;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A placeholder fragment containing a simple view.
 * @author Jason Jenkins
 * @version 0.6.2-20150931
 */
public class ContactFragment extends Fragment implements OnClickListener, LoaderManager.LoaderCallbacks<ContentResponse> {

	final static private String CLASS_NAME = ContactFragment.class.getSimpleName();
	final static private String LOGTAG = CLASS_NAME;
	
	private static final String KEY_CONTACT_CONTENT = CLASS_NAME + ".KEY_CONTACT_CONTENT";
	private static final String KEY_DIALOG_STATE = CLASS_NAME + ".KEY_DIALOG_CONTENT";	
	
	private static final int REQUEST_CONTACT_CONTENT_LOADER = 0;
	private static final int REQUEST_DIALOG_CONTENT_LOADER = 1;
	
	private static final int REQUEST_SEND_CONTACT_LOADER = 2;
	
	private static final String TAG_CONFIRM_DIALOG = CLASS_NAME + ".TAG_CONFIRM_DIALOG";
	
	
	public ContactFragment() {
	}
	
	private View mProgressView = null;
	private TextView mContentView = null;

	private EditText mEt_name = null;
	private EditText mEt_phone = null;
	private EditText mEt_email = null;
	private EditText mEt_message = null;
	
	private Button mBtn_submit = null;
	private View mSendingProgressView = null;
	
	
	private String mContentText = null;
	private DialogStateHolder mDialogState = null;
	
	/** Reserved for the activity to listen on. */
	private OnClickListener mFooterListener = null;
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_CONTACT_CONTENT, mContentText);
		outState.putParcelable(KEY_DIALOG_STATE, mDialogState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_contact_us,
				container, false);
		mEt_name = (EditText) rootView.findViewById(R.id.ibs_contact_et_name);
		mEt_phone = (EditText) rootView.findViewById(R.id.ibs_contact_et_phone);
		mEt_email = (EditText) rootView.findViewById(R.id.ibs_contact_et_email);
		mEt_message = (EditText) rootView.findViewById(R.id.ibs_contact_et_message);
		
		mBtn_submit = (Button) Utility.setOnClickAndReturnView(rootView, R.id.ibs_button_submit, this);
		mSendingProgressView = rootView.findViewById(R.id.ibs_contact_progressbar_sending);
		
		mContentView = (TextView) rootView.findViewById(R.id.ibs_contact_text);
		mProgressView = Utility.getProgressView(rootView);
		
		checkIfSendingMessage(false);
		
		if (savedInstanceState != null) {
			mContentText = savedInstanceState.getString(KEY_CONTACT_CONTENT);
			mDialogState = (DialogStateHolder) savedInstanceState.getParcelable(KEY_DIALOG_STATE);
		} else {
			mDialogState = new DialogStateHolder();			
		}
		
		if (mContentText == null) {
			getLoaderManager().initLoader(REQUEST_CONTACT_CONTENT_LOADER, null, this);
			checkIfLoading(true);
		} else {
			setContentView(mContentText);
			checkIfLoading(false);
		}
		
		if (!mDialogState.fetched) {
			getLoaderManager().initLoader(REQUEST_DIALOG_CONTENT_LOADER, null, this);
		}
			
		
		rootView.findViewById(R.id.ibs_footer_home).setOnClickListener(this);
		rootView.findViewById(R.id.ibs_footer_about).setOnClickListener(this);
		rootView.findViewById(R.id.ibs_footer_contact).setOnClickListener(this);
		rootView.findViewById(R.id.ibs_footer_privacy).setOnClickListener(this);
		rootView.findViewById(R.id.ibs_footer_terms).setOnClickListener(this);
		
		return rootView;
	}

	
	@Override //quick and easy interaction listener; not the right way but it works.
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		try {
			mFooterListener = (OnClickListener) getActivity();
			
		} catch (ClassCastException notImplemented) {
			Log.w(LOGTAG, "Activity must implement 'OnClickListener'");
			throw notImplemented;
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Checks to see if the fragment is sending, if so disable views and show progress bars
	 * accordingly.
	 */
	private void checkIfSendingMessage(boolean force) {
		boolean isSending = force || Utility.checkIfLoading(getLoaderManager(), REQUEST_SEND_CONTACT_LOADER);
		
		setAllInputEnabled(!isSending); 
		
		if (mSendingProgressView != null) {
			Utility.switchFadeProgressViews(mSendingProgressView, mBtn_submit, isSending);
		}
	}
	
	/** Enables/Disables all inputs based upon the bool. */
	private void setAllInputEnabled(boolean enabled) {
		if (mBtn_submit != null){
			mBtn_submit.setEnabled(enabled);
			mEt_name.setEnabled(enabled);
			mEt_email.setEnabled(enabled);
			mEt_phone.setEnabled(enabled);
			mEt_message.setEnabled(enabled);	
			
			if (!enabled) {
				mEt_name.setError(null);
				mEt_email.setError(null);
				mEt_phone.setError(null);
				mEt_message.setError(null);
			}
		}
	}
	
	/** Sets all input contents to blank. */
	private void clearInputContents(){
		if (mBtn_submit != null){
			mEt_name.setText("");
			mEt_email.setText("");
			mEt_phone.setText("");
			mEt_message.setText("");
		}
	}
	
	/**
	 * Processes the contact response and checks for any error. 
	 * If there are no errors, it shows the confirm dialog. 
	 * @param response
	 */
	private void processContactResponse(ContactResponse response) {
		if (response == null){
			if (mEt_name != null) {
				mEt_name.post(new Runnable() {				
					@Override
					public void run() { 
						showConfirmTooltip(getString(R.string.ibs_error_cannotSendMessage)); 
					}
				});		
			}
			return; //we cannot process non-results
		}
		
		checkAndSetError(mEt_name, response.getNameErrorMessage());
		checkAndSetError(mEt_email, response.getEmailErrorMessage());
		checkAndSetError(mEt_phone, response.getPhoneErrorMessage());
		checkAndSetError(mEt_message, response.getMessageErrorMessage());	
		
		final boolean successful = response.isSuccessful();			
		
		if (mEt_name != null) {
			mEt_name.post(new Runnable() {				
				@Override
				public void run() {
					if (successful) {
						clearInputContents();
						showConfirmTooltip(mDialogState.message);
					} else {
						showConfirmTooltip(getString(R.string.ibs_error_cannotSendMessage));
					}
				}
			});		
		}
	}
	
	/** Checks to see if the input is not null, the error not empty. 
	 * And if both are so, sets the error to the input
	 * @param input
	 * @param error
	 * @return <code>true</code> on error, <code>false</code> on no errors.
	 */
	static private boolean checkAndSetError(EditText input, String error) {
		if (input != null && !error.isEmpty()) {
			input.setError(error);
			return true;
		}
		return false;
	}
	
	private void setContentView(String content) {
		if (mContentView != null) { //content text is not always in layout
			mContentView.setText(content);			
		}
	}

	
	
	/**
	 * Performs view safety checks, then animates views (if forced) or checks whether to
	 * animate views based on loader state.
	 * @param force
	 */
	private void checkIfLoading(boolean force) {
		Utility.checkIfLoading(getLoaderManager(), REQUEST_CONTACT_CONTENT_LOADER, mProgressView, mContentView, force);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helpers - dialog/tooltip
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Attempts to find and set the dialog message, while caching it on this fragment.  */
	private void getAndSetConfirmMessage(ContentResponse data) {
		
		String message = getString(R.string.ibs_error_cannotLoadContent);
		if (data != null) {
			message = data.getContent();
			mDialogState.fetched = true;
		} else {
			mDialogState.fetched = false;
		}
		mDialogState.message = message;
		
		Fragment frag = getFragmentManager().findFragmentByTag(TAG_CONFIRM_DIALOG);
		if (frag != null && frag instanceof MessageToolTip) {
			((MessageToolTip) frag).updateMessage(message);
		}		
	}

	
	/** Shows the dialog/tooltip with message. */
	private void showConfirmTooltip(String message) {
		final View anchor = getActivity().findViewById(R.id.ibs_text_contact_title);
		new MessageToolTip.Builder(message, anchor)
			.setCentered(false)
			.build()
			.show(getFragmentManager(), TAG_CONFIRM_DIALOG);
	}
	
		
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	 
	private LoaderManager.LoaderCallbacks<ContactResponse> mContactCallback = 
			new LoaderManager.LoaderCallbacks<ContactResponse>() {
		@Override
		public Loader<ContactResponse> onCreateLoader(int id, Bundle args) {
			switch (id) {
			
			case REQUEST_SEND_CONTACT_LOADER:
				checkIfSendingMessage(true);
				return new ContactAsyncLoader(
						getActivity(), 
						mEt_name.getText().toString(), 
						mEt_email.getText().toString(), 
						mEt_phone.getText().toString(), 
						mEt_message.getText().toString());
			default:
				throw new UnsupportedOperationException("Id is not recognized? " + id);
			}
			
		}
		
		@Override
		public void onLoaderReset(Loader<ContactResponse> loader) {
			// nothing to do yet.			
		}
		
		 @Override
		public void onLoadFinished(Loader<ContactResponse> loader,
				ContactResponse data) {
			 final int id = loader.getId();
			 
			 //makes it easier to tell if we are still loading
			 getLoaderManager().destroyLoader(id); 
			 switch (id) {

			 case REQUEST_SEND_CONTACT_LOADER:
				 checkIfSendingMessage(false);
				 processContactResponse(data);
				 break;
				 
			 }
			
		}
			};

	
	@Override
	public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case REQUEST_CONTACT_CONTENT_LOADER:
			checkIfLoading(true);
			return new SimpleContentAsyncLoader(getActivity(), getString(R.string.ibs_config_load_contact));
		case REQUEST_DIALOG_CONTENT_LOADER:
			return new SimpleContentAsyncLoader(getActivity(), getString(R.string.ibs_config_load_contactConfirm));
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
		 case REQUEST_CONTACT_CONTENT_LOADER:
		 	if (data == null) {
				mContentText = null;
				setContentView(getString(R.string.ibs_error_cannotLoadContent));
			} else {
				mContentText = data.getContent();
				setContentView(data.getContent());
			}
			checkIfLoading(false);
			break;
		 
		 case REQUEST_DIALOG_CONTENT_LOADER:
			 getAndSetConfirmMessage(data);
			 break;
			 
		 }
		
	}
	 
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End Loaders
	////////////////////////////////////////////////////////////////////////////////////////////////
	 
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibs_button_submit:
			getLoaderManager().initLoader(REQUEST_SEND_CONTACT_LOADER, null, mContactCallback);
			break;
			
		default://fall through to main
			mFooterListener.onClick(v);
			break;
		}
	}


}
