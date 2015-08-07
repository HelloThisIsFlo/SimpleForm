package com.shockn745.simpleform;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Utility class to build a pre-21 compatible "circular reveal"
 * In fact, returns an alpha animation for pre-21
 */
public class AnimCompatUtils {

    /**
     * Creates a either a circular reveal or an alpha animation depending on the API
     * API >= 21 : Circular reveal + fast_out_slow_in interpolator
     * API < 21 : Alpha animation + decelerate_interpolator
     * If startRadius < finalRadius, the alpha animation will be reveal
     * If startRadius > finalRadius, the alpha animation will be hide
     *
     * @param context Context to access resources
     * @param toReveal View to reveal (or hide)
     * @param centerX Center of the circle : x coordinate
     * @param centerY Center of the circle : x coordinate
     * @param startRadius Start radius
     * @param finalRadius Final radius
     * @param duration Duration
     * @return Animator created
     */
    public static Animator createCircularReveal(
            Context context,
            View toReveal,
            int centerX,
            int centerY,
            int startRadius,
            int finalRadius,
            int duration) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Animator revealAnim = ViewAnimationUtils
                    .createCircularReveal(toReveal, centerX, centerY, startRadius, finalRadius)
                    .setDuration(duration);
            Interpolator interpolator = AnimationUtils.loadInterpolator(
                    context,
                    android.R.interpolator.fast_out_slow_in
            );
            revealAnim.setInterpolator(interpolator);

            return revealAnim;
        } else {
            // Pre-lollipop : Return alpha anim
            Animator alphaAnim;
            if (startRadius <= finalRadius) {
                // Reveal
                alphaAnim = ObjectAnimator.ofFloat(
                        toReveal,
                        "alpha",
                        0,
                        1
                );
            } else {
                // Hide
                alphaAnim = ObjectAnimator.ofFloat(
                        toReveal,
                        "alpha",
                        1,
                        0
                );
            }

            alphaAnim.setInterpolator(new DecelerateInterpolator());

            return alphaAnim;
        }
    }

    /**
     * Create either a fast_out_slow_in_interpolator or a deccelerate_interpolator
     * depending on the API level
     * @param context Context to access resources
     * @return The generated interpolator
     */
    public static Interpolator createInterpolator(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return AnimationUtils.loadInterpolator(
                    context,
                    android.R.interpolator.fast_out_slow_in
            );
        } else {
            return new DecelerateInterpolator();
        }
    }
}
