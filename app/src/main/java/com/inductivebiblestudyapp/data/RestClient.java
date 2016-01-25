package com.inductivebiblestudyapp.data;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inductivebiblestudyapp.DebugConstants;
import com.inductivebiblestudyapp.data.model.ContentResponse;
import com.inductivebiblestudyapp.data.model.EmailSigninResponse;
import com.inductivebiblestudyapp.data.model.ImageListResponse;
import com.inductivebiblestudyapp.data.model.LetteringListResponse;
import com.inductivebiblestudyapp.data.model.MarkingItem;
import com.inductivebiblestudyapp.data.model.ProfileResponse;
import com.inductivebiblestudyapp.data.model.StudyNotesResponse;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.data.model.bible.BibleChapterResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleSearchResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseDetailsResponse;
import com.inductivebiblestudyapp.data.model.bible.BibleVerseResponse;
import com.inductivebiblestudyapp.data.model.bible.wordstudy.CrossReferenceResponse;
import com.inductivebiblestudyapp.data.service.BibleFetchService;
import com.inductivebiblestudyapp.data.service.ChapterThemeEditService;
import com.inductivebiblestudyapp.data.service.ContactService;
import com.inductivebiblestudyapp.data.service.DivisionThemeEditService;
import com.inductivebiblestudyapp.data.service.EmailSendService;
import com.inductivebiblestudyapp.data.service.EmailSigninService;
import com.inductivebiblestudyapp.data.service.EmailSignupService;
import com.inductivebiblestudyapp.data.service.ForgotPasswordService;
import com.inductivebiblestudyapp.data.service.IBSSearchService;
import com.inductivebiblestudyapp.data.service.ImageService;
import com.inductivebiblestudyapp.data.service.LetteringService;
import com.inductivebiblestudyapp.data.service.MarkingService;
import com.inductivebiblestudyapp.data.service.NoteEditService;
import com.inductivebiblestudyapp.data.service.ProfileFetchService;
import com.inductivebiblestudyapp.data.service.ProfileUpdateService;
import com.inductivebiblestudyapp.data.service.SimpleContentService;
import com.inductivebiblestudyapp.data.service.SocialSigninService;
import com.inductivebiblestudyapp.data.service.StudyNotesService;
import com.inductivebiblestudyapp.data.service.WordStudyService;
import com.squareup.okhttp.OkHttpClient;

/**
 * The rest client that builds services to send and parse requests/responses from the server.
 * @author Jason Jenkins
 * @version 0.27.3-20150903
 * */
public class RestClient {

	private static final boolean DEBUGGING = DebugConstants.DEBUG_REQUESTS;
	private static final String BASE_URL = 	ApiConstants.BASE_URL;
	
	/*
	 * Note a singleton is used as advised by the library's creator:
	 *  http://stackoverflow.com/questions/20579185/is-there-a-way-to-reuse-builder-code-for-retrofit/20627010#20627010
	 */

	private static final RestClient sInstance = new RestClient();
	
