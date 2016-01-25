package com.inductivebiblestudyapp.util;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.LetteringItem;
import com.inductivebiblestudyapp.ui.style.TextStyle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Common static methods for reuse & debugging.
 *  
 * @author Jason Jenkins
 * @version 0.11.1-20150824
 */
public class Utility {
	
	/** Attempts to recycle a given {@link ImageView}s assigned bitmap, if any.
	 * Always try to call this before re-setting an image view with a created bitmap.
	 *  Do <b>NOT</b> call this for image views with resources set on them. */
	public static void recycleBitmap(ImageView imageview) {
		try {
			if (imageview == null) return;
			Bitmap bitmap = ((BitmapDrawable) imageview.getDrawable()).getBitmap();
			
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
			}
		} catch (Exception e) {}
	}
	
	/** Builds style from a given lettering item. Assumes that letterings will 
	 * only use 16 character hex colours. */
	public static TextStyle buildStyleFromLettering(LetteringItem lettering) {
		TextStyle.Builder builder = new TextStyle.Builder();
		
		builder.setBold(lettering.getBold())
			.setItalics(lettering.getItalics())
			.setStrikeThrough(lettering.getStrikethrough())
			
			.setUnderline(lettering.getUnderline())
			.setDoubleUnderline(lettering.getDoubleUnderline())
			.setBoxed(lettering.getBoxed())
			.setDoubleBoxed(lettering.getDoubleBoxed());
		
		if (!lettering.getFontColor().isEmpty()) {
			String number = lettering.getFontColor().replace("#", "");
			builder.setTextColor((int)Long.parseLong(number, 16));
		}
		
		if (!lettering.getBackgroundColor().isEmpty()) {
			String number = lettering.getBackgroundColor().replace("#", "");
			builder.setHighlightColor((int)Long.parseLong(number, 16));
		}
		
		
		return builder.build();
	}
	
	/** Makes simple long toast message; useful for visual debugging mostly.
	 * Centers text. */
	public static void toastMessage(Context context, int message) {
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		centerToastText(toast);
		toast.show();
	}

	
	/** Makes simple long toast message; useful for visual debugging mostly. 
	 * Centers text.*/
	public static void toastMessage(Context context, String message) {
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		centerToastText(toast);
		toast.show();
	}
	
	/** Copes text to clipboar.
	 * @param context The current context
	 * @param label The label used
	 * @param text The text to be copied.
	 * @return <code>true</code> if successful, <code>false</code> otherwise.
	 */
	public static boolean copyToClipboard(Context context, String label, String text) {
        try {            
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
	
	/** Finds the requested view, sets text and returns said view. 
	 * @throws NullPointerException If the view is not found 
	 * (and we still try to set text on it)*/
	public static TextView setTextViewAndReturn(View rootView, int id, String text) {
		TextView tv = (TextView) rootView.findViewById(id);
		tv.setText(text);
		return tv;
	}
	
	
	/** Finds the requested view, sets listener and returns said view. 
	 * @throws NullPointerException If the view is not found 
	 * (and we still try to set a listener on it)*/
	public static View setOnClickAndReturnView(View rootView, int id, OnClickListener l) {
		View v = rootView.findViewById(id);
		v.setOnClickListener(l);
		return v;
	}
	
	/**
	 * Replaces the visible fragment (with animation) & adds to backstack.
	 * @param fragment The fragment to switch in.
	 * @param tag The tag to give the fragment.
	 */
	public static void pushFragments(int id, FragmentManager fragManager, Fragment fragment, String tag){
		fragManager.beginTransaction()
				.addToBackStack(tag)
				.setCustomAnimations(
						android.R.anim.fade_in, 
						android.R.anim.fade_out,
						android.R.anim.fade_in, 
						android.R.anim.fade_out)
				.replace(id, fragment, tag)
				.commit();
	}
	
	/**
	 * Performs a simple check. Requires the loader be destroyed in onFinish
	 *  @param loaderManager
	 * @param loaderId 
	 * @return <code>true</code> if loading, <code>false</code> otherwise.
	 */ 
	public static boolean checkIfLoading(LoaderManager loaderManager, int loaderId)  {
		return loaderManager.getLoader(loaderId) != null;
	}
	
	/**
	 * Performs view safety checks, then switches views (if forced) or checks whether to
	 * animate views based on loader state.  Requires the loader be destroyed in onFinish
	 *  @param loaderManager
	 * @param loaderId 
	 * @param progressView
	 * @param containerView
	 * @param force
	 */ 
	public static void checkIfLoading(LoaderManager loaderManager, int loaderId, 
			final View progressView, final View containerView, boolean force)  {
		if (progressView == null || containerView == null) {
			//if either view is null, we cannot animate them.
			return;
		}
		if (force) { //makes the spinner spin, if necessary.
			progressView.setVisibility(View.VISIBLE);
			containerView.setVisibility(View.GONE);
		} else {
			Utility.switchIfLoading(loaderManager, loaderId, progressView, containerView);
		}
	}
	
	/**
	 * Method for easy checking if a loader is running. Note: Method requires the loader
	 * call {@link LoaderManager#destroyLoader(int)} in 
	 * {@link LoaderManager.LoaderCallbacks#onLoadFinished(android.content.Loader, Object)}.
	 * 
	 * @param loaderManager
	 * @param loaderId 
	 * @param progressView
	 * @param textView
	 * @return <code>true</code> if loading (loadering not null), <code>false</code> if not.
	 */
	public static boolean switchIfLoading(LoaderManager loaderManager, int loaderId, 
			final View progressView, final View textView) {
		boolean loading = loaderManager.getLoader(loaderId) != null;
		switchFadeProgressViews(progressView, textView, loading);
		return loading;
	}
	
	/** Easy view getter. */
	public static View getProgressView(View rootView) {
		return rootView.findViewById(R.id.ibs_progress_bar_loading);		
	}
	
	private static final int FADE_DURATION = 200; //ms
	
	/**
	 * Takes the rootview, finds the progress bar, then the textview and whether loading or not.
	 * @param progressView The progress view
	 * @param containerView The view to show/hide
	 * @param loading <code>true</code> if loading (and to show the progress bar), <code>false</code>
	 * if not.
	 */
	public static void switchFadeProgressViews(final View progressView, final View containerView, boolean loading) {
		final View hide = loading ? containerView : progressView;
		final View show = loading ? progressView : containerView;
		
		if (show.equals(hide)) {
			Log.w("Utility", "These are the same views; you can't switch visibility of the same view!");
			return;
		}
		
		show.animate()
        	.alpha(1.0f)
        	.setDuration(FADE_DURATION)
			.setListener(new AnimatorListenerAdapter() {
			    @Override
	            public void onAnimationEnd(Animator animation) {
	                super.onAnimationEnd(animation);
	                show.clearAnimation();
	        }
	    });
		show.setVisibility(View.VISIBLE); //we can't animate what we can't see
		
		hide.animate()
        	.alpha(0.0f)
        	.setDuration(FADE_DURATION)
        	.setListener(new AnimatorListenerAdapter() {
			    @Override
	            public void onAnimationEnd(Animator animation) {
	                super.onAnimationEnd(animation);
	                hide.setVisibility(View.INVISIBLE);
	                hide.clearAnimation();
            }
        });
		hide.setVisibility(View.VISIBLE); //we can't animate what we can't see
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static void centerToastText(Toast toast) {
		View v = toast.getView().findViewById(android.R.id.message);
		if( v != null && v instanceof TextView) {
			((TextView) v).setGravity(Gravity.CENTER);
		}
	}
	
}
