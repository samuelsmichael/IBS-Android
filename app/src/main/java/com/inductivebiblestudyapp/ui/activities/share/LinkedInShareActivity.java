package com.inductivebiblestudyapp.ui.activities.share;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.util.Utility;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Refer to:
 *  - https://developer.linkedin.com/docs/share-on-linkedin
 * @author Jason Jenkins
 * @version 0.3.1-20150811
 */
public class LinkedInShareActivity extends AppCompatActivity implements TextWatcher, OnClickListener {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = LinkedInShareActivity.class
			.getSimpleName();
	public final static String LOGTAG = CLASS_NAME;
	
	/** String. Required. The email text to send. */
	public final static String EXTRA_TEXT =  Intent.EXTRA_TEXT;
	/** Parcellable/{@link ImageItem} . Optional. The image item to share via email api. */ 
	public final static String EXTRA_IMAGE_ITEM =  CLASS_NAME + ".EXTRA_IMAGE_ITEM";
	/** String . Optional. The title to give. */ 
	public final static String EXTRA_IMAGE_TITLE =  CLASS_NAME + ".EXTRA_IMAGE_TITLE";
	
	private final static String KEY_LINKEDIN_ACCESSTOKEN =  CLASS_NAME + ".KEY_LINKEDIN_ACCESSTOKEN";
	private final static String KEY_IS_SHARING =  CLASS_NAME + ".KEY_IS_SHARING";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End keys
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private final static int LINKEDIN_INPUT_LIMIT = 600;
	
	private final static String LINKEDIN_CODE_PUBLIC = "anyone";
	private final static String LINKEDIN_CODE_CONNECTIONS = "connections-only";
	
	/** Share JSON prepared string. 
	 * Takes two strings: 
	 *  1) comment, 
	 *  2) blank string OR the contents of {@link #LINKEDIN_SHARE_LINK_JSON} 
	 *  3) code (anyone or connections-only) */
	private static final String LINKEDIN_SHARE_JSON = "{" +
		    "\"comment\":\"%s\"," +
		    " %s " +
		    "\"visibility\":{" +
		    "    \"code\":\"%s\"}" +		    
		    "}";
	
	/** Share JSON prepared string. 
	 * Takes 4 strings: 1) title, 2) description, 3) submitted-url, 4) submitted-imageurl */
	private static final String LINKEDIN_SHARE_LINK_JSON =
			"\"content\": {" +
	    	"\"title\": \"%s\"," +
	    	"\"description\": \"%s\"," +
	    	"\"submitted-url\": \"%s\"," + 
	    	"\"submitted-image-url\": \"%s\" " +
	    	"},";
			
	
	/*
	 * Sample JSON POST body:
	 * {
  "comment": "Check out developer.linkedin.com!",
  "content": {
    "title": "LinkedIn Developers Resources",
    "description": "Leverage LinkedIn's APIs to maximize engagement",
    "submitted-url": "https://developer.linkedin.com",  
    "submitted-image-url": "https://example.com/logo.png"
  },
  "visibility": {
    "code": "anyone"
  }  
}
	 */
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	

	
	/*
	 * This class and layout was done in a huge rush to satisfy the 
	 * confirmation dialog (due to LinkedIn not returning RESULT_OK). 
	 * 
	 *  Thus the disorganized layout and class. 
	 */
	
	/** Set in {@link #showLoading(boolean)} */
	private ProgressDialog mLoadingdialog = null;
	
	private View mShareButton = null;
	
	private EditText mInputShareText = null;
	private TextView mInputTextCount = null;
	private Spinner mAudienceSpinner = null;
	/** Sometimes null. */
	private ImageView mImageView = null; 
	
	private View mRootView = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End views
	////////////////////////////////////////////////////////////////////////////////////////////////		
		 
	private int mColorRed = 0;
	private int mColorGray = 0;
	
	private String mLinkedInAccessToken = "";
	
