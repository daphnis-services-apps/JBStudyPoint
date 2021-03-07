package com.jb.study.point.authentication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.jb.study.point.MainActivity;
import com.jb.study.point.R;
import com.jb.study.point.helper.SessionManager;

import net.khirr.android.privacypolicy.PrivacyPolicyDialog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RegisterActivity extends AppCompatActivity {
    private TextView login_button, privacy_policy;
    private EditText email, password, confirm_password, name;
    private Button register;
    private SessionManager session;
    private AlertDialog progressDialog;
    private PrivacyPolicyDialog dialog;
    private String user_email, user_password, user_confirm_password, user_name;
    private boolean validate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // For changing the status bar color
        // while creating/opening an activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(ContextCompat.getColor(RegisterActivity.this, R.color.purple));// set status background purple
        }

        //Initialize Views
        initViews();

        // Session manager
        session = new SessionManager(this);

        //Setting Privacy Policy & ToS
        dialog = new PrivacyPolicyDialog(this, "https://www.jbstudyapi.techvkt.com/terms-and-conditions", "https://www.jbstudyapi.techvkt.com/privacy-policy");

        //Register User clickListener
        register.setOnClickListener(v -> {

            user_email = email.getText().toString();
            user_password = password.getText().toString();
            user_confirm_password = confirm_password.getText().toString();
            user_name = name.getText().toString();

            //validating email and password
            if (editTextValidation(user_email, user_password, user_confirm_password) && emailValidate(user_email) && passwordValidate(user_password, user_confirm_password)) {
                validate = true;
                dialog.show();
            }
        });

        //Login User clickListener
        login_button.setOnClickListener(v -> {

            //Take him to LoginActivity
            Intent register_to_login_intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(register_to_login_intent);
            finish();
        });

        //Privacy Policy clickListener
        privacy_policy.setOnClickListener(v -> {
            validate = false;
            dialog.show();
        });

        //Adding Privacy Policy Lines
        dialog.addPoliceLine("This application requires Internet Access and must collect the following information: Device Storage, Unique Installation Id, Version of the Application, Time Zone and Information about the language of the device.");
        dialog.addPoliceLine("This application sends error reports, installation and send it to a server of the Fabric.io company to analyze and process it.");
        dialog.addPoliceLine("This application uses a YouTube Data API for getting content videos from Youtube Server.");
        dialog.addPoliceLine("All details about the use of data are available in our Privacy Policies, as well as all Terms of Service links below.");

        // Customizing Policy Dialog
        dialog.setTitleTextColor(Color.parseColor("#222222"));
        dialog.setAcceptButtonColor(ContextCompat.getColor(this, R.color.pink));

        // Policy Dialog Title
        dialog.setTitle("Terms of Service & Privacy Policy");

        //Policy Dialog clickListener
        dialog.setOnClickListener(new PrivacyPolicyDialog.OnClickListener() {

            //if Accepted
            @Override
            public void onAccept(boolean b) {
                if (validate) {
                    //Call register func
                    registerUser(user_name, user_email, user_password);
                } else {
                    Toast.makeText(RegisterActivity.this, "Policy Accepted", Toast.LENGTH_SHORT).show();
                }
            }

            //if Rejected
            @Override
            public void onCancel() {
                Toast.makeText(RegisterActivity.this, "Please Accept Terms of Services & Privacy Policy", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**Password Validation*/
    private boolean passwordValidate(String user_password, String user_confirm_password) {
        if (!(user_password.equals(user_confirm_password))) {
            confirm_password.setError("Passwords Not Matched");
            return false;
        }
        return true;
    }

    /**Email Validation*/
    private boolean emailValidate(String user_email) {
        if (!(Patterns.EMAIL_ADDRESS.matcher(user_email).matches())) {
            email.setError("Please Enter Valid Email");
            return false;
        }
        return true;
    }

    /**EditTexts Validation*/
    private boolean editTextValidation(String user_email, String user_password, String user_confirm_password) {
        if (TextUtils.isEmpty(user_email) && TextUtils.isEmpty(user_password) && TextUtils.isEmpty(user_confirm_password)) {
            email.setError("Please Enter Email");
            email.requestFocus();
            password.setError("Please Enter Password");
            password.requestFocus();
            confirm_password.setError("Please Confirm Password");
            confirm_password.requestFocus();
        } else if (TextUtils.isEmpty(user_password) && TextUtils.isEmpty(user_confirm_password)) {
            password.setError("Please Enter Password");
            password.requestFocus();
            confirm_password.setError("Please Confirm Password");
            confirm_password.requestFocus();
        } else if (TextUtils.isEmpty(user_email) && TextUtils.isEmpty(user_confirm_password)) {
            email.setError("Please Enter Email");
            email.requestFocus();
            confirm_password.setError("Please Confirm Password");
            confirm_password.requestFocus();
        } else if (TextUtils.isEmpty(user_email) && TextUtils.isEmpty(user_password)) {
            email.setError("Please Enter Email");
            email.requestFocus();
            password.setError("Please Enter Password");
            password.requestFocus();
        } else if (TextUtils.isEmpty(user_email)) {
            email.setError("Please Enter Email");
            email.requestFocus();
        } else if (TextUtils.isEmpty(user_password)) {
            password.setError("Please Enter password");
            password.requestFocus();
        } else if (TextUtils.isEmpty(user_confirm_password)) {
            confirm_password.setError("Please Confirm Password");
            confirm_password.requestFocus();
        } else {
            return true;
        }
        return false;
    }

    /**Initializing Views*/
    private void initViews() {
        login_button = findViewById(R.id.signin_now_text);
        register = findViewById(R.id.register_button);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.confirm_password);
        privacy_policy = findViewById(R.id.privacy_policy_text);
    }

    /**Register API Method Function*/
    private void registerUser(String name, String user_email, String user_password) {

        //Starting Dialog
        progressDialog = new SpotsDialog(this, R.style.RegisterDialog);
        progressDialog.setCancelable(false);
        showDialog();

        //Initiating API Interface
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        //getting API Call
        @SuppressLint("HardwareIds") Call<String> call = api.getUserRegister(name, user_email, user_password, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

        //API Call Response
        call.enqueue(new Callback<String>() {

            //On Success Response
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    //getting Response Body
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            //Parsing the JSON
                            parseRegData(jsonResponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            hideDialog();
                        }

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(RegisterActivity.this, "Nothing returned", Toast.LENGTH_LONG).show();
                        hideDialog();
                    }
                } else if (response.errorBody() != null) {
                    //On Error Body Response(email error)
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        Toast.makeText(RegisterActivity.this, jsonObject.getJSONObject("errors").getJSONArray("email").get(0).toString(), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    }

                }
            }

            //On Failure Response
            @Override
            public void onFailure(@NonNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                hideDialog();
            }
        });
    }

    /** Parsing the JSON Response*/
    private void parseRegData(String response) throws JSONException {

        JSONObject jsonObject = new JSONObject(response);
        JSONObject object = (JSONObject) jsonObject.get("user");

        //Save Current Session
        saveInfo(object);
        hideDialog();
        Toast.makeText(RegisterActivity.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
    }

    /**Getting and Saving Current User*/
    private void saveInfo(JSONObject user) {
        session.setLogin(true);
        try {
            //saving current user email in Shared Preferences
            getSharedPreferences("SHARED_PREFS", MODE_PRIVATE).edit().putString("email", user.getString("email")).apply();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            hideDialog();
        }
    }

    /**Showing Dialog*/
    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    /**Hide Dialog*/
    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}