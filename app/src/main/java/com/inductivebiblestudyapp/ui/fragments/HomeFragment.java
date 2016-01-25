package com.inductivebiblestudyapp.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.loaders.SimpleContentAsyncLoader;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.ui.actionwrappers.FooterViewActionWrapper;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A placeholder fragment containing a simple view.
 * @author Jason Jenkins
 * @version 0.4.1-20150716
 */
public class HomeFragment extends Fragment implements OnClickListener, LoaderManager.LoaderCallbacks<ContentResponse> {
	final static private String CLASS_NAME = HomeFragment.class.getSimpleName();
	final static private String LOGTAG = CLASS_NAME;
	
	private static final String KEY_CONTENT = CLASS_NAME + ".KEY_CONTENT";	
	
	private static final int REQUEST_WELCOME_LOADER = 0;
	
	
	public HomeFragment() {
	}
	

	/** Reserved for the activity to listen on. */
	private OnClickListener mFooterListener = null;
	
	private TextView mContentView = null;
	private View mProgressView = null;
	
	private String mContentMessage = null;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_CONTENT, mContentMessage);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_home, container,
				false);
		rootView.findViewById(R.id.ibs_button_signIn).setOnClickListener(this);
		rootView.findViewById(R.id.ibs_button_signUp).setOnClickListener(this);
		
		mContentView = (TextView) rootView.findViewById(R.id.ibs_text_welcome);
		mProgressView = Utility.getProgressView(rootView);
		
		if (savedInstanceState != null) {
			mContentMessage = savedInstanceState.getString(KEY_CONTENT);			
		} 
		
		if (mContentMessage == null) {
			getLoaderManager().initLoader(REQUEST_WELCOME_LOADER, null, this);
			checkIfLoading(false);
		} else {
			mContentView.setText(mContentMessage);
		}
		
		FooterViewActionWrapper.newInstance(getActivity(), rootView, false);
		
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
		Utility.checkIfLoading(getLoaderManager(), REQUEST_WELCOME_LOADER, mProgressView, mContentView, force);
	}
		
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
		checkIfLoading(true);
		return new SimpleContentAsyncLoader(getActivity(), getString(R.string.ibs_config_load_welcome));
	}
	
	@Override
	public void onLoaderReset(Loader<ContentResponse> loader) {
		// nothing to do yet.
		
	}
	
	 @Override
	public void onLoadFinished(Loader<ContentResponse> loader,
			ContentResponse data) {
		 //makes it easier to tell if we are still loading
		 getLoaderManager().destroyLoader(REQUEST_WELCOME_LOADER); 
		 
		if (data == null) {
			mContentMessage = null;
			mContentView.setText(R.string.ibs_error_cannotLoadContent);
		} else {
			mContentMessage = data.getContent();
			mContentView.setText(data.getContent());
		}
		checkIfLoading(false);
	}
	
	@Override  
	public void onClick(View v) {
		mFooterListener.onClick(v);
	}
}
