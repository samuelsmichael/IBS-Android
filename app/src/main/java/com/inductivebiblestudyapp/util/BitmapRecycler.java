package com.inductivebiblestudyapp.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

/**
 * A utility class aimed at recycling bitmaps efficiently but without causing exceptions
 * due to recycling bitmaps in use.
 * @author Jason Jenkins
 * @version 0.2.0-20150824 */
public class BitmapRecycler {
	
	private final List<BitmapWrapper> mRecyclingList = new ArrayList<BitmapRecycler.BitmapWrapper>();
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Adds a bitmap and increments the reference count. */
	public synchronized void addBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return;
		}
		final int index = mRecyclingList.indexOf(bitmap);
		if (index < 0) {
			mRecyclingList.add(new BitmapWrapper(bitmap));
		} else {
			mRecyclingList.get(index).count++; //increment
		}
	}
	
	/** Convenience method, same as calling {@link #addBitmap(Bitmap)} using {@link ImageView}'s bitmap.
	 * Note: This is context safe, it does not store the context only the bitmap. */
	public void addFromImageView(ImageView imageview) {
		if (imageview == null || imageview.getDrawable() == null) {
			return;
		}
		addBitmap(((BitmapDrawable) imageview.getDrawable()).getBitmap());
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End adding
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	/** Convenience method, same as calling {@link #recycle(Bitmap)} using {@link ImageView}'s bitmap.
	 * Note: This is context safe, it does not store the context only the bitmap.
	 * @param imageview The image view to check it's bitmap drawable for recycling.
	 * Can be <code>null</code>, as can its {@link ImageView#getDrawable()}
	 */
	public boolean recycle(ImageView imageview) {
		if (imageview == null || imageview.getDrawable() == null) {
			return false;
		}
		
		Bitmap bitmap = ((BitmapDrawable) imageview.getDrawable()).getBitmap();
		
		boolean success = recycle(bitmap);
		if (success) {
			imageview.setImageBitmap(null); //clear all references
		}
		return success;
	}
	
	/** Attempts to recycle bitmap, if still in use (as denoted by count) it does not
	 * and return <code>false</code>, if last reference to be recycled return <code>true</code>
	 * @param bitmap
	 * @return <code>true</code> if recycled, <code>false</code> if not.
	 */
	public synchronized boolean recycle(Bitmap bitmap) {
		final int index = mRecyclingList.indexOf(bitmap);
		if (index < 0) {
			return false; //not found
		} else {
			BitmapWrapper bWrapper = mRecyclingList.get(index);
			bWrapper.count--; //decrement
			if (0 > bWrapper.count) {
				return false;
			}
			if (!bitmap.isRecycled()) {
				bitmap.recycle();
			}
			bWrapper.mBitmap.clear();
			return mRecyclingList.remove(index) != null;
		}
	}
	
	/** Attempts to recycle the entire list, ignoring any counts.
	 * Only call when you are sure no bitmaps listed will be redrawn (e.g. onDestroy() )
	 */
	public synchronized void recycleAll() {
		for (BitmapWrapper item : mRecyclingList) {
			Bitmap bitmap = item.mBitmap.get();
			
			MemoryCache memoryCache = ImageLoader.getInstance().getMemoryCache();
			for (String key : memoryCache.keys()) {
			    if (bitmap == memoryCache.get(key)) {
			      continue; //skip
			    }
			}
			
			if (bitmap != null) {
				bitmap.recycle();
			}
			item.mBitmap.clear();
		}
		mRecyclingList.clear();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////

	private static class BitmapWrapper {
		final WeakReference<Bitmap> mBitmap;
		private int count = 0;
		public BitmapWrapper(Bitmap bitmap) {
			mBitmap = new WeakReference<Bitmap>(bitmap);
			count++;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof Bitmap && o != null) {
				return o.equals(mBitmap.get());
			}
			return super.equals(o);
		}
	}
}
