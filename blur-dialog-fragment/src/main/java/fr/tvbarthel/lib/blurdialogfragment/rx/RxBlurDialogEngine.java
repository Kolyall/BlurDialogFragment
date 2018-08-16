package fr.tvbarthel.lib.blurdialogfragment.rx;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

import fr.tvbarthel.lib.blurdialogfragment.R;
import fr.tvbarthel.lib.blurdialogfragment.settings.BlurResponse;
import fr.tvbarthel.lib.blurdialogfragment.settings.BlurringSettings;
import fr.tvbarthel.lib.blurdialogfragment.settings.DefaultSettings;
import jp.wasabeef.picasso.transformations.BlurTransformation;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Encapsulate the whole behaviour to provide a blur effect on a DialogFragment.
 * <p/>
 * All the screen behind the dialog will be blurred except the action bar.
 * <p/>
 * Simply linked all methods to the matching lifecycle ones.
 */
public class RxBlurDialogEngine {
    private static final String TAG = RxBlurDialogEngine.class.getSimpleName();

    /**
     * Used to enable or disable debug mod.
     */
    private boolean mDebugEnable = false;

    /**
     * Factor used to down scale background. High quality isn't necessary
     * since the background will be blurred.
     */
    private float mDownScaleFactor = DefaultSettings.DEFAULT_BLUR_DOWN_SCALE_FACTOR;

    /**
     * Radius used for fast blur algorithm.
     */
    private int mBlurRadius = DefaultSettings.DEFAULT_BLUR_RADIUS;

    /**
     * Holding activity.
     */
    private Activity mHoldingActivity;

    /**
     * Transformation for background.
     */
    private List<Transformation> mTransformations = new ArrayList<>();

    /**
     * Allow to use a toolbar without set it as action bar.
     */
    private Toolbar mToolbar;

    /**
     * Duration used to animate in and out the blurred image.
     * <p/>
     * In milli.
     */
    private int mAnimationDuration;

    /**
     * Boolean used to know if the actionBar should be blurred.
     */
    private boolean mBlurredActionBar;

    /**
     * Boolean used to know if RenderScript should be used
     */
    private boolean mUseRenderScript;
    private ImageView mBlurredImageView;

    /**
     * Constructor.
     *
     * @param holdingActivity activity which holds the DialogFragment.
     */
    public RxBlurDialogEngine(Activity holdingActivity) {
        mHoldingActivity = holdingActivity;
        mAnimationDuration = holdingActivity.getResources().getInteger(R.integer.blur_dialog_animation_duration);
    }

    /**
     * Must be linked to the original lifecycle.
     *
     * @param activity holding activity.
     */
    public void onAttach(Activity activity) {
        mHoldingActivity = activity;
    }

