package com.inductivebiblestudyapp.data.loaders;

import android.content.Context;

import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.model.UpdateResult;


/**
 * Loader to perform updates. 
 * <code>true</code> on success, <code>false</code> or <code>null</code> on failure.
 * @author Jason Jenkins
 * @version 0.1.0-20150716
 */
abstract class AbstractUpdateAsyncLoader2<S> extends AbstractFetchAsyncLoader<UpdateResult> {
	
	protected final S mService;
	protected final String mAccessToken;
	
	/**
	 * 
	 * @param context
	 * @param args
	 */
	public AbstractUpdateAsyncLoader2(Context context, S service) {
		super(context);
		this.mService = service;		

		CurrentUser user = new CurrentUser(context);
		mAccessToken = user.getIBSAccessToken();
				
	}
	

	
}
