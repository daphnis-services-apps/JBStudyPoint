package com.jb.study.point.payment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.jb.study.point.ProfileActivity;
import com.jb.study.point.R;
import com.jb.study.point.authentication.UserInterface;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

public class PaymentActivity extends AppCompatActivity {
    final int UPI_PAYMENT = 0;

    private Button get_subscription, send_receipt;
    private ImageView back,  status_icon;
    private CircleImageView user_profile;
    private TextView user_name, subscription_status;
    private AlertDialog progressDialog;

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable();
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = getWindow().getDecorView();
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            this.getWindow().setStatusBarColor(Color.WHITE);
        }

        initViews();

        settingsValues();
    }

    private void settingsValues() {
        SharedPreferences sharedPreferences = getSharedPreferences("USER_DETAILS",MODE_PRIVATE);
        user_name.setText(sharedPreferences.getString("name",""));
        Glide.with(this)
                .load(sharedPreferences.getString("photo",""))
                .centerCrop()
                .placeholder(R.drawable.circle_cropped)
                .into(user_profile);
        if (sharedPreferences.getString("subscription","").equals("active")) {
            subscription_status.setText(R.string.active);
            status_icon.setImageResource(R.drawable.ic_baseline_check_circle_24);
            get_subscription.setText(R.string.view_subscription);
            send_receipt.setVisibility(View.GONE);
        }
        hideDialog();
    }

    private void updateSubscription() {
        showDialog();

        SharedPreferences sharedPreferences = getSharedPreferences("USER_DETAILS", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        String name = sharedPreferences.getString("name", "");
        String dob = sharedPreferences.getString("dob", "");
        String gender = sharedPreferences.getString("gender", "");
        String subscription = "active";
        String validity = "31-May-2021";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        MultipartBody.Part part = null;

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
                            Toast.makeText(PaymentActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            hideDialog();
                        }

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(PaymentActivity.this, "Nothing returned", Toast.LENGTH_LONG).show();
                        hideDialog();
                    }
                } else if (response.errorBody() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        Toast.makeText(PaymentActivity.this, jsonObject.getJSONObject("errors").getJSONArray("email").get(0).toString(), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(PaymentActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    }

                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(PaymentActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("HardwareIds")
    private void saveInfo(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        JSONObject jsonArray = jsonObject.getJSONObject("UpdateDetails");
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
            settingsValues();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            hideDialog();
        }

    }

    private void initViews() {
        get_subscription = findViewById(R.id.get_subscription);
        send_receipt = findViewById(R.id.send_receipt);
        back = findViewById(R.id.back_payment_button);
        user_name = findViewById(R.id.user_name);
        user_profile = findViewById(R.id.user_photo);
        subscription_status = findViewById(R.id.subscription_status);
        status_icon = findViewById(R.id.status_icon);

        progressDialog = new SpotsDialog(this, R.style.SaveDialog);
        progressDialog.setCancelable(false);
    }

    void payUsingUpi(String amount, String upiId, String name, String note) {

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("mc", "")
                .appendQueryParameter("tr", "1458527441954")
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();


        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        // check if intent resolves
        if (null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(PaymentActivity.this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT) {
            if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                if (data != null) {
                    String trxt = data.getStringExtra("response");
                    Log.d("UPI", "onActivityResult: " + trxt);
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add(trxt);
                    upiPaymentDataOperation(dataList);
                } else {
                    Log.d("UPI", "onActivityResult: " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
            } else {
                Log.d("UPI", "onActivityResult: " + "Return data is null"); //when user simply back without payment
                ArrayList<String> dataList = new ArrayList<>();
                dataList.add("nothing");
                upiPaymentDataOperation(dataList);
            }
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(PaymentActivity.this)) {
            String str = data.get(0);
            Log.d("UPIPAY", "upiPaymentDataOperation: " + str);
            String paymentCancel = "";
            if (str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String[] response = str.split("&");
            for (String s : response) {
                String[] equalStr = s.split("=");
                if (equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    } else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                } else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }

            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(PaymentActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: " + approvalRefNo);
                updateSubscription();
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(PaymentActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PaymentActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(PaymentActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_payment_button:
                finish();
                break;

            case R.id.get_subscription: {
                if (getSharedPreferences("USER_DETAILS",MODE_PRIVATE).getString("subscription","").equals("active")) {
                    startActivity(new Intent(PaymentActivity.this, SubscriptionActivity.class));
                    finish();
                } else {
                    payUsingUpi("1", "8755475312@okbizaxis", "JB STUDY POINT", "JB Study Point Subscription");
                }
            }
            break;

            case R.id.send_receipt: {
                final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                startActivity(intent);
            }
            break;

            default:
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                break;
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