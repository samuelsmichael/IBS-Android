package com.inductivebiblestudyapp.ui.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager.OnBillingStateListener;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager.OnTransactionEventListener;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager.OnUpgradeInfoListener;
import com.inductivebiblestudyapp.data.loaders.SimpleContentAsyncLoader;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.ui.OnBackButtonListener;
import com.inductivebiblestudyapp.ui.OnTabResetListener;
import com.inductivebiblestudyapp.ui.OnUpgradeRequestListener;
import com.inductivebiblestudyapp.ui.dialogs.SimpleConfirmDialog;
import com.inductivebiblestudyapp.ui.dialogs.SimpleYesNoDialog;
import com.inductivebiblestudyapp.ui.fragments.profile.BibleFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.CustomMarkingsFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.ProfileFragment;
import com.inductivebiblestudyapp.ui.fragments.profile.StudyNotesFragment;
import com.inductivebiblestudyapp.ui.widget.SlidingTabLayout;
import com.inductivebiblestudyapp.ui.widget.SlidingTabLayout.OnTabClickListener;
import com.inductivebiblestudyapp.util.DialogStateHolder;
import com.inductivebiblestudyapp.util.Utility;


/**
 * Creates the profile tabs and loads their fragments into a pager
 * with tabs. Also handles toolbar/actionbar and settings, there in.
 * 
 * @author Jason Jenkins
 * @version 0.18.0-20150921
 */
