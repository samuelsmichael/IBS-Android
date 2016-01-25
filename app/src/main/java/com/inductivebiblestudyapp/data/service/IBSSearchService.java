package com.inductivebiblestudyapp.data.service;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

import com.inductivebiblestudyapp.data.ApiConstants;
import com.inductivebiblestudyapp.data.model.StudyNotesResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleSearchResponse;

/**
 * 
 * @author Jason Jenkins
 * @version 0.2.0-20150815
 */
public interface IBSSearchService {
	
	/**
	 * Asynchronous. Searches the bible for the given text, limited by certain field if necessary.
	 * @param accessToken  The IBS access token for the current user
	 * @param query The text to search the bible content
	 * @param bookId Optional (<code>null</code> if not used). The book to limit the search to.
	 * Note: Should be null if searching by chapter or verse range.
	 * @param chapterId Optional (<code>null</code> if not used). The chapter to limit the search to.
	 * Note: Should be null if searching by book or verse range.
	 * @param verseStartId Optional (<code>null</code> if not used). The verse range start 
	 * to limit the search to. Note: Should be null if searching by book or chapter.
	 * @param verseEndId Optional (<code>null</code> if not used). The verse range end 
	 * to limit the search to.
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "search")
	public void searchBible(@Query("access_token") String accessToken,
			@Query("q") String query,
			@Query("book_id") String bookId,
			@Query("chapter_id") String chapterId,
			@Query("verse_id_start") String verseStartId,
			@Query("verse_id_end") String verseEndId,
			Callback<BibleSearchResponse> callback);
	
	/**
	 * Asynchronous. Searches the study notes created by users, limited by the locations
	 * they were created. 
	 * @param accessToken  The IBS access token for the current user
	 * @param query The user created text to search for. Note this will always be either
	 * the name of the content, or the user input text of some kind.
	 * @param bookId Optional (<code>null</code> if not used). The book to limit the search to.
	 * Note: Should be null if searching by chapter or verse range.
	 * @param chapterId Optional (<code>null</code> if not used). The chapter to limit the search to.
	 * Note: Should be null if searching by book or verse range.
	 * @param verseStartId Optional (<code>null</code> if not used). The verse range start 
	 * to limit the search to. Note: Should be null if searching by book or chapter.
	 * @param verseEndId Optional (<code>null</code> if not used). The verse range end 
	 * to limit the search to.
	 * @param callback
	 */
	@GET(ApiConstants.PATH + "search?type=note")
	public void searchStudyNotes(@Query("access_token") String accessToken,
			@Query("q") String query,
			@Query("book_id") String bookId,
			@Query("chapter_id") String chapterId,
			@Query("verse_id_start") String verseStartId,
			@Query("verse_id_end") String verseEndId,
			Callback<StudyNotesResponse> callback);
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Synchronous. Searches the bible for the given text, limited by certain field if necessary.
	 * @param accessToken  The IBS access token for the current user
	 * @param query The text to search the bible content
	 * @param bookId Optional (<code>null</code> if not used). The book to limit the search to.
	 * Note: Should be null if searching by chapter or verse range.
	 * @param chapterId Optional (<code>null</code> if not used). The chapter to limit the search to.
	 * Note: Should be null if searching by book or verse range.
	 * @param verseStartId Optional (<code>null</code> if not used). The verse range start 
	 * to limit the search to. Note: Should be null if searching by book or chapter.
	 * @param verseEndId Optional (<code>null</code> if not used). The verse range end 
	 * to limit the search to.
	 */
	@GET(ApiConstants.PATH + "search")
	public BibleSearchResponse searchBible(@Query("access_token") String accessToken,
			@Query("q") String query,
			@Query("book_id") String bookId,
			@Query("chapter_id") String chapterId,
			@Query("verse_id_start") String verseStartId,
			@Query("verse_id_end") String verseEndId);
	
	/**
	 * Synchronous. Searches the study notes created by users, limited by the locations
	 * they were created. 
	 * @param accessToken  The IBS access token for the current user
	 * @param query The user created text to search for. Note this will always be either
	 * the name of the content, or the user input text of some kind.
	 * @param bookId Optional (<code>null</code> if not used). The book to limit the search to.
	 * Note: Should be null if searching by chapter or verse range.
	 * @param chapterId Optional (<code>null</code> if not used). The chapter to limit the search to.
	 * Note: Should be null if searching by book or verse range.
	 * @param verseStartId Optional (<code>null</code> if not used). The verse range start 
	 * to limit the search to. Note: Should be null if searching by book or chapter.
	 * @param verseEndId Optional (<code>null</code> if not used). The verse range end 
	 * to limit the search to.
	 */
	@GET(ApiConstants.PATH + "search?type=note")
	public StudyNotesResponse searchStudyNotes(@Query("access_token") String accessToken,
			@Query("q") String query,
			@Query("book_id") String bookId,
			@Query("chapter_id") String chapterId,
			@Query("verse_id_start") String verseStartId,
			@Query("verse_id_end") String verseEndId);
	

}
