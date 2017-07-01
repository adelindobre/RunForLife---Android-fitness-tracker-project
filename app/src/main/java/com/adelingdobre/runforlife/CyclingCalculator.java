package com.adelingdobre.runforlife;

/**
 * Created by AdelinGDobre on 6/30/2017.
 */

public class CyclingCalculator {
    public String gender;
    public double age;
    public double weight; //kg
    public double height; //cm
    public double total_distance; //km
    public double total_time; //minutes
    public double current_time; //minutes
    public double level;
    public double total_calories_burned;
    public double current_calories_burned;
    public double estimated_calories;

    public CyclingCalculator(){}

    public void setInitialParameters(String gender, double age, double weight, double height,
                                     double distance, double time, double level){
        this.gender = gender;
        this.age = age;
        this.weight = weight; //kg
        this.height = height; //cm
        this.total_time = time; //minutes
        this.total_distance = distance; //km
        this.level = level;
    }

    public void setCurrentParameters(double timeInterval){
        //timeInterval in miliseconds
        this.current_time = timeInterval / (double)1000 / (double)60; //minutes
    }

    public void reset(){
        this.gender = null;
        this.age = 0;
        this.weight = 0;
        this.height = 0;
        this.level = 0;
        this.total_time = 0;
        this.current_time = 0;
        this.total_calories_burned = 0;
        this.current_calories_burned = 0;
    }

    public double getCalories(double time){
        double cb;
        double bmr = 0;
        double met;
        double t = time / (double)60; //hours
        double wkg = this.weight;
        double hc = this.height;
        double age = this.age;

        switch((int)this.level){
            case 0:
                met = 4.0;
                break;
            case 1:
                met = 6.8;
                break;
            case 2:
                met = 8.0;
                break;
            case 3:
                met = 10.0;
                break;
            case 4:
                met = 12.0;
                break;
            case 5:
                met = 15.8;
                break;
            case 6:
                met = 8.5;
                break;
            default:
                met = 5.0;
                break;
        }
        if(this.gender.compareTo("male") == 0)
            bmr = (13.75 * wkg) + (5.0 * hc) - (6.76 * age) + 66.0;
        if(this.gender.compareTo("female") == 0)
            bmr = (9.56 * wkg) + (1.85 * hc) - (4.68 * age) + 655.0;

        cb = (bmr / 24.0) * met * t;

        return cb;
    }

    public void setCurrentCaloriesBurned(){
        this.current_calories_burned = getCalories(this.current_time);
    }

    public void setTotalCaloriesBurned(){
        this.total_calories_burned = getCalories(this.total_time);
    }

    public void setEstimatedCalories(){
        this.estimated_calories = this.total_calories_burned - this.current_calories_burned;
    }
}
