package com.inductivebiblestudyapp.billing;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.List;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.inductivebiblestudyapp.DebugConstants;
import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.billing.util.IabHelper;
import com.inductivebiblestudyapp.billing.util.IabResult;
import com.inductivebiblestudyapp.billing.util.Inventory;
import com.inductivebiblestudyapp.billing.util.Purchase;
import com.inductivebiblestudyapp.billing.util.Security;

/**
 * Manages the interactions between the app and Google Billing services version 3.
 * Requires:
 * <pre>
 * &lt;uses-permission android:name="com.android.vending.BILLING" /&gt;
 *</pre>
 * to be present in the Manifest.
 * 
 * Remember to always call:
 * <ul><li>{@link #onCreate()}</li>
 * <li>{@link #onDestroy()}</li>
 * <li>{@link #onActivityResult(int, int, Intent)}</li>
 * </ol> 
 * 
 * @author Jason Jenkins
 * @version 0.4.0-20150904
 */
abstract public class BillingManager {
	final static private String LOGTAG = BillingManager.class
			.getSimpleName();
	/*
	 * Useful reading:
	 *  - http://developer.android.com/google/play/billing/billing_reference.html
	 *  - https://developer.android.com/google/play/billing/billing_integrate.html
	 *  - http://developer.android.com/training/in-app-billing/preparing-iab-app.html#GetSample
	 *  - http://developer.android.com/google/play/billing/billing_testing.html#billing-testing-static
	 */
	
	//ALWAYS set to false before release
	private static final boolean KEY_LOGGING = DebugConstants.DEBUG_LICENSE_KEY; 
	
	/**
	 * When you make an In-app Billing request with this product ID, Google Play responds 
	 * as though you successfully purchased an item. The response includes a JSON string, 
	 * which contains fake purchase information (for example, a fake order ID). In some cases, 
	 * the JSON string is signed and the response includes the signature so you can 
	 * test your signature verification implementation using these responses.
	 */
	protected static final String SKU_ID_TEST_PURCHASED = "android.test.purchased";
	/** When you make an In-app Billing request with this product ID Google Play responds 
	 * as though the purchase was canceled. This can occur when an error is encountered 
	 * in the order process, such as an invalid credit card, or when you cancel 
	 * a user's order before it is charged. */
	protected static final String SKU_ID_TEST_CANCELED = "android.test.canceled";
	/**
	 * When you make an In-app Billing request with this product ID, Google Play responds 
	 * as though the purchase was refunded. Refunds cannot be initiated through 
	 * Google Play's in-app billing service. Refunds must be initiated by you (the merchant). 
	 * After you process a refund request through your Google payments merchant account, 
	 * a refund message is sent to your application by Google Play. This occurs only 
	 * when Google Play gets notification from Google payments that a refund has been made.
	 */
	protected static final String SKU_ID_TEST_REFUNDED = "android.test.refunded";
	/** When you make an In-app Billing request with this product ID, Google Play responds 
	 * as though the item being purchased was not listed in your application's product list. */
	protected static final String SKU_ID_TEST_NOT_AVAILABLE = "android.test.item_unavailable";
	
	/**The purchase state of the order. Possible values are 0 (purchased), 1 (canceled), or 2 (refunded). */
	protected static final int INV_PURCHASE_PURCHASED = 0;
	/**The purchase state of the order. Possible values are 0 (purchased), 1 (canceled), or 2 (refunded). */
	protected static final int INV_PURCHASE_CANCELED = 1;
	/**The purchase state of the order. Possible values are 0 (purchased), 1 (canceled), or 2 (refunded). */
	protected static final int INV_PURCHASE_REFUNDED = 2;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected IabHelper mBillingHelper;
	
	protected final FragmentActivity mActivity;
	
	private boolean mIsDebugEnabled = false;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public BillingManager(FragmentActivity activity) {
		this.mActivity = activity;
	}
	
