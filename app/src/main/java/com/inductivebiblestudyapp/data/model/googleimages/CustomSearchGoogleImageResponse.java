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
 * The response from the custom search api.
 * @author Jason Jenkins
 * @version 0.2.0-20150730
 */
public class CustomSearchGoogleImageResponse implements GoogleImageResponse {	

	
	/*
	 * Sample output:
	 * 
	 * server key: AIzaSyCrxvXgyoIjGhF34W2walWG5kgx0ljC-yM
	 * https://www.googleapis.com/customsearch/v1?key=API_KEY&cx=SEARCH_ENGINE_ID&safe=high&imgType=clipart&searchType=image&q=test&start=11

{
 "kind": "customsearch#search",
 "url": {
  "type": "application/json",
  "template": "https://www.googleapis.com/customsearch/v1?q={searchTerms}&num={count?}&start={startIndex?}&lr={language?}&safe={safe?}&cx={cx?}&cref={cref?}&sort={sort?}&filter={filter?}&gl={gl?}&cr={cr?}&googlehost={googleHost?}&c2coff={disableCnTwTranslation?}&hq={hq?}&hl={hl?}&siteSearch={siteSearch?}&siteSearchFilter={siteSearchFilter?}&exactTerms={exactTerms?}&excludeTerms={excludeTerms?}&linkSite={linkSite?}&orTerms={orTerms?}&relatedSite={relatedSite?}&dateRestrict={dateRestrict?}&lowRange={lowRange?}&highRange={highRange?}&searchType={searchType}&fileType={fileType?}&rights={rights?}&imgSize={imgSize?}&imgType={imgType?}&imgColorType={imgColorType?}&imgDominantColor={imgDominantColor?}&alt=json"
 },
 "queries": {
  "nextPage": [
   {
    "title": "Google Custom Search - test",
    "totalResults": "2570000",
    "searchTerms": "test",
    "count": 10,
    "startIndex": 21,
    "inputEncoding": "utf8",
    "outputEncoding": "utf8",
    "safe": "high",
    "cx": "018371220663064130258:gh5j8fb-6ju",
    "searchType": "image"
   }
  ],
  "request": [ //that is the current page
   {
    "title": "Google Custom Search - test",
    "totalResults": "2570000",
    "searchTerms": "test",
    "count": 10,
    "startIndex": 11,
    "inputEncoding": "utf8",
    "outputEncoding": "utf8",
    "safe": "high",
    "cx": "018371220663064130258:gh5j8fb-6ju",
    "searchType": "image",
    "imgType": "clipart"
   }
  ],
  "previousPage": [
   ....
  ]
 },
 "context": {
  "title": "ImageSearch"
 },
 "searchInformation": {
  "searchTime": 0.318978,
  "formattedSearchTime": "0.32",
  "totalResults": "2570000",
  "formattedTotalResults": "2,570,000"
 },
 "items": [
  {
   "kind": "customsearch#result",
   "title": "Tom Jenkins at Headingley for the Ashes | Sport | The Guardian",
   "htmlTitle": "Tom Jenkins at Headingley for the Ashes | Sport | The Guardian",
   "link": "http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2009/8/9/1249829346916/The-4th-Ashes-Test-Match--007.jpg",
   "displayLink": "www.theguardian.com",
   "snippet": "Tom Jenkins at Headingley for",
   "htmlSnippet": "Tom Jenkins at Headingley for",
   "mime": "image/jpeg",
   "image": {
    "contextLink": "http://www.theguardian.com/sport/gallery/2009/aug/09/ashes-cricket",
    "height": 390,
    "width": 590,
    "byteSize": 53492,
    "thumbnailLink": "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQrIsuMmBBsmtl-mIKCs7jZyqNBc6Ect8bttQZwObBRjBIf-7Y6mdSNm-Y",
    "thumbnailHeight": 89,
    "thumbnailWidth": 135
   }
  },
  ...
  ]
 }
 
 Sample error: 
 
 {
 "error": {
  "errors": [
   {
    "domain": "usageLimits",
    "reason": "keyInvalid",
    "message": "Bad Request"
   }
  ],
  "code": 400,
  "message": "Bad Request"
 }
}

	 * 
	 */
	
	public static final String API_HEADER = "X-Android-Package-Name: com.inductivebiblestudyapp";
	/** The Android API key. Note: Only works on Android. */
	public static final String API_KEY = "AIzaSyCvy4lIeFYVblvt9oX3wkYKUiabQBSSJjA";
	/** The unique search engine id; cx */
	public static final String SEARCH_ENGINE_ID = "018371220663064130258:gh5j8fb-6ju";	
	/** The query result size or "num" Max is 10. */
	public static final int RESULT_SIZE = 10;
	

	CustomSearchGoogleImageResult[] results = new CustomSearchGoogleImageResult[0];
	Page nextPage;
	
	int start;
	int currentPageIndex;
	String estimatedResultCount;
	
	String responseDetails;
	String responseStatus;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
	

