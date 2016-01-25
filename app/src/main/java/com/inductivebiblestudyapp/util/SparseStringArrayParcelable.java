/*
 * Copyright 2014 Jason J. (iamovrhere)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.inductivebiblestudyapp.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;


/** Allows a SparseBooleanArray to be parcelable.
 * Because bundling sparse arrays is annoying.
 *  - http://stackoverflow.com/questions/11270161/best-way-to-store-sparsebooleanarray-in-bundle?answertab=active#tab-top
 */
public class SparseStringArrayParcelable extends SparseArray<String> implements Parcelable {
  public static Parcelable.Creator<SparseStringArrayParcelable> CREATOR = new Parcelable.Creator<SparseStringArrayParcelable>() {
    @Override
    public SparseStringArrayParcelable createFromParcel(Parcel source) {
    	SparseStringArrayParcelable read = new SparseStringArrayParcelable();
    	
		int size = source.readInt();
		
		int[] keys = new int[size];
		String[] values = new String[size];
		
		source.readIntArray(keys);
		source.readStringArray(values);
		
		for (int i = 0; i < size; i++) {
			read.put(keys[i], values[i]);
		}
		
		return read;
    }

    @Override
    public SparseStringArrayParcelable[] newArray(int size) {
      return new SparseStringArrayParcelable[size];
    }
  };

  public SparseStringArrayParcelable() {

  }

  public SparseStringArrayParcelable(SparseArray<String> sparseStringArray) {
    for (int i = 0; i < sparseStringArray.size(); i++) {
      this.put(sparseStringArray.keyAt(i), sparseStringArray.valueAt(i));
    }
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    int[] keys = new int[size()];
    String[] values = new String[size()];

    for (int i = 0; i < size(); i++) {
      keys[i] = keyAt(i);
      values[i] = valueAt(i);
    }

    dest.writeInt(size());
    dest.writeIntArray(keys);
    dest.writeStringArray(values);
  }
}
