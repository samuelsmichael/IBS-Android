package com.inductivebiblestudyapp.ui.dialogs;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse;
import com.inductivebiblestudyapp.data.model.bible.IBibleSearchItem;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse.Chapter;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse.Book;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse.Verse;
import com.inductivebiblestudyapp.ui.adapters.SearchSpinnerAdapter;
import com.inductivebiblestudyapp.util.Utility;

/**
 * Requires the called fragment to call {@link #setTargetFragment(Fragment, int)}.
 * 
 * Returns the results via {@link #RESULT_OK} and {@link #EXTRA_RESULT_SEARCH_BUNDLE}.
 * 
 * @author Jason Jenkins
 * @version 0.5.4-20150827
 */
public class BibleSearchDialog extends DialogFragment implements OnClickListener, 
	TextWatcher, OnItemSelectedListener {
	
	final static private String CLASS_NAME = BibleSearchDialog.class
			.getSimpleName();
	
	private static final String LOGTAG = CLASS_NAME;
	
	/** String. The search term to initialize with. */
	private static final String ARG_SEARCH_TERM = CLASS_NAME + ".ARG_SEARCH_TERM";
	/** Bundle. The search bundle as created by this dialog. */
	private static final String ARG_SEARCH_BUNDLE = CLASS_NAME + ".ARG_SEARCH_BUNDLE";
	
	/** Integer. Index of book in spinner. */
	private static final String KEY_BOOK_INDEX = CLASS_NAME + ".KEY_BOOK_INDEX";
	/** Integer. Index of chapter in spinner. */
	private static final String KEY_CHAPTER_INDEX = CLASS_NAME + ".KEY_CHAPTER_INDEX";
	/** Integer. Index of verse start in spinner. */
	private static final String KEY_VERSE_START_INDEX = CLASS_NAME + ".KEY_VERSE_START_INDEX";
	/** Integer. Index of verse end in spinner. */
	private static final String KEY_VERSE_END_INDEX = CLASS_NAME + ".KEY_VERSE_END_INDEX";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start public constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** String. Required. */
	public static final String KEY_SEARCH_QUERY = CLASS_NAME + ".KEY_SEARCH_QUERY";
	
	/** String. Optional. */
	public static final String KEY_BOOK_ID = CLASS_NAME + ".KEY_BOOK_ID";
	/** String. Optional. */
	public static final String KEY_CHAPTER_ID = CLASS_NAME + ".KEY_CHAPTER_ID";
	/** String. Optional. Requires {@value #KEY_VERSE_END_ID}.	  */
	public static final String KEY_VERSE_START_ID = CLASS_NAME + ".KEY_VERSE_START_ID";
	/** String. Optional. Requires {@link #KEY_VERSE_START_ID}. */
	public static final String KEY_VERSE_END_ID = CLASS_NAME + ".KEY_VERSE_END_ID";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End Bundle constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static final int RESULT_OK = Activity.RESULT_OK;
	
	public static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
	
	/** The search bundle. */
	public static final String EXTRA_RESULT_SEARCH_BUNDLE = CLASS_NAME + ".EXTRA_RESULT_SEARCH_BUNDLE";
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @param query The query to start the dialog with
	 */
	public static BibleSearchDialog newInstance(String query) {
		return newInstance(query, null); 
	}
	
	/**
	 * @param query The query to start the dialog with
	 * @param searchBundle The search bundle as created by this dialog. Modifying or
	 * changing the bundle may result in unexpected behaviour. Can be <code>null</code>.
	 */
	public static BibleSearchDialog newInstance(String query, Bundle searchBundle) {
		BibleSearchDialog dialog = new BibleSearchDialog(); 
		Bundle args = new Bundle();
		
		args.putString(ARG_SEARCH_TERM, query);
		if (searchBundle != null) {
			args.putBundle(ARG_SEARCH_BUNDLE, searchBundle);
		}
		
		dialog.setArguments(args);
		return dialog;
	}
	
	
	public BibleSearchDialog() {}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End initializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private TextView mSearchStatus = null;
	private EditText mSearchInput = null;
	
	private Spinner mBookSpinner = null;
	private Spinner mChapterSpinner = null;
	private Spinner mVerseStartSpinner = null;
	private Spinner mVerseEndSpinner = null;
	
	private View mBookContainer = null;
	private View mChapterContainer = null;
	private View mVerseContainer = null;
	
	private View mSearchButton = null;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private SearchSpinnerAdapter mBookAdapter = null;
	private SearchSpinnerAdapter mChapterAdapter = null;
	private SearchSpinnerAdapter mVerseStartAdapter = null;
	private SearchSpinnerAdapter mVerseEndAdapter = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End adapters
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private String mAccessToken = null;
	
	/** <code>null</code> by default, tracks the unpacking a supplied search bundle if needed.
	 * When the methods return <code>false</code>, please null this object. */
	private SearchBundleUnpacker mSearchBundleUnpacker = null;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mBookSpinner != null) { //check views are not null.			
			outState.putBundle(ARG_SEARCH_BUNDLE, buildSearchBundle());
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SimpleDarkDialog);
		
		mAccessToken = new CurrentUser(getActivity()).getIBSAccessToken();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//inflate from activity to ensure the dialog is styled correctly.
		View rootView = View.inflate(getActivity(), R.layout.dialog_bible_search, null); 
		
		initViews(rootView);
		
		Bundle args = getArguments() ;
		
		if (savedInstanceState != null) {
			mSearchBundleUnpacker = 
					new SearchBundleUnpacker(savedInstanceState.getBundle(ARG_SEARCH_BUNDLE));
		} else if (args != null) {
			mSearchInput.setText(args.getString(ARG_SEARCH_TERM));
			if (args.containsKey(ARG_SEARCH_BUNDLE)) {
				mSearchBundleUnpacker = 
						new SearchBundleUnpacker(args.getBundle(ARG_SEARCH_BUNDLE));
			}
		}		
		
		checkTextLengthAndSetView(mSearchInput.getText());
		
		loadBibleBooks();
				
		return rootView;
	}



	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Initializer methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Finds all necessary views. */
	private void initViews(View rootView) {
		mSearchInput = (EditText) rootView.findViewById(R.id.ibs_dialog_biblesearch_input);
		mSearchInput.addTextChangedListener(this);
		
		mSearchStatus = (TextView) rootView.findViewById(R.id.ibs_dialog_biblesearch_text_status);
		
		mBookContainer = rootView.findViewById(R.id.ibs_dialog_biblesearch_container_book);
		mChapterContainer = rootView.findViewById(R.id.ibs_dialog_biblesearch_container_chapter);
		mVerseContainer = rootView.findViewById(R.id.ibs_dialog_biblesearch_container_verseRange);
				
		initSpinners(rootView);
		
		mSearchButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_dialog_button_positive, this);
		Utility.setOnClickAndReturnView(rootView, R.id.ibs_dialog_button_negative, this);
		
		mSearchStatus.setVisibility(View.GONE);
		mBookContainer.setVisibility(View.GONE);
		mChapterContainer.setVisibility(View.GONE);
		mVerseContainer.setVisibility(View.GONE);
	}


	/** Initializes all spinners. */
	private void initSpinners(View rootView) {
		mBookSpinner = findSpinnerAndSetListener(rootView, R.id.ibs_dialog_biblesearch_select_book);
		mBookAdapter = new SearchSpinnerAdapter(getActivity(), 
							R.layout.spinner_search_item_text, 
							android.R.layout.simple_spinner_dropdown_item,
							R.string.ibs_spinner_bibleSearch_all);
		mBookSpinner.setAdapter(mBookAdapter);
		
		mChapterSpinner = findSpinnerAndSetListener(rootView, R.id.ibs_dialog_biblesearch_select_chapter);
		mChapterAdapter =  new SearchSpinnerAdapter(getActivity(), 
							R.layout.spinner_search_item_text, 
							android.R.layout.simple_spinner_dropdown_item,
							R.string.ibs_spinner_bibleSearch_all);
		mChapterAdapter.setDisplayNameCallback(new SearchSpinnerAdapter.DisplayNameCallback() {
			@Override
			public String getDisplayString(IBibleSearchItem item) {
				return ((Chapter) item).getNumber();
			}
		});
		mChapterSpinner.setAdapter(mChapterAdapter);
		
		final SearchSpinnerAdapter.DisplayNameCallback verseCallback = new SearchSpinnerAdapter.DisplayNameCallback() {
			@Override
			public String getDisplayString(IBibleSearchItem item) {
				return ((Verse) item).getNumber();
			}
		};
		
		mVerseStartSpinner = findSpinnerAndSetListener(rootView, R.id.ibs_dialog_biblesearch_select_verseStart);
		mVerseStartAdapter = new SearchSpinnerAdapter(getActivity(), 
								R.layout.spinner_search_item_text, 
								android.R.layout.simple_spinner_dropdown_item,
								R.string.ibs_spinner_bibleSearch_all);
		mVerseStartAdapter.setDisplayNameCallback(verseCallback);
		mVerseStartSpinner.setAdapter(mVerseStartAdapter);
		
		mVerseEndSpinner = findSpinnerAndSetListener(rootView, R.id.ibs_dialog_biblesearch_select_verseEnd);
		mVerseEndAdapter = new SearchSpinnerAdapter(getActivity(), 
								R.layout.spinner_search_item_text, 
								android.R.layout.simple_spinner_dropdown_item,
								R.string.ibs_spinner_bibleSearch_all);
		mVerseEndAdapter.setDisplayNameCallback(verseCallback);
		mVerseEndSpinner.setAdapter(mVerseEndAdapter);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**Assumes necessary views are set. 
	 * Builds the search bundle based on input */
	private Bundle buildSearchBundle() {
		Bundle result = new Bundle();
		result.putString(KEY_SEARCH_QUERY, mSearchInput.getText().toString().trim());
		
		try {		
			Book bookSelected = (Book) mBookSpinner.getSelectedItem();  
			if (bookSelected != null) {// process selected book
				result.putString(KEY_BOOK_ID, bookSelected.getBookId());
				result.putInt(KEY_BOOK_INDEX, mBookSpinner.getSelectedItemPosition());
				
				Chapter chapterSelected = (Chapter) mChapterSpinner.getSelectedItem();
				if (chapterSelected != null) { //process selected chapter
					result.putString(KEY_CHAPTER_ID, chapterSelected.getChapterId());
					result.putInt(KEY_CHAPTER_INDEX, mChapterSpinner.getSelectedItemPosition());
					
					Verse verseStart = (Verse) mVerseStartSpinner.getSelectedItem();
					Verse verseEnd = (Verse) mVerseEndSpinner.getSelectedItem();
					if (verseStart != null && verseEnd != null) {
						result.putString(KEY_VERSE_START_ID, verseStart.getVerseId());
						result.putString(KEY_VERSE_END_ID, verseEnd.getVerseId());
						
						result.putInt(KEY_VERSE_START_INDEX, mVerseStartSpinner.getSelectedItemPosition());
						result.putInt(KEY_VERSE_END_INDEX, mVerseEndSpinner.getSelectedItemPosition());
					}
				}
			}
		} catch (ClassCastException e) {
			Log.w(LOGTAG, "Unexpected ClassCastException getting item.", e);
		}
		return result;
	}	
	
	/** Assumes necessary views are set. 
	 * Checks the button and sets the view's state; disabling/enabling buttons
	 * and updating counts.
	 */
	private void checkTextLengthAndSetView(CharSequence text) {
		final int LENGTH = text.toString().trim().length();
		if (LENGTH <= 0) {
			mSearchButton.setEnabled(false);
		} else {
			mSearchButton.setEnabled(true);
		}
	}

	

	
	/** Finds the spinner and sets onItemSelected */
	private Spinner findSpinnerAndSetListener(View rootView, int id) {
		Spinner spinner = (Spinner) rootView.findViewById(id);
		spinner.setOnItemSelectedListener(this);
		return spinner;
	}
	
	/** Sets the dialog result based on {@link #getTargetRequestCode()} and dismiss() */
	private void setResultAndFinish(int resultCode, Intent data) {
		final int requestCode = getTargetRequestCode();
		getTargetFragment().onActivityResult(requestCode, resultCode, data);
		dismiss();
	}
	
	/** Finds the fragment itself, may return <code>null</code>. */
	/*package*/ BibleSearchDialog self() {
		if (getFragmentManager() == null || getTag() == null) {
			return this;
		}
		return (BibleSearchDialog) getFragmentManager().findFragmentByTag(getTag());
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/*package*/ static void setBooks(BibleSearchDialog self, BibleResponse response) {
		if (self != null) { //remember: use self to always get the actual dialog
			self.mBookAdapter.clear();
			self.mBookContainer.setVisibility(View.VISIBLE);
			self.mBookAdapter.addAll(response.getOldTestament());
			self.mBookAdapter.addAll(response.getNewTestament());
			
			boolean notApplied = true;
			if (self.mSearchBundleUnpacker != null) {
				notApplied = !self.mSearchBundleUnpacker.onBooksComplete(self.mBookSpinner);
			}
			if (notApplied) {
				self.mSearchBundleUnpacker = null;
				self.mBookSpinner.setSelection(0);
			}	
			hideStatus(self);
		}
	}
	
	/*package*/ static void setChapters(BibleSearchDialog self, BibleChapterResponse response) {
		if (self != null) { //remember: use self to always get the actual dialog
			self.mChapterAdapter.clear();
			self.mChapterContainer.setVisibility(View.VISIBLE);
			self.mChapterAdapter.addAll(response.getChapters());
			
			boolean notApplied = true;
			if (self.mSearchBundleUnpacker != null) {
				notApplied = !self.mSearchBundleUnpacker.onChaptersComplete(self.mChapterSpinner);
			}
			if (notApplied) {
				self.mSearchBundleUnpacker = null;
				self.mChapterSpinner.setSelection(0);
			}			
			hideStatus(self);
		}
	}
	
	/*package*/ static void setVerses(BibleSearchDialog self, BibleVerseResponse response) {
		if (self != null) { //remember: use self to always get the actual dialog
			self.mVerseStartAdapter.clear();
			self.mVerseEndAdapter.clear();
			
			self.mVerseContainer.setVisibility(View.VISIBLE);
			
			self.mVerseStartAdapter.addAll(response.getVerses());
			self.mVerseEndAdapter.addAll(response.getVerses());
			
			boolean notApplied = true;
			if (self.mSearchBundleUnpacker != null) {
				notApplied = !self.mSearchBundleUnpacker.onVersesComplete(self.mVerseStartSpinner, self.mVerseEndSpinner);
				self.mSearchBundleUnpacker = null; //always null out
			}
			if (notApplied) {				
				self.mSearchBundleUnpacker = null;
				self.mVerseStartSpinner.setSelection(0);
				self.mVerseEndSpinner.setSelection(0);
			}
			hideStatus(self);
		}
	}
	
	/**
	 * @param self
	 * @param stringId
	 * @param v The array of views to hide (can be <code>null</code>).
	 */
	/*package*/ static void setAndShowStatus(BibleSearchDialog self, int stringId, View...views) {
		if (self != null) { //remember: use self to always get the actual dialog
			self.mSearchStatus.setText(stringId);
			self.mSearchStatus.setVisibility(View.VISIBLE);
			
			if (views != null) {
				for (View view : views) {
					if (view != null) {
						view.setVisibility(View.GONE);
					}
				}
			}
		}
	}
	
	
	/*package*/ static void hideStatus(BibleSearchDialog self) {
		if (self != null) { //remember: use self to always get the actual dialog
			self.mSearchStatus.setVisibility(View.GONE);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Fetch helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** loads the bible books and sets them into the spinner. */
	private void loadBibleBooks() {
		setAndShowStatus(this, R.string.ibs_text_loading, mBookContainer, mChapterContainer, mVerseContainer);
		BibleResponse response = AppCache.getBibleResponse();
		
		if (response == null) {
			RestClient	.getInstance()
						.getBibleFetchService()
						.getBookList(mAccessToken, 
					new Callback<BibleResponse>() {
				
				@Override
				public void success(BibleResponse response, Response arg1) {
					Log.d(CLASS_NAME, "Success book!");
					BibleSearchDialog self = self();
					
					if (response != null) {
						AppCache.setBibleResponse(response);
						setBooks(self, response);						
					} else {
						setAndShowStatus(self, R.string.ibs_error_cannotConnect, mBookContainer, mVerseContainer);
					}
				}				
				
				@Override
				public void failure(RetrofitError arg0) {
					Log.d(CLASS_NAME, "Failed to get books");
					setAndShowStatus(self(), R.string.ibs_error_cannotConnect, mBookContainer, mVerseContainer);
				}
			});
		} else {
			setBooks(this, response);
		}
	}
	
	
	/** loads the book chapters and sets them into the spinner. 
	 * @param bookId The book id to load, if <code>null</code> simply hides the view. */
	private void loadBibleChapters(final String bookId) {		
		setAndShowStatus(this, R.string.ibs_text_loading,  mChapterContainer, mVerseContainer);
		if (bookId == null) {//we show status loading, hide container, then hide status.
			hideStatus(this);
			return;
		}
		BibleChapterResponse response = AppCache.getBibleChapterResponse(bookId);
		
		if (response == null) {
			RestClient	.getInstance()
						.getBibleFetchService()
						.getChapterList(mAccessToken, bookId,
					new Callback<BibleChapterResponse>() {
				
				@Override
				public void success(BibleChapterResponse response, Response arg1) {
					Log.d(CLASS_NAME, "Success book!");
					BibleSearchDialog self = self();
					
					if (response != null) {
						AppCache.addBibleChapterResponse(bookId, response);;
						setChapters(self, response);
					} else {
						setAndShowStatus(self, R.string.ibs_error_cannotConnect, mChapterContainer);
					}
				}				
				
				@Override
				public void failure(RetrofitError arg0) {
					Log.d(CLASS_NAME, "Failed to get books");
					setAndShowStatus(self(), R.string.ibs_error_cannotConnect, mChapterContainer);
				}
			});
		} else {
			setChapters(this, response);
		}
	}
	
	
	/** loads the book chapters and sets them into the spinner.
	 * @param bookId The book id to load, if <code>null</code> simply hides the view.  */
	private void loadBibleVerses(final String chapterId) {
		setAndShowStatus(this, R.string.ibs_text_loading, mVerseContainer);
		if (chapterId == null) { //we show status loading, hide container, then hide status.
			hideStatus(this);
			return;
		}
		BibleVerseResponse response = AppCache.getBibleVerseResponse(chapterId);
		
		if (response == null) {
			RestClient	.getInstance()
						.getBibleFetchService()
						.getVerseList(mAccessToken, chapterId,
					new Callback<BibleVerseResponse>() {
				
				@Override
				public void success(BibleVerseResponse response, Response arg1) {
					Log.d(CLASS_NAME, "Success book!");
					BibleSearchDialog self = self();
					
					if (response != null) {
						AppCache.addBibleVerseResponse(chapterId, response);
						setVerses(self, response);
					} else {
						setAndShowStatus(self, R.string.ibs_error_cannotConnect, mVerseContainer);
					}
				}				
				
				@Override
				public void failure(RetrofitError arg0) {
					Log.d(CLASS_NAME, "Failed to get books");
					setAndShowStatus(self(), R.string.ibs_error_cannotConnect, mVerseContainer);
				}
			});
		} else {
			setVerses(this, response);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * Class used for tracking the unpacking of the search bundle. 
	 * @version 0.1.0-20150809
	 */
	private static class SearchBundleUnpacker {
		
		final int mBookIndex;
		final int mChapterIndex;
		final int mVerseStartIndex;
		final int mVerseEndIndex;
		
		public SearchBundleUnpacker(Bundle searchBundle) {						
			mBookIndex = searchBundle.getInt(KEY_BOOK_INDEX, -1);
			mChapterIndex = searchBundle.getInt(KEY_CHAPTER_INDEX, -1);
			mVerseStartIndex = searchBundle.getInt(KEY_VERSE_START_INDEX, -1);
			mVerseEndIndex = searchBundle.getInt(KEY_VERSE_END_INDEX, -1);
		}
		
		/** @return <code>true</code> applied, <code>false</code> when nothing was applied. */
		boolean onBooksComplete(Spinner books) {
			if (mBookIndex < 0) {
				return false;
			}
			books.setSelection(mBookIndex);
			return true;
		}
		
		/** @return <code>true</code> applied, <code>false</code> when nothing was applied. */
		boolean onChaptersComplete(Spinner chapter) {
			if (mChapterIndex < 0) {
				return false;
			}
			chapter.setSelection(mChapterIndex);
			return true;
		}
		
		/** @return <code>true</code> applied, <code>false</code> when nothing was applied. */
		boolean onVersesComplete(Spinner verseStart, Spinner verseEnd) {
			if (mVerseStartIndex < 0 || mVerseEndIndex < 0) {
				return false;
			}
			verseStart.setSelection(mVerseStartIndex);
			verseEnd.setSelection(mVerseEndIndex);
			return true;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Implemented Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibs_dialog_button_positive:
			Intent result = new Intent();
			result.putExtra(EXTRA_RESULT_SEARCH_BUNDLE, buildSearchBundle());
			setResultAndFinish(RESULT_OK, result);
			break;		
			
		case R.id.ibs_dialog_button_negative:
			setResultAndFinish(RESULT_CANCELED, null);
			break;
		}
	}	
	
	@Override
	public void onCancel(DialogInterface dialog) {	
		super.onCancel(dialog);
		setResultAndFinish(RESULT_CANCELED, null);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Text watcher methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		checkTextLengthAndSetView(s);
	}

	@Override
	public void afterTextChanged(Editable s) {}


	////////////////////////////////////////////////////////////////////////////////////////////////
	//// On ItemSelected listener methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent.equals(mBookSpinner)) {
			try {
				Book selected = (Book) mBookSpinner.getSelectedItem();
				if (selected != null) {
					loadBibleChapters(selected.getBookId());
				} else {
					loadBibleChapters(null);
				}
			} catch (ClassCastException e) {
				Log.w(LOGTAG, "Unexpected behaviour; cannot cast selected book", e);
			}
			
		} else if (parent.equals(mChapterSpinner)) {
			try {
				Chapter selected = (Chapter) mChapterSpinner.getSelectedItem();
				if (selected != null) {
					loadBibleVerses(selected.getChapterId());
				} else {
					loadBibleVerses(null);
				}
			} catch (ClassCastException e) {
				Log.w(LOGTAG, "Unexpected behaviour; cannot cast selected chapter", e);
			}
			
		} else if (parent.equals(mVerseStartSpinner) || parent.equals(mVerseEndSpinner)) {
			final int count = mVerseStartSpinner.getCount(); //same for both spinners
			
			final int selectedStart = mVerseStartSpinner.getSelectedItemPosition();
			final int selectedEnd = mVerseEndSpinner.getSelectedItemPosition();
			
			/*
			 * 1. First we ensure that only both can be "All" or none.
			 */
			if (parent.equals(mVerseStartSpinner)) {
				if (selectedStart == 0) {
					mVerseEndSpinner.setSelection(0);
					return;
				} else if (selectedEnd == 0) {
					mVerseEndSpinner.setSelection(count - 1);
					return;
				}
				
			} else if (parent.equals(mVerseEndSpinner)) {
				if ( selectedEnd == 0) {
					mVerseStartSpinner.setSelection(0);
					return;
				} else if (selectedStart == 0) {
					mVerseStartSpinner.setSelection(1);
					return;
				}				
			} 
			
			/*
			 * 2. Then we ensure that the start cannot be larger than end
			 *  as this would not make sense.
			 */
			if (selectedStart > selectedEnd) { //cannot have start after end, switch.
				mVerseStartSpinner.setSelection(selectedEnd);
				mVerseEndSpinner.setSelection(selectedStart);
				
			}
			
		} else {
			Log.w(LOGTAG, "Unknown spinner selected: " + parent);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		//nothing to do here
	}	
}