public class ProfileActivity extends AppCompatActivity implements OnClickListener, 
	OnItemClickListener, OnTabClickListener, OnUpgradeRequestListener, 
	OnUpgradeInfoListener, OnTransactionEventListener, 
	OnBillingStateListener {

	final static private String CLASS_NAME = ProfileActivity.class
			.getSimpleName();

	private static final String LOGTAG = CLASS_NAME;
	
	private static final String TAG_LOGOUT_DIALOG = CLASS_NAME + ".TAG_LOGOUT_DIALOG";
	
	private static String TAG_UPGRADE_CONFIRM = CLASS_NAME + ".TAG_UPGRADE_CONFIRM";	
	
	private static final String KEY_UPGRADE_RESULT_PENDING = CLASS_NAME + ".KEY_UPGRADE_RESULT_PENDING";
	
	private static final int REQUEST_UPGRADE_ACTIVITY = 0;
	
	private static final int REQUEST_UPGRADE_PROCESS = 1;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The upgrade button used to launch upgrade activity. */
	private View mUpgradeButton = null;
	
	private ListView mDrawerList = null;
	/**  The actual drawer to open and close. */
	private DrawerLayout mDrawerLayout = null;

	private ActionBarDrawerToggle mDrawerToggle =  null;
	
	/** The current pager. */
	private ProfilePagerAdapter mPagerAdapter = null;
	/** The current pager. */
	private ViewPager mPager = null;
	/** The tab picker/slider. */
	private SlidingTabLayout mTabSlider = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// end views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private UpgradeBillingManager mUpgradeBillingManager = null;
	
	private boolean mUpgradeResultPending = false;
	
	private int mDrawerGravity = -1;
	
	/** The current state of billing, set by {@link #mUpgradeBillingManager}. 
	 * <code>true</code> if valid, <code>false</code> if an error. 
	 * Set in {@link #onBillingState(int)}.
	 */
	private boolean mBillingStateGood = false;
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_UPGRADE_RESULT_PENDING, mUpgradeResultPending);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		if (savedInstanceState != null) {
			mUpgradeResultPending = savedInstanceState.getBoolean(KEY_UPGRADE_RESULT_PENDING);
		}
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.ibs_profile_toolbar);
	    setSupportActionBar(toolbar);

	    initNavDrawer(toolbar);
		initPagerAndTabs();
		reattachDialogs();
        
		mUpgradeButton = findViewById(R.id.ibs_profile_button_upgrade);
		mUpgradeButton.setOnClickListener(this);
		
		mUpgradeBillingManager = new UpgradeBillingManager(this);
		mUpgradeBillingManager.onCreate();
		mUpgradeBillingManager.setOnUpgradeInfoListener(this);
		mUpgradeBillingManager.setOnTransactionEventListener(this);
		mUpgradeBillingManager.setOnBillingStateListener(this);
		
		onUpgradeStateChanged(mUpgradeBillingManager.isUpgradeComplete());
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mUpgradeBillingManager.onDestroy();
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (mUpgradeResultPending && requestCode == REQUEST_UPGRADE_ACTIVITY) {
			//only every true if the upgrade activity is requested
			mUpgradeResultPending = false;  
			if (resultCode == Activity.RESULT_OK) {
				onUpgradeStateChanged(true);
			}
			
		} else if (mUpgradeBillingManager != null && mUpgradeBillingManager.onActivityResult(requestCode, resultCode, data)) {
			//consumed result 
		} else {
			final int index = mPager.getCurrentItem();
			Fragment frag = mPagerAdapter.getLiveFragment(index);
			if (frag != null) {
				frag.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
		    return true;
		}
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			toggleDrawerVisibility();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		if (mPager != null) {
			int index = mPager.getCurrentItem();
			//we tested the fragments earlier so everything should be good to go here.
			OnBackButtonListener frag = (OnBackButtonListener) mPagerAdapter.getLiveFragment(index);
			
			if (frag.onConsumeBackButton() == false){
				//we did not consume? up we go!
				super.onBackPressed();
			}
		} else {
			//our view isn't alive? Really? Oh dear.
			super.onBackPressed();
		}		
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Initializer methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Checks if dialog exists and reattaches listener. */
	private void reattachDialogs() {
		SimpleYesNoDialog dialog = (SimpleYesNoDialog) getSupportFragmentManager().findFragmentByTag(TAG_LOGOUT_DIALOG);
		if (dialog != null) {
			dialog.setDialogOnClickListener(mLogoutListener);
		}
	}
	
	/** Initializes the navigation drawer */
	@SuppressLint("RtlHardcoded") private void initNavDrawer(Toolbar toolbar) {
		toolbar.setLogo(R.drawable.ic_logo_stacked);
		
		
		String[] navArray = { 
				 getString(R.string.ibs_footer_home), //0
				 getString(R.string.ibs_footer_about),  //1
				 getString(R.string.ibs_footer_contact), //2
				 getString(R.string.ibs_footer_privacy), //3
				 getString(R.string.ibs_footer_terms), //4
				 getString(R.string.ibs_navbar_help), //5
				 getString(R.string.ibs_navbar_logout), //6
				 };
		
		mDrawerList = (ListView) findViewById(R.id.ibs_profile_navList);
		ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, R.layout.navigation_drawer_item, navArray);
		mDrawerList.setAdapter(mAdapter);
		mDrawerList.setOnItemClickListener(this);
		
		

		mDrawerLayout = (DrawerLayout) findViewById(R.id.ibs_profile_drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				toolbar,
		        R.string.ibs_drawer_open, R.string.ibs_drawer_close) {

		    /** Called when a drawer has settled in a completely open state. */
		    public void onDrawerOpened(View drawerView) {
		    	super.onDrawerOpened(drawerView);		    	
		    	//nothing to do
		    }

		    /** Called when a drawer has settled in a completely closed state. */
		    public void onDrawerClosed(View drawerView) {
		    	super.onDrawerClosed(drawerView);
		    	//nothing to do
		    }
		};
		mDrawerToggle.setDrawerIndicatorEnabled(false);
		
		final int DRAWER_GRAVITY = Gravity.RIGHT;
		mDrawerGravity = DRAWER_GRAVITY;
		
		mDrawerToggle.setToolbarNavigationClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				toggleDrawerVisibility();
			}
		});
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
	    mDrawerLayout.post(new Runnable() {
	        @Override
	        public void run() {
	            mDrawerToggle.syncState();
	        }
	    });
	}

	/** Toggles the drawer open & shut depending on the state. */
	private void toggleDrawerVisibility() {
		if (mDrawerLayout.isDrawerOpen(mDrawerGravity)) {
			mDrawerLayout.closeDrawer(mDrawerGravity);
			//mDrawerIcon.reverseTransition(NAV_ANIM_DURATION);
			
			mDrawerLayout.setTag(0); //zero for close
		} else {
			mDrawerLayout.openDrawer(mDrawerGravity);
			//mDrawerIcon.startTransition(NAV_ANIM_DURATION);
			
			mDrawerLayout.setTag(1); //one for open
		}
	}
	
	
	/** Initializes the paged and tabs. */
	private void initPagerAndTabs() throws NotFoundException {
		final Fragment[] frags = new Fragment[] {
			new ProfileFragment(),
			new BibleFragment(),
			new StudyNotesFragment(),
			new CustomMarkingsFragment()
		};
		fragmentTester(frags);
		
		String[] names = getResources().getStringArray(R.array.ibs_profile_tab_titles);
		
		mPagerAdapter = new ProfilePagerAdapter(getSupportFragmentManager(), frags, names);
		
		mPager = (ViewPager) findViewById(R.id.ibs_profile_pager);
		mPager.setAdapter(mPagerAdapter);
		
		//help mitigate fragment & speed loading times 
		mPager.setOffscreenPageLimit(frags.length);
		// the above works for now, but there is some state issues with fragments
		//during rotation. changing the above will result in NullPointerExceptions sometimes
		//We need to determine *why*. Otherwise, keep it at length. It likely has to do
		//with the child fragments, etc. 
		
		
		/* For usage see:
		 *  -http://www.android4devs.com/2015/01/how-to-make-material-design-sliding-tabs.html
		 *  -https://github.com/google/iosched/blob/master/android/src/main/java/com/google/samples/apps/iosched/ui/
		 */
		mTabSlider = (SlidingTabLayout) findViewById(R.id.ibs_profile_tabs);
        mTabSlider.setCustomTabView(R.layout.textview_tab_item, R.id.textview_tab_item);
        mTabSlider.setOnTabClickListener(this);
        
		// FIXED bug: under the right conditions, the evenly distribute tabs cause one or more text view to wrap
        // this in turn causes the underline to be lost and odd appearance. 
        // Fixed by setting the markings to have an uneven distribution under certain sizes
        mTabSlider.setDistributeEvenly(true);
        if (getResources().getBoolean(R.bool.ibs_markings_tab_large)) {
        	mTabSlider.setUnevenDistribution(3);
        }
 
        mTabSlider.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorAccent);
            }
        }); 
        mTabSlider.setViewPager(mPager);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper & Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	

	/**
	 * Simple fragment testing to fail immediately.
	 * @param frags
	 */
	private static void fragmentTester(Fragment[] frags) {
		for (Fragment fragment : frags) {
			try {
				@SuppressWarnings("unused")
				OnBackButtonListener l = (OnBackButtonListener) fragment;
			} catch (ClassCastException e) {
				Log.e(LOGTAG, "Fragments must implement '" + OnBackButtonListener.class.getSimpleName() + "': " + e);
				throw e;
			}
		}
	}
	

	/** Attempts to find and set the dialog message, while caching it on this fragment.
	 * @param requestId The dialog  */
	private void getAndSetConfirmMessage(ContentResponse data) {
		
		String title = getString(R.string.ibs_error_sorry);
		String message = getString(R.string.ibs_error_cannotLoadContent);
		if (data != null) {
			message = data.getContent();
			title = data.getTitle();
			AppCache.setUpgradeDialogContent(true, title, message);
		} else {
			AppCache.setUpgradeDialogContent(false, title, message);
		}
		
		Fragment frag = getSupportFragmentManager().findFragmentByTag(TAG_UPGRADE_CONFIRM);
		if (frag != null && frag instanceof SimpleConfirmDialog) {
			((SimpleConfirmDialog) frag).updateContent(title, message);
		}		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes start here
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	/**
	 * Simple fragment to implement the ViewPager in Profile.
	 * @author Jason Jenkins
	 * @version 0.2.0-20150609
	 */
	private static class ProfilePagerAdapter extends FragmentPagerAdapter {

		final private Fragment[] fragments;	
		final private String[] titles;
		
		final private SparseArray<Fragment> mLiveFragments = new SparseArray<Fragment>();
		
		public ProfilePagerAdapter(FragmentManager fm, Fragment[] fragments, String[] titles) { 
			super(fm);	
			if (fragments.length != titles.length) {
				throw new IllegalArgumentException(
						"Mismatched array lengths; must have a label for each fragment");
			}
			this.fragments = fragments;
			this.titles = titles;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}
		
		@Override
		public Fragment getItem(int position) {
			return fragments[position];
		}

		@Override
		public int getCount() {
			return fragments.length;
		}
		
		/** @return Returns the live fragment at the given index. */
		public Fragment getLiveFragment(int index) {
			return mLiveFragments.get(index);
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment frag = (Fragment) super.instantiateItem(container, position);
			mLiveFragments.put(position, frag);
			return frag;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			mLiveFragments.remove(position);
			super.destroyItem(container, position, object);
		}

	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////	

	@Override
	public boolean onTabClick(View v, int index) {
		if (index != mPager.getCurrentItem()) {
			//if the clicked tab is not already selected, do not consume
			return false;
		}
		//same tab, check if to consume.
		Fragment frag = mPagerAdapter.getLiveFragment(index);
		if (frag instanceof OnTabResetListener) {
			((OnTabResetListener) frag).onConsumeTabReset();
		}
		return true;
	}
	
	private DialogInterface.OnClickListener mLogoutListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				CurrentUser.logout(ProfileActivity.this);
				break;

			default:
				break;
			}
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibs_profile_button_upgrade:
			startActivityForResult(new Intent(this, UpgradeActivity.class), REQUEST_UPGRADE_ACTIVITY);
			mUpgradeResultPending = true;
			break;
		}
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position,
			long id) {

		mDrawerList.clearChoices();
		mDrawerList.clearFocus();
		mDrawerLayout.closeDrawers();
		
		if (position == 0) { //home button
			return;			
		}
		Intent infoIntent = new Intent(this, FooterInfoActivity.class);
		String action = "";
		
		switch (position) {
		case 0: //home
		default: 
			return;  //do not launch info activity
			
		case 1: //about
			action = FooterInfoActivity.ACTION_SHOW_ABOUT;
			break;
			
		case 2: //contact
			action = FooterInfoActivity.ACTION_SHOW_CONTACT;
			break;
			
		case 3: //privacy
			action = FooterInfoActivity.ACTION_SHOW_PRIVACY;
			break;
		
		case 4: //terms
			action = FooterInfoActivity.ACTION_SHOW_TERMS;
			break;			
			
		case 5: { //help
			final String url = getString(R.string.ibs_help_url);
			
			if (url.startsWith("http://") || url.startsWith("https://")) {
				Intent help = new Intent(Intent.ACTION_VIEW);
				help.setData(Uri.parse(url));
				startActivity(help);
			} else {
				Log.e(LOGTAG, "Cannot view link:" + url);
			}
			return; //do not launch info activity
		}
		
		case 6: //logout
			SimpleYesNoDialog dialog = SimpleYesNoDialog.newInstance(
					getString(R.string.ibs_text_logoutConfirm));
			dialog.setDialogOnClickListener(mLogoutListener);
			dialog.show(getSupportFragmentManager(), TAG_LOGOUT_DIALOG);		
			return; //do not launch info activity
		}
		
		//launching activity
		infoIntent.setAction(action);
		startActivity(infoIntent);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Upgrade listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private LoaderManager.LoaderCallbacks<ContentResponse> mUpgradeConfirmLoader = new LoaderManager.LoaderCallbacks<ContentResponse>() {
	
		@Override
	public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
		return new SimpleContentAsyncLoader(ProfileActivity.this, getString(R.string.ibs_config_load_upgradeConfirm));		
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
		
			 getAndSetConfirmMessage(data);
		 }
	};

	@Override
	public void requestUpgrade() {
		if (!AppCache.getUpgradeDialogState().fetched) {
			final int id = 0x101010;
			getSupportLoaderManager().initLoader(id, null, mUpgradeConfirmLoader);
		}
		
		if (mUpgradeBillingManager != null) {
			mUpgradeBillingManager.purchaseUpgrade(REQUEST_UPGRADE_PROCESS);
		}
	}

	@Override
	public void onUpgradeInfo(String title, String description, String price,
			String currencyCode) {}


	@Override
	public void onUpgradeStateChanged(boolean upgradeComplete) {
		mUpgradeButton.setVisibility(upgradeComplete ? View.INVISIBLE : View.VISIBLE);		
	}
	
	/* (non-Javadoc)
	 * @see com.inductivebiblestudyapp.billing.UpgradeBillingManager.OnBillingStateListener#onBillingState(int)
	 */
	@Override
	public void onBillingState(int state) {
		mBillingStateGood = state == 0;
		if (!mBillingStateGood) {
			Utility.toastMessage(this, getString(R.string.ibs_error_billing_cannotIntialize));
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start transaction callbacks
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onTransactionStart() {}

	@Override
	public void onTransactionCanceled() {
		//either for user cancellations or credit card issues
		Toast.makeText(this, R.string.ibs_error_billing_transactionCancelled, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onTransactionComplete() {
		DialogStateHolder upgradeState = AppCache.getUpgradeDialogState();
		
		SimpleConfirmDialog dialog = SimpleConfirmDialog.newInstance(upgradeState.title, upgradeState.message);
		dialog.show(getSupportFragmentManager(), TAG_UPGRADE_CONFIRM);
		//quick cheat to force the chapter to refresh
		AppCache.setImageListResponse(null);
	}


	@Override
	public void onTransactionError(int error) {
		switch (error) {
		case OnTransactionEventListener.ERROR_ALREADY_OWNED:
			Utility.toastMessage(this, getString(R.string.ibs_error_billing_cannotPurchaseAlreadyOwn));
			mUpgradeButton.setEnabled(false);
			break;
			
		case OnTransactionEventListener.ERROR_INVALID_ATTEMPT:
			Utility.toastMessage(this, getString(R.string.ibs_error_billing_invalidTransaction));
			break;
			
		case OnTransactionEventListener.ERROR_UNEXPECTED:
			Utility.toastMessage(this, getString(R.string.ibs_error_billing_errorDuringBilling));
			break;
		default:
			break;
		}
		
	}
	
}