	/** @return The sole instance of the client. */
	public static RestClient getInstance() {
		return sInstance;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End statics
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private SimpleContentService simpleContentService;
	private ContactService contactService;
	private EmailSigninService emailSigninService;
	private EmailSignupService emailSignupService;
	
	private ForgotPasswordService forgotPasswordService;
	
	private SocialSigninService socialSigninService;
	
	private ProfileUpdateService profileUpdateService;
	
	private ProfileFetchService profileFetchService;
	
	private NoteEditService noteEditService;
	private DivisionThemeEditService divisionThemeEditService;
	private ChapterThemeEditService chapterThemeEditService;
	
	private ImageService imageService;
	private LetteringService letteringService;
	
	private MarkingService markingService;	 
	
	private StudyNotesService studyNotesService;
	
	private EmailSendService emailSendService;
	
	private IBSSearchService IBSSearchService;
	
	private WordStudyService wordStudyService;
	
	private BibleFetchService bibleFetchService;
	
	protected RestClient() {
		Gson gson = new GsonBuilder()
					.setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
					.registerTypeHierarchyAdapter(SimpleContentService.class, new ContentResponse.ContentDeserializer())
					.registerTypeAdapter(EmailSigninResponse.class, new EmailSigninResponse.EmailSigninDeserializer())
					.registerTypeAdapter(ProfileResponse.class, new ProfileResponse.ProfileDeserializer())
					.registerTypeAdapter(BibleResponse.class, new BibleResponse.BibleDeserializer())
					.registerTypeAdapter(BibleChapterResponse.class, new BibleChapterResponse.BibleChapterDeserializer())
					.registerTypeAdapter(BibleVerseResponse.class, new BibleVerseResponse.BibleVerseDeserializer())
					.registerTypeAdapter(ImageListResponse.class, new ImageListResponse.ImageListDeserializer())
					.registerTypeAdapter(LetteringListResponse.class, new LetteringListResponse.LetteringListDeserializer())
					.registerTypeAdapter(UpdateResult.class, new UpdateResult.UpdateResultDeserializer())
					.registerTypeAdapter(MarkingItem.class, new MarkingItem.MarkingItemDeserializer())
					.registerTypeAdapter(StudyNotesResponse.class, new StudyNotesResponse.StudyNotesDeserializer())
					.registerTypeAdapter(BibleSearchResponse.class, new BibleSearchResponse.BibleSearchDeserializer())
					.registerTypeAdapter(BibleVerseDetailsResponse.class, new BibleVerseDetailsResponse.BibleVerseDetailsDeserializer())
					.registerTypeAdapter(CrossReferenceResponse.class, new CrossReferenceResponse.CrossReferenceDeserializer())
					.create();
					
		RestAdapter restAdapter = new RestAdapter.Builder()
			.setLogLevel(DEBUGGING ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
			.setEndpoint(BASE_URL)
			.setConverter(new DynamicJsonConverter(gson))
			.setClient(new OkClient(new OkHttpClient()))
			.build();
		
		simpleContentService = restAdapter.create(SimpleContentService.class);
		contactService = restAdapter.create(ContactService.class);
		emailSigninService = restAdapter.create(EmailSigninService.class);
		emailSignupService = restAdapter.create(EmailSignupService.class);
		
		socialSigninService = restAdapter.create(SocialSigninService.class);
		
		profileUpdateService = restAdapter.create(ProfileUpdateService.class);
		profileFetchService = restAdapter.create(ProfileFetchService.class);
		
		forgotPasswordService = restAdapter.create(ForgotPasswordService.class);
		
		noteEditService = restAdapter.create(NoteEditService.class);
		divisionThemeEditService = restAdapter.create(DivisionThemeEditService.class);
		chapterThemeEditService = restAdapter.create(ChapterThemeEditService.class);
		
		imageService = restAdapter.create(ImageService.class);
		letteringService = restAdapter.create(LetteringService.class);
		
		markingService = restAdapter.create(MarkingService.class);
		
		studyNotesService = restAdapter.create(StudyNotesService.class);
		emailSendService = restAdapter.create(EmailSendService.class);
		
		IBSSearchService = restAdapter.create(IBSSearchService.class);
		
		wordStudyService = restAdapter.create(WordStudyService.class);
		
		bibleFetchService = restAdapter.create(BibleFetchService.class);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Service getters
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public BibleFetchService getBibleFetchService() {
		return bibleFetchService;
	}
	
	public WordStudyService getWordStudyService() {
		return wordStudyService;
	}
	
	public IBSSearchService getIBSSearchService() {
		return IBSSearchService;
	}
	
	public EmailSendService getEmailSendService() {
		return emailSendService;
	}
	
	public StudyNotesService getStudyNotesService() {
		return studyNotesService;
	}
	
	public MarkingService getMarkingService() {
		return markingService;
	}
	
	public LetteringService getLetteringService() {
		return letteringService;
	}
	
	public ImageService getImageService() {
		return imageService;
	}
	
	public ChapterThemeEditService getChapterThemeEditService() {
		return chapterThemeEditService;
	}
	
	public DivisionThemeEditService getDivisionThemeEditService() {
		return divisionThemeEditService;
	}
	
	public NoteEditService getNoteEditService() {
		return noteEditService;
	}

	
	public ProfileFetchService getProfileFetchService() {
		return profileFetchService;
	}
	
	public ForgotPasswordService getForgotPasswordService() {
		return forgotPasswordService;
	}
	
	public ProfileUpdateService getProfileUpdateService() {
		return profileUpdateService;
	}
	
	public SocialSigninService getSocialSigninService() {
		return socialSigninService;
	}
	
	
	public EmailSignupService getEmailSignupService() {
		return emailSignupService;
	}
	
	public EmailSigninService getEmailSigninService() {
		return emailSigninService;
	}
	
	public SimpleContentService getSimpleContentService() {
		return simpleContentService;
	}
	
	public ContactService getContactService(){
		return contactService;
	}
	
}
