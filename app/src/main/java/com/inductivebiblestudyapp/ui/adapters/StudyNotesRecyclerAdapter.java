package com.inductivebiblestudyapp.ui.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.StudyNotesResponse;
import com.inductivebiblestudyapp.data.model.StudyNotesResponse.StudyNoteItem;
/**
 * Adapter for study notes listing.
 * @author Jason Jenkins
 * @version 0.8.2-20150924
 */
public class StudyNotesRecyclerAdapter extends Adapter<StudyNotesRecyclerAdapter.ViewHolder> {
	final static private String LOGTAG = StudyNotesRecyclerAdapter.class
			.getSimpleName();	

	public static final int SORT_NONE = 0;
	public static final int SORT_ASC = 1;
	public static final int SORT_DESC = 2;
	
	public static final int TYPE_CHAPTER_THEME = 0;
	public static final int TYPE_DIV_THEME = 1;
	public static final int TYPE_NOTES = 2;
	public static final int TYPE_IMAGE = 3;
	public static final int TYPE_LETTERING = 4;	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private final List<StudyNoteItem> mNoteList = new ArrayList<StudyNoteItem>();
	private final List<StudyNoteItem> mChapterThemeList = new ArrayList<StudyNoteItem>();
	private final List<StudyNoteItem> mDivThemeList = new ArrayList<StudyNoteItem>();
	private final List<StudyNoteItem> mImageList = new ArrayList<StudyNoteItem>();
	private final List<StudyNoteItem> mLetteringList = new ArrayList<StudyNoteItem>();
	
	private final List<StudyNoteItem> mOutputStudyNotes = new ArrayList<StudyNoteItem>();	
	
	private final OnStudyNoteItemClickListener mOnStudyNoteClickListener;
	
	private final int mEmptyStringResource;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End finals
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private boolean mIsLoading = true;
	private boolean mIsEmpty = true;
	
	private String mErrorMessage = "";
	
	private boolean mShowNotes = true;
	private boolean mShowDivisionThemes = true;
	private boolean mShowChapterThemes = true;
	private boolean mShowImageMarkings = true;
	private boolean mShowLetteringMarkings = true;
	
	/*default*/ int mSortByBook = SORT_ASC;
	/*default*/ int mSortByChapter = SORT_ASC;
	/*default*/ int mSortByVerse = SORT_ASC;
	
	
	/**
	 * @param activity
	 */
	public StudyNotesRecyclerAdapter(OnStudyNoteItemClickListener listener, int emptyStringRes) {
		mOnStudyNoteClickListener = listener;
		mEmptyStringResource = emptyStringRes;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End intializations
	////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean isEmpty() {
		return mIsEmpty;
	}

	/** Clears all notes + errors and resets the view to both "loading" and "empty" */
	public void clear() {
		mNoteList.clear();
		mDivThemeList.clear();
		mChapterThemeList.clear();
		mImageList.clear();
		mLetteringList.clear();
		mOutputStudyNotes.clear();
		mIsLoading = true;
		mErrorMessage = "";
		mIsEmpty = mOutputStudyNotes.isEmpty();
		notifyDataSetChangedWrapper();
	}
	
	/** Converts a response to a list of notes. Passing <code>null</code> sets it to empty. */
	public void setStudyNotes(StudyNotesResponse response) {
		mNoteList.clear();
		mDivThemeList.clear();
		mChapterThemeList.clear();
		mImageList.clear();
		mLetteringList.clear();
		
		if (response != null) {
			
			StudyNoteItem[] studyNotes = response.getStudyNotes();
			for(StudyNoteItem studyNote : studyNotes) {
				switch (studyNote.getItemType()) {
				case StudyNoteItem.TYPE_NOTE:
					mNoteList.add(studyNote);
					break;
					
				case StudyNoteItem.TYPE_DIV_THEME:
					mDivThemeList.add(studyNote);
					break;
					
				case StudyNoteItem.TYPE_CHAPTER_THEME:
					mChapterThemeList.add(studyNote);
					break;
					
				case StudyNoteItem.TYPE_MARKING_IMAGE:
					mImageList.add(studyNote);
					break;
					
				case StudyNoteItem.TYPE_MARKING_LETTERING:
					mLetteringList.add(studyNote);
					break;

				default:
					Log.w(LOGTAG, "Unrecognized type: " + 
							studyNote.getItemType() + "->" + studyNote.toString());
					break;
				}
			}
		}
		
		mIsLoading = false; 
		updateFilteredOutputList();		
	}
	
