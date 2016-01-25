package com.inductivebiblestudyapp.data.model.googleimages;

import java.lang.reflect.Type;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * The response from the basic ajax API.
 * @author Jason Jenkins
 * @version 0.2.1-20150730
 */
public class BasicGoogleImageResponse implements GoogleImageResponse {	
	
	/*
	 * Sample output:
	 * 
	 * https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8&start=0&q=test
{
  "responseData": {
    "results": [
      {
        "GsearchResultClass": "GimageSearch",
        "width": "1050",
        "height": "701",
        "imageId": "ANd9GcRO9ZkSuDqt0-CRhLrWhHAyeyt41Z5I8WhOhTkGCvjiHmRiTSvDBfHKYjx_",
        "tbWidth": "150",
        "tbHeight": "100",
        "unescapedUrl": "http:\/\/www.pbs.org\/newshour\/wp-content\/uploads\/2014\/08\/Testing.jpg",
        "url": "http:\/\/www.pbs.org\/newshour\/wp-content\/uploads\/2014\/08\/Testing.jpg",
        "visibleUrl": "www.pbs.org",
        "title": "An insider&#39;s tips for approaching employment <b>tests<\/b>",
        "titleNoFormatting": "An insider&#39;s tips for approaching employment tests",
        "originalContextUrl": "http:\/\/www.pbs.org\/newshour\/making-sense\/ask-the-headhunter-an-insiders-tips-for-approaching-employment-tests\/",
        "content": "employment <b>tests<\/b>",
        "contentNoFormatting": "employment tests",
        "tbUrl": "http:\/\/t1.gstatic.com\/images?q=tbn:ANd9GcRO9ZkSuDqt0-CRhLrWhHAyeyt41Z5I8WhOhTkGCvjiHmRiTSvDBfHKYjx_"
      },
      ...
    ],
    "cursor": {
      "resultCount": "96,800,000",
      "pages": [
        {
          "start": "0",
          "label": 1 //one-indexed page count
        },
        ...
        {
          "start": "56", //max limit 56
          "label": 8 //max limit 8 pages
        }
      ],
      "estimatedResultCount": "96800000",
      "currentPageIndex": 0, //zero-indexed page count
      "moreResultsUrl": "http:\/\/www.google.com\/images?oe=utf8&ie=utf8&source=uds&start=0&hl=en&q=test",
      "searchResultTime": "0.20"
    }
  },
  "responseDetails": null,
  "responseStatus": 200
}

	 * 
	 */
	
	/** The query result size or "rsz" */
	public static final int RESULT_SIZE = 8;

	BasicGoogleImageResult[] results = new BasicGoogleImageResult[0];
	BasicPage nextPage;
	
	int start;
	int currentPageIndex;
	String estimatedResultCount;
	
	String responseDetails;
	String responseStatus;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** 
	 * {@inheritDoc}
	 * <br />
	 * Expect a page limit of around 8.
	 */
	@Override
	public BasicPage getNextPage() {
		return nextPage;
	}
	
	
	@Override
	public int getCurrentPageIndex() {
		return currentPageIndex;
	}
	
	
	@Override
	public BasicGoogleImageResult[] getResults() {
		return results;
	}
	
	@Override
	public String getEstimatedResultCount() {
		return estimatedResultCount;
	}
	
	@Override
	public int getStart() {
		return start;
	}

	@Override
	public boolean isSuccessful() {
		if (responseStatus != null && responseStatus.contains("200")) {
			return true;
		}
		return false;
	}


