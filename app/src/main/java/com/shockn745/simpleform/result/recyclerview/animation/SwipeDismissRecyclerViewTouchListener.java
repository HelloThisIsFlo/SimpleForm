package com.shockn745.simpleform.result.recyclerview.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * A {@link View.OnTouchListener} that makes the list items in a {@link ListView}
 * dismissable. {@link ListView} is given special treatment because by default it handles touches
 * for its list items... i.e. it's in charge of drawing the pressed state (the list selector),
 * handling list item clicks, etc.
 *
 * <p>After creating the listener, the caller should also call
 * {@link ListView#setOnScrollListener(AbsListView.OnScrollListener)}, passing
 * in the scroll listener returned by {@link #makeScrollListener()}. If a scroll listener is
 * already assigned, the caller should still pass scroll changes through to this listener. This will
 * ensure that this {@link SwipeDismissRecyclerViewTouchListener} is paused during list view
 * scrolling.</p>
 *
 * <p>Example usage:</p>
 *
 * <pre>
 * SwipeDismissRecyclerViewTouchListener touchListener =
 *         new SwipeDismissRecyclerViewTouchListener(
 *                 listView,
 *                 new SwipeDismissRecyclerViewTouchListener.OnDismissCallback() {
 *                     public void onDismiss(ListView listView, int[] reverseSortedPositions) {
 *                         for (int position : reverseSortedPositions) {
 *                             adapter.remove(adapter.getItem(position));
 *                         }
 *                         adapter.notifyDataSetChanged();
 *                     }
 *                 });
 * listView.setOnTouchListener(touchListener);
 * listView.setOnScrollListener(touchListener.makeScrollListener());
 * </pre>
 *
 */
public class SwipeDismissRecyclerViewTouchListener implements View.OnTouchListener {

    // Cached ViewConfiguration and system-wide constant values
    private final int mSlop;
    private final int mMinFlingVelocity;
    private final int mMaxFlingVelocity;
    private final long mAnimationTime;

    // Fixed properties
    private final RecyclerView mRecyclerView;
    private final DismissCallbacks mDismissCallbacks;
    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero
    private final Handler mHandler;
    private final long mRecyclerViewRemoveAnimationDuration;

    // Transient properties
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private int mSwipingSlop;
    private VelocityTracker mVelocityTracker;
    private int mDownPosition;
    private View mDownView;
    private boolean mPaused;
    private boolean mDismissAnimationRunning;

    // Properties for the OnScrollListener & LayoutManager
    private final Activity mActivity;
    private int mCurrentTranslationY;


    /**
     * The callback interface used by {@link SwipeDismissRecyclerViewTouchListener} to inform its client
     * about a successful dismissal of one or more list item positions.
     */
    public interface DismissCallbacks {
        /**
         * Called to determine whether the given position can be dismissed.
         */
        boolean canDismiss(int position);

        /**
         * Called when the user has indicated they she would like to dismiss a list item
         *
         * @param recyclerView  The originating {@link ListView}.
         * @param position      Position of the item to dismiss
         */
        void onDismiss(RecyclerView recyclerView, int position);
    }

    /**
     * Constructs a new swipe-to-dismiss touch listener for the given list view.
     *
     * @param recyclerView  The list view whose items should be dismissable.
     * @param callbacks The callback to trigger when the user has indicated that she would like to
     *                  dismiss one or more list items.
     */
    public SwipeDismissRecyclerViewTouchListener(
            RecyclerView recyclerView,
            DismissCallbacks callbacks,
            Activity activity) {
        this.mActivity = activity;
        ViewConfiguration vc = ViewConfiguration.get(recyclerView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = recyclerView.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mRecyclerView = recyclerView;
        mDismissCallbacks = callbacks;
        mHandler = new Handler();
        RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
        if (itemAnimator != null) {
            mRecyclerViewRemoveAnimationDuration = itemAnimator.getRemoveDuration();
        } else {
            mRecyclerViewRemoveAnimationDuration = 0;
        }
    }

    /**
     * Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
     *
     * @param enabled Whether or not to watch for gestures.
     */
    private void setEnabled(boolean enabled) {
        mPaused = !enabled;
    }

    /**
     * Returns an {@link AbsListView.OnScrollListener} to be added to the {@link
     * ListView} using {@link ListView#setOnScrollListener(AbsListView.OnScrollListener)}.
     * If a scroll listener is already assigned, the caller should still pass scroll changes through
     * to this listener. This will ensure that this {@link SwipeDismissRecyclerViewTouchListener} is
     * paused during list view scrolling.</p>
     *
     * Also this scroll listener hides/unhides the toolbar & FAB when scrolling
     *
     * @see SwipeDismissRecyclerViewTouchListener
     */
    public RecyclerView.OnScrollListener makeScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                setEnabled(newState != RecyclerView.SCROLL_STATE_DRAGGING);
            }

        };
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mViewWidth < 2) {
            mViewWidth = mRecyclerView.getWidth();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                // Prevent scrolling if animation is running
                if (mDismissAnimationRunning || isAddRemoveAnimationRunning()) {
                    return true;
                }
                if (mPaused) {
                    return false;
                }

                // TODO EXTERNAL : ensure this is a finger, and set a flag

                // Find the child view that was touched (perform a hit test)
                // cf : http://stackoverflow.com/questions/13296162/what-is-the-definition-of-the-value-supplied-by-the-android-function-view-gethit
                Rect rect = new Rect();
                int[] listViewCoords = new int[2];
                mRecyclerView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                View child;
                int childCount = mRecyclerView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    child = mRecyclerView.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        mDownView = child;
                        break;
                    }
                }

                if (mDownView != null) {
                    mDownX = motionEvent.getRawX();
                    mDownY = motionEvent.getRawY();
                    mDownPosition = mRecyclerView.getChildPosition(mDownView);

                    // Helper for tracking the velocity of touch events, for implementing
                    // flinging and other such gestures
                    // cf : Doc VelocityTracker
                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(motionEvent);
                }
                return false;
            }

            // When is ACTION_CANCEL called ?
            // -----------------------------
            // The current gesture has been aborted. You will not receive any more points in it.
            // You should treat this as an up event, but not perform any action that you normally
            // would.
            //
            // cf : http://stackoverflow.com/questions/11960861/what-causes-a-motionevent-action-cancel-in-android
            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker == null) {
                    break;
                }

                if (mDownView != null && mSwiping) {
                    // Animate view back in initial position
                    mDownView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    break;
                }

                float deltaX = motionEvent.getRawX() - mDownX;
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity(); // Used later
                float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
                boolean dismiss = false;
                boolean dismissRight = false;
                if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
                    dismiss = true;
                    dismissRight = deltaX > 0;
                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                        && absVelocityY < absVelocityX && mSwiping) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (velocityX < 0) == (deltaX < 0);
                    dismissRight = mVelocityTracker.getXVelocity() > 0;
                }
                if (dismiss
                        && mDownPosition != ListView.INVALID_POSITION
                        && mDismissCallbacks.canDismiss(mDownPosition)) {
                    // dismiss
                    final View downView = mDownView; // mDownView gets null'd before animation ends
                    final int downPosition = mDownPosition;
                    // Deactivate listener during animation (only one swipe to dismiss at a time)
                    mDismissAnimationRunning = true;
                    mDownView.animate()
                            .translationX(dismissRight ? mViewWidth : -mViewWidth)
                            .alpha(0)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    performDismiss(downView, downPosition);
                                    // Delay reset mDismissAnimationRunning to prevent swipe
                                    // between dismiss & remove animations
                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mDismissAnimationRunning = false;
                                        }
                                    }, mRecyclerViewRemoveAnimationDuration);
                                }
                            });
                } else {
                    // cancel
                    mDownView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // Prevent scrolling if animation is running
                if (mDismissAnimationRunning || isAddRemoveAnimationRunning()) {
                    return true;
                }
                if (mVelocityTracker == null
                        || mPaused) {
                    break;
                }

                mVelocityTracker.addMovement(motionEvent);
                float deltaX = motionEvent.getRawX() - mDownX;
                float deltaY = motionEvent.getRawY() - mDownY;
                if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                    mSwiping = true;
                    mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
                    mRecyclerView.requestDisallowInterceptTouchEvent(true);

                    // Cancel ListView's touch (un-highlighting the item)
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex()
                                    << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mRecyclerView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                if (mSwiping) {
                    // Animate the view to follow finger and fade out
                    mDownView.setTranslationX(deltaX - mSwipingSlop);
                    mDownView.setAlpha(Math.max(
                                    0f,
                                    Math.min(1f,1f - 1f * Math.abs(deltaX) / mViewWidth)
                            )
                    );
                    return true;
                }
                break;
            }
        }
        return false;
    }

    /**
     * Check if the built in remove animation of the recyclerview is running
     * @return true if the animation is running
     */
    private boolean isAddRemoveAnimationRunning() {
        RecyclerView.ItemAnimator itemAnimator = mRecyclerView.getItemAnimator();
        return itemAnimator != null && itemAnimator.isRunning();
    }

    private void performDismiss(final View dismissView, final int dismissPosition) {
        // Animate the dismissed list item to zero-height and fire the dismiss callback when
        // all dismissed list item animations have completed. This triggers layout on each animation
        // frame; in the future we may want to do something smarter and more performant.

        // Trigger callback
        mDismissCallbacks.onDismiss(mRecyclerView, dismissPosition);

        // Reset mDownPosition to avoid MotionEvent.ACTION_UP trying to start a dismiss
        // animation with a stale position
        // (mDownPosition is reset on "ACTION_DOWN")
        mDownPosition = ListView.INVALID_POSITION;

        // Send a cancel event
        long time = SystemClock.uptimeMillis();
        MotionEvent cancelEvent = MotionEvent.obtain(time, time,
                MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mRecyclerView.dispatchTouchEvent(cancelEvent);


        // Reset view presentation after the end of the built in animation of RecyclerView
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissView.setAlpha(1f);
                dismissView.setTranslationX(0);
            }
        }, mRecyclerViewRemoveAnimationDuration);
    }
}
