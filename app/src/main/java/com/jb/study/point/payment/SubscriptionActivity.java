package com.jb.study.point.payment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.jb.study.point.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class SubscriptionActivity extends AppCompatActivity {

    private Button renew_subscription;
    private ImageView back, status_icon;
    private CircleImageView profile_pic;
    private TextView name, email, subscription, validity, subscription_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = getWindow().getDecorView();
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            this.getWindow().setStatusBarColor(Color.WHITE);
        }

        initViews();

        settingsValues();

        renew_subscription.setOnClickListener(v -> {
            startActivity(new Intent(SubscriptionActivity.this, PaymentActivity.class));
            finish();
        });

        back.setOnClickListener(v -> finish());
    }

    private void settingsValues() {
        //user = SharedPrefManager.getInstance(this).getUser();
        SharedPreferences sharedPreferences = getSharedPreferences("USER_DETAILS",MODE_PRIVATE);
        name.setText(sharedPreferences.getString("name",""));
        Glide.with(this)
                .load(sharedPreferences.getString("photo",""))
                .centerCrop()
                .placeholder(R.drawable.circle_cropped)
                .into(profile_pic);
        email.setText(sharedPreferences.getString("email",""));
        subscription.setText(sharedPreferences.getString("subscription",""));
        validity.setText(sharedPreferences.getString("validity",""));
        if (sharedPreferences.getString("subscription","").equals("active")) {
            subscription_status.setText(R.string.active);
            status_icon.setImageResource(R.drawable.ic_baseline_check_circle_24);
        }
    }

    private void initViews() {
        renew_subscription = findViewById(R.id.renew_subscription);
        back = findViewById(R.id.back_subscription_button);
        profile_pic = findViewById(R.id.profile_pic);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        subscription = findViewById(R.id.subscription_status_text);
        validity = findViewById(R.id.validity);
        subscription_status = findViewById(R.id.subscription_status);
        status_icon = findViewById(R.id.status_icon);
    }

}