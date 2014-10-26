package com.example.android.contactslist.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.graphics.Path;
import java.util.ArrayList;
import java.util.Map;
import android.graphics.Rect;

import com.example.android.contactslist.R;

public class WordCloudView extends View {

    private String TAG = "WordCloudView";
    private Paint mRedPaint;
    //private RectF mSectorOval;
    private Paint mBackgroundPaint;

    private int cx = 0;
    private int cy = 0;
    private boolean mLargeCanvas = false;
    private Context mContext;
    private ArrayList<Word> wordList;
    private Word word;
    private int largest_number_of_uses = 0;
    final private static int MAX_TEXT_SIZE = 150;
    final private static int SMALLEST_TEXT_SIZE = 25;
    private int largest_text_size;
    private int size;
    private int width;
    private int height;
    final private static int NUM_WORDS_DISPLAY = 15;
    final private static int MAX_PLACEMENT_ITERATIONS = 150;
    private int primary_display = 0;





    public WordCloudView(Context context) {
    super(context);
      // build from code
    init();
  }

  public WordCloudView(Context context, AttributeSet attrs) {
    super(context, attrs);
      // inflate from xml
      // other talk does this one
      // http://is.gd/AndroidCustomComp
    init();
  }

  public WordCloudView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
      // xml with style
    init();
  }

  private void init() {

    wordList = new ArrayList<Word>();

    mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mBackgroundPaint.setColor(Color.parseColor("#F5F5F5"));
    mBackgroundPaint.setStyle(Paint.Style.FILL);
    //setBackgroundColor(Color.TRANSPARENT);

    mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mRedPaint.setColor(getResources().getColor(android.R.color.holo_red_dark));
    mRedPaint.setStyle(Paint.Style.FILL);

    //mSectorOval = new RectF();
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
    }


    public void setWordList(ArrayList<Map.Entry<String,Integer>> word_list){

        int[] colors = {R.color.pasty_1, R.color.pasty_2, R.color.pasty_3,
                R.color.pasty_4, R.color.pasty_5};

        width = getWidth();
        height = getHeight();
        size = Math.min(width, height);
        cx = width / 2;
        cy = height / 2;

        largest_text_size = height/4;

        largest_text_size = (largest_text_size < MAX_TEXT_SIZE ? largest_text_size : MAX_TEXT_SIZE);

        int i = 0;

        for(Map.Entry<String,Integer> entry:word_list){
            Log.d(TAG, "Setting word");

            setWord(entry.getKey(), entry.getValue(), colors[i%5]);

            i++;
            // only list 10 words for now
            if(i >= NUM_WORDS_DISPLAY){
                break;
            }
        }
        invalidate();

    }


    private void setWord(String string, int number_of_uses, int color) {

        // we should already be in sorted descending order
        if(number_of_uses > largest_number_of_uses){
            largest_number_of_uses = number_of_uses;
        }

        int string_length = string.toCharArray().length;
        int i = 0;

        word = new Word();
        word.box = new Rect();
        word.path = new Path();

        word.color = getResources().getColor(color);

        word.string = string;
        word.number_of_uses = number_of_uses;

        // scale the text size
        word.textSize = (int)((float)(largest_text_size - SMALLEST_TEXT_SIZE)*(float)number_of_uses/
                        (float)largest_number_of_uses) + SMALLEST_TEXT_SIZE;

        int word_graphic_length = (int)((float)string_length*(float)word.textSize*0.6f);

        word.xLocation = (int)(Math.random()*(width));
        word.yLocation = (int)(Math.random()*(height));

        do {

            word.xLocation = (int)((float)word_graphic_length/(float)8) + word.xLocation;
            word.yLocation = (int)((float)word.textSize/(float)4) + word.yLocation;

            if(word.xLocation < 0){
                word.xLocation = width - word_graphic_length;
            }
            if(word.xLocation + word_graphic_length > width){
                word.xLocation = 0;
            }
            if(word.yLocation < 0){
                word.yLocation = height - word.textSize;
            }
            if(word.yLocation + word.textSize > height){
                word.yLocation = 0;
            }

            //word.xLocation = (int)(((double)i*(double)word.textSize/2)*Math.cos((double)i/(double)8)) + cx;
            //word.yLocation = (int)(((double)i*(double)word.textSize/2)*Math.sin((double)i/(double)8)) + cy;

            word.box.set(word.xLocation,
                    word.yLocation,
                    word.xLocation+ word_graphic_length,
                    word.yLocation + word.textSize);

            //word.box.sort();


            // define first point in the path
            //word.path.moveTo(word.xLocation, word.yLocation);

            // define last point in the path, with the word having translated along the x axis
            //word.path.lineTo(word.xLocation + word_graphic_length, word.yLocation);

            i++;

        }while((i < MAX_PLACEMENT_ITERATIONS) && (collidesWithWordList(word)));

        Log.d(TAG, "Word moves: " + Integer.toString(i));

        wordList.add(word);
    }

    private boolean outOfBounds(Word word) {

        if(word.box.right > width ||
                word.box.left < 0 ||
                word.box.top < 0 ||
                word.box.bottom > height){
            return true;
        }


        return false;
    }

    private boolean collidesWithWordList(Word word) {
        boolean collision = false;


        // if the list is empty, then there can be no collision
        if(wordList.isEmpty()){
            return false;
        }

        for(Word entry: wordList){
            if(entry.box.intersect(word.box)){
                collision = true;
                return collision;
            }
        }

        return collision;
    }


    protected void onDraw(Canvas canvas) {

       Log.d(TAG, "On Draw");
        int i, y_pos;

       setPadding(7, 7, 7, 7);

        //canvas.drawLine(0,0,width, 0,mRedPaint);
        //canvas.drawLine(0,height-5,width, height-5,mRedPaint);


        switch(primary_display%2){
            case 0:
            default:
                for(Word entry:wordList){
                    //canvas.drawRect(entry.box, mRedPaint);

                    // set the display text
                    //canvas.drawTextOnPath(entry.string, entry.getPath(),
                    //       0, entry.getTextHeight(), entry.getPaint());

                    canvas.drawText(entry.string,
                            entry.xLocation + (int)(word.textSize/10),
                            entry.yLocation + (int)(entry.textSize*0.9),
                            entry.getPaint());
                }
                break;
            case 1:
                y_pos = 10;
                for(i=0; i< wordList.size();i++){

                    if(i%2 == 0){
                        canvas.drawText(wordList.get(i).string,
                                cx + 20
                                        - (int)((wordList.get(i).string.toCharArray().length)
                                        *wordList.get(i).textSize*0.25f),
                                y_pos + (int)(wordList.get(i).textSize*0.9),
                                wordList.get(i).getPaint());
                        y_pos = y_pos + (int)(wordList.get(i).textSize*0.7);

                    }else {

                        canvas.drawText(wordList.get(i).string + " " + wordList.get(i+1).string,
                                cx + 20
                                        - (int)((wordList.get(i).string.toCharArray().length +
                                        wordList.get(i+1).string.toCharArray().length + 1)
                                        *wordList.get(i).textSize*0.25f),
                                y_pos + (int)(wordList.get(i).textSize*0.9),
                                wordList.get(i).getPaint());

                        y_pos = y_pos + (int)(wordList.get(i).textSize*0.7);
                        i++;
                    }


                }
                break;

        }
        primary_display++;

    }

    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == event.ACTION_DOWN){ //Only execute on Finger down action


            float ScaleFrom = (float)0.0;
            float ScaleTo = (float)1.0;

            ScaleAnimation scaleAnimation = new ScaleAnimation(ScaleFrom, ScaleTo, ScaleFrom, ScaleTo, cx, cy);
            scaleAnimation.setDuration(100);
            scaleAnimation.setInterpolator(new AccelerateInterpolator(1.5f));
            this.startAnimation(scaleAnimation);

            invalidate();

        }

        return true;
    }

    public class Word {
        public String string = "";
        public int number_of_uses = 0;
        public int yLocation = 0;
        public int xLocation = 0;
        public int color = Color.LTGRAY;
        public int textSize = 100;
        public Paint paint = null;
        public Path path = null;
        public Rect box = null;

        public Paint getPaint(){
            paint = new Paint();
            paint.setTextSize(textSize);
            paint.setTypeface(Typeface.SANS_SERIF);
            paint.setColor(color);
            paint.setAntiAlias(true);
            //paint.setAlpha(1);

            return paint;
        }

        public Path getPath(){
            return path;
        }

        public float getTextHeight(){
            return 0;//textSize;
        }
    }

}