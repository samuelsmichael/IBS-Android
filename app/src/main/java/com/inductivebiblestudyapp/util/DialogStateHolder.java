package com.inductivebiblestudyapp.util;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Simple state holder for dialogs
 * @author Jason Jenkins
 * @version 0.1.1-20150806 
 */
public class DialogStateHolder implements Parcelable {
	
	public String title = null;
	public String message = null;
	public boolean fetched = false;
	
  public static Parcelable.Creator<DialogStateHolder> CREATOR = new Parcelable.Creator<DialogStateHolder>() {
    @Override
    public DialogStateHolder createFromParcel(Parcel source) {
    	DialogStateHolder read = new DialogStateHolder();
    		
		String[] values = new String[2];
		
		source.readStringArray(values);
		
		read.fetched = source.readInt() == 1;
				
		read.title = values[0];
		read.message = values[1];
		
		return read;
    }

    @Override
    public DialogStateHolder[] newArray(int size) {
      return new DialogStateHolder[size];
    }
  };

  public DialogStateHolder() {

  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    String[] values = new String[]{ title, message };
    dest.writeStringArray(values);
    dest.writeInt(fetched ? 1 : 0);
  }
}
