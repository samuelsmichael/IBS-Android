package com.inductivebiblestudyapp.ui.fragments.profile.bible;

import java.text.DecimalFormat;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.loaders.BibleSearchLoader;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleSearchResponse;
import com.inductivebiblestudyapp.data.model.bible.IBibleSearchItem;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse.Chapter;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse.Book;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse.Verse;
import com.inductivebiblestudyapp.ui.adapters.BibleBookListAdapter;
import com.inductivebiblestudyapp.ui.adapters.BibleSearchResultsRecyclerAdapter;
import com.inductivebiblestudyapp.ui.adapters.BibleSearchResultsRecyclerAdapter.OnBibleSearchItemClickListener;
import com.inductivebiblestudyapp.ui.dialogs.BibleSearchDialog;
import com.inductivebiblestudyapp.ui.widget.ExpandableHeightGridView;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A simple {@link Fragment} subclass. Use the {@link BibleBookListFragment#newInstance}
 * factory method to create an instance of this fragment.
 * @version 0.5.5-20150831
 */
public class BibleBookListFragment extends Fragment implements AdapterView.OnItemClickListener, 
	TextWatcher, OnEditorActionListener, OnBibleSearchItemClickListener {
	
	final static private String CLASS_NAME = BibleBookListFragment.class
			.getSimpleName();
	private static final String LOGTAG = CLASS_NAME;
	
	private static final String TAG_BIBLE_SEARCH = CLASS_NAME + ".TAG_BIBLE_SEARCH";
	
	/** Bundle. The current search bundle. Can be <code>null</code> */
	private static final String KEY_CURRENT_SEARCH_BUNDLE = CLASS_NAME + ".KEY_CURRENT_SEARCH";
	
	private static final int REQUEST_SEARCH_DIALOG = 0;
	private static final int REQUEST_SEARCH_LOADER = 1;
	
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###,###,###");
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 */
	public static BibleBookListFragment newInstance() {
		BibleBookListFragment fragment = new BibleBookListFragment();
		return fragment;
	}

	public BibleBookListFragment() {
		// Required empty public constructor
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factory methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	private TextView mTitleSummary = null;
	private EditText mSearchInput = null; 
	
	private View mBookContainer = null;
	private RecyclerView mSearchResults = null;
	
	private ProgressDialog mSearchingDialog = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private OnInteractionListener mListener =  null;
	
	private BibleBookListAdapter mOldTestAdapter = null;
	private BibleBookListAdapter mNewTestAdapter = null;
	
	private BibleSearchResultsRecyclerAdapter  mSearchResultsAdapter = null;
	
	private int mSpanCount = 2; //TODO refactor 2
	
	/** The current search bundle. <code>null</code> if not currently in search mode. */
	private Bundle mCurrentSearchBundle = null;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle(KEY_CURRENT_SEARCH_BUNDLE, mCurrentSearchBundle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View  rootView = inflater.inflate(R.layout.fragment_bible_books_list, container, false);
		
		initViews(rootView);
		
		if (savedInstanceState != null) {
			mCurrentSearchBundle = savedInstanceState.getBundle(KEY_CURRENT_SEARCH_BUNDLE);
		}
		
		updateBookLists(); //always update book lists
		restoreViewStateAndQuery(true);
		
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
		if (REQUEST_SEARCH_DIALOG == requestCode && resultCode == BibleSearchDialog.RESULT_OK) {
			if (data != null) {
				Bundle args = data.getExtras().getBundle(BibleSearchDialog.EXTRA_RESULT_SEARCH_BUNDLE);
				String query = args.getString(BibleSearchDialog.KEY_SEARCH_QUERY, "");
				
				if (query.isEmpty()) {
					Log.w(LOGTAG, "Unusual state: Cannot perform an empty query");
					return;
				}
				
				mCurrentSearchBundle = args;
				restoreViewStateAndQuery(true);
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Init methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes all views. */
	private void initViews(View rootView) {
		mSearchInput = (EditText) rootView.findViewById(R.id.ibs_bible_books_search);
		mSearchInput.addTextChangedListener(this);
		mSearchInput.setOnEditorActionListener(this);
		
		mTitleSummary = (TextView) rootView.findViewById(R.id.ibs_bible_books_titleSummary);
		
		mSearchResultsAdapter = new BibleSearchResultsRecyclerAdapter(this);
		
		mBookContainer = rootView.findViewById(R.id.ibs_bible_books_container_books);
		
		mSearchResults = (RecyclerView) rootView.findViewById(R.id.ibs_bible_books_searchResults_recyclerview);
		// use a grid layout manager
		GridLayoutManager resultsContainer  = new GridLayoutManager(getActivity(), mSpanCount); 
		mSearchResults.setLayoutManager(resultsContainer);
		mSearchResults.setAdapter(mSearchResultsAdapter);
		
		mOldTestAdapter = new BibleBookListAdapter(getActivity());
		mNewTestAdapter = new BibleBookListAdapter(getActivity());
		
		ExpandableHeightGridView oldTestGridView = (ExpandableHeightGridView) rootView.findViewById(R.id.ibs_bible_books_gridview_old);
		oldTestGridView.setAdapter(mOldTestAdapter);
		oldTestGridView.setExpanded(true);
		oldTestGridView.setOnItemClickListener(this);
		
		ExpandableHeightGridView newTestGridView = (ExpandableHeightGridView) rootView.findViewById(R.id.ibs_bible_books_gridview_new);
		newTestGridView.setAdapter(mNewTestAdapter);
		newTestGridView.setExpanded(true);
		newTestGridView.setOnItemClickListener(this);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*package*/ BibleBookListFragment self() {
		if (getFragmentManager() == null || getTag() == null) {
			return this;
		}
		return (BibleBookListFragment) getFragmentManager().findFragmentByTag(getTag());
	}
	
	/** Updates the book list, whether from cache or from web. Assumes 
	 * Adapters are valid. */
	private void updateBookLists() {
		BibleResponse response = AppCache.getBibleResponse();
		final String error = getString(R.string.ibs_error_cannotConnect_short);
		
		mOldTestAdapter.setMessage(getString(R.string.ibs_text_loading));
		mNewTestAdapter.setMessage(getString(R.string.ibs_text_loading));
		
		if (response == null) {		
			RestClient	.getInstance()
						.getBibleFetchService()
						.getBookList(new CurrentUser(getActivity()).getIBSAccessToken(), 
					new Callback<BibleResponse>() {
				
				@Override
				public void success(BibleResponse response, Response arg1) {
					Log.d(CLASS_NAME, "Success book!");
					AppCache.setBibleResponse(response);
					BibleBookListFragment myself = self();
					if (myself != null && myself.mOldTestAdapter != null) {
						if (response != null) {
							myself.mOldTestAdapter.updateBookList(response.getOldTestament());
							myself.mNewTestAdapter.updateBookList(response.getNewTestament());
						} else {
							myself.mOldTestAdapter.setMessage(error);
							myself.mNewTestAdapter.setMessage(error);
						}
					}
				}
				
				@Override
				public void failure(RetrofitError arg0) {
					Log.d(CLASS_NAME, "Failed to get books");
					BibleBookListFragment myself = self();
					if (myself != null && myself.mOldTestAdapter != null) {
						myself.mOldTestAdapter.setMessage(error);
						myself.mNewTestAdapter.setMessage(error);
					}
				}
			});
		} else {
			mOldTestAdapter.updateBookList(response.getOldTestament());
			mNewTestAdapter.updateBookList(response.getNewTestament());
		}
	}
	
	/** Restores the view state. Hiding/showing views, populating content and showing dialogs
	 * when needed. 
	 */
	private void restoreViewStateAndQuery(boolean performQuery) {
		if (mCurrentSearchBundle == null) {
			mTitleSummary.setText(R.string.ibs_label_booksInOrder);
			mBookContainer.setVisibility(View.VISIBLE);
			mSearchResults.setVisibility(View.GONE);
			
		} else {
			final String query =  mCurrentSearchBundle.getString(BibleSearchDialog.KEY_SEARCH_QUERY);
			
			mBookContainer.setVisibility(View.GONE);
			mSearchResults.setVisibility(View.VISIBLE);
			
			if (Utility.checkIfLoading(getLoaderManager(), REQUEST_SEARCH_LOADER)) {
				mSearchingDialog = ProgressDialog.show(getActivity(), "", getString(R.string.ibs_text_searching));
			}
			
			if (performQuery) {
				mSearchResultsAdapter.clear(); //clear results before attempting to load
				getLoaderManager().initLoader(REQUEST_SEARCH_LOADER, mCurrentSearchBundle, mSearchCallback);
				//when loading, apply "searching for"
				mTitleSummary.setText(getString(R.string.ibs_label_searchingFor, query));
			} else {
				String count = "0";
				if (!mSearchResultsAdapter.isEmpty()) {
					count = NUMBER_FORMAT.format(mSearchResultsAdapter.getItemCount());
				}
				
				mTitleSummary.setText(getString(R.string.ibs_label_searchResultsNumbered, count, query));
			}
			
			mSearchInput.setText(query); 
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private LoaderCallbacks<BibleSearchResponse> mSearchCallback = new LoaderCallbacks<BibleSearchResponse>() {
		@Override
		public void onLoaderReset(Loader<BibleSearchResponse> arg0) {}
		
		@Override
		public void onLoadFinished(Loader<BibleSearchResponse> loader,
				BibleSearchResponse response) {
			getLoaderManager().destroyLoader(loader.getId());
			
			if (mSearchingDialog != null) {
				mSearchingDialog.cancel();
				mSearchingDialog = null;
			}
			
			if (response != null) {
				Log.d(LOGTAG, "response: " + response);
				mSearchResultsAdapter.setResults(response);
				
				if (mSearchResults != null) {
					mSearchResults.post(new Runnable() {						
						@Override
						public void run() {
							mSearchResults.scrollToPosition(0);							
						}
					});
				}
				restoreViewStateAndQuery(false);
			} else {
				restoreViewStateAndQuery(false);
				Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
				mSearchResultsAdapter.setResults(null);
				mTitleSummary.setText(R.string.ibs_error_cannotConnect);
			}
		}
		
		@Override
		public Loader<BibleSearchResponse> onCreateLoader(int requestCode, Bundle searchBundle) {
			//searchBundle here assumes the result from the BibleSearchDialog
			Bundle loaderArgs = new Bundle();
			copyBundle(searchBundle, loaderArgs);
			
			return new BibleSearchLoader(getActivity(), loaderArgs, mSearchWebRequestListener);
		}
		
		/*package*/ void copyBundle(Bundle dialogArgs, Bundle loaderArgs) {
			final int SIZE = 5;
			final String[] dialogKeys = new String[SIZE];
			final String[] loaderKeys = new String[SIZE];
			
			dialogKeys[0] = BibleSearchDialog.KEY_SEARCH_QUERY ;
			loaderKeys[0] = BibleSearchLoader.KEY_SEARCH_QUERY ;
			dialogKeys[1] = BibleSearchDialog.KEY_BOOK_ID ;
			loaderKeys[1] = BibleSearchLoader.KEY_BOOK_ID ;
			dialogKeys[2] = BibleSearchDialog.KEY_CHAPTER_ID ;
			loaderKeys[2] = BibleSearchLoader.KEY_CHAPTER_ID ;
			dialogKeys[3] = BibleSearchDialog.KEY_VERSE_START_ID ;
			loaderKeys[3] = BibleSearchLoader.KEY_VERSE_START_ID ;
			dialogKeys[4] = BibleSearchDialog.KEY_VERSE_END_ID ;
			loaderKeys[4] = BibleSearchLoader.KEY_VERSE_END_ID ;
			
			for (int index = 0; index < SIZE; index++) {
				if (dialogArgs.containsKey(dialogKeys[index])) {
					loaderArgs.putString(
							loaderKeys[index], 
							dialogArgs.getString(dialogKeys[index], null)
							);
				}
			}
		}
		
	};
	
	private BibleSearchLoader.OnWebRequestListener mSearchWebRequestListener = 
			new BibleSearchLoader.OnWebRequestListener() {
		@Override
		public void onWebRequest() {
			if (mSearchResults == null) {
				return;
			}
			mSearchResults.post(new Runnable() { //run on ui thread
				
				@Override
				public void run() {
					if (mSearchingDialog == null) { //causes the dialog to be launched once
						mSearchingDialog = ProgressDialog.show(getActivity(), "", getString(R.string.ibs_text_searching));
					}
				}
			});			
		}
	};
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		BibleBookListAdapter adpater = (BibleBookListAdapter) parent.getAdapter();
		Book book = adpater.getItem(position);
		if (book != null) {
			mListener.onBibleBookSelect(book);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start text watcher
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		final int length = s.length();
		if (length <= 0) { //cleared
			mCurrentSearchBundle = null;
			restoreViewStateAndQuery(false);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {}	

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Action listeners 
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			final String searchTerm =   mSearchInput.getText().toString().trim();
			Log.d(LOGTAG, "searching: " + searchTerm);
			
			BibleSearchDialog dialog = 
					BibleSearchDialog.newInstance(searchTerm, mCurrentSearchBundle);
			dialog.setTargetFragment(this, REQUEST_SEARCH_DIALOG);
			dialog.show(getFragmentManager(), TAG_BIBLE_SEARCH);
			
			return true;
		}
		return false;
	}

	@Override
	public void onBibleSearchItemClick(IBibleSearchItem item) {
		if (item instanceof Book) {
			mListener.onBibleBookSelect((Book) item);
		} else if (item instanceof Chapter) {
			mListener.onBibleChapterSelect(((Chapter) item).getChapterId());
		} else if (item instanceof Verse) {
			Verse verse = (Verse) item;
			mListener.onBibleVerseSelect(verse.getParentChapterId(), verse.getVerseId());
		} else {
			Log.w(LOGTAG, "Unknown search item found: '" + item + "'");
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal interface
	////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @version 0.5.0-20150808
	 */
	public static interface OnInteractionListener {
		/** Informs the listener which book was selected. 
		 * Either in the book list or search results. */
		public void onBibleBookSelect(Book bookSelected);
		/** Informs listener which chapter was selected.*/
		public void onBibleChapterSelect(String chapterId);
		/**  Informs listener which verse was selected. */
		public void onBibleVerseSelect(String chapterId, String verseId);
	}
}
