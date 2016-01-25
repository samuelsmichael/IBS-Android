/*
 * Copyright 2015 Jason J.
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
package com.inductivebiblestudyapp.ui.dialogs;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.inductivebiblestudyapp.R;

/**
 * The basic structure for a dialog with a tooltip arrow pointing down/u[ at the element suggested.
 * NOTE: Centering will only work for views which are narrower than the dialog, otherwise the
 * arrow will be misaligned. 
 * <br />
 * Based heavily on: https://github.com/kvirair/Quick-Action/
 * 
 * 
 * Arrows must be set in create view.
 * 
 * <br />
 * 
 * Creates a 250dp wide dialog by default.
 * 
 * @author Jason Jenkins
 * @version 0.7.1-20150611
 */
abstract public class SimpleTooltipDialog extends DialogFragment implements OnClickListener {
	//final static private String CLASS_NAME = SimpleTooltipDialog.class.getSimpleName();

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The downward pointing arrow view. */
	protected View mDownArrow = null;
	/** The downward pointing arrow view. */
	protected View mUpArrow = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SimpleToolTipDialog);		
	}
	
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Finds the given view and sets the click listener if found. */ 
	protected void findAndSetListener(View rootView, int id) {
		View view = rootView.findViewById(id);
		if (view != null) {
			view.setOnClickListener(this);
		}
	}
	

	/**
	 * Converts dp to px. 
	 * @param dp the dp in
	 * @return The raw px out
	 */
	protected static int dpToPx(Resources res, double dp) {
		return (int) (dp * res.getDisplayMetrics().density);
	}
	
	/**
	 * Returns screen size in size 2 array.
	 * Taken directly from:
     * https://github.com/kvirair/Quick-Action/blob/master/src/garin/artemiy/quickaction/library/QuickAction.java
	 * @param dimens 0 will become X size, 1 Y size.
	 */
    @SuppressWarnings("deprecation")
    protected void getScreenDimens(int[] dimens) {
    	if (dimens.length < 2){
    		throw new IllegalArgumentException("Cannot store result in length <2");
    	}
    	final int X = 0;
    	final int Y = 1;
    	
    	WindowManager windowManager = 
    			(WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
    	
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            dimens[X] = size.x;
            dimens[Y] = size.y;
        } else {
        	dimens[X] = windowManager.getDefaultDisplay().getWidth();
        	dimens[Y] = windowManager.getDefaultDisplay().getHeight();
        }
    }
    
    /**
     * Taken from:
     * https://github.com/kvirair/Quick-Action/blob/master/src/garin/artemiy/quickaction/library/QuickAction.java
     * @return the status bar height.
     */
    protected int getStatusBarHeight() {
        int result = 0;
        final String PARAM_STATUS_BAR_HEIGHT = "status_bar_height";
        final String PARAM_DIMEN = "dimen";
        final String PARAM_ANDROID = "android";
        
        int resourceId = getActivity().getResources().getIdentifier(PARAM_STATUS_BAR_HEIGHT, PARAM_DIMEN, PARAM_ANDROID);
        if (resourceId > 0) {
        	result = getActivity().getResources().getDimensionPixelSize(resourceId);
        }

        return result;
    }
	
	/*
	 * Based on:
	 * https://github.com/kvirair/Quick-Action/blob/master/src/garin/artemiy/quickaction/library/QuickAction.java
	 * QuickAction#show(View)
	 */
	
    /** Adjusts the location of the dialog and positions the arrow.
     *  
     * @param dialog The dialog
     * @param rootView The rootView of the dialog
     * @param anchorLocation An X =0, Y=1 array of the anchor location via getLocationOnScreen.
     * @param anchorDimens  An X =0, Y=1 array of anchor size in pixels
     * @param centered Whether to center the dialog or not.
     */
    protected void adjustView(Dialog dialog, View rootView, 
			int[] anchorLocation, int[] anchorDimens, boolean centered)  {
		final int X_INDEX = 0;
        final int Y_INDEX = 1;
        
       
        try {

            int [] screenSize = new int[2];
            getScreenDimens(screenSize);
            
            rootView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            final int rootWidth = rootView.getMeasuredWidth();
            final int rootHeight = rootView.getMeasuredHeight();
            
            
            final int[] screenCenter = new int[]{
            		screenSize[X_INDEX]/2,
            		screenSize[Y_INDEX]/2
            };
            
            //we have not displayed it yet, so we cannot measure location. 
            //Instead, we will estimate based on Gravity.CENTER x and y
            final int[] estRootLocation = new int[]{
        		screenCenter[X_INDEX] - rootWidth/2,
        		screenCenter[Y_INDEX] - rootHeight/2
            };
            
            //if not left, right
            final boolean anchorToLeft = screenCenter[X_INDEX] >=  anchorLocation[X_INDEX];
            //if not top, bottom
            final boolean anchorToTop = screenCenter[Y_INDEX] >=  anchorLocation[Y_INDEX];
            
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            
            
            // lets start by calculating our midpoint
            
            //the horizontal center of the anchor relative to left side of screen 
            final int anchorMidpointX =  anchorLocation[X_INDEX] + anchorDimens[X_INDEX]/2;
            
            //were the arrow margin should go.
            int arrowMarginLeft = 0;
            if (centered) {
            	wmlp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            	
            	arrowMarginLeft = anchorMidpointX - estRootLocation[X_INDEX];
            	
            } else {
            	//LEFT required if manually positioning.  
            	wmlp.gravity = Gravity.TOP | Gravity.LEFT;
            	
            	//center our dialog under our anchor as best as possible 
            	int dialogPosFromLeft = anchorMidpointX - rootWidth/2;
            	
            	//check our values are sane.
                if (dialogPosFromLeft + rootWidth >= screenSize[X_INDEX]) {
                	dialogPosFromLeft = screenSize[X_INDEX] - rootWidth;
                } else if (dialogPosFromLeft <= 0) {
                	dialogPosFromLeft = 1;
                } 
                
            	wmlp.x = dialogPosFromLeft;
                
                arrowMarginLeft = anchorMidpointX - dialogPosFromLeft;
            }
            //horizontal positions are now calculated, now let's align our dialog vertically to be directly above our view.

            
            //the position from the top of the screen, to place the dialog.
            int desiredPosFromTop = 0;
            //the arrow params
            LinearLayout.LayoutParams arrowParams = null;
            
            if (anchorToTop) {
            	//position ourself above.
            	desiredPosFromTop = anchorLocation[Y_INDEX] + anchorDimens[Y_INDEX] - getStatusBarHeight();
            	mDownArrow.setVisibility(View.GONE);
            	arrowParams = (LinearLayout.LayoutParams) mUpArrow.getLayoutParams();
            } else {
            	desiredPosFromTop = anchorLocation[Y_INDEX] - rootHeight - getStatusBarHeight();
            	mUpArrow.setVisibility(View.GONE);
            	arrowParams = (LinearLayout.LayoutParams) mDownArrow.getLayoutParams();
            }
            
            arrowParams.leftMargin = arrowMarginLeft;
            
            
            if (desiredPosFromTop >= screenSize[Y_INDEX]) {
            	wmlp.y = screenSize[Y_INDEX] -1;
            } else if (desiredPosFromTop <= 0) {
            	wmlp.y = 1;
            } else {
            	wmlp.y = desiredPosFromTop;
            }

            dialog.getWindow().setAttributes(wmlp); //force change, even at runtime.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
