package utils;

/**
 * Created by AdelinGDobre on 5/6/2017.
 */

public class User {
    private String username;
    private String email;
    private String password;
    private String picture;
    private String gender;
    private String weight;
    private String height;
    private String heartRate;
    private String age;
    private String tag;

    public User(){
    }
    public User(String aUser, String aPass, String amail, String picture, String gender,
                String weight, String height, String heartRate, String age, String tag){
        this.username = aUser;
        this.password = aPass;
        this.email = amail;
        this.picture = picture;
        this.gender = gender;
        this.weight = weight;
        this.height = height;
        this.heartRate = heartRate;
        this.age = age;
        this.tag = tag;
    }

    public User(String aUser, String aPass, String amail){
        this.username = aUser;
        this.password = aPass;
        this.email = amail;
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getEmail(){
        return email;
    }
    public String getGender(){ return gender; }
    public String getPicture(){ return picture; }
    public String getTag() { return tag; }
    public String getAge(){ return age; }
    public String getWeight(){ return weight; }
    public String getHeight(){ return height; }
    public String getHeartRate(){ return heartRate; }


    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public void setPicture(String picture) { this.picture = picture; }
    public void setGender(String gender){
        this.gender = gender;
    }
    public void setWeight(String weight){
        this.weight = weight;
    }
    public void setHeight(String height){ this.height = height; }
    public void setHeartRate(String heartRate){
        this.heartRate = heartRate;
    }
    public void setAge(String age){
        this.age = age;
    }
    public void setTag(String tag) { this.tag = tag; }
}
