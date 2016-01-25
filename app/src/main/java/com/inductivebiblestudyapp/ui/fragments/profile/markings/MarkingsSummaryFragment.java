package com.inductivebiblestudyapp.ui.fragments.profile.markings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.ImageItem;
import com.inductivebiblestudyapp.data.model.LetteringItem;
import com.inductivebiblestudyapp.ui.adapters.ImageFetchGridAdapter;
import com.inductivebiblestudyapp.ui.adapters.ImageFetchRecyclerAdapter;
import com.inductivebiblestudyapp.ui.adapters.LetteringFetchGridAdapter;
import com.inductivebiblestudyapp.ui.adapters.LetteringFetchRecyclerAdapter;
import com.inductivebiblestudyapp.ui.adapters.ImageFetchRecyclerAdapter.OnImageItemClickListener;
import com.inductivebiblestudyapp.ui.adapters.LetteringFetchRecyclerAdapter.OnAdapterStateListener;
import com.inductivebiblestudyapp.ui.widget.ExpandableHeightGridView;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A simple {@link Fragment} subclass. Use the {@link MarkingsSummaryFragment#newInstance}
 * factory method to create an instance of this fragment.
 * @version 0.4.0-20150811
 */
public class MarkingsSummaryFragment extends Fragment implements OnClickListener, 
	TextWatcher, OnEditorActionListener, OnCheckedChangeListener {
	
	final static private String CLASS_NAME = MarkingsSummaryFragment.class
			.getSimpleName();
	private static final String LOGTAG = CLASS_NAME;
	
	/** The delay before performing filtering */
	private static final int DELAY_USER_TYPING = 200; //ms
	/** The what to post to query. */
	private static final int HANDLER_WHAT_QUERY = 0;
	
	/** The id of the default sort to reset to. */
	private static final int DEFAULT_SORT_ID = R.id.ibs_sortBy_radioOption_byName;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 */
	public static MarkingsSummaryFragment newInstance() {
		MarkingsSummaryFragment fragment = new MarkingsSummaryFragment();
		return fragment;
	}

	public MarkingsSummaryFragment() {
		// Required empty public constructor
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End factory methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private EditText mSearchInput = null;
	private RadioGroup mSortByRadioGroup = null;
	private View mSortContainer = null;	

	private ExpandableHeightGridView mLetteringGridView = null;
	private ExpandableHeightGridView mImageGridView = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private LetteringFetchRecyclerAdapter mLetteringAdapter = null; 
	private ImageFetchRecyclerAdapter mImageAdapter = null;
	
	private OnInteractionListener mListener =  null;
	
	private int mLetteringColumnCount = 0;
	private int mLetteringStretchMode = 0;
	
	private int mImageColumnCount = 0;
	private int mImageStretchMode = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View  rootView = inflater.inflate(R.layout.fragment_markings_summary, container, false);
		
		initLetteringViews(rootView);		
		initImageViews(rootView);
		
		initSearchAndSortViews(rootView);
		
		if (savedInstanceState == null) { //first run
			mSortByRadioGroup.check(DEFAULT_SORT_ID);
		} else {
			//initialize the sort.
			onCheckedChanged(mSortByRadioGroup, mSortByRadioGroup.getCheckedRadioButtonId());
		}
		
		// Inflate the layout for this fragment
		return rootView;
	}	

	
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mImageAdapter.clear();
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
	/** Initializes the search and sort views. */
	private void initSearchAndSortViews(View rootView) {
		mSearchInput = (EditText) rootView.findViewById(R.id.ibs_custom_markings_summary_search_input);
		mSearchInput.setOnEditorActionListener(this);
		mSearchInput.addTextChangedListener(this);
		mSortContainer = rootView.findViewById(R.id.ibs_custom_markings_summary_sortby_container);
		
		mSortByRadioGroup = (RadioGroup) rootView.findViewById(R.id.ibs_sortBy_radiogroup);
		mSortByRadioGroup.setOnCheckedChangeListener(this);
	}
	
	/** Initializes the image views. */
	private void initImageViews(View rootView) {
		mImageAdapter = new ImageFetchRecyclerAdapter(getActivity(), 6); //TODO refactor the 6
		
		
		mImageGridView = (ExpandableHeightGridView) rootView.findViewById(R.id.ibs_custom_markings_summary_gridview_images);
		mImageGridView.setAdapter(new ImageFetchGridAdapter(getActivity(), mImageAdapter)); 
		mImageGridView.setExpanded(true);
		mImageColumnCount = mImageGridView.getNumColumns();	
		mImageStretchMode = mImageGridView.getStretchMode();
		
		mImageAdapter.setOnImageItemClickListener(mImageGridClick);
		mImageAdapter.setOnAdapterStateListener(mImageGridState);
		
		Utility.setOnClickAndReturnView(rootView, R.id.ibs_button_customMarking_viewAll_images, this);
		Utility.setOnClickAndReturnView(rootView, R.id.ibs_button_customMarking_create_images, this);
	}

	/** Initializes the letterings views. */
	private void initLetteringViews(View rootView) {
		mLetteringAdapter = new LetteringFetchRecyclerAdapter(getActivity(), 9); //TODO refactor the 9
		
		mLetteringGridView = (ExpandableHeightGridView) rootView.findViewById(R.id.ibs_custom_markings_summary_gridview_letterformat);
		mLetteringGridView.setAdapter(new LetteringFetchGridAdapter(getActivity(), mLetteringAdapter)); 
		mLetteringGridView.setExpanded(true);
		mLetteringColumnCount = mLetteringGridView.getNumColumns();	
		mLetteringStretchMode = mLetteringGridView.getStretchMode();

		mLetteringAdapter.setOnLetteringItemClickListener(mLetteringGridClick);
		mLetteringAdapter.setOnAdapterStateListener(mLetteringGridState);		

		Utility.setOnClickAndReturnView(rootView, R.id.ibs_button_customMarking_viewAll_letterformat, this);
		Utility.setOnClickAndReturnView(rootView, R.id.ibs_button_customMarking_create_letterformat, this);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibs_button_customMarking_viewAll_images:
			mListener.onViewAllImages();
			break;
		case R.id.ibs_button_customMarking_viewAll_letterformat:
			mListener.onViewAllLetterings();
			break;
		case R.id.ibs_button_customMarking_create_images:
			mListener.onSummaryCreateNewImage();
			break;
		case R.id.ibs_button_customMarking_create_letterformat:
			mListener.onSummaryCreateNewLettering();
			break;
		default:
			break;
		}
		
	}
	
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.ibs_sortBy_radioOption_byName:
			mImageAdapter.setSortMode(ImageFetchRecyclerAdapter.SORT_NAME_ASC);
			mLetteringAdapter.setSortMode(ImageFetchRecyclerAdapter.SORT_NAME_ASC);
			break;
			
		case R.id.ibs_sortBy_radioOption_byRecent:
			mImageAdapter.setSortMode(ImageFetchRecyclerAdapter.SORT_RECENT_ASC);
			mLetteringAdapter.setSortMode(ImageFetchRecyclerAdapter.SORT_RECENT_ASC);
			break;

		default:
			Log.w(CLASS_NAME, "Unspecified action: " + checkedId);
			break;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see android.widget.TextView.OnEditorActionListener#onEditorAction(android.widget.TextView, int, android.view.KeyEvent)
	 */
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			final String searchTerm =   mSearchInput.getText().toString().trim();
			Log.d(LOGTAG, "searching: " + searchTerm);			
			mSearchHandler.sendEmptyMessage(HANDLER_WHAT_QUERY); //perform search
			return true;
		}
		return false;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start text watcher
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The search handler to perform filtering in. */
	private final Handler mSearchHandler = new Handler(new Handler.Callback() {		
		@Override
		public boolean handleMessage(Message msg) {
			if (mSearchInput != null) {
				String filterString = mSearchInput.getText().toString().trim();
				mImageAdapter.filter(filterString);
				mLetteringAdapter.filter(filterString);
				return true;
			}
			return false;
		}
	});
	


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.length() > 0) {
			mSortContainer.setVisibility(View.VISIBLE);
		} else {
			mSortContainer.setVisibility(View.GONE);
			mSortByRadioGroup.check(DEFAULT_SORT_ID);
		}
		mSearchHandler.removeMessages(HANDLER_WHAT_QUERY);
		mSearchHandler.sendEmptyMessageDelayed(HANDLER_WHAT_QUERY, DELAY_USER_TYPING);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start item listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private LetteringFetchRecyclerAdapter.OnLetteringItemClickListener mLetteringGridClick = 
			new LetteringFetchRecyclerAdapter.OnLetteringItemClickListener() {
		@Override
		public void onLetteringItemClick(LetteringItem item) {
			mListener.onSummaryLetteringClick(item);			
		}
	};
	
	private LetteringFetchRecyclerAdapter.OnAdapterStateListener mLetteringGridState =
			new LetteringFetchRecyclerAdapter.OnAdapterStateListener() {
				
				@Override
				public void onStateUpdate(int state) {
					switch (state) {
					case OnAdapterStateListener.STATE_OK:
						mLetteringGridView.setNumColumns(mLetteringColumnCount);
						mLetteringGridView.setStretchMode(mLetteringStretchMode);
						break;
					case OnAdapterStateListener.STATE_EMPTY:			
					case OnAdapterStateListener.STATE_CANNOT_CONNECT:
						mLetteringGridView.setNumColumns(1);
						mLetteringGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
						break;

					}
				}
			};
	
	private OnImageItemClickListener mImageGridClick = new OnImageItemClickListener() {		
		@Override
		public void onImageItemClick(ImageItem item) {
			mListener.onSummaryImageClick(item);
		}
	};
	
	private ImageFetchRecyclerAdapter.OnAdapterStateListener mImageGridState =
			new ImageFetchRecyclerAdapter.OnAdapterStateListener() {
				
				@Override
				public void onStateUpdate(int state) {
					switch (state) {
					case OnAdapterStateListener.STATE_OK:
						mImageGridView.setNumColumns(mImageColumnCount);
						mImageGridView.setStretchMode(mImageStretchMode);
						break;
					case OnAdapterStateListener.STATE_EMPTY:			
					case OnAdapterStateListener.STATE_CANNOT_CONNECT:
						mImageGridView.setNumColumns(1);
						mImageGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
						break;

					}
				}
			};
	

	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal interface
	////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @version 0.3.0-20150715
	 */
	public static interface OnInteractionListener {
		public void onViewAllImages();
		
		public void onViewAllLetterings();
		
		public void onSummaryImageClick(ImageItem imageItem);
		
		public void onSummaryCreateNewImage();
		
		public void onSummaryLetteringClick(LetteringItem letteringItem);
		
		public void onSummaryCreateNewLettering();
	}

}
