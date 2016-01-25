package com.inductivebiblestudyapp.billing;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.inductivebiblestudyapp.DebugConstants;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.billing.util.IabHelper;
import com.inductivebiblestudyapp.billing.util.IabResult;
import com.inductivebiblestudyapp.billing.util.Inventory;
import com.inductivebiblestudyapp.billing.util.Purchase;
import com.inductivebiblestudyapp.billing.util.SkuDetails;
import com.inductivebiblestudyapp.util.PreferenceUtil;

/**
 * Manages the upgrade subscription. 
 * 
 * @author Jason Jenkins
 * @version 0.4.0-20150921
 */
public class UpgradeBillingManager extends BillingManager 
	implements IabHelper.QueryInventoryFinishedListener, IabHelper.OnIabPurchaseFinishedListener {
	
	/*
	 * Helpful reading:
	 *  - http://developer.android.com/training/in-app-billing/test-iab-app.html
	 *  - http://developer.android.com/google/play/billing/billing_testing.html
	 */
	
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = UpgradeBillingManager.class
			.getSimpleName();
	private static final  String LOGTAG = CLASS_NAME;
	
	private static final String SKU_UPGRADE_ID = "com.inductivebiblestudyapp.sub.premium";
	
	/** A simple preference on whether upgrade has been done. Only used within this class.
	 * Default is "unset". */
	private static final String PREF_KEY_UPGRADE_COMPLETED = CLASS_NAME + ".PREF_KEY_UPGRADE_COMPLETED";
	
	private static final boolean DEBUG_TEST_VALUE = SKU_UPGRADE_ID.equals(SKU_ID_TEST_PURCHASED);
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End Constants 
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Set once during creation. */
	private static String sUpgradeHash = "unset"; 

	private final String mUpgradeDeveloperPayload;
	
	private OnTransactionEventListener mTransactionEventListener = null;
	private OnUpgradeInfoListener mUpgradeInfoListener = null;
	private OnBillingStateListener mBillingStateListener = null;
	
	private static final int TEST_TIME_LIMIT = 300000; //5 minutes. 60000; //1 minute.  
	private static long mPurchaseTime = 0; 
	
	public UpgradeBillingManager(FragmentActivity activity) {
		super(activity);
		
		String userId = new CurrentUser(activity).getMemberId();
		
		try {
			mUpgradeDeveloperPayload = toSHA1(userId + ";" + SKU_UPGRADE_ID);
			
			if (TextUtils.isEmpty(sUpgradeHash)) {
				sUpgradeHash = toSHA1(userId + ";" + PREF_KEY_UPGRADE_COMPLETED);
			}
			
		} catch (Exception e) {
			String msg = "Cannot generate upgrade identifier";
			Log.e(LOGTAG, msg +" : " + e);
			throw new IllegalStateException(msg, e);
		}
	}
	
	@Override
	public void onCreate() {
		//set debugging & testing mode false as otherwise it Log debugging warnings to output
		final boolean debug = DebugConstants.DEBUG_BILLING;
		enableDebugging(debug);
		super.onCreate(); //must be done after helper created
		mBillingHelper.enableTestingMode(debug);
		mBillingHelper.enableDebugLogging(debug, "bibleupgrade");
	}
	
	public void setOnTransactionEventListener(
			OnTransactionEventListener listener) {
		this.mTransactionEventListener = listener;
	}
	
	public void setOnUpgradeInfoListener(
			OnUpgradeInfoListener listener) {
		this.mUpgradeInfoListener = listener;
	}
	
	public void setOnBillingStateListener(
			OnBillingStateListener listener) {
		this.mBillingStateListener = listener;
	}
	
	/**   
	 * @return <code>true</code> if upgrade completed, <code>false</code> if not.*/
	public boolean isUpgradeComplete() {
		/* Note: This preference is set every time this class is loaded. It essentially exists
		 * to provide a minor speed up at first load. If a rooted user attempts to fake this,
		 * it will be reset once the api checks.
		 */
		return PreferenceUtil.getPreferences(mActivity).getString(PREF_KEY_UPGRADE_COMPLETED, "").equals(sUpgradeHash);
	}
	
	/**  Can only be called once the class has been initialized via {@link #UpgradeBillingManager(Activity)}.
	 * @return <code>true</code> if upgrade completed, <code>false</code> if not.*/
	public static boolean isUpgradeComplete(Context context) {
		return PreferenceUtil.getPreferences(context).getString(PREF_KEY_UPGRADE_COMPLETED, "").equals(sUpgradeHash);
	}
	
	/** Starts the process of purchasing an upgrade.
	 * @param requestCode The requestCode used by method {@link #onActivityResult(int, int, android.content.Intent)}.
	 * @see #onActivityResult(int, int, android.content.Intent)
	 */
	public void purchaseUpgrade(int requestCode) {
		if (mTransactionEventListener != null) {
			mTransactionEventListener.onTransactionStart();
		}
		if (SKU_UPGRADE_ID.equals(SKU_ID_TEST_PURCHASED)) {
			super.purchaseItem(requestCode, SKU_UPGRADE_ID);
		} else {
			super.purchaseSubscription(requestCode, SKU_UPGRADE_ID);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Override methods 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected void onIabSetupSuccess() {
		if (mBillingStateListener != null) {
			mBillingStateListener.onBillingState(0);
		}
	}
	
	@Override
	protected void onIabSetupFailed(IabResult result) {
		if (mBillingStateListener != null) {
			mBillingStateListener.onBillingState(1);
		 }
	}
	
	@Override
	protected IabHelper.QueryInventoryFinishedListener getInventoryFinishedListener() {
		return this;
	}

	@Override
	protected IabHelper.OnIabPurchaseFinishedListener getPurchaseFinishedListener() {
		return this;
	}
	
	@Override
	protected String generateDeveloperPayload(String skuProductId) {
		if (SKU_UPGRADE_ID.equals(skuProductId)) {
			return mUpgradeDeveloperPayload;
		} 
		return "";
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Used for testing primarily, consumes a purchase (such as #SKU_ID_TEST_PURCHASED )*/
	@SuppressWarnings("unused")
	private void consumeItem(Purchase purchase) {
		mBillingHelper.consumeAsync(purchase, mTestConsumeFinishedListener);
	}
	
	/** Tests if there is a change between the stored and new visibility, if so, 
	 * store the new visibility and alert listener. */
	private void checkAndUpdateUpgradeState(boolean upgradePurchased) {
		SharedPreferences pref = PreferenceUtil.getPreferences(mActivity);
		
		boolean upgradeComplete = isUpgradeComplete();

		if (upgradePurchased != upgradeComplete) {
			pref.edit().putString(PREF_KEY_UPGRADE_COMPLETED, upgradePurchased ? sUpgradeHash : "").commit();
			
			if (mUpgradeInfoListener != null) {        		
        		mUpgradeInfoListener.onUpgradeStateChanged(upgradePurchased);
			}
			
			if (DEBUG_TEST_VALUE && upgradePurchased) {
				mPurchaseTime = System.currentTimeMillis();
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected List<String> getItemSkus() {
		List<String> subSkus = new ArrayList<String>();
		subSkus.add(SKU_UPGRADE_ID);
		return subSkus;
	}
	
	@Override
	protected List<String> getSubSkus() {
		List<String> subSkus = new ArrayList<String>();
		subSkus.add(SKU_UPGRADE_ID);
		return subSkus;
	}

	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
		 if (result.isFailure()) {
			 debug("Error purchasing: " + result, LOGTAG);
			 
			 switch (result.getResponse()) {
			 
			 //if user cancelled, credit card issue or network issue.
			 case IabHelper.IABHELPER_USER_CANCELLED:
			 case IabHelper.BILLING_RESPONSE_RESULT_USER_CANCELED:
			 case IabHelper.BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE:
				 if (mTransactionEventListener != null) {
					 mTransactionEventListener.onTransactionCanceled();
				 }
				 break;

				//should never happen
			 case IabHelper.BILLING_RESPONSE_RESULT_DEVELOPER_ERROR:
				 Toast.makeText(mActivity, R.string.ibs_error_billing_developerError, Toast.LENGTH_LONG).show();
				 break;
				//should never happen
			 case IabHelper.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
			 	 Toast.makeText(mActivity, R.string.ibs_error_billing_itemUnavailable, Toast.LENGTH_LONG).show();
				 break;
			 case IabHelper.IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE:				 
				 Toast.makeText(mActivity, R.string.ibs_error_billing_doesNotSupportSubscriptions, Toast.LENGTH_LONG).show();
				 break;
				 
				 //cannot recover from these
			 case IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED:
				 if (mTransactionEventListener != null) {
					 mTransactionEventListener.onTransactionError(OnTransactionEventListener.ERROR_ALREADY_OWNED);
				 }
				 break;
			 case IabHelper.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
			 case IabHelper.BILLING_RESPONSE_RESULT_ERROR:
				 if (mTransactionEventListener != null) {
					 mTransactionEventListener.onTransactionError(OnTransactionEventListener.ERROR_UNEXPECTED);
				 }
				 break;
			 default:
				 break;
			 }
			 return;
        }
		 
        if (!verifyDeveloperPayload(purchase)) {
        	debug("Error purchasing. Authenticity verification failed.");
        	 if (mTransactionEventListener != null) {
	    			mTransactionEventListener.onTransactionError(OnTransactionEventListener.ERROR_INVALID_ATTEMPT);
	    		}
            return;
        }
        
        if (purchase != null) {
        	if (SKU_UPGRADE_ID.equals(SKU_ID_TEST_PURCHASED)) {
        		//we are in test mode
        		checkAndUpdateUpgradeState(SKU_UPGRADE_ID.contains(SKU_ID_TEST_PURCHASED));
        	} else {
        		checkAndUpdateUpgradeState(SKU_UPGRADE_ID.equals(purchase.getSku()));
        	}
        }
        
        if (mTransactionEventListener != null) {
			mTransactionEventListener.onTransactionComplete();
		}
		
	}


	@SuppressWarnings("unused")
	@Override
	public void onQueryInventoryFinished(IabResult result, Inventory inv) {
		if (result.isFailure()) {
            debug("Failed to query inventory: " + result, LOGTAG);
            return;
        }
        
        SkuDetails details = inv.getSkuDetails(SKU_UPGRADE_ID);        
        debug(inv);
        debug(details);
        if (details != null) {
        	if (mUpgradeInfoListener != null) {        		
        		mUpgradeInfoListener.onUpgradeInfo(
        				details.getTitle(), details.getDescription(), 
        				details.getPrice(), details.getCurrencyCode());
        	}
        } else if (SKU_UPGRADE_ID.contains("android.test")) {
        	//we are in testing mode:
        	if (mUpgradeInfoListener != null) {        		
        		mUpgradeInfoListener.onUpgradeInfo(
        				"", "test mode", 
        				"$0.99", "ABC");
        	}
        }
        
        Purchase purchase = inv.getPurchase(SKU_UPGRADE_ID);
        debug(purchase);
        if (purchase != null) {
        	boolean purchased = purchase.getPurchaseState() == INV_PURCHASE_PURCHASED;
        	checkAndUpdateUpgradeState(purchased);
        	
        	if (DEBUG_TEST_VALUE) {
	        	final long timeSincePurchase = System.currentTimeMillis() - mPurchaseTime;
	        	if (SKU_UPGRADE_ID.equals(SKU_ID_TEST_PURCHASED) && timeSincePurchase > TEST_TIME_LIMIT) {
	        		// if we are in test mode, consume immediately after setting update
	        		consumeItem(purchase);
	        	}
        	}
        } else {
        	checkAndUpdateUpgradeState(false);
        }
        
		
	}
	
	// Called when consumption is complete
    final private IabHelper.OnConsumeFinishedListener mTestConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            debug("Consumption finished. Purchase: " + purchase + ", result: " + result, LOGTAG);

            // if we were disposed of in the meantime, quit.
            if (mBillingHelper == null) return;
            checkAndUpdateUpgradeState(false); //we just consumed, so cancel it
            debug(result.isSuccess() ? "Consumption successful." : "Consumpetion failed.", LOGTAG);            
        }
    };
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////

    /** Provides information about the upgrade. 
     * @version 0.1.0-20150812 */
	public static interface OnUpgradeInfoListener {
		/**
		 * Provides information about the upgrade
		 * @param title The title of the upgrade item.
		 * @param description The description of the upgrade item
		 * @param price The price in the form: $0.00, where "$" is the local currency
		 * @param currencyCode The current code to display
		 */
		public void onUpgradeInfo(String title, String description, String price, 
				String currencyCode);
		/** When upgrade state changes. */
		public void onUpgradeStateChanged(boolean upgradeComplete);
	}
	
	/** A listener for determining the point within the transaction. 
	 * @version 0.2.0-20150813 */
	public static interface OnTransactionEventListener {
		/** When the transaction has experienced and error, such as fraudlent activity
		 * or a poor design decision by the developer. */
		public static final int ERROR_INVALID_ATTEMPT = 1;
		/** Attempt to re-purchase and existing item. */
		public static final int ERROR_ALREADY_OWNED = 2;
		/** An unexpected error occurred during billing. */
		public static final int ERROR_UNEXPECTED = 3;
		
		/** When the transaction has just started. */
		public void onTransactionStart();
		/** When the transaction has either been cancelled by the user, 
		 * or experienced credit card issue. */
		public void onTransactionCanceled();
		/** When the transaction has completed successfully. */
		public void onTransactionComplete();
		/** When an unknown transaction error or re-purchasing of an already owned item occured. */
		public void onTransactionError(int error);

	}
	
	/** A listener for the state of billing. 
	 * @version 0.2.0-20150813 */
	public static interface OnBillingStateListener {
		/** @state 0 for ok, anything else bad. */
		public void onBillingState(int state);
	}
}
