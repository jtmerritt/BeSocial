package com.okcommunity.contacts.cultivate.CommunicationModels;

/**
 * Created by Tyson Macdonald on 12/24/2014.
 *
 *    This model assumes that relationships decay linearly with the number of days since last contact
 *
 *    y = y0 - t
 *
 *    y0 = the contact's event interval limit expressed in hours
 *    r = decay rate. hours/hour = 1 -- number of hours decayed perhour interval
 *    t = interval time in days
 *    y = result value
 */
public class CountdownModel extends CommunicationModel{

    @Override
    public void setInitialValues(double Yo, double decayRate){
        this.mYo = Yo;
        this.mDecayRate = 1; // hour for hour
    }

    @Override
    public void addToValue(double x){
        mYo = eventIntervalLimit_hours;
    }  // reset to the limit


    // method calculuates the result after some time interval,
    // and replaces the initial condition as setup for the next calculation
    @Override
    public double calculateFofT(double hours){

        // do nothing if the interval is less than an hour
        if(hours >= 1){
            mYo = mYo - hours;  // decrement value for time
        }

        // fix to a lower limit of 0
        if(mYo < 0){mYo = 0;}
        return mYo;
    }

    // returns the number of hours
    @Override
    public double estimatedTriggerTime(double yEnd){

        // sanitize the input
        if(yEnd < 0){
            yEnd = 0;
        }

        // return the projection of the number of hours to decay to the finish point, yEnd
        return (mYo - yEnd);
    }
}
