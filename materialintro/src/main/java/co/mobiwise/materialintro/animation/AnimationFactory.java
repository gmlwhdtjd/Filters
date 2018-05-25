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

        firstDotAnimatorSet.playTogether(alpha1,alpha2,alpha3);
        firstDotAnimatorSet.start();
    }

}
