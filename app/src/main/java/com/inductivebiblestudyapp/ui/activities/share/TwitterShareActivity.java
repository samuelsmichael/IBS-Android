package com.inductivebiblestudyapp.ui.activities.share;


import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.twitter.TwitterLoginActivity;
import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.util.Utility;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Simple activity to show the twitter share for twitter.
 * Allows for the RESULT_OK to be returned when tweeted.
 * 
 * @author Jason Jenkins
 * @version 0.3.3-20150811
 */
public class TwitterShareActivity extends AppCompatActivity implements TextWatcher, OnClickListener {
	final static private String CLASS_NAME = TwitterShareActivity.class
			.getSimpleName();
	
	private static final String KEY_TWITTER_HANDLE = CLASS_NAME + ".KEY_TWITTER_HANDLE";

	/** String. Required. The email text to send. */
	public static final String EXTRA_TEXT =  Intent.EXTRA_TEXT;
	/** Parcellable/{@link ImageItem} . Optional. The image item to share via email api. */ 
	public static final String EXTRA_IMAGE_ITEM =  CLASS_NAME + ".EXTRA_IMAGE_ITEM";
	/** String . Optional. The local image file uri for sharing. */ 
	public static final String EXTRA_IMAGE_URI =  CLASS_NAME + ".EXTRA_IMAGE_URI";
	
	//private static final String TWITTER_URL = "https://twitter.com/intent/tweet?text=%s"; //&url=%s";
	//private static final String TWITTER_COMPLETE_URL = "https://twitter.com/intent/tweet/complete";
		
	private static final int TWITTER_LIMIT = 140;
	private static final int TWITTER_LIMIT_WITH_IMAGE = 117;
	
	private static final int DISPLAY_HANDLE_LIMIT = 40;
	
	private static final int REQUEST_SHARE_TWITTER = 0;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private EditText mInputShareText = null;
	
	private View mTweetButton = null;
	private TextView mCancelTitle = null;
	private TextView mInputTextCount = null;
	
	/** Sometimes null */
	private ImageView mImageView = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Default is <code>null</code> */
	private ImageItem mImageItem = null;
	private Uri mImageUri = null;
	
	private int mColorRed = 0;
	private int mColorGray = 0;
	/** Set in {@link #shareContent()} */
	private StatusUpdate mStatusUpdate = null;
	
