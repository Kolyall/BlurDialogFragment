package fr.tvbarthel.lib.blurdialogfragment.rx;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import fr.tvbarthel.lib.blurdialogfragment.manager.BlurManager;
import fr.tvbarthel.lib.blurdialogfragment.settings.BlurResponse;
import fr.tvbarthel.lib.blurdialogfragment.settings.BlurringSettings;
import fr.tvbarthel.lib.blurdialogfragment.utils.Utils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Nick Unuchek on 30.11.2017.
 */

public class RxBluring {
    public static final String TAG = RxBluring.class.getSimpleName();

    private Activity mHoldingActivity;
    private Toolbar mToolbar;

    private BlurringSettings mBlurringSettings;

    public RxBluring(Activity holdingActivity, Toolbar toolbar,BlurringSettings blurringSettings) {
        mHoldingActivity = holdingActivity;
        mToolbar = toolbar;
        mBlurringSettings = blurringSettings;
    }

    public Observable<BlurResponse> getBlurResponseObservable() {

        return Observable.just(Utils.takeScreenShot(mHoldingActivity))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap(bitmap -> {


                    View backgroundView = mHoldingActivity.getWindow().getDecorView();

                    //retrieve background view, must be achieved on ui thread since
                    //only the original thread that created a view hierarchy can touch its views.

                    Rect rect = new Rect();
                    backgroundView.getWindowVisibleDisplayFrame(rect);
                    /**
                     * After rotation, the DecorView has no height and no width. Therefore
                     * .getDrawingCache() return null. That's why we  have to force measure and layout.
                     */
                    if (bitmap == null) {
                        backgroundView.measure(
                                View.MeasureSpec.makeMeasureSpec(rect.width(), View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(rect.height(), View.MeasureSpec.EXACTLY)
                        );
                        backgroundView.layout(0, 0, backgroundView.getMeasuredWidth(),
                                backgroundView.getMeasuredHeight());
                        bitmap = Utils.takeScreenShot(mHoldingActivity);
                    }
                    BlurManager blurManager = new BlurManager(mHoldingActivity, mToolbar, bitmap, backgroundView, mBlurringSettings);
                    BlurResponse blurResponse = blurManager.performBlurring();

                    backgroundView.destroyDrawingCache();
                    backgroundView.setDrawingCacheEnabled(false);
                    return Observable.just(blurResponse);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


}