    /**
     * Resume the engine.
     *
     * @param retainedInstance use getRetainInstance.
     */
    public void onResume(boolean retainedInstance) {
        if (mBlurredImageView == null || retainedInstance) {
            if (mHoldingActivity.getWindow().getDecorView().isShown()) {
                performBlur();
            } else {
                mHoldingActivity.getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            // dialog can have been closed before being drawn
                            if (mHoldingActivity != null) {
                                mHoldingActivity.getWindow().getDecorView()
                                    .getViewTreeObserver().removeOnPreDrawListener(this);
                                performBlur();
                            }
                            return true;
                        }
                    }
                );
            }
        }
    }
    Subscription mSubscription;
    public void performBlur() {
        List<Transformation> transformations = new ArrayList<>();
        transformations.add(getBlurTransformation());
        transformations.addAll(mTransformations);
        BlurringSettings blurringSettings = new BlurringSettings(mBlurredActionBar,mDownScaleFactor,mUseRenderScript,mBlurRadius,transformations);
        RxBluring rxBluring = new RxBluring(mHoldingActivity,mToolbar,blurringSettings);
        mSubscription = rxBluring.getBlurResponseObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BlurResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(BlurResponse blurResponse) {
                        mBlurredImageView = blurResponse.getImageView();
                        FrameLayout.LayoutParams layoutParams = blurResponse.getLayoutParams();

                        mHoldingActivity.getWindow().addContentView(
                                mBlurredImageView,
                                layoutParams
                        );

                        mBlurredImageView.setAlpha(0f);
                        mBlurredImageView
                                .animate()
                                .alpha(1f)
                                .setDuration(mAnimationDuration)
                                .setInterpolator(new LinearInterpolator())
                                .start();
                    }
                });
    }

    /**
     * Must be linked to the original lifecycle.
     */
    @SuppressLint("NewApi")
    public void onDismiss() {
        //remove blurred background and clear memory, could be null if dismissed before blur effect
        //processing ends
        //cancel async task
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        if (mBlurredImageView != null) {
                mBlurredImageView
                    .animate()
                    .alpha(0f)
                    .setDuration(mAnimationDuration)
                    .setInterpolator(new AccelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            removeBlurredView();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            removeBlurredView();
                        }
                    }).start();
        }
    }

    /**
     * Must be linked to the original lifecycle.
     */
    public void onDetach() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mSubscription = null;
        mHoldingActivity = null;
    }

    /**
     * Enable / disable debug mode.
     * <p/>
     * LogCat and graphical information directly on blurred screen.
     *
     * @param enable true to display log in LogCat.
     */
    public void debug(boolean enable) {
        mDebugEnable = enable;
    }

    /**
     * Apply custom down scale factor.
     * <p/>
     * By default down scale factor is set to
     * {@link DefaultSettings#DEFAULT_BLUR_DOWN_SCALE_FACTOR}
     * <p/>
     * Higher down scale factor will increase blurring speed but reduce final rendering quality.
     *
     * @param factor customized down scale factor, must be at least 1.0 ( no down scale applied )
     */
    public void setDownScaleFactor(float factor) {
        if (factor >= 1.0f) {
            mDownScaleFactor = factor;
        } else {
            mDownScaleFactor = 1.0f;
        }
    }

    /**
     * Apply custom blur radius.
     * <p/>
     * By default blur radius is set to
     * {@link DefaultSettings#DEFAULT_BLUR_RADIUS}
     *
     * @param radius custom radius used to blur.
     */
    public void setBlurRadius(int radius) {
        if (radius >= 0) {
            mBlurRadius = radius;
        } else {
            mBlurRadius = 0;
        }
    }

    /**
     * Set use of RenderScript
     * <p/>
     * By default RenderScript is set to
     * {@link DefaultSettings#DEFAULT_USE_RENDERSCRIPT}
     * <p/>
     * Don't forget to add those lines to your build.gradle
     * <pre>
     *  defaultConfig {
     *  ...
     *  renderscriptTargetApi 22
     *  renderscriptSupportModeEnabled true
     *  ...
     *  }
     * </pre>
     *
     * @param useRenderScript use of RenderScript
     */
    public void setUseRenderScript(boolean useRenderScript) {
        mUseRenderScript = useRenderScript;
    }

    /**
     * Enable / disable blurred action bar.
     * <p/>
     * When enabled, the action bar is blurred in addition of the content.
     *
     * @param enable true to blur the action bar.
     */
    public void setBlurActionBar(boolean enable) {
        mBlurredActionBar = enable;
    }

    /**
     * Set a toolbar which isn't set as action bar.
     *
     * @param toolbar toolbar.
     */
    public void setToolbar(Toolbar toolbar) {
        mToolbar = toolbar;
    }


    private Transformation getBlurTransformation() {
        return new BlurTransformation(mHoldingActivity, mBlurRadius, 1);
    }


    /**
     * Removed the blurred view from the view hierarchy.
     */
    private void removeBlurredView() {
        if (mBlurredImageView != null) {
            ViewGroup parent = (ViewGroup) mBlurredImageView.getParent();
            if (parent != null) {
                parent.removeView(mBlurredImageView);
            }
            mBlurredImageView = null;
        }
    }

    public void addTransformations(List<Transformation> transformations) {
        if (transformations!=null && !transformations.isEmpty())
        mTransformations.addAll(transformations);
    }

    /**
     * Async task used to process blur out of ui thread
     */
}
