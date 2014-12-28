package com.example.android.contactslist.CommunicationModels;

/**
 * Created by Tyson Macdonald on 9/20/2014.
 *
 *    This model assumes that relationships decay, compounded hourly
 *
 *    y = y0 - r*t
 *
 *    y0 = initial value
 *    r = decay rate
 *    t = interval time in hours
 *    y = result value
 */
public class LinearDecayModel extends CommunicationModel{
    // method calculuates the result after some time interval,
    // and replaces the initial condition as setup for the next calculation

    @Override
    public double calculateFofT(double hours){

        // do nothing if the interval is less than an hour
        if(hours >= 1){
            mYo = mYo - mDecayRate*hours;  // decrement value for time
        }

        // fix to a lower limit of 0
        if(mYo < 0){mYo = 0;}
        return mYo;
    }

    @Override
    public double estimatedTriggerTime(double yEnd){

        // return the projection of the number of hours to decay to the finish point, yEnd
        return (mYo - yEnd)/mDecayRate;
    }

}