	@Override
	public String toString() {
		return super.toString() + 
				"(( status: " + responseStatus + " - " + responseDetails + " ))" +
				"[currentPageIndex:" + currentPageIndex + 
				", estimatedResultCount: "+ estimatedResultCount +
				", nextPage: "+ nextPage +
				", results: "+ results +
				"]";
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	/**
	 * 
	 * @author Jason Jenkins
	 * @version 0.2.0-20150714
	 */
	public static class BasicGoogleImageResult implements GoogleImageResult {
		
		public BasicGoogleImageResult() {}
	
		@SerializedName("title")
		String title;
		@SerializedName("titleNoFormatting")
		String titleNoFormatting;
		@SerializedName("unescapedUrl")
		String unescapedUrl;
		@SerializedName("tbUrl")
		String tbUrl;
		
		@Override
		public String getTitle() {
			return title;
		}
		
		@Override
		public String getTitleNoFormatting() {
			return titleNoFormatting;
		}
		
		@Override
		public String getUnescapedUrl() {
			return Uri.decode(unescapedUrl);
		}
		
		@Override
		public String getThumbnailUrl() {
			return Uri.decode(tbUrl);
		}
		
		@Override
		public String toString() {
			return super.toString() + "[title: " + title + ", tbUrl: " + tbUrl + "]";
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Parcellable implementation
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		public static final Parcelable.Creator<BasicGoogleImageResult> CREATOR = new Parcelable.Creator<BasicGoogleImageResult>() {
		    public BasicGoogleImageResult createFromParcel(Parcel in) {
		        return new BasicGoogleImageResult(in);
		    }
		
		    public BasicGoogleImageResult[] newArray(int size) {
		        return new BasicGoogleImageResult[size];
		    }
		};
		
		public BasicGoogleImageResult(Parcel src) {
			String[] values = new String[4];
			src.readStringArray(values);
			
			title = values[0];
			titleNoFormatting = values[1];
			unescapedUrl = values[2];	
			tbUrl = values[3];
		}
		

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			String[] values = new String[]{
				title,
				titleNoFormatting, 
				unescapedUrl, 
				tbUrl
			};
			
			dest.writeStringArray(values);
		}

		
		@Override
		public int describeContents() {
			return 0;
		}
	}
	
	/** 
	 * The minimum items required for a page result.
	 * @version 0.1.0-20150714 */
	protected class BasicPage implements Page {
		@SerializedName("start")
		String start;
		@SerializedName("label")
		String label;
		
		@Override
		public String getStart() {
			return start;
		}
		
		@Override
		public String getLabel() {
			return label;
		}
		
		@Override
		public int getStartAsInt() {
			try {
				return Integer.parseInt(start);
			} catch (Exception e) {
				return -1;
			}
		}
		
		@Override
		public int getLabelAsInt() {
			try {
				return Integer.parseInt(label);
			} catch (Exception e) {
				return -1;
			}
		}
		
		@Override
		public String toString() {
			return super.toString() + "[start: " + start + ", label: " + label + "]";
		}
	}


	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal deserializers
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** @version 0.1.0-20150730 */
	public static class BasicGoogleImageSearchDeserializer implements JsonDeserializer<BasicGoogleImageResponse> {
		@Override
		public BasicGoogleImageResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
			      throws JsonParseException {
			final JsonObject data = json.getAsJsonObject().get("responseData").getAsJsonObject();
			
			BasicGoogleImageResponse response = new BasicGoogleImageResponse();
			
			try {
				response.results = 
						context.deserialize(data.get("results").getAsJsonArray(), BasicGoogleImageResult[].class);
				
				final JsonObject cursor = data.get("cursor").getAsJsonObject();
				
				response.currentPageIndex = cursor.get("currentPageIndex").getAsInt();			
				response.estimatedResultCount = cursor.get("estimatedResultCount").getAsString();
				

				BasicPage[] pages = 
						context.deserialize(cursor.get("pages").getAsJsonArray(), BasicPage[].class);
				
				final int nextPage = response.currentPageIndex + 1;
				if (nextPage < pages.length) {
					response.nextPage = pages[nextPage];
				} else {
					response.nextPage = null;
				}
				
				if (response.currentPageIndex < pages.length) {
					response.start = pages[response.currentPageIndex].getStartAsInt();
				}
				
			} catch (Exception e) {
				Log.w("BasicGoogleImageSearchDeserializer", "Parsing exception: " + e);
			}
			
			response.responseStatus = json.getAsJsonObject().get("responseStatus").getAsString();
			JsonElement details = json.getAsJsonObject().get("responseDetails");
			if (details != null && !details.isJsonNull()) {
				response.responseDetails = details.getAsString();
			}
			
			return response;
		}
	}
	
}
