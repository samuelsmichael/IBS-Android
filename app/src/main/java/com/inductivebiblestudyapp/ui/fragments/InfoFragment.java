package com.inductivebiblestudyapp.ui.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.loaders.SimpleContentAsyncLoader;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A placeholder fragment containing a simple view.
 * @author Jason Jenkins
 * @version 0.3.1-20150612
 */
public class InfoFragment extends Fragment implements OnClickListener, LoaderManager.LoaderCallbacks<ContentResponse> {
	final static private String CLASS_NAME = InfoFragment.class.getSimpleName();
	final static private String LOGTAG = CLASS_NAME;
	
	private static final String KEY_TITLE = CLASS_NAME + ".KEY_TITLE";
	private static final String KEY_CONTENT_LOADER = CLASS_NAME + ".KEY_CONTENT_LOADER";
	
	private static final String KEY_CONTENT = CLASS_NAME + ".KEY_CONTENT";	
	/**Bundle key: Boolean. Whether the content has been found or to keep fetching. */
	private static final String KEY_CONTENT_FOUND = CLASS_NAME + ".KEY_CONTENT_FOUND";
	
	private static final int REQUEST_INFO_LOADER = 0;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static InfoFragment newInstance(String title, String contentLoader) {
		InfoFragment frag = new InfoFragment();
		Bundle args = new Bundle();
		args.putString(KEY_TITLE, title);
		args.putString(KEY_CONTENT_LOADER, contentLoader);
		frag.setArguments(args);
		return frag;
	}
	
	public InfoFragment() {
	}
	
	/** Reserved for the activity to listen on. */
	private OnClickListener mFooterListener = null;
	
	private String mLoaderRequest = null;
	
	private TextView mContent = null;
	
	private View mProgress = null;
	/** Set in {@link #onLoadFinished(Loader, ContentResponse)} */
	private boolean mIsContentFound = false;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_CONTENT_FOUND, mIsContentFound);
		outState.putString(KEY_CONTENT, mContent.getText().toString());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_info,
				container, false);
		
		Bundle args = getArguments();
		if (args != null) {
			String title = args.getString(KEY_TITLE);
			mLoaderRequest = args.getString(KEY_CONTENT_LOADER);
			
			((TextView) rootView.findViewById(R.id.ibs_details_text_title)).setText(title);
		}
		mContent = (TextView) rootView.findViewById(R.id.ibs_details_text_content);
		mProgress = Utility.getProgressView(rootView);
		
		if (savedInstanceState != null) {
			mContent.setText(savedInstanceState.getString(KEY_CONTENT));
			mIsContentFound = savedInstanceState.getBoolean(KEY_CONTENT_FOUND, false);
		} else {
			mIsContentFound = false;
		}
		

		if (!mIsContentFound) { //only look if we have no content
			getLoaderManager().initLoader(REQUEST_INFO_LOADER, null, this);
			checkIfLoading(false);
		}
				
		rootView.findViewById(R.id.ibs_footer_home).setOnClickListener(this);
		rootView.findViewById(R.id.ibs_footer_about).setOnClickListener(this);
		rootView.findViewById(R.id.ibs_footer_contact).setOnClickListener(this);
		rootView.findViewById(R.id.ibs_footer_privacy).setOnClickListener(this);
		rootView.findViewById(R.id.ibs_footer_terms).setOnClickListener(this);
		
		return rootView;
	}
	
	@Override //quick and easy interaction listener; not the right way but it works.
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		
		
		try {
			mFooterListener = (OnClickListener) getActivity();
			
		} catch (ClassCastException notImplemented) {
			Log.w(LOGTAG, "Activity must implement 'OnClickListener'");
			throw notImplemented;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		checkIfLoading(false);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Performs view safety checks, then animates views (if forced) or checks whether to
	 * animate views based on loader state.
	 * @param force
	 */
	private void checkIfLoading(boolean force) {
		Utility.checkIfLoading(getLoaderManager(), REQUEST_INFO_LOADER, mProgress, mContent, force);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
		checkIfLoading(true);
		return new SimpleContentAsyncLoader(getActivity(), mLoaderRequest);
	}
	
	@Override
	public void onLoaderReset(Loader<ContentResponse> loader) {
		// nothing to do yet.
		
	}
	
	 @Override
	public void onLoadFinished(Loader<ContentResponse> loader,
			ContentResponse data) {
		 //makes it easier to tell if we are still loading
		 getLoaderManager().destroyLoader(REQUEST_INFO_LOADER); 
		 
		if (data == null) {
			mIsContentFound = false;
			mContent.setText(R.string.ibs_error_cannotLoadContent);
		} else {
			mIsContentFound = true;
			mContent.setText(data.getContent());
		}
		checkIfLoading(false);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		default://fall through to main
			mFooterListener.onClick(v);
			break;		
		}
	}
}
