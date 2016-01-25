package com.inductivebiblestudyapp.ui.fragments.profile.bible;

import java.util.ArrayList;
import java.util.List;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.DivisionTheme;
import com.inductivebiblestudyapp.data.model.MarkingItem;
import com.inductivebiblestudyapp.ui.style.span.NoStyleClickableSpan;
import com.inductivebiblestudyapp.ui.style.span.ObjectEnabledCallback;
import com.inductivebiblestudyapp.ui.style.span.ToggleVisibilitySpan;
import com.inductivebiblestudyapp.ui.style.span.VerseNumberSpan;
import com.inductivebiblestudyapp.util.BitmapRecycler;
import com.inductivebiblestudyapp.util.RecyclableImageViewAware;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * A manager whose purpose to manage the creation, positioning and functionality
 * of the division theme buttons, notes buttons as they are overlayed on the verse.
 * 
 * Call {@link #recycleBitmaps()} to recycle all bitmaps.
 * @author Jason Jenkins
 * @version 0.5.3-20150831
 */
class ChapterOverlayManager {
	
	final TextView mChapterText;
	final ViewGroup mParentContainer;
	final OnOverlayUpdateListener mUpdateListener;
	
	private final List<WrapperAdapter> mUnaddedItems = new ArrayList<WrapperAdapter>();
	private final List<WrapperAdapter> mAddedItems = new ArrayList<WrapperAdapter>();
	
	final BitmapRecycler mBitmapRecycler = new BitmapRecycler();
	
	/**
	 * 
	 * @param chapterTextView The view with which to set the position of elements.
	 * @param parent The container to position said elements within.
	 * @param listener The listener for when overlay needs to send an update.
	 */
	public ChapterOverlayManager(TextView chapterTextView, ViewGroup parent, OnOverlayUpdateListener listener) {
		this.mChapterText = chapterTextView;
		this.mParentContainer = parent;
		this.mUpdateListener = listener;
	}
	
	/** Adds a division theme to the list of unadded items to list . */
	public void addDivisionTheme(DivisionThemeWrapper wrapper) {
		wrapper.setOnOverlayUpdateListener(mUpdateListener);
		mUnaddedItems.add(wrapper);
	}
	
	/** Adds a note to the list of unadded items to list . */
	public void addNote(NoteWrapper wrapper) {
		mUnaddedItems.add(wrapper);
	}
	
	/** Adds an image item to the list of unadded items to list . */
	public void addImageMarking(ImageMarkingWrapper wrapper) {
		wrapper.setBitmapRecycle(mBitmapRecycler); //important
		mUnaddedItems.add(wrapper);
	}
	
	/** Removes all overlay views from parent & keeps them to re-apply.
	 * Does NOT recycle images.
	 * Note: Must be done from UI thread. */
	public void clearViews() {
		while (!mAddedItems.isEmpty()) {
			WrapperAdapter item = mAddedItems.remove(0);
			mParentContainer.removeView(item.getView(mParentContainer));
			mUnaddedItems.add(item);
			if (item instanceof ImageMarkingWrapper) {
				((ImageMarkingWrapper) item).cancelImage(); 
			}			
		}
	}
	
	/** Clears ALL items from the manager & from parent view. 
	 * Recycles all images.
	 * Note: can be done from any thread.*/
	public void clearAll() {
		mParentContainer.post(new Runnable() {			
			@Override
			public void run() {
				clearViews();
				mUnaddedItems.clear();
				mAddedItems.clear();

				mBitmapRecycler.recycleAll();
			}
		});
	}
	
	/** Destroys all bitmaps regardless of visibility. */
	public void recycleBitmaps() {
		for (WrapperAdapter item : mAddedItems) {
			if (item instanceof ImageMarkingWrapper) {
				((ImageMarkingWrapper) item).cancelImage(); 
			}
		}
		for (WrapperAdapter item : mUnaddedItems) {
			if (item instanceof ImageMarkingWrapper) {
				((ImageMarkingWrapper) item).cancelImage(); 
			}
		}	
		mParentContainer.postDelayed(new Runnable() {
			@Override
			public void run() { //reduce the chances of redrawing recycled bitmaps
				mBitmapRecycler.recycleAll();
			}
		}, 500);
	}
	
	/*
	 * 
	 * TODO recycle views more efficiently, perhaps using a scrollview listener etc.
	 * 
	 */
	
	/** Adds views whenever possible to parent, based on chapterTextView content. 
	 * Note: Must be done from UI thread. 
	 * */
	public void addViews() {
		final String text =  mChapterText.getText().toString();
		final int textLength = text.length();
		
		boolean indexWithinText = true;
				
		while (indexWithinText && !mUnaddedItems.isEmpty()) {			
			final int textIndex =  mUnaddedItems.get(0).getIndex();
			indexWithinText = textIndex <= textLength;
			
			if (indexWithinText) {
				WrapperAdapter item = mUnaddedItems.remove(0);
				
				View view = item.getView(mParentContainer);
				if (view.getParent() == null) {
					mParentContainer.addView(view);
				}
				
				item.positionView(mChapterText, textLength);
				mAddedItems.add(item);
			}	
		}		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Gets the pixel position of the top of the line, given the index and length of
	 * text.
	 * @param textView
	 * @param index
	 * @param length
	 * @return
	 */
	/*default*/ static int getLineTop(TextView textView, int index, int length) {
		int line = getLineNumber(textView, index, length);
		return textView.getLayout().getLineTop(line);
		
	}
	
	/** Gets the pixel position of the ascent of the line, given the index and length of
	 * text.
	 * @param textView
	 * @param index
	 * @param length
	 * @return The negative ascent value, as is paint convention
	 */
	/*default*/ static int getLineAscent(TextView textView, int index, int length) {
		int line = getLineNumber(textView, index, length);
		return textView.getLayout().getLineAscent(line);
		
	}
	
	/** Gets the pixel position of the descent of the line, given the index and length of
	 * text.
	 * @param textView
	 * @param index
	 * @param length
	 * @return The negative descent value, as is paint convention
	 */
	/*default*/ static int getLineDescent(TextView textView, int index, int length) {
		int line = getLineNumber(textView, index, length);
		return textView.getLayout().getLineDescent(line);
		
	}

	/** Gets the pixel position of the baseline of the line, given the index and length of
	 * text.
	 * @param textView
	 * @param index
	 * @param length
	 * @return
	 */
	/*default*/ static int getLineBaseline(TextView textView, int index, int length) {
		int line = getLineNumber(textView, index, length);
		return textView.getLayout().getLineBaseline(line);
		
	}
	
	/** Gets the line number. */
	/*default*/  static int getLineNumber(TextView textView, int index, int length) {
		int line = 0;
		if (index >= length) {
			line = textView.getLayout().getLineForOffset(index-1);
		} else {
			line = textView.getLayout().getLineForOffset(index);
		}
		return line;
	}
	
	
	
	/** Takes view and aligns the content to top, returning the {@link MarginLayoutParams} */
	/*default*/ static MarginLayoutParams setAlignTop(View view) {
		return setAlign(view, true);
	}
	
	/** Takes view and aligns the content to top/bottom depending on the bool, 
	 * returning the {@link MarginLayoutParams} */
	/*default*/ static MarginLayoutParams setAlign(View view, boolean top) {
		LayoutParams params = view.getLayoutParams();
		MarginLayoutParams marParams = null;
		if (params instanceof FrameLayout.LayoutParams) {
			FrameLayout.LayoutParams frameParams = (FrameLayout.LayoutParams) params;
			frameParams.gravity = top ? Gravity.TOP : Gravity.BOTTOM;
			marParams = frameParams;
			
		} else if (params instanceof RelativeLayout.LayoutParams) {
			RelativeLayout.LayoutParams relParams = (RelativeLayout.LayoutParams) params;
			relParams.addRule(top ? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
			marParams = relParams;
			
		} else {
			marParams = (MarginLayoutParams) params;
		}
		return marParams;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.4.0-20150726 */
	private static interface WrapperAdapter {
		/** Gets view if built, if not builds first then gets. 
		 * @return The previously/newly built view. */ 
		public View getView(ViewGroup parent);		
		/** @return The index of text relating to the positioning of the item */
		public int getIndex();
		/** 
		 * Positions the view based upon the text position.
		 * @return <code>true</code> if positioned, <code>false</code> if not.
		 */
		public boolean positionView(TextView verseTextView, int textLength);
	}

	/** Contains the means to create & populate the note view, as well as perform functionality. 
	 * @version 0.3.0-20150803
	 * */
	public static class DivisionThemeWrapper implements WrapperAdapter, OnCheckedChangeListener, ObjectEnabledCallback {
		final int mStartIndex;
		final String mDivThemeText;
				
		private CheckBox mView = null;
		private OnOverlayUpdateListener mListener = null;
		
		private int mTopMargin = 0;
		private boolean mSetMargin = false;
		
		private boolean mIsExpanded = true;
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// End members
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		public DivisionThemeWrapper(int startIndex, DivisionTheme divisionTheme) {
			this.mStartIndex = startIndex;
			this.mDivThemeText = divisionTheme.getText();
		}

		
		/** 
		 * Sets the spans for the verse number & the verse body. To be hidden/disabled
		 * when the division theme is toggled.
		 *  */
		public void addSpans(VerseNumberSpan verseNumber, ToggleVisibilitySpan verse, NoStyleClickableSpan clickableSpan) {
			verseNumber.setEnabledCallback(this);
			verse.setVisibilityCallback(this);
			clickableSpan.setEnabledCallback(this);
		}
		
		/** Add a note to the division theme to hide/show when pressed. */
		public void addNotes(NoteWrapper wrapper) {
			//note that this depends on the views being fetched by getView and repositioned.
			wrapper.setVisibilityCallback(this);
		}
		
		/** Add a image item  to the division theme to hide/show when pressed. */
		public void addImageMarking(ImageMarkingWrapper wrapper) {
			//note that this depends on the views being fetched by getView and repositioned.
			wrapper.setVisibilityCallback(this);
		}
		

		public void setOnOverlayUpdateListener(OnOverlayUpdateListener listener) {
			this.mListener = listener;
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Adapter methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		
 
		@Override
		public View getView(ViewGroup parent) {
			if (mView == null) {
				mView = (CheckBox) LayoutInflater.from(parent.getContext())
							.inflate(R.layout.checkbox_division_theme, parent, false);
				mView.setText(mDivThemeText);
				mView.setChecked(mIsExpanded);
				mView.setOnCheckedChangeListener(this);
			}
			return mView;
		}
		
		@Override
		public int getIndex() {
			return mStartIndex;
		}
		@Override
		public boolean positionView(TextView verseTextView, int textLength) {
			final int y = 	getLineBaseline(verseTextView, mStartIndex, textLength) + 
							getLineAscent(verseTextView, mStartIndex, textLength);
	
			
			if (mView == null) {
				return false;
			}
			MarginLayoutParams marParams = setAlignTop(mView);
			
			if (mTopMargin == 0 && !mSetMargin) { //cache margin for future layouts
				mTopMargin = marParams.topMargin;
				mSetMargin = true;
			}
			
			//offset is the top of the line + the top margin.
			final int top = y + mTopMargin - marParams.bottomMargin;
			
			marParams.setMargins(	marParams.leftMargin, top , 
									marParams.rightMargin, marParams.bottomMargin);
			mView.requestLayout();
			
			return true;
		}
		
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Helper methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		/** Hides/shows the the contents of the division theme, such as subsequent
		 * notes & spans.
		 * 
		 * @param expanded <code>true</code> to show, <code>false</code> to hide.
		 */
		protected void setExpanded(boolean expanded) {
			this.mIsExpanded = expanded;
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Listeners
		////////////////////////////////////////////////////////////////////////////////////////////////


		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			buttonView.setEnabled(false); //prevent double clicks
			setExpanded(isChecked);			
			if (mListener != null) {
				mListener.onOverlayUpdate();
			}
			buttonView.setEnabled(true);
		}

		@Override
		public boolean isObjectEnabled() {
			return mIsExpanded;
		}	
	}
	
	/** Contains the means to create & populate the view, as well as perform functionality. 
	 * @version 0.2.1-20150803
	 * */
	public static class NoteWrapper implements WrapperAdapter, OnClickListener {
		final int mStartIndex;
		final int mVerseIndex;
		final OnNoteClickListener mListener;
		final VerseNumberSpan mVerseNumberSpan;
		
		private View mView = null;
		
		private int mTopMargin = 0;
		private boolean mSetMargin = false;
		
		private ObjectEnabledCallback mVisibilityCallback = null;
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// End members
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		public NoteWrapper(int startIndex, int verseIndex, VerseNumberSpan verseNumber, OnNoteClickListener listener) {
			this.mStartIndex = startIndex;
			this.mVerseIndex = verseIndex;
			this.mListener = listener;
			this.mVerseNumberSpan = verseNumber;
		}
		
		/** Not used. */
		@Deprecated
		public void setVerticalOffset(int offset) {}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Over ride methods
		////////////////////////////////////////////////////////////////////////////////////////////////

		@Override
		public View getView(ViewGroup parent) {
			if (mView == null) {
				mView = LayoutInflater.from(parent.getContext())
							.inflate(R.layout.button_notes, parent, false);
				mView.setOnClickListener(this);				
			}
			if (mVisibilityCallback != null) {
				mView.setVisibility(mVisibilityCallback.isObjectEnabled() ? View.VISIBLE : View.GONE);
			}
			return mView;
		}

		@Override
		public int getIndex() {
			return mStartIndex;
		}

		@Override
		public boolean positionView(TextView verseTextView, int textLength) {
			final int line = getLineNumber(verseTextView, mStartIndex, textLength);
			final int lineBottom = verseTextView.getLayout().getLineBottom(line);
						
			if (mView == null) {
				return false;
			}
			MarginLayoutParams marParams = setAlignTop(mView);
			
			if (mTopMargin == 0 && !mSetMargin) { //cache margin for future layouts
				mTopMargin = marParams.topMargin;
				mSetMargin = true;
			}	
			
			//offset is the top of the line + the top margin.
			int top = lineBottom + mTopMargin;
			
			if (!mVerseNumberSpan.isSingleLine()) {
				final int lineTop = verseTextView.getLayout().getLineTop(line);		
				top += (lineBottom - lineTop); //add another line
			}
			if (mView.getVisibility() != View.VISIBLE) {
				top = 0;
			}
			
			marParams.setMargins(	marParams.leftMargin, top , 
									marParams.rightMargin, marParams.bottomMargin);
			mView.requestLayout();
			
			return true;
		}
		

		@Override
		public void onClick(View v) {
			mListener.onNoteClick(mVerseIndex);
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Helper methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		/** Sets the callback for the note to check whether the view is visible or not. */
		protected void setVisibilityCallback(ObjectEnabledCallback callback) {
			this.mVisibilityCallback = callback;
		}
		
		/** Hides/shows the spans held accountable to this division theme
		 * 
		 * @param visible <code>true</code> to show, <code>false</code> to hide.
		 */
		protected void setVisibility(boolean visible) {
			if (mView != null) {
				mView.setVisibility(visible ? View.VISIBLE : View.GONE);
			}
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// public interfaces
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		public static interface OnNoteClickListener {
			public void onNoteClick(int verseIndex);
		}

		
	}
	
	/** Contains the means to create & populate the image view, as well as perform functionality.  
	 * @version 0.4.1-20150814
	 * */
	public static class ImageMarkingWrapper implements WrapperAdapter, OnClickListener {
		
		final OnImageMarkingClickListener mListener;
		final MarkingItem mMarkingItem;
		
		
		private View mView = null;
		private RecyclableImageViewAware mImgAware = null;
		
		private int mBottomMargin = 0;
		private int mLeftMargin = 0;
		private boolean mSetMargin = false;
		
		private ObjectEnabledCallback mVisibilityCallback = null;

		private BitmapRecycler mBitmapRecycler = null;
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// End members
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		public ImageMarkingWrapper(MarkingItem imageMarking, OnImageMarkingClickListener listener) {
			if (imageMarking.getMarkingType() != MarkingItem.TYPE_IMAGE) {
				throw new IllegalArgumentException("Cannot supply non-image marking");
			}
			this.mMarkingItem = imageMarking;
			this.mListener = listener;
		}
		
		public void cancelImage() {
			if (mImgAware != null) {
				ImageLoader	.getInstance().cancelDisplayTask(mImgAware);
			}
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Override methods
		////////////////////////////////////////////////////////////////////////////////////////////////

		@Override
		public View getView(ViewGroup parent) {
			if (mView == null) {
				mView = LayoutInflater.from(parent.getContext())
							.inflate(R.layout.button_image_marking, parent, false);
				mView.setOnClickListener(this);				
				
				mImgAware = new RecyclableImageViewAware( (ImageButton) mView, false, mBitmapRecycler);				
				ImageLoader	.getInstance()
							.displayImage(mMarkingItem.getImageItem().getUrl(), mImgAware);
				mView.setContentDescription(mMarkingItem.getImageItem().getName());
			}
			if (mVisibilityCallback != null) {
				mView.setVisibility(mVisibilityCallback.isObjectEnabled() ? View.VISIBLE : View.GONE);
			}
			return mView;
		}

		@Override
		public int getIndex() {
			return mMarkingItem.getStartIndex();
		}

		@Override
		public boolean positionView(TextView verseTextView, int textLength) {
			/*
			 * Positioning for images is done bottom-up as it is easier to deal with when
			 * used in conjunction with the division theme items. The div. theme items
			 * add to the ascent of a line and it is cumbersome to calculate the top reliably
			 * with this in play. By measuring from the bottom, the image can be easily placed
			 * regardless of division themes on this verse or not.
			 */
			final int startIndex = mMarkingItem.getStartIndex();
			final int endIndex = mMarkingItem.getEndIndex();
						
			final int line = getLineNumber(verseTextView, startIndex, endIndex - startIndex);
			final int x = (int) verseTextView.getLayout().getPrimaryHorizontal(startIndex);
			final int lineStart = verseTextView.getLayout().getLineStart(line);
			final int xStart =  (int) verseTextView.getLayout().getPrimaryHorizontal(0);
			
			int lineOffset = 0;
			if (lineStart == startIndex - 1 || x == xStart) { //if at line start, offset
				lineOffset = 1;
			}
			
			int diffY = 	verseTextView.getLayout().getLineBaseline(line + lineOffset) + 
							verseTextView.getLayout().getLineDescent(line + lineOffset);
						
			if (mView == null) {
				return false;
			}
			MarginLayoutParams marParams = setAlign(mView, false);
			

			final int y = 	verseTextView.getLayout().getHeight() - diffY;
			
			if (mBottomMargin == 0 && mLeftMargin == 0 && !mSetMargin) { //cache margin for future layouts
				mBottomMargin = marParams.bottomMargin;
				mLeftMargin = marParams.leftMargin;
				mSetMargin = true;
			}	
			
			//offset is the top of the line + the top margin.
			int bottom = y + mBottomMargin;			
			int left = x + mLeftMargin;
			if (mView.getVisibility() != View.VISIBLE) {
				bottom = 0;
				left = 0;
			}
			
			marParams.setMargins(	left, marParams.topMargin , 
									marParams.rightMargin, bottom);
			mView.requestLayout();
			
			return true;
		}
		

		@Override
		public void onClick(View v) {
			mListener.onImageMarkingClick(mMarkingItem);
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Helper methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		protected void setBitmapRecycle(BitmapRecycler bitmapRecycler) {
			mBitmapRecycler = bitmapRecycler;
		}
		
		/** Sets the callback for the note to check whether the view is visible or not. */
		protected void setVisibilityCallback(ObjectEnabledCallback callback) {
			this.mVisibilityCallback = callback;
		}
		
		/** Hides/shows the spans held accountable to this division theme
		 * 
		 * @param visible <code>true</code> to show, <code>false</code> to hide.
		 */
		protected void setVisibility(boolean visible) {
			if (mView != null) {
				mView.setVisibility(visible ? View.VISIBLE : View.GONE);
			}
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// public interfaces
		////////////////////////////////////////////////////////////////////////////////////////////////
		/** @version 0.1.0-20150803 */
		public static interface OnImageMarkingClickListener {
			/**
			 * @param imageMarking The image item clicked on
			 */
			public void onImageMarkingClick(MarkingItem imageMarking);
		}
		
	}	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.1.0-20150724 */
	public static interface OnOverlayUpdateListener {
		public void onOverlayUpdate();
	}
}
