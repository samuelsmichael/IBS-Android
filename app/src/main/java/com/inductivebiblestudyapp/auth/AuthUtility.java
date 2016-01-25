package com.inductivebiblestudyapp.auth;

import java.io.IOException;
import java.net.MalformedURLException;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.util.Log;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.util.ImageUtil;

/** @version 0.1.1-20150810 */
public class AuthUtility {
	/**
	 * Loads, caches and converts image into base64.
	 * @param context
	 * @param network
	 * @param imageUrl Can be <code>null</code>
	 * @return base64 or <code>null</code>
	 */
	public static String getBase64ImageFromUrl(Context context, String network, String imageUrl) {
		String base64Image = null;
		
		Bitmap bitmap = null;
		if (imageUrl != null && !imageUrl.isEmpty()) {
			try {
				final int size = (int) context.getResources().getDimension(R.dimen.ibs_profile_image_size);								
				
				bitmap = ImageUtil.networkFetch(imageUrl, size);	
				if (bitmap != null) {
					base64Image = ImageUtil.bitmapToBase64(bitmap);
				}
				
			} catch (MalformedURLException e) {
				Log.e(network, "Twitter image not found: " + imageUrl);
			} catch (NotFoundException e) {
				Log.e(network, "Twitter image not found: " + imageUrl);
			} catch (IOException e) {
				Log.d(network, "Could not write file: " + imageUrl);
			} finally {
				if (bitmap != null) {
					bitmap.recycle();
				}
			}
		}
		return base64Image;
	}
}
