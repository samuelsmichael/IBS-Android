/*
 * Copyright 2014 Jason J.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.inductivebiblestudyapp.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

//Taken directly from an opensource project.
/** A utility to help pick images, process and copy images from the image picker.
 * @author Jason J.
 * @version 0.1.0-20140814
 */
public class ImagePickerUtil {

	/** Assists in launching picker. 
	 * @param activity The activity to launch from.
	 * @param requestId The requestId to listen for in 
	 * {@link Activity#onActivityResult}.
	 */
	public static void launchPicker(Activity activity, int requestId){
		/*Intent i = new Intent(Intent.ACTION_PICK, 
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);*/
		Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
		activity.startActivityForResult(i, requestId);
	}
	
	
	/** Gets the image file path from the camera/image data.
	 * May or may not throw an exception if used incorrectly.
	 * @param cameraData The data to process (such as that from onActivityResult())
	 * @param context The current context
	 * @return The path of the image.
	 */
	public static String getPathFromCameraData(Context context, 
			Intent cameraData){
		Uri imageUri = cameraData.getData();
		return getPath(context, imageUri);
	}
			
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Private helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Gets the file path from the uri.
	 * May or may not throw an exception if used incorrectly.
	 * @param cameraData The data to process (such as that from onActivityResult())
	 * @param context The current context
	 * @return The path of the image.
	 */
	public static String getPath(Context context, Uri uri){
		String[] filePathColumn = { MediaStore.Images.Media.DATA }; 
		Cursor cursor = context	.getContentResolver()
								.query(uri,filePathColumn, null, null, null); 
		cursor.moveToFirst();
		//TODO get path column by more stable means
		int columnIndex = cursor.getColumnIndex(filePathColumn[0]); 
		String path = cursor.getString(columnIndex); 
		cursor.close();		
		return path;
	}	
	
	
}