package com.inductivebiblestudyapp.ui.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.inductivebiblestudyapp.AppCache;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager.OnBillingStateListener;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager.OnTransactionEventListener;
import com.inductivebiblestudyapp.billing.UpgradeBillingManager.OnUpgradeInfoListener;
import com.inductivebiblestudyapp.data.loaders.SimpleContentAsyncLoader;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.ui.dialogs.SimpleConfirmDialog;
import com.inductivebiblestudyapp.util.DialogStateHolder;
import com.inductivebiblestudyapp.util.Utility;

/**
 * A placeholder fragment containing a simple view.
 * @version 0.2.2-20150921
 */
public class UpgradeFragment extends Fragment implements OnClickListener, 
	LoaderManager.LoaderCallbacks<ContentResponse>, OnUpgradeInfoListener, OnTransactionEventListener, 
	OnBillingStateListener {
	
	final static private String CLASS_NAME = UpgradeFragment.class
			.getSimpleName();
	private static String TAG_UPGRADE_CONFIRM = CLASS_NAME + ".TAG_UPGRADE_CONFIRM";	
	
	private static final String KEY_UPGRADE_CONTENT = CLASS_NAME + ".KEY_CONTACT_CONTENT";
	
	private static final int REQUEST_UPGRADE_CONTENT_LOADER = 0;
	private static final int REQUEST_DIALOG_CONTENT_LOADER = 1;
	
	private static final int REQUEST_CONFIRM_DIALOG = 2;
	private static final int REQUEST_UPGRADE_PURCHASE = 3;
	
	public UpgradeFragment() {
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private View mProgressView = null;
	private View mContainerView = null;
	
	private View mUpgradeButton = null;
	private TextView mUpgradePrice = null;
	
	private TextView mContentView = null;
	
	private UpgradeBillingManager mUpgradeBillingManager = null;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End views & context
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private String mContentMessage = null;
	
	/** The current state of billing, set by {@link #mUpgradeBillingManager}. 
	 * <code>true</code> if valid, <code>false</code> if an error. 
	 * Set in {@link #onBillingState(int)}.
	 */
	private boolean mBillingStateGood = false;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_UPGRADE_CONTENT, mContentMessage);
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mUpgradeBillingManager = new UpgradeBillingManager(getActivity());
		mUpgradeBillingManager.onCreate();
		mUpgradeBillingManager.setOnUpgradeInfoListener(this);
		mUpgradeBillingManager.setOnTransactionEventListener(this);
		mUpgradeBillingManager.setOnBillingStateListener(this);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_upgrade,
				container, false);
		
		mUpgradeButton = Utility.setOnClickAndReturnView(rootView, R.id.ibs_button_upgrade, this);
		mUpgradeButton.setEnabled(mBillingStateGood);
		mUpgradePrice = (TextView) rootView.findViewById(R.id.ibs_upgrade_text_price);
	
		
		mContentView = (TextView) rootView.findViewById(R.id.ibs_upgrade_package_info);
		mProgressView = Utility.getProgressView(rootView);
		mContainerView = rootView.findViewById(R.id.ibs_upgrade_container);
		
		if (savedInstanceState != null) {
			mContentMessage = savedInstanceState.getString(KEY_UPGRADE_CONTENT);
		} 
		
		if (mContentMessage == null) {
			getLoaderManager().initLoader(REQUEST_UPGRADE_CONTENT_LOADER, null, this);
			checkIfLoading(true);
		} else if (mContentView != null) { //content text is not always in layout
			mContentView.setText(mContentMessage);
			checkIfLoading(false);
		}
		
		if (!AppCache.getUpgradeDialogState().fetched) {
			getLoaderManager().initLoader(REQUEST_DIALOG_CONTENT_LOADER, null, this);
		}
		
		return rootView;
	}

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mUpgradeBillingManager != null) {
			mUpgradeBillingManager.onDestroy();
			mUpgradeBillingManager = null;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_CONFIRM_DIALOG:			
			getActivity().setResult(Activity.RESULT_OK);
			getActivity().finish();
			break;
			
		case REQUEST_UPGRADE_PURCHASE:
			if (mUpgradeBillingManager != null) {
				mUpgradeBillingManager.onActivityResult(requestCode, resultCode, data);
			} else {
				Log.w(CLASS_NAME, "Upgrade manager null");
			}
			break;
			
		default:
			Log.d(CLASS_NAME, "Unknown request: " + requestCode);
			break;
		}	
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
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
		
		Fragment frag = getFragmentManager().findFragmentByTag(TAG_UPGRADE_CONFIRM);
		if (frag != null && frag instanceof SimpleConfirmDialog) {
			((SimpleConfirmDialog) frag).updateContent(title, message);
		}		
	}
	
	
	/**
	 * Performs view safety checks, then animates views (if forced) or checks whether to
	 * animate views based on loader state.
	 * @param force
	 */
	private void checkIfLoading(boolean force) {
		Utility.checkIfLoading(getLoaderManager(), REQUEST_UPGRADE_CONTENT_LOADER, mProgressView, mContainerView, force);
	}
		
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public Loader<ContentResponse> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case REQUEST_UPGRADE_CONTENT_LOADER:
			checkIfLoading(true);
			return new SimpleContentAsyncLoader(getActivity(), getString(R.string.ibs_config_load_upgrade));
		case REQUEST_DIALOG_CONTENT_LOADER:
			return new SimpleContentAsyncLoader(getActivity(), getString(R.string.ibs_config_load_upgradeConfirm));
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
		 case REQUEST_UPGRADE_CONTENT_LOADER:
		 	if (data == null) {
				mContentMessage = null;
				mContentView.setText(R.string.ibs_error_cannotLoadContent);
			} else {
				mContentMessage = data.getContent();
				mContentView.setText(data.getContent());
			}
			checkIfLoading(false);
			break;
		 
		 case REQUEST_DIALOG_CONTENT_LOADER:
			 getAndSetConfirmMessage(data);
		 }
		
	}
	 
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End Loaders
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibs_button_upgrade:
			if (mUpgradeBillingManager == null) {
				return;
			}
			if (!mUpgradeBillingManager.doesDeviceSupportSubscriptions()) {
				Utility.toastMessage(getActivity(), getString(R.string.ibs_error_billing_doesNotSupportSubscriptions));
				
			} else if (mBillingStateGood) {
				try {
					mUpgradeBillingManager.purchaseUpgrade(REQUEST_UPGRADE_PURCHASE);
				} catch (IllegalStateException e) {
					Log.w(CLASS_NAME, "Upgrade purchase click cancelled: " + e);
				}
			}			
			break;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start upgrade listeners
	////////////////////////////////////////////////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see com.inductivebiblestudyapp.billing.UpgradeBillingManager.OnUpgradeInfoListener#onUpgradeInfo(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void onUpgradeInfo(String title, String description, String price,
			String currencyCode) {
		if (mUpgradePrice != null) { 
			mUpgradePrice.setText(getString(R.string.ibs_text_price, price)); 
		}
	}


	/* (non-Javadoc)
	 * @see com.inductivebiblestudyapp.billing.UpgradeBillingManager.OnUpgradeInfoListener#onUpgradeStateChanged(boolean)
	 */
	@Override
	public void onUpgradeStateChanged(boolean upgradeComplete) {}
	


	/* (non-Javadoc)
	 * @see com.inductivebiblestudyapp.billing.UpgradeBillingManager.OnBillingStateListener#onBillingState(int)
	 */
	@Override
	public void onBillingState(int state) {
		mBillingStateGood = state == 0;
		if (!mBillingStateGood) {
			Utility.toastMessage(getActivity(), getString(R.string.ibs_error_billing_cannotIntialize));
		}
		if (mUpgradeButton != null) {
			mUpgradeButton.setEnabled(mBillingStateGood);		
			mUpgradePrice.setText(R.string.ibs_error_billing_priceUnknown);
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
		Toast.makeText(getActivity(), R.string.ibs_error_billing_transactionCancelled, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onTransactionComplete() {
		DialogStateHolder upgradeState = AppCache.getUpgradeDialogState();
		
		SimpleConfirmDialog dialog = SimpleConfirmDialog.newInstance(upgradeState.title, upgradeState.message);
		dialog.show(getFragmentManager(), TAG_UPGRADE_CONFIRM);
		dialog.setTargetFragment(this, REQUEST_CONFIRM_DIALOG);
	}


	@Override
	public void onTransactionError(int error) {
		switch (error) {
		case OnTransactionEventListener.ERROR_ALREADY_OWNED:
			Utility.toastMessage(getActivity(), getString(R.string.ibs_error_billing_cannotPurchaseAlreadyOwn));
			mUpgradeButton.setEnabled(false);
			break;
			
		case OnTransactionEventListener.ERROR_INVALID_ATTEMPT:
			Utility.toastMessage(getActivity(), getString(R.string.ibs_error_billing_invalidTransaction));
			break;
			
		case OnTransactionEventListener.ERROR_UNEXPECTED:
			Utility.toastMessage(getActivity(), getString(R.string.ibs_error_billing_errorDuringBilling));
			break;
		default:
			break;
		}
		
	}

	
}
