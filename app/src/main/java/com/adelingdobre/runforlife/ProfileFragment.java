package com.adelingdobre.runforlife;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.os.Environment;

import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import facebook_utils.UserModel;
import utils.User;
import utils.UsersDB;
import utils.ValueFormatter;


public class ProfileFragment extends Fragment {

    public User user;
    public ImageView imageView;
    public TextView name;
    public TextView mail;
    public RadioGroup radioGroup;
    public RadioButton male;
    public RadioButton female;
    public Button saveButton;
    public Button cancelButton;
    public EditText age;
    public EditText weight;
    public EditText height;
    public EditText heartRate;
    public TextView calories;
    public Spinner activitySpinner;

    public DailyCalculator calculator;

    public String gender;
    public String ageInfo, weightInfo, heightInfo, heartRateInfo;

    ValueFormatter vf;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(UserModel userModel) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("username", userModel.userName);
        args.putString("useremail", userModel.userEmail);
        args.putString("userpass", userModel.password);
        args.putString("userpic", userModel.profilePic);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calculator = new DailyCalculator();
        vf = new ValueFormatter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final UsersDB db;
        User profile;
        db = MainActivity.usersDB;

        Bundle args = getArguments();
        user = new User();
        user.setUsername(args.getString("username"));
        user.setEmail(args.getString("useremail"));
        user.setPassword(args.getString("userpass"));
        user.setPicture(args.getString("userpic"));

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        name = (TextView)view.findViewById(R.id.name_info);
        mail = (TextView)view.findViewById(R.id.email_info);
        imageView = (ImageView) view.findViewById(R.id.imageProfile);
        radioGroup = (RadioGroup)view.findViewById(R.id.radioGender);

        age = (EditText)view.findViewById(R.id.age_info);
        weight = (EditText)view.findViewById(R.id.weight_info);
        height = (EditText)view.findViewById(R.id.height_info);
        heartRate = (EditText)view.findViewById(R.id.heartRate_info);
        saveButton = (Button)view.findViewById(R.id.save_button);
        cancelButton = (Button)view.findViewById(R.id.cancel_button);
        male = (RadioButton)view.findViewById(R.id.male_button);
        female = (RadioButton)view.findViewById(R.id.female_button);
        calories = (TextView)view.findViewById(R.id.calories_info);
        activitySpinner = (Spinner)view.findViewById(R.id.activitySpinner);

        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.activity_level, R.layout.spinner_item2);
        activityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        activitySpinner.setAdapter(activityAdapter);

        name.setText(user.getUsername());
        mail.setText(user.getEmail());
        if(user.getPassword() == null)
            imageView.setImageURI(Uri.parse(user.getPicture()));
        else
            imageView.setImageResource(R.drawable.user);

        if(db.emailExists(user.getEmail())){
            profile = db.getUserByEmail(user.getEmail());
//            Log.d("profile", "genul " + profile.getUsername());
            if(profile.getGender() != null){
                if(profile.getGender().compareTo("male") == 0)
                    ((RadioButton) radioGroup.findViewById(R.id.male_button)).setChecked(true);
                if(profile.getGender().compareTo("female") == 0)
                    ((RadioButton)radioGroup.findViewById(R.id.female_button)).setChecked(true);
            }
            if(profile.getAge() != null)
                age.setText(profile.getAge());
            if(profile.getWeight() != null)
                weight.setText(profile.getWeight());
            if(profile.getHeight() != null)
                height.setText(profile.getHeight());
            if(profile.getHeartRate() != null)
                heartRate.setText(profile.getHeartRate());
            if(profile.getCalories() != null)
                calories.setText(vf.formatCalories(Double.parseDouble(profile.getCalories())));
        }else{
            user.setTag("facebook");
            db.insertFullProfile(user);
        }

        saveButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                View genderbutton = radioGroup.findViewById(selectedId);
                int idx  = radioGroup.indexOfChild(genderbutton);
                if (idx == 0)
                    gender = "female";
                else
                    gender = "male";

                ageInfo = age.getText().toString();
                weightInfo = weight.getText().toString();
                heightInfo = height.getText().toString();
                heartRateInfo = heartRate.getText().toString();

                if(ageInfo.compareTo("") != 0 && gender.compareTo("") != 0 &&
                        weightInfo.compareTo("") != 0 && heightInfo.compareTo("") != 0)
                {
                    calculator.initParameters(gender, Double.parseDouble(ageInfo), Double.parseDouble(weightInfo),
                            Double.parseDouble(heightInfo), activitySpinner.getSelectedItemPosition());
                    calculator.setDailyCalories();
                } else {
                    calculator.daily_calories = 0;
                    toastIt("Complete your profile");
                }
                calories.setText(vf.formatCalories(calculator.daily_calories));

                db.updateInfo(user.getEmail(), gender, ageInfo, weightInfo, heightInfo, heartRateInfo,
                        Double.toString(calculator.daily_calories));
            }
        });

        return view;
    }

    private void toastIt(String message){
        Toast.makeText(getActivity(), message,
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onActivityCreated(Bundle savedStateInstance){
        super.onActivityCreated(savedStateInstance);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    public Bitmap getFbProfilePicture(String id) throws Exception {
        URL imageURL = new URL(id);
        Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
        return bitmap;
    }
}
