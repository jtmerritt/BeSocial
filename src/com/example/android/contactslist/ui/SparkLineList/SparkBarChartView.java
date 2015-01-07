package com.example.android.contactslist.ui.SparkLineList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.ScaleAnimation;

import com.example.android.contactslist.R;


public class SparkBarChartView extends View {

    private String TAG = "SimpleBarChartView";
    private Paint mBluePaint;
    private Paint mGreenPaint;
    private Paint mOffWhitePaint;

    private float mIncomingCount;
    private float mOutgoingCount;

    private int cx = 0;
    private int cy = 0;
    static private final float RELATIVE_BAR_HEIGHT = 0.4f;
    private boolean display_values = false;



  public SparkBarChartView(Context context, AttributeSet attrs) {
    super(context, attrs);
      // inflate from xml
      // other talk does this one
      // http://is.gd/AndroidCustomComp
    init();
  }

  private void init() {

    setBackgroundColor(Color.TRANSPARENT);

    mBluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mBluePaint.setColor(getResources().getColor(R.color.incoming_event));
    mBluePaint.setStyle(Paint.Style.FILL);

    mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mGreenPaint.setColor(getResources().getColor(R.color.outgoing_event));
    mGreenPaint.setStyle(Paint.Style.FILL);

    mOffWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mOffWhitePaint.setColor(getResources().getColor(R.color.off_white_1));
    mOffWhitePaint.setStyle(Paint.Style.FILL);
  }

    public Parcelable onSaveInstanceState() {
        // save state - so if you rotate the screen it will do the right thing.
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        //bundle.putFloat("fraction", mfraction);

        return bundle;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            //mfraction = bundle.getFloat("fraction");
            super.onRestoreInstanceState(bundle.getParcelable("superState"));
        } else {
            super.onRestoreInstanceState(state);
        }
        //setFractionFloat(mfraction);
    }

    public void setCounts(int incomingCount, int outgoingCount){
        init();
        mIncomingCount = incomingCount;
        mOutgoingCount = outgoingCount;

        invalidate();
    }


   protected void onDraw(Canvas canvas) {

       Path path = new Path();
       Path path2 = new Path();


       int width = getWidth() - getPaddingLeft() - getPaddingRight();
       int height = getHeight() - getPaddingTop() - getPaddingBottom();

       cx = width/2;
       cy = height/2;

       float bar_height = RELATIVE_BAR_HEIGHT*height;
       float max_bar_length = width;
       float incoming_bar_length;
       float outgoing_bar_length;
       float vertical_bar_margines = (height - bar_height - bar_height)/3.0f;

       // set the text height to that of the bar
       mOffWhitePaint.setTextSize(bar_height);
       mBluePaint.setTextSize(bar_height);
       mGreenPaint.setTextSize(bar_height);

       // if both values are under 10, present 10 as the full scale
       if(mIncomingCount <=10  && mOutgoingCount <= 10 ){
           incoming_bar_length = mIncomingCount*max_bar_length/10;
           outgoing_bar_length = mOutgoingCount*max_bar_length/10;
       }else{

           // make the lesser bar a fraction of the larger maximizing the length of the larger
           if(mIncomingCount >= mOutgoingCount){
               incoming_bar_length = max_bar_length;
               outgoing_bar_length = mOutgoingCount*max_bar_length/mIncomingCount;
           }else {
               outgoing_bar_length = max_bar_length;
               incoming_bar_length = mIncomingCount*max_bar_length/mOutgoingCount;
           }
       }



       // draw the outgoing bar
       float outgoing_start_x = 0;
       float outgoing_end_x = outgoing_start_x + outgoing_bar_length;
       float outgoing_start_y = vertical_bar_margines;
       float outgoing_end_y = bar_height + outgoing_start_y;

       canvas.drawRect(outgoing_start_x, outgoing_start_y,
               outgoing_end_x, outgoing_end_y, mGreenPaint);


       // draw the incoming bar
       float incoming_start_x = 0;
       float incoming_end_x = incoming_start_x + incoming_bar_length;
       float incoming_start_y = outgoing_end_y + vertical_bar_margines;
       float incoming_end_y = bar_height + incoming_start_y;

       canvas.drawRect(incoming_start_x, incoming_start_y,
               incoming_end_x, incoming_end_y, mBluePaint);


       // if the flag is true, then display the corresponding numerical values
       if(display_values){


           // text height includes the small dangly bit that dips below the line, so...
           // raise the position of the characters by a small amount
           canvas.drawText(Integer.toString((int)mOutgoingCount),
                   outgoing_start_x+5, outgoing_end_y - bar_height*0.15f, mOffWhitePaint);

           canvas.drawText(Integer.toString((int)mIncomingCount),
                   incoming_start_x+5, incoming_end_y - bar_height*0.15f, mOffWhitePaint);
       }else {

           // if counts are zero, display a sad face
           if(mOutgoingCount == 0 ){
               path.setLastPoint(outgoing_start_x, outgoing_start_y);
               path.lineTo(outgoing_start_x, outgoing_end_y);
               mGreenPaint.setTextSkewX(-0.25f);



               canvas.drawTextOnPath(getResources().getText(R.string.text_sad_face).toString(),
                       path, 0, 0, mGreenPaint);

               //canvas.drawText(":-(", outgoing_start_x+5, outgoing_end_y - bar_height*0.15f, mOffWhitePaint);
           }
           if(mIncomingCount == 0){

               path2.setLastPoint(incoming_start_x, incoming_start_y);
               path2.lineTo(incoming_start_x, incoming_end_y);
               mBluePaint.setTextSkewX(-0.25f);


               canvas.drawTextOnPath(getResources().getText(R.string.text_sad_face).toString(),
                       path2, 0, 0, mBluePaint);


               //canvas.drawText(":-(",incoming_start_x+5, incoming_end_y - bar_height*0.15f, mOffWhitePaint);
           }
       }

   }


    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == event.ACTION_DOWN){ //Only execute on Finger down action


            float ScaleFrom = (float)0.0;
            float ScaleTo = (float)1.0;

            ScaleAnimation scaleAnimation = new ScaleAnimation(ScaleFrom, ScaleTo, ScaleFrom, ScaleTo, cx, cy);
            scaleAnimation.setDuration(150);
            scaleAnimation.setInterpolator(new AccelerateInterpolator(1.5f));
            this.startAnimation(scaleAnimation);

            display_values = !display_values;

            invalidate();

            Log.d(TAG, "Touch");

        }

        return true;
    }

}