package com.inductivebiblestudyapp.data.loaders;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;

import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.data.service.SimpleContentService;

/**
 * Loader to fetch/parse simple api content in re-attachable {@link AsyncTaskLoader}. 
 * @author Jason Jenkins
 * @version 0.3.0-20150615
 */
public class SimpleContentAsyncLoader extends AbstractFetchAsyncLoader<ContentResponse> {
	private final SimpleContentService mService;
	
	private final String mPageName;
	
	
	public SimpleContentAsyncLoader(Context context, String pageName) {
		super(context);
		this.mService = RestClient.getInstance().getSimpleContentService();
		this.mPageName = pageName;
	}

	@Override
	protected ContentResponse fetchResult() {
		return (ContentResponse) mService.getContent(mPageName); 
	}
}
