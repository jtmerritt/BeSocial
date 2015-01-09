package com.okcommunity.contacts.cultivate.util;


import android.content.Context;
import android.graphics.Bitmap;
import com.squareup.picasso.Transformation;


/**
 * Created by Tyson Macdonald on 12/18/2014.
 *
 * based on http://stackoverflow.com/questions/23740307/load-large-images-with-picasso-and-custom-transform-object
 */


/*
                    final File f = new File(path);

                    Picasso.with(mContext)
                            .load(f)
                            .priority(Picasso.Priority.HIGH)
                            .placeholder(R.drawable.ic_contact_picture_180_holo_light)
                            .resize(screenWidth, getScreenHeight)
                            .centerInside()
                            .noFade()
                            .into(mContactDetailImageView);


                    Picasso.with(mContext)
                            .load(f)
                            //.priority(Picasso.Priority.LOW)
                            //.placeholder(R.drawable.ic_contact_picture_180_holo_light)
                            .resize(screenWidth, getScreenHeight)
                            .centerInside()
                            .transform(new BlurTransform(mContext, screenWidth))
                            .skipMemoryCache()
                            .noFade()

                            .into(mBlurredContactDetailImageView)
                            ;
*/

public class BlurTransform implements Transformation {
    Context context;
    int screenWidth;

    public BlurTransform(Context context, int screenWidth) {
        this.context = context;
        this.screenWidth = screenWidth;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        final Bitmap blurredBackgroundImage = Blur.fastblur(context, source, 25);


        // trim the blurred image down to size
        final Bitmap result = Bitmap.createScaledBitmap(
                blurredBackgroundImage, screenWidth,
                (int) (blurredBackgroundImage.getHeight()
                        * ((float) screenWidth) /
                        ( float) blurredBackgroundImage.getWidth()), false);

        source.recycle();

        return result;
    }

    @Override
    public String key() {
        return "blur";
    }

};