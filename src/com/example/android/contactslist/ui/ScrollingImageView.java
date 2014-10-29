package com.example.android.contactslist.ui;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom view used to render an horizontal part (slice) of an image.
 *
 * Modified from Nicolas POMEPUY's work found at
 * https://github.com/PomepuyN/BlurEffectForAndroidDesign/blob/master/BlurEffect/src/com/npi/blureffect/ScrollableImageView.java
 *
 */
class ScrollingImageView extends View {

    // A bitmap adapted to the View size
    private Bitmap adaptedImage;
    // A Paint object used to render the image
    private Paint paint = new Paint();
    // The original Bitmap
    private Bitmap originalImage;
    // The screen width used to render the image
    private int screenWidth;
    private int scrollY = 0;

    public ScrollingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScrollingImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollingImageView(Context context) {
        this(context, null);
    }

    /**
     * Draws the view if the adapted image is not null
     */
    @Override
    protected void onDraw(Canvas canvas) {

        if (adaptedImage != null)
            canvas.drawBitmap(adaptedImage, 0, 0, paint);

    }

    /**
     * Handle an external scroll and render the image by switching it by a
     * distance
     *
     * @param distY
     *            the distance from the top
     */
    public void updateScroll(float distY) {
        distY = Math.abs(distY);

        if (getHeight() > 0 && originalImage != null && distY != scrollY) {

            scrollY = (int)distY;
            if (scrollY < originalImage.getHeight() - getHeight()) {
                adaptedImage = Bitmap.createBitmap(originalImage, 0, scrollY, screenWidth, getHeight());

                invalidate();
            }
        }

    }

    public void setBackgroundImage(Bitmap bmp) {
        this.originalImage = bmp;
        adaptedImage = Bitmap.createBitmap(bmp);
        invalidate();
    }

    public void setViewWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

}