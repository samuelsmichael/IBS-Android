package com.inductivebiblestudyapp.data.loaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.data.service.ImageService;
import com.inductivebiblestudyapp.util.ImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Loader to update profile api using {@link AsyncTaskLoader}. 
 * <code>true</code> on success, <code>false</code> or <code>null</code> on failure.
 * @author Jason Jenkins
 * @version 0.5.1-20150810
 */
public class ImageUpdateAsyncLoader extends AbstractUpdateAsyncLoader2<ImageService> {
	final static private String CLASS_NAME = ImageUpdateAsyncLoader.class
			.getSimpleName();
	
	
	/** String. Required. */
	public static final String KEY_IMAGE_NAME = CLASS_NAME + ".KEY_IMAGE_NAME";
	/** String. Required. */
	public static final String KEY_FILE_EXTENSION = CLASS_NAME + ".KEY_FILE_EXTENSION";
	/** String. Required. */
	public static final String KEY_IMAGE_PATH = CLASS_NAME + ".KEY_IMAGE_PATH";
	
	/** String. Optional. */
	public static final String KEY_IMAGE_ID = CLASS_NAME + ".KEY_IMAGE_ID";
	
	private final Bundle mArgs;
	private int mImageSize;
			
	private String mBase64 = "";
	
	public ImageUpdateAsyncLoader(Context context, Bundle args) {
		super(context, RestClient.getInstance().getImageService());
		if ( !(	args.containsKey(KEY_IMAGE_NAME) && args.containsKey(KEY_FILE_EXTENSION) &&
				args.containsKey(KEY_IMAGE_PATH)) ) {
			throw new IllegalArgumentException("Bundle is missing one or more minimum requirments: " + args);
		}
		this.mArgs = args;		
		this.mImageSize = (int) context.getResources().getDimension(R.dimen.ibs_general_image_size);
	}
	
	

	@Override
	protected UpdateResult fetchResult() {
		UpdateResult result = null;
		String path = mArgs.getString(KEY_IMAGE_PATH);
		if (!path.contains("://")) {
			path = "file://" + path; //set to file path
		}
		
		Bitmap bitmapOriginal =  null;
		Bitmap bitmapFinal = null;
		
		int attempt = 0;
		boolean success = false;
		while (!success && attempt < 5) { //by 5 attempts we are 25% smaller than originally, so we give up.
			try {
				attempt++;
				
				if (mBase64 != null && mBase64.isEmpty()) { 
					//only get & resize image if the base64 has not been created yet.
					bitmapOriginal = ImageLoader.getInstance().loadImageSync(path);
					bitmapFinal = ImageUtil.scaleBitmap(bitmapOriginal, mImageSize);
				
					bitmapOriginal.recycle();
				
					mBase64 = ImageUtil.bitmapToBase64(bitmapFinal);
					bitmapFinal.recycle();
				}
			
				if (mArgs.containsKey(KEY_IMAGE_ID)) {
					result = mService.edit(
							mAccessToken, 
							mArgs.getString(KEY_IMAGE_ID),
							mArgs.getString(KEY_IMAGE_NAME), 
							mBase64, 
							mArgs.getString(KEY_FILE_EXTENSION));
				} else {
					result = mService.create(
							mAccessToken, 
							mArgs.getString(KEY_IMAGE_NAME), 
							mBase64, 
							mArgs.getString(KEY_FILE_EXTENSION));
				}
				success = true; //if we made it here, it was successful
		
			} catch (OutOfMemoryError e) { //we mostly expect out memory errors here
				Log.w(CLASS_NAME, "OOM - Resizing");
				mImageSize = (int) (mImageSize * 0.75f); //try reducing by 25% 
				mBase64 = "";

				recycleAndSleep(bitmapOriginal, bitmapFinal);
				
			} catch (Exception e) { //something else happened?
				Log.w(CLASS_NAME, "Exception occurred - Resizing", e);
				mImageSize = (int) (mImageSize * 0.75f); //try reducing by 25%
				mBase64 = "";
				
				recycleAndSleep(bitmapOriginal, bitmapFinal);				
			}
		}
		
		
		return result;
	}
	
	/** Recycles the two bitmaps then sleeps the thread so the garbage collector has more time
	 *  to clean. */
	private static void recycleAndSleep(Bitmap bitmapOriginal, Bitmap bitmapFinal) {
		if (bitmapOriginal != null) {
			bitmapOriginal.recycle();
		}
		if(bitmapFinal != null) {
			bitmapFinal.recycle();
		}
		try {
			Thread.sleep(RETRY_TIME); //briefly sleep to let garbage collector collect
		} catch (InterruptedException e) {}
	}	
}