	/** Always call this in onCreate(Bundle) to initialize the object. 
	 * Remember to call {@link #onDestroy()} when destroying. */
	public void onCreate() {
		String base64EncodedPublicKey = 
				getKeyPart(R.string.ibs_public_key_base64_p0, 0) +
				getKeyPart(R.string.ibs_public_key_base64_p1, 1) +
				getKeyPart(R.string.ibs_public_key_base64_p2, 2);
		if (KEY_LOGGING) {
			Log.d(LOGTAG, "key: " + base64EncodedPublicKey); 
		}
		
		mBillingHelper = new IabHelper(mActivity, base64EncodedPublicKey);
		mBillingHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			  @Override
			public void onIabSetupFinished(IabResult result) {
			  if (!result.isSuccess()) {
			         // There was a problem.
			         Log.w(LOGTAG, "Problem setting up In-app Billing: " + result);
			         onIabSetupFailed(result);
			         return;
		      }
			  onIabSetupSuccess();
			  Log.d(LOGTAG, "In-app Billing is set up OK");
			  
			  // Have we been disposed of in the meantime? If so, quit.
              if (mBillingHelper == null) return;
              
              queryInventory();
			}
		});
	}
	
	
	/** Always call this in onDestroy(). Remember to call {@link #onCreate()}
	 * to restart using. */
	public void onDestroy() {
		if (mBillingHelper != null) {
			mBillingHelper.dispose();
		}
	}
	
	/** Processes the billing response, always call in onActivityResult(int, int, Intent) 
	 * @return <code>true</code> when result is consumed, <code>false</code> if not. */
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mBillingHelper == null){
			return false;
		}
        if (mBillingHelper.handleActivityResult(requestCode, resultCode, data)) {
        	debug("Result consumed by billing: " + resultCode);
        	return true;
        }

        return false;
	}

	
	 /** Returns whether subscriptions are supported. */
	public final boolean doesDeviceSupportSubscriptions() {
		return mBillingHelper.subscriptionsSupported();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Protected methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Queries inventory to get what the user owns and what is to offer. */
	protected void queryInventory() {
		// IAB is fully set up. Now, let's get an inventory of stuff we own.
        debug("Setup successful. Querying inventory.");
        mBillingHelper.queryInventoryAsync(true, getItemSkus(), getSubSkus(), mUserInventoryListener);		
	}
	
	/** 
	 * Override this if you wish to limit the item skus search for.
	 * @return the list of single purchase items to limit the inventory search to. Default is <code>null</code> */
	protected List<String> getItemSkus() {
		return null;
	}
	
	/** Override this if you wish to limit the subscription skus search for. 
	 * @return the list of subscription items to limit the inventory search to. Default is <code>null</code> */
	protected List<String> getSubSkus() {
		return null;
	}
	
	/*
	 * Security Recommendation: When you send a purchase request, create a String token 
	 * that uniquely identifies this purchase request and include this token in the 
	 * developerPayload.You can use a randomly generated string as the token. 
	 * When you receive the purchase response from Google Play, make sure to check the 
	 * returned data signature, the orderId, and the developerPayload String. 
	 */
	
	/**
	 * Performs a simple purchase of a single item.
	 * @param requestCode
	 * @param skuProductId
	 * @see #generateDeveloperPayload(String)
	 */
	protected void purchaseItem(int requestCode, String skuProductId) {
		String payload = generateDeveloperPayload(skuProductId); 
        
		mBillingHelper.launchPurchaseFlow(mActivity, skuProductId, requestCode,
                mPurchaseFinishedListener, payload);
	}
	
	/**
	 * Performs a simple purchase of continuing subscription.
	 * @param requestCode
	 * @param skuProductId
	 * @see #generateDeveloperPayload(String)
	 */
	protected void purchaseSubscription(int requestCode, String skuProductId) {
		String payload = generateDeveloperPayload(skuProductId); 
        
		mBillingHelper.launchSubscriptionPurchaseFlow(mActivity, skuProductId, requestCode,
                mPurchaseFinishedListener, payload);
	}	

	/** Verifies the developer payload of a purchase. */
    protected boolean verifyDeveloperPayload(Purchase purchase) {
        String payload = purchase.getDeveloperPayload();
        return generateDeveloperPayload(purchase.getSku()).equals(payload);
       
    }
    

	/** Generates a developer payload to uniquely identify the product. Quote from sample project:
	 * <pre>
	 * Verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         *
	 * </pre>
	 * @return The developer payload to pass onto the subscription/purchase flow. This
	 * must be the same across multiple devices */
	abstract protected String generateDeveloperPayload(String skuProductId);

	/** Used to notify the user that set up has succeeded. */
	abstract protected void onIabSetupSuccess();
	
	/** Used to notify the user that set up failed for whatever reason is given in the result. */
	abstract protected void onIabSetupFailed(IabResult result);
	
	/** @return The purchase listener safely called, must not return <code>null</code>. */
	abstract protected IabHelper.OnIabPurchaseFinishedListener getPurchaseFinishedListener();
	
	/** This inventory gives not only user owned items, but available items.  
	 * @return The inventory finished listener, safely called. Must not return <code>null</code>. */
	abstract protected IabHelper.QueryInventoryFinishedListener getInventoryFinishedListener();
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility & helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
    
	/** This will turn on debugging at level Log.w(). This is so it can be seen during release mode. */
    protected void enableDebugging(boolean enable) {
		this.mIsDebugEnabled = enable;
	}
    
    /** Debugging at level Log.w(). This is so it can be seen during release mode. */
    protected final void debug(Object message, String logtag) {
    	if (mIsDebugEnabled) {
    		Log.w(logtag, ""+message);
    	}
    }
    protected final void debug(Object message) {
    	debug(message, LOGTAG);
    }
    
    
    /** Obfuscates/Deobfuscates the string. This should make an attacker's job difficult.
     * Use:
     * 1. Take original public key and encode it with this method, logging enabled.
     * 2. Take log and save the output key part
     * 3. Place the output (obfuscated key) in resources
     * 4. Decode with this method.
     * 
     * @param stringId
     * @param mode The method to obfuscate with. 
     * Keep this constant between obfuscating/deobfuscating.
     * @return
     */
    //http://stackoverflow.com/questions/14352758/android-in-app-billing-securing-application-public-key
    private String getKeyPart(int stringId, int mode) {
    	// Replace this with your encoded key.
    	String base64EncodedPublicKey = mActivity.getString(stringId);
    	
    	if (KEY_LOGGING) {
	    	Log.d(LOGTAG, "original: " + base64EncodedPublicKey);
    	}

    	if (mode == 1) {
    		final int length = base64EncodedPublicKey.length();
    		String last = "";
    		if (length % 2 != 0) { 
    			//accounting for odd number lengths by removing one letter & readding it
    			last = base64EncodedPublicKey.substring(length - 1);
    			base64EncodedPublicKey = base64EncodedPublicKey.substring(0, length - 1);
    		}
    		base64EncodedPublicKey = //move middle to front and vice versa
    				base64EncodedPublicKey.substring(length/2) +
    				base64EncodedPublicKey.substring(0, length/2) +
    				last;
    	} else if (mode == 2) { //reverse string order
    		base64EncodedPublicKey = new StringBuilder(base64EncodedPublicKey).reverse().toString();
    	} else {
    		//this is intentionally misleading, it is meant to do nothing
    		base64EncodedPublicKey.substring(0, base64EncodedPublicKey.length()/3);
    		base64EncodedPublicKey.substring(base64EncodedPublicKey.length()/3);
    	}
    	
    	// Get byte sequence to play with.
    	byte[] bytes = base64EncodedPublicKey.getBytes();
    	

    	// Swap upper and lower case letters.
    	for (int i = 0; i < bytes.length; i++) {
    	    if(bytes[i] >= 'A' && bytes[i] <= 'Z')
    	        bytes[i] = (byte)( 'a' + (bytes[i] - 'A'));
    	    else if(bytes[i] >= 'a' && bytes[i] <= 'z')
    	        bytes[i] = (byte)( 'A' + (bytes[i] - 'a'));
    	}   	

    	// Assign back to string.
    	String result = new String( bytes );
    	if (KEY_LOGGING) {
	    	Log.d(LOGTAG, "result: " + result); 
    	}

    	return result;
    }
  
    //http://stackoverflow.com/questions/5757024/make-sha1-encryption-on-android
    private static String convertToHex(byte[] data) { 
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) { 
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do { 
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                }
                else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        } 
        return buf.toString();
    } 


    protected static String toSHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException  { 
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash = new byte[40];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    } 
    
	/** Verifies the signature of the purchase. Note this will note work for 
	 * <b><code>android.test.purchased</code></b>.
	 * <br /> 
	 * See:
	 * - http://stackoverflow.com/questions/14600664/android-in-app-purchase-signature-verification-failed
	 * - http://stackoverflow.com/questions/19534210/in-app-billing-not-working-after-update-google-store/19539213#19539213
	 * @param publicKey
	 * @param signedData
	 * @param signature
	 * @return
	 */
	protected static final boolean verifyPurchase(String publicKey, String signedData, String signature) {
		if (TextUtils.isEmpty(signedData) || TextUtils.isEmpty(publicKey)
                || TextUtils.isEmpty(signature)) {
			Log.e(LOGTAG, "Purchase verification failed: missing data.");
            return false;
		}

	    PublicKey key = Security.generatePublicKey(publicKey);
	    return Security.verify(key, signedData, signature);

	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Listener notifies that the user has completed purchase. */
    private final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            debug("Purchase finished: " + result + ", purchase: " + purchase);

            if (mBillingHelper != null) {
                getPurchaseFinishedListener().onIabPurchaseFinished(result, purchase);
            } else {
            	debug("Helper not available");
            }
        }
    };
    
   
	
	/** Listener notifies that the user has these items. */
    private final IabHelper.QueryInventoryFinishedListener mUserInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            debug("Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mBillingHelper != null){
            	getInventoryFinishedListener().onQueryInventoryFinished(result, inventory);
            }  else {
            	debug("Helper not available");
            }            
        }
    };
}
