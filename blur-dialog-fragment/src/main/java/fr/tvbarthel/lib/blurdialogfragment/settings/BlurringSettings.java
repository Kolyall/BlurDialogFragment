package fr.tvbarthel.lib.blurdialogfragment.settings;

import com.squareup.picasso.Transformation;

import java.util.List;

/**
 * Created by Nick Unuchek on 30.11.2017.
 */

public class BlurringSettings {
    boolean blurredActionBar;
    float downScaleFactor;
    boolean useRenderScript;
    int blurRadius;
    List<Transformation> transformations;

    public BlurringSettings(boolean blurredActionBar, float downScaleFactor, boolean useRenderScript, int blurRadius, List<Transformation> transformations) {
        this.blurredActionBar = blurredActionBar;
        this.downScaleFactor = downScaleFactor;
        this.useRenderScript = useRenderScript;
        this.blurRadius = blurRadius;
        this.transformations = transformations;
    }

    public boolean isBlurredActionBar() {
        return blurredActionBar;
    }

    public float getDownScaleFactor() {
        return downScaleFactor;
    }

    public boolean isUseRenderScript() {
        return useRenderScript;
    }

    public int getBlurRadius() {
        return blurRadius;
    }

    public List<Transformation> getTransformations() {
        return transformations;
    }
}
