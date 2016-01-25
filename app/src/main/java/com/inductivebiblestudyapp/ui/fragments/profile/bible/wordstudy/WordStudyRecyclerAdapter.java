package com.inductivebiblestudyapp.ui.fragments.profile.bible.wordstudy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseDetailsResponse.StrongsNumberEntry;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.CrossReference;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.CrossReferenceResponse;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.StrongsDefinition;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.StrongsResponse;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.StrongsResponse.CrossReferenceEntry;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.wordstudy.WordStudyFragment.OnInteractionListener;
import com.inductivebiblestudyapp.ui.style.StyleUtil;

/**
 * Adapter for strong's definition & cross references. Makes for more efficient displays
 * @version 0.5.1-20150828
 */
class WordStudyRecyclerAdapter extends Adapter<RecyclerView.ViewHolder> {
	final static private String CLASS_NAME = WordStudyRecyclerAdapter.class
			.getSimpleName();
	
	/** Parcellable. StrongsNumberEntry */
	public static final String ARG_STRONGS_NUMBER_ENTRY = CLASS_NAME + ".ARG_STRONGS_NUMBER_ENTRY";
	
	public static final String ARG_STRONGS_NUMBER = CLASS_NAME + ".ARG_STRONGS_NUMBER";
	/** Can be null or empty. */
	public static final String ARG_STRONGS_LANGUAGE = CLASS_NAME + ".ARG_STRONGS_LANGUAGE";
	/** Can be null or empty. */
	public static final String ARG_WORD_DEFINED = CLASS_NAME + ".ARG_WORD_DEFINED";
	
	
	private static final int TYPE_DEFINITION = 0;
	private static final int TYPE_CROSS_REFERENCE_PARENT = 1;
	private static final int TYPE_CROSS_REFERENCE_ITEM = 2;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private final String mStrongsNumber;
	/** Can be empty. */
	private final String mDefaultLanguage;
	/** Can be empty. */
	private final String mWordDefined;
	
	private final CrossReferenceManager mCrossReferenceManager = new CrossReferenceManager();
	
	private final OnAdapterInteractionListener mAdapterListener;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End finals
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** On fragment interaction. */
	private OnInteractionListener mFragListener = null;
	
	private String mErrorMessage = "";
	
