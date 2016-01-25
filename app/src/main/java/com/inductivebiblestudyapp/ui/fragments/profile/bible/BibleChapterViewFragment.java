package com.inductivebiblestudyapp.ui.fragments.profile.bible;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.AppCache.OnCacheUpdateListener;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager.OnBillingStateListener;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.loaders.BibleChapterViewLoader;
import com.inductivebiblestudyapp.data.loaders.ChapterThemeEditAsyncLoader;
import com.inductivebiblestudyapp.data.loaders.DivisionThemeEditAsyncLoader;
import com.inductivebiblestudyapp.data.loaders.MarkingApplyAsyncLoader;
import com.inductivebiblestudyapp.data.loaders.NoteEditAsyncLoader;
import com.inductivebiblestudyapp.data.loaders.SimpleContentAsyncLoader;
import com.inductivebiblestudyapp.data.model.ChapterTheme;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.data.model.DivisionTheme;
import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.data.model.ImageListResponse;
import com.inductivebiblestudyapp.data.model.LetteringItem;
import com.inductivebiblestudyapp.data.model.LetteringListResponse;
import com.inductivebiblestudyapp.data.model.MarkingItem;
import com.inductivebiblestudyapp.data.model.Note;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse.Chapter;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse.Verse;
import com.inductivebiblestudyapp.ui.OnUpgradeRequestListener;
import com.inductivebiblestudyapp.ui.activities.share.ShareActivity;
import com.inductivebiblestudyapp.ui.dialogs.CustomMarkingsTooltip;
import com.inductivebiblestudyapp.ui.dialogs.ImageMarkingDialog;
import com.inductivebiblestudyapp.ui.dialogs.SimpleSaveDialog;
import com.inductivebiblestudyapp.ui.dialogs.SimpleYesNoDialog;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.ChapterOverlayManager.ImageMarkingWrapper;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.ChapterOverlayManager.NoteWrapper.OnNoteClickListener;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.ChapterOverlayManager.OnOverlayUpdateListener;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.ChapterSpanBuilder.OnPositionFoundListener;
import com.inductivebiblestudyapp.ui.style.span.CustomMovementMethod;
import com.inductivebiblestudyapp.ui.style.span.HideParagraphSpan;
import com.inductivebiblestudyapp.util.DialogStateHolder;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A simple {@link Fragment} subclass. Use the {@link BibleChapterViewFragment#newInstance}
 * factory method to create an instance of this fragment.
 * 
 * @version 0.22.0-20150921
 */
public class BibleChapterViewFragment extends Fragment 
	implements OnClickListener, OnMenuItemClickListener, ActionMode.Callback, 
	LoaderManager.LoaderCallbacks<ContentResponse>, OnTouchListener,
	BibleChapterViewLoader.OnResponseListener, 
	CustomMarkingsTooltip.OnMarkingSetListener, OnOverlayUpdateListener, 
	OnNoteClickListener, ChapterSpanBuilder.OnVerseInteractionListener, OnPositionFoundListener {
		
	final static private String CLASS_NAME = BibleChapterViewFragment.class
			.getSimpleName();
	
	private static final String ARG_CONTENT_STUB = CLASS_NAME + ".ARG_CONTENT_STUB";
	
	private static final String ARG_CHAPTER_ID = CLASS_NAME + ".ARG_CHAPTER_ID";
	
	private static final String ARG_SEEK_ID = CLASS_NAME + ".ARG_SEEK_ID";
	private static final String ARG_SEEK_TYPE = CLASS_NAME + ".ARG_SEEK_TYPE";
	
	private static final String ARG_DISABLE_NEXT_PREV = CLASS_NAME + ".ARG_DISABLE_NEXT_PREV";
	
	private static final String ARG_CHAPTER_ITEM = CLASS_NAME + ".ARG_CHAPTER_ITEM";
	
	private static final String KEY_CURRENT_VERSE = CLASS_NAME + ".KEY_CURRENT_VERSE";
	private static final String KEY_CHAPTER_THEME = CLASS_NAME + ".KEY_CHAPTER_THEME";
	
	private static final String KEY_SELECTION_START = CLASS_NAME + ".KEY_SELECTION_START";
	private static final String KEY_SELECTION_END = CLASS_NAME + ".KEY_SELECTION_END";
	
	private static final String KEY_SCROLL_VISIBLE_OFFSET = CLASS_NAME + ".KEY_SCROLL_VISIBLE_OFFSET";
	
	/** Share pending a save. */
	private static final String KEY_PENDING_SHARE_TEXT = CLASS_NAME + ".KEY_PENDING_SHARE";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End args
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static final String TAG_CUSTOM_MARKINGS_TOOLTIP_DIALOGFRAG = 
			CLASS_NAME + ".TAG_CUSTOM_MARKINGS_TOOLTIP_DIALOGFRAG";
	
	private static final String TAG_NOTE_DIALOG = CLASS_NAME + ".TAG_NOTE_DIALOG";
	private static final String TAG_DIVTHEME_DIALOG = CLASS_NAME + ".TAG_DIVTHEME_DIALOG";
	private static final String TAG_CHAPTER_THEME_DIALOG = CLASS_NAME + ".TAG_THEME_DIALOG";
	
	private static final String TAG_REMOVE_NOTE_DIALOG = CLASS_NAME + ".TAG_REMOVE_NOTE_DIALOG";
	private static final String TAG_REMOVE_DIVTHEME_DIALOG = CLASS_NAME + ".TAG_REMOVE_DIVTHEME_DIALOG";
	private static final String TAG_REMOVE_THEME_DIALOG = CLASS_NAME + ".TAG_REMOVE_THEME_DIALOG";
	
	private static final String TAG_DISPLAY_IMAGE_MARKING = CLASS_NAME + ".TAG_DISPLAY_IMAGE_MARKING";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End dialog tags
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static final int REQUEST_DELETE_NOTE = 0x0;
	private static final int REQUEST_DELETE_DIVTHEME = 0x1;
	
	private static final int REQUEST_DELETE_THEME = 0x2;
	
	private static final int REQUEST_UPDATE_NOTE = 0x3;
	private static final int REQUEST_UPDATE_DIVISION_THEME = 0x4;
	private static final int REQUEST_UPDATE_CHAPTER_THEME = 0x5;
	
	private static final int REQUEST_CREATE_MARKINGS = 0x6;
	
	private static final int REQUEST_FETCH_CURRENT_CHAPTER_CONTENT = 0x10;
	private static final int REQUEST_FETCH_NEXT_CHAPTER_CONTENT = 0x11;
	private static final int REQUEST_FETCH_PREV_CHAPTER_CONTENT = 0x12;
	
	/** The request code for all things {@link ImageMarkingDialog}. */
	private static final int REQUEST_DISPLAY_IMAGE_MARKING = 0x101;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End request ids
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final int TYPE_NONE = 0;
	public static final int TYPE_CHAPTER_THEME = TYPE_NONE;
	public static final int TYPE_NOTE = 1;
	public static final int TYPE_DIV_THEME = 2;
	public static final int TYPE_MARKING = 3;
	public static final int TYPE_VERSE = 4;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 */
	public static BibleChapterViewFragment newInstance(String contentStub, Chapter chapter) {
		BibleChapterViewFragment fragment = new BibleChapterViewFragment();
		Bundle args = new Bundle();
		
		AppCache.addChapterData(chapter.getChapterId(), chapter);
		
		args.putString(ARG_CONTENT_STUB, contentStub);
		args.putParcelable(ARG_CHAPTER_ITEM, chapter);
		
		fragment.setArguments(args);
		return fragment;
	}
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param contentStub The stub used for the confirmation dialogs in share
	 * @param chapterId The chapter to load
	 * @param id The id of the element to scroll to, can be <code>null</code>
	 * @param type The type of the item to scroll to.
	 * @return
	 */
	public static BibleChapterViewFragment newInstance(String contentStub, String chapterId, String id, int type) {
		return newInstance(contentStub, chapterId, id, type, false);
	}
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param contentStub The stub used for the confirmation dialogs in share
	 * @param chapterId The chapter to load
	 * @param id The id of the element to scroll to, can be <code>null</code>
	 * @param type The type of the item to scroll to.
	 * @param enableNextPrev <code>true</code> to enable next & previous buttons, <code>false</code>
	 * to disable.
	 * @return
	 */
	public static BibleChapterViewFragment newInstance(String contentStub, String chapterId, String id, int type, boolean enableNextPrev) {
		BibleChapterViewFragment fragment = new BibleChapterViewFragment();
		Bundle args = new Bundle();
		args.putString(ARG_CONTENT_STUB, contentStub);
		args.putString(ARG_CHAPTER_ID, chapterId);
		
		args.putString(ARG_SEEK_ID, id);
		args.putInt(ARG_SEEK_TYPE, type);
		
		args.putBoolean(ARG_DISABLE_NEXT_PREV, !enableNextPrev);
		
		fragment.setArguments(args);
		return fragment;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factory methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	public BibleChapterViewFragment() {
		// Required empty public constructor
	}
	
	private OnInteractionListener mListener = null;
	
	private ActionMode mActionMode = null;	
	
	private View mNextButton = null;
	private View mPrevButton = null;	
	private View mShareButton = null;
	private View mDeleteButton = null;	
	
	private TextView mChapterTitleTheme = null;
	private TextView mChapterVerseContent = null;
	private TextView mCopyrightText = null;
	private View mPopupAnchor = null;
	
	private ScrollView mScrollView = null;
	
	private PopupMenu mCurrentPopup = null;	
	
	private SimpleSaveDialog mNoteDialog = null;
	private SimpleSaveDialog mDivThemeDialog = null;
	private SimpleSaveDialog mChapterThemeDialog = null;	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The share note/div. theme text that was being saved, pending a share. */
	private String mPendingShareText = "";
	
	/** The stub used in content fetches. */
	private String mContentStub = null;
	
	private boolean mIsCurrentChapterLoaded = false;
	/** Set in {@link #setChapterVerseContent(List)}. */
	private boolean mIsChapterVerseContentUpdating = false;
	
	private String mChapterName = null;
	private String mChapterId = null;
	
	/** Set as argument in {@link #newInstance(String, String, String, int)} */
	private int mSeekType = TYPE_CHAPTER_THEME;
	private String mSeekId = "";
	/** Default is -1. The index to find and show a note/theme/marking. */
	private int mSeekIndex = -1;
	
	private boolean mIsNextPrevDisabled = false;
	
	/** The starting point of the text selection. */	
	private int mSelectedStart = -1; 
	/** The end point of the text selection. */
	private int mSelectedEnd = -1;	
	
	/** The line number. */
	private int mScrollLineVisibleOffset = 0;
	
	private final List<SpannableString> mContentSpanList = new ArrayList<SpannableString>();
	
	private Chapter mChapterItem = null;
		
	/** Set in {@link #onVerseClick(String)} && {@link #onLetteringClick(View, Verse, MarkingItem)} */
	private Verse mCurrentVerse = null;
	/** Set in {@link #onLetteringClick(View, Verse, MarkingItem)} */
	private MarkingItem mCurrentLetteringMarking = null;
	
	/** Can be <code>null</code>/empty. Set in {@link #onBibleResponse(BibleVerseResponse)}. */
	private ChapterTheme mChapterTheme = null;
	
	/** List of the chapter's verses. Set in {@link #onBibleResponse(BibleVerseResponse)}.
	 * Should be parallel to {@link #mVerseIndexStarts}. */
	private Verse[] mVersesList = new Verse[0];
	
	/** Set in {@link #onBibleResponse(BibleVerseResponse)} */
	private final List<Integer> mVerseIndexStarts = new ArrayList<Integer>(); 
	
	/** Keep a local cache of them for when we need to use/reuse them.
	 * Key:  activity/loader request keys, Value: dialog state holder */
	private SparseArray<DialogStateHolder> mRequestCodeToDialogStateMap =  new SparseArray<DialogStateHolder>();
	private SparseArray<String> mRequestCodeToDialogTagMap =  new SparseArray<String>();
	
	
	private ChapterOverlayManager mOverlayManager = null;
	private ChapterSpanBuilder mChapterSpanBuilder = null; 
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_CURRENT_VERSE, mCurrentVerse);
		outState.putParcelable(KEY_CHAPTER_THEME, mChapterTheme);
		
		outState.putInt(KEY_SELECTION_START, mSelectedStart);
		outState.putInt(KEY_SELECTION_END, mSelectedEnd);
				
		outState.putParcelable(ARG_CHAPTER_ITEM, mChapterItem);
		outState.putString(ARG_CHAPTER_ID, mChapterId);
		
		outState.putString(KEY_PENDING_SHARE_TEXT, mPendingShareText);
		
		if (mScrollView != null && mChapterVerseContent != null) {
			calculateVisibleOffset();
		    outState.putInt(KEY_SCROLL_VISIBLE_OFFSET, mScrollLineVisibleOffset);
		}
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		

		Bundle args = getArguments();		
		if (args != null) {
			mContentStub = args.getString(ARG_CONTENT_STUB);
			mIsNextPrevDisabled = args.getBoolean(ARG_DISABLE_NEXT_PREV, false);
		}
		
		if (savedInstanceState != null) {
			mCurrentVerse = savedInstanceState.getParcelable(KEY_CURRENT_VERSE);
			mChapterTheme = savedInstanceState.getParcelable(KEY_CHAPTER_THEME);

			parseArgs(savedInstanceState);
			
			mSelectedStart = savedInstanceState.getInt(KEY_SELECTION_START);
			mSelectedEnd = savedInstanceState.getInt(KEY_SELECTION_END);
			mScrollLineVisibleOffset = savedInstanceState.getInt(KEY_SCROLL_VISIBLE_OFFSET, mScrollLineVisibleOffset);
			
			mPendingShareText = savedInstanceState.getString(KEY_PENDING_SHARE_TEXT, "");
		}
		
		mRequestCodeToDialogStateMap.put(REQUEST_DELETE_NOTE, new DialogStateHolder());
		mRequestCodeToDialogStateMap.put(REQUEST_DELETE_DIVTHEME, new DialogStateHolder());
		mRequestCodeToDialogStateMap.put(REQUEST_DELETE_THEME, new DialogStateHolder());
		
		mRequestCodeToDialogTagMap.put(REQUEST_DELETE_NOTE, TAG_REMOVE_NOTE_DIALOG);
		mRequestCodeToDialogTagMap.put(REQUEST_DELETE_THEME, TAG_REMOVE_THEME_DIALOG);
		mRequestCodeToDialogTagMap.put(REQUEST_DELETE_DIVTHEME, TAG_REMOVE_DIVTHEME_DIALOG);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View  rootView = inflater.inflate(R.layout.fragment_bible_chapter_view, container, false);

		Bundle args = getArguments();
		
		if (savedInstanceState != null) {
			parseArgs(savedInstanceState);
		} else if (args != null) {
			parseArgs(args);
		}

		initViews(rootView);
		
		//fetch the delete dialogs				
		LoaderManager loaderManager = getLoaderManager();		
		loaderManager.initLoader(REQUEST_DELETE_NOTE, null, this);
		loaderManager.initLoader(REQUEST_DELETE_THEME, null, this);
		loaderManager.initLoader(REQUEST_DELETE_DIVTHEME, null, this);

		//set chapter theme, which should be empty & thus just set the title.
		setChapterThemeAndCopyright();
		setPrevNextButtons(false);
		
		checkAndResetDialogsAndLoaders();				
		updateVerseView();
		
		AppCache.addBibleVerseUpdateListener(mChapterUpdateListener);
		//lettering and image listeners are only added if the custom markings dialog is not found
		
		return rootView;
	}


	
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();		
		if (mCurrentPopup != null) {
			mCurrentPopup.dismiss();
			mCurrentPopup = null;
		}
		if (mOverlayManager != null) {
			mOverlayManager.recycleBitmaps();
		}
		AppCache.removeImageListUpdateListener(mImageUpdateListener);
		AppCache.removeLetteringListUpdateListener(mLetteringUpdateListener);
		AppCache.removeBibleVerseUpdateListener(mChapterUpdateListener);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		DialogFragment frag = (DialogFragment) getFragmentManager().findFragmentByTag(TAG_CUSTOM_MARKINGS_TOOLTIP_DIALOGFRAG);
		if (frag != null && frag.getDialog() != null && frag.getDialog().isShowing()) { 
			//this dialog is in the foreground, everyone else can wait.
			frag.onActivityResult(requestCode, resultCode, data);
			return;
		}
		
		final String accessToken = new CurrentUser(getActivity()).getIBSAccessToken();
		String tag = "";
		
		switch (requestCode) {
		case REQUEST_DISPLAY_IMAGE_MARKING:
			if (ImageMarkingDialog.RESULT_DELETE_SUCCESS == resultCode) {
				forceChapterVerseReload();				
			}
			return; //no dialog to dismiss
		
		case REQUEST_DELETE_NOTE:
			if (DialogInterface.BUTTON_POSITIVE == resultCode) {
				if (mCurrentVerse != null) {			
					RestClient.getInstance().getNoteEditService()
									.delete(accessToken, 
											mCurrentVerse.getNote().getNoteId(), 
											createDeleteCallback(
													TAG_REMOVE_NOTE_DIALOG, 
													TAG_NOTE_DIALOG,
													R.string.ibs_text_deleteSuccess_note)
											);

					return; //we do not want to dismiss yet
				}
			}

			tag = TAG_REMOVE_NOTE_DIALOG;
			break;
			
		case REQUEST_DELETE_DIVTHEME:
			if (DialogInterface.BUTTON_POSITIVE == resultCode) {
				if (mCurrentVerse != null) {			
					RestClient.getInstance().getDivisionThemeEditService()
									.delete(accessToken, 
											mCurrentVerse.getDivisionTheme().getDivisionThemeId(), 
											createDeleteCallback(
													TAG_REMOVE_DIVTHEME_DIALOG,
													TAG_DIVTHEME_DIALOG,
													R.string.ibs_text_deleteSuccess_divisionTheme)
											);

					return; //we do not want to dismiss yet
				}
			}

			tag = TAG_REMOVE_DIVTHEME_DIALOG;
			break;
			
		case REQUEST_DELETE_THEME:
			if (DialogInterface.BUTTON_POSITIVE == resultCode) {
				if (mChapterTheme != null) {				
					RestClient.getInstance().getChapterThemeEditService()
									.delete(accessToken, mChapterTheme.getChapterThemeId(), 
											createDeleteCallback(
													TAG_REMOVE_THEME_DIALOG, 
													null,
													R.string.ibs_text_deleteSuccess_chapterTheme)
											);

					return; //we do not want to dismiss yet
				}
			}			
			//we will dismiss this
			tag = TAG_REMOVE_THEME_DIALOG;
			break;

		default:
			break;
		}
		dismissDialog(tag);
		
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
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Initializer methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes all the views. */
	private void initViews(View rootView) {
		mChapterTitleTheme = (TextView) rootView.findViewById(R.id.ibs_bible_chapter_title);		
		mChapterVerseContent = ((TextView) rootView.findViewById(R.id.ibs_bible_chapter_verses));
		mChapterVerseContent.setCustomSelectionActionModeCallback(this);
		
		mCopyrightText = ((TextView) rootView.findViewById(R.id.ibs_bible_chapter_copyright));
		
		
		mCopyrightText = ((TextView) rootView.findViewById(R.id.ibs_bible_chapter_copyright));
		
		/* Disabling the save state prevents both of these: 
		 *   - java.lang.IndexOutOfBoundsException: setSpan (1 ... 0) 
		 *   - java.lang.RuntimeException: bogus span encoding ...
		 *   
		 * during activity restoration after memory cleanup (e.g. killed with task cleaner).
		 * 
		 * The "IndexOutOfBoundsException" is caused by a selection bug, see:
		 *  - https://code.google.com/p/android/issues/detail?id=5164
		 *  
		 * The "RuntimeException: bogus span encoding" is caused by unusual characters 
		 * such as the copyright symbol in some copyright texts. Both are platform bugs.
		 */
		mChapterVerseContent.setSaveFromParentEnabled(false);
		mChapterVerseContent.setSaveEnabled(true);
		mCopyrightText.setSaveFromParentEnabled(false);
		mCopyrightText.setSaveEnabled(true);		
		
		ViewGroup verseContainer = (ViewGroup) rootView.findViewById(R.id.ibs_bible_chapter_verses_container); 
		
		mOverlayManager = new ChapterOverlayManager(mChapterVerseContent, verseContainer, this);
		mChapterSpanBuilder = new ChapterSpanBuilder(mChapterVerseContent, mOverlayManager);
		
		Utility.setOnClickAndReturnView(rootView, R.id.ibs_bible_chapter_button_theme_edit, this);

		mScrollView = (ScrollView) rootView.findViewById(R.id.ibs_bible_chapter_scrollview);
		mPopupAnchor = rootView.findViewById(R.id.ibs_bible_chapter_anchorview_hack);		
		

		mShareButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_bible_chapter_button_share, this);
		mDeleteButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_bible_chapter_button_theme_delete, this);
		
		mNextButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_bible_chapter_button_next_chapter, this);
		mPrevButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_bible_chapter_button_previous_chapter, this);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Parses args and sets the chapter data. */
	private void parseArgs(Bundle args) {
		mChapterName = getString(R.string.ibs_text_loading);
		mChapterId = args.getString(ARG_CHAPTER_ID);
		
		onChapterData((Chapter) args.getParcelable(ARG_CHAPTER_ITEM));
		
		if (args.containsKey(ARG_SEEK_TYPE) && args.containsKey(ARG_SEEK_ID)) { //only load once
			mSeekType = args.getInt(ARG_SEEK_TYPE);
			mSeekId = args.getString(ARG_SEEK_ID);
		}
	}
	
	//NOTE: Remember package visibility is faster than private in member objects
	
	/** Calculates the scrollable visible offset, if possible. */
	private int calculateVisibleOffset() {
		if (mChapterVerseContent.getLayout() != null) {
			final int firstVisibleLine = mChapterVerseContent.getLayout().getLineForVertical(mScrollView.getScrollY());
			mScrollLineVisibleOffset = mChapterVerseContent.getLayout().getLineStart(firstVisibleLine);
		}
		return mScrollLineVisibleOffset;
	}
	
	/**
	 * Launches the share activity with the given title & text
	 * @param title
	 * @param text
	 */
	/*default*/ void launchShareActivity(String title, String text) {
		ShareActivity.launchShare(getActivity(), mContentStub, title, text);
	}
	
	/**
	 * Creates the callback to handle delete operations.
	 * @param tag1 The first dialog to dismiss (can be <code>null</code>).
	 * @param tag2 The second dialog to dismiss (can be <code>null</code>).
	 * @param confirmId The resource id of the confirmation toast
	 * @return
	 */
	/*default */ Callback<UpdateResult> createDeleteCallback(
			@Nullable final String tag1, @Nullable final String tag2, final int confirmId) {
		return new Callback<UpdateResult>() {
			
			@Override
			public void success(UpdateResult response, Response arg1) {
				Log.d(CLASS_NAME, "Success delete!");
				 
				 forceChapterVerseReload();
				 Utility.toastMessage(getActivity(), getString(confirmId));
				 if (tag1 != null) {
					 dismissDialog(tag1);
				 }
				 if (tag2 != null) {
					 dismissDialog(tag2);
				 }
			}
			
			@Override
			public void failure(RetrofitError arg0) {
				Log.d(CLASS_NAME, "Failed to delete");
				
				if (getActivity() != null) {
					Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
					 if (tag1 != null) {
						 dismissDialog(tag1);
					 }
				}
			}
		};
	}

	/*default*/ void dismissDialog(String tag) {
		DialogFragment dialog = (DialogFragment) getFragmentManager().findFragmentByTag(tag);
		if (dialog == null) {
			return;
		}
		dialog.dismiss();
	}
	/** Clears cache, Calculates the scroll, and calls {@link #chapterVerseReload()}
	/*default*/ void forceChapterVerseReload() {
		//prevent loops
		AppCache.removeBibleVerseUpdateListener(mChapterUpdateListener);
			
		AppCache.addBibleVerseResponse(mChapterId, null);
		
		AppCache.addBibleVerseUpdateListener(mChapterUpdateListener);
		
		chapterVerseReload();
	}
	
	/** Calculates the scroll, and re-loads the verse.
	 * Resets the lettering and image list listeners.
	 * */
	/*default*/ void chapterVerseReload() {

		AppCache.addLetteringListUpdateListener(mLetteringUpdateListener);
		AppCache.addImageListUpdateListener(mImageUpdateListener);
		
		if (mChapterVerseContent != null) {
			mChapterVerseContent.post(new Runnable() {				
				@Override
				public void run() {
					if (mScrollView != null) {
						mSeekIndex = -1; //clear seek index
						mSeekId = "";
						calculateVisibleOffset();
					}
					updateVerseView();
				}
			});
		}		 
	}
	
	/**
	 * Fetches the verse, loads it in chunks, and restores scroll. 
	 * To reload the verse entirely see {@link #forceChapterVerseReload()}.
	 */
	private void updateVerseView() {
		//re-attaches any next loaders if in progress.
		LoaderManager lm = getLoaderManager();
		if (Utility.checkIfLoading(lm, REQUEST_FETCH_NEXT_CHAPTER_CONTENT)) {
			lm.initLoader(REQUEST_FETCH_NEXT_CHAPTER_CONTENT, null, mFetchLoader);
			setPrevNextButtons(false);
			
		} else if (Utility.checkIfLoading(lm, REQUEST_FETCH_PREV_CHAPTER_CONTENT)) {
			lm.initLoader(REQUEST_FETCH_PREV_CHAPTER_CONTENT, null, mFetchLoader);
			setPrevNextButtons(false);
			
		} else {
			lm.initLoader(REQUEST_FETCH_CURRENT_CHAPTER_CONTENT, null, mFetchLoader);
		}
		
	}

	/** Hides & shows buttons based on the existence of chapter ids. */
	/*default*/ void setPrevNextButtons(boolean enabled) {
		if (mPrevButton == null || mNextButton == null || mChapterItem == null) {
			return;
		}
		final boolean isPrevHidden = mIsNextPrevDisabled || mChapterItem.getPreviousChapterId().isEmpty();
		final boolean isNextHidden = mIsNextPrevDisabled || mChapterItem.getNextChapterId().isEmpty();
		
		mPrevButton.setVisibility(isPrevHidden ? View.GONE : View.VISIBLE);
		mPrevButton.setEnabled(enabled);
		mNextButton.setVisibility( isNextHidden ? View.GONE : View.VISIBLE);
		mNextButton.setEnabled(enabled);
	}
	
	/*default*/ void setChapterVerseContent(List<SpannableString> spanList) {
		mIsChapterVerseContentUpdating =  true;
		if (spanList.isEmpty()) {
			Log.w(CLASS_NAME, "Unexpected lack of content");
			mChapterVerseContent.setText(getString(R.string.ibs_error_cannotConnect));
		} else {
			/*
			 * To reduce frame drops for large content, text is split into chunks.
			 * Chunks are then applied via post Runnables with a slight delay
			 * to prevent ui freezes.
			 * 
			 * The method is recursive and ends when the last element of the list has been 
			 * visited.
			 */
			//(This might be fixed) fix bug where the text very occasionally changes font size on refresh
			mChapterVerseContent.setText(""); // clear text
			
			mChapterVerseContent.setText(spanList.get(0));
			mOverlayManager.addViews();
			recursiveChapterContentSet(spanList, 1);
		}
		
		mChapterVerseContent.setMovementMethod(new CustomMovementMethod(this));
	}
	
	/** Recursively uses post commands to set the content. */
	private void recursiveChapterContentSet(final List<SpannableString> spanList, final int index) {
		if (mChapterVerseContent == null) {
			mIsChapterVerseContentUpdating = false;
			//woops! Can't do anything!
			return;
		}
		
		if (spanList.size() <= index) {
			mIsChapterVerseContentUpdating = false;
			
			int firstVisibleLineOffset =  0;
			if (mSeekIndex > 0) {
				firstVisibleLineOffset = mChapterVerseContent.getLayout().getLineForOffset(mSeekIndex);
			} else {
				firstVisibleLineOffset =  mChapterVerseContent.getLayout().getLineForOffset(mScrollLineVisibleOffset);
			}
        	final int pixelOffset = mChapterVerseContent.getLayout().getLineTop(firstVisibleLineOffset);
			
			if (mScrollView != null) {
				mScrollView.post(new Runnable() {					
					@Override
					public void run() {
		            	mScrollView.scrollTo(0, pixelOffset);
					}
				});
			}
			mScrollView.invalidate();
			return; //all text applied!
		}
		//pop the first chunk
		final CharSequence content = spanList.get(index); 
		mChapterVerseContent.postDelayed(new Runnable() {			
			@Override
			public void run() { //apply the content
				mChapterVerseContent.append(content);
				mOverlayManager.addViews();
				//move onto the next chunk
				recursiveChapterContentSet(spanList, index + 1);
			}
		}, ChapterSpanBuilder.VERSE_CHUNK_TIME);
	}
	
	/** Sets the chapter theme based on the current {@link #mChapterTheme}.
	 * Assumes {@link #mChapterTitleTheme} & {@link #mDeleteButton} are set. */
	/*default*/ void setChapterThemeAndCopyright() {
		String chapterTitle = "";
		if (mChapterTheme != null && !mChapterTheme.getText().isEmpty()) {
			chapterTitle = getString(R.string.ibs_title_chapterWithTheme, 
									mChapterName, mChapterTheme.getText());
			mDeleteButton.setVisibility(View.VISIBLE);
			mShareButton.setVisibility(View.VISIBLE);
		} else {
			chapterTitle = mChapterName;
			
			mDeleteButton.setVisibility(View.GONE);
			mShareButton.setVisibility(View.GONE);
		}		
		
		mChapterTitleTheme.setText(chapterTitle);		
		
		if (mCopyrightText != null && mChapterItem != null) {
			mCopyrightText.setText(Html.fromHtml(mChapterItem.getCopyright()));
		}
	}
	 
	 /** Attempts to find and set the dialog message, while caching it on this fragment.
	 * @param requestId The dialog  */
	private void getAndSetConfirmMessage(int id, ContentResponse data) {
		
		DialogStateHolder mDialogState = mRequestCodeToDialogStateMap.get(id);
			
		String message = getString(R.string.ibs_error_cannotLoadContent);
		if (data != null) {
			message = data.getContent();
			mDialogState.fetched = true;
		} 

		mDialogState.message = message;
		
		String tag = mRequestCodeToDialogTagMap.get(id);
		
		Fragment frag = getFragmentManager().findFragmentByTag(tag);
		if (frag != null && frag instanceof SimpleYesNoDialog) {
			((SimpleYesNoDialog) frag).updateContent(message);
		}		
	}
	
	/** Finds the dialogs & loaders and re-attaches them and their listeners. */
	private void checkAndResetDialogsAndLoaders() {
		final FragmentManager fm = getFragmentManager();
		mChapterThemeDialog = (SimpleSaveDialog) fm.findFragmentByTag(TAG_CHAPTER_THEME_DIALOG);
		if (mChapterThemeDialog != null) {			
			mChapterThemeDialog.setDialogOnClickListener(mChapterThemeListenr);
			
			checkAndReattachLoader(REQUEST_UPDATE_CHAPTER_THEME, mUpdateLoader);
		}
		
		mDivThemeDialog = (SimpleSaveDialog) fm.findFragmentByTag(TAG_DIVTHEME_DIALOG);
		if (mDivThemeDialog != null) {			
			mDivThemeDialog.setDialogOnClickListener(mDivThemeListener);
			
			checkAndReattachLoader(REQUEST_UPDATE_DIVISION_THEME, mUpdateLoader);
		}
		
		mNoteDialog = (SimpleSaveDialog) fm.findFragmentByTag(TAG_DIVTHEME_DIALOG);
		if (mNoteDialog != null) {
			mNoteDialog.setDialogOnClickListener(mNoteDialogListener);
			
			checkAndReattachLoader(REQUEST_UPDATE_NOTE, mUpdateLoader);
		}
		
		CustomMarkingsTooltip tooltip = 
				(CustomMarkingsTooltip) fm.findFragmentByTag(TAG_CUSTOM_MARKINGS_TOOLTIP_DIALOGFRAG); 
		if (tooltip != null) {
			tooltip.setOnMarkingSetListener(this);
		} else if (checkAndReattachLoader(REQUEST_CREATE_MARKINGS, mApplyMarkingLoader) == false) {
			//only set listeners if there is neither a dialog nor a loader
			AppCache.addImageListUpdateListener(mImageUpdateListener);
			AppCache.addLetteringListUpdateListener(mLetteringUpdateListener);
		}		
	}
	/** <code>true</code> if the loader is found, <code>false</code> if not. */
	private boolean checkAndReattachLoader(int id, LoaderCallbacks<?> callback) {
		Loader<Boolean> loader = getLoaderManager().getLoader(id);
		if (loader != null) { //reattach loader
			getLoaderManager().initLoader(id, null, callback);
			return true;
		}
		return false;
	}
	
	/** Toasts "Changes Saved" and dismisses dialog if not null */
	/*default*/ void changesSave(DialogFragment dialog) {
		Utility.toastMessage(getActivity(), getString(R.string.ibs_text_changesSaved));
		if (dialog != null) {
			dialog.dismiss();
		}
	}
	

	/**
	 * Shares the text, using verse as title, and dismisses the dialog.
	 * @param text
	 * @param tag
	 */
	/*package*/ void shareVerseText(final String text, final String tag) {
		final String title = mChapterItem.getName() + ":" + mCurrentVerse.getNumber();
		launchShareActivity(title, text);
		dismissDialog(tag);
	}
	
	
	/** Updates the popup menu to change "add" to "edit" for notes & div themes, 
	 * based on the current verse. */
	/*default*/ void updatePopMenu(PopupMenu popup) {
		if (mCurrentVerse == null) {
			return;
		}
		if (mCurrentVerse.hasNote()) { //we have a note, change to edit note
			popup.getMenu().findItem(R.id.addedit_note).setTitle(R.string.ibs_title_edit_note); 
		} else {
			popup.getMenu().findItem(R.id.addedit_note).setTitle(R.string.ibs_title_add_note);
		}
		
		if (mCurrentVerse.hasDivisionTheme()) {  //we have a div theme, change to add div theme
			popup.getMenu().findItem(R.id.addedit_divTheme).setTitle(R.string.ibs_title_edit_divTheme); 
		} else {
			popup.getMenu().findItem(R.id.addedit_divTheme).setTitle(R.string.ibs_title_add_divTheme);
		}
	}
	
	/** Trims the hidden paragraphs from sharing/copy output. */
	/*package*/ static String trimHiddenParagraphs(TextView chapterVerseContent, int start, int end) {		
		final Spanned fullText = (Spanned) chapterVerseContent.getText();			
		String selectedText = fullText.toString().substring(start, end);
		
		HideParagraphSpan[] spans = ((Spanned) chapterVerseContent.getText()).getSpans(start, end, HideParagraphSpan.class);
		for (HideParagraphSpan hideParagraphSpan : spans) {
			final int spanStart = fullText.getSpanStart(hideParagraphSpan);
			final int spanEnd = fullText.getSpanEnd(hideParagraphSpan);
			
			if (spanStart < 0 || spanEnd < 0 || start < 0) {
				continue; //not found, skip
			}
			
			if (spanStart - start > 0) {
				selectedText = selectedText.substring(0, spanStart-start);
			}
			selectedText = selectedText.substring(spanEnd-start);
			
			if (start == spanStart) {
				start = spanEnd;
			}
		}
		return selectedText.trim();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Action Mode callback
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}
	
	@Override
	public void onDestroyActionMode(ActionMode mode) {
		mActionMode = null;
	}
	
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		if (mActionMode == null) {
			mActionMode = mode;
		}
		menu.removeItem(android.R.id.selectAll);
			
		mode.getMenuInflater().inflate(R.menu.selection_context_menu, menu);
		
		return true;
	}
	
	
	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		int start = 0;
		int end = 0;
		String text = "";
		if (mChapterVerseContent != null) {
			start =  mChapterVerseContent.getSelectionStart();
			end = mChapterVerseContent.getSelectionEnd();
			
			final Spanned fullText = (Spanned) mChapterVerseContent.getText();			
			text = fullText.toString().substring(start, end);
			
			HideParagraphSpan[] spans = ((Spanned) mChapterVerseContent.getText()).getSpans(start, end, HideParagraphSpan.class);
			for (HideParagraphSpan hideParagraphSpan : spans) {
				final int spanStart = fullText.getSpanStart(hideParagraphSpan);
				final int spanEnd = fullText.getSpanEnd(hideParagraphSpan);
				
				if (spanStart < 0 || spanEnd < 0 || start < 0) {
					continue; //not found, skip
				}
				
				if (spanStart - start > 0) {
					text = text.substring(0, spanStart-start);
				}
				text = text.substring(spanEnd-start);
				
				if (start == spanStart) {
					start = spanEnd; //skip that index.
				}
			}
			
		}
		switch (item.getItemId()){
		case R.id.select_custom_marking:
			if (mChapterVerseContent != null) {
				
				mSelectedStart = start;
				mSelectedEnd = end;
				
				if (mSelectedEnd - mSelectedStart > 0) { //if non-empty selection
					CustomMarkingsTooltip tooltip = CustomMarkingsTooltip.newInstance(mChapterVerseContent);
					tooltip.setOnMarkingSetListener(this);
					tooltip.show(getFragmentManager(), TAG_CUSTOM_MARKINGS_TOOLTIP_DIALOGFRAG);

					//remove to prevent double updates
					AppCache.removeImageListUpdateListener(mImageUpdateListener);
					AppCache.removeLetteringListUpdateListener(mLetteringUpdateListener);
				}
				
			}
			Log.d(CLASS_NAME, "select_custom_marking");
			
			if (mActionMode != null) {
				mActionMode.finish();
			}
			return true;
			
		case android.R.id.copy:
			//manual copy to avoid exceptions & handle text manipulation
			Utility.copyToClipboard(getActivity(), "chapter-selection", text);
			if (mActionMode != null) {
				mActionMode.finish();
			}
			return true;
			
			default:
		}				
		
		return false;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Loader listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Used to add/edit notes. */
	private LoaderManager.LoaderCallbacks<UpdateResult> mApplyMarkingLoader = new LoaderManager.LoaderCallbacks<UpdateResult>(){
			@Override
			public Loader<UpdateResult> onCreateLoader(int id, Bundle args) {
				if (args == null) {
					return null;
				}
				if (mSelectedEnd < 0 || mSelectedStart < 0 ) {
					Log.w(CLASS_NAME, "Create Marking - One or more of the required values are invalid");
					return null;
				}
				
				//First we must determine verse range
				final int SIZE = mVerseIndexStarts.size();
				String endVerseNumber = "";
				String startVerseNumber = "";
				
				if (SIZE != mVersesList.length) {
					Log.w(CLASS_NAME, "Warning! Mismatched verse & index lengths!");
				}
				
				for (int index = SIZE - 1; index >= 0; index--) {
					final int verseStart = mVerseIndexStarts.get(index);
					if (mSelectedEnd >= verseStart && endVerseNumber.isEmpty()) { 
						//we have found our end verse
						endVerseNumber = mVersesList[index].getNumber();
					}
					if (mSelectedStart >= verseStart && startVerseNumber.isEmpty()) {
						//we have found our starting verse 
						startVerseNumber = mVersesList[index].getNumber();
						break; //we have populated a start and end
					}
				}
				
				String verseRange = "";
				if (startVerseNumber.isEmpty() || endVerseNumber.isEmpty()) {
					Log.w(CLASS_NAME, "Create Marking - Cannot determine verse range");
					return null;
				} else if (startVerseNumber.equals(endVerseNumber)) {
					//takes start:"5" and end:"5" --> 5
					verseRange = startVerseNumber;
				} else {
					//takes start:"1-2" + end:"4-5" --> "1-5"
					// or start:"11" + end:"12" --> "11-12"
					verseRange = startVerseNumber.replaceAll("\\-\\d+", "") + "-" + 
								endVerseNumber.replaceAll("\\d+\\-", "");
				}
				
				return new MarkingApplyAsyncLoader(getActivity(), args, mChapterId, mSelectedStart, mSelectedEnd, verseRange);
			}
				
			
			@Override
			public void onLoaderReset(Loader<UpdateResult> loader) {}
			
			 @Override
			public void onLoadFinished(Loader<UpdateResult> loader,
					UpdateResult data) {
				 final int id = loader.getId();
				 
				 mSelectedStart = mSelectedEnd = -1; 
				 				 
				 //makes it easier to tell if we are still loading
				 getLoaderManager().destroyLoader(id);
				
				//prevent redundant updates
				AppCache.removeLetteringListUpdateListener(mLetteringUpdateListener);
				AppCache.removeImageListUpdateListener(mImageUpdateListener);
				 
				// clear cache for "most recently used" data
				AppCache.setLetteringListResponse(null); 
				AppCache.setImageListResponse(null); 
				 
				if (data != null && data.isSuccessful()) {						
					forceChapterVerseReload();
					Utility.toastMessage(getActivity(), getString(R.string.ibs_text_changesSaved));
				} else {
					Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
				}
			}
	};
	
	private LoaderManager.LoaderCallbacks<List<SpannableString>> mFetchLoader = new LoaderManager.LoaderCallbacks<List<SpannableString>>() {		
		@Override
		public void onLoaderReset(Loader<List<SpannableString>> arg0) {}		
		@Override
		public void onLoadFinished(Loader<List<SpannableString>> loader, List<SpannableString> spanList) {
			final int id = loader.getId();			 
			//makes it easier to tell if we are still loading
			getLoaderManager().destroyLoader(id);
			
			if (spanList == null) {
				mChapterVerseContent.post(new Runnable() {		//run on ui thread.	
					@Override
					public void run() {
						setPrevNextButtons(true);
						if (mIsCurrentChapterLoaded) {
							mIsCurrentChapterLoaded = false;
							updateVerseView();
						}
					}
				});
				Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
				
				if (!mIsCurrentChapterLoaded) {
					spanList = new ArrayList<SpannableString>();			
					spanList.add( new SpannableString(getString(R.string.ibs_error_cannotConnect)) );
				}
			}
			final List<SpannableString> spanList2 = spanList;
			
			if (mChapterVerseContent == null) {
				return;
			}		
			mChapterVerseContent.post(new Runnable() {		//run on ui thread.	
				@Override
				public void run() {
					mIsCurrentChapterLoaded = true;
					setPrevNextButtons(true);
					setChapterThemeAndCopyright();
					
					mContentSpanList.clear();
					mContentSpanList.addAll(spanList2);
					setChapterVerseContent(spanList2); 
				}
			});
		}
		
		@Override
		public Loader<List<SpannableString>> onCreateLoader(int which, Bundle args) {
			switch (which) {
			case REQUEST_FETCH_CURRENT_CHAPTER_CONTENT:
				return new BibleChapterViewLoader(getActivity(), mChapterId, BibleChapterViewFragment.this);
				
			case REQUEST_FETCH_NEXT_CHAPTER_CONTENT:
				final String nextId = mChapterItem.getNextChapterId();
				if (nextId.isEmpty()) {
					return null;
				}
				return new BibleChapterViewLoader(getActivity(), nextId, BibleChapterViewFragment.this);
				
			case REQUEST_FETCH_PREV_CHAPTER_CONTENT:
				final String prevId = mChapterItem.getPreviousChapterId();
				if (prevId.isEmpty()) {
					return null;
				}
				return new BibleChapterViewLoader(getActivity(), prevId, BibleChapterViewFragment.this);

			default:
				throw new UnsupportedOperationException("This request is not recognised: " + which);
			}
		}
	};
	
	/** Used to add/edit notes. */
	private LoaderManager.LoaderCallbacks<Boolean> mUpdateLoader = new LoaderManager.LoaderCallbacks<Boolean>(){
			@Override
			public Loader<Boolean> onCreateLoader(int id, Bundle args) {
				if (args == null) {
					return null;
				}
				switch (id) {
				case REQUEST_UPDATE_NOTE:
					return new NoteEditAsyncLoader(getActivity(), args);					
					
				case REQUEST_UPDATE_DIVISION_THEME:
					return new DivisionThemeEditAsyncLoader(getActivity(), args);
					
				case REQUEST_UPDATE_CHAPTER_THEME:
					return new ChapterThemeEditAsyncLoader(getActivity(), args);

				default:
					throw new UnsupportedOperationException("That request is invalid: " + id);
				}
			}
				
			
			@Override
			public void onLoaderReset(Loader<Boolean> loader) {}
			
			 @Override
			public void onLoadFinished(Loader<Boolean> loader,
					Boolean data) {
				 final int id = loader.getId();
				 
				 //makes it easier to tell if we are still loading
				 getLoaderManager().destroyLoader(id);
				 
				 if (data != null && data){
					 
					 forceChapterVerseReload();
				 }
				
				 switch (id) {
					case REQUEST_UPDATE_NOTE:
						 if (mNoteDialog != null) {
							 if (data != null && data) {
								 changesSave(mNoteDialog);
								 
								 if (!TextUtils.isEmpty(mPendingShareText)) {
									 shareVerseText(mPendingShareText, TAG_NOTE_DIALOG);
								 }
								 
							 } else {
								 mNoteDialog.setInputError(getString(R.string.ibs_error_cannotConnect));
							 }
						 }
						 mPendingShareText = "";
						 break;
					 
					case REQUEST_UPDATE_DIVISION_THEME:
						if (mDivThemeDialog != null) {
							 if (data != null && data) {
								 changesSave(mDivThemeDialog);
								 
								 if (!TextUtils.isEmpty(mPendingShareText)) {
									 shareVerseText(mPendingShareText, TAG_DIVTHEME_DIALOG);
								 }
								 
							 } else {
								 mDivThemeDialog.setInputError(getString(R.string.ibs_error_cannotConnect));
							 }
						}
						mPendingShareText = "";
						break;
						
					case REQUEST_UPDATE_CHAPTER_THEME:
						if (mChapterThemeDialog != null) {
							 if (data != null && data) {
								 changesSave(mChapterThemeDialog);
								 AppCache.addBibleChapterResponse(mChapterItem.getParentBookId(), null);
							 } else {
								 mChapterThemeDialog.setInputError(getString(R.string.ibs_error_cannotConnect));
							 }
						 }
						break;
				 }
			}
	};

	
	@Override
	public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case REQUEST_DELETE_NOTE:
			return new SimpleContentAsyncLoader(
					getActivity(), 
					mContentStub + getString(R.string.ibs_config_load_removeNotes));
			
		case REQUEST_DELETE_THEME:
			return new SimpleContentAsyncLoader(
					getActivity(), 
					mContentStub + getString(R.string.ibs_config_load_removeTheme));
			
		case REQUEST_DELETE_DIVTHEME:
			return new SimpleContentAsyncLoader(
					getActivity(), 
					mContentStub + getString(R.string.ibs_config_load_removeDivTheme));
		default:
			throw new UnsupportedOperationException("Id is not recognized? " + id);
		}
		
	}
	
	@Override
	public void onLoaderReset(Loader<ContentResponse> loader) {
		// nothing to do yet.
		
	}
	
	 @Override
	public void onLoadFinished(Loader<ContentResponse> loader,
			ContentResponse data) {
		 final int id = loader.getId();
		 
		 //makes it easier to tell if we are still loading
		 getLoaderManager().destroyLoader(id); 
		 switch (id) {
		 case REQUEST_DELETE_NOTE:
		 case REQUEST_DELETE_THEME:
		 case REQUEST_DELETE_DIVTHEME:
			 getAndSetConfirmMessage(id, data);
		 }
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// OnClick listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			int mTouchX = (int) event.getX();
			int mTouchY = (int) event.getY();			

			int scrollY = mScrollView.getScrollY();
					
			MarginLayoutParams params = (MarginLayoutParams) mPopupAnchor.getLayoutParams();
			params.setMargins(mTouchX, mTouchY - scrollY, 0, 0);
			mPopupAnchor.requestLayout();			
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.ibs_bible_chapter_button_next_chapter:
			mScrollLineVisibleOffset = 0;
			getLoaderManager().initLoader(REQUEST_FETCH_NEXT_CHAPTER_CONTENT, null, mFetchLoader);
			mChapterVerseContent.setText(R.string.ibs_text_loading);
			mOverlayManager.clearAll();
			setPrevNextButtons(false);
			break;
			
		case R.id.ibs_bible_chapter_button_previous_chapter:
			mScrollLineVisibleOffset = 0;
			getLoaderManager().initLoader(REQUEST_FETCH_PREV_CHAPTER_CONTENT, null, mFetchLoader);
			mChapterVerseContent.setText(R.string.ibs_text_loading);
			mOverlayManager.clearAll();
			setPrevNextButtons(false);
			break;
		
		case R.id.ibs_bible_chapter_button_theme_edit:
			if (mChapterTheme == null) {
				mChapterThemeDialog = SimpleSaveDialog.newInstance(getString(R.string.ibs_title_add_chapterTheme), false);
			} else {
				mChapterThemeDialog = 
						SimpleSaveDialog.newInstance(
								getString(R.string.ibs_title_edit_chapterTheme), 
								mChapterTheme.getText(), false);
			}
			mChapterThemeDialog.setDialogOnClickListener(mChapterThemeListenr);
			mChapterThemeDialog.show(getFragmentManager(), TAG_CHAPTER_THEME_DIALOG);
			break;
			
		case R.id.ibs_bible_chapter_button_share:
			launchShareActivity(mChapterItem.getName(), mChapterTheme.getText());
			break;
			
		case R.id.ibs_bible_chapter_button_theme_delete:
			SimpleYesNoDialog dialog = SimpleYesNoDialog.newInstance(
					mRequestCodeToDialogStateMap.get(REQUEST_DELETE_THEME).message, true
					);
			dialog.setTargetFragment(this, REQUEST_DELETE_THEME);
			dialog.show(getFragmentManager(), TAG_REMOVE_THEME_DIALOG);
			break;
		}
	}
	
	
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		
		switch (item.getItemId()){
		
		case R.id.share_lettering: 
			if (mCurrentLetteringMarking.getMarkingType() == MarkingItem.TYPE_LETTERING) {
				String text = mChapterVerseContent.getText().toString();
				final int start = mCurrentLetteringMarking.getStartIndex();
				final int end = mCurrentLetteringMarking.getEndIndex();
				
				String title = mChapterItem.getName() + ":" + mCurrentLetteringMarking.getVerseRange();
				text = trimHiddenParagraphs(mChapterVerseContent, start, end);
				
				launchShareActivity(title, text);
			}
			break;
			
		case R.id.delete_lettering:
			final String accessToken = new CurrentUser(getActivity()).getIBSAccessToken();
			
			if (mCurrentLetteringMarking.getMarkingType() == MarkingItem.TYPE_LETTERING) {
				RestClient.getInstance().getMarkingService()
					.delete(
						accessToken, 
						mCurrentLetteringMarking.getId(), 
						createDeleteCallback(null, null, R.string.ibs_text_deleteSuccess_letteringMarking)
								);
			}
			break;

		case R.id.addedit_note: 
			if (mCurrentVerse.hasNote()) {
				mNoteDialog = SimpleSaveDialog.newInstance(
						getString(R.string.ibs_title_edit_note), 
						mCurrentVerse.getNote().getText());
			} else {
				mNoteDialog = SimpleSaveDialog.newInstance(getString(R.string.ibs_title_add_note), false);
			}
			mNoteDialog.setDialogOnClickListener(mNoteDialogListener);
			mNoteDialog.show(getFragmentManager(), TAG_NOTE_DIALOG);
			break;
			
		case R.id.addedit_divTheme:
			final boolean upgradeComplete = UpgradeBillingManager.isUpgradeComplete(getActivity()); 
			if (upgradeComplete) {
			
				if (mCurrentVerse.hasDivisionTheme()) {
					mDivThemeDialog = SimpleSaveDialog.newInstance(
							getString(R.string.ibs_title_edit_divTheme), 
							mCurrentVerse.getDivisionTheme().getText());
				} else {
					mDivThemeDialog = SimpleSaveDialog.newInstance(getString(R.string.ibs_title_add_divTheme), false);
				}
				mDivThemeDialog.setDialogOnClickListener(mDivThemeListener);
				mDivThemeDialog.show(getFragmentManager(), TAG_DIVTHEME_DIALOG);
			} else {
				((OnUpgradeRequestListener) getActivity()).requestUpgrade();
			}
			break;
			
		case R.id.addedit_verse_details:
			mListener.onVerseDetails(mCurrentVerse);
			break;
		}
		
		mCurrentPopup = null;
		
		return true;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Dialog listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private DialogInterface.OnClickListener mChapterThemeListenr = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch(which) {
			case DialogInterface.BUTTON_POSITIVE:
				final String content = mChapterThemeDialog.getInputText().trim(); 
				
				Bundle args = new Bundle();
				args.putString(ChapterThemeEditAsyncLoader.KEY_TEXT, content);
				
				if (mChapterTheme != null) {
					if (content.equals(mChapterTheme.getText().trim())){
						changesSave(mChapterThemeDialog);
						return; //unchanged, so do not launch
					}
					
					args.putString(ChapterThemeEditAsyncLoader.KEY_THEME_ID, mChapterTheme.getChapterThemeId());
				} else {
					args.putString(ChapterThemeEditAsyncLoader.KEY_CHAPTER_ID, mChapterId);
				}
				getLoaderManager().initLoader(REQUEST_UPDATE_CHAPTER_THEME, args, mUpdateLoader);
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				// nothing?
				break;
			}
		}
	};
	
	private DialogInterface.OnClickListener mNoteDialogListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			final Note note = mCurrentVerse.getNote();
			
			switch(which) {
			case DialogInterface.BUTTON_POSITIVE:
				if (mNoteDialog != null) {
					final String content =  mNoteDialog.getInputText().trim();
					
					Bundle args = new Bundle();
					args.putString(NoteEditAsyncLoader.KEY_TEXT, content);
					
					if (mCurrentVerse.hasNote()) {						
						if (content.equals(note.getText().trim())){
							changesSave(mNoteDialog);
							return; //unchanged, so do not launch
						}
						
						args.putString(NoteEditAsyncLoader.KEY_NOTE_ID, note.getNoteId());
					} else {
						args.putString(NoteEditAsyncLoader.KEY_VERSE_ID, mCurrentVerse.getVerseId());
					}
					
					getLoaderManager().initLoader(REQUEST_UPDATE_NOTE, args, mUpdateLoader); 
				}
				break;
				
			case DialogInterface.BUTTON_NEGATIVE:
				// nothing?
				break;
				
			case SimpleSaveDialog.DIALOG_SHARE_ACTION:
				
				if (mNoteDialog != null) {
					final String content =  mNoteDialog.getInputText().trim();					
					if (content.equals(note.getText())) {
						//share
						shareVerseText(note.getText(), TAG_NOTE_DIALOG);
						
					} else {
						//save then share (eventually)
						mPendingShareText = content;
						
						Bundle args = new Bundle();
						args.putString(NoteEditAsyncLoader.KEY_TEXT, content);
						args.putString(NoteEditAsyncLoader.KEY_NOTE_ID, note.getNoteId());
						
						getLoaderManager().initLoader(REQUEST_UPDATE_NOTE, args, mUpdateLoader); 
					}					
				}
				
				break;
				
			case SimpleSaveDialog.DIALOG_DELETE_ACTION:
				final int id = REQUEST_DELETE_NOTE;
				DialogFragment dialogFrag = SimpleYesNoDialog.newInstance(
						mRequestCodeToDialogStateMap.get(id).message, true);
				
				dialogFrag.setTargetFragment(BibleChapterViewFragment.this, id);
				dialogFrag.show(BibleChapterViewFragment.this.getFragmentManager(), mRequestCodeToDialogTagMap.get(id));
				break;
			}
		}
	};
	

	
	private DialogInterface.OnClickListener mDivThemeListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			final DivisionTheme divTheme = mCurrentVerse.getDivisionTheme();
			
			switch(which) {
			case DialogInterface.BUTTON_POSITIVE:
				if (mDivThemeDialog != null) {
					final String content = mDivThemeDialog.getInputText().trim();
					
					Bundle args = new Bundle();
					args.putString(DivisionThemeEditAsyncLoader.KEY_TEXT, content);
					
					if (mCurrentVerse.hasDivisionTheme()) {
						if (content.equals(divTheme.getText().trim())){
							changesSave(mDivThemeDialog);
							return; //unchanged, so do not launch
						}
						
						args.putString(DivisionThemeEditAsyncLoader.KEY_DIVISION_ID, divTheme.getDivisionThemeId());
					} else {
						args.putString(DivisionThemeEditAsyncLoader.KEY_VERSE_ID, mCurrentVerse.getVerseId());
					}
					
					getLoaderManager().initLoader(REQUEST_UPDATE_DIVISION_THEME, args, mUpdateLoader); 
				}
				break;
				
			case DialogInterface.BUTTON_NEGATIVE:
				// nothing?
				break;
				
			case SimpleSaveDialog.DIALOG_SHARE_ACTION:
				if (mDivThemeDialog != null) {
					final String content =  mDivThemeDialog.getInputText().trim();					
					if (content.equals(divTheme.getText())) {
						//share
						shareVerseText(divTheme.getText(), TAG_DIVTHEME_DIALOG);
						
					} else {
						//save then share (eventually)
						mPendingShareText = content;
						
						Bundle args = new Bundle();
						args.putString(DivisionThemeEditAsyncLoader.KEY_TEXT, content);
						args.putString(DivisionThemeEditAsyncLoader.KEY_DIVISION_ID, divTheme.getDivisionThemeId());

						getLoaderManager().initLoader(REQUEST_UPDATE_DIVISION_THEME, args, mUpdateLoader); 
					}					
				}
				break;
				
			case SimpleSaveDialog.DIALOG_DELETE_ACTION:
				final int id = REQUEST_DELETE_DIVTHEME;
				DialogFragment dialogFrag = SimpleYesNoDialog.newInstance(
						mRequestCodeToDialogStateMap.get(id).message, true);
				
				dialogFrag.setTargetFragment(BibleChapterViewFragment.this, id);
				dialogFrag.show(BibleChapterViewFragment.this.getFragmentManager(), mRequestCodeToDialogTagMap.get(id));
				break;
			}
		}
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start BibleChapterViewLoader.OnResponseListener
	////////////////////////////////////////////////////////////////////////////////////////////////
	

	/** Sets the chapter content for the view. <br />
	 * {@inheritDoc} */
	@Override
	public List<SpannableString> onBibleResponse(BibleVerseResponse response) {		
		/*
		 * To avoid frame drops for large content, text is split into chunks.
		 * See setChapterVerseContent() for more details.
		 */
		
		if (response == null) {
			Log.d(CLASS_NAME, "Failed to get verses");
			
			List<SpannableString> spanList = new ArrayList<SpannableString>();			
			spanList.add( new SpannableString(getString(R.string.ibs_error_cannotConnect)) );
			return spanList;
		} else {
			//clear anything that needs clearing
			mVerseIndexStarts.clear();
			mOverlayManager.clearAll();
			
			mVersesList = response.getVerses();
			mChapterTheme = response.getChapterTheme();
			
			//TODO add scroll to listener here
			
			switch (mSeekType) {
			case TYPE_NOTE:
				mChapterSpanBuilder.find(mSeekId, ChapterSpanBuilder.TYPE_NOTE, this);
				break;
			case TYPE_DIV_THEME:
				mChapterSpanBuilder.find(mSeekId, ChapterSpanBuilder.TYPE_DIV_THEME, this);
				break;
			case TYPE_MARKING:
				mChapterSpanBuilder.find(mSeekId, ChapterSpanBuilder.TYPE_MARKING, this);
				break;
			case TYPE_VERSE:
				mChapterSpanBuilder.find(mSeekId, ChapterSpanBuilder.TYPE_VERSE, this);
				break;
			case TYPE_CHAPTER_THEME:
			default:
				break;
			}
			
			return mChapterSpanBuilder.build(response, mVerseIndexStarts, this, this, mImageMarkingListener);			
		}
						
	}	
	

	@Override
	public void onChapterData(final Chapter chapter) {
		if (chapter != null ) {
			mChapterId = chapter.getChapterId();
			mChapterName = chapter.getParentBookShort() + " " + chapter.getNumber();
			mChapterItem = chapter;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End BibleChapterViewLoader.OnResponseListener
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	// OnNoteClickListener
	@Override
	public void onNoteClick(int index) {
		mCurrentVerse = mVersesList[index];
		if (mCurrentVerse.hasNote()) {
			mNoteDialog = SimpleSaveDialog.newInstance(
					getString(R.string.ibs_title_edit_note), 
					mCurrentVerse.getNote().getText());		
			mNoteDialog.setDialogOnClickListener(mNoteDialogListener);
			mNoteDialog.show(getFragmentManager(), TAG_NOTE_DIALOG);
		}
	}

	/// OnOverlayUpdateListener
	@Override
	public void onOverlayUpdate() {
		if (!mIsChapterVerseContentUpdating) { 
			mOverlayManager.clearViews();
			calculateVisibleOffset();
			setChapterVerseContent(mContentSpanList);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// start CustomMarkingsTooltip.OnMarkingSetListener
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onLetteringSet(LetteringItem item) {
		Bundle args = new Bundle();
		args.putString(MarkingApplyAsyncLoader.KEY_TYPE_ID, item.getId());
		args.putString(MarkingApplyAsyncLoader.KEY_TYPE, MarkingApplyAsyncLoader.TYPE_LETTERING);
		getLoaderManager().initLoader(REQUEST_CREATE_MARKINGS, args, mApplyMarkingLoader);
	}

	@Override
	public void onImageSet(ImageItem item) {
		Bundle args = new Bundle();
		args.putString(MarkingApplyAsyncLoader.KEY_TYPE_ID, item.getId());
		args.putString(MarkingApplyAsyncLoader.KEY_TYPE, MarkingApplyAsyncLoader.TYPE_IMAGE);
		getLoaderManager().initLoader(REQUEST_CREATE_MARKINGS, args, mApplyMarkingLoader);		
	}
	
	@Override
	public void onTooltipCancel() {
		AppCache.addLetteringListUpdateListener(mLetteringUpdateListener);
		AppCache.addImageListUpdateListener(mImageUpdateListener);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// OnVerseInteractionListener
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/* Handles the verse clicks. */
	@Override
	public void onVerseClick(final View view, final Verse verse) {	
		mCurrentVerse = verse;
		final OnMenuItemClickListener listener = this;
		final Context context = view.getContext();
		
		Log.d(CLASS_NAME, "context(): " + context);
		Log.d(CLASS_NAME, "view: " + view);
		
		
		view.post(new Runnable() {			
			@Override
			public void run() {
				View fragView = getView();
				if (fragView == null) {
					return;
				}
				View anchor = fragView.findViewById(R.id.ibs_bible_chapter_anchorview_hack);

				mCurrentPopup = new PopupMenu(context, anchor); 
				mCurrentPopup.getMenuInflater().inflate(R.menu.addedit_verse_actions, mCurrentPopup.getMenu());
				mCurrentPopup.setOnMenuItemClickListener(listener);
				
				updatePopMenu(mCurrentPopup);
				
				mCurrentPopup.show();	
			}
		});	
	}

	@Override
	public void onLetteringClick(final View view, final Verse verse, final MarkingItem marking) {
		mCurrentVerse = verse;
		mCurrentLetteringMarking = marking;
				
		final OnMenuItemClickListener listener = this;
		final Context context = view.getContext();
		
		Log.d(CLASS_NAME, "context(): " + context);
		Log.d(CLASS_NAME, "view: " + view);
		
		
		view.post(new Runnable() {			
			@Override
			public void run() {
				View anchor = view.getRootView().findViewById(R.id.ibs_bible_chapter_anchorview_hack);
				
				mCurrentPopup = new PopupMenu(context, anchor); 
				mCurrentPopup.getMenuInflater()
							.inflate(	R.menu.sharedelete_lettering_addedit_verse_actions, 
										mCurrentPopup.getMenu());
				mCurrentPopup.setOnMenuItemClickListener(listener);
				
				updatePopMenu(mCurrentPopup);
				
				mCurrentPopup.show();	
			}
		});	
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Image Marking click listener
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private ImageMarkingWrapper.OnImageMarkingClickListener mImageMarkingListener = 
			new ImageMarkingWrapper.OnImageMarkingClickListener() {
		@Override
		public void onImageMarkingClick(MarkingItem imageMarking) {
			if (mChapterVerseContent != null && mChapterItem != null) {
				
			}
			int start = imageMarking.getStartIndex();
			int end = imageMarking.getEndIndex();
			String selectedText = trimHiddenParagraphs(mChapterVerseContent, start, end);
			
			
			final String title = mChapterItem.getName() + ":" + imageMarking.getVerseRange();
			
			ImageMarkingDialog dialog = ImageMarkingDialog.newInstance(mContentStub, imageMarking, title, selectedText);
			dialog.setTargetFragment(BibleChapterViewFragment.this, REQUEST_DISPLAY_IMAGE_MARKING);
			dialog.show(getFragmentManager(), TAG_DISPLAY_IMAGE_MARKING);
			
		}

	};
	

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// OnPositionFoundListener 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onPositionFound(String id, int textIndex) {
		mSeekIndex = textIndex;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Cache listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private OnCacheUpdateListener<ImageListResponse> mImageUpdateListener = new OnCacheUpdateListener<ImageListResponse>() {
		@Override
		public void onCacheUpdate(String key, ImageListResponse value) {
			if (value == null) {
				chapterVerseReload();
			}
		}
	}; 
	
	private OnCacheUpdateListener<LetteringListResponse> mLetteringUpdateListener = new OnCacheUpdateListener<LetteringListResponse>() {
		@Override
		public void onCacheUpdate(String key, LetteringListResponse value) {
			if (value == null) {
				chapterVerseReload();
			}
		}
	}; 
	
	private OnCacheUpdateListener<BibleVerseResponse> mChapterUpdateListener = new OnCacheUpdateListener<BibleVerseResponse>() {
		@Override
		public void onCacheUpdate(String key, BibleVerseResponse value) {
			if (key != null && key.equals(mChapterId) && value != null) {
				if (!Utility.checkIfLoading(getLoaderManager(), REQUEST_FETCH_CURRENT_CHAPTER_CONTENT)) {
					chapterVerseReload(); //prevent loops
				}
			}
		}
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// External interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * @version 0.1.0-20150610
	 *
	 */
	public static interface OnInteractionListener {
		
		public void onVerseDetails(Verse verse);
	}

}
