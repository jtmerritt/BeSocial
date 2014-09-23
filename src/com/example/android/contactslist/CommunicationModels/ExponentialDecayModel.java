package com.example.android.contactslist.CommunicationModels;

import java.lang.Math;

/**
 * Created by Tyson Macdonald on 9/20/2014.
 *
 *    This model assumes that relationships decay exponentially by the number of hours passed
 *
 *    y = y0(1-r)^t
 *
 *    y0 = initial value
 *    r = decay rate
 *    t = interval time in days
 *    y = result value
 */
public class ExponentialDecayModel {

    double mYo;  // current value
    double mDecayRate; // number of hours

    public ExponentialDecayModel(){

    }

    public void setInitialValues(double Yo, double decayRate){
        this.mYo = Yo;
        this.mDecayRate = decayRate; // number of hours
    }

    public void addToValue(double x){
        mYo += x;
    }

    public double getCurrentValue(){
        return mYo;
    }

    // method calculuates the result after some time interval,
    // and replaces the initial condition as setup for the next calculation
    public double calculateFofT(double hours){

        // do nothing if the interval is less than an hour
        if(hours >= 1){
            mYo = mYo*Math.pow((1-mDecayRate), hours);  // decrement value for time
        }
        return mYo;
    }
}
