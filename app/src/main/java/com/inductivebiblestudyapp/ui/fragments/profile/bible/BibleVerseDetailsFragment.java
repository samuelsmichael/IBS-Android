package com.inductivebiblestudyapp.ui.fragments.profile.bible;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseDetailsResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseDetailsResponse.StrongsNumberEntry;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseDetailsResponse.StrongsVerseData;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse.Verse;
import com.inductivebiblestudyapp.ui.style.StyleUtil;
import com.inductivebiblestudyapp.ui.style.span.MyStyledClickableSpan;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A simple {@link Fragment} subclass. Use the {@link BibleVerseDetailsFragment#newInstance}
 * factory method to create an instance of this fragment.
 * @author Jason Jenkins
 * @version 0.5.3-20150827
 */
public class BibleVerseDetailsFragment extends Fragment  {
	final static private String CLASS_NAME = BibleVerseDetailsFragment.class
			.getSimpleName();
	private static final String LOGTAG = CLASS_NAME;
	
	private static final int SPAN_FLAG = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
			
	private static final String ARG_VERSE = CLASS_NAME + ".ARG_VERSE";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 */
	public static BibleVerseDetailsFragment newInstance(Verse verse) {
		BibleVerseDetailsFragment fragment = new BibleVerseDetailsFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_VERSE, verse);
		fragment.setArguments(args);
		return fragment;
	}
	
	public BibleVerseDetailsFragment() {
		// Required empty public constructor
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factory methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	private View mContainerView = null;
	private View mProgressView = null;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End views
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	private OnInteractionListener mListener = null;	
	
	private Verse mVerse = null;
	
	private VerseDetailsAdapter mAdapter = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View  rootView = inflater.inflate(R.layout.fragment_bible_verse_details, container, false);
		
		Bundle args = getArguments();
		if (args != null) {
			mVerse = args.getParcelable(ARG_VERSE);
		}
		
		initViews(rootView);
		
		fetchVerseDetails();
		
		return rootView;
	}

	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		try {
			mListener = (OnInteractionListener) getParentFragment();
			if (mAdapter != null) {
				mAdapter.setOnInteractionListener(mListener); 
			}			
		} catch (ClassCastException e) {
			Log.e(getTag(), getParentFragment().toString() + " must implement " + OnInteractionListener.class.getName());
			throw e;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Initializer methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes the views. */
	private void initViews(View rootView) {

		ListView listView = (ListView) rootView.findViewById(R.id.ibs_bible_verseDetails_container);
		
		mAdapter = new VerseDetailsAdapter(mVerse);
		listView.setAdapter(mAdapter);
		
		mProgressView = Utility.getProgressView(rootView);
		mContainerView = listView;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private BibleVerseDetailsFragment self() {
		if (getFragmentManager() == null || getTag() == null) {
			return this;
		}
		return (BibleVerseDetailsFragment) getFragmentManager().findFragmentByTag(getTag());
	}
	
	/** Begins fetch for verse details. */
	private void fetchVerseDetails() {
		final String verseId = mVerse.getVerseId();
		BibleVerseDetailsResponse response = AppCache.getBibleVerseDetailsResponse(verseId);
		
		if (response == null) { //if not in cache		
			mContainerView.setVisibility(View.GONE);
			mProgressView.setVisibility(View.VISIBLE);
			
			String accessToken = new CurrentUser(getActivity()).getIBSAccessToken();
			RestClient	.getInstance()
						.getBibleFetchService()
						.getVerseDetails(accessToken, verseId,
				new Callback<BibleVerseDetailsResponse>() {
					
					@Override
					public void success(BibleVerseDetailsResponse response, Response arg1) {
						Log.d(LOGTAG, "success? details: " + response);
						if (response != null) {
							AppCache.addBibleVerseDetailsResponse(verseId, response);
						}
						if (self() != null) {
							self().loadResponse(response);
						}
					}
					
					@Override
					public void failure(RetrofitError arg0) {
						Log.d(LOGTAG, "failed details: " + arg0);
						if (self() != null) {
							self().loadResponse(null);
						}
					}
				});
		} else { //load cached response immediately
			loadResponse(response);
			mContainerView.setVisibility(View.VISIBLE);
			mProgressView.setVisibility(View.GONE);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Populating helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/** Takes response and loads it into the view. If the response supplied is <code>null</code>
	 * an error is shown.
	 * @param response
	 */
	private void loadResponse(BibleVerseDetailsResponse response) {
		if (response == null) {
			mAdapter.setError(getString(R.string.ibs_error_cannotConnect));
		} else {
			mAdapter.setResponse(response);
		}
		
		Utility.switchFadeProgressViews(mProgressView, mContainerView, false);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** @version 0.1.2-20150827 */
	private static class VerseDetailsAdapter extends BaseAdapter {
		
		private static final int VIEW_TYPE_VERSE = 0;
		private static final int VIEW_TYPE_STRONGS = 1;
		
		final private Verse mVerse;
		
		private List<StrongsNumberPair> mStrongPairs = new ArrayList<StrongsNumberPair>();
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// End final members
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		private Spanned mVerseSpannedText = null;
		private OnInteractionListener mListener = null;
		
		
		public VerseDetailsAdapter(Verse verse){
			mVerse = verse;			
			mVerseSpannedText = Html.fromHtml(
					mVerse.getVerseOnlyText()
					);
			
		}
		
		public void setOnInteractionListener(OnInteractionListener listener) {
			this.mListener = listener;
		}
		
		/** Sets the output error. */
		public void setError(String error) {
			mStrongPairs.clear();
			mStrongPairs.add(new StrongsNumberPair(error, ""));
			notifyDataSetChanged();
		}
		
		/** 
		 * @param response Cannot be null.
		 */
		public void setResponse(BibleVerseDetailsResponse response) {
			mStrongPairs.clear();
			
			populateStrongsNumbers(response);
			notifyDataSetChanged();
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Override methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		@Override
		public int getItemViewType(int position) {
			if (position == 0) {
				return VIEW_TYPE_VERSE;
			}
			return VIEW_TYPE_STRONGS;
		}
		
		@Override
		public int getCount() {
			return 1 + mStrongPairs.size();
		}

		/** Always returns null. */
		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return 	VIEW_TYPE_VERSE == getItemViewType(position) ? 
					getVerseView(convertView, parent) :
					getStrongsNumberView(position - 1, convertView, parent);
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// View methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		protected View getVerseView(View convertView, ViewGroup parent) {
			VerseViewHolder holder = null;
			
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.list_item_verse_details_verse, parent, false); 
				holder = new VerseViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (VerseViewHolder) convertView.getTag();
			}
			
			holder.title.setText(mVerse.getParentChapter());
			holder.verseText.setText(mVerseSpannedText);
			holder.verseNumber.setText(mVerse.getNumber());
			
			return convertView;
		}
		
		protected View getStrongsNumberView(int position, View convertView, ViewGroup parent) {
			StrongsViewHolder holder = null;
			
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.list_item_verse_details_strongs, parent, false); 
				holder = new StrongsViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (StrongsViewHolder) convertView.getTag();
			}			
			StrongsNumberPair content = mStrongPairs.get(position);
			
			if (content.numbers.length() > 0) { //if there are numbers, set visible
				holder.numbers.setVisibility(View.VISIBLE);
			} else { //otherwise, hide it
				holder.numbers.setVisibility(View.GONE);
			}
			holder.text.setText(content.text);
			holder.numbers.setText(content.numbers);
			
			return convertView;
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Helper methods
		////////////////////////////////////////////////////////////////////////////////////////////////

		
		/** Assumes the view is available & response non-null */
		private void populateStrongsNumbers(BibleVerseDetailsResponse response) {
			StrongsVerseData[] verseData = response.getStrongsVerseData();
			
			for (StrongsVerseData strongsVerseData : verseData) {
				populateWordStudy(strongsVerseData);
			}
		}
		
		/** Assumes the view is available & response non-null */
		private void populateWordStudy(StrongsVerseData verseData) {
			StrongsNumberEntry[] strongs = verseData.getStrongs();			
			
			SpannableString verseString = new SpannableString(mVerseSpannedText);
			
			List<StrongsNumberEntry> entries = new ArrayList<BibleVerseDetailsResponse.StrongsNumberEntry>();
			
			for (StrongsNumberEntry strongsNumberEntry : strongs) {
				if (entries.contains(strongsNumberEntry)) {
					//avoid duplicates
					continue;
				}

				final String word = strongsNumberEntry.getText();		
				
				findAndHighlightWord(word, verseString);
				
				
				SpannableStringBuilder strNumberBuilder = new SpannableStringBuilder();
				
				StrongsNumberEntry current = strongsNumberEntry;
				while (current != null) {
					applyStrongsNumbers(strNumberBuilder, current);
					
					current = current.getNextEntry();
				}
				
				mStrongPairs.add(new StrongsNumberPair(word, strNumberBuilder));
				
				entries.add(strongsNumberEntry);
			}			
			mVerseSpannedText = (Spanned) verseString.subSequence(0, verseString.length());
		}
		
		/** Applies the word related to the strongs number. */
		private CharSequence applyStrongWord(final String word) {
			if (word.isEmpty()) {
				return "";
			}
			MyStyledClickableSpan clickspan = 
					new MyStyledClickableSpan(new MyStyledClickableSpan.OnClickListener() {				
				@Override
				public void onClick(View widget) {
					if (mListener != null) {
						mListener.onVerseDefinition(word);
					}
				}
			});
			
			SpannableString spanString = new SpannableString(word);
			spanString.setSpan(clickspan, 0, word.length(), SPAN_FLAG);
			return spanString;
		}
		/** Applies the strongs number all related to the word. */
		private void applyStrongsNumbers(SpannableStringBuilder strBuilder,
				final StrongsNumberEntry entry) {
			MyStyledClickableSpan clickspan2 = new MyStyledClickableSpan(new MyStyledClickableSpan.OnClickListener() {
				
				@Override
				public void onClick(View widget) {
					if (mListener != null) {
						mListener.onVerseStrongsNumber(entry);
					}
				}
			});	
			
			if (strBuilder.length() > 0) {
				strBuilder.append(", "); //separate by commas
			}
			
			SpannableString spanString = SpannableString.valueOf(entry.getLanguage() +entry.getNumber());
			spanString.setSpan(clickspan2, 0, spanString.length(), SPAN_FLAG);
			
			strBuilder.append(spanString);
		}

		/**
		 * See {@link StyleUtil#findWordAndBold(String, SpannableString, int)} */
		private boolean findAndHighlightWord(final String word, SpannableString spanString) {
			return StyleUtil.findWordAndBold(word, spanString, SPAN_FLAG);
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Internal classes
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		/** @version 0.1.0-20150819 */
		private static class StrongsNumberPair {
			final CharSequence text;
			final CharSequence numbers;
			public StrongsNumberPair(CharSequence text, CharSequence numbers) {
				this.text = text;
				this.numbers = numbers;
			}
		}
		
		/** @version 0.1.0-20150819 */
		public static class VerseViewHolder {
			final TextView title;
			final TextView verseNumber;
			final TextView verseText;
			
			public VerseViewHolder(View rootView) {
				title = (TextView) rootView.findViewById(R.id.ibs_bible_verseDetails_verse_title);
				verseNumber = (TextView)  rootView.findViewById(R.id.ibs_bible_verseDetails_verse_number);
				verseText = (TextView)  rootView.findViewById(R.id.ibs_bible_verseDetails_verse_text);
			}
		}
		
		/** @version 0.1.0-20150819 */
		public static class StrongsViewHolder {
			final TextView numbers;
			final TextView text;
			public StrongsViewHolder(View rootView) {
				text = (TextView)  rootView.findViewById(R.id.list_item_verseDetails_strongs_word);
				numbers = (TextView) rootView.findViewById(R.id.list_item_verseDetails_strongs_numbers);
				numbers.setMovementMethod(LinkMovementMethod.getInstance()); //make numbers clickable
			}
		}
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// OnClick listeners
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	/**
	 * 
	 * @version 0.4.0-20150821
	 *
	 */
	public static interface OnInteractionListener {
		public void onVerseStrongsNumber(StrongsNumberEntry strongsNumber);
		public void onVerseDefinition(String word);
		public void onVerseCrossReference(String word);
	}

}
