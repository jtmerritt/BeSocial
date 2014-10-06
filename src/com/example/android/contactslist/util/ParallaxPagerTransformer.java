package com.example.android.contactslist.util;

import android.support.v4.view.ViewPager;
import android.view.View;
/*
    Taken directly from https://github.com/xgc1986/ParallaxPagerTransformer
 */
public class ParallaxPagerTransformer implements ViewPager.PageTransformer {

    private int id;
    private int border = 0;
    private float speed = 0.2f;

    public ParallaxPagerTransformer(int id) {
        this.id = id;
    }
    //TODO Modify to take in 2 ids for paralax of 2 photos

    @Override
    public void transformPage(View view, float position) {

        View parallaxView = view.findViewById(id);

        if (parallaxView != null) {
            if (position > -1 && position < 1) {
                float width = parallaxView.getWidth();
                parallaxView.setTranslationX(-(position * width * speed));
                float sc = ((float)view.getWidth() - border)/ view.getWidth();
                if (position == 0) {
                    view.setScaleX(1);
                    view.setScaleY(1);
                } else {
                    view.setScaleX(sc);
                    view.setScaleY(sc);
                }
            }
        }
    }

    public void setBorder(int px) {
        border = px;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }


}