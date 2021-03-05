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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.jb.study.point.MainActivity;
import com.jb.study.point.R;
import com.jb.study.point.helper.SessionManager;

import net.khirr.android.privacypolicy.PrivacyPolicyDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView register_button, privacy_policy;
    private EditText email, password;
    private Button login;
    private SessionManager session;
    private AlertDialog progressDialog;
    private PrivacyPolicyDialog dialog;
    private boolean validate = false;
    private String user_email, user_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(ContextCompat.getColor(LoginActivity.this,R.color.purple));// set status background white
        }

        //initialize Views
        initViews();

        //getting current session
        session = new SessionManager(this);
        dialog = new PrivacyPolicyDialog(this,"https://www.jbstudyapi.techvkt.com/terms-and-conditions","https://www.jbstudyapi.techvkt.com/privacy-policy");

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                user_email = email.getText().toString();
                user_password = password.getText().toString();

                if (editTextValidation(user_email, user_password) && emailValidate(user_email)) {
                    validate = true;
                    dialog.show();
                }

            }
        });

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login_to_register_intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(login_to_register_intent);
                finish();
            }
        });

        privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate = false;
                dialog.show();
            }
        });

        dialog.addPoliceLine("This application requires Internet Access and must collect the following information: Device Storage, Unique Installation Id, Version of the Application, Time Zone and Information about the language of the device.");
        dialog.addPoliceLine("This application sends error reports, installation and send it to a server of the Fabric.io company to analyze and process it.");
        dialog.addPoliceLine("This application uses a YouTube Data API for getting content videos from Youtube Server.");
        dialog.addPoliceLine("All details about the use of data are available in our Privacy Policies, as well as all Terms of Service links below.");

        //  Customizing (Optional)
        dialog.setTitleTextColor(Color.parseColor("#222222"));
        dialog.setAcceptButtonColor(ContextCompat.getColor(this, R.color.pink));

        //  Title
        dialog.setTitle("Terms of Service & Privacy Policy");

        dialog.setOnClickListener(new PrivacyPolicyDialog.OnClickListener() {
            @Override
            public void onAccept(boolean b) {
                if (validate) {
                    loginUser(user_email,user_password);
                } else{
                    Toast.makeText(LoginActivity.this, "Policy Accepted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Please Accept Terms of Services & Privacy Policy", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean emailValidate(String user_email) {
        if (!(Patterns.EMAIL_ADDRESS.matcher(user_email).matches())) {
            email.setError("Please Enter Valid Email");
            email.requestFocus();
            return false;
        }
        return true;
    }

    private boolean editTextValidation(String user_email, String user_password) {
        if (TextUtils.isEmpty(user_email) && TextUtils.isEmpty(user_password)) {
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
        } else {
            return true;
        }
        return false;
    }

    private void initViews() {
        register_button = findViewById(R.id.register_now_text);
        login = findViewById(R.id.login_button);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        privacy_policy = findViewById(R.id.privacy_policy_text);
    }

    private void loginUser(String user_email, String user_password) {
        progressDialog = new SpotsDialog(this,R.style.LoginDialog);
        progressDialog.setCancelable(false);
        showDialog();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        @SuppressLint("HardwareIds") Call<String> call = api.getUserLogin(user_email,user_password, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonresponse = response.body();
                        try {
                             parseRegData(jsonresponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            hideDialog();
                        }

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(LoginActivity.this,"Nothing returned",Toast.LENGTH_LONG).show();
                        hideDialog();
                    }
                } else if(response.errorBody()!=null){
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        Toast.makeText(LoginActivity.this, jsonObject.getJSONObject("errors").getJSONArray("email").get(0).toString(), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    }

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                hideDialog();
            }
        });

    }

    private void parseRegData(String response) throws JSONException {

        JSONObject jsonObject = new JSONObject(response);
        JSONObject object = (JSONObject) jsonObject.get("user");

        saveEmail(object);
        hideDialog();
        Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
    }

    private void saveEmail(JSONObject user){

        session.setLogin(true);
        try{
            getSharedPreferences("SHARED_PREFS",MODE_PRIVATE).edit().putString("email",user.getString("email")).apply();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            hideDialog();
        }
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

}