	/**
	 * Sets the filter for the adapter.
	 * @param types The TYPE to show or hide
	 * @param show <code>true</code> to show, <code>false</code> to remove/hide.
	 * @throws IllegalArgumentException if the lengths do not match
	 */
	public void setFilter(int[] type, boolean[] show) {
		final int SIZE = type.length;
		if (SIZE != show.length) {
			throw new IllegalArgumentException("Arrays must be the same length");
		}
		
		for (int index = 0; index < SIZE; index++) {
			switch (type[index]) {
			case TYPE_CHAPTER_THEME:
				mShowChapterThemes = show[index];
				break;
			case TYPE_DIV_THEME:
				mShowDivisionThemes = show[index];
				break;
			case TYPE_NOTES:
				mShowNotes = show[index];
				break;
			case TYPE_IMAGE:
				mShowImageMarkings = show[index];
				break;
			case TYPE_LETTERING:
				mShowLetteringMarkings = show[index];
				break;

			default:
				Log.w(LOGTAG, "Unknown filter passed: " + type[index] );
			}
		}

		updateFilteredOutputList();
	}
	
	/**
	 * Calls {@link #setFilter(int[], boolean[])}
	 */
	public void setFilter(int type, boolean show) {
		setFilter(new int[]{type}, new boolean[]{show});
	}
	
	
	/** Sets the error state for the adapter. */
	public void setError(String error) {
		this.mErrorMessage = error;
		mIsLoading = false;
		mIsEmpty = false;
		notifyDataSetChangedWrapper();
	}
		
	
	/** Sets the sorting method for the adapter. 
	 * Expects {@link #SORT_ASC}, {@link #SORT_DESC}, or {@link #SORT_NONE}. */
	public void setSort(int book, int chapter, int verse) {
		if (	book != SORT_DESC && chapter != SORT_DESC && verse != SORT_DESC &&
				book != SORT_ASC && chapter != SORT_ASC && verse != SORT_ASC &&
				book != SORT_NONE && chapter != SORT_NONE && verse != SORT_NONE) {
			Log.w(LOGTAG, "Unknown sort passed: " + book + ", " + chapter + ", " + verse);
			return;
		}
		mSortByBook = book;
		mSortByChapter = chapter;
		mSortByVerse = verse;
		
		Collections.sort(mOutputStudyNotes, mSortComparator);
		notifyDataSetChangedWrapper();
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Override methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public int getItemCount() {
		if (mIsLoading || mIsEmpty || hasError()) {
			return 1; //the message item
		}
		
		//display all
		return mOutputStudyNotes.size();
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		if (mIsLoading) {
			setMessage(holder, R.string.ibs_text_loading);
			return;
		} else if (mIsEmpty) {
			setMessage(holder, mEmptyStringResource);
			return;
		} else if (hasError()) {
			setMessage(holder, mErrorMessage);
			return;
		}
		
		final StudyNoteItem item = mOutputStudyNotes.get(position);
		
		holder.book.setText(item.getParentBookName());
		holder.chapter.setText(item.getParentChapterNumber());
		
		String verseRange = item.getVerseRange();
		if (verseRange.isEmpty()) {
			verseRange = holder.verse.getContext().getString(R.string.ibs_text_studyNotes_unsetText);
		}
		holder.verse.setText(verseRange);
		
		holder.rootView.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				if (mOnStudyNoteClickListener != null) {
					mOnStudyNoteClickListener.onStudyNoteItemClick(item);
				}					
			}
		});
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	    View v = LayoutInflater.from(parent.getContext())
	    			.inflate(R.layout.list_item_notes, parent, false);
	    
	    return new ViewHolder(v);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////	

	private boolean hasError() {
		return !TextUtils.isEmpty(mErrorMessage);
	}
	
	private void setMessage(ViewHolder holder, String message) {
		holder.book.setText(message);
		holder.chapter.setText(R.string.ibs_text_studyNotes_unsetText);
		holder.verse.setText(R.string.ibs_text_studyNotes_unsetText);
	}
	
	private void setMessage(ViewHolder holder, int stringRes) {
		holder.book.setText(stringRes);
		holder.chapter.setText(R.string.ibs_text_studyNotes_unsetText);
		holder.verse.setText(R.string.ibs_text_studyNotes_unsetText);
	}
	
	
	private void notifyDataSetChangedWrapper(){
		notifyDataSetChanged();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Filtering methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Updates the filtered output state, based on the boolean values set. */
	private void updateFilteredOutputList() {
		mOutputStudyNotes.clear();		
		
		addOrRemoveToOutput(mShowChapterThemes, mChapterThemeList);
		addOrRemoveToOutput(mShowNotes, mNoteList);
		addOrRemoveToOutput(mShowDivisionThemes, mDivThemeList);
		addOrRemoveToOutput(mShowImageMarkings, mImageList);
		addOrRemoveToOutput(mShowLetteringMarkings, mLetteringList);
		
		Collections.sort(mOutputStudyNotes, mSortComparator);
		
		mIsEmpty = mOutputStudyNotes.isEmpty();
		
		notifyDataSetChangedWrapper();
	}
	
	/**
	 * Adds/removes a group of notes from the list based on the boolean.
	 */
	private void addOrRemoveToOutput(boolean add, List<StudyNoteItem> list) {
		if (add) {
			mOutputStudyNotes.addAll(list);
		} else {
			mOutputStudyNotes.removeAll(list);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Sorting 
	////////////////////////////////////////////////////////////////////////////////////////////////


	/** The sorter that sorts the content according to the current mode. */
	private Comparator<StudyNoteItem> mSortComparator = new Comparator<StudyNotesResponse.StudyNoteItem>() {

		/*
		 * Remember: 
		 * 
		 * https://developer.android.com/reference/java/util/Comparator.html#compare%28T,%20T%29
		 * 
		 *  - compare(a,a) returns zero for all a
		 *  - the sign of compare(a,b) must be the opposite of the sign of compare(b,a) for all pairs of (a,b)
		 *  - From compare(a,b) > 0 and compare(b,c) > 0 it must follow compare(a,c) > 0 for all possible combinations of (a,b,c)
		 *  
		 *  
		 *  an integer < 0 if leftHandSide is less than rightHandSide, 
		 *  0 if they are equal, 
		 *  and > 0 if leftHandSide is greater than rightHandSide.
		 *  
		 *  
		 *  (non-Javadoc)
		 */
		@Override
		//compare(StudyNoteItem lhs, StudyNoteItem rhs)
		public int compare(StudyNoteItem leftHS, StudyNoteItem rightHS) {
			
			// sort by book name
			int bookSortValue = 0;
			switch (mSortByBook) {			
			case SORT_ASC:
				bookSortValue = leftHS.getParentBookName().compareTo(rightHS.getParentBookName());
				break;
			case SORT_DESC:
				bookSortValue = rightHS.getParentBookName().compareTo(leftHS.getParentBookName());
				break;
			case SORT_NONE:
			default:
				bookSortValue = 0;
				break;
			}
			if (0 != bookSortValue) { //sort by book first
				return bookSortValue;
			}
			
			/////////////////////////////////////////////////////////////////////////////
			//sort by chapter number
			int chapterSortValue = 0;
			final int chapterNumLeft = Integer.valueOf(leftHS.getParentChapterNumber());
			final int chapterNumRight = Integer.valueOf(rightHS.getParentChapterNumber());
			
			switch (mSortByChapter) {			
			case SORT_ASC:
				chapterSortValue = compareInts(chapterNumLeft, chapterNumRight);
				break;
			case SORT_DESC:
				chapterSortValue = compareInts(chapterNumRight, chapterNumLeft);
				break;
			case SORT_NONE:
				chapterSortValue = 0;
			default:
				break;
			}
			if (0 != chapterSortValue) { // sort then by chapter
				return chapterSortValue;
			} 
			
			/////////////////////////////////////////////////////////////////////////////
			//sort by verse number
			int verseSortValue = 0;
			final int verseNumLeft = parseVerseNumber(leftHS.getVerseRange());
			final int verseNumRight = parseVerseNumber(rightHS.getVerseRange());	
			
			switch (mSortByVerse) {			
			case SORT_ASC:
				verseSortValue = compareInts(verseNumLeft, verseNumRight); 
				break;
			case SORT_DESC:
				verseSortValue = compareInts(verseNumRight, verseNumLeft); 
				break;
			case SORT_NONE:
			default:
				verseSortValue = 0;
				break;
			}
			if (0 != verseSortValue) { // then sort by verse number
				return verseSortValue;
			}
			
			/////////////////////////////////////////////////////////////////////////////
			//sort by type: 1. chapter theme, 2. note, 3. div themes, 3. images, 4. letterings
			int typeSortValue = compareInts(leftHS.getItemType(), rightHS.getItemType()); 
			if (0 != typeSortValue) { //then by type
				return typeSortValue;
			}
			
			/////////////////////////////////////////////////////////////////////////////
			//then finally by id/creation date
			int idSortValue = 0;
			try {
				int idLeft = Integer.valueOf(leftHS.getItemId());
				int idRight = Integer.valueOf(rightHS.getItemId());
				
				idSortValue = compareInts(idLeft, idRight);
			} catch (NumberFormatException e){}
			
			return idSortValue;
		}
		
	};
	
	
	/**
	 * 
	 * Remember: 
	 * 
	 * https://developer.android.com/reference/java/util/Comparator.html#compare%28T,%20T%29
	 * 
	 *  - compare(a,a) returns zero for all a
	 *  - the sign of compare(a,b) must be the opposite of the sign of compare(b,a) for all pairs of (a,b)
	 *  - From compare(a,b) > 0 and compare(b,c) > 0 it must follow compare(a,c) > 0 for all possible combinations of (a,b,c)
	 *  
	 *  @return
	 *  an integer < 0 if left is less than right, 
	 *  0 if they are equal, 
	 *  and > 0 if left is greater than right.
	 *
	 */
	/*default*/ static int compareInts(int left, int right) {
		if (left < right) { 
			return -1;
		} else if (left > right) {
			return 1;
		} else {
			return 0;
		}
	}
	/** Parses and returns the first verse number or 0 if empty. 
	 * If hypenated (48-63) returns the first number (48) */
	/*default*/ static int parseVerseNumber(String verseNumber) {
		if (verseNumber == null || verseNumber.isEmpty()) {
			return 0;
		}
		int dashIndex = verseNumber.indexOf("-");
		if (dashIndex > 0) {
			verseNumber = verseNumber.substring(0, dashIndex);
		}
		return Integer.parseInt(verseNumber);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.1.0-20150722 */
	public static class ViewHolder extends RecyclerView.ViewHolder {
		public final TextView book;
		public final TextView chapter;
		public final TextView verse;
		
		public final View rootView;
		
		public ViewHolder(View rootView) {
			super(rootView);
			this.rootView = rootView;
			this.book = (TextView) rootView.findViewById(R.id.list_item_notes_book);
			this.chapter = (TextView) rootView.findViewById(R.id.list_item_notes_chapter);
			this.verse = (TextView) rootView.findViewById(R.id.list_item_notes_verse);
		}
	}
	
	/** @version 0.1.0-20150720 */
	public static interface OnStudyNoteItemClickListener {
		public void onStudyNoteItemClick(StudyNoteItem item);
	}
}
