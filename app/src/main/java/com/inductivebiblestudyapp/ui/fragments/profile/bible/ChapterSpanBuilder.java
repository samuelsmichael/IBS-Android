package com.inductivebiblestudyapp.ui.fragments.profile.bible;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint.FontMetricsInt;
import android.text.Html;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager;
import com.inductivebiblestudyapp.data.model.LetteringItem;
import com.inductivebiblestudyapp.data.model.MarkingItem;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse.Verse;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.ChapterOverlayManager.DivisionThemeWrapper;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.ChapterOverlayManager.ImageMarkingWrapper;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.ChapterOverlayManager.NoteWrapper;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.ChapterOverlayManager.ImageMarkingWrapper.OnImageMarkingClickListener;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.ChapterOverlayManager.NoteWrapper.OnNoteClickListener;
import com.inductivebiblestudyapp.ui.style.span.AbstractCustomBackgroundSpan;
import com.inductivebiblestudyapp.ui.style.span.HideParagraphSpan;
import com.inductivebiblestudyapp.ui.style.span.HideSpan;
import com.inductivebiblestudyapp.ui.style.span.NoStyleClickableSpan;
import com.inductivebiblestudyapp.ui.style.span.PaddedBoxSpan;
import com.inductivebiblestudyapp.ui.style.span.ToggleVisibilitySpan;
import com.inductivebiblestudyapp.ui.style.span.VerseNumberSpan;
import com.inductivebiblestudyapp.ui.style.span.VerticalPaddingSpan;
import com.inductivebiblestudyapp.ui.style.span.AbstractCustomBackgroundSpan.OnMeasureCallback;
import com.inductivebiblestudyapp.ui.style.span.PaddedBoxSpan.PreserveHeightCallback;
import com.inductivebiblestudyapp.util.Utility;

/**
 * Builds the spans for the chapter view class. 
 * @author Jason Jenkins
 * @version 0.6.0-20150921
 */
class ChapterSpanBuilder {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = ChapterSpanBuilder.class
			.getSimpleName();	
	
	/** The time between loading chunks for ui smoothness. Tested on Psalms 119 */
	public static final int VERSE_CHUNK_TIME = 10; //ms
	/** The limit of verses before they must be split to be applied. */
	public static final int VERSE_CHUNK_LIMIT = 60;
	
	private static final int SPAN_FLAG = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;

	/** Subtract the heading number from this; such that:
	 * <pre>
	 * &lt;h> == 1.7
	 * &lt;h1> == 1.6
	 * &lt;h2> == 1.5
	 * &lt;h3> == 1.4
	 * &lt;h4> == 1.3
	 * </pre> */
	private static final float HEADING_SCALE_START = 1.7f;
	
	public static final int TYPE_NOTE = 0;
	public static final int TYPE_DIV_THEME = 1;
	public static final int TYPE_MARKING = 2;
	public static final int TYPE_VERSE = 3;
	
	/** Mark used to find the verse number within the verse text, for hiding purposes. */
	private static final String ITEM_MARK =  "[[v]]";
	
	private final Context mContext;
	private final ChapterOverlayManager mOverlayManager;
	private final TextView mChapterVerseContent;
	
	private final int mDimenDivisionThemePadding;
	private final int mDimenMinTouchSize;
	private final int mDimenSmallTouchSize;
	private final int mDimenVerseNumberPadding;
	private final float mDimenVerseNumberTextSize;

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End final members
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	private OnPositionFoundListener mPositionFoundListener = null;
	private String mId = "";
	private int mType = -1;
	
	/** Whether or not the upgrade features such as image & division themes are available. */
	private boolean mUpgradeFeaturesAvailable = false;
	
	public ChapterSpanBuilder(TextView chapterVerseContent, ChapterOverlayManager overlayManager) {
		this.mContext = chapterVerseContent.getContext();
		this.mOverlayManager = overlayManager;
		this.mChapterVerseContent = chapterVerseContent;
		
		this.mDimenDivisionThemePadding = (int) getResources().getDimension(R.dimen.ibs_bible_chapter_verseTopPadding);
		this.mDimenMinTouchSize = (int) getResources().getDimension(R.dimen.min_touch_size);
		this.mDimenSmallTouchSize = (int) getResources().getDimension(R.dimen.small_touch_size);
		this.mDimenVerseNumberPadding = (int)getResources().getDimension(R.dimen.ibs_bible_verseNumber_padding);
		this.mDimenVerseNumberTextSize = getResources().getDimension(R.dimen.ibs_bible_verseNumber_textSize);
	}
	
