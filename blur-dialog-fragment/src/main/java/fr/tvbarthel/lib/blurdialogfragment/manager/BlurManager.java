package fr.tvbarthel.lib.blurdialogfragment.manager;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Transformation;

import fr.tvbarthel.lib.blurdialogfragment.BuildConfig;
import fr.tvbarthel.lib.blurdialogfragment.R;
import fr.tvbarthel.lib.blurdialogfragment.settings.BlurResponse;
import fr.tvbarthel.lib.blurdialogfragment.settings.BlurringSettings;
import fr.tvbarthel.lib.blurdialogfragment.utils.Utils;

/**
 * Created by Nick Unuchek on 30.11.2017.
 */

public class BlurManager {
    public static final String TAG = BlurManager.class.getSimpleName();

    Activity mHoldingActivity;
    Toolbar mToolbar;
    Bitmap mBitmap;
    View mView;
    BlurringSettings mBlurringSettings;

    public BlurManager(Activity holdingActivity, Toolbar toolbar, Bitmap bitmap, View view, BlurringSettings blurringSettings) {
        mHoldingActivity = holdingActivity;
        mToolbar = toolbar;
        mBitmap = bitmap;
        mView = view;
        mBlurringSettings = blurringSettings;
    }

    /**
     * Blur the given bitmap and add it to the activity.
     *
     *  bkg  should be a bitmap of the background.
     *  view background mView.
     */
    public BlurResponse performBlurring() {
        long startMs = System.currentTimeMillis();
        //define layout params to the previous imageView in order to match its parent
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );

        //overlay used to build scaled preview and blur background
        Bitmap overlay = null;

        //evaluate top offset due to action bar, 0 if the actionBar should be blurred.
        int actionBarHeight;
        if (mBlurringSettings.isBlurredActionBar()) {
            actionBarHeight = 0;
        } else {
            actionBarHeight = Utils.getActionBarHeight(mHoldingActivity, mToolbar);
        }

        //evaluate top offset due to status bar
        int statusBarHeight = 0;
        if ((mHoldingActivity.getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0) {
            //not in fullscreen mode
            statusBarHeight = Utils.getStatusBarHeight(mHoldingActivity);
        }

        // check if status bar is translucent to remove status bar offset in order to provide blur
        // on content bellow the status.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && Utils.isStatusBarTranslucent(mHoldingActivity)) {
            statusBarHeight = 0;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && Utils.hasStatusBar(mHoldingActivity)) {
            statusBarHeight = 0;//dont cur bg, because blur dialog bg has status bar
        }

        final int topOffset = actionBarHeight + statusBarHeight;
        // evaluate bottom or right offset due to navigation bar.
        int bottomOffset = 0;
        int rightOffset = 0;
        final int navBarSize = Utils.getNavigationBarOffset(mHoldingActivity);
        Resources resources = mHoldingActivity.getResources();
        if (resources.getBoolean(R.bool.blur_dialog_has_bottom_navigation_bar)) {
            bottomOffset = navBarSize;
        } else {
            rightOffset = navBarSize;
        }

        //add offset to the source boundaries since we don't want to blur actionBar pixels
        Rect srcRect = new Rect(
                0,
                topOffset,
                mBitmap.getWidth() - rightOffset,
                mBitmap.getHeight() - bottomOffset
        );

        //in order to keep the same ratio as the one which will be used for rendering, also
        //add the offset to the overlay.
        double height = Math.ceil((mView.getHeight() - topOffset - bottomOffset) / mBlurringSettings.getDownScaleFactor());
        double width = Math.ceil(((mView.getWidth() - rightOffset) * height
                / (mView.getHeight() - topOffset - bottomOffset)));

        // Render script doesn't work with RGB_565
        if (mBlurringSettings.isUseRenderScript()) {
            overlay = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        } else {
            overlay = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.RGB_565);
        }
        try {
            if (mHoldingActivity instanceof AppCompatActivity) {
                //add offset as top margin since actionBar height must also considered when we display
                // the blurred background. Don't want to draw on the actionBar.
                layoutParams.setMargins(0, actionBarHeight, 0, 0);
                layoutParams.gravity = Gravity.TOP;
            }
        } catch (NoClassDefFoundError e) {
            // no dependency to appcompat, that means no additional top offset due to actionBar.
            layoutParams.setMargins(0, 0, 0, 0);
        }
        //scale and draw background mView on the canvas overlay
        Canvas canvas = new Canvas(overlay);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);

        //build drawing destination boundaries
        final RectF destRect = new RectF(0, 0, overlay.getWidth(), overlay.getHeight());

        //draw background from source area in source background to the destination area on the overlay
        canvas.drawBitmap(mBitmap, srcRect, destRect, paint);

        //apply fast blur on overlay
        for (Transformation transformation: mBlurringSettings.getTransformations()){
            overlay = transformation.transform(overlay);
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Blur method : " + (mBlurringSettings.isUseRenderScript() ? "RenderScript" : "FastBlur"));
            Log.d(TAG, "Radius : " + mBlurringSettings.getBlurRadius());
            Log.d(TAG, "Down Scale Factor : " + mBlurringSettings.getDownScaleFactor());
            Log.d(TAG, "Blurred achieved in : " + (System.currentTimeMillis() - startMs) + " ms");
            Log.d(TAG, "Allocation : " + mBitmap.getRowBytes() + "ko (screen capture) + "
                    + overlay.getRowBytes() + "ko (blurred bitmap)"
                    + (!mBlurringSettings.isUseRenderScript() ? " + temp buff " + overlay.getRowBytes() + "ko." : "."));
//            Rect bounds = new Rect();
//            Canvas canvas1 = new Canvas(overlay);
//            paint.setColor(Color.BLACK);
//            paint.setAntiAlias(true);
//            paint.setTextSize(20.0f);
//            paint.getTextBounds(blurTime, 0, blurTime.length(), bounds);
//            canvas1.drawText(blurTime, 2, bounds.height(), paint);
        }
        //set bitmap in an image mView for final rendering
        ImageView imageView = new ImageView(mHoldingActivity);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageDrawable(new BitmapDrawable(resources, overlay));

        return new BlurResponse(imageView,layoutParams);
    }

}
