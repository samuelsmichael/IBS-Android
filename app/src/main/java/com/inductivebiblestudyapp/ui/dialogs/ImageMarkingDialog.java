package com.inductivebiblestudyapp.ui.dialogs;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.MarkingItem;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.ui.activities.share.ShareActivity;
import com.inductivebiblestudyapp.util.Utility;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Displays an image marking. 
 * 
 * @author Jason Jenkins
 * @version 0.2.1-20150810
 */
public class ImageMarkingDialog extends DialogFragment implements OnClickListener {
	final static private String CLASS_NAME = ImageMarkingDialog.class
			.getSimpleName();
	
	
	/** Argument key: String. The dialog title. */
	private static final String ARG_TITLE = CLASS_NAME + ".ARG_TITLE";	
	/** Argument key: String. The dialog content. */
	private static final String ARG_CONTENT_TEXT = CLASS_NAME + ".ARG_CONTENT_TEXT";
	/** Argument key: String. The share content stub. */
	private static final String ARG_CONTENT_STUB = CLASS_NAME + ".ARG_CONTENT_STUB";
	
	/** Argument key: {@link MarkingItem} Parcelable. */
	private static final String ARG_MARKING_ITEM = CLASS_NAME + ".ARG_MARKING_ITEM";
	

	/** The result given when the deleting was a success. */
	public static final int RESULT_DELETE_SUCCESS = 1;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
		
	/** 
	 * @param contentStub The content stub used in share requests
	 * @param markingItem The image marking item to show in dialog
	 * @param title The title to give the window. 
	 * @param contentText The content of the window
	 */
	public static ImageMarkingDialog newInstance(String contentStub, MarkingItem markingItem, 
			String title, String contentText) {
		ImageMarkingDialog simpleDialog = new ImageMarkingDialog(); 
		Bundle args = new Bundle();
				
		args.putParcelable(ARG_MARKING_ITEM, markingItem);
		args.putString(ARG_TITLE, title);
		args.putString(ARG_CONTENT_TEXT, contentText);
		args.putString(ARG_CONTENT_STUB, contentStub);
		
		simpleDialog.setArguments(args);
		return simpleDialog;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factory methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public ImageMarkingDialog() {}
	
	private View mDeleteButton = null;
	private View mShareButton = null;
	
	private ImageView mImageView = null;
	
	private MarkingItem mMarkingItem = null;
	
	private String mTitle = null;
	private String mContentText = null;
	private String mContentStub = null;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SimpleDarkDialog);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//inflate from activity to ensure the dialog is styled correctly.
		View rootView = View.inflate(getActivity(), R.layout.dialog_image_marking, null); 

		TextView mTitleView = (TextView) rootView.findViewById(R.id.ibs_dialog_imageMarking_text_title);
		TextView mContentView = (TextView) rootView.findViewById(R.id.ibs_dialog_imageMarking_text_body);
		
		mImageView = (ImageView) rootView.findViewById(R.id.ibs_dialog_imageMarking_img_preview); 
		
		Bundle args = getArguments() ;
				
		if (args != null){
			mTitle = args.getString(ARG_TITLE);
			mTitleView.setText(mTitle);
			
			mContentText = args.getString(ARG_CONTENT_TEXT);
			mContentView.setText(mContentText);
			
			mContentStub = args.getString(ARG_CONTENT_STUB);
			mMarkingItem = args.getParcelable(ARG_MARKING_ITEM);
			
			ImageLoader.getInstance().displayImage(mMarkingItem.getImageItem().getUrl(), mImageView);			
		} 
		
		mShareButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_dialog_imageMarking_button_share, this);
		mDeleteButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_dialog_imageMarking_button_delete, this);
				
		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mImageView != null) {
			Utility.recycleBitmap(mImageView);
		}
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Used to enabled and disable all text & button inputs for regular mode. */
	/*package*/ void enableInputs(boolean enabled) {
		if (mDeleteButton == null) {
			return;
		}
		mDeleteButton.setEnabled(enabled);
		mShareButton.setEnabled(enabled);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Implemented Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.ibs_dialog_imageMarking_button_share:
			ShareActivity.launchShare(getActivity(), mContentStub, mTitle, mContentText, mMarkingItem.getImageItem());
			dismiss();
			break;
		case R.id.ibs_dialog_imageMarking_button_delete:
			enableInputs(false);
			final String accessToken = new CurrentUser(getActivity()).getIBSAccessToken();
			RestClient.getInstance().getMarkingService().delete(accessToken, mMarkingItem.getId(), new Callback<UpdateResult>() {
				
				@Override
				public void success(UpdateResult arg0, Response arg1) {
					if (arg0 != null && arg0.isSuccessful()) {
						final Fragment target = getTargetFragment();	
						target.onActivityResult(getTargetRequestCode(), RESULT_DELETE_SUCCESS, null);
						enableInputs(true);
						Utility.toastMessage(getActivity(), getString(R.string.ibs_text_deleteSuccess_imageMarking));
						dismiss();
					} else {
						Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
						enableInputs(true);
					}
				}
				
				@Override
				public void failure(RetrofitError arg0) {
					Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
					enableInputs(true);
				}
			});
			break;		
		}		
	}	
	
}
