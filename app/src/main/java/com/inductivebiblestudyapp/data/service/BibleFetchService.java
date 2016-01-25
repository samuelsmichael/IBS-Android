package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseDetailsResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse.Chapter;

/**
 * Responsible for all bible fetches.
 * 
 * @author Jason Jenkins
 * @version 0.1.0-20150818
 */
public interface BibleFetchService {
	
	/**
	 * Asynchronous. Gets the list of bible books.
	 * @param accessToken The IBS access token for the current user
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "bible")
	public void getBookList(@Query("access_token") String accessToken, Callback<BibleResponse> callback
			);
	
	/**
	 * Asynchronous. Gets the list of chapters for a given bible book.
	 * @param accessToken The IBS access token for the current user
	 * @param bookId The book id to fetch chapters for.
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "bible/{book_id}")
	public void getChapterList(@Query("access_token") String accessToken,  
			@Path("book_id") String bookId, Callback<BibleChapterResponse> callback
			);	
	
	/**
	 * Asynchronous. Fetches chapter data for a given chapter. Does NOT
	 * return the chapter content. Do not confuse with {@link #getVerseList(String, String)}.
	 * @param accessToken The IBS access token for the current user
	 * @param chapterId The id of the chapter to fetch information for
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "chapter/{chapter_id}")
	public void getChapter(@Query("access_token") String accessToken,  
			@Path("chapter_id") String chapterId, Callback<Chapter> callback);
	
	/**
	 * Asynchronous. Fetches the list of verses, notes, division themes, markings, chapter themes,
	 * and other chapter data for a given chapter. Do not
	 * confuse with {@link #getChapter(String, String)}.
	 * @param accessToken The IBS access token for the current user
	 * @param chapterId The id of the chapter to fetch verses for
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "verse")
	public void getVerseList(@Query("access_token") String accessToken,  
			@Query("parent_chapter_id") String chapterId, Callback<BibleVerseResponse> callback
			);
	
	/**
	 * Asynchronous. Fetches the verse details for a given verse. This includes the list
	 * of Strong's numbers for a given verse. 
	 * @param accessToken The IBS access token for the current user
	 * @param verseId The id of the verse to fetch details for.
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "verseDetails")
	public void getVerseDetails(@Query("access_token") String accessToken, 
			@Query("verse_id") String verseId, 
			Callback<BibleVerseDetailsResponse> callback
			);
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Synchronous. Gets the list of bible books.
	 * @param accessToken The IBS access token for the current user
	 */
	@GET(ApiConstants.PATH + "bible")
	public BibleResponse getBookList(@Query("access_token") String accessToken);
	
	/**
	 * Asynchronous. Gets the list of chapters for a given bible book.
	 * @param accessToken The IBS access token for the current user
	 * @param bookId The book id to fetch chapters for.
	 */
	@GET(ApiConstants.PATH + "bible/{book_id}")
	public BibleChapterResponse getChapterList(@Query("access_token") String accessToken,
			@Path("book_id") String bookId);
	
	/**
	 * Asynchronous. Fetches chapter data for a given chapter. Does NOT
	 * return the chapter content. Do not confuse with {@link #getVerseList(String, String)}.
	 * @param accessToken The IBS access token for the current user
	 * @param chapterId The id of the chapter to fetch information for
	 */
	@GET(ApiConstants.PATH + "chapter/{chapter_id}")
	public Chapter getChapter(@Query("access_token") String accessToken,
			@Path("chapter_id") String chapterId);

	/**
	 * Asynchronous. Fetches the list of verses, notes, division themes, markings, chapter themes,
	 * and other chapter data for a given chapter. Do not
	 * confuse with {@link #getChapter(String, String)}.
	 * @param accessToken The IBS access token for the current user
	 * @param chapterId The id of the chapter to fetch verses for
	 */
	@GET(ApiConstants.PATH + "verse")
	public BibleVerseResponse getVerseList(@Query("access_token") String accessToken,
			@Query("parent_chapter_id") String chapterId);

	/**
	 * Asynchronous. Fetches the verse details for a given verse. This includes the list
	 * of Strong's numbers for a given verse. 
	 * @param accessToken The IBS access token for the current user
	 * @param verseId The id of the verse to fetch details for.
	 */
	@GET(ApiConstants.PATH + "verseDetails")
	public BibleVerseDetailsResponse getVerseDetails(@Query("access_token") String accessToken, 
			@Query("verse_id") String verseId);
	

}