	@Override
	public Page getNextPage() {
		return nextPage;
	}
	
	
	@Override
	public int getCurrentPageIndex() {
		return currentPageIndex;
	}
	
	
	@Override
	public CustomSearchGoogleImageResult[] getResults() {
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
		if (responseStatus != null && responseStatus.startsWith("200")) {
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
	 * @version 0.1.0-20150730
	 */
	public static class CustomSearchGoogleImageResult implements GoogleImageResult {
		
		public CustomSearchGoogleImageResult() {}
		String htmlSnippet;
		String snippet;
		String link;		
		Image image;
			
		@Override
		public String getTitle() {
			return htmlSnippet;
		}
		
		@Override
		public String getTitleNoFormatting() {
			return snippet;
		}
		
		@Override
		public String getUnescapedUrl() {
			return Uri.decode(link);
		}
		
		@Override
		public String getThumbnailUrl() {
			if (image != null) {
				return image.thumbnailLink;
			}
			return "";
		}
		
		@Override
		public String toString() {
			return super.toString() + "[title: " + htmlSnippet + ", tbUrl: " + image + "]";
		}
		
		private static class Image {
			String thumbnailLink;
			@Override
			public String toString() {
				return super.toString() + "["+thumbnailLink+"]";
			}
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//// Parcellable implementation
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		public static final Parcelable.Creator<CustomSearchGoogleImageResult> CREATOR = new Parcelable.Creator<CustomSearchGoogleImageResult>() {
		    public CustomSearchGoogleImageResult createFromParcel(Parcel in) {
		        return new CustomSearchGoogleImageResult(in);
		    }
		
		    public CustomSearchGoogleImageResult[] newArray(int size) {
		        return new CustomSearchGoogleImageResult[size];
		    }
		};
		
		public CustomSearchGoogleImageResult(Parcel src) {
			String[] values = new String[4];
			src.readStringArray(values);
			
			htmlSnippet = values[0];
			snippet = values[1];
			link = values[2];	
			image = new Image();
			image.thumbnailLink = values[3];
		}
		

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			String[] values = new String[]{
				htmlSnippet,
				snippet, 
				link, 
				"" //thumbnail
			};
			if (image != null) {
				values[3] = image.thumbnailLink;
			}
			
			dest.writeStringArray(values);
		}

		
		@Override
		public int describeContents() {
			return 0;
		}
	}
	
	/** 
	 * The minimum items required for a page result.
	 * @version 0.1.0-20150730 */
	protected class CSPage implements Page {
		@SerializedName("startIndex")
		int startIndex;
		int count;
		String totalResults;
		
		@Override
		public String getStart() {
			return String.valueOf(startIndex);
		}
		
		@Override
		public String getLabel() {
			return String.valueOf(getLabelAsInt());
		}
		
		@Override
		public int getStartAsInt() {
			return startIndex - 1;
		}
		
		@Override
		public int getLabelAsInt() {
			return getPageIndex() + 1;  //e.g. 11/10 + 1 is page 2
		}
		
		protected int getPageIndex() {
			return startIndex/count; //e.g. 1/10 == 0, 21/10 == 2, etc.
		}
		
		@Override
		public String toString() {
			return super.toString() + "[start: " + startIndex + ", label: " + getLabelAsInt() + "]";
		}
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal deserializers
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** @version 0.1.0-20150730 */
	public static class CustomSearchGoogleImageSearchDeserializer implements JsonDeserializer<CustomSearchGoogleImageResponse> {
		@Override
		public CustomSearchGoogleImageResponse deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
			      throws JsonParseException {
			final JsonElement queryData = json.getAsJsonObject().get("queries");
			final JsonElement items = json.getAsJsonObject().get("items");
			
			CustomSearchGoogleImageResponse response = new CustomSearchGoogleImageResponse();
			
			try {
				if (queryData.isJsonNull() || items.isJsonNull()) {
					final JsonObject error = json.getAsJsonObject().get("error").getAsJsonObject();
					response.responseStatus = error.get("code").getAsString();
					response.responseDetails = error.get("message").getAsString();
				} else {
					response.responseStatus = "200";
					response.responseDetails = "";
				}
				
				final JsonObject queries = queryData.getAsJsonObject();
				
				response.results = 
						context.deserialize(items.getAsJsonArray(), CustomSearchGoogleImageResult[].class);
	
				CSPage[] nextPage = 
						context.deserialize(queries.get("nextPage").getAsJsonArray(), CSPage[].class);
				
				if (nextPage.length > 0) {
					response.nextPage = nextPage[0];
				} else {
					response.nextPage = null;
				}
				
				
				CSPage[] currentPage = 
						context.deserialize(queries.get("request").getAsJsonArray(), CSPage[].class);
				
				if (currentPage.length > 0) {
					response.estimatedResultCount = currentPage[0].totalResults;
					response.currentPageIndex = currentPage[0].getPageIndex();
					response.start = currentPage[0].getStartAsInt();
				} else {
					response.estimatedResultCount = "-1";
					response.currentPageIndex = -1;
					response.start = -1;
				}
				
			} catch (Exception e) {
				Log.w("CustomSearchGoogleImageSearchDeserializer", "Parsing exception: " + e);
			}
			
			return response;
		}
	}
	
}
