package fr.tvbarthel.lib.blurdialogfragment.settings;

import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by Nick Unuchek on 30.11.2017.
 */
public class BlurResponse {
    ImageView mImageView;
    FrameLayout.LayoutParams mLayoutParams;

    public BlurResponse(ImageView imageView, FrameLayout.LayoutParams mLayoutParams) {
        this.mImageView = imageView;
        this.mLayoutParams = mLayoutParams;
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public FrameLayout.LayoutParams getLayoutParams() {
        return mLayoutParams;
    }
}