	/**
	 * Asynchronous calls. Sets the item to find when loading the content.
	 * @param id A supported TYPE
	 * @param type
	 * @param listener
	 * @throws IllegalArgumentException If the type is invalid.
	 */
	public void find(String id, int type, OnPositionFoundListener listener) {
		if (	type != TYPE_NOTE && type != TYPE_DIV_THEME && type != TYPE_MARKING &&
				type != TYPE_VERSE ) {
			throw new IllegalArgumentException("Type '"+type+"' is not supported");
		}
		this.mId = id;
		this.mPositionFoundListener = listener;
		this.mType = type;
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// start Wrapper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Convenience method. */
	private Resources getResources() {
		return mContext.getResources();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// end wrapper methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** Builds and returns the {@link SpannableString} {@link List}, using the given response. */
	public List<SpannableString> build(final BibleVerseResponse response, final List<Integer> verseIndexStarts,
			final OnVerseInteractionListener verseListener, final OnNoteClickListener noteListener, 
			final OnImageMarkingClickListener imageMarkingListener) {
		
		mUpgradeFeaturesAvailable = UpgradeBillingManager.isUpgradeComplete(mContext);
		
		List<MarkingItem> markings = new ArrayList<MarkingItem>();
		for (MarkingItem markingItem : response.getMarkings()) {
			markings.add(markingItem);
		}
		Collections.sort(markings, mMarkingsSort);
		
		final Verse[] verses = response.getVerses();
		List<SpannableString> spanList = new ArrayList<SpannableString>();
		
		SpannableString spanString = null;
		List<String> chapterChunkList = new ArrayList<String>();
		
		final int SIZE = verses.length;		
		Log.d(CLASS_NAME, "Verse Size: "+SIZE);
		
		
		for (int round = 0; round < 2; round++) {
			
			if (round > 0 && !chapterChunkList.isEmpty()) { 
				//create new span from the first bucket
				spanString = new SpannableString(Html.fromHtml(chapterChunkList.remove(0)));
			}
			
			/** Used to place the first line of every verse for the FULL text. */ 
			int verseTotalStart = 0;
			/** Used to specify the verse start for this bucket. */
			int verseStart = 0;
			
			String chapterChunk = "";
			DivisionThemeWrapper divisionThemeWrapper = null;
			
			int chunkOffset = 0;
			
			for (int verseIndex = 0; verseIndex < SIZE; verseIndex++ ) {
				final Verse verse = verses[verseIndex];
				final String verseOut = verse.getText();

				final boolean showHeadings = verse.isShowingHeadings();
				
				if (0 == round) {
					chapterChunk = addVerseAndChunk(chapterChunkList,
							chapterChunk, verseOut, verseIndex, showHeadings);
				} else {
					//register verse start
					verseIndexStarts.add(verseTotalStart);
					
					final int verseLength = Html.fromHtml(updateHtmlTags(verseOut, false)).length(); 
					final int verseEnd =  verseStart + verseLength;

					checkIdForMatch(verse.getVerseId(), TYPE_VERSE, verseTotalStart);
					
					// create spans here					
					final ToggleVisibilitySpan toggleSpan = new ToggleVisibilitySpan();
					//set toggle first so that division theme heights are kept.
					spanString.setSpan(toggleSpan, verseStart, verseEnd, SPAN_FLAG); 
					
					final int[] verseNum = hideVerseNumber(verse, verseOut, spanString, verseStart);
					if (verseNum[0] < verseTotalStart) {
						//if a new chunk, add offset
						verseNum[0] += chunkOffset;
						verseNum[1] += chunkOffset;
					}
					//hide/style headings first so that they do not effect division themes
					restyleHeadings(verse, verseOut, spanString, verseStart, showHeadings);
					
					
					final VerseNumberSpan verseMarginSpan = 
							new VerseNumberSpan(verse.getNumber(),
									verseNum[0],
									mDimenVerseNumberPadding,
									mDimenVerseNumberPadding, 
									mDimenVerseNumberTextSize);
					
					final NoStyleClickableSpan clickSpan = new NoStyleClickableSpan(new NoStyleClickableSpan.OnClickListener() {						
						@Override
						public void onClick(View widget) {
							verseListener.onVerseClick(widget, verse);
						}
					});
					
					
					//check themes & notes 
					boolean divisionThemeAtThisVerse = false;
					
					if (verse.getDivisionTheme() != null && mUpgradeFeaturesAvailable) { //if we have a division theme
						if (divisionThemeWrapper != null) { 
							//if we previously had one, add to manager
							mOverlayManager.addDivisionTheme(divisionThemeWrapper);
						}
						checkIdForMatch(verse.getDivisionTheme().getDivisionThemeId(), TYPE_DIV_THEME, verseTotalStart);
						
						//the verse start to use. If we aren't using headings, align to verse start
						final int vStart = showHeadings ? verseTotalStart : verseNum[0];
						
						divisionThemeWrapper = 
								new DivisionThemeWrapper(vStart, verse.getDivisionTheme());
						divisionThemeAtThisVerse = true;
					}
					if (divisionThemeWrapper != null) { 
						divisionThemeWrapper.addSpans(verseMarginSpan, toggleSpan, clickSpan);
					}
					
					if (verse.getNote() != null) { //if we have a note
						
						checkIdForMatch(verse.getNote().getNoteId(), TYPE_NOTE, verseTotalStart);
						
						NoteWrapper noteWrapper = 
								new NoteWrapper(verseNum[0], verseIndex, verseMarginSpan, noteListener);
						//set the offset if there also a division theme using a height offset
						//noteWrapper.setVerticalOffset(divisionThemeAtThisVerse ? mDimenDivisionThemePadding : 0);
						
						if (divisionThemeWrapper != null) {
							divisionThemeWrapper.addNotes(noteWrapper);
						}
						mOverlayManager.addNote(noteWrapper);
						
						verseMarginSpan.setMinimumVerseHeight(
								verseMarginSpan.getNumberHeight(mChapterVerseContent.getPaint()) + 
								+ mDimenMinTouchSize
								);
					}
					
					PaddedBoxSpan.PreserveHeightCallback preserveHeightCallback = null;
					
					if (divisionThemeAtThisVerse) { //if division theme add padding.
						final int topPad = mDimenDivisionThemePadding; 						
						VerticalPaddingSpan padding = new VerticalPaddingSpan(topPad, 1);
						
						//the relative start of this chunk.
						final int vStart = showHeadings ? verseStart : verseNum[0] - chunkOffset;
						
						//only apply to ~ 7 characters (this should include verse number + some text)						
						int end = vStart + 5; 
						
						final int firstVEnd = verseEnd < end ? verseEnd : end;
						
						spanString.setSpan(padding,  vStart, firstVEnd, SPAN_FLAG);
						
						//create the callback once per verse, never more.
						preserveHeightCallback = new PaddedBoxSpan.PreserveHeightCallback() {								
							@Override
							public boolean preserveHeight(CharSequence text, int start, int end,
									int spanstartv, int v, FontMetricsInt fm) {								
								//if this line has vertical padding, anywhere on the line.
								//We want to preserve that height								
								return (start <= vStart && firstVEnd <= end);

							}
						};
					}
					
					int rightAlignOffset = testForHebrew(verseOut, verseStart);
					
					//	markings should be applied before click spans
					
					for (int markIndex = 0; markIndex < markings.size(); markIndex++) {
						MarkingItem currentMarking = markings.get(markIndex);
						
						try {
							if ( applyMarking(currentMarking, spanString, verseTotalStart,
										verseLength, chunkOffset, rightAlignOffset,
										verse,  
										verseListener, imageMarkingListener,
										divisionThemeWrapper, 
										preserveHeightCallback ) ) {
								//if applied, remove and step back
								markings.remove(markIndex);
								markIndex -= 1;
							} 
						} catch (IndexOutOfBoundsException e) {
							Log.e(CLASS_NAME, 
									String.format("Failed to apply marking (%s) at index[%d]", 
											currentMarking.toString(), markIndex), e);
							//e.printStackTrace();
						}
						
					}
					
					
					spanString.setSpan(clickSpan, verseStart, verseEnd, SPAN_FLAG); 
					spanString.setSpan(verseMarginSpan, verseStart, verseEnd, SPAN_FLAG);
					spanString.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_NORMAL), verseStart, verseEnd, SPAN_FLAG);
				
					
					
					if (isChunkable(verseIndex) && !chapterChunkList.isEmpty()) {
						//if we are at a chunk, limit and have buckets left
						//add a chunk to the list and reset our remaining span size
						//Log.d(CLASS_NAME, "Chunked at: " + index);
						chunkOffset += spanString.length();
						
						spanList.add(spanString);
						spanString = new SpannableString(Html.fromHtml(chapterChunkList.remove(0)));
						verseStart = 0;
					} else { //otherwise, keep adding to this chunk
						verseStart = verseEnd;
					}
					verseTotalStart += verseLength;
				}				
			}
			
			if (divisionThemeWrapper != null) { 
				//if we previously had one, add to manager
				mOverlayManager.addDivisionTheme(divisionThemeWrapper);
			}
			
			if (round == 0) {
				//add remaining string to bucket
				chapterChunkList.add(chapterChunk);
			}
		}
		
		if (spanString.length() > 0) { //add the last chunk.
			spanList.add(spanString);
		}
		
		return spanList;
	}

	

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Checks id & type and notifies the position listener, if any. */
	private void checkIdForMatch(final String id, final int type, final int textIndex) {
		if (mPositionFoundListener != null) {
			if (type == mType && mId.equals(id)) {
				switch (type) {
				case TYPE_NOTE:
				case TYPE_DIV_THEME:
				case TYPE_MARKING:
				case TYPE_VERSE:
					mPositionFoundListener.onPositionFound(mId, textIndex);
					break;
				}
			}
		}
	}
	
