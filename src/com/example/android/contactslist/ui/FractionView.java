package com.example.android.contactslist.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.RectF;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.example.android.contactslist.R;

public class FractionView extends View {

    private String TAG = "FractionView";
    private Paint mCirclePaint;
    private Paint mRedPaint;
    private RectF mSectorOval;
    private Paint mGreenPaint;
    private Paint mBackgroundPaint;
    private Paint mTextPaint = new Paint();

    private float mfraction = 1;
    private int mDaysRemaining;
    private int mTotalDays;
    private String mDisplayString_1;
    private String mDisplayString_2;
    private int cx = 0;
    private int cy = 0;
    private int radius = 0;
    private int touchCount = 0;
    private TranslateAnimation translateAnimation;
    private int textSize = 25;
    private boolean mLargeCanvas = false;




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

      //TODO: fix background color to match rest of UI.  For some reason the color code does not produce the right color.
    mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mBackgroundPaint.setColor(Color.parseColor("#F5F5F5"));
    mBackgroundPaint.setStyle(Paint.Style.FILL);

    setBackgroundColor(Color.parseColor("#F5F5F5"));//Color.LTGRAY);
    //TODO Choose better color or transpanrancy for background

    mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mRedPaint.setColor(getResources().getColor(android.R.color.holo_green_light));//Color.RED);
    mRedPaint.setStyle(Paint.Style.FILL);

    mSectorOval = new RectF();

    mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mGreenPaint.setColor(getResources().getColor(android.R.color.holo_red_dark));//Color.GREEN);
    mGreenPaint.setStyle(Paint.Style.FILL);

    mTextPaint.setTextSize(0);
    mTextPaint.setColor(Color.BLACK);
    mTextPaint.setTextAlign(Paint.Align.CENTER);
    mTextPaint.setTypeface(Typeface.SANS_SERIF);
    mTextPaint.setAntiAlias(true);

  }

    public Parcelable onSaveInstanceState() {
        // save state - so if you rotate the screen it will do the right thing.
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putFloat("fraction", mfraction);

        return bundle;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mfraction = bundle.getFloat("fraction");
            super.onRestoreInstanceState(bundle.getParcelable("superState"));
        } else {
            super.onRestoreInstanceState(state);
        }
        setFractionFloat(mfraction);
    }

    public void setFraction(int days_remaining, int total_days) {
        // Prevent invalid state
        //TODO: Create a special display case for the due date being in the past.  Color things black?
        if ((days_remaining < 0)){
            days_remaining = 0;
        }
        if(total_days <= 0){
            total_days = 0;
        }
        if((days_remaining > total_days)){
            total_days += days_remaining;
        }

        mDaysRemaining = days_remaining;
        mTotalDays = total_days;
        mfraction = ((float)days_remaining)/((float)total_days);
        invalidate();

        if (mListener != null) {
            mListener.onChange(days_remaining, total_days);
        }
        setDisplayStrings();
    }

    public void setFractionFloat(float fraction) {
        if (fraction < 0) return;
        // Prevent invalid state
        if (fraction > 1) return;

        mfraction = fraction;
        invalidate();

    }

   protected void onDraw(Canvas canvas) {

       setPadding(7, 7, 7, 7);
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

       mLargeCanvas = size > 199; // define a large canvase as greater than 199p on the shortest dimension
       //Log.d(TAG, "Width: " + width + " Height: " + height + " X: " + cx + " Y: " + cy + " PaddingTop: " + getPaddingTop() + " PaddingBottom: " + getPaddingBottom());

       canvas.drawArc(mSectorOval, 270, getSweepAngle(), true, mRedPaint);
       // implement "Sweep" for indicator

       canvas.drawCircle(cx, cy, (radius-7), mBackgroundPaint);  //Define thickness of the ring

       // set the display text
       //if(mLargeCanvas){
          canvas.drawText(mDisplayString_1, cx, cy+14, mTextPaint);
      /*}else{
           Path circle = new Path();
           circle.addCircle(cx, cy, radius, Path.Direction.CW);
           canvas.drawTextOnPath(mDisplayString_1, circle, height-30, 30, mTextPaint);
       }*/



   }
    private float getSweepAngle() {
        return (float)(mfraction * 360f);
    }

    private void setDisplayStrings(){
        //for reasons unknown, breaking this operation into several lines was essential for operation
        /*
       float displayValue = mfraction*100;
       displayValue = Math.round(displayValue);
       displayValue = displayValue/100;
       mDisplayString = String.valueOf(displayValue);
       */

        mDisplayString_1 = String.valueOf(mDaysRemaining);
        mDisplayString_1 += " day";

        if(mLargeCanvas){
            textSize = 40;
            mTextPaint.setTextSize(textSize);
        }/*else{
            mDisplayString_1 += "  d a y ";
        }*/

        if(mDaysRemaining !=1){
            mDisplayString_1 += "s";
        }

        mDisplayString_2 = "t o  g o";

        //mDisplayString = String.valueOf(mTotalDays-mDaysRemaining) + "  days  lapsed";

    }

    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == event.ACTION_DOWN){ //Only execute on Finger down action

            float ScaleFrom = (float)0.9;
            float ScaleTo = (float)1.15;

            ScaleAnimation scaleAnimation = new ScaleAnimation(ScaleFrom, ScaleTo, ScaleFrom, ScaleTo, cx, cy);
            scaleAnimation.setDuration(100);
            scaleAnimation.setInterpolator(new AccelerateInterpolator(1.5f));
            this.startAnimation(scaleAnimation);

            /*
            translateAnimation = new TranslateAnimation(0, 10, 0, 10);
            translateAnimation.setDuration(100);
            translateAnimation.setInterpolator(new AccelerateInterpolator(1.5f));
            translateAnimation.setRepeatCount(2);
            translateAnimation.setRepeatMode(Animation.REVERSE);
            this.startAnimation(translateAnimation);
            */
            setDisplayStrings();
            switch ((touchCount%2 )){
                 case 1:
                     mTextPaint.setTextSize(0); // hide text
                     break;
                case 2:
                    mTextPaint.setTextSize(textSize);  //show text
                    break;
                case 0:
                default:
                    mTextPaint.setTextSize(textSize);  //show text

            }
            invalidate();
            touchCount++;
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