package com.inductivebiblestudyapp.ui.activities.share;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.plus.PlusShare;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.loaders.SimpleContentAsyncLoader;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.data.model.EmailConfirmResponse;
import com.inductivebiblestudyapp.data.model.IContentResponse;
import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.ui.actionwrappers.FooterViewActionWrapper;
import com.inductivebiblestudyapp.ui.dialogs.SimpleConfirmDialog;
import com.inductivebiblestudyapp.util.DialogStateHolder;
import com.inductivebiblestudyapp.util.Utility;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.AuthListener;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 
 * @author Jason Jenkins
 * @version 0.14.2-2015085
 */
public class ShareActivity extends AppCompatActivity implements OnClickListener,
	SimpleConfirmDialog.OnClickListener, LoaderManager.LoaderCallbacks<ContentResponse> {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = ShareActivity.class.getSimpleName();
	private static final String LOGTAG = CLASS_NAME;
	
	private static final String TYPE_SHARE_TEXT = "text/plain";
	private static final String TYPE_SHARE_IMAGE = "image/*";	

	
	//private static final String TWITTER_URL = "https://twitter.com/intent/tweet?text=%s";	
	/* The character limit of twitter to warn with. */
	//private static final int TWITTER_LIMIT = 140;
	
	private static final String KEY_DIALOG_STATE = CLASS_NAME + ".KEY_DIALOG_STATE";
	private static final String TAG_CONFIRM_DIALOG = CLASS_NAME + ".TAG_CONFIRM_DIALOG";
	
	/** String. Required. The content confirmation stub for the query. */
	public static final String ARG_SHARE_CONTENT_STUB = CLASS_NAME + ".ARG_SHARE_CONTENT_STUB";
	/** String. Required. The share title content. */
	public static final String ARG_SHARE_TITLE = CLASS_NAME + ".ARG_SHARE_TITLE";
	/** String. Required. The share text content. */
	public static final String ARG_SHARE_TEXT = CLASS_NAME + ".ARG_SHARE_TEXT";
	
	/** Parcelable. Optional. {@link ImageItem} to share. */
	public static final String ARG_SHARE_IMAGE = CLASS_NAME + ".ARG_SHARE_IMAGE";
	

	
	private static final int REQUEST_CONFIRM_FACEBOOK_SHARE = 12;
	private static final int REQUEST_CONFIRM_TWITTER_SHARE = 13;
	private static final int REQUEST_CONFIRM_LINKEDIN_SHARE = 14;
	private static final int REQUEST_CONFIRM_GOOGLE_PLUS_SHARE = 15;
	
	private static final int REQUEST_CONFIRM_EMAIL_SHARE = 16;
	
	/** The file to share cache directory. Used by */
	private static final File sShareCacheDir = 
			new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/cache/"+ShareActivity.class.getPackage() + "/share");
		
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Creates and launches share activity for text. */
	public static void launchShare(Context context, String contentStub, String title, String text){
		Intent shareIntent = new Intent(context, ShareActivity.class);
		shareIntent.putExtra(ShareActivity.ARG_SHARE_CONTENT_STUB, contentStub);
		shareIntent.putExtra(ShareActivity.ARG_SHARE_TITLE, title);
		shareIntent.putExtra(ShareActivity.ARG_SHARE_TEXT, text);
		context.startActivity(shareIntent);
	}
	
	/** Creates and launches share activity for image. */
	public static void launchShare(Context context, String contentStub, String title, String text, 
			ImageItem imageItem){
		Intent shareIntent = new Intent(context, ShareActivity.class);
		shareIntent.putExtra(ShareActivity.ARG_SHARE_CONTENT_STUB, contentStub);
		shareIntent.putExtra(ShareActivity.ARG_SHARE_TITLE, title);
		shareIntent.putExtra(ShareActivity.ARG_SHARE_TEXT, text);
		shareIntent.putExtra(ARG_SHARE_IMAGE, imageItem);
		context.startActivity(shareIntent);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End Launch Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private TextView mTitleText = null;
	private TextView mContentText = null;
	
	/** Usually <code>null</code> unless {@link #mImageItemShare} is not <code>null</code> */
	private ImageView mImageView = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End views
	////////////////////////////////////////////////////////////////////////////////////////////////

    private ShareDialog mShareDialog = null; 
    private CallbackManager mFacebookCallbackManager = null;
    
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End share objects
	////////////////////////////////////////////////////////////////////////////////////////////////
    	
	/** A map of: RequestCode -> PageRequest name.	 */
	private final SparseArray<String> mRequestCodeToPageRequestMap = new SparseArray<String>();
	// Using HashMaps as opposed to SpareArray because easier to store/restore in onSaveInstanceState

	/** Keep a local cache of them for when we need to use/reuse them.
	 * Key:  activity/loader request keys, Value: dialog state holder */
	private SparseArray<DialogStateHolder> mRequestCodeToDialogStateMap =  new SparseArray<DialogStateHolder>();
	
	/** The stub for either bible or study notes content confirmations. */ 
	private String mShareContentStub = "";
	
	/** The share image, sometimes null. */
	private ImageItem mImageItemShare = null;
	
	/** The default share link to use when sharing via facebook. */
	private String mShareLink = null;
	/** Default sharing image when sharing via facebook */
	private String mShareImageLink = null;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSparseParcelableArray(KEY_DIALOG_STATE, mRequestCodeToDialogStateMap);
		
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		
		View rootView = findViewById(android.R.id.content); //root for all content
		
		mTitleText = (TextView) rootView.findViewById(R.id.ibs_share_title_note);
		mContentText = (TextView) rootView.findViewById(R.id.ibs_share_text_content);
		
		mShareLink = getString(R.string.ibs_share_url_fb);
		mShareImageLink = getString(R.string.ibs_share_icon_url);
		
		Utility.setOnClickAndReturnView(rootView, R.id.ibs_social_button_email, this);
		Utility.setOnClickAndReturnView(rootView, R.id.ibs_social_button_facebook, this);
		Utility.setOnClickAndReturnView(rootView, R.id.ibs_social_button_google_plus, this);
		Utility.setOnClickAndReturnView(rootView, R.id.ibs_social_button_linkedin, this);
		Utility.setOnClickAndReturnView(rootView, R.id.ibs_social_button_twitter, this);
		
		Bundle extras = getIntent().getExtras();
		
		mShareContentStub = extras.getString(ARG_SHARE_CONTENT_STUB);
		mTitleText.setText(extras.getString(ARG_SHARE_TITLE, ""));
		mContentText.setText(extras.getString(ARG_SHARE_TEXT, ""));
		
		if (extras.containsKey(ARG_SHARE_IMAGE)) {
			mImageItemShare = extras.getParcelable(ARG_SHARE_IMAGE);
			mImageView = (ImageView) rootView.findViewById(R.id.ibs_share_image_preview);
			mImageView.setVisibility(View.VISIBLE);
			ImageLoader	.getInstance()
						.displayImage(mImageItemShare.getUrl(), mImageView);
		}
		
		populateMaps();
		
		if (savedInstanceState != null) {
			mRequestCodeToDialogStateMap = savedInstanceState.getSparseParcelableArray(KEY_DIALOG_STATE);
		} 		
		
		FooterViewActionWrapper.newInstance(this, rootView);	
		

		FacebookSdk.sdkInitialize(getApplicationContext());
		mFacebookCallbackManager = CallbackManager.Factory.create();
		mShareDialog = new ShareDialog(this);
	    mShareDialog.registerCallback(mFacebookCallbackManager, new FacebookCallback<Sharer.Result>() {

	        @Override
	        public void onSuccess(Sharer.Result result) {
	        	loadAndShowConfirmDialog(REQUEST_CONFIRM_FACEBOOK_SHARE);
	            Log.d(LOGTAG, "success");
	        }

	        @Override
	        public void onError(FacebookException error) {
	            Log.d(LOGTAG, "error");
	        }

	        @Override
	        public void onCancel() {
	            Log.d(LOGTAG, "cancel");
	        }
	    });
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Utility.recycleBitmap(mImageView); //only recycle created bitmaps
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.share, menu);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(LOGTAG, String.format("Request: %d, Result: %d, Data: %s", requestCode, resultCode, ""+data));
		
		super.onActivityResult(requestCode, resultCode, data);
		mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
		LISessionManager.getInstance(getApplicationContext())
			.onActivityResult(this, requestCode, resultCode, data);
		
		Bundle extras = null;
		if (data != null) {
			extras = data.getExtras();
		}
		
		switch (requestCode) {
		case REQUEST_CONFIRM_EMAIL_SHARE:
			if (extras != null) {
				EmailConfirmResponse confirm = extras.getParcelable(EmailShareActivity.RESULT_CONFIRM_RESPONSE);
				getAndSetConfirmMessage(REQUEST_CONFIRM_EMAIL_SHARE, confirm);
				loadAndShowConfirmDialog(REQUEST_CONFIRM_EMAIL_SHARE);
			}
		case REQUEST_CONFIRM_TWITTER_SHARE:
		case REQUEST_CONFIRM_GOOGLE_PLUS_SHARE:
			clearShareCache();
		case REQUEST_CONFIRM_LINKEDIN_SHARE:
			if (Activity.RESULT_OK == resultCode) {
				loadAndShowConfirmDialog(requestCode);				
			} else {
				//Note: Activity.RESULT_CANCELED is 0
				Log.d(LOGTAG, "Not ok: " + resultCode);
			}
			break;
		default:
			break;
		}
	}


	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void populateMaps() {
		mRequestCodeToPageRequestMap.put(REQUEST_CONFIRM_EMAIL_SHARE, 
				mShareContentStub + getString(R.string.ibs_config_load_shareConfirmEmail));
		
		mRequestCodeToPageRequestMap.put(REQUEST_CONFIRM_FACEBOOK_SHARE, 
				mShareContentStub + getString(R.string.ibs_config_load_shareConfirmFacebook));
		mRequestCodeToPageRequestMap.put(REQUEST_CONFIRM_GOOGLE_PLUS_SHARE, 
				mShareContentStub + getString(R.string.ibs_config_load_shareConfirmGooglePlus));
		mRequestCodeToPageRequestMap.put(REQUEST_CONFIRM_LINKEDIN_SHARE, 
				mShareContentStub + getString(R.string.ibs_config_load_shareConfirmLinkedIn));
		mRequestCodeToPageRequestMap.put(REQUEST_CONFIRM_TWITTER_SHARE, 
				mShareContentStub + getString(R.string.ibs_config_load_shareConfirmTwitter));
		
		mRequestCodeToDialogStateMap.put(REQUEST_CONFIRM_EMAIL_SHARE, new DialogStateHolder());
		
		mRequestCodeToDialogStateMap.put(REQUEST_CONFIRM_FACEBOOK_SHARE, new DialogStateHolder());
		mRequestCodeToDialogStateMap.put(REQUEST_CONFIRM_GOOGLE_PLUS_SHARE, new DialogStateHolder());
		mRequestCodeToDialogStateMap.put(REQUEST_CONFIRM_LINKEDIN_SHARE, new DialogStateHolder());
		mRequestCodeToDialogStateMap.put(REQUEST_CONFIRM_TWITTER_SHARE, new DialogStateHolder());
	}
	
	private static String generateTag(int id) {
		return TAG_CONFIRM_DIALOG + id;
	}
	
	/** Attempts to find and set the dialog message, while caching it on this fragment.
	 * @param requestId The dialog  */
	private void getAndSetConfirmMessage(int id, IContentResponse data) {
			
		DialogStateHolder dialogState = mRequestCodeToDialogStateMap.get(id);
		
		String title = getString(R.string.ibs_error_sorry);
		String message = getString(R.string.ibs_error_cannotLoadContent);
		if (data != null) {
			message = data.getContent();
			title = data.getTitle();
			dialogState.fetched = true;
		} 

		dialogState.title = title;
		dialogState.message = message;
		
		Fragment frag = getSupportFragmentManager().findFragmentByTag(generateTag(id));
		if (frag != null && frag instanceof SimpleConfirmDialog) {
			((SimpleConfirmDialog) frag).updateContent(title, message);
		}		
	}
	
	/** Loads and shows confirmation dialog based on request code. 
	 * See {@link #populateMaps()}*/
	private void loadAndShowConfirmDialog(int requestCode) {
		DialogStateHolder holder = mRequestCodeToDialogStateMap.get(requestCode);				
		SimpleConfirmDialog.newInstance(holder.title, holder.message)
			.show(getSupportFragmentManager(), generateTag(requestCode));
	}
	
	private void preloadContent(int id) {
		if (!mRequestCodeToDialogStateMap.get(id).fetched) {
			getSupportLoaderManager().initLoader(id, null, this);
		}
	}
	
	/** Encodes given string. */
	public static String urlEncode(String string) {
	    try {
	        return URLEncoder.encode(string, "UTF-8");
	    }
	    catch (UnsupportedEncodingException e) {
	        Log.e(LOGTAG, "UTF-8 should always be supported", e);
	        throw new RuntimeException("URLEncoder.encode() failed for " + string);
	    }
	}


	/** Takes the two text views and creates a single share message */
	private String getShareMessage() {
		final String subject =  getTitleText();
		final String body = mContentText.getText().toString();
		
		String message = subject + " - " + body;
		return message;
	}
	/** Returns the title text from the title TextView */
	private String getTitleText() {
		return mTitleText.getText().toString();
	}
	
	/** Launches the google play install for a given id. If Google play not found, launches
	 * web link.
	 * @param packageName The package name given such as "com.google.android.apps.plus" in 
	 * https://play.google.com/store/apps/details?id=com.google.android.apps.plus&hl=en*/
	private void launchGooglePlay(final String packageName) {
		try {
			Intent getPlus = 
					new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
			startActivityForResult(getPlus, 0);
			
		} catch (ActivityNotFoundException e1) { //Play not installed?
			Intent getPlus = 
					new Intent(Intent.ACTION_VIEW, 
							Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
			startActivityForResult(getPlus, 0);
		}
	}
	
	
	/** Authorizes linkedin & hides view if necessary */
	private void authLinkedIn() {
		final com.linkedin.platform.utils.Scope scope = com.linkedin.platform.utils.Scope.build(
        		com.linkedin.platform.utils.Scope.R_BASICPROFILE, 
        		com.linkedin.platform.utils.Scope.W_SHARE);
	
		LISessionManager.getInstance(getApplicationContext())
		.init(this, scope, mLinkedinAuthListener, true);
		        	
/*	    	//first use token to connect
	    	LISessionManager.getInstance(getApplicationContext())
	    		.init(com.linkedin.platform.AccessToken.buildAccessToken(mLinkedInAccessToken));*/
	}
	
	/** Returns the image view's bitmap or <code>null</code> */
	private Bitmap getImageViewBitmap() {
		Bitmap image = null;		
		if (mImageView != null && mImageView.getDrawable() != null) {
			image = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
		}
		return image;
	}

	
	/** Finds and deletes the temporary image from google plus */
	private void clearShareCache() {
		if (sShareCacheDir.exists() && sShareCacheDir.isDirectory()) {
			String[] children = sShareCacheDir.list();
	        for (int i = 0; i < children.length; i++) {
	            new File(sShareCacheDir, children[i]).delete();
	        }
		}
	}
	
	/** Copies the image bitmap to cache and returns the uri, or null on failure. 
	 * @param useBitmap <code>true</code> to use the image view bitmap, false to copy from file.
	 * */
	private Uri copyImageToCache(boolean useBitmap) {
		clearShareCache(); //delete all contents first, always
		sShareCacheDir.mkdirs();
		File dst = new File(sShareCacheDir, "attachment"+mImageItemShare.hashCode()+".png");
		
		try {
			dst.createNewFile();
			
			if (useBitmap) { //write file directly
				Bitmap bitmap = getImageViewBitmap();
				
				if (bitmap == null && useBitmap){
					return null;
				}
				
			    OutputStream out = new FileOutputStream(dst);
			    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			    out.close();
			} else {
				File src = ImageLoader.getInstance().getDiskCache().get(mImageItemShare.getUrl());				
				
				InputStream in = new FileInputStream(src);
			    OutputStream out = new FileOutputStream(dst);			    

			    // Transfer bytes from in to out
			    byte[] buf = new byte[1024];
			    int len;
			    while ((len = in.read(buf)) > 0) {
			        out.write(buf, 0, len);
			    }
			    out.close();
			    in.close();
			}
		} catch (IOException e) {
			Log.e(CLASS_NAME, "Copying file went wrong");
			e.printStackTrace();
		}
		
		return Uri.fromFile(dst);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Share methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Launches share intent for result */
	private void shareByEmail() {
		String message = getShareMessage();
		
		Intent emailShare = new Intent(this, EmailShareActivity.class);
		emailShare.putExtra(EmailShareActivity.EXTRA_TEXT, message);
		
		if (mImageItemShare != null) {
			emailShare.putExtra(EmailShareActivity.EXTRA_IMAGE_ITEM, mImageItemShare);
		}
		
		startActivityForResult(emailShare, REQUEST_CONFIRM_EMAIL_SHARE);
		
		/* 
		 * How to share with regular email, note: does not allow for confirmation message
		 * 
		 * final String subject =  mTitleText.getText().toString();
		final String body = mContentText.getText().toString();
		
		Intent share = new Intent(Intent.ACTION_VIEW);
		Uri data = Uri.parse("mailto:?subject="+ subject +"&body=" + body);
		share.setData(data);
		
		Intent picker = Intent.createChooser(share, getString(R.string.ibs_title_pickEmailShare));
		
		startActivityForResult(picker, REQUEST_CONFIRM_EMAIL_SHARE);*/
	}
	
	/** Shares either by twitter app or twitter website. */
	private void shareByGooglePlus() {		
		String message = getShareMessage();
		
		PlusShare.Builder builder = new PlusShare.Builder(this)
			.setText(message);
		
		if (mImageItemShare != null) {
			
		    String mime = TYPE_SHARE_IMAGE;
		    
		    Uri pictureUri = copyImageToCache(false);
		    if (pictureUri == null) {
		    	return;
		    }
			builder	.setStream(pictureUri) 
					.setType(mime);     		
		} else {
			builder.setType(TYPE_SHARE_TEXT);			
		}
		
		Intent share = builder.getIntent();
		
		try {
			startActivityForResult(share, REQUEST_CONFIRM_GOOGLE_PLUS_SHARE);
		} catch (ActivityNotFoundException e) { //Google plus not installed
			Log.w(LOGTAG, "Google+ not found");
			final String googlePlusId = "com.google.android.apps.plus";
			launchGooglePlay(googlePlusId);
		}
		
	}


	

	/** Shares either by LinkedIn app or twitter website. */
	private void shareByLinkedIn() {
		
		authLinkedIn();
		
		
		/*//To share with the regular intent
		if (true) return;
		
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType(TYPE_SHARE_TEXT);
		share.putExtra(Intent.EXTRA_TEXT, message);

		// Narrow down to official Twitter app, if available:
		List<ResolveInfo> matches = getPackageManager().queryIntentActivities(share, 0);
		boolean linkedInFound = false;
		for (ResolveInfo info : matches) {
		    if (info.activityInfo.packageName.toLowerCase(Locale.US).startsWith("com.linkedin.")) {
		        share.setPackage(info.activityInfo.packageName);
		        linkedInFound = true;
		    }
		}
		
		if (linkedInFound) {
			startActivityForResult(share, REQUEST_CONFIRM_LINKEDIN_SHARE);
		} else {
			launchGooglePlay("com.linkedin.android");
		}*/

	}
	
	/** Shares either by twitter app or twitter website. */
	private void shareByTwitter() {
		String message = getShareMessage();
		
		
		Intent intent = new Intent(this, TwitterShareActivity.class);
		intent.putExtra(TwitterShareActivity.EXTRA_TEXT, message);
		
		if (mImageItemShare != null) {
			Uri pictureUri = copyImageToCache(false);
			if (pictureUri != null) {
				intent.putExtra(TwitterShareActivity.EXTRA_IMAGE_ITEM, mImageItemShare);
				intent.putExtra(TwitterShareActivity.EXTRA_IMAGE_URI, pictureUri.toString());
			}
		}
		
		startActivityForResult(intent, REQUEST_CONFIRM_TWITTER_SHARE);
		
		/*
		 * This way does not return RESULT_OK, using web view instead.
		 * 
		String tweetUrl = String.format("https://twitter.com/intent/tweet?text=%s", urlEncode(message)); 
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));

		// Narrow down to official Twitter app, if available:
		List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
		for (ResolveInfo info : matches) {
		    if (info.activityInfo.packageName.toLowerCase(Locale.US).contains(".twitter")) {
		        intent.setPackage(info.activityInfo.packageName);
		    }
		}
		intent.putExtra(TwitterShareActivity.EXTRA_TEXT, message);
		startActivityForResult(intent, REQUEST_CONFIRM_TWITTER_SHARE);  */
	}
	
	/** Attempts to launch share intent for result for facebook. */
	private void shareByFacebook() {
		String title = getTitleText();
		String message = getShareMessage();
		
		/*
		 * For links see: 
		 *  -ShareLinkContent;
		 *  -https://developers.facebook.com/docs/sharing/android
		 *  
		 * For plain text see:
		 *  -https://developers.facebook.com/docs/sharing/opengraph/android
		 *  
		 *  https://developers.facebook.com/docs/apps/review/prefill
		 */
		
	    /*final AccessToken accessToken = AccessToken.getCurrentAccessToken();
	    if (accessToken == null) {
	    	//request permissions
	    	//https://developers.facebook.com/docs/facebook-login/permissions/v2.4#checking
	    	//TO-DO abstract permissions to resources
		    final Collection<String> permissions =  Arrays.asList("publish_actions");
		    
		    LoginManager.getInstance().registerCallback( CallbackManager.Factory.create(), new FacebookCallback<LoginResult>() {
				
				@Override
				public void onSuccess(LoginResult result) {				
					shareByFacebook();
				}
				
				@Override
				public void onError(FacebookException error) {
					
				}
				
				@Override
				public void onCancel() {
					
				}
			});
		    LoginManager.getInstance().logInWithPublishPermissions(this, permissions);
	    } else {
	    	// This works!
	    	FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
	    	GraphRequest request = GraphRequest.newPostRequest(accessToken, "me/feed", null, new GraphRequest.Callback() {
				
				@Override
				public void onCompleted(GraphResponse response) {
					Utility.toastMessage(ShareActivity.this, "Success");
				}
			});
	    	Bundle parameters = new Bundle();
	    	parameters.putString("message", message);
	    	request.setParameters(parameters);
	    	request.executeAsync();
	    }*/
		
		if (mImageItemShare != null && ShareDialog.canShow(SharePhotoContent.class)) {
			Bitmap image = getImageViewBitmap();
			
			if (image != null) {
				SharePhoto photo = new SharePhoto.Builder()
		        	.setBitmap(image) 
		        	.build();
				
				Utility.copyToClipboard(getApplicationContext(), "note", message);
		    	Utility.toastMessage(this, getString(R.string.ibs_text_share_copiedToClipboard));
		    	SharePhotoContent photoContent = new SharePhotoContent.Builder()
		    		.addPhoto(photo)
		    		.build();
		    	
		    	mShareDialog.show(photoContent);
			}
		} else if (ShareDialog.canShow(ShareLinkContent.class)) {
	    	Utility.copyToClipboard(getApplicationContext(), "note", message);
	    	Utility.toastMessage(this, getString(R.string.ibs_text_share_copiedToClipboard));
	    	
	        ShareLinkContent linkContent = new ShareLinkContent.Builder()
	              	.setContentDescription(message)
	              	.setContentTitle(title)
	              	.setContentUrl(Uri.parse(mShareLink))
	              	.setImageUrl(Uri.parse(mShareImageLink))
	               	.build();

	        mShareDialog.show(linkContent);
	    }
		
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	//Initial authorization
	private AuthListener mLinkedinAuthListener = new AuthListener() {
		 
		@Override
		public void onAuthSuccess() {
			Log.d(LOGTAG, "LinkedIn onAuthSuccess ");
			
			LISessionManager.getInstance(getApplicationContext())
							.getSession().getAccessToken().toString();	
			

			String message = getShareMessage();
			
			Intent shareLinkedin = new Intent(ShareActivity.this, LinkedInShareActivity.class);
			shareLinkedin.putExtra(LinkedInShareActivity.EXTRA_TEXT, message);
			
			if (mImageItemShare != null) {
				String title = getTitleText();
				shareLinkedin.putExtra(LinkedInShareActivity.EXTRA_IMAGE_TITLE, title);
				shareLinkedin.putExtra(LinkedInShareActivity.EXTRA_IMAGE_ITEM, mImageItemShare);
			}			
			
			startActivityForResult(shareLinkedin, REQUEST_CONFIRM_LINKEDIN_SHARE);
		}
		
		@Override
		public void onAuthError(LIAuthError error) {
			//prepare appropriate error
			
			Intent share = new Intent(Intent.ACTION_SEND);
			// Narrow down to official LinkedIn app, if available:
			List<ResolveInfo> matches = getPackageManager().queryIntentActivities(share, 0);
			boolean linkedInFound = false;
			for (ResolveInfo info : matches) {
			    if (info.activityInfo.packageName.toLowerCase(Locale.US).startsWith("com.linkedin.")) {
			        share.setPackage(info.activityInfo.packageName);
			        linkedInFound = true;
			    }
			}
			
			if (linkedInFound) {
				Utility.toastMessage(ShareActivity.this, getString(R.string.ibs_error_cannotConnect));
			} else {
				launchGooglePlay("com.linkedin.android");
			}
			
		}
	};
	
	@Override
	public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case REQUEST_CONFIRM_EMAIL_SHARE:
		case REQUEST_CONFIRM_FACEBOOK_SHARE:
		case REQUEST_CONFIRM_GOOGLE_PLUS_SHARE:
		case REQUEST_CONFIRM_LINKEDIN_SHARE:
		case REQUEST_CONFIRM_TWITTER_SHARE:
			return new SimpleContentAsyncLoader(this, mRequestCodeToPageRequestMap.get(id));
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
		 case REQUEST_CONFIRM_EMAIL_SHARE:
		 case REQUEST_CONFIRM_FACEBOOK_SHARE:
		 case REQUEST_CONFIRM_GOOGLE_PLUS_SHARE:
		 case REQUEST_CONFIRM_TWITTER_SHARE:
		 case REQUEST_CONFIRM_LINKEDIN_SHARE:
			 getAndSetConfirmMessage(id, data);
		 }
		
	}
	 
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End Loaders
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start click listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onClick(DialogInterface dialog, int which, int requestCode) {
		switch (requestCode) {
		
		default:
			finish();
			break;
		}
			
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.ibs_social_button_email: {
			shareByEmail();			
			//email will provide confirmation message
		}			
			return;
			
		case R.id.ibs_social_button_facebook: {
			shareByFacebook();
			preloadContent(REQUEST_CONFIRM_FACEBOOK_SHARE);
		}
			break;
			
		case R.id.ibs_social_button_google_plus: {
			shareByGooglePlus();
			preloadContent(REQUEST_CONFIRM_GOOGLE_PLUS_SHARE);
		}
			
			break;
		case R.id.ibs_social_button_linkedin: {
			shareByLinkedIn();
			preloadContent(REQUEST_CONFIRM_LINKEDIN_SHARE);
		}
			
			break;
		case R.id.ibs_social_button_twitter: {
			shareByTwitter();
			preloadContent(REQUEST_CONFIRM_TWITTER_SHARE);
		}
			
			break;
			
		}
		
	}
}
