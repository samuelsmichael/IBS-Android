package com.inductivebiblestudyapp.ui.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.ui.actionwrappers.EditImageActionWrapper;
import com.inductivebiblestudyapp.util.BitmapRecycler;
import com.inductivebiblestudyapp.util.Utility;

/**
 * <p>There is a bug in the support library for dialog fragments:
 * <ul>
 * <li>http://stackoverflow.com/questions/14016043/dialogfragment-displayed-from-oncontextitemselected-doesnt-survive-onpause-onre</li>
 * <li>https://code.google.com/p/android/issues/detail?id=41901</li>
 * </ul>
 * 
 * In short, when launching an activity from a dialog or pausing, etc. the dialog disappears
 * from view (but is still present on rotation). This may or may not be limited to 
 * a dialog launched from from popup or another dialog, etc. 
 * </p>
 * <p>To deal with this bug, one can either add a delay or change ui elements.
 * As such, this dialog fragment has been converted into an {@link Activity} in the hopes 
 * that it will be a robust solution</p> 
 * 
 * Creates a faux dialog with a yes and no button for saving images.
 * 

 * Creates a 250dp wide dialog by default.
 * 
 * @author Jason Jenkins
 * @version 0.3.5-20150824
 */
public class CreateImageDialogActivity extends AppCompatActivity implements  
	EditImageActionWrapper.OnActionListener {
	
	final static private String CLASS_NAME = CreateImageDialogActivity.class
			.getSimpleName();
	
	private static final String KEY_IMAGE_PATH = CLASS_NAME + ".KEY_IMAGE_PATH";
	
	public static final String EXTRA_RESULT_IMAGE = CLASS_NAME + ".EXTRA_RESULT_IMAGE";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Creates new instance. Dialog only dismisses on cancel, must manually dismiss on positive. */
	public static CreateImageDialogActivity newInstance() {
		CreateImageDialogActivity imageDialog = new CreateImageDialogActivity();		
		return imageDialog;
	}
	
	public CreateImageDialogActivity() {}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End initializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private final BitmapRecycler mBitmapRecycler = new BitmapRecycler();
	
	private View mSubmitPositive = null;
	private View mSubmitNegative = null;
	
	private EditText mInputImageName = null;
	
	private ImageView mImagePreview = null;
	private View mProgressView = null; 
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private EditImageActionWrapper mImageActionWrapper = null;
	
	private String mImagePath = null;
	
	@Override
	public void onSaveInstanceState(Bundle saveState) {
		super.onSaveInstanceState(saveState);
		saveState.putString(KEY_IMAGE_PATH, mImagePath);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dialog_image_save);
	
		View rootView = findViewById(android.R.id.content);
				
		mInputImageName = (EditText) rootView.findViewById(R.id.ibs_dialog_input);
		mImagePreview = (ImageView) rootView.findViewById(R.id.ibs_dialog_image_preview);
		mProgressView = Utility.getProgressView(rootView);
		
		mSubmitPositive = findViewById(R.id.ibs_dialog_button_positive);
		mSubmitNegative = findViewById(R.id.ibs_dialog_button_negative);
		
		mImageActionWrapper = 
				new EditImageActionWrapper(this, rootView, mInputImageName, mSubmitPositive, mSubmitNegative, this);
		
		if (savedInstanceState != null) {
			mImagePath = savedInstanceState.getString(KEY_IMAGE_PATH);
			mImageActionWrapper.setImagePath(mImagePath);
		}

		mImageActionWrapper.loadImage();
		
		enableInputs(true);
				
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mImageActionWrapper != null) {
			mImageActionWrapper.destroy();
		}
		mBitmapRecycler.recycleAll();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mImageActionWrapper.onActivityResult(requestCode, resultCode, data);
	}

	
	/** Sets errors and enables the dialog inputs.
	 *  @param error Sets the input error. */
	public void setInputError(String error) {
		mInputImageName.setError(error);
		enableInputs(true);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Used to enabled and disable all text & button inputs for regular mode. */
	private void enableInputs(boolean enabled) {
		if (mInputImageName == null) {
			return;
		}
		mInputImageName.setEnabled(enabled);
		mSubmitPositive.setEnabled(enabled);
		mSubmitNegative.setEnabled(enabled);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Implemented Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	

	@Override
	public void onSave(ImageItem item) {
		Intent data = new Intent();
		data.putExtra(EXTRA_RESULT_IMAGE, item);
		setResult(RESULT_OK, data);
		finish();
	}

	@Override
	public void onCancel() {
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public void enableViews(boolean enabled) {
		enableInputs(enabled);
	}
	

	@Override
	@SuppressWarnings("deprecation")
	public void onNoImage() {
		if (mImagePreview != null) {
			//do not recycle resources
			mImagePreview.setVisibility(View.VISIBLE);
			mProgressView.setVisibility(View.GONE);
			mImagePreview.setImageDrawable(getResources().getDrawable(R.drawable.ic_no_image));
		}
	}

	/** Used in {@link #onImageLoadStart()} and {@link #onImageLoaded(String, Bitmap)} */
	private boolean mOnImageLoadStart = false;
	
	@Override
	public void onImageLoadStart() {
		if (mImagePreview != null) {
			mOnImageLoadStart = true;
			Utility.switchFadeProgressViews(mProgressView, mImagePreview, true);
		}
	}

	@Override
	public void onImageLoaded(String imageUri, Bitmap loadedImage) {
		mBitmapRecycler.addBitmap(loadedImage);
		
		if (mInputImageName != null && mImagePreview != null) {
			mBitmapRecycler.recycle(mImagePreview); //only recycle non-resource bitmaps
			
			if (mOnImageLoadStart) {
				Utility.switchFadeProgressViews(mProgressView, mImagePreview, false);
				mOnImageLoadStart = false;
			}
			
			String name = mInputImageName.getText().toString();
			mImagePreview.setImageBitmap(loadedImage);
			mImagePreview.setContentDescription(name);
			
			mImagePath = mImageActionWrapper.getImagePath();
		}
	}
	

}
