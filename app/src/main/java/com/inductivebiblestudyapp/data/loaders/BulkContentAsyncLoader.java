package com.inductivebiblestudyapp.data.loaders;

import java.util.HashMap;
import java.util.Map.Entry;

import retrofit.RetrofitError;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.data.service.SimpleContentService;

/**
 * Loader to fetch/parse multiple api content in re-attachable {@link AsyncTaskLoader}. 
 * @author Jason Jenkins
 * @version 0.1.0-20150611
 */
public class BulkContentAsyncLoader extends AsyncTaskLoader<HashMap<String, ContentResponse>> {
	/** Class name for debugging purposes. */
	final static private String LOGTAG = BulkContentAsyncLoader.class
			.getSimpleName();

	private final SimpleContentService mService;
	
	private final HashMap<String, String> mPageNames;
	
	/**
	 * Fetches multiple pages at once.
	 * @param context The activity context
	 * @param pages A map between the expected output key and the page requests; that is,
	 * <code>outputKey -&gt; pageRequest</code>
	 */
	public BulkContentAsyncLoader(Context context, HashMap<String, String> pages) {
		super(context);
		this.mService = RestClient.getInstance().getSimpleContentService();
		this.mPageNames = pages;
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		forceLoad(); //required for bug
	}
	
	
	@Override
	public HashMap<String, ContentResponse> loadInBackground() {
		Log.d(LOGTAG, "loadInBackground");
		try {
			HashMap<String, ContentResponse> results = new HashMap<String, ContentResponse>();
			for (Entry<String, String> pageReq : mPageNames.entrySet()) {
				results.put(pageReq.getKey(), mService.getContent(pageReq.getValue()));
			}
			return results;
		} catch (RetrofitError e) {
			Log.e(LOGTAG, "Cannot retrieve contents: " + e);
			return null;
		}
		
	}
	

}
