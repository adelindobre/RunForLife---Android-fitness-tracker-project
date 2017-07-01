package com.adelingdobre.runforlife;

import android.util.Log;
import android.widget.Toast;

/**
 * Created by AdelinGDobre on 6/12/2017.
 */

public class RunningCalculator {

    public double weight; //kg
    public double height; //cm
    public double surface_grade; //item pos
    public double age;
    public double heartrate;
    public double treadmill;
    public double total_distance; //km
    public double total_time; //min
    public double total_calories_burned;
    public double current_distance; // km
    public double current_time; //min
    public double current_surface_grade; //item pos
    public double current_calories_burned;
    public double estimated_calories;
    public String gender;
    public GrossCalculator grossCalculator;

    public RunningCalculator(){
        grossCalculator = new GrossCalculator();
    }

    public void setInitialParameters(double age, double weight, double height, double heartrate, double treadmill,
                                     double surface_grade, double distance, double time, String gender){
        //distance in km, time in min
        //weight in kg, height in cm
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

    public void setInitialParameters(double age, double weight, double height, double heartrate, double treadmill,
                                     double total_distance, double total_time, String gender){
        //distance in km, time in min
        //weight in kg, height in cm
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.heartrate = heartrate;
        this.treadmill = treadmill;
        this.gender = gender;
        this.total_distance = total_distance;
        this.total_time = total_time;
        grossCalculator.setInitialParameters(this.gender, this.age, this.weight, this.height);
    }

    public void setCurrentParameters(double distance, double time, double heightInterval){
        //distance in meters, time in miliseconds, heightInterval in meters;
        this.current_distance = distance / (double)1000;
        this.current_time = time / (double)1000 / (double)60;
        this.current_surface_grade = computeSlope(heightInterval, distance);
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

    public double getCalories(double distance, double level){
        //distance in km
        //level item pos
        double cb = (double)0;
        double wkg = this.weight;
        double g = level - (double)20;
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
    public double getGrossCalories(double netCalories, double time){
        //time in minutes
        grossCalculator.setCurrentParameters(time, netCalories);
        return grossCalculator.getGrossCalories();
    }

    public void setTotalCaloriesBurned(){
        //this.total_distance in km , this.total_time in min
        double netCalories = getCalories(this.total_distance, this.surface_grade);
        grossCalculator.setCurrentParameters(this.total_time, netCalories);
        this.total_calories_burned = grossCalculator.getGrossCalories();
        //Log.d("test", "total net = " + netCalories + ", gross = " + total_calories_burned);
    }

    public void setCurrentCaloriesBurned(){
        double netCalories = getCalories(this.current_distance, this.current_surface_grade);
        grossCalculator.setCurrentParameters(this.current_time, netCalories);
        this.current_calories_burned = grossCalculator.getGrossCalories();
        //Log.d("test", " current net = " + netCalories + ", gross = " + current_calories_burned);
    }

    public void setEstimatedCalories(){
        this.estimated_calories = this.total_calories_burned - this.current_calories_burned;
    }

    public double computeSlope(double heightInterval, double distance){
        double slope = ((heightInterval / distance)) * (double)100;
        if(slope <= (double)-20)
            return 0;
        if((slope > (double)-20) && (slope <= (double)-19))
            return 1;
        if((slope > (double)-19) && (slope <= (double)-18))
            return 2;
        if((slope > (double)-18) && (slope <= (double)-17))
            return 3;
        if((slope > (double)-17) && (slope <= (double)-16))
            return 4;
        if((slope > (double)-16) && (slope <= (double)-15))
            return 5;
        if((slope > (double)-15) && (slope <= (double)-14))
            return 6;
        if((slope > (double)-14) && (slope <= (double)-13))
            return 7;
        if((slope > (double)-13) && (slope <= (double)-12))
            return 8;
        if((slope > (double)-12) && (slope <= (double)-11))
            return 9;
        if((slope > (double)-11) && (slope <= (double)-10))
            return 10;
        if((slope > (double)-10) && (slope <= (double)-9))
            return 11;
        if((slope > (double)-9) && (slope <= (double)-8))
            return 12;
        if((slope > (double)-8) && (slope <= (double)-7))
            return 13;
        if((slope > (double)-7) && (slope <= (double)-6))
            return 14;
        if((slope > (double)-6) && (slope <= (double)-5))
            return 15;
        if((slope > (double)-5) && (slope <= (double)-4))
            return 16;
        if((slope > (double)-4) && (slope <= (double)-3))
            return 17;
        if((slope > (double)-3) && (slope <= (double)-2))
            return 18;
        if((slope > (double)-2) && (slope <= (double)-1))
            return 19;
        if((slope > (double)-1) && (slope <= (double)0))
            return 20;
        if((slope > (double)0) && (slope <= (double)1))
            return 21;
        if((slope > (double)1) && (slope <= (double)2))
            return 22;
        if((slope > (double)2) && (slope <= (double)3))
            return 23;
        if((slope > (double)3) && (slope <= (double)4))
            return 24;
        if((slope > (double)4) && (slope <= (double)5))
            return 25;
        if((slope > (double)5) && (slope <= (double)6))
            return 26;
        if((slope > (double)6) && (slope <= (double)7))
            return 27;
        if((slope > (double)7) && (slope <= (double)8))
            return 28;
        if((slope > (double)8) && (slope <= (double)9))
            return 29;
        if((slope > (double)9) && (slope <= (double)10))
            return 30;
        if((slope > (double)10) && (slope <= (double)11))
            return 31;
        if((slope > (double)11) && (slope <= (double)12))
            return 32;
        if((slope > (double)12) && (slope <= (double)13))
            return 33;
        if((slope > (double)13) && (slope <= (double)14))
            return 34;
        if((slope > (double)14) && (slope <= (double)15))
            return 35;
        if(slope >= 15)
            return 35;

        return 5;
    }


}
