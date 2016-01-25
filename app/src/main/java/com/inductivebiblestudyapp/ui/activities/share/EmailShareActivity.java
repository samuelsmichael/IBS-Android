package com.inductivebiblestudyapp.ui.activities.share;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.loaders.EmailSendAsyncLoader;
import com.inductivebiblestudyapp.data.model.EmailConfirmResponse;
import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.util.Utility;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * The email share using the server email api. On {@link Activity#RESULT_OK} returns the
 * content message via {@link #RESULT_CONFIRM_RESPONSE} in the intent data.
 * @author Jason Jenkins
 * @version 0.3.1-20150811
 */
public class EmailShareActivity extends AppCompatActivity implements OnClickListener, 
	LoaderCallbacks<EmailConfirmResponse> {
	
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = EmailShareActivity.class
			.getSimpleName();

	/** String. Required. The email text to send. */
	public final static String EXTRA_TEXT =  Intent.EXTRA_TEXT;
	/** Parcellable/{@link ImageItem} . Optional. The image item to share via email api. */ 
	public final static String EXTRA_IMAGE_ITEM =  CLASS_NAME + ".EXTRA_IMAGE_ITEM";
	
	
	/** Bundle key. Parcellable. A {@link EmailConfirmResponse} */
	public final static String RESULT_CONFIRM_RESPONSE =  CLASS_NAME + ".RESULT_CONFIRM_RESPONSE";
	
	private final Pattern PATTERN_VALID_EMAIL =
			Pattern.compile("\\<?([a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}[\\@]{1}[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}([\\.]{1}[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+)\\>?\\s*?,?\\s*?");
	
	private final static int REQUEST_SEND_EMAIL = 0;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private EditText mInputAddresses = null;
	private EditText mInputShare = null;	
	/** Sometimes null */
	private ImageView mImageView = null;
	
	private ProgressDialog mSendingDialog = null;
	
	/** Default is empty. */
	private String mImageId = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_email_share);

		Toolbar toolbar = (Toolbar) findViewById(R.id.ibs_email_share_toolbar);
	    setSupportActionBar(toolbar);
	    
	    View rootView = findViewById(android.R.id.content); //root for all content
	    
	    mInputAddresses = (EditText) findViewById(R.id.ibs_email_input_sendTo);
	    mInputShare = (EditText) findViewById(R.id.ibs_email_input_content_body);
	    
	    Utility.setOnClickAndReturnView(rootView, R.id.ibs_email_button_cancel, this);
	    Utility.setOnClickAndReturnView(rootView, R.id.ibs_email_button_share, this);
		
	    Bundle extras = getIntent().getExtras();
	    
	    if (extras.containsKey(EXTRA_IMAGE_ITEM)) {
	    	ImageItem image = extras.getParcelable(EXTRA_IMAGE_ITEM);
	    	mImageId = image.getId() == null ? "" : image.getId();
	    	
	    	mImageView = (ImageView) findViewById(R.id.ibs_email_image_preview);
	    	mImageView.setVisibility(View.VISIBLE);
	    	ImageLoader	.getInstance()
						.displayImage(image.getUrl(), mImageView);
	    }
	    
	    if (savedInstanceState == null) {
			String text = extras.getString(EXTRA_TEXT);
			mInputShare.setText(text);
		} else {
			checkAndShowSendingDialog(false);
		}
	    
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Utility.recycleBitmap(mImageView); //only recycle created bitmaps
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helpers methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Checks and sees if the fragment is updating. If so, show dialog. 
	 * Otherwise dismiss (if possible). Be cautious with this method around loaders.
	 * @param force Force show the dialog.
	 */
	private void checkAndShowSendingDialog(boolean force) {
		boolean show = force;		
		if (Utility.checkIfLoading(getSupportLoaderManager(), REQUEST_SEND_EMAIL)) {
			//reattach the loader if loader exists
			getSupportLoaderManager().initLoader(REQUEST_SEND_EMAIL, null, this);
			show = true;
		}
		
		if (mSendingDialog == null && show) {
			mSendingDialog = ProgressDialog.show(this, "", getString(R.string.ibs_text_sending));			
		} else if (mSendingDialog != null) {
			mSendingDialog.dismiss();
			mSendingDialog = null;
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibs_email_button_cancel:
			finish();
			break;

		case R.id.ibs_email_button_share:
			mInputAddresses.setError(null); //clear errors
			
			String inputAddresses = mInputAddresses.getText().toString().trim();
			String message = mInputShare.getText().toString().trim();
			
			if (inputAddresses.isEmpty()) {
				mInputAddresses.setError(getString(R.string.ibs_error_email_mustSpecifyRecipients));
				return;
			} 
			Matcher matcher = PATTERN_VALID_EMAIL.matcher(inputAddresses);
			String outputEmails = "";
			while (matcher.find()) {
				if (!outputEmails.isEmpty()) {
					outputEmails += ",";
					inputAddresses = inputAddresses.trim().replaceFirst(",", "").trim();
				}
				String email = matcher.group(1);
				inputAddresses = matcher.replaceFirst("").trim();
				matcher.reset(inputAddresses);
				outputEmails += email;
			}
			inputAddresses = inputAddresses.trim().replaceFirst(",", "").trim();
			Log.d(CLASS_NAME, "output: " + outputEmails);
			
			if (!inputAddresses.isEmpty()) {
				mInputAddresses.setError(getString(R.string.ibs_error_email_notAValidEmail, inputAddresses));
				return;
			}
			Bundle args = new Bundle();
			args.putString(EmailSendAsyncLoader.KEY_RECIPIENTS, outputEmails);
			args.putString(EmailSendAsyncLoader.KEY_MESSAGE, message);
			if (mImageId != null && !mImageId.isEmpty()) {
				args.putString(EmailSendAsyncLoader.KEY_IMAGE_ID, mImageId);
			}

			getSupportLoaderManager().initLoader(REQUEST_SEND_EMAIL, args, this);
			checkAndShowSendingDialog(true);
			break;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Loader listeners
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public Loader<EmailConfirmResponse> onCreateLoader(int id, Bundle args) {
		return new EmailSendAsyncLoader(this, args);
	}

	@Override
	public void onLoadFinished(Loader<EmailConfirmResponse> loader, EmailConfirmResponse result) {
		
		getSupportLoaderManager().destroyLoader(loader.getId()); //easy tracking
		
		if (result != null && result.isSuccessful()) {
			Log.d(CLASS_NAME, "Success?: " + result);
			Intent data = new Intent();
			data.putExtra(RESULT_CONFIRM_RESPONSE, result);
			setResult(RESULT_OK, data);
			finish();
		} else {
			Log.d(CLASS_NAME, "Failed: " + result);
			Utility.toastMessage(this, getString(R.string.ibs_error_email_cannotSend));
		}
		checkAndShowSendingDialog(false);
	}

	@Override
	public void onLoaderReset(Loader<EmailConfirmResponse> arg0) {}
}
