package com.inductivebiblestudyapp.ui.fragments.profile.markings;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.loaders.SimpleContentAsyncLoader;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.data.model.LetteringItem;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.ui.actionwrappers.EditLetteringsActionWrapper;
import com.inductivebiblestudyapp.ui.actionwrappers.EditLetteringsActionWrapper.OnActionListener;
import com.inductivebiblestudyapp.ui.dialogs.SimpleYesNoDialog;
import com.inductivebiblestudyapp.ui.widget.ExpandableHeightGridView;
import com.inductivebiblestudyapp.util.DialogStateHolder;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A simple {@link Fragment} subclass. Use the {@link MarkingsEditLetteringFragment#newInstance}
 * factory method to create an instance of this fragment.
 * @version 0.4.1-20150814
 */
public class MarkingsEditLetteringFragment extends Fragment implements OnClickListener,
	LoaderManager.LoaderCallbacks<ContentResponse>, OnActionListener  {
	
	final static private String CLASS_NAME = MarkingsEditLetteringFragment.class
			.getSimpleName();
	private static final String TAG_REMOVE_DIALOG = CLASS_NAME + ".TAG_REMOVE_DIALOG";
	
	private static final String ARG_LETTERING_ITEM = CLASS_NAME + ".ARG_LETTERING_ITEM";
	

	private static final String KEY_DIALOG_STATE = CLASS_NAME + ".KEY_DIALOG_STATE";
	
	public static final int REQUEST_DELETE_DIALOG = 1;
	
	private static final int REQUEST_REMOVE_DIALOG_LOADER = 0;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End Constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 */
	public static MarkingsEditLetteringFragment newInstance() {
		MarkingsEditLetteringFragment fragment = new MarkingsEditLetteringFragment();
		return fragment;
	}
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * @param letteringItem The lettering item to start editing.
	 */
	public static MarkingsEditLetteringFragment newInstance(LetteringItem letteringItem) {
		MarkingsEditLetteringFragment fragment = new MarkingsEditLetteringFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_LETTERING_ITEM, letteringItem);
		fragment.setArguments(args);
		return fragment;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factory methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	public MarkingsEditLetteringFragment() {
		// Required empty public constructor
	}
	

	private DialogStateHolder mDialogState = new DialogStateHolder(); 

	private OnInteractionListener mListener =  null;
	
	private LetteringItem mLetteringItem = null;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putParcelable(KEY_DIALOG_STATE, mDialogState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View  rootView = inflater.inflate(R.layout.fragment_markings_edit_view_lettering, container, false);
		
		RecyclerView libraryView = 
				(RecyclerView) rootView.findViewById(R.id.ibs_markings_customMarkings_createMarking_recyclerView);
		ExpandableHeightGridView gridView = (ExpandableHeightGridView) rootView.findViewById(R.id.ibs_markings_customMarkings_gridview_buttons);
		
		gridView.setExpanded(true);
		
		EditText inputText = (EditText) rootView.findViewById(R.id.ibs_markings_customMarkings_input_name);
		
		View saveButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_markings_customMarkings_button_save, this);
		View deleteButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_markings_customMarkings_button_delete, this);
		

		//performs actions on behalf of fragment
		EditLetteringsActionWrapper actionWrapper = 
				new EditLetteringsActionWrapper(getActivity(), libraryView, gridView,  inputText, saveButton, deleteButton, this);
		
		Bundle args = getArguments();
		if (args != null) {
			mLetteringItem = args.getParcelable(ARG_LETTERING_ITEM);
			actionWrapper.setLetteringItem(mLetteringItem);
		}
		if (savedInstanceState != null) {
			mDialogState = savedInstanceState.getParcelable(KEY_DIALOG_STATE);
		}
		if (!mDialogState.fetched) {
			getLoaderManager().initLoader(REQUEST_REMOVE_DIALOG_LOADER, null, this);
			
		}
		
		// Inflate the layout for this fragment
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		try {
			mListener = (OnInteractionListener) getParentFragment();
		} catch (ClassCastException e) {
			Log.e(getTag(), "Parent fragment must implement " + OnInteractionListener.class.getName());
			throw e;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {	
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_DELETE_DIALOG:
			
			if (DialogInterface.BUTTON_POSITIVE == resultCode) {
				
				if (mLetteringItem != null) {
					final String letteringId = mLetteringItem.getId(); 
					RestClient.getInstance().getLetteringService()
						.delete(new CurrentUser(getActivity()).getIBSAccessToken(), letteringId, 
								new Callback<UpdateResult>() {									
									@Override
									public void success(UpdateResult arg0, Response arg1) {
										if (arg0 != null && arg0.isSuccessful()) {
											AppCache.setLetteringListResponse(null);
											Utility.toastMessage(getActivity(), getString(R.string.ibs_text_deleteSuccess_lettering));
											mListener.onLetteringPopRequest();
										} else {
											Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
										}
										dismissDialog(TAG_REMOVE_DIALOG);
									}
									
									@Override
									public void failure(RetrofitError arg0) {
										Log.d(CLASS_NAME, "Failed to delete: " + letteringId);
										Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
										dismissDialog(TAG_REMOVE_DIALOG);
									}
								}
						);
				}
			} else {
				dismissDialog(TAG_REMOVE_DIALOG);
			}
			break;

		default:
			break;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Dismisses a given dialog, if found. */
	private void dismissDialog(String tag) {
		DialogFragment dialog = (DialogFragment) getFragmentManager().findFragmentByTag(tag);
		if (dialog != null) {
			dialog.dismiss();
		}
	}
	
	/** Attempts to find and set the dialog message, while caching it on this fragment.
	 * @param requestId The dialog  */
	private void getAndSetConfirmMessage(ContentResponse data) {
			
		String message = getString(R.string.ibs_error_cannotLoadContent);
		if (data != null) {
			message = data.getContent();
			mDialogState.fetched = true;
		} 

		mDialogState.message = message;
		
		Fragment frag = getFragmentManager().findFragmentByTag(TAG_REMOVE_DIALOG);
		if (frag != null && frag instanceof SimpleYesNoDialog) {
			((SimpleYesNoDialog) frag).updateContent(message);
		}		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onClick(View v) {
		
	}
	
	
	@Override
	public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case REQUEST_REMOVE_DIALOG_LOADER:
			return new SimpleContentAsyncLoader(
					getActivity(), 
					getString(R.string.ibs_config_load_removeCustomMarking));
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
		 case REQUEST_REMOVE_DIALOG_LOADER:
			 getAndSetConfirmMessage(data);
		 }
		
	}
	 
	@Override
	public void onSave(LetteringItem item) {
		mListener.onLetteringPopRequest();
	}

	@Override
	public void onCancel() {
		if (mLetteringItem != null) { //edit mode, thus delete
			DialogFragment dialog = 
			SimpleYesNoDialog.newInstance(mDialogState.message, true);
			dialog.setTargetFragment(this, REQUEST_DELETE_DIALOG);
			dialog.show(getFragmentManager(), TAG_REMOVE_DIALOG);
		} else {
			mListener.onLetteringPopRequest();
		}
	}

	@Override
	public void enableViews(boolean enabled) {
				
	}
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Interaction interface
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	 /** @version 0.2.0-20150715 */
	public static interface OnInteractionListener {
		public void onLetteringPopRequest();
	}

	

}
