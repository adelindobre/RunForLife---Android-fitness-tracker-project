package com.adelingdobre.runforlife;

import android.util.Log;
import android.widget.Toast;

/**
 * Created by AdelinGDobre on 6/12/2017.
 */

public class RunningCalculator {

    public double weight; //kg
    public double height; //cm
    public double surface_grade;
    public double age;
    public double heartrate;
    public double treadmill;
    public double total_distance;
    public double total_time;
    public double total_calories_burned;
    public double current_distance;
    public double current_time;
    public double current_calories_burned;
    public double estimated_calories;
    public String gender;
    public GrossCalculator grossCalculator;

    public RunningCalculator(){
        grossCalculator = new GrossCalculator();
    }

    public void setInitialParameters(double age, double weight, double height, double heartrate, double treadmill,
                                     double surface_grade, double distance, double time, String gender){

        this.age = age;
        this.weight = weight;
        this.height = height;
        this.heartrate = heartrate;
        this.treadmill = treadmill;
        this.surface_grade = surface_grade;
        this.total_distance = distance;
        this.total_time = time;
        this.gender = gender;
        grossCalculator.setInitialParameters(this.gender, this.age, this.weight, this.height);
    }

    public void setCurrentParameters(double distance, double time){
        this.current_distance = distance / (double)1000;
        this.current_time = time / (double)1000 / (double)60;
    }

    public void reset(){
        this.age = 0;
        this.weight = 0;
        this.height = 0;
        this.heartrate = 0;
        this.treadmill = 0;
        this.surface_grade = 0;
        this.total_distance = 0;
        this.total_time = 0;
        this.total_calories_burned = 0;
        this.current_distance = 0;
        this.current_time = 0;
        this.current_calories_burned = 0;
        this.estimated_calories = 0;
        this.gender = "";
    }

    public double getCalories(double distance){
        double cb = (double)0;
        double wkg = this.weight;
        double g = this.surface_grade - (double)20;
        double drk = distance;
        double cff = 0;
        double tf = 0;

        double mhr = (double)208 - (0.7 * this.age);
        double rhr = this.heartrate * (double)3;
        double vo2max = 15.3 * (mhr/rhr);

        if(vo2max >= 56)
            cff = (double)1;
        if((vo2max >= 54) && (vo2max < 56))
            cff = 1.01;
        if((vo2max >= 52) && (vo2max < 54))
            cff = 1.02;
        if((vo2max >= 50) && (vo2max < 52))
            cff = 1.03;
        if((vo2max >= 48) && (vo2max < 50))
            cff = 1.04;
        if((vo2max >= 46) && (vo2max < 48))
            cff = 1.05;
        if((vo2max >= 44) && (vo2max < 46))
            cff = 1.06;
        if((vo2max < 44))
            cff = 1.07;

        if(this.treadmill == 0)
            tf = (double)0;
        if(this.treadmill == 1)
            tf = 0.84;

        if((g >= -20) && (g <= -15))
            cb = (((-0.01 * g) + 0.50) * wkg + tf) * drk * cff;
        if((g >= -15) && (g <= -10))
            cb = (((-0.02 * g) + 0.35) * wkg + tf) * drk * cff;
        if((g >= -10) && (g <= 0))
            cb = (((0.04 * g) + 0.95) * wkg + tf) * drk * cff;
        if((g >= 0) && (g <= 10))
            cb = (((0.05 * g) + 0.95) * wkg + tf) * drk * cff;
        if((g >= 10) && (g <= 15))
            cb = (((0.07 * g) + 0.75) * wkg + tf) * drk * cff;

        return cb;
    }

    public void setTotalCaloriesBurned(){
        double netCalories = getCalories(this.total_distance);
        grossCalculator.setCurrentParameters(this.total_time, netCalories);
        this.total_calories_burned = grossCalculator.getGrossCalories();
        //Log.d("test", "total net = " + netCalories + ", gross = " + total_calories_burned);
    }

    public void setCurrentCaloriesBurned(){
        double netCalories = getCalories(this.current_distance);
        grossCalculator.setCurrentParameters(this.current_time, netCalories);
        this.current_calories_burned = grossCalculator.getGrossCalories();
        //Log.d("test", " current net = " + netCalories + ", gross = " + current_calories_burned);
    }

    public void setEstimatedCalories(){
        this.estimated_calories = this.total_calories_burned - this.current_calories_burned;
    }


}
