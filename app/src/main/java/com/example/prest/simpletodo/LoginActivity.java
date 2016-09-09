package com.example.prest.simpletodo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private Toolbar loginToolbar;
    private EditText et_username, et_password, et_email;
    private TextView txt_login_error_message;
    private String username, password, email;
    private Button btn_submit_login_credentials, btn_cancel_register_mode, btn_start_register;
    private View blank_view_button_gap;
    private LOGIN_ACTIVITY_MODE login_activity_mode;
    private LinearLayout lin_layout_email_group;
    private final String applicationID = "253uxoJvouZhIYDeUaa5LRnJSbILCJGejWXKrT9B";
    private final String clientID = "9CujVxzGDAHapdvioaB2A1pQr1iH3rIU0MRqqvSi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_activity_mode = LOGIN_ACTIVITY_MODE.SIGN_IN_MODE;


        try{
            ParseObject.registerSubclass(note.class);
            Parse.initialize(this, applicationID, clientID);
            ParseAnalytics.trackAppOpened(getIntent());
        }catch (RuntimeException r) {
        }

        loginToolbar = (Toolbar) findViewById(R.id.act_main_toolbar);
        et_password = (EditText) findViewById(R.id.et_password);
        et_username = (EditText) findViewById(R.id.et_username);
        et_email = (EditText) findViewById(R.id.et_email);
        txt_login_error_message = (TextView) findViewById(R.id.txt_login_error_message);
        btn_submit_login_credentials = (Button) findViewById(R.id.btn_submit_login_credentials);
        btn_cancel_register_mode = (Button) findViewById(R.id.btn_cancel_register_mode);
        btn_start_register = (Button) findViewById(R.id.btn_start_register);
        blank_view_button_gap = (View) findViewById(R.id.blank_view_button_gap);
        lin_layout_email_group = (LinearLayout) findViewById(R.id.lin_layout_email_group);

        //hide irrelevant views
        txt_login_error_message.setVisibility(View.INVISIBLE);
        btn_cancel_register_mode.setVisibility(View.GONE);
        blank_view_button_gap.setVisibility(View.GONE);
        lin_layout_email_group.setVisibility(View.GONE);

        this.setSupportActionBar(loginToolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_main);
        getSupportActionBar().setTitle("My Notes");
    }

    //"Submit" button onClick()
    public void onSubmitLoginInfo(View view) {

        //DISABLE THE SUBMIT BUTTON TO PREVENT USER FROM MULTIPLE LOGINS/REGISTRATIONS
        btn_submit_login_credentials.setEnabled(false);

        //pull the values entered
        username = et_username.getText().toString().trim();
        password = et_password.getText().toString().trim();
        email = et_email.getText().toString().trim();

        //input validation
        if (username.isEmpty()) {
            txt_login_error_message.setText("Oops! You forgot to enter your username.");
            txt_login_error_message.setVisibility(View.VISIBLE);
            btn_submit_login_credentials.setEnabled(true);
        } else if (password.isEmpty()) {
            txt_login_error_message.setText("Oops! You forgot to enter your password.");
            txt_login_error_message.setVisibility(View.VISIBLE);
            btn_submit_login_credentials.setEnabled(true);
        } else {
            if (login_activity_mode.equals(LOGIN_ACTIVITY_MODE.SIGN_IN_MODE)) {
                // TODO: 9/5/2016 call the onSignIn() method, get result codes from both of them and then close the activity and go to MainActivity()
                this.onSignInAttempt(this.username, this.password);

            } else if (login_activity_mode.equals(LOGIN_ACTIVITY_MODE.SIGN_UP_MODE)) {
                if (username.length() < 6) {
                    txt_login_error_message.setText("Username must be more than 5 characters!");
                } else if (username.length() > 20) {
                    txt_login_error_message.setText("Username must be less than 20 characters!");
                } else if (password.length() < 6) {
                    txt_login_error_message.setText("Password must be more than 5 characters!");
                } else if (password.length() > 40) {
                    txt_login_error_message.setText("Password must be less than 40 characters!");
                } else {
                    this.getUserPasswordReEntry();
                }
            }
        }


    }

    private void getUserPasswordReEntry() {
        //pop up dialog to get user's re-entered password
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Re-Enter Password:");
        builder.setIcon(R.mipmap.ic_secure_account);

        //edit text for the password entry
        final EditText et_password_re_enter = new EditText(getApplicationContext());

        //set dialog to builder
        final AlertDialog alert = builder.create();

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input validation
                if (et_password_re_enter.getText().toString().trim().isEmpty()) {
                    Toast.makeText(LoginActivity.this.getBaseContext(), "Your passwords do not match!", Toast.LENGTH_LONG).show();
                } else {
                    //call sign up method
                    onSignUpAttempt(username, password, email);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alert.dismiss();
            }
        });

        //show dialog
        alert.show();
    }

    private void onSignInAttempt(String username, String password) {
        //input validation before sending it off
        if (this.checkUserInput(username, password) == false) {
            txt_login_error_message.setVisibility(View.VISIBLE);
            //nothing here, error message sent in the checkUserInput() method
        } else {
            //// TODO: 9/5/2016 login logic here
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            i.putExtra("LOGIN_RESULT", "ACCESS_GRANTED");
            //result_ox even when it's not ok lol
            startActivity(i, new Bundle());
            this.finish();
        }
    }

    private void onSignUpAttempt(String username, String password, String email) {
        if (this.checkUserInput(username, password, email) == false) {
            txt_login_error_message.setVisibility(View.VISIBLE);
            //nothing here, error message sent in the checkUserInput() method
            btn_submit_login_credentials.setEnabled(true);
        } else if (this.checkUserInput(username, password, email) == true) {
            //passes input validation checks, attempt to register here
            ParseUser user = new ParseUser();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);

            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        i.putExtra("LOGIN_RESULT", "ACCESS_GRANTED");
                        startActivity(i, new Bundle());
                        LoginActivity.this.finish();
                    } else {
                        //enable the login button again
                        btn_submit_login_credentials.setEnabled(true);
                        //get the result code for the sign up attempt
                        int error_code = e.getCode();
                        switch (error_code) {
                            case (ParseException.EMAIL_TAKEN):
                                txt_login_error_message.setText("Sorry, that email is already registered!");
                                break;
                            case (ParseException.USERNAME_TAKEN):
                                txt_login_error_message.setText("Sorry, that username is already taken!");
                                break;
                            case (ParseException.CONNECTION_FAILED):
                                txt_login_error_message.setText("Sorry, there was an error!\nCheck your internet connection and try again.");
                                break;
                            default:
                                txt_login_error_message.setText("Something went wrong!\nPlease try again");
                        }
                    }
                }
            });
        }
    }

    public void startSignUpMode(View view) {
        //change submit button text
        btn_submit_login_credentials.setText("Sign Up");
        //change register button text
        btn_start_register.setText("Already have an account? Sign in!");

        //show all relevant buttons
        btn_cancel_register_mode.setVisibility(View.VISIBLE);
        blank_view_button_gap.setVisibility(View.GONE);
        txt_login_error_message.setVisibility(View.INVISIBLE);
        lin_layout_email_group.setVisibility(View.VISIBLE);
    }

    public void startSignInMode(View view) {

        //change submit button text
        btn_submit_login_credentials.setText("Sign In");
        //change register button text
        btn_start_register.setText("Don't have an account? Register!");

        //hide irrelevant views here
        btn_cancel_register_mode.setVisibility(View.GONE);
        blank_view_button_gap.setVisibility(View.GONE);
        txt_login_error_message.setVisibility(View.INVISIBLE);
        lin_layout_email_group.setVisibility(View.INVISIBLE);
    }

    private boolean checkUserInput(String username, String password, String email){
        InputValidatorHelper validator = new InputValidatorHelper();

        if(validator.isNullOrEmpty(username)) {
            txt_login_error_message.setText("Enter a username!");
            return true;
        } else if (validator.isNullOrEmpty(password)) {
            txt_login_error_message.setText("Enter a password!");
            return true;
        } else if (validator.isValidPassword(password, false)) {
            txt_login_error_message.setText("Password invalid!");
            return true;
        } else {
            return true;
        }
    }

    private boolean checkUserInput(String username, String password) {
        // TODO: 9/5/2016 fix this bullshit
        InputValidatorHelper validator = new InputValidatorHelper();

        if(validator.isNullOrEmpty(username)) {
            txt_login_error_message.setText("Enter a username!");
            btn_submit_login_credentials.setEnabled(true);
            return true;
        } else if (validator.isNullOrEmpty(password)) {
            txt_login_error_message.setText("Enter a password!");
            btn_submit_login_credentials.setEnabled(true);
            return true;
        } else if (validator.isValidPassword(password, false)) {
            txt_login_error_message.setText("Password invalid!");
            btn_submit_login_credentials.setEnabled(true);
            return true;
        } else {
            return true;
        }
    }

    private class InputValidatorHelper {

        public boolean isValidEmail(String string) {
            final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
            Pattern pattern = Pattern.compile(EMAIL_PATTERN);
            Matcher matcher = pattern.matcher(string);
            return matcher.matches();
        }

        public boolean isValidPassword(String string, boolean allowSpecialChars) {
            String PATTERN;
            if (allowSpecialChars) {
                //PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";
                PATTERN = "^[a-zA-Z@#$%]\\w{5,19}$";
            } else {
                //PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20})";
                PATTERN = "^[a-zA-Z]\\w{5,19}$";
            }


            Pattern pattern = Pattern.compile(PATTERN);
            Matcher matcher = pattern.matcher(string);
            return matcher.matches();
        }

        public boolean isNullOrEmpty(String string) {
            return TextUtils.isEmpty(string);
        }

        public boolean isNumeric(String string) {
            return TextUtils.isDigitsOnly(string);
        }
    }
}
