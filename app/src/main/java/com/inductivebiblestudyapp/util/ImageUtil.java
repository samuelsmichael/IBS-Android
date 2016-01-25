package com.inductivebiblestudyapp.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

/**
 * Utility class for fetching images & parsing base64 images.
 * @author Jason Jenkins
 * @version 0.3.1-20150810
 */
public class ImageUtil {	
	final static private String CLASS_NAME = ImageUtil.class.getSimpleName();
	
	public static final String IMG_USER_PROFILE = "profile_picture.png";
	
	public static final String BASE64_ENCODING = "UTF-8";
	public static final int BASE64_FLAG = Base64.NO_WRAP;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Scales the bitmap to max width/height.
	 * @param bitmap
	 * @param maxWidth in pixels.
	 * @param maxHeight in pixels.
	 * @return The bitmap scaled or <code>null</code> if input is null.
	 */
	public static Bitmap scaleBitmap(Bitmap bitmap, int max) {
		if (bitmap == null) {
			return null;
		}
		int dstWidth = bitmap.getWidth();
		int dstHeight = bitmap.getHeight();
		
		
		if (dstHeight >= dstWidth) { //scale by height
			dstWidth *= (float)max/(float)dstHeight;
			dstHeight = max;
		} else { //scale by width
			dstHeight *= (float)max/(float)dstWidth;
			dstWidth = max;
		}
		
		return Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false);
	}
	
	/**
	 * Attempts to read a Bitmap from path in smallest way.
	 * @param context The application context.
	 * @param path The path of the file
	 * @return The bitmap if successful, <code>null</code> on failure.
	 */
	public static Bitmap readBitmapFromPath(Context context, String path, int size) {
		return readBitmapFromPath(path, size);
	}
	
	/**
	 * Attempts to read a Bitmap from cache. Initially tries external, if that fails it tries
	 * internal. Uses the value {@value #IMG_USER_PROFILE} for the file name.
	 * @param context The application context.
	 * @return The bitmap if successful, <code>null</code> on failure.
	 */
	public static Bitmap readBitmapFromCache(Context context, int size) {
		File picture = new File(context.getExternalCacheDir(), IMG_USER_PROFILE);
		if (!picture.exists()) { //does an image exist in external cache
			picture = new File(context.getCacheDir(), IMG_USER_PROFILE);
			
			if (!picture.exists()) {
				return null;
			}
		}
		
		return readBitmapFromPath(picture.getPath(), size);
	}

	/**
	 * Attempts to write the Bitmap to cache. Initially tries external, if that fails it tries
	 * internal. Uses the value {@value #IMG_USER_PROFILE} for the file name.
	 * @param context The application context.
	 * @param bitmap The image to save.
	 * @return <code>true</code> if it successfully wrote, <code>false</code> otherwise.
	 */
	public static boolean writeBitmapToCache(Context context, Bitmap bitmap) {
		File cache = context.getExternalCacheDir();
		if (cache == null) {
			cache = context.getCacheDir();
		}
		File picture = new File(cache, IMG_USER_PROFILE);
		FileOutputStream output = null;
		
		boolean success = false;
		try {
			output = new FileOutputStream(picture);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
			success = true;
			
		} catch (FileNotFoundException e) {
			Log.e(CLASS_NAME, "Image did not save correctly: " + e);
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {}
			}
		}
		return success;		
	}
	
	/**
	 * Attempts to read a Bitmap from cache. Initially tries external, if that fails it tries
	 * internal. Uses the value {@value #IMG_USER_PROFILE} for the file name.
	 * @param context The application context.
	 * @return The bitmap if successful, <code>null</code> on failure.
	 */
	public static Bitmap readBitmapFromCache(Context context) {
		File picture = new File(context.getExternalCacheDir(), IMG_USER_PROFILE);
		if (!picture.exists()) { //does an image exist in external cache
			picture = new File(context.getCacheDir(), IMG_USER_PROFILE);
			
			if (!picture.exists()) {
				return null;
			}
		}
		
		return readBitmapFromFile(picture);
	}
	
	/**
	 * Network thread action. Fetches the image from the given url and saves to cache.
	 * @param url The url to fetch an image from.
	 * @return The bitmap of the image
	 * @throws MalformedURLException If the url is poorly formed
	 * @throws IOException If something goes wrong during connection (e.g. cannot connect)
	 */
	public static Bitmap networkFetch(String url) throws MalformedURLException, IOException {
		return BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
	}
	
	/**
	 * Network thread action. Fetches the image from the given url and saves to cache.
	 * @param url The url to fetch an image from.
	 * @param size The size to resample the image to.
	 * @return The bitmap of the image
	 * @throws MalformedURLException If the url is poorly formed
	 * @throws IOException If something goes wrong during connection (e.g. cannot connect)
	 */
	public static Bitmap networkFetch(String url, final int size) throws MalformedURLException, IOException {
		BitmapFactory.Options opts = reduceBitmapSize(size);

		Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

        return scaleBitmap(bitmap, size);
	}

	
	
	/**
	 * Converts a bitmap to base 64 bytes. Compresses via PNG.
	 * @param bitmap
	 * @return byte array representing image
	 */
	public static byte[] bitmapToBase64Bytes(Bitmap bitmap) {
		System.gc();
		ByteArrayOutputStream output = new ByteArrayOutputStream(bitmap.getByteCount());		
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
		return output.toByteArray();
	}
	
	/**
	 * Converts a bitmap to base64 string. Uses mode {@link BASE64_FLAG}
	 * @param bitmap
	 * @return Base64 string representing the image.
	 */
	public static String bitmapToBase64(Bitmap bitmap) {
		 try {
			byte[] bytes = bitmapToBase64Bytes(bitmap);
			return new String(Base64.encode(bytes, 0, bytes.length, BASE64_FLAG), BASE64_ENCODING);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
	 * Converts base64 String to a bitmap.  {@link BASE64_FLAG}
	 * @param base64
	 * @return The bitmap to display.
	 */
	public static Bitmap base64ToBitmap(String base64) {
		byte[] bytes = Base64.decode(base64, BASE64_FLAG );
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	// see: http://stackoverflow.com/questions/14322482/phonegap-picture-base64-android
	// http://stackoverflow.com/questions/4830711/how-to-convert-a-image-into-base64-string
	private static Bitmap readBitmapFromPath(String path, final int size) {

        //Decode image size
        BitmapFactory.Options opts = reduceBitmapSize(size);

        BitmapFactory.decodeFile(path, opts);

        return BitmapFactory.decodeFile(path, opts);
	}
	
	/**
	 * Reduces bitmaps file size.
	 * @param size
	 * @return
	 */
	private static BitmapFactory.Options reduceBitmapSize(final int size) {
		//Decode image size
        BitmapFactory.Options opts = new BitmapFactory.Options();
        //opts.inJustDecodeBounds = true;
        opts.inDither = false;                     //Disable Dithering mode
        opts.inPurgeable = true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
        opts.inInputShareable = true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
        opts.inTempStorage = new byte[1024]; 

        //Find the correct scale value. It should be the power of 2.
        int scale=1;
        while(opts.outWidth/scale/2 >= size && opts.outHeight/scale/2 >= size) {
            scale*=2;
        }

        //Decode with inSampleSize
         opts.inSampleSize=scale;
         
		return opts;
	}
	
	
	private static Bitmap readBitmapFromFile(File picture) {
		InputStream input = null;
		Bitmap result = null;
		try {
			input = new FileInputStream(picture);			
			result = BitmapFactory.decodeStream(input);
					
		} catch (FileNotFoundException e) {
			Log.e(CLASS_NAME, "Image did not read correctly: " + e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {}
			}
		}
		return result;
	}
}
