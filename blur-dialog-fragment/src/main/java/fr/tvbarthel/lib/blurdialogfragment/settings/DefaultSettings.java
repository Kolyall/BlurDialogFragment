package fr.tvbarthel.lib.blurdialogfragment.settings;

/**
 * Created by Nick Unuchek on 30.11.2017.
 */

public class DefaultSettings {
    /**
     * Since image is going to be blurred, we don't care about resolution.
     * Down scale factor to reduce blurring time and memory allocation.
     */
    public static final float DEFAULT_BLUR_DOWN_SCALE_FACTOR = 4.0f;
    /**
     * Radius used to blur the background
     */
    public static final int DEFAULT_BLUR_RADIUS = 8;
    /**
     * Default dimming policy.
     */
    public static final boolean DEFAULT_DIMMING_POLICY = false;
    /**
     * Default debug policy.
     */
    public static final boolean DEFAULT_DEBUG_POLICY = false;
    /**
     * Default action bar blurred policy.
     */
    public static final boolean DEFAULT_ACTION_BAR_BLUR = false;
    /**
     * Default use of RenderScript.
     */
    public static final boolean DEFAULT_USE_RENDERSCRIPT = false;
}
