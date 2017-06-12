package com.adelingdobre.runforlife;

import android.content.Intent;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Activity;
import android.widget.Toast;

import facebook_utils.UserModel;
import utils.Constants;
import utils.UsersDB;
import utils.ValidateUserInfo;
import utils.CheckNetwork;
import utils.User;

public class RegisterActivity extends Activity implements View.OnClickListener {

    EditText edit_name, edit_email, edit_password;
    TextView txt_alreadyHave;
    Button btn_registrar;
    View mProgressView;
    View mRegisterFormView;

    private CreateUserTask mCreateTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        String email;
        if (savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null){
                email = null;
            } else {
                email = extras.getString(Constants.TAG_EMAIL);
            }
        } else {
            email = savedInstanceState.getString(Constants.TAG_EMAIL);
        }

        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_email = (EditText) findViewById(R.id.edit_email);
        edit_email.setText(email);
        edit_password = (EditText) findViewById(R.id.edit_password);
        txt_alreadyHave = (TextView) findViewById(R.id.txt_already_have);
        txt_alreadyHave.setOnClickListener(this);

        mProgressView = findViewById(R.id.login_progress);
        mRegisterFormView = findViewById(R.id.login_form);

        btn_registrar = (Button) findViewById(R.id.btn_register);
        btn_registrar.setOnClickListener(this);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void toastIt(String message){
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();
    }

    public void attemptCreate() {
        // Store values at the time of the login attempt.
        String name = edit_name.getText().toString();
        String email = edit_email.getText().toString();
        String password = edit_password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        ValidateUserInfo validate = new ValidateUserInfo();

        // Check for a valid email address.
        if (TextUtils.isEmpty(name)) {
            edit_name.setError(getString(R.string.error_field_required));
            focusView = edit_name;
            cancel = true;
        } else if (TextUtils.isEmpty(email)) {
            edit_email.setError(getString(R.string.error_field_required));
            focusView = edit_email;
            cancel = true;
        } else if (!validate.isEmailValid(email)) {
            edit_email.setError(getString(R.string.error_invalid_email));
            focusView = edit_email;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            edit_password.setError(getString(R.string.error_field_required));
            focusView = edit_password;
            cancel = true;
        } else if (!validate.isPasswordValid(password)) {
            edit_password.setError(getString(R.string.error_invalid_password));
            focusView = edit_password;
            cancel = true;
        }

        if(cancel) {
            //There was an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user registration attempt.
            UsersDB db;
            User user;

            db = new UsersDB(this);
            if(db.userExists(name)){
                toastIt("Username " + name + " already exists!");
            }else if(db.emailExists(email)){
                toastIt("Email " + email + " already exists!");
            }else{
                user = new User(name, password, email);
                long insertID = db.insertUser(user);

                mProgressView.setVisibility(View.VISIBLE);
                mRegisterFormView.setVisibility(View.GONE);

                mCreateTask = new CreateUserTask(name, email, password);
                mCreateTask.execute((Void)null);
            }
        }
    }

    public void onClick(View view){
        switch(view.getId()) {
            case R.id.btn_register:
                attemptCreate();
                break;
            case R.id.txt_already_have:
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
                break;
        }
    }

    public class CreateUserTask extends AsyncTask<Void, Void, Boolean> {

        private final String mName;
        private final String mEmail;
        private final String mPassword;

        CreateUserTask(String name, String email, String password) {
            mName = name;
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            //toastIt("Successfully added " + mName);
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }
                return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCreateTask = null;
            mProgressView.setVisibility(View.GONE);

            if (success) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                UserModel userModel = new UserModel();
                userModel.userName = mName;
                userModel.userEmail = mEmail;
                userModel.password = mPassword;
                intent.putExtra(UserModel.class.getSimpleName(), userModel);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "App Error", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mCreateTask = null;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }
}
