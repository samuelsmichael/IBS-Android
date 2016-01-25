package com.inductivebiblestudyapp.ui.adapters;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.AppCache.OnCacheUpdateListener;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse.Chapter;

/**
 * Manages the bible chapter list.
 * @author Jason Jenkins
 * @version 0.3.5-20150818
 */
public class BibleChapterListAdapter extends ArrayAdapter<Chapter> implements OnCacheUpdateListener<BibleVerseResponse> {
	/** Class name for debugging purposes. */
	final static private String LOGTAG = BibleChapterListAdapter.class
			.getSimpleName();
	
	private final LayoutInflater mInflater;
	private final int mLayoutId;
	private final Resources mRes;
	
	private final String mBookId;
	private final String mAccessToken;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End final members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private Chapter[] chapters = new Chapter[0];
	private int SIZE = 0;
	
	private boolean mCannotConnect = false;
	private boolean mIsLoading = false;
	
	public BibleChapterListAdapter(Context context, String bookId) {
		super(context, R.layout.grid_item_bible_chapter_text);
		this.mBookId = bookId;
		this.mLayoutId =  R.layout.grid_item_bible_chapter_text;
		
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mRes = context.getResources();
		this.mAccessToken = new CurrentUser(context).getIBSAccessToken();
		
		fetchChapterList();
	}
	

	
	public void updateChapterList(Chapter[] chapters) {
		this.SIZE = chapters.length;
		this.chapters = chapters;
		
		if (SIZE != 0) {
			mCannotConnect = false;
			mIsLoading = false;
		} else {
			mCannotConnect = true;
			mIsLoading = false;
		}
		
		notifyDataSetChanged();
	}
	
	/**
	 * Can be <code>null</code> when empty.
	 * {@inheritDoc}
	 */
	@Override
	public Chapter getItem(int position) {
		if (mCannotConnect || mIsLoading){
			return null;
		}
		return chapters[position];
	}
	
	
	@Override
	public int getCount() {
		if (mCannotConnect || mIsLoading){
			return 1;
		}
		return SIZE;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Holder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(mLayoutId, parent, false);
			holder = new Holder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		if (mIsLoading) {
			holder.text.setText(R.string.ibs_text_loading);
		} else if (mCannotConnect) {
			holder.text.setText(R.string.ibs_error_cannotConnect);
		} else {
			String outputText = "";
			Chapter chapter = chapters[position];
			
			if (chapter.getChapterTheme() != null) {
				outputText = mRes.getString(R.string.ibs_title_chapterWithTheme, 
							chapter.getNumber(), chapter.getChapterTheme().getText());
			} else {
				outputText = chapter.getNumber();
			}

			holder.text.setText(outputText);
		}

		
		return convertView;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Fetches the chapter list and attempts to populate the adapter. */
	private void fetchChapterList() {
		BibleChapterResponse response = AppCache.getBibleChapterResponse(mBookId);
		
		if (response == null) {
			mIsLoading = true;
			notifyDataSetChanged();
			
			RestClient.getInstance()
				.getBibleFetchService()
				.getChapterList(mAccessToken, mBookId, new Callback<BibleChapterResponse>() {
				
				@Override
				public void success(BibleChapterResponse response, Response arg1) {
					if (response != null) {
						Log.d(LOGTAG, "Success chapters!");
						AppCache.addBibleChapterResponse(mBookId, response);
						updateChapterList(response.getChapters());
					} else {
						updateChapterList(new Chapter[0]);
					}
				}
				
				@Override
				public void failure(RetrofitError arg0) {
					Log.d(LOGTAG, "Failed to get chapters");
					updateChapterList(new Chapter[0]);
				}
			});
		} else {
			updateChapterList(response.getChapters());
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class Holder {
		public final TextView text;
		public Holder(View rootView) {
			this.text = (TextView) rootView.findViewById(R.id.grid_item_text);
		}
	}	
	
	@Override
	public void onCacheUpdate(String key, BibleVerseResponse value) {
		if (value == null && key != null && key.contains(mBookId)) { 
			//if null and the chapter being updated is within this book
			AppCache.addBibleChapterResponse(mBookId, null);
			fetchChapterList();
		}
	}

}