	private StrongsDefinition mStrongsDefinition = null;
	
	
	/**
	 * @param context
	 * @param args
	 * @param listener
	 */
	public WordStudyRecyclerAdapter(Context context, Bundle args, OnAdapterInteractionListener listener) {
		
		mAdapterListener = listener;
		
		if (args.getParcelable(ARG_STRONGS_NUMBER_ENTRY) != null) {
			StrongsNumberEntry entry  = args.getParcelable(ARG_STRONGS_NUMBER_ENTRY);
			
			mStrongsNumber = entry.getNumber();
			mDefaultLanguage = entry.getLanguage();
			mWordDefined = entry.getText();
			
		} else {
			mStrongsNumber = args.getString(ARG_STRONGS_NUMBER);
			mDefaultLanguage = args.getString(ARG_STRONGS_LANGUAGE, "");
			mWordDefined = args.getString(ARG_WORD_DEFINED, "");
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End initialization
	////////////////////////////////////////////////////////////////////////////////////////////////

	public void setOnInteractionListener(OnInteractionListener mListener) {
		this.mFragListener = mListener;
	}
	
	/** Clears all results & errors and resets the view to both "loading" and "empty" */
	public void clear() {
		mCrossReferenceManager.clear();
		
		mStrongsDefinition = null;
		mErrorMessage = "";
	}
	
	/** Sets the results for the adapter. 
	 * @param response  */
	public void setResponse(StrongsResponse response) {
		mStrongsDefinition = null;
		mCrossReferenceManager.clear();
		
		CrossReferenceEntry[] crossRefs = response.getCrossReferenceEntries();
	
		final int SIZE = crossRefs.length;
		for (int index = 0; index < SIZE; index++) {
			mCrossReferenceManager.crossRefAdd(new CrossReferenceParent(crossRefs[index]));
		}
		
		mStrongsDefinition = response.getStrongsDefinition();
		
		mErrorMessage = "";
		notifyDataSetChanged();
	}
	
	/** Sets the error state for the adapter. */
	public void setError(String error) {
		this.mErrorMessage = error;
		notifyDataSetChanged();
	}
	
	/** Sets the cross reference content. If the response is <code>null</code> sets 
	 * and error message. If not, populates and refreshes the view.
	 * @param parent
	 * @param response
	 */
	synchronized public void setCrossReferenceResponse(CrossReferenceParent parent, CrossReferenceResponse response) {
		if (!parent.mIsLoading) { //if not loading, cancel
			return;
		}
		
		final int position = parent.mPosition;
		if (response == null) {
			parent.mErrorMessage = R.string.ibs_error_cannotConnect_short;				
		} else {
			parent.mErrorMessage = 0;
			int length = mCrossReferenceManager.expandCrossRef(parent, response.getCrossReferences());
			notifyItemRangeInserted(position + 1, length);
		}
		parent.mIsLoading = false;
	
		notifyItemChanged(position);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Override methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public int getItemCount() {
		if (hasError()) {
			return 1;
		}
		return 	(null == mStrongsDefinition ? 0 : 1) + 
				mCrossReferenceManager.getCrossRefCount();
	}

	/** See {@link CrossReferenceManager}.
	 * {@inheritDoc}
	 */
	@Override
	public int getItemViewType(int position) { 
		return mCrossReferenceManager.getItemViewType(position);
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		if (hasError()) {
			StrongsDefinitionViewHolder defHolder = (StrongsDefinitionViewHolder) holder;
			setStrongsError(defHolder);
		}
		switch (getItemViewType(position)) {
		case TYPE_DEFINITION:
			StrongsDefinitionViewHolder defHolder = (StrongsDefinitionViewHolder) holder;
			
			setStrongsDefinition(defHolder);
			
			break;

		case TYPE_CROSS_REFERENCE_PARENT:
			CrossReferenceParentViewHolder crossRefParentHolder = (CrossReferenceParentViewHolder) holder;
			
			if (mCrossReferenceManager.mCrossRefParents.isEmpty()) {
				crossRefParentHolder.crossReferenceTitle.setText(R.string.ibs_text_noCrossReferences);
			} else {					
				setCrossReferenceParent(crossRefParentHolder, position);
			}
			break;
		
		case TYPE_CROSS_REFERENCE_ITEM:
		default:
			CrossReferenceItemViewHolder crossRefItemHolder = (CrossReferenceItemViewHolder) holder;
			
			if (mCrossReferenceManager.mCrossRefParents.isEmpty()) {
				//should never reach here
				Log.w("WordStudyAdapter", "Unexpected state reached; cannot draw empty cross references");
			} else {
				setCrossReferenceItem(crossRefItemHolder, position);
			}
			break;
		}
		
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	    View v = null;
	    switch (viewType) {
		case TYPE_DEFINITION:

		    v = LayoutInflater.from(parent.getContext())
		    			.inflate(R.layout.list_item_word_study_definition, parent, false);
			StrongsDefinitionViewHolder holder = new StrongsDefinitionViewHolder(v);				
			
			return holder;

		case TYPE_CROSS_REFERENCE_PARENT:
			v = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.list_item_word_study_crossref_parent, parent, false);
		return new CrossReferenceParentViewHolder(v);
	
		case TYPE_CROSS_REFERENCE_ITEM:
		default:
			 v = LayoutInflater.from(parent.getContext())
    			.inflate(R.layout.list_item_word_study_cross_reference, parent, false);
			return new CrossReferenceItemViewHolder(v);
		}
	    
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	private boolean hasError() {
		return !TextUtils.isEmpty(mErrorMessage);
	}
	
	/** Sets the strong's definition based on the current selection. */
	private void setStrongsError(StrongsDefinitionViewHolder holder) {
		holder.strongNumbersTitle.setText(mErrorMessage);
		holder.transliterationPhonetic.setText(mErrorMessage);
		holder.partOfSpeech.setText("");
		holder.wordOrigin.setText("");
		holder.ipdDefinition.setText("");	
	}
	
	/** Sets the strong's definition based on the current selection. */
	private void setStrongsDefinition(StrongsDefinitionViewHolder holder) {

		if (TextUtils.isEmpty(mWordDefined)) {
			setTextView(holder.strongNumbersTitle, 
					R.string.ibs_title_strongsDef_strongsNum, mDefaultLanguage + mStrongsNumber);
		} else {
			setTextViewFromHtml(holder.strongNumbersTitle, 
					R.string.ibs_title_strongsDef_strongsNum_word, mDefaultLanguage + mStrongsNumber, mWordDefined);
		}
		
		if (mStrongsDefinition == null) {
			Log.d(CLASS_NAME, "Unusual behaviour, no definition available.");
			return;
		}
		
		
		int stringRef = StrongsDefinition.LANGUAGE_HEBREW.equals(mDefaultLanguage) ? 
					R.string.ibs_text_strongsDef_transliteration_hebrew :
					R.string.ibs_text_strongsDef_transliteration_greek ;
		
		setTextViewFromHtml(holder.transliterationPhonetic, stringRef, 
				mStrongsDefinition.getTransliteration(), mStrongsDefinition.getPhonetic());
		
		String partOfSpeech = mStrongsDefinition.getPartOfSpeech();
		if (!TextUtils.isEmpty(partOfSpeech)) {
			setTextView(holder.partOfSpeech, R.string.ibs_text_strongsDef_partOfSpeech, 
					partOfSpeech);
		} else {
			holder.partOfSpeech.setText("");
		}
		
		setTextView(holder.wordOrigin, R.string.ibs_text_strongsDef_origin, 
				mStrongsDefinition.getWordOrigin());
		
		setTextViewFromHtml(holder.ipdDefinition, R.string.ibs_text_strongsDef_definition_ipd, 
				mStrongsDefinition.getIpdDefinition());		
	}
	
	/** Sets the cross reference, based on index. */
	private void setCrossReferenceParent(final CrossReferenceParentViewHolder holder, final int position) {
		final int index = mCrossReferenceManager.getPositionOffset(position, TYPE_CROSS_REFERENCE_PARENT);
		
		final CrossReferenceParent crossRefParent = mCrossReferenceManager.mCrossRefParents.get(index);
		final CrossReferenceEntry crossRef = crossRefParent.mCrossRefTitle;
			
		String title = holder.crossReferenceTitle.getContext()
				.getString(
						R.string.ibs_checkbox_crossReferenceTitle, 
						crossRef.getTitle(),
						crossRef.getNumberOfOccurences()
						);
		holder.crossReferenceTitle.setText(title);
		
		//set loading / error view.
		final int loadingVisibility = crossRefParent.mIsLoading ? View.VISIBLE : View.GONE;
		holder.progressBar.setVisibility(loadingVisibility);			
		if (crossRefParent.mErrorMessage != 0) {
			holder.statusMessage.setText(crossRefParent.mErrorMessage);
			holder.statusMessage.setVisibility(View.VISIBLE);
		} else {
			holder.statusMessage.setVisibility(loadingVisibility);
		}
		
		holder.crossReferenceTitle.setOnCheckedChangeListener(null);
		boolean expanded = !crossRefParent.mCrossReferences.isEmpty(); //if empty, not expanded
		holder.crossReferenceTitle.setChecked(expanded);
		
		holder.rootView.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				holder.crossReferenceTitle.setChecked(!holder.crossReferenceTitle.isChecked());
			}
		});
		
		holder.crossReferenceTitle.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {				
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//use the current up to date position
				final int pos = crossRefParent.mPosition; 
				
				if (isChecked && !crossRefParent.mIsLoading) { 
					holder.progressBar.setVisibility(View.VISIBLE);
					holder.statusMessage.setText(R.string.ibs_text_loading);
					holder.statusMessage.setVisibility(View.VISIBLE);
					
					crossRefParent.mIsLoading = true;
					crossRefParent.mErrorMessage = 0;
					
					//starts the loading of error.
					mAdapterListener.onCrossReferences(crossRefParent, crossRefParent.mCrossRefTitle);						
					
				} else {
					if (crossRefParent.mIsLoading) {
						crossRefParent.mIsLoading = false;
						notifyItemChanged(pos);
					}
					int length = mCrossReferenceManager.collapseCrossRef(crossRefParent);
					notifyItemRangeRemoved(pos + 1, length);
				}
				/*
				Log.d(CLASS_NAME, 
						"Checked: (" + crossRefParent + ") " + isChecked + " -> " + pos ); 
*/			}
		});
	}
	
	/** Sets the cross reference, based on index. */
	private void setCrossReferenceItem(CrossReferenceItemViewHolder holder, int position) {
		final int parentIndex = mCrossReferenceManager.getPositionOffset(position, TYPE_CROSS_REFERENCE_PARENT);
		final int index = mCrossReferenceManager.getPositionOffset(position, TYPE_CROSS_REFERENCE_ITEM);
		final CrossReference crossRef =  
				mCrossReferenceManager	.mCrossRefParents.get(parentIndex)
										.mCrossReferences.get(index);
		
		final String reference = crossRef.getBookNameShort() + " " + 
				crossRef.getChapterNumber() + ":" + crossRef.getVerseRange();
		
		holder.verseReference.setText(reference);
		SpannableString spanString = new SpannableString(Html.fromHtml(crossRef.getVerseTextOnly()));
		StyleUtil.findWordAndBoldUnderline(crossRef.getTitle(), spanString, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);		
		holder.verseText.setText(spanString);
		
		holder.rootView.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				if (mFragListener != null) {
					mFragListener.onWordStudyVerseCrossReference(crossRef);
				}
			}
		});
	}



	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Finds textview. */
	private static TextView findTextView(View rootView, int id) {
		return (TextView) rootView.findViewById(id);
	}
	
	/** Simple method sets the text view. */
	private static void setTextView(TextView textview, int stringId, Object...args) {
		textview.setText(textview.getContext().getString(stringId, args));
	}
	
	/** Simple method sets the text view. */
	private static void setTextViewFromHtml(TextView textview, int stringId, Object...args) {
		textview.setText( Html.fromHtml( textview.getContext().getString(stringId, args), null, new StrongsTagHandler()) );		
	}


		
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Manages the adapters offsets for 
	 * {@link WordStudyRecyclerAdapter#getItemViewType(int)}, positions &
	 * cross references
	 * 
	 * @version 0.2.1-20150917 */
	private static class CrossReferenceManager {
		
		/** Keeps a sorted list of indices of {@link #mCrossRefParents} which are expanded.
		 */
		private final TreeSet<Integer> mExpandedCrossRefIndices = new TreeSet<Integer>();
		
		private final List<CrossReferenceParent> mCrossRefParents = new ArrayList<CrossReferenceParent>();
		
		private int mExpandedCount = 0;
		
		synchronized public void clear() {
			mCrossRefParents.clear();
			mExpandedCrossRefIndices.clear();
			mExpandedCount = 0;
		}
		
		/** Adds content to the cross reference. */
		synchronized public void crossRefAdd(CrossReferenceParent crossRef) {
			mCrossRefParents.add(crossRef);
			crossRef.mPosition = mCrossRefParents.size(); //position has offset of 1
		}
					
		
		/** Returns the cross reference total count; parents and expanded. */
		synchronized public int getCrossRefCount() {
			return mCrossRefParents.size() + mExpandedCount;
		}
		/**
		 * Expands a cross references by populating it and keeping counts.
		 * @return the length of the added content.
		 */
		public int expandCrossRef(CrossReferenceParent crossRefParent, 
				CrossReference[] crossReferences) {
			return expandCrossRef(crossRefParent, Arrays.asList(crossReferences));
		}
		/**
		 * Expands a cross references by populating it and keeping counts.
		 * @return the length of the added content.
		 */
		synchronized public int expandCrossRef(CrossReferenceParent crossRefParent, 
				List<CrossReference> crossReferences) {

			final int crossRefIndex = mCrossRefParents.indexOf(crossRefParent);				
			if (crossRefIndex < 0) {
				Log.d(CLASS_NAME, "Cannot expand non-parent: index=" + crossRefIndex);
				return 0;
			}
			
			final int length = crossReferences.size();
			
			crossRefParent.mCrossReferences.clear();
			crossRefParent.mCrossReferences.addAll(crossReferences);
	
			final int SIZE = mCrossRefParents.size();				
			for (int index = crossRefIndex; index < SIZE; index++) { 
				CrossReferenceParent parent = mCrossRefParents.get(index);
				if (crossRefParent.equals(parent)) {
					continue;
				}
				// for each parent after the expansion
				// increase the old position by length of expansion
				parent.mPosition += length; 
			}
			
			mExpandedCrossRefIndices.add(crossRefIndex);
			
			mExpandedCount += length;
			return length;
		}
		
		
		/** Uses to remove the cross ref parent has been collapsed to remove offset.
		 * @return the length of the removed content.
		 *  */
		synchronized public int collapseCrossRef(CrossReferenceParent crossRefParent) {
			
			final int crossRefIndex = mCrossRefParents.indexOf(crossRefParent);				
			if (crossRefIndex < 0) {
				Log.d(CLASS_NAME, "Cannot collapse non-parent: index=" + crossRefIndex);
				return 0;
			}
			
			final int length = crossRefParent.mCrossReferences.size();
			
			crossRefParent.mCrossReferences.clear();
							
			final int SIZE = mCrossRefParents.size();				
			for (int index = crossRefIndex; index < SIZE; index++) { 
				CrossReferenceParent parent = mCrossRefParents.get(index);
				if (crossRefParent.equals(parent)) {
					continue;
				}
				// for each parent after the expansion
				// decrease the old position by length of expansion
				parent.mPosition -= length; 
			}
			
			mExpandedCrossRefIndices.remove(crossRefIndex);
			mExpandedCount -= length;
			return length;
		}
		
		/**
		 * Returns the index based on type. 
		 * @param position
		 * @param viewType
		 * @return
		 */
		synchronized public int getPositionOffset(int position, int viewType) {
			switch (viewType) {
			case TYPE_DEFINITION:					
				return position;
			
			case TYPE_CROSS_REFERENCE_PARENT: {
				
				int offset = 1; //initial offset of 1 from definition
				
				for (Integer expandedIndex : mExpandedCrossRefIndices) {
					CrossReferenceParent parent = mCrossRefParents.get(expandedIndex);
					
					if (position > parent.mPosition) { //if after an expanded position
						if (position < parent.getExpandEnd()) {
							//we are within this expanded parent, get the parent 
							position = parent.mPosition;
							break;
						} else {
							//we are after the expanded position
							offset += parent.mCrossReferences.size(); //remove length
						}
					}
				}					
				return position - offset; 
			}
			
			case TYPE_CROSS_REFERENCE_ITEM:
			default: {
				
				for (Integer expandedIndex : mExpandedCrossRefIndices) {
					CrossReferenceParent parent = mCrossRefParents.get(expandedIndex);
				
					if (position >= parent.getExpandStart() && position < parent.getExpandEnd()) {
						//within range, thus this must be the offset
						return position - parent.getExpandStart(); 
					}
				}
				// should never reach here, as all cross ref items should be found in
				// mExpandedCrossRefIndices
				Log.d(CLASS_NAME, "getPositionOffset: Should never reach this line. position=" + position);
				return 0; 
				
			}
			}
		}
		
		/** 
		 * Returns the item view type based on position.
		 * @param position
		 * @return
		 */
		synchronized public int getItemViewType(final int position) {
			if (position == 0) {
				return TYPE_DEFINITION;
			}
			
			if (mExpandedCrossRefIndices.size() <= 0) {
				//nothing is expanded so it can only be titles
				return TYPE_CROSS_REFERENCE_PARENT;
			}
			
			for (Integer expandedIndex : mExpandedCrossRefIndices) {
				CrossReferenceParent parent = mCrossRefParents.get(expandedIndex);
				
				if (position <= parent.mPosition) {
					//if position is before/equal to the current parent position
					return TYPE_CROSS_REFERENCE_PARENT;
				} else if (position < parent.getExpandEnd()) {
					//if within the length of cross-reference 
					return TYPE_CROSS_REFERENCE_ITEM; 
				}
				//continue onto the next expanded item
			}
			
			//if we fall through, it must be a parent
			return TYPE_CROSS_REFERENCE_PARENT;
		}
	}
	
	/** @version 0.2.0-20150823 */
	public static class CrossReferenceParent {
		final CrossReferenceEntry mCrossRefTitle;
		final List<CrossReference> mCrossReferences = new ArrayList<CrossReference>();
		
		/** The current position of the cross reference. */
		private int mPosition = 0;
		/** The error message to display if non-zero. Default is 0. */
		private int mErrorMessage = 0;
		/** Whether this item is currently loading; prevent double clicks. */
		private boolean mIsLoading = false;
		
		public CrossReferenceParent(CrossReferenceEntry crossRefTitle) {
			mCrossRefTitle = crossRefTitle;
		}
		
		public int getExpandStart() {
			return mPosition + 1;
		}
		/** @return start + length */
		public int getExpandEnd() {
			return mPosition + mCrossReferences.size() + 1;
		}
		
		@Override
		public String toString() {
			return super.toString() + "[cross-refs: " + mCrossReferences.size() + "]";
		}
	}
	
	/**Assumes layout {@link R.layout#list_item_word_study_definition}.
	 * @version 0.2.0-20150820 */
	public static class StrongsDefinitionViewHolder extends RecyclerView.ViewHolder {
		
		public final TextView strongNumbersTitle;
		
		public final TextView partOfSpeech;
		public final TextView wordOrigin;
		public final TextView ipdDefinition;	
		public final TextView transliterationPhonetic;
		
		public StrongsDefinitionViewHolder(View rootView) {
			super(rootView);
			transliterationPhonetic = findTextView(rootView, R.id.ibs_wordStudyDef_text_transliterationPhonetic);
			strongNumbersTitle = findTextView(rootView, R.id.ibs_wordStudyDef_title_strongsNumber);
			partOfSpeech = findTextView(rootView,  R.id.ibs_wordStudyDef_text_partOfSpeech);
			wordOrigin = findTextView(rootView,  R.id.ibs_wordStudyDef_text_origin);
			ipdDefinition = findTextView(rootView,  R.id.ibs_wordStudyDef_text_ipdDefinition);
		}
	}
	
	/**Assumes layout {@link R.layout#list_item_word_study_crossref_parent}. 
	 * @version 0.2.0-20150823 */
	public static class CrossReferenceParentViewHolder extends RecyclerView.ViewHolder {
		public final CheckBox crossReferenceTitle;
		public final TextView statusMessage;
		public final View progressBar;
		
		public final View rootView;
		
		public CrossReferenceParentViewHolder(View rootView) {
			super(rootView);
			this.rootView = rootView;
			this.crossReferenceTitle = (CheckBox) rootView.findViewById(R.id.list_item_wordStudyCrossRef_parent_checkbox);
			this.statusMessage = findTextView(rootView, R.id.list_item_wordStudyCrossRef_parent_message);
			this.progressBar = rootView.findViewById(R.id.list_item_wordStudyCrossRef_parent_progess);
		}
	}
	
	/**Assumes layout {@link R.layout#list_item_word_study_cross_reference}. 
	 * @version 0.1.0-20150821 */
	public static class CrossReferenceItemViewHolder extends RecyclerView.ViewHolder {
		public final TextView verseReference;
		public final TextView verseText;
		
		public final View rootView;
		
		public CrossReferenceItemViewHolder(View rootView) {
			super(rootView);
			this.rootView = rootView;
			this.verseReference = findTextView(rootView, R.id.list_item_wordStudyCrossRef_verseReference);
			this.verseText = findTextView(rootView, R.id.list_item_wordStudyCrossRef_verseText);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.2.0-20150824 */
	public static interface OnAdapterInteractionListener {
		public void onCrossReferences(final CrossReferenceParent crossRefParent, final CrossReferenceEntry crossRef);
		public void onStrongsNumber(final String strongsNumber, final String lang);
	}

}
