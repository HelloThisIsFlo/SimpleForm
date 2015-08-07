package com.shockn745.simpleform.main;

import android.animation.Animator;
import android.content.Context;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.shockn745.simpleform.AnimCompatUtils;
import com.shockn745.simpleform.R;

/**
 * Class to handle FAB animations
 */
public class FabAnimator {

    private FloatingActionButton mFAB;
    private Context mContext;

    private float mTranslationDistance;
    private boolean isHidden = false;

    private final static int DURATION = 1000;

    /**
     * Contructor : Use only when layout has been done
     * @param context
     * @param fab
     */
    public FabAnimator(Context context, FloatingActionButton fab) {
        mContext = context;
        mFAB = fab;
        float height = context.getResources().getDimension(R.dimen.fab_size);
        float margin = context.getResources().getDimension(R.dimen.fab_margin);
        float extra = context.getResources().getDimension(R.dimen.translation_extra);
        mTranslationDistance = height + margin + extra;
    }

    public void initFAB() {
        if (!isHidden) {
            mFAB.setEnabled(false);
            mFAB.setTranslationY(mTranslationDistance);
            isHidden = true;
        }
    }

    public void showFAB() {
        if (isHidden) {
            isHidden = false;
            mFAB.animate()
                    .translationY(0)
                    .setInterpolator(AnimCompatUtils.createInterpolator(mContext))
                    .setDuration(DURATION)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mFAB.setEnabled(true);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    })
                    .start();
        }
    }

    public void hideFAB() {
        if (!isHidden) {
            isHidden = true;
            mFAB.setEnabled(false);

            mFAB.animate()
                    .translationY(mTranslationDistance)
                    .setDuration(DURATION)
                    .setInterpolator(AnimCompatUtils.createInterpolator(mContext))
                    .start();
        }
    }

}
