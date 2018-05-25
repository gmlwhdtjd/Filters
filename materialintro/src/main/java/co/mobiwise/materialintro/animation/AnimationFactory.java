package co.mobiwise.materialintro.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.widget.ImageView;

import co.mobiwise.materialintro.R;

/**
 * Created by mertsimsek on 25/01/16.
 */
public class AnimationFactory {

    /**
     * MaterialIntroView will appear on screen with
     * fade in animation. Notifies onAnimationStartListener
     * when fade in animation is about to start.
     *
     * @param view
     * @param duration
     * @param onAnimationStartListener
     */

    public static boolean isAnimationPlaying = false;


    public static void animateFadeIn(View view, long duration, final AnimationListener.OnAnimationStartListener onAnimationStartListener) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        objectAnimator.setDuration(duration);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimationPlaying = true;
                if (onAnimationStartListener != null)
                    onAnimationStartListener.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimationPlaying = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        if (!isAnimationPlaying) {
            objectAnimator.start();
        }
    }

    /**
     * MaterialIntroView will disappear from screen with
     * fade out animation. Notifies onAnimationEndListener
     * when fade out animation is ended.
     *
     * @param view
     * @param duration
     * @param onAnimationEndListener
     */
    public static void animateFadeOut(View view, long duration, final AnimationListener.OnAnimationEndListener onAnimationEndListener) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 1, 0);
        objectAnimator.setDuration(duration);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimationPlaying = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimationPlaying = false;
                if (onAnimationEndListener != null)
                    onAnimationEndListener.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        if (!isAnimationPlaying) {
            objectAnimator.start();
        }
    }

    public static void performAnimation(View view) {

        ImageView guidePoint1 = view.findViewById(R.id.guide_pointer1);
        ImageView guidePoint2 = view.findViewById(R.id.guide_pointer2);
        ImageView guidePoint3 = view.findViewById(R.id.guide_pointer3);


        AnimatorSet firstDotAnimatorSet = new AnimatorSet();

        ValueAnimator alpha1 = ObjectAnimator.ofFloat(guidePoint1, "alpha", 1.0f, 0.0f);
        alpha1.setDuration(900);
        alpha1.setRepeatCount(ValueAnimator.INFINITE);
        alpha1.setRepeatMode(ValueAnimator.REVERSE);
        alpha1.setStartDelay(600);

        ValueAnimator alpha2 = ObjectAnimator.ofFloat(guidePoint2, "alpha", 1.0f, 0.0f);
        alpha2.setDuration(900);
        alpha2.setRepeatCount(ValueAnimator.INFINITE);
        alpha2.setRepeatMode(ValueAnimator.REVERSE);
        alpha2.setStartDelay(300);

        ValueAnimator alpha3 = ObjectAnimator.ofFloat(guidePoint3, "alpha", 1.0f, 0.0f);
        alpha3.setDuration(900);
        alpha3.setRepeatCount(ValueAnimator.INFINITE);
        alpha3.setRepeatMode(ValueAnimator.REVERSE);

        ValueAnimator scaleX1 = ObjectAnimator.ofFloat(guidePoint1, "scaleX", 1.0f, 1.2f);
        scaleX1.setDuration(900);
        scaleX1.setRepeatCount(ValueAnimator.INFINITE);
        scaleX1.setRepeatMode(ValueAnimator.REVERSE);
        scaleX1.setStartDelay(600);

        ValueAnimator scaleX2 = ObjectAnimator.ofFloat(guidePoint2, "scaleX", 1.0f, 1.2f);
        scaleX2.setDuration(900);
        scaleX2.setRepeatCount(ValueAnimator.INFINITE);
        scaleX2.setRepeatMode(ValueAnimator.REVERSE);
        scaleX2.setStartDelay(300);

        ValueAnimator scaleX3 = ObjectAnimator.ofFloat(guidePoint3, "scaleX", 1.0f, 1.2f);
        scaleX3.setDuration(900);
        scaleX3.setRepeatCount(ValueAnimator.INFINITE);
        scaleX3.setRepeatMode(ValueAnimator.REVERSE);

        ValueAnimator scaleY1 = ObjectAnimator.ofFloat(guidePoint1, "scaleY", 1.0f, 1.2f);
        scaleY1.setDuration(900);
        scaleY1.setRepeatCount(ValueAnimator.INFINITE);
        scaleY1.setRepeatMode(ValueAnimator.REVERSE);
        scaleY1.setStartDelay(600);

        ValueAnimator scaleY2 = ObjectAnimator.ofFloat(guidePoint2, "scaleY", 1.0f, 1.2f);
        scaleY2.setDuration(900);
        scaleY2.setRepeatCount(ValueAnimator.INFINITE);
        scaleY2.setRepeatMode(ValueAnimator.REVERSE);
        scaleY2.setStartDelay(300);

        ValueAnimator scaleY3 = ObjectAnimator.ofFloat(guidePoint3, "scaleY", 1.0f, 1.2f);
        scaleY3.setDuration(900);
        scaleY3.setRepeatCount(ValueAnimator.INFINITE);
        scaleY3.setRepeatMode(ValueAnimator.REVERSE);

        firstDotAnimatorSet.playTogether(alpha1,alpha2,alpha3,scaleX1,scaleX2,scaleX3, scaleY1,scaleY2,scaleY3);
        firstDotAnimatorSet.start();
    }

}
