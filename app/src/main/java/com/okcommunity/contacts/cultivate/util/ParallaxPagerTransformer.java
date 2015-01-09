package com.okcommunity.contacts.cultivate.util;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.okcommunity.contacts.cultivate.FloatingActionButton.FloatingActionButton2;

/*
    Taken directly from https://github.com/xgc1986/ParallaxPagerTransformer
 */
public class ParallaxPagerTransformer implements ViewPager.PageTransformer {

    private int id_1, id_2, fab_id;
    private int border = 0;
    private float speed = 0.5f;

    public ParallaxPagerTransformer(int id_1, int id_2, int fab_id) {
        this.id_1 = id_1;
        this.id_2 = id_2;
        this.fab_id = fab_id;
    }

    @Override
    public void transformPage(View view, float position) {

        View parallaxView_1 = view.findViewById(id_1);
        View parallaxView_2 = view.findViewById(id_2);
        FloatingActionButton2 fabView = (FloatingActionButton2) view.findViewById(fab_id);

        if (parallaxView_1 != null || parallaxView_2 != null) {
            if (position > -1 && position < 1) {
                float width_1 = parallaxView_1.getWidth();
                parallaxView_1.setTranslationX(-(position * width_1 * speed));

                float width_2 = parallaxView_2.getWidth();
                parallaxView_2.setTranslationX(-(position * width_2 * speed));

                // keep the floating action button in the in place on the screen during side scroll
                fabView.setTranslationX(-(position * width_2));

                float sc = ((float)view.getWidth() - border)/ view.getWidth();
                if (position == 0) {
                    view.setScaleX(1);
                    view.setScaleY(1);
                } else {
                    view.setScaleX(sc);
                    view.setScaleY(sc);

                    // if the button is checked, then perform a click on it to retract the menu
                    if(fabView.isChecked() == true){
                        fabView.performClick();
                    }
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