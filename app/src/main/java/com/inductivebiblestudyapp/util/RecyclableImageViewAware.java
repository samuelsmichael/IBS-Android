package com.inductivebiblestudyapp.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

/** Image aware view with bitmap recycle. Depends on Universal Image Loader library.
 * Set {@link BitmapRecycler} when possible.
 * @version 1.1.1-20150823 */
public class RecyclableImageViewAware extends ImageViewAware {
	final static private String LOGTAG = RecyclableImageViewAware.class
			.getSimpleName();
	
	final BitmapRecycler mRecycler;

	/**
	 * Constructor. <br />
	 * References {@link #RecyclableImageViewAware(android.widget.ImageView, boolean, BitmapRecycler) RecyclableImageViewAware(imageView, true, BitmapRecycler)}.
	 *
	 * @param imageView {@link android.widget.ImageView ImageView} to work with
	 */
	public RecyclableImageViewAware(ImageView imageView, BitmapRecycler recycler) {
		this(imageView, true, recycler);
	}
	

	/**
	 * Constructor
	 *
	 * @param imageView           {@link android.widget.ImageView ImageView} to work with
	 * @param checkActualViewSize <b>true</b> - then {@link #getWidth()} and {@link #getHeight()} will check actual
	 *                            size of ImageView. It can cause known issues like
	 *                            <a href="https://github.com/nostra13/Android-Universal-Image-Loader/issues/376">this</a>.
	 *                            But it helps to save memory because memory cache keeps bitmaps of actual (less in
	 *                            general) size.
	 *                            <p/>
	 *                            <b>false</b> - then {@link #getWidth()} and {@link #getHeight()} will <b>NOT</b>
	 *                            consider actual size of ImageView, just layout parameters. <br /> If you set 'false'
	 *                            it's recommended 'android:layout_width' and 'android:layout_height' (or
	 *                            'android:maxWidth' and 'android:maxHeight') are set with concrete values. It helps to
	 *                            save memory.
	 *                            <p/>
	 */
	public RecyclableImageViewAware(ImageView imageView, boolean checkActualViewSize, BitmapRecycler recycler) {
		super(imageView, checkActualViewSize);
		mRecycler = recycler;
	}
	
	@Override
	public boolean setImageDrawable(Drawable drawable) {
		
		if (mRecycler != null && drawable != null && drawable instanceof BitmapDrawable) {
			Bitmap bitmap =  ((BitmapDrawable) drawable).getBitmap();
			if (bitmap.isRecycled()) {
				return false; //cannot draw recycled bitmaps
			}
			
			mRecycler.addBitmap(bitmap);
		}
		try {
			return super.setImageDrawable(drawable);
		} catch (IllegalStateException e) {
			Log.w(LOGTAG, "Cannot set bitmap. Bitmap was recycled." , e);
		} catch (IllegalArgumentException e) {
			Log.w(LOGTAG, "Cannot set bitmap. Bitmap was recycled." , e);
		}
		return false;
	}
	
	@Override
	public boolean setImageBitmap(Bitmap bitmap) {
		if (bitmap != null & bitmap.isRecycled()) {
			return false; //cannot draw recycled bitmaps
		}
		
		if (mRecycler != null) {
			mRecycler.addBitmap(bitmap);
		}
		try {			
			return super.setImageBitmap(bitmap);
		} catch (IllegalStateException e) {
			Log.w(LOGTAG, "Cannot set bitmap. Bitmap was recycled." , e);
		} catch (IllegalArgumentException e) {
			Log.w(LOGTAG, "Cannot set bitmap. Bitmap was recycled." , e);
		}
		return false;	
	}
	
}
