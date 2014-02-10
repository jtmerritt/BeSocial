package com.example.android.contactslist.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.RectF;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.example.android.contactslist.R;

public class FractionView extends View {

    private Paint mCirclePaint;
    private Paint mRedPaint;
    private RectF mSectorOval;
    private Paint mGreenPaint;
    private Paint mTextPaint = new Paint();

    private float mfraction = 1;
    private int cx = 0;
    private int cy = 0;
    private int radius = 0;
    private TranslateAnimation translateAnimation;
    private final int textSize = 40;
    private boolean mShowTextOnTouch = true;




    public FractionView(Context context) {
    super(context);
      // build from code
    init();
  }

  public FractionView(Context context, AttributeSet attrs) {
    super(context, attrs);
      // inflate from xml
      // other talk does this one
      // http://is.gd/AndroidCustomComp
    init();
  }

  public FractionView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
      // xml with style
    init();
  }

  private void init() {
    setBackgroundColor(Color.parseColor("#F5F5F5"));//Color.LTGRAY);
    mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mCirclePaint.setColor(Color.CYAN);
    mCirclePaint.setStyle(Paint.Style.FILL);

    mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mRedPaint.setColor(getResources().getColor(android.R.color.holo_red_dark));//Color.RED);
    mRedPaint.setStyle(Paint.Style.FILL);

    mSectorOval = new RectF();

    mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mGreenPaint.setColor(getResources().getColor(android.R.color.holo_green_light));//Color.GREEN);
    mGreenPaint.setStyle(Paint.Style.FILL);

    mTextPaint.setTextSize(0);
    mTextPaint.setColor(Color.BLACK);
    mTextPaint.setTextAlign(Paint.Align.CENTER);

    //mTextPaint.setAlpha(0);
    /*
    translateAnimation = new TranslateAnimation(0, 0, 0,  300);
    translateAnimation.setDuration(1000);
    translateAnimation.setInterpolator(new AccelerateInterpolator(1.0f));
    translateAnimation.setRepeatCount(999);
    translateAnimation.setRepeatMode(Animation.REVERSE);
    this.startAnimation(translateAnimation);
    */

  }

    public Parcelable onSaveInstanceState() {
        // save state - so if you rotate the screen it will do the right thing.
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putFloat("fraction", mfraction);
        bundle.putBoolean("showTextOnTouch", mShowTextOnTouch);

        return bundle;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mfraction = bundle.getFloat("fraction");
            mShowTextOnTouch = bundle.getBoolean("showTextOnTouch");
            super.onRestoreInstanceState(bundle.getParcelable("superState"));
        } else {
            super.onRestoreInstanceState(state);
        }
        setFractionFloat(mfraction);
    }

    public void setFraction(int numerator, int denominator) {
        if (numerator < 0) return;
        if (denominator <= 0) return;
       // Prevent invalid state
        if (numerator > denominator) return;

        mfraction = ((float)numerator)/((float)denominator);
        invalidate();

        if (mListener != null) {
            mListener.onChange(numerator, denominator);
        }

    }

    public void setFractionFloat(float fraction) {
        if (fraction < 0) return;
        // Prevent invalid state
        if (fraction > 1) return;

        mfraction = fraction;
        invalidate();

    }

   protected void onDraw(Canvas canvas) {
        /*int cx = 30;
        int cy = 30;
        int radius = 20;
        canvas.drawCircle(cx, cy, radius, mCirclePaint);
      // canvas.drawCircle(cx*2, cy*2, radius, mCirclePaint);
      // canvas.drawCircle(cx, cy*2, radius, mCirclePaint);
      // canvas.drawCircle(cx*2, cy, radius, mCirclePaint);
*/

       //for reasons unknown, breaking this operation into several lines was essential for operation
       float displayValue = mfraction*100;
       displayValue = Math.round(displayValue);
       displayValue = displayValue/100;

       canvas.drawCircle(200, 100, 5, mGreenPaint);
       int width = getWidth() - getPaddingLeft() - getPaddingRight();
       int height = getHeight() - getPaddingTop() - getPaddingBottom();
       int size = Math.min(width, height);
       cx = width / 2 + getPaddingLeft();
       cy = height / 2 + getPaddingTop();
       radius = size / 2;
       canvas.drawCircle(cx, cy, radius, mGreenPaint);

       mSectorOval.top = (height - size) / 2 + getPaddingTop();
       mSectorOval.left = (width - size) / 2 + getPaddingLeft();
       mSectorOval.bottom = mSectorOval.top + size;
       mSectorOval.right = mSectorOval.left + size;

       canvas.drawArc(mSectorOval, 270, getSweepAngle(), true, mRedPaint);

       canvas.drawText(String.valueOf(displayValue), cx, cy+14, mTextPaint);  //Round to the nearest tenth

   }
    private float getSweepAngle() {
        return (float)(mfraction * 360f);
    }

    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == event.ACTION_DOWN){ //Only execute on Finger down action

            translateAnimation = new TranslateAnimation(0, 10, 0, 10);
            translateAnimation.setDuration(100);
            translateAnimation.setInterpolator(new AccelerateInterpolator(1.5f));
            translateAnimation.setRepeatCount(2);
            translateAnimation.setRepeatMode(Animation.REVERSE);
            this.startAnimation(translateAnimation);

            if(mShowTextOnTouch){
                mTextPaint.setTextSize(textSize);  //show text
                mShowTextOnTouch = false;
            }else{
                mTextPaint.setTextSize(0); // hide text
             mShowTextOnTouch = true;
            }
        }

        /*
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }
        //Change view when finger is lifted
        // Increment the numerator, cycling back to 0 when we have filled the
        // whole circle.
        int numerator = mfraction + 1;
        if (numerator > mDenominator) {
            numerator = 0;
        }

        float x = event.getX();
        float y = event.getY();

        double distance = Math.sqrt((cx - x)*(cx -x) + (cy - y)*(cy-y));

        if (distance < radius)
            setFraction(numerator, mDenominator);
            */
        return true;
    }

    public interface OnChangeListener {
        public void onChange(int numerator, int denominator);
    }
    private OnChangeListener mListener = null;
    public void setOnChangeListener(OnChangeListener listener) {
        mListener = listener;
    }

}