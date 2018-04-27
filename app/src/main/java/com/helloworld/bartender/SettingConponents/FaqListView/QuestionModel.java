package com.helloworld.bartender.SettingConponents.FaqListView;

import android.animation.TimeInterpolator;

/**
 * Created by samer on 2018-04-16.
 */

public class QuestionModel {
    public final String question;
    public final String answer;
    public final TimeInterpolator interpolator;

    public QuestionModel(String description,String answer, TimeInterpolator interpolator) {
        this.question = description;
        this.interpolator = interpolator;
        this.answer = answer;
    }
}
