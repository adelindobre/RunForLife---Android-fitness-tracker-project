package com.adelingdobre.runforlife;

import android.util.Log;
import android.widget.Toast;

/**
 * Created by AdelinGDobre on 6/11/2017.
 */

public class WalkingCalculator {

    public double weight; //kg
    public double surface_grade;  //item position
    public double total_distance; //meters
    public double total_time; //minutes
    public double total_calories_burned;
    public double current_calories_burned;
    public double current_distance; //meters
    public double current_time; //minutes
    public double current_surface_grade; //item position
    public double estimated_calories;
    public double current_speed;

    public WalkingCalculator(){}

    public void setInitialParameters(double weight){
        this.weight = weight;
    }
    public void setInitialParameters(double weight, double total_distance, double total_time, double surface_grade){
        //weight in kg, total_distance in km, total_time in minutes, surface_grade - item selected position
        this.weight = weight;
        this.total_distance = total_distance * (double)1000;
        this.total_time = total_time;
        this.surface_grade = surface_grade;
    }

    public void setCurrentParameters(double distance, double timeInterval, double velocity, double heightInterval){
        //distance in meters, timeInterval in miliseconds, heightInterval in meters
        this.current_distance = distance;
        this.current_time = timeInterval / (double)1000 / (double)60;
        this.current_speed = velocity;
        this.current_surface_grade = computeSlope(heightInterval, distance);
        //Log.d("vars", this.current_time + " " + timeInterval + " " + this.current_speed);
    }

    public void reset(){
        this.weight = 0;
        this.surface_grade = 0;
        this.total_distance = 0;
        this.total_time = 0;
        this.total_calories_burned = 0;
        this.current_calories_burned = 0;
        this.current_distance = 0;
        this.current_time = 0;
        this.estimated_calories = 0;
        this.current_speed = 0;
    }

    public double getCalories(double distance, double time, double level){
        //distance in meters
        //time in minutes
        //weight in kg
        //level - item position
        double cb = 0;
        double km  = distance / (double)1000;
        double t = time / (double)60;
        double kph = km / t;
        double wkg = this.weight;

        switch((int)level) {
            case 0:
                cb = (0.0251 * Math.pow(kph, 3) - 0.2157 * Math.pow(kph, 2) + 0.7888 * kph + 1.2957) * wkg * t;
                break;
            case 1:
                cb = (0.0244 * Math.pow(kph, 3) - 0.2079 * Math.pow(kph, 2) + 0.8053 * kph + 1.3281) * wkg * t;
                break;
            case 2:
                cb = (0.0237 * Math.pow(kph, 3) - 0.2000 * Math.pow(kph, 2) + 0.8217 * kph + 1.3605) * wkg * t;
                break;
            case 3:
                cb = (0.0230 * Math.pow(kph, 3) - 0.1922 * Math.pow(kph, 2) + 0.8382 * kph + 1.3929) * wkg * t;
                break;
            case 4:
                cb = (0.0222 * Math.pow(kph, 3) - 0.1844 * Math.pow(kph, 2) + 0.8546 * kph + 1.4253) * wkg * t;
                break;
            case 5:
                cb = (0.0215 * Math.pow(kph, 3) - 0.1765 * Math.pow(kph, 2) + 0.8710 * kph + 1.4577) * wkg * t;
                break;
            case 6:
                cb = (0.0171 * Math.pow(kph, 3) - 0.1062 * Math.pow(kph, 2) + 0.6080 * kph + 1.8600) * wkg * t;
                break;
            case 7:
                cb = (0.0184 * Math.pow(kph, 3) - 0.1134 * Math.pow(kph, 2) + 0.6566 * kph + 1.9200) * wkg * t;
                break;
            case 8:
                cb = (0.0196 * Math.pow(kph, 3) - 0.1205 * Math.pow(kph, 2) + 0.7053 * kph + 1.9800) * wkg * t;
                break;
            case 9:
                cb = (0.0208 * Math.pow(kph, 3) - 0.1277 * Math.pow(kph, 2) + 0.7539 * kph + 2.0400) * wkg * t;
                break;
            case 10:
                cb = (0.0221 * Math.pow(kph, 3) - 0.1349 * Math.pow(kph, 2) + 0.8025 * kph + 2.1000) * wkg * t;
                break;
            default:
                cb = 0;
                break;
        }

        if (cb != (double)0)
            return cb;

        double mpm = distance / time;
        double fg = 0;
        switch((int)level){
            case 11:
                fg = 0.06;
                break;
            case 12:
                fg = 0.07;
                break;
            case 13:
                fg = 0.08;
                break;
            case 14:
                fg = 0.09;
                break;
            case 15:
                fg = 0.10;
                break;
            case 16:
                fg = 0.11;
                break;
            case 17:
                fg = 0.12;
                break;
            case 18:
                fg = 0.13;
                break;
            case 19:
                fg = 0.14;
                break;
            case 20:
                fg = 0.15;
                break;
        }
        cb = (0.1 * mpm + 1.8 * mpm * fg + 3.5) * wkg * t * (double)60 * (double)5 / (double)1000;
        return cb;
    }

    public void setTotalCaloriesBurned(){
        this.total_calories_burned = getCalories(this.total_distance, this.total_time, this.surface_grade);
    }

    public void setCurrentCaloriesBurned(){
        this.current_calories_burned = getCalories(this.current_distance, this.current_time, this.current_surface_grade);
    }

    public void setEstimatedCalories(){
        this.estimated_calories = this.total_calories_burned - this.current_calories_burned;
    }

    public double computeSlope(double heightInterval, double distance){
        double slope = ((heightInterval / distance)) * (double)100;
        if(slope <= (double)-5)
            return 0;
        if((slope > (double)-5) && (slope <= (double)-4))
            return 1;
        if((slope > (double)-4) && (slope <= (double)-3))
            return 2;
        if((slope > (double)-3) && (slope <= (double)-2))
            return 3;
        if((slope > (double)-2) && (slope <= (double)-1))
            return 4;
        if((slope > (double)-1) && (slope <= (double)0))
            return 5;
        if((slope > (double)0) && (slope <= (double)1))
            return 6;
        if((slope > (double)1) && (slope <= (double)2))
            return 7;
        if((slope > (double)2) && (slope <= (double)3))
            return 8;
        if((slope > (double)3) && (slope <= (double)4))
            return 9;
        if((slope > (double)4) && (slope <= (double)5))
            return 10;
        if((slope > (double)5) && (slope <= (double)6))
            return 11;
        if((slope > (double)6) && (slope <= (double)7))
            return 12;
        if((slope > (double)7) && (slope <= (double)8))
            return 13;
        if((slope > (double)8) && (slope <= (double)9))
            return 14;
        if((slope > (double)9) && (slope <= (double)10))
            return 15;
        if((slope > (double)10) && (slope <= (double)11))
            return 16;
        if((slope > (double)11) && (slope <= (double)12))
            return 17;
        if((slope > (double)12) && (slope <= (double)13))
            return 18;
        if((slope > (double)13) && (slope <= (double)14))
            return 19;
        if((slope > (double)14) && (slope <= (double)15))
            return 20;
        if(slope >= 15)
            return 20;

        return 5;
    }
}
