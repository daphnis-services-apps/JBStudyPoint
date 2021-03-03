package com.jb.study.point.payment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.jb.study.point.R;
import com.jb.study.point.authentication.UserInterface;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    private ImageView back, status_icon;
    private CircleImageView user_profile;
    private TextView user_name, subscription_status;
    private AlertDialog progressDialog;
    private String setAmount = "0";
    private Dialog dialog;

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
        SharedPreferences sharedPreferences = getSharedPreferences("USER_DETAILS", MODE_PRIVATE);
        user_name.setText(sharedPreferences.getString("name", ""));
        Glide.with(this)
                .load(sharedPreferences.getString("photo", ""))
                .centerCrop()
                .placeholder(R.drawable.circle_cropped)
                .into(user_profile);
        if (sharedPreferences.getString("subscription", "").equals("active")) {
            subscription_status.setText(R.string.active);
            status_icon.setImageResource(R.drawable.ic_baseline_check_circle_24);
            get_subscription.setText(R.string.view_subscription);
            send_receipt.setVisibility(View.GONE);
        }
        hideDialog();
    }

    private void updateSubscription(String status, String txnId) {
        showDialog();

        SharedPreferences sharedPreferences = getSharedPreferences("USER_DETAILS", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        String subscription;
        String validity;
        if(status.equals("success")) {
            subscription = "active";
            validity = "31-May-2021";
        } else {
            subscription = "pending from Bank status";
            validity = "N/A";
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        MultipartBody.Part part = null;

        //Create request body with text description and text media type
        RequestBody paymentStatus = RequestBody.create(MediaType.parse("text/plain"), status);
        RequestBody subscriptionUpdate = RequestBody.create(MediaType.parse("text/plain"), subscription);
        RequestBody approvalNo = RequestBody.create(MediaType.parse("text/plain"), txnId);
        @SuppressLint("SimpleDateFormat") RequestBody paymentDate = RequestBody.create(MediaType.parse("text/plain"), new SimpleDateFormat("dd-MMM-yyyy").format(new Date()));
        Date date = new Date();
        if (setAmount.equals("1500"))
            date.setMonth(date.getMonth() % 12 + 1);
        @SuppressLint("SimpleDateFormat") RequestBody validityUpdate = RequestBody.create(MediaType.parse("text/plain"), setAmount.equals("1500") && status.equals("success") ? new SimpleDateFormat("dd-MMM-yyyy").format(date) : validity);
        RequestBody emailUpdate = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody paymentHistory = RequestBody.create(MediaType.parse("text/plain"), "yes_"+setAmount);
        RequestBody paymentAmount = RequestBody.create(MediaType.parse("text/plain"), setAmount);
        //
        Call<String> call = api.getSubscriptionUpdatedUser(part, paymentStatus, subscriptionUpdate, approvalNo, paymentDate, validityUpdate, emailUpdate, paymentHistory, paymentAmount);
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
                        Toast.makeText(PaymentActivity.this, jsonObject.toString(), Toast.LENGTH_LONG).show();
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

        dialog = new Dialog(this);
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
                    String text = data.getStringExtra("response");
                    Log.d("UPI", "onActivityResult: " + text);
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add(text);
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
            String txnId = "";
            String[] response = str.split("&");
            for (String s : response) {
                String[] equalStr = s.split("=");
                if (equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    } else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    } else if(equalStr[0].toLowerCase().equals("txnId".toLowerCase())) {
                        txnId = equalStr[1];
                    }
                } else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }

            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(PaymentActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: " + approvalRefNo);
                updateSubscription(status, txnId);
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(PaymentActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            } else if (status.equals("pending")){
                Toast.makeText(PaymentActivity.this, "Transaction Pending. Please wait 48 hours for Payment update. If payment not updated kindly contact us.", Toast.LENGTH_LONG).show();
                updateSubscription(status, txnId);
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
                if (getSharedPreferences("USER_DETAILS", MODE_PRIVATE).getString("subscription", "").equals("active")) {
                    startActivity(new Intent(PaymentActivity.this, SubscriptionActivity.class));
                    finish();
                } else {
                    getSubscription();
                }
            }
            break;

            case R.id.send_receipt: {
                sendReceipt();
            }
            break;

            default:
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void getSubscription() {
        dialog.setContentView(R.layout.activity_subscription_pager);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.show();

        RelativeLayout days30 = dialog.findViewById(R.id.days30);
        RelativeLayout days90 = dialog.findViewById(R.id.days90);
        RelativeLayout days90Renew = dialog.findViewById(R.id.days90Renew);
        LinearLayout paymentLayout = dialog.findViewById(R.id.payment_layout);
        ImageView getSubscription = dialog.findViewById(R.id.get_subscription);

        if(getSharedPreferences("USER_DETAILS",MODE_PRIVATE).getString("payment_history","").equals("yes_1500")){
            paymentLayout.setVisibility(View.GONE);
            days90Renew.setVisibility(View.VISIBLE);
        }

        days30.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                days30.setBackground(getResources().getDrawable(R.drawable.button_white_selected));
                days90.setBackground(getResources().getDrawable(R.drawable.button_white));
                setAmount = "1500";
            }
        });

        days90.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                days90.setBackground(getResources().getDrawable(R.drawable.button_white_selected));
                days30.setBackground(getResources().getDrawable(R.drawable.button_white));
                setAmount = "3000";
            }
        });

        days90Renew.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                days90Renew.setBackground(getResources().getDrawable(R.drawable.button_white_selected));
                setAmount = "1500";
            }
        });

        getSubscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setAmount.equals("0"))
                    Toast.makeText(PaymentActivity.this, "Please choose one of the Package", Toast.LENGTH_SHORT).show();
                else
                    payUsingUpi(setAmount, "8755475312@okbizaxis", "JB STUDY POINT", "JB Study Point Subscription");
            }
        });
    }

    private void sendReceipt() {
        String subject = "Subscription Payment Receipt";
        String message = "Hello, Sir. My name is " + getSharedPreferences("USER_DETAILS", MODE_PRIVATE).getString("name", "") + ". I purchase the premium membership and here is the payment receipt. Please check the Attachments in this email";
        Intent intent = new Intent(Intent.ACTION_SEND);
        String[] strTo = {"jbstudypoint2020@gamil.com"};
        intent.putExtra(Intent.EXTRA_EMAIL, strTo);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setType("text/email");
        intent.setPackage("com.google.android.gm");
        startActivity(intent);
    }

    private void showDialog() {
        if(!dialog.isShowing())
            dialog.show();

        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if(dialog.isShowing())
            dialog.hide();

        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}