	/** Add verse to the chapterChunk, adding to chunk bucket when necessary. */
	private String addVerseAndChunk(List<String> chapterChunkList,
			String chapterChunk, final String verseOut, int verseIndex, boolean showHeadings) {
		final String verseString = updateHtmlTags(verseOut, showHeadings); 
				
		chapterChunk += verseString;
								
		if (isChunkable(verseIndex)) {
			chapterChunkList.add(chapterChunk); //add a chunk to the bucket
			chapterChunk = "";			
		}
		
		//Log.d(CLASS_NAME, "Verse " + verse.getNumber() + ": " + verse.getText());
		//Log.d(CLASS_NAME, "Verse (raw)" + verse.getNumber() + ": " + verse.getRawText());
		//Log.d(CLASS_NAME, "Verse (out)" + verse.getNumber() + ", " + verseString);
		return chapterChunk;
	}
	
	
	/*
	 * Please note:
	 * Previous versions of applyMarking WILL have a different ordering of arguments.
	 * Beware of previous versions.
	 */
	
	/**
	 * Attempts to apply a marking either fully or partially. If the marking is not fully applied
	 * it returns <code>false</code>, otherwise returns <code>true</code> when applied.
	 * @param currentMarking
	 * @param spanString
	 * @param verseTotalStart The total start of the verse, regardless of chunk
	 * @param chunkOffset The offset that helps calculate positions in the current chunk	  
	 * @param rightAlignOffset The offset for any right aligned lines, -1 if unset.	 
	 * @param verse <b>cannot</b> be <code>null</code>
	 * @param verseListener <b>cannot</b> be <code>null</code>
	 * @param imageMarkingListener <b>cannot</b> be <code>null</code> 
	 * @param divisionThemeWrapper can be <code>null</code>
	 * @param preserveHeightCallback can be <code>null</code>
	 * @return
	 */
	private boolean applyMarking(final MarkingItem currentMarking,
			final SpannableString spanString, final int verseTotalStart,
			final int verseLength, final int chunkOffset, final int rightAlignOffset,
			final Verse verse,  
			final OnVerseInteractionListener verseListener,
			final OnImageMarkingClickListener imageMarkingListener,
			final DivisionThemeWrapper divisionThemeWrapper,
			final PreserveHeightCallback preserveHeightCallback) {
		
		final int verseStart = verseTotalStart - chunkOffset;
		final int verseEnd = verseStart + verseLength;
		
		final MeasureCallback measureCallback = // remember: the right align needs the total position, thus add the chunk offset
				newMeasureCallback(mDimenVerseNumberPadding * 2, rightAlignOffset + chunkOffset);

		int start = currentMarking.getStartIndex() - chunkOffset;
		int end = currentMarking.getEndIndex() - chunkOffset;
		
		if (currentMarking != null) {
			switch (currentMarking.getMarkingType()) {
			
			case MarkingItem.TYPE_LETTERING: {
				LetteringItem lettering = currentMarking.getLetteringItem();
				
				
				boolean isFirstRun = true; 
				if (verseStart > start) { //we must be continuing a previous marking
					start = verseStart; //adjust to new start
					isFirstRun = false;
				}
				
				if (verseStart <= start && start < verseEnd) { //we are starting a marking
					if (isFirstRun) {
						//only check once
						checkIdForMatch(currentMarking.getId(), TYPE_MARKING, currentMarking.getStartIndex());
						
					}
					
					if (end > verseEnd) { //if marking spans multiple verses, return false.
						Utility.buildStyleFromLettering(lettering)
								.setSpanFlag(SPAN_FLAG)							
								.setOnMeasureCallback(measureCallback)
								.setEnabedCallback(divisionThemeWrapper)
								.apply(spanString, start, verseEnd, verseStart, verseEnd, chunkOffset);
						applyLetteringClickSpan(
									newLetteringClickSpan(currentMarking, verse, divisionThemeWrapper,
											verseListener),
								spanString, start, verseEnd, SPAN_FLAG);						
						return false;
						
					} else if (end > spanString.length()) {
						Log.e(CLASS_NAME, "Skipped: " + spanString.subSequence(start, spanString.length()));
						//we can neither apply nor continue, jump back for a better offset
						return false;
					} else { //if within this verse only, clear it
						Utility.buildStyleFromLettering(lettering)
								.setSpanFlag(SPAN_FLAG)						
								.setOnMeasureCallback(measureCallback)
								.setEnabedCallback(divisionThemeWrapper)
								.apply(spanString, start, end, verseStart, verseEnd, chunkOffset);

						applyLetteringClickSpan(
									newLetteringClickSpan(currentMarking, verse, divisionThemeWrapper,
											verseListener),
								spanString, start, end, SPAN_FLAG);
						return true;
					}								
				}
			}
				break;

			case MarkingItem.TYPE_IMAGE: {
				if (!mUpgradeFeaturesAvailable) {
					return true;
				}
				
				if (verseStart <= start && start < verseEnd) { //we are starting an image marking
					checkIdForMatch(currentMarking.getId(), TYPE_MARKING, verseTotalStart);
					
					//add the text space
					int size = (int) (mDimenSmallTouchSize * 0.8f);
					PaddedBoxSpan padSpan = new PaddedBoxSpan(mDimenSmallTouchSize, size, divisionThemeWrapper)
						.restoreStyles(spanString, start, start+1);
						
					spanString.setSpan(
							padSpan, 
							start, 
							start+1, SPAN_FLAG);
					
					ImageMarkingWrapper imageMarking = 
							new ImageMarkingWrapper(currentMarking, imageMarkingListener);
														
					mOverlayManager.addImageMarking(imageMarking);
					if (divisionThemeWrapper != null) {
						divisionThemeWrapper.addImageMarking(imageMarking);
						if (divisionThemeWrapper.getIndex() == verseTotalStart) {
							//we are on this line, remove max height
							padSpan.setPreserveHeightCallback(preserveHeightCallback);
						}
					}
					
					return true; //clear image
				}
			}
			
			}
		}
		return false;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Uses in {@link #addVerseAndChunk(List, String, String, int)} to update the tags.
	 * @param showHeadings BEWARE of using this as hard-coded false for strings NOT
	 * being used for measuring. BEWARE of using this as false for display strings */
	private static String updateHtmlTags(final String verseOut, boolean showHeadings) {
		String result =	verseOut.replaceAll("<sup.*>(?=\\d)", "").replaceAll("</sup>", "");
		
		result = result.replaceAll("<h\\d?.*?>(?=.)", "<b>")
				//add a new line to replace the previous indent from headings
				.replaceAll("</h\\d?>", "</b><br />");		
		return result;
	}
	
	/**
	 * 
	 * @param margin The left/right margin to apply
	 * @param rightAlignStart The lines that are right aligned -1 if none
	 * @return
	 */
	private static MeasureCallback newMeasureCallback(int margin, int rightAlignStart) {
		MeasureCallback callback = new MeasureCallback(margin, rightAlignStart);
		return callback;
	}
	
	/** 
	 * Applies lettering span, re-shuffling underlying click spans when necessary.
	 * @param clickable
	 * @param spanString
	 * @param start
	 * @param end
	 * @param flag
	 */
	private static void applyLetteringClickSpan(final ClickableSpan clickable, 
			final SpannableString spanString, final int start, final int end, 
			final int flag) {
		
		//get prexisting clicks
		final ClickableSpan[] preExisingClicks = spanString.getSpans(start, end, ClickableSpan.class);
		
		spanString.setSpan(clickable, start, end, SPAN_FLAG);
		
		final int SIZE = preExisingClicks.length;
		for (int index = 0; index < SIZE; index++) {
			ClickableSpan internalClick = preExisingClicks[index];
			
			final int internalStart = spanString.getSpanStart(internalClick);
			final int internalEnd = spanString.getSpanEnd(internalClick);
			final int flags = spanString.getSpanFlags(internalClick);
			
			//visual spans applied on same text are *least* clickable when applied last but most visible 
			//remove and re-add older spans. Thus the new one is first, making it more clickable
			spanString.removeSpan(internalClick); 
			spanString.setSpan(internalClick, internalStart, internalEnd, flags);
		}
	}

	/**
	 * Creates a new click span for letterings.
	 * @param currentMarking
	 * @param verse
	 * @param divisionThemeWrapper can be <code>null</code>
	 * @param verseListener
	 * @return
	 */
	private static NoStyleClickableSpan newLetteringClickSpan(
			final MarkingItem currentMarking, final Verse verse,
			final DivisionThemeWrapper divisionThemeWrapper,
			final OnVerseInteractionListener verseListener) {
		NoStyleClickableSpan clickSpan = new NoStyleClickableSpan(new NoStyleClickableSpan.OnClickListener() {							
			@Override
			public void onClick(View widget) {
				verseListener.onLetteringClick(widget, verse, currentMarking);
			}
		});
		clickSpan.setEnabledCallback(divisionThemeWrapper);
		return clickSpan;
	}

	/** Calculates the position of Hebrew lines (if any) and returns position. 
	 * @return -1 on failure, postive index on success */
	private static int testForHebrew(final String verseOut, int verseStart) {
		final String verseReplace = Html.fromHtml(
				verseOut.replaceAll("<p class=\"qa\">", ITEM_MARK)).toString();					
		return verseReplace.indexOf(ITEM_MARK) + verseStart;
	}
	
	/** Calculates the position of headings (if any) and restyles them with spans
	 * for the purposes of measuring in {@link AbstractCustomBackgroundSpan}.
	 * @return <code>true</code> if headings were restyles, <code>false</code> otherwise  */
	private static boolean restyleHeadings(final Verse verse, final String verseOut,
			SpannableString spanString, int verseStart, boolean showHeading) {
		try {
			boolean headingsFound = false;
			//break the heading tags & strip all other html entities
			String workingVerse = Html.fromHtml(verseOut.replaceAll("<h", ">h").replaceAll("</h", ">/h")).toString();
			
			while (true) { //loop, until there are no more headings
				int headingTagStart = workingVerse.indexOf(">h");
				
				if (headingTagStart < 0) { //not found
					return headingsFound; //exit
				}
				
				workingVerse = workingVerse.replaceFirst(">h\\d?.*?>(?=.{0,3}[A-Z])", ""); //measuring
				final int headingTagEnd = workingVerse.indexOf(">/h");
				
				//finds the level of heading and applies it.
				Matcher matcher = Pattern.compile(">/h(\\d)?>").matcher(workingVerse);		
				float scaleOffset = 0.0f;
				if (matcher.find()) {
					try {
						//if we find a number, apply an offset
						float num = Integer.valueOf(matcher.group(1)); //gets the number
						scaleOffset = num/10;
					} catch (NumberFormatException e) {}
				}
				
				//insert newline, remove tag
				workingVerse = 	workingVerse.substring(0, headingTagEnd) + "\n" + 
								matcher.replaceFirst("").substring(headingTagEnd);
				
				final float scale = HEADING_SCALE_START - scaleOffset;
				
				if (headingTagEnd >= 0) {//add heading span
					final int headingStart = verseStart + headingTagStart;
					final int headingEnd = verseStart + headingTagEnd;
					if (showHeading) {
						spanString.setSpan(
								new RelativeSizeSpan(scale), 
								headingStart, 
								headingEnd, 
								SPAN_FLAG);
					} else {
						int newLineOffset = 1;
						if (workingVerse.indexOf("\n\n") == headingTagEnd) {
							newLineOffset = 2; //hide the extra newline as well
						}
						spanString.setSpan(
								new HideParagraphSpan(), 
								headingStart, 
								headingEnd + newLineOffset, 
								SPAN_FLAG);
					}
					headingsFound = true;
				}
			}
		} catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("restyleHeadings: ['" + verseOut + "'] ->" + e.getMessage());
		}
	}
	
	/** Calculates the position of and hides the verse number from the output. 
	 * @return The verse number offset [start, end]  */
	private static int[] hideVerseNumber(final Verse verse, final String verseOut,
			SpannableString spanString, int verseStart) {
		final String verseReplace = Html.fromHtml( 
						//BEWARE: of this line; do not use false with a non-measuring result
						updateHtmlTags( verseOut.replaceAll("<sup.*sup>", ITEM_MARK), false )
					).toString();					
		final int verseNumberOffset = verseReplace.indexOf(ITEM_MARK);
		
		if (verseNumberOffset >= 0) {//hide verse number
			//Log.d(CLASS_NAME, "Verse verseReplace: " + verseReplace);
			//Log.d(CLASS_NAME, "Verse verseNumberOffset: " + verseNumberOffset);
			final int verseNumStart = verseStart + verseNumberOffset;
			final int verseNumEnd = verseNumStart + verse.getNumber().length();
			spanString.setSpan(new HideSpan(), verseNumStart, verseNumEnd, SPAN_FLAG);
			
			return new int[]{verseNumStart, verseNumEnd};
		}
		return new int[]{0, 0};
	}
	
	/** Whether index signifies a chunkable portion of text. */
	private static boolean isChunkable(int verseIndex) {
		return verseIndex > 0 && verseIndex % VERSE_CHUNK_LIMIT == 0;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Implemented interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	/** Sorts to apply the letterings first, then images, then the least recent first, most recent last. */
	private Comparator<MarkingItem> mMarkingsSort = new Comparator<MarkingItem>() {
		
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
		public int compare(MarkingItem lhs, MarkingItem rhs) {
			//first sort letterings to be first
			if (lhs.getMarkingType() == MarkingItem.TYPE_LETTERING && rhs.getMarkingType() == MarkingItem.TYPE_IMAGE) {
				//left first
				return -1; 
			} else if (rhs.getMarkingType() == MarkingItem.TYPE_LETTERING && lhs.getMarkingType() == MarkingItem.TYPE_IMAGE) {
				//right first
				return 1;
			}
			
			//next, sort those with the smallest ids first
			return compareIds(lhs.getId(), rhs.getId());
		}
		
		/** Parses and compares the ids if possible.
		 * If the id is larger, put to top, otherwise put to bottom.
		 *  */
		/*default*/ int compareIds(String lhsId, String rhsId) {
			if (lhsId == null || lhsId.isEmpty()) {
				return -1;
			}
			if (rhsId == null || rhsId.isEmpty()) {
				return 1;
			}
			final int left =  Integer.parseInt(lhsId);
			final int right =  Integer.parseInt(rhsId);
			
			//put the larger id LAST, the smaller FIRST
			if (left < right) {
				return -1;
			} else if (left > right) {
				return 1;
			} else {
				return 0;
			}
		}
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Assume the alignment to be normal */
	private static class MeasureCallback implements OnMeasureCallback {
	
		final int mMargin;	
		final int mRightAlignedStart;
		
		/**
		 * @param marginLeft The left margin (or right a line is right aligned)
		 * @param rightAlignStart The start of a line that is right aligned. Set to -1 for none.
		 */
		public MeasureCallback(int marginLeft, int rightAlignStart) {
			mMargin = marginLeft;
			mRightAlignedStart = rightAlignStart;
		}


		@Override
		public int getMarginRight(int start, int end) {
			return isRightAligned(start, end) ? mMargin : 0; 
		}

		@Override
		public int getMarginLeft(int start, int end) {
			return isRightAligned(start, end) ? 0 : mMargin;
		}

		@Override
		public boolean isRightAligned(int start, int end) {
			return start <= mRightAlignedStart && mRightAlignedStart < end;
		}
		
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @version 0.3.0-20150727 */
	public static interface OnVerseInteractionListener {
		/** Provides the verse & textview clicked for given verse */
		public void onVerseClick(final View widget, final Verse verse);
		
		/** Provides the verse & textview clicked for given lettering */
		public void onLetteringClick(final View widget, final Verse verse, final MarkingItem markingItem);
	}
	
	/** @version 0.2.0-20150727 */
	public static interface OnPositionFoundListener {
		public void onPositionFound(String id, int textIndex);
	}
}
