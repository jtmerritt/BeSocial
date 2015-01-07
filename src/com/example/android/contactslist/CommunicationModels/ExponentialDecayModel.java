package com.example.android.contactslist.CommunicationModels;

import java.lang.Math;

/**
 * Created by Tyson Macdonald on 9/20/2014.
 *
 *    This model assumes that relationships decay exponentially, compounded hourly
 *
 *    y = y0e^(-kt/10)
 *
 *    y0 = initial value
 *    k = decay constant
 *    t = interval time in hours
 *    y = result value
 */
public class ExponentialDecayModel extends CommunicationModel{

    private final double MULTIPLIER = 10;

    // method calculates the result after some time interval,
    // and replaces the initial condition as setup for the next calculation
    @Override
    public double calculateFofT(double hours){

        // do nothing if the interval is less than an hour
        if(hours >= 1){
            mYo = mYo*Math.pow(Math.E, -mDecayRate*hours/MULTIPLIER);  // decrement value for time
        }
        return mYo;
    }

    @Override
    public double estimatedTriggerTime(double yEnd){

        // return the projection of the number of hours to decay to the finish point, yEnd
        return MULTIPLIER*Math.log(yEnd/mYo)/-mDecayRate;
    }

}
