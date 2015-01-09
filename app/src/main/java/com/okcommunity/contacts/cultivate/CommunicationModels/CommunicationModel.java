package com.okcommunity.contacts.cultivate.CommunicationModels;

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
public class CommunicationModel {

    double mYo;  // current value
    double mDecayRate; // number of hours
    int eventIntervalLimit_hours;


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

        return 0;
    }

    // number of hours until the next event is due
    public double estimatedTriggerTime(double yEnd){

        // return the projection of the number of hours to decay to the finish point, yEnd
        return 0;
    }

    public void setEventIntervalLimit(int limit_hours){
        eventIntervalLimit_hours = limit_hours;
    }
}