	private String mImageTitle = null;
	private ImageItem mImageItem = null;
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_LINKEDIN_ACCESSTOKEN, mLinkedInAccessToken);
		outState.putBoolean(KEY_IS_SHARING, mLoadingdialog != null);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_linked_in_share);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.ibs_linkedin_share_toolbar);
	    setSupportActionBar(toolbar);
		
		mRootView = findViewById(android.R.id.content); //root for all content
		
		final String[] shareTypes = new String[] {
			getString(R.string.ibs_label_linkedIn_sharePublic),
			getString(R.string.ibs_label_linkedIn_shareConnections)
		};
		mColorRed = getResources().getColor(R.color.ibs_red);
		mColorGray = getResources().getColor(R.color.ibs_gray);
		
		mInputShareText = (EditText) findViewById(R.id.ibs_linkedin_share_text_content);
		mInputShareText.addTextChangedListener(this);
		
		mInputTextCount = (TextView) findViewById(R.id.ibs_linkedin_share_text_characterCount);
		
		mAudienceSpinner = (Spinner) findViewById(R.id.ibs_linkedin_share_spinner_audience);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_text, shareTypes);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAudienceSpinner.setAdapter(adapter);
		mAudienceSpinner.setSelection(1);
		
		mShareButton = Utility.setOnClickAndReturnView(mRootView, R.id.ibs_linkedin_button_share, this);
		Utility.setOnClickAndReturnView(mRootView, R.id.ibs_linkedin_text_cancel, this);
		
		Bundle extras = getIntent().getExtras();
		
		if(extras.containsKey(EXTRA_IMAGE_ITEM) && extras.containsKey(EXTRA_IMAGE_TITLE)) {
			mImageView = (ImageView) findViewById(R.id.ibs_linkedin_image_preview);
			mImageItem = extras.getParcelable(EXTRA_IMAGE_ITEM);
			mImageTitle = extras.getString(EXTRA_IMAGE_TITLE);
			
			mImageView.setContentDescription(mImageItem.getName());
			mImageView.setVisibility(View.VISIBLE);
			ImageLoader	.getInstance()
						.displayImage(mImageItem.getUrl(), mImageView);
		}
		
		if (savedInstanceState == null) {
			String text = extras.getString(EXTRA_TEXT);
			mInputShareText.setText(text);
			checkTextLengthAndSetView(text);

		} else {
			mLinkedInAccessToken = savedInstanceState.getString(KEY_LINKEDIN_ACCESSTOKEN, mLinkedInAccessToken); 
			if (savedInstanceState.getBoolean(KEY_IS_SHARING)) {
				showLoading(true);
			}
			checkTextLengthAndSetView(mInputShareText.getText());
		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		showLoading(false);
		Utility.recycleBitmap(mImageView); //only recycle created bitmaps
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	/** Shares the given content, using the given code. */
	private void shareContent(String text, String code) {		
		showLoading(true);
		APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
		
		String linkContent = "";
		if (mImageItem != null) {
			String url = mImageItem.getUrl();
			linkContent = String.format(LINKEDIN_SHARE_LINK_JSON, mImageTitle, "", url, url);
		}
		
		final String url = "https://api.linkedin.com/v1/people/~/shares";
		final String payload = String.format(LINKEDIN_SHARE_JSON, text, linkContent, code);
		
		apiHelper.postRequest(this, url, payload, mShareListener);
	}
	
	/** Shows dialog if <code>true</code>, hides if <code>false</code> */
	private void showLoading(boolean show) {
		if (mLoadingdialog == null && show) {
			mLoadingdialog = ProgressDialog.show(this, "", getString(R.string.ibs_text_sharing));			
		} else if (mLoadingdialog != null && !show) {
			mLoadingdialog.dismiss();
			mLoadingdialog = null;
		}
	}
	
	/** Assumes necessary views are set. 
	 * Checks the button and sets the view's state; disabling/enabling buttons
	 * and updating counts.
	 */
	private void checkTextLengthAndSetView(CharSequence text) {
		final int LENGTH = text.toString().trim().length();
		if (LENGTH <= 0 || LENGTH > LINKEDIN_INPUT_LIMIT) {
			mInputTextCount.setTextColor(mColorRed);
			mShareButton.setEnabled(false);
		} else {
			mShareButton.setEnabled(true);
			mInputTextCount.setTextColor(mColorGray);
		}
		mInputTextCount.setText(String.valueOf(LENGTH));
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////

	private ApiListener mShareListener = new ApiListener() {
		 
		@Override
		public void onApiSuccess(ApiResponse apiResponse) {
			Log.d(LOGTAG, "LinkedIn apiResponse: " + apiResponse);
			setResult(RESULT_OK);
			finish();				
		}
		
		@Override
		public void onApiError(LIApiError LIApiError) {
			Log.d(LOGTAG, "LinkedIn error: " + LIApiError);
			String message = null;
			if (LIApiError != null && LIApiError.getApiErrorResponse() != null){
				message = LIApiError.getApiErrorResponse().message;
			}
			if (LIApiError.getHttpStatusCode() == 400 && message != null) {
				Utility.toastMessage(LinkedInShareActivity.this, message);
			} else {
				Utility.toastMessage(LinkedInShareActivity.this, getString(R.string.ibs_error_cannotConnect));
			}
			showLoading(false);
		}
	};
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start text listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		checkTextLengthAndSetView(s);
	}

	@Override
	public void afterTextChanged(Editable s) {}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start click listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibs_linkedin_button_share: 
			String text = mInputShareText.getText().toString().trim();
			if (text.isEmpty()) {
				Utility.toastMessage(this, getString(R.string.ibs_error_cannotBeEmpty));
				return;
			}
			final int index = mAudienceSpinner.getSelectedItemPosition();
			String code = "";
			if (index == 0) {
				//public
				code = LINKEDIN_CODE_PUBLIC;
			} else if (index == 1) {
				//connections
				code = LINKEDIN_CODE_CONNECTIONS;
			}
			Log.d(CLASS_NAME, "code selected: " + code);
			
			shareContent(text, code);
			
			break;
			
		case R.id.ibs_linkedin_text_cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
			
		default:
			break;
		}
	}
}
