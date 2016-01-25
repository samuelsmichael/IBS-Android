package com.inductivebiblestudyapp.ui.viewstub;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;

/**
 * Wrapper class to perform operations within. 
 * @author Jason Jenkins
 * @version 0.1.1-20150605
 *
 */
abstract public class ViewStubWrapper {
	final static private String LOGTAG = ViewStubWrapper.class.getSimpleName();
	
	protected final Fragment mFragment;
	protected final View mRootView;
	
	protected final int mStubId;
	protected final int mLayoutId;
	
	protected View mStubView = null;
		
	/**
	 * Builds a simple wrapper to perform actions within. 
	 * @param fragment 
	 * @param rootView
	 * @param stubId
	 * @param layoutId
	 */
	public ViewStubWrapper(Fragment fragment, View rootView, int stubId, int layoutId) {
		if (stubId == layoutId) {
			Log.w(LOGTAG, "Both the stubId and layoutId match; this may cause issues");
		}
		this.mFragment = fragment;
		this.mRootView = rootView;
		this.mStubId = stubId;
		this.mLayoutId = layoutId;
		
		mStubView = rootView.findViewById(layoutId);
	}
	
	/** @return <code>true</code> if previously inflated, <code>false</code> otherwise. */
	public boolean isInflated(){
		boolean inflated = mRootView.findViewById(mLayoutId) != null;
		if (inflated && mStubView == null){
			mStubView = mRootView.findViewById(mLayoutId);
			onStubLoaded(mStubView);
		}
		return inflated;
	}
	
	/** Gets the stub, inflating when necessary. */
	public View getStub() {
		if (!isInflated()) { //if not inflated, inflate.
			mStubView = ((ViewStub) mRootView.findViewById(mStubId)).inflate();
			if (!isInflated()) {
				Log.w(LOGTAG, "We tried to inflate, but still did not inflate?");
			} else {
				onStubLoaded(mStubView);
			}
		}
		return mStubView;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Protected methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Called when the stub has been discovered to be loaded. */
	abstract protected void onStubLoaded(View stub);
}