	private ProgressDialog mSendingDialog = null;
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mCancelTitle != null) {
			outState.putString(KEY_TWITTER_HANDLE, mCancelTitle.getText().toString());
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_twitter_share);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.ibs_twitter_share_toolbar);
	    setSupportActionBar(toolbar);
		
		View rootView = findViewById(android.R.id.content); //root for all content
			
		
		Bundle extras = getIntent().getExtras();	
		final String text = extras.getString(EXTRA_TEXT);
		
		if (extras.containsKey(EXTRA_IMAGE_ITEM) && extras.containsKey(EXTRA_IMAGE_URI)) {
			mImageItem = extras.getParcelable(EXTRA_IMAGE_ITEM);
			
			mImageView = (ImageView) findViewById(R.id.ibs_twitter_image_preview);
			mImageView.setVisibility(View.VISIBLE);
			mImageView.setContentDescription(mImageItem.getName());
			ImageLoader	.getInstance()
						.displayImage(mImageItem.getUrl(), mImageView);
			
			mImageUri = Uri.parse(extras.getString(EXTRA_IMAGE_URI));
		}
		
		mInputTextCount = (TextView) findViewById(R.id.ibs_twitter_share_text_characterRemaining);
		mInputShareText = (EditText) findViewById(R.id.ibs_twitter_input_content_body);
		mInputShareText.addTextChangedListener(this);
		
		mTweetButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_twitter_button_share, this);
		mCancelTitle = (TextView) Utility.setOnClickAndReturnView(rootView, R.id.ibs_twitter_text_cancel, this);
				
		mColorRed = getResources().getColor(R.color.ibs_red);
		mColorGray = getResources().getColor(R.color.ibs_gray);
		
		if (savedInstanceState == null) { //only on first run
			mInputShareText.setText(text);
			checkTextLengthAndSetView(text);
			
			Intent i = new Intent(this, TwitterLoginActivity.class);
			startActivityForResult(i, 0);
		} else {
			checkTextLengthAndSetView(mInputShareText.getText());
			
			mCancelTitle.setText(savedInstanceState.getString(KEY_TWITTER_HANDLE));
			
			if (Utility.checkIfLoading(getSupportLoaderManager(), REQUEST_SHARE_TWITTER)) {
				showTweetingDialog(true);
				getSupportLoaderManager().initLoader(REQUEST_SHARE_TWITTER, null, mTwitterTweeterCallback);
			}
		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Utility.recycleBitmap(mImageView); //only recycle created bitmaps
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == TwitterLoginActivity.RESULT_OK) {
			Bundle extras = data.getExtras();
			final long userId = extras.getLong(TwitterLoginActivity.RESULT_EXTRA_USER_ID);
			final String screenName = extras.getString(TwitterLoginActivity.RESULT_EXTRA_SCREEN_NAME);
			
			Log.d(CLASS_NAME, 
					String.format("UserId: %d, screenName: %s", userId, screenName));
			String title = getString(R.string.ibs_title_twitter_shareHandle, screenName);
			if (title.length() > DISPLAY_HANDLE_LIMIT) {
				title = title.substring(0, DISPLAY_HANDLE_LIMIT) + "...";
			}
			mCancelTitle.setText(title);			
				
		} else if (requestCode == TwitterLoginActivity.RESULT_FAILED) {
			Utility.toastMessage(this, getString(R.string.ibs_error_cannotConnect));
		} else { //assume cancel
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helpers methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Shows or hides the sending dialog. */
	private void showTweetingDialog(boolean show) {
		if (mSendingDialog == null && show) {
			mSendingDialog = ProgressDialog.show(this, "", getString(R.string.ibs_text_tweeting));
		} else if (mSendingDialog != null && !show) {
			mSendingDialog.dismiss();
		}
	}
	
	/** Assumes necessary views are set. 
	 * Checks the button and sets the view's state; disabling/enabling buttons
	 * and updating counts.
	 */
	private void checkTextLengthAndSetView(CharSequence text) {
		final int LENGTH = text.toString().trim().length();
		final int remaining = (mImageItem == null ? TWITTER_LIMIT : TWITTER_LIMIT_WITH_IMAGE) - LENGTH; 
		if (LENGTH <= 0 || remaining < 0) {
			mInputTextCount.setTextColor(mColorRed);
			mTweetButton.setEnabled(false);
		} else {
			mTweetButton.setEnabled(true);
			mInputTextCount.setTextColor(mColorGray);
		}
		mInputTextCount.setText(String.valueOf(remaining));
	}
	
	/** Shares the given content, using the given code. */
	private void shareContent() {	
		final String text = mInputShareText.getText().toString().trim();
		String imageUrl = "none"; 
		
		StatusUpdate status = new StatusUpdate(text);
		
		if (mImageItem != null) {
			imageUrl = mImageItem.getUrl();
			File image = new File(mImageUri.getPath());
			status.setMedia(image);
		}
		Log.d(CLASS_NAME, String.format("Text: %s, image: %s",  text, imageUrl));
		
		mStatusUpdate = status;
		showTweetingDialog(true);
		getSupportLoaderManager().initLoader(REQUEST_SHARE_TWITTER, null, mTwitterTweeterCallback);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private LoaderCallbacks<Boolean> mTwitterTweeterCallback = new LoaderCallbacks<Boolean>() {
		@Override
		public void onLoaderReset(Loader<Boolean> arg0) {}
		
		@Override
		public void onLoadFinished(Loader<Boolean> arg0, Boolean result) {
			getSupportLoaderManager().destroyLoader(arg0.getId());
			
			showTweetingDialog(false);
			
			if (result != null && result) {
				setResult(RESULT_OK); //end successfully
				finish();
			} else {
				Utility.toastMessage(TwitterShareActivity.this, getString(R.string.ibs_error_cannotConnect));
			}
			
		}
		
		@Override
		public Loader<Boolean> onCreateLoader(int arg0, Bundle arg1) {
			Twitter twitter = TwitterLoginActivity.getTwitterInstance(TwitterShareActivity.this);
			return new TwitterTweeterLoader(TwitterShareActivity.this, twitter, mStatusUpdate);
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
		case R.id.ibs_twitter_button_share:			
			shareContent();			
			break;
			
		case R.id.ibs_twitter_text_cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
			
		default:
			break;
		}
	}	
}
