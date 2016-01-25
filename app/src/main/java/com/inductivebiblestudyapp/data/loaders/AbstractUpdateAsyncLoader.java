package com.inductivebiblestudyapp.data.loaders;

import android.content.Context;

import com.inductivebiblestudyapp.auth.CurrentUser;

/**
 * Loader to edit/create note. 
 * <code>true</code> on success, <code>false</code> or <code>null</code> on failure.
 * @author Jason Jenkins
 * @version 0.1.0-20150626
 */
abstract class AbstractUpdateAsyncLoader<Service> extends AbstractFetchAsyncLoader<Boolean> {
	
	protected final Service mService;
	protected final String mAccessToken;
	
	/**
	 * 
	 * @param context
	 * @param args
	 */
	public AbstractUpdateAsyncLoader(Context context, Service service) {
		super(context);
		this.mService = service;		

		CurrentUser user = new CurrentUser(context);
		mAccessToken = user.getIBSAccessToken();
				
	}
	
	@Override
	public Boolean loadInBackground() {
		Boolean result = super.loadInBackground();
		if (null == result) {
			return false; 
		}
		return result;
	}

	
}
