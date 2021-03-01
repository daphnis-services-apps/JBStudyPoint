package com.jb.study.point;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.jb.study.point.authentication.UserInterface;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ProfileActivity extends AppCompatActivity {
    private static final int SELECT_IMAGE = 100;
    private static final int REQUEST_CODE = 400; // can be set Any Integer you Want
    String[] permissions;
    private ImageView back, edit_name_button, edit_dob_button, change_password;
    private RadioGroup gender;
    private CircleImageView profile_pic;
    private Button save_details;
    private TextView name, email, dob;
    private EditText edit_name, edit_gender, edit_dob;
    private String filepath;
    private AlertDialog progressDialog;
    private Calendar myCalendar;
    private DatePickerDialog.OnDateSetListener date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = getWindow().getDecorView();
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            this.getWindow().setStatusBarColor(Color.WHITE);
        }

        initViews();

        settingsValues();

        permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }; // initialize Permission Array

        if (!checkPermissions()) { // checking if permission is Already Granted or Not
            requestPermission(); // if Not Granted, Asking for Permissions (Allow or Deny)
        }

        save_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDetails();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "dd/MM/yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                dob.setText(sdf.format(myCalendar.getTime()));

            }

        };
    }

    private boolean checkPermissions() {
        boolean checkPermission = false;
        boolean finalPermisiion = true;

        for (int i = 0; i < permissions.length; i++) {
            checkPermission = ContextCompat.checkSelfPermission(this,
                    permissions[i]) == (PackageManager.PERMISSION_GRANTED); //checking for each Permission is granted or not
            if (!checkPermission) { // if any of the permission is not Granted then finalPermisiion will be set to false
                finalPermisiion = false;
            }
        }
        return finalPermisiion;
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
    }

    private void settingsValues() {
        SharedPreferences sharedPreferences = getSharedPreferences("USER_DETAILS", MODE_PRIVATE);
        name.setText(sharedPreferences.getString("name", ""));
        email.setText(sharedPreferences.getString("email", ""));
        //gender.setText(sharedPreferences.getString("gender", ""));
        dob.setText(sharedPreferences.getString("dob", ""));
        Glide.with(this)
                .load(sharedPreferences.getString("photo", ""))
                .centerCrop()
                .placeholder(R.drawable.circle_cropped)
                .into(profile_pic);
        hideDialog();
    }

    private void initViews() {
        back = findViewById(R.id.back_profile_button);
        profile_pic = findViewById(R.id.edit_profile_pic);
        edit_name_button = findViewById(R.id.name_edit_button);
        edit_dob_button = findViewById(R.id.dob_edit_button);
        change_password = findViewById(R.id.change_password_button);
        save_details = findViewById(R.id.save_details);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        dob = findViewById(R.id.dob);
        edit_name = findViewById(R.id.name_edit);
        gender = findViewById(R.id.genderRadioGroup);

        progressDialog = new SpotsDialog(this, R.style.SaveDialog);
        progressDialog.setCancelable(false);

        myCalendar = Calendar.getInstance();

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_profile_button:
                finish();
                break;

            case R.id.edit_profile_pic:
                Intent imageChooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
                imageChooserIntent.setType("image/*");
                startActivityForResult(imageChooserIntent, SELECT_IMAGE);
                break;

            case R.id.name_edit_button:
                name.setVisibility(View.GONE);
                edit_name.setVisibility(View.VISIBLE);
                edit_name.setText(name.getText().toString());
                edit_name.requestFocus();
                InputMethodManager inputMethodManager =
                        (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(
                        edit_name.getApplicationWindowToken(),
                        InputMethodManager.SHOW_FORCED, 0);
                break;

            case R.id.dob_edit_button:

                new DatePickerDialog(ProfileActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                break;

            case R.id.change_password_button:
                break;

            default:
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void saveDetails() {
        showDialog();

        String email = this.email.getText().toString();
        String name = edit_name.getText().toString().equals("") ? this.name.getText().toString() : edit_name.getText().toString();
        String dob = this.dob.getText().toString();
        String gender = this.gender.getCheckedRadioButtonId() == R.id.male ? "Male" : "Female";

        SharedPreferences sharedPreferences = getSharedPreferences("USER_DETAILS", MODE_PRIVATE);
        String subscription = sharedPreferences.getString("subscription", "");
        String validity = sharedPreferences.getString("validity", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        MultipartBody.Part part = null;

        if(filepath!=null) {
            File file = new File(filepath);
            // Create a request body with file and image media type
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
            // Create MultipartBody.Part using file request-body,file name and part name
            part = MultipartBody.Part.createFormData("photo", file.getName(), fileReqBody);
        }
        //Create request body with text description and text media type
        RequestBody nameUpdate = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody subscriptionUpdate = RequestBody.create(MediaType.parse("text/plain"), subscription);
        RequestBody genderUpdate = RequestBody.create(MediaType.parse("text/plain"), gender);
        RequestBody dobUpdate = RequestBody.create(MediaType.parse("text/plain"), dob);
        RequestBody validityUpdate = RequestBody.create(MediaType.parse("text/plain"), validity);
        RequestBody emailUpdate = RequestBody.create(MediaType.parse("text/plain"), email);
        //
        Call<String> call = api.getUpdatedUser(part, nameUpdate, subscriptionUpdate, genderUpdate, dobUpdate, validityUpdate, emailUpdate);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonresponse = response.body();
                        try {
                            saveInfo(jsonresponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ProfileActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            hideDialog();
                        }

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(ProfileActivity.this, "Nothing returned", Toast.LENGTH_LONG).show();
                        hideDialog();
                    }
                } else if (response.errorBody() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        Toast.makeText(ProfileActivity.this, jsonObject.getJSONObject("errors").getJSONArray("email").get(0).toString(), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ProfileActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    }

                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(ProfileActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                Toast.makeText(ProfileActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("HardwareIds")
    private void saveInfo(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        JSONObject jsonArray = jsonObject.getJSONObject("userDetails");
        try {
            getSharedPreferences("USER_DETAILS", MODE_PRIVATE).edit()
                    .putString("name", jsonArray.getString("name"))
                    .putString("email", jsonArray.getString("email"))
                    .putString("gender", jsonArray.getString("gender"))
                    .putString("subscription", jsonArray.getString("subscription"))
                    .putString("validity", jsonArray.getString("validity"))
                    .putString("photo", jsonArray.getString("photo"))
                    .putString("dob", jsonArray.getString("dob"))
                    .putString("device_token", jsonArray.getString("device_token"))
                    .apply();
            Toast.makeText(this, "Details Updated", Toast.LENGTH_SHORT).show();
            name.setText(jsonArray.getString("name"));
            name.setVisibility(View.VISIBLE);
            edit_name.setVisibility(View.GONE);
            settingsValues();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            hideDialog();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);

        // Request is image selection
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == RESULT_OK) {
                // Retrieve selected image
                Uri selectedImage = returnedIntent.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filepath = cursor.getString(columnIndex);
                profile_pic.setImageURI(selectedImage);

            }
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

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean checkPermission = false;
                boolean finalPermisiion = true;
                for (int i = 0; i < grantResults.length; i++) {
                    checkPermission = grantResults[i] == PackageManager.PERMISSION_GRANTED; //checking for each Permission is successfully granted or not
                    if(!checkPermission){ // if any of the permission is denied then finalPermisiion will be set to false
                        finalPermisiion = false;
                    }
                }
                if (finalPermisiion) {
                    //do your work
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}