package com.inductivebiblestudyapp.ui.fragments.profile.bible.wordstudy;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseDetailsResponse.StrongsNumberEntry;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.CrossReference;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.CrossReferenceResponse;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.StrongsResponse;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.StrongsResponse.CrossReferenceEntry;
import com.inductivebiblestudyapp.ui.fragments.profile.bible.wordstudy.WordStudyRecyclerAdapter.CrossReferenceParent;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the
 * {@link WordStudyFragment.OnInteractionListener} interface to
 * handle interaction events. Use the
 * {@link WordStudyFragment#newInstance} factory method to create an
 * instance of this fragment.
 * @version 0.7.5-20150827
 * 
 */
public class WordStudyFragment extends Fragment {
	final static private String CLASS_NAME = WordStudyFragment.class
			.getSimpleName();
	
	/** Parcellable. StrongsNumberEntry */
	private static final String ARG_STRONGS_NUMBER_ENTRY = CLASS_NAME + ".ARG_STRONGS_NUMBER_ENTRY";
	
	private static final String ARG_STRONGS_NUMBER = CLASS_NAME + ".ARG_STRONGS_NUMBER";
	/** Can be null or empty. */
	private static final String ARG_STRONGS_LANGUAGE = CLASS_NAME + ".ARG_STRONGS_LANGUAGE";
	/** Can be null or empty. */
	private static final String ARG_WORD_DEFINED = CLASS_NAME + ".ARG_WORD_DEFINED";
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @return A new instance of fragment StrongsDefintionFragment.
	 */
	public static WordStudyFragment newInstance(StrongsNumberEntry strongsNumber) {
		WordStudyFragment fragment = new WordStudyFragment();
		Bundle args = new Bundle();
		
		args.putParcelable(ARG_STRONGS_NUMBER_ENTRY, strongsNumber);
		
		fragment.setArguments(args);
		return fragment;
	}
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param strongsNumber
	 * @param language The language to loaded with
	 * @param word The word being defined, if blank or null ignored.
	 * @return A new instance of fragment StrongsDefintionFragment.
	 */
	@Deprecated
	public static WordStudyFragment newInstance(String strongsNumber, 
			String language,
			String word) {
		WordStudyFragment fragment = new WordStudyFragment();
		Bundle args = new Bundle();
		
		args.putString(ARG_STRONGS_NUMBER, strongsNumber);
		args.putString(ARG_WORD_DEFINED, word);
		args.putString(ARG_STRONGS_LANGUAGE, language);
		
		fragment.setArguments(args);
		return fragment;
	}

	public WordStudyFragment() {
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
	
	private WordStudyRecyclerAdapter mDisplayAdapter = null; 
	
	private OnInteractionListener mListener = null;	

	private String mStrongsNumber = null;
	private String mStrongsLanguage = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_word_study, container,
				false);
		Bundle args = getArguments();
		
		if (args != null) {
			StrongsNumberEntry entry = args.getParcelable(ARG_STRONGS_NUMBER_ENTRY);
			if (entry == null) {
				mStrongsNumber = args.getString(ARG_STRONGS_NUMBER);
				mStrongsLanguage = args.getString(ARG_STRONGS_LANGUAGE);
			} else {
				mStrongsNumber = entry.getNumber();
				mStrongsLanguage = entry.getLanguage();
			}
		}
		
		initViews(rootView, args);
		
		loadStrongsNumberDef();
		
		if (savedInstanceState == null) {
			
		} else {
			
		}
		
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnInteractionListener) getParentFragment();
			if (mDisplayAdapter != null) {
				mDisplayAdapter.setOnInteractionListener(mListener);
			}
		} catch (ClassCastException e) {
			throw new ClassCastException(getParentFragment().toString()
					+ " must implement " + OnInteractionListener.class.getSimpleName());
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Initializer helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Initializes & sets views. */
	private void initViews(View rootView, Bundle args) {
		
		Bundle adapterArgs = new Bundle();
		adapterArgs.putParcelable(WordStudyRecyclerAdapter.ARG_STRONGS_NUMBER_ENTRY, args.getParcelable(ARG_STRONGS_NUMBER_ENTRY));
		adapterArgs.putString(WordStudyRecyclerAdapter.ARG_STRONGS_NUMBER, args.getString(ARG_STRONGS_NUMBER));
		adapterArgs.putString(WordStudyRecyclerAdapter.ARG_STRONGS_LANGUAGE, args.getString(ARG_STRONGS_LANGUAGE));
		adapterArgs.putString(WordStudyRecyclerAdapter.ARG_WORD_DEFINED, args.getString(ARG_WORD_DEFINED));
		
		mDisplayAdapter = new WordStudyRecyclerAdapter(getActivity(), adapterArgs, mAdapterListener);
		RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.ibs_strongsdef_container_recyclerView);
		mRecyclerView.setAdapter(mDisplayAdapter);
		if (mListener != null) {
			mDisplayAdapter.setOnInteractionListener(mListener);
		}
		
        // use a vertical linear layout manager
        LinearLayoutManager linearLayout  = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        
        mRecyclerView.setLayoutManager(linearLayout); 
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        
		
		mProgressView = Utility.getProgressView(rootView);
		mContainerView = mRecyclerView;
		
		mProgressView.setVisibility(View.VISIBLE);
		mContainerView.setVisibility(View.GONE);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	private void loadStrongsNumberDef() {
		final String key = mStrongsLanguage + mStrongsNumber;
		StrongsResponse response = AppCache.getStrongsResponse(key);
		
		if (response == null) { //if not found in cache		
			String accessToken = new CurrentUser(getActivity()).getIBSAccessToken();
			RestClient	.getInstance()
						.getWordStudyService()
						.getStrongs(accessToken, mStrongsNumber, 
								mStrongsLanguage, null,  
								new Callback<StrongsResponse>() {
				
				@Override
				public void success(StrongsResponse response, Response arg1) {
					Log.d(CLASS_NAME, "success: " + response);
					if (response != null) {
						AppCache.addStrongsResponse(key, response);
					}
					if (self() != null) {
						self().loadDefintion(response);
					}
				}
				
				@Override
				public void failure(RetrofitError arg0) {
					Log.d(CLASS_NAME, "Failed: " + arg0);
					if (self() != null) {
						self().loadDefintion(null);
					}
				}
			});
		} else {
			//if in cache, load immediately
			loadDefintion(response);
		}
	}
	
	private WordStudyFragment self() {
		if (getFragmentManager() == null || getTag() == null) {
			return this;
		}
		return (WordStudyFragment) getFragmentManager().findFragmentByTag(getTag());
	}
	
	/** Takes the response and loads it. */
	private void loadDefintion(StrongsResponse response) {
		
		if (response == null) {
			mDisplayAdapter.setError(getString(R.string.ibs_error_cannotConnect));
			Utility.toastMessage(getActivity(), getString(R.string.ibs_error_cannotConnect));
			Utility.switchFadeProgressViews(mProgressView, mContainerView, false);
			return;
		}
		
		mDisplayAdapter.setResponse(response);
		
		Utility.switchFadeProgressViews(mProgressView, mContainerView, false);
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private WordStudyRecyclerAdapter.OnAdapterInteractionListener mAdapterListener = 
			new WordStudyRecyclerAdapter.OnAdapterInteractionListener() {
		
		@Override
		public void onStrongsNumber(final String strongsNumber, final String lang) {
			// load new strong's number			
		}
		
		@Override
		public void onCrossReferences(final CrossReferenceParent crossRefParent, CrossReferenceEntry crossRef) {
			final String key = crossRef.getKjvTranslationId();
			CrossReferenceResponse response = AppCache.getCrossReferenceResponse(key);
			
			if (response == null) { //if not found in cache		
				String accessToken = new CurrentUser(getActivity()).getIBSAccessToken();
				RestClient	.getInstance()
					.getWordStudyService()
					.getCrossReferences(accessToken, key,
						new Callback<CrossReferenceResponse>() {
		
					@Override
					public void success(CrossReferenceResponse response, Response arg1) {
						Log.d(CLASS_NAME, "success: " + response);
						if (response != null) {
							AppCache.addCrossReferenceResponse(key, response);
						}
						if (self() != null) {
							self().mDisplayAdapter.setCrossReferenceResponse(crossRefParent, response);
						}
					}
					
					@Override
					public void failure(RetrofitError arg0) {
						Log.d(CLASS_NAME, "Failed: " + arg0);
						if (self() != null) {
							self().mDisplayAdapter.setCrossReferenceResponse(crossRefParent, null);
						}
					}
				});
			} else {
				self().mDisplayAdapter.setCrossReferenceResponse(crossRefParent, response);
			}
		}
	};
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * @version 0.3.0-20150824
	 */
	public interface OnInteractionListener {
		public void onWordStudyVerseCrossReference(CrossReference crossReference);
	}



	
}
