package com.adelingdobre.runforlife;

/**
 * Created by AdelinGDobre on 6/12/2017.
 */

public class GrossCalculator {
    public double netCalories;
    public double duration;
    public String gender;
    public double age;
    public double weight;
    public double height;

    public GrossCalculator(){}

    public void setInitialParameters(String gender, double age, double weight, double height){
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.height = height;
    }

    public void setCurrentParameters(double duration, double netCalories){
        this.netCalories = netCalories;
        this.duration = duration;
    }
    public double getGrossCalories(){
        double gcb;
        double ncb = this.netCalories;
        double rmrcb;
        double bmr = 0;
        double ad = duration / (double)60;
        double wkg = this.weight;
        double hc = this.height;

        if(this.gender.compareTo("male") == 0)
            bmr = (13.75 * wkg) + ((double)5 * hc) - (6.76 * age) + 66;
        if(this.gender.compareTo("female") == 0)
            bmr = (9.56 * wkg) + (1.85 * hc) - (4.68 * age) + 655;

        rmrcb = ((bmr * 1.1) / (double)24) * ad;
        gcb = ncb + rmrcb;

        return gcb;
    }
}
