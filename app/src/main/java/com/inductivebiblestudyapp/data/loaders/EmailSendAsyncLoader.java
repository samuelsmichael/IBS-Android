package com.inductivebiblestudyapp.data.loaders;

import android.content.Context;
import android.os.Bundle;

import com.inductivebiblestudyapp.auth.CurrentUser;
import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.EmailConfirmResponse;
import com.inductivebiblestudyapp.data.service.EmailSendService;

/**
 * Loader to edit/create note. 
 * <code>true</code> on success, <code>false</code> or <code>null</code> on failure.
 * @author Jason Jenkins
 * @version 0.2.0-20150804
 */
public class EmailSendAsyncLoader extends AbstractFetchAsyncLoader<EmailConfirmResponse> {
	final static private String CLASS_NAME = EmailSendAsyncLoader.class
			.getSimpleName();
	
	/** String. Required. The email content. */
	public static final String KEY_MESSAGE = CLASS_NAME + ".KEY_MESSAGE";
	/** String. Required. The email contacts to send to. */
	public static final String KEY_RECIPIENTS = CLASS_NAME + ".KEY_RECIPIENTS";
	
	/** String. Optional. The optional image to attach with email. */
	public static final String KEY_IMAGE_ID = CLASS_NAME + ".KEY_IMAGE_ID";

	
	private final String mMessage;
	private final String mRecipients;
	
	private final String mImageId;
	
	private final EmailSendService mService;
	
	private final String mAccessToken;
	
	/**
	 * 
	 * @param context
	 * @param args
	 */
	public EmailSendAsyncLoader(Context context, Bundle args) {
		super(context);
		if (	!args.containsKey(KEY_MESSAGE) && 
				!(	args.containsKey(KEY_RECIPIENTS)  )
				) {
			throw new IllegalArgumentException("Bundle is missing one or more minimum requirments: " + args);
		}		
		
		mAccessToken = new CurrentUser(context).getIBSAccessToken();
		mService = RestClient.getInstance().getEmailSendService();
		mMessage = args.getString(KEY_MESSAGE, "");
		mRecipients = args.getString(KEY_RECIPIENTS);
		
		//the id or null, which means "not set" to retrofit
		mImageId = args.getString(KEY_IMAGE_ID, null); 
	}
	
	@Override
	protected EmailConfirmResponse fetchResult() {
		EmailConfirmResponse result = null;
		
		result = mService.send(mAccessToken, mRecipients, mMessage, mImageId);
		
		return result;
	}	
}
