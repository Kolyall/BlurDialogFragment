package fr.tvbarthel.lib.blurdialogfragment.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.v7.widget.Toolbar;
import android.view.View;

import fr.tvbarthel.lib.blurdialogfragment.manager.BlurManager;
import fr.tvbarthel.lib.blurdialogfragment.settings.BlurResponse;
import fr.tvbarthel.lib.blurdialogfragment.settings.BlurringSettings;
import fr.tvbarthel.lib.blurdialogfragment.utils.Utils;

/**
 * Async task used to process blur out of ui thread
 */
public class BlurAsyncTask extends AsyncTask<Void, Void, BlurResponse> {

    private Bitmap mBackground;
    private View mBackgroundView;
    private Activity mHoldingActivity;
    private Toolbar mToolbar;
    private BlurringSettings mBlurringSettings;

    BlurAsyncTaskListener mBlurAsyncTaskListener;

    public BlurAsyncTask(Activity activity, Toolbar toolbar, BlurringSettings blurringSettings,BlurAsyncTaskListener blurAsyncTaskListener){
        mHoldingActivity = activity;
        mToolbar = toolbar;
        mBlurringSettings = blurringSettings;
        mBlurAsyncTaskListener = blurAsyncTaskListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mBackgroundView = mHoldingActivity.getWindow().getDecorView();

        //retrieve background view, must be achieved on ui thread since
        //only the original thread that created a view hierarchy can touch its views.

        Rect rect = new Rect();
        mBackgroundView.getWindowVisibleDisplayFrame(rect);
        mBackground = Utils.takeScreenShot(mHoldingActivity);
        /**
         * After rotation, the DecorView has no height and no width. Therefore
         * .getDrawingCache() return null. That's why we  have to force measure and layout.
         */
        if (mBackground == null) {
            mBackgroundView.measure(
                View.MeasureSpec.makeMeasureSpec(rect.width(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(rect.height(), View.MeasureSpec.EXACTLY)
            );
            mBackgroundView.layout(0, 0, mBackgroundView.getMeasuredWidth(),
                mBackgroundView.getMeasuredHeight());
            mBackground = Utils.takeScreenShot(mHoldingActivity);
        }
    }

    @Override
    protected BlurResponse doInBackground(Void... params) {
        //process to the blue
        if (!isCancelled()) {
            BlurManager blurManager = new BlurManager(mHoldingActivity, mToolbar, mBackground, mBackgroundView,mBlurringSettings);
            BlurResponse blurResponse = blurManager.performBlurring();
            //clear memory
            mBackground.recycle();
            return blurResponse;
        } else {
            return null;
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void onPostExecute(BlurResponse blurResponse) {
        super.onPostExecute(blurResponse);
        mBackgroundView.destroyDrawingCache();
        mBackgroundView.setDrawingCacheEnabled(false);

        mHoldingActivity.getWindow().addContentView(
                blurResponse.getImageView(),
                blurResponse.getLayoutParams()
        );

        mBlurAsyncTaskListener.onDoneExecute(blurResponse);

        mBackgroundView = null;
        mBackground = null;
    }

    public interface BlurAsyncTaskListener{

        void onDoneExecute(BlurResponse blurResponse);
    }
}
