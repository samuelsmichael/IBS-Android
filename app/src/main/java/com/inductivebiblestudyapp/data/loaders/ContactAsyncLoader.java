package com.inductivebiblestudyapp.data.loaders;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;

import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.ContactResponse;
import com.inductivebiblestudyapp.data.service.ContactService;

/**
 * Loader to fetch/parse contact api using {@link AsyncTaskLoader}. 
 * @author Jason Jenkins
 * @version 0.2.1-20150902
 */
public class ContactAsyncLoader extends AbstractFetchAsyncLoader<ContactResponse> {
	private final ContactService mService;
	
	private final String mName;
	private final String mEmail;
	private final String mPhone;
	private final String mMessage;
	
	public ContactAsyncLoader(Context context, String name, String email, String phone, 
			String message) {
		super(context);
		
		if (name == null || email == null || phone == null || message == null) {
			throw new NullPointerException("Cannot have null inputs: " + 
					"[name:" +  name + ", email: "+ email+ 
					", phone: "+ phone+ ", message: "+ message+"]");
		}
		
		this.mService = RestClient.getInstance().getContactService();
		
		this.mName = name.trim();
		this.mEmail = email.trim();
		this.mPhone = phone.trim();
		this.mMessage = message.trim();
	}

	@Override
	protected ContactResponse fetchResult() {
		ContactResponse result = (ContactResponse) mService.sendConactMessage(mName, mEmail, mPhone, mMessage);
		
		return result;
	}	
}
