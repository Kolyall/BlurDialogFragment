package fr.tvbarthel.lib.blurdialogfragment.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;

import fr.tvbarthel.lib.blurdialogfragment.R;

/**
 * Created by Nick Unuchek on 30.11.2017.
 */

public class Utils {
    /**
     * Retrieve offset introduce by the navigation bar.
     *
     * @return bottom offset due to navigation bar.
     * @param holdingActivity
     */
    public static int getNavigationBarOffset(Activity holdingActivity) {
        int result = 0;
        if(hasNavigationBar(holdingActivity)) {
            //The device has a navigation bar
            Resources resources = holdingActivity.getResources();

            int orientation = resources.getConfiguration().orientation;
            int resourceId;
            if (isTablet(holdingActivity)){
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
            }  else {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
            }

            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    public static boolean hasNavigationBar(Activity holdingActivity) {
        boolean hasMenuKey = ViewConfiguration.get(holdingActivity).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        return !hasMenuKey && !hasBackKey;
    }

    public static boolean isTablet(Context c) {
        return (c.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * Used to check if the status bar is translucent.
     *
     * @return true if the status bar is translucent.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isStatusBarTranslucent(Activity holdingActivity) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{android.R.attr.windowTranslucentStatus};
        TypedArray array = holdingActivity.obtainStyledAttributes(typedValue.resourceId, attribute);
        boolean isStatusBarTranslucent = array.getBoolean(0, false);
        array.recycle();
        return isStatusBarTranslucent;
    }
    /**
     * Used to check if the status bar is translucent.
     *
     * @return true if the status bar is translucent.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isWindowDrawsSystemBarBackgrounds(Activity holdingActivity) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{android.R.attr.windowDrawsSystemBarBackgrounds};
        TypedArray array = holdingActivity.obtainStyledAttributes(typedValue.resourceId, attribute);
        boolean isStatusBarTranslucent = array.getBoolean(0, false);
        array.recycle();
        return isStatusBarTranslucent;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean hasStatusBar(Activity holdingActivity) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{R.attr.blurDialogHasStatusBar};
        TypedArray array = holdingActivity.obtainStyledAttributes(typedValue.resourceId, attribute);
        boolean isStatusBarTranslucent = array.getBoolean(0, false);
        array.recycle();
        return isStatusBarTranslucent;
    }

    /**
     * Retrieve action bar height.
     *
     * @return action bar height in px.
     */
    public static int getActionBarHeight(Activity holdingActivity, Toolbar toolbar) {
        int actionBarHeight = 0;

        try {
            if (toolbar != null) {
                actionBarHeight = toolbar.getHeight();
            } else if (holdingActivity instanceof AppCompatActivity) {
                ActionBar supportActionBar
                    = ((AppCompatActivity) holdingActivity).getSupportActionBar();
                if (supportActionBar != null) {
                    actionBarHeight = supportActionBar.getHeight();
                }
            } else  {
                android.app.ActionBar actionBar = holdingActivity.getActionBar();
                if (actionBar != null) {
                    actionBarHeight = actionBar.getHeight();
                }
            }
        } catch (NoClassDefFoundError e) {
                android.app.ActionBar actionBar = holdingActivity.getActionBar();
                if (actionBar != null) {
                    actionBarHeight = actionBar.getHeight();
                }
        }
        return actionBarHeight;
    }

    /**
     * retrieve status bar height in px
     *
     * @return status bar height in px
     */
    public static int getStatusBarHeight(Activity holdingActivity) {
        int result = 0;
        int resourceId = holdingActivity.getResources()
            .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = holdingActivity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static Bitmap takeScreenShot(Activity activity) {
        View backgroundView = activity.getWindow().getDecorView();
        backgroundView.setDrawingCacheEnabled(true);
        backgroundView.buildDrawingCache();
        Bitmap bitmap = backgroundView.getDrawingCache();

        int statusBarHeight = 0;
//            int statusBarHeight = getStatusBarHeight();
        int width = backgroundView.getMeasuredWidth();
        int height = backgroundView.getMeasuredHeight();
        Bitmap out = Bitmap.createBitmap(bitmap, 0, statusBarHeight, width, height-statusBarHeight);
        backgroundView.destroyDrawingCache();
        return out;
    }
}
