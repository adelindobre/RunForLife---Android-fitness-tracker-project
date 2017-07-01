package com.adelingdobre.runforlife;

/**
 * Created by AdelinGDobre on 7/1/2017.
 */

public class DailyCalculator {

    public String gender;
    public double age;
    public double weight; //kg
    public double height; //cm
    public double level; //spinner position
    public double daily_calories;

    public DailyCalculator(){}

    public void initParameters(String gender, double age, double weight, double height, double level){
        this.gender = gender;
        this.weight = weight;
        this.height = height;
        this.age  = age;
        this.level = level;
    }

    public double getDailyCalories(){
        double dce = 0, alf = 0, hc = this.height, wkg = this.weight, age = this.age;

        switch((int)level){
            case 0:
                alf = 1.2;
                break;
            case 1:
                alf = 1.375;
                break;
            case 2:
                alf = 1.55;
                break;
            case 3:
                alf = 1.725;
                break;
            case 4:
                alf = 1.9;
                break;
            default:
                alf = 1.2;
                break;
        }

        if(this.gender.compareTo("male") == 0)
            dce = alf * ((13.75 * wkg) + (5 * hc) - (6.76 * age) + 66);
        if(this.gender.compareTo("female") == 0)
            dce = alf * ((9.56 * wkg) + (1.85 * hc) - (4.68 * age) + 655);

        return dce;
    }

    public void setDailyCalories(){
        this.daily_calories = getDailyCalories();
    }
}
