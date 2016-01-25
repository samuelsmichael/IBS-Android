package com.inductivebiblestudyapp.ui.fragments.profile.notes;

import java.text.DecimalFormat;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnDismissListener;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.AppCache.OnCacheUpdateListener;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.loaders.StudyNotesSearchLoader;
import com.inductivebiblestudyapp.data.model.ImageListResponse;
import com.inductivebiblestudyapp.data.model.LetteringListResponse;
import com.inductivebiblestudyapp.data.model.StudyNotesResponse;
import com.inductivebiblestudyapp.data.model.StudyNotesResponse.StudyNoteItem;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse;
import com.inductivebiblestudyapp.ui.adapters.StudyNotesRecyclerAdapter;
import com.inductivebiblestudyapp.ui.adapters.StudyNotesRecyclerAdapter.OnStudyNoteItemClickListener;
import com.inductivebiblestudyapp.ui.dialogs.BibleSearchDialog;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A simple {@link Fragment} subclass. Use the {@link NotesListFragment#newInstance}
 * factory method to create an instance of this fragment.
 * @author Jason Jenkins
 * @version 0.5.3-20150826
 */ 
public class NotesListFragment extends Fragment  implements OnStudyNoteItemClickListener, 
	OnClickListener, TextWatcher, OnEditorActionListener, OnMenuItemClickListener, OnDismissListener {
	
	final static private String CLASS_NAME = NotesListFragment.class
			.getSimpleName();
	/** Class name for debugging purposes. */
	final static private String LOGTAG = CLASS_NAME;
	
	/** Bundle key: Int array. The states of the column sorts.
	 * Stored in order of book, chapter, verse */
	private static final String KEY_SORT_STATES = CLASS_NAME + ".KEY_SORT_STATES";
	
	
	private static final String TAG_NOTE_SEARCH = CLASS_NAME + ".TAG_NOTE_SEARCH";
	
	
	/** Bundle. The current search bundle. Can be <code>null</code> */
	private static final String KEY_CURRENT_SEARCH_BUNDLE = CLASS_NAME + ".KEY_CURRENT_SEARCH";
	
	private static final int REQUEST_SEARCH_DIALOG = 0;
	
	private static final int REQUEST_SEARCH_LOADER = 0x10;
	
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###,###,###");
	
	
	/** Boolean. Whether to show the chapter theme. */
	private static final String EXTRA_CHAPTER_THEME = CLASS_NAME + ".EXTRA_CHAPTER_THEME";
	
	/** Boolean. Whether to show the division theme. */
	private static final String EXTRA_DIV_THEME = CLASS_NAME + ".EXTRA_DIV_THEME";
	/** Boolean. Whether to show the notes. */
	private static final String EXTRA_NOTES = CLASS_NAME + ".EXTRA_NOTES";
	/** Boolean. Whether to show the image markings. */
	private static final String EXTRA_IMAGE_MARKINGS = CLASS_NAME + ".EXTRA_IMAGE_MARKINGS";
	/** Boolean. Whether to show the lettering markings. */
	private static final String EXTRA_LETTERING_MARKINGS = CLASS_NAME + ".EXTRA_LETTERING_MARKINGS";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 */
	public static NotesListFragment newInstance() {
		NotesListFragment fragment = new NotesListFragment();
		return fragment;
	}

	public NotesListFragment() {
		// Required empty public constructor
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factor methods & constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private RecyclerView mStudyNotesList = null;
	
	private TextView mBookSort = null;
	private TextView mChapterSort = null;
	private TextView mVerseSort = null;
	
	private EditText mSearchInput = null;	
	private TextView mSearchSummary = null;
	
	private View mFilterButton = null;
	
	private ProgressDialog mSearchingDialog = null;	
	private PopupMenu mFilterPopup = null; 
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private boolean mFilterShowChapterTheme = true;
	private boolean mFilterShowDivTheme = true;
	private boolean mFilterShowNotes = true;
	private boolean mFilterShowImageMarkings = true;
	private boolean mFilterShowLetteringMarkings = true;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End booleans for filtering
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private StudyNotesRecyclerAdapter mStudyNotesAdapter = null;
	private StudyNotesRecyclerAdapter mSearchResultsAdapter = null;
	
	private OnInteractionListener mInteractionListener = null;
	
	private String mAccessToken = null;
	
	
	/** The current search bundle. <code>null</code> if not currently in search mode. */
	private Bundle mCurrentSearchBundle = null;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBundle(KEY_CURRENT_SEARCH_BUNDLE, mCurrentSearchBundle);
		
		outState.putBoolean(EXTRA_CHAPTER_THEME, mFilterShowChapterTheme);
		outState.putBoolean(EXTRA_DIV_THEME, mFilterShowDivTheme);
		outState.putBoolean(EXTRA_NOTES, mFilterShowNotes);
		outState.putBoolean(EXTRA_IMAGE_MARKINGS, mFilterShowImageMarkings);
		outState.putBoolean(EXTRA_LETTERING_MARKINGS, mFilterShowLetteringMarkings);
		
		if (mBookSort != null || mChapterSort != null || mVerseSort != null) {
			int[] states = new int[] {
					(Integer) mBookSort.getTag(),
					(Integer) mChapterSort.getTag(),
					(Integer) mVerseSort.getTag()
			};
			outState.putIntArray(KEY_SORT_STATES, states);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View  rootView = inflater.inflate(R.layout.fragment_notes_list, container, false);
		
		mAccessToken = new CurrentUser(getActivity()).getIBSAccessToken();
		
		initViews(rootView);        
        initSortState(savedInstanceState);
        
        if (savedInstanceState != null) {
			mCurrentSearchBundle = savedInstanceState.getBundle(KEY_CURRENT_SEARCH_BUNDLE);
			
			mFilterShowChapterTheme = savedInstanceState.getBoolean(EXTRA_CHAPTER_THEME);
			mFilterShowDivTheme = savedInstanceState.getBoolean(EXTRA_DIV_THEME);
			mFilterShowNotes = savedInstanceState.getBoolean(EXTRA_NOTES);
			mFilterShowImageMarkings = savedInstanceState.getBoolean(EXTRA_IMAGE_MARKINGS);
			mFilterShowLetteringMarkings = savedInstanceState.getBoolean(EXTRA_LETTERING_MARKINGS);			
		}
		
        AppCache.addBibleVerseUpdateListener(mVerseListener);
		AppCache.addLetteringListUpdateListener(mLetteringListener);
		AppCache.addImageListUpdateListener(mImageListener);
		
		restoreViewStateAndQuery(true);
		updateStudyNotes();
		
		return rootView;
	}

	
	

	@Override
	public void onDestroyView() {
		super.onDestroyView();		
		
		if (mSearchingDialog != null) {
			mSearchingDialog.dismiss();
			mSearchingDialog = null;
		}
		if (mFilterPopup != null) {
			mFilterPopup.dismiss();
			mFilterPopup = null; 
		}
		
		AppCache.removeBibleVerseUpdateListener(mVerseListener);
		AppCache.removeLetteringListUpdateListener(mLetteringListener);
		AppCache.removeImageListUpdateListener(mImageListener);
	}

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		try {
			mInteractionListener = (OnInteractionListener) getParentFragment();
		} catch (ClassCastException e) {
			Log.e(getTag(), "Parent fragment must implement " + OnInteractionListener.class.getName());
			throw e;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_SEARCH_DIALOG:
			if (resultCode == BibleSearchDialog.RESULT_OK) {
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
			break;
			
		default:
			break;
		}
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Initializers methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes the views */
	private void initViews(View rootView) {
		mStudyNotesAdapter = new StudyNotesRecyclerAdapter(this, R.string.ibs_text_noStudyNotes);
		mSearchResultsAdapter = new StudyNotesRecyclerAdapter(this, R.string.ibs_text_noStudyNotesResults);
		
		mStudyNotesList = (RecyclerView) rootView.findViewById(R.id.ibs_studyNotes_recycleView);
        mStudyNotesList.setHasFixedSize(true);       

        // use a horizontal linear layout manager
        LinearLayoutManager imgContainer  = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        
        mStudyNotesList.setLayoutManager(imgContainer); 
        mStudyNotesList.setItemAnimator(new DefaultItemAnimator());
        
        mBookSort = (TextView) Utility.setOnClickAndReturnView(rootView, R.id.ibs_studyNotes_sortBy_book, this);
        mChapterSort = (TextView) Utility.setOnClickAndReturnView(rootView, R.id.ibs_studyNotes_sortBy_chapter, this);
        mVerseSort = (TextView) Utility.setOnClickAndReturnView(rootView, R.id.ibs_studyNotes_sortBy_verse, this);
        
        mSearchInput = (EditText) rootView.findViewById(R.id.ibs_studyNotes_edittext_search);
		mSearchInput.addTextChangedListener(this);
		mSearchInput.setOnEditorActionListener(this);
		
		mSearchSummary = (TextView) rootView.findViewById(R.id.ibs_studyNotes_text_search_summary);	
		
		mFilterButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_studyNotes_text_filter, this);
	}
	
	/** Initializes the starting states of the views. */
	private void initSortState(Bundle saveState) {
		int[] states = new int[] {
				StudyNotesRecyclerAdapter.SORT_ASC,
				StudyNotesRecyclerAdapter.SORT_ASC,
				StudyNotesRecyclerAdapter.SORT_ASC
			};
		
		if (saveState != null) {
			if (saveState.containsKey(KEY_SORT_STATES)) {
				states = saveState.getIntArray(KEY_SORT_STATES);
			}
		}
		
		mBookSort.setTag(states[0]);
		mChapterSort.setTag(states[1]);
		mVerseSort.setTag(states[2]);
		
		setDrawableByState(mBookSort);
		setDrawableByState(mChapterSort);
		setDrawableByState(mVerseSort);
		
		setSortByTags();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*package*/ void launchSearchProgressDialog() {
		if (mSearchingDialog == null) { //causes the dialog to be launched once
			mSearchingDialog = ProgressDialog.show(getActivity(), "", getString(R.string.ibs_text_searching));
		}
	}
	
	/*package*/ void scrollStudyNotesToTop() {
		if (mStudyNotesList != null) {
			mStudyNotesList.post(new Runnable() {						
				@Override
				public void run() {
					mStudyNotesList.scrollToPosition(0);							
				}
			});
		}
	}
	
	/** Used the check whether or not the study notes need to be refreshed, if so do. */
	/*package*/ void checkAndReload(Object value) {
		if (value == null) {
			AppCache.setsStudyNotesReponse(null);
			updateStudyNotes();
		}
	}
	
	/*package*/ NotesListFragment self() {
		if (getFragmentManager() == null || getTag() == null) {
			return null;
		}
		return (NotesListFragment) getFragmentManager().findFragmentByTag(getTag());
	}
	
	/** Updates the book list, whether from cache or from web. Assumes 
	 * Adapters are valid. */
	private void updateStudyNotes() {
		StudyNotesResponse response = AppCache.getStudyNotesResponse();
		if (response != null) {
			mStudyNotesAdapter.setStudyNotes(response);
			return;
		}
		
		RestClient.getInstance().getStudyNotesService()
			.list(mAccessToken, new Callback<StudyNotesResponse>() {

				@Override
				public void success(StudyNotesResponse response, Response arg1) {
					Log.d(LOGTAG, "Successful study notes");
					NotesListFragment myself = self();
					
					if (response != null) {
						AppCache.setsStudyNotesReponse(response);
						if (myself != null && myself.mStudyNotesAdapter != null) {
							myself.mStudyNotesAdapter.setStudyNotes(response);
						}
						
					} else {
						if (myself != null && myself.mStudyNotesAdapter != null) {
							myself.mStudyNotesAdapter.setStudyNotes(null);
							myself.mStudyNotesAdapter.setError(myself.getString(R.string.ibs_error_cannotConnect));
						}
					}
				}
				
				@Override
				public void failure(RetrofitError arg0) {
					Log.d(LOGTAG, "Failed study notes");
					NotesListFragment myself =  self();
					if (myself != null && myself.mStudyNotesAdapter != null) {
						myself.mStudyNotesAdapter.setStudyNotes(null);
						myself.mStudyNotesAdapter.setError(myself.getString(R.string.ibs_error_cannotConnect));
					}
				}
			}
		);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Filter helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Creates and shows the filter popup */
	private void showFilterPopup() {
		if (mFilterPopup == null) {
			mFilterPopup = new PopupMenu(getActivity(), mFilterButton);
			mFilterPopup.inflate(R.menu.filter_study_notes);
			mFilterPopup.setOnMenuItemClickListener(this);
			mFilterPopup.setOnDismissListener(this);
		}
		setMenuCheck(R.id.filter_studyNotes_chapterThemes, mFilterShowChapterTheme);
		setMenuCheck(R.id.filter_studyNotes_divisionThemes, mFilterShowDivTheme);
		setMenuCheck(R.id.filter_studyNotes_notes, mFilterShowNotes);
		setMenuCheck(R.id.filter_studyNotes_imageMarkings, mFilterShowImageMarkings);
		setMenuCheck(R.id.filter_studyNotes_letteringMarkings, mFilterShowLetteringMarkings);
		
		mFilterPopup.show();
	}
	
	/** Restores the check box states. */
	private void setMenuCheck(int id, boolean value) {
		if (mFilterPopup != null) {
			MenuItem menuItem = mFilterPopup.getMenu().findItem(id);
			menuItem.setChecked(value)
			//below is required to prevent the menu from being dismissed prematurely
					.setActionView(new View(getActivity()))
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
			MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
				
				@Override
				public boolean onMenuItemActionExpand(MenuItem item) {
					return false;
				}
				
				@Override
				public boolean onMenuItemActionCollapse(MenuItem item) {
					return false;
				}
			});
		}
	}
	
	/** Updates the filter state. */
	private void updateFilteredState() {
		final int[] types = new int[]{
			StudyNotesRecyclerAdapter.TYPE_CHAPTER_THEME,
			StudyNotesRecyclerAdapter.TYPE_DIV_THEME,
			StudyNotesRecyclerAdapter.TYPE_NOTES,
			StudyNotesRecyclerAdapter.TYPE_IMAGE,
			StudyNotesRecyclerAdapter.TYPE_LETTERING	
		};
		
		final boolean[] show = new boolean[] {
			mFilterShowChapterTheme,
			mFilterShowDivTheme,
			mFilterShowNotes,
			mFilterShowImageMarkings,
			mFilterShowLetteringMarkings
		};
		
		mSearchResultsAdapter.setFilter(types, show);
		mStudyNotesAdapter.setFilter(types, show);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Search helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Restores the view state. 
	 * First checking the filter state. Then 
	 * hiding/showing views, populating content and showing dialogs
	 * when needed. 
	 */
	private void restoreViewStateAndQuery(boolean performQuery) {
		//always update the filter state if possible.
		updateFilteredState();
		
		if (mCurrentSearchBundle == null) {
			mSearchSummary.setText("");
			mSearchSummary.setVisibility(View.GONE);
			
			mStudyNotesList.setAdapter(mStudyNotesAdapter);
			mSearchResultsAdapter.clear();
		} else {
			final String query =  mCurrentSearchBundle.getString(BibleSearchDialog.KEY_SEARCH_QUERY);
			
			mSearchSummary.setVisibility(View.VISIBLE);
			mStudyNotesList.setAdapter(mSearchResultsAdapter);			
			
			if (Utility.checkIfLoading(getLoaderManager(), REQUEST_SEARCH_LOADER)) {
				launchSearchProgressDialog();
			}
			
			if (performQuery) {
				mSearchResultsAdapter.clear(); //clear results before attempting to load
				getLoaderManager().initLoader(REQUEST_SEARCH_LOADER, mCurrentSearchBundle, mSearchCallback);
				//when loading, apply "searching for"
				mSearchSummary.setText(getString(R.string.ibs_label_searchingFor, query));
			} else {
				String count = "0";
				if (!mSearchResultsAdapter.isEmpty()) {
					count = NUMBER_FORMAT.format(mSearchResultsAdapter.getItemCount());
				}
				
				mSearchSummary.setText(getString(R.string.ibs_label_searchResultsNumbered, count, query));
			}
			
			mSearchInput.setText(query); 
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Sort state helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/** Toggles the sorting view state. */
	private void progressSortState(TextView view) {
		int startState = 0;
		int nextState = 0;
		
		if (view.getTag() != null) {
			try {
				startState = (Integer) view.getTag();
			} catch (ClassCastException e) {}
		}
		
		if (startState == StudyNotesRecyclerAdapter.SORT_ASC) { //move to next state
			//state Ascending -> descending
			nextState = StudyNotesRecyclerAdapter.SORT_DESC;			
			
		} else if (startState == StudyNotesRecyclerAdapter.SORT_DESC) {
			//state descending -> none
			nextState = StudyNotesRecyclerAdapter.SORT_NONE;
			
		} else { //state none -> Ascending
			nextState = StudyNotesRecyclerAdapter.SORT_ASC;
		}
		view.setTag(nextState);
		setDrawableByState(view);
	}
	
	/** Sets the drawable based on tag state. Assumes tag is set correctly. */
	@SuppressWarnings("deprecation")
	private void setDrawableByState(TextView view) {
		final int startState = (Integer) view.getTag();
		Drawable right = null;
		
		if (startState == StudyNotesRecyclerAdapter.SORT_ASC) {
			right = getResources().getDrawable(R.drawable.ic_arrow_down);			
		} else if (startState == StudyNotesRecyclerAdapter.SORT_DESC) {
			right = getResources().getDrawable(R.drawable.ic_arrow_up);
		} else { //state none
			right = null;
		}
		view.setCompoundDrawablesWithIntrinsicBounds(null, null, right, null);
	}
	
	/** Sets the adapter sort mode by view tags. Assumes views are not null. */
	private void setSortByTags() {
		if (mBookSort.getTag() == null) {
			mBookSort.setTag(StudyNotesRecyclerAdapter.SORT_NONE);
		}
		if (mChapterSort.getTag() == null) {
			mChapterSort.setTag(StudyNotesRecyclerAdapter.SORT_NONE);
		}
		if (mVerseSort.getTag() == null) {
			mVerseSort.setTag(StudyNotesRecyclerAdapter.SORT_NONE);
		}
		mStudyNotesAdapter.setSort(	
				(Integer) mBookSort.getTag(), 
				(Integer)mChapterSort.getTag(), 
				(Integer)mVerseSort.getTag()
				);
		mSearchResultsAdapter.setSort(	
				(Integer) mBookSort.getTag(), 
				(Integer)mChapterSort.getTag(), 
				(Integer)mVerseSort.getTag()
				);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibs_studyNotes_sortBy_book:
		case R.id.ibs_studyNotes_sortBy_chapter:
		case R.id.ibs_studyNotes_sortBy_verse:
			progressSortState((TextView) v);
			setSortByTags();
			break;
			
		case R.id.ibs_studyNotes_text_filter:
			showFilterPopup();
			break;
		}
		
	}

	
	@Override
	public void onStudyNoteItemClick(StudyNoteItem item) {
		String type = "";
		switch (item.getItemType()) {
		case StudyNoteItem.TYPE_NOTE:
			type = "note";
			break;
		case StudyNoteItem.TYPE_CHAPTER_THEME:
			type = "chapter theme";
			break;
		case StudyNoteItem.TYPE_DIV_THEME:
			type = "div theme";
			break;
		case StudyNoteItem.TYPE_MARKING_IMAGE:
			type = "image";
			break;
		case StudyNoteItem.TYPE_MARKING_LETTERING:
			type = "lettering";
			break;
		default:
			type = "unknown";
		}
		String message =  type + " item clicked: " + item.toString();
		//Utility.toastMessage(getActivity(), message);
		Log.d(LOGTAG, message);
		mInteractionListener.onNoteClick(item); 
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start filter menu listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Used only in {@link #onMenuItemClick(MenuItem)} & {@link #onDismiss(PopupMenu)}. */
	private boolean mMenuItemClicked = false;
	@Override
	public boolean onMenuItemClick(MenuItem menuItem) {
		mMenuItemClicked = true;
		
		if (menuItem.isCheckable()) {
			menuItem.setChecked(!menuItem.isChecked());
		}
		
		switch (menuItem.getItemId()) {
		case R.id.filter_studyNotes_chapterThemes:
			mFilterShowChapterTheme = menuItem.isChecked();
			break;
		case R.id.filter_studyNotes_notes:
			mFilterShowNotes = menuItem.isChecked();
			break;
			
		case R.id.filter_studyNotes_divisionThemes:
			mFilterShowDivTheme = menuItem.isChecked();
			break;
			
		case R.id.filter_studyNotes_imageMarkings:
			mFilterShowImageMarkings = menuItem.isChecked();
			break;
			
		case R.id.filter_studyNotes_letteringMarkings:
			mFilterShowLetteringMarkings = menuItem.isChecked();
			break;
			
		default:
			Log.d(LOGTAG, "OnMenuItemClick - Unknown click: " + menuItem.getItemId());
			break;
		}
		
		//do not dismiss popup
		return false;
	}
	

	@Override
	public void onDismiss(PopupMenu arg0) {
		if (mMenuItemClicked) {
			//performs the filter update & updates the views
			restoreViewStateAndQuery(false);
		}
		mMenuItemClicked = false;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start search listeners
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
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			final String searchTerm =   mSearchInput.getText().toString().trim();
			Log.d(LOGTAG, "searching: " + searchTerm);
			
			BibleSearchDialog dialog = 
					BibleSearchDialog.newInstance(searchTerm, mCurrentSearchBundle);
			dialog.setTargetFragment(this, REQUEST_SEARCH_DIALOG);
			dialog.show(getFragmentManager(), TAG_NOTE_SEARCH);
			
			return true;
		}
		return false;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Loaders & cache listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private StudyNotesSearchLoader.OnWebRequestListener mSearchWebRequestListener = 
			new StudyNotesSearchLoader.OnWebRequestListener() {
		@Override
		public void onWebRequest() {
			if (mStudyNotesList == null) {
				return;
			}
			mStudyNotesList.post(new Runnable() { //run on ui thread
				@Override
				public void run() {
					launchSearchProgressDialog();
				}
			});			
		}
	};
	
	private LoaderCallbacks<StudyNotesResponse> mSearchCallback = new LoaderCallbacks<StudyNotesResponse>() {
		@Override
		public void onLoaderReset(Loader<StudyNotesResponse> arg0) {}
		
		@Override
		public void onLoadFinished(Loader<StudyNotesResponse> loader,
				StudyNotesResponse response) {
			getLoaderManager().destroyLoader(loader.getId());
			
			if (mSearchingDialog != null) {
				mSearchingDialog.cancel();
				mSearchingDialog = null;
			}
			
			if (response != null) {
				Log.d(LOGTAG, "response: " + response);
				mSearchResultsAdapter.setStudyNotes(response);
				
				scrollStudyNotesToTop();
				restoreViewStateAndQuery(false);
			} else {
				restoreViewStateAndQuery(false);
				Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
				mSearchResultsAdapter.setStudyNotes(null);
				mSearchSummary.setText(R.string.ibs_error_cannotConnect);
			}
		}

		
		
		@Override
		public Loader<StudyNotesResponse> onCreateLoader(int requestCode, Bundle searchBundle) {
			//searchBundle here assumes the result from the BibleSearchDialog
			Bundle loaderArgs = new Bundle();
			copyBundle(searchBundle, loaderArgs);
			
			return new StudyNotesSearchLoader(getActivity(), loaderArgs, mSearchWebRequestListener);
		}
		
		/*package*/ void copyBundle(Bundle dialogArgs, Bundle loaderArgs) {
			final int SIZE = 5;
			final String[] dialogKeys = new String[SIZE];
			final String[] loaderKeys = new String[SIZE];
			
			dialogKeys[0] = BibleSearchDialog.KEY_SEARCH_QUERY ;
			loaderKeys[0] = StudyNotesSearchLoader.KEY_SEARCH_QUERY ;
			dialogKeys[1] = BibleSearchDialog.KEY_BOOK_ID ;
			loaderKeys[1] = StudyNotesSearchLoader.KEY_BOOK_ID ;
			dialogKeys[2] = BibleSearchDialog.KEY_CHAPTER_ID ;
			loaderKeys[2] = StudyNotesSearchLoader.KEY_CHAPTER_ID ;
			dialogKeys[3] = BibleSearchDialog.KEY_VERSE_START_ID ;
			loaderKeys[3] = StudyNotesSearchLoader.KEY_VERSE_START_ID ;
			dialogKeys[4] = BibleSearchDialog.KEY_VERSE_END_ID ;
			loaderKeys[4] = StudyNotesSearchLoader.KEY_VERSE_END_ID ;
			
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

	private final OnCacheUpdateListener<BibleVerseResponse> mVerseListener = new OnCacheUpdateListener<BibleVerseResponse>() {
		@Override
		public void onCacheUpdate(String key, BibleVerseResponse value) {
			checkAndReload(value);
		}
	};  
	
	private final OnCacheUpdateListener<LetteringListResponse> mLetteringListener = new OnCacheUpdateListener<LetteringListResponse>() {
		@Override
		public void onCacheUpdate(String key, LetteringListResponse value) {
			checkAndReload(value);
		}
	};
	
	private final OnCacheUpdateListener<ImageListResponse> mImageListener = new OnCacheUpdateListener<ImageListResponse>() {
		@Override
		public void onCacheUpdate(String key, ImageListResponse value) {
			checkAndReload(value);
		}
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// public interfaces 
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** @version 0.2.0-20150727 */
	public static interface OnInteractionListener {
		public void onNoteClick(StudyNoteItem noteItem);
	}



